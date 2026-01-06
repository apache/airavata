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

import static org.junit.jupiter.api.Assertions.*;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test to verify Flyway migrations run correctly.
 * Tests that migrations are applied and database schema is created.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "flyway.enabled=false" // Disable FlywayConfig since TestcontainersConfig handles migrations
        })
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(AiravataServerProperties.class)
public class FlywayMigrationTest {

    @Test
    public void testAppCatalogMigrations(@Qualifier("appCatalogDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/app_catalog")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");


        for (MigrationInfo info : migrations) {
            assertNotNull(info.getVersion(), "Migration should have version");
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testExperimentCatalogMigrations(@Qualifier("registryDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/experiment_catalog")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testProfileServiceMigrations(@Qualifier("profileServiceDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/profile_service")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testReplicaCatalogMigrations(@Qualifier("replicaDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/replica_catalog")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testWorkflowCatalogMigrations(@Qualifier("workflowDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/workflow_catalog")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testSharingRegistryMigrations(@Qualifier("sharingDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/sharing_registry")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }

    @Test
    public void testCredentialStoreMigrations(@Qualifier("credentialStoreDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:conf/db/migration/credential_store")
                .load();

        MigrationInfo[] migrations = flyway.info().all();
        assertTrue(migrations.length > 0, "Should have migration scripts");

        for (MigrationInfo info : migrations) {
            assertTrue(
                    info.getState().isApplied() || info.getState().isResolved(),
                    "Migration " + info.getVersion() + " should be applied or resolved");
        }
    }
}
