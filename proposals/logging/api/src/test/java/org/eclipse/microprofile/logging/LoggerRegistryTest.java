package org.eclipse.microprofile.logging;

import org.eclipse.microprofile.logging.specialized.SpecializedLogEvent;
import org.eclipse.microprofile.logging.specialized.SpecializedLogEventSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the utility class that maps Loggers to a combination of their names and suppliers.
 */
public class LoggerRegistryTest {

  /**
   * Test that accessing a Logger by the same name but different LogEvent Types does
   * not fail the application.
   * 
   * @param info Test information.
   */
  @Test
  public void testDifferentLoggerSuppliersSameNameExplicit(TestInfo info) {
    final String loggerName = info.getDisplayName();
    
    final Logger logEventLogger = LoggerFactory.getLogger(loggerName);
    logEventLogger.debug(e -> String.format("Logger %s for LogEvents", loggerName));
    
    final Logger<SpecializedLogEvent> specializedLogEventLogger = 
        LoggerFactory.getLogger(loggerName, new SpecializedLogEventSupplier());
    
    assertFalse(logEventLogger == specializedLogEventLogger);
    
    specializedLogEventLogger.debug(e -> {
      e.name = "Special";
      e.version = 2;
      return String.format("Logger %s for SpecializedLogEvents", loggerName);
    });
    
    final Logger originalLogger = LoggerFactory.getLogger(loggerName);
    assertTrue(logEventLogger == originalLogger);
  }
  
  /**
   * Test that accessing a Logger by the same name but different LogEvent Types does
   * not fail the application.
   */
  @Test
  public void testDifferentLoggerSuppliersSameNameImplicit() {
    final Logger logEventLogger = LoggerFactory.getLogger();
    logEventLogger.debug(e -> String.format("Logger %s ", logEventLogger.getName()));
    
    final Logger<SpecializedLogEvent> specializedLogEventLogger = 
        LoggerFactory.getLogger(new SpecializedLogEventSupplier());
    
    assertFalse(logEventLogger == specializedLogEventLogger);
    
    specializedLogEventLogger.debug(e -> {
      e.name = "Special";
      e.version = 2;
      return String.format("Logger %s for SpecializedLogEvents", specializedLogEventLogger.getName());
    });
    
    final Logger originalLogger = LoggerFactory.getLogger();
    assertTrue(logEventLogger == originalLogger);
  }
}
