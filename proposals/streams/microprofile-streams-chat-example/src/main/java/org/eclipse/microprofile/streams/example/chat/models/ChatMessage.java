package org.eclipse.microprofile.streams.example.chat.models;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public class ChatMessage {
  private final String message;

  @JsonbCreator
  public ChatMessage(@JsonbProperty("message") String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChatMessage that = (ChatMessage) o;

    return message.equals(that.message);
  }

  @Override
  public int hashCode() {
    return message.hashCode();
  }

  @Override
  public String toString() {
    return "ChatMessage{" +
        "message='" + message + '\'' +
        '}';
  }
}
