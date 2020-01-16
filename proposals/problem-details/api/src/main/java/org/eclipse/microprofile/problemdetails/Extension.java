package org.eclipse.microprofile.problemdetails;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated methods or fields are used to build additional properties of the problem detail.
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface Extension {
    /**
     * Defaults to the field/method name
     */
    String value() default "";
}
