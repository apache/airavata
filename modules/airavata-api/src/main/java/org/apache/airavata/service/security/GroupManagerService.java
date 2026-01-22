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
package org.apache.airavata.service.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.exception.AuthorizationException;
import org.apache.airavata.common.model.GroupModel;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.profile.exception.GroupManagerServiceException;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.GroupType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnApiService
public class GroupManagerService {
    private static final Logger logger = LoggerFactory.getLogger(GroupManagerService.class);

    private final UserProfileService userProfileService;
    private final SharingRegistryService sharingService;

    public GroupManagerService(UserProfileService userProfileService, SharingRegistryService sharingService) {
        this.userProfileService = userProfileService;
        this.sharingService = sharingService;
    }

    private SharingRegistryService getSharingService() {
        return sharingService;
    }

    public String createGroup(AuthzToken authzToken, GroupModel groupModel)
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

        var sharingUserGroup = new UserGroup();
        sharingUserGroup.setGroupId(UUID.randomUUID().toString());
        sharingUserGroup.setName(groupModel.getName());
        sharingUserGroup.setDescription(groupModel.getDescription());
        sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
        sharingUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        var gatewayId = getDomainId(authzToken);
        sharingUserGroup.setDomainId(gatewayId);
        sharingUserGroup.setOwnerId(getUserId(authzToken));

        var groupId = getSharingService().createGroup(sharingUserGroup);
        internalAddUsersToGroup(getSharingService(), gatewayId, groupModel.getMembers(), groupId);
        if (groupModel.getAdmins() != null && !groupModel.getAdmins().isEmpty()) {
            try {
                getSharingService().addGroupAdmins(gatewayId, groupId, groupModel.getAdmins());
            } catch (DuplicateEntryException e) {
                // Ignore duplicate admin entries
            }
        }
        return groupId;
    }

    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
        var userId = getUserId(authzToken);
        var domainId = getDomainId(authzToken);
        if (!(getSharingService().hasOwnerAccess(domainId, groupModel.getId(), userId)
                || getSharingService().hasAdminAccess(domainId, groupModel.getId(), userId))) {
            throw new AuthorizationException("User does not have permission to update group");
        }

        var sharingUserGroup = new UserGroup();
        sharingUserGroup.setGroupId(groupModel.getId());
        sharingUserGroup.setName(groupModel.getName());
        sharingUserGroup.setDescription(groupModel.getDescription());
        sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
        sharingUserGroup.setDomainId(getDomainId(authzToken));

        // adding and removal of users should be handle separately
        getSharingService().updateGroup(sharingUserGroup);
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

    public GroupModel getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        UserGroup userGroup = getSharingService().getGroup(domainId, groupId);
        GroupModel groupModel = convertToGroupModel(userGroup, getSharingService());
        return groupModel;
    }

    public List<GroupModel> getGroups(AuthzToken authzToken)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        List<UserGroup> userGroups = getSharingService().getGroups(domainId, 0, -1);
        return convertToGroupModels(userGroups, getSharingService());
    }

    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, SharingRegistryException {
        final String domainId = getDomainId(authzToken);
        List<UserGroup> userGroups = getSharingService().getAllMemberGroupsForUser(domainId, userName);
        return convertToGroupModels(userGroups, getSharingService());
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

    private List<GroupModel> convertToGroupModels(List<UserGroup> userGroups, SharingRegistryService sharingService)
            throws SharingRegistryException {
        if (userGroups == null) {
            return new ArrayList<>();
        }

        List<GroupModel> groupModels = new ArrayList<>();

        for (UserGroup userGroup : userGroups) {
            if (userGroup != null) {
                GroupModel groupModel = convertToGroupModel(userGroup, sharingService);
                groupModels.add(groupModel);
            }
        }
        return groupModels;
    }

    private GroupModel convertToGroupModel(UserGroup userGroup, SharingRegistryService sharingService)
            throws SharingRegistryException {
        if (userGroup == null) {
            throw new SharingRegistryException("UserGroup cannot be null");
        }

        GroupModel groupModel = new GroupModel();
        groupModel.setId(userGroup.getGroupId());
        groupModel.setName(userGroup.getName());
        groupModel.setDescription(userGroup.getDescription());
        groupModel.setOwnerId(userGroup.getOwnerId());
        final List<String> admins = (userGroup.getGroupAdmins() != null)
                ? userGroup.getGroupAdmins().stream()
                        .map(groupAdmin -> groupAdmin.getAdminId())
                        .toList()
                : new ArrayList<>();
        groupModel.setAdmins(admins);

        // Initialize members list if null
        if (groupModel.getMembers() == null) {
            groupModel.setMembers(new ArrayList<>());
        }

        List<User> groupMembers =
                sharingService.getGroupMembersOfTypeUser(userGroup.getDomainId(), userGroup.getGroupId(), 0, -1);
        if (groupMembers != null) {
            groupMembers.stream()
                    .filter(user -> user != null && user.getUserId() != null)
                    .forEach(user -> groupModel.getMembers().add(user.getUserId()));
        }
        return groupModel;
    }

    private boolean internalAddUsersToGroup(
            SharingRegistryService sharingService, String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {

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
                    user.setCreatedTime(AiravataUtils.getUniqueTimestamp().getTime());
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
