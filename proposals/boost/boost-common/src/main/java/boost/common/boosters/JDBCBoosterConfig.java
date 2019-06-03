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

import static boost.common.config.ConfigConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boost.common.BoostException;
import boost.common.BoostLoggerI;
import boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import boost.common.config.BoostProperties;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":jdbc")
public class JDBCBoosterConfig extends AbstractBoosterConfig {

    public static String DERBY = "derby";
    public static String DB2 = "db2";
    public static String MYSQL = "mysql";

    public static String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String DB2_DRIVER_CLASS_NAME = "com.ibm.db2.jcc.DB2Driver";
    public static String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    public static String DERBY_DEPENDENCY = "org.apache.derby:derby";
    public static String DB2_DEPENDENCY = "com.ibm.db2.jcc:db2jcc";
    public static String MYSQL_DEPENDENCY = "mysql:mysql-connector-java";

    private static String DERBY_DEFAULT = "org.apache.derby:derby:10.14.2.0";

    protected Properties boostConfigProperties;
    private String dependency;
    private String productName;

    public JDBCBoosterConfig(Map<String, String> dependencies, BoostLoggerI logger) throws BoostException {
        super(dependencies.get(getCoordinates(JDBCBoosterConfig.class)));

        // TODO: Should consider getting properties on the fly to avoid timing issues of
        // when properties are set
        boostConfigProperties = BoostProperties.getConfiguredBoostProperties(logger);

        // Determine JDBC driver dependency
        if (dependencies.containsKey(JDBCBoosterConfig.DERBY_DEPENDENCY)) {
            String derbyVersion = dependencies.get(JDBCBoosterConfig.DERBY_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.DERBY_DEPENDENCY + ":" + derbyVersion;
            this.productName = DERBY;

        } else if (dependencies.containsKey(JDBCBoosterConfig.DB2_DEPENDENCY)) {
            String db2Version = dependencies.get(JDBCBoosterConfig.DB2_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.DB2_DEPENDENCY + ":" + db2Version;
            this.productName = DB2;

        } else if (dependencies.containsKey(JDBCBoosterConfig.MYSQL_DEPENDENCY)) {
            String mysqlVersion = dependencies.get(JDBCBoosterConfig.MYSQL_DEPENDENCY);
            this.dependency = JDBCBoosterConfig.MYSQL_DEPENDENCY + ":" + mysqlVersion;
            this.productName = MYSQL;

        } else {
            this.dependency = DERBY_DEFAULT;
            this.productName = DERBY;
        }
    }

    public Properties getDatasourceProperties() {

        Properties datasourceProperties = new Properties();

        // Find and add all "boost.db." properties.
        for (String key : boostConfigProperties.stringPropertyNames()) {
            if (key.startsWith(BoostProperties.DATASOURCE_PREFIX)) {
                String value = (String) boostConfigProperties.get(key);
                datasourceProperties.put(key, value);
            }
        }

        if (!datasourceProperties.containsKey(BoostProperties.DATASOURCE_URL)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_DATABASE_NAME)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_SERVER_NAME)
                && !datasourceProperties.containsKey(BoostProperties.DATASOURCE_PORT_NUMBER)) {

            // No db connection properties have been specified. Set defaults.
            if (productName.equals(DERBY)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_DATABASE_NAME, DERBY_DB);
                datasourceProperties.put(BoostProperties.DATASOURCE_CREATE_DATABASE, "create");

            } else if (productName.equals(DB2)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_URL,
                        "jdbc:db2://localhost:" + DB2_DEFAULT_PORT_NUMBER);

            } else if (productName.equals(MYSQL)) {
                datasourceProperties.put(BoostProperties.DATASOURCE_URL,
                        "jdbc:mysql://localhost:" + MYSQL_DEFAULT_PORT_NUMBER);
            }
        }
        return datasourceProperties;
    }

    @Override
    public List<String> getDependencies() {
        List<String> deps = new ArrayList<String>();
        deps.add(dependency);

        return deps;
    }

    public String getProductName() {
        return productName;
    }
}
