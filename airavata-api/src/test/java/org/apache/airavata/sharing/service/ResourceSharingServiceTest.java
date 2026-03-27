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
package org.apache.airavata.sharing.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceAuthorizationException;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceSharingServiceTest {

    @Mock
    SharingRegistryServerHandler sharingHandler;

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
        User user1 = new User();
        user1.setUserId("user1@testGateway");
        User user2 = new User();
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
}
