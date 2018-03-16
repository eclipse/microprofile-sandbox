package org.eclipse.microprofile.streams;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * An outgoing stream that can be published to on demand.
 */
public interface OutgoingStream<T> {

  /**
   * Publish a single message.
   *
   * @param message The message to publish.
   * @return A CompletionStage that will be redeemed successfully when the message is published, or with an error
   *         otherwise.
   */
  CompletionStage<Void> publish(T message);

  /**
   * Create a subscriber which will publish all the messages it receives to the outgoing stream.
   * <p/>
   * If this is an enveloped outgoing stream (ie, where T is of type Envelope&lt;M&gt;), then the envelopes will be
   * acked.
   *
   * @return The subscriber.
   */
  Flow.Subscriber<T> subscriber();

  /**
   * Create a processor which will publish all the messages it receives, and emit an Ack for each message that it
   * successfully publishes, in the order that it receives the messages.
   * <p/>
   * If this is an enveloped outgoing stream (ie, where T is of type Envelope&lt;M&gt;), then the acks emitted will be
   * the ones returned from the envelope.
   *
   * @return The processor.
   */
  Flow.Processor<T, Ack> processor();

  /**
   * Create a copy of this stream that publishes to the given topic name.
   */
  OutgoingStream<T> withTopic(String topic);
}
