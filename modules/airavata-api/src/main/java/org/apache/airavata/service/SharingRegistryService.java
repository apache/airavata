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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServiceUtils;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnApiService;
import org.apache.airavata.sharing.entities.EntityPK;
import org.apache.airavata.sharing.entities.EntityTypePK;
import org.apache.airavata.sharing.entities.GroupAdminPK;
import org.apache.airavata.sharing.entities.GroupMembershipPK;
import org.apache.airavata.sharing.entities.PermissionTypePK;
import org.apache.airavata.sharing.entities.SharingPK;
import org.apache.airavata.sharing.entities.UserGroupPK;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.Entity;
import org.apache.airavata.sharing.model.EntityType;
import org.apache.airavata.sharing.model.GroupAdmin;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.GroupChildType;
import org.apache.airavata.sharing.model.GroupMembership;
import org.apache.airavata.sharing.model.GroupType;
import org.apache.airavata.sharing.model.PermissionType;
import org.apache.airavata.sharing.model.SearchCriteria;
import org.apache.airavata.sharing.model.Sharing;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.SharingType;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.apache.airavata.sharing.services.DomainService;
import org.apache.airavata.sharing.services.EntityService;
import org.apache.airavata.sharing.services.EntityTypeService;
import org.apache.airavata.sharing.services.GroupAdminService;
import org.apache.airavata.sharing.services.GroupMembershipService;
import org.apache.airavata.sharing.services.PermissionTypeService;
import org.apache.airavata.sharing.services.SharingService;
import org.apache.airavata.sharing.services.UserGroupService;
import org.apache.airavata.sharing.services.UserService;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.sharing.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnApiService
public class SharingRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(SharingRegistryService.class);

    private final EntityService entityService;
    private final DomainService domainService;
    private final EntityTypeService entityTypeService;
    private final PermissionTypeService permissionTypeService;
    private final GatewayService gatewayService;

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
            GatewayService gatewayService,
            @Qualifier("sharingUserService") UserService userService,
            UserGroupService userGroupService,
            GroupMembershipService groupMembershipService,
            SharingService sharingService,
            GroupAdminService groupAdminService) {
        this.entityService = entityService;
        this.domainService = domainService;
        this.entityTypeService = entityTypeService;
        this.permissionTypeService = permissionTypeService;
        this.gatewayService = gatewayService;
        this.userService = userService;
        this.userGroupService = userGroupService;
        this.groupMembershipService = groupMembershipService;
        this.sharingService = sharingService;
        this.groupAdminService = groupAdminService;
    }

    public static String OWNER_PERMISSION_NAME = "OWNER";

    /**
     * Initializes domain fields on a gateway and creates the OWNER permission for that domain.
     *
     * <p>This method is transactional, ensuring that both the domain initialization and OWNER
     * permission creation happen atomically. If permission creation fails, the domain
     * initialization will be rolled back.
     *
     * <p><strong>Gateway-Domain Relationship:</strong>
     * Domains are now stored as part of GatewayEntity. The domainId corresponds to the gateway's
     * gatewayId. This method sets the domain-specific fields (description, timestamps,
     * initialUserGroupId) on an existing gateway.
     *
     * <p>The typical flow is:
     * <ol>
     *   <li>Create a Gateway via {@link org.apache.airavata.service.AiravataService#addGateway}</li>
     *   <li>Initialize domain fields via this method (automatically done by addGateway)</li>
     * </ol>
     *
     * @param domain The domain to create. The domainId must match an existing gateway's gatewayId.
     * @return The domain ID
     * @throws SharingRegistryException If domain creation fails or if no gateway exists with the given domainId
     * @throws DuplicateEntryException If the domain fields are already initialized on the gateway
     * @see org.apache.airavata.registry.entities.GatewayEntity
     */
    @Transactional
    public String createDomain(Domain domain) throws SharingRegistryException, DuplicateEntryException {
        try {
            // Check if domain fields are already initialized on the gateway
            Domain existingDomain = domainService.get(domain.getDomainId());
            if (existingDomain != null && existingDomain.getCreatedTime() != null) {
                throw new DuplicateEntryException("Domain already initialized for gateway: " + domain.getDomainId());
            }

            // Validate that a gateway exists with this domainId
            if (!gatewayService.isGatewayExist(domain.getDomainId())) {
                throw new SharingRegistryException(
                        String.format(
                                "Cannot create domain: No gateway exists with gatewayId '%s'. "
                                        + "A domain's domainId must correspond to an existing gateway's gatewayId.",
                                domain.getDomainId()));
            }

            domain.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            domain.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            domainService.create(domain);

            // create the global permission for the domain
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(domain.getDomainId() + ":" + OWNER_PERMISSION_NAME);
            permissionType.setDomainId(domain.getDomainId());
            permissionType.setName(OWNER_PERMISSION_NAME);
            permissionType.setDescription("GLOBAL permission to " + domain.getDomainId());
            permissionType.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            permissionType.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            permissionTypeService.create(permissionType);

            return domain.getDomainId();
        } catch (DuplicateEntryException e) {
            throw e;
        } catch (RegistryException e) {
            String message = String.format(
                    "Error while validating gateway for domain: domainId=%s", domain.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
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
            domain.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            domain = getUpdatedObject(oldDomain, domain);
            domainService.update(domain);
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while updating domain: domainId=%s", domain.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    /**
     * Checks if a domain exists by checking if the corresponding gateway exists.
     *
     * @param domainId the domain ID (equivalent to gatewayId)
     * @return true if the gateway/domain exists
     */
    public boolean isDomainExists(String domainId) throws SharingRegistryException {
        return ServiceUtils.executeBool(
                () -> domainService.isExists(domainId),
                SharingRegistryException.class,
                "Error checking if domain exists: domainId=%s",
                domainId);
    }

    /**
     * Deleting a domain is not supported since domains are now part of gateways.
     * Gateway deletion should be handled through GatewayService.
     *
     * @param domainId the domain ID
     * @return false - domain deletion not supported
     * @throws SharingRegistryException always - domain deletion not supported
     */
    public boolean deleteDomain(String domainId) throws SharingRegistryException {
        throw new SharingRegistryException(
                "Domain deletion is not supported. Domains are part of gateways. "
                        + "Delete the gateway instead through the appropriate gateway management service.");
    }

    public Domain getDomain(String domainId) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> domainService.get(domainId),
                SharingRegistryException.class,
                "Error getting domain: domainId=%s",
                domainId);
    }

    public List<Domain> getDomains(int offset, int limit) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> domainService.getAll(),
                SharingRegistryException.class,
                "Error getting domains: offset=%d, limit=%d",
                offset,
                limit);
    }

    /**
     * * User Operations
     * *
     */
    public String createUser(User user) throws SharingRegistryException {
        try {
            if (userService.get(user.getUserId(), user.getDomainId()) != null)
                throw new SharingRegistryException("There exist user with given user id");

            user.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            user.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            userService.create(user);

            // Auto-create personal group (Zanzibar-like model: every user has a personal group)
            String personalGroupId = user.getUserId() + "_personal";
            UserGroup personalGroup = new UserGroup();
            personalGroup.setGroupId(personalGroupId);
            personalGroup.setDomainId(user.getDomainId());
            personalGroup.setName(user.getUserName() != null ? user.getUserName() : user.getUserId());
            personalGroup.setDescription("Personal group for " + (user.getUserName() != null ? user.getUserName() : user.getUserId()));
            personalGroup.setOwnerId(user.getUserId());
            personalGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            personalGroup.setGroupCardinality(GroupCardinality.SINGLE_USER);
            personalGroup.setIsPersonalGroup(true);
            personalGroup.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            personalGroup.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            createGroup(personalGroup);

            Domain domain = domainService.get(user.getDomainId());
            if (domain != null && domain.getInitialUserGroupId() != null) {
                addUsersToGroup(user.getDomainId(), List.of(user.getUserId()), domain.getInitialUserGroupId());
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
            User oldUser = userService.get(user.getUserId(), user.getDomainId());
            user.setCreatedTime(oldUser.getCreatedTime());
            user.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            user = getUpdatedObject(oldUser, user);
            userService.update(user);

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(user.getUserId() + "_personal");
            userGroupPK.setDomainId(user.getDomainId());
            UserGroup userGroup = userGroupService.get(userGroupPK);
            if (userGroup != null) {
                userGroup.setName(user.getUserName());
                userGroup.setDescription("Personal group for " + user.getUserName());
                updateGroup(userGroup);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format(
                    "Error while updating user: userId=%s, domainId=%s", user.getUserId(), user.getDomainId());
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public boolean isUserExists(String domainId, String userId) throws SharingRegistryException {
        return ServiceUtils.executeBool(
                () -> userService.isExists(userId, domainId),
                SharingRegistryException.class,
                "Error checking if user exists: domainId=%s, userId=%s",
                domainId,
                userId);
    }

    public boolean deleteUser(String domainId, String userId) throws SharingRegistryException {
        try {
            userService.delete(userId, domainId);

            // Delete the user's personal group (allowed when user is being deleted)
            String personalGroupId = userId + "_personal";
            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(personalGroupId);
            userGroupPK.setDomainId(domainId);
            if (userGroupService.get(userGroupPK) != null) {
                userGroupService.delete(userGroupPK);
            }
            return true;
        } catch (SharingRegistryException e) {
            String message = String.format("Error while deleting user: domainId=%s, userId=%s", domainId, userId);
            logger.error(message, e);
            throw new SharingRegistryException(message);
        }
    }

    public User getUser(String domainId, String userId) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> userService.get(userId, domainId),
                SharingRegistryException.class,
                "Error getting user: domainId=%s, userId=%s",
                domainId,
                userId);
    }

    public List<User> getUsers(String domain, int offset, int limit) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> {
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(DBConstants.UserTable.DOMAIN_ID, domain);
                    return userService.select(null, filters, offset, limit);
                },
                SharingRegistryException.class,
                "Error getting users: domain=%s, offset=%d, limit=%d",
                domain,
                offset,
                limit);
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
            // Client created groups are USER_LEVEL_GROUP; MULTI_USER unless personal group
            if (group.getGroupType() == null) {
                group.setGroupType(GroupType.USER_LEVEL_GROUP);
            }
            if (!Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                group.setGroupCardinality(GroupCardinality.MULTI_USER);
            }
            if (group.getGroupCardinality() == null) {
                group.setGroupCardinality(GroupCardinality.MULTI_USER);
            }
            if (group.getIsPersonalGroup() == null) {
                group.setIsPersonalGroup(false);
            }
            group.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            group.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            // Add group admins once the group is created
            if (group.getGroupAdmins() == null) {
                group.setGroupAdmins(new java.util.ArrayList<>());
            } else {
                group.getGroupAdmins().clear();
            }
            userGroupService.create(group);

            addUsersToGroup(group.getDomainId(), List.of(group.getOwnerId()), group.getGroupId());
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
            group.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
        return ServiceUtils.executeBool(
                () -> {
                    UserGroupPK userGroupPK = new UserGroupPK();
                    userGroupPK.setDomainId(domainId);
                    userGroupPK.setGroupId(groupId);
                    return userGroupService.isExists(userGroupPK);
                },
                SharingRegistryException.class,
                "Error checking if group exists: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    public boolean deleteGroup(String domainId, String groupId) throws SharingRegistryException {
        UserGroup group = getGroup(domainId, groupId);
        if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
            throw new SharingRegistryException("Cannot delete personal group. Remove the user first.");
        }
        ServiceUtils.executeVoid(
                () -> {
                    UserGroupPK userGroupPK = new UserGroupPK();
                    userGroupPK.setGroupId(groupId);
                    userGroupPK.setDomainId(domainId);
                    userGroupService.delete(userGroupPK);
                },
                SharingRegistryException.class,
                "Error deleting group: domainId=%s, groupId=%s",
                domainId,
                groupId);
        return true;
    }

    public UserGroup getGroup(String domainId, String groupId) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> {
                    UserGroupPK userGroupPK = new UserGroupPK();
                    userGroupPK.setGroupId(groupId);
                    userGroupPK.setDomainId(domainId);
                    UserGroup group = userGroupService.get(userGroupPK);
                    // Backward compatibility: getGroup(domainId, userId) used to return the user's single-user group.
                    // Personal groups have groupId = userId + "_personal". If no group found, treat groupId as userId.
                    if (group == null && isUserExists(domainId, groupId)) {
                        UserGroupPK personalPK = new UserGroupPK();
                        personalPK.setGroupId(groupId + "_personal");
                        personalPK.setDomainId(domainId);
                        group = userGroupService.get(personalPK);
                    }
                    return group;
                },
                SharingRegistryException.class,
                "Error getting group: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    public List<UserGroup> getGroups(String domain, int offset, int limit) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> {
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(DBConstants.UserGroupTable.DOMAIN_ID, domain);
                    filters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
                    return userGroupService.select(null, filters, offset, limit);
                },
                SharingRegistryException.class,
                "Error getting groups: domain=%s, offset=%d, limit=%d",
                domain,
                offset,
                limit);
    }

    public boolean addUsersToGroup(String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {
        try {
            UserGroup group = getGroup(domainId, groupId);
            if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                // Personal group: only the owner may be a member (added at creation)
                if (userIds == null || userIds.size() != 1 || !userIds.get(0).equals(group.getOwnerId())) {
                    throw new SharingRegistryException("Cannot add members to personal group.");
                }
            }
            if (userIds == null) {
                userIds = java.util.Collections.emptyList();
            }
            for (int i = 0; i < userIds.size(); i++) {
                // Check if membership already exists to avoid duplicate key exceptions
                GroupMembershipPK membershipPK = new GroupMembershipPK();
                membershipPK.setParentId(groupId);
                membershipPK.setChildId(userIds.get(i));
                membershipPK.setDomainId(domainId);
                GroupMembership existing = groupMembershipService.get(membershipPK);
                if (existing == null) {
                    // Only create if it doesn't exist
                    GroupMembership groupMembership = new GroupMembership();
                    groupMembership.setParentId(groupId);
                    groupMembership.setChildId(userIds.get(i));
                    groupMembership.setChildType(GroupChildType.USER);
                    groupMembership.setDomainId(domainId);
                    groupMembership.setCreatedTime(
                            AiravataUtils.getUniqueTimestamp().getTime());
                    groupMembership.setUpdatedTime(
                            AiravataUtils.getUniqueTimestamp().getTime());
                    groupMembershipService.create(groupMembership);
                }
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
            UserGroup group = getGroup(domainId, groupId);
            if (Boolean.TRUE.equals(group.getIsPersonalGroup())) {
                throw new SharingRegistryException("Cannot remove user from their personal group.");
            }
            if (userIds == null) {
                userIds = java.util.Collections.emptyList();
            }
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
                removeGroupAdmins(domainId, groupId, List.of(newOwnerId));
            }

            UserGroupPK userGroupPK = new UserGroupPK();
            userGroupPK.setGroupId(groupId);
            userGroupPK.setDomainId(domainId);
            UserGroup userGroup = userGroupService.get(userGroupPK);
            UserGroup newUserGroup = new UserGroup();
            newUserGroup.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
        return ServiceUtils.executeBool(
                () -> {
                    GroupAdminPK groupAdminPK = new GroupAdminPK();
                    groupAdminPK.setGroupId(groupId);
                    groupAdminPK.setAdminId(adminId);
                    groupAdminPK.setDomainId(domainId);
                    // Use isExists() instead of get() != null to avoid JPA first-level cache issues
                    // where a recently deleted entity might still be returned from cache
                    return groupAdminService.isExists(groupAdminPK);
                },
                SharingRegistryException.class,
                "Error checking admin access: domainId=%s, groupId=%s, adminId=%s",
                domainId,
                groupId,
                adminId);
    }

    public boolean hasOwnerAccess(String domainId, String groupId, String ownerId) throws SharingRegistryException {
        return ServiceUtils.executeBool(
                () -> {
                    UserGroupPK userGroupPK = new UserGroupPK();
                    userGroupPK.setGroupId(groupId);
                    userGroupPK.setDomainId(domainId);
                    UserGroup group = userGroupService.get(userGroupPK);
                    return group.getOwnerId().equals(ownerId);
                },
                SharingRegistryException.class,
                "Error checking owner access: domainId=%s, groupId=%s, ownerId=%s",
                domainId,
                groupId,
                ownerId);
    }

    public List<User> getGroupMembersOfTypeUser(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> groupMembershipService.getAllChildUsers(domainId, groupId),
                SharingRegistryException.class,
                "Error getting group members of type user: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    public List<UserGroup> getGroupMembersOfTypeGroup(String domainId, String groupId, int offset, int limit)
            throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> groupMembershipService.getAllChildGroups(domainId, groupId),
                SharingRegistryException.class,
                "Error getting group members of type group: domainId=%s, groupId=%s",
                domainId,
                groupId);
    }

    public boolean addChildGroupsToParentGroup(String domainId, List<String> childIds, String groupId)
            throws SharingRegistryException {
        try {
            for (String childId : childIds) {
                // Check for self-reference: cannot add a group as its own child
                if (childId.equals(groupId)) {
                    throw new SharingRegistryException(
                            String.format("Cannot add group %s as its own child (self-reference)", groupId));
                }

                // Check if childId is an ancestor of groupId (the parent group)
                // If childId is an ancestor of groupId, then adding childId as a child of groupId would create a cycle
                try {
                    List<GroupMembership> parentAncestors = groupMembershipService.getAllParentMembershipsForChild(
                            domainId, groupId, GroupChildType.GROUP);
                    // Collect all parentIds from the ancestor chain (including indirect ancestors)
                    Set<String> ancestorIds = parentAncestors.stream()
                            .map(GroupMembership::getParentId)
                            .collect(Collectors.toSet());
                    // Also include groupId itself in the set to check for direct matches
                    ancestorIds.add(groupId);
                    // Check if childId appears anywhere in the ancestor chain
                    boolean wouldCreateCycle = ancestorIds.contains(childId);

                    if (wouldCreateCycle) {
                        throw new SharingRegistryException(String.format(
                                "Cannot add group %s as child of %s: would create cyclic dependency",
                                childId, groupId));
                    }
                } catch (SharingRegistryException e) {
                    // Re-throw cyclic dependency exceptions
                    if (e.getMessage() != null && e.getMessage().contains("cyclic dependency")) {
                        throw e;
                    }
                    // If we can't check (e.g., group doesn't exist yet), log and continue
                    // The actual group membership creation will fail if there's a real issue
                    logger.debug("Could not check for cyclic dependency for group {}: {}", childId, e.getMessage());
                }

                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setParentId(groupId);
                groupMembership.setChildId(childId);
                groupMembership.setChildType(GroupChildType.GROUP);
                groupMembership.setDomainId(domainId);
                groupMembership.setCreatedTime(
                        AiravataUtils.getUniqueTimestamp().getTime());
                groupMembership.setUpdatedTime(
                        AiravataUtils.getUniqueTimestamp().getTime());
                groupMembershipService.create(groupMembership);
            }
            return true;
        } catch (SharingRegistryException e) {
            // If the exception already contains a meaningful message (e.g., self-reference or cyclic dependency),
            // re-throw it as-is. Otherwise, wrap it with a generic error message.
            if (e.getMessage() != null
                    && (e.getMessage().contains("self-reference")
                            || e.getMessage().contains("cyclic dependency"))) {
                throw e;
            }
            String message = String.format(
                    "Error while adding child groups to parent group: domainId=%s, groupId=%s", domainId, groupId);
            logger.error(message, e);
            throw new SharingRegistryException(message, e);
        }
    }

    public boolean removeChildGroupFromParentGroup(String domainId, String childId, String groupId)
            throws SharingRegistryException {
        ServiceUtils.executeVoid(
                () -> {
                    GroupMembershipPK groupMembershipPK = new GroupMembershipPK();
                    groupMembershipPK.setParentId(groupId);
                    groupMembershipPK.setChildId(childId);
                    groupMembershipPK.setDomainId(domainId);
                    groupMembershipService.delete(groupMembershipPK);
                },
                SharingRegistryException.class,
                "Error removing child group from parent: domainId=%s, childId=%s, groupId=%s",
                domainId,
                childId,
                groupId);
        return true;
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        return ServiceUtils.execute(
                () -> groupMembershipService.getAllMemberGroupsForUser(domainId, userId),
                SharingRegistryException.class,
                "Error getting member groups for user: domainId=%s, userId=%s",
                domainId,
                userId);
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

            entityType.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            entityType.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
            entityType.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
            permissionType.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            permissionType.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
            permissionType.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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

            if (!userService.isExists(entity.getOwnerId(), entity.getDomainId())) {
                // Auto-create user for Airavata easy integration: when creating entities, if the owner user
                // doesn't exist in the sharing registry, create a minimal user entry automatically.
                // This handles cases where user profiles exist but haven't been synchronized to the sharing registry.
                // In a strict implementation, this would throw an exception, but for ease of integration,
                // we create the user automatically.
                User user = new User();
                user.setUserId(entity.getOwnerId());
                user.setDomainId(entity.getDomainId());
                user.setUserName(user.getUserId().split("@")[0]);

                createUser(user);
            }
            entity.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            entity.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());

            // Handle null case - getOriginalEntityCreationTime() returns Long (boxed), which can be null
            Long originalTime = entity.getOriginalEntityCreationTime();
            if (originalTime == null || originalTime == 0) {
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
            newSharing.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            newSharing.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());

            sharingService.create(newSharing);

            // creating records for inherited permissions
            if (entity.getParentEntityId() != null
                    && !entity.getParentEntityId().isEmpty()) {
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
                newSharing.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
                newSharing.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());

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
            // Check for permission changes when needed
            entity.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());
            EntityPK entityPK = new EntityPK();
            entityPK.setDomainId(entity.getDomainId());
            entityPK.setEntityId(entity.getEntityId());
            Entity oldEntity = entityService.get(entityPK);
            entity.setCreatedTime(oldEntity.getCreatedTime());
            // check if parent entity changed and re-add inherited permissions
            if (!Objects.equals(oldEntity.getParentEntityId(), entity.getParentEntityId())) {
                logger.debug("Parent entity changed for {}, updating inherited permissions", entity.getEntityId());
                if (oldEntity.getParentEntityId() != null
                        && !oldEntity.getParentEntityId().isEmpty()) {
                    logger.debug(
                            "Removing inherited permissions from {} that were inherited from parent {}",
                            entity.getEntityId(),
                            oldEntity.getParentEntityId());
                    sharingService.removeAllIndirectCascadingPermissionsForEntity(
                            entity.getDomainId(), entity.getEntityId());
                }
                if (entity.getParentEntityId() != null
                        && !entity.getParentEntityId().isEmpty()) {
                    // re-add INDIRECT_CASCADING permissions
                    logger.debug(
                            "Adding inherited permissions to {} that are inherited from parent {}",
                            entity.getEntityId(),
                            entity.getParentEntityId());
                    addCascadingPermissionsForEntity(entity);
                }
            }
            entity = getUpdatedObject(oldEntity, entity);
            long sharedCount = sharingService.getSharedCount(entity.getDomainId(), entity.getEntityId());
            entity.setSharedCount(sharedCount);
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
            // Check for permission changes when needed
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
                sharing.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
                sharing.setUpdatedTime(AiravataUtils.getUniqueTimestamp().getTime());

                sharings.add(sharing);
            }

            if (cascadePermission) {
                // Adding permission for the specified users/groups for all child entities
                entityService.getChildEntities(domainId, entityId).forEach(temp::addLast);
                while (!temp.isEmpty()) {
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
                        sharing.setCreatedTime(
                                AiravataUtils.getUniqueTimestamp().getTime());
                        sharing.setUpdatedTime(
                                AiravataUtils.getUniqueTimestamp().getTime());
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
            long sharedCount = sharingService.getSharedCount(domainId, entityId);
            entity.setSharedCount(sharedCount);
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
            List<String> groupIds = new ArrayList<>(
                    parentMemberships.stream().map(GroupMembership::getParentId).toList());
            groupIds.add(userId);
            return sharingService.hasAccess(
                    domainId,
                    entityId,
                    groupIds,
                    List.of(permissionTypeId, permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId)));
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
            long sharedCount = sharingService.getSharedCount(domainId, entityId);
            entity.setSharedCount(sharedCount);
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
        HashMap<String, Object> newHT = fieldsToHT(newEntityFields, newEntity);

        Class<?> oldEntityClass = oldEntity.getClass();
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

    private static HashMap<String, Object> fieldsToHT(Field[] fields, Object obj) {
        HashMap<String, Object> hashtable = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object retrievedObject = field.get(obj);
                if (retrievedObject != null) {
                    logger.debug("scanning " + field.getName());
                    hashtable.put(field.getName(), field.get(obj));
                }
            } catch (IllegalAccessException e) {
                logger.debug("Could not access field: {}", field.getName(), e);
            }
        }
        return hashtable;
    }
}
