package org.apache.airavata.registry.core.repositories.common;

import org.apache.airavata.registry.core.repositories.appcatalog.ApplicationDeploymentRepositoryTest;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeploymentRepositoryTest.class);

    public enum Database {APP_CATALOG, EXP_CATALOG, REPLICA_CATALOG}

    private DerbyDBManager dbManager = new DerbyDBManager();
    private Database[] databases;

    public TestBase(Database... databases) {
        if (databases == null) {
            throw new IllegalArgumentException("Databases can not be null");
        }
        this.databases = databases;
    }

    @Before
    public void setUp() {
        try {
            dbManager.startDatabaseServer();

            for (Database database: databases) {
                logger.info("Creating database " + database.name());
                dbManager.destroyDatabase(getDatabaseName(database));
                dbManager.initializeDatabase(getDatabaseName(database), getDatabasePath(database));
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
            dbManager.destroyDatabase(getDatabaseName(database));
        }
        dbManager.stopDatabaseServer();
    }

    private String getDatabasePath(Database database) {
        switch (database) {
            case APP_CATALOG:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("appcatalog-derby.sql")).getPath();
            case EXP_CATALOG:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("expcatalog-derby.sql")).getPath();
            case REPLICA_CATALOG:
                return Objects.requireNonNull(getClass().getClassLoader().getResource("replicacatalog-derby.sql")).getPath();
            default:
                return null;
        }
    }

    private String getDatabaseName(Database database) {
        switch (database) {
            case APP_CATALOG:
                return "app_catalog";
            case EXP_CATALOG:
                return "experiment_catalog";
            case REPLICA_CATALOG:
                return "replica_catalog";
            default:
                return null;
        }
    }

}
