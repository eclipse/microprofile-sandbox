package org.eclipse.microprofile.streams.example;

import akka.stream.Materializer;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Sink;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.Ingest;
import org.eclipse.microprofile.streams.example.models.UserDetails;
import org.eclipse.microprofile.streams.example.models.UserEvent;
import org.eclipse.microprofile.streams.example.repository.UserDetailsRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.Flow;

@ApplicationScoped
public class UserDetailsSubscriber {

  private final UserDetailsRepository repository;
  private final Materializer materializer;

  @Inject
  public UserDetailsSubscriber(UserDetailsRepository repository, Materializer materializer) {
    this.repository = repository;
    this.materializer = materializer;
  }

  @Ingest(topic = "user-details.events")
  public Flow.Subscriber<Envelope<UserEvent>> handleUserDetails() {
    return JavaFlowSupport.Source.<Envelope<UserEvent>>asSubscriber()
        .mapAsync(1, msg -> {
          UserEvent event = msg.getPayload();
          return repository.updateUserDetails(new UserDetails(event.getId(),
              event.getName(), event.getEmail())).thenApply(v -> msg);
        })
        .to(Sink.foreach(Envelope::commit))
        .run(materializer);
  }

  public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object init) {
    // This is just used to ensure this bean gets eagerly initialised.
  }
}
