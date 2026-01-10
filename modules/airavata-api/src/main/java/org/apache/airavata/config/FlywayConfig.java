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
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration for Flyway database migrations using Spring Boot's Flyway integration.
 * Each persistence unit has its own Flyway instance that manages migrations
 * for its corresponding database.
 *
 * <p>Migration files are loaded from {airavataHome}/conf/db/migration/{database_name}/
 * where {airavataHome} is resolved from:
 * <ul>
 *   <li>System property -Dairavata.home=XXX (highest priority)</li>
 *   <li>airavata.home property in airavata.properties (if set and non-empty)</li>
 *   <li>Resources root (IDE mode: modules/distribution/src/main/resources)</li>
 * </ul>
 *
 * <p>Flyway is enabled by default in production but can be disabled via
 * flyway.enabled=false property.
 */
@Configuration
@ConditionalOnProperty(prefix = "flyway", name = "enabled", havingValue = "true")
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * Get the migration location path for a given database.
     * Uses filesystem path: {configDir}/db/migration/{databaseName}
     * getConfigDir() handles both production and IDE modes automatically.
     */
    private static String getMigrationLocation(String databaseName) {
        // getConfigDir() now handles both production and IDE modes
        String configDir = AiravataConfigUtils.getConfigDir();
        String migrationPath =
                configDir + File.separator + "db" + File.separator + "migration" + File.separator + databaseName;
        logger.debug("Flyway migration location for {}: {}", databaseName, migrationPath);
        return "filesystem:" + migrationPath;
    }

    /**
     * Common Flyway configuration customizer that can be shared across all datasources.
     * Uses Spring Boot's FlywayConfigurationCustomizer for consistent configuration.
     */
    private FlywayConfigurationCustomizer createFlywayCustomizer(String databaseName) {
        return config -> {
            config.baselineOnMigrate(true);
            config.validateOnMigrate(true);
            config.locations(getMigrationLocation(databaseName));
        };
    }

    /**
     * Configure Flyway for profile service database using Spring Boot's approach.
     */
    @Bean(name = "profileServiceFlyway", initMethod = "migrate")
    @DependsOn("profileServiceDataSource")
    public Flyway profileServiceFlyway(@Qualifier("profileServiceDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("profile_service"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for app catalog database.
     */
    @Bean(name = "appCatalogFlyway", initMethod = "migrate")
    @DependsOn("appCatalogDataSource")
    public Flyway appCatalogFlyway(@Qualifier("appCatalogDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("app_catalog"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for experiment catalog database.
     */
    @Bean(name = "expCatalogFlyway", initMethod = "migrate")
    @DependsOn("registryDataSource")
    public Flyway expCatalogFlyway(@Qualifier("registryDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("experiment_catalog"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for replica catalog database.
     */
    @Bean(name = "replicaCatalogFlyway", initMethod = "migrate")
    @DependsOn("replicaDataSource")
    public Flyway replicaCatalogFlyway(@Qualifier("replicaDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("replica_catalog"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for workflow catalog database.
     */
    @Bean(name = "workflowCatalogFlyway", initMethod = "migrate")
    @DependsOn("workflowDataSource")
    public Flyway workflowCatalogFlyway(@Qualifier("workflowDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("workflow_catalog"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for sharing registry database.
     */
    @Bean(name = "sharingRegistryFlyway", initMethod = "migrate")
    @DependsOn("sharingDataSource")
    public Flyway sharingRegistryFlyway(@Qualifier("sharingDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("sharing_registry"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for credential store database.
     */
    @Bean(name = "credentialStoreFlyway", initMethod = "migrate")
    @DependsOn("credentialStoreDataSource")
    public Flyway credentialStoreFlyway(@Qualifier("credentialStoreDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("credential_store"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Configure Flyway for research catalog database.
     */
    @Bean(name = "researchCatalogFlyway", initMethod = "migrate")
    @DependsOn("researchCatalogDataSource")
    public Flyway researchCatalogFlyway(@Qualifier("researchCatalogDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(getMigrationLocation("research_catalog"))
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }

    /**
     * Flyway migration strategy bean for Spring Boot autoconfiguration.
     * This allows Spring Boot to manage Flyway lifecycle if needed.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Custom migration strategy if needed
            // For now, just run migrate (default behavior)
            flyway.migrate();
        };
    }
}
