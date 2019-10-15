package org.eclipse.microprofile.logging;

/**
 * Interface by which an application obtains access to and populate log event
 * data when a log statement is executed. 
 * 
 * <p>
 * In order for an application to log application specific meta-data, the {@link LogEvent}
 * class should be sub-classed by the application. When a {@link Logger} instance 
 * is accessed/created, the Type of {@link LogEvent} is specified in the {@link Logger}
 * declaration. As such, it is at this point an application specific {@link LogEvent} type
 * can be specified by the application.
 * </p>
 * 
 * <p>
 * E.g.
 * <ul>
 *   <li>{@code Logger log = LoggerFactory.getLogger(info.getDisplayName());} // A logger exposing {@link LogEvent}</li>
 *   <li>{@code Logger<SpecializedLogEvent> log = LoggerFactory.getLogger("logger", new SpecializedLogEventSupplier());} // A logger named "logger", exposing {@code SpecializedLogEvent} instances to log statements.</li>
 * </ul>
 * </p>
 * 
 * @param <T> The Type of {@link LogEvent} this function will expose to the implementation.
 */
@FunctionalInterface
public interface LogFunction<T extends LogEvent> {
  
  String log(T event);
}
