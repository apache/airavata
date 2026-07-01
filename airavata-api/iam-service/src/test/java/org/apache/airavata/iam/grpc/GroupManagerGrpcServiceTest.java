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
package org.apache.airavata.iam.grpc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.airavata.api.iam.groupmanager.GetAllGroupsUserBelongsRequest;
import org.apache.airavata.api.iam.groupmanager.GetGroupsRequest;
import org.apache.airavata.api.iam.groupmanager.GetGroupsWithAccessResponse;
import org.apache.airavata.api.iam.groupmanager.GroupWithAccess;
import org.apache.airavata.config.Constants;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.iam.model.GroupAdminEntity;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.model.security.proto.AuthzToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupManagerGrpcServiceTest {

    private static final String GATEWAY_ID = "testGateway";
    private static final String CALLER = "caller@" + GATEWAY_ID;

    @Mock
    SharingService sharingHandler;

    GroupManagerGrpcService service;

    @BeforeEach
    void setUp() {
        service = new GroupManagerGrpcService(sharingHandler, new GroupWithAccessAssembler(sharingHandler));
        // GrpcRequestContext.current() resolves the caller from UserContext thread-locals.
        UserContext.setAuthzToken(AuthzToken.newBuilder()
                .setAccessToken("token")
                .putClaimsMap(Constants.USER_NAME, "caller")
                .putClaimsMap(Constants.GATEWAY_ID, GATEWAY_ID)
                .build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private static UserGroupEntity group(String id) {
        UserGroupEntity entity = new UserGroupEntity();
        entity.setGroupId(id);
        entity.setDomainId(GATEWAY_ID);
        entity.setName("Group " + id);
        entity.setDescription("desc " + id);
        entity.setOwnerId("owner@" + GATEWAY_ID);
        GroupAdminEntity admin = new GroupAdminEntity();
        admin.setAdminId("admin@" + GATEWAY_ID);
        entity.setGroupAdmins(List.of(admin));
        return entity;
    }

    private static SharingService.GroupAccess access(boolean member) {
        // memberIds always carries the roster; only the read gate decides whether it is exposed.
        return new SharingService.GroupAccess(
                List.of("m1@" + GATEWAY_ID, "m2@" + GATEWAY_ID), false, false, member, false, false, false);
    }

    @Test
    void getGroupsWithAccess_insiderSeesRosterOutsiderDoesNot() throws Exception {
        UserGroupEntity insiderGroup = group("g-in");
        UserGroupEntity outsiderGroup = group("g-out");
        when(sharingHandler.getGroups(GATEWAY_ID, 0, -1)).thenReturn(List.of(insiderGroup, outsiderGroup));
        when(sharingHandler.getGroupAccessFlags(GATEWAY_ID, "g-in", CALLER, insiderGroup))
                .thenReturn(access(true));
        when(sharingHandler.getGroupAccessFlags(GATEWAY_ID, "g-out", CALLER, outsiderGroup))
                .thenReturn(access(false));

        GetGroupsWithAccessResponse response = capture(o -> service.getGroupsWithAccess(
                GetGroupsRequest.getDefaultInstance(), o));

        assertEquals(2, response.getGroupsCount());

        GroupWithAccess insider = response.getGroups(0);
        assertEquals("g-in", insider.getGroup().getId());
        assertTrue(insider.getAccess().getIsMember());
        // Insider: roster (members + admins) is exposed.
        assertEquals(List.of("m1@" + GATEWAY_ID, "m2@" + GATEWAY_ID), insider.getGroup().getMembersList());
        assertEquals(List.of("admin@" + GATEWAY_ID), insider.getGroup().getAdminsList());

        GroupWithAccess outsider = response.getGroups(1);
        assertEquals("g-out", outsider.getGroup().getId());
        assertFalse(outsider.getAccess().getIsMember());
        // Outsider: flags are returned but the member/admin roster is withheld.
        assertTrue(outsider.getGroup().getMembersList().isEmpty());
        assertTrue(outsider.getGroup().getAdminsList().isEmpty());
        // Non-roster fields remain visible.
        assertEquals("Group g-out", outsider.getGroup().getName());
        assertEquals("owner@" + GATEWAY_ID, outsider.getGroup().getOwnerId());
    }

    @Test
    void getAllGroupsUserBelongsWithAccess_unionsEachGroupWithFlags() throws Exception {
        String userName = "m1@" + GATEWAY_ID;
        UserGroupEntity g1 = group("g1");
        UserGroupEntity g2 = group("g2");
        when(sharingHandler.getAllMemberGroupEntitiesForUser(GATEWAY_ID, userName))
                .thenReturn(List.of(g1, g2));
        when(sharingHandler.getGroupAccessFlags(GATEWAY_ID, "g1", CALLER, g1)).thenReturn(access(true));
        when(sharingHandler.getGroupAccessFlags(GATEWAY_ID, "g2", CALLER, g2)).thenReturn(access(true));

        GetGroupsWithAccessResponse response = capture(o -> service.getAllGroupsUserBelongsWithAccess(
                GetAllGroupsUserBelongsRequest.newBuilder().setUserName(userName).build(), o));

        assertEquals(2, response.getGroupsCount());
        GroupWithAccess first = response.getGroups(0);
        assertEquals("g1", first.getGroup().getId());
        assertTrue(first.getAccess().getIsMember());
        // member of the group: roster exposed
        assertEquals(List.of("m1@" + GATEWAY_ID, "m2@" + GATEWAY_ID), first.getGroup().getMembersList());
        assertEquals("g2", response.getGroups(1).getGroup().getId());
    }

    private static <T> T capture(java.util.function.Consumer<StreamObserver<T>> call) {
        AtomicReference<T> ref = new AtomicReference<>();
        AtomicReference<Throwable> err = new AtomicReference<>();
        call.accept(new StreamObserver<>() {
            @Override
            public void onNext(T value) {
                ref.set(value);
            }

            @Override
            public void onError(Throwable t) {
                err.set(t);
            }

            @Override
            public void onCompleted() {}
        });
        if (err.get() != null) {
            fail("adapter reported error", err.get());
        }
        return ref.get();
    }
}
