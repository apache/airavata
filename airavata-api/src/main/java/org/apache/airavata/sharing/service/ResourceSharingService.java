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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSharingService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceSharingService.class);

    private final SharingRegistryServerHandler sharingHandler;
    private final RegistryServerHandler registryHandler;

    public ResourceSharingService(SharingRegistryServerHandler sharingHandler, RegistryServerHandler registryHandler) {
        this.sharingHandler = sharingHandler;
        this.registryHandler = registryHandler;
    }

    // Backwards-compatible constructor for tests
    public ResourceSharingService(SharingRegistryServerHandler sharingHandler) {
        this(sharingHandler, null);
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
            BiFunction<SharingRegistryServerHandler, ResourcePermissionType, Collection<User>> userListFunction) {
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
            BiFunction<SharingRegistryServerHandler, ResourcePermissionType, Collection<UserGroup>> groupListFunction) {
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
        GatewayGroups gatewayGroups = SharingHelper.retrieveGatewayGroups(registryHandler, gatewayId);
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
