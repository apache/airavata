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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.profile.utils.TenantManagementKeycloakImpl;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Helper class for mocking Keycloak operations in tests.
 * Provides static methods and mock setup utilities.
 */
public class MockKeycloakHelper {

    /**
     * Creates a mock TenantManagementKeycloakImpl with default behaviors.
     * Use this in test classes with @MockBean annotation.
     */
    public static TenantManagementKeycloakImpl createMockKeycloakClient() {
        return Mockito.mock(TenantManagementKeycloakImpl.class);
    }

    /**
     * Sets up mock behaviors for gateway setup operations.
     */
    public static void setupMockGatewaySetup(
            TenantManagementKeycloakImpl mockClient, Gateway gateway) throws Exception {
        Gateway gatewayWithCredentials = new Gateway();
        gatewayWithCredentials.setGatewayId(gateway.getGatewayId());
        gatewayWithCredentials.setOauthClientId("test-client-id");
        gatewayWithCredentials.setOauthClientSecret("test-client-secret");

        Mockito.when(mockClient.addTenant(Mockito.any(), Mockito.any())).thenReturn(gatewayWithCredentials);
        Mockito.when(mockClient.createTenantAdminAccount(Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(mockClient.configureClient(Mockito.any(), Mockito.any())).thenReturn(gatewayWithCredentials);
    }

    /**
     * Sets up mock behaviors for user operations.
     */
    public static void setupMockUserOperations(
            TenantManagementKeycloakImpl mockClient, String gatewayId, String username) throws Exception {
        UserProfile userProfile = TestDataFactory.createTestUserProfile(username, gatewayId);

        Mockito.when(mockClient.isUserExist(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(true);
        Mockito.when(mockClient.isUserAccountEnabled(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(true);
        Mockito.when(mockClient.getUser(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(userProfile);
        Mockito.when(mockClient.isUsernameAvailable(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(false);
    }

    /**
     * Sets up mock behaviors for user creation.
     */
    public static void setupMockUserCreation(
            TenantManagementKeycloakImpl mockClient, String gatewayId, String username) throws Exception {
        Mockito.when(mockClient.createUser(
                        Mockito.anyString(),
                        Mockito.eq(gatewayId),
                        Mockito.eq(username),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(mockClient.isUserExist(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(true);
    }

    /**
     * Sets up mock behaviors for user enable/disable operations.
     */
    public static void setupMockUserEnableDisable(
            TenantManagementKeycloakImpl mockClient, String gatewayId, String username) throws Exception {
        Mockito.when(mockClient.enableUserAccount(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(true);
        Mockito.when(mockClient.isUserAccountEnabled(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username)))
                .thenReturn(true);
    }

    /**
     * Sets up mock behaviors for password reset.
     */
    public static void setupMockPasswordReset(
            TenantManagementKeycloakImpl mockClient, String gatewayId, String username) throws Exception {
        Mockito.when(mockClient.resetUserPassword(
                        Mockito.anyString(), Mockito.eq(gatewayId), Mockito.eq(username), Mockito.anyString()))
                .thenReturn(true);
    }

    /**
     * Sets up mock behaviors for user search.
     */
    public static void setupMockUserSearch(
            TenantManagementKeycloakImpl mockClient, String gatewayId) throws Exception {
        List<UserProfile> users = new ArrayList<>();
        users.add(TestDataFactory.createTestUserProfile("user1", gatewayId));
        users.add(TestDataFactory.createTestUserProfile("user2", gatewayId));

        Mockito.when(mockClient.getUsers(Mockito.anyString(), Mockito.eq(gatewayId), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(users);
        Mockito.when(mockClient.findUser(
                        Mockito.anyString(), Mockito.eq(gatewayId), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(users);
    }

    /**
     * Sets up mock behaviors for role operations.
     */
    public static void setupMockRoleOperations(
            TenantManagementKeycloakImpl mockClient, String gatewayId, String username, String roleName)
            throws Exception {
        Mockito.when(mockClient.addRoleToUser(Mockito.any(), Mockito.eq(gatewayId), Mockito.eq(username), Mockito.eq(roleName)))
                .thenReturn(true);
        Mockito.when(mockClient.removeRoleFromUser(Mockito.any(), Mockito.eq(gatewayId), Mockito.eq(username), Mockito.eq(roleName)))
                .thenReturn(true);

        List<UserProfile> usersWithRole = new ArrayList<>();
        usersWithRole.add(TestDataFactory.createTestUserProfile(username, gatewayId));
        Mockito.when(mockClient.getUsersWithRole(Mockito.any(), Mockito.eq(gatewayId), Mockito.eq(roleName)))
                .thenReturn(usersWithRole);
    }
}

