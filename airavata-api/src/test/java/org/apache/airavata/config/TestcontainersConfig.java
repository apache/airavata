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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration using Testcontainers to provide MariaDB databases for testing.
 * Each persistence unit gets its own database container.
 * Flyway migrations are applied automatically to each database.
 */
@TestConfiguration
@Profile("test")
public class TestcontainersConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestcontainersConfig.class);
    private static final String MARIADB_VERSION = "10.4.13";
    private static final String TEST_DATABASE_PREFIX = "test_";

    // Flag to use existing containers instead of Testcontainers
    private static final boolean USE_EXISTING_CONTAINERS =
            System.getProperty("testcontainers.use.existing", "false").equals("true")
                    || System.getenv("TESTCONTAINERS_USE_EXISTING") != null;

    // Connection details for existing MariaDB containers
    private static final String DB_HOST = System.getProperty("test.db.host", "localhost");
    private static final int DB_PORT = Integer.parseInt(System.getProperty("test.db.port", "13306"));
    private static final String DB_USER = System.getProperty("test.db.user", "airavata");
    private static final String DB_PASSWORD = System.getProperty("test.db.password", "123456");

    // Shared containers - reused across tests for performance
    private static MariaDBContainer<?> profileServiceContainer;
    private static MariaDBContainer<?> appCatalogContainer;
    private static MariaDBContainer<?> expCatalogContainer;
    private static MariaDBContainer<?> replicaCatalogContainer;
    private static MariaDBContainer<?> workflowCatalogContainer;
    private static MariaDBContainer<?> sharingRegistryContainer;
    private static MariaDBContainer<?> credentialStoreContainer;

    private static synchronized MariaDBContainer<?> getOrCreateContainer(String databaseName) {
        // If using existing containers, return null (we'll create DataSource directly)
        if (USE_EXISTING_CONTAINERS) {
            logger.info("Using existing MariaDB container for {}", databaseName);
            return null;
        }

        MariaDBContainer<?> container = null;
        switch (databaseName) {
            case "profile_service":
                container = profileServiceContainer;
                break;
            case "app_catalog":
                container = appCatalogContainer;
                break;
            case "experiment_catalog":
                container = expCatalogContainer;
                break;
            case "replica_catalog":
                container = replicaCatalogContainer;
                break;
            case "workflow_catalog":
                container = workflowCatalogContainer;
                break;
            case "sharing_registry":
                container = sharingRegistryContainer;
                break;
            case "credential_store":
                container = credentialStoreContainer;
                break;
        }

        if (container == null || !container.isRunning()) {
            // Configure Testcontainers to use Rancher Desktop socket
            String rdSocket = System.getProperty("user.home") + "/.rd/docker.sock";
            File rdSocketFile = new File(rdSocket);
            if (rdSocketFile.exists()) {
                System.setProperty("docker.host", "unix://" + rdSocket);
                System.setProperty("DOCKER_HOST", "unix://" + rdSocket);
            }

            container = new MariaDBContainer<>(DockerImageName.parse("mariadb:" + MARIADB_VERSION))
                    .withDatabaseName(TEST_DATABASE_PREFIX + databaseName)
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
            container.start();

            // Apply Flyway migrations
            if (!USE_EXISTING_CONTAINERS) {
                @SuppressWarnings("resource") // Flyway doesn't implement AutoCloseable and doesn't need to be closed
                Flyway flyway = Flyway.configure()
                        .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                        .locations("classpath:db/migration/" + databaseName)
                        .baselineOnMigrate(true)
                        .validateOnMigrate(true)
                        .load();
                flyway.migrate();
            }

            // Cache the container
            switch (databaseName) {
                case "profile_service":
                    profileServiceContainer = container;
                    break;
                case "app_catalog":
                    appCatalogContainer = container;
                    break;
                case "experiment_catalog":
                    expCatalogContainer = container;
                    break;
                case "replica_catalog":
                    replicaCatalogContainer = container;
                    break;
                case "workflow_catalog":
                    workflowCatalogContainer = container;
                    break;
                case "sharing_registry":
                    sharingRegistryContainer = container;
                    break;
                case "credential_store":
                    credentialStoreContainer = container;
                    break;
            }
        }

        return container;
    }

    private static DataSource createDataSource(MariaDBContainer<?> container, String dbName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        if (USE_EXISTING_CONTAINERS || container == null) {
            // Create database if it doesn't exist
            String rootJdbcUrl = String.format(
                    "jdbc:mariadb://%s:%d/mysql?autoReconnect=true&tinyInt1isBit=false", DB_HOST, DB_PORT);
            try (var rootConn = java.sql.DriverManager.getConnection(rootJdbcUrl, "root", "123456")) {
                try (var stmt = rootConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
                    logger.info("Ensured database {} exists", dbName);
                }
            } catch (Exception e) {
                logger.warn("Failed to create database {}: {}", dbName, e.getMessage());
            }

            String jdbcUrl = String.format(
                    "jdbc:mariadb://%s:%d/%s?autoReconnect=true&tinyInt1isBit=false", DB_HOST, DB_PORT, dbName);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            logger.info("Connecting to existing MariaDB container: {}", jdbcUrl);

            // Apply Flyway migrations to existing database
            try {
                @SuppressWarnings("resource")
                Flyway flyway = Flyway.configure()
                        .dataSource(jdbcUrl, DB_USER, DB_PASSWORD)
                        .locations("classpath:db/migration/" + dbName)
                        .baselineOnMigrate(true)
                        .validateOnMigrate(false)
                        .cleanDisabled(false)
                        .load();

                // Check if migration is needed
                var info = flyway.info();
                var pendingMigrations = info.pending();

                // Only clean if there are pending migrations or if schema history shows issues
                if (pendingMigrations.length > 0 || info.current() == null) {
                    // Clean existing schema and run migrations from scratch
                    try {
                        flyway.clean();
                        logger.info("Cleaned schema {}", dbName);
                    } catch (Exception cleanEx) {
                        logger.debug(
                                "Clean failed (may be expected if schema doesn't exist): {}", cleanEx.getMessage());
                        // Try to drop tables manually if clean fails
                        try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD)) {
                            try (var stmt = conn.createStatement()) {
                                var rs = conn.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
                                java.util.List<String> tables = new java.util.ArrayList<>();
                                while (rs.next()) {
                                    String tableName = rs.getString("TABLE_NAME");
                                    if (!tableName.equals("flyway_schema_history")) {
                                        tables.add(tableName);
                                    }
                                }
                                rs.close();
                                if (!tables.isEmpty()) {
                                    stmt.execute("SET FOREIGN_KEY_CHECKS=0");
                                    for (String table : tables) {
                                        try {
                                            stmt.executeUpdate("DROP TABLE IF EXISTS `" + table + "`");
                                        } catch (Exception dropEx) {
                                            logger.debug("Failed to drop table {}: {}", table, dropEx.getMessage());
                                        }
                                    }
                                    stmt.execute("SET FOREIGN_KEY_CHECKS=1");
                                    logger.info("Manually dropped {} tables from {}", tables.size(), dbName);
                                }
                            }
                        } catch (Exception manualCleanEx) {
                            logger.debug("Manual clean also failed: {}", manualCleanEx.getMessage());
                        }
                    }
                }

                // Repair schema history after clean to ensure consistency
                try {
                    flyway.repair();
                } catch (Exception repairEx) {
                    logger.debug(
                            "Repair failed (may be expected if schema history doesn't exist): {}",
                            repairEx.getMessage());
                }

                flyway.migrate();
                logger.info("Applied Flyway migrations to {}", dbName);
            } catch (Exception e) {
                logger.warn("Failed to migrate {}: {}, trying repair and migrate only", dbName, e.getMessage());
                // Fallback: try repair and migrate
                try {
                    @SuppressWarnings("resource")
                    Flyway flyway2 = Flyway.configure()
                            .dataSource(jdbcUrl, DB_USER, DB_PASSWORD)
                            .locations("classpath:db/migration/" + dbName)
                            .baselineOnMigrate(true)
                            .validateOnMigrate(false)
                            .load();
                    flyway2.repair();
                    flyway2.migrate();
                    logger.info("Repaired and applied Flyway migrations to {}", dbName);
                } catch (Exception e2) {
                    logger.error("Failed to repair/migrate {}: {}", dbName, e2.getMessage(), e2);
                    // Last resort: try to continue anyway - Hibernate will create tables if needed
                    logger.warn("Continuing with potentially incomplete schema for {}", dbName);
                }
            }
        } else {
            // Use Testcontainers container
            config.setJdbcUrl(container.getJdbcUrl());
            config.setUsername(container.getUsername());
            config.setPassword(container.getPassword());
        }

        config.setConnectionTestQuery("SELECT 1");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public DataSource profileServiceDataSource() {
        return createDataSource(getOrCreateContainer("profile_service"), "profile_service");
    }

    @Bean
    public DataSource appCatalogDataSource() {
        return createDataSource(getOrCreateContainer("app_catalog"), "app_catalog");
    }

    @Bean
    public DataSource registryDataSource() {
        return createDataSource(getOrCreateContainer("experiment_catalog"), "experiment_catalog");
    }

    @Bean
    public DataSource replicaDataSource() {
        return createDataSource(getOrCreateContainer("replica_catalog"), "replica_catalog");
    }

    @Bean
    public DataSource workflowDataSource() {
        return createDataSource(getOrCreateContainer("workflow_catalog"), "workflow_catalog");
    }

    @Bean
    public DataSource sharingDataSource() {
        return createDataSource(getOrCreateContainer("sharing_registry"), "sharing_registry");
    }

    @Bean
    public DataSource credentialStoreDataSource() {
        return createDataSource(getOrCreateContainer("credential_store"), "credential_store");
    }
}
