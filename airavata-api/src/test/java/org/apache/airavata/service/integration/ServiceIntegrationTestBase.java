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
import org.apache.airavata.model.security.AuthzToken;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
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
        classes = {org.apache.airavata.config.JpaConfig.class, ServiceIntegrationTestBase.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.userprofile.enabled=true",
            "services.groupmanager.enabled=true",
            "services.iam.enabled=true",
            "services.orchestrator.enabled=false",
            "services.registryService.enabled=true",
            "services.credentialstore.enabled=true",
            "services.sharingregistry.enabled=true",
            "security.manager.enabled=false"
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
     */
    protected void commitTransaction() {
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
        TestTransaction.start();
    }

    /**
     * Test configuration that enables component scanning for services
     * without loading background services or Thrift servers.
     */
    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.service",
                "org.apache.airavata.profile",
                "org.apache.airavata.sharing",
                "org.apache.airavata.credential",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|registry\\.messaging)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = ".*\\$.*"  // Exclude inner classes (Thrift-generated)
                ),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = ".*\\.cpi\\..*"  // Exclude Thrift CPI classes
                )
            })
    @Import({org.apache.airavata.config.AiravataPropertiesConfiguration.class, org.apache.airavata.config.DozerMapperConfig.class})
    static class TestConfiguration {
        @org.springframework.context.annotation.Bean
        public org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback defaultKeyStorePasswordCallback(
                org.apache.airavata.config.AiravataServerProperties properties) {
            return new org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback(properties);
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public org.apache.airavata.security.AiravataSecurityManager airavataSecurityManager() {
            return new org.apache.airavata.security.AiravataSecurityManager() {
                @Override
                public boolean isUserAuthorized(org.apache.airavata.model.security.AuthzToken authzToken, java.util.Map<String, String> metaData) throws org.apache.airavata.security.AiravataSecurityException {
                    return true;
                }

                @Override
                public org.apache.airavata.model.security.AuthzToken getUserManagementServiceAccountAuthzToken(String gatewayId) throws org.apache.airavata.security.AiravataSecurityException {
                    org.apache.airavata.model.security.AuthzToken token = new org.apache.airavata.model.security.AuthzToken("test-service-token");
                    java.util.Map<String, String> claims = new java.util.HashMap<>();
                    claims.put("gatewayId", gatewayId);
                    token.setClaimsMap(claims);
                    return token;
                }

                @Override
                public org.apache.airavata.security.UserInfo getUserInfoFromAuthzToken(org.apache.airavata.model.security.AuthzToken authzToken) throws org.apache.airavata.security.AiravataSecurityException {
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
                    return userInfo;
                }
            };
        }
    }
}
