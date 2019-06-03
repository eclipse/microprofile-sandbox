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
package boost.runtimes.tomee.boosters;

import java.util.List;
import java.util.Map;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.MPRestClientBoosterConfig;

public class TomeeMPRestClientBoosterConfig extends MPRestClientBoosterConfig {

    public TomeeMPRestClientBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = super.getDependencies();
        deps.add("org.apache.cxf:cxf-rt-rs-mp-client:3.2.7");
        deps.add("org.eclipse.microprofile.rest.client:microprofile-rest-client-api:1.1");
        return deps;
    }
}

