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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.model.UserGroupEntity;
import org.apache.airavata.interfaces.GatewayGroupsProvider;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.group.proto.ResourcePermissionType;
import org.apache.airavata.model.group.proto.ResourceType;
import org.apache.airavata.util.SharingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceSharingService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceSharingService.class);

    private final SharingService sharingHandler;
    private final RegistryProvider registryHandler;
    private final GatewayGroupsProvider gatewayGroupsInitializer;

    @Autowired
    public ResourceSharingService(
            SharingService sharingHandler,
            RegistryProvider registryHandler,
            GatewayGroupsProvider gatewayGroupsInitializer) {
        this.sharingHandler = sharingHandler;
        this.registryHandler = registryHandler;
        this.gatewayGroupsInitializer = gatewayGroupsInitializer;
    }

    // Backwards-compatible constructor for tests
    public ResourceSharingService(SharingService sharingHandler) {
        this(sharingHandler, null, null);
    }

    public boolean shareResourceWithUsers(
            RequestContext ctx, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new ServiceAuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    sharingHandler.shareEntityWithUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":WRITE", true);
                } else if (userPermission.getValue().equals(ResourcePermissionType.READ)) {
                    sharingHandler.shareEntityWithUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":READ", true);
                } else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        sharingHandler.shareEntityWithUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                gatewayId + ":MANAGE_SHARING",
                                true);
                    } else {
                        throw new ServiceAuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                    }
                } else {
                    throw new ServiceException("Invalid ResourcePermissionType: " + userPermission.getValue());
                }
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error sharing resource with users. Resource ID: " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public boolean shareResourceWithGroups(
            RequestContext ctx, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new ServiceAuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    sharingHandler.shareEntityWithGroups(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":WRITE", true);
                } else if (groupPermission.getValue().equals(ResourcePermissionType.READ)) {
                    sharingHandler.shareEntityWithGroups(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":READ", true);
                } else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        sharingHandler.shareEntityWithGroups(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                gatewayId + ":MANAGE_SHARING",
                                true);
                    } else {
                        throw new ServiceAuthorizationException(
                                "User is not allowed to grant sharing permission because the user is not the resource owner.");
                    }
                } else {
                    throw new ServiceException("Invalid ResourcePermissionType: " + groupPermission.getValue());
                }
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error sharing resource with groups. Resource ID: " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public boolean revokeSharingOfResourceFromUsers(
            RequestContext ctx, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new ServiceAuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            for (Map.Entry<String, ResourcePermissionType> userPermission : userPermissionList.entrySet()) {
                if (userPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    sharingHandler.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":WRITE");
                } else if (userPermission.getValue().equals(ResourcePermissionType.READ)) {
                    sharingHandler.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(userPermission.getKey()), gatewayId + ":READ");
                } else if (userPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        sharingHandler.revokeEntitySharingFromUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(userPermission.getKey()),
                                gatewayId + ":MANAGE_SHARING");
                    } else {
                        throw new ServiceAuthorizationException(
                                "User is not allowed to change sharing permission because the user is not the resource owner.");
                    }
                } else {
                    throw new ServiceException("Invalid ResourcePermissionType: " + userPermission.getValue());
                }
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error revoking resource sharing from users. Resource ID: " + resourceId + ": " + e.getMessage(),
                    e);
        }
    }

    public boolean revokeSharingOfResourceFromGroups(
            RequestContext ctx, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        try {
            if (!userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)
                    && !userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
                throw new ServiceAuthorizationException(
                        "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
            }
            // For certain resource types, restrict admin group unsharing
            ResourceType resourceType = getResourceType(gatewayId, resourceId);
            if (isAdminRestrictedResourceType(resourceType)) {
                validateAdminGroupNotRevoked(gatewayId, resourceId, groupPermissionList);
            }
            for (Map.Entry<String, ResourcePermissionType> groupPermission : groupPermissionList.entrySet()) {
                if (groupPermission.getValue().equals(ResourcePermissionType.WRITE)) {
                    sharingHandler.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":WRITE");
                } else if (groupPermission.getValue().equals(ResourcePermissionType.READ)) {
                    sharingHandler.revokeEntitySharingFromUsers(
                            gatewayId, resourceId, Arrays.asList(groupPermission.getKey()), gatewayId + ":READ");
                } else if (groupPermission.getValue().equals(ResourcePermissionType.MANAGE_SHARING)) {
                    if (userHasAccess(gatewayId, userId, resourceId, ResourcePermissionType.OWNER)) {
                        createManageSharingPermissionTypeIfMissing(gatewayId);
                        sharingHandler.revokeEntitySharingFromUsers(
                                gatewayId,
                                resourceId,
                                Arrays.asList(groupPermission.getKey()),
                                gatewayId + ":MANAGE_SHARING");
                    } else {
                        throw new ServiceAuthorizationException(
                                "User is not allowed to change sharing because the user is not the resource owner");
                    }
                } else {
                    throw new ServiceException("Invalid ResourcePermissionType: " + groupPermission.getValue());
                }
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error revoking resource sharing from groups. Resource ID: " + resourceId + ": " + e.getMessage(),
                    e);
        }
    }

    public List<String> getAllAccessibleUsers(
            RequestContext ctx, String resourceId, ResourcePermissionType permissionType) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return getAllAccessibleUsersInternal(gatewayId, resourceId, permissionType, (handler, t) -> {
                try {
                    return handler.getListOfSharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ServiceException(
                    "Error getting all accessible users for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public List<String> getAllDirectlyAccessibleUsers(
            RequestContext ctx, String resourceId, ResourcePermissionType permissionType) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return getAllAccessibleUsersInternal(gatewayId, resourceId, permissionType, (handler, t) -> {
                try {
                    return handler.getListOfDirectlySharedUsers(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ServiceException(
                    "Error getting directly accessible users for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public List<String> getAllAccessibleGroups(
            RequestContext ctx, String resourceId, ResourcePermissionType permissionType) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return getAllAccessibleGroupsInternal(gatewayId, resourceId, permissionType, (handler, t) -> {
                try {
                    return handler.getListOfSharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ServiceException(
                    "Error getting all accessible groups for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public List<String> getAllDirectlyAccessibleGroups(
            RequestContext ctx, String resourceId, ResourcePermissionType permissionType) throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        try {
            return getAllAccessibleGroupsInternal(gatewayId, resourceId, permissionType, (handler, t) -> {
                try {
                    return handler.getListOfDirectlySharedGroups(gatewayId, resourceId, gatewayId + ":" + t.name());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new ServiceException(
                    "Error getting directly accessible groups for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    public boolean userHasAccess(RequestContext ctx, String resourceId, ResourcePermissionType permissionType)
            throws ServiceException {
        String gatewayId = ctx.getGatewayId();
        String userId = ctx.getUserId();
        try {
            return userHasAccess(gatewayId, userId, resourceId, permissionType);
        } catch (Exception e) {
            throw new ServiceException(
                    "Error checking user access for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    // Precedence used to fold the per-permission direct-grant lists into a single highest-permission
    // map: a later (more privileged) name overwrites an earlier one. OWNER is excluded — owners are
    // not modifiable through declarative set-sharing.
    private static final ResourcePermissionType[] SETTABLE_PRECEDENCE = {
        ResourcePermissionType.READ, ResourcePermissionType.WRITE, ResourcePermissionType.MANAGE_SHARING
    };

    // Implied-permission sets for grant/revoke deltas: WRITE implies READ; MANAGE_SHARING implies
    // READ + WRITE. Mirrors the Python _IMPLIED_PERMISSIONS.
    private static final Map<ResourcePermissionType, Set<ResourcePermissionType>> IMPLIED_PERMISSIONS = Map.of(
            ResourcePermissionType.READ,
            Set.of(ResourcePermissionType.READ),
            ResourcePermissionType.WRITE,
            Set.of(ResourcePermissionType.READ, ResourcePermissionType.WRITE),
            ResourcePermissionType.MANAGE_SHARING,
            Set.of(
                    ResourcePermissionType.READ,
                    ResourcePermissionType.WRITE,
                    ResourcePermissionType.MANAGE_SHARING));

    /**
     * Declaratively set an entity's DIRECT sharing. {@code desiredUsers} / {@code desiredGroups} are
     * {@code id -> permission NAME} maps describing the desired end-state; ids absent from a map have
     * their direct grants revoked. The server reads the current direct grants, computes the
     * grant/revoke diff (per {@link #IMPLIED_PERMISSIONS}), and applies it — replacing the Python
     * SDK's client-side diff (sharing_resources.apply_sharing_update). OWNER is not settable.
     */
    public void setEntitySharing(
            RequestContext ctx,
            String resourceId,
            Map<String, ResourcePermissionType> desiredUsers,
            Map<String, ResourcePermissionType> desiredGroups)
            throws ServiceException {
        // Authorization gate: only the owner or a MANAGE_SHARING holder may change sharing. The
        // share/revoke methods below enforce the same gate per call, but checking up-front gives a
        // single clean PERMISSION_DENIED before any partial mutation, and matches the read-then-write
        // contract of a declarative endpoint. userHasAccess(MANAGE_SHARING) is true for owners too.
        if (!userHasAccess(ctx, resourceId, ResourcePermissionType.MANAGE_SHARING)) {
            throw new ServiceAuthorizationException(
                    "User is not allowed to change sharing because the user is either not the resource owner or does not have access to share the resource");
        }
        try {
            Map<String, ResourcePermissionType> currentUsers =
                    currentDirectGrants((t) -> getAllDirectlyAccessibleUsers(ctx, resourceId, t));
            // The OWNER is unioned into the READ/WRITE/MANAGE_SHARING accessor results but ownership is
            // not a settable grant; drop it (mirroring the read side) so a desired map that omits the
            // owner does not compute a revoke against the owner's grants.
            for (String ownerId : getAllDirectlyAccessibleUsers(ctx, resourceId, ResourcePermissionType.OWNER)) {
                currentUsers.remove(ownerId);
            }
            Map<String, ResourcePermissionType> currentGroups =
                    currentDirectGrants((t) -> getAllDirectlyAccessibleGroups(ctx, resourceId, t));

            applyDeltas(
                    currentUsers,
                    desiredUsers,
                    (permissions) -> shareResourceWithUsers(ctx, resourceId, permissions),
                    (permissions) -> revokeSharingOfResourceFromUsers(ctx, resourceId, permissions));
            applyDeltas(
                    currentGroups,
                    desiredGroups,
                    (permissions) -> shareResourceWithGroups(ctx, resourceId, permissions),
                    (permissions) -> revokeSharingOfResourceFromGroups(ctx, resourceId, permissions));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error setting sharing for resource " + resourceId + ": " + e.getMessage(), e);
        }
    }

    /** A direct-grant accessor for a single permission name; throws checked exceptions. */
    @FunctionalInterface
    private interface DirectGrantAccessor {
        List<String> apply(ResourcePermissionType permissionType) throws ServiceException;
    }

    /** Applies a per-permission share / revoke bucket (the non-empty buckets only). */
    @FunctionalInterface
    private interface ShareRevokeApplier {
        void apply(Map<String, ResourcePermissionType> idToPermission) throws ServiceException;
    }

    // {id -> highest direct permission name} over [READ, WRITE, MANAGE_SHARING] in precedence order
    // (more privileged overwrites). OWNER is excluded — owners are not modifiable here. Mirrors the
    // Python current-map construction.
    private Map<String, ResourcePermissionType> currentDirectGrants(DirectGrantAccessor accessor)
            throws ServiceException {
        Map<String, ResourcePermissionType> current = new LinkedHashMap<>();
        for (ResourcePermissionType name : SETTABLE_PRECEDENCE) {
            for (String id : accessor.apply(name)) {
                current.put(id, name);
            }
        }
        return current;
    }

    // Compute the grant/revoke deltas (over current ∪ desired) and fire the share / revoke appliers
    // for each non-empty bucket, grant-then-revoke. Mirrors the Python compute_sharing_deltas +
    // apply_sharing_update.
    private void applyDeltas(
            Map<String, ResourcePermissionType> current,
            Map<String, ResourcePermissionType> desired,
            ShareRevokeApplier shareApplier,
            ShareRevokeApplier revokeApplier)
            throws ServiceException {
        Map<ResourcePermissionType, Map<String, ResourcePermissionType>> grant = new LinkedHashMap<>();
        Map<ResourcePermissionType, Map<String, ResourcePermissionType>> revoke = new LinkedHashMap<>();

        Set<String> allIds = new HashSet<>();
        allIds.addAll(current.keySet());
        allIds.addAll(desired.keySet());

        for (String id : allIds) {
            Set<ResourcePermissionType> currentSet = implied(current.get(id));
            Set<ResourcePermissionType> newSet = implied(desired.get(id));
            for (ResourcePermissionType name : currentSet) {
                if (!newSet.contains(name)) {
                    revoke.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(id, name);
                }
            }
            for (ResourcePermissionType name : newSet) {
                if (!currentSet.contains(name)) {
                    grant.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(id, name);
                }
            }
        }

        for (Map<String, ResourcePermissionType> bucket : grant.values()) {
            shareApplier.apply(bucket);
        }
        for (Map<String, ResourcePermissionType> bucket : revoke.values()) {
            revokeApplier.apply(bucket);
        }
    }

    // The implied-permission set of a name; null (no grant) -> empty set. Mirrors the Python None->set().
    private static Set<ResourcePermissionType> implied(ResourcePermissionType name) {
        if (name == null) {
            return Set.of();
        }
        return IMPLIED_PERMISSIONS.getOrDefault(name, Set.of());
    }

    // Internal helpers

    boolean userHasAccess(String gatewayId, String userId, String entityId, ResourcePermissionType permissionType) {
        String qualifiedUserId = userId + "@" + gatewayId;
        try {
            boolean hasOwnerAccess = sharingHandler.userHasAccess(
                    gatewayId, qualifiedUserId, entityId, gatewayId + ":" + ResourcePermissionType.OWNER);
            if (permissionType.equals(ResourcePermissionType.OWNER)) {
                return hasOwnerAccess;
            } else if (permissionType.equals(ResourcePermissionType.WRITE)) {
                return hasOwnerAccess
                        || sharingHandler.userHasAccess(
                                gatewayId, qualifiedUserId, entityId, gatewayId + ":" + ResourcePermissionType.WRITE);
            } else if (permissionType.equals(ResourcePermissionType.READ)) {
                return hasOwnerAccess
                        || sharingHandler.userHasAccess(
                                gatewayId, qualifiedUserId, entityId, gatewayId + ":" + ResourcePermissionType.READ);
            } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
                return hasOwnerAccess
                        || sharingHandler.userHasAccess(
                                gatewayId,
                                qualifiedUserId,
                                entityId,
                                gatewayId + ":" + ResourcePermissionType.MANAGE_SHARING);
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if user has access", e);
        }
    }

    private List<String> getAllAccessibleUsersInternal(
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingService, ResourcePermissionType, Collection<UserEntity>> userListFunction) {
        HashSet<String> accessibleUsers = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.WRITE)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.OWNER)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.READ)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.OWNER)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.OWNER)) {
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.OWNER)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.MANAGE_SHARING)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
            userListFunction
                    .apply(sharingHandler, ResourcePermissionType.OWNER)
                    .forEach(u -> accessibleUsers.add(u.getUserId()));
        }
        return new ArrayList<>(accessibleUsers);
    }

    private List<String> getAllAccessibleGroupsInternal(
            String gatewayId,
            String resourceId,
            ResourcePermissionType permissionType,
            BiFunction<SharingService, ResourcePermissionType, Collection<UserGroupEntity>> groupListFunction) {
        HashSet<String> accessibleGroups = new HashSet<>();
        if (permissionType.equals(ResourcePermissionType.WRITE)) {
            groupListFunction
                    .apply(sharingHandler, ResourcePermissionType.WRITE)
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.READ)) {
            groupListFunction
                    .apply(sharingHandler, ResourcePermissionType.READ)
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        } else if (permissionType.equals(ResourcePermissionType.MANAGE_SHARING)) {
            groupListFunction
                    .apply(sharingHandler, ResourcePermissionType.MANAGE_SHARING)
                    .forEach(g -> accessibleGroups.add(g.getGroupId()));
        }
        return new ArrayList<>(accessibleGroups);
    }

    private ResourceType getResourceType(String domainId, String entityId) throws Exception {
        var entity = sharingHandler.getEntity(domainId, entityId);
        for (ResourceType resourceType : ResourceType.values()) {
            if (entity.getEntityTypeId().equals(domainId + ":" + resourceType.name())) {
                return resourceType;
            }
        }
        throw new RuntimeException("Unrecognized entity type id: " + entity.getEntityTypeId());
    }

    private boolean isAdminRestrictedResourceType(ResourceType resourceType) {
        return resourceType == ResourceType.EXPERIMENT
                || resourceType == ResourceType.APPLICATION_DEPLOYMENT
                || resourceType == ResourceType.GROUP_RESOURCE_PROFILE;
    }

    private void validateAdminGroupNotRevoked(
            String gatewayId, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws Exception {
        if (registryHandler == null) {
            return;
        }
        GatewayGroups gatewayGroups =
                SharingHelper.retrieveGatewayGroups(registryHandler, gatewayGroupsInitializer, gatewayId);
        if (gatewayGroups == null) {
            return;
        }
        String adminsGroupId = gatewayGroups.getAdminsGroupId();
        if (adminsGroupId != null && groupPermissionList.containsKey(adminsGroupId)) {
            throw new ServiceAuthorizationException("Cannot revoke sharing from the admin group " + adminsGroupId);
        }
    }

    void createManageSharingPermissionTypeIfMissing(String domainId) throws Exception {
        SharingHelper.createManageSharingPermissionTypeIfMissing(sharingHandler, domainId);
    }
}
