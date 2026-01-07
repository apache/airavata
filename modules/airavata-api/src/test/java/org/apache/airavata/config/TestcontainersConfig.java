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
import java.net.Socket;
import java.time.Duration;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration using Testcontainers to provide infrastructure services for testing.
 * Supports MariaDB databases, Kafka, RabbitMQ, and Zookeeper.
 * Each persistence unit gets its own database container.
 * Flyway migrations are applied automatically to each database.
 * Messaging services (Kafka, RabbitMQ) are shared across tests for performance.
 */
@TestConfiguration
@Profile("test")
public class TestcontainersConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestcontainersConfig.class);
    private static final String MARIADB_VERSION = "10.4.13";
    private static final String KAFKA_VERSION = "7.6.0";
    private static final String RABBITMQ_VERSION = "3.13-management";
    private static final String ZOOKEEPER_VERSION = "3.9";
    private static final String TEST_DATABASE_PREFIX = "test_";

    // Connection details for existing containers (from docker-compose.yml)
    private static final String DB_HOST = System.getProperty("test.db.host", "localhost");
    private static final int DB_PORT = Integer.parseInt(System.getProperty("test.db.port", "13306"));
    private static final String DB_USER = System.getProperty("test.db.user", "airavata");
    private static final String DB_PASSWORD = System.getProperty("test.db.password", "123456");
    private static final String DB_ROOT_PASSWORD = System.getProperty("test.db.root.password", "123456");

    private static final String KAFKA_HOST = System.getProperty("test.kafka.host", "localhost");
    private static final int KAFKA_PORT = Integer.parseInt(System.getProperty("test.kafka.port", "9092"));

    private static final String RABBITMQ_HOST = System.getProperty("test.rabbitmq.host", "localhost");
    private static final int RABBITMQ_PORT = Integer.parseInt(System.getProperty("test.rabbitmq.port", "5672"));
    private static final String RABBITMQ_USER = System.getProperty("test.rabbitmq.user", "guest");
    private static final String RABBITMQ_PASSWORD = System.getProperty("test.rabbitmq.password", "guest");

    private static final String ZOOKEEPER_HOST = System.getProperty("test.zookeeper.host", "localhost");
    private static final int ZOOKEEPER_PORT = Integer.parseInt(System.getProperty("test.zookeeper.port", "2181"));

    // Flag to use existing containers - auto-detected or explicitly set
    private static volatile Boolean useExistingContainers = null;

    /**
     * Check if a service is accessible at the configured host and port.
     * This auto-detects if services from docker-compose are running.
     */
    private static boolean isServiceAccessible(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if MariaDB is accessible at the configured host and port.
     * This auto-detects if services from docker-compose are running.
     */
    private static boolean isMariaDBAccessible() {
        String testUrl =
                String.format("jdbc:mariadb://%s:%d/mysql?autoReconnect=true&tinyInt1isBit=false", DB_HOST, DB_PORT);
        try (var conn = java.sql.DriverManager.getConnection(testUrl, "root", DB_ROOT_PASSWORD)) {
            return conn.isValid(2);
        } catch (Exception e) {
            logger.debug(
                    "MariaDB not accessible at {}:{}, will use Testcontainers: {}", DB_HOST, DB_PORT, e.getMessage());
            return false;
        }
    }

    /**
     * Check if Kafka is accessible at the configured host and port.
     */
    private static boolean isKafkaAccessible() {
        return isServiceAccessible(KAFKA_HOST, KAFKA_PORT);
    }

    /**
     * Check if RabbitMQ is accessible at the configured host and port.
     */
    private static boolean isRabbitMQAccessible() {
        return isServiceAccessible(RABBITMQ_HOST, RABBITMQ_PORT);
    }

    /**
     * Check if Zookeeper is accessible at the configured host and port.
     */
    private static boolean isZookeeperAccessible() {
        return isServiceAccessible(ZOOKEEPER_HOST, ZOOKEEPER_PORT);
    }

    /**
     * Determine if we should use existing containers.
     * Checks explicit flag first, then auto-detects by checking MariaDB accessibility.
     */
    private static boolean shouldUseExistingContainers() {
        if (useExistingContainers != null) {
            return useExistingContainers;
        }

        // Check explicit flag first
        String explicitFlag = System.getProperty("testcontainers.use.existing");
        if (explicitFlag != null) {
            useExistingContainers = explicitFlag.equals("true");
            logger.info("Using explicit flag: testcontainers.use.existing={}", useExistingContainers);
            return useExistingContainers;
        }

        if (System.getenv("TESTCONTAINERS_USE_EXISTING") != null) {
            useExistingContainers = true;
            logger.info("Using environment variable: TESTCONTAINERS_USE_EXISTING is set");
            return useExistingContainers;
        }

        // Auto-detect: check if MariaDB is accessible
        useExistingContainers = isMariaDBAccessible();
        if (useExistingContainers) {
            logger.info("Auto-detected existing MariaDB at {}:{}, will use existing containers", DB_HOST, DB_PORT);
        } else {
            logger.info("No existing MariaDB detected at {}:{}, will use Testcontainers", DB_HOST, DB_PORT);
        }
        return useExistingContainers;
    }

    // Shared containers - reused across tests for performance
    private static MariaDBContainer<?> profileServiceContainer;
    private static MariaDBContainer<?> appCatalogContainer;
    private static MariaDBContainer<?> expCatalogContainer;
    private static MariaDBContainer<?> replicaCatalogContainer;
    private static MariaDBContainer<?> workflowCatalogContainer;
    private static MariaDBContainer<?> sharingRegistryContainer;
    private static MariaDBContainer<?> credentialStoreContainer;
    private static MariaDBContainer<?> researchCatalogContainer;

    // Messaging service containers - shared across all tests
    private static KafkaContainer kafkaContainer;
    private static RabbitMQContainer rabbitMQContainer;
    private static org.testcontainers.containers.GenericContainer<?> zookeeperContainer;

    private static synchronized MariaDBContainer<?> getOrCreateContainer(String databaseName) {
        // If using existing containers, return null (we'll create DataSource directly)
        if (shouldUseExistingContainers()) {
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
            case "research_catalog":
                container = researchCatalogContainer;
                break;
        }

        if (container == null || !container.isRunning()) {
            // Testcontainers will automatically use DOCKER_HOST or TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE
            // environment variables if set. No need to hardcode paths - configure at environment level.
            @SuppressWarnings("resource") // Container is stored in static variable and reused across tests
            MariaDBContainer<?> newContainer = new MariaDBContainer<>(
                            DockerImageName.parse("mariadb:" + MARIADB_VERSION))
                    .withDatabaseName(TEST_DATABASE_PREFIX + databaseName)
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
            newContainer.start();
            container = newContainer;

            // Apply Flyway migrations
            if (!shouldUseExistingContainers()) {
                @SuppressWarnings("resource") // Flyway doesn't implement AutoCloseable and doesn't need to be closed
                Flyway flyway = Flyway.configure()
                        .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                        .locations("classpath:conf/db/migration/" + databaseName)
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
                case "research_catalog":
                    researchCatalogContainer = container;
                    break;
            }
        }

        return container;
    }

    /**
     * Get or create Kafka container. Reuses existing container if available.
     */
    public static synchronized String getKafkaBootstrapServers() {
        if (shouldUseExistingContainers() && isKafkaAccessible()) {
            logger.info("Using existing Kafka at {}:{}", KAFKA_HOST, KAFKA_PORT);
            return KAFKA_HOST + ":" + KAFKA_PORT;
        }

        if (kafkaContainer == null || !kafkaContainer.isRunning()) {
            logger.info("Creating new Kafka container");
            kafkaContainer =
                    new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:" + KAFKA_VERSION)).withReuse(true);
            kafkaContainer.start();
            logger.info("Kafka container started at {}", kafkaContainer.getBootstrapServers());
        }
        return kafkaContainer.getBootstrapServers();
    }

    /**
     * Get or create RabbitMQ container. Reuses existing container if available.
     */
    public static synchronized String getRabbitMQUrl() {
        if (shouldUseExistingContainers() && isRabbitMQAccessible()) {
            logger.info("Using existing RabbitMQ at {}:{}", RABBITMQ_HOST, RABBITMQ_PORT);
            return String.format("amqp://%s:%s@%s:%d/", RABBITMQ_USER, RABBITMQ_PASSWORD, RABBITMQ_HOST, RABBITMQ_PORT);
        }

        if (rabbitMQContainer == null || !rabbitMQContainer.isRunning()) {
            logger.info("Creating new RabbitMQ container");
            rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:" + RABBITMQ_VERSION))
                    .withReuse(true)
                    .waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1)
                            .withStartupTimeout(Duration.ofMinutes(2)));
            rabbitMQContainer.start();
            logger.info("RabbitMQ container started at {}", rabbitMQContainer.getAmqpUrl());
        }
        return rabbitMQContainer.getAmqpUrl();
    }

    /**
     * Get or create Zookeeper container. Reuses existing container if available.
     */
    public static synchronized String getZookeeperConnectionString() {
        if (shouldUseExistingContainers() && isZookeeperAccessible()) {
            logger.info("Using existing Zookeeper at {}:{}", ZOOKEEPER_HOST, ZOOKEEPER_PORT);
            return ZOOKEEPER_HOST + ":" + ZOOKEEPER_PORT;
        }

        if (zookeeperContainer == null || !zookeeperContainer.isRunning()) {
            logger.info("Creating new Zookeeper container");
            zookeeperContainer = new org.testcontainers.containers.GenericContainer<>(
                            DockerImageName.parse("zookeeper:" + ZOOKEEPER_VERSION))
                    .withExposedPorts(2181)
                    .withReuse(true)
                    .waitingFor(Wait.forLogMessage(".*binding to port.*", 1).withStartupTimeout(Duration.ofMinutes(2)));
            zookeeperContainer.start();
            logger.info(
                    "Zookeeper container started at {}:{}",
                    zookeeperContainer.getHost(),
                    zookeeperContainer.getMappedPort(2181));
        }
        return zookeeperContainer.getHost() + ":" + zookeeperContainer.getMappedPort(2181);
    }

    /**
     * Health check utility to verify all services are accessible.
     */
    public static boolean areAllServicesHealthy() {
        boolean dbHealthy = shouldUseExistingContainers() ? isMariaDBAccessible() : true;
        boolean kafkaHealthy = shouldUseExistingContainers()
                ? isKafkaAccessible()
                : (kafkaContainer != null && kafkaContainer.isRunning());
        boolean rabbitMQHealthy = shouldUseExistingContainers()
                ? isRabbitMQAccessible()
                : (rabbitMQContainer != null && rabbitMQContainer.isRunning());
        boolean zookeeperHealthy = shouldUseExistingContainers()
                ? isZookeeperAccessible()
                : (zookeeperContainer != null && zookeeperContainer.isRunning());

        return dbHealthy && kafkaHealthy && rabbitMQHealthy && zookeeperHealthy;
    }

    private static DataSource createDataSource(MariaDBContainer<?> container, String dbName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        if (shouldUseExistingContainers() || container == null) {
            // Create database if it doesn't exist
            String rootJdbcUrl = String.format(
                    "jdbc:mariadb://%s:%d/mysql?autoReconnect=true&tinyInt1isBit=false", DB_HOST, DB_PORT);
            try (var rootConn = java.sql.DriverManager.getConnection(rootJdbcUrl, "root", DB_ROOT_PASSWORD)) {
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
                        .locations("classpath:conf/db/migration/" + dbName)
                        .baselineOnMigrate(true)
                        .validateOnMigrate(false)
                        .cleanDisabled(false)
                        .load();

                // Check current migration state
                var info = flyway.info();
                var pendingMigrations = info.pending();
                var currentVersion = info.current();

                // If there are pending migrations or no current version, we need to migrate
                // But first check if schema exists - if not, we can migrate directly
                boolean schemaExists = false;
                try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD)) {
                    var rs = conn.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
                    schemaExists = rs.next();
                    rs.close();
                } catch (Exception e) {
                    logger.debug("Could not check schema existence: {}", e.getMessage());
                }

                // Check if expected tables exist (not just any tables)
                boolean expectedTablesExist = false;
                if (schemaExists) {
                    try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD)) {
                        var rs = conn.getMetaData().getTables(null, null, null, new String[] {"TABLE"});
                        java.util.Set<String> tableNames = new java.util.HashSet<>();
                        while (rs.next()) {
                            String tableName = rs.getString("TABLE_NAME");
                            if (!tableName.equals("flyway_schema_history")) {
                                tableNames.add(tableName);
                            }
                        }
                        rs.close();
                        // Check for expected tables based on database name
                        if (dbName.equals("credential_store")) {
                            expectedTablesExist = tableNames.contains("CREDENTIALS");
                            if (!expectedTablesExist) {
                                logger.debug(
                                        "credential_store missing CREDENTIALS table. Found tables: {}", tableNames);
                            }
                        } else if (dbName.equals("profile_service")) {
                            expectedTablesExist =
                                    tableNames.contains("USER_PROFILE") || tableNames.contains("NSF_DEMOGRAPHIC");
                            if (!expectedTablesExist) {
                                logger.debug("profile_service missing expected tables. Found tables: {}", tableNames);
                            }
                        } else if (dbName.equals("experiment_catalog")) {
                            // Check for key tables - all must exist
                            boolean hasExperiment = tableNames.contains("EXPERIMENT");
                            boolean hasJobStatus = tableNames.contains("JOB_STATUS");
                            boolean hasProcessStatus = tableNames.contains("PROCESS_STATUS");
                            expectedTablesExist = hasExperiment && hasJobStatus && hasProcessStatus;
                            if (!expectedTablesExist) {
                                logger.warn(
                                        "experiment_catalog missing expected tables. EXPERIMENT: {}, JOB_STATUS: {}, PROCESS_STATUS: {}. Found tables: {}",
                                        hasExperiment,
                                        hasJobStatus,
                                        hasProcessStatus,
                                        tableNames.size() > 20 ? tableNames.size() + " tables" : tableNames);
                            }
                        } else if (dbName.equals("app_catalog")) {
                            // Check for key tables - COMPUTE_RESOURCE and BATCH_QUEUE must exist
                            boolean hasComputeResource = tableNames.contains("COMPUTE_RESOURCE");
                            boolean hasBatchQueue = tableNames.contains("BATCH_QUEUE");
                            expectedTablesExist = hasComputeResource && hasBatchQueue;
                            if (!expectedTablesExist) {
                                logger.warn(
                                        "app_catalog missing expected tables. COMPUTE_RESOURCE: {}, BATCH_QUEUE: {}. Found tables: {}",
                                        hasComputeResource,
                                        hasBatchQueue,
                                        tableNames.size() > 20 ? tableNames.size() + " tables" : tableNames);
                            }
                        } else if (dbName.equals("replica_catalog")) {
                            expectedTablesExist =
                                    tableNames.contains("DATA_PRODUCT") && tableNames.contains("DATA_PRODUCT_METADATA");
                        } else if (dbName.equals("workflow_catalog")) {
                            expectedTablesExist = tableNames.contains("AIRAVATA_WORKFLOW");
                            if (!expectedTablesExist) {
                                logger.debug(
                                        "replica_catalog missing DATA_PRODUCT table. Found tables: {}", tableNames);
                            }
                        } else {
                            // For other databases, if we have any tables, assume they're correct
                            expectedTablesExist = !tableNames.isEmpty();
                        }
                    } catch (Exception e) {
                        logger.debug("Could not check expected tables: {}", e.getMessage());
                    }
                }

                // If schema doesn't exist, expected tables don't exist, or no current version with pending migrations,
                // migrate
                if (!schemaExists || !expectedTablesExist || (currentVersion == null && pendingMigrations.length > 0)) {
                    if (!expectedTablesExist && currentVersion != null) {
                        logger.warn(
                                "Schema {} has migration history but expected tables are missing. Cleaning and re-applying migrations.",
                                dbName);
                        try {
                            flyway.clean();
                        } catch (Exception cleanEx) {
                            logger.debug("Clean failed, will try manual cleanup: {}", cleanEx.getMessage());
                        }
                    }
                    logger.info(
                            "Schema {} is empty or missing expected tables, running migrations from scratch", dbName);
                    flyway.migrate();
                    logger.info("Applied Flyway migrations to {}", dbName);
                } else if (pendingMigrations.length > 0) {
                    // Schema exists but has pending migrations
                    // Check if we can migrate incrementally or need to clean
                    try {
                        // Try to migrate incrementally first
                        flyway.migrate();
                        logger.info("Applied pending Flyway migrations to {}", dbName);
                    } catch (Exception migrateEx) {
                        // If incremental migration fails (e.g., table doesn't exist for ALTER),
                        // clean and migrate from scratch
                        logger.warn(
                                "Incremental migration failed for {}: {}, cleaning and migrating from scratch",
                                dbName,
                                migrateEx.getMessage());
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
                        // Repair schema history after clean
                        try {
                            flyway.repair();
                        } catch (Exception repairEx) {
                            logger.debug(
                                    "Repair failed (may be expected if schema history doesn't exist): {}",
                                    repairEx.getMessage());
                        }
                        // Now migrate from scratch
                        flyway.migrate();
                        logger.info("Applied Flyway migrations to {} after clean", dbName);
                    }
                } else {
                    logger.debug("Schema {} is up to date, no migrations needed", dbName);
                }
            } catch (Exception e) {
                logger.warn("Failed to migrate {}: {}, trying repair and migrate only", dbName, e.getMessage());
                // Fallback: try repair and migrate
                try {
                    @SuppressWarnings("resource")
                    Flyway flyway2 = Flyway.configure()
                            .dataSource(jdbcUrl, DB_USER, DB_PASSWORD)
                            .locations("classpath:conf/db/migration/" + dbName)
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

    @Bean
    public DataSource researchCatalogDataSource() {
        return createDataSource(getOrCreateContainer("research_catalog"), "research_catalog");
    }

    /**
     * Bean to provide Kafka bootstrap servers configuration.
     * Can be injected into tests that need Kafka connectivity.
     */
    @Bean(name = "kafkaBootstrapServers")
    public String kafkaBootstrapServers() {
        return getKafkaBootstrapServers();
    }

    /**
     * Bean to provide RabbitMQ connection URL.
     * Can be injected into tests that need RabbitMQ connectivity.
     */
    @Bean(name = "rabbitMQUrl")
    public String rabbitMQUrl() {
        return getRabbitMQUrl();
    }

    /**
     * Bean to provide Zookeeper connection string.
     * Can be injected into tests that need Zookeeper connectivity.
     */
    @Bean(name = "zookeeperConnectionString")
    public String zookeeperConnectionString() {
        return getZookeeperConnectionString();
    }
}
