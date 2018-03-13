package org.eclipse.microprofile.streams;

import java.util.concurrent.Flow;

/**
 * An incoming stream, for unmanaged subscriptions.
 */
public interface IncomingStream<T> extends Flow.Publisher<T> {

  /**
   * Create a copy of this stream that publishes to a topic with a different name.
   */
  IncomingStream<T> withTopic(String topic);

  /**
   * Create a copy of this stream that starts subscriptions from the given offset.
   */
  IncomingStream<T> fromOffset(long offset);
}
