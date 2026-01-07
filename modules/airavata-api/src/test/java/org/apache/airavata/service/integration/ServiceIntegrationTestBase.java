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

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.security.UserInfo;
import org.apache.airavata.security.model.AuthzToken;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
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
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
            // IAM configuration
            "security.iam.server-url=",
            // Disable Flyway in tests - TestcontainersConfig handles migrations
            "flyway.enabled=false",
            // Enable services for conditional beans
            "services.rest.enabled=false",
            "services.thrift.enabled=true"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public abstract class ServiceIntegrationTestBase {

    protected static final String TEST_GATEWAY_ID = "test-gateway";
    protected static final String TEST_USERNAME = "test-user";
    protected static final String TEST_ACCESS_TOKEN = "test-access-token";

    protected AuthzToken testAuthzToken;

    @org.springframework.beans.factory.annotation.Autowired
    protected org.apache.airavata.config.AiravataServerProperties properties;

    @org.junit.jupiter.api.BeforeAll
    public static void setupTestContainers() {
        // Initialize Testcontainers services early to ensure URLs are available
        org.apache.airavata.config.TestcontainersConfig.getKafkaBootstrapServers();
        org.apache.airavata.config.TestcontainersConfig.getRabbitMQUrl();
        org.apache.airavata.config.TestcontainersConfig.getZookeeperConnectionString();
    }

    @BeforeEach
    public void setUpBase() {
        testAuthzToken = createTestAuthzToken(TEST_GATEWAY_ID, TEST_USERNAME);

        // Apply test properties for messaging services (Kafka, RabbitMQ, Zookeeper)
        // This must happen before any messaging factory is used
        if (properties != null) {
            org.apache.airavata.config.TestPropertiesHelper.applyTestProperties(properties);
        }
    }

    /**
     * Creates a test AuthzToken with the specified gateway ID and username.
     */
    protected AuthzToken createTestAuthzToken(String gatewayId, String username) {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(TEST_ACCESS_TOKEN);
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.GATEWAY_ID, gatewayId);
        claimsMap.put(Constants.USER_NAME, username);
        authzToken.setClaimsMap(claimsMap);
        return authzToken;
    }

    /**
     * Helper method to end the current transaction and start a new one.
     * Useful for testing scenarios where you need to commit data.
     * This ensures all entity managers are flushed before committing so data is visible in subsequent transactions.
     */
    protected void commitTransaction() {
        if (TestTransaction.isActive()) {
            try {
                // Flag for commit - this will commit when end() is called
                // Spring's transaction manager will automatically flush entity managers on commit
                TestTransaction.flagForCommit();
                // End the transaction - this will commit if flagged, or rollback if not
                TestTransaction.end();
            } catch (org.springframework.transaction.UnexpectedRollbackException e) {
                // If transaction was already marked for rollback, we need to start fresh
                // This can happen if an exception occurred during the transaction
                // Just end the current transaction and start a new one
                TestTransaction.end();
            }
        }
        // Start a new transaction for subsequent operations
        TestTransaction.start();
    }

    /**
     * Test configuration that enables component scanning for services.
     * Infrastructure components are automatically excluded via @Profile("!test").
     */
    @Configuration
    @org.springframework.data.jpa.repository.config.EnableJpaRepositories(
            basePackages = {
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.credential.repositories"
            },
            enableDefaultTransactions = false)
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
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security"
            })
    public static class TestConfiguration {
        @Bean
        public org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback defaultKeyStorePasswordCallback(
                org.apache.airavata.config.AiravataServerProperties properties) {
            return new org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback(properties);
        }

        // ThriftToDomainMapperRegistry removed - all RabbitMQ messages use domain models (never Thrift)

        @Bean
        @Primary
        public org.apache.airavata.security.AiravataSecurityManager airavataSecurityManager() {
            return new org.apache.airavata.security.AiravataSecurityManager() {
                @Override
                public boolean isUserAuthorized(AuthzToken authzToken, Map<String, String> metaData)
                        throws AiravataSecurityException {
                    return true;
                }

                @Override
                public AuthzToken getUserManagementServiceAccountAuthzToken(String gatewayId)
                        throws AiravataSecurityException {
                    var token = new AuthzToken("test-service-token");
                    var claims = new HashMap<String, String>();
                    claims.put("gatewayId", gatewayId);
                    token.setClaimsMap(claims);
                    return token;
                }

                @Override
                public UserInfo getUserInfoFromAuthzToken(AuthzToken authzToken) throws AiravataSecurityException {
                    // Extract from token if available, otherwise use defaults
                    String userId = "test-user";
                    String gatewayId = "test-gateway";
                    if (authzToken != null && authzToken.getClaimsMap() != null) {
                        userId = authzToken.getClaimsMap().getOrDefault("userName", userId);
                        gatewayId = authzToken.getClaimsMap().getOrDefault("gatewayId", gatewayId);
                    }
                    org.apache.airavata.security.UserInfo userInfo = new org.apache.airavata.security.UserInfo();
                    userInfo.setUsername(userId);
                    userInfo.setSub(gatewayId + "@" + userId);
                    userInfo.setEmailAddress(userId + "@example.com");
                    userInfo.setFirstName("Test");
                    userInfo.setLastName("User");
                    return userInfo;
                }
            };
        }

        @Bean(name = "testIamAdminService")
        @Primary
        @org.springframework.boot.autoconfigure.condition.ConditionalOnBean({
            org.apache.airavata.profile.repositories.UserProfileRepository.class,
            org.apache.airavata.service.registry.RegistryService.class
        })
        public org.apache.airavata.service.security.IamAdminService testIamAdminService(
                org.apache.airavata.config.AiravataServerProperties properties,
                org.apache.airavata.profile.repositories.UserProfileRepository userProfileRepository,
                org.apache.airavata.profile.mappers.UserProfileMapper userProfileMapper,
                org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
                org.apache.airavata.service.registry.RegistryService registryService,
                org.apache.airavata.messaging.Dispatcher dbEventDispatcher) {
            // Create a mock implementation that extends IamAdminService behavior
            return new org.apache.airavata.service.security.IamAdminService(
                    properties,
                    userProfileRepository,
                    userProfileMapper,
                    credentialStoreService,
                    registryService,
                    dbEventDispatcher) {
                @Override
                public boolean isUsernameAvailable(AuthzToken authzToken, String username)
                        throws org.apache.airavata.profile.exception.IamAdminServicesException {
                    // Return true for test purposes
                    return true;
                }

                @Override
                public boolean registerUser(
                        AuthzToken authzToken,
                        String username,
                        String emailAddress,
                        String firstName,
                        String lastName,
                        String newPassword)
                        throws org.apache.airavata.profile.exception.IamAdminServicesException {
                    // Return true for test purposes
                    return true;
                }

                @Override
                public java.util.List<org.apache.airavata.common.model.UserProfile> findUsers(
                        AuthzToken authzToken, String email, String userId)
                        throws org.apache.airavata.profile.exception.IamAdminServicesException {
                    // Return empty list for test purposes
                    return java.util.Collections.emptyList();
                }

                @Override
                public org.apache.airavata.common.model.Gateway setUpGateway(
                        AuthzToken authzToken, org.apache.airavata.common.model.Gateway gateway)
                        throws org.apache.airavata.profile.exception.IamAdminServicesException,
                                org.apache.airavata.credential.exception.CredentialStoreException {
                    // Return gateway as-is for test purposes
                    return gateway;
                }
            };
        }
    }
}
