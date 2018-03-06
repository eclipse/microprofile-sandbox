package org.eclipse.microprofile.streams.impl;

import akka.actor.*;
import akka.kafka.ConsumerSettings;
import akka.kafka.ProducerSettings;
import akka.pattern.Backoff;
import akka.pattern.BackoffOptions;
import akka.stream.Materializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import scala.concurrent.duration.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This starts subscribers and publishers.
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

  public <T> RunningStream startIncomingStream(StreamDescriptor<? super T> descriptor, T t) {
    if (descriptor instanceof StreamDescriptor.FlowIncomingDescriptor) {
      return startIncomingStream((StreamDescriptor.FlowIncomingDescriptor<? super T>) descriptor, t);
    } else if (descriptor instanceof StreamDescriptor.SourceOutgoingDescriptor) {
      return startOutgoingStream((StreamDescriptor.SourceOutgoingDescriptor<? super T>) descriptor, t);
    } else {
      throw new UnsupportedOperationException("Unknown descriptor: " + descriptor);
    }
  }

  private <T> RunningStream startIncomingStream(StreamDescriptor.FlowIncomingDescriptor<? super T> descriptor, T t) {
    ConsumerSettings<String, Object> consumerSettings =
        ConsumerSettings.create(system,
            // todo allow custom deserializers other than jsonb
            new StringDeserializer(), new Deserializer<>() {
              @Override
              public void configure(Map<String, ?> map, boolean b) {
              }

              @Override
              public Object deserialize(String s, byte[] bytes) {
                return jsonb.fromJson(new ByteArrayInputStream(bytes), descriptor.incomingMessageType());
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

    Props streamSupervisor = Props.create(ConsumerSupervisor.class,
        () -> new ConsumerSupervisor<>(materializer, consumerSettings, descriptor, t));

    return startStream(streamSupervisor, descriptor);
  }

  private <T> RunningStream startOutgoingStream(StreamDescriptor.SourceOutgoingDescriptor<? super T> descriptor, T t) {
    ProducerSettings<String, Object> producerSettings =
        ProducerSettings.create(system,
            // todo allow custom deserializers other than jsonb
            new StringSerializer(), new Serializer<>() {
              @Override
              public void configure(Map<String, ?> map, boolean b) {
              }

              @Override
              public byte[] serialize(String topic, Object data) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                jsonb.toJson(data, baos);
                return baos.toByteArray();
              }

              @Override
              public void close() {
              }
            })
            // todo make configurable
            .withBootstrapServers("localhost:9092");

    Props streamSupervisor = Props.create(ProducerSupervisor.class,
        () -> new ProducerSupervisor<>(materializer, producerSettings, descriptor, t));

    return startStream(streamSupervisor, descriptor);
  }

  private RunningStream startStream(Props streamSupervisor, StreamDescriptor<?> descriptor) {
    // todo make exponential backoff parameters configurable
    BackoffOptions options = Backoff.onStop(streamSupervisor, "stream",
        Duration.create(3, TimeUnit.SECONDS),
        Duration.create(30, TimeUnit.SECONDS),
        0.2);

    ActorRef actor = system.actorOf(options.props(), descriptor.id());
    return () -> actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
  }
}
