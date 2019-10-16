package org.eclipse.microprofile.logging.specialized;

import java.util.function.Supplier;

/**
 * An example supplier that can be used to prepopulate "application" specific 
 * structured log data rather than every log statement having to fill in that
 * data.
 */
public class PrePopulatedSpecializedLogEventSupplier implements Supplier<SpecializedLogEvent> {

  private final int version;

  private final String name;

  public PrePopulatedSpecializedLogEventSupplier(int version, String name) {
    this.version = version;
    this.name = name;
  }

  @Override
  public SpecializedLogEvent get() {
    final SpecializedLogEvent event = new SpecializedLogEvent();
    event.version = version;
    event.name = name;
    return event;
  }
}
