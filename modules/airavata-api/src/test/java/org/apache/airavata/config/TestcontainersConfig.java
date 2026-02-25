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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Test configuration using Testcontainers to provide infrastructure services for testing.
 * All services are fully managed by Testcontainers - no external dependencies required.
 *
 * <p>This configuration provides a UNIFIED database for all entity packages, replacing the
 * previous 8 separate databases. All entities from all packages are stored in a single
 * MariaDB container for simplicity and consistency.
 *
 * <p>SLURM and SFTP containers are available for connectivity tests.
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
    private static final String MARIADB_VERSION = "11.8";

    // Unified database container - all entities in one database
    private static MariaDBContainer<?> unifiedDatabaseContainer;

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
    @SuppressWarnings("resource") // Container stored in static and reused; Testcontainers manages lifecycle
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
    @SuppressWarnings("resource") // Container stored in static and reused; Testcontainers manages lifecycle
    public static synchronized SftpConnectionInfo getSftpContainer() {
        if (sftpContainer == null || !sftpContainer.isRunning()) {
            logger.info("Starting SFTP container (emberstack/sftp - multi-arch support)");
            sftpUploadDirVerified = false; // Reset verification flag when starting new container

            // Load SFTP configuration file from test resources
            Path sftpConfigPath = null;
            try (InputStream configStream = TestcontainersConfig.class.getResourceAsStream("/sftp.json")) {
                if (configStream == null) {
                    throw new IllegalStateException("SFTP configuration file /sftp.json not found in test resources");
                }
                sftpConfigPath = Files.createTempFile("sftp-config", ".json");
                Files.copy(configStream, sftpConfigPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load SFTP configuration file", e);
            }

            final Path configPath = sftpConfigPath;
            // Use emberstack/sftp which supports both ARM64 and AMD64 architectures
            sftpContainer = new GenericContainer<>(DockerImageName.parse("emberstack/sftp:latest"))
                    .withExposedPorts(22)
                    .withCopyFileToContainer(MountableFile.forHostPath(configPath), "/app/config/sftp.json")
                    .withReuse(true)
                    // Wait for port 22 to be listening (more reliable than log message)
                    .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
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
    @SuppressWarnings("resource") // Container stored in static and reused; Testcontainers manages lifecycle
    public static synchronized String getKeycloakUrl() {
        if (keycloakContainer == null || !keycloakContainer.isRunning()) {
            logger.info("Starting Keycloak container");

            // Load realm file using test class's classloader (fixes classloader issue)
            // The testcontainers-keycloak library's withRealmImportFile() uses Application ClassLoader
            // which doesn't have access to Maven test resources. We work around this by:
            // 1. Loading the file using the test class's classloader
            // 2. Copying it to the container's import directory
            // 3. Keycloak will automatically import it on startup
            Path realmPath = null;
            try {
                try (InputStream realmStream =
                        TestcontainersConfig.class.getResourceAsStream("/keycloak/realm-default.json")) {
                    if (realmStream == null) {
                        throw new IllegalStateException(
                                "Keycloak realm file /keycloak/realm-default.json not found in test resources");
                    }
                    realmPath = Files.createTempFile("realm-default", ".json");
                    Files.copy(realmStream, realmPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load Keycloak realm file", e);
            }

            final Path configPath = realmPath;
            // Keycloak automatically imports realm files from /opt/keycloak/data/import/ on startup
            // Use quay.io/keycloak:26.5 (same as compose.yml and KeycloakTestConfig); withAdminUsername creates master
            // realm admin
            keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.5")
                    .withCopyFileToContainer(
                            org.testcontainers.utility.MountableFile.forHostPath(configPath),
                            "/opt/keycloak/data/import/realm-default.json")
                    .withAdminUsername("admin")
                    .withAdminPassword("admin")
                    .withReuse(true)
                    // Keycloak exposes health on management port 9000; wait for main server log instead
                    .waitingFor(Wait.forLogMessage(".*Listening on:.*", 1).withStartupTimeout(Duration.ofMinutes(5)));
            keycloakContainer.start();
            logger.info("Keycloak container started at: {}", keycloakContainer.getAuthServerUrl());

            // Enable direct access grants on admin-cli client in master realm
            // This is required for password-based authentication in Keycloak 25+
            // where admin-cli no longer has directAccessGrantsEnabled by default
            enableAdminCliDirectAccessGrantsWithRetry();
            // Verify master realm admin token can be obtained (so IamAdminService.getUsers etc. work)
            waitForMasterRealmAdminToken(keycloakContainer.getAuthServerUrl());
        }
        return keycloakContainer.getAuthServerUrl();
    }

    private static final int ADMIN_CLI_RETRY_ATTEMPTS = 5;
    private static final long ADMIN_CLI_RETRY_DELAY_MS = 2000;
    private static final int MASTER_TOKEN_VERIFY_ATTEMPTS = 15;
    private static final long MASTER_TOKEN_VERIFY_DELAY_MS = 2000;

    private static void enableAdminCliDirectAccessGrantsWithRetry() {
        for (int attempt = 1; attempt <= ADMIN_CLI_RETRY_ATTEMPTS; attempt++) {
            if (enableAdminCliDirectAccessGrants()) {
                return;
            }
            if (attempt < ADMIN_CLI_RETRY_ATTEMPTS) {
                try {
                    Thread.sleep(ADMIN_CLI_RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while enabling admin-cli direct access grants", e);
                }
            }
        }
        logger.warn("Could not enable admin-cli direct access grants after {} attempts", ADMIN_CLI_RETRY_ATTEMPTS);
    }

    /**
     * Verify that master realm admin token can be obtained via password grant (admin-cli client).
     * Retries until success or max attempts, so IamAdminService tests have the required Keycloak state.
     */
    private static void waitForMasterRealmAdminToken(String serverUrl) {
        String tokenUrl = serverUrl + "/realms/master/protocol/openid-connect/token";
        String formBody = "grant_type=password&username=admin&password=admin&client_id=admin-cli";
        for (int attempt = 1; attempt <= MASTER_TOKEN_VERIFY_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) URI.create(tokenUrl).toURL().openConnection();
                try {
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    try (OutputStream out = conn.getOutputStream()) {
                        out.write(formBody.getBytes(StandardCharsets.UTF_8));
                    }
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            String response = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                            if (response.contains("access_token")) {
                                logger.info("Master realm admin token verified after {} attempt(s)", attempt);
                                return;
                            }
                        }
                    }
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                logger.debug(
                        "Master realm admin token attempt {}/{} failed: {}",
                        attempt,
                        MASTER_TOKEN_VERIFY_ATTEMPTS,
                        e.getMessage());
            }
            if (attempt < MASTER_TOKEN_VERIFY_ATTEMPTS) {
                try {
                    Thread.sleep(MASTER_TOKEN_VERIFY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for master realm admin token", e);
                }
            }
        }
        throw new IllegalStateException("Keycloak testcontainer: master realm admin token could not be obtained after "
                + MASTER_TOKEN_VERIFY_ATTEMPTS + " attempts. "
                + "Ensure Keycloak image has bootstrap admin in master realm and admin-cli has direct access grants enabled. "
                + "IamAdminService integration tests require this to run without skips.");
    }

    /**
     * Enable direct access grants on the admin-cli client in the master realm.
     * This is required for password-based authentication in Keycloak 25+ where
     * the admin-cli client no longer has directAccessGrantsEnabled by default.
     *
     * <p>This method uses the Keycloak admin CLI (kcadm.sh) to update the admin-cli
     * client configuration.
     *
     * @return true if direct access grants were enabled successfully, false otherwise
     */
    private static boolean enableAdminCliDirectAccessGrants() {
        if (keycloakContainer == null || !keycloakContainer.isRunning()) {
            logger.warn("Cannot enable admin-cli direct access grants: Keycloak container not running");
            return false;
        }

        try {
            // First, authenticate with kcadm
            var authResult = keycloakContainer.execInContainer(
                    "/opt/keycloak/bin/kcadm.sh",
                    "config",
                    "credentials",
                    "--server",
                    "http://localhost:8080",
                    "--realm",
                    "master",
                    "--user",
                    "admin",
                    "--password",
                    "admin");

            if (authResult.getExitCode() != 0) {
                logger.warn("Failed to authenticate with kcadm: {} {}", authResult.getStdout(), authResult.getStderr());
                return false;
            }

            // Get the client ID (internal UUID) of admin-cli
            var getClientResult = keycloakContainer.execInContainer(
                    "/opt/keycloak/bin/kcadm.sh",
                    "get",
                    "clients",
                    "-r",
                    "master",
                    "-q",
                    "clientId=admin-cli",
                    "--fields",
                    "id");

            if (getClientResult.getExitCode() != 0) {
                logger.warn(
                        "Failed to get admin-cli client: {} {}",
                        getClientResult.getStdout(),
                        getClientResult.getStderr());
                return false;
            }

            // Parse the client ID from the JSON response (allow newlines/whitespace)
            String clientOutput = getClientResult.getStdout().replace("\n", " ").replace("\r", " ");
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(clientOutput);
            String clientUuid = matcher.find() ? matcher.group(1).trim() : null;

            if (clientUuid == null || clientUuid.isEmpty()) {
                logger.warn("Could not parse admin-cli client UUID from: {}", getClientResult.getStdout());
                return false;
            }

            // Enable direct access grants on the admin-cli client
            var updateResult = keycloakContainer.execInContainer(
                    "/opt/keycloak/bin/kcadm.sh",
                    "update",
                    "clients/" + clientUuid,
                    "-r",
                    "master",
                    "-s",
                    "directAccessGrantsEnabled=true");

            if (updateResult.getExitCode() == 0) {
                logger.info("Successfully enabled direct access grants on admin-cli client");
                return true;
            }
            logger.warn(
                    "Failed to enable direct access grants: {} {}", updateResult.getStdout(), updateResult.getStderr());
            return false;
        } catch (Exception e) {
            logger.warn("Failed to enable admin-cli direct access grants: {}", e.getMessage());
            return false;
        }
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
    @SuppressWarnings("resource") // Container stored in static and reused; Testcontainers manages lifecycle
    private static synchronized MariaDBContainer<?> getOrCreateUnifiedContainer() {
        if (unifiedDatabaseContainer == null || !unifiedDatabaseContainer.isRunning()) {
            logger.info("Creating unified MariaDB container for all entities");
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
        // Configure shutdown behavior to prevent premature closure during Hibernate cleanup
        // This ensures the DataSource stays open long enough for Hibernate to complete schema cleanup
        config.setInitializationFailTimeout(-1); // Don't fail on initialization timeout
        config.setRegisterMbeans(false); // Disable JMX registration to avoid shutdown conflicts
        // Set longer connection lifetime to prevent premature closure during test cleanup
        // These values ensure connections stay open during Hibernate's delayed drop actions
        config.setMaxLifetime(1800000); // 30 minutes - keep connections alive during shutdown
        config.setIdleTimeout(600000); // 10 minutes idle timeout
        config.setKeepaliveTime(300000); // 5 minutes - keep connections alive
        logger.info("Creating DataSource '{}' at {}", poolName, container.getJdbcUrl());
        return new HikariDataSource(config);
    }

    /**
     * Primary DataSource for all entities.
     * Configured with destroyMethod = "" to prevent Spring from auto-closing the DataSource
     * before Hibernate completes its schema cleanup operations during shutdown.
     * The DataSource will be cleaned up by Testcontainers when the container is removed.
     */
    @Bean(destroyMethod = "")
    @Primary
    public DataSource dataSource() {
        return createDataSource(getOrCreateUnifiedContainer(), "AiravataTestPool");
    }

    /**
     * Creates the EXPERIMENT_SUMMARY database view after Hibernate DDL runs.
     * Also drops any auto-generated foreign key constraints on the EVENT table
     * that Hibernate may have created due to @OneToMany relationships.
     *
     * Hibernate's ddl-auto=create-drop creates a TABLE for ExperimentSummaryEntity,
     * but it should be a VIEW that aggregates data from EXPERIMENT and STATUS (filtered by PARENT_TYPE='EXPERIMENT').
     * RESOURCE_HOST_ID is now on EXPERIMENT (USER_CONFIGURATION_DATA merged).
     */
    @Bean
    public org.springframework.boot.ApplicationRunner createExperimentSummaryView(DataSource dataSource) {
        return args -> {
            logger.info("Creating EXPERIMENT_SUMMARY view and cleaning up constraints for tests");
            try (java.sql.Connection conn = dataSource.getConnection();
                    java.sql.Statement stmt = conn.createStatement()) {

                // Drop any auto-generated foreign key constraints on EVENT table
                dropForeignKeyConstraints(stmt, "EVENT");

                // Fix METADATA table if it has CREATION_TIME (legacy schema): align with entity CREATED_AT/UPDATED_AT
                try {
                    java.sql.ResultSet rs = stmt.executeQuery(
                            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                                    + "AND TABLE_NAME = 'METADATA' AND COLUMN_NAME = 'CREATION_TIME'");
                    if (rs.next()) {
                        rs.close();
                        stmt.execute(
                                "ALTER TABLE METADATA CHANGE COLUMN CREATION_TIME CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
                        java.sql.ResultSet rs2 = stmt.executeQuery(
                                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'METADATA' AND COLUMN_NAME = 'UPDATE_TIME'");
                        if (rs2.next()) {
                            rs2.close();
                            stmt.execute(
                                    "ALTER TABLE METADATA CHANGE COLUMN UPDATE_TIME UPDATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
                        } else {
                            rs2.close();
                        }
                        logger.info("Aligned METADATA table with CREATED_AT/UPDATED_AT");
                    } else {
                        rs.close();
                    }
                } catch (Exception e) {
                    logger.debug("METADATA table alignment skipped or failed: {}", e.getMessage());
                }

                // Check if EVENT table exists before creating dependent views
                boolean eventsTableExists = false;
                try {
                    java.sql.ResultSet rs = stmt.executeQuery(
                            "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'EVENT'");
                    eventsTableExists = rs.next();
                    rs.close();
                } catch (Exception e) {
                    logger.debug("Could not check EVENT table existence: {}", e.getMessage());
                }

                if (!eventsTableExists) {
                    logger.warn(
                            "EVENT table does not exist yet, skipping view creation (Hibernate DDL may not have run)");
                    return;
                }

                // Drop the table Hibernate created
                stmt.execute("DROP TABLE IF EXISTS EXPERIMENT_SUMMARY");
                // Drop the view if it exists (for reusable containers)
                stmt.execute("DROP VIEW IF EXISTS LATEST_EXPERIMENT_STATUS");
                stmt.execute("DROP VIEW IF EXISTS EXPERIMENT_SUMMARY");

                // Create LATEST_EXPERIMENT_STATUS view first (EXPERIMENT_SUMMARY depends on it)
                // Uses EVENT table with EVENT_KIND='STATUS' and PARENT_TYPE='EXPERIMENT'
                stmt.execute(
                        "CREATE VIEW LATEST_EXPERIMENT_STATUS AS "
                                + "SELECT E1.PARENT_ID AS EXPERIMENT_ID, E1.STATE AS STATE, "
                                + "E1.EVENT_TIME AS TIME_OF_STATE_CHANGE "
                                + "FROM EVENT E1 "
                                + "WHERE E1.PARENT_TYPE = 'EXPERIMENT' AND E1.EVENT_KIND = 'STATUS' "
                                + "AND E1.SEQUENCE_NUM = (SELECT MAX(E2.SEQUENCE_NUM) FROM EVENT E2 "
                                + "WHERE E2.PARENT_ID = E1.PARENT_ID AND E2.PARENT_TYPE = 'EXPERIMENT' AND E2.EVENT_KIND = 'STATUS')");

                // Create EXPERIMENT_SUMMARY view (matches V1 schema: GATEWAY_ID from PROJECT, CREATED_AT from
                // EXPERIMENT)
                stmt.execute("CREATE VIEW EXPERIMENT_SUMMARY AS "
                        + "SELECT E.EXPERIMENT_ID AS EXPERIMENT_ID, E.PROJECT_ID AS PROJECT_ID, "
                        + "P.GATEWAY_ID AS GATEWAY_ID, E.USER_NAME AS USER_NAME, "
                        + "E.EXECUTION_ID AS EXECUTION_ID, E.EXPERIMENT_NAME AS EXPERIMENT_NAME, "
                        + "E.CREATED_AT AS CREATED_AT, E.DESCRIPTION AS DESCRIPTION, "
                        + "ES.STATE AS STATE, E.RESOURCE_HOST_ID AS RESOURCE_HOST_ID, "
                        + "ES.TIME_OF_STATE_CHANGE AS TIME_OF_STATE_CHANGE "
                        + "FROM (EXPERIMENT E INNER JOIN PROJECT P ON E.PROJECT_ID = P.PROJECT_ID "
                        + "LEFT JOIN LATEST_EXPERIMENT_STATUS ES ON E.EXPERIMENT_ID = ES.EXPERIMENT_ID)");

                logger.info("Successfully created EXPERIMENT_SUMMARY view");
            } catch (Exception e) {
                logger.error("Failed to create EXPERIMENT_SUMMARY view: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to create EXPERIMENT_SUMMARY view", e);
            }
        };
    }

    /**
     * Drops all foreign key constraints on the specified table.
     * This is necessary because Hibernate auto-generates FK constraints for @OneToMany relationships,
     * but the unified EVENT table uses a discriminator pattern and should not have FK constraints.
     */
    private void dropForeignKeyConstraints(java.sql.Statement stmt, String tableName) {
        try {
            // Query to find all foreign key constraints on the table
            java.sql.ResultSet rs =
                    stmt.executeQuery("SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '"
                            + tableName + "' " + "AND CONSTRAINT_TYPE = 'FOREIGN KEY'");

            java.util.List<String> constraintNames = new java.util.ArrayList<>();
            while (rs.next()) {
                constraintNames.add(rs.getString("CONSTRAINT_NAME"));
            }
            rs.close();

            // Drop each foreign key constraint
            for (String constraintName : constraintNames) {
                logger.info("Dropping foreign key constraint {} on table {}", constraintName, tableName);
                stmt.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + constraintName);
            }
        } catch (Exception e) {
            logger.warn("Failed to drop foreign key constraints on {}: {}", tableName, e.getMessage());
            // Continue execution even if this fails - the table might not exist yet or have no FKs
        }
    }
}
