package org.eclipse.microprofile.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * (Mock) Logger used for testing that exposes internals for Tests to interrogate.
 * 
 * @param <T> The Type of LogEvents the Logger generates.
 */
public class MockLogger<T extends LogEvent> extends AbstractLogger<T> {

  private static Level LEVEL;
  
  private final List<T> logEvents = new ArrayList<>();
  
  
  public MockLogger(String name, Supplier<T> supplier) {
    super(name, supplier);
  }

  @Override
  public void writeLog(Level lvl, T event) {
    logEvents.add(event);
    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    int lineNumber = 0;
    if (stackTrace.length >= 4) {
      StackTraceElement elem = stackTrace[4];
      lineNumber = elem.getLineNumber();
    }
    
    System.out.println(String.format("%-5s %s:%d - %s", lvl, getName(), lineNumber, getJsonString(event)));
  }
  
  public static void setLevel(Level lvl) {
    LEVEL = lvl;
  }

  @Override
  public boolean isLoggable(Level lvl) {
    return lvl.intValue() >= LEVEL.intValue();
  }

  public List<T> getEvents() {
    return logEvents;
  }
}
