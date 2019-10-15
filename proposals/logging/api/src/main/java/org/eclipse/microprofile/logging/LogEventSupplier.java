package org.eclipse.microprofile.logging;

import java.util.function.Supplier;

/**
 * Class to create {@link LogEvent} instances.
 * 
 * <p>
 * This particular class will create a new instance for every call to {@link #get()}
 * </p>
 */
public class LogEventSupplier implements Supplier<LogEvent> {

  @Override
  public LogEvent get() {
    return new LogEvent();
  }

}
