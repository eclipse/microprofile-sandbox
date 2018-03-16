package org.eclipse.microprofile.streams.impl;

import akka.Done;
import akka.NotUsed;
import akka.kafka.ProducerMessage;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.Materializer;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.OutgoingStream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public class EnvelopedKafkaOutgoingStream<T> implements OutgoingStream<Envelope<T>> {

  private final ProducerSettings<String, T> settings;
  private final String topic;
  private final Materializer materializer;

  public EnvelopedKafkaOutgoingStream(ProducerSettings<String, T> settings, String topic,
      Materializer materializer) {
    this.settings = settings;
    this.topic = topic;
    this.materializer = materializer;
  }

  @Override
  public CompletionStage<Void> publish(Envelope<T> message) {
    return Source.single(message)
        .runWith(sink(), materializer)
        .thenApply(d -> null);
  }

  @Override
  public Flow.Subscriber<Envelope<T>> subscriber() {
    return JavaFlowSupport.Source.<Envelope<T>>asSubscriber()
        .to(sink())
        .run(materializer);
  }

  private Sink<Envelope<T>, CompletionStage<Done>> sink() {
    return flow()
        .mapAsync(1, envelope -> envelope.ack().thenApply(v -> Done.getInstance()))
        .toMat(Sink.ignore(), Keep.right());
  }

  @Override
  public Flow.Processor<Envelope<T>, Ack> processor() {
    return JavaFlowSupport.Flow.toProcessor(
        flow().map(Envelope::getAck)
    ).run(materializer);
  }

  private akka.stream.javadsl.Flow<Envelope<T>, Envelope<T>, NotUsed> flow() {
    return akka.stream.javadsl.Flow.<Envelope<T>>create()
        .map(envelope -> new ProducerMessage.Message<>(
            new ProducerRecord<String, T>(topic, envelope.getPayload()),
            envelope
        ))
        .via(Producer.flow(settings))
        .map(result -> result.message().passThrough());
  }

  @Override
  public EnvelopedKafkaOutgoingStream<T> withTopic(String topic) {
    return new EnvelopedKafkaOutgoingStream<>(settings, topic, materializer);
  }

  public OutgoingStream<T> unwrap() {
    return new OutgoingStream<T>() {
      @Override
      public CompletionStage<Void> publish(T message) {
        return EnvelopedKafkaOutgoingStream.this.publish(wrapped(message));
      }

      @Override
      public Flow.Subscriber<T> subscriber() {
        return JavaFlowSupport.Source.<T>asSubscriber()
            .map(this::wrapped)
            .to(sink())
            .run(materializer);
      }

      @Override
      public Flow.Processor<T, Ack> processor() {
        return JavaFlowSupport.Flow.toProcessor(
            akka.stream.javadsl.Flow.<T>create()
                .map(this::wrapped)
                .via(flow())
                .map(Envelope::getAck)
        ).run(materializer);
      }

      @Override
      public OutgoingStream<T> withTopic(String topic) {
        return EnvelopedKafkaOutgoingStream.this.withTopic(topic).unwrap();
      }

      private Envelope<T> wrapped(T message) {
        return Envelope.ackableEnvelope(message, () -> CompletableFuture.completedFuture(null));
      }
    };
  }
}
