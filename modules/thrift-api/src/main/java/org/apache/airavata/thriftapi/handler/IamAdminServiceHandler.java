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

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.profile.exception.IamAdminServicesException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.credential.exception.CredentialStoreException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException();
            ex.setMessage("Credential Store Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AuthExceptions.AuthorizationException) {
            var ex = new org.apache.airavata.thriftapi.exception.AuthorizationException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.Gateway setUpGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway gateway)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateway = gatewayMapper.toDomain(gateway);
            var result = iamAdminService.setUpGateway(domainAuthzToken, domainGateway);
            return gatewayMapper.toThrift(result);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUsernameAvailable(domainAuthzToken, username);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.registerUser(
                    domainAuthzToken, username, emailAddress, firstName, lastName, newPassword);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.enableUser(domainAuthzToken, username);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUserEnabled(domainAuthzToken, username);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.isUserExist(domainAuthzToken, username);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.UserProfile getUser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = iamAdminService.getUser(domainAuthzToken, username);
            return userProfileMapper.toThrift(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> getUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, int offset, int limit, String search)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfiles = iamAdminService.getUsers(domainAuthzToken, offset, limit, search);
            return domainProfiles.stream().map(userProfileMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username, String newPassword)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.resetUserPassword(domainAuthzToken, username, newPassword);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> findUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String email, String userId)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfiles = iamAdminService.findUsers(domainAuthzToken, email, userId);
            return domainProfiles.stream().map(userProfileMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = userProfileMapper.toDomain(userDetails);
            iamAdminService.updateUserProfile(domainAuthzToken, domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String username)
            throws org.apache.airavata.thriftapi.profile.exception.IamAdminServicesException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return iamAdminService.deleteUser(domainAuthzToken, username);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
