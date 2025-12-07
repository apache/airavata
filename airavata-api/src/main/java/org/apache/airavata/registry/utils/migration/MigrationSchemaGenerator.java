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
package org.apache.airavata.registry.utils.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.registry.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.utils.JPAUtil.AppCatalogJPAUtils;
import org.apache.airavata.registry.utils.JPAUtil.ExpCatalogJPAUtils;
import org.apache.airavata.registry.utils.JPAUtil.RepCatalogJPAUtils;
import org.apache.airavata.registry.utils.ReplicaCatalogDBInitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationSchemaGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MigrationSchemaGenerator.class);

    private enum Database {
        app_catalog(
                new AppCatalogDBInitConfig().setDbInitScriptPrefix("appcatalog"),
                AppCatalogJPAUtils.PERSISTENCE_UNIT_NAME),
        experiment_catalog(
                new ExpCatalogDBInitConfig().setDbInitScriptPrefix("expcatalog"),
                ExpCatalogJPAUtils.PERSISTENCE_UNIT_NAME),
        replica_catalog(
                new ReplicaCatalogDBInitConfig().setDbInitScriptPrefix("replicacatalog"),
                RepCatalogJPAUtils.PERSISTENCE_UNIT_NAME);

        private final DBInitConfig dbInitConfig;
        private final String persistenceUnitName;

        Database(DBInitConfig dbInitConfig, String persistenceUnitName) {
            this.dbInitConfig = dbInitConfig;
            this.persistenceUnitName = persistenceUnitName;
        }
    }

    public static void main(String[] args) throws Exception {

        String schemaAction = args.length > 0 ? args[0] : "add";
        try {
            for (Database database : Database.values()) {

                waitForDatabaseServer(database.dbInitConfig, 60);
                try {
                    logger.info("initializing database " + database.name());
                    DBInitializer.initializeDB(database.dbInitConfig);
                } catch (Exception e) {

                    logger.error("Failed to initialize database " + database.name(), e);
                } finally {
                    String outputFile = "add".equals(schemaAction)
                            ? database.name() + "-migration.sql"
                            : database.name() + "-schema.sql";
                    logger.info("creating database script: " + outputFile);
                    MappingToolRunner.run(
                            database.dbInitConfig, outputFile, database.persistenceUnitName, schemaAction);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create the databases", e);
            throw e;
        }
    }

    private static void waitForDatabaseServer(DBInitConfig dbInitConfig, int timeoutSeconds) {

        long startTime = System.currentTimeMillis();
        boolean connected = false;
        while (!connected) {

            if ((System.currentTimeMillis() - startTime) / 1000 > timeoutSeconds) {
                throw new RuntimeException(
                        "Failed to connect to database server after " + timeoutSeconds + " seconds!");
            }
            Connection conn = null;
            try {
                Class.forName(dbInitConfig.getDriver());
                conn = DriverManager.getConnection(
                        dbInitConfig.getUrl(), dbInitConfig.getUser(), dbInitConfig.getPassword());
                connected = conn.isValid(10);
            } catch (Exception e) {
                logger.debug("Failed to connect to database: " + e.getMessage() + ", waiting 1 second before retrying");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    logger.warn("Thread sleep interrupted, ignoring");
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close connection, ignoring");
                    }
                }
            }
        }
    }
}
