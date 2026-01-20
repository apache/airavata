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
package org.apache.airavata.service.integration;

import java.util.concurrent.TimeUnit;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.security.model.AuthzToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
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
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            ServiceIntegrationTestBase.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.security.manager.enabled=false",
            "airavata.security.authzCache.enabled=true",
            "airavata.flyway.enabled=false",
            "airavata.services.rest.enabled=false",
            "airavata.services.thrift.enabled=true",
            "airavata.dapr.enabled=false"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public abstract class ServiceIntegrationTestBase {

    protected static final String TEST_GATEWAY_ID = "default";
    protected static final String TEST_USERNAME = "default-admin";

    private static String keycloakUrl;

    protected AuthzToken testAuthzToken;

    @org.springframework.beans.factory.annotation.Autowired
    protected org.apache.airavata.config.AiravataServerProperties properties;

    @org.springframework.beans.factory.annotation.Autowired
    protected org.apache.airavata.registry.services.GatewayService gatewayService;

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
    }

    @BeforeEach
    public void setUpBase() throws RegistryException {
        // Ensure test gateway exists in EXPCATALOG_GATEWAY table
        // This is required because USERS table has FK to EXPCATALOG_GATEWAY
        if (gatewayService != null && !gatewayService.isGatewayExist(TEST_GATEWAY_ID)) {
            org.apache.airavata.common.model.Gateway gateway = new org.apache.airavata.common.model.Gateway();
            gateway.setGatewayId(TEST_GATEWAY_ID);
            gateway.setGatewayName("Default Test Gateway");
            gateway.setDomain(TEST_GATEWAY_ID);
            gateway.setEmailAddress("test@" + TEST_GATEWAY_ID + ".org");
            gatewayService.addGateway(gateway);
        }

        testAuthzToken = createRealAuthzToken(TEST_GATEWAY_ID, TEST_USERNAME);
        if (properties != null) {
            org.apache.airavata.config.TestPropertiesHelper.logProperties(properties);
        }
    }

    protected AuthzToken createRealAuthzToken(String gatewayId, String username) {
        return org.apache.airavata.config.KeycloakTokenHelper.createRealAuthzToken(
                keycloakUrl, properties, gatewayId, username);
    }

    @Configuration
    @org.springframework.context.annotation.PropertySource("classpath:application.properties")
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.profile.mappers",
                "org.apache.airavata.profile.utils",
                "org.apache.airavata.sharing.services",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.sharing.mappers",
                "org.apache.airavata.sharing.utils",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
                "org.apache.airavata.credential.utils",
                "org.apache.airavata.messaging",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security"
            })
    public static class TestConfiguration {

        /**
         * Manually bind AiravataServerProperties from Environment using Spring Boot's Binder.
         * This ensures all nested records are properly constructed from properties.
         */
        @Bean
        @org.springframework.context.annotation.Primary
        public org.apache.airavata.config.AiravataServerProperties airavataServerProperties(
                org.springframework.core.env.Environment environment) {
            return org.springframework.boot.context.properties.bind.Binder.get(environment)
                    .bind("airavata", org.apache.airavata.config.AiravataServerProperties.class)
                    .orElseThrow(() ->
                            new IllegalStateException("Failed to bind AiravataServerProperties from environment"));
        }

        @Bean
        public org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback defaultKeyStorePasswordCallback(
                org.apache.airavata.config.AiravataServerProperties properties) {
            return new org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback(properties);
        }
    }
}
