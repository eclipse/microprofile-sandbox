package org.eclipse.microprofile.logging;

/**
 * The {@code Logger} interface is the main interface by which applications can send logging data
 * to a Logging Implementation.
 * 
 * <p>
 * The Logging Implementation is abstracted from the Application and is provided by the runtime
 * platform on which the application is hosted, and as such the application does not need to pick
 * a Logging implementation.
 * </p>
 * <p>
 * By using the Logging implementation provided by the runtime, the Application benefits from its
 * Log output being combined with that of the rest of the runtime. Typically, a runtime will also
 * provide a means of managing the format of the log output, the level at which logging is enabled
 * and the specific areas of the runtime and the application that are enabled for log output.
 * </p>
 * 
 * <p>
 * Applications access a Logger instance by using the methods provided by the {@link LoggerFactory}.
 * For Example: {@code Logger log = LoggerFactory.getLogger(Foo.class.getName())}
 * </p>
 * 
 * @param <T> The type of {@link LogEvent} that will be given to a {@link LogFunction} to populate.
 */
public interface Logger<T extends LogEvent> {
  
  /**
   * Log a {@link LogEvent} at the DEBUG level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * Note: This is a convenience method for a call to {@link #log(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogFunction)}
   * Please see that method for further details.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void debug(LogFunction<T> f);

  /**
   * Log a {@link LogEvent} at the ERROR level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * Note: This is a convenience method for a call to {@link #log(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogFunction)}
   * Please see that method for further details.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void error(LogFunction<T> f);

  /**
   * Log a {@link LogEvent} at the INFO level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * Note: This is a convenience method for a call to {@link #log(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogFunction)}
   * Please see that method for further details.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void info(LogFunction<T> f);

  /**
   * Log a {@link LogEvent} at the TRACE level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * Note: This is a convenience method for a call to {@link #log(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogFunction)}
   * Please see that method for further details.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void trace(LogFunction<T> f);

  /**
   * Log a {@link LogEvent} at the WARN level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * Note: This is a convenience method for a call to {@link #log(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogFunction)}
   * Please see that method for further details.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void warn(LogFunction<T> f);
  
  /**
   * Log a {@link LogEvent} to the OpenTracing server, if available.
   * 
   * <p>
   * This method will always send the data to the Tracing server if Tracing is
   * available.
   * </p>
   * 
   * <p>
   * If the {@code mp.logging.span.level} property is set via MicroProfile Config,
   * this method will also send the {@link LogEvent} for standard logging.
   * </p>
   * 
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   */
  void span(LogFunction<T> f);
  
  /**
   * Log a {@link LogEvent} at the specified level.
   * 
   * <p>
   * The {@link LogFunction} is responsible for returning the
   * desired log message and, if appropriate, populating other
   * related data within a {@link LogEvent}.
   * </p>
   * 
   * <p>
   * If the {@code mp.logging.span.implicit} property is set via MicroProfile Config,
   * then all logging at levels greater-than-or-equal-to the Span Implicit level will 
   * also be logged to the Trace server as a Span log, if Tracing is available.
   * </p>
   * 
   * 
   * @param lvl The level at which to log the {@link LogEvent}
   * @param f Function to generate log message and populate a {@link LogEvent} as required.
   * 
   */
  void log(Level lvl, LogFunction<T> f);
  
  /**
   * Is the Logging implementation configured to output log statements
   * at the given Level?
   * 
   * @param lvl The Logging Level
   * @return true if a log statement at the supplied level would be logged.
   */
  boolean isLoggable(Level lvl);
  
  /**
   * Get the name of the Logger
   * 
   * @return The name
   */
  String getName();
}
