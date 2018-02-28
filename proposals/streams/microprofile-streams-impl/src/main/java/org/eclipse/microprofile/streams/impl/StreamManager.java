package org.eclipse.microprofile.streams.impl;

import akka.actor.*;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.pattern.Backoff;
import akka.pattern.BackoffOptions;
import akka.stream.Materializer;
import akka.stream.javadsl.JavaFlowSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.streams.Envelope;
import scala.concurrent.duration.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

/**
 * This starts subscribers.
 *
 * For those unfamiliar with Akka and Akka streams, essentially what is being done here is an Actor is created that
 * runs the stream and monitors its liveness. If/when the stream crashes, the actor shuts itself down. An exponential
 * backoff supervisor actor is used to restart the actor (and hence the stream) after an exponential backoff (to avoid
 * hammering already failing resources in a tight loop).
 */
@ApplicationScoped
public class StreamManager {

  private final ActorSystem system;
  private final Materializer materializer;
  private final Jsonb jsonb = JsonbBuilder.create();

  @Inject
  public StreamManager(ActorSystem system, Materializer materializer) {
    this.system = system;
    this.materializer = materializer;
  }

  public <T> RunningStream startSubscriber(IngestSubscriber<T> ingestSubscriber, T t) {

    ConsumerSettings<String, Object> consumerSettings =
        ConsumerSettings.create(system,
            // todo allow custom deserializers other than jsonb
            new StringDeserializer(), new Deserializer<>() {
              @Override
              public void configure(Map<String, ?> map, boolean b) {
              }

              @Override
              public Object deserialize(String s, byte[] bytes) {
                return jsonb.fromJson(new ByteArrayInputStream(bytes), ingestSubscriber.getMessageType());
              }

              @Override
              public void close() {
              }
            })
            // todo make configurable
            .withBootstrapServers("localhost:9092")
            // todo provide via annotation
            .withGroupId("group1")
            // todo should also be configurable
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    Props streamSupervisor = Props.create(StreamSupervisor.class,
        () -> new StreamSupervisor<>(materializer, consumerSettings, ingestSubscriber, t));

    // todo make exponential backoff parameters configurable
    BackoffOptions options = Backoff.onStop(streamSupervisor, "stream",
        Duration.create(3, TimeUnit.SECONDS),
        Duration.create(30, TimeUnit.SECONDS),
        0.2);

    // todo name the actor something sensible
    ActorRef actor = system.actorOf(options.props());
    return () -> actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
  }

  private static class StreamSupervisor<T> extends AbstractActor {
    private final Materializer materializer;
    private final ConsumerSettings<String, Object> consumerSettings;
    private final IngestSubscriber<T> ingestSubscriber;
    private final T instance;
    private Consumer.Control control;

    public StreamSupervisor(Materializer materializer, ConsumerSettings<String, Object> consumerSettings,
        IngestSubscriber<T> ingestSubscriber, T instance) {
      this.materializer = materializer;
      this.consumerSettings = consumerSettings;
      this.ingestSubscriber = ingestSubscriber;
      this.instance = instance;
    }

    @Override
    public void preStart() throws Exception {
      Flow.Subscriber<Envelope<Object>> subscriber =
          (Flow.Subscriber<Envelope<Object>>) ingestSubscriber.subscriber(instance);

      control = Consumer.committableSource(consumerSettings, Subscriptions.topics(ingestSubscriber.getTopic()))
          .<Envelope<Object>>map(KafkaEnvelope::new)
          .to(JavaFlowSupport.Sink.fromSubscriber(subscriber))
          .run(materializer);

      control.isShutdown().whenComplete((done, error) -> {
        // todo handle error
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
      });
    }

    @Override
    public void postStop() throws Exception {
      control.stop();
    }

    @Override
    public Receive createReceive() {
      return emptyBehavior();
    }

  }

  private static class KafkaEnvelope<T> implements Envelope<T> {
    private final ConsumerMessage.CommittableMessage<?, T> message;

    public KafkaEnvelope(ConsumerMessage.CommittableMessage<?, T> message) {
      this.message = message;
    }

    @Override
    public T getPayload() {
      return message.record().value();
    }
    @Override
    public CompletionStage<Void> commit() {
      return message.committableOffset().commitJavadsl().thenApply(done -> null);
    }
  }

}
