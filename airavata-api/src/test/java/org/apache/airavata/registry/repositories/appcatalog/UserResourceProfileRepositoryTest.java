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
package org.apache.airavata.registry.repositories.appcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.UserResourceProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, UserResourceProfileRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UserResourceProfileRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}

    private final UserResourceProfileService userResourceProfileService;

    private String userId = "testUser";
    private String gatewayId = "testGateway";

    public UserResourceProfileRepositoryTest(UserResourceProfileService userResourceProfileService) {
        super(Database.APP_CATALOG);
        this.userResourceProfileService = userResourceProfileService;
    }

    @Test
    public void testUserResourceProfileRepository() throws AppCatalogException {
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
        if (!userResourceProfileService.isUserResourceProfileExists(userId, gatewayId))
            userResourceProfileService.addUserResourceProfile(userResourceProfile);
        assertEquals(userId, userResourceProfile.getUserId());

        userResourceProfile.setIdentityServerTenant("tenant2");
        userResourceProfileService.updateUserResourceProfile(userId, gatewayId, userResourceProfile);

        UserResourceProfile retrievedUserResourceProfile =
                userResourceProfileService.getUserResourceProfile(userId, gatewayId);
        assertTrue(retrievedUserResourceProfile.getUserStoragePreferences().size() == 1);
        assertEquals(
                userResourceProfile.getIdentityServerTenant(), retrievedUserResourceProfile.getIdentityServerTenant());

        UserComputeResourcePreference retrievedUserComputeResourcePreference =
                userResourceProfileService.getUserComputeResourcePreference(
                        userId, gatewayId, userComputeResourcePreference.getComputeResourceId());
        assertEquals(
                userComputeResourcePreference.getLoginUserName(),
                retrievedUserComputeResourcePreference.getLoginUserName());

        UserStoragePreference retrievedUserStoragePreference = userResourceProfileService.getUserStoragePreference(
                userId, gatewayId, userStoragePreference.getStorageResourceId());
        assertEquals(
                userStoragePreference.getFileSystemRootLocation(),
                retrievedUserStoragePreference.getFileSystemRootLocation());

        assertTrue(userResourceProfileService.getAllUserResourceProfiles().size() == 1);
        assertTrue(userResourceProfileService
                        .getAllUserComputeResourcePreferences(userId, gatewayId)
                        .size()
                == 1);
        assertTrue(userResourceProfileService
                        .getAllUserStoragePreferences(userId, gatewayId)
                        .size()
                == 1);
        assertTrue(userResourceProfileService.getGatewayProfileIds(gatewayId).size() == 1);
        assertEquals(userId, userResourceProfileService.getUserNamefromID(userId, gatewayId));

        userResourceProfileService.removeUserComputeResourcePreferenceFromGateway(
                userId, gatewayId, userComputeResourcePreference.getComputeResourceId());
        userResourceProfileService.removeUserDataStoragePreferenceFromGateway(
                userId, gatewayId, userStoragePreference.getStorageResourceId());
        userResourceProfileService.removeUserResourceProfile(userId, gatewayId);
    }
}
