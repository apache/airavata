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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.models.GroupCardinality;
import org.apache.airavata.sharing.models.User;
import org.apache.airavata.sharing.models.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GatewayGroupsInitializerTest {
    public static final String GATEWAY_ID = "test-gateway";
    public static final String IDENTITY_SERVER_PWD_CRED_TOKEN = "identity-server-pwd-cred-token";
    public static final String TEST_ADMIN_USERNAME = "test-admin-username";
    public static final String ADMIN_OWNER_ID = TEST_ADMIN_USERNAME + "@" + GATEWAY_ID;

    @Mocked
    RegistryService mockRegistryService;

    @Mocked
    SharingRegistryService mockSharingRegistryService;

    @Mocked
    CredentialStoreService mockCredentialStoreService;

    GatewayGroupsInitializer gatewayGroupsInitializer;

    @BeforeEach
    public void setUp() {
        gatewayGroupsInitializer = new GatewayGroupsInitializer(
                mockRegistryService, mockSharingRegistryService, mockCredentialStoreService);
    }

    @Test
    public void testWithoutAdminUser() {
        runTest(false);
    }

    @Test
    public void testWithAdminUser() {
        runTest(true);
    }

    private void runTest(boolean doesAdminUserExist) {
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(GATEWAY_ID);
        gatewayResourceProfile.setIdentityServerPwdCredToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setLoginUserName(TEST_ADMIN_USERNAME);
        passwordCredential.setGatewayId(GATEWAY_ID);
        passwordCredential.setToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        try {
            new Expectations() {
                {
                    mockRegistryService.getGatewayResourceProfile(GATEWAY_ID);
                    result = gatewayResourceProfile;
                    mockCredentialStoreService.getPasswordCredential(IDENTITY_SERVER_PWD_CRED_TOKEN, GATEWAY_ID);
                    result = passwordCredential;
                    mockSharingRegistryService.isUserExists(GATEWAY_ID, ADMIN_OWNER_ID);
                    result = doesAdminUserExist;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expectations for gateway groups initializer", e);
        }

        GatewayGroups gatewayGroups;
        try {
            gatewayGroups = gatewayGroupsInitializer.initialize(GATEWAY_ID);
            assertEquals(GATEWAY_ID, gatewayGroups.getGatewayId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize gateway groups", e);
        }

        try {
            new Verifications() {
                {
                    if (!doesAdminUserExist) {
                        User adminUser = withCapture();
                        mockSharingRegistryService.createUser(adminUser);
                        assertEquals(adminUser.getUserId(), ADMIN_OWNER_ID);
                        assertEquals(adminUser.getUserName(), TEST_ADMIN_USERNAME);
                        assertEquals(adminUser.getDomainId(), GATEWAY_ID);
                    }

                    List<UserGroup> groups = new ArrayList<>();
                    mockSharingRegistryService.createGroup(withCapture(groups));
                    assertEquals(3, groups.size());
                    groups.forEach(group -> {
                        assertEquals(GATEWAY_ID, group.getDomainId());
                        assertEquals(ADMIN_OWNER_ID, group.getOwnerId());
                        assertEquals(GroupCardinality.MULTI_USER, group.getGroupCardinality());
                    });
                    groups.forEach(group -> assertEquals(GATEWAY_ID, group.getDomainId()));
                    UserGroup gatewayUsersGroup = groups.get(0);
                    UserGroup adminsGroup = groups.get(1);
                    UserGroup readOnlyAdminsGroup = groups.get(2);
                    assertEquals("Gateway Users", gatewayUsersGroup.getName());
                    assertEquals(gatewayGroups.getDefaultGatewayUsersGroupId(), gatewayUsersGroup.getGroupId());
                    assertEquals("Admin Users", adminsGroup.getName());
                    assertEquals(gatewayGroups.getAdminsGroupId(), adminsGroup.getGroupId());
                    assertEquals("Read Only Admin Users", readOnlyAdminsGroup.getName());
                    assertEquals(gatewayGroups.getReadOnlyAdminsGroupId(), readOnlyAdminsGroup.getGroupId());
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create verifications for gateway groups initializer", e);
        }
    }
}
