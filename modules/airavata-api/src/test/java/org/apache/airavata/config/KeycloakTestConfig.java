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

    // Devcontainer Keycloak settings (from docker-compose.yml)
    private static final String KEYCLOAK_HOST = System.getProperty("test.keycloak.host", "localhost");
    private static final int KEYCLOAK_PORT = Integer.parseInt(System.getProperty("test.keycloak.port", "18080"));
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    // Testcontainers fallback
    private static final String KEYCLOAK_VERSION = "26.0";
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
            URL url = new URL(realmsUrl);
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
        }
        return keycloakContainer;
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
            java.net.URL url = new java.net.URL(tokenUrl);
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
     * Provides AiravataServerProperties configured with Keycloak.
     * Only created if no other AiravataServerProperties bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(AiravataServerProperties.class)
    public AiravataServerProperties keycloakAiravataServerProperties() {
        String serverUrl = getKeycloakServerUrl();

        // Check if admin-cli direct access grants are enabled (for devcontainer)
        if (isKeycloakAccessible()) {
            isAdminCliDirectAccessGrantsEnabled(serverUrl);
        }

        var iam = new AiravataServerProperties.Security.Iam(
                true, // enabled
                serverUrl,
                "airavata-client", // default client ID
                "secret", // will be configured per test
                new AiravataServerProperties.Security.Iam.Super(ADMIN_USERNAME, ADMIN_PASSWORD));

        var security = new AiravataServerProperties.Security(
                null, // tls
                null, // authzCache
                null, // authentication
                iam,
                null // vault
                );

        var services = new AiravataServerProperties.Services(
                new AiravataServerProperties.Services.Thrift(false, null),
                new AiravataServerProperties.Services.Rest(false, null),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        return new AiravataServerProperties(
                "", // home
                "default", // defaultGateway
                true, // validationEnabled
                null, // sharing
                1000, // inMemoryCacheSize
                "/tmp/airavata", // localDataLocation
                1073741824, // maxArchiveSize
                null, // streamingTransfer
                null, // hibernate
                security, // security
                null, // flyway
                services // services
                );
    }
}
