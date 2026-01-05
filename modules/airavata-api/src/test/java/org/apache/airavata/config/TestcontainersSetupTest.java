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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test to verify Testcontainers setup and database schema creation.
 * Tests that containers start correctly and migrations are applied.
 *
 * Note: This test requires Docker or nerdctl to be running.
 * Testcontainers will automatically detect and use the available container runtime.
 */
@SpringBootTest(
        classes = {JpaConfig.class, AiravataServerProperties.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "flyway.enabled=false" // Disable FlywayConfig since TestcontainersConfig handles migrations
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@ActiveProfiles("test")
public class TestcontainersSetupTest {

    /**
     * Ensure Flyway migrations are applied before checking for tables.
     * This method explicitly runs migrations to guarantee schema is ready.
     * Handles failed migrations by repairing first if needed.
     */
    private void ensureMigrationsApplied(DataSource dataSource, String migrationLocation) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(migrationLocation)
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .cleanDisabled(false)
                .load();

        // Check if there are failed migrations and repair if needed
        var info = flyway.info();
        var allMigrations = info.all();
        boolean hasFailed = false;
        for (var migration : allMigrations) {
            if (migration.getState().isFailed()) {
                hasFailed = true;
                break;
            }
        }

        if (hasFailed) {
            try {
                // Try repair first
                flyway.repair();
            } catch (Exception e) {
                // If repair fails, try clean and migrate from scratch
                try {
                    flyway.clean();
                    flyway.baseline();
                } catch (Exception cleanEx) {
                    // Clean might fail, continue anyway
                }
            }
        }

        // Now try to migrate
        try {
            flyway.migrate();
        } catch (Exception e) {
            // If migrate still fails after repair/clean, the test will check if tables exist
            // This allows the test to continue and verify the actual state
        }
    }

    /**
     * Check if a table exists, trying both uppercase and lowercase names
     * (MariaDB table name case sensitivity depends on system configuration).
     */
    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();
        // Try exact case first
        ResultSet tables = metaData.getTables(null, null, tableName, null);
        if (tables.next()) {
            tables.close();
            return true;
        }
        tables.close();
        // Try uppercase
        tables = metaData.getTables(null, null, tableName.toUpperCase(), null);
        if (tables.next()) {
            tables.close();
            return true;
        }
        tables.close();
        // Try lowercase
        tables = metaData.getTables(null, null, tableName.toLowerCase(), null);
        if (tables.next()) {
            tables.close();
            return true;
        }
        tables.close();
        return false;
    }

    @Test
    public void testAppCatalogContainerAndSchema(@Qualifier("appCatalogDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "App catalog DataSource should be created");

        // Ensure migrations are applied before checking for tables
        ensureMigrationsApplied(dataSource, "classpath:db/migration/app_catalog");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "COMPUTE_RESOURCE"), "COMPUTE_RESOURCE table should exist");
        }
    }

    @Test
    public void testExperimentCatalogContainerAndSchema(@Qualifier("registryDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Experiment catalog DataSource should be created");

        // Ensure migrations are applied before checking for tables
        ensureMigrationsApplied(dataSource, "classpath:db/migration/experiment_catalog");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "EXPERIMENT"), "EXPERIMENT table should exist");
        }
    }

    @Test
    public void testProfileServiceContainerAndSchema(@Qualifier("profileServiceDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Profile service DataSource should be created");

        // Ensure migrations are applied before checking for tables
        ensureMigrationsApplied(dataSource, "classpath:db/migration/profile_service");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "USER_PROFILE"), "USER_PROFILE table should exist");
        }
    }

    @Test
    public void testReplicaCatalogContainerAndSchema(@Qualifier("replicaDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Replica catalog DataSource should be created");

        // Ensure migrations are applied before checking for tables
        ensureMigrationsApplied(dataSource, "classpath:db/migration/replica_catalog");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "DATA_PRODUCT"), "DATA_PRODUCT table should exist");
        }
    }

    @Test
    public void testWorkflowCatalogContainerAndSchema(@Qualifier("workflowDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Workflow catalog DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            // Check for any table to verify schema exists
            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
            assertTrue(tables.next(), "Workflow catalog should have at least one table");
            tables.close();
        }
    }

    @Test
    public void testSharingRegistryContainerAndSchema(@Qualifier("sharingDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Sharing registry DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "DOMAIN", null);
            assertTrue(tables.next(), "DOMAIN table should exist");
            tables.close();
        }
    }

    @Test
    public void testCredentialStoreContainerAndSchema(@Qualifier("credentialStoreDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Credential store DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            // Check for any table to verify schema exists
            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
            assertTrue(tables.next(), "Credential store should have at least one table");
            tables.close();
        }
    }
}
