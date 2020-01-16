package org.eclipse.microprofile.problemdetails;

public enum LogLevel {
    /**
     * <code>DEBUG</code> for <code>4xx</code> and <code>ERROR</code> for <code>5xx</code> and anything else.
     */
    AUTO,

    ERROR, WARNING, INFO, DEBUG, OFF
}
