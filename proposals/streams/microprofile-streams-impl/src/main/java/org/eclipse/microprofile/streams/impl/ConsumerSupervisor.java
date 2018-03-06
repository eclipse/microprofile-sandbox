package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.pattern.PatternsCS;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

class ConsumerSupervisor<T> extends AbstractActor {
  private final Materializer materializer;
  private final ConsumerSettings<String, Object> consumerSettings;
  private final StreamDescriptor.FlowIncomingDescriptor<T> descriptor;
  private final T instance;
  private Consumer.Control control;
  private Function<Ack, CompletionStage<Void>> ackFunction;

  public ConsumerSupervisor(Materializer materializer, ConsumerSettings<String, Object> consumerSettings,
      StreamDescriptor.FlowIncomingDescriptor<T> descriptor, T instance) {
    this.materializer = materializer;
    this.consumerSettings = consumerSettings;
    this.descriptor = descriptor;
    this.instance = instance;
  }

  @Override
  public void preStart() throws Exception {
    akka.stream.javadsl.Flow<Envelope<Object>, Ack, Function<Ack, CompletionStage<Void>>> subscriber =
        (akka.stream.javadsl.Flow) descriptor.incomingFlow(instance);

    CompletionStage<Done> complete = Consumer.committableSource(consumerSettings, Subscriptions.topics(descriptor.incomingTopic()))
        .<Envelope<Object>>map(message -> new KafkaEnvelope<>(message, ackFunction))
        .viaMat(subscriber, (control, ack) -> {
          this.control = control;
          this.ackFunction = ack;
          return NotUsed.getInstance();
        }).mapAsync(1, ack -> {
          if (ack instanceof KafkaAck) {
            return ((KafkaAck) ack).getMessage()
                .committableOffset().commitJavadsl().thenApply(d -> Done.getInstance());
          } else {
            throw new IllegalArgumentException("Don't know how to handle ack of type " + ack.getClass() +
                ". Kafka consumers must only emit acks returned by the passed in Envelope.");
          }
        })
        .runWith(Sink.ignore(), materializer);

    PatternsCS.pipe(complete, context().dispatcher()).pipeTo(self(), self());
  }

  @Override
  public void postStop() throws Exception {
    control.stop();
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Status.Failure.class, failure -> {

          if (failure.cause() instanceof Exception) {
            throw (Exception) failure.cause();
          } else {
            throw new Exception(failure.cause());
          }

        }).match(Done.class, done -> {

          context().stop(self());

        }).build();
  }

  private static class KafkaEnvelope<T> implements Envelope<T> {
    private final ConsumerMessage.CommittableMessage<?, T> message;
    private final Function<Ack, CompletionStage<Void>> ackFunction;

    public KafkaEnvelope(ConsumerMessage.CommittableMessage<?, T> message,
        Function<Ack, CompletionStage<Void>> ackFunction) {
      this.message = message;
      this.ackFunction = ackFunction;
    }

    @Override
    public T getPayload() {
      return message.record().value();
    }

    @Override
    public CompletionStage<Void> ack() {
      return ackFunction.apply(getAck());
    }

    @Override
    public Ack getAck() {
      return new KafkaAck(message);
    }
  }

  private static class KafkaAck implements Ack {

    private final ConsumerMessage.CommittableMessage<?, ?> message;

    public KafkaAck(ConsumerMessage.CommittableMessage<?, ?> message) {
      this.message = message;
    }

    public ConsumerMessage.CommittableMessage<?, ?> getMessage() {
      return message;
    }
  }
}
