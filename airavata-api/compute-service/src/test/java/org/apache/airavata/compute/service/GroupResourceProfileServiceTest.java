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
package org.apache.airavata.compute.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupResourceProfileServiceTest {

    @Mock
    RegistryHandler registryHandler;

    @Mock
    SharingFacade sharingHandler;

    @Mock
    GatewayGroupsInitializer gatewayGroupsInitializer;

    GroupResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        // Sharing is enabled via airavata-server.properties on the classpath.
        // Configure the sharing mock to allow all access checks and entity operations.
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(sharingHandler.searchEntityIds(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(List.of());

        service = new GroupResourceProfileService(registryHandler, sharingHandler, gatewayGroupsInitializer);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createGroupResourceProfile_sharingDisabled_returnsId() throws Exception {
        GroupResourceProfile profile =
                GroupResourceProfile.newBuilder().setGatewayId("testGateway").build();
        profile =
                profile.toBuilder().setGroupResourceProfileName("test-profile").build();
        when(registryHandler.createGroupResourceProfile(profile)).thenReturn("grp-profile-1");

        // Sharing enabled: mock entity creation and gateway groups for sharing registration
        when(sharingHandler.createEntity(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("grp-profile-1");
        when(registryHandler.isGatewayGroupsExists("testGateway")).thenReturn(true);
        GatewayGroups groups = GatewayGroups.newBuilder()
                .setGatewayId("testGateway")
                .setAdminsGroupId("admins")
                .setReadOnlyAdminsGroupId("readOnlyAdmins")
                .build();
        when(registryHandler.getGatewayGroups("testGateway")).thenReturn(groups);
        when(sharingHandler.isPermissionExists("testGateway", "testGateway:MANAGE_SHARING"))
                .thenReturn(true);

        String result = service.createGroupResourceProfile(ctx, profile);

        assertEquals("grp-profile-1", result);
        verify(registryHandler).createGroupResourceProfile(profile);
    }

    @Test
    void getGroupResourceProfile_sharingDisabled_returnsProfile() throws Exception {
        GroupResourceProfile profile = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-1")
                .build();
        when(registryHandler.getGroupResourceProfile("grp-profile-1")).thenReturn(profile);

        GroupResourceProfile result = service.getGroupResourceProfile(ctx, "grp-profile-1");

        assertNotNull(result);
        assertEquals("grp-profile-1", result.getGroupResourceProfileId());
    }

    @Test
    void getGroupResourceList_delegatesToRegistry() throws Exception {
        GroupResourceProfile p1 = GroupResourceProfile.getDefaultInstance();
        GroupResourceProfile p2 = GroupResourceProfile.getDefaultInstance();
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
        GroupResourceProfile profile = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-1")
                .setGatewayId("testGateway")
                .build();

        assertDoesNotThrow(() -> service.updateGroupResourceProfile(ctx, profile));
        verify(registryHandler).updateGroupResourceProfile(profile);
    }

    @Test
    void getGroupComputeResourcePrefList_sharingDisabled_returnsPrefs() throws Exception {
        GroupComputeResourcePreference pref = GroupComputeResourcePreference.getDefaultInstance();
        when(registryHandler.getGroupComputeResourcePrefList("grp-profile-1")).thenReturn(List.of(pref));

        List<GroupComputeResourcePreference> result = service.getGroupComputeResourcePrefList(ctx, "grp-profile-1");

        assertEquals(1, result.size());
    }

    @Test
    void getGatewayGroups_sharingDisabled_returnsGroups() throws Exception {
        GatewayGroups groups =
                GatewayGroups.newBuilder().setGatewayId("testGateway").build();
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
        ComputeResourcePolicy policy = ComputeResourcePolicy.newBuilder()
                .setResourcePolicyId("policy-1")
                .build();
        when(registryHandler.getGroupComputeResourcePolicy("policy-1")).thenReturn(policy);

        ComputeResourcePolicy result = service.getGroupComputeResourcePolicy(ctx, "policy-1");

        assertNotNull(result);
        assertEquals("policy-1", result.getResourcePolicyId());
    }
}
