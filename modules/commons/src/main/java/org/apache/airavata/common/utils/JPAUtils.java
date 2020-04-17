package org.apache.airavata.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.Cache;
// import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
// import javax.persistence.Query;
// import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.persistence.ArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPAUtils
 */
public class JPAUtils {

    private static class EntityManagerFactoryWrapper implements EntityManagerFactory {

        private final EntityManagerFactory factory;
        private final JDBCConfig jdbcConfig;

        EntityManagerFactoryWrapper(EntityManagerFactory factory, JDBCConfig jdbcConfig) {
            this.factory = factory;
            this.jdbcConfig = jdbcConfig;
        }

        @Override
        public EntityManager createEntityManager() {
            return wrapCreateEntityManager(() -> this.factory.createEntityManager());
        }

        @Override
        public EntityManager createEntityManager(Map map) {
            return wrapCreateEntityManager(() -> this.factory.createEntityManager(map));
        }

        // @Override
        // public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        //     return wrapCreateEntityManager(() -> this.factory.createEntityManager(synchronizationType));
        // }

        // @Override
        // public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        //     return wrapCreateEntityManager(() -> this.factory.createEntityManager(synchronizationType, map));
        // }

        private EntityManager wrapCreateEntityManager(Supplier<EntityManager> entityManagerSupplier) {

            try {
                return entityManagerSupplier.get();
            } catch (ArgumentException e) {

                Map<String, String> finalProperties = new HashMap<>(JPAUtils.DEFAULT_ENTITY_MANAGER_FACTORY_PROPERTIES);
                finalProperties.putAll(JPAUtils.createConnectionProperties(this.jdbcConfig));
                JDBCConfiguration jdbcConfiguration = new JDBCConfigurationImpl();
                jdbcConfiguration.fromProperties(finalProperties);

                Options options = new Options();
                options.put("sqlFile", "migration.sql");
                // If you want to generate the entire schema instead of just what is
                // needed to bring the database up to date, use schemaAction=build
                // options.put("schemaAction", "build");
                options.put("foreignKeys", "true");
                options.put("indexes", "true");
                options.put("primaryKeys", "true");
                try {
                    MappingTool.run(jdbcConfiguration, new String[] {}, options, null);
                } catch (Exception mappingToolEx) {
                    logger.error("Failed to run MappingTool", mappingToolEx);
                    throw new RuntimeException(
                            "Failed to get EntityManager, then failed to run MappingTool to generate migration script",
                            e);
                }
                throw new RuntimeException("Failed to get EntityManager, but successfully executed "
                        + "MappingTool to generate migration script (to file named "
                        + "migration.sql) in case the error was caused by the database "
                        + "schema being out of date with the mappings", e);
            }
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder() {
            return this.factory.getCriteriaBuilder();
        }

        @Override
        public Metamodel getMetamodel() {
            return this.factory.getMetamodel();
        }

        @Override
        public boolean isOpen() {
            return this.factory.isOpen();
        }

        @Override
        public void close() {
            this.factory.close();
        }

        @Override
        public Map<String, Object> getProperties() {
            return this.factory.getProperties();
        }

        @Override
        public Cache getCache() {
            return this.factory.getCache();
        }

        @Override
        public PersistenceUnitUtil getPersistenceUnitUtil() {
            return this.factory.getPersistenceUnitUtil();
        }

        // @Override
        // public void addNamedQuery(String name, Query query) {
        //     this.factory.addNamedQuery(name, query);
        // }

        // @Override
        // public <T> T unwrap(Class<T> cls) {
        //     return this.factory.unwrap(cls);
        // }

        // @Override
        // public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        //     this.factory.addNamedEntityGraph(graphName, entityGraph);
        // }

    }

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
        return new EntityManagerFactoryWrapper(
                Persistence.createEntityManagerFactory(persistenceUnitName, finalProperties), jdbcConfig);
    }

    private static Map<String, String> createConnectionProperties(JDBCConfig jdbcConfig) {
        String connectionProperties = "DriverClassName=" + jdbcConfig.getDriver() + "," + "Url=" + jdbcConfig.getURL()
                + "?autoReconnect=true," + "Username=" + jdbcConfig.getUser() + "," + "Password="
                + jdbcConfig.getPassword() + ",validationQuery=" + jdbcConfig.getValidationQuery();
        logger.debug("Connection properties={}", connectionProperties);
        return Collections.singletonMap("openjpa.ConnectionProperties", connectionProperties);
    }
}
