package org.eclipse.microprofile.streams.impl;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This qualifier gets added automatically by the CDI extension to any parameters that are annotated with
 * {@link org.eclipse.microprofile.streams.Incoming} or {@link org.eclipse.microprofile.streams.Outgoing}.
 *
 * It's used so that the producer will only be used for these.
 */
@Qualifier
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectedStream {
}
