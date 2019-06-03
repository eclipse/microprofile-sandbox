package io.openliberty.boost.common.utils;

import java.util.HashMap;
import java.util.Map;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.boosters.AbstractBoosterConfig;
import io.openliberty.boost.common.boosters.JDBCBoosterConfig;

public class BoosterUtil {

    public static Map<String, String> createDependenciesWithBoosterAndVersion(Class<?> booster, String version)
            throws BoostException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(AbstractBoosterConfig.getCoordinates(booster), version);
        return map;
    }

    public static Map<String, String> getJDBCDependency() throws BoostException {
        return BoosterUtil.createDependenciesWithBoosterAndVersion(JDBCBoosterConfig.class, "0.1-SNAPSHOT");
    }
}