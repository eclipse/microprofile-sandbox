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
package boost.runtimes.wildfly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import boost.common.BoostLoggerI;
import boost.common.boosters.JDBCBoosterConfig;
import boost.common.config.BoostProperties;
import boost.common.config.ServerConfigGenerator;
import boost.common.utils.BoostUtil;

/**
 * Create a Liberty server.xml
 *
 */
public class WildlfyServerConfigGenerator implements ServerConfigGenerator {

	private final String DEFAULT_HOSTNAME = "localhost";
	private final String DEFAULT_HTTP_PORT = "8080";
	private final String DEFAULT_HTTPS_PORT = "8443";
	private final String DEFAULT_JNDI_NAME = "java:jboss/datasources/DefaultDataSource";
	
	private final String STANDALONE_XML = "standalone.xml";
	private final String SOCKET_BINDING_ELEMENT = "socket-binding";
	private final String INTERFACE_ELEMENT = "interface";
	private final String INET_ADDRESS_ELEMENT = "inet-address";
	private final String DATASOURCES_ELEMENT = "datasources";
	private final String DATASOURCE_ELEMENT = "datasource";
	private final String CONNECTION_URL_ELEMENT = "connection-url";
	private final String DRIVERS_ELEMENT = "drivers";
	private final String DRIVER_ELEMENT = "driver";
	private final String DRIVER_CLASS_ELEMENT = "driver-class";
	private final String SECURITY_ELEMENT = "security";
	private final String USER_NAME_ELEMENT = "user-name";
	private final String PASSWORD_ELEMENT = "password";
	private final String DEFAULT_BINDINGS_ELEMENT = "default-bindings";
	
	private final String JNDI_NAME_ATTRIBUTE = "jndi-name";
	private final String POOL_NAME_ATTRIBUTE = "pool-name";
	private final String NAME_ATTRIBUTE = "name";
	private final String MODULE_ATTRIBUTE = "module";
	
    private final String wildflyInstallDir;
    private final String configPath;
    private final BoostLoggerI logger;
    private WildflyCli cli;


    public WildlfyServerConfigGenerator(String wildflyInstallDir, BoostLoggerI logger)
            throws ParserConfigurationException {

        this.wildflyInstallDir = wildflyInstallDir;
        this.configPath = wildflyInstallDir + "/standalone/configuration";
        this.logger = logger;
        
        cli = new WildflyCli(wildflyInstallDir, logger);
    }

    @Override
    public void addKeystore(Map<String, String> keystoreProps, Map<String, String> keyProps) {

    }

    @Override
    public void addApplication(String appName) throws Exception {
        cli.run("deploy --force " + appName);
    }

    @Override
    public void addHostname(String hostname) throws Exception {
    	Document doc = getStandaloneDoc();
        
        // Get interfaces
        NodeList interfaceElements = doc.getElementsByTagName(INTERFACE_ELEMENT);

        for (int i = 0; i < interfaceElements.getLength(); i++) {
            Element interfaceElement = (Element) interfaceElements.item(i);
            
            if (interfaceElement.getAttribute("name").equals("public")) {
            	NodeList inetAddresses = interfaceElement.getElementsByTagName(INET_ADDRESS_ELEMENT);
            	Element inetAddress = (Element) inetAddresses.item(0);
            	
            	inetAddress.setAttribute("value", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HOST + ":" + hostname));
            }
        }
        
        writeToStandaloneFile(doc);
    }

    @Override
    public void addHttpPort(String httpPort) throws Exception {
    	Document doc = getStandaloneDoc();
        
        // Get socket-binding elements
        NodeList socketBindings = doc.getElementsByTagName(SOCKET_BINDING_ELEMENT);

        // Set port value to boost variable
        for (int i = 0; i < socketBindings.getLength(); i++) {
            Element socketBinding = (Element) socketBindings.item(i);
            if (socketBinding.getAttribute("name").equals("http")) {
            	socketBinding.setAttribute("port", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HTTP_PORT + ":" + httpPort));
            }
        }
        
        writeToStandaloneFile(doc);
    }

    @Override
    public void addHttpsPort(String httpsPort) throws Exception {
        Document doc = getStandaloneDoc();
        
        // Get socket-binding elements
        NodeList socketBindings = doc.getElementsByTagName(SOCKET_BINDING_ELEMENT);

        // Set port value to boost variable
        for (int i = 0; i < socketBindings.getLength(); i++) {
            Element socketBinding = (Element) socketBindings.item(i);
            if (socketBinding.getAttribute("name").equals("https")) {
            	socketBinding.setAttribute("port", BoostUtil.makeVariable(BoostProperties.ENDPOINT_HTTPS_PORT + ":" + httpsPort));
            }
        }
        
        writeToStandaloneFile(doc);
    }

    @Override
    public void addDataSource(Map<String,String> driverInfo, Properties boostDbProperties) throws Exception {
    	Document doc = getStandaloneDoc();
    	
    	// Get datasources element (only one in doc)
        Element datasources = (Element) doc.getElementsByTagName(DATASOURCES_ELEMENT).item(0);
        
        // Get drivers element (only one child "drivers" elemenet)
        Element drivers = (Element) datasources.getElementsByTagName(DRIVERS_ELEMENT).item(0);
        
    	// Create driver config
        Element driver = doc.createElement(DRIVER_ELEMENT);
        driver.setAttribute(NAME_ATTRIBUTE, driverInfo.get(JDBCBoosterConfig.DRIVER_NAME));
        driver.setAttribute(MODULE_ATTRIBUTE, "boost." + driverInfo.get(JDBCBoosterConfig.DRIVER_NAME));
        
        Element driverClass = doc.createElement(DRIVER_CLASS_ELEMENT);
        driverClass.setTextContent(driverInfo.get(JDBCBoosterConfig.DRIVER_CLASS_NAME));
        
        driver.appendChild(driverClass);
        drivers.appendChild(driver);
        
    	// Create datasource config
        Element datasource = doc.createElement(DATASOURCE_ELEMENT);
        datasource.setAttribute(JNDI_NAME_ATTRIBUTE, DEFAULT_JNDI_NAME);
        datasource.setAttribute(POOL_NAME_ATTRIBUTE, "DefaultDataSource");
        datasource.setAttribute("enabled", "true");
        
        // Add driver
        Element driverRef = doc.createElement(DRIVER_ELEMENT);
        driverRef.setTextContent(driverInfo.get(JDBCBoosterConfig.DRIVER_NAME));
        datasource.appendChild(driverRef);

        // Add security
        String user = (String) boostDbProperties.get(BoostProperties.DATASOURCE_USER);
        String pass = (String) boostDbProperties.get(BoostProperties.DATASOURCE_PASSWORD);
        if (user != null && pass != null) {
        	Element security = doc.createElement(SECURITY_ELEMENT);
        	Element username = doc.createElement(USER_NAME_ELEMENT);
        	Element password = doc.createElement(PASSWORD_ELEMENT);
        	
        	String usernameVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_USER + ":" + user);
            username.setTextContent(usernameVar);
            
            String passwordVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_PASSWORD + ":" + pass);
            password.setTextContent(passwordVar);
            
            security.appendChild(username);
            security.appendChild(password);
            datasource.appendChild(security);
        }

        // Add url
        Element connectionUrl = doc.createElement(CONNECTION_URL_ELEMENT);
        String url = (String) boostDbProperties.get(BoostProperties.DATASOURCE_URL);
        
        if (url != null) {
        	String urlVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_URL + ":" + url);
        	connectionUrl.setTextContent(urlVar);
        	
        } else {
        	
            // Build the url
            StringBuilder jdbcUrl = new StringBuilder();
            jdbcUrl.append("jdbc:" + driverInfo.get(JDBCBoosterConfig.DRIVER_NAME));

            if (driverInfo.get(JDBCBoosterConfig.DRIVER_NAME).equals(JDBCBoosterConfig.DERBY_ARTIFACT_ID)) {

                // Derby's URL is slightly different than MySQL and DB2
                String databaseName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_DATABASE_NAME);
                String databaseNameVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME + ":" + databaseName);
                jdbcUrl.append(":${jboss.server.base.dir}/" + databaseNameVar);

                String createDatabase = (String) boostDbProperties.get(BoostProperties.DATASOURCE_CREATE_DATABASE);
                if ("create".equals(createDatabase)) {
                    jdbcUrl.append(";create=true");
                }
                
            } else {
                String serverName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_SERVER_NAME);
                if (serverName != null) {
                	String serverNameVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_SERVER_NAME + ":" + serverName);
                    jdbcUrl.append("://" + serverNameVar);
                }

                String portNumber = (String) boostDbProperties.get(BoostProperties.DATASOURCE_PORT_NUMBER);
                if (portNumber != null) {
                	String portNumberVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_PORT_NUMBER + ":" + portNumber);
                    jdbcUrl.append(":" + portNumberVar);
                }

                String databaseName = (String) boostDbProperties.get(BoostProperties.DATASOURCE_DATABASE_NAME);
                if (databaseName != null) {
                	String databaseNameVar = BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME + ":" + databaseName);
                    jdbcUrl.append("/" + databaseNameVar);
                }
            }
            
            connectionUrl.setTextContent(jdbcUrl.toString());
        }
        
        datasource.appendChild(connectionUrl);
        datasources.appendChild(datasource);
        
        // Change datasource in default bindings (only one in doc)
        Element defaultBindings = (Element) doc.getElementsByTagName(DEFAULT_BINDINGS_ELEMENT).item(0);
        
        defaultBindings.setAttribute("datasource", DEFAULT_JNDI_NAME);

        writeToStandaloneFile(doc);
    }
    
    @Override
	public String getDefaultHostname() {
		return DEFAULT_HOSTNAME;
	}

	@Override
	public String getDefaultHttpPort() {
		return DEFAULT_HTTP_PORT;
	}

	@Override
	public String getDefaultHttpsPort() {
		return DEFAULT_HTTPS_PORT;
	}
	
	private Document getStandaloneDoc() throws ParserConfigurationException, SAXException, IOException {
		// Read standalone.xml
        File standaloneXml = new File(configPath + "/" + STANDALONE_XML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(standaloneXml);
        
        return doc;
	}
	
	private void writeToStandaloneFile(Document doc) throws TransformerException {
		// Overwrite content
		File standaloneXml = new File(configPath + "/" + STANDALONE_XML);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(standaloneXml);
        transformer.transform(source, result);
	}
	
	public void createJbossWebXml(String path) throws ParserConfigurationException, TransformerException {
		
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        
        Element jbossWeb = doc.createElement("jboss-web");
        doc.appendChild(jbossWeb);
        
        Element contextRoot = doc.createElement("context-root");
        contextRoot.setTextContent("/");
        jbossWeb.appendChild(contextRoot);
        
        File jbossWebFile = new File(path + "/jboss-web.xml");
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(jbossWebFile);
        transformer.transform(source, result);
        
		
//		<?xml version="1.0" encoding="UTF-8"?>
//		<jboss-web xmlns="http://www.jboss.com/xml/ns/javaee"
//		   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//		   xsi:schemaLocation="
//		      http://www.jboss.com/xml/ns/javaee
//		      http://www.jboss.org/j2ee/schema/jboss-web_5_1.xsd">
//		   <context-root>/</context-root>
//		</jboss-web>
	}

}
