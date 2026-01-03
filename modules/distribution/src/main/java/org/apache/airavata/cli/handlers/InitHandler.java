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

import javax.sql.DataSource;
import org.apache.airavata.cli.util.DatabaseInitializer;
import org.apache.airavata.config.DatabaseVersionConstants;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class InitHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitHandler.class);

    private final Flyway profileServiceFlyway;
    private final Flyway appCatalogFlyway;
    private final Flyway expCatalogFlyway;
    private final Flyway replicaCatalogFlyway;
    private final Flyway sharingRegistryFlyway;
    private final Flyway credentialStoreFlyway;

    private final DataSource profileServiceDataSource;
    private final DataSource appCatalogDataSource;
    private final DataSource registryDataSource;
    private final DataSource replicaDataSource;
    private final DataSource sharingDataSource;
    private final DataSource credentialStoreDataSource;

    public InitHandler(
            @Qualifier("profileServiceFlyway") Flyway profileServiceFlyway,
            @Qualifier("appCatalogFlyway") Flyway appCatalogFlyway,
            @Qualifier("expCatalogFlyway") Flyway expCatalogFlyway,
            @Qualifier("replicaCatalogFlyway") Flyway replicaCatalogFlyway,
            @Qualifier("sharingRegistryFlyway") Flyway sharingRegistryFlyway,
            @Qualifier("credentialStoreFlyway") Flyway credentialStoreFlyway,
            @Qualifier("profileServiceDataSource") DataSource profileServiceDataSource,
            @Qualifier("appCatalogDataSource") DataSource appCatalogDataSource,
            @Qualifier("registryDataSource") DataSource registryDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource,
            @Qualifier("sharingDataSource") DataSource sharingDataSource,
            @Qualifier("credentialStoreDataSource") DataSource credentialStoreDataSource) {
        this.profileServiceFlyway = profileServiceFlyway;
        this.appCatalogFlyway = appCatalogFlyway;
        this.expCatalogFlyway = expCatalogFlyway;
        this.replicaCatalogFlyway = replicaCatalogFlyway;
        this.sharingRegistryFlyway = sharingRegistryFlyway;
        this.credentialStoreFlyway = credentialStoreFlyway;
        this.profileServiceDataSource = profileServiceDataSource;
        this.appCatalogDataSource = appCatalogDataSource;
        this.registryDataSource = registryDataSource;
        this.replicaDataSource = replicaDataSource;
        this.sharingDataSource = sharingDataSource;
        this.credentialStoreDataSource = credentialStoreDataSource;
    }

    public void initializeDatabases(boolean clean) {
        logger.info("Starting database initialization (clean={})...", clean);

        DatabaseInitializer.initializeDatabase(
                "profile_service",
                profileServiceFlyway,
                profileServiceDataSource,
                "user_profile_catalog_version",
                DatabaseVersionConstants.PROFILE_SERVICE_VERSION,
                "CONFIG_VAL",
                false,
                clean);
        DatabaseInitializer.initializeDatabase(
                "app_catalog",
                appCatalogFlyway,
                appCatalogDataSource,
                "app_catalog_version",
                DatabaseVersionConstants.APP_CATALOG_VERSION,
                "CONFIG_VAL",
                false,
                clean);
        DatabaseInitializer.initializeDatabase(
                "experiment_catalog",
                expCatalogFlyway,
                registryDataSource,
                "registry.version",
                DatabaseVersionConstants.EXPERIMENT_CATALOG_VERSION,
                "CONFIG_VAL",
                true,
                clean);
        DatabaseInitializer.initializeDatabase(
                "replica_catalog",
                replicaCatalogFlyway,
                replicaDataSource,
                "data_catalog_version",
                DatabaseVersionConstants.REPLICA_CATALOG_VERSION,
                "CONFIG_VAL",
                false,
                clean);
        DatabaseInitializer.initializeDatabase(
                "sharing_registry",
                sharingRegistryFlyway,
                sharingDataSource,
                "sharing_reg_version",
                DatabaseVersionConstants.SHARING_REGISTRY_VERSION,
                "CONFIG_VALUE",
                false,
                clean);
        DatabaseInitializer.initializeDatabase(
                "credential_store",
                credentialStoreFlyway,
                credentialStoreDataSource,
                "credential_store_version",
                DatabaseVersionConstants.CREDENTIAL_STORE_VERSION,
                "CONFIG_VAL",
                false,
                clean);

        System.out.println("✓ Database initialization completed successfully.");
        logger.info("Database initialization completed successfully.");
    }
}
