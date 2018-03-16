package org.eclipse.microprofile.streams.impl;

import org.eclipse.microprofile.streams.*;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;

@ApplicationScoped
public class InjectedStreamsProducers {

  private final StreamManager streamManager;

  @Inject
  public InjectedStreamsProducers(StreamManager streamManager) {
    this.streamManager = streamManager;
  }

  @InjectedStream
  @Produces
  public <T> OutgoingStream<T> produceOutgoingStream(InjectionPoint injectionPoint) {
    Outgoing outgoing = injectionPoint.getAnnotated().getAnnotation(Outgoing.class);
    if (outgoing == null) {
      // Perhaps this validation should be done earlier by an observes method on the extension?
      throw new DefinitionException("Cannot provide OutgoingStream for injection point (" + injectionPoint +
          ") without @Outgoing annotation.");
    }

    Type outType = ReflectionUtils.getTypeArguments(injectionPoint.getType(), () ->
        "Injection point (" + injectionPoint + ")")[0];

    if (Reflections.getRawType(outType).equals(Envelope.class)) {
      Type actualType = ReflectionUtils.getTypeArguments(outType, () ->
          "Enveloped injection point (" + injectionPoint + ")")[0];

      return (OutgoingStream) streamManager.createUnmanagedOutgoingStream(actualType, outgoing.topic());
    } else {
      return streamManager.<T>createUnmanagedOutgoingStream(outType, outgoing.topic()).unwrap();
    }
  }

  @InjectedStream
  @Produces
  public <T> IncomingStream<T> produceIncomingStream(InjectionPoint injectionPoint) {
    Incoming incoming = injectionPoint.getAnnotated().getAnnotation(Incoming.class);

    if (incoming == null) {
      // Perhaps this validation should be done earlier by an observes method on the extension?
      throw new DefinitionException("Cannot provide IncomingStream for injection point (" + injectionPoint +
          ") without @Incoming annotation.");
    } else {
      Type inType = ReflectionUtils.getTypeArguments(injectionPoint.getType(), () ->
          "Injection point (" + injectionPoint + ")")[0];

      if (Reflections.getRawType(inType).equals(Envelope.class)) {
        Type actualType = ReflectionUtils.getTypeArguments(inType, () ->
            "Enveloped injection point (" + injectionPoint + ")")[0];

        return (IncomingStream<T>) streamManager.createUnmanagedIncomingStream(actualType, incoming.topic());
      } else {
        return streamManager.<T>createUnmanagedIncomingStream(inType, incoming.topic()).unwrap();
      }
    }
  }

  @InjectedStream
  @Produces
  public <T> Flow.Publisher<T> produceIncomingStreamPublisher(InjectionPoint injectionPoint) {
    return produceIncomingStream(injectionPoint);
  }
}
