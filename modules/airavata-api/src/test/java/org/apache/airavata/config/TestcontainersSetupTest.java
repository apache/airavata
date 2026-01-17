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
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "flyway.enabled=false", // Disable FlywayConfig since TestcontainersConfig handles migrations
            "security.iam.enabled=false",
            "security.manager.enabled=false",
            "security.authzCache.enabled=false",
        })
@TestPropertySource(locations = "classpath:application.properties")
@ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(AiravataServerProperties.class)
public class TestcontainersSetupTest {

    /**
     * Ensure Flyway migrations are applied before checking for tables.
     * This method explicitly runs migrations to guarantee schema is ready.
     * Handles failed migrations by repairing first if needed.
     * Also checks if tables actually exist - if Flyway history says migrations are applied
     * but tables don't exist, cleans and re-applies migrations.
     */
    private void ensureMigrationsApplied(DataSource dataSource, String migrationLocation) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(migrationLocation)
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .cleanDisabled(false)
                .load();

        // Check if any tables exist (other than flyway_schema_history)
        boolean tablesExist = false;
        int tableCount = 0;
        try (var conn = dataSource.getConnection()) {
            var rs = conn.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!tableName.equals("flyway_schema_history")) {
                    tableCount++;
                }
            }
            rs.close();
            tablesExist = tableCount > 0;
        } catch (Exception e) {
            // If we can't check, assume tables don't exist and proceed with migration
        }

        var info = flyway.info();
        var allMigrations = info.all();
        var currentVersion = info.current();
        boolean hasFailed = false;
        for (var migration : allMigrations) {
            if (migration.getState().isFailed()) {
                hasFailed = true;
                break;
            }
        }

        // Check specifically for profile_service tables
        boolean profileTablesExist = false;
        try (var conn = dataSource.getConnection()) {
            var rs = conn.getMetaData().getTables(null, null, "USER_PROFILE", null);
            profileTablesExist = rs.next();
            rs.close();
            if (!profileTablesExist) {
                // Also check NSF_DEMOGRAPHIC tables
                rs = conn.getMetaData().getTables(null, null, "NSF_DEMOGRAPHIC", null);
                profileTablesExist = rs.next();
                rs.close();
            }
        } catch (Exception e) {
            // Ignore
        }

        System.out.println("Migration check: currentVersion=" + currentVersion + ", tablesExist=" + tablesExist
                + ", tableCount=" + tableCount + ", profileTablesExist=" + profileTablesExist
                + ", allMigrations.length=" + allMigrations.length);

        // If Flyway says migrations are applied but profile tables don't exist, clean and re-apply
        if (currentVersion != null && !profileTablesExist && allMigrations.length > 0) {
            try {
                System.out.println("Detected mismatch: Flyway version " + currentVersion
                        + " but no profile tables exist. Cleaning and re-applying migrations.");
                flyway.clean();
                System.out.println("Database cleaned successfully");
                // Don't call baseline() - let migrate() apply migrations from scratch
                var migrateResult = flyway.migrate();
                System.out.println("Migration completed. Migrations applied: " + migrateResult.migrationsExecuted
                        + ", success: " + migrateResult.success);
                if (migrateResult.migrationsExecuted > 0 && !migrateResult.migrations.isEmpty()) {
                    var firstMigration = migrateResult.migrations.get(0);
                    System.out.println(
                            "Applied migration: " + firstMigration.version + " - " + firstMigration.description);
                }
                // Verify tables exist after migration - check multiple ways
                try (var conn = dataSource.getConnection()) {
                    String catalog = conn.getCatalog();
                    System.out.println("Connected to database: " + catalog);
                    var rs = conn.getMetaData().getTables(catalog, null, "USER_PROFILE", null);
                    boolean userProfileExists = rs.next();
                    rs.close();
                    if (!userProfileExists) {
                        // Try case-insensitive
                        rs = conn.getMetaData().getTables(catalog, null, "user_profile", null);
                        userProfileExists = rs.next();
                        rs.close();
                    }
                    // List all tables to see what exists
                    rs = conn.getMetaData().getTables(catalog, null, null, new String[] {"TABLE"});
                    java.util.List<String> tables = new java.util.ArrayList<>();
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_NAME"));
                    }
                    rs.close();
                    System.out.println("After migration, USER_PROFILE exists: " + userProfileExists + ". Total tables: "
                            + tables.size());
                    if (tables.size() < 10) {
                        System.out.println("Tables found: " + tables);
                    }
                }
                return;
            } catch (Exception e) {
                System.out.println("Clean/migrate failed: " + e.getMessage());
                e.printStackTrace();
                // If clean fails, try repair and migrate
                try {
                    flyway.repair();
                    flyway.migrate();
                } catch (Exception e2) {
                    System.out.println("Repair also failed: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
        }

        if (hasFailed) {
            try {
                flyway.repair();
            } catch (Exception e) {
                try {
                    flyway.clean();
                    flyway.baseline();
                } catch (Exception cleanEx) {
                    // Ignore
                }
            }
        }

        try {
            flyway.migrate();
        } catch (Exception e) {
            // If migrate fails, try clean and migrate from scratch
            try {
                flyway.clean();
                flyway.baseline();
                flyway.migrate();
            } catch (Exception e2) {
                // Last resort - log and continue
            }
        }
    }

    /**
     * Check if a table exists, trying both uppercase and lowercase names
     * (MariaDB table name case sensitivity depends on system configuration).
     */
    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();

        ResultSet tables = metaData.getTables(null, null, tableName, null);
        if (tables.next()) {
            tables.close();
            return true;
        }
        tables.close();

        tables = metaData.getTables(null, null, tableName.toUpperCase(), null);
        if (tables.next()) {
            tables.close();
            return true;
        }
        tables.close();

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

        ensureMigrationsApplied(dataSource, "classpath:conf/db/migration/app_catalog");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "COMPUTE_RESOURCE"), "COMPUTE_RESOURCE table should exist");
        }
    }

    @Test
    public void testExperimentCatalogContainerAndSchema(@Qualifier("registryDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Experiment catalog DataSource should be created");

        ensureMigrationsApplied(dataSource, "classpath:conf/db/migration/experiment_catalog");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "EXPERIMENT"), "EXPERIMENT table should exist");
        }
    }

    @Test
    public void testProfileServiceContainerAndSchema(@Qualifier("profileServiceDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Profile service DataSource should be created");

        ensureMigrationsApplied(dataSource, "classpath:conf/db/migration/profile_service");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");
            assertTrue(tableExists(conn, "USER_PROFILE"), "USER_PROFILE table should exist");
        }
    }

    @Test
    public void testReplicaCatalogContainerAndSchema(@Qualifier("replicaDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Replica catalog DataSource should be created");

        ensureMigrationsApplied(dataSource, "classpath:conf/db/migration/replica_catalog");

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

            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
            assertTrue(tables.next(), "Credential store should have at least one table");
            tables.close();
        }
    }
}
