package org.eclipse.microprofile.streams;

/**
 * Represents a message ACK.
 *
 * ACK's can be used to correlate the successful status of handling a message with the message that was handled.
 */
public interface Ack {

  /**
   * An uncorrelated ack.
   *
   * When handling enveloped messages, the Ack from the envelope must be emitted, since the
   * Ack may contain correlation data that indicates to the message broker provider which
   * message is being acked.
   */
  Ack UNCORRELATED = UncorrelatedAck.INSTANCE;
}
