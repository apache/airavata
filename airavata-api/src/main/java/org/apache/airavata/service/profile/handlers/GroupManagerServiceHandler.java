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
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.group.GroupModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.exception.GroupManagerServiceException;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupManagerServiceHandler implements GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);
    private org.apache.airavata.service.GroupManagerService groupManagerService;

    public GroupManagerServiceHandler() {
        groupManagerService = new org.apache.airavata.service.GroupManagerService();
    }

    @Override
    public String getAPIVersion() throws TException {
        return group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(AuthzToken authzToken, GroupModel groupModel)
            throws GroupManagerServiceException, AuthorizationException, TException {
        try {
            return groupManagerService.createGroup(authzToken, groupModel);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.updateGroup(authzToken, groupModel);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.deleteGroup(authzToken, groupId, ownerId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.getGroup(authzToken, groupId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
        try {
            return groupManagerService.getGroups(authzToken);
        } catch (GroupManagerServiceException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error Retrieving Groups";
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
            return groupManagerService.getAllGroupsUserBelongs(authzToken, userName);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.addUsersToGroup(authzToken, userIds, groupId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.removeUsersFromGroup(authzToken, userIds, groupId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.transferGroupOwnership(authzToken, groupId, newOwnerId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.addGroupAdmins(authzToken, groupId, adminIds);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.removeGroupAdmins(authzToken, groupId, adminIds);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.hasAdminAccess(authzToken, groupId, adminId);
        } catch (GroupManagerServiceException e) {
            throw e;
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
            return groupManagerService.hasOwnerAccess(authzToken, groupId, ownerId);
        } catch (GroupManagerServiceException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Error Checking Owner Access for the Group. Group ID: " + groupId + " Owner ID: " + ownerId;
            logger.error(msg, e);
            GroupManagerServiceException exception = new GroupManagerServiceException();
            exception.setMessage(msg + " More info : " + e.getMessage());
            throw exception;
        }
    }
}
