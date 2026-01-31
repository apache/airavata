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
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.thriftapi.mapper.AuthzTokenMapper;
import org.apache.airavata.thriftapi.mapper.UserProfileMapper;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class UserProfileServiceHandler implements org.apache.airavata.thriftapi.profile.model.UserProfileService.Iface {

    private final UserProfileService userProfileService;
    private final AuthzTokenMapper authzTokenMapper = AuthzTokenMapper.INSTANCE;
    private final UserProfileMapper userProfileMapper = UserProfileMapper.INSTANCE;

    public UserProfileServiceHandler(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.profile.model.profile_user_cpiConstants.USER_PROFILE_CPI_VERSION;
    }

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.profile.exception.UserProfileServiceException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.profile.exception.IamAdminServicesException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException();
            ex.setMessage("IAM Admin Services Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AuthExceptions.AuthorizationException) {
            var ex = new org.apache.airavata.thriftapi.exception.AuthorizationException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
    }

    @Override
    @SecurityCheck
    public String initializeUserProfile(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return userProfileService.initializeUserProfile(domainAuthzToken);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public String addUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.UserProfile userProfile)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = userProfileMapper.toDomain(userProfile);
            return userProfileService.addUserProfile(domainAuthzToken, domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.UserProfile userProfile)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = userProfileMapper.toDomain(userProfile);
            return userProfileService.updateUserProfile(domainAuthzToken, domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.UserProfile getUserProfileById(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = userProfileService.getUserProfileById(domainAuthzToken, userId, gatewayId);
            return userProfileMapper.toThrift(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return userProfileService.deleteUserProfile(domainAuthzToken, userId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> getAllUserProfilesInGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfiles =
                    userProfileService.getAllUserProfilesInGateway(domainAuthzToken, gatewayId, offset, limit);
            return domainProfiles.stream().map(userProfileMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean doesUserExist(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return userProfileService.doesUserExist(domainAuthzToken, userId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
