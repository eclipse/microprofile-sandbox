package org.eclipse.microprofile.streams;

import java.util.concurrent.CompletionStage;

/**
 * A message envelope.
 *
 * @param <T> The type of the message payload.
 */
public interface Envelope<T> {

  T getPayload();

  CompletionStage<Void> commit();
}
