package org.eclipse.microprofile.streams;

import java.util.concurrent.CompletionStage;

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
}
