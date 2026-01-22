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
package org.apache.airavata.config;

import java.io.File;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration for Flyway database migrations.
 *
 * <p>All Airavata data is stored in a single unified 'airavata' database.
 * Migration files are loaded from {airavataHome}/conf/db/migration/airavata/
 * where {airavataHome} is resolved from:
 * <ul>
 *   <li>System property -Dairavata.home=XXX (highest priority)</li>
 *   <li>airavata.home property in application.properties (if set and non-empty)</li>
 *   <li>Resources root (IDE mode: modules/distribution/src/main/resources)</li>
 * </ul>
 *
 * <p>Flyway can be disabled via airavata.flyway.enabled=false property.
 */
@Configuration
@ConditionalOnProperty(prefix = "airavata.flyway", name = "enabled", havingValue = "true")
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * Get the migration location path for the unified database.
     */
    private static String getMigrationLocation() {
        var configDir = AiravataConfigUtils.getConfigDir();
        var migrationPath =
                configDir + File.separator + "db" + File.separator + "migration" + File.separator + "airavata";
        logger.debug("Flyway migration location: {}", migrationPath);
        return "filesystem:" + migrationPath;
    }

    /**
     * Configure Flyway for the unified Airavata database.
     */
    @Bean(name = "flyway", initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation())
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Flyway migration strategy bean for Spring Boot autoconfiguration.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return Flyway::migrate;
    }
}
