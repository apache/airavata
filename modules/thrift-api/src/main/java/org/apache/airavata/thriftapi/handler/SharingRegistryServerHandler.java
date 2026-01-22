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
package org.apache.airavata.thriftapi.handler;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.Entity;
import org.apache.airavata.sharing.model.EntityType;
import org.apache.airavata.sharing.model.PermissionType;
import org.apache.airavata.sharing.model.SearchCriteria;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class SharingRegistryServerHandler
        implements org.apache.airavata.thriftapi.sharing.model.SharingRegistryService.Iface {
    private final SharingRegistryService sharingRegistryService;

    public SharingRegistryServerHandler(SharingRegistryService sharingRegistryService) {
        this.sharingRegistryService = sharingRegistryService;
    }

    public static String OWNER_PERMISSION_NAME = SharingRegistryService.OWNER_PERMISSION_NAME;

    @Override
    public String getAPIVersion() {
        return org.apache.airavata.thriftapi.sharing.model.sharing_cpiConstants.SHARING_CPI_VERSION;
    }

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.sharing.model.DuplicateEntryException) {
            var ex = new org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException();
            if (e.getMessage() != null) ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.sharing.model.SharingRegistryException) {
            var ex = new org.apache.airavata.thriftapi.sharing.model.SharingRegistryException();
            if (e.getMessage() != null) ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.sharing.model.SharingRegistryException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
    }

    /**
     * * Domain Operations
     * *
     */
    @Override
    public String createDomain(org.apache.airavata.thriftapi.sharing.model.Domain domain)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException,
                    org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException, TException {
        try {
            Domain domainDomain = convertToDomainDomain(domain);
            return sharingRegistryService.createDomain(domainDomain);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updateDomain(org.apache.airavata.thriftapi.sharing.model.Domain domain)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            Domain domainDomain = convertToDomainDomain(domain);
            return sharingRegistryService.updateDomain(domainDomain);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * <p>API method to check Domain Exists</p>
     *
     * @param domainId
     */
    @Override
    public boolean isDomainExists(String domainId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isDomainExists(domainId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteDomain(String domainId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteDomain(domainId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.Domain getDomain(String domainId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            Domain domainDomain = sharingRegistryService.getDomain(domainId);
            return convertToThriftDomain(domainDomain);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.Domain> getDomains(int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<Domain> domainDomains = sharingRegistryService.getDomains(offset, limit);
            return domainDomains.stream().map(this::convertToThriftDomain).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * * User Operations
     * *
     */
    @Override
    public String createUser(org.apache.airavata.thriftapi.sharing.model.User user)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException,
                    org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException, TException {
        try {
            User domainUser = convertToDomainUser(user);
            return sharingRegistryService.createUser(domainUser);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updatedUser(org.apache.airavata.thriftapi.sharing.model.User user)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            User domainUser = convertToDomainUser(user);
            return sharingRegistryService.updatedUser(domainUser);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * <p>API method to check User Exists</p>
     *
     * @param userId
     */
    @Override
    public boolean isUserExists(String domainId, String userId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isUserExists(domainId, userId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteUser(String domainId, String userId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteUser(domainId, userId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.User getUser(String domainId, String userId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            User domainUser = sharingRegistryService.getUser(domainId, userId);
            return convertToThriftUser(domainUser);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.User> getUsers(String domain, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<User> domainUsers = sharingRegistryService.getUsers(domain, offset, limit);
            return domainUsers.stream().map(this::convertToThriftUser).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * * Group Operations
     * *
     */
    @Override
    public String createGroup(org.apache.airavata.thriftapi.sharing.model.UserGroup group)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            UserGroup domainGroup = convertToDomainUserGroup(group);
            return sharingRegistryService.createGroup(domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updateGroup(org.apache.airavata.thriftapi.sharing.model.UserGroup group)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            UserGroup domainGroup = convertToDomainUserGroup(group);
            return sharingRegistryService.updateGroup(domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * API method to check Group Exists
     * @param domainId
     * @param groupId
     * @return
     * @throws SharingRegistryException
     * @     */
    @Override
    public boolean isGroupExists(String domainId, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isGroupExists(domainId, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteGroup(String domainId, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteGroup(domainId, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.UserGroup getGroup(String domainId, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            UserGroup domainGroup = sharingRegistryService.getGroup(domainId, groupId);
            return convertToThriftUserGroup(domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.UserGroup> getGroups(String domain, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<UserGroup> domainGroups = sharingRegistryService.getGroups(domain, offset, limit);
            return domainGroups.stream().map(this::convertToThriftUserGroup).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.addUsersToGroup(domainId, userIds, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeUsersFromGroup(domainId, userIds, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.transferGroupOwnership(domainId, groupId, newOwnerId);
        } catch (Throwable e) {
            if (e instanceof DuplicateEntryException) {
                var ex = new org.apache.airavata.thriftapi.sharing.model.SharingRegistryException();
                if (e.getMessage() != null) ex.setMessage(e.getMessage());
                ex.initCause(e);
                throw ex;
            }
            throw wrapException(e);
        }
    }

    @Override
    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.addGroupAdmins(domainId, groupId, adminIds);
        } catch (Throwable e) {
            if (e instanceof DuplicateEntryException) {
                var ex = new org.apache.airavata.thriftapi.sharing.model.SharingRegistryException();
                if (e.getMessage() != null) ex.setMessage(e.getMessage());
                ex.initCause(e);
                throw ex;
            }
            throw wrapException(e);
        }
    }

    @Override
    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeGroupAdmins(domainId, groupId, adminIds);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean hasAdminAccess(String domainId, String groupId, String adminId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.hasAdminAccess(domainId, groupId, adminId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.hasOwnerAccess(domainId, groupId, ownerId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.User> getGroupMembersOfTypeUser(
            String domainId, String groupId, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<User> domainUsers = sharingRegistryService.getGroupMembersOfTypeUser(domainId, groupId, offset, limit);
            return domainUsers.stream().map(this::convertToThriftUser).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.UserGroup> getGroupMembersOfTypeGroup(
            String domainId, String groupId, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<UserGroup> domainGroups =
                    sharingRegistryService.getGroupMembersOfTypeGroup(domainId, groupId, offset, limit);
            return domainGroups.stream().map(this::convertToThriftUserGroup).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.addChildGroupsToParentGroup(domainId, childIds, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.removeChildGroupFromParentGroup(domainId, childId, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.UserGroup> getAllMemberGroupsForUser(
            String domainId, String userId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<UserGroup> domainGroups = sharingRegistryService.getAllMemberGroupsForUser(domainId, userId);
            return domainGroups.stream().map(this::convertToThriftUserGroup).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * * EntityType Operations
     * *
     */
    @Override
    public String createEntityType(org.apache.airavata.thriftapi.sharing.model.EntityType entityType)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException,
                    org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException, TException {
        try {
            EntityType domainEntityType = convertToDomainEntityType(entityType);
            return sharingRegistryService.createEntityType(domainEntityType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updateEntityType(org.apache.airavata.thriftapi.sharing.model.EntityType entityType)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            EntityType domainEntityType = convertToDomainEntityType(entityType);
            return sharingRegistryService.updateEntityType(domainEntityType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * <p>API method to check EntityType Exists</p>
     *
     * @param entityTypeId
     */
    @Override
    public boolean isEntityTypeExists(String domainId, String entityTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isEntityTypeExists(domainId, entityTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteEntityType(String domainId, String entityTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteEntityType(domainId, entityTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.EntityType getEntityType(String domainId, String entityTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            EntityType domainEntityType = sharingRegistryService.getEntityType(domainId, entityTypeId);
            return convertToThriftEntityType(domainEntityType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.EntityType> getEntityTypes(
            String domain, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<EntityType> domainEntityTypes = sharingRegistryService.getEntityTypes(domain, offset, limit);
            return domainEntityTypes.stream()
                    .map(this::convertToThriftEntityType)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * * Permission Operations
     * *
     */
    @Override
    public String createPermissionType(org.apache.airavata.thriftapi.sharing.model.PermissionType permissionType)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException,
                    org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException, TException {
        try {
            PermissionType domainPermissionType = convertToDomainPermissionType(permissionType);
            return sharingRegistryService.createPermissionType(domainPermissionType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updatePermissionType(org.apache.airavata.thriftapi.sharing.model.PermissionType permissionType)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            PermissionType domainPermissionType = convertToDomainPermissionType(permissionType);
            return sharingRegistryService.updatePermissionType(domainPermissionType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * <p>API method to check Permission Exists</p>
     *
     * @param permissionId
     */
    @Override
    public boolean isPermissionExists(String domainId, String permissionId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isPermissionExists(domainId, permissionId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deletePermissionType(String domainId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deletePermissionType(domainId, permissionTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.PermissionType getPermissionType(
            String domainId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            PermissionType domainPermissionType = sharingRegistryService.getPermissionType(domainId, permissionTypeId);
            return convertToThriftPermissionType(domainPermissionType);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.PermissionType> getPermissionTypes(
            String domain, int offset, int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<PermissionType> domainPermissionTypes =
                    sharingRegistryService.getPermissionTypes(domain, offset, limit);
            return domainPermissionTypes.stream()
                    .map(this::convertToThriftPermissionType)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * * Entity Operations
     * *
     */
    @Override
    public String createEntity(org.apache.airavata.thriftapi.sharing.model.Entity entity)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException,
                    org.apache.airavata.thriftapi.sharing.model.DuplicateEntryException, TException {
        try {
            Entity domainEntity = convertToDomainEntity(entity);
            return sharingRegistryService.createEntity(domainEntity);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean updateEntity(org.apache.airavata.thriftapi.sharing.model.Entity entity)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            Entity domainEntity = convertToDomainEntity(entity);
            return sharingRegistryService.updateEntity(domainEntity);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * <p>API method to check Entity Exists</p>
     *
     * @param entityId
     */
    @Override
    public boolean isEntityExists(String domainId, String entityId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.isEntityExists(domainId, entityId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteEntity(String domainId, String entityId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.deleteEntity(domainId, entityId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.sharing.model.Entity getEntity(String domainId, String entityId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            Entity domainEntity = sharingRegistryService.getEntity(domainId, entityId);
            return convertToThriftEntity(domainEntity);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.Entity> searchEntities(
            String domainId,
            String userId,
            List<org.apache.airavata.thriftapi.sharing.model.SearchCriteria> filters,
            int offset,
            int limit)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<SearchCriteria> domainFilters =
                    filters.stream().map(this::convertToDomainSearchCriteria).collect(Collectors.toList());
            List<Entity> domainEntities =
                    sharingRegistryService.searchEntities(domainId, userId, domainFilters, offset, limit);
            return domainEntities.stream().map(this::convertToThriftEntity).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.User> getListOfSharedUsers(
            String domainId, String entityId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<User> domainUsers = sharingRegistryService.getListOfSharedUsers(domainId, entityId, permissionTypeId);
            return domainUsers.stream().map(this::convertToThriftUser).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.User> getListOfDirectlySharedUsers(
            String domainId, String entityId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<User> domainUsers =
                    sharingRegistryService.getListOfDirectlySharedUsers(domainId, entityId, permissionTypeId);
            return domainUsers.stream().map(this::convertToThriftUser).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.UserGroup> getListOfSharedGroups(
            String domainId, String entityId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<UserGroup> domainGroups =
                    sharingRegistryService.getListOfSharedGroups(domainId, entityId, permissionTypeId);
            return domainGroups.stream().map(this::convertToThriftUserGroup).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.sharing.model.UserGroup> getListOfDirectlySharedGroups(
            String domainId, String entityId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            List<UserGroup> domainGroups =
                    sharingRegistryService.getListOfDirectlySharedGroups(domainId, entityId, permissionTypeId);
            return domainGroups.stream().map(this::convertToThriftUserGroup).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    /**
     * Sharing Entity with Users and Groups
     * @param domainId
     * @param entityId
     * @param userList
     * @param permissionTypeId
     * @param cascadePermission
     * @return
     * @throws SharingRegistryException
     * @     */
    @Override
    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.shareEntityWithUsers(
                    domainId, entityId, userList, permissionTypeId, cascadePermission);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.shareEntityWithGroups(
                    domainId, entityId, groupList, permissionTypeId, cascadePermission);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.revokeEntitySharingFromUsers(domainId, entityId, userList, permissionTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.revokeEntitySharingFromGroups(
                    domainId, entityId, groupList, permissionTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws org.apache.airavata.thriftapi.sharing.model.SharingRegistryException, TException {
        try {
            return sharingRegistryService.userHasAccess(domainId, userId, entityId, permissionTypeId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    // Conversion helper methods
    private Domain convertToDomainDomain(org.apache.airavata.thriftapi.sharing.model.Domain thrift) {
        var domain = new Domain();
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetName()) domain.setName(thrift.getName());
        if (thrift.isSetDescription()) domain.setDescription(thrift.getDescription());
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.Domain convertToThriftDomain(Domain domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.Domain();
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getName() != null) thrift.setName(domain.getName());
        if (domain.getDescription() != null) thrift.setDescription(domain.getDescription());
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private User convertToDomainUser(org.apache.airavata.thriftapi.sharing.model.User thrift) {
        var domain = new User();
        if (thrift.isSetUserId()) domain.setUserId(thrift.getUserId());
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetUserName()) domain.setUserName(thrift.getUserName());
        if (thrift.isSetFirstName()) domain.setFirstName(thrift.getFirstName());
        if (thrift.isSetLastName()) domain.setLastName(thrift.getLastName());
        if (thrift.isSetEmail()) domain.setEmail(thrift.getEmail());
        if (thrift.isSetIcon()) domain.setIcon(thrift.getIcon());
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.User convertToThriftUser(User domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.User();
        if (domain.getUserId() != null) thrift.setUserId(domain.getUserId());
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getUserName() != null) thrift.setUserName(domain.getUserName());
        if (domain.getFirstName() != null) thrift.setFirstName(domain.getFirstName());
        if (domain.getLastName() != null) thrift.setLastName(domain.getLastName());
        if (domain.getEmail() != null) thrift.setEmail(domain.getEmail());
        if (domain.getIcon() != null) thrift.setIcon(domain.getIcon());
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private UserGroup convertToDomainUserGroup(org.apache.airavata.thriftapi.sharing.model.UserGroup thrift) {
        var domain = new UserGroup();
        if (thrift.isSetGroupId()) domain.setGroupId(thrift.getGroupId());
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetName()) domain.setName(thrift.getName());
        if (thrift.isSetDescription()) domain.setDescription(thrift.getDescription());
        if (thrift.isSetOwnerId()) domain.setOwnerId(thrift.getOwnerId());
        if (thrift.isSetGroupType())
            domain.setGroupType(org.apache.airavata.sharing.model.GroupType.valueOf(
                    thrift.getGroupType().name()));
        if (thrift.isSetGroupCardinality())
            domain.setGroupCardinality(org.apache.airavata.sharing.model.GroupCardinality.valueOf(
                    thrift.getGroupCardinality().name()));
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.UserGroup convertToThriftUserGroup(UserGroup domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.UserGroup();
        if (domain.getGroupId() != null) thrift.setGroupId(domain.getGroupId());
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getName() != null) thrift.setName(domain.getName());
        if (domain.getDescription() != null) thrift.setDescription(domain.getDescription());
        if (domain.getOwnerId() != null) thrift.setOwnerId(domain.getOwnerId());
        if (domain.getGroupType() != null)
            thrift.setGroupType(org.apache.airavata.thriftapi.sharing.model.GroupType.valueOf(
                    domain.getGroupType().name()));
        if (domain.getGroupCardinality() != null)
            thrift.setGroupCardinality(org.apache.airavata.thriftapi.sharing.model.GroupCardinality.valueOf(
                    domain.getGroupCardinality().name()));
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private EntityType convertToDomainEntityType(org.apache.airavata.thriftapi.sharing.model.EntityType thrift) {
        var domain = new EntityType();
        if (thrift.isSetEntityTypeId()) domain.setEntityTypeId(thrift.getEntityTypeId());
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetName()) domain.setName(thrift.getName());
        if (thrift.isSetDescription()) domain.setDescription(thrift.getDescription());
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.EntityType convertToThriftEntityType(EntityType domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.EntityType();
        if (domain.getEntityTypeId() != null) thrift.setEntityTypeId(domain.getEntityTypeId());
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getName() != null) thrift.setName(domain.getName());
        if (domain.getDescription() != null) thrift.setDescription(domain.getDescription());
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private PermissionType convertToDomainPermissionType(
            org.apache.airavata.thriftapi.sharing.model.PermissionType thrift) {
        var domain = new PermissionType();
        if (thrift.isSetPermissionTypeId()) domain.setPermissionTypeId(thrift.getPermissionTypeId());
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetName()) domain.setName(thrift.getName());
        if (thrift.isSetDescription()) domain.setDescription(thrift.getDescription());
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.PermissionType convertToThriftPermissionType(
            PermissionType domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.PermissionType();
        if (domain.getPermissionTypeId() != null) thrift.setPermissionTypeId(domain.getPermissionTypeId());
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getName() != null) thrift.setName(domain.getName());
        if (domain.getDescription() != null) thrift.setDescription(domain.getDescription());
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private Entity convertToDomainEntity(org.apache.airavata.thriftapi.sharing.model.Entity thrift) {
        var domain = new Entity();
        if (thrift.isSetEntityId()) domain.setEntityId(thrift.getEntityId());
        if (thrift.isSetDomainId()) domain.setDomainId(thrift.getDomainId());
        if (thrift.isSetEntityTypeId()) domain.setEntityTypeId(thrift.getEntityTypeId());
        if (thrift.isSetOwnerId()) domain.setOwnerId(thrift.getOwnerId());
        if (thrift.isSetParentEntityId()) domain.setParentEntityId(thrift.getParentEntityId());
        if (thrift.isSetName()) domain.setName(thrift.getName());
        if (thrift.isSetDescription()) domain.setDescription(thrift.getDescription());
        if (thrift.isSetBinaryData()) domain.setBinaryData(thrift.getBinaryData());
        if (thrift.isSetFullText()) domain.setFullText(thrift.getFullText());
        if (thrift.isSetOriginalEntityCreationTime())
            domain.setOriginalEntityCreationTime(thrift.getOriginalEntityCreationTime());
        if (thrift.isSetCreatedTime()) domain.setCreatedTime(thrift.getCreatedTime());
        if (thrift.isSetUpdatedTime()) domain.setUpdatedTime(thrift.getUpdatedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.sharing.model.Entity convertToThriftEntity(Entity domain) {
        var thrift = new org.apache.airavata.thriftapi.sharing.model.Entity();
        if (domain.getEntityId() != null) thrift.setEntityId(domain.getEntityId());
        if (domain.getDomainId() != null) thrift.setDomainId(domain.getDomainId());
        if (domain.getEntityTypeId() != null) thrift.setEntityTypeId(domain.getEntityTypeId());
        if (domain.getOwnerId() != null) thrift.setOwnerId(domain.getOwnerId());
        if (domain.getParentEntityId() != null) thrift.setParentEntityId(domain.getParentEntityId());
        if (domain.getName() != null) thrift.setName(domain.getName());
        if (domain.getDescription() != null) thrift.setDescription(domain.getDescription());
        if (domain.getBinaryData() != null) thrift.setBinaryData(domain.getBinaryData());
        if (domain.getFullText() != null) thrift.setFullText(domain.getFullText());
        if (domain.getOriginalEntityCreationTime() != null)
            thrift.setOriginalEntityCreationTime(domain.getOriginalEntityCreationTime());
        if (domain.getCreatedTime() != null) thrift.setCreatedTime(domain.getCreatedTime());
        if (domain.getUpdatedTime() != null) thrift.setUpdatedTime(domain.getUpdatedTime());
        return thrift;
    }

    private SearchCriteria convertToDomainSearchCriteria(
            org.apache.airavata.thriftapi.sharing.model.SearchCriteria thrift) {
        var domain = new SearchCriteria();
        if (thrift.isSetSearchField())
            domain.setSearchField(org.apache.airavata.sharing.model.EntitySearchField.valueOf(
                    thrift.getSearchField().name()));
        if (thrift.isSetValue()) domain.setValue(thrift.getValue());
        if (thrift.isSetSearchCondition())
            domain.setSearchCondition(org.apache.airavata.sharing.model.SearchCondition.valueOf(
                    thrift.getSearchCondition().name()));
        return domain;
    }
}
