package org.eclipse.microprofile.logging;

import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Logging Configuration Items.
 */
public class Configuration {

  /**
   * The logging level at which calls to {@link Logger#span(org.eclipse.microprofile.logging.LogFunction)}
   * will also be send to the Logging Framework.
   * 
   * <p>
   * This item's:
   * <ul>
   *  <li>Key: {@code mp.logging.span.level}</li>
   *  <li>Default Value: {@link Level#DEBUG}.</li>
   * </ul>
   * </p>
   */
  public static final Item SPAN_LEVEL = new Item("mp.logging.span.level", Level.DEBUG);
  
  /**
   * The logging level at which the {@link LogEvent} data will also be sent to Span logging.
   * 
   * <p>
   * This item's:
   * <ul>
   *  <li>Key: {@code mp.logging.span.implicit}</li>
   *  <li>Default Value: {@link Level#OFF}.</li>
   * </ul>
   * </p>
   */
  public static final Item SPAN_IMPLICIT_LEVEL = new Item("mp.logging.span.implicit", Level.OFF);
  
  /**
   * Utility method to get a configuration from MicoProfile Config.
   * 
   * @param configItem The Configuration Item to retrieve.
   * 
   * @return The configuration value (or its default).
   */
  public static Level get(Configuration.Item configItem) {
    Level returnLevel = configItem.getDefault();
    Optional<String> configValue = null;
    try {
      Config config = ConfigProvider.getConfig();
      configValue = config.getOptionalValue(configItem.getKey(), String.class);
    } catch (Throwable t) {
      // Do nothing as MP Config is an optional component.
    }

    if (configValue != null && configValue.isPresent()) {
      try {
        returnLevel = Level.parse(configValue.get());
      } catch (IllegalArgumentException iae) {
        // Unable to find level with configured name
      }
    }
    
    return returnLevel;
  }
  
  public static class Item {
    private final String key;
    private final Level defaultVal;
  
    private Item(String key, Level defaultVal) {
      this.key = key;
      this.defaultVal = defaultVal;
    }

    public String getKey() {
      return key;
    }

    public Level getDefault() {
      return defaultVal;
    }
  }
}
