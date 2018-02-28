package org.eclipse.microprofile.streams.example.repository;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.eclipse.microprofile.streams.example.models.UserDetails;

import javax.enterprise.context.ApplicationScoped;
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

}
