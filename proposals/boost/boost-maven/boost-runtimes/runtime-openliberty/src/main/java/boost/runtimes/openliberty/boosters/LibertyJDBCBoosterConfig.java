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

import static boost.common.config.ConfigConstants.*;

import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.JDBCBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.utils.BoostUtil;
import boost.runtimes.openliberty.LibertyServerConfigGenerator;
import boost.runtimes.openliberty.boosters.LibertyBoosterI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LibertyJDBCBoosterConfig extends JDBCBoosterConfig implements LibertyBoosterI {

    public LibertyJDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies, logger);

    }

    @Override
    public String getFeature() {
        String compilerVersion = System.getProperty(BoostProperties.INTERNAL_COMPILER_TARGET);

        if ("1.8".equals(compilerVersion) || "8".equals(compilerVersion) || "9".equals(compilerVersion)
                || "10".equals(compilerVersion)) {
            return JDBC_42;
        } else if ("11".equals(compilerVersion)) {
            return JDBC_43;
        } else {
            return JDBC_41; // Default to the spec for Liberty's
                            // minimum supported JRE (version 7
                            // as of 17.0.0.3)
        }
    }

    @Override
    public void addServerConfig(LibertyServerConfigGenerator libertyServerConfigGenerator) throws BoostException {
        try {
            addDataSource(getProductName(), getDatasourceProperties(), libertyServerConfigGenerator,
                    libertyServerConfigGenerator.getServerXmlDoc());
        } catch (Exception e) {
            throw new BoostException("Error when configuring JDBC data source.");
        }
    }

    private void addDataSource(String productName, Properties serverProperties,
            LibertyServerConfigGenerator libertyServerConfigGenerator, Document serverXml) throws Exception {

        String driverJar = null;
        String datasourcePropertiesElement = null;

        if (productName.equals(JDBCBoosterConfig.DERBY)) {
            driverJar = DERBY_JAR;
            datasourcePropertiesElement = PROPERTIES_DERBY_EMBEDDED;
        } else if (productName.equals(JDBCBoosterConfig.DB2)) {
            driverJar = DB2_JAR;
            datasourcePropertiesElement = PROPERTIES_DB2_JCC;
        } else if (productName.equals(JDBCBoosterConfig.MYSQL)) {
            driverJar = MYSQL_JAR;
            datasourcePropertiesElement = PROPERTIES;
        }

        Element serverRoot = serverXml.getDocumentElement();

        // Find the root server element
        NodeList list = serverXml.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeName().equals("server")) {
                serverRoot = (Element) list.item(i);
            }
        }

        // Add library
        Element lib = serverXml.createElement(LIBRARY);
        lib.setAttribute("id", JDBC_LIBRARY_1);
        Element fileLoc = serverXml.createElement(FILESET);
        fileLoc.setAttribute("dir", RESOURCES);
        fileLoc.setAttribute("includes", driverJar);
        lib.appendChild(fileLoc);
        serverRoot.appendChild(lib);

        // Add datasource
        Element dataSource = serverXml.createElement(DATASOURCE);
        dataSource.setAttribute("id", DEFAULT_DATASOURCE);
        dataSource.setAttribute(JDBC_DRIVER_REF, JDBC_DRIVER_1);

        // Add all configured datasource properties
        Element props = serverXml.createElement(datasourcePropertiesElement);
        addDatasourceProperties(serverProperties, props);
        dataSource.appendChild(props);

        serverRoot.appendChild(dataSource);

        // Add jdbc driver
        Element jdbcDriver = serverXml.createElement(JDBC_DRIVER);
        jdbcDriver.setAttribute("id", JDBC_DRIVER_1);
        jdbcDriver.setAttribute(LIBRARY_REF, JDBC_LIBRARY_1);
        serverRoot.appendChild(jdbcDriver);

        // Add properties to bootstrap.properties
        libertyServerConfigGenerator.addBootstrapProperties(serverProperties);
    }

    private void addDatasourceProperties(Properties serverProperties, Element propertiesElement) {
        for (String property : serverProperties.stringPropertyNames()) {
            String attribute = property.replace(BoostProperties.DATASOURCE_PREFIX, "");
            propertiesElement.setAttribute(attribute, BoostUtil.makeVariable(property));
        }
    }
}
