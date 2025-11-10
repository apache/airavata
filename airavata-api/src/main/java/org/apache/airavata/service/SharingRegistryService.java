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
package org.apache.airavata.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import org.apache.airavata.sharing.registry.db.entities.*;
import org.apache.airavata.sharing.registry.db.repositories.*;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharingRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryService.class);

    public static String OWNER_PERMISSION_NAME = "OWNER";

    /**
     * * Domain Operations
     * *
     */
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException {
        if ((new DomainRepository()).get(domain.getDomainId()) != null)
            throw new DuplicateEntryException("There exist domain with given domain id");

        domain.setCreatedTime(System.currentTimeMillis());
        domain.setUpdatedTime(System.currentTimeMillis());
        (new DomainRepository()).create(domain);

        // create the global permission for the domain
        PermissionType permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domain.getDomainId() + ":" + OWNER_PERMISSION_NAME);
        permissionType.setDomainId(domain.getDomainId());
        permissionType.setName(OWNER_PERMISSION_NAME);
        permissionType.setDescription("GLOBAL permission to " + domain.getDomainId());
        permissionType.setCreatedTime(System.currentTimeMillis());
        permissionType.setUpdatedTime(System.currentTimeMillis());
        (new PermissionTypeRepository()).create(permissionType);

        return domain.getDomainId();
    }

    public boolean updateDomain(Domain domain) throws SharingRegistryException {
        Domain oldDomain = (new DomainRepository()).get(domain.getDomainId());
        domain.setCreatedTime(oldDomain.getCreatedTime());
        domain.setUpdatedTime(System.currentTimeMillis());
        domain = getUpdatedObject(oldDomain, domain);
        (new DomainRepository()).update(domain);
        return true;
    }

    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        return (new DomainRepository()).isExists(domainId);
    }

    public boolean deleteDomain(String domainId) throws SharingRegistryException {
        (new DomainRepository()).delete(domainId);
        return true;
    }

    public Domain getDomain(String domainId) throws SharingRegistryException {
        return (new DomainRepository()).get(domainId);
    }

    public List<Domain> getDomains(int offset, int limit) throws SharingRegistryException {
        return (new DomainRepository()).select(new HashMap<>(), offset, limit);
    }

    /**
     * * User Operations
     * *
     */
    public String createUser(User user) throws SharingRegistryException, DuplicateEntryException {
        UserPK userPK = new UserPK();
        userPK.setUserId(user.getUserId());
        userPK.setDomainId(user.getDomainId());
        if ((new UserRepository()).get(userPK) != null)
            throw new DuplicateEntryException("There exist user with given user id");

        user.setCreatedTime(System.currentTimeMillis());
        user.setUpdatedTime(System.currentTimeMillis());
        (new UserRepository()).create(user);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(user.getUserId());
        userGroup.setDomainId(user.getDomainId());
        userGroup.setName(user.getUserName());
        userGroup.setDescription("user " + user.getUserName() + " group");
        userGroup.setOwnerId(user.getUserId());
        userGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
        userGroup.setGroupCardinality(GroupCardinality.SINGLE_USER);
        (new UserGroupRepository()).create(userGroup);

        Domain domain = new DomainRepository().get(user.getDomainId());
        if (domain.getInitialUserGroupId() != null) {
            addUsersToGroup(
                    user.getDomainId(),
                    Collections.singletonList(user.getUserId()),
                    domain.getInitialUserGroupId());
        }

        return user.getUserId();
    }

    public boolean updatedUser(User user) throws SharingRegistryException {
        UserPK userPK = new UserPK();
        userPK.setUserId(user.getUserId());
        userPK.setDomainId(user.getDomainId());
        User oldUser = (new UserRepository()).get(userPK);
        user.setCreatedTime(oldUser.getCreatedTime());
        user.setUpdatedTime(System.currentTimeMillis());
        user = getUpdatedObject(oldUser, user);
        (new UserRepository()).update(user);

        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(user.getUserId());
        userGroupPK.setDomainId(user.getDomainId());
        UserGroup userGroup = (new UserGroupRepository()).get(userGroupPK);
        userGroup.setName(user.getUserName());
        userGroup.setDescription("user " + user.getUserName() + " group");
        updateGroup(userGroup);
        return true;
    }

    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException {
        UserPK userPK = new UserPK();
        userPK.setDomainId(domainId);
        userPK.setUserId(userId);
        return (new UserRepository()).isExists(userPK);
    }

    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException {
        UserPK userPK = new UserPK();
        userPK.setUserId(userId);
        userPK.setDomainId(domainId);
        (new UserRepository()).delete(userPK);

        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(userId);
        userGroupPK.setDomainId(domainId);
        (new UserGroupRepository()).delete(userGroupPK);
        return true;
    }

    public User getUser(String domainId, String userId) throws SharingRegistryException {
        UserPK userPK = new UserPK();
        userPK.setUserId(userId);
        userPK.setDomainId(domainId);
        return (new UserRepository()).get(userPK);
    }

    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
        return (new UserRepository()).select(filters, offset, limit);
    }

    /**
     * * Group Operations
     * *
     */
    public String createGroup(UserGroup group) throws SharingRegistryException {
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(group.getGroupId());
        userGroupPK.setDomainId(group.getDomainId());
        if ((new UserGroupRepository()).get(userGroupPK) != null)
            throw new SharingRegistryException("There exist group with given group id");
        // Client created groups are always of type MULTI_USER
        group.setGroupCardinality(GroupCardinality.MULTI_USER);
        group.setCreatedTime(System.currentTimeMillis());
        group.setUpdatedTime(System.currentTimeMillis());
        // Add group admins once the group is created
        group.unsetGroupAdmins();
        (new UserGroupRepository()).create(group);

        addUsersToGroup(group.getDomainId(), Arrays.asList(group.getOwnerId()), group.getGroupId());
        return group.getGroupId();
    }

    public boolean updateGroup(UserGroup group) throws SharingRegistryException {
        group.setUpdatedTime(System.currentTimeMillis());
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(group.getGroupId());
        userGroupPK.setDomainId(group.getDomainId());
        UserGroup oldGroup = (new UserGroupRepository()).get(userGroupPK);
        group.setGroupCardinality(oldGroup.getGroupCardinality());
        group.setCreatedTime(oldGroup.getCreatedTime());
        group = getUpdatedObject(oldGroup, group);

        if (!group.getOwnerId().equals(oldGroup.getOwnerId()))
            throw new SharingRegistryException("Group owner cannot be changed");

        (new UserGroupRepository()).update(group);
        return true;
    }

    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException {
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setDomainId(domainId);
        userGroupPK.setGroupId(groupId);
        return (new UserGroupRepository()).isExists(userGroupPK);
    }

    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException {
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(groupId);
        userGroupPK.setDomainId(domainId);
        (new UserGroupRepository()).delete(userGroupPK);
        return true;
    }

    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException {
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(groupId);
        userGroupPK.setDomainId(domainId);
        return (new UserGroupRepository()).get(userGroupPK);
    }

    public List<UserGroup> getGroups(String domain, int offset, int limit) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.UserGroupTable.DOMAIN_ID, domain);
        // Only return groups with MULTI_USER cardinality which is the only type of cardinality allowed for client
        // created groups
        filters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
        return (new UserGroupRepository()).select(filters, offset, limit);
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        for (int i = 0; i < userIds.size(); i++) {
            GroupMembership groupMembership = new GroupMembership();
            groupMembership.setParentId(groupId);
            groupMembership.setChildId(userIds.get(i));
            groupMembership.setChildType(GroupChildType.USER);
            groupMembership.setDomainId(domainId);
            groupMembership.setCreatedTime(System.currentTimeMillis());
            groupMembership.setUpdatedTime(System.currentTimeMillis());
            (new GroupMembershipRepository()).create(groupMembership);
        }
        return true;
    }

    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        for (String userId : userIds) {
            if (hasOwnerAccess(domainId, groupId, userId)) {
                throw new SharingRegistryException(
                        "List of User Ids contains Owner Id. Cannot remove owner from the group");
            }
        }

        for (int i = 0; i < userIds.size(); i++) {
            GroupMembershipPK groupMembershipPK = new GroupMembershipPK();
            groupMembershipPK.setParentId(groupId);
            groupMembershipPK.setChildId(userIds.get(i));
            groupMembershipPK.setDomainId(domainId);
            (new GroupMembershipRepository()).delete(groupMembershipPK);
        }
        return true;
    }

    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, DuplicateEntryException {
        List<User> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);
        if (!isUserBelongsToGroup(groupUser, newOwnerId)) {
            throw new SharingRegistryException("New group owner is not part of the group");
        }

        if (hasOwnerAccess(domainId, groupId, newOwnerId)) {
            throw new DuplicateEntryException("User already the current owner of the group");
        }
        // remove the new owner as Admin if present
        if (hasAdminAccess(domainId, groupId, newOwnerId)) {
            removeGroupAdmins(domainId, groupId, Arrays.asList(newOwnerId));
        }

        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(groupId);
        userGroupPK.setDomainId(domainId);
        UserGroup userGroup = (new UserGroupRepository()).get(userGroupPK);
        UserGroup newUserGroup = new UserGroup();
        newUserGroup.setUpdatedTime(System.currentTimeMillis());
        newUserGroup.setOwnerId(newOwnerId);
        newUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        newUserGroup.setCreatedTime(userGroup.getCreatedTime());
        newUserGroup = getUpdatedObject(userGroup, newUserGroup);

        (new UserGroupRepository()).update(newUserGroup);

        return true;
    }

    private boolean isUserBelongsToGroup(List<User> groupUser, String newOwnerId) {
        for (User user : groupUser) {
            if (user.getUserId().equals(newOwnerId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException, DuplicateEntryException {
        List<User> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);

        for (String adminId : adminIds) {
            if (!isUserBelongsToGroup(groupUser, adminId)) {
                throw new SharingRegistryException(
                        "Admin not the user of the group. GroupId : " + groupId + ", AdminId : " + adminId);
            }
            GroupAdminPK groupAdminPK = new GroupAdminPK();
            groupAdminPK.setGroupId(groupId);
            groupAdminPK.setAdminId(adminId);
            groupAdminPK.setDomainId(domainId);

            if ((new GroupAdminRepository()).get(groupAdminPK) != null)
                throw new DuplicateEntryException("User already an admin for the group");

            GroupAdmin admin = new GroupAdmin();
            admin.setAdminId(adminId);
            admin.setDomainId(domainId);
            admin.setGroupId(groupId);
            (new GroupAdminRepository()).create(admin);
        }
        return true;
    }

    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException {
        for (String adminId : adminIds) {
            GroupAdminPK groupAdminPK = new GroupAdminPK();
            groupAdminPK.setAdminId(adminId);
            groupAdminPK.setDomainId(domainId);
            groupAdminPK.setGroupId(groupId);
            (new GroupAdminRepository()).delete(groupAdminPK);
        }
        return true;
    }

    public boolean hasAdminAccess(String domainId, String groupId, String adminId)
            throws SharingRegistryException {
        GroupAdminPK groupAdminPK = new GroupAdminPK();
        groupAdminPK.setGroupId(groupId);
        groupAdminPK.setAdminId(adminId);
        groupAdminPK.setDomainId(domainId);

        if ((new GroupAdminRepository()).get(groupAdminPK) != null) return true;
        return false;
    }

    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId)
            throws SharingRegistryException {
        UserGroupPK userGroupPK = new UserGroupPK();
        userGroupPK.setGroupId(groupId);
        userGroupPK.setDomainId(domainId);
        UserGroup getGroup = (new UserGroupRepository()).get(userGroupPK);

        if (getGroup.getOwnerId().equals(ownerId)) return true;
        return false;
    }

    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        // TODO limit offset
        List<User> groupMemberUsers = (new GroupMembershipRepository()).getAllChildUsers(domainId, groupId);
        return groupMemberUsers;
    }

    public List<UserGroup> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        // TODO limit offset
        List<UserGroup> groupMemberGroups = (new GroupMembershipRepository()).getAllChildGroups(domainId, groupId);
        return groupMemberGroups;
    }

    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException {
        for (String childId : childIds) {
            // Todo check for cyclic dependencies
            GroupMembership groupMembership = new GroupMembership();
            groupMembership.setParentId(groupId);
            groupMembership.setChildId(childId);
            groupMembership.setChildType(GroupChildType.GROUP);
            groupMembership.setDomainId(domainId);
            groupMembership.setCreatedTime(System.currentTimeMillis());
            groupMembership.setUpdatedTime(System.currentTimeMillis());
            (new GroupMembershipRepository()).create(groupMembership);
        }
        return true;
    }

    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException {
        GroupMembershipPK groupMembershipPK = new GroupMembershipPK();
        groupMembershipPK.setParentId(groupId);
        groupMembershipPK.setChildId(childId);
        groupMembershipPK.setDomainId(domainId);
        (new GroupMembershipRepository()).delete(groupMembershipPK);
        return true;
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId)
            throws SharingRegistryException {
        GroupMembershipRepository groupMembershipRepository = new GroupMembershipRepository();
        return groupMembershipRepository.getAllMemberGroupsForUser(domainId, userId);
    }

    /**
     * * EntityType Operations
     * *
     */
    public String createEntityType(EntityType entityType)
            throws SharingRegistryException, DuplicateEntryException {
        EntityTypePK entityTypePK = new EntityTypePK();
        entityTypePK.setDomainId(entityType.getDomainId());
        entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
        if ((new EntityTypeRepository()).get(entityTypePK) != null)
            throw new DuplicateEntryException("There exist EntityType with given EntityType id");

        entityType.setCreatedTime(System.currentTimeMillis());
        entityType.setUpdatedTime(System.currentTimeMillis());
        (new EntityTypeRepository()).create(entityType);
        return entityType.getEntityTypeId();
    }

    public boolean updateEntityType(EntityType entityType) throws SharingRegistryException {
        entityType.setUpdatedTime(System.currentTimeMillis());
        EntityTypePK entityTypePK = new EntityTypePK();
        entityTypePK.setDomainId(entityType.getDomainId());
        entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
        EntityType oldEntityType = (new EntityTypeRepository()).get(entityTypePK);
        entityType.setCreatedTime(oldEntityType.getCreatedTime());
        entityType = getUpdatedObject(oldEntityType, entityType);
        (new EntityTypeRepository()).update(entityType);
        return true;
    }

    public boolean isEntityTypeExists(String domainId, String entityTypeId)
            throws SharingRegistryException {
        EntityTypePK entityTypePK = new EntityTypePK();
        entityTypePK.setDomainId(domainId);
        entityTypePK.setEntityTypeId(entityTypeId);
        return (new EntityTypeRepository()).isExists(entityTypePK);
    }

    public boolean deleteEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        EntityTypePK entityTypePK = new EntityTypePK();
        entityTypePK.setDomainId(domainId);
        entityTypePK.setEntityTypeId(entityTypeId);
        (new EntityTypeRepository()).delete(entityTypePK);
        return true;
    }

    public EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        EntityTypePK entityTypePK = new EntityTypePK();
        entityTypePK.setDomainId(domainId);
        entityTypePK.setEntityTypeId(entityTypeId);
        return (new EntityTypeRepository()).get(entityTypePK);
    }

    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.EntityTypeTable.DOMAIN_ID, domain);
        return (new EntityTypeRepository()).select(filters, offset, limit);
    }

    /**
     * * Permission Operations
     * *
     */
    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException {
        PermissionTypePK permissionTypePK = new PermissionTypePK();
        permissionTypePK.setDomainId(permissionType.getDomainId());
        permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
        if ((new PermissionTypeRepository()).get(permissionTypePK) != null)
            throw new DuplicateEntryException("There exist PermissionType with given PermissionType id");
        permissionType.setCreatedTime(System.currentTimeMillis());
        permissionType.setUpdatedTime(System.currentTimeMillis());
        (new PermissionTypeRepository()).create(permissionType);
        return permissionType.getPermissionTypeId();
    }

    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException {
        permissionType.setUpdatedTime(System.currentTimeMillis());
        PermissionTypePK permissionTypePK = new PermissionTypePK();
        permissionTypePK.setDomainId(permissionType.getDomainId());
        permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
        PermissionType oldPermissionType = (new PermissionTypeRepository()).get(permissionTypePK);
        permissionType = getUpdatedObject(oldPermissionType, permissionType);
        (new PermissionTypeRepository()).update(permissionType);
        return true;
    }

    public boolean isPermissionExists(String domainId, String permissionId)
            throws SharingRegistryException {
        PermissionTypePK permissionTypePK = new PermissionTypePK();
        permissionTypePK.setDomainId(domainId);
        permissionTypePK.setPermissionTypeId(permissionId);
        return (new PermissionTypeRepository()).isExists(permissionTypePK);
    }

    public boolean deletePermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException {
        PermissionTypePK permissionTypePK = new PermissionTypePK();
        permissionTypePK.setDomainId(domainId);
        permissionTypePK.setPermissionTypeId(permissionTypeId);
        (new PermissionTypeRepository()).delete(permissionTypePK);
        return true;
    }

    public PermissionType getPermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException {
        PermissionTypePK permissionTypePK = new PermissionTypePK();
        permissionTypePK.setDomainId(domainId);
        permissionTypePK.setPermissionTypeId(permissionTypeId);
        return (new PermissionTypeRepository()).get(permissionTypePK);
    }

    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(DBConstants.PermissionTypeTable.DOMAIN_ID, domain);
        return (new PermissionTypeRepository()).select(filters, offset, limit);
    }

    /**
     * * Entity Operations
     * *
     */
    public String createEntity(Entity entity) throws SharingRegistryException, DuplicateEntryException {
        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(entity.getDomainId());
        entityPK.setEntityId(entity.getEntityId());
        if ((new EntityRepository()).get(entityPK) != null)
            throw new DuplicateEntryException("There exist Entity with given Entity id");

        UserPK userPK = new UserPK();
        userPK.setDomainId(entity.getDomainId());
        userPK.setUserId(entity.getOwnerId());
        if (!(new UserRepository()).isExists(userPK)) {
            // Todo this is for Airavata easy integration. Proper thing is to throw an exception here
            User user = new User();
            user.setUserId(entity.getOwnerId());
            user.setDomainId(entity.getDomainId());
            user.setUserName(user.getUserId().split("@")[0]);

            createUser(user);
        }
        entity.setCreatedTime(System.currentTimeMillis());
        entity.setUpdatedTime(System.currentTimeMillis());

        if (entity.getOriginalEntityCreationTime() == 0) {
            entity.setOriginalEntityCreationTime(entity.getCreatedTime());
        }
        (new EntityRepository()).create(entity);

        // Assigning global permission for the owner
        Sharing newSharing = new Sharing();
        newSharing.setPermissionTypeId(
                (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(entity.getDomainId()));
        newSharing.setEntityId(entity.getEntityId());
        newSharing.setGroupId(entity.getOwnerId());
        newSharing.setSharingType(SharingType.DIRECT_CASCADING);
        newSharing.setInheritedParentId(entity.getEntityId());
        newSharing.setDomainId(entity.getDomainId());
        newSharing.setCreatedTime(System.currentTimeMillis());
        newSharing.setUpdatedTime(System.currentTimeMillis());

        (new SharingRepository()).create(newSharing);

        // creating records for inherited permissions
        if (entity.getParentEntityId() != null && entity.getParentEntityId() != "") {
            addCascadingPermissionsForEntity(entity);
        }

        return entity.getEntityId();
    }

    private void addCascadingPermissionsForEntity(Entity entity) throws SharingRegistryException {
        Sharing newSharing;
        List<Sharing> sharings = (new SharingRepository())
                .getCascadingPermissionsForEntity(entity.getDomainId(), entity.getParentEntityId());
        for (Sharing sharing : sharings) {
            newSharing = new Sharing();
            newSharing.setPermissionTypeId(sharing.getPermissionTypeId());
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(sharing.getGroupId());
            newSharing.setInheritedParentId(sharing.getInheritedParentId());
            newSharing.setSharingType(SharingType.INDIRECT_CASCADING);
            newSharing.setDomainId(entity.getDomainId());
            newSharing.setCreatedTime(System.currentTimeMillis());
            newSharing.setUpdatedTime(System.currentTimeMillis());

            (new SharingRepository()).create(newSharing);
        }
    }

    public boolean updateEntity(Entity entity) throws SharingRegistryException {
        // TODO Check for permission changes
        entity.setUpdatedTime(System.currentTimeMillis());
        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(entity.getDomainId());
        entityPK.setEntityId(entity.getEntityId());
        Entity oldEntity = (new EntityRepository()).get(entityPK);
        entity.setCreatedTime(oldEntity.getCreatedTime());
        // check if parent entity changed and re-add inherited permissions
        if (!Objects.equals(oldEntity.getParentEntityId(), entity.getParentEntityId())) {
            logger.debug("Parent entity changed for {}, updating inherited permissions", entity.getEntityId());
            if (oldEntity.getParentEntityId() != null && oldEntity.getParentEntityId() != "") {
                logger.debug(
                        "Removing inherited permissions from {} that were inherited from parent {}",
                        entity.getEntityId(),
                        oldEntity.getParentEntityId());
                (new SharingRepository())
                        .removeAllIndirectCascadingPermissionsForEntity(entity.getDomainId(), entity.getEntityId());
            }
            if (entity.getParentEntityId() != null && entity.getParentEntityId() != "") {
                // re-add INDIRECT_CASCADING permissions
                logger.debug(
                        "Adding inherited permissions to {} that are inherited from parent {}",
                        entity.getEntityId(),
                        entity.getParentEntityId());
                addCascadingPermissionsForEntity(entity);
            }
        }
        entity = getUpdatedObject(oldEntity, entity);
        entity.setSharedCount((new SharingRepository()).getSharedCount(entity.getDomainId(), entity.getEntityId()));
        (new EntityRepository()).update(entity);
        return true;
    }

    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException {
        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(domainId);
        entityPK.setEntityId(entityId);
        return (new EntityRepository()).isExists(entityPK);
    }

    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException {
        // TODO Check for permission changes
        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(domainId);
        entityPK.setEntityId(entityId);
        (new EntityRepository()).delete(entityPK);
        return true;
    }

    public Entity getEntity(String domainId, String entityId) throws SharingRegistryException {
        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(domainId);
        entityPK.setEntityId(entityId);
        return (new EntityRepository()).get(entityPK);
    }

    public List<Entity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        List<String> groupIds = new ArrayList<>();
        groupIds.add(userId);
        (new GroupMembershipRepository())
                .getAllParentMembershipsForChild(domainId, userId).stream()
                        .forEach(gm -> groupIds.add(gm.getParentId()));
        return (new EntityRepository()).searchEntities(domainId, groupIds, filters, offset, limit);
    }

    public List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return (new UserRepository()).getAccessibleUsers(domainId, entityId, permissionTypeId);
    }

    public List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return (new UserRepository()).getDirectlyAccessibleUsers(domainId, entityId, permissionTypeId);
    }

    public List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return (new UserGroupRepository()).getAccessibleGroups(domainId, entityId, permissionTypeId);
    }

    public List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        return (new UserGroupRepository()).getDirectlyAccessibleGroups(domainId, entityId, permissionTypeId);
    }

    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException {
        return shareEntity(domainId, entityId, userList, permissionTypeId, cascadePermission);
    }

    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        return shareEntity(domainId, entityId, groupList, permissionTypeId, cascadePermission);
    }

    private boolean shareEntity(
            String domainId,
            String entityId,
            List<String> groupOrUserList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
        }

        List<Sharing> sharings = new ArrayList<>();

        // Adding permission for the specified users/groups for the specified entity
        LinkedList<Entity> temp = new LinkedList<>();
        for (String userId : groupOrUserList) {
            Sharing sharing = new Sharing();
            sharing.setPermissionTypeId(permissionTypeId);
            sharing.setEntityId(entityId);
            sharing.setGroupId(userId);
            sharing.setInheritedParentId(entityId);
            sharing.setDomainId(domainId);
            if (cascadePermission) {
                sharing.setSharingType(SharingType.DIRECT_CASCADING);
            } else {
                sharing.setSharingType(SharingType.DIRECT_NON_CASCADING);
            }
            sharing.setCreatedTime(System.currentTimeMillis());
            sharing.setUpdatedTime(System.currentTimeMillis());

            sharings.add(sharing);
        }

        if (cascadePermission) {
            // Adding permission for the specified users/groups for all child entities
            (new EntityRepository())
                    .getChildEntities(domainId, entityId).stream().forEach(e -> temp.addLast(e));
            while (temp.size() > 0) {
                Entity entity = temp.pop();
                String childEntityId = entity.getEntityId();
                for (String userId : groupOrUserList) {
                    Sharing sharing = new Sharing();
                    sharing.setPermissionTypeId(permissionTypeId);
                    sharing.setEntityId(childEntityId);
                    sharing.setGroupId(userId);
                    sharing.setInheritedParentId(entityId);
                    sharing.setSharingType(SharingType.INDIRECT_CASCADING);
                    sharing.setInheritedParentId(entityId);
                    sharing.setDomainId(domainId);
                    sharing.setCreatedTime(System.currentTimeMillis());
                    sharing.setUpdatedTime(System.currentTimeMillis());
                    sharings.add(sharing);
                    (new EntityRepository())
                            .getChildEntities(domainId, childEntityId).stream()
                                    .forEach(e -> temp.addLast(e));
                }
            }
        }
        (new SharingRepository()).create(sharings);

        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(domainId);
        entityPK.setEntityId(entityId);
        Entity entity = (new EntityRepository()).get(entityPK);
        entity.setSharedCount((new SharingRepository()).getSharedCount(domainId, entityId));
        (new EntityRepository()).update(entity);
        return true;
    }

    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
        }
        return revokeEntitySharing(domainId, entityId, userList, permissionTypeId);
    }

    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
        }
        return revokeEntitySharing(domainId, entityId, groupList, permissionTypeId);
    }

    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        // check whether the user has permission directly or indirectly
        List<GroupMembership> parentMemberships =
                (new GroupMembershipRepository()).getAllParentMembershipsForChild(domainId, userId);
        List<String> groupIds = new ArrayList<>();
        parentMemberships.stream().forEach(pm -> groupIds.add(pm.getParentId()));
        groupIds.add(userId);
        return (new SharingRepository())
                .hasAccess(
                        domainId,
                        entityId,
                        groupIds,
                        Arrays.asList(
                                permissionTypeId,
                                (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId)));
    }

    public boolean revokeEntitySharing(
            String domainId, String entityId, List<String> groupOrUserList, String permissionTypeId)
            throws SharingRegistryException {
        if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
            throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be removed");
        }

        // revoking permission for the entity
        for (String groupId : groupOrUserList) {
            SharingPK sharingPK = new SharingPK();
            sharingPK.setEntityId(entityId);
            sharingPK.setGroupId(groupId);
            sharingPK.setPermissionTypeId(permissionTypeId);
            sharingPK.setInheritedParentId(entityId);
            sharingPK.setDomainId(domainId);

            (new SharingRepository()).delete(sharingPK);
        }

        // revoking permission from inheritance
        List<Sharing> temp = new ArrayList<>();
        (new SharingRepository())
                .getIndirectSharedChildren(domainId, entityId, permissionTypeId).stream()
                        .forEach(s -> temp.add(s));
        for (Sharing sharing : temp) {
            String childEntityId = sharing.getEntityId();
            for (String groupId : groupOrUserList) {
                SharingPK sharingPK = new SharingPK();
                sharingPK.setEntityId(childEntityId);
                sharingPK.setGroupId(groupId);
                sharingPK.setPermissionTypeId(permissionTypeId);
                sharingPK.setInheritedParentId(entityId);
                sharingPK.setDomainId(domainId);

                (new SharingRepository()).delete(sharingPK);
            }
        }

        EntityPK entityPK = new EntityPK();
        entityPK.setDomainId(domainId);
        entityPK.setEntityId(entityId);
        Entity entity = (new EntityRepository()).get(entityPK);
        entity.setSharedCount((new SharingRepository()).getSharedCount(domainId, entityId));
        (new EntityRepository()).update(entity);
        return true;
    }

    private <T> T getUpdatedObject(T oldEntity, T newEntity) throws SharingRegistryException {
        Field[] newEntityFields = newEntity.getClass().getDeclaredFields();
        Hashtable newHT = fieldsToHT(newEntityFields, newEntity);

        Class oldEntityClass = oldEntity.getClass();
        Field[] oldEntityFields = oldEntityClass.getDeclaredFields();

        for (Field field : oldEntityFields) {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                Object o = newHT.get(field.getName());
                if (o != null) {
                    Field f = null;
                    try {
                        f = oldEntityClass.getDeclaredField(field.getName());
                        f.setAccessible(true);
                        logger.debug("setting " + f.getName());
                        f.set(oldEntity, o);
                    } catch (Exception e) {
                        throw new SharingRegistryException(e.getMessage());
                    }
                }
            }
        }
        return oldEntity;
    }

    private static Hashtable<String, Object> fieldsToHT(Field[] fields, Object obj) {
        Hashtable<String, Object> hashtable = new Hashtable<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object retrievedObject = field.get(obj);
                if (retrievedObject != null) {
                    logger.debug("scanning " + field.getName());
                    hashtable.put(field.getName(), field.get(obj));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return hashtable;
    }
}

