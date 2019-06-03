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
package io.openliberty.boost.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import boost.runtimes.LibertyServerConfigGenerator;

import io.openliberty.boost.common.utils.BoostUtil;
import io.openliberty.boost.common.utils.CommonLogger;
import io.openliberty.boost.common.utils.ConfigFileUtils;
import io.openliberty.boost.common.BoostLoggerI;
import io.openliberty.boost.common.boosters.JDBCBoosterConfig;

import static io.openliberty.boost.common.config.ConfigConstants.*;
import static io.openliberty.boost.common.utils.DOMUtils.getDirectChildrenByTag;

public class LibertyServerConfigGeneratorTest {

    @Rule
    public TemporaryFolder outputDir = new TemporaryFolder();

    BoostLoggerI logger = CommonLogger.getInstance();

    private final String MYSQL_URL = "jdbc:mysql://localhost:3306/testdb";
    private final String DB2_URL = "jdbc:db2://localhost:50000/testdb";

    /**
     * Test adding feature
     *
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddSpringFeature() throws ParserConfigurationException, TransformerException, IOException {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);
        serverConfig.addFeature(SPRING_BOOT_15);
        serverConfig.writeToServer();

        String serverXML = outputDir.getRoot().getAbsolutePath() + "/server.xml";

        boolean featureFound = ConfigFileUtils.findStringInServerXml(serverXML,
                "<feature>" + SPRING_BOOT_15 + "</feature>");

        assertTrue("The " + SPRING_BOOT_15 + " feature was not found in the server configuration", featureFound);

    }

    /**
     * Test that the server.xml and boostrap.properties are fully configured
     * with the Derby datasource
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddDatasource_Derby() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Add basic properties
        Properties datasourceProperties = new Properties();
        datasourceProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
        datasourceProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");

        serverConfig.addDataSource(JDBCBoosterConfig.DERBY, datasourceProperties);
        serverConfig.writeToServer();

        // Parse server.xml
        File serverXml = new File(outputDir.getRoot().getAbsolutePath() + "/server.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(serverXml);

        Element serverRoot = doc.getDocumentElement();

        // Check that the <library> element is correctly configured
        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
        assertEquals("Didn't find one and only one library", 1, libraryList.size());

        Element library = libraryList.get(0);
        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));

        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
        assertEquals("Fileset includes attribute is not correct", DERBY_JAR, fileset.getAttribute("includes"));

        // Check that the <dataSource> element is correctly configured
        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());

        Element dataSource = dataSourceList.get(0);
        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
                dataSource.getAttribute(JDBC_DRIVER_REF));

        List<Element> propertiesDerbyEmbeddedList = getDirectChildrenByTag(dataSource, PROPERTIES_DERBY_EMBEDDED);
        assertEquals("Didn't find one and only one " + PROPERTIES_DERBY_EMBEDDED, 1,
                propertiesDerbyEmbeddedList.size());

        Element propertiesDerbyEmbedded = propertiesDerbyEmbeddedList.get(0);
        assertEquals("The createDatabase attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_CREATE_DATABASE),
                propertiesDerbyEmbedded.getAttribute(CREATE_DATABASE));
        assertEquals("The databaseName attribute is not correct",
                BoostUtil.makeVariable(BoostProperties.DATASOURCE_DATABASE_NAME),
                propertiesDerbyEmbedded.getAttribute(DATABASE_NAME));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());

        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));

        // Check bootstrap.properties content
        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String databaseNameFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_DATABASE_NAME);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_DATABASE_NAME
                + " is not correct", DERBY_DB, databaseNameFound);

        String createFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_CREATE_DATABASE);

        assertEquals("The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_CREATE_DATABASE
                + " is not correct", "create", createFound);
    }

    /**
     * Test that the server.xml and boostrap.properties are fully configured
     * with the DB2 datasource
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddDatasource_DB2() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Add basic properties
        Properties datasourceProperties = new Properties();
        datasourceProperties.put(BoostProperties.DATASOURCE_URL, DB2_URL);

        serverConfig.addDataSource(JDBCBoosterConfig.DB2, datasourceProperties);
        serverConfig.writeToServer();

        // Parse server.xml
        File serverXml = new File(outputDir.getRoot().getAbsolutePath() + "/server.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(serverXml);

        Element serverRoot = doc.getDocumentElement();

        // Check that the <library> element is correctly configured
        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
        assertEquals("Didn't find one and only one library", 1, libraryList.size());

        Element library = libraryList.get(0);
        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));

        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
        assertEquals("Fileset includes attribute is not correct", DB2_JAR, fileset.getAttribute("includes"));

        // Check that the <dataSource> element is correctly configured
        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());

        Element dataSource = dataSourceList.get(0);
        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
                dataSource.getAttribute(JDBC_DRIVER_REF));

        List<Element> propertiesDb2JccList = getDirectChildrenByTag(dataSource, PROPERTIES_DB2_JCC);
        assertEquals("Didn't find one and only one " + PROPERTIES_DB2_JCC, 1, propertiesDb2JccList.size());

        Element propertiesDb2Jcc = propertiesDb2JccList.get(0);
        assertEquals("The url attribute is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_URL),
                propertiesDb2Jcc.getAttribute(URL));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());

        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));

        // Check bootstrap.properties content
        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String urlFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_URL);

        assertEquals(
                "The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_URL + " is not correct",
                DB2_URL, urlFound);
    }

    /**
     * Test that the server.xml and boostrap.properties are fully configured
     * with the MySQL datasource
     * 
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    @Test
    public void testAddJdbcBoosterConfig_MySQL() throws Exception {

        LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(
                outputDir.getRoot().getAbsolutePath(), logger);

        // Add basic properties
        Properties datasourceProperties = new Properties();
        datasourceProperties.put(BoostProperties.DATASOURCE_URL, MYSQL_URL);

        serverConfig.addDataSource(JDBCBoosterConfig.MYSQL, datasourceProperties);
        serverConfig.writeToServer();

        File serverXml = new File(outputDir.getRoot().getAbsolutePath() + "/server.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(serverXml);

        Element serverRoot = doc.getDocumentElement();

        // Check that the <library> element is correctly configured
        List<Element> libraryList = getDirectChildrenByTag(serverRoot, LIBRARY);
        assertEquals("Didn't find one and only one library", 1, libraryList.size());

        Element library = libraryList.get(0);
        assertEquals("Library id is not correct", JDBC_LIBRARY_1, library.getAttribute("id"));

        Element fileset = getDirectChildrenByTag(library, FILESET).get(0);
        assertEquals("Fileset dir attribute is not correct", RESOURCES, fileset.getAttribute("dir"));
        assertEquals("Fileset includes attribute is not correct", MYSQL_JAR, fileset.getAttribute("includes"));

        // Check that the <dataSource> element is correctly configured
        List<Element> dataSourceList = getDirectChildrenByTag(serverRoot, DATASOURCE);
        assertEquals("Didn't find one and only one dataSource", 1, dataSourceList.size());

        Element dataSource = dataSourceList.get(0);
        assertEquals("DataSource id is not correct", DEFAULT_DATASOURCE, dataSource.getAttribute("id"));
        assertEquals("DataSource jdbcDriverRef is not correct", JDBC_DRIVER_1,
                dataSource.getAttribute(JDBC_DRIVER_REF));

        List<Element> propertiesList = getDirectChildrenByTag(dataSource, PROPERTIES);
        assertEquals("Didn't find one and only one properties", 1, propertiesList.size());

        Element properties = propertiesList.get(0);
        assertEquals("The url attribute is not correct", BoostUtil.makeVariable(BoostProperties.DATASOURCE_URL),
                properties.getAttribute(URL));

        // Check that the <jdbcDriver> element is correctly configured
        List<Element> jdbcDriverList = getDirectChildrenByTag(serverRoot, JDBC_DRIVER);
        assertEquals("Didn't find one and only one jdbcDriver", 1, jdbcDriverList.size());

        Element jdbcDriver = jdbcDriverList.get(0);
        assertEquals("JdbcDriver id is not correct", JDBC_DRIVER_1, jdbcDriver.getAttribute("id"));
        assertEquals("JdbcDriver libraryRef is not correct", JDBC_LIBRARY_1, jdbcDriver.getAttribute(LIBRARY_REF));

        // Check bootstrap.properties content
        String bootstrapProperties = outputDir.getRoot().getAbsolutePath() + "/bootstrap.properties";

        String urlFound = ConfigFileUtils.findPropertyInBootstrapProperties(bootstrapProperties,
                BoostProperties.DATASOURCE_URL);

        assertEquals(
                "The property set in bootstrap.properties for " + BoostProperties.DATASOURCE_URL + " is not correct",
                MYSQL_URL, urlFound);
    }

}
