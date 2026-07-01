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
package org.apache.airavata.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.iam.model.EntityEntity;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceSharingServiceTest {

    @Mock
    SharingService sharingHandler;

    ResourceSharingService resourceSharingService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        resourceSharingService = new ResourceSharingService(sharingHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void shareResourceWithUsers_ownerCanShare() throws Exception {
        // User is the owner
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        boolean result = resourceSharingService.shareResourceWithUsers(
                ctx, "resource-1", Map.of("otherUser", ResourcePermissionType.READ));

        assertTrue(result);
        verify(sharingHandler)
                .shareEntityWithUsers(eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:READ"), eq(true));
    }

    @Test
    void shareResourceWithUsers_nonOwnerWithoutSharingPermissionRejected() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(false);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> resourceSharingService.shareResourceWithUsers(
                        ctx, "resource-1", Map.of("otherUser", ResourcePermissionType.READ)));
    }

    @Test
    void revokeSharingOfResourceFromUsers_ownerCanRevoke() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        boolean result = resourceSharingService.revokeSharingOfResourceFromUsers(
                ctx, "resource-1", Map.of("otherUser", ResourcePermissionType.WRITE));

        assertTrue(result);
        verify(sharingHandler)
                .revokeEntitySharingFromUsers(eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:WRITE"));
    }

    @Test
    void userHasAccess_delegatesToSharingHandler() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:READ"))
                .thenReturn(true);

        boolean result = resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.READ);

        assertTrue(result);
    }

    @Test
    void getAllAccessibleUsers_returnsUserIds() throws Exception {
        UserEntity user1 = new UserEntity();
        user1.setUserId("user1@testGateway");
        UserEntity user2 = new UserEntity();
        user2.setUserId("user2@testGateway");
        when(sharingHandler.getListOfSharedUsers("testGateway", "resource-1", "testGateway:READ"))
                .thenReturn(List.of(user1));
        when(sharingHandler.getListOfSharedUsers("testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(List.of(user2));

        List<String> result =
                resourceSharingService.getAllAccessibleUsers(ctx, "resource-1", ResourcePermissionType.READ);

        assertEquals(2, result.size());
        assertTrue(result.contains("user1@testGateway"));
        assertTrue(result.contains("user2@testGateway"));
    }

    @Test
    void shareResourceWithGroups_ownerCanShare() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        boolean result = resourceSharingService.shareResourceWithGroups(
                ctx, "resource-1", Map.of("group-1", ResourcePermissionType.WRITE));

        assertTrue(result);
        verify(sharingHandler)
                .shareEntityWithGroups(
                        eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:WRITE"), eq(true));
    }

    @Test
    void shareResourceWithGroups_nonOwnerWithoutSharingPermissionRejected() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(false);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> resourceSharingService.shareResourceWithGroups(
                        ctx, "resource-1", Map.of("group-1", ResourcePermissionType.READ)));
    }

    @Test
    void shareResourceWithGroups_manageSharingUserCanShareRead() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(true);

        boolean result = resourceSharingService.shareResourceWithGroups(
                ctx, "resource-1", Map.of("group-1", ResourcePermissionType.READ));

        assertTrue(result);
        verify(sharingHandler)
                .shareEntityWithGroups(
                        eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:READ"), eq(true));
    }

    @Test
    void revokeSharingOfResourceFromGroups_ownerCanRevoke() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);
        EntityEntity entity = new EntityEntity();
        entity.setEntityTypeId("testGateway:OTHER");
        when(sharingHandler.getEntity("testGateway", "resource-1")).thenReturn(entity);

        boolean result = resourceSharingService.revokeSharingOfResourceFromGroups(
                ctx, "resource-1", Map.of("group-1", ResourcePermissionType.WRITE));

        assertTrue(result);
        verify(sharingHandler)
                .revokeEntitySharingFromUsers(eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:WRITE"));
    }

    @Test
    void revokeSharingOfResourceFromGroups_nonOwnerWithoutSharingPermissionRejected() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(false);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> resourceSharingService.revokeSharingOfResourceFromGroups(
                        ctx, "resource-1", Map.of("group-1", ResourcePermissionType.READ)));
    }

    @Test
    void getAllDirectlyAccessibleUsers_returnsUserIds() throws Exception {
        UserEntity user1 = new UserEntity();
        user1.setUserId("user1@testGateway");
        when(sharingHandler.getListOfDirectlySharedUsers("testGateway", "resource-1", "testGateway:WRITE"))
                .thenReturn(List.of(user1));
        UserEntity owner = new UserEntity();
        owner.setUserId("owner@testGateway");
        when(sharingHandler.getListOfDirectlySharedUsers("testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(List.of(owner));

        List<String> result =
                resourceSharingService.getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.WRITE);

        assertEquals(2, result.size());
        assertTrue(result.contains("user1@testGateway"));
        assertTrue(result.contains("owner@testGateway"));
    }

    @Test
    void getAllAccessibleGroups_returnsGroupIds() throws Exception {
        UserGroupEntity group1 = new UserGroupEntity();
        group1.setGroupId("group-1");
        when(sharingHandler.getListOfSharedGroups("testGateway", "resource-1", "testGateway:READ"))
                .thenReturn(List.of(group1));

        List<String> result =
                resourceSharingService.getAllAccessibleGroups(ctx, "resource-1", ResourcePermissionType.READ);

        assertEquals(1, result.size());
        assertTrue(result.contains("group-1"));
    }

    @Test
    void getAllDirectlyAccessibleGroups_returnsGroupIds() throws Exception {
        UserGroupEntity group1 = new UserGroupEntity();
        group1.setGroupId("group-1");
        when(sharingHandler.getListOfDirectlySharedGroups("testGateway", "resource-1", "testGateway:WRITE"))
                .thenReturn(List.of(group1));

        List<String> result =
                resourceSharingService.getAllDirectlyAccessibleGroups(ctx, "resource-1", ResourcePermissionType.WRITE);

        assertEquals(1, result.size());
        assertTrue(result.contains("group-1"));
    }

    @Test
    void userHasAccess_writePermission_trueWhenWriteAccess() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:WRITE"))
                .thenReturn(true);

        boolean result = resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.WRITE);

        assertTrue(result);
    }

    @Test
    void userHasAccess_manageSharingPermission() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess(
                        "testGateway", "testUser@testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(true);

        boolean result = resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);

        assertTrue(result);
    }

    @Test
    void userHasAccess_ownerImpliesAllPermissions() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        assertTrue(resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.OWNER));
        assertTrue(resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.WRITE));
        assertTrue(resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.READ));
        assertTrue(resourceSharingService.userHasAccess(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING));
    }

    @Test
    void shareResourceWithUsers_writePermission() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        boolean result = resourceSharingService.shareResourceWithUsers(
                ctx, "resource-1", Map.of("otherUser", ResourcePermissionType.WRITE));

        assertTrue(result);
        verify(sharingHandler)
                .shareEntityWithUsers(
                        eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:WRITE"), eq(true));
    }

    @Test
    void revokeSharingOfResourceFromUsers_readPermission() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(true);

        boolean result = resourceSharingService.revokeSharingOfResourceFromUsers(
                ctx, "resource-1", Map.of("otherUser", ResourcePermissionType.READ));

        assertTrue(result);
        verify(sharingHandler)
                .revokeEntitySharingFromUsers(eq("testGateway"), eq("resource-1"), anyList(), eq("testGateway:READ"));
    }

    @Test
    void getAllAccessibleUsers_ownerPermission_returnsOnlyOwners() throws Exception {
        UserEntity owner = new UserEntity();
        owner.setUserId("owner@testGateway");
        when(sharingHandler.getListOfSharedUsers("testGateway", "resource-1", "testGateway:OWNER"))
                .thenReturn(List.of(owner));

        List<String> result =
                resourceSharingService.getAllAccessibleUsers(ctx, "resource-1", ResourcePermissionType.OWNER);

        assertEquals(1, result.size());
        assertTrue(result.contains("owner@testGateway"));
    }

    @Test
    void getAllAccessibleGroups_manageSharingPermission() throws Exception {
        UserGroupEntity group1 = new UserGroupEntity();
        group1.setGroupId("group-manage-1");
        when(sharingHandler.getListOfSharedGroups("testGateway", "resource-1", "testGateway:MANAGE_SHARING"))
                .thenReturn(List.of(group1));

        List<String> result =
                resourceSharingService.getAllAccessibleGroups(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);

        assertEquals(1, result.size());
        assertTrue(result.contains("group-manage-1"));
    }

    @Test
    void setEntitySharing_computesAndAppliesUserDeltas() throws Exception {
        // Spy the SUT so the delta computation runs for real while the share/revoke delegations can
        // be verified with the exact per-permission maps. The grant/revoke methods are stubbed to
        // no-op (they have their own tested behaviour and their own auth gate).
        ResourceSharingService service = spy(resourceSharingService);

        // Auth gate: caller is the owner (MANAGE_SHARING access is true).
        doReturn(true).when(service).userHasAccess(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);

        // Current DIRECT user grants: u1=READ, u2=WRITE (WRITE also reports the READ list per the
        // store, but the highest-permission fold yields u1=READ, u2=WRITE).
        doReturn(List.of("u1", "u2"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.READ);
        doReturn(List.of("u2"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.WRITE);
        doReturn(List.of())
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);
        // No owner among the direct user grants in this scenario.
        doReturn(List.of())
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.OWNER);
        // No current group grants.
        doReturn(List.of())
                .when(service)
                .getAllDirectlyAccessibleGroups(eq(ctx), eq("resource-1"), any(ResourcePermissionType.class));

        // Stub the share/revoke delegations to no-op; we verify the maps they receive.
        doReturn(true).when(service).shareResourceWithUsers(eq(ctx), eq("resource-1"), anyMap());
        doReturn(true).when(service).revokeSharingOfResourceFromUsers(eq(ctx), eq("resource-1"), anyMap());

        // Desired: u1 -> WRITE (upgrade), u3 -> MANAGE_SHARING (new), u2 omitted (revoke all).
        Map<String, ResourcePermissionType> desiredUsers = Map.of(
                "u1", ResourcePermissionType.WRITE,
                "u3", ResourcePermissionType.MANAGE_SHARING);

        service.setEntitySharing(ctx, "resource-1", desiredUsers, Map.of());

        // Implied-permission diff per id:
        //   u1: {READ} -> {READ,WRITE}            => grant WRITE
        //   u2: {READ,WRITE} -> {}                => revoke READ, revoke WRITE
        //   u3: {} -> {READ,WRITE,MANAGE_SHARING} => grant READ, WRITE, MANAGE_SHARING
        // Expected grant buckets: READ={u3}, WRITE={u1,u3}, MANAGE_SHARING={u3}.
        // Expected revoke buckets: READ={u2}, WRITE={u2}.
        verify(service)
                .shareResourceWithUsers(ctx, "resource-1", Map.of("u3", ResourcePermissionType.READ));
        verify(service)
                .shareResourceWithUsers(
                        ctx,
                        "resource-1",
                        Map.of("u1", ResourcePermissionType.WRITE, "u3", ResourcePermissionType.WRITE));
        verify(service)
                .shareResourceWithUsers(
                        ctx, "resource-1", Map.of("u3", ResourcePermissionType.MANAGE_SHARING));
        verify(service)
                .revokeSharingOfResourceFromUsers(ctx, "resource-1", Map.of("u2", ResourcePermissionType.READ));
        verify(service)
                .revokeSharingOfResourceFromUsers(ctx, "resource-1", Map.of("u2", ResourcePermissionType.WRITE));

        // Exactly three grant buckets and two revoke buckets for users; no group share/revoke (empty).
        verify(service, times(3)).shareResourceWithUsers(eq(ctx), eq("resource-1"), anyMap());
        verify(service, times(2)).revokeSharingOfResourceFromUsers(eq(ctx), eq("resource-1"), anyMap());
        verify(service, never()).shareResourceWithGroups(eq(ctx), eq("resource-1"), anyMap());
        verify(service, never()).revokeSharingOfResourceFromGroups(eq(ctx), eq("resource-1"), anyMap());
    }

    @Test
    void setEntitySharing_rejectsCallerWithoutManageSharing() throws Exception {
        ResourceSharingService service = spy(resourceSharingService);
        doReturn(false).when(service).userHasAccess(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> service.setEntitySharing(
                        ctx, "resource-1", Map.of("u1", ResourcePermissionType.READ), Map.of()));

        verify(service, never()).shareResourceWithUsers(any(), anyString(), anyMap());
        verify(service, never()).revokeSharingOfResourceFromUsers(any(), anyString(), anyMap());
    }

    @Test
    void setEntitySharing_excludesOwnerFromRevoke() throws Exception {
        // The OWNER is unioned into every direct accessor list. A desired map that omits the owner
        // (the read side drops it) must NOT compute a revoke against the owner.
        ResourceSharingService service = spy(resourceSharingService);
        doReturn(true).when(service).userHasAccess(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);
        doReturn(List.of("owner@testGateway"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.READ);
        doReturn(List.of("owner@testGateway"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.WRITE);
        doReturn(List.of("owner@testGateway"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.MANAGE_SHARING);
        doReturn(List.of("owner@testGateway"))
                .when(service)
                .getAllDirectlyAccessibleUsers(ctx, "resource-1", ResourcePermissionType.OWNER);
        doReturn(List.of())
                .when(service)
                .getAllDirectlyAccessibleGroups(eq(ctx), eq("resource-1"), any(ResourcePermissionType.class));

        // Desired omits the owner; nothing else changes -> no share and no revoke should fire.
        service.setEntitySharing(ctx, "resource-1", Map.of(), Map.of());

        verify(service, never()).revokeSharingOfResourceFromUsers(eq(ctx), eq("resource-1"), anyMap());
        verify(service, never()).shareResourceWithUsers(eq(ctx), eq("resource-1"), anyMap());
    }
}
