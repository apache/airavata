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
package org.apache.airavata.sharing.registry.server;

import java.util.List;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.service.cpi.sharing_cpiConstants;
import org.apache.thrift.TException;

public class SharingRegistryServerHandler implements SharingRegistryService.Iface {
    private org.apache.airavata.service.SharingRegistryService sharingRegistryService;

    public static String OWNER_PERMISSION_NAME =
            org.apache.airavata.service.SharingRegistryService.OWNER_PERMISSION_NAME;

    public SharingRegistryServerHandler() throws ApplicationSettingsException, TException {
        this(new SharingRegistryDBInitConfig());
    }

    public SharingRegistryServerHandler(SharingRegistryDBInitConfig sharingRegistryDBInitConfig)
            throws ApplicationSettingsException, TException {
        DBInitializer.initializeDB(sharingRegistryDBInitConfig);
        sharingRegistryService = new org.apache.airavata.service.SharingRegistryService();
    }

    @Override
    public String getAPIVersion() throws TException {
        return sharing_cpiConstants.SHARING_CPI_VERSION;
    }

    /**
     * * Domain Operations
     * *
     */
    @Override
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException, TException {
        return sharingRegistryService.createDomain(domain);
    }

    @Override
    public boolean updateDomain(Domain domain) throws SharingRegistryException, TException {
        return sharingRegistryService.updateDomain(domain);
    }

    /**
     * <p>API method to check Domain Exists</p>
     *
     * @param domainId
     */
    @Override
    public boolean isDomainExists(String domainId) throws SharingRegistryException, TException {
        return sharingRegistryService.isDomainExists(domainId);
    }

    @Override
    public boolean deleteDomain(String domainId) throws SharingRegistryException, TException {
        return sharingRegistryService.deleteDomain(domainId);
    }

    @Override
    public Domain getDomain(String domainId) throws SharingRegistryException, TException {
        return sharingRegistryService.getDomain(domainId);
    }

    @Override
    public List<Domain> getDomains(int offset, int limit) throws TException {
        return sharingRegistryService.getDomains(offset, limit);
    }

    /**
     * * User Operations
     * *
     */
    @Override
    public String createUser(User user) throws SharingRegistryException, DuplicateEntryException, TException {
        return sharingRegistryService.createUser(user);
    }

    @Override
    public boolean updatedUser(User user) throws SharingRegistryException, TException {
        return sharingRegistryService.updatedUser(user);
    }

    /**
     * <p>API method to check User Exists</p>
     *
     * @param userId
     */
    @Override
    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException, TException {
        return sharingRegistryService.isUserExists(domainId, userId);
    }

    @Override
    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException, TException {
        return sharingRegistryService.deleteUser(domainId, userId);
    }

    @Override
    public User getUser(String domainId, String userId) throws SharingRegistryException, TException {
        return sharingRegistryService.getUser(domainId, userId);
    }

    @Override
    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException, TException {
        return sharingRegistryService.getUsers(domain, offset, limit);
    }

    /**
     * * Group Operations
     * *
     */
    @Override
    public String createGroup(UserGroup group) throws SharingRegistryException, TException {
        return sharingRegistryService.createGroup(group);
    }

    @Override
    public boolean updateGroup(UserGroup group) throws SharingRegistryException, TException {
        return sharingRegistryService.updateGroup(group);
    }

    /**
     * API method to check Group Exists
     * @param domainId
     * @param groupId
     * @return
     * @throws SharingRegistryException
     * @throws TException
     */
    @Override
    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException, TException {
        return sharingRegistryService.isGroupExists(domainId, groupId);
    }

    @Override
    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException, TException {
        return sharingRegistryService.deleteGroup(domainId, groupId);
    }

    @Override
    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException, TException {
        return sharingRegistryService.getGroup(domainId, groupId);
    }

    @Override
    public List<UserGroup> getGroups(String domain, int offset, int limit) throws TException {
        return sharingRegistryService.getGroups(domain, offset, limit);
    }

    @Override
    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.addUsersToGroup(domainId, userIds, groupId);
    }

    @Override
    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.removeUsersFromGroup(domainId, userIds, groupId);
    }

    @Override
    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.transferGroupOwnership(domainId, groupId, newOwnerId);
    }

    @Override
    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, TException {
        return sharingRegistryService.addGroupAdmins(domainId, groupId, adminIds);
    }

    @Override
    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, TException {
        return sharingRegistryService.removeGroupAdmins(domainId, groupId, adminIds);
    }

    @Override
    public boolean hasAdminAccess(String domainId, String groupId, String adminId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.hasAdminAccess(domainId, groupId, adminId);
    }

    @Override
    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.hasOwnerAccess(domainId, groupId, ownerId);
    }

    @Override
    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getGroupMembersOfTypeUser(domainId, groupId, offset, limit);
    }

    @Override
    public List<UserGroup> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getGroupMembersOfTypeGroup(domainId, groupId, offset, limit);
    }

    @Override
    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.addChildGroupsToParentGroup(domainId, childIds, groupId);
    }

    @Override
    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.removeChildGroupFromParentGroup(domainId, childId, groupId);
    }

    @Override
    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getAllMemberGroupsForUser(domainId, userId);
    }

    /**
     * * EntityType Operations
     * *
     */
    @Override
    public String createEntityType(EntityType entityType)
            throws SharingRegistryException, DuplicateEntryException, TException {
        return sharingRegistryService.createEntityType(entityType);
    }

    @Override
    public boolean updateEntityType(EntityType entityType) throws SharingRegistryException, TException {
        return sharingRegistryService.updateEntityType(entityType);
    }

    /**
     * <p>API method to check EntityType Exists</p>
     *
     * @param entityTypeId
     */
    @Override
    public boolean isEntityTypeExists(String domainId, String entityTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.isEntityTypeExists(domainId, entityTypeId);
    }

    @Override
    public boolean deleteEntityType(String domainId, String entityTypeId) throws SharingRegistryException, TException {
        return sharingRegistryService.deleteEntityType(domainId, entityTypeId);
    }

    @Override
    public EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException, TException {
        return sharingRegistryService.getEntityType(domainId, entityTypeId);
    }

    @Override
    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws TException {
        return sharingRegistryService.getEntityTypes(domain, offset, limit);
    }

    /**
     * * Permission Operations
     * *
     */
    @Override
    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException, TException {
        return sharingRegistryService.createPermissionType(permissionType);
    }

    @Override
    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException, TException {
        return sharingRegistryService.updatePermissionType(permissionType);
    }

    /**
     * <p>API method to check Permission Exists</p>
     *
     * @param permissionId
     */
    @Override
    public boolean isPermissionExists(String domainId, String permissionId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.isPermissionExists(domainId, permissionId);
    }

    @Override
    public boolean deletePermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.deletePermissionType(domainId, permissionTypeId);
    }

    @Override
    public PermissionType getPermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getPermissionType(domainId, permissionTypeId);
    }

    @Override
    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getPermissionTypes(domain, offset, limit);
    }

    /**
     * * Entity Operations
     * *
     */
    @Override
    public String createEntity(Entity entity) throws SharingRegistryException, DuplicateEntryException, TException {
        return sharingRegistryService.createEntity(entity);
    }

    @Override
    public boolean updateEntity(Entity entity) throws SharingRegistryException, TException {
        return sharingRegistryService.updateEntity(entity);
    }

    /**
     * <p>API method to check Entity Exists</p>
     *
     * @param entityId
     */
    @Override
    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException, TException {
        return sharingRegistryService.isEntityExists(domainId, entityId);
    }

    @Override
    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException, TException {
        return sharingRegistryService.deleteEntity(domainId, entityId);
    }

    @Override
    public Entity getEntity(String domainId, String entityId) throws SharingRegistryException, TException {
        return sharingRegistryService.getEntity(domainId, entityId);
    }

    @Override
    public List<Entity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException, TException {
        return sharingRegistryService.searchEntities(domainId, userId, filters, offset, limit);
    }

    @Override
    public List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getListOfSharedUsers(domainId, entityId, permissionTypeId);
    }

    @Override
    public List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getListOfDirectlySharedUsers(domainId, entityId, permissionTypeId);
    }

    @Override
    public List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getListOfSharedGroups(domainId, entityId, permissionTypeId);
    }

    @Override
    public List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.getListOfDirectlySharedGroups(domainId, entityId, permissionTypeId);
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
     * @throws TException
     */
    @Override
    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException, TException {
        return sharingRegistryService.shareEntityWithUsers(
                    domainId, entityId, userList, permissionTypeId, cascadePermission);
    }

    @Override
    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException, TException {
        return sharingRegistryService.shareEntityWithGroups(
                    domainId, entityId, groupList, permissionTypeId, cascadePermission);
    }

    @Override
    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.revokeEntitySharingFromUsers(domainId, entityId, userList, permissionTypeId);
    }

    @Override
    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.revokeEntitySharingFromGroups(
                    domainId, entityId, groupList, permissionTypeId);
    }

    @Override
    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException, TException {
        return sharingRegistryService.userHasAccess(domainId, userId, entityId, permissionTypeId);
    }
}
