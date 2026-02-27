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

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for Keycloak.
 * Auto-detects devcontainer Keycloak on port 18080, falling back to Testcontainers if not available.
 */
@org.springframework.context.annotation.Configuration
@Profile("test")
public class KeycloakTestConfig {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakTestConfig.class);

    // Devcontainer Keycloak settings (from compose.yml)
    private static final String KEYCLOAK_HOST = System.getProperty("test.keycloak.host", "localhost");
    private static final int KEYCLOAK_PORT = Integer.parseInt(System.getProperty("test.keycloak.port", "18080"));
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    // Testcontainers fallback
    private static final String KEYCLOAK_VERSION = "26.5";
    private static KeycloakContainer keycloakContainer;

    // Cache for devcontainer detection
    private static volatile Boolean useDevcontainer = null;
    private static String cachedServerUrl = null;

    /**
     * Check if devcontainer Keycloak is accessible on port 18080.
     */
    public static boolean isKeycloakAccessible() {
        if (useDevcontainer != null) {
            return useDevcontainer;
        }

        // Try Keycloak realms endpoint (works for all Keycloak versions)
        try {
            String realmsUrl = String.format("http://%s:%d/realms/master", KEYCLOAK_HOST, KEYCLOAK_PORT);
            URL url = URI.create(realmsUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode == 200) {
                useDevcontainer = true;
                cachedServerUrl = String.format("http://%s:%d", KEYCLOAK_HOST, KEYCLOAK_PORT);
                logger.info("Devcontainer Keycloak detected at {}", cachedServerUrl);
                return true;
            }
        } catch (Exception e) {
            logger.debug("Keycloak not accessible at {}:{}: {}", KEYCLOAK_HOST, KEYCLOAK_PORT, e.getMessage());
        }

        useDevcontainer = false;
        return false;
    }

    /**
     * Check if Keycloak is available (either devcontainer or Docker for Testcontainers).
     */
    public static boolean isKeycloakAvailable() {
        // First check devcontainer
        if (isKeycloakAccessible()) {
            return true;
        }

        // Then check if Docker is available for Testcontainers
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            logger.info("Docker available for Testcontainers Keycloak");
            return true;
        } catch (Exception e) {
            logger.info("Neither devcontainer Keycloak nor Docker available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the Keycloak server URL (devcontainer or Testcontainers).
     */
    public static synchronized String getKeycloakServerUrl() {
        // Use devcontainer if available
        if (isKeycloakAccessible()) {
            if (cachedServerUrl != null) {
                return cachedServerUrl;
            }
            // If accessible but URL not cached, construct it
            return String.format("http://%s:%d", KEYCLOAK_HOST, KEYCLOAK_PORT);
        }

        // Fall back to Testcontainers
        KeycloakContainer container = getKeycloakContainer();
        if (container != null) {
            return container.getAuthServerUrl();
        }
        throw new IllegalStateException("Keycloak is not available (neither devcontainer nor Testcontainers)");
    }

    /**
     * Get or create a Testcontainers Keycloak instance (only if devcontainer not available).
     */
    private static synchronized KeycloakContainer getKeycloakContainer() {
        // Prefer devcontainer Keycloak if available
        if (isKeycloakAccessible()) {
            logger.info("Using devcontainer Keycloak, skipping Testcontainers");
            return null;
        }

        if (keycloakContainer == null || !keycloakContainer.isRunning()) {
            logger.info("Starting Testcontainers Keycloak...");
            keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)
                    .withAdminUsername(ADMIN_USERNAME)
                    .withAdminPassword(ADMIN_PASSWORD)
                    .withReuse(true);
            keycloakContainer.start();
            logger.info("Testcontainers Keycloak started at: {}", keycloakContainer.getAuthServerUrl());

            // Enable direct access grants on admin-cli client in master realm
            // This is required for password-based authentication in Keycloak 25+
            enableAdminCliDirectAccessGrants(keycloakContainer);
        }
        return keycloakContainer;
    }

    /**
     * Enable direct access grants on the admin-cli client in the master realm.
     * This is required for password-based authentication in Keycloak 25+/26+ where
     * the admin-cli client no longer has directAccessGrantsEnabled by default.
     */
    private static void enableAdminCliDirectAccessGrants(KeycloakContainer container) {
        if (container == null || !container.isRunning()) {
            logger.warn("Cannot enable admin-cli direct access grants: Keycloak container not running");
            return;
        }

        try {
            // First, authenticate with kcadm
            var authResult = container.execInContainer(
                    "/opt/keycloak/bin/kcadm.sh",
                    "config",
                    "credentials",
                    "--server",
                    "http://localhost:8080",
                    "--realm",
                    "master",
                    "--user",
                    ADMIN_USERNAME,
                    "--password",
                    ADMIN_PASSWORD);

            if (authResult.getExitCode() != 0) {
                logger.warn("Failed to authenticate with kcadm: {} {}", authResult.getStdout(), authResult.getStderr());
                return;
            }

            // Get the client ID (internal UUID) of admin-cli
            var getClientResult = container.execInContainer(
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
                return;
            }

            // Parse the client ID from the JSON response
            String clientOutput = getClientResult.getStdout();
            // Response format: [ { "id" : "uuid-here" } ]
            String clientUuid = null;
            if (clientOutput.contains("\"id\"")) {
                int startIdx = clientOutput.indexOf("\"id\"");
                int colonIdx = clientOutput.indexOf(":", startIdx);
                int quoteStart = clientOutput.indexOf("\"", colonIdx);
                int quoteEnd = clientOutput.indexOf("\"", quoteStart + 1);
                if (quoteStart > 0 && quoteEnd > quoteStart) {
                    clientUuid = clientOutput.substring(quoteStart + 1, quoteEnd);
                }
            }

            if (clientUuid == null || clientUuid.isEmpty()) {
                logger.warn("Could not parse admin-cli client UUID from: {}", clientOutput);
                return;
            }

            // Enable direct access grants on the admin-cli client
            var updateResult = container.execInContainer(
                    "/opt/keycloak/bin/kcadm.sh",
                    "update",
                    "clients/" + clientUuid,
                    "-r",
                    "master",
                    "-s",
                    "directAccessGrantsEnabled=true");

            if (updateResult.getExitCode() == 0) {
                logger.info("Successfully enabled direct access grants on admin-cli client");
            } else {
                logger.warn(
                        "Failed to enable direct access grants: {} {}",
                        updateResult.getStdout(),
                        updateResult.getStderr());
            }
        } catch (Exception e) {
            logger.warn("Failed to enable admin-cli direct access grants: {}", e.getMessage());
        }
    }

    /**
     * Provides Keycloak auth server URL for tests.
     */
    @Bean(name = "keycloakServerUrl")
    public String keycloakServerUrl() {
        return getKeycloakServerUrl();
    }

    /**
     * Provides Keycloak admin username.
     */
    @Bean(name = "keycloakAdminUsername")
    public String keycloakAdminUsername() {
        return ADMIN_USERNAME;
    }

    /**
     * Provides Keycloak admin password.
     */
    @Bean(name = "keycloakAdminPassword")
    public String keycloakAdminPassword() {
        return ADMIN_PASSWORD;
    }

    /**
     * Check if admin-cli direct access grants are enabled by attempting authentication.
     * Returns true if password grant works, false otherwise.
     */
    private static boolean isAdminCliDirectAccessGrantsEnabled(String serverUrl) {
        try {
            String tokenUrl = serverUrl + "/realms/master/protocol/openid-connect/token";
            java.net.URL url = java.net.URI.create(tokenUrl).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            String formData = "grant_type=password&client_id=admin-cli&username=" + ADMIN_USERNAME + "&password="
                    + ADMIN_PASSWORD;
            conn.getOutputStream().write(formData.getBytes());

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                logger.debug("admin-cli direct access grants are enabled");
                return true;
            } else {
                logger.warn("admin-cli direct access grants are NOT enabled. Response code: {}", responseCode);
                logger.warn("To enable: Restart Keycloak container to import realm-master.json, or run:");
                logger.warn(
                        "  docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:18080 --realm master --user admin --password admin");
                logger.warn(
                        "  docker exec keycloak /opt/keycloak/bin/kcadm.sh update clients/master -s '{\"directAccessGrantsEnabled\":true}' -r master");
                return false;
            }
        } catch (Exception e) {
            logger.debug("Could not check admin-cli direct access grants: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Provides ServerProperties configured with Keycloak.
     * Only created if no other ServerProperties bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(ServerProperties.class)
    public ServerProperties keycloakServerProperties() {
        String serverUrl = getKeycloakServerUrl();

        // Check if admin-cli direct access grants are enabled (for devcontainer)
        if (isKeycloakAccessible()) {
            isAdminCliDirectAccessGrantsEnabled(serverUrl);
        }

        var iam = new ServerProperties.Security.Iam(
                true, // enabled
                serverUrl,
                "default", // realm
                "airavata-client", // default client ID
                "secret", // will be configured per test
                new ServerProperties.Security.Iam.Super(ADMIN_USERNAME, ADMIN_PASSWORD));

        var security = new ServerProperties.Security(
                null, // tls
                null, // authentication
                iam,
                null // vault
                );

        var services = new ServerProperties.Services(
                new ServerProperties.Services.Rest(false),
                null, // participant
                null, // controller
                null, // scheduler
                null, // monitor
                null, // sharing
                null, // registry
                null, // research
                null, // agent
                null, // fileserver
                null, // telemetry
                null); // dbus

        return new ServerProperties(
                "", // home
                "default", // defaultGateway
                true, // validationEnabled
                null, // sharing
                1000, // inMemoryCacheSize
                "/tmp/airavata", // localDataLocation
                1073741824, // maxArchiveSize
                null, // streamingTransfer
                null, // hibernate
                null, // cors
                security, // security
                null, // flyway
                services // services
                );
    }
}
