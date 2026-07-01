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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.iam.sharing.GroupPermission;
import org.apache.airavata.api.iam.sharing.SharedEntity;
import org.apache.airavata.api.iam.sharing.UserPermission;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.exception.ServiceNotFoundException;
import org.apache.airavata.iam.grpc.GroupWithAccessAssembler;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.iam.repository.UserProfileRepository;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.user.proto.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Assembles the composed SharedEntity view (owner + per-user / per-group permissions) server-side,
 * replacing the Python SDK's sharing_resources.get_shared_entity / get_all_shared_entity. The
 * accessor lists are already authorization-scoped by {@link ResourceSharingService}, so there is no
 * extra READ gate here (mirroring the Python which has none).
 */
@Service
public class SharedEntityService {

    // A later (more privileged) grant overwrites an earlier one, so WRITE (which implies READ) wins
    // over a bare READ. Mirrors the Python _PERMISSION_PRECEDENCE.
    private static final ResourcePermissionType[] PERMISSION_PRECEDENCE = {
        ResourcePermissionType.READ, ResourcePermissionType.WRITE, ResourcePermissionType.MANAGE_SHARING
    };

    private final ResourceSharingService resourceSharingService;
    private final UserProfileRepository userProfileRepository;
    private final SharingService sharingHandler;
    private final GroupWithAccessAssembler groupAssembler;

    @Autowired
    public SharedEntityService(
            ResourceSharingService resourceSharingService,
            UserProfileRepository userProfileRepository,
            SharingService sharingHandler,
            GroupWithAccessAssembler groupAssembler) {
        this.resourceSharingService = resourceSharingService;
        this.userProfileRepository = userProfileRepository;
        this.sharingHandler = sharingHandler;
        this.groupAssembler = groupAssembler;
    }

    /** {@code (entityId, permissionType) -> qualified ids} accessor, one of the four sharing lists. */
    @FunctionalInterface
    private interface Accessor {
        List<String> apply(String entityId, ResourcePermissionType permissionType) throws Exception;
    }

    /**
     * {@code directly} selects the direct-only (true) vs. accessible-including-inherited (false)
     * accessor pair, then loads the user/group permission maps, drops the owner from the users list,
     * fetches every profile/group, and composes a {@link SharedEntity}.
     */
    public SharedEntity loadSharedEntity(RequestContext ctx, String entityId, boolean directly)
            throws ServiceException {
        try {
            Accessor usersAccessor;
            Accessor groupsAccessor;
            if (directly) {
                usersAccessor = (id, t) -> resourceSharingService.getAllDirectlyAccessibleUsers(ctx, id, t);
                groupsAccessor = (id, t) -> resourceSharingService.getAllDirectlyAccessibleGroups(ctx, id, t);
            } else {
                usersAccessor = (id, t) -> resourceSharingService.getAllAccessibleUsers(ctx, id, t);
                groupsAccessor = (id, t) -> resourceSharingService.getAllAccessibleGroups(ctx, id, t);
            }

            Map<String, ResourcePermissionType> users = collectPermissions(usersAccessor, entityId);
            // The owner is the single DIRECT owner (the OWNER grant); there is exactly one (indirect
            // cascading owners are not returned by these RPCs).
            List<String> ownerIds = usersAccessor.apply(entityId, ResourcePermissionType.OWNER);
            if (ownerIds.isEmpty()) {
                throw new ServiceNotFoundException("Shared entity " + entityId + " has no owner grant");
            }
            String ownerId = ownerIds.get(0);
            users.remove(ownerId);

            Map<String, ResourcePermissionType> groups = collectPermissions(groupsAccessor, entityId);

            SharedEntity.Builder builder = SharedEntity.newBuilder().setEntityId(entityId);

            for (Map.Entry<String, ResourcePermissionType> entry : users.entrySet()) {
                UserProfile profile = loadProfile(ctx, entry.getKey());
                builder.addUserPermissions(UserPermission.newBuilder()
                        .setUser(profile)
                        .setPermissionType(entry.getValue().name())
                        .build());
            }

            for (Map.Entry<String, ResourcePermissionType> entry : groups.entrySet()) {
                UserGroupEntity group = sharingHandler.getGroup(ctx.getGatewayId(), entry.getKey());
                if (group == null) {
                    // A grant can outlive its group; skip a dangling group grant rather than failing.
                    continue;
                }
                builder.addGroupPermissions(GroupPermission.newBuilder()
                        .setGroup(groupAssembler.buildGroupWithAccess(ctx, group))
                        .setPermissionType(entry.getValue().name())
                        .build());
            }

            UserProfile owner = loadProfile(ctx, ownerId);
            builder.setOwner(owner);
            builder.setIsOwner(owner.getUserId().equals(ctx.getUserId()));
            builder.setHasSharingPermission(
                    resourceSharingService.userHasAccess(ctx, entityId, ResourcePermissionType.MANAGE_SHARING));

            return builder.build();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error loading shared entity " + entityId + ": " + e.getMessage(), e);
        }
    }

    // {id -> permission_name} across the precedence order (most privileged grant wins). Mirrors the
    // Python _collect_permissions.
    private Map<String, ResourcePermissionType> collectPermissions(Accessor accessor, String entityId)
            throws Exception {
        Map<String, ResourcePermissionType> result = new LinkedHashMap<>();
        for (ResourcePermissionType name : PERMISSION_PRECEDENCE) {
            for (String id : accessor.apply(entityId, name)) {
                result.put(id, name);
            }
        }
        return result;
    }

    // The accessible-* lists key on qualified user@gateway ids, but getUserProfileByIdAndGateWay
    // keys on the bare username (mirrors the Python _username_from_internal_id).
    private UserProfile loadProfile(RequestContext ctx, String qualifiedUserId) {
        String bare = qualifiedUserId.substring(0, qualifiedUserId.lastIndexOf('@'));
        return userProfileRepository.getUserProfileByIdAndGateWay(bare, ctx.getGatewayId());
    }
}
