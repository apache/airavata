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
package org.apache.airavata.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.model.GatewayGroups;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.security.authzcache.AuthzCacheIndex;
import org.apache.airavata.security.authzcache.AuthzCacheManager;
import org.apache.airavata.security.authzcache.AuthzCacheManagerFactory;
import org.apache.airavata.security.authzcache.AuthzCachedStatus;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.sharing.model.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            KeyCloakSecurityManagerTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "security.tls.enabled=true",
            "security.iam.server-url=", // Empty to skip IAM HTTP calls in tests
            "security.manager.enabled=true",
            "security.authzCache.enabled=true", // Enable cache - tests will mock cache behavior
            // Infrastructure components excluded via component scanning - no property flags needed
            "test.keycloak.security.manager=true"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class KeyCloakSecurityManagerTest {

    public static final String TEST_USERNAME = "test-user";
    public static final String TEST_GATEWAY = "test-gateway";
    public static final String TEST_ACCESS_TOKEN = "abc123";

    @MockitoBean
    private RegistryService mockRegistryService;

    @MockitoBean
    private SharingRegistryService mockSharingRegistryService;

    @MockitoBean
    private AuthzCacheManagerFactory mockAuthzCacheManagerFactory;

    @MockitoBean
    private AuthzCacheManager mockAuthzCacheManager;

    @MockitoBean
    private CredentialStoreService mockCredentialStoreService;

    @MockitoBean
    private org.apache.airavata.security.GatewayGroupsInitializer mockGatewayGroupsInitializer;

    @Autowired
    private KeyCloakSecurityManager keyCloakSecurityManager;

    @Configuration
    @org.springframework.boot.test.context.TestConfiguration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.security",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {
        @Bean
        @Primary
        public AiravataServerProperties airavataServerProperties(org.springframework.core.env.Environment environment) {
            AiravataServerProperties properties = new AiravataServerProperties();
            properties.setEnvironment(environment);
            properties.security.tls.enabled = true;
            // Leave IAM server URL empty to skip HTTP calls in tests
            properties.security.iam.serverUrl = "";
            properties.security.authzCache.enabled = true; // Enable cache - tests will mock it
            return properties;
        }

        @Bean
        @Primary
        @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
                name = "test.keycloak.security.manager",
                havingValue = "true")
        public org.apache.airavata.security.GatewayGroupsInitializer gatewayGroupsInitializer() {
            return org.mockito.Mockito.mock(org.apache.airavata.security.GatewayGroupsInitializer.class);
        }

        @Bean
        @Primary
        @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
                name = "test.keycloak.security.manager",
                havingValue = "true")
        public org.apache.airavata.security.AiravataSecurityManager airavataSecurityManager(
                org.apache.airavata.service.registry.RegistryService registryService,
                org.apache.airavata.service.SharingRegistryService sharingRegistryService,
                org.apache.airavata.config.AiravataServerProperties properties,
                org.apache.airavata.security.authzcache.AuthzCacheManagerFactory authzCacheManagerFactory,
                org.apache.airavata.security.GatewayGroupsInitializer gatewayGroupsInitializer)
                throws org.apache.airavata.security.AiravataSecurityException {
            return new org.apache.airavata.security.KeyCloakSecurityManager(
                    registryService,
                    sharingRegistryService,
                    properties,
                    authzCacheManagerFactory,
                    gatewayGroupsInitializer);
        }
    }

    @BeforeEach
    public void setUp() throws AiravataSecurityException, ApplicationSettingsException {
        reset(mockRegistryService, mockSharingRegistryService, mockAuthzCacheManagerFactory, mockAuthzCacheManager);
        // Configure the factory to return the mock cache manager
        when(mockAuthzCacheManagerFactory.getAuthzCacheManager()).thenReturn(mockAuthzCacheManager);

        // Mock GatewayResourceProfile for tests that need it (when token validation is attempted)
        GatewayResourceProfile mockGwrp = new GatewayResourceProfile();
        mockGwrp.setGatewayID(TEST_GATEWAY);
        mockGwrp.setIdentityServerTenant("test-realm");
        try {
            when(mockRegistryService.getGatewayResourceProfile(TEST_GATEWAY)).thenReturn(mockGwrp);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void testDisallowedGatewayUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("getAllGatewaySSHPubKeys", false);
    }

    @Test
    public void testAllowedGatewayUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("createProject", true);
    }

    @Test
    public void testAllowedGatewayUserMethodForUserHasAccess()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("userHasAccess", true);
    }

    @Test
    public void testAllowedGatewayUserMethodForGetGroupResourceList()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("getGroupResourceList", true);
    }

    @Test
    public void testAllowedGatewayUserMethodForRevokeSharingOfResourceFromGroups()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("revokeSharingOfResourceFromGroups", true);
    }

    @Test
    public void testAllowedGatewayUserMethodForGetApplicationDeployment()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        runGatewayUserMethodTest("getApplicationDeployment", true);
    }

    private void runGatewayUserMethodTest(String methodName, boolean expectedAuthorization)
            throws IOException, ApplicationSettingsException, AiravataSecurityException {
        createExpectationsForTokenVerification();
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, false);

        runIsUserAuthorizedTest(methodName, expectedAuthorization);
    }

    @Test
    public void testAllowedAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForTokenVerification();
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(true, false);

        runIsUserAuthorizedTest("deleteGateway", true);
    }

    @Test
    public void testAllowedReadOnlyAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForTokenVerification();
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("getAllGatewaySSHPubKeys", true);
    }

    @Test
    public void testDisallowedReadOnlyAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForTokenVerification();
        createExpectationsForAuthzCacheDisabled();
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("deleteGateway", false);
    }

    @Test
    public void testAuthorizedMethodFromCache()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForAuthzCache(true, "someMethod", AuthzCachedStatus.AUTHORIZED);

        runIsUserAuthorizedTest("someMethod", true);
    }

    @Test
    public void testNotAuthorizedMethodFromCache()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForAuthzCache(true, "someMethod", AuthzCachedStatus.NOT_AUTHORIZED);

        runIsUserAuthorizedTest("someMethod", false);
    }

    @Test
    public void testWithAuthzDecisionNotInCache()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForTokenVerification();
        createExpectationsForGatewayGroupsMembership(false, true);
        createExpectationsForAuthzCache(true, "getAllGatewaySSHPubKeys", AuthzCachedStatus.NOT_CACHED);

        runIsUserAuthorizedTest("getAllGatewaySSHPubKeys", true);
    }

    private void runIsUserAuthorizedTest(String apiMethod, boolean expectedAuthorization)
            throws AiravataSecurityException, ApplicationSettingsException {
        AuthzToken authzToken = new AuthzToken();
        authzToken.setAccessToken(TEST_ACCESS_TOKEN);
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.USER_NAME, TEST_USERNAME);
        claimsMap.put(Constants.GATEWAY_ID, TEST_GATEWAY);
        authzToken.setClaimsMap(claimsMap);
        Map<String, String> metadata = new HashMap<>();
        metadata.put(Constants.API_METHOD_NAME, apiMethod);
        boolean authorized = keyCloakSecurityManager.isUserAuthorized(authzToken, metadata);
        if (expectedAuthorization) {
            assertTrue(authorized, "User should be authorized for method " + apiMethod);
        } else {
            assertFalse(authorized, "User should NOT be authorized for method " + apiMethod);
        }
    }

    private void createExpectationsForTokenVerification() throws IOException, ApplicationSettingsException {
        // Note: URL and HttpURLConnection mocking is complex with Mockito.
        // This test may need to be refactored to use a different approach for HTTP mocking,
        // such as WireMock or a custom test configuration that provides mock HTTP connections.
        // For now, the test structure is preserved but HTTP mocking would need to be implemented
        // using Mockito or another mocking framework compatible with Spring Boot Test.
        // The actual HTTP calls in KeyCloakSecurityManager would need to be mocked at a different level
        // or the class would need to be refactored to use a testable HTTP client abstraction.
    }

    private void createExpectationsForGatewayGroupsMembership(
            boolean isInAdminsGroup, boolean isInReadOnlyAdminsGroup) {

        try {
            var testGatewayGroups = new GatewayGroups();
            testGatewayGroups.setGatewayId(TEST_GATEWAY);
            testGatewayGroups.setAdminsGroupId("admins-group-id");
            testGatewayGroups.setReadOnlyAdminsGroupId("read-only-admins-group-id");
            testGatewayGroups.setDefaultGatewayUsersGroupId("default-gateway-users-group-id");
            when(mockRegistryService.isGatewayGroupsExists(TEST_GATEWAY)).thenReturn(true);
            when(mockRegistryService.getGatewayGroups(TEST_GATEWAY)).thenReturn(testGatewayGroups);

            List<UserGroup> userGroups = new ArrayList<>();
            UserGroup dummyGroup1 = new UserGroup();
            dummyGroup1.setGroupId("dummy1-group-id");
            userGroups.add(dummyGroup1);
            UserGroup dummyGroup2 = new UserGroup();
            dummyGroup2.setGroupId("dummy2-group-id");
            userGroups.add(dummyGroup2);
            if (isInAdminsGroup) {
                UserGroup adminsGroup = new UserGroup();
                adminsGroup.setGroupId("admins-group-id");
                userGroups.add(adminsGroup);
            }
            if (isInReadOnlyAdminsGroup) {
                UserGroup readOnlyAdminsGroup = new UserGroup();
                readOnlyAdminsGroup.setGroupId("read-only-admins-group-id");
                userGroups.add(readOnlyAdminsGroup);
            }
            when(mockSharingRegistryService.getAllMemberGroupsForUser(TEST_GATEWAY, TEST_USERNAME + "@" + TEST_GATEWAY))
                    .thenReturn(userGroups);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expectations for gateway groups membership", e);
        }
    }

    private void createExpectationsForAuthzCacheDisabled()
            throws ApplicationSettingsException, AiravataSecurityException {

        // For disabled cache tests, mock cache to return NOT_CACHED so it falls through to group membership check
        createExpectationsForAuthzCache(true, null, AuthzCachedStatus.NOT_CACHED);
    }

    private void createExpectationsForAuthzCache(
            boolean cacheEnabled, String apiMethod, AuthzCachedStatus authzCachedStatus)
            throws ApplicationSettingsException, AiravataSecurityException {

        // Configure the factory to return the mock cache manager
        when(mockAuthzCacheManagerFactory.getAuthzCacheManager()).thenReturn(mockAuthzCacheManager);

        if (cacheEnabled && apiMethod != null && authzCachedStatus != null) {
            when(mockAuthzCacheManager.getAuthzCachedStatus(any(AuthzCacheIndex.class)))
                    .thenReturn(authzCachedStatus);
            // Mock addToAuthzCache for cache updates (needed for NOT_CACHED status)
            doNothing().when(mockAuthzCacheManager).addToAuthzCache(any(AuthzCacheIndex.class), any());

            // For NOT_CACHED status, gateway groups should already be set up by the test
            // Don't overwrite them here
        } else if (cacheEnabled && authzCachedStatus != null) {
            // Cache enabled but no specific method - return NOT_CACHED for all
            when(mockAuthzCacheManager.getAuthzCachedStatus(any(AuthzCacheIndex.class)))
                    .thenReturn(authzCachedStatus);
            doNothing().when(mockAuthzCacheManager).addToAuthzCache(any(AuthzCacheIndex.class), any());
            // For NOT_CACHED status, set up default gateway groups if not already set up
            if (authzCachedStatus == AuthzCachedStatus.NOT_CACHED) {
                createExpectationsForGatewayGroupsMembership(false, false);
            }
        }
    }
}
