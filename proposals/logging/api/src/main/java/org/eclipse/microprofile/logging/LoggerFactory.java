package org.eclipse.microprofile.logging;

import java.util.function.Supplier;
import javax.enterprise.inject.spi.CDI;

/**
 * Utility class for accessing {@link Logger} instances.
 */
public class LoggerFactory {

  private static final NoOpLoggerFactory NOOPFACTORY = new NoOpLoggerFactory();

  private static class FactoryHolder {
    private static LoggerFactoryProvider initFactory() {
      try {
        return CDI.current().select(LoggerFactoryProvider.class).get();
      } catch (Throwable t) {
        // no factory
      }
      return NOOPFACTORY;
    }
    
    static final LoggerFactoryProvider FACTORY = initFactory();
  }

  /**
   * Get a {@link Logger}
   *
   * <p>
   * Logger instances accessed this way will supply {@link LogEvent} instances to the {@link LogFunction} provided to
   * the various Logger methods.
   * </p>
   *
   * <p>
   * The name of the Logger will be inferred by using the name of the calling Class. This is effectively the same as
   * calling {@code LoggerFactory.getLogger(Foo.class.getName())
   * whilst within the Foo.class.
   * </p>
   *
   * @return The named Logger
   */
  public static Logger<LogEvent> getLogger() {
    return getFactory().getLogger();
  }

  /**
   * Get a {@link Logger} that will provide instances of the {@link LogEvent} sub-class to the {@link LogFunction}
   * provided the various Logger methods.
   *
   * <p>
   * The {@link Supplier} is responsible for generating instances of the LogEvent sub-class.
   * </p>
   *
   * <p>
   * The name of the Logger will be inferred in the same way as {@link LoggerFactory#getLogger()}
   * </p>
   *
   * @param <T> The LogEvent sub-class type.
   * @param supplier The {@link Supplier} instance responsible for creating instances of the LogEvent sub-class.
   *
   * @return The named Logger
   */
  public static <T extends LogEvent> Logger<T> getLogger(Supplier<T> supplier) {
    return getFactory().getLogger(supplier);
  }

  /**
   * Get a {@link Logger} for the given name
   *
   * <p>
   * Logger instances accessed this way will supply {@link LogEvent} instances to the {@link LogFunction} provided to
   * the various Logger methods.
   * </p>
   *
   * @param name The name to use for the Logger.
   *
   * @return The named Logger
   */
  public static Logger<LogEvent> getLogger(String name) {
    return getFactory().getLogger(name);
  }

  /**
   * Get a {@link Logger} for the given name, that will provide instances of the {@link LogEvent} sub-class to the
   * {@link LogFunction} provided the various Logger methods.
   *
   * <p>
   * The {@link Supplier} is responsible for generating instances of the LogEvent sub-class.
   * </p>
   *
   * <p>
   * The name of the Logger will be inferred in the same way as {@link LoggerFactory#getLogger()}
   * </p>
   *
   * @param <T> The LogEvent sub-class type.
   * @param name The name to use for the Logger.
   * @param supplier The {@link Supplier} instance responsible for creating instances of the LogEvent sub-class.
   *
   * @return The named Logger
   */
  public static <T extends LogEvent> Logger<T> getLogger(String name, Supplier<T> supplier) {
    return getFactory().getLogger(name, supplier);
  }
  
  private static LoggerFactoryProvider getFactory() {
    return FactoryHolder.FACTORY;
  }
}
