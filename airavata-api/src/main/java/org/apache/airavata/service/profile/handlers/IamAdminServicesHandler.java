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
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IamAdminServicesHandler implements IamAdminServices.Iface {

    private static final Logger logger = LoggerFactory.getLogger(IamAdminServicesHandler.class);
    private org.apache.airavata.service.IamAdminService iamAdminService;

    public IamAdminServicesHandler() {
        iamAdminService = new org.apache.airavata.service.IamAdminService();
    }

    @Override
    public String getAPIVersion() throws TException {
        return iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway)
            throws IamAdminServicesException, AuthorizationException {
        try {
            return iamAdminService.setUpGateway(authzToken, gateway);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Gateway Setup Failed, reason: " + ex.getMessage(), ex);
            IamAdminServicesException iamAdminServicesException = new IamAdminServicesException(ex.getMessage());
            throw iamAdminServicesException;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.isUsernameAvailable(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while checking username availability, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
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
        try {
            return iamAdminService.registerUser(authzToken, username, emailAddress, firstName, lastName, newPassword);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while registering user into Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException {
        try {
            return iamAdminService.enableUser(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while enabling user account, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.isUserEnabled(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while checking if user account is enabled, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.isUserExist(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while checking if user account exists, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public UserProfile getUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.getUser(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while retrieving user profile from IAM backend, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.getUsers(authzToken, offset, limit, search);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while retrieving user profile from IAM backend, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.resetUserPassword(authzToken, username, newPassword);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while resetting user password in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.findUsers(authzToken, email, userId);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while retrieving users from Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            iamAdminService.updateUserProfile(authzToken, userDetails);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while updating user profile, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(AuthzToken authzToken, String username)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.deleteUser(authzToken, username);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while deleting user, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.addRoleToUser(authzToken, username, roleName);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while adding role to user, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.removeRoleFromUser(authzToken, username, roleName);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while removing role from user, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName)
            throws IamAdminServicesException, AuthorizationException, TException {
        try {
            return iamAdminService.getUsersWithRole(authzToken, roleName);
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception ex) {
            String msg = "Error while retrieving users with role, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }
}
