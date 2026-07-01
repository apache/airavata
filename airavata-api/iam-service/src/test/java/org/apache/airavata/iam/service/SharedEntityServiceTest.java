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
import org.apache.airavata.api.iam.groupmanager.GroupWithAccess;
import org.apache.airavata.api.iam.sharing.GroupPermission;
import org.apache.airavata.api.iam.sharing.SharedEntity;
import org.apache.airavata.api.iam.sharing.UserPermission;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.iam.grpc.GroupWithAccessAssembler;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.model.group.proto.GroupModel;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.user.proto.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SharedEntityServiceTest {

    private static final String GATEWAY_ID = "testGateway";
    private static final String CALLER = "caller";
    private static final String ENTITY_ID = "entity-1";

    @Mock
    ResourceSharingService resourceSharingService;

    @Mock
    UserProfileRepository userProfileRepository;

    @Mock
    SharingService sharingHandler;

    @Mock
    GroupWithAccessAssembler groupAssembler;

    SharedEntityService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new SharedEntityService(
                resourceSharingService, userProfileRepository, sharingHandler, groupAssembler);
        ctx = new RequestContext(CALLER, GATEWAY_ID, "token", Map.of());
    }

    private static UserProfile profile(String userId) {
        return UserProfile.newBuilder()
                .setUserId(userId)
                .setGatewayId(GATEWAY_ID)
                .build();
    }

    private static String qid(String user) {
        return user + "@" + GATEWAY_ID;
    }

    @Test
    void loadSharedEntity_directOnly_composesOwnerUsersAndGroupsWithPrecedence() throws Exception {
        // Direct accessor returns:
        //   READ: alice, bob, owner ; WRITE: bob, owner ; MANAGE_SHARING: owner ; OWNER: owner
        // After precedence overwrite: alice=READ, bob=WRITE, owner=MANAGE_SHARING — then owner dropped.
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of(qid("alice"), qid("bob"), qid(CALLER)));
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of(qid("bob"), qid(CALLER)));
        when(resourceSharingService.getAllDirectlyAccessibleUsers(
                        ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of(qid(CALLER)));
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.OWNER))
                .thenReturn(List.of(qid(CALLER)));

        // One group with WRITE access.
        when(resourceSharingService.getAllDirectlyAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of());
        when(resourceSharingService.getAllDirectlyAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of("group-1"));
        when(resourceSharingService.getAllDirectlyAccessibleGroups(
                        ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of());

        when(userProfileRepository.getUserProfileByIdAndGateWay("alice", GATEWAY_ID)).thenReturn(profile("alice"));
        when(userProfileRepository.getUserProfileByIdAndGateWay("bob", GATEWAY_ID)).thenReturn(profile("bob"));
        when(userProfileRepository.getUserProfileByIdAndGateWay(CALLER, GATEWAY_ID)).thenReturn(profile(CALLER));

        UserGroupEntity group = new UserGroupEntity();
        group.setGroupId("group-1");
        when(sharingHandler.getGroup(GATEWAY_ID, "group-1")).thenReturn(group);
        GroupWithAccess gwa = GroupWithAccess.newBuilder()
                .setGroup(GroupModel.newBuilder().setId("group-1").build())
                .build();
        when(groupAssembler.buildGroupWithAccess(ctx, group)).thenReturn(gwa);

        when(resourceSharingService.userHasAccess(ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(true);

        SharedEntity result = service.loadSharedEntity(ctx, ENTITY_ID, true);

        assertEquals(ENTITY_ID, result.getEntityId());
        assertEquals(CALLER, result.getOwner().getUserId());
        assertTrue(result.getIsOwner());
        assertTrue(result.getHasSharingPermission());

        // Owner is excluded from user_permissions; alice=READ, bob=WRITE (WRITE overwrote READ).
        assertEquals(2, result.getUserPermissionsCount());
        Map<String, String> userPerms = result.getUserPermissionsList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        up -> up.getUser().getUserId(), UserPermission::getPermissionType));
        assertEquals("READ", userPerms.get("alice"));
        assertEquals("WRITE", userPerms.get("bob"));
        assertFalse(userPerms.containsKey(CALLER));

        assertEquals(1, result.getGroupPermissionsCount());
        GroupPermission gp = result.getGroupPermissions(0);
        assertEquals("group-1", gp.getGroup().getGroup().getId());
        assertEquals("WRITE", gp.getPermissionType());
    }

    @Test
    void loadSharedEntity_notOwner_setsIsOwnerFalse() throws Exception {
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of(qid("other")));
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of());
        when(resourceSharingService.getAllDirectlyAccessibleUsers(
                        ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of());
        when(resourceSharingService.getAllDirectlyAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.OWNER))
                .thenReturn(List.of(qid("other")));
        when(resourceSharingService.getAllDirectlyAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of());
        when(resourceSharingService.getAllDirectlyAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of());
        when(resourceSharingService.getAllDirectlyAccessibleGroups(
                        ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of());

        when(userProfileRepository.getUserProfileByIdAndGateWay("other", GATEWAY_ID)).thenReturn(profile("other"));
        when(resourceSharingService.userHasAccess(ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(false);

        SharedEntity result = service.loadSharedEntity(ctx, ENTITY_ID, true);

        // The single direct OWNER is "other"; the caller is not the owner.
        assertEquals("other", result.getOwner().getUserId());
        assertFalse(result.getIsOwner());
        assertFalse(result.getHasSharingPermission());
        // The owner is the only user grant and is dropped, leaving no user permissions.
        assertEquals(0, result.getUserPermissionsCount());
    }

    @Test
    void loadSharedEntity_inherited_usesAccessibleAccessors() throws Exception {
        when(resourceSharingService.getAllAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of(qid(CALLER)));
        when(resourceSharingService.getAllAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of());
        when(resourceSharingService.getAllAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of());
        when(resourceSharingService.getAllAccessibleUsers(ctx, ENTITY_ID, ResourcePermissionType.OWNER))
                .thenReturn(List.of(qid(CALLER)));
        when(resourceSharingService.getAllAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.READ))
                .thenReturn(List.of());
        when(resourceSharingService.getAllAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.WRITE))
                .thenReturn(List.of());
        when(resourceSharingService.getAllAccessibleGroups(ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(List.of());

        when(userProfileRepository.getUserProfileByIdAndGateWay(CALLER, GATEWAY_ID)).thenReturn(profile(CALLER));
        when(resourceSharingService.userHasAccess(ctx, ENTITY_ID, ResourcePermissionType.MANAGE_SHARING))
                .thenReturn(true);

        SharedEntity result = service.loadSharedEntity(ctx, ENTITY_ID, false);

        assertEquals(CALLER, result.getOwner().getUserId());
        assertTrue(result.getIsOwner());
        assertEquals(0, result.getUserPermissionsCount());
        // The direct accessors must not be touched on the inherited path.
        verify(resourceSharingService, never())
                .getAllDirectlyAccessibleUsers(any(), anyString(), any());
        verify(resourceSharingService, never())
                .getAllDirectlyAccessibleGroups(any(), anyString(), any());
    }
}
