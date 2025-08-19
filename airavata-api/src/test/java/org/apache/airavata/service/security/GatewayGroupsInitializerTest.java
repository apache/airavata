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
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.security.GatewayGroupsInitializer;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GatewayGroupsInitializerTest {
    public static final String GATEWAY_ID = "test-gateway";
    public static final String IDENTITY_SERVER_PWD_CRED_TOKEN = "identity-server-pwd-cred-token";
    public static final String TEST_ADMIN_USERNAME = "test-admin-username";
    public static final String ADMIN_OWNER_ID = TEST_ADMIN_USERNAME + "@" + GATEWAY_ID;

    @Mocked
    RegistryService.Client mockRegistryClient;

    @Mocked
    SharingRegistryService.Client mockSharingRegistryClient;

    @Mocked
    CredentialStoreService.Client mockCredentialStoreClient;

    GatewayGroupsInitializer gatewayGroupsInitializer;

    @BeforeEach
    public void setUp() {
        gatewayGroupsInitializer =
                new GatewayGroupsInitializer(mockRegistryClient, mockSharingRegistryClient, mockCredentialStoreClient);
    }

    @Test
    public void testWithoutAdminUser() throws TException {
        runTest(false);
    }

    @Test
    public void testWithAdminUser() throws TException {
        runTest(true);
    }

    private void runTest(boolean doesAdminUserExist) throws TException {
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(GATEWAY_ID);
        gatewayResourceProfile.setIdentityServerPwdCredToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setLoginUserName(TEST_ADMIN_USERNAME);
        passwordCredential.setGatewayId(GATEWAY_ID);
        passwordCredential.setToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        new Expectations() {
            {
                mockRegistryClient.getGatewayResourceProfile(GATEWAY_ID);
                result = gatewayResourceProfile;
                mockCredentialStoreClient.getPasswordCredential(IDENTITY_SERVER_PWD_CRED_TOKEN, GATEWAY_ID);
                result = passwordCredential;
                mockSharingRegistryClient.isUserExists(GATEWAY_ID, ADMIN_OWNER_ID);
                result = doesAdminUserExist;
            }
        };

        GatewayGroups gatewayGroups = gatewayGroupsInitializer.initialize(GATEWAY_ID);
        assertEquals(GATEWAY_ID, gatewayGroups.getGatewayId());

        new Verifications() {
            {
                User adminUser;

                if (!doesAdminUserExist) {
                    mockSharingRegistryClient.createUser(adminUser = withCapture());
                    assertEquals(adminUser.getUserId(), ADMIN_OWNER_ID);
                    assertEquals(adminUser.getUserName(), TEST_ADMIN_USERNAME);
                    assertEquals(adminUser.getDomainId(), GATEWAY_ID);
                }

                List<UserGroup> groups = new ArrayList<>();
                mockSharingRegistryClient.createGroup(withCapture(groups));
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
    }
}
