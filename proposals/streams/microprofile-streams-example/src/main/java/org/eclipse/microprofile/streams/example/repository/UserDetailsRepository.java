package org.eclipse.microprofile.streams.example.repository;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import org.eclipse.microprofile.streams.example.models.UserDetails;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class UserDetailsRepository {

  private final CqlSession session;
  private final CompletionStage<PreparedStatement> updateStatement;

  public UserDetailsRepository() {
    this.session = CqlSession.builder()
        .withKeyspace(CqlIdentifier.fromCql("streaming"))
        .build();
    this.updateStatement = session.prepareAsync(
        "insert into user_details (id, name, email) values (:id, :name, :email)"
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
   *
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



}
