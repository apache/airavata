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
package org.apache.airavata.iam;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.ApplicationSettingsException;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.exception.AiravataSecurityException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.UserGroup;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.iam.service.KeycloakRequestAuthenticator;
import org.apache.airavata.iam.service.MethodAuthorizationConfig;
import org.apache.airavata.iam.service.RequestAuthenticator;
import org.apache.airavata.iam.service.SharingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfiguration.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            KeycloakRequestAuthenticatorTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "security.tls.enabled=true",
            "security.iam.server-url=", // Empty to skip IAM HTTP calls in tests
            "security.manager.enabled=true",
            "test.keycloak.security.manager=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
public class KeycloakRequestAuthenticatorTest {

    public static final String TEST_USERNAME = "test-user";
    public static final String TEST_GATEWAY = "test-gateway";
    public static final String TEST_ACCESS_TOKEN = "abc123";

    @MockitoBean
    private ResourceProfileAdapter mockResourceProfileAdapter;

    @MockitoBean
    private GatewayService mockGatewayGroupsService;

    @MockitoBean
    private SharingService mockSharingService;

    @MockitoBean
    private CredentialStoreService mockCredentialStoreService;

    @MockitoBean
    private GatewayGroupsInitializer mockGatewayGroupsInitializer;

    @Autowired
    private KeycloakRequestAuthenticator keyCloakSecurityManager;

    @Configuration
    @org.springframework.boot.test.context.TestConfiguration
    @ComponentScan(basePackages = {"org.apache.airavata.iam"})
    static class TestConfiguration {
        @Bean
        @Primary
        public ServerProperties airavataServerProperties(org.springframework.core.env.Environment environment) {
            var tls = org.mockito.Mockito.mock(ServerProperties.Security.Tls.class);
            org.mockito.Mockito.when(tls.enabled()).thenReturn(true);

            var iam = org.mockito.Mockito.mock(ServerProperties.Security.Iam.class);
            org.mockito.Mockito.when(iam.serverUrl()).thenReturn("");

            var security = org.mockito.Mockito.mock(ServerProperties.Security.class);
            org.mockito.Mockito.when(security.tls()).thenReturn(tls);
            org.mockito.Mockito.when(security.iam()).thenReturn(iam);

            var properties = org.mockito.Mockito.mock(ServerProperties.class);
            org.mockito.Mockito.when(properties.security()).thenReturn(security);

            return properties;
        }

        @Bean
        @Primary
        @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
                name = "test.keycloak.security.manager",
                havingValue = "true")
        public GatewayGroupsInitializer gatewayGroupsInitializer() {
            return org.mockito.Mockito.mock(GatewayGroupsInitializer.class);
        }

        @Bean
        @Primary
        @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
                name = "test.keycloak.security.manager",
                havingValue = "true")
        public RequestAuthenticator airavataSecurityManager(
                GatewayService gatewayGroupsService,
                SharingService sharingService,
                ServerProperties properties,
                GatewayGroupsInitializer gatewayGroupsInitializer,
                MethodAuthorizationConfig methodAuthorizationConfig)
                throws AiravataSecurityException {
            return new KeycloakRequestAuthenticator(
                    gatewayGroupsService,
                    sharingService,
                    properties,
                    gatewayGroupsInitializer,
                    methodAuthorizationConfig);
        }
    }

    @BeforeEach
    public void setUp() throws AiravataSecurityException, ApplicationSettingsException {
        reset(mockGatewayGroupsService, mockSharingService);
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
        createExpectationsForGatewayGroupsMembership(false, false);

        runIsUserAuthorizedTest(methodName, expectedAuthorization);
    }

    @Test
    public void testAllowedAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForGatewayGroupsMembership(true, false);

        runIsUserAuthorizedTest("deleteGateway", true);
    }

    @Test
    public void testAllowedReadOnlyAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("getAllGatewaySSHPubKeys", true);
    }

    @Test
    public void testDisallowedReadOnlyAdminUserMethod()
            throws AiravataSecurityException, ApplicationSettingsException, IOException {
        createExpectationsForGatewayGroupsMembership(false, true);

        runIsUserAuthorizedTest("deleteGateway", false);
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

    private void createExpectationsForGatewayGroupsMembership(
            boolean isInAdminsGroup, boolean isInReadOnlyAdminsGroup) {

        try {
            var testGatewayGroups = new GatewayGroups();
            testGatewayGroups.setGatewayId(TEST_GATEWAY);
            testGatewayGroups.setAdminsGroupId("admins-group-id");
            testGatewayGroups.setReadOnlyAdminsGroupId("read-only-admins-group-id");
            testGatewayGroups.setDefaultGatewayUsersGroupId("default-gateway-users-group-id");
            when(mockGatewayGroupsService.isGatewayGroupsExists(TEST_GATEWAY)).thenReturn(true);
            when(mockGatewayGroupsService.getGatewayGroups(TEST_GATEWAY)).thenReturn(testGatewayGroups);

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
            when(mockSharingService.getAllMemberGroupsForUser(TEST_GATEWAY, TEST_USERNAME + "@" + TEST_GATEWAY))
                    .thenReturn(userGroups);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expectations for gateway groups membership", e);
        }
    }
}
