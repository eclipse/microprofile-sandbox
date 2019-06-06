/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package boost.common.config;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import boost.common.BoostLoggerI;

public final class BoostProperties {

    // HTTP Endpoint properties
    public static final String ENDPOINT_HOST = "boost.http.host";
    public static final String ENDPOINT_HTTP_PORT = "boost.http.port";
    public static final String ENDPOINT_HTTPS_PORT = "boost.http.securePort";

    // Datasource default properties
    public static final String DATASOURCE_PREFIX = "boost.db.";
    public static final String DATASOURCE_DATABASE_NAME = "boost.db.databaseName";
    public static final String DATASOURCE_SERVER_NAME = "boost.db.serverName";
    public static final String DATASOURCE_PORT_NUMBER = "boost.db.portNumber";
    public static final String DATASOURCE_USER = "boost.db.user";
    public static final String DATASOURCE_PASSWORD = "boost.db.password";
    public static final String DATASOURCE_CREATE_DATABASE = "boost.db.createDatabase";
    public static final String DATASOURCE_URL = "boost.db.url";

    public static final String AES_ENCRYPTION_KEY = "boost.aes.key";

    public static final String INTERNAL_COMPILER_TARGET = "boost.internal.compiler.target";

    /**
     * Return a list of all properties that need to be encrypted
     * 
     * @return
     */
    public static Map<String, String> getPropertiesToEncrypt() {
        Map<String, String> propertiesToEncrypt = new HashMap<String, String>();

        // Add default encryption types for properties we define
        propertiesToEncrypt.put(DATASOURCE_PASSWORD, "aes");

        return propertiesToEncrypt;
    }

    public static Properties getConfiguredBoostProperties(BoostLoggerI logger) {
        Properties systemProperties = System.getProperties();

        Properties boostProperties = new Properties();

        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {

            if (entry.getKey().toString().startsWith("boost.")) {

                // logger.debug("Found boost property: " +
                // entry.getKey() + ":" + entry.getValue());

                boostProperties.put(entry.getKey(), entry.getValue());
            }
        }

        return boostProperties;
    }

}
