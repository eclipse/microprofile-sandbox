package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.stream.*;
import akka.stream.javadsl.*;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.Incoming;
import org.eclipse.microprofile.streams.Outgoing;
import org.jboss.weld.util.reflection.Reflections;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is my first time really writing a CDI extension, so I've probably got a lot wrong.
 */
public class AkkaKafkaStreamsCdiExtension implements Extension {

  public <T> void locateStreams(@Observes ProcessInjectionTarget<T> bean, BeanManager beanManager) {

    // Find all the @Ingest annotated methods, and read them.
    List<StreamDescriptor<? super T>> subscribers = locateStreams(bean.getAnnotatedType());

    if (!subscribers.isEmpty()) {
      // Wrap the injection target in our own one that starts up and shuts down streams.
      bean.setInjectionTarget(new StreamsInjectionTarget<>(bean.getInjectionTarget(), subscribers, beanManager));
    }
  }

  public <T, X> void processInjections(@Observes ProcessInjectionPoint<T, X> injectionPoint) {
    Annotated annotated = injectionPoint.getInjectionPoint().getAnnotated();
    if (annotated.getAnnotation(Incoming.class) != null || annotated.getAnnotation(Outgoing.class) != null) {
      injectionPoint.configureInjectionPoint().addQualifier(new InjectedStreamQualifier());
    }
  }

  public void afterBeanDiscovery(@Observes BeforeBeanDiscovery discovery, BeanManager beanManager) {
    // todo what name should these have?
    //discovery.addAnnotatedType(beanManager.createAnnotatedType(ActorSystemProvider.class), ActorSystemProvider.class.getName());
    //discovery.addAnnotatedType(beanManager.createAnnotatedType(StreamManager.class), StreamManager.class.getName());
  }

  private <T> List<StreamDescriptor<? super T>> locateStreams(AnnotatedType<T> type) {
    List<StreamDescriptor<? super T>> streams = new ArrayList<>();

    for (AnnotatedMethod<? super T> method : type.getMethods()) {
      Incoming incoming = method.getAnnotation(Incoming.class);
      Outgoing outgoing = method.getAnnotation(Outgoing.class);
      if (incoming != null && outgoing != null) {
        // todo handle processor methods
      } else if (incoming != null) {
        streams.add(readSubscriberMethod(incoming, method));
      } else if (outgoing != null) {
        streams.add(readPublisherMethod(outgoing, method));
      }
    }

    return streams;
  }

  /**
   * The M type variable is a lie because that type is completely unknown at the point of invocation of this method,
   * but it does allow us to gain some type-safety below.
   */
  private <T, M> StreamDescriptor.IncomingDescriptor<T> readSubscriberMethod(Incoming incoming, AnnotatedMethod<T> method) {
    // Probably shouldn't use Weld reflections utilities here... but for a PoC maybe it's ok.

    String methodName = method.getJavaMember().getName();
    Type rawReturnType = Reflections.getRawType(method.getBaseType());

    akka.japi.Function<T, akka.stream.javadsl.Flow<?, ?, Function<Ack, CompletionStage<Void>>>> rawStream;
    Type inType;
    Type outType;

    if (rawReturnType == null || rawReturnType.equals(Void.TYPE)) {
      if (method.getParameters().isEmpty()) {
        throw new DefinitionException("Single element incoming method must take a parameter");
      }
      rawStream = instance -> {
        Sink sink = Sink.foreach(element -> method.getJavaMember().invoke(instance, element));
        return akka.stream.javadsl.Flow.fromSinkAndSourceMat(sink, createAckSource(), Keep.right());
      };
      inType = method.getParameters().get(0).getBaseType();
      outType = Ack.class;

      // todo handle synchronous acking without envelopes

    } else if (rawReturnType.equals(Flow.Subscriber.class)) {
      Type[] subscriberTypes = getReturnTypeArguments(method);
      rawStream = instance -> {
        Sink sink = JavaFlowSupport.Sink.fromSubscriber((Flow.Subscriber) method.getJavaMember().invoke(instance));
        return akka.stream.javadsl.Flow.fromSinkAndSourceMat(sink, createAckSource(), Keep.right());
      };
      inType = subscriberTypes[0];
      outType = Ack.class;

    } else if (rawReturnType.equals(Subscriber.class)) {
      Type[] subscriberTypes = getReturnTypeArguments(method);
      rawStream = instance -> {
        Sink sink = Sink.fromSubscriber((Subscriber) method.getJavaMember().invoke(instance));
        return akka.stream.javadsl.Flow.fromSinkAndSourceMat(sink, createAckSource(), Keep.right());
      };
      inType = subscriberTypes[0];
      outType = Ack.class;

    } else if (rawReturnType.equals(Flow.Processor.class)) {
      Type[] processorTypes = getReturnTypeArguments(method);
      rawStream = instance -> JavaFlowSupport.Flow.fromProcessor(() -> (Flow.Processor) method.getJavaMember().invoke(instance))
          .mapMaterializedValue(n -> createErrorCommit("Flow.Processor"));
      inType = processorTypes[0];
      outType = processorTypes[1];

    } else if (rawReturnType.equals(Processor.class)) {
      Type[] processorTypes = getReturnTypeArguments(method);
      rawStream = instance -> akka.stream.javadsl.Flow.fromProcessor(() -> (Processor) method.getJavaMember().invoke(instance))
          .mapMaterializedValue(n -> createErrorCommit("Processor"));
      inType = processorTypes[0];
      outType = processorTypes[1];

    } else if (rawReturnType.equals(Sink.class)) {
      Type[] sinkTypes = getReturnTypeArguments(method);
      rawStream = instance -> {
        Sink sink = (Sink) method.getJavaMember().invoke(instance);
        return akka.stream.javadsl.Flow.fromSinkAndSourceMat(sink, createAckSource(), Keep.right());
      };
      inType = sinkTypes[0];
      outType = Ack.class;

    } else if (rawReturnType.equals(akka.stream.javadsl.Flow.class)) {
      Type[] flowTypes = getReturnTypeArguments(method);
      rawStream = instance -> ((akka.stream.javadsl.Flow) method.getJavaMember().invoke(instance))
          .mapMaterializedValue(n -> createErrorCommit("Flow"));
      inType = flowTypes[0];
      outType = flowTypes[1];

    } else if (rawReturnType.equals(CompletionStage.class)) {
      if (method.getParameters().isEmpty()) {
        throw new DefinitionException("Single element incoming method must take a parameter");
      }
      Type[] resultTypes = getReturnTypeArguments(method);
      if (Reflections.getRawType(resultTypes[0]).equals(Void.class)) {
        rawStream = instance -> akka.stream.javadsl.Flow.create()
            .mapAsync(1, element ->
                ((CompletionStage) method.getJavaMember().invoke(instance, element)).thenApply(v -> Done.getInstance()))
            .mapMaterializedValue(n -> createErrorCommit("single element streams that return CompletionStage"));
        outType = Done.class;
      } else {
        rawStream = instance -> akka.stream.javadsl.Flow.create()
            .mapAsync(1, element -> (CompletionStage) method.getJavaMember().invoke(instance, element))
            .mapMaterializedValue(n -> createErrorCommit("single element streams that return CompletionStage"));
        outType = resultTypes[0];
      }

      inType = method.getParameters().get(0).getBaseType();

    } else {
      throw new DefinitionException("Incoming method " + methodName +
          " does not return a Subscriber, Processor, Sink, Flow, CompletionStage or void.");
    }

    akka.japi.Function<T, akka.stream.javadsl.Flow<Envelope<M>, Ack, Function<Ack, CompletionStage<Void>>>> stream;
    Type actualInType;

    if (outType.equals(Ack.class) && outType.equals(Void.class) && outType.equals(Done.class)) {
      throw new DefinitionException("Incoming method " + methodName + " must emit Ack, Void or Done.");
    }

    // Check if an envelope is supplied
    if (Reflections.getRawType(inType).equals(Envelope.class)) {
      Type[] envelopeArguments = ReflectionUtils.getTypeArguments(inType, () -> "Envelopes accepted by " + methodName);
      actualInType = envelopeArguments[0];

      if (outType.equals(Ack.class)) {
        // return as is
        stream = (akka.japi.Function) rawStream;

      } else {
        // Wrap to ensure the out type is Ack.
        akka.japi.Function<T, akka.stream.javadsl.Flow<Envelope<M>, ?, Function<Ack, CompletionStage<Void>>>> actualTypedStream =
            (akka.japi.Function) rawStream;
        stream = instance -> oneToOneAckFlow(actualTypedStream.apply(instance));
      }
    } else {

      actualInType = inType;

      if (outType.equals(Ack.class)) {
        // We have no envelope, but we are emitting Ack. This is either Sink or Subscriber,
        // and so we must be auto acking.
        akka.japi.Function<T, akka.stream.javadsl.Flow<M, Ack, Function<Ack, CompletionStage<Void>>>> actualTypedStream =
            (akka.japi.Function) rawStream;

        stream = instance -> akka.stream.javadsl.Flow.<Envelope<M>>create()
            .mapAsync(1, message -> message.ack().thenApply(n -> message.getPayload()))
            .viaMat(actualTypedStream.apply(instance), Keep.right());
      } else {

        akka.japi.Function<T, akka.stream.javadsl.Flow<M, ?, Function<Ack, CompletionStage<Void>>>> actualTypedStream =
            (akka.japi.Function) rawStream;

        stream = instance ->
            oneToOneAckFlow(
                akka.stream.javadsl.Flow.<Envelope<M>>create()
                    .map(Envelope::getPayload)
                    .viaMat(actualTypedStream.apply(instance), Keep.right())
            );

      }

    }

    return new StreamDescriptor.FlowIncomingDescriptor<T>() {
      public String id() {
        return "stream-" + method.getDeclaringType().getJavaClass().getSimpleName() + "-" + methodName;
      }

      @Override
      public String incomingTopic() {
        return incoming.topic();
      }

      @Override
      public Type incomingMessageType() {
        return actualInType;
      }

      @Override
      public akka.stream.javadsl.Flow<Envelope<?>, Ack, Function<Ack, CompletionStage<Void>>> incomingFlow(T instance) throws Exception {
        return (akka.stream.javadsl.Flow) stream.apply(instance);
      }
    };
  }

  /**
   * The M type variable is a lie because that type is completely unknown at the point of invocation of this method,
   * but it does allow us to gain some type-safety below.
   */
  private <T, M> StreamDescriptor.OutgoingDescriptor<T> readPublisherMethod(Outgoing outgoing, AnnotatedMethod<T> method) {
    // Probably shouldn't use Weld reflections utilities here... but for a PoC maybe it's ok.

    String methodName = method.getJavaMember().getName();
    Type rawReturnType = Reflections.getRawType(method.getBaseType());

    akka.japi.Function<T, Source<?, ?>> rawStream;
    Type outType;

    if (rawReturnType.equals(Flow.Publisher.class)) {
      Type[] publisherTypes = getReturnTypeArguments(method);
      rawStream = instance -> JavaFlowSupport.Source.fromPublisher((Flow.Publisher) method.getJavaMember().invoke(instance));
      outType = publisherTypes[0];
    } else if (rawReturnType.equals(Publisher.class)) {
      Type[] publisherTypes = getReturnTypeArguments(method);
      rawStream = instance -> Source.fromPublisher((Publisher) method.getJavaMember().invoke(instance));
      outType = publisherTypes[0];
    } else if (rawReturnType.equals(Source.class)) {
      Type[] sourceTypes = getReturnTypeArguments(method);
      rawStream = instance -> (Source) method.getJavaMember().invoke(instance);
      outType = sourceTypes[0];
    } else {
      throw new DefinitionException("Outgoing method " + methodName +
          " does not return a Publisher or Source");
    }

    Type actualOutType;
    akka.japi.Function<T, Source<Envelope<M>, ?>> actualTypedStream;

    if (Reflections.getRawType(outType).equals(Envelope.class)) {
      Type[] envelopeArguments = ReflectionUtils.getTypeArguments(outType, () -> "Envelopes published by " + methodName);
      actualOutType = envelopeArguments[0];
      actualTypedStream = (akka.japi.Function) rawStream;
    } else {
      actualOutType = outType;
      actualTypedStream = instance ->
          rawStream.apply(instance).map(payload -> new NoopEnvelope<>((M) payload));
    }

    return new StreamDescriptor.SourceOutgoingDescriptor<T>() {
      @Override
      public String id() {
        return "stream-" + method.getDeclaringType().getJavaClass().getSimpleName() + "-" + methodName;
      }

      @Override
      public String outgoingTopic() {
        return outgoing.topic();
      }

      @Override
      public Type outgoingMessageType() {
        return actualOutType;
      }

      @Override
      public Source<Envelope<?>, ?> outgoingSource(T instance) throws Exception {
        return (Source) actualTypedStream.apply(instance);
      }
    };
  }

  private static class NoopEnvelope<T> implements Envelope<T> {
    private final T payload;

    public NoopEnvelope(T payload) {
      this.payload = payload;
    }

    @Override
    public T getPayload() {
      return (T) payload;
    }
  }

  private <T, R> akka.stream.javadsl.Flow<Envelope<T>, Ack, Function<Ack, CompletionStage<Void>>> oneToOneAckFlow(
      akka.stream.javadsl.Flow<Envelope<T>, R, Function<Ack, CompletionStage<Void>>> flow
  ) {
    return akka.stream.javadsl.Flow.fromGraph(GraphDSL.create(flow,
        (b, wrappedFlow) -> {
          final UniformFanOutShape<Envelope<T>, Envelope<T>> bcast = b.add(Broadcast.create(2));
          final FanInShape2<Envelope<T>, R, Ack> zip =
              b.add(ZipWith.create((env, r) -> env.getAck()));

          b.from(bcast).toInlet(zip.in0());
          b.from(bcast).via(wrappedFlow).toInlet(zip.in1());

          return FlowShape.of(bcast.in(), zip.out());
        }));
  }

  private Type[] getReturnTypeArguments(AnnotatedMethod<?> method) {
    return ReflectionUtils.getTypeArguments(method.getBaseType(),
        () -> method.getBaseType().getTypeName() + " returned from " + method.getJavaMember().getName());
  }


  private Source<Ack, Function<Ack, CompletionStage<Void>>> createAckSource() {
    return Source.<Ack>queue(8, OverflowStrategy.backpressure())
        .mapMaterializedValue(sourceQueue -> ack -> sourceQueue.offer(ack).thenApply(result -> {
          if (result instanceof QueueOfferResult.Failure) {
            throw new RuntimeException(((QueueOfferResult.Failure) result).cause());
          } else {
            return null;
          }
        }));
  }

  private Function<Ack, CompletionStage<Void>> createErrorCommit(String streamName) {
    return ack -> {
      throw new RuntimeException("Commit not support on envelopes used in " + streamName + " streams.");
    };
  }

  /**
   * Injection target that starts up and shuts down streams.
   */
  private class StreamsInjectionTarget<T> implements InjectionTarget<T> {
    private final InjectionTarget<T> wrapped;
    private final List<StreamDescriptor<? super T>> descriptors;
    private final BeanManager beanManager;
    // Not sure if there's an easier way to track this
    private final Map<T, List<RunningStream>> runningStreams = new IdentityHashMap<>(new ConcurrentHashMap<>());

    public StreamsInjectionTarget(InjectionTarget<T> wrapped, List<StreamDescriptor<? super T>> descriptors, BeanManager beanManager) {
      this.wrapped = wrapped;
      this.descriptors = descriptors;
      this.beanManager = beanManager;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
      wrapped.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance) {
      wrapped.postConstruct(instance);

      // Is this the best way to get it, or is there a more efficient way?
      StreamManager streamManager = CDI.current().select(StreamManager.class).get();

      List<RunningStream> streams = new ArrayList<>();
      try {
        // Start all streams
        descriptors.forEach(ingest ->
            streams.add(streamManager.startManagedStream(ingest, instance))
        );
        runningStreams.put(instance, streams);
      } catch (RuntimeException e) {
        stopStreams(streams);
        throw e;
      }
    }

    private void stopStreams(List<RunningStream> streams) {
      streams.forEach(stream -> {
        try {
          stream.stop();
        } catch (RuntimeException ignored) {
          // todo log better
          ignored.printStackTrace();
        }
      });
    }

    @Override
    public void preDestroy(T instance) {
      try {
        List<RunningStream> streams = runningStreams.remove(instance);
        if (streams != null) {
          stopStreams(streams);
        } else {
          // This is odd, preDestroy invoked with out a bean that was constructed?
          // todo log better
          System.out.println("preDestroy invoked on bean that hadn't been previously passed to postConstruct?");
        }
      } finally {
        wrapped.preDestroy(instance);
      }
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
      return wrapped.produce(ctx);
    }

    @Override
    public void dispose(T instance) {
      wrapped.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
      return wrapped.getInjectionPoints();
    }
  }

}
