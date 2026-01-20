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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import javax.sql.DataSource;
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
 *
 * <p>This configuration provides a UNIFIED database for all entity packages, replacing the
 * previous 8 separate databases. All entities from all packages are stored in a single
 * MariaDB container for simplicity and consistency.
 *
 * <p>Messaging services (Kafka, RabbitMQ) are shared across tests for performance.
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

    // Unified database container - all entities in one database
    private static MariaDBContainer<?> unifiedDatabaseContainer;

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
     * Uses csniper/slurm-lab which supports both arm64 and amd64 architectures.
     * Falls back gracefully if container cannot be started.
     */
    public static synchronized SlurmConnectionInfo getSlurmContainer() {
        if (slurmContainer == null || !slurmContainer.isRunning()) {
            logger.info("Starting SLURM container");

            try {
                // Use csniper/slurm-lab which supports both arm64 and amd64 architectures
                // This image runs slurmd with SSH enabled on port 22
                Duration startupTimeout = Duration.ofMinutes(5);

                slurmContainer = new GenericContainer<>(DockerImageName.parse("csniper/slurm-lab:latest"))
                        .withExposedPorts(22)
                        .withPrivilegedMode(true)
                        .withReuse(true)
                        // Wait for any log output to indicate container has started
                        // The waitForSshReady method will handle SSH-specific readiness
                        .waitingFor(Wait.forLogMessage(".*", 1).withStartupTimeout(startupTimeout));
                slurmContainer.start();

                // Configure SSH to allow root password authentication
                // The csniper/slurm-lab image may have root login disabled by default
                try {
                    // Set root password
                    slurmContainer.execInContainer("bash", "-c", "echo 'root:root' | chpasswd");
                    // Enable password authentication and root login in SSH config
                    slurmContainer.execInContainer(
                            "bash",
                            "-c",
                            "sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && "
                                    + "sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && "
                                    + "sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config");
                    // Restart SSH service
                    try {
                        slurmContainer.execInContainer("systemctl", "restart", "sshd");
                    } catch (Exception e) {
                        // Try alternative restart methods
                        try {
                            slurmContainer.execInContainer("service", "ssh", "restart");
                        } catch (Exception e2) {
                            slurmContainer.execInContainer("service", "sshd", "restart");
                        }
                    }
                    logger.info("Configured SSH for password authentication");
                } catch (Exception e) {
                    logger.warn("Could not configure SSH settings, continuing anyway: {}", e.getMessage());
                }

                // Additional wait for SSH service to fully initialize after container reports ready
                // The SSH daemon may take additional time to accept connections even after port is listening
                int sshWaitSeconds = 45;
                waitForSshReady(slurmContainer.getHost(), slurmContainer.getMappedPort(22), sshWaitSeconds);

                logger.info(
                        "SLURM container started at {}:{}", slurmContainer.getHost(), slurmContainer.getMappedPort(22));
            } catch (Exception e) {
                logger.error("Failed to start SLURM container: {}", e.getMessage());
                throw new RuntimeException("SLURM container unavailable: " + e.getMessage(), e);
            }
        }
        return new SlurmConnectionInfo(
                slurmContainer.getHost(),
                slurmContainer.getMappedPort(22),
                "root", // default user in csniper/slurm-lab
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
     * Uses emberstack/sftp which supports both ARM64 and AMD64 architectures.
     */
    public static synchronized SftpConnectionInfo getSftpContainer() {
        if (sftpContainer == null || !sftpContainer.isRunning()) {
            logger.info("Starting SFTP container (emberstack/sftp - multi-arch support)");
            sftpUploadDirVerified = false; // Reset verification flag when starting new container
            
            // Load SFTP configuration file from test resources
            Path sftpConfigPath = null;
            try {
                InputStream configStream = TestcontainersConfig.class.getResourceAsStream("/sftp.json");
                if (configStream == null) {
                    throw new IllegalStateException("SFTP configuration file /sftp.json not found in test resources");
                }
                sftpConfigPath = Files.createTempFile("sftp-config", ".json");
                Files.copy(configStream, sftpConfigPath, StandardCopyOption.REPLACE_EXISTING);
                configStream.close();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load SFTP configuration file", e);
            }
            
            final Path configPath = sftpConfigPath;
            // Use emberstack/sftp which supports both ARM64 and AMD64 architectures
            sftpContainer = new GenericContainer<>(DockerImageName.parse("emberstack/sftp:latest"))
                    .withExposedPorts(22)
                    .withFileSystemBind(configPath.toString(), "/app/config/sftp.json")
                    .withReuse(true)
                    // Wait for the SSH server to be fully ready (indicated by log message)
                    .waitingFor(
                            Wait.forLogMessage(".*Server listening on.*", 1).withStartupTimeout(Duration.ofMinutes(2)));
            sftpContainer.start();
            logger.info("SFTP container started at {}:{}", sftpContainer.getHost(), sftpContainer.getMappedPort(22));
        }

        // Always verify the upload directory exists (handles reused containers)
        if (!sftpUploadDirVerified) {
            ensureSftpUploadDirectoryExists();
            sftpUploadDirVerified = true;
        }

        return new SftpConnectionInfo(sftpContainer.getHost(), sftpContainer.getMappedPort(22), "testuser", "testpass");
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

    /**
     * Get or create unified database container.
     * All entities from all packages are stored in this single database.
     */
    private static synchronized MariaDBContainer<?> getOrCreateUnifiedContainer() {
        if (unifiedDatabaseContainer == null || !unifiedDatabaseContainer.isRunning()) {
            logger.info("Creating unified MariaDB container for all entities");
            @SuppressWarnings("resource") // Container is stored in static variable and reused across tests
            MariaDBContainer<?> newContainer = new MariaDBContainer<>(
                            DockerImageName.parse("mariadb:" + MARIADB_VERSION))
                    .withDatabaseName("airavata_unified")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);
            newContainer.start();
            unifiedDatabaseContainer = newContainer;
            logger.info("Unified database container started at {}", unifiedDatabaseContainer.getJdbcUrl());
        }
        return unifiedDatabaseContainer;
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

    private static DataSource createDataSource(MariaDBContainer<?> container, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setConnectionTestQuery("SELECT 1");
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(20); // Increased pool size for unified database
        config.setConnectionTimeout(30000);
        config.setPoolName(poolName);
        logger.info("Creating DataSource '{}' at {}", poolName, container.getJdbcUrl());
        return new HikariDataSource(config);
    }

    /**
     * Primary DataSource for all entities.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return createDataSource(getOrCreateUnifiedContainer(), "AiravataTestPool");
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

    /**
     * Creates the EXPERIMENT_SUMMARY database view after Hibernate DDL runs.
     * Hibernate's ddl-auto=create-drop creates a TABLE for ExperimentSummaryEntity,
     * but it should be a VIEW that aggregates data from EXPERIMENT, EXPERIMENT_STATUS,
     * and USER_CONFIGURATION_DATA tables.
     */
    @Bean
    public org.springframework.boot.ApplicationRunner createExperimentSummaryView(DataSource dataSource) {
        return args -> {
            logger.info("Creating EXPERIMENT_SUMMARY view for tests");
            try (java.sql.Connection conn = dataSource.getConnection();
                    java.sql.Statement stmt = conn.createStatement()) {
                // Drop the table Hibernate created
                stmt.execute("DROP TABLE IF EXISTS EXPERIMENT_SUMMARY");
                // Drop the view if it exists (for reusable containers)
                stmt.execute("DROP VIEW IF EXISTS LATEST_EXPERIMENT_STATUS");
                stmt.execute("DROP VIEW IF EXISTS EXPERIMENT_SUMMARY");

                // Create LATEST_EXPERIMENT_STATUS view first (EXPERIMENT_SUMMARY depends on it)
                stmt.execute("CREATE VIEW LATEST_EXPERIMENT_STATUS AS "
                        + "SELECT ES1.EXPERIMENT_ID AS EXPERIMENT_ID, ES1.STATE AS STATE, "
                        + "ES1.TIME_OF_STATE_CHANGE AS TIME_OF_STATE_CHANGE "
                        + "FROM EXPERIMENT_STATUS ES1 LEFT JOIN EXPERIMENT_STATUS ES2 "
                        + "ON (ES1.EXPERIMENT_ID = ES2.EXPERIMENT_ID "
                        + "AND ES1.TIME_OF_STATE_CHANGE < ES2.TIME_OF_STATE_CHANGE) "
                        + "WHERE ES2.TIME_OF_STATE_CHANGE IS NULL");

                // Create EXPERIMENT_SUMMARY view
                stmt.execute("CREATE VIEW EXPERIMENT_SUMMARY AS "
                        + "SELECT E.EXPERIMENT_ID AS EXPERIMENT_ID, E.PROJECT_ID AS PROJECT_ID, "
                        + "E.GATEWAY_ID AS GATEWAY_ID, E.USER_NAME AS USER_NAME, "
                        + "E.EXECUTION_ID AS EXECUTION_ID, E.EXPERIMENT_NAME AS EXPERIMENT_NAME, "
                        + "E.CREATION_TIME AS CREATION_TIME, E.DESCRIPTION AS DESCRIPTION, "
                        + "ES.STATE AS STATE, UD.RESOURCE_HOST_ID AS RESOURCE_HOST_ID, "
                        + "ES.TIME_OF_STATE_CHANGE AS TIME_OF_STATE_CHANGE "
                        + "FROM ((EXPERIMENT E LEFT JOIN LATEST_EXPERIMENT_STATUS ES "
                        + "ON (E.EXPERIMENT_ID = ES.EXPERIMENT_ID)) "
                        + "LEFT JOIN USER_CONFIGURATION_DATA UD "
                        + "ON (E.EXPERIMENT_ID = UD.EXPERIMENT_ID)) WHERE 1");

                logger.info("Successfully created EXPERIMENT_SUMMARY view");
            } catch (Exception e) {
                logger.error("Failed to create EXPERIMENT_SUMMARY view: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to create EXPERIMENT_SUMMARY view", e);
            }
        };
    }
}
