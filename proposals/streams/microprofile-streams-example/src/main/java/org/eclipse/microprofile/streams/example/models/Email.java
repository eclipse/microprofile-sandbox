package org.eclipse.microprofile.streams.example.models;

public class Email {
  private final String toAddress;
  private final String toName;
  private final String subject;
  private final String content;

  public Email(String toAddress, String toName, String subject, String content) {
    this.toAddress = toAddress;
    this.toName = toName;
    this.subject = subject;
    this.content = content;
  }

  public String getToAddress() {
    return toAddress;
  }

  public String getToName() {
    return toName;
  }

  public String getSubject() {
    return subject;
  }

  public String getContent() {
    return content;
  }
}
