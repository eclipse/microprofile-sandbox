package org.eclipse.microprofile.streams.impl;

import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.Ingest;
import org.jboss.weld.util.reflection.Reflections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

/**
 * This is my first time really writing a CDI extension, so I've probably got a lot wrong.
 */
public class AkkaKafkaStreamsCdiExtension implements Extension {

  public <T> void locateStreams(@Observes ProcessInjectionTarget<T> bean, BeanManager beanManager) {

    // Find all the @Ingest annotated methods, and read them.
    List<IngestSubscriber<T>> subscribers = locateSubscribers(bean.getAnnotatedType());

    if (!subscribers.isEmpty()) {
      // Wrap the injection target in our own one that starts up and shuts down streams.
      bean.setInjectionTarget(new StreamsInjectionTarget<>(bean.getInjectionTarget(), subscribers, beanManager));
    }
  }

  public void afterBeanDiscovery(@Observes BeforeBeanDiscovery discovery, BeanManager beanManager) {
    // todo what name should these have?
    discovery.addAnnotatedType(beanManager.createAnnotatedType(ActorSystemProvider.class), ActorSystemProvider.class.getName());
    discovery.addAnnotatedType(beanManager.createAnnotatedType(StreamManager.class), StreamManager.class.getName());
  }

  private <T> List<IngestSubscriber<T>> locateSubscribers(AnnotatedType<T> type) {
    List<IngestSubscriber<T>> ingests = new ArrayList<>();

    for (AnnotatedMethod<? super T> method: type.getMethods()) {
      Ingest ingest = method.getAnnotation(Ingest.class);
      if (ingest != null) {

        String methodName = method.getJavaMember().getName();

        // Ensure the type is subscriber
        // Probably shouldn't use Weld reflections utilities here... but for a PoC maybe it's ok.
        if (!Reflections.getRawType(method.getBaseType()).equals(Flow.Subscriber.class)) {
          throw new DefinitionException("@" + Ingest.class + " annotated method " + methodName +
              " does not return " + Flow.Subscriber.class);
        }

        Type[] subscriberTypes = Reflections.getActualTypeArguments(method.getBaseType());
        if (subscriberTypes == null || subscriberTypes.length == 0) {
          throw new DefinitionException("Subscriber returned from " + method.getJavaMember().getName() + " is not parameterized.");
        }

        if (!Reflections.getRawType(subscriberTypes[0]).equals(Envelope.class)) {
          throw new DefinitionException(methodName + " must return a subscriber of " + Envelope.class);
        }

        Type[] envelopeTypes = Reflections.getActualTypeArguments(subscriberTypes[0]);

        if (envelopeTypes == null || envelopeTypes.length == 0) {
          throw new DefinitionException("Subscriber returned from " + method.getJavaMember().getName() + " is using unparameterized envelopes.");
        }

        Type messageType = envelopeTypes[0];
        ingests.add(new IngestSubscriber<>(ingest.topic(), method.getJavaMember(), messageType));
      }
    }

    return ingests;
  }

  /**
   * Injection target that starts up and shuts down streams.
   */
  private class StreamsInjectionTarget<T> implements InjectionTarget<T> {
    private final InjectionTarget<T> wrapped;
    private final List<IngestSubscriber<T>> ingests;
    private final BeanManager beanManager;
    // Not sure if there's an easier way to track this
    private final Map<T, List<RunningStream>> runningStreams = new IdentityHashMap<>(new ConcurrentHashMap<>());

    public StreamsInjectionTarget(InjectionTarget<T> wrapped, List<IngestSubscriber<T>> ingests, BeanManager beanManager) {
      this.wrapped = wrapped;
      this.ingests = ingests;
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
        ingests.forEach(ingest ->
            streams.add(streamManager.startSubscriber(ingest, instance))
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
