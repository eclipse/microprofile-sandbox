package org.eclipse.microprofile.logging.span;

import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import javax.enterprise.inject.spi.Bean;
import org.eclipse.microprofile.logging.Configuration;
import org.eclipse.microprofile.logging.LazyEval;
import org.eclipse.microprofile.logging.Level;
import org.eclipse.microprofile.logging.Logger;
import org.eclipse.microprofile.logging.LoggerFactory;
import org.eclipse.microprofile.logging.MockLoggerFactory;
import org.eclipse.microprofile.logging.Utils;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@ExtendWith(WeldJunit5Extension.class)
public class SpanLoggingTest {
  
  private static MockTracer TRACER = new MockTracer();
  
  @WeldSetup
  public WeldInitiator weld = WeldInitiator.from(Tracer.class,
                                                 MockLoggerFactory.class)
                                           .addBeans(createTracer())
                                           .build();

  /**
   * Enable CDI (Weld) to return the test (Mock)Tracer
   *
   * @return The Tracer instance
   */
  static Bean<?> createTracer() {
    TRACER = new MockTracer();
    return MockBean.builder()
            .types(Tracer.class)
            .creating(TRACER)
            .build();
  }
  
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
  
  @AfterEach
  public void afterEach() {
    TRACER.close();
  }
  
  /**
   * Test a Span log statement goes to the Tracer/Span.
   *
   * @param info Test information.
   */
  @Test
  public void testSpan(TestInfo info) {
    setSpanImplicitLevel(Level.OFF);
    
    final MockSpan span = initSpan(info.getDisplayName());
    log.span(e -> "Span Message");

    Utils.assertNotEmpty(log);
  }

  /**
   * Test that Span logging can be disabled regardless of the log level.
   *
   * @param info Test information
   */
  @Test
  public void testLogEnabledSpanDisabled(TestInfo info) {
    Utils.setLoggerLevel(Level.DEBUG);
    setSpanImplicitLevel(Level.OFF);

    LazyEval lazy = mock(LazyEval.class);
    when(lazy.evaluate()).thenReturn("lazy");

    final MockSpan span = initSpan(info.getDisplayName());
    log.debug(e -> {
      lazy.evaluate();
      return "Log Message";
    });

    assertTrue(span.logEntries().isEmpty(), "Unexpected Log Entries");

    verify(lazy).evaluate();
    
    Utils.assertLogCount(log, 1);
  }

  /**
   * Test that Span Implicit logging will output to the Span when using the equivalent standard logging method (I.e.
   * .debug() also logs to Span).
   *
   * @param info Test information
   */
  @Test
  public void testSpanImplicit(TestInfo info) {
    setSpanImplicitLevel(Level.DEBUG);

    LazyEval lazy = mock(LazyEval.class);
    when(lazy.evaluate()).thenReturn("lazy");

    final MockSpan span = initSpan(info.getDisplayName());
    log.debug(e -> {
      lazy.evaluate();
      assertNotNull(e.spanId, "Span ID was null");
      return String.format("Span ID Log Message [%s]", e.spanId);
    });

    // Check the Span received logging.
    assertFalse(span.logEntries().isEmpty(), "Expected Log Entries");

    verify(lazy).evaluate();
    
    // The logger should also have received logging.
    Utils.assertLogCount(log, 1);
  }

  /**
   * Test that if the Span level is "higher", it will be logged for multiple log levels.
   *
   * @param info Test information
   */
  @Test
  public void testSpanImplicitAtMultipleLevels(TestInfo info) {
    final int expectedEntriesCount = 3;
    Utils.setLoggerLevel(Level.DEBUG);
    setSpanImplicitLevel(Level.DEBUG);

    LazyEval lazy = mock(LazyEval.class);
    when(lazy.evaluate()).thenReturn("lazy");

    final MockSpan span = initSpan(info.getDisplayName());

    log.debug(e -> {
      lazy.evaluate();
      return "Debug Log Message";
    });

    log.warn(e -> {
      lazy.evaluate();
      return "Warn Log Message";
    });

    log.info(e -> {
      lazy.evaluate();
      return "Info Log Message";
    });

    assertTrue(span.logEntries().size() == expectedEntriesCount, "Unexpected number of Log Entries[" + span.logEntries().size() + "]");

    verify(lazy, times(expectedEntriesCount)).evaluate();
    
    Utils.assertLogCount(log, expectedEntriesCount);
  }

  /**
   * Demonstrate a Span log statement that goes to the Tracer/Span and to the standard log output if the Span level is
   * configured.
   *
   * @param info Test information.
   */
  @Test
  public void testSpanAndLog(TestInfo info) {
    setSpanLevel(Level.DEBUG);
    
    final MockSpan span = initSpan(info.getDisplayName());
    log.span(e -> "Span and Log Message");

    assertFalse(span.logEntries().isEmpty(), "Expected Log Entries");
    
    Utils.assertLogCount(log, 1);
  }

  /**
   * Initialise a Span that will receive logging data.
   *
   * @param name The name of the Span
   *
   * @return The initialised Span
   */
  private MockSpan initSpan(String name) {
    // Create a Tracer and an active Span for each test
    // to (possibly) use.
    final MockSpan span = TRACER.buildSpan(name).start();
    TRACER.activateSpan(span);
    assertNotNull(TRACER.activeSpan());

    return span;
  }
  
  private void setSpanLevel(Level lvl) {
    System.setProperty(Configuration.SPAN_LEVEL.getKey(), lvl.getName());
  }

  private void setSpanImplicitLevel(Level lvl) {
    System.setProperty(Configuration.SPAN_IMPLICIT_LEVEL.getKey(), lvl.getName());
  }
}
