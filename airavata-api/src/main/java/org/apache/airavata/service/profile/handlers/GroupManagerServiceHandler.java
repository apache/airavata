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
package org.apache.airavata.service.profile.handlers;

import java.util.List;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupManagerServiceHandler implements GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);
    private org.apache.airavata.service.GroupManagerService groupManagerService;

    public GroupManagerServiceHandler() {
        groupManagerService = new org.apache.airavata.service.GroupManagerService();
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.createGroup(authzToken, groupModel);
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.updateGroup(authzToken, groupModel);
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.deleteGroup(authzToken, groupId, ownerId);
    }

    @Override
    @SecurityCheck
    public GroupModel getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.getGroup(authzToken, groupId);
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getGroups(AuthzToken authzToken)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.getGroups(authzToken);
    }

    @Override
    @SecurityCheck
    public List<GroupModel> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.getAllGroupsUserBelongs(authzToken, userName);
    }

    @Override
    public boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.addUsersToGroup(authzToken, userIds, groupId);
    }

    @Override
    public boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.removeUsersFromGroup(authzToken, userIds, groupId);
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.transferGroupOwnership(authzToken, groupId, newOwnerId);
    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.addGroupAdmins(authzToken, groupId, adminIds);
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.removeGroupAdmins(authzToken, groupId, adminIds);
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.hasAdminAccess(authzToken, groupId, adminId);
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, AuthorizationException {
        return groupManagerService.hasOwnerAccess(authzToken, groupId, ownerId);
    }
}
