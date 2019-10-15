/*
 */

package org.eclipse.microprofile.logging;

/**
 *
 */
@FunctionalInterface
interface LoggerBuilder {

  public Logger build();
}
