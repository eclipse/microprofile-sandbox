package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.Pair;
import akka.kafka.*;
import akka.kafka.javadsl.Producer;
import akka.pattern.PatternsCS;
import akka.stream.KillSwitch;
import akka.stream.KillSwitches;
import akka.stream.Materializer;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.streams.Envelope;

import java.util.concurrent.CompletionStage;

public class ProducerSupervisor<T> extends AbstractActor {
  private final Materializer materializer;
  private final ProducerSettings<String, Object> producerSettings;
  private final StreamDescriptor.SourceOutgoingDescriptor<T> descriptor;
  private final T instance;
  private KillSwitch killSwitch;

  public ProducerSupervisor(Materializer materializer, ProducerSettings<String, Object> producerSettings,
      StreamDescriptor.SourceOutgoingDescriptor<T> descriptor, T instance) {
    this.materializer = materializer;
    this.producerSettings = producerSettings;
    this.descriptor = descriptor;
    this.instance = instance;
  }

  @Override
  public void preStart() throws Exception {
    Source<Envelope<Object>, ?> publisher = (Source) descriptor.outgoingSource(instance);

    Pair<UniqueKillSwitch, CompletionStage<Done>> pair = publisher.map(envelope -> new ProducerMessage.Message<>(
        new ProducerRecord<String, Object>(descriptor.outgoingTopic(), envelope.getPayload()),
        envelope
    ))
        .viaMat(KillSwitches.single(), Keep.right())
        .via(Producer.flow(producerSettings))
        .mapAsync(1, result -> result.message().passThrough().ack().thenApply(v -> Done.getInstance()))
        .toMat(Sink.ignore(), Keep.both())
        .run(materializer);

    killSwitch = pair.first();
    PatternsCS.pipe(pair.second(), context().dispatcher()).pipeTo(self(), self());
  }

  @Override
  public void postStop() throws Exception {
    killSwitch.shutdown();
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
}
