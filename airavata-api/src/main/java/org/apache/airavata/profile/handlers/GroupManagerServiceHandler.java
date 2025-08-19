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
package org.apache.airavata.profile.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.profile.user.core.repositories.UserProfileRepository;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.GroupType;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupManagerServiceHandler implements GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);

    private UserProfileRepository userProfileRepository = new UserProfileRepository();

    public GroupManagerServiceHandler() {}

    @Override
    public String getAPIVersion() throws TException {
        return group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            // TODO Validations for authorization
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(UUID.randomUUID().toString());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
            String gatewayId = getDomainId(authzToken);
            sharingUserGroup.setDomainId(gatewayId);
            sharingUserGroup.setOwnerId(getUserId(authzToken));

            String groupId = sharingRegistry.createGroup(sharingUserGroup);
            internalAddUsersToGroup(sharingRegistry, gatewayId, groupModel.getMembers(), groupId);
            addGroupAdmins(authzToken, groupId, groupModel.getAdmins());
            return groupId;
        } catch (Exception e) {
            String msg = "Error Creating Group";
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupModel.getId(), userId)
                    || sharingRegistry.hasAdminAccess(domainId, groupModel.getId(), userId))) {
                throw new GroupManagerServiceException("User does not have permission to update group");
            }

            UserGroup sharingUserGroup = new UserGroup();
            sharingUserGroup.setGroupId(groupModel.getId());
            sharingUserGroup.setName(groupModel.getName());
            sharingUserGroup.setDescription(groupModel.getDescription());
            sharingUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingUserGroup.setDomainId(getDomainId(authzToken));

            // adding and removal of users should be handle separately
            sharingRegistry.updateGroup(sharingUserGroup);
            return true;
        } catch (Exception e) {
            String msg = "Error Updating Group";
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have permission to delete group");
            }

            sharingRegistry.deleteGroup(getDomainId(authzToken), groupId);
            return true;
        } catch (Exception e) {
            String msg = "Error Deleting Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            final String domainId = getDomainId(authzToken);
            UserGroup userGroup = sharingRegistry.getGroup(domainId, groupId);

            GroupModel groupModel = convertToGroupModel(userGroup, sharingRegistry);

            return groupModel;
        } catch (Exception e) {
            String msg = "Error Retreiving Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getGroups(AuthzToken authzToken)
            throws GroupManagerServiceException, AuthorizationException, TException {
        final String domainId = getDomainId(authzToken);
        SharingRegistryService.Iface sharingRegistry = null;
        try {
            sharingRegistry = getSharingRegistry();
            List<UserGroup> userGroups = sharingRegistry.getGroups(domainId, 0, -1);

            return convertToGroupModels(userGroups, sharingRegistry);
        } catch (Exception e) {
            String msg = "Error Retrieving Groups. Domain ID: " + domainId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            final String domainId = getDomainId(authzToken);
            List<UserGroup> userGroups = sharingRegistry.getAllMemberGroupsForUser(domainId, userName);

            return convertToGroupModels(userGroups, sharingRegistry);
        } catch (Exception e) {
            String msg = "Error Retreiving All Groups for User. User ID: " + userName;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId)
                    || sharingRegistry.hasAdminAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have access to add users to the group");
            }
            return internalAddUsersToGroup(sharingRegistry, domainId, userIds, groupId);

        } catch (Exception e) {
            String msg = "Error adding users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId)
                    || sharingRegistry.hasAdminAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have access to remove users to the group");
            }
            return sharingRegistry.removeUsersFromGroup(domainId, userIds, groupId);
        } catch (Exception e) {
            String msg = "Error remove users to group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException(
                        "User does not have Owner permission to transfer group ownership");
            }
            return sharingRegistry.transferGroupOwnership(getDomainId(authzToken), groupId, newOwnerId);
        } catch (Exception e) {
            String msg = "Error Transferring Group Ownership";
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have Owner permission to add group admins");
            }
            return sharingRegistry.addGroupAdmins(getDomainId(authzToken), groupId, adminIds);
        } catch (Exception e) {
            String msg = "Error Adding Admins to Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            String userId = getUserId(authzToken);
            String domainId = getDomainId(authzToken);
            if (!(sharingRegistry.hasOwnerAccess(domainId, groupId, userId))) {
                throw new GroupManagerServiceException("User does not have Owner permission to remove group admins");
            }
            return sharingRegistry.removeGroupAdmins(getDomainId(authzToken), groupId, adminIds);
        } catch (Exception e) {
            String msg = "Error Removing Admins from the Group. Group ID: " + groupId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            return sharingRegistry.hasAdminAccess(getDomainId(authzToken), groupId, adminId);
        } catch (Exception e) {
            String msg = "Error Checking Admin Access for the Group. Group ID: " + groupId + " Admin ID: " + adminId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            SharingRegistryService.Iface sharingRegistry = getSharingRegistry();
            return sharingRegistry.hasOwnerAccess(getDomainId(authzToken), groupId, ownerId);
        } catch (Exception e) {
            String msg = "Error Checking Owner Access for the Group. Group ID: " + groupId + " Owner ID: " + ownerId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }

    private SharingRegistryService.Iface getSharingRegistry()
            throws TException, ApplicationSettingsException {
        return AiravataServiceFactory.getSharingRegistry();
    }

    private String getDomainId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
    }

    private String getUserId(AuthzToken authzToken) {
        return authzToken.getClaimsMap().get(Constants.USER_NAME) + "@" + getDomainId(authzToken);
    }

    private List<GroupModel> convertToGroupModels(
            List<UserGroup> userGroups, SharingRegistryService.Iface sharingRegistry) throws TException {

        List<GroupModel> groupModels = new ArrayList<>();

        for (UserGroup userGroup : userGroups) {
            GroupModel groupModel = convertToGroupModel(userGroup, sharingRegistry);

            groupModels.add(groupModel);
        }
        return groupModels;
    }

    private GroupModel convertToGroupModel(UserGroup userGroup, SharingRegistryService.Iface sharingRegistry)
            throws TException {
        GroupModel groupModel = new GroupModel();
        groupModel.setId(userGroup.getGroupId());
        groupModel.setName(userGroup.getName());
        groupModel.setDescription(userGroup.getDescription());
        groupModel.setOwnerId(userGroup.getOwnerId());
        final List<String> admins = userGroup.getGroupAdmins().stream()
                .map(groupAdmin -> groupAdmin.getAdminId())
                .collect(Collectors.toList());
        groupModel.setAdmins(admins);

        sharingRegistry.getGroupMembersOfTypeUser(userGroup.getDomainId(), userGroup.getGroupId(), 0, -1).stream()
                .forEach(user -> groupModel.addToMembers(user.getUserId()));
        return groupModel;
    }

    private boolean internalAddUsersToGroup(
            SharingRegistryService.Iface sharingRegistry, String domainId, List<String> userIds, String groupId)
            throws SharingRegistryException, TException {

        // FIXME: workaround for UserProfiles that failed to sync to the sharing
        // registry: create any missing users in the sharing registry
        for (String userId : userIds) {
            if (!sharingRegistry.isUserExists(domainId, userId)) {
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
                sharingRegistry.createUser(user);
            }
        }
        return sharingRegistry.addUsersToGroup(domainId, userIds, groupId);
    }
}
