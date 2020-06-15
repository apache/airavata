package org.apache.airavata.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPAUtils
 */
public class JPAUtils {

    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);
    private final static Map<String, String> DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES;
    static {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
        properties.put("openjpa.DynamicEnhancementAgent", "true");
        properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
        properties.put("openjpa.RemoteCommitProvider", "sjvm");
        properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
        // use the following to enable logging of all SQL statements
        // properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO,
        // SQL=TRACE");
        properties.put("openjpa.jdbc.SynchronizeMappings", "validate");
        properties.put("openjpa.jdbc.QuerySQLCache", "false");
        properties.put("openjpa.DetachState", "all");
        properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72,"
                + " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
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

        return getEntityManagerFactory(persistenceUnitName, jdbcConfig, Collections.emptyMap());
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

        Map<String, String> finalProperties = new HashMap<>(DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES);
        finalProperties.putAll(createConnectionProperties(jdbcConfig));
        finalProperties.putAll(properties);
        return Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties);
    }

    public static Map<String, String> createConnectionProperties(JDBCConfig jdbcConfig) {
        String connectionProperties = "DriverClassName=" + jdbcConfig.getDriver() + "," + "Url=" + jdbcConfig.getURL()
                + "?autoReconnect=true," + "Username=" + jdbcConfig.getUser() + "," + "Password="
                + jdbcConfig.getPassword() + ",validationQuery=" + jdbcConfig.getValidationQuery();
        logger.debug("Connection properties={}", connectionProperties);
        return Collections.singletonMap("openjpa.ConnectionProperties", connectionProperties);
    }
}
