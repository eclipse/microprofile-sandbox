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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import boost.common.config.BoostProperties;
import boost.common.config.ServerConfigGenerator;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":http")
public class HTTPEndpointBoosterConfig extends AbstractBoosterConfig {
	
	BoostLoggerI logger;

    public HTTPEndpointBoosterConfig(BoostLoggerI logger) throws BoostException {
        super(null);
        
        this.logger = logger;
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }

	@Override
	public void addServerConfig(ServerConfigGenerator configGenerator) throws Exception {
		 // Add default http endpoint configuration
        Properties boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);

        String host = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HOST, configGenerator.getDefaultHostname());
        configGenerator.addHostname(host);

        String httpPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTP_PORT, configGenerator.getDefaultHttpPort());
        configGenerator.addHttpPort(httpPort);

        String httpsPort = (String) boostConfigProperties.getOrDefault(BoostProperties.ENDPOINT_HTTPS_PORT, configGenerator.getDefaultHttpsPort());
        configGenerator.addHttpsPort(httpsPort);
	}
}
