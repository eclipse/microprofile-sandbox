package org.eclipse.microprofile.logging;

import java.util.function.Supplier;

/**
 * 
 */
public interface LoggerFactoryProvider {
  
  Logger getLogger();
  
  <T extends LogEvent> Logger<T> getLogger(Supplier<T> supplier);
  
  Logger getLogger(String name);
  
  <T extends LogEvent> Logger<T> getLogger(String name, Supplier<T> supplier);
}
