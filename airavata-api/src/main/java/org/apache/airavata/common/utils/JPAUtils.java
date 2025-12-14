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
        // Hibernate configuration
        // MariaDB/MySQL dialect configuration
        // Note: This will be overridden per-persistence-unit if H2 is detected
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        // Connection pool settings (HikariCP is used via DataSource)
        properties.put("hibernate.connection.provider_disables_autocommit", "true");
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
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", jdbcConfig.getDriver());
        properties.put("jakarta.persistence.jdbc.url", url + urlSuffix);
        properties.put("jakarta.persistence.jdbc.user", jdbcConfig.getUser());
        properties.put("jakarta.persistence.jdbc.password", jdbcConfig.getPassword());
        logger.debug(
                "Connection properties: driver={}, url={}, user={}",
                jdbcConfig.getDriver(),
                url + urlSuffix,
                jdbcConfig.getUser());
        return properties;
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
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", driver);
        properties.put("jakarta.persistence.jdbc.url", url + urlSuffix);
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", password);
        logger.debug("Connection properties: driver={}, url={}, user={}", driver, url + urlSuffix, user);
        return properties;
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
        // Use H2 dialect and enable schema creation for H2 databases
        if (url != null && url.startsWith("jdbc:h2:")) {
            finalProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            // Enable automatic schema creation for H2 in-memory databases
            finalProperties.put("hibernate.hbm2ddl.auto", "update");
        }
        return Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties);
    }
}
