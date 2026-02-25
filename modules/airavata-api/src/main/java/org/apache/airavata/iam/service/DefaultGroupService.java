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
import java.util.List;
import java.util.UUID;
import org.apache.airavata.iam.exception.AuthExceptions.AuthorizationException;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.iam.exception.GroupManagerServiceException;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.GroupCardinality;
import org.apache.airavata.iam.model.GroupMember;
import org.apache.airavata.iam.model.GroupType;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.model.UserGroup;
import org.apache.airavata.iam.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultGroupService implements GroupService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultGroupService.class);

    private final UserService userProfileService;
    private final SharingService sharingService;

    public DefaultGroupService(UserService userProfileService, SharingService sharingService) {
        this.userProfileService = userProfileService;
        this.sharingService = sharingService;
    }

    private SharingService getSharingService() {
        return sharingService;
    }

    public String createGroup(AuthzToken authzToken, UserGroup group)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        // Validate authorization: user must be authenticated and belong to the gateway
        if (authzToken == null
                || authzToken.getClaimsMap() == null
                || authzToken.getClaimsMap().isEmpty()) {
            throw new AuthorizationException("Invalid authorization token");
        }
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (userId == null || userId.isEmpty() || domainId == null || domainId.isEmpty()) {
            throw new AuthorizationException("Invalid user or gateway information in authorization token");
        }

        group.setGroupId(UUID.randomUUID().toString());
        group.setGroupType(GroupType.USER_LEVEL_GROUP);
        group.setGroupCardinality(GroupCardinality.MULTI_USER);
        group.setDomainId(domainId);
        group.setOwnerId(userId);

        var groupId = getSharingService().createGroup(group);
        var members =
                group.getMembers() != null ? group.getMembers() : java.util.Collections.<String>emptyList();
        internalAddUsersToGroup(getSharingService(), domainId, members, groupId);
        if (group.getAdmins() != null && !group.getAdmins().isEmpty()) {
            try {
                getSharingService().addGroupAdmins(domainId, groupId, group.getAdmins());
            } catch (DuplicateEntryException e) {
                // Ignore duplicate admin entries
            }
        }
        return groupId;
    }

    public boolean updateGroup(AuthzToken authzToken, UserGroup group)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        var userId = getUserId(authzToken);
        var domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, group.getGroupId(), userId)
                || getSharingService().hasAdminAccess(domainId, group.getGroupId(), userId))) {
            throw new AuthorizationException("User does not have permission to update group");
        }

        group.setGroupType(GroupType.USER_LEVEL_GROUP);
        group.setDomainId(domainId);

        // adding and removal of users should be handle separately
        getSharingService().updateGroup(group);
        return true;
    }

    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have permission to delete group");
        }

        getSharingService().deleteGroup(getDomainId(authzToken), groupId);
        return true;
    }

    public UserGroup getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        UserGroup userGroup = getSharingService().getGroup(domainId, groupId);
        populateMembersAndAdmins(userGroup, getSharingService());
        return userGroup;
    }

    public List<UserGroup> getGroups(AuthzToken authzToken)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        List<UserGroup> userGroups = getSharingService().getGroups(domainId, 0, -1);
        for (UserGroup ug : userGroups) {
            if (ug != null) populateMembersAndAdmins(ug, getSharingService());
        }
        return userGroups != null ? userGroups : new ArrayList<>();
    }

    public List<UserGroup> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        List<UserGroup> userGroups = getSharingService().getAllMemberGroupsForUser(domainId, userName);
        for (UserGroup ug : userGroups) {
            if (ug != null) populateMembersAndAdmins(ug, getSharingService());
        }
        return userGroups != null ? userGroups : new ArrayList<>();
    }

    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId)
                || getSharingService().hasAdminAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have access to add users to the group");
        }
        return internalAddUsersToGroup(getSharingService(), domainId, userIds, groupId);
    }

    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId)
                || getSharingService().hasAdminAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have access to remove users to the group");
        }
        return getSharingService().removeUsersFromGroup(domainId, userIds, groupId);
    }

    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have Owner permission to transfer group ownership");
        }
        try {
            return getSharingService().transferGroupOwnership(getDomainId(authzToken), groupId, newOwnerId);
        } catch (DuplicateEntryException e) {
            throw new SharingRegistryException(
                    String.format("Error transferring group ownership: %s", e.getMessage()), e);
        }
    }

    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have Owner permission to add group admins");
        }
        try {
            return getSharingService().addGroupAdmins(getDomainId(authzToken), groupId, adminIds);
        } catch (DuplicateEntryException e) {
            throw new SharingRegistryException(String.format("Error adding group admins: %s", e.getMessage()), e);
        }
    }

    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        String userId = getUserId(authzToken);
        String domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupId, userId))) {
            throw new AuthorizationException("User does not have Owner permission to remove group admins");
        }
        return getSharingService().removeGroupAdmins(getDomainId(authzToken), groupId, adminIds);
    }

    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException, SharingRegistryException {
        return getSharingService().hasAdminAccess(getDomainId(authzToken), groupId, adminId);
    }

    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, SharingRegistryException {
        return getSharingService().hasOwnerAccess(getDomainId(authzToken), groupId, ownerId);
    }

    private String getDomainId(AuthzToken authzToken) {
        if (authzToken == null || authzToken.getClaimsMap() == null) {
            return null;
        }
        return authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
    }

    private String getUserId(AuthzToken authzToken) {
        if (authzToken == null || authzToken.getClaimsMap() == null) {
            return null;
        }
        String userName = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String domainId = getDomainId(authzToken);
        if (userName == null || domainId == null) {
            return null;
        }
        return userName + "@" + domainId;
    }

    private void populateMembersAndAdmins(UserGroup userGroup, SharingService sharingService)
            throws SharingRegistryException {
        if (userGroup == null) {
            throw new SharingRegistryException("UserGroup cannot be null");
        }

        userGroup.setAdmins((userGroup.getGroupAdmins() != null)
                ? userGroup.getGroupAdmins().stream()
                        .map(GroupMember::getChildId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList())
                : new ArrayList<>());

        List<String> memberIds = new ArrayList<>();
        List<User> groupMembers =
                sharingService.getGroupMembersOfTypeUser(userGroup.getDomainId(), userGroup.getGroupId(), 0, -1);
        if (groupMembers != null) {
            groupMembers.stream()
                    .filter(user -> user != null && user.getUserId() != null)
                    .forEach(user -> memberIds.add(user.getUserId()));
        }
        userGroup.setMembers(memberIds);
    }

    private boolean internalAddUsersToGroup(
            SharingService sharingService, String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {

        if (userIds == null) {
            userIds = java.util.Collections.emptyList();
        }
        // Workaround for UserProfiles that failed to sync to the sharing registry:
        // Create any missing users in the sharing registry to ensure group membership can be established.
        // This handles cases where user profiles exist in the profile service but haven't been
        // synchronized to the sharing registry yet.
        for (String userId : userIds) {
            if (!sharingService.isUserExists(domainId, userId)) {
                User user = new User();
                user.setDomainId(domainId);
                user.setUserId(userId);
                // userId is airavataInternalUserId (format: "userId@gatewayId")
                UserProfile userProfile = userProfileService.getUserProfileByAiravataInternalUserId(userId);
                if (userProfile == null) {
                    // Log warning but don't throw - user may not be visible due to transaction isolation
                    // The user will be added to the group anyway, and if they don't exist, the addUsersToGroup
                    // operation will handle it appropriately
                    logger.warn(
                            "User profile not found for userId: {}. User may not be visible due to transaction isolation. "
                                    + "Skipping user creation in sharing registry, but will attempt to add to group.",
                            userId);
                    // Create a minimal user entry with just the userId to allow group membership
                    user.setUserName(userId.split("@")[0]); // Extract username from airavataInternalUserId
                    user.setCreatedTime(IdGenerator.getUniqueTimestamp().getTime());
                    user.setEmail(null);
                    user.setFirstName(null);
                    user.setLastName(null);
                } else {
                    user.setUserName(userProfile.getUserId());
                    user.setCreatedTime(userProfile.getCreationTime());
                    user.setEmail(
                            userProfile.getEmails() != null
                                            && userProfile.getEmails().size() > 0
                                    ? userProfile.getEmails().get(0)
                                    : null);
                    user.setFirstName(userProfile.getFirstName());
                    user.setLastName(userProfile.getLastName());
                }
                try {
                    sharingService.createUser(user);
                } catch (SharingRegistryException e) {
                    // If user creation fails (e.g., duplicate), log and continue
                    // The user might already exist or there might be a race condition
                    logger.debug(
                            "Failed to create user in sharing registry for userId: {}. Error: {}",
                            userId,
                            e.getMessage());
                }
            }
        }
        return sharingService.addUsersToGroup(domainId, userIds, groupId);
    }
}
