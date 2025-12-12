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
package org.apache.airavata.api.thrift.handler;

import java.util.List;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.service.security.GroupManagerService;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GroupManagerServiceHandler implements org.apache.airavata.profile.groupmanager.cpi.GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);
    private final GroupManagerService groupManagerService;

    public GroupManagerServiceHandler(GroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
        logger.info("GroupManagerServiceHandler initialized with Spring-injected GroupManagerService");
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return org.apache.airavata.profile.groupmanager.cpi.group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.createGroup(authzToken, groupModel);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error creating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.updateGroup(authzToken, groupModel);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error updating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.deleteGroup(authzToken, groupId, ownerId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error deleting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.getGroup(authzToken, groupId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error getting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getGroups(AuthzToken authzToken)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.getGroups(authzToken);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error getting groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.getAllGroupsUserBelongs(authzToken, userName);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error getting user groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.addUsersToGroup(authzToken, userIds, groupId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error adding users to group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.removeUsersFromGroup(authzToken, userIds, groupId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error removing users from group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.transferGroupOwnership(authzToken, groupId, newOwnerId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error transferring group ownership: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.addGroupAdmins(authzToken, groupId, adminIds);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error adding group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.removeGroupAdmins(authzToken, groupId, adminIds);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error removing group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.hasAdminAccess(authzToken, groupId, adminId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error checking admin access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException {
        try {
            return groupManagerService.hasOwnerAccess(authzToken, groupId, ownerId);
        } catch (SharingRegistryException e) {
            GroupManagerServiceException ex =
                    new GroupManagerServiceException("Error checking owner access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}
