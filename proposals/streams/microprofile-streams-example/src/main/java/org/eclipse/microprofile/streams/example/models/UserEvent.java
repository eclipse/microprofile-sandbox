package org.eclipse.microprofile.streams.example.models;

import org.eclipse.microprofile.streams.example.jsonb.JsonbUuidDeserializer;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTypeDeserializer;
import java.util.UUID;

/**
 * A user details event.
 */
public class UserEvent {
  private final EventType type;
  private UUID id;
  private final String name;
  private final String email;

  @JsonbCreator
  public UserEvent(
      @JsonbProperty("type") EventType type,
      @JsonbProperty("name") String name,
      @JsonbProperty("email") String email) {
    this.type = type;
    this.id = id;
    this.name = name;
    this.email = email;
  }

  // See https://github.com/javaee/jsonb-spec/issues/71 for why this only works with a setter.
  @JsonbTypeDeserializer(JsonbUuidDeserializer.class)
  public void setId(UUID id) {
    this.id = id;
  }

  public EventType getType() {
    return type;
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

  public enum EventType { CREATED, UPDATED }
}
