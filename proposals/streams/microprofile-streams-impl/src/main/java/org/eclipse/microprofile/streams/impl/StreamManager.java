package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.*;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.pattern.Backoff;
import akka.pattern.BackoffOptions;
import akka.pattern.PatternsCS;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;
import scala.concurrent.duration.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * This starts subscribers.
 *
 * For those unfamiliar with Akka and Akka streams, essentially what is being done here is an Actor is created that
 * runs the stream and monitors its liveness. If/when the stream crashes, the actor shuts itself down. An exponential
 * backoff supervisor actor is used to restart the actor (and hence the stream) after an exponential backoff (to avoid
 * hammering already failing resources in a tight loop).
 */
@ApplicationScoped
public class StreamManager {

  private final ActorSystem system;
  private final Materializer materializer;
  private final Jsonb jsonb = JsonbBuilder.create();

  @Inject
  public StreamManager(ActorSystem system, Materializer materializer) {
    this.system = system;
    this.materializer = materializer;
  }

  public <T> RunningStream startStream(StreamDescriptor<? super T> descriptor, T t) {
    if (descriptor instanceof StreamDescriptor.FlowIncomingDescriptor) {
      return startStream((StreamDescriptor.FlowIncomingDescriptor<? super T>) descriptor, t);
    } else {
      throw new UnsupportedOperationException("Unknown descriptor: " + descriptor);
    }
  }

  private <T> RunningStream startStream(StreamDescriptor.FlowIncomingDescriptor<? super T> descriptor, T t) {
    ConsumerSettings<String, Object> consumerSettings =
        ConsumerSettings.create(system,
            // todo allow custom deserializers other than jsonb
            new StringDeserializer(), new Deserializer<>() {
              @Override
              public void configure(Map<String, ?> map, boolean b) {
              }

              @Override
              public Object deserialize(String s, byte[] bytes) {
                return jsonb.fromJson(new ByteArrayInputStream(bytes), descriptor.ingressMessageType());
              }

              @Override
              public void close() {
              }
            })
            // todo make configurable
            .withBootstrapServers("localhost:9092")
            // todo provide via annotation
            .withGroupId("group1")
            // todo should also be configurable
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    Props streamSupervisor = Props.create(StreamSupervisor.class,
        () -> new StreamSupervisor<>(materializer, consumerSettings, descriptor, t));

    // todo make exponential backoff parameters configurable
    BackoffOptions options = Backoff.onStop(streamSupervisor, "stream",
        Duration.create(3, TimeUnit.SECONDS),
        Duration.create(30, TimeUnit.SECONDS),
        0.2);

    ActorRef actor = system.actorOf(options.props(), descriptor.id());
    return () -> actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
  }

  private static class StreamSupervisor<T> extends AbstractActor {
    private final Materializer materializer;
    private final ConsumerSettings<String, Object> consumerSettings;
    private final StreamDescriptor.FlowIncomingDescriptor<T> descriptor;
    private final T instance;
    private Consumer.Control control;
    private Function<Ack, CompletionStage<Void>> ackFunction;

    public StreamSupervisor(Materializer materializer, ConsumerSettings<String, Object> consumerSettings,
        StreamDescriptor.FlowIncomingDescriptor<T> descriptor, T instance) {
      this.materializer = materializer;
      this.consumerSettings = consumerSettings;
      this.descriptor = descriptor;
      this.instance = instance;
    }

    @Override
    public void preStart() throws Exception {
      akka.stream.javadsl.Flow<Envelope<Object>, Ack, Function<Ack, CompletionStage<Void>>> subscriber =
          (akka.stream.javadsl.Flow) descriptor.ingressFlow(instance);

      CompletionStage<Done> complete = Consumer.committableSource(consumerSettings, Subscriptions.topics(descriptor.ingressTopic()))
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
