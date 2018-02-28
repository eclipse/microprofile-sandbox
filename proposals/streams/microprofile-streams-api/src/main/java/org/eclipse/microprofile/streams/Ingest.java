package org.eclipse.microprofile.streams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with @Ingest must return a {@link java.util.concurrent.Flow.Subscriber} to consume messages on
 * the methods topic.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ingest {

  /**
   * The topic to be ingested.
   */
  String topic();
}
