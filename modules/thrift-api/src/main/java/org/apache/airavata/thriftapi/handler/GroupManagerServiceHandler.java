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

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.profile.exception.GroupManagerServiceException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.sharing.model.SharingRegistryException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException();
            ex.setMessage("Error from Sharing Registry: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AuthorizationException) {
            var ex = new org.apache.airavata.thriftapi.exception.AuthorizationException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
    }

    @Override
    @SecurityCheck
    public String createGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.GroupModel groupModel)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroup = groupModelMapper.toDomain(groupModel);
            return groupManagerService.createGroup(domainAuthzToken, domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroup = groupModelMapper.toDomain(groupModel);
            return groupManagerService.updateGroup(domainAuthzToken, domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String ownerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.deleteGroup(domainAuthzToken, groupId, ownerId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.GroupModel getGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroup = groupManagerService.getGroup(domainAuthzToken, groupId);
            return groupModelMapper.toThrift(domainGroup);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupModel> getGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroups = groupManagerService.getGroups(domainAuthzToken);
            return domainGroups.stream().map(groupModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupModel> getAllGroupsUserBelongs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userName)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroups = groupManagerService.getAllGroupsUserBelongs(domainAuthzToken, userName);
            return domainGroups.stream().map(groupModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean addUsersToGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.addUsersToGroup(domainAuthzToken, userIds, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean removeUsersFromGroup(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, List<String> userIds, String groupId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.removeUsersFromGroup(domainAuthzToken, userIds, groupId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean transferGroupOwnership(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String newOwnerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.transferGroupOwnership(domainAuthzToken, groupId, newOwnerId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean addGroupAdmins(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.addGroupAdmins(domainAuthzToken, groupId, adminIds);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupAdmins(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, List<String> adminIds)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.removeGroupAdmins(domainAuthzToken, groupId, adminIds);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean hasAdminAccess(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String adminId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.hasAdminAccess(domainAuthzToken, groupId, adminId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean hasOwnerAccess(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupId, String ownerId)
            throws org.apache.airavata.thriftapi.profile.exception.GroupManagerServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return groupManagerService.hasOwnerAccess(domainAuthzToken, groupId, ownerId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
