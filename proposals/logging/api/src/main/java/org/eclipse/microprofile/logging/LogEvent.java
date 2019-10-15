package org.eclipse.microprofile.logging;

import javax.json.bind.annotation.JsonbTransient;

/**
 * A base event containing simple data for a logging event.
 * 
 * <p>
 * If an application wishes to add extra structured data to the
 * logging statements, subclasses can be created to hold those
 * additional properties. 
 * </p>
 * 
 * <p>
 * This Logging framework requires the use of JSON-B {@link http://json-b.net/}
 * to serialize a LogEvent or sub-classes. As such, subclasses
 * must adhere to the default serialization mechanism of JSON-B; namely:
 * <ul>
 *  <li>Getters take precedence over members.</li>
 *  <li>If getter methods exist, those should be annotated to control serialization, if desired.</li>
 *  <li>Public members can be used instead of setter/getters. If public members are used, they
 *      should be annotated to control serialization.</li>
 * </ul>
 * </p>
 */
public class LogEvent {
  /** 
   * The log event message
   */
  public String message;
  
  /** 
   * The ID of the Span in which the log statement occurred. 
   */
  public String spanId;
  
  /** 
   * The associated Throwable (or sub-class) if applicable to the log statement.
   * 
   * Note: This property will not be serialized into the resulting JSON as the Throwable
   * is passed separately to both the log framework and span logging as appropriate.
   */
  @JsonbTransient
  public Throwable throwable;
}
