package org.eclipse.microprofile.streams.impl;

import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.Materializer;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Sink;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.IncomingStream;

import java.util.Objects;
import java.util.concurrent.Flow;

public class EnvelopedKafkaIncomingStream<T> implements IncomingStream<Envelope<T>> {
  private final ConsumerSettings<String, T> consumerSettings;
  private final String topic;
  private final Materializer materializer;
  private final Long offset;

  public EnvelopedKafkaIncomingStream(ConsumerSettings<String, T> consumerSettings, String topic, Materializer materializer) {
    this(consumerSettings, topic, materializer, null);
  }

  public EnvelopedKafkaIncomingStream(ConsumerSettings<String, T> consumerSettings, String topic, Materializer materializer, Long offset) {
    this.consumerSettings = consumerSettings;
    this.topic = topic;
    this.materializer = materializer;
    this.offset = offset;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super Envelope<T>> subscriber) {
    Objects.requireNonNull(subscriber, "Subscriber must not be null");

    if (offset == null) {
      Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
          .map(msg ->
              new KafkaEnvelope<>(msg,
                  ack -> ((KafkaAck) ack).getMessage().committableOffset()
                      .commitJavadsl().thenApply(d -> null))
          ).runWith((Sink) JavaFlowSupport.Sink.fromSubscriber(subscriber), materializer);
    } else {
      Consumer.plainSource(consumerSettings,
          Subscriptions.assignmentWithOffset(new TopicPartition(topic, 0), offset)
      )
          .map(msg -> Envelope.ackableEnvelope(msg.value(), () -> {
            throw new RuntimeException("Acking not possible when manual offset tracking is used.");
          }))
          .runWith((Sink) JavaFlowSupport.Sink.fromSubscriber(subscriber), materializer);
    }

  }

  IncomingStream<T> unwrap() {
    return new IncomingStream<T>() {
      @Override
      public void subscribe(Flow.Subscriber<? super T> subscriber) {
        Objects.requireNonNull(subscriber, "Subscriber must not be null");

        if (offset == null) {
          Consumer.atMostOnceSource(consumerSettings, Subscriptions.topics(topic))
              .map(ConsumerRecord::value)
              .runWith((Sink) JavaFlowSupport.Sink.fromSubscriber(subscriber), materializer);
        } else {
          Consumer.plainSource(consumerSettings,
              Subscriptions.assignmentWithOffset(new TopicPartition(topic, 0), offset))
              .map(ConsumerRecord::value)
              .runWith((Sink) JavaFlowSupport.Sink.fromSubscriber(subscriber), materializer);
        }
      }

      @Override
      public IncomingStream<T> withTopic(String topic) {
        return EnvelopedKafkaIncomingStream.this.withTopic(topic).unwrap();
      }

      @Override
      public IncomingStream<T> fromOffset(long offset) {
        return EnvelopedKafkaIncomingStream.this.fromOffset(offset).unwrap();
      }

    };
  }

  @Override
  public EnvelopedKafkaIncomingStream<T> withTopic(String topic) {
    return new EnvelopedKafkaIncomingStream<>(consumerSettings, topic, materializer, offset);
  }

  @Override
  public EnvelopedKafkaIncomingStream<T> fromOffset(long offset) {
    return new EnvelopedKafkaIncomingStream<>(consumerSettings, topic, materializer, offset);
  }
}
