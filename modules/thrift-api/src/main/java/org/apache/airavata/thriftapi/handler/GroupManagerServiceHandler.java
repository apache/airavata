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
package org.apache.airavata.thriftapi.handler;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.service.security.GroupManagerService;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.thriftapi.mapper.AuthzTokenMapper;
import org.apache.airavata.thriftapi.mapper.GroupModelMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GroupManagerServiceHandler
        implements org.apache.airavata.thriftapi.profile.model.GroupManagerService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(GroupManagerServiceHandler.class);
    private final GroupManagerService groupManagerService;
    private final AuthzTokenMapper authzTokenMapper = AuthzTokenMapper.INSTANCE;
    private final GroupModelMapper groupModelMapper = GroupModelMapper.INSTANCE;

    public GroupManagerServiceHandler(GroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
        logger.info("GroupManagerServiceHandler initialized with Spring-injected GroupManagerService");
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.profile.model.group_manager_cpiConstants.GROUP_MANAGER_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String createGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.GroupModel groupModel)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupModel domainGroup = groupModelMapper.toDomain(groupModel);
            return groupManagerService.createGroup(domainAuthzToken, domainGroup);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error creating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error creating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.GroupModel groupModel)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupModel domainGroup = groupModelMapper.toDomain(groupModel);
            return groupManagerService.updateGroup(domainAuthzToken, domainGroup);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error updating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error updating group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String ownerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.deleteGroup(domainAuthzToken, groupId, ownerId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error deleting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error deleting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.GroupModel getGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupModel domainGroup =
                    groupManagerService.getGroup(domainAuthzToken, groupId);
            return groupModelMapper.toThrift(domainGroup);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupModel> getGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.GroupModel> domainGroups =
                    groupManagerService.getGroups(domainAuthzToken);
            return domainGroups.stream().map(groupModelMapper::toThrift).collect(Collectors.toList());
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupModel> getAllGroupsUserBelongs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userName)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.GroupModel> domainGroups =
                    groupManagerService.getAllGroupsUserBelongs(domainAuthzToken, userName);
            return domainGroups.stream().map(groupModelMapper::toThrift).collect(Collectors.toList());
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting user groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error getting user groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public boolean addUsersToGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.addUsersToGroup(domainAuthzToken, userIds, groupId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error adding users to group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error adding users to group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public boolean removeUsersFromGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.removeUsersFromGroup(domainAuthzToken, userIds, groupId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error removing users from group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error removing users from group: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String newOwnerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.transferGroupOwnership(domainAuthzToken, groupId, newOwnerId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error transferring group ownership: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error transferring group ownership: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.addGroupAdmins(domainAuthzToken, groupId, adminIds);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error adding group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error adding group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.removeGroupAdmins(domainAuthzToken, groupId, adminIds);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error removing group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error removing group admins: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String adminId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.hasAdminAccess(domainAuthzToken, groupId, adminId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error checking admin access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error checking admin access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String ownerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.hasOwnerAccess(domainAuthzToken, groupId, ownerId);
        } catch (SharingRegistryException e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error checking owner access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            throw convertToThriftGroupManagerServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException(
                            "Error checking owner access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // Helper methods for exception conversion
    private org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException
            convertToThriftGroupManagerServiceException(
                    org.apache.airavata.profile.exception.GroupManagerServiceException e) {
        org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException thriftException =
                new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.AuthorizationException convertToThriftAuthorizationException(
            org.apache.airavata.common.exception.AuthorizationException e) {
        org.apache.airavata.thriftapi.exception.AuthorizationException thriftException =
                new org.apache.airavata.thriftapi.exception.AuthorizationException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }
}
