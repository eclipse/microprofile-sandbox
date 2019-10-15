package org.eclipse.microprofile.logging;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.enterprise.inject.spi.CDI;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * Base class containing convenience methods for basic logging methods as well
 * as the logic for Span logging and Span ID population in {@link LogEvent} instances.
 * 
 * <p>
 * Sub-classes providing specific log implementations such as SLF4J, Log4j2, java.util.logging, etc.
 * should only need to implement the abstract methods exposed by this base class I.e. the code
 * related specifically to logging to the logging framework.
 * </p>
 * 
 * @param <T>
 */
public abstract class AbstractLogger<T extends LogEvent> implements Logger<T> {

  private final Supplier<T> supplier;
  private final String name;
  private final Jsonb jsonB;
  
  private Tracer tracer;
  
  public AbstractLogger(final String name, Supplier<T> supplier) {
    this.name = name;
    this.supplier = supplier;
    this.jsonB = JsonbBuilder.create();
    initTracer();
  }
  
  public Supplier<T> getSupplier() {
    return supplier;
  }

  @Override
  public void debug(LogFunction<T> f) {
    log(Level.DEBUG, f);
  }

  @Override
  public void error(LogFunction<T> f) {
    log(Level.ERROR, f);
  }

  @Override
  public void info(LogFunction<T> f) {
    log(Level.INFO, f);
  }

  @Override
  public void trace(LogFunction<T> f) {
    log(Level.TRACE, f);
  }

  @Override
  public void warn(LogFunction<T> f) {
    log(Level.WARN, f);
  }
  
  @Override
  public void span(LogFunction<T> f) {
    privateSpan(f);
  }
  
  @Override
  public void log(Level lvl, LogFunction<T> f) {
    if (isLoggable(lvl)) {
      final T event = getSupplier().get();
      
      // If the log will go to the Span, initialise
      // the log event with the Span ID before calling
      // the log function.
      if (isSpanImplicitLoggable(lvl)) {
        final Span span = tracer.activeSpan();
        event.spanId = span.context().toSpanId();
      }
      
      // Invoke the log function
      event.message = f.log(event);
      
      writeLog(lvl, event);
      
      if (isSpanImplicitLoggable(lvl)) {
        writeSpan(event);
      }
    }
  }
  
  /**
   * Perform Span Logging.
   * 
   * The implementation for Span logging is done here rather than
   * in the {@link #span(org.eclipse.microprofile.logging.LogFunction)) method in order to create a consistent Stack Trace 
   * before calling down into {@link #writeLog(org.eclipse.microprofile.logging.Level, org.eclipse.microprofile.logging.LogEvent))
   * 
   * @param f Log function
   */
  private void privateSpan(LogFunction<T> f) {
    final T event = getSupplier().get();
    
    // Although this method has been called, Span
    // logging may not be available as a Tracer
    // may not exist.
    boolean logFunctionCalled = false;
    if (isSpanAvailable()) {
      logFunctionCalled = true;
      final Span span = tracer.activeSpan();
      event.spanId = span.context().toSpanId();
      event.message = f.log(event);
      writeSpan(event);
    } 
    
    final Level spanLevel = getSpanLevel();
    if (isLoggable(spanLevel)) {
      if (!logFunctionCalled) {
        event.message = f.log(event);
      }
      
      writeLog(spanLevel, event);
    }
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  /**
   * Get the JSON formatted String version of the supplied {@link LogEvent}
   * 
   * @param event The event to convert to a JSON String.
   * 
   * @return The event as a JSON String.
   */
  public String getJsonString(T event) {
    return jsonB.toJson(event);
  }
  
  /**
   * Write the {@link LogEvent} to the Logging subsystem so the event
   * can be written to file, console, etc.
   * 
   * @param lvl The Level of the log statement.
   * @param event The log data.
   */
  public abstract void writeLog(Level lvl, T event);
  
  /**
   * Is Span logging enabled for the given Level
   * 
   * @param lvl The level to compare
   * @return true The level is sufficient and Tracing is available.
   */
  private boolean isSpanAvailable() {
    return tracer != null &&
           tracer.activeSpan() != null;
  }
  
  /**
   * Is Span logging enabled for the given Level
   * 
   * @param lvl The level to compare
   * @return true The level is sufficient and Tracing is available.
   */
  private boolean isSpanImplicitLoggable(Level lvl) {
    return lvl.intValue() >= getSpanImplicitLevel().intValue() &&
           tracer != null &&
           tracer.activeSpan() != null;
  }
  
  /**
   * Write the {@link LogEvent} to Span Logging.
   * 
   * <p>
   * Note: This method expects Tracing to be available. As such, a call
   * to this method must be wrapped in {@link #isSpanLoggable(org.eclipse.microprofile.logging.Level)}
   * </p>
   * 
   * @param event The log data.
   */
  private void writeSpan(T event) {
    final Map<String, ?> fields = build(event);
    final Span span = tracer.activeSpan();
    span.log(fields);
  }
  
  

  /**
   * Convert the {@link LogEvent} data into Span log data.
   * 
   * @param event The event to convert.
   * @return The Span log data.
   */
  private Map<String, ?> build(LogEvent event) {
    final Map<String, Object> fields = new HashMap<>();
    final Throwable thrown = event.throwable;
    if (thrown != null) {
      fields.put(Fields.EVENT, "error");
      fields.put(Fields.ERROR_KIND, "Exception");
      fields.put(Fields.ERROR_OBJECT, thrown);
      fields.put(Fields.MESSAGE, thrown.getMessage());
    } else {
      fields.put(Fields.MESSAGE, event.message);
    }
    return fields;
  }
  
  /**
   * Get the configured Logging Level at which {@link #span(org.eclipse.microprofile.logging.LogFunction)}
   * method calls should be logged at. 
   * 
   * @return The configured Span Log Level.
   */
  private Level getSpanLevel() {
    return Configuration.get(Configuration.SPAN_LEVEL);
  }
  
  /**
   * Get the configured Logging Level at which standard log methods such as debug(), warn(), etc.
   * should also perform span logging.
   * 
   * @return The configured Span Implicit Log Level.
   */
  private Level getSpanImplicitLevel() {
    return Configuration.get(Configuration.SPAN_IMPLICIT_LEVEL);
  }
  
  /**
   * Initialise the OpenTracing Tracer instance, if available.
   */
  private void initTracer() {
    if (tracer == null) {
      try {
        tracer = CDI.current().select(Tracer.class).get();
      } catch (Throwable ise) {
        // Tracer not available. This can be quite likely so,
        // this isn't an issue.
      }
    }
  }
}
