package org.eclipse.microprofile.problemdetails;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the type url to be used for the annotated exception.
 * The default is a URN <code>urn:problem-type:[simple-class-name-with-dashes]</code>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Type {
    String value();
}
