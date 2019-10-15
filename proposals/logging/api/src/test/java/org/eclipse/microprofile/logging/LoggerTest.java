package org.eclipse.microprofile.logging;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(WeldJunit5Extension.class)
public class LoggerTest {

  @WeldSetup
  public WeldInitiator weld = WeldInitiator.from(MockLoggerFactory.class)
                                           .build();
  
  private Logger log;

  @BeforeAll
  public static void beforeAll() {
    // Reduce noise from Weld. 
    java.util.logging.Logger weldLogger = java.util.logging.Logger.getLogger("org.jboss.weld");
    weldLogger.setLevel(java.util.logging.Level.OFF);
  }
  
  @BeforeEach
  public void beforeEach(TestInfo info) {
    // Reset the Logging level before each test.
    Utils.setLoggerLevel(Level.DEBUG);
    
    log = LoggerFactory.getLogger(info.getDisplayName());
  }
  
  /**
   * Demonstrate the simplest log statement.
   *
   * @param info Test information
   */
  @Test
  public void testSimplestStatement(TestInfo info) {
    log.debug(e -> "A simple log message");
    
    Utils.assertLogCount(log, 1);
  }

  /**
   * Test that a logger can be accessed that uses the default naming policy (I.e. the class name).
   */
  @Test
  public void testDefaultNamedLogger() {
    Logger defaultNamedLogger = LoggerFactory.getLogger();
    defaultNamedLogger.debug(e -> "A simple log message");
    
    Utils.assertLogCount(defaultNamedLogger, 1);
  }
  
  /**
   * Test instantiating a logger from within a nested class.
   */
  @Test
  public void testNestedLogger() {
    final NestedClass nestedClass = new NestedClass();
    final Logger nestedLogger = nestedClass.getLogger();
    nestedLogger.debug(e -> "A simple log message");
    
    Utils.assertLogCount(nestedLogger, 1);
  }

  /**
   * Demonstrate a simple log statement that uses a formatted/parameterised message.
   *
   * @param info Test information.
   */
  @Test
  public void testFormattedString(TestInfo info) {
    log.debug(e -> String.format("Message parameter one [%s] and [%s]", "val1", "val2"));
    
    Utils.assertLogCount(log, 1);
  }
  
  /**
   * Ensure the configured logging level filters log statements that are not of a sufficient level.
   *
   * @param info Test information
   */
  @Test
  public void testFilteredLog(TestInfo info) {
    Utils.setLoggerLevel(Level.ERROR);

    log.debug(e -> fail("Log statement should not have been called"));
    
    Utils.assertEmpty(log);
  }

  /**
   * Demonstrate logging an exception
   *
   * @param info Test information
   */
  @Test
  public void testException(TestInfo info) {
    final Exception exc = new Exception();
    log.error(e -> {
      e.throwable = exc;
      return "Exception happened";
    });
    
    Utils.assertLogCount(log, 1);
    final LogEvent logEvent = ((MockLogger<LogEvent>) log).getEvents().remove(0);
    assertNotNull(logEvent.throwable);
  }

  private class NestedClass {

    Logger<LogEvent> log = LoggerFactory.getLogger();

    public Logger<LogEvent> getLogger() {
      return log;
    }
  }
}
