package org.eclipse.microprofile.streams;

final class UncorrelatedAck implements Ack {
  private UncorrelatedAck() {}

  static final UncorrelatedAck INSTANCE = new UncorrelatedAck();

  @Override
  public String toString() {
    return "UncorrelatedAck";
  }
}
