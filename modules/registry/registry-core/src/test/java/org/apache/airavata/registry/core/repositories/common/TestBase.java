package org.apache.airavata.registry.core.repositories.common;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.DerbyTestUtil;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.WorkflowCatalogDBInitConfig;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

    public enum Database {APP_CATALOG, EXP_CATALOG, REPLICA_CATALOG, WORKFLOW_CATALOG}

    private Database[] databases;

    public TestBase(Database... databases) {
        if (databases == null) {
            throw new IllegalArgumentException("Databases can not be null");
        }
        this.databases = databases;
    }

    @Before
    public void setUp() throws Exception {
        try {
            DerbyUtil.startDerbyInServerMode("127.0.0.1", 20000, "airavata", "airavata");

            for (Database database: databases) {
                logger.info("Creating database " + database.name());
                DerbyTestUtil.destroyDatabase(getDatabaseJDBCConfig(database));
                DBInitializer.initializeDB(getDBInitConfig(database));
            }
        } catch (Exception e) {
            logger.error("Failed to create the databases" , e);
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        for (Database database: databases) {
            System.out.println("Tearing down database " + database.name());
            DerbyTestUtil.destroyDatabase(getDatabaseJDBCConfig(database));
        }
        DerbyUtil.stopDerbyServer();
    }

    private JDBCConfig getDatabaseJDBCConfig(Database database) {
        return getDBInitConfig(database).getJDBCConfig();
    }

    private DBInitConfig getDBInitConfig(Database database) {
        switch (database) {
            case APP_CATALOG:
                return new AppCatalogDBInitConfig()
                    .setDbInitScriptPrefix("appcatalog");
            case EXP_CATALOG:
                return new ExpCatalogDBInitConfig()
                    .setDbInitScriptPrefix("expcatalog");
            case REPLICA_CATALOG:
                return new ReplicaCatalogDBInitConfig()
                    .setDbInitScriptPrefix("replicacatalog");
            case WORKFLOW_CATALOG:
                return new WorkflowCatalogDBInitConfig()
                    .setDbInitScriptPrefix("airavataworkflowcatalog");
            default:
                return null;
        }
    }

}
