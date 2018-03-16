package org.eclipse.microprofile.streams;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
   *
   * If th
   */
  default CompletionStage<Void> ack() {
    return CompletableFuture.completedFuture(null);
  }

  /**
   * Get the ACK signal for this message.
   *
   * This should be emitted from a processor processing messages.
   */
  default Ack getAck() {
    return Ack.UNCORRELATED;
  }

  /**
   * Get the message offset, if the underlying broker supports offset based messaging.
   */
  default Optional<Long> offset() {
    return Optional.empty();
  }

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
    };
  }
}
