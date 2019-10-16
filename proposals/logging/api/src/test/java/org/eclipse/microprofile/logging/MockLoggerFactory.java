package org.eclipse.microprofile.logging;

import java.util.function.Supplier;

public class MockLoggerFactory implements LoggerFactoryProvider {
  
  private static final LoggerRegistry REGISTRY = new LoggerRegistry();
  
  /** General purpose LogEvent supplier, need only one instance of this */
  private static final LogEventSupplier LOG_EVENT_SUPPLIER = new LogEventSupplier();

  @Override
  public Logger<LogEvent> getLogger() {
    return REGISTRY.getLogger(LOG_EVENT_SUPPLIER, loggerKey -> new MockLogger(loggerKey.getName(), new LogEventSupplier()));
  }

  @Override
  public <T extends LogEvent> Logger<T> getLogger(Supplier<T> supplier) {
    return REGISTRY.getLogger(supplier, loggerKey -> new MockLogger(loggerKey.getName(), supplier));
  }

  @Override
  public Logger<LogEvent> getLogger(String name) {
    return REGISTRY.getLogger(name, LOG_EVENT_SUPPLIER, loggerKey -> new MockLogger(loggerKey.getName(), new LogEventSupplier()));
  }

  @Override
  public <T extends LogEvent> Logger<T> getLogger(String name, Supplier<T> supplier) {
    return REGISTRY.getLogger(name, supplier, loggerKey -> new MockLogger(loggerKey.getName(), supplier));
  }
}
