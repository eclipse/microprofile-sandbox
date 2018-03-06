package org.eclipse.microprofile.streams;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * A message envelope.
 *
 * @param <T> The type of the message payload.
 */
public interface Envelope<T> {

  /**
   * The payload for this message.
   */
  T getPayload();

  /**
   * Acknowledge this message.
   */
  CompletionStage<Void> ack();

  /**
   * Get the ACK signal for this message.
   *
   * This should be emitted from a processor processing messages.
   */
  Ack getAck();

  static <T> Envelope<T> ackableEnvelope(T payload, Supplier<CompletionStage<Void>> ack) {
    return new Envelope<T>() {
      @Override
      public T getPayload() {
        return payload;
      }

      @Override
      public CompletionStage<Void> ack() {
        return ack.get();
      }

      @Override
      public Ack getAck() {
        return new Ack() {};
      }
    };
  }
}
