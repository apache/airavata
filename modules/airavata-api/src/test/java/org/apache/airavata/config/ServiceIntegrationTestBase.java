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

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all service integration tests.
 * Provides Spring Boot test setup with Testcontainers MariaDB databases.
 * All tests use @Transactional for automatic rollback.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfiguration.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ServiceIntegrationTestBase.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.security.manager.enabled=false",
            "airavata.flyway.enabled=false",
            "airavata.services.http.server.port=8080"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public abstract class ServiceIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ServiceIntegrationTestBase.class);
    private static final int KEYCLOAK_READINESS_ATTEMPTS = 10;
    private static final long KEYCLOAK_READINESS_DELAY_MS = 1000;
    private static final int KEYCLOAK_TOKEN_RETRIES = 3;
    private static final long KEYCLOAK_TOKEN_RETRY_DELAY_MS = 2000;

    protected static final String TEST_GATEWAY_ID = "default";
    protected static final String TEST_USERNAME = "default-admin";

    private static String keycloakUrl;

    protected AuthzToken testAuthzToken;

    @org.springframework.beans.factory.annotation.Autowired
    protected org.apache.airavata.config.ServerProperties properties;

    @org.springframework.beans.factory.annotation.Autowired
    protected org.apache.airavata.gateway.service.GatewayService gatewayService;

    @org.springframework.beans.factory.annotation.Autowired
    protected jakarta.persistence.EntityManager entityManager;

    /**
     * Flush pending changes to the database and clear the JPA first-level cache.
     * Use this before fetching entities that were modified via child entity saves
     * to ensure fresh data is loaded from the database.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @org.springframework.test.context.DynamicPropertySource
    static void configureProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        keycloakUrl = org.apache.airavata.config.TestcontainersConfig.getKeycloakUrl();

        registry.add("airavata.security.iam.server-url", () -> keycloakUrl);
        System.setProperty("airavata.security.iam.server-url", keycloakUrl);
        // Keycloak testcontainer master realm admin (admin/admin) - required for IamAdminService admin token
        registry.add("airavata.security.iam.super-admin.username", () -> "admin");
        registry.add("airavata.security.iam.super-admin.password", () -> "admin");
    }

    @BeforeEach
    public void setUpBase() throws RegistryException {
        // Ensure test gateway exists in EXPCATALOG_GATEWAY table
        // This is required because USERS table has FK to EXPCATALOG_GATEWAY
        if (gatewayService != null && !gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            org.apache.airavata.gateway.model.Gateway gateway = new org.apache.airavata.gateway.model.Gateway();
            gateway.setGatewayId(TEST_GATEWAY_ID);
            gateway.setGatewayName("Default Test Gateway");
            gateway.setDomain(TEST_GATEWAY_ID);
            gateway.setEmailAddress("test@" + TEST_GATEWAY_ID + ".org");
            gatewayService.createGateway(gateway);
        }

        // Ensure test user exists in USER table (required by search/authorization checks)
        ensureTestUserExists();

        waitForKeycloakReady();
        testAuthzToken = obtainKeycloakTokenWithRetry();
        if (properties != null) {
            org.apache.airavata.config.TestPropertiesHelper.logProperties(properties);
        }
    }

    private void ensureTestUserExists() {
        String userId = TEST_USERNAME + "@" + TEST_GATEWAY_ID;
        var existing = entityManager.find(org.apache.airavata.iam.entity.UserEntity.class, userId);
        if (existing == null) {
            var user = new org.apache.airavata.iam.entity.UserEntity(TEST_USERNAME, TEST_GATEWAY_ID);
            entityManager.persist(user);
            entityManager.flush();
        }
    }

    /**
     * Wait for Keycloak to be ready (container up and default realm imported) before
     * attempting token acquisition. Reduces IAM test skips when Keycloak is slow to start.
     */
    private void waitForKeycloakReady() {
        String realmUrl = keycloakUrl + "/realms/default";
        for (int attempt = 1; attempt <= KEYCLOAK_READINESS_ATTEMPTS; attempt++) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) URI.create(realmUrl).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int code = conn.getResponseCode();
                conn.disconnect();
                if (code == 200) {
                    logger.debug("Keycloak realm ready after {} attempt(s)", attempt);
                    return;
                }
            } catch (Exception e) {
                logger.debug(
                        "Keycloak readiness attempt {}/{} failed: {}",
                        attempt,
                        KEYCLOAK_READINESS_ATTEMPTS,
                        e.getMessage());
            }
            if (attempt < KEYCLOAK_READINESS_ATTEMPTS) {
                try {
                    Thread.sleep(KEYCLOAK_READINESS_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Obtain Keycloak token with retries to handle Keycloak container/realm startup delay.
     * Keycloak may not be ready immediately after container start; retrying gives realm
     * import and default-admin user time to become available.
     */
    private AuthzToken obtainKeycloakTokenWithRetry() {
        for (int attempt = 1; attempt <= KEYCLOAK_TOKEN_RETRIES; attempt++) {
            try {
                return createRealAuthzToken(TEST_GATEWAY_ID, TEST_USERNAME);
            } catch (Exception e) {
                logger.debug(
                        "Keycloak token attempt {}/{} failed: {}", attempt, KEYCLOAK_TOKEN_RETRIES, e.getMessage());
                if (attempt < KEYCLOAK_TOKEN_RETRIES) {
                    try {
                        Thread.sleep(KEYCLOAK_TOKEN_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return null;
    }

    protected AuthzToken createRealAuthzToken(String gatewayId, String username) {
        return org.apache.airavata.config.KeycloakTokenHelper.createRealAuthzToken(
                keycloakUrl, properties, gatewayId, username);
    }

    @Configuration
    @org.springframework.context.annotation.PropertySource("classpath:application.properties")
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry",
                "org.apache.airavata.iam",
                "org.apache.airavata.util",
                "org.apache.airavata.exception",
                "org.apache.airavata.status.model",
                "org.apache.airavata.status.entity",
                "org.apache.airavata.experiment",
                "org.apache.airavata.compute",
                "org.apache.airavata.accounting",
                "org.apache.airavata.workflow",
                "org.apache.airavata.research",
                "org.apache.airavata.sharing",
                "org.apache.airavata.gateway",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.credential",
                "org.apache.airavata.execution",
                "org.apache.airavata.storage",
                "org.apache.airavata.job",
                "org.apache.airavata.process",
                "org.apache.airavata.user"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = org.apache.airavata.config.IntegrationTestConfiguration.class)
            })
    public static class TestConfiguration {

        /**
         * Manually bind ServerProperties from Environment using Spring Boot's Binder.
         * This ensures all nested records are properly constructed from properties.
         */
        @Bean
        @org.springframework.context.annotation.Primary
        public org.apache.airavata.config.ServerProperties airavataServerProperties(
                org.springframework.core.env.Environment environment) {
            return org.springframework.boot.context.properties.bind.Binder.get(environment)
                    .bind("airavata", org.apache.airavata.config.ServerProperties.class)
                    .orElseThrow(() ->
                            new IllegalStateException("Failed to bind ServerProperties from environment"));
        }

        @Bean
        public org.apache.airavata.credential.util.KeyStorePasswordCallback keyStorePasswordCallback(
                org.apache.airavata.config.ServerProperties properties) {
            return new org.apache.airavata.credential.util.KeyStorePasswordCallback(properties);
        }
    }
}
