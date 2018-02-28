package org.eclipse.microprofile.streams.example.models;

import java.util.UUID;

/**
 * A user details object.
 */
public class UserDetails {

  private final UUID id;
  private final String name;
  private final String email;

  public UserDetails(UUID id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserDetails that = (UserDetails) o;

    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;
    return email.equals(that.email);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + email.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "UserDetails{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
