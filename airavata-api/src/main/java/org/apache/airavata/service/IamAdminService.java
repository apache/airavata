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
package org.apache.airavata.service;

import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.profile.utils.TenantManagementKeycloakImpl;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IamAdminService {
    private static final Logger logger = LoggerFactory.getLogger(IamAdminService.class);

    @Autowired
    private AiravataServerProperties properties;

    @Autowired
    private UserProfileService userProfileService;

    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.IAM_ADMIN);

    @Autowired
    private final CredentialStoreService credentialStoreService;

    @Autowired
    private final RegistryService registryService;

    public IamAdminService(CredentialStoreService credentialStoreService, RegistryService registryService) {
        this.credentialStoreService = credentialStoreService;
        this.registryService = registryService;
    }

    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway)
            throws IamAdminServicesException, CredentialStoreException {
        var keycloakclient = new TenantManagementKeycloakImpl();
        PasswordCredential isSuperAdminCredentials;
        try {
            isSuperAdminCredentials = getSuperAdminPasswordCredential();
        } catch (IamAdminServicesException e) {
            String msg = String.format(
                    "Error getting super admin credentials for gateway setup: gatewayId=%s, gatewayName=%s. Reason: %s",
                    gateway.getGatewayId(), gateway.getGatewayName(), e.getMessage());
            logger.error(msg, e);
            throw e;
        }

        try {
            keycloakclient.addTenant(isSuperAdminCredentials, gateway);

            // Load the tenant admin password stored in gateway request
            // Admin password token should already be stored under requested gateway's gatewayId
            var tenantAdminPasswordCredential = credentialStoreService.getPasswordCredential(
                    gateway.getIdentityServerPasswordToken(), gateway.getGatewayId());

            if (!keycloakclient.createTenantAdminAccount(
                    isSuperAdminCredentials, gateway, tenantAdminPasswordCredential.getPassword())) {
                logger.error("Admin account creation failed !!, please refer error logs for reason");
            }
            var gatewayWithIdAndSecret = keycloakclient.configureClient(isSuperAdminCredentials, gateway);
            return gatewayWithIdAndSecret;
        } catch (CredentialStoreException e) {
            throw e;
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Gateway Setup Failed: %s", ex.getMessage());
            logger.error(msg, ex);
            IamAdminServicesException exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException {
        try {
            var keycloakClient = new TenantManagementKeycloakImpl();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return keycloakClient.isUsernameAvailable(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException e) {
            throw e;
        }
    }

    public boolean registerUser(
            AuthzToken authzToken,
            String username,
            String emailAddress,
            String firstName,
            String lastName,
            String newPassword)
            throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.createUser(
                    authzToken.getAccessToken(), gatewayId, username, emailAddress, firstName, lastName, newPassword))
                return true;
            else return false;
        } catch (IamAdminServicesException ex) {
            String msg =
                    String.format("Error while registering user=%s into IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean enableUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.enableUserAccount(authzToken.getAccessToken(), gatewayId, username)) {
                // Check if user profile exists, if not create it
                boolean userProfileExists =
                        userProfileService.getUserProfileByIdAndGateWay(username, gatewayId) != null;
                if (!userProfileExists) {
                    // Load basic user profile information from Keycloak and then save in UserProfileRepository
                    UserProfile userProfile = keycloakclient.getUser(authzToken.getAccessToken(), gatewayId, username);
                    userProfile.setCreationTime(
                            AiravataUtils.getCurrentTimestamp().getTime());
                    userProfile.setLastAccessTime(
                            AiravataUtils.getCurrentTimestamp().getTime());
                    userProfile.setValidUntil(-1);
                    userProfileService.createUserProfile(userProfile);
                    // Dispatch IAM_ADMIN service event for a new USER_PROFILE
                    dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, userProfile);
                }
                return true;
            } else {
                return false;
            }
        } catch (IamAdminServicesException ex) {
            throw ex;
        } catch (AiravataException ex) {
            String msg = String.format("Error while enabling user=%s in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean isUserEnabled(AuthzToken authzToken, String username) throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.isUserAccountEnabled(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while checking if user=%s is enabled in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean isUserExist(AuthzToken authzToken, String username) throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.isUserExist(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while checking if user=%s exists in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public UserProfile getUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.getUser(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while retrieving user=%s profile from IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.getUsers(authzToken.getAccessToken(), gatewayId, offset, limit, search);
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while retrieving users from IAM backend: %s", ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword)
            throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.resetUserPassword(authzToken.getAccessToken(), gatewayId, username, newPassword))
                return true;
            else return false;
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while resetting user=%s password in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId)
            throws IamAdminServicesException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.findUser(authzToken.getAccessToken(), gatewayId, email, userId);
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while finding users in IAM backend: %s", ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException {
        try {
            TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
            String username = userDetails.getUserId();
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            keycloakclient.updateUserProfile(authzToken.getAccessToken(), gatewayId, username, userDetails);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while updating user=%s profile in IAM backend: %s",
                    userDetails.getUserId(), ex.getMessage());
            logger.error(msg, ex);
            var exception = new IamAdminServicesException(msg);
            exception.initCause(ex);
            throw exception;
        }
    }

    public boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        try {
            TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
            String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return keycloakclient.deleteUser(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while deleting user=%s in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw ex;
        }
    }

    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryServiceException, CredentialStoreException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
        return keycloakclient.addRoleToUser(isRealmAdminCredentials, gatewayId, username, roleName);
    }

    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryServiceException, CredentialStoreException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
        return keycloakclient.removeRoleFromUser(isRealmAdminCredentials, gatewayId, username, roleName);
    }

    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName)
            throws IamAdminServicesException, RegistryServiceException, CredentialStoreException {
        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
        return keycloakclient.getUsersWithRole(isRealmAdminCredentials, gatewayId, roleName);
    }

    private PasswordCredential getSuperAdminPasswordCredential() throws IamAdminServicesException {
        PasswordCredential isSuperAdminCredentials = new PasswordCredential();
        isSuperAdminCredentials.setLoginUserName(properties.getIam().getSuperAdminUsername());
        isSuperAdminCredentials.setPassword(properties.getIam().getSuperAdminPassword());
        return isSuperAdminCredentials;
    }

    private PasswordCredential getTenantAdminPasswordCredential(String tenantId)
            throws IamAdminServicesException, RegistryServiceException, CredentialStoreException {
        GatewayResourceProfile gwrp = registryService.getGatewayResourceProfile(tenantId);
        return credentialStoreService.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
    }
}
