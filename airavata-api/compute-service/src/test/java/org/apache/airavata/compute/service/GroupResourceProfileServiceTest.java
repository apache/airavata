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
import org.apache.airavata.api.groupprofile.GroupResourceProfileWithAccess;
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
    void getGroupResourceProfileWithAccess_writeAndAllTokenReadsPass_writeTrue() throws Exception {
        GroupComputeResourcePreference pref = GroupComputeResourcePreference.newBuilder()
                .setResourceSpecificCredentialStoreToken("resource-token")
                .build();
        GroupResourceProfile profile = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-1")
                .setGatewayId("testGateway")
                .setDefaultCredentialStoreToken("default-token")
                .addComputePreferences(pref)
                .build();
        when(registryHandler.getGroupResourceProfile("grp-profile-1")).thenReturn(profile);
        // Caller is not OWNER of the profile or any token (so each token READ check is actually
        // consulted), holds WRITE on the profile, and READ on every token (lenient default).
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), endsWith(":OWNER")))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "grp-profile-1", "testGateway:WRITE"))
                .thenReturn(true);

        GroupResourceProfileWithAccess result = service.getGroupResourceProfileWithAccess(ctx, "grp-profile-1");

        assertTrue(result.getAccess().getUserHasWriteAccess());
        verify(sharingHandler)
                .userHasAccess("testGateway", "testUser@testGateway", "default-token", "testGateway:READ");
        verify(sharingHandler)
                .userHasAccess("testGateway", "testUser@testGateway", "resource-token", "testGateway:READ");
    }

    @Test
    void getGroupResourceProfileWithAccess_tokenReadDenied_writeFalse() throws Exception {
        GroupComputeResourcePreference pref = GroupComputeResourcePreference.newBuilder()
                .setResourceSpecificCredentialStoreToken("resource-token")
                .build();
        GroupResourceProfile profile = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-1")
                .setGatewayId("testGateway")
                .setDefaultCredentialStoreToken("default-token")
                .addComputePreferences(pref)
                .build();
        when(registryHandler.getGroupResourceProfile("grp-profile-1")).thenReturn(profile);
        // Caller has WRITE on the profile but no access (neither OWNER nor READ) to the
        // resource-specific token, so the composite write flag must be false.
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "grp-profile-1", "testGateway:WRITE"))
                .thenReturn(true);
        when(sharingHandler.userHasAccess(
                        eq("testGateway"), eq("testUser@testGateway"), eq("resource-token"), anyString()))
                .thenReturn(false);

        GroupResourceProfileWithAccess result = service.getGroupResourceProfileWithAccess(ctx, "grp-profile-1");

        assertFalse(result.getAccess().getUserHasWriteAccess());
    }

    @Test
    void getGroupResourceListWithAccess_perRowFlags_writableAndTokenDenied() throws Exception {
        // Profile 1: WRITE on the profile + READ on its single token -> writable.
        GroupResourceProfile p1 = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-1")
                .setGatewayId("testGateway")
                .setDefaultCredentialStoreToken("token-1")
                .build();
        // Profile 2: WRITE on the profile but READ denied on its token -> not writable.
        GroupResourceProfile p2 = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-profile-2")
                .setGatewayId("testGateway")
                .setDefaultCredentialStoreToken("token-2")
                .build();
        when(registryHandler.getGroupResourceList(eq("testGateway"), anyList())).thenReturn(List.of(p1, p2));

        // Caller is OWNER of nothing (SharingHelper.userHasAccess is OWNER-inclusive, so denying
        // OWNER lets the explicit WRITE/READ stubs decide each row).
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), endsWith(":OWNER")))
                .thenReturn(false);
        // WRITE on both profiles.
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "grp-profile-1", "testGateway:WRITE"))
                .thenReturn(true);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "grp-profile-2", "testGateway:WRITE"))
                .thenReturn(true);
        // READ allowed on profile 1's token, denied on profile 2's token.
        when(sharingHandler.userHasAccess(eq("testGateway"), eq("testUser@testGateway"), eq("token-1"), anyString()))
                .thenReturn(true);
        when(sharingHandler.userHasAccess(eq("testGateway"), eq("testUser@testGateway"), eq("token-2"), anyString()))
                .thenReturn(false);

        List<GroupResourceProfileWithAccess> result = service.getGroupResourceListWithAccess(ctx, "testGateway");

        assertEquals(2, result.size());
        assertEquals("grp-profile-1", result.get(0).getGroupResourceProfile().getGroupResourceProfileId());
        assertTrue(result.get(0).getAccess().getUserHasWriteAccess());
        assertEquals("grp-profile-2", result.get(1).getGroupResourceProfile().getGroupResourceProfileId());
        assertFalse(result.get(1).getAccess().getUserHasWriteAccess());
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

    @Test
    void updateGroupResourceProfileReconciled_removesOrphansThenUpdatesAndRefetches() throws Exception {
        GroupResourceProfile original = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-1")
                .addComputePreferences(GroupComputeResourcePreference.newBuilder()
                        .setComputeResourceId("c-keep")
                        .setGroupResourceProfileId("grp-1"))
                .addComputePreferences(GroupComputeResourcePreference.newBuilder()
                        .setComputeResourceId("c-drop")
                        .setGroupResourceProfileId("grp-1"))
                .addComputeResourcePolicies(
                        ComputeResourcePolicy.newBuilder().setResourcePolicyId("pol-drop"))
                .build();
        GroupResourceProfile incoming = GroupResourceProfile.newBuilder()
                .setGroupResourceProfileId("grp-1")
                .addComputePreferences(
                        GroupComputeResourcePreference.newBuilder().setComputeResourceId("c-keep"))
                .build();
        GroupResourceProfileWithAccess refreshed = GroupResourceProfileWithAccess.newBuilder()
                .setGroupResourceProfile(incoming)
                .build();

        GroupResourceProfileService spy = spy(service);
        doReturn(original).when(spy).getGroupResourceProfile(ctx, "grp-1");
        doReturn(true).when(spy).removeGroupComputePrefs(eq(ctx), anyString(), anyString());
        doReturn(true).when(spy).removeGroupComputeResourcePolicy(eq(ctx), anyString());
        doNothing().when(spy).updateGroupResourceProfile(ctx, incoming);
        doReturn(refreshed).when(spy).getGroupResourceProfileWithAccess(ctx, "grp-1");

        GroupResourceProfileWithAccess result = spy.updateGroupResourceProfileReconciled(ctx, incoming);

        // Orphaned child pref removed; retained one untouched.
        verify(spy).removeGroupComputePrefs(ctx, "c-drop", "grp-1");
        verify(spy, never()).removeGroupComputePrefs(ctx, "c-keep", "grp-1");
        // Orphaned compute resource policy removed.
        verify(spy).removeGroupComputeResourcePolicy(ctx, "pol-drop");
        // Update applied + refreshed profile returned.
        verify(spy).updateGroupResourceProfile(ctx, incoming);
        assertSame(refreshed, result);
    }
}
