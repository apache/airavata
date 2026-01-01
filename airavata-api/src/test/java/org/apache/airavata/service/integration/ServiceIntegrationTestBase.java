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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all service integration tests.
 * Provides Spring Boot test setup with H2 in-memory databases.
 * All tests use @Transactional for automatic rollback.
 */
@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            ServiceIntegrationTestBase.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
            // IAM configuration
            "security.iam.server-url="
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public abstract class ServiceIntegrationTestBase {

    protected static final String TEST_GATEWAY_ID = "test-gateway";
    protected static final String TEST_USERNAME = "test-user";
    protected static final String TEST_ACCESS_TOKEN = "test-access-token";

    protected AuthzToken testAuthzToken;

    @BeforeEach
    public void setUpBase() {
        testAuthzToken = createTestAuthzToken(TEST_GATEWAY_ID, TEST_USERNAME);
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
            // Flag for commit - this will commit when end() is called
            TestTransaction.flagForCommit();
            // End the transaction - this will commit if flagged, or rollback if not
            TestTransaction.end();
        }
        // Start a new transaction for subsequent operations
        TestTransaction.start();
    }

    /**
     * Test configuration that enables component scanning for services
     * without loading background services or Thrift servers.
     */
    @Configuration
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
            },
            useDefaultFilters = false,
            includeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                // Exclude infrastructure components - use DI instead of property flags
                // Helix components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.helix.adaptor.SSHJAgentAdaptor.class,
                            org.apache.airavata.helix.adaptor.SSHJStorageAdaptor.class,
                            org.apache.airavata.helix.agent.ssh.SshAgentAdaptor.class,
                            org.apache.airavata.helix.agent.storage.StorageResourceAdaptorImpl.class,
                            org.apache.airavata.helix.core.support.TaskHelperImpl.class,
                            org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.task.AWSTaskFactory.class,
                            org.apache.airavata.helix.impl.task.AiravataTask.class,
                            org.apache.airavata.helix.impl.task.SlurmTaskFactory.class,
                            org.apache.airavata.helix.impl.task.TaskFactory.class,
                            org.apache.airavata.helix.impl.task.aws.utils.AWSTaskUtil.class,
                            org.apache.airavata.helix.impl.task.submission.config.GroovyMapBuilder.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class
                        }),
                // Monitor components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class
                        }),
                // DB Event Manager components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.manager.dbevent.DBEventManagerRunner.class,
                            org.apache.airavata.manager.dbevent.messaging.DBEventManagerMessagingFactory.class,
                            org.apache.airavata.manager.dbevent.messaging.impl.DBEventMessageHandler.class
                        }),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {org.apache.airavata.config.BackgroundServicesLauncher.class}),
                // Orchestrator components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.orchestrator.impl.SimpleOrchestratorImpl.class,
                            org.apache.airavata.orchestrator.utils.OrchestratorUtils.class,
                            org.apache.airavata.orchestrator.validation.impl.ValidationServiceImpl.class,
                            org.apache.airavata.orchestrator.validator.BatchQueueValidator.class,
                            org.apache.airavata.orchestrator.validator.GroupResourceProfileValidator.class
                        }),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {org.apache.airavata.config.SecurityManagerConfig.class})
            })
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {
        @Bean
        public org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback defaultKeyStorePasswordCallback(
                org.apache.airavata.config.AiravataServerProperties properties) {
            return new org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback(properties);
        }

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
            org.apache.airavata.service.profile.UserProfileService.class,
            org.apache.airavata.service.registry.RegistryService.class
        })
        public org.apache.airavata.service.security.IamAdminService testIamAdminService(
                org.apache.airavata.config.AiravataServerProperties properties,
                org.apache.airavata.service.profile.UserProfileService userProfileService,
                org.apache.airavata.service.security.CredentialStoreService credentialStoreService,
                org.apache.airavata.service.registry.RegistryService registryService,
                org.apache.airavata.messaging.core.MessagingFactory messagingFactory) {
            // Create a mock implementation that extends IamAdminService behavior
            return new org.apache.airavata.service.security.IamAdminService(
                    properties, userProfileService, credentialStoreService, registryService, messagingFactory) {
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
