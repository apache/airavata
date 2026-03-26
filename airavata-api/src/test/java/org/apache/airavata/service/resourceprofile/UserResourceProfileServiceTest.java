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
package org.apache.airavata.service.resourceprofile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserResourceProfileServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    UserResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new UserResourceProfileService(registryHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void registerUserResourceProfile_returnsId() throws Exception {
        UserResourceProfile profile = new UserResourceProfile();
        profile.setUserId("testUser");
        profile.setGatewayID("testGateway");
        when(registryHandler.registerUserResourceProfile(profile)).thenReturn("testUser");

        String result = service.registerUserResourceProfile(ctx, profile);

        assertEquals("testUser", result);
    }

    @Test
    void isUserResourceProfileExists_delegatesToRegistry() throws Exception {
        when(registryHandler.isUserResourceProfileExists("testUser", "testGateway"))
                .thenReturn(true);

        boolean result = service.isUserResourceProfileExists(ctx, "testUser", "testGateway");

        assertTrue(result);
    }

    @Test
    void getUserResourceProfile_delegatesToRegistry() throws Exception {
        UserResourceProfile profile = new UserResourceProfile();
        profile.setUserId("testUser");
        when(registryHandler.getUserResourceProfile("testUser", "testGateway")).thenReturn(profile);

        UserResourceProfile result = service.getUserResourceProfile(ctx, "testUser", "testGateway");

        assertNotNull(result);
        assertEquals("testUser", result.getUserId());
    }

    @Test
    void deleteUserResourceProfile_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteUserResourceProfile("testUser", "testGateway"))
                .thenReturn(true);

        boolean result = service.deleteUserResourceProfile(ctx, "testUser", "testGateway");

        assertTrue(result);
    }

    @Test
    void addUserComputeResourcePreference_delegatesToRegistry() throws Exception {
        UserComputeResourcePreference pref = new UserComputeResourcePreference();
        when(registryHandler.addUserComputeResourcePreference("testUser", "testGateway", "compute-1", pref))
                .thenReturn(true);

        boolean result = service.addUserComputeResourcePreference(ctx, "testUser", "testGateway", "compute-1", pref);

        assertTrue(result);
    }

    @Test
    void getAllUserComputeResourcePreferences_delegatesToRegistry() throws Exception {
        List<UserComputeResourcePreference> prefs = List.of(new UserComputeResourcePreference());
        when(registryHandler.getAllUserComputeResourcePreferences("testUser", "testGateway"))
                .thenReturn(prefs);

        List<UserComputeResourcePreference> result =
                service.getAllUserComputeResourcePreferences(ctx, "testUser", "testGateway");

        assertEquals(1, result.size());
    }

    @Test
    void getAllUserResourceProfiles_delegatesToRegistry() throws Exception {
        List<UserResourceProfile> profiles = List.of(new UserResourceProfile());
        when(registryHandler.getAllUserResourceProfiles()).thenReturn(profiles);

        List<UserResourceProfile> result = service.getAllUserResourceProfiles(ctx);

        assertEquals(1, result.size());
    }

    @Test
    void getLatestQueueStatuses_delegatesToRegistry() throws Exception {
        List<QueueStatusModel> statuses = List.of(new QueueStatusModel());
        when(registryHandler.getLatestQueueStatuses()).thenReturn(statuses);

        List<QueueStatusModel> result = service.getLatestQueueStatuses(ctx);

        assertEquals(1, result.size());
    }

    @Test
    void addUserStoragePreference_delegatesToRegistry() throws Exception {
        UserStoragePreference pref = new UserStoragePreference();
        when(registryHandler.addUserStoragePreference("testUser", "testGateway", "storage-1", pref))
                .thenReturn(true);

        boolean result = service.addUserStoragePreference(ctx, "testUser", "testGateway", "storage-1", pref);

        assertTrue(result);
    }

    @Test
    void registryException_wrappedAsServiceException() throws Exception {
        when(registryHandler.getUserResourceProfile("bad-user", "testGateway"))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> service.getUserResourceProfile(ctx, "bad-user", "testGateway"));
    }
}
