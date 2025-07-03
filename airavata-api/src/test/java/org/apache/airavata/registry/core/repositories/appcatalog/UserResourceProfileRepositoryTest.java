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
package org.apache.airavata.registry.core.repositories.appcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserResourceProfileRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileRepositoryTest.class);

    private UserResourceProfileRepository userResourceProfileRepository;
    private String userId = "testUser";
    private String gatewayId = "testGateway";

    public UserResourceProfileRepositoryTest() {
        super(Database.APP_CATALOG);
        userResourceProfileRepository = new UserResourceProfileRepository();
    }

    @Test
    public void UserResourceProfileRepositoryTest() throws AppCatalogException {
        UserComputeResourcePreference userComputeResourcePreference = new UserComputeResourcePreference();
        userComputeResourcePreference.setComputeResourceId("computeResource1");
        userComputeResourcePreference.setLoginUserName(userId);
        userComputeResourcePreference.setPreferredBatchQueue("queue1");
        userComputeResourcePreference.setScratchLocation("location1");

        UserStoragePreference userStoragePreference = new UserStoragePreference();
        userStoragePreference.setStorageResourceId("storageResource1");
        userStoragePreference.setLoginUserName(userId);
        userStoragePreference.setFileSystemRootLocation("location2");
        userStoragePreference.setResourceSpecificCredentialStoreToken("token1");

        UserResourceProfile userResourceProfile = new UserResourceProfile();
        userResourceProfile.setUserId(userId);
        userResourceProfile.setGatewayID(gatewayId);
        userResourceProfile.setCredentialStoreToken("token");
        userResourceProfile.setUserComputeResourcePreferences(Arrays.asList(userComputeResourcePreference));
        userResourceProfile.setUserStoragePreferences(Arrays.asList(userStoragePreference));
        userResourceProfile.setIdentityServerTenant("tenant1");
        userResourceProfile.setIdentityServerPwdCredToken("password");
        if (!userResourceProfileRepository.isUserResourceProfileExists(userId, gatewayId))
            userResourceProfileRepository.addUserResourceProfile(userResourceProfile);
        assertEquals(userId, userResourceProfile.getUserId());

        userResourceProfile.setIdentityServerTenant("tenant2");
        userResourceProfileRepository.updateUserResourceProfile(userId, gatewayId, userResourceProfile);

        UserResourceProfile retrievedUserResourceProfile =
                userResourceProfileRepository.getUserResourceProfile(userId, gatewayId);
        assertTrue(retrievedUserResourceProfile.getUserStoragePreferences().size() == 1);
        assertEquals(
                userResourceProfile.getIdentityServerTenant(), retrievedUserResourceProfile.getIdentityServerTenant());

        UserComputeResourcePreference retrievedUserComputeResourcePreference =
                userResourceProfileRepository.getUserComputeResourcePreference(
                        userId, gatewayId, userComputeResourcePreference.getComputeResourceId());
        assertEquals(
                userComputeResourcePreference.getLoginUserName(),
                retrievedUserComputeResourcePreference.getLoginUserName());

        UserStoragePreference retrievedUserStoragePreference = userResourceProfileRepository.getUserStoragePreference(
                userId, gatewayId, userStoragePreference.getStorageResourceId());
        assertEquals(
                userStoragePreference.getFileSystemRootLocation(),
                retrievedUserStoragePreference.getFileSystemRootLocation());

        assertTrue(userResourceProfileRepository.getAllUserResourceProfiles().size() == 1);
        assertTrue(userResourceProfileRepository
                        .getAllUserComputeResourcePreferences(userId, gatewayId)
                        .size()
                == 1);
        assertTrue(userResourceProfileRepository
                        .getAllUserStoragePreferences(userId, gatewayId)
                        .size()
                == 1);
        assertTrue(userResourceProfileRepository.getGatewayProfileIds(gatewayId).size() == 1);
        assertEquals(userId, userResourceProfileRepository.getUserNamefromID(userId, gatewayId));

        userResourceProfileRepository.removeUserComputeResourcePreferenceFromGateway(
                userId, gatewayId, userComputeResourcePreference.getComputeResourceId());
        userResourceProfileRepository.removeUserDataStoragePreferenceFromGateway(
                userId, gatewayId, userStoragePreference.getStorageResourceId());
        userResourceProfileRepository.removeUserResourceProfile(userId, gatewayId);
    }
}
