
package org.apache.airavata.service.profile.gateway.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by goshenoy on 3/6/17.
 */
public class JPAUtils {

    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);

    private static final String PERSISTENCE_UNIT_NAME = "gateway_profile_catalog";

    @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManagerFactory factory;

    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManager entityManager;

    public static EntityManager getEntityManager() {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + Utils.getJDBCDriver() + "," + "Url=" +
                    Utils.getJDBCURL() + "?autoReconnect=true," +
                    "Username=" + Utils.getJDBCUser() + "," + "Password=" + Utils.getJDBCPassword() +
                    ",validationQuery=" + Utils.getValidationQuery();
            logger.info(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "warn");
            properties.put("openjpa.RemoteCommitProvider", "sjvm");
            properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72," +
                    " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }

        entityManager = factory.createEntityManager();
        return entityManager;
    }
}
