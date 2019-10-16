package org.eclipse.microprofile.logging.specialized;

import javax.json.bind.annotation.JsonbProperty;
import org.eclipse.microprofile.logging.ExtendedData;
import org.eclipse.microprofile.logging.LogEvent;

/**
 * An example of an "application" specific log event that
 * captures additional structured log data.
 */
public class SpecializedLogEvent extends LogEvent {

  @JsonbProperty("v")
  public int version;
  
  public String name;
  
  public ExtendedData extData;
  
  @Override
  public String toString() {
    return "SpecializedLogEvent [" + version + ", " + name + "]";
  }
}
