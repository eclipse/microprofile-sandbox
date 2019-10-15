package org.eclipse.microprofile.logging;

import java.util.function.Supplier;

/**
 * Factory to build {@link NoOpLogger} instances.
 */
class NoOpLoggerFactory implements LoggerFactoryProvider {
  
  private static final NoOpLogger LOGGER = new NoOpLogger("noop", null);

  @Override
  public Logger getLogger() {
    return NoOpLoggerFactory.LOGGER;
  }

  @Override
  public <T extends LogEvent> Logger<T> getLogger(Supplier<T> supplier) {
    return NoOpLoggerFactory.LOGGER;
  }

  @Override
  public Logger getLogger(String name) {
    return NoOpLoggerFactory.LOGGER;
  }

  @Override
  public <T extends LogEvent> Logger<T> getLogger(String name, Supplier<T> supplier) {
    return NoOpLoggerFactory.LOGGER;
  }

}
