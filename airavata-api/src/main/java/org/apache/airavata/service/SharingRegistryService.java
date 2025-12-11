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
import org.apache.airavata.sharing.entities.*;
import org.apache.airavata.sharing.models.*;
import org.apache.airavata.sharing.services.DomainService;
import org.apache.airavata.sharing.services.EntityService;
import org.apache.airavata.sharing.services.EntityTypeService;
import org.apache.airavata.sharing.services.GroupAdminService;
import org.apache.airavata.sharing.services.GroupMembershipService;
import org.apache.airavata.sharing.services.PermissionTypeService;
import org.apache.airavata.sharing.services.SharingService;
import org.apache.airavata.sharing.services.UserGroupService;
import org.apache.airavata.sharing.services.UserService;
import org.apache.airavata.sharing.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "services.sharingRegistryService.enabled",
        havingValue = "true",
        matchIfMissing = true)
    public class SharingRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryService.class);

    private final EntityService entityService;
    private final DomainService domainService;
    private final EntityTypeService entityTypeService;
    private final PermissionTypeService permissionTypeService;

    @Qualifier("sharingUserService")
    private final UserService userService;

    private final UserGroupService userGroupService;
    private final GroupMembershipService groupMembershipService;
    private final SharingService sharingService;
    private final GroupAdminService groupAdminService;

    public SharingRegistryService(
            EntityService entityService,
            DomainService domainService,
            EntityTypeService entityTypeService,
            PermissionTypeService permissionTypeService,
            @Qualifier("sharingUserService") UserService userService,
            UserGroupService userGroupService,
            GroupMembershipService groupMembershipService,
            SharingService sharingService,
            GroupAdminService groupAdminService) {
        this.entityService = entityService;
        this.domainService = domainService;
        this.entityTypeService = entityTypeService;
        this.permissionTypeService = permissionTypeService;
        this.userService = userService;
        this.userGroupService = userGroupService;
        this.groupMembershipService = groupMembershipService;
        this.sharingService = sharingService;
        this.groupAdminService = groupAdminService;
    }

    public static String OWNER_PERMISSION_NAME = "OWNER";

    /**
     * * Domain Operations
     * *
     */
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException {
        try {
            if (domainService.get(domain.getDomainId()) != null)
                throw new DuplicateEntryException("There exist domain with given domain id");

            domain.setCreatedTime(System.currentTimeMillis());
            domain.setUpdatedTime(System.currentTimeMillis());
            domainService.create(domain);

            // create the global permission for the domain
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":" + OWNER_PERMISSION_NAME);
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName(OWNER_PERMISSION_NAME);
            permissionType.setDescription("GLOBAL permission to " + domain.getDomainId());
            permissionType.setCreatedTime(System.currentTimeMillis());
            permissionType.setUpdatedTime(System.currentTimeMillis());
            permissionTypeService.create(permissionType);

            return domain.getDomainId();
        } catch (SharingRegistryException e) {
            String message = String.format("Error while creating domain: domainId=%s", domain.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updateDomain(Domain domain) throws SharingRegistryException {
        try {
            Domain oldDomain = domainService.get(domain.getDomainId());
            domain.setCreatedTime(oldDomain.getCreatedTime());
            domain.setUpdatedTime(System.currentTimeMillis());
            domain = getUpdatedObject(oldDomain, domain);
            domainService.update(domain);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while updating domain: domainId=%s", domain.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        try {
            return domainService.isExists(domainId);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while checking if domain exists: domainId=%s", domainId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deleteDomain(String domainId) throws SharingRegistryException {
        try {
            domainService.delete(domainId);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting domain: domainId=%s", domainId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public Domain getDomain(String domainId) throws SharingRegistryException {
        try {
            return domainService.get(domainId);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting domain: domainId=%s", domainId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<Domain> getDomains(int offset, int limit) throws SharingRegistryException {
        try {
            return domainService.getAll();
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting domains: offset=%d, limit=%d", offset, limit);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * * User Operations
     * *
     */
    public String createUser(User user) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(user.getUserId());
            userPK.setDomainId(user.getDomainId());
            if (userService.get(userPK) != null)
                throw new SharingRegistryException("There exist user with given user id");

            user.setCreatedTime(System.currentTimeMillis());
            user.setUpdatedTime(System.currentTimeMillis());
            userService.create(user);

            UserGroup userGroup = new UserGroup();
            userGroup.setGroupId(user.getUserId());
            userGroup.setDomainId(user.getDomainId());
            userGroup.setName(user.getUserName());
            userGroup.setDescription("user " + user.getUserName() + " group");
            userGroup.setOwnerId(user.getUserId());
            userGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            userGroup.setGroupCardinality(GroupCardinality.SINGLE_USER);
            userGroupService.create(userGroup);

            Domain domain = domainService.get(user.getDomainId());
            if (domain.getInitialUserGroupId() != null) {
                addUsersToGroup(
                        user.getDomainId(),
                        Collections.singletonList(user.getUserId()),
                        domain.getInitialUserGroupId());
            }

            return user.getUserId();
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating user: userId=%s, domainId=%s", user.getUserId(), user.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updatedUser(User user) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(user.getUserId());
            userPK.setDomainId(user.getDomainId());
            User oldUser = userService.get(userPK);
            user.setCreatedTime(oldUser.getCreatedTime());
            user.setUpdatedTime(System.currentTimeMillis());
            user = getUpdatedObject(oldUser, user);
            userService.update(user);

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(user.getUserId());
            userGroupPK.setDomainId(user.getDomainId());
            UserGroup userGroup = userGroupService.get(userGroupPK);
            userGroup.setName(user.getUserName());
            userGroup.setDescription("user " + user.getUserName() + " group");
            updateGroup(userGroup);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating user: userId=%s, domainId=%s", user.getUserId(), user.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setDomainId(domainId);
            userPK.setUserId(userId);
            return userService.isExists(userPK);
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while checking if user exists: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(userId);
            userPK.setDomainId(domainId);
            userService.delete(userPK);

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(userId);
            userGroupPK.setDomainId(domainId);
            userGroupService.delete(userGroupPK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting user: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public User getUser(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(userId);
            userPK.setDomainId(domainId);
            return userService.get(userPK);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting user: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
            // Use Criteria API for dynamic filtering - simplified for now
            return userService.select(null, filters, offset, limit);
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while getting users: domain=%s, offset=%d, limit=%d", domain, offset, limit);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * * Group Operations
     * *
     */
    public String createGroup(UserGroup group) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(group.getGroupId());
            userGroupPK.setDomainId(group.getDomainId());
            if (userGroupService.get(userGroupPK) != null)
                throw new SharingRegistryException("There exist group with given group id");
            // Client created groups are always of type MULTI_USER
            group.setGroupCardinality(GroupCardinality.MULTI_USER);
            group.setCreatedTime(System.currentTimeMillis());
            group.setUpdatedTime(System.currentTimeMillis());
            // Add group admins once the group is created
            group.unsetGroupAdmins();
            userGroupService.create(group);

            addUsersToGroup(group.getDomainId(), Arrays.asList(group.getOwnerId()), group.getGroupId());
            return group.getGroupId();
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating group: groupId=%s, domainId=%s", group.getGroupId(), group.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updateGroup(UserGroup group) throws SharingRegistryException {
        try {
            group.setUpdatedTime(System.currentTimeMillis());
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(group.getGroupId());
            userGroupPK.setDomainId(group.getDomainId());
            UserGroup oldGroup = userGroupService.get(userGroupPK);
            group.setGroupCardinality(oldGroup.getGroupCardinality());
            group.setCreatedTime(oldGroup.getCreatedTime());
            group = getUpdatedObject(oldGroup, group);

            if (!group.getOwnerId().equals(oldGroup.getOwnerId()))
                throw new SharingRegistryException("Group owner cannot be changed");

            userGroupService.update(group);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating group: groupId=%s, domainId=%s", group.getGroupId(), group.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setDomainId(domainId);
            userGroupPK.setGroupId(groupId);
            return userGroupService.isExists(userGroupPK);
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while checking if group exists: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            userGroupService.delete(userGroupPK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            return userGroupService.get(userGroupPK);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<UserGroup> getGroups(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.UserGroupTable.DOMAIN_ID, domain);
            // Only return groups with MULTI_USER cardinality which is the only type of cardinality allowed for client
            // created groups
            filters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
            return userGroupService.select(null, filters, offset, limit);
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while getting groups: domain=%s, offset=%d, limit=%d", domain, offset, limit);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
            for (int i = 0; i < userIds.size(); i++) {
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setParentId(groupId);
                groupMembership.setChildId(userIds.get(i));
                groupMembership.setChildType(GroupChildType.USER);
                groupMembership.setDomainId(domainId);
                groupMembership.setCreatedTime(System.currentTimeMillis());
                groupMembership.setUpdatedTime(System.currentTimeMillis());
                groupMembershipService.create(groupMembership);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while adding users to group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean removeUsersFromGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
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
                groupMembershipService.delete(groupMembershipPK);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while removing users from group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException, DuplicateEntryException {
        try {
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
            UserGroup userGroup = userGroupService.get(userGroupPK);
            UserGroup newUserGroup = new UserGroup();
            newUserGroup.setUpdatedTime(System.currentTimeMillis());
            newUserGroup.setOwnerId(newOwnerId);
            newUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
            newUserGroup.setCreatedTime(userGroup.getCreatedTime());
            newUserGroup = getUpdatedObject(userGroup, newUserGroup);

            userGroupService.update(newUserGroup);

            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while transferring group ownership: domainId=%s, groupId=%s, newOwnerId=%s",
                    domainId, groupId, newOwnerId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
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
        try {
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

                if (groupAdminService.get(groupAdminPK) != null)
                    throw new DuplicateEntryException("User already an admin for the group");

                GroupAdmin admin = new GroupAdmin();
                admin.setAdminId(adminId);
                admin.setDomainId(domainId);
                admin.setGroupId(groupId);
                groupAdminService.create(admin);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while adding group admins: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean removeGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException {
        try {
            for (String adminId : adminIds) {
                GroupAdminPK groupAdminPK = new GroupAdminPK();
                groupAdminPK.setAdminId(adminId);
                groupAdminPK.setDomainId(domainId);
                groupAdminPK.setGroupId(groupId);
                groupAdminService.delete(groupAdminPK);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message =
                    String.format("Error while removing group admins: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean hasAdminAccess(String domainId, String groupId, String adminId) throws SharingRegistryException {
        try {
            GroupAdminPK groupAdminPK = new GroupAdminPK();
            groupAdminPK.setGroupId(groupId);
            groupAdminPK.setAdminId(adminId);
            groupAdminPK.setDomainId(domainId);

            if (groupAdminService.get(groupAdminPK) != null) return true;
            return false;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking admin access: domainId=%s, groupId=%s, adminId=%s",
                    domainId, groupId, adminId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            UserGroup getGroup = userGroupService.get(userGroupPK);

            if (getGroup.getOwnerId().equals(ownerId)) return true;
            return false;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking owner access: domainId=%s, groupId=%s, ownerId=%s",
                    domainId, groupId, ownerId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        try {
            // TODO limit offset
            List<User> groupMemberUsers = groupMembershipService.getAllChildUsers(domainId, groupId);
            return groupMemberUsers;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting group members of type user: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<UserGroup> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        try {
            // TODO limit offset
            List<UserGroup> groupMemberGroups = groupMembershipService.getAllChildGroups(domainId, groupId);
            return groupMemberGroups;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting group members of type group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException {
        try {
            for (String childId : childIds) {
                // Todo check for cyclic dependencies
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setParentId(groupId);
                groupMembership.setChildId(childId);
                groupMembership.setChildType(GroupChildType.GROUP);
                groupMembership.setDomainId(domainId);
                groupMembership.setCreatedTime(System.currentTimeMillis());
                groupMembership.setUpdatedTime(System.currentTimeMillis());
                groupMembershipService.create(groupMembership);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while adding child groups to parent group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException {
        try {
            GroupMembershipPK groupMembershipPK = new GroupMembershipPK();
            groupMembershipPK.setParentId(groupId);
            groupMembershipPK.setChildId(childId);
            groupMembershipPK.setDomainId(domainId);
            groupMembershipService.delete(groupMembershipPK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while removing child group from parent group: domainId=%s, childId=%s, groupId=%s",
                    domainId, childId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        try {
            return groupMembershipService.getAllMemberGroupsForUser(domainId, userId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting all member groups for user: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * * EntityType Operations
     * *
     */
    public String createEntityType(EntityType entityType) throws SharingRegistryException, DuplicateEntryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(entityType.getDomainId());
            entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
            if (entityTypeService.get(entityTypePK) != null)
                throw new DuplicateEntryException("There exist EntityType with given EntityType id");

            entityType.setCreatedTime(System.currentTimeMillis());
            entityType.setUpdatedTime(System.currentTimeMillis());
            entityTypeService.create(entityType);
            return entityType.getEntityTypeId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating entity type: entityTypeId=%s, domainId=%s",
                    entityType.getEntityTypeId(), entityType.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updateEntityType(EntityType entityType) throws SharingRegistryException {
        try {
            entityType.setUpdatedTime(System.currentTimeMillis());
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(entityType.getDomainId());
            entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
            EntityType oldEntityType = entityTypeService.get(entityTypePK);
            entityType.setCreatedTime(oldEntityType.getCreatedTime());
            entityType = getUpdatedObject(oldEntityType, entityType);
            entityTypeService.update(entityType);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating entity type: entityTypeId=%s, domainId=%s",
                    entityType.getEntityTypeId(), entityType.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isEntityTypeExists(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            return entityTypeService.isExists(entityTypePK);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking if entity type exists: domainId=%s, entityTypeId=%s", domainId, entityTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deleteEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            entityTypeService.delete(entityTypePK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while deleting entity type: domainId=%s, entityTypeId=%s", domainId, entityTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public EntityType getEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            return entityTypeService.get(entityTypePK);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting entity type: domainId=%s, entityTypeId=%s", domainId, entityTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<EntityType> getEntityTypes(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.EntityTypeTable.DOMAIN_ID, domain);
            return entityTypeService.select(filters, offset, limit);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting entity types: domain=%s, offset=%d, limit=%d", domain, offset, limit);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * * Permission Operations
     * *
     */
    public String createPermissionType(PermissionType permissionType)
            throws SharingRegistryException, DuplicateEntryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(permissionType.getDomainId());
            permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
            if (permissionTypeService.get(permissionTypePK) != null)
                throw new DuplicateEntryException("There exist PermissionType with given PermissionType id");
            permissionType.setCreatedTime(System.currentTimeMillis());
            permissionType.setUpdatedTime(System.currentTimeMillis());
            permissionTypeService.create(permissionType);
            return permissionType.getPermissionTypeId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating permission type: permissionTypeId=%s, domainId=%s",
                    permissionType.getPermissionTypeId(), permissionType.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updatePermissionType(PermissionType permissionType) throws SharingRegistryException {
        try {
            permissionType.setUpdatedTime(System.currentTimeMillis());
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(permissionType.getDomainId());
            permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
            PermissionType oldPermissionType = permissionTypeService.get(permissionTypePK);
            permissionType = getUpdatedObject(oldPermissionType, permissionType);
            permissionTypeService.update(permissionType);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating permission type: permissionTypeId=%s, domainId=%s",
                    permissionType.getPermissionTypeId(), permissionType.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isPermissionExists(String domainId, String permissionId) throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionId);
            return permissionTypeService.isExists(permissionTypePK);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking if permission exists: domainId=%s, permissionId=%s", domainId, permissionId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deletePermissionType(String domainId, String permissionTypeId) throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionTypeId);
            permissionTypeService.delete(permissionTypePK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while deleting permission type: domainId=%s, permissionTypeId=%s",
                    domainId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public PermissionType getPermissionType(String domainId, String permissionTypeId) throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionTypeId);
            return permissionTypeService.get(permissionTypePK);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting permission type: domainId=%s, permissionTypeId=%s",
                    domainId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<PermissionType> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.PermissionTypeTable.DOMAIN_ID, domain);
            return permissionTypeService.select(filters, offset, limit);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting permission types: domain=%s, offset=%d, limit=%d", domain, offset, limit);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * * Entity Operations
     * *
     */
    public String createEntity(Entity entity) throws SharingRegistryException, DuplicateEntryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(entity.getDomainId());
            entityPK.setEntityId(entity.getEntityId());
            if (entityService.get(entityPK) != null)
                throw new DuplicateEntryException("There exist Entity with given Entity id");

            UserPK userPK = new UserPK();
            userPK.setDomainId(entity.getDomainId());
            userPK.setUserId(entity.getOwnerId());
            if (!userService.isExists(userPK)) {
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
            entityService.create(entity);

            // Assigning global permission for the owner
            Sharing newSharing = new Sharing();
            newSharing.setPermissionTypeId(
                    permissionTypeService.getOwnerPermissionTypeIdForDomain(entity.getDomainId()));
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(entity.getOwnerId());
            newSharing.setSharingType(SharingType.DIRECT_CASCADING);
            newSharing.setInheritedParentId(entity.getEntityId());
            newSharing.setDomainId(entity.getDomainId());
            newSharing.setCreatedTime(System.currentTimeMillis());
            newSharing.setUpdatedTime(System.currentTimeMillis());

            sharingService.create(newSharing);

            // creating records for inherited permissions
            if (entity.getParentEntityId() != null && entity.getParentEntityId() != "") {
                addCascadingPermissionsForEntity(entity);
            }

            return entity.getEntityId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while creating entity: entityId=%s, domainId=%s, ownerId=%s",
                    entity.getEntityId(), entity.getDomainId(), entity.getOwnerId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    private void addCascadingPermissionsForEntity(Entity entity) throws SharingRegistryException {
        try {
            Sharing newSharing;
            List<Sharing> sharings =
                    sharingService.getCascadingPermissionsForEntity(entity.getDomainId(), entity.getParentEntityId());
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

                sharingService.create(newSharing);
            }
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while adding cascading permissions for entity: entityId=%s, domainId=%s, parentEntityId=%s",
                    entity.getEntityId(), entity.getDomainId(), entity.getParentEntityId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean updateEntity(Entity entity) throws SharingRegistryException {
        try {
            // TODO Check for permission changes
            entity.setUpdatedTime(System.currentTimeMillis());
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(entity.getDomainId());
            entityPK.setEntityId(entity.getEntityId());
            Entity oldEntity = entityService.get(entityPK);
            entity.setCreatedTime(oldEntity.getCreatedTime());
            // check if parent entity changed and re-add inherited permissions
            if (!Objects.equals(oldEntity.getParentEntityId(), entity.getParentEntityId())) {
                logger.debug("Parent entity changed for {}, updating inherited permissions", entity.getEntityId());
                if (oldEntity.getParentEntityId() != null && oldEntity.getParentEntityId() != "") {
                    logger.debug(
                            "Removing inherited permissions from {} that were inherited from parent {}",
                            entity.getEntityId(),
                            oldEntity.getParentEntityId());
                    sharingService.removeAllIndirectCascadingPermissionsForEntity(
                            entity.getDomainId(), entity.getEntityId());
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
            entity.setSharedCount(sharingService.getSharedCount(entity.getDomainId(), entity.getEntityId()));
            entityService.update(entity);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating entity: entityId=%s, domainId=%s",
                    entity.getEntityId(), entity.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            return entityService.isExists(entityPK);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking if entity exists: domainId=%s, entityId=%s", domainId, entityId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException {
        try {
            // TODO Check for permission changes
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            entityService.delete(entityPK);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting entity: domainId=%s, entityId=%s", domainId, entityId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public Entity getEntity(String domainId, String entityId) throws SharingRegistryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            return entityService.get(entityPK);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while getting entity: domainId=%s, entityId=%s", domainId, entityId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<Entity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        try {
            List<String> groupIds = new ArrayList<>();
            groupIds.add(userId);
            groupMembershipService.getAllParentMembershipsForChild(domainId, userId).stream()
                    .forEach(gm -> groupIds.add(gm.getParentId()));
            return entityService.searchEntities(domainId, groupIds, filters, offset, limit);
        } catch (SharingRegistryException e) {
            String message = String.format("Error while searching entities: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<User> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return userService.getAccessibleUsers(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting list of shared users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<User> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return userService.getDirectlyAccessibleUsers(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting list of directly shared users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<UserGroup> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return userGroupService.getAccessibleGroups(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting list of shared groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public List<UserGroup> getListOfDirectlySharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return userGroupService.getDirectlyAccessibleGroups(domainId, entityId, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while getting list of directly shared groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException {
        try {
            return shareEntity(domainId, entityId, userList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while sharing entity with users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        try {
            return shareEntity(domainId, entityId, groupList, permissionTypeId, cascadePermission);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while sharing entity with groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    private boolean shareEntity(
            String domainId,
            String entityId,
            List<String> groupOrUserList,
            String permissionTypeId,
            boolean cascadePermission)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId))) {
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
                entityService.getChildEntities(domainId, entityId).stream().forEach(e -> temp.addLast(e));
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
                        entityService.getChildEntities(domainId, childEntityId).stream()
                                .forEach(e -> temp.addLast(e));
                    }
                }
            }
            for (Sharing sharing : sharings) {
                sharingService.create(sharing);
            }

            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            Entity entity = entityService.get(entityPK);
            entity.setSharedCount(sharingService.getSharedCount(domainId, entityId));
            entityService.update(entity);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while sharing entity: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharing(domainId, entityId, userList, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while revoking entity sharing from users: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharing(domainId, entityId, groupList, permissionTypeId);
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while revoking entity sharing from groups: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            // check whether the user has permission directly or indirectly
            List<GroupMembership> parentMemberships =
                    groupMembershipService.getAllParentMembershipsForChild(domainId, userId);
            List<String> groupIds = new ArrayList<>();
            parentMemberships.stream().forEach(pm -> groupIds.add(pm.getParentId()));
            groupIds.add(userId);
            return sharingService.hasAccess(
                    domainId,
                    entityId,
                    groupIds,
                    Arrays.asList(permissionTypeId, permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId)));
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while checking user access: domainId=%s, userId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, userId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean revokeEntitySharing(
            String domainId, String entityId, List<String> groupOrUserList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals(permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId))) {
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

                sharingService.delete(sharingPK);
            }

            // revoking permission from inheritance
            List<Sharing> temp = new ArrayList<>();
            sharingService.getIndirectSharedChildren(domainId, entityId, permissionTypeId).stream()
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

                    sharingService.delete(sharingPK);
                }
            }

            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            Entity entity = entityService.get(entityPK);
            entity.setSharedCount(sharingService.getSharedCount(domainId, entityId));
            entityService.update(entity);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while revoking entity sharing: domainId=%s, entityId=%s, permissionTypeId=%s",
                    domainId, entityId, permissionTypeId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
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
                    } catch (NoSuchFieldException | IllegalAccessException e) {
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
