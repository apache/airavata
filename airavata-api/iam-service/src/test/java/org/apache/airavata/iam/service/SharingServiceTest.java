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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.apache.airavata.iam.model.GroupAdminEntity;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SharingService#reconcileGroupMembership} — the declarative member/admin diff. */
class SharingServiceTest {

    private static UserEntity user(String id) {
        UserEntity u = new UserEntity();
        u.setUserId(id);
        return u;
    }

    private static GroupAdminEntity admin(String id) {
        GroupAdminEntity a = new GroupAdminEntity();
        a.setAdminId(id);
        return a;
    }

    @Test
    void reconcileGroupMembership_update_computesAddRemoveDeltasAndPromotesAdmin() throws Exception {
        SharingService service = spy(new SharingService());

        // Current: members {u1, u2}, admins {u1}.
        doReturn(List.of(user("u1@gw"), user("u2@gw")))
                .when(service)
                .getGroupMembersOfTypeUser(eq("gw"), eq("g1"), anyInt(), anyInt());
        UserGroupEntity entity = new UserGroupEntity();
        entity.setGroupId("g1");
        entity.setGroupAdmins(List.of(admin("u1@gw")));
        doReturn(entity).when(service).getGroup("gw", "g1");

        doReturn(true).when(service).addUsersToGroup(eq("gw"), eq(List.of("u3@gw")), eq("g1"));
        doReturn(true).when(service).removeUsersFromGroup(eq("gw"), eq(List.of("u2@gw")), eq("g1"));
        doReturn(true).when(service).addGroupAdmins(eq("gw"), eq("g1"), eq(List.of("u3@gw")));
        doReturn(true).when(service).removeGroupAdmins(eq("gw"), eq("g1"), eq(List.of("u1@gw")));

        // Desired: members {u1}, admins {u3}. u3 is an admin but not a desired member -> promoted to member.
        // owner@gw is the owner (excluded from the diff); it is not among the test users.
        service.reconcileGroupMembership("gw", "g1", List.of("u1@gw"), List.of("u3@gw"), "owner@gw", false);

        verify(service).addUsersToGroup("gw", List.of("u3@gw"), "g1"); // u3 added (incl. promotion)
        verify(service).removeUsersFromGroup("gw", List.of("u2@gw"), "g1"); // u2 dropped
        verify(service).addGroupAdmins("gw", "g1", List.of("u3@gw")); // u3 promoted to admin
        verify(service).removeGroupAdmins("gw", "g1", List.of("u1@gw")); // u1 demoted from admin
    }

    @Test
    void reconcileGroupMembership_create_addsAllDesiredWithAdminPromotion() throws Exception {
        SharingService service = spy(new SharingService());

        // New group: no current roster is read.
        doReturn(true).when(service).addUsersToGroup(eq("gw"), eq(List.of("u1@gw", "u3@gw")), eq("g1"));
        doReturn(true).when(service).addGroupAdmins(eq("gw"), eq("g1"), eq(List.of("u3@gw")));

        // Desired: members {u1}, admins {u3}; u3 promoted to member.
        service.reconcileGroupMembership("gw", "g1", List.of("u1@gw"), List.of("u3@gw"), "owner@gw", true);

        verify(service).addUsersToGroup("gw", List.of("u1@gw", "u3@gw"), "g1");
        verify(service).addGroupAdmins("gw", "g1", List.of("u3@gw"));
        // A new group has no current roster, so nothing is revoked.
        verify(service, never()).removeUsersFromGroup(eq("gw"), org.mockito.ArgumentMatchers.anyList(), eq("g1"));
        verify(service, never()).removeGroupAdmins(eq("gw"), eq("g1"), org.mockito.ArgumentMatchers.anyList());
    }
}
