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
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.GwyResourceProfileService;
import org.apache.airavata.registry.services.UserService;
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

    private static Database[] createDatabases(
            AiravataServerProperties properties,
            GwyResourceProfileService gwyResourceProfileService,
            GatewayService gatewayService,
            UserService userService) {
        return new Database[] {
            new Database(
                    new AppCatalogDBInitConfig(properties, gwyResourceProfileService)
                            .setDbInitScriptPrefix("appcatalog"),
                    AppCatalogJPAUtils.PERSISTENCE_UNIT_NAME),
            new Database(
                    new ExpCatalogDBInitConfig(properties, gatewayService, userService)
                            .setDbInitScriptPrefix("expcatalog"),
                    ExpCatalogJPAUtils.PERSISTENCE_UNIT_NAME),
            new Database(
                    new ReplicaCatalogDBInitConfig(properties).setDbInitScriptPrefix("replicacatalog"),
                    RepCatalogJPAUtils.PERSISTENCE_UNIT_NAME)
        };
    }

    private static class Database {
        private final DBInitConfig dbInitConfig;
        private final String persistenceUnitName;

        Database(DBInitConfig dbInitConfig, String persistenceUnitName) {
            this.dbInitConfig = dbInitConfig;
            this.persistenceUnitName = persistenceUnitName;
        }
    }

    public static void main(String[] args) throws Exception {
        // Note: This utility requires dependencies that should be provided via Spring context
        // or passed as parameters. For standalone execution, these need to be initialized.
        logger.warn("MigrationSchemaGenerator requires AiravataServerProperties and service dependencies.");
        logger.warn(
                "This utility should be run within a Spring application context or dependencies must be provided manually.");
        throw new UnsupportedOperationException(
                "MigrationSchemaGenerator must be run within a Spring application context. "
                        + "Use Spring Boot application or provide dependencies manually via constructor.");
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
