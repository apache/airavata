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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.user.core.repositories.UserProfileRepository;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.GroupType;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupManagerService {
    private static final Logger logger = LoggerFactory.getLogger(GroupManagerService.class);
    private UserProfileRepository userProfileRepository = new UserProfileRepository();
    private SharingRegistryService sharingService = new SharingRegistryService();

    public String createGroup(AuthzToken authzToken, GroupModel groupModel) throws GroupManagerServiceException {
        try {
            // TODO Validations for authorization
            var sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(UUID.randomUUID().toString());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
            var gatewayId = getDomainId(authzToken);
            sharingUserGroup.setDomainId(gatewayId);
            sharingUserGroup.setOwnerId(getUserId(authzToken));

            var groupId = sharingService.createGroup(sharingUserGroup);
            internalAddUsersToGroup(sharingService, gatewayId, groupModel.getMembers(), groupId);
            if (groupModel.getAdmins() != null && !groupModel.getAdmins().isEmpty()) {
                sharingService.addGroupAdmins(gatewayId, groupId, groupModel.getAdmins());
            }
            return groupId;
        } catch (Exception e) {
            var msg = "Error Creating Group";
            logger.error(msg, e);
            var exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel) throws GroupManagerServiceException {
        try {
            var userId = getUserId(authzToken);
            var domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupModel.getId(), userId)
                    || sharingService.hasAdminAccess(domainId, groupModel.getId(), userId))) {
                throw new GroupManagerServiceException("User does not have permission to update group");
            }

            var sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(groupModel.getId());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setDomainId(getDomainId(authzToken));

            // adding and removal of users should be handle separately
            sharingService.updateGroup(sharingUserGroup);
            return true;
        } catch (Exception e) {
            String msg = "Error Updating Group";
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have permission to delete group");
            }

            sharingService.deleteGroup(getDomainId(authzToken), groupId);
            return true;
        } catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public GroupModel getGroup(AuthzToken authzToken, String groupId) throws GroupManagerServiceException {
        try {
            final String domainId = getDomainId(authzToken);
            UserGroup userGroup = sharingService.getGroup(domainId, groupId);
            GroupModel groupModel = convertToGroupModel(userGroup, sharingService);
            return groupModel;
        } catch (Exception e) {
            String msg = "Error Retreiving Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public List<GroupModel> getGroups(AuthzToken authzToken) throws GroupManagerServiceException {
        final String domainId = getDomainId(authzToken);
        try {
            List<UserGroup> userGroups = sharingService.getGroups(domainId, 0, -1);
            return convertToGroupModels(userGroups, sharingService);
        } catch (Exception e) {
            String msg = "Error Retrieving Groups. Domain ID: " + domainId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException {
        try {
            final String domainId = getDomainId(authzToken);
            List<UserGroup> userGroups = sharingService.getAllMemberGroupsForUser(domainId, userName);
            return convertToGroupModels(userGroups, sharingService);
        } catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId)
                    || sharingService.hasAdminAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have access to add users to the group");
            }
            return internalAddUsersToGroup(sharingService, domainId, userIds, groupId);

        } catch (Exception e) {
            String msg = "Error adding users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId)
                    || sharingService.hasAdminAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have access to remove users to the group");
            }
            return sharingService.removeUsersFromGroup(domainId, userIds, groupId);
        } catch (Exception e) {
            String msg = "Error remove users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException(
                        "User does not have Owner permission to transfer group ownership");
            }
            return sharingService.transferGroupOwnership(getDomainId(authzToken), groupId, newOwnerId);
        } catch (Exception e) {
            String msg = "Error Transferring Group Ownership";
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have Owner permission to add group admins");
            }
            return sharingService.addGroupAdmins(getDomainId(authzToken), groupId, adminIds);
        } catch (Exception e) {
            String msg = "Error Adding Admins to Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException {
        try {
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingService.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have Owner permission to remove group admins");
            }
            return sharingService.removeGroupAdmins(getDomainId(authzToken), groupId, adminIds);
        } catch (Exception e) {
            String msg = "Error Removing Admins from the Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException {
        try {
            return sharingService.hasAdminAccess(getDomainId(authzToken), groupId, adminId);
        } catch (Exception e) {
            String msg = "Error Checking Admin Access for the Group. Group ID: " + groupId + " Admin ID: " + adminId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException {
        try {
            return sharingService.hasOwnerAccess(getDomainId(authzToken), groupId, ownerId);
        } catch (Exception e) {
            String msg = "Error Checking Owner Access for the Group. Group ID: " + groupId + " Owner ID: " + ownerId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    private String getDomainId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
    }

    private String getUserId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + getDomainId(authzToken);
    }

    private List<GroupModel> convertToGroupModels(List<UserGroup> userGroups, SharingRegistryService sharingService)
            throws SharingRegistryException {

        List<GroupModel> groupModels = new ArrayList<>();

        for (UserGroup userGroup : userGroups) {
            GroupModel groupModel = convertToGroupModel(userGroup, sharingService);

            groupModels.add(groupModel);
        }
        return groupModels;
    }

    private GroupModel convertToGroupModel(UserGroup userGroup, SharingRegistryService sharingService)
            throws SharingRegistryException {
        GroupModel groupModel = new GroupModel();
        groupModel.setId(userGroup.getGroupId());
        groupModel.setName(userGroup.getName());
        groupModel.setDescription(userGroup.getDescription());
        groupModel.setOwnerId(userGroup.getOwnerId());
        final List<String> admins = userGroup.getGroupAdmins().stream()
                .map(groupAdmin -> groupAdmin.getAdminId())
                .collect(Collectors.toList());
        groupModel.setAdmins(admins);

        sharingService.getGroupMembersOfTypeUser(userGroup.getDomainId(), userGroup.getGroupId(), 0, -1).stream()
                .forEach(user -> groupModel.addToMembers(user.getUserId()));
        return groupModel;
    }

    private boolean internalAddUsersToGroup(
            SharingRegistryService sharingService, String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException {

        // FIXME: workaround for UserProfiles that failed to sync to the sharing
        // registry: create any missing users in the sharing registry
        for (String userId : userIds) {
            if (!sharingService.isUserExists(domainId, userId)) {
                User user = new User();
                user.setDomainId(domainId);
                user.setUserId(userId);
                UserProfile userProfile = userProfileRepository.get(userId);
                user.setUserName(userProfile.getUserId());
                user.setCreatedTime(userProfile.getCreationTime());
                user.setEmail(
                        userProfile.getEmailsSize() > 0
                                ? userProfile.getEmails().get(0)
                                : null);
                user.setFirstName(userProfile.getFirstName());
                user.setLastName(userProfile.getLastName());
                sharingService.createUser(user);
            }
        }
        return sharingService.addUsersToGroup(domainId, userIds, groupId);
    }
}
