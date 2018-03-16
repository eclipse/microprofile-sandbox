package org.eclipse.microprofile.streams.impl;

import akka.stream.javadsl.Source;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;

import java.lang.reflect.Type;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface StreamDescriptor<T> {

  String id();

  interface IncomingDescriptor<T> extends StreamDescriptor<T> {
    String incomingTopic();
    Type incomingMessageType();
  }

  interface FlowIncomingDescriptor<T> extends IncomingDescriptor<T> {
    akka.stream.javadsl.Flow<Envelope<?>, Ack, Function<Ack, CompletionStage<Void>>> incomingFlow(T instance) throws Exception;
  }

  interface OutgoingDescriptor<T> extends StreamDescriptor<T> {
    String outgoingTopic();
    Type outgoingMessageType();
  }

  interface SourceOutgoingDescriptor<T> extends OutgoingDescriptor<T> {
    Source<Envelope<?>, ?> outgoingSource(T instance) throws Exception;
  }
}
