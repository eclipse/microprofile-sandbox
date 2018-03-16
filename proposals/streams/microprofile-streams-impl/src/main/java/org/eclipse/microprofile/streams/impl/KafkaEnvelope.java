package org.eclipse.microprofile.streams.impl;

import akka.kafka.ConsumerMessage;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

class KafkaEnvelope<T> implements Envelope<T> {
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

  @Override
  public Optional<Long> offset() {
    return Optional.of(message.committableOffset().partitionOffset().offset());
  }
}
