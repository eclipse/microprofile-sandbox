package org.eclipse.microprofile.streams.impl;

import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;

import java.lang.reflect.Type;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface StreamDescriptor<T> {

  interface IncomingDescriptor<T> extends StreamDescriptor<T> {
    String id();
    String ingressTopic();
    Type ingressMessageType();
  }

  interface FlowIncomingDescriptor<T> extends IncomingDescriptor<T> {
    akka.stream.javadsl.Flow<Envelope<?>, Ack, Function<Ack, CompletionStage<Void>>> ingressFlow(T instance) throws Exception;
  }
}
