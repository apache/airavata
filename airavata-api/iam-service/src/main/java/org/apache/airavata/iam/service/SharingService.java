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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.iam.model.*;
import org.apache.airavata.iam.repository.*;
import org.apache.airavata.iam.util.DBConstants;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.interfaces.SharingProvider;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.sharing.registry.models.proto.GroupCardinality;
import org.apache.airavata.sharing.registry.models.proto.GroupChildType;
import org.apache.airavata.sharing.registry.models.proto.GroupType;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;
import org.apache.airavata.sharing.registry.models.proto.SharingType;
import org.apache.airavata.sharing.registry.models.proto.UserGroup;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SharingService implements SharingFacade, SharingProvider {
    private static final Logger logger = LoggerFactory.getLogger(SharingService.class);

    public static String OWNER_PERMISSION_NAME = "OWNER";

    public String getAPIVersion() throws SharingRegistryException {
        return "0.1";
    }

    /**
     * * Domain Operations
     * *
     */
    public String createDomain(DomainEntity domain) throws SharingRegistryException, DuplicateEntryException {
        try {
            if ((new DomainRepository()).get(domain.getDomainId()) != null)
                throw new DuplicateEntryException("There exist domain with given domain id");

            domain.setCreatedTime(System.currentTimeMillis());
            domain.setUpdatedTime(System.currentTimeMillis());
            (new DomainRepository()).create(domain);

            // create the global permission for the domain
            PermissionTypeEntity permissionType = new PermissionTypeEntity();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":" + OWNER_PERMISSION_NAME);
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName(OWNER_PERMISSION_NAME);
            permissionType.setDescription("GLOBAL permission to " + domain.getDomainId());
            permissionType.setCreatedTime(System.currentTimeMillis());
            permissionType.setUpdatedTime(System.currentTimeMillis());
            (new PermissionTypeRepository()).create(permissionType);

            return domain.getDomainId();
        } catch (DuplicateEntryException e) {
            logger.warn("A domain with id={} already exists, skipping recreation", domain.getDomainId());
            return domain.getDomainId();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean updateDomain(DomainEntity domain) throws SharingRegistryException {
        try {
            DomainEntity oldDomain = (new DomainRepository()).get(domain.getDomainId());
            domain.setCreatedTime(oldDomain.getCreatedTime());
            domain.setUpdatedTime(System.currentTimeMillis());
            domain = getUpdatedObject(oldDomain, domain);
            (new DomainRepository()).update(domain);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check Domain Exists</p>
     *
     * @param domainId
     */
    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        try {
            return (new DomainRepository()).isExists(domainId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deleteDomain(String domainId) throws SharingRegistryException {
        try {
            (new DomainRepository()).delete(domainId);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public DomainEntity getDomain(String domainId) throws SharingRegistryException {
        try {
            return (new DomainRepository()).get(domainId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<DomainEntity> getDomains(int offset, int limit) throws SharingRegistryException {
        try {
            return (new DomainRepository()).select(new HashMap<>(), offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * User Operations
     * *
     */
    public String createUser(UserEntity user) throws SharingRegistryException, DuplicateEntryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(user.getUserId());
            userPK.setDomainId(user.getDomainId());
            if ((new UserRepository()).get(userPK) != null)
                throw new DuplicateEntryException("There exist user with given user id");

            user.setCreatedTime(System.currentTimeMillis());
            user.setUpdatedTime(System.currentTimeMillis());
            (new UserRepository()).create(user);

            UserGroupEntity userGroup = new UserGroupEntity();
            userGroup.setGroupId(user.getUserId());
            userGroup.setDomainId(user.getDomainId());
            userGroup.setName(user.getUserName());
            userGroup.setDescription("user " + user.getUserName() + " group");
            userGroup.setOwnerId(user.getUserId());
            userGroup.setGroupType(GroupType.USER_LEVEL_GROUP.name());
            userGroup.setGroupCardinality(GroupCardinality.SINGLE_USER.name());
            (new UserGroupRepository()).create(userGroup);

            DomainEntity domain = new DomainRepository().get(user.getDomainId());
            if (domain.getInitialUserGroupId() != null) {
                addUsersToGroup(
                        user.getDomainId(),
                        Collections.singletonList(user.getUserId()),
                        domain.getInitialUserGroupId());
            }

            return user.getUserId();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean updatedUser(UserEntity user) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(user.getUserId());
            userPK.setDomainId(user.getDomainId());
            UserEntity oldUser = (new UserRepository()).get(userPK);
            user.setCreatedTime(oldUser.getCreatedTime());
            user.setUpdatedTime(System.currentTimeMillis());
            user = getUpdatedObject(oldUser, user);
            (new UserRepository()).update(user);

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(user.getUserId());
            userGroupPK.setDomainId(user.getDomainId());
            UserGroupEntity userGroup = (new UserGroupRepository()).get(userGroupPK);
            userGroup.setName(user.getUserName());
            userGroup.setDescription("user " + user.getUserName() + " group");
            updateGroup(userGroup);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check User Exists</p>
     *
     * @param userId
     */
    @Override
    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setDomainId(domainId);
            userPK.setUserId(userId);
            return (new UserRepository()).isExists(userPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(userId);
            userPK.setDomainId(domainId);
            (new UserRepository()).delete(userPK);

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(userId);
            userGroupPK.setDomainId(domainId);
            (new UserGroupRepository()).delete(userGroupPK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public UserEntity getUser(String domainId, String userId) throws SharingRegistryException {
        try {
            UserPK userPK = new UserPK();
            userPK.setUserId(userId);
            userPK.setDomainId(domainId);
            return (new UserRepository()).get(userPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserEntity> getUsers(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
            return (new UserRepository()).select(filters, offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * Group Operations
     * *
     */
    public String createGroup(UserGroupEntity group) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(group.getGroupId());
            userGroupPK.setDomainId(group.getDomainId());
            if ((new UserGroupRepository()).get(userGroupPK) != null)
                throw new SharingRegistryException("There exist group with given group id");
            // Client created groups are always of type MULTI_USER
            group.setGroupCardinality(GroupCardinality.MULTI_USER.name());
            group.setCreatedTime(System.currentTimeMillis());
            group.setUpdatedTime(System.currentTimeMillis());
            // Add group admins once the group is created
            // group admins are managed separately;
            (new UserGroupRepository()).create(group);

            addUsersToGroup(group.getDomainId(), Arrays.asList(group.getOwnerId()), group.getGroupId());
            return group.getGroupId();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean updateGroup(UserGroupEntity group) throws SharingRegistryException {
        try {
            group.setUpdatedTime(System.currentTimeMillis());
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(group.getGroupId());
            userGroupPK.setDomainId(group.getDomainId());
            UserGroupEntity oldGroup = (new UserGroupRepository()).get(userGroupPK);
            group.setGroupCardinality(oldGroup.getGroupCardinality());
            group.setCreatedTime(oldGroup.getCreatedTime());
            group = getUpdatedObject(oldGroup, group);

            if (!group.getOwnerId().equals(oldGroup.getOwnerId()))
                throw new SharingRegistryException("Group owner cannot be changed");

            (new UserGroupRepository()).update(group);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Declaratively reconcile a group's roster to the desired members + admins lists, computing and
     * applying add/remove deltas server-side. This is the server-side counterpart of the thin client's
     * former member/admin diffing: {@code added = desired − current}, {@code removed = current − desired}
     * for both members and admins, and any desired admin that is not also a desired member is promoted to
     * a member (admins must belong to the group). Each non-empty delta is applied via the existing
     * add/remove member + admin operations; metadata is not touched here (the caller updates it
     * separately via updateGroup).
     *
     * @param newGroup {@code true} when the group was just created (no current roster, so all desired
     *     members/admins are added); {@code false} for an update against the existing roster.
     */
    public void reconcileGroupMembership(
            String domainId,
            String groupId,
            List<String> desiredMembers,
            List<String> desiredAdmins,
            String ownerId,
            boolean newGroup)
            throws SharingRegistryException {
        try {
            Set<String> desiredMemberSet = new LinkedHashSet<>(desiredMembers);
            Set<String> desiredAdminSet = new LinkedHashSet<>(desiredAdmins);

            // The owner is managed by createGroup and holds implicit membership/ownership; it is never
            // a settable member/admin here and must never be added (duplicate) or removed. Exclude it
            // from both the desired and current sets so the diff never targets the owner.
            if (ownerId != null && !ownerId.isEmpty()) {
                desiredMemberSet.remove(ownerId);
                desiredAdminSet.remove(ownerId);
            }

            // Admins must belong to the group: any desired admin not already a desired member is
            // promoted to a member too (mirrors the former client-side _member_admin_diff).
            Set<String> promotedMembers = new LinkedHashSet<>(desiredAdminSet);
            promotedMembers.removeAll(desiredMemberSet);
            Set<String> finalMemberSet = new LinkedHashSet<>(desiredMemberSet);
            finalMemberSet.addAll(promotedMembers);

            Set<String> currentMembers = new LinkedHashSet<>();
            Set<String> currentAdmins = new LinkedHashSet<>();
            if (!newGroup) {
                getGroupMembersOfTypeUser(domainId, groupId, 0, -1)
                        .forEach(u -> currentMembers.add(u.getUserId()));
                UserGroupEntity entity = getGroup(domainId, groupId);
                if (entity != null && entity.getGroupAdmins() != null) {
                    entity.getGroupAdmins().forEach(a -> currentAdmins.add(a.getAdminId()));
                }
                if (ownerId != null && !ownerId.isEmpty()) {
                    currentMembers.remove(ownerId);
                    currentAdmins.remove(ownerId);
                }
            }

            List<String> addedMembers = new ArrayList<>(finalMemberSet);
            addedMembers.removeAll(currentMembers);
            List<String> removedMembers = new ArrayList<>(currentMembers);
            removedMembers.removeAll(finalMemberSet);

            List<String> addedAdmins = new ArrayList<>(desiredAdminSet);
            addedAdmins.removeAll(currentAdmins);
            List<String> removedAdmins = new ArrayList<>(currentAdmins);
            removedAdmins.removeAll(desiredAdminSet);

            // Apply member adds before admin adds (an admin must already be a member), and admin
            // removals before member removals (an owner/member cannot be dropped while still an admin).
            if (!addedMembers.isEmpty()) {
                addUsersToGroup(domainId, addedMembers, groupId);
            }
            if (!removedAdmins.isEmpty()) {
                removeGroupAdmins(domainId, groupId, removedAdmins);
            }
            if (!addedAdmins.isEmpty()) {
                addGroupAdmins(domainId, groupId, addedAdmins);
            }
            if (!removedMembers.isEmpty()) {
                removeUsersFromGroup(domainId, removedMembers, groupId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * API method to check Group Exists
     * @param domainId
     * @param groupId
     * @return
     * @throws SharingRegistryException
     * @throws SharingRegistryException
     */
    public boolean isGroupExists(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setDomainId(domainId);
            userGroupPK.setGroupId(groupId);
            return (new UserGroupRepository()).isExists(userGroupPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            (new UserGroupRepository()).delete(userGroupPK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public UserGroupEntity getGroup(String domainId, String groupId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            return (new UserGroupRepository()).get(userGroupPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserGroupEntity> getGroups(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.UserGroupTable.DOMAIN_ID, domain);
            // Only return groups with MULTI_USER cardinality which is the only type of cardinality allowed for client
            // created groups
            filters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
            return (new UserGroupRepository()).select(filters, offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
            for (int i = 0; i < userIds.size(); i++) {
                GroupMembershipEntity groupMembership = new GroupMembershipEntity();
                groupMembership.setParentId(groupId);
                groupMembership.setChildId(userIds.get(i));
                groupMembership.setChildType(GroupChildType.USER.name());
                groupMembership.setDomainId(domainId);
                groupMembership.setCreatedTime(System.currentTimeMillis());
                groupMembership.setUpdatedTime(System.currentTimeMillis());
                (new GroupMembershipRepository()).create(groupMembership);
            }
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
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
                (new GroupMembershipRepository()).delete(groupMembershipPK);
            }
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean transferGroupOwnership(String domainId, String groupId, String newOwnerId)
            throws SharingRegistryException {
        try {
            List<UserEntity> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);
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
            UserGroupEntity userGroup = (new UserGroupRepository()).get(userGroupPK);
            UserGroupEntity newUserGroup = new UserGroupEntity();
            newUserGroup.setUpdatedTime(System.currentTimeMillis());
            newUserGroup.setOwnerId(newOwnerId);
            newUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER.name());
            newUserGroup.setCreatedTime(userGroup.getCreatedTime());
            newUserGroup = getUpdatedObject(userGroup, newUserGroup);

            (new UserGroupRepository()).update(newUserGroup);

            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    private boolean isUserBelongsToGroup(List<UserEntity> groupUser, String newOwnerId) {
        for (UserEntity user : groupUser) {
            if (user.getUserId().equals(newOwnerId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addGroupAdmins(String domainId, String groupId, List<String> adminIds)
            throws SharingRegistryException {
        try {
            List<UserEntity> groupUser = getGroupMembersOfTypeUser(domainId, groupId, 0, -1);

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

                GroupAdminEntity admin = new GroupAdminEntity();
                admin.setAdminId(adminId);
                admin.setDomainId(domainId);
                admin.setGroupId(groupId);
                (new GroupAdminRepository()).create(admin);
            }
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
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
                (new GroupAdminRepository()).delete(groupAdminPK);
            }
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean hasAdminAccess(String domainId, String groupId, String adminId) throws SharingRegistryException {
        try {
            GroupAdminPK groupAdminPK = new GroupAdminPK();
            groupAdminPK.setGroupId(groupId);
            groupAdminPK.setAdminId(adminId);
            groupAdminPK.setDomainId(domainId);

            if ((new GroupAdminRepository()).get(groupAdminPK) != null) return true;
            return false;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /** The caller's six group-access flags plus the group's member ids (see GroupAccessFlags). */
    public record GroupAccess(
            List<String> memberIds,
            boolean isAdmin,
            boolean isOwner,
            boolean isMember,
            boolean isGatewayAdminsGroup,
            boolean isReadOnlyGatewayAdminsGroup,
            boolean isDefaultGatewayUsersGroup) {}

    /**
     * Compose the caller's group-access flags server-side, replacing the per-flag round-trips the
     * client does today. {@code group} is the already-fetched group (its owner id drives is_owner);
     * the caller id is the qualified {@code user@gateway}.
     */
    public GroupAccess getGroupAccessFlags(String domainId, String groupId, String callerId, UserGroupEntity group)
            throws SharingRegistryException {
        List<String> memberIds = getGroupMembersOfTypeUser(domainId, groupId, 0, -1).stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList());
        GatewayGroups gg = new GatewayGroupsRepository().get(domainId);
        boolean isAdmin = hasAdminAccess(domainId, groupId, callerId);
        boolean isOwner = group.getOwnerId() != null && group.getOwnerId().equals(callerId);
        boolean isMember = memberIds.contains(callerId);
        boolean isGatewayAdminsGroup = gg != null && groupId.equals(gg.getAdminsGroupId());
        boolean isReadOnlyGatewayAdminsGroup = gg != null && groupId.equals(gg.getReadOnlyAdminsGroupId());
        boolean isDefaultGatewayUsersGroup = gg != null && groupId.equals(gg.getDefaultGatewayUsersGroupId());
        return new GroupAccess(
                memberIds,
                isAdmin,
                isOwner,
                isMember,
                isGatewayAdminsGroup,
                isReadOnlyGatewayAdminsGroup,
                isDefaultGatewayUsersGroup);
    }

    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId) throws SharingRegistryException {
        try {
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            UserGroupEntity getGroup = (new UserGroupRepository()).get(userGroupPK);

            if (getGroup.getOwnerId().equals(ownerId)) return true;
            return false;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserEntity> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        try {
            // TODO limit offset
            List<UserEntity> groupMemberUsers = (new GroupMembershipRepository()).getAllChildUsers(domainId, groupId);
            return groupMemberUsers;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserGroupEntity> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        try {
            // TODO limit offset
            List<UserGroupEntity> groupMemberGroups =
                    (new GroupMembershipRepository()).getAllChildGroups(domainId, groupId);
            return groupMemberGroups;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException {
        try {
            for (String childId : childIds) {
                // Todo check for cyclic dependencies
                GroupMembershipEntity groupMembership = new GroupMembershipEntity();
                groupMembership.setParentId(groupId);
                groupMembership.setChildId(childId);
                groupMembership.setChildType(GroupChildType.GROUP.name());
                groupMembership.setDomainId(domainId);
                groupMembership.setCreatedTime(System.currentTimeMillis());
                groupMembership.setUpdatedTime(System.currentTimeMillis());
                (new GroupMembershipRepository()).create(groupMembership);
            }
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException {
        try {
            GroupMembershipPK groupMembershipPK = new GroupMembershipPK();
            groupMembershipPK.setParentId(groupId);
            groupMembershipPK.setChildId(childId);
            groupMembershipPK.setDomainId(domainId);
            (new GroupMembershipRepository()).delete(groupMembershipPK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserGroupEntity> getAllMemberGroupEntitiesForUser(String domainId, String userId)
            throws SharingRegistryException {
        try {
            GroupMembershipRepository groupMembershipRepository = new GroupMembershipRepository();
            return groupMembershipRepository.getAllMemberGroupsForUser(domainId, userId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * EntityType Operations
     * *
     */
    public String createEntityType(EntityTypeEntity entityType)
            throws SharingRegistryException, DuplicateEntryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(entityType.getDomainId());
            entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
            if ((new EntityTypeRepository()).get(entityTypePK) != null)
                throw new DuplicateEntryException("There exist EntityType with given EntityType id");

            entityType.setCreatedTime(System.currentTimeMillis());
            entityType.setUpdatedTime(System.currentTimeMillis());
            (new EntityTypeRepository()).create(entityType);
            return entityType.getEntityTypeId();
        } catch (DuplicateEntryException e) {
            logger.warn("An entity type with id={} already exists, skipping recreation", entityType.getEntityTypeId());
            return entityType.getEntityTypeId();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean updateEntityType(EntityTypeEntity entityType) throws SharingRegistryException {
        try {
            entityType.setUpdatedTime(System.currentTimeMillis());
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(entityType.getDomainId());
            entityTypePK.setEntityTypeId(entityType.getEntityTypeId());
            EntityTypeEntity oldEntityType = (new EntityTypeRepository()).get(entityTypePK);
            entityType.setCreatedTime(oldEntityType.getCreatedTime());
            entityType = getUpdatedObject(oldEntityType, entityType);
            (new EntityTypeRepository()).update(entityType);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check EntityType Exists</p>
     *
     * @param entityTypeId
     */
    public boolean isEntityTypeExists(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            return (new EntityTypeRepository()).isExists(entityTypePK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deleteEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            (new EntityTypeRepository()).delete(entityTypePK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public EntityTypeEntity getEntityType(String domainId, String entityTypeId) throws SharingRegistryException {
        try {
            EntityTypePK entityTypePK = new EntityTypePK();
            entityTypePK.setDomainId(domainId);
            entityTypePK.setEntityTypeId(entityTypeId);
            return (new EntityTypeRepository()).get(entityTypePK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<EntityTypeEntity> getEntityTypes(String domain, int offset, int limit) throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.EntityTypeTable.DOMAIN_ID, domain);
            return (new EntityTypeRepository()).select(filters, offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * Permission Operations
     * *
     */
    public String createPermissionType(PermissionTypeEntity permissionType)
            throws SharingRegistryException, DuplicateEntryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(permissionType.getDomainId());
            permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
            if ((new PermissionTypeRepository()).get(permissionTypePK) != null)
                throw new DuplicateEntryException("There exist PermissionType with given PermissionType id");
            permissionType.setCreatedTime(System.currentTimeMillis());
            permissionType.setUpdatedTime(System.currentTimeMillis());
            (new PermissionTypeRepository()).create(permissionType);
            return permissionType.getPermissionTypeId();
        } catch (DuplicateEntryException e) {
            logger.warn(
                    "A permission type with id={} already exists, skipping recreation",
                    permissionType.getPermissionTypeId());
            return permissionType.getPermissionTypeId();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean updatePermissionType(PermissionTypeEntity permissionType) throws SharingRegistryException {
        try {
            permissionType.setUpdatedTime(System.currentTimeMillis());
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(permissionType.getDomainId());
            permissionTypePK.setPermissionTypeId(permissionType.getPermissionTypeId());
            PermissionTypeEntity oldPermissionType = (new PermissionTypeRepository()).get(permissionTypePK);
            permissionType = getUpdatedObject(oldPermissionType, permissionType);
            (new PermissionTypeRepository()).update(permissionType);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check Permission Exists</p>
     *
     * @param permissionId
     */
    public boolean isPermissionExists(String domainId, String permissionId) throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionId);
            return (new PermissionTypeRepository()).isExists(permissionTypePK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deletePermissionType(String domainId, String permissionTypeId) throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionTypeId);
            (new PermissionTypeRepository()).delete(permissionTypePK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public PermissionTypeEntity getPermissionType(String domainId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            PermissionTypePK permissionTypePK = new PermissionTypePK();
            permissionTypePK.setDomainId(domainId);
            permissionTypePK.setPermissionTypeId(permissionTypeId);
            return (new PermissionTypeRepository()).get(permissionTypePK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<PermissionTypeEntity> getPermissionTypes(String domain, int offset, int limit)
            throws SharingRegistryException {
        try {
            HashMap<String, String> filters = new HashMap<>();
            filters.put(DBConstants.PermissionTypeTable.DOMAIN_ID, domain);
            return (new PermissionTypeRepository()).select(filters, offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * * EntityEntity Operations
     * *
     */
    public String createEntity(EntityEntity entity) throws SharingRegistryException, DuplicateEntryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(entity.getDomainId());
            entityPK.setEntityId(entity.getEntityId());
            if ((new EntityRepository()).get(entityPK) != null)
                throw new DuplicateEntryException("There exist EntityEntity with given EntityEntity id");

            UserPK userPK = new UserPK();
            userPK.setDomainId(entity.getDomainId());
            userPK.setUserId(entity.getOwnerId());
            if (!(new UserRepository()).isExists(userPK)) {
                // Todo this is for Airavata easy integration. Proper thing is to throw an exception here
                UserEntity user = new UserEntity();
                user.setUserId(entity.getOwnerId());
                user.setDomainId(entity.getDomainId());
                user.setUserName(user.getUserId().split("@")[0]);

                createUser(user);
            }
            entity.setCreatedTime(System.currentTimeMillis());
            entity.setUpdatedTime(System.currentTimeMillis());

            if (entity.getOriginalEntityCreationTime() == null || entity.getOriginalEntityCreationTime() == 0) {
                entity.setOriginalEntityCreationTime(entity.getCreatedTime());
            }
            (new EntityRepository()).create(entity);

            // Assigning global permission for the owner
            SharingEntity newSharing = new SharingEntity();
            newSharing.setPermissionTypeId(
                    (new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(entity.getDomainId()));
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(entity.getOwnerId());
            newSharing.setSharingType(SharingType.DIRECT_CASCADING.name());
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
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);

            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    private void addCascadingPermissionsForEntity(EntityEntity entity) throws SharingRegistryException {
        SharingEntity newSharing;
        List<SharingEntity> sharings = (new SharingRepository())
                .getCascadingPermissionsForEntity(entity.getDomainId(), entity.getParentEntityId());
        for (SharingEntity sharing : sharings) {
            newSharing = new SharingEntity();
            newSharing.setPermissionTypeId(sharing.getPermissionTypeId());
            newSharing.setEntityId(entity.getEntityId());
            newSharing.setGroupId(sharing.getGroupId());
            newSharing.setInheritedParentId(sharing.getInheritedParentId());
            newSharing.setSharingType(SharingType.INDIRECT_CASCADING.name());
            newSharing.setDomainId(entity.getDomainId());
            newSharing.setCreatedTime(System.currentTimeMillis());
            newSharing.setUpdatedTime(System.currentTimeMillis());

            (new SharingRepository()).create(newSharing);
        }
    }

    public boolean updateEntity(EntityEntity entity) throws SharingRegistryException {
        try {
            // TODO Check for permission changes
            entity.setUpdatedTime(System.currentTimeMillis());
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(entity.getDomainId());
            entityPK.setEntityId(entity.getEntityId());
            EntityEntity oldEntity = (new EntityRepository()).get(entityPK);
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
            entity.setSharedCount(
                    (long) (new SharingRepository()).getSharedCount(entity.getDomainId(), entity.getEntityId()));
            (new EntityRepository()).update(entity);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * <p>API method to check EntityEntity Exists</p>
     *
     * @param entityId
     */
    public boolean isEntityExists(String domainId, String entityId) throws SharingRegistryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            return (new EntityRepository()).isExists(entityPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean deleteEntity(String domainId, String entityId) throws SharingRegistryException {
        try {
            // TODO Check for permission changes
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            (new EntityRepository()).delete(entityPK);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public EntityEntity getEntity(String domainId, String entityId) throws SharingRegistryException {
        try {
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(domainId);
            entityPK.setEntityId(entityId);
            return (new EntityRepository()).get(entityPK);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<EntityEntity> searchEntities(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        try {
            List<String> groupIds = new ArrayList<>();
            groupIds.add(userId);
            (new GroupMembershipRepository())
                    .getAllParentMembershipsForChild(domainId, userId).stream()
                            .forEach(gm -> groupIds.add(gm.getParentId()));
            return (new EntityRepository()).searchEntities(domainId, groupIds, filters, offset, limit);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserEntity> getListOfSharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return (new UserRepository()).getAccessibleUsers(domainId, entityId, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserEntity> getListOfDirectlySharedUsers(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return (new UserRepository()).getDirectlyAccessibleUsers(domainId, entityId, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);

            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserGroupEntity> getListOfSharedGroups(String domainId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            return (new UserGroupRepository()).getAccessibleGroups(domainId, entityId, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public List<UserGroupEntity> getListOfDirectlySharedGroups(
            String domainId, String entityId, String permissionTypeId) throws SharingRegistryException {
        try {
            return (new UserGroupRepository()).getDirectlyAccessibleGroups(domainId, entityId, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);

            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Sharing EntityEntity with Users and Groups
     * @param domainId
     * @param entityId
     * @param userList
     * @param permissionTypeId
     * @param cascadePermission
     * @return
     * @throws SharingRegistryException
     * @throws SharingRegistryException
     */
    public boolean shareEntityWithUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId, boolean cascadePermission)
            throws SharingRegistryException {
        try {
            return shareEntity(domainId, entityId, userList, permissionTypeId, cascadePermission);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
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
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
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
            if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }

            List<SharingEntity> sharings = new ArrayList<>();

            // Adding permission for the specified users/groups for the specified entity
            LinkedList<EntityEntity> temp = new LinkedList<>();
            for (String userId : groupOrUserList) {
                SharingEntity sharing = new SharingEntity();
                sharing.setPermissionTypeId(permissionTypeId);
                sharing.setEntityId(entityId);
                sharing.setGroupId(userId);
                sharing.setInheritedParentId(entityId);
                sharing.setDomainId(domainId);
                if (cascadePermission) {
                    sharing.setSharingType(SharingType.DIRECT_CASCADING.name());
                } else {
                    sharing.setSharingType(SharingType.DIRECT_NON_CASCADING.name());
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
                    EntityEntity entity = temp.pop();
                    String childEntityId = entity.getEntityId();
                    for (String userId : groupOrUserList) {
                        SharingEntity sharing = new SharingEntity();
                        sharing.setPermissionTypeId(permissionTypeId);
                        sharing.setEntityId(childEntityId);
                        sharing.setGroupId(userId);
                        sharing.setInheritedParentId(entityId);
                        sharing.setSharingType(SharingType.INDIRECT_CASCADING.name());
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
            EntityEntity entity = (new EntityRepository()).get(entityPK);
            entity.setSharedCount((long) (new SharingRepository()).getSharedCount(domainId, entityId));
            (new EntityRepository()).update(entity);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean revokeEntitySharingFromUsers(
            String domainId, String entityId, List<String> userList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharing(domainId, entityId, userList, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean revokeEntitySharingFromGroups(
            String domainId, String entityId, List<String> groupList, String permissionTypeId)
            throws SharingRegistryException {
        try {
            if (permissionTypeId.equals((new PermissionTypeRepository()).getOwnerPermissionTypeIdForDomain(domainId))) {
                throw new SharingRegistryException(OWNER_PERMISSION_NAME + " permission cannot be assigned or removed");
            }
            return revokeEntitySharing(domainId, entityId, groupList, permissionTypeId);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId)
            throws SharingRegistryException {
        try {
            // check whether the user has permission directly or indirectly
            List<GroupMembershipEntity> parentMemberships =
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
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public boolean revokeEntitySharing(
            String domainId, String entityId, List<String> groupOrUserList, String permissionTypeId)
            throws SharingRegistryException {
        try {
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
            List<SharingEntity> temp = new ArrayList<>();
            (new SharingRepository())
                    .getIndirectSharedChildren(domainId, entityId, permissionTypeId).stream()
                            .forEach(s -> temp.add(s));
            for (SharingEntity sharing : temp) {
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
            EntityEntity entity = (new EntityRepository()).get(entityPK);
            entity.setSharedCount((long) (new SharingRepository()).getSharedCount(domainId, entityId));
            (new EntityRepository()).update(entity);
            return true;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            throw new SharingRegistryException(ex.getMessage() + " Stack trace:" + ExceptionUtils.getStackTrace(ex));
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

    // =========================================================================
    // SharingFacade adapter methods
    // =========================================================================

    @Override
    public String createDomain(String domainId, String name, String description) throws Exception {
        DomainEntity domain = new DomainEntity();
        domain.setDomainId(domainId);
        domain.setName(name);
        domain.setDescription(description);
        return createDomain(domain);
    }

    @Override
    public String createEntityType(String entityTypeId, String domainId, String name, String description)
            throws Exception {
        EntityTypeEntity entityType = new EntityTypeEntity();
        entityType.setEntityTypeId(entityTypeId);
        entityType.setDomainId(domainId);
        entityType.setName(name);
        entityType.setDescription(description);
        return createEntityType(entityType);
    }

    @Override
    public String createPermissionType(String permissionTypeId, String domainId, String name, String description)
            throws Exception {
        PermissionTypeEntity permissionType = new PermissionTypeEntity();
        permissionType.setPermissionTypeId(permissionTypeId);
        permissionType.setDomainId(domainId);
        permissionType.setName(name);
        permissionType.setDescription(description);
        return createPermissionType(permissionType);
    }

    @Override
    public String createEntity(
            String entityId,
            String domainId,
            String entityTypeId,
            String ownerId,
            String name,
            String description,
            String parentEntityId)
            throws Exception {
        EntityEntity entity = new EntityEntity();
        entity.setEntityId(entityId);
        entity.setDomainId(domainId);
        entity.setEntityTypeId(entityTypeId);
        entity.setOwnerId(ownerId);
        entity.setName(name);
        entity.setDescription(description);
        entity.setParentEntityId(parentEntityId);
        return createEntity(entity);
    }

    @Override
    public boolean updateEntityMetadata(
            String domainId, String entityId, String name, String description, String parentEntityId) throws Exception {
        EntityEntity entity = getEntity(domainId, entityId);
        entity.setName(name);
        entity.setDescription(description);
        entity.setParentEntityId(parentEntityId);
        return updateEntity(entity);
    }

    @Override
    public List<String> searchEntityIds(
            String domainId, String userId, List<SearchCriteria> filters, int offset, int limit) throws Exception {
        return searchEntities(domainId, userId, filters, offset, limit).stream()
                .map(EntityEntity::getEntityId)
                .collect(Collectors.toList());
    }

    // ── SharingProvider adapter methods ──────────────────────────────────────────

    @Override
    public String createUser(String userId, String domainId, String userName) throws Exception {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setDomainId(domainId);
        user.setCreatedTime(System.currentTimeMillis());
        user.setUpdatedTime(System.currentTimeMillis());
        user.setUserName(userName);
        return createUser(user);
    }

    @Override
    public String createGroup(UserGroup group) throws Exception {
        UserGroupEntity entity = new UserGroupEntity();
        entity.setGroupId(group.getGroupId());
        entity.setDomainId(group.getDomainId());
        entity.setGroupCardinality(group.getGroupCardinality().name());
        entity.setCreatedTime(group.getCreatedTime());
        entity.setUpdatedTime(group.getUpdatedTime());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setOwnerId(group.getOwnerId());
        entity.setGroupType(group.getGroupType().name());
        createGroup(entity);
        return entity.getGroupId();
    }

    @Override
    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws Exception {
        List<UserGroupEntity> entities = getAllMemberGroupEntitiesForUser(domainId, userId);
        return entities.stream().map(this::toProto).collect(Collectors.toList());
    }

    private UserGroup toProto(UserGroupEntity entity) {
        UserGroup.Builder builder = UserGroup.newBuilder()
                .setGroupId(entity.getGroupId())
                .setDomainId(entity.getDomainId())
                .setName(entity.getName() != null ? entity.getName() : "")
                .setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : "");
        if (entity.getDescription() != null) {
            builder.setDescription(entity.getDescription());
        }
        if (entity.getGroupType() != null) {
            try {
                builder.setGroupType(GroupType.valueOf(entity.getGroupType()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getGroupCardinality() != null) {
            try {
                builder.setGroupCardinality(GroupCardinality.valueOf(entity.getGroupCardinality()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getCreatedTime() != null) {
            builder.setCreatedTime(entity.getCreatedTime());
        }
        if (entity.getUpdatedTime() != null) {
            builder.setUpdatedTime(entity.getUpdatedTime());
        }
        return builder.build();
    }
}
