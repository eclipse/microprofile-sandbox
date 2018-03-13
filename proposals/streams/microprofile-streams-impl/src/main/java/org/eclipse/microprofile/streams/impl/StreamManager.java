package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.*;
import akka.kafka.*;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Producer;
import akka.stream.*;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RestartSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;
import scala.concurrent.duration.Duration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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

  public <T> RunningStream startManagedStream(StreamDescriptor<? super T> descriptor, T t) {
    if (descriptor instanceof StreamDescriptor.FlowIncomingDescriptor) {
      return startIncomingStream((StreamDescriptor.FlowIncomingDescriptor<? super T>) descriptor, t);
    } else if (descriptor instanceof StreamDescriptor.SourceOutgoingDescriptor) {
      return startOutgoingStream((StreamDescriptor.SourceOutgoingDescriptor<? super T>) descriptor, t);
    } else {
      throw new UnsupportedOperationException("Unknown descriptor: " + descriptor);
    }
  }

  public <T> EnvelopedKafkaOutgoingStream<T> createUnmanagedOutgoingStream(Type outgoingType, String topic) {
    ProducerSettings<String, T> settings = createOutgoingStreamProducerSettings();

    return new EnvelopedKafkaOutgoingStream<>(settings, topic, materializer);
  }

  public <T> EnvelopedKafkaIncomingStream<T> createUnmanagedIncomingStream(Type incomingType, String topic) {
    ConsumerSettings<String, T> settings = createIncomingStreamProducerSettings(incomingType);

    return new EnvelopedKafkaIncomingStream<>(settings, topic, materializer);
  }

  private <T> RunningStream startIncomingStream(StreamDescriptor.FlowIncomingDescriptor<? super T> descriptor, T t) {
    ConsumerSettings<String, Object> consumerSettings = createIncomingStreamProducerSettings(descriptor.incomingMessageType());

    // todo make exponential backoff parameters configurable
    // todo name the right parts of the stream so that error reporting makes sense
    Source<Done, NotUsed> stream = RestartSource.withBackoff(
        Duration.create(3, TimeUnit.SECONDS),
        Duration.create(30, TimeUnit.SECONDS),
        0.2,
        () -> {
          AtomicReference<Function<Ack, CompletionStage<Void>>> ackFunction = new AtomicReference<>();

          akka.stream.javadsl.Flow<Envelope<Object>, Ack, Function<Ack, CompletionStage<Void>>> subscriber =
              (akka.stream.javadsl.Flow) descriptor.incomingFlow(t);

          return Consumer.committableSource(consumerSettings, Subscriptions.topics(descriptor.incomingTopic()))
              .<Envelope<Object>>map(message -> new KafkaEnvelope<>(message, ackFunction.get()))
              .viaMat(subscriber, (control, ack) -> {
                ackFunction.set(ack);
                return NotUsed.getInstance();
              }).mapAsync(1, ack -> {
            if (ack instanceof KafkaAck) {
              return ((KafkaAck) ack).getMessage()
                  .committableOffset().commitJavadsl().thenApply(d -> Done.getInstance());
            } else {
              throw new IllegalArgumentException("Don't know how to handle ack of type " + ack.getClass() +
                  ". Kafka consumers must only emit acks returned by the passed in Envelope.");
            }
          });
        }
    );

    KillSwitch killSwitch = stream
        .viaMat(KillSwitches.single(), Keep.right())
        .to(Sink.ignore())
        .run(materializer);

    return killSwitch::shutdown;
  }

  private <T> ConsumerSettings<String, T> createIncomingStreamProducerSettings(Type messageType) {
    return ConsumerSettings.create(system,
        // todo allow custom deserializers other than jsonb
        new StringDeserializer(), new Deserializer<T>() {
          @Override
          public void configure(Map<String, ?> map, boolean b) {
          }

          @Override
          public T deserialize(String s, byte[] bytes) {
            return (T) jsonb.fromJson(new ByteArrayInputStream(bytes), messageType);
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
  }

  private <T> ProducerSettings<String, T> createOutgoingStreamProducerSettings() {
      return ProducerSettings.create(system,
            // todo allow custom deserializers other than jsonb
            new StringSerializer(), new Serializer<T>() {
              @Override
              public void configure(Map<String, ?> map, boolean b) {
              }

              @Override
              public byte[] serialize(String topic, T data) {
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
  }

  private <T> RunningStream startOutgoingStream(StreamDescriptor.SourceOutgoingDescriptor<? super T> descriptor, T t) {

    ProducerSettings<String, Object> producerSettings = createOutgoingStreamProducerSettings();

    // todo make exponential backoff parameters configurable
    // todo name the right parts of the stream so that error reporting makes sense
    Source<Done, NotUsed> stream = RestartSource.withBackoff(
        Duration.create(3, TimeUnit.SECONDS),
        Duration.create(30, TimeUnit.SECONDS),
        0.2,
        () -> {

          Source<Envelope<Object>, ?> publisher = (Source) descriptor.outgoingSource(t);

          return publisher.map(envelope -> new ProducerMessage.Message<>(
              new ProducerRecord<String, Object>(descriptor.outgoingTopic(), envelope.getPayload()),
              envelope
          ))
              .via(Producer.flow(producerSettings))
              .mapAsync(1, result -> result.message().passThrough().ack().thenApply(v -> Done.getInstance()));
        });

    KillSwitch killSwitch = stream
        .viaMat(KillSwitches.single(), Keep.right())
        .to(Sink.ignore())
        .run(materializer);

    return killSwitch::shutdown;
  }

}
