package org.eclipse.microprofile.problemdetails;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated methods or fields are used to build the <code>detail</code>
 * field of the problem detail. Multiple details are joined to a single string
 * delimited by `. `: a period and a space character.
 * <p>
 * Defaults to the message of the exception.
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface Detail {}
