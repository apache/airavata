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
package org.apache.airavata.cli.util;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initializeDatabase(
            String dbName,
            Flyway flyway,
            DataSource dataSource,
            String configKey,
            String version,
            String configValueColumn,
            boolean clean) {
        try {
            logger.info("Initializing database: {}", dbName);

            if (clean) {
                logger.info("Cleaning database: {}", dbName);
                flyway.clean();
                logger.info("Database {} cleaned successfully", dbName);
            }

            logger.info("Running Flyway migrations for: {}", dbName);
            flyway.migrate();
            logger.info("Flyway migrations completed for: {}", dbName);
            // Schema version is tracked in flyway_schema_history; CONFIGURATION table removed.

        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", dbName, e);
            throw new RuntimeException("Database initialization failed for: " + dbName, e);
        }
    }
}
