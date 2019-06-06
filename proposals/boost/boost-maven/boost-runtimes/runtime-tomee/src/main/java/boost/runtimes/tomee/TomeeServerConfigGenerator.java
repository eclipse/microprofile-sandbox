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
package boost.runtimes.tomee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.xml.parsers.ParserConfigurationException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.boosters.JDBCBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.config.ConfigConstants;
import boost.common.utils.BoostUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class TomeeServerConfigGenerator {

    private final String CATALINA_PROPERTIES = "catalina.properties";
    private final String SERVER_XML = "server.xml";
    private final String TOMEE_XML = "tomee.xml";

    private final String CONNECTOR_ELEMENT = "Connector";
    private final String ENGINE_ELEMENT = "Engine";
    private final String HOST_ELEMENT = "Host";
    private final String RESOURCE_ELEMENT = "Resource";

    private final String JDBC_DRIVER_PROPERTY = "JdbcDriver";
    private final String JDBC_URL_PROPERTY = "JdbcUrl";
    private final String USERNAME_PROPERTY = "UserName";
    private final String PASSWORD_PROPERTY = "Password";
    private final String CONNECTION_PROPERTIES_PROPERTY = "connectionProperties";

    private final String configPath;
    private final String tomeeInstallPath;
    private BoostLoggerI logger;

    public TomeeServerConfigGenerator(String configPath, BoostLoggerI logger) throws ParserConfigurationException {

        this.configPath = configPath;
        this.tomeeInstallPath = configPath + "/.."; // one directory back from
                                                    // 'apache-ee/conf'
        this.logger = logger;
    }

    public void addServerConfig(AbstractBoosterConfig boosterConfig) throws Exception {
        if (boosterConfig instanceof JDBCBoosterConfig) {
            addDataSource(((JDBCBoosterConfig)boosterConfig).getProductName(), ((JDBCBoosterConfig)boosterConfig).getDatasourceProperties());
        }
    }

    public void addJarsDirToSharedLoader() throws ParserConfigurationException {
        try {

            // User common.loader here.. some of our dependency jars are needed
            // for applications and others are needed for the server. Using
            // common.loader will provide for both cases
            BufferedReader file = new BufferedReader(new FileReader(configPath + "/" + CATALINA_PROPERTIES));
            String line;
            String replaceString = "common.loader=";
            String replaceWithString = "common.loader=\"${catalina.home}/boost\",\"${catalina.home}/"
                    + ConfigConstants.TOMEEBOOST_JAR_DIR + "/*.jar\",";
            StringBuffer inputBuffer = new StringBuffer();

            createTomeeBoostJarDir();

            while ((line = file.readLine()) != null) {
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }

            String inputStr = inputBuffer.toString();
            file.close();

            logger.debug("catalina.properties before: \n" + inputStr);
            inputStr = inputStr.replace(replaceString, replaceWithString);
            logger.debug("catalina.properties after: \n" + inputStr);

            // write the new String with the replaced line OVER the same file
            FileOutputStream fileOut = new FileOutputStream(configPath + "/" + CATALINA_PROPERTIES);
            fileOut.write(inputStr.getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }
    }

    private void createTomeeBoostJarDir() {
        File dir = new File(tomeeInstallPath + "/" + ConfigConstants.TOMEEBOOST_JAR_DIR);

        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (successful) {
            // creating the directory succeeded
            System.out.println("directory was created successfully");
        } else {
            // creating the directory failed
            System.out.println("failed trying to create the directory");
        }
    }

    public void addHttpPort(String httpPort) throws Exception {

        // Read server.xml
        File serverXml = new File(configPath + "/" + SERVER_XML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(serverXml);
        doc.getDocumentElement().normalize();

        // Get Connector element
        NodeList connectors = doc.getElementsByTagName(CONNECTOR_ELEMENT);

        // Set port value to boost variable
        for (int i = 0; i < connectors.getLength(); i++) {
            Element connector = (Element) connectors.item(i);
            if (connector.getAttribute("protocol").equals("HTTP/1.1")) {
                connector.setAttribute("port", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HTTP_PORT));
            }
        }

        // Overwrite content
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(serverXml);
        transformer.transform(source, result);

        // Set boost.http.port in catalina.properties
        addCatalinaProperty(BoostProperties.ENDPOINT_HTTP_PORT, httpPort);
    }

    public void addHttpsPort(String httpsPort) throws Exception {
        // Not supported yet
    }

    public void addHostname(String hostname) throws Exception {

        // Read server.xml
        File serverXml = new File(configPath + "/" + SERVER_XML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(serverXml);
        doc.getDocumentElement().normalize();

        // Get Engine element
        NodeList engines = doc.getElementsByTagName(ENGINE_ELEMENT);

        // Set defaultHost value to boost variable
        for (int i = 0; i < engines.getLength(); i++) {
            Element engine = (Element) engines.item(i);
            engine.setAttribute("defaultHost", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HOST));
        }

        // Get Host element
        NodeList hosts = doc.getElementsByTagName(HOST_ELEMENT);

        // Set defaultHost value to boost variable
        for (int i = 0; i < hosts.getLength(); i++) {
            Element host = (Element) hosts.item(i);
            host.setAttribute("name", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HOST));
        }

        // Overwrite content
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(serverXml);
        transformer.transform(source, result);

        // Set boost.http.port in catalina.properties
        addCatalinaProperty(BoostProperties.ENDPOINT_HOST, hostname);
    }

    public void addApplication(String appName) {
        // Not currently used
    }

    public void addKeystore(Map<String, String> keystoreProps, Map<String, String> keyProps) {
        // No keystore support yet
    }

    public void addDataSource(String productName, Properties boostDbProperties) throws Exception {
        // Read tomee.xml
        File tomeeXml = new File(configPath + "/" + TOMEE_XML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(tomeeXml);
        Node tomee = doc.getFirstChild();

        // Create Resource element
        Element resource = doc.createElement(RESOURCE_ELEMENT);
        resource.setAttribute("id", "DefaultDataSource");
        resource.setAttribute("type", "DataSource");
        resource.appendChild(doc.createTextNode(System.lineSeparator()));
        tomee.appendChild(resource);

        // Add driver class name
        String driverClassName = "";
        if (productName.equals(JDBCBoosterConfig.DERBY)) {
            driverClassName = JDBCBoosterConfig.DERBY_DRIVER_CLASS_NAME;
        } else if (productName.equals(JDBCBoosterConfig.DB2)) {
            driverClassName = JDBCBoosterConfig.DB2_DRIVER_CLASS_NAME;
        } else if (productName.equals(JDBCBoosterConfig.MYSQL)) {
            driverClassName = JDBCBoosterConfig.MYSQL_DRIVER_CLASS_NAME;
        }
        Text jdbcDriverText = doc.createTextNode(JDBC_DRIVER_PROPERTY + " = " + driverClassName + System.lineSeparator());
        resource.appendChild(jdbcDriverText);
        

        // Add UserName if set. Remove from list to avoid adding it again below
        String username = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_USER);
        if (username != null) {
            Text usernameText = doc.createTextNode(
                    USERNAME_PROPERTY + " = " + BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER) + System.lineSeparator());
            resource.appendChild(usernameText);

            addCatalinaProperty(BoostProperties.DATASOURCE_USER, username);
        }

        // Add Password if set. Remove from list to avoid adding it again below
        String password = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_PASSWORD);
        if (password != null) {
            Text passwordText = doc.createTextNode(
                    PASSWORD_PROPERTY + " = " + BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD) + System.lineSeparator());
            resource.appendChild(passwordText);

            
            addCatalinaProperty(BoostProperties.DATASOURCE_PASSWORD, password);
        }

        // Tomee requires a url for datasource configuration. If one was not
        // set,
        // we need to build it based on the separate datasource properties
        // configured.
        String url = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_URL);
        if (url != null) {
            Text jdbcUrlText = doc
                    .createTextNode(JDBC_URL_PROPERTY + " = " + BoostUtil.makeVariable(BoostProperties.DATASOURCE_URL) + System.lineSeparator());
            resource.appendChild(jdbcUrlText);

            addCatalinaProperty(BoostProperties.DATASOURCE_URL, url);
        } else {

            // Build the url
            StringBuilder jdbcUrl = new StringBuilder();
            jdbcUrl.append("jdbc:" + productName);

            if (productName.equals(JDBCBoosterConfig.DERBY)) {

                // Derby's URL is slightly different than MySQL and DB2
                String databaseName = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_DATABASE_NAME);
                jdbcUrl.append(":" + BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME));
                addCatalinaProperty(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);

                String createDatabase = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_CREATE_DATABASE);
                if ("create".equals(createDatabase)) {
                    jdbcUrl.append(";create=true");
                }
            } else {

                String serverName = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_SERVER_NAME);
                if (serverName != null) {
                    jdbcUrl.append("://" + BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME));
                    addCatalinaProperty(BoostProperties.DATASOURCE_SERVER_NAME, serverName);
                }

                String portNumber = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_PORT_NUMBER);
                if (portNumber != null) {
                    jdbcUrl.append(":" + BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER));
                    addCatalinaProperty(BoostProperties.DATASOURCE_PORT_NUMBER, portNumber);
                }

                String databaseName = (String) boostDbProperties.remove(BoostProperties.DATASOURCE_DATABASE_NAME);
                if (databaseName != null) {
                    jdbcUrl.append("/" + BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME));
                    addCatalinaProperty(BoostProperties.DATASOURCE_DATABASE_NAME, databaseName);
                }
            }

            Text jdbcUrlText = doc.createTextNode(JDBC_URL_PROPERTY + " = " + jdbcUrl.toString() + System.lineSeparator());
            resource.appendChild(jdbcUrlText);
        }

        // Add any additional datasource properties as connectionProperties
        StringBuilder connectionProperties = new StringBuilder();
        for (String boostProperty : boostDbProperties.stringPropertyNames()) {
            String datasourceProperty = boostProperty.replace(BoostProperties.DATASOURCE_PREFIX, "");
            connectionProperties.append(datasourceProperty);
            connectionProperties.append("=");
            connectionProperties.append(BoostUtil.makeVariable(boostProperty));
            connectionProperties.append(";");

            addCatalinaProperty(boostProperty, boostDbProperties.getProperty(boostProperty));
        }

        Text connectionPropertiesText = doc
                .createTextNode(CONNECTION_PROPERTIES_PROPERTY + " = [" + connectionProperties.toString() + "]"   + System.lineSeparator());
        resource.appendChild(connectionPropertiesText);

        // Overwrite content
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(tomeeXml);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(source, result);

    }

    private void addCatalinaProperty(String key, String value) throws IOException {

        BufferedWriter output = new BufferedWriter(new FileWriter(configPath + "/" + CATALINA_PROPERTIES, true));
        output.newLine();
        output.append(key + "=" + value);
        output.close();
    }

}
