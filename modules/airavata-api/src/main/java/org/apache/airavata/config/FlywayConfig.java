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

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration for Flyway database migrations.
 * Each persistence unit has its own Flyway instance that manages migrations
 * for its corresponding database.
 *
 * Flyway is enabled by default in production but can be disabled via
 * flyway.enabled=false property.
 */
@Configuration
@ConditionalOnProperty(name = "flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    /**
     * Configure Flyway for profile service database.
     */
    @Bean(name = "profileServiceFlyway", initMethod = "migrate")
    @DependsOn("profileServiceDataSource")
    public Flyway profileServiceFlyway(@Qualifier("profileServiceDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/profile_service")
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
                .locations("classpath:db/migration/app_catalog")
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
                .locations("classpath:db/migration/experiment_catalog")
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
                .locations("classpath:db/migration/replica_catalog")
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
                .locations("classpath:db/migration/workflow_catalog")
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
                .locations("classpath:db/migration/sharing_registry")
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
                .locations("classpath:db/migration/credential_store")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();
    }
}
