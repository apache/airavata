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
package org.apache.airavata.service.groupprofile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupResourceProfileServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    @Mock
    SharingRegistryServerHandler sharingHandler;

    GroupResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new GroupResourceProfileService(registryHandler, sharingHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createGroupResourceProfile_sharingDisabled_returnsId() throws Exception {
        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGatewayId("testGateway");
        profile.setGroupResourceProfileName("test-profile");
        when(registryHandler.createGroupResourceProfile(profile)).thenReturn("grp-profile-1");

        // Sharing disabled (ServerSettings.isEnableSharing() returns false in tests)
        String result = service.createGroupResourceProfile(ctx, profile);

        assertEquals("grp-profile-1", result);
        verify(registryHandler).createGroupResourceProfile(profile);
    }

    @Test
    void getGroupResourceProfile_sharingDisabled_returnsProfile() throws Exception {
        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGroupResourceProfileId("grp-profile-1");
        when(registryHandler.getGroupResourceProfile("grp-profile-1")).thenReturn(profile);

        GroupResourceProfile result = service.getGroupResourceProfile(ctx, "grp-profile-1");

        assertNotNull(result);
        assertEquals("grp-profile-1", result.getGroupResourceProfileId());
    }

    @Test
    void getGroupResourceList_delegatesToRegistry() throws Exception {
        GroupResourceProfile p1 = new GroupResourceProfile();
        GroupResourceProfile p2 = new GroupResourceProfile();
        when(registryHandler.getGroupResourceList(eq("testGateway"), anyList())).thenReturn(List.of(p1, p2));

        List<GroupResourceProfile> result = service.getGroupResourceList(ctx, "testGateway");

        assertEquals(2, result.size());
        verify(registryHandler).getGroupResourceList(eq("testGateway"), anyList());
    }

    @Test
    void removeGroupResourceProfile_sharingDisabled_returnsTrue() throws Exception {
        when(registryHandler.removeGroupResourceProfile("grp-profile-1")).thenReturn(true);

        boolean result = service.removeGroupResourceProfile(ctx, "grp-profile-1");

        assertTrue(result);
        verify(registryHandler).removeGroupResourceProfile("grp-profile-1");
    }

    @Test
    void updateGroupResourceProfile_sharingDisabled_delegatesToRegistry() throws Exception {
        GroupResourceProfile profile = new GroupResourceProfile();
        profile.setGroupResourceProfileId("grp-profile-1");
        profile.setGatewayId("testGateway");

        assertDoesNotThrow(() -> service.updateGroupResourceProfile(ctx, profile));
        verify(registryHandler).updateGroupResourceProfile(profile);
    }

    @Test
    void getGroupComputeResourcePrefList_sharingDisabled_returnsPrefs() throws Exception {
        GroupComputeResourcePreference pref = new GroupComputeResourcePreference();
        when(registryHandler.getGroupComputeResourcePrefList("grp-profile-1")).thenReturn(List.of(pref));

        List<GroupComputeResourcePreference> result = service.getGroupComputeResourcePrefList(ctx, "grp-profile-1");

        assertEquals(1, result.size());
    }

    @Test
    void getGatewayGroups_sharingDisabled_returnsGroups() throws Exception {
        GatewayGroups groups = new GatewayGroups();
        groups.setGatewayId("testGateway");
        when(registryHandler.isGatewayGroupsExists("testGateway")).thenReturn(true);
        when(registryHandler.getGatewayGroups("testGateway")).thenReturn(groups);

        GatewayGroups result = service.getGatewayGroups(ctx);

        assertNotNull(result);
        assertEquals("testGateway", result.getGatewayId());
    }

    @Test
    void removeGroupComputePrefs_sharingDisabled_delegatesToRegistry() throws Exception {
        when(registryHandler.removeGroupComputePrefs("compute-1", "grp-profile-1"))
                .thenReturn(true);

        boolean result = service.removeGroupComputePrefs(ctx, "compute-1", "grp-profile-1");

        assertTrue(result);
    }

    @Test
    void getGroupComputeResourcePolicy_sharingDisabled_returnsPolicy() throws Exception {
        ComputeResourcePolicy policy = new ComputeResourcePolicy();
        policy.setResourcePolicyId("policy-1");
        when(registryHandler.getGroupComputeResourcePolicy("policy-1")).thenReturn(policy);

        ComputeResourcePolicy result = service.getGroupComputeResourcePolicy(ctx, "policy-1");

        assertNotNull(result);
        assertEquals("policy-1", result.getResourcePolicyId());
    }
}
