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
package org.apache.airavata.cli.handlers;

import java.io.File;
import javax.sql.DataSource;
import org.apache.airavata.cli.util.DatabaseInitializer;
import org.apache.airavata.config.AiravataConfigUtils;
import org.apache.airavata.config.DatabaseVersionConstants;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Initializes the unified Airavata database using Flyway migrations.
 * Uses the single primary DataSource and runs Flyway programmatically so that
 * init does not depend on FlywayConfig beans (avoids migrate-on-init and shutdown ordering).
 */
@Service
public class InitHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitHandler.class);

    private static final String UNIFIED_DB_NAME = "airavata";
    private static final String VERSION_CONFIG_KEY = "airavata.catalog.version";
    private static final String CONFIG_VAL_COLUMN = "CONFIG_VAL";

    private final DataSource dataSource;

    public InitHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initializeDatabases(boolean clean) {
        logger.info("Starting database initialization (clean={})...", clean);

        String configDir = AiravataConfigUtils.getConfigDir();
        String migrationPath =
                configDir + File.separator + "db" + File.separator + "migration" + File.separator + "airavata";
        String location = "filesystem:" + migrationPath;
        logger.debug("Flyway migration location: {}", location);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .cleanDisabled(!clean)
                .load();

        DatabaseInitializer.initializeDatabase(
                UNIFIED_DB_NAME,
                flyway,
                dataSource,
                VERSION_CONFIG_KEY,
                DatabaseVersionConstants.APP_CATALOG_VERSION,
                CONFIG_VAL_COLUMN,
                false,
                clean);

        System.out.println("✓ Database initialization completed successfully.");
        logger.info("Database initialization completed successfully.");
    }
}
