package org.eclipse.microprofile.streams.example;

import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Source;
import org.eclipse.microprofile.streams.Envelope;
import org.eclipse.microprofile.streams.Outgoing;
import org.eclipse.microprofile.streams.example.models.Email;
import org.eclipse.microprofile.streams.example.models.UserDetails;
import org.eclipse.microprofile.streams.example.repository.UserDetailsRepository;
import scala.concurrent.duration.FiniteDuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PeriodicUserNotifier {

  private final UserDetailsRepository repository;
  private final Materializer materializer;

  @Inject
  public PeriodicUserNotifier(UserDetailsRepository repository, Materializer materializer) {
    this.repository = repository;
    this.materializer = materializer;
  }

  /**
   * Creates a publisher that publishes a reminder once a month to each person that is on the
   * system that they receive notifications from this service.
   */
  @Outgoing(topic = "email-service.emails")
  public Flow.Publisher<Envelope<Email>> handleUserDetails() {

    return Source.tick(
        // Start after 1 second
        FiniteDuration.create(1, TimeUnit.SECONDS),
        // Requery database for notifications every 7 days
        FiniteDuration.create(7, TimeUnit.DAYS),
        new Object()
    )
        // Execute query to get user details of users not notified in the last month.
        .flatMapConcat(o -> {
          LocalDate since = LocalDate.now().minusMonths(1);
          return repository.getUserDetails(since);
        })
        // Convert user details to an email
        .map(userDetails -> {
          Email email = createReminderEmail(userDetails);
          return Envelope.ackableEnvelope(email,
              // This is the ack function that will be updated when the message is sent
              () -> repository.updateLastNotified(userDetails.getId(), LocalDate.now()));
        })
        // And convert to a Publisher
        .runWith(JavaFlowSupport.Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), materializer);
  }

  private Email createReminderEmail(UserDetails userDetails) {
    return new Email(userDetails.getEmail(), userDetails.getName(), "Notification reminder",
        "Hi " + userDetails.getName() + ",\n\nThis is just a friendly note to " +
            "remind you that you receive email notifications from our system.\n\n" +
            "Bye!"
    );
  }

  public void eagerInit(@Observes @Initialized(ApplicationScoped.class) Object init) {
    // This is just used to ensure this bean gets eagerly initialised.
  }
}
