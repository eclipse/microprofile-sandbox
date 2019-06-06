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

public final class ConfigConstants {

    public static final String FEATURE = "feature";
    public static final String DEPENDENCY = "dependency";
    public static final String GROUPID = "groupId";
    public static final String ARTIFACTID = "artifactId";
    public static final String VERSION = "version";
    public static final String WAR_PKG_TYPE = "war";

    public static final String TOMEEBOOST_JAR_DIR = "boost";

    public static final String INSTALL_PACKAGE_ALL = "all";
    public static final String INSTALL_PACKAGE_DEP = "dependencies";

    public static final String FEATURE_MANAGER = "featureManager";
    public static final String HTTP_ENDPOINT = "httpEndpoint";
    public static final String DEFAULT_HTTP_ENDPOINT = "defaultHttpEndpoint";

    public static final String APPLICATION = "application";

    // KeyStore configuration element/attribute names
    public static final String KEYSTORE = "keyStore";
    public static final String KEY_ENTRY = "keyEntry";
    public static final String KEY_PASSWORD = "keyPassword";

    // KeyStore configuration values
    public static final String DEFAULT_KEYSTORE = "defaultKeyStore";

    // Datasource configuration element/attribute names
    public static final String DATASOURCE = "dataSource";
    public static final String DATABASE_NAME = "databaseName";
    public static final String CREATE_DATABASE = "createDatabase";
    public static final String SERVER_NAME = "serverName";
    public static final String PORT_NUMBER = "portNumber";
    public static final String JNDI_NAME = "jndiName";
    public static final String JDBC_DRIVER_REF = "jdbcDriverRef";
    public static final String JDBC_DRIVER = "jdbcDriver";
    public static final String LIBRARY_REF = "libraryRef";
    public static final String LIBRARY = "library";
    public static final String FILESET = "fileset";
    public static final String PROPERTIES_DERBY_EMBEDDED = "properties.derby.embedded";
    public static final String PROPERTIES_DB2_JCC = "properties.db2.jcc";
    public static final String PROPERTIES = "properties";
    public static final String CONTAINER_AUTH_DATA_REF = "containerAuthDataRef";
    public static final String URL = "url";

    // Datasource configuration values
    public static final String DEFAULT_DATASOURCE = "DefaultDataSource";
    public static final String JDBC_DRIVER_1 = "JdbcDriver1";
    public static final String JDBC_LIBRARY_1 = "Library1";
    public static final String DERBY_DB = "DerbyDB";
    public static final String DATASOURCE_AUTH_DATA = "datasourceAuth";
    public static final String DERBY_JAR = "derby*.jar";
    public static final String DB2_JAR = "db2jcc*.jar";
    public static final String MYSQL_JAR = "mysql*.jar";
    public static final String DB2_DEFAULT_PORT_NUMBER = "50000";
    public static final String MYSQL_DEFAULT_PORT_NUMBER = "3306";

    // Authentication configuration element/attribute names
    public static final String AUTH_DATA = "authData";

    // General purpose configuration element/attribute names
    public static final String LOCATION = "location";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String TYPE = "type";
    public static final String PROVIDER = "provider";
    public static final String NAME = "name";
    public static final String CONTEXT_ROOT = "context-root";
    public static final String RESOURCES = "resources";

    // General purpose configuration values
    public static final String LOCALHOST = "localhost";

    // Liberty features
    public static final String SPRING_BOOT_15 = "springBoot-1.5";
    public static final String SPRING_BOOT_20 = "springBoot-2.0";
    public static final String SERVLET_40 = "servlet-4.0";

    public static final String WEBSOCKET_11 = "websocket-1.1";
    public static final String TRANSPORT_SECURITY_10 = "transportSecurity-1.0";
    public static final String JAXRS_20 = "jaxrs-2.0";
    public static final String JAXRS_21 = "jaxrs-2.1";
    public static final String JDBC_40 = "jdbc-4.0";
    public static final String JDBC_41 = "jdbc-4.1";
    public static final String JDBC_42 = "jdbc-4.2";
    public static final String JDBC_43 = "jdbc-4.3";
    public static final String JPA_21 = "jpa-2.1";
    public static final String JPA_22 = "jpa-2.2";
    public static final String CDI_20 = "cdi-2.0";
    public static final String MPHEALTH_10 = "mpHealth-1.0";
    public static final String MPRESTCLIENT_11 = "mpRestClient-1.1";
    public static final String JSONP_11 = "jsonp-1.1";
    public static final String MPCONFIG_13 = "mpConfig-1.3";
    public static final String MPOPENTRACING_11 = "mpOpenTracing-1.1";

    public static final String SHARED_RESOURCES_DIR = "${shared.resource.dir}";
    public static final String SERVER_OUTPUT_DIR = "${server.output.dir}";

}
