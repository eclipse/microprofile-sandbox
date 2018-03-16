package org.eclipse.microprofile.streams.example.repository;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.eclipse.microprofile.streams.example.models.UserDetails;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class UserDetailsRepository {

  private final CqlSession session;
  private final CompletionStage<PreparedStatement> updateStatement;
  private final CompletionStage<PreparedStatement> userDetailsQuery;
  private final CompletionStage<PreparedStatement> updateLastNotifiedStatement;

  public UserDetailsRepository() {
    this.session = CqlSession.builder()
        .withKeyspace(CqlIdentifier.fromCql("streaming"))
        .build();
    this.updateStatement = session.prepareAsync(
        "update user_details set name = :name, email = :email where id = :id"
    );
    this.userDetailsQuery = session.prepareAsync(
        "select id, name, email, last_notified from user_details"
    );
    this.updateLastNotifiedStatement = session.prepareAsync(
        "update user_details set last_notified = :lastNotified where id = :id"
    );
  }

  /**
   * Update the user details for the given user.
   *
   * @param userDetails The details to update.
   * @return A completion stage that is redeemed if the operation is successful, or redeemed with an error otherwise.
   */
  public CompletionStage<Void> updateUserDetails(UserDetails userDetails) {
    return updateStatement.thenCompose(statement ->
        session.executeAsync(statement.bind()
            .setUuid("id", userDetails.getId())
            .setString("name", userDetails.getName())
            .setString("email", userDetails.getEmail()))
    ).thenApply(resultSet -> null);
  }

  /**
   * Inserts many user details using a batch statement.
   * <p>
   * This probably doesn't perform any better than doing it one at a time, but in some databases it might.
   */
  public CompletionStage<Void> updateManyUserDetails(Iterable<UserDetails> userDetails) {
    return updateStatement.thenCompose(statement -> {
      BatchStatement batch = BatchStatement.newInstance(DefaultBatchType.UNLOGGED);
      for (UserDetails details : userDetails) {
        batch = batch.add(statement.bind()
            .setUuid("id", details.getId())
            .setString("name", details.getName())
            .setString("email", details.getEmail()));
      }
      return session.executeAsync(batch);
    }).thenApply(resultSet -> null);
  }

  /**
   * Get the user details of users who haven't been notified that they are on the system since the last notified
   * date from the database as a stream.
   */
  public Source<UserDetails, NotUsed> getUserDetails(LocalDate notNotifiedSince) {
    return Source.unfoldAsync(
        // We're going to unfold a CompletionStage<Optional<AsyncResultSet>>. The Optional will be
        // empty if there's no more pages (since the previous result set can tell you that).
        userDetailsQuery.thenCompose(statement ->
            session.executeAsync(statement.bind())
        ).thenApply(Optional::of),

        request -> request.thenApply(maybeResultSet -> maybeResultSet.map(resultSet -> {

          // Extract all user details out to a list
          List<UserDetails> userDetails = new ArrayList<>();
          for (Row row : resultSet.currentPage()) {
            userDetails.add(new UserDetails(
                row.getUuid("id"),
                row.getString("name"),
                row.getString("email"),
                row.getLocalDate("last_notified")
            ));
          }

          // Create the next request, if there's more pages, it will be the next page,
          // otherwise it will be a completion stage of empty.
          CompletionStage<Optional<AsyncResultSet>> nextRequest;
          if (resultSet.hasMorePages()) {
            nextRequest = resultSet.fetchNextPage().thenApply(Optional::of);
          } else {
            nextRequest = CompletableFuture.completedFuture(Optional.empty());
          }

          return Pair.create(nextRequest, userDetails);
        })))
        // And flatten the stream of lists to a stream of user details
        .mapConcat(userDetails -> userDetails)
        // And filter out results that are before the last notified
        .filter(userDetails -> userDetails.getLastNotified() == null ||
            userDetails.getLastNotified().isBefore(notNotifiedSince));
  }

  public CompletionStage<Void> updateLastNotified(UUID id, LocalDate lastNotified) {
    return updateLastNotifiedStatement.thenCompose(statement ->
        session.executeAsync(statement.bind()
            .setUuid("id", id)
            .setLocalDate("lastNotified", lastNotified)
        )
    ).thenApply(r -> null);
  }


}
