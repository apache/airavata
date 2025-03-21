package org.apache.airavata.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPAUtils
 */
public class JPAUtils {

    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);
    private final static Map<String, String> DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES;
    static {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dbcp2.initialSize", "5");
        properties.put("hibernate.dbcp2.maxTotal", "20");
        properties.put("hibernate.dbcp2.maxIdle", "10");
        properties.put("hibernate.dbcp2.minIdle", "2");
        properties.put("hibernate.dbcp2.maxWaitMillis", "5000");
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
    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName, JDBCConfig jdbcConfig,
            Map<String, String> properties) {

        Map<String, String> finalProperties = new HashMap<>();
        finalProperties.putAll(createConnectionProperties(jdbcConfig));
        finalProperties.putAll(properties);
        return Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties);
    }

    public static Map<String, String> createConnectionProperties(JDBCConfig jdbcConfig) {
        Map<String, String> connectionProperties = new HashMap<>();
        connectionProperties.put("hibernate.connection.provider_class", "org.apache.commons.dbcp2.BasicDataSource");
        connectionProperties.put("hibernate.connection.driver_class", jdbcConfig.getDriver());
        connectionProperties.put("hibernate.connection.url", jdbcConfig.getURL() + "?autoReconnect=true");
        connectionProperties.put("hibernate.connection.username", jdbcConfig.getUser());
        connectionProperties.put("hibernate.connection.password", jdbcConfig.getPassword());
        if (jdbcConfig.getValidationQuery() != null && !jdbcConfig.getValidationQuery().isEmpty()) {
            connectionProperties.put("hibernate.connection.validationQuery", jdbcConfig.getValidationQuery());
        }
        logger.debug("Connection properties={}", connectionProperties);
        return connectionProperties;
    }
}
