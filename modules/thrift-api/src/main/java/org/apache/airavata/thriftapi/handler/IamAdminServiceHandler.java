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
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.exception.AuthorizationException;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.security.IamAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IamAdminServiceHandler implements org.apache.airavata.thriftapi.profile.model.IamAdminServices.Iface {

    private static final Logger logger = LoggerFactory.getLogger(IamAdminServiceHandler.class);
    private final IamAdminService iamAdminService;

    public IamAdminServiceHandler(IamAdminService iamAdminService) {
        this.iamAdminService = iamAdminService;
        logger.info("IamAdminServiceHandler initialized with Spring-injected IamAdminService");
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return org.apache.airavata.profile.model.iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway)
            throws IamAdminServicesException, AuthorizationException {
        try {
            return iamAdminService.setUpGateway(authzToken, gateway);
        } catch (CredentialStoreException e) {
            IamAdminServicesException ex = new IamAdminServicesException("Error setting up gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.isUsernameAvailable(authzToken, username);
    }

    @Override
    @SecurityCheck
    public boolean registerUser(
            AuthzToken authzToken,
            String username,
            String emailAddress,
            String firstName,
            String lastName,
            String newPassword)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.registerUser(authzToken, username, emailAddress, firstName, lastName, newPassword);
    }

    @Override
    @SecurityCheck
    public boolean enableUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.enableUser(authzToken, username);
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.isUserEnabled(authzToken, username);
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.isUserExist(authzToken, username);
    }

    @Override
    @SecurityCheck
    public UserProfile getUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.getUser(authzToken, username);
    }

    @Override
    @SecurityCheck
    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.getUsers(authzToken, offset, limit, search);
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.resetUserPassword(authzToken, username, newPassword);
    }

    @Override
    @SecurityCheck
    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.findUsers(authzToken, email, userId);
    }

    @Override
    @SecurityCheck
    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails)
            throws IamAdminServicesException, AuthorizationException {
        iamAdminService.updateUserProfile(authzToken, userDetails);
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        return iamAdminService.deleteUser(authzToken, username);
    }
}
