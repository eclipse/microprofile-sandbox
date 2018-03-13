package org.eclipse.microprofile.streams.impl;

import akka.kafka.ConsumerMessage;
import org.eclipse.microprofile.streams.Ack;

class KafkaAck implements Ack {

  private final ConsumerMessage.CommittableMessage<?, ?> message;

  public KafkaAck(ConsumerMessage.CommittableMessage<?, ?> message) {
    this.message = message;
  }

  public ConsumerMessage.CommittableMessage<?, ?> getMessage() {
    return message;
  }
}
