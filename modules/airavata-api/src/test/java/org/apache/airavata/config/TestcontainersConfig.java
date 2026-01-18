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
import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.time.Duration;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration using Testcontainers to provide infrastructure services for testing.
 * All services are fully managed by Testcontainers - no external dependencies required.
 * Each persistence unit gets its own database container.
 * Flyway migrations are applied automatically to each database.
 * Messaging services (Kafka, RabbitMQ) are shared across tests for performance.
 * SLURM and SFTP containers are available for connectivity tests.
 *
 * <h2>Expected Warnings</h2>
 *
 * <h3>Keycloak Container Warnings</h3>
 * <p>The Keycloak container generates several warnings that are expected and can be safely ignored:</p>
 * <ul>
 *   <li><b>JDBC resource leak warnings</b>: Keycloak's internal H2 database connection pool generates
 *       warnings about unclosed resources. These are normal for container-based testing and don't
 *       indicate actual leaks in the Airavata codebase.</li>
 *   <li><b>"Running the server in development mode"</b>: Expected when running Keycloak in a test
 *       container. The container uses dev mode for faster startup.</li>
 *   <li><b>"KC-SERVICES0047: deprecated properties"</b>: Some Keycloak configuration properties
 *       used by the testcontainers-keycloak library may be deprecated in newer Keycloak versions.</li>
 * </ul>
 * <p>These warnings are suppressed via logging configuration in application.properties.</p>
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

    // Shared database containers - reused across tests for performance
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
    private static GenericContainer<?> zookeeperContainer;

    // Infrastructure containers for connectivity tests
    private static GenericContainer<?> slurmContainer;
    private static GenericContainer<?> sftpContainer;

    // IAM container
    private static KeycloakContainer keycloakContainer;

    /**
     * Connection information for SLURM container.
     */
    public record SlurmConnectionInfo(String host, int port, String user, String password) {}

    /**
     * Connection information for SFTP container.
     */
    public record SftpConnectionInfo(String host, int port, String user, String password) {}

    /**
     * Get or create SLURM container. Always starts a fresh container managed by Testcontainers.
     * Uses giovtorres/slurm-docker-cluster:latest which is more commonly available.
     * For ARM64 systems, uses a longer timeout due to emulation overhead.
     * Falls back gracefully if container cannot be started.
     */
    public static synchronized SlurmConnectionInfo getSlurmContainer() {
        if (slurmContainer == null || !slurmContainer.isRunning()) {
            logger.info("Starting SLURM container");
            
            // Detect system architecture
            String arch = System.getProperty("os.arch", "").toLowerCase();
            boolean isArm64 = arch.equals("aarch64") || arch.equals("arm64");
            
            if (isArm64) {
                logger.warn("ARM64 architecture detected. SLURM container will run under emulation and may be slow.");
                logger.warn("Consider using an ARM64-native SLURM image or skipping SLURM tests on ARM64 systems.");
            }
            
            try {
                // Use giovtorres/slurm-docker-cluster which is more commonly available
                // This image runs slurmd with SSH enabled on port 22
                // For ARM64, use longer timeout due to emulation overhead
                Duration startupTimeout = isArm64 ? Duration.ofMinutes(15) : Duration.ofMinutes(5);
                
                slurmContainer = new GenericContainer<>(DockerImageName.parse("giovtorres/slurm-docker-cluster:latest"))
                        .withExposedPorts(22)
                        .withPrivilegedMode(true)
                        .withReuse(true)
                        // Wait for SSH to be ready using log message - the container logs "sshd" when SSH starts
                        // This is more reliable than port listening on slow emulated containers
                        .waitingFor(Wait.forLogMessage(".*sshd.*", 1)
                                .withStartupTimeout(startupTimeout));
                slurmContainer.start();
                
                // Additional wait for SSH service to fully initialize after container reports ready
                // The SSH daemon may take additional time to accept connections even after logging startup
                // On ARM64 emulation, wait much longer since everything is slow
                int sshWaitSeconds = isArm64 ? 120 : 45;
                waitForSshReady(slurmContainer.getHost(), slurmContainer.getMappedPort(22), sshWaitSeconds);
                
                logger.info("SLURM container started at {}:{}", slurmContainer.getHost(), slurmContainer.getMappedPort(22));
            } catch (Exception e) {
                logger.error("Failed to start SLURM container: {}", e.getMessage());
                if (isArm64) {
                    logger.error("SLURM container failed on ARM64. This is likely due to architecture mismatch.");
                    logger.error("The giovtorres/slurm-docker-cluster image is amd64-only and requires emulation on ARM64.");
                    logger.error("Consider using an ARM64-compatible alternative or skipping SLURM connectivity tests.");
                }
                throw new RuntimeException("SLURM container unavailable: " + e.getMessage(), e);
            }
        }
        return new SlurmConnectionInfo(
                slurmContainer.getHost(),
                slurmContainer.getMappedPort(22),
                "root", // default user in slurm-docker-cluster
                "root" // default password (or empty in some versions)
        );
    }

    /**
     * Wait for SSH to be ready by attempting connections with retries.
     * The SLURM container's SSH service may take time to fully initialize even after
     * the container reports as started.
     */
    private static void waitForSshReady(String host, int port, int maxWaitSeconds) {
        logger.info("Waiting for SSH to be ready at {}:{} (max {} seconds)", host, port, maxWaitSeconds);
        long startTime = System.currentTimeMillis();
        long maxWaitMillis = maxWaitSeconds * 1000L;
        int retryDelayMs = 3000; // 3 seconds between retries
        
        while (System.currentTimeMillis() - startTime < maxWaitMillis) {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 10000);
                // Try to read SSH banner to verify SSH is truly ready
                socket.setSoTimeout(10000);
                java.io.InputStream is = socket.getInputStream();
                byte[] buffer = new byte[256];
                int bytesRead = is.read(buffer);
                if (bytesRead > 0) {
                    String response = new String(buffer, 0, bytesRead);
                    if (response.startsWith("SSH-")) {
                        logger.info("SSH service is ready (received banner: {})", response.trim());
                        return;
                    }
                }
            } catch (Exception e) {
                logger.debug("SSH not ready yet: {}", e.getMessage());
            }
            
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for SSH", e);
            }
        }
        
        logger.warn("SSH service may not be fully ready after {} seconds", maxWaitSeconds);
    }

    /**
     * Check if the system is running on ARM64 architecture (Apple Silicon, etc.)
     * SLURM tests run very slowly under emulation on ARM64.
     */
    public static boolean isArm64Architecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.equals("aarch64") || arch.equals("arm64");
    }

    // Track whether upload directory has been verified for the current container
    private static volatile boolean sftpUploadDirVerified = false;

    /**
     * Get or create SFTP container. Always starts a fresh container managed by Testcontainers.
     * The container is configured with a testuser that has an upload directory at /home/testuser/upload.
     */
    public static synchronized SftpConnectionInfo getSftpContainer() {
        if (sftpContainer == null || !sftpContainer.isRunning()) {
            logger.info("Starting SFTP container");
            sftpUploadDirVerified = false; // Reset verification flag when starting new container
            sftpContainer = new GenericContainer<>(DockerImageName.parse("atmoz/sftp:latest"))
                    .withExposedPorts(22)
                    .withCommand("testuser:testpass:1001:1001:upload")
                    .withReuse(true)
                    // Wait for the SSH server to be fully ready (indicated by log message)
                    .waitingFor(Wait.forLogMessage(".*Server listening on.*", 1)
                            .withStartupTimeout(Duration.ofMinutes(2)));
            sftpContainer.start();
            logger.info("SFTP container started at {}:{}", sftpContainer.getHost(), sftpContainer.getMappedPort(22));
        }

        // Always verify the upload directory exists (handles reused containers)
        if (!sftpUploadDirVerified) {
            ensureSftpUploadDirectoryExists();
            sftpUploadDirVerified = true;
        }

        return new SftpConnectionInfo(
                sftpContainer.getHost(),
                sftpContainer.getMappedPort(22),
                "testuser",
                "testpass");
    }

    /**
     * Ensure the SFTP upload directory exists and has correct permissions.
     */
    private static void ensureSftpUploadDirectoryExists() {
        try {
            var result = sftpContainer.execInContainer("ls", "-la", "/home/testuser/upload");
            if (result.getExitCode() != 0) {
                logger.info("Upload directory doesn't exist, creating it...");
                // Create the directory with correct ownership
                sftpContainer.execInContainer("mkdir", "-p", "/home/testuser/upload");
                sftpContainer.execInContainer("chown", "1001:1001", "/home/testuser/upload");
                sftpContainer.execInContainer("chmod", "755", "/home/testuser/upload");
                logger.info("Created upload directory /home/testuser/upload");
            } else {
                logger.debug("Upload directory exists: {}", result.getStdout().trim());
            }
        } catch (Exception e) {
            logger.warn("Could not verify/create upload directory: {}", e.getMessage());
            // Try to create anyway
            try {
                sftpContainer.execInContainer("mkdir", "-p", "/home/testuser/upload");
                sftpContainer.execInContainer("chown", "1001:1001", "/home/testuser/upload");
                sftpContainer.execInContainer("chmod", "755", "/home/testuser/upload");
            } catch (Exception e2) {
                logger.error("Failed to create upload directory: {}", e2.getMessage());
            }
        }
    }

    /**
     * Get or create Keycloak container for IAM testing.
     * Imports the default realm configuration from test resources.
     *
     * @return The Keycloak auth server URL (e.g., http://localhost:32768)
     */
    public static synchronized String getKeycloakUrl() {
        if (keycloakContainer == null || !keycloakContainer.isRunning()) {
            logger.info("Starting Keycloak container");
            keycloakContainer = new KeycloakContainer("keycloak/keycloak:25.0")
                    .withRealmImportFile("keycloak/realm-default.json")
                    .withAdminUsername("admin")
                    .withAdminPassword("admin")
                    .withReuse(true);
            keycloakContainer.start();
            logger.info("Keycloak container started at: {}", keycloakContainer.getAuthServerUrl());
        }
        return keycloakContainer.getAuthServerUrl();
    }

    /**
     * Check if Keycloak container is running and available.
     *
     * @return true if Keycloak is running, false otherwise
     */
    public static boolean isKeycloakAvailable() {
        return keycloakContainer != null && keycloakContainer.isRunning();
    }

    private static synchronized MariaDBContainer<?> getOrCreateContainer(String databaseName) {
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
            logger.info("Creating new MariaDB container for {}", databaseName);
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
            @SuppressWarnings("resource") // Flyway doesn't implement AutoCloseable and doesn't need to be closed
            Flyway flyway = Flyway.configure()
                    .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                    .locations("classpath:conf/db/migration/" + databaseName)
                    .baselineOnMigrate(true)
                    .validateOnMigrate(true)
                    .load();
            flyway.migrate();
            logger.info("Applied Flyway migrations to {}", databaseName);

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
     * Get or create Kafka container. Always starts a fresh container managed by Testcontainers.
     */
    public static synchronized String getKafkaBootstrapServers() {
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
     * Get or create RabbitMQ container. Always starts a fresh container managed by Testcontainers.
     */
    public static synchronized String getRabbitMQUrl() {
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
     * Get or create Zookeeper container. Always starts a fresh container managed by Testcontainers.
     */
    public static synchronized String getZookeeperConnectionString() {
        if (zookeeperContainer == null || !zookeeperContainer.isRunning()) {
            logger.info("Creating new Zookeeper container");
            zookeeperContainer = new GenericContainer<>(DockerImageName.parse("zookeeper:" + ZOOKEEPER_VERSION))
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
     * Health check utility to verify all services are running.
     */
    public static boolean areAllServicesHealthy() {
        boolean kafkaHealthy = kafkaContainer != null && kafkaContainer.isRunning();
        boolean rabbitMQHealthy = rabbitMQContainer != null && rabbitMQContainer.isRunning();
        boolean zookeeperHealthy = zookeeperContainer != null && zookeeperContainer.isRunning();

        return kafkaHealthy && rabbitMQHealthy && zookeeperHealthy;
    }

    private static DataSource createDataSource(MariaDBContainer<?> container, String dbName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setConnectionTestQuery("SELECT 1");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        logger.info("Creating DataSource for {} at {}", dbName, container.getJdbcUrl());
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
     * Bean to provide Spring AMQP ConnectionFactory.
     * Required for MessagingFactory to create Spring-managed subscribers.
     * Eagerly initialized to ensure it's available for dependency injection.
     */
    @Bean
    @Primary
    public ConnectionFactory rabbitConnectionFactory() {
        String rabbitUrl = getRabbitMQUrl();
        logger.info("Creating Spring AMQP ConnectionFactory for: {}", rabbitUrl);

        // Parse the AMQP URL to extract host and port
        // URL format: amqp://guest:guest@host:port
        try {
            java.net.URI uri = new java.net.URI(rabbitUrl);
            String host = uri.getHost();
            int port = uri.getPort();

            org.springframework.amqp.rabbit.connection.CachingConnectionFactory connectionFactory =
                    new org.springframework.amqp.rabbit.connection.CachingConnectionFactory(host, port);
            connectionFactory.setUsername("guest");
            connectionFactory.setPassword("guest");

            logger.info("Created Spring AMQP ConnectionFactory for {}:{}", host, port);
            return connectionFactory;
        } catch (Exception e) {
            logger.error("Failed to create RabbitMQ ConnectionFactory: {}", e.getMessage());
            throw new RuntimeException("Failed to create RabbitMQ ConnectionFactory", e);
        }
    }

    /**
     * Bean to provide RabbitAdmin for exchange/queue declarations.
     * Required for Spring AMQP-based messaging operations.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        logger.info("Creating RabbitAdmin for test context");
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Bean to provide RabbitTemplate for message publishing.
     * Required for Spring AMQP-based message publishing.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        logger.info("Creating RabbitTemplate for test context");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
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
