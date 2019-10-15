package org.eclipse.microprofile.logging.specialized;

import java.util.function.Supplier;

public class SpecializedLogEventSupplier implements Supplier<SpecializedLogEvent> {

  @Override
  public SpecializedLogEvent get() {
    return new SpecializedLogEvent();
  }

}
