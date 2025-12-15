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
import org.apache.airavata.service.security.IamAdminService;
import org.apache.airavata.thriftapi.mapper.AuthzTokenMapper;
import org.apache.airavata.thriftapi.mapper.GatewayMapper;
import org.apache.airavata.thriftapi.mapper.UserProfileMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IamAdminServiceHandler implements org.apache.airavata.thriftapi.profile.model.IamAdminServices.Iface {

    private static final Logger logger = LoggerFactory.getLogger(IamAdminServiceHandler.class);
    private final IamAdminService iamAdminService;
    private final AuthzTokenMapper authzTokenMapper = AuthzTokenMapper.INSTANCE;
    private final GatewayMapper gatewayMapper = GatewayMapper.INSTANCE;
    private final UserProfileMapper userProfileMapper = UserProfileMapper.INSTANCE;

    public IamAdminServiceHandler(IamAdminService iamAdminService) {
        this.iamAdminService = iamAdminService;
        logger.info("IamAdminServiceHandler initialized with Spring-injected IamAdminService");
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache
                .airavata
                .thriftapi
                .profile
                .model
                .iam_admin_services_cpiConstants
                .IAM_ADMIN_SERVICES_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.Gateway setUpGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway gateway)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(gateway);
            org.apache.airavata.common.model.Gateway result =
                    iamAdminService.setUpGateway(domainAuthzToken, domainGateway);
            return gatewayMapper.toThrift(result);
        } catch (org.apache.airavata.common.exception.CredentialStoreException e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error setting up gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error setting up gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUsernameAvailable(domainAuthzToken, username);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error checking username availability: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean registerUser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String username,
            String emailAddress,
            String firstName,
            String lastName,
            String newPassword)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.registerUser(
                    domainAuthzToken, username, emailAddress, firstName, lastName, newPassword);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error registering user: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.enableUser(domainAuthzToken, username);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error enabling user: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUserEnabled(domainAuthzToken, username);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error checking if user is enabled: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUserExist(domainAuthzToken, username);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error checking if user exists: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.UserProfile getUser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserProfile domainProfile =
                    iamAdminService.getUser(domainAuthzToken, username);
            return userProfileMapper.toThrift(domainProfile);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error getting user: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> getUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, int offset, int limit, String search)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.UserProfile> domainProfiles =
                    iamAdminService.getUsers(domainAuthzToken, offset, limit, search);
            return domainProfiles.stream().map(userProfileMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error getting users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username, String newPassword)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.resetUserPassword(domainAuthzToken, username, newPassword);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error resetting user password: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> findUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String email, String userId)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.UserProfile> domainProfiles =
                    iamAdminService.findUsers(domainAuthzToken, email, userId);
            return domainProfiles.stream().map(userProfileMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error finding users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void updateUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.UserProfile userDetails)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserProfile domainProfile = userProfileMapper.toDomain(userDetails);
            iamAdminService.updateUserProfile(domainAuthzToken, domainProfile);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error updating user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.deleteUser(domainAuthzToken, username);
        } catch (org.apache.airavata.profile.exception.IamAdminServicesException e) {
            throw convertToThriftIamAdminServicesException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException ex =
                    new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException(
                            "Error deleting user: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // Helper methods for exception conversion
    private org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException
            convertToThriftIamAdminServicesException(
                    org.apache.airavata.profile.exception.IamAdminServicesException e) {
        org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException thriftException =
                new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException();
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
