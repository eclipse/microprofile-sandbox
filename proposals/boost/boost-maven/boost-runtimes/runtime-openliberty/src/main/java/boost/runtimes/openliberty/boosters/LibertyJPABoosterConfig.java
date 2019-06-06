/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package boost.runtimes.openliberty.boosters;

import static boost.common.config.ConfigConstants.JPA_21;
import static boost.common.config.ConfigConstants.JPA_22;

import java.util.Map;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.JPABoosterConfig;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

public class LibertyJPABoosterConfig extends JPABoosterConfig implements LibertyBoosterI {

    public LibertyJPABoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
    public String getFeature() {
        if (getVersion().equals(EE_7_VERSION)) {
            return JPA_21;
        } else if (getVersion().equals(EE_8_VERSION)) {
            return JPA_22;
        }
        return null;
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) {

    }
}
