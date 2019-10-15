package org.eclipse.microprofile.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Utils {
  
  public static void setLoggerLevel(Level lvl) {
    MockLogger.setLevel(lvl);
  }
  
  public static void assertEmpty(Logger testLogger) {
    assertTrue(((MockLogger) testLogger).getEvents().isEmpty());
  }
  
  public static void assertNotEmpty(Logger testLogger) {
    assertFalse(((MockLogger) testLogger).getEvents().isEmpty());
  }

  public static void assertLogCount(Logger testLogger, int count) {
    final MockLogger mockLog = (MockLogger) testLogger;
    assertTrue(mockLog.getEvents().size() == count, "Count of log messages [" + mockLog.getEvents().size() + "] did not match expected size [" + count + "]");
  }
}
