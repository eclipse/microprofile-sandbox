package io.openliberty.boost.common.utils;

import io.openliberty.boost.common.BoostLoggerI;

public class CommonLogger implements BoostLoggerI {
    
    private static CommonLogger logger = null;

    public static CommonLogger getInstance() {
        if (logger == null) {
            logger = new CommonLogger();
        }
        return logger;
    }

    @Override
    public void debug(String msg) {
        System.out.println("debug: " + msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        debug(msg);
        debug(e);
    }

    @Override
    public void debug(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void warn(String msg) {
        System.out.println("warn: " + msg);
    }

    @Override
    public void info(String msg) {
        System.out.println("info: " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println("error: " + msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

}