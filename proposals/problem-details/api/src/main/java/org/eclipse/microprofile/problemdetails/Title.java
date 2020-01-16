package org.eclipse.microprofile.problemdetails;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the title string to be used for the annotated exception.
 * The default is derived from the simple class name by splitting the
 * camel case name into words.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Title {
    String value();
}
