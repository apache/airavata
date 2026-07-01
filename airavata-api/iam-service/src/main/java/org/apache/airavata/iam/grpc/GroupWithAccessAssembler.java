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

import java.util.stream.Collectors;
import org.apache.airavata.api.iam.groupmanager.GroupAccessFlags;
import org.apache.airavata.api.iam.groupmanager.GroupWithAccess;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.iam.model.GroupAdminEntity;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.model.group.proto.GroupModel;
import org.springframework.stereotype.Component;

/**
 * Builds the GroupModel-plus-access envelope shared by GroupManager (single + list) and the
 * SharedEntity composite. The flags are always returned, but the member and admin rosters are
 * populated only when the caller is an insider (member, admin, owner, or a gateway admin) so an
 * outsider never sees a group's roster.
 */
@Component
public class GroupWithAccessAssembler {

    private final SharingService sharingHandler;

    public GroupWithAccessAssembler(SharingService sharingHandler) {
        this.sharingHandler = sharingHandler;
    }

    public GroupWithAccess buildGroupWithAccess(RequestContext ctx, UserGroupEntity entity) throws Exception {
        String callerId = ctx.getUserId() + "@" + ctx.getGatewayId();
        SharingService.GroupAccess access =
                sharingHandler.getGroupAccessFlags(ctx.getGatewayId(), entity.getGroupId(), callerId, entity);
        boolean insider = access.isMember()
                || access.isAdmin()
                || access.isOwner()
                || ctx.isGatewayAdmin()
                || ctx.isReadOnlyGatewayAdmin();
        // toGroupModel populates id/name/owner/description (+admins); members and admins are the
        // roster fields gated to insiders, so strip them when the caller is an outsider.
        GroupModel.Builder group = toGroupModel(entity).toBuilder();
        if (insider) {
            group.addAllMembers(access.memberIds());
        } else {
            group.clearMembers();
            group.clearAdmins();
        }
        return GroupWithAccess.newBuilder()
                .setGroup(group.build())
                .setAccess(GroupAccessFlags.newBuilder()
                        .setIsAdmin(access.isAdmin())
                        .setIsOwner(access.isOwner())
                        .setIsMember(access.isMember())
                        .setIsGatewayAdminsGroup(access.isGatewayAdminsGroup())
                        .setIsReadOnlyGatewayAdminsGroup(access.isReadOnlyGatewayAdminsGroup())
                        .setIsDefaultGatewayUsersGroup(access.isDefaultGatewayUsersGroup())
                        .build())
                .build();
    }

    static GroupModel toGroupModel(UserGroupEntity entity) {
        GroupModel.Builder b = GroupModel.newBuilder();
        if (entity.getGroupId() != null) b.setId(entity.getGroupId());
        if (entity.getName() != null) b.setName(entity.getName());
        if (entity.getOwnerId() != null) b.setOwnerId(entity.getOwnerId());
        if (entity.getDescription() != null) b.setDescription(entity.getDescription());
        if (entity.getGroupAdmins() != null) {
            b.addAllAdmins(entity.getGroupAdmins().stream()
                    .map(GroupAdminEntity::getAdminId)
                    .collect(Collectors.toList()));
        }
        return b.build();
    }
}
