package org.eclipse.microprofile.problemdetails;

import javax.ws.rs.core.Response;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the http status code to be used for the annotated exception.
 * This will also be included as the <code>status</code> field of the
 * problem detail.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Status {
    Response.Status value();
}
