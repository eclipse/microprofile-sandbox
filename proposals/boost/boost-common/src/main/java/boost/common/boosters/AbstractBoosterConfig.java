/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package boost.common.boosters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import boost.common.BoostException;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public abstract class AbstractBoosterConfig {

    public static final String RUNTIMES_GROUP_ID = "boost.runtimes";
    public static final String BOOSTERS_GROUP_ID = "boost.boosters";
    public static final String EE_7_VERSION = "0.1-SNAPSHOT";
    public static final String EE_8_VERSION = "0.2-SNAPSHOT";
    public static final String MP_20_VERSION = "0.2-SNAPSHOT";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface BoosterCoordinates {
        String value();
    }

    public static String getCoordinates(Class<?> klass) throws BoostException {
        BoosterCoordinates coordinates = klass.getAnnotation(BoosterCoordinates.class);
        if (coordinates == null) {
            throw new BoostException(
                    String.format("class '%s' must have a BoosterCoordinates annotation", klass.getName()));
        }
        return coordinates.value();
    }

    private final String version;

    protected AbstractBoosterConfig(String version) {
        this.version = version;
    }

    /**
     * Return the dependency that this booster requires
     * 
     * @return
     */
    public abstract List<String> getDependencies();

    public String getVersion() {
        return version;
    }

}
