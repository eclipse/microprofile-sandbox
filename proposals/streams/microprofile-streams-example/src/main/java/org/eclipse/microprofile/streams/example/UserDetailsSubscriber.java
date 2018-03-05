package org.eclipse.microprofile.streams.example;

import akka.Done;
import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.eclipse.microprofile.streams.Ack;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.Incoming;
import org.eclipse.microprofile.streams.example.models.UserDetails;
import org.eclipse.microprofile.streams.example.models.UserEvent;
import org.eclipse.microprofile.streams.example.repository.UserDetailsRepository;
import org.reactivestreams.Subscriber;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserDetailsSubscriber {

  private final UserDetailsRepository repository;
  private final Materializer materializer;

  @Inject
  public UserDetailsSubscriber(UserDetailsRepository repository, Materializer materializer) {
    this.repository = repository;
    this.materializer = materializer;
  }

  /**
   * This shows just returning a subscriber.
   *
   * Because we're using RS, we can control the parallelism.
   */
  @Incoming(topic = "user-details.events")
  public Flow.Subscriber<Envelope<UserEvent>> handleUserDetails() {
    return JavaFlowSupport.Source.<Envelope<UserEvent>>asSubscriber()
        // Handle up to 4 messages in parallel.
        .mapAsync(4, msg -> {
          UserEvent event = msg.getPayload();
          return repository.updateUserDetails(new UserDetails(event.getId(),
              event.getName(), event.getEmail())).thenApply(v -> msg);
        })
        .to(Sink.foreach(Envelope::ack))
        .run(materializer);
  }

  /**
   * This shows handling one message at a time without Reactive Streams.
   *
   * The container will wrap this in Reactive Streams. Convenient when all you want to do is simple processing
   * of each message, without any form of transformation, or control over parallelism, batching, or involvement
   * with other reactive streams providers.
   */
  @Incoming(topic = "user-details.events2")
  public CompletionStage<Void> handleUserDetails2(UserEvent event) {
    return repository.updateUserDetails(new UserDetails(event.getId(),
        event.getName(), event.getEmail())).thenApply(v -> null);
  }

  /**
   * Again, handling of one message a time, this time with no way to emit back pressure.
   *
   * You should probably never do this.
   */
  @Incoming(topic = "user-details.events3")
  public void handleUserDetails3(Envelope<UserEvent> msg) {
    UserEvent event = msg.getPayload();
    repository.updateUserDetails(new UserDetails(event.getId(),
        event.getName(), event.getEmail())).thenCompose(v -> msg.ack());
  }

  /**
   * This shows handling by emitting the Ack type, without an explicit ack by the developer.
   *
   * This time we show how a user can implement batching of events, which depending on what
   * you're doing, may yield superior throughput.
   */
  @Incoming(topic = "user-details.events4")
  public Flow.Processor<Envelope<UserEvent>, Ack> handleUserDetails4() {
    return JavaFlowSupport.Flow.toProcessor(akka.stream.javadsl.Flow.<Envelope<UserEvent>>create()

        // Batch into batches of 20 events.
        .batch(20,
            event -> new ArrayList<>(Collections.singletonList(event)),
            (list, event) -> {
              list.add(event);
              return list;
            }
        )

        // Save the batched events to the database
        .mapAsync(1, batch -> {

          // Convert to a List<UserDetails>
          List<UserDetails> userDetailsList = batch.stream()
              .map(msg -> {
                UserEvent event = msg.getPayload();
                return new UserDetails(event.getId(), event.getName(), event.getEmail());
              }).collect(Collectors.toList());

          // And save
          return repository.updateManyUserDetails(userDetailsList)
              // And emit the ack of the last event
              .thenApply(v -> batch.get(batch.size() - 1).getAck());
        })).run(materializer);
  }

  /**
   * This shows how the same API can be used with Reactive Streams instead of JDK9 Flow.
   */
  @Incoming(topic = "user-details.events5")
  public Subscriber<Envelope<UserEvent>> handleUserDetails5() {
    return Source.<Envelope<UserEvent>>asSubscriber()
        .mapAsync(4, msg -> {
          UserEvent event = msg.getPayload();
          return repository.updateUserDetails(new UserDetails(event.getId(),
              event.getName(), event.getEmail())).thenApply(v -> msg);
        })
        .to(Sink.foreach(Envelope::ack))
        .run(materializer);
  }

  /**
   * And if a developer wants to use some non portable features, for convenience, this shows
   * how an implementation can support non portable types, such as an Akka streams flow.
   *
   * This time, we emit Done to indicate successful processing, which the implementation
   * will automatically map to the corresponding Ack for the message processed.
   */
  @Incoming(topic = "user-details.events6")
  public akka.stream.javadsl.Flow<UserEvent, Done, NotUsed> handleUserDetails6() {
    return akka.stream.javadsl.Flow.<UserEvent>create()
        .mapAsync(4, event ->
            repository.updateUserDetails(new UserDetails(event.getId(),
              event.getName(), event.getEmail())).thenApply(v -> Done.getInstance())
        );
  }


  public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object init) {
    // This is just used to ensure this bean gets eagerly initialised.
  }
}
