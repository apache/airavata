/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPAUtils
 */
public class JPAUtils {

    private static final Logger logger = LoggerFactory.getLogger(JPAUtils.class);
    private static final Map<String, String> DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES;

    static {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp2.BasicDataSource");
        // Allow unenhanced classes at runtime - this is needed for Spring Data JPA integration
        // where metamodel is accessed before EntityManagerFactory is fully initialized
        // "supported" allows unenhanced classes but may have performance implications
        // "warn" allows unenhanced classes and logs warnings
        properties.put(
                "openjpa.RuntimeUnenhancedClasses",
                System.getProperty("openjpa.RuntimeUnenhancedClasses", "supported"));
        // Disable dynamic enhancement agent since we use build-time enhancement
        // Enabling both can cause conflicts or class loading issues
        properties.put(
                "openjpa.DynamicEnhancementAgent", System.getProperty("openjpa.DynamicEnhancementAgent", "false"));
        properties.put("openjpa.RemoteCommitProvider", "sjvm");
        properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
        // use the following to enable logging of all SQL statements
        // properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO,
        // SQL=TRACE");
        properties.put("openjpa.jdbc.SynchronizeMappings", "validate");
        properties.put("openjpa.jdbc.QuerySQLCache", "false");
        properties.put("openjpa.DetachState", "all");
        properties.put(
                "openjpa.ConnectionFactoryProperties",
                "PrettyPrint=true, PrettyPrintLineLength=72,"
                        + " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
        // MariaDB/MySQL dialect configuration to handle boolean to tinyint mapping
        // Note: This will be overridden per-persistence-unit if H2 is detected
        properties.put("openjpa.jdbc.DBDictionary", "mysql");
        properties.put(
                "openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=cascade, JoinForeignKeyDeleteAction=cascade");
        DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES = properties;
    }

    /**
     * Create an {@link EntityManagerFactory} with the default settings.
     *
     * @param persistenceUnitName
     * @param jdbcConfig
     * @return {@link EntityManagerFactory}
     */
    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName, JDBCConfig jdbcConfig) {

        return getEntityManagerFactory(persistenceUnitName, jdbcConfig, DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES);
    }

    /**
     * Create an {@link EntityManagerFactory}. The given properties will override
     * the default properties.
     *
     * @param persistenceUnitName
     * @param jdbcConfig
     * @param properties
     * @return {@link EntityManagerFactory}
     */
    public static EntityManagerFactory getEntityManagerFactory(
            String persistenceUnitName, JDBCConfig jdbcConfig, Map<String, String> properties) {

        Map<String, String> finalProperties = new HashMap<>(DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES);
        finalProperties.putAll(createConnectionProperties(jdbcConfig));
        finalProperties.putAll(properties);
        return Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties);
    }

    public static Map<String, String> createConnectionProperties(JDBCConfig jdbcConfig) {
        String url = jdbcConfig.getURL();
        // H2 URLs use ; as parameter separator and don't need MySQL-specific parameters
        String urlSuffix = "";
        if (url != null && !url.startsWith("jdbc:h2:")) {
            // MySQL/MariaDB specific parameters
            urlSuffix = "?autoReconnect=true&tinyInt1isBit=false";
        }
        String connectionProperties = "DriverClassName=" + jdbcConfig.getDriver() + "," + "Url=" + url
                + urlSuffix + "," + "Username=" + jdbcConfig.getUser() + "," + "Password="
                + jdbcConfig.getPassword() + ",validationQuery=" + jdbcConfig.getValidationQuery();
        logger.debug("Connection properties={}", connectionProperties);
        return Collections.singletonMap("openjpa.ConnectionProperties", connectionProperties);
    }

    /**
     * Create connection properties directly from database configuration values.
     */
    public static Map<String, String> createConnectionProperties(
            String driver, String url, String user, String password, String validationQuery) {
        // H2 URLs use ; as parameter separator and don't need MySQL-specific parameters
        String urlSuffix = "";
        if (url != null && !url.startsWith("jdbc:h2:")) {
            // MySQL/MariaDB specific parameters
            urlSuffix = "?autoReconnect=true&tinyInt1isBit=false";
        }
        String connectionProperties = "DriverClassName=" + driver + "," + "Url=" + url
                + urlSuffix + "," + "Username=" + user + "," + "Password="
                + password + ",validationQuery=" + validationQuery;
        logger.debug("Connection properties={}", connectionProperties);
        return Collections.singletonMap("openjpa.ConnectionProperties", connectionProperties);
    }

    /**
     * Create an EntityManagerFactory directly from database configuration values.
     */
    public static EntityManagerFactory getEntityManagerFactory(
            String persistenceUnitName,
            String driver,
            String url,
            String user,
            String password,
            String validationQuery) {
        Map<String, String> finalProperties = new HashMap<>(DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES);
        finalProperties.putAll(createConnectionProperties(driver, url, user, password, validationQuery));
        // Use H2 dictionary and enable schema creation for H2 databases
        if (url != null && url.startsWith("jdbc:h2:")) {
            finalProperties.put("openjpa.jdbc.DBDictionary", "h2");
            // Enable automatic schema creation for H2 in-memory databases
            // buildSchema mode creates missing tables and adds missing columns
            // SchemaAction=add creates tables/columns if they don't exist, without altering existing ones
            finalProperties.put(
                    "openjpa.jdbc.SynchronizeMappings",
                    "buildSchema(ForeignKeys=true,SchemaAction=add,IgnoreErrors=true)");
        }
        return Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties);
    }
}
