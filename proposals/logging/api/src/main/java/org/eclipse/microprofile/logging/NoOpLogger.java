package org.eclipse.microprofile.logging;

import java.util.function.Supplier;

/**
 * A black hole Logger implementation for the occasion where no
 * actual logging implementation is available.
 */
class NoOpLogger<T extends LogEvent> extends AbstractLogger<T> {

  public NoOpLogger(String name, Supplier<T> supplier) {
    super(name, supplier);
  }

  @Override
  public boolean isLoggable(Level lvl) {
    return false;
  }

  @Override
  public void span(LogFunction<T> f) {
    // nothing to do.
  }

  @Override
  public void log(Level lvl, LogFunction<T> f) {
    // nothing to do.
  }

  @Override
  public void writeLog(Level lvl, T event) {
    // nothing to do.
  }
}
