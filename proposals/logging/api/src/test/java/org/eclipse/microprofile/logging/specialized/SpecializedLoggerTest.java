package org.eclipse.microprofile.logging.specialized;

import org.eclipse.microprofile.logging.ExtendedData;
import org.eclipse.microprofile.logging.Level;
import org.eclipse.microprofile.logging.Logger;
import org.eclipse.microprofile.logging.LoggerFactory;
import org.eclipse.microprofile.logging.MockLogger;
import org.eclipse.microprofile.logging.MockLoggerFactory;
import org.eclipse.microprofile.logging.Utils;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WeldJunit5Extension.class)
public class SpecializedLoggerTest {
  
  @WeldSetup
  public WeldInitiator weld = WeldInitiator.from(MockLoggerFactory.class)
                                           .build();
  
  private Logger<SpecializedLogEvent> log;

  public static void beforeAll() {
    // Reduce noise from Weld. 
    java.util.logging.Logger weldLogger = java.util.logging.Logger.getLogger("org.jboss.weld");
    weldLogger.setLevel(java.util.logging.Level.OFF);
  }
  
  @BeforeEach
  public void beforeEach(TestInfo info) {
    // Reset the Logging level before each test.
    Utils.setLoggerLevel(Level.DEBUG);
    
    log = LoggerFactory.getLogger(info.getDisplayName(), new SpecializedLogEventSupplier());
  }
  
  /**
   * Demonstrate a simple log statement that uses a formatted/parameterised message.
   *
   * @param info Test information.
   */
  @Test
  public void testMethodCallToGetData(TestInfo info) {
    log.debug(e -> {
      e.name = getTestName(info);
      return "A log message";
    });
    
    Utils.assertLogCount(log, 1);
  }
  
  /**
   * Demonstrate using a Supplier that prepopulates log event data such that individual log statements need only to set
   * the statement specific data.
   *
   * @param info Test information.
   */
  @Test
  public void testPrepopulatedDataInEvent(TestInfo info) {
    final int expectedVersion = 7;
    final String expectedName = "testname";
    final PrePopulatedSpecializedLogEventSupplier supplier = new PrePopulatedSpecializedLogEventSupplier(expectedVersion, expectedName);
    final Logger<SpecializedLogEvent> prePopEventLogger = LoggerFactory.getLogger(info.getDisplayName(), supplier);
    prePopEventLogger.debug(e -> {
      assertTrue(expectedVersion == e.version, "Version mismatch [" + expectedVersion + "], [" + e.version + "]");
      assertTrue(expectedName.equals(e.name), "Name mismatch [" + expectedName + "], [" + e.name + "]");
      return "Log message";
    });
  }
  
  /**
   * Demonstrate adding/setting additional Logging data into an Application specific log data object.
   *
   * @param info Test information.
   */
  @Test
  public void testDataInSpecializedLogEvent(TestInfo info) {
    final int expectedVersion = 1;
    log.debug(e -> {
      e.version = expectedVersion;
      return "Log message";
    });
    
    Utils.assertLogCount(log, 1);
    final SpecializedLogEvent logEvent = ((MockLogger<SpecializedLogEvent>) log).getEvents().remove(0);
    assertTrue(logEvent.version == expectedVersion, "Version in Event [" + logEvent.version + "] did not match expected version [" + expectedVersion + "]");
  }
  
  /**
   * Demonstrate adding a nested object to a log event that itself should be serialized.
   *
   * @param info Test information.
   */
  @Test
  public void testExtendedData(TestInfo info) {
    log.debug(e -> {
      e.version = 1;
      e.name = info.getDisplayName();
      e.extData = getExtendedData("Sub name", "1.2");
      return "Log message";
    });
  }
  
  private ExtendedData getExtendedData(String subName, String subVersion) {
    final ExtendedData data = new ExtendedData();
    data.subName = subName;
    data.subVersion = subVersion;
    return data;
  }
  
  private String getTestName(TestInfo info) {
    return info.getDisplayName();
  }
}
