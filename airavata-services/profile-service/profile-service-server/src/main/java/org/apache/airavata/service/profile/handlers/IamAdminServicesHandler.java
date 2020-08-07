/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.CustosToAiravataDataModelMapper;
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.custos.iam.service.FindUsersResponse;
import org.apache.custos.iam.service.OperationStatus;
import org.apache.custos.iam.service.RegisterUserResponse;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.user.management.client.UserManagementClient;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IamAdminServicesHandler implements IamAdminServices.Iface {

    private final static Logger logger = LoggerFactory.getLogger(IamAdminServicesHandler.class);
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.IAM_ADMIN);

    private UserManagementClient userManagementClient;

    public IamAdminServicesHandler() {
        try {
            userManagementClient = CustosUtils.getCustosClientProvider().getUserManagementClient();

        } catch (Exception ex) {
            logger.error("Error while initiating Custos User management client ");
        }
    }

    @Override
    public String getAPIVersion() throws TException {
        return iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
    }

//    @Override
//    @SecurityCheck
//    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway) throws IamAdminServicesException, AuthorizationException {
//        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
//        PasswordCredential isSuperAdminCredentials = getSuperAdminPasswordCredential();
//        try {
//            keycloakclient.addTenant(isSuperAdminCredentials, gateway);
//
//            // Load the tenant admin password stored in gateway request
//            CredentialStoreService.Client credentialStoreClient = getCredentialStoreServiceClient();
//            // Admin password token should already be stored under requested gateway's gatewayId
//            PasswordCredential tenantAdminPasswordCredential = credentialStoreClient.getPasswordCredential(gateway.getIdentityServerPasswordToken(), gateway.getGatewayId());
//
//            if (!keycloakclient.createTenantAdminAccount(isSuperAdminCredentials, gateway, tenantAdminPasswordCredential.getPassword())) {
//                logger.error("Admin account creation failed !!, please refer error logs for reason");
//            }
//            Gateway gatewayWithIdAndSecret = keycloakclient.configureClient(isSuperAdminCredentials, gateway);
//            return gatewayWithIdAndSecret;
//        } catch (TException | ApplicationSettingsException ex) {
//            logger.error("Gateway Setup Failed, reason: " + ex.getMessage(), ex);
//            IamAdminServicesException iamAdminServicesException = new IamAdminServicesException(ex.getMessage());
//            throw iamAdminServicesException;
//        }
//    }

    @Override
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway) throws IamAdminServicesException, AuthorizationException, TException {
        return null;
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            OperationStatus status =
                    CustosUtils.getCustosClientProvider().getUserManagementClient().isUsernameAvailable(username, custosId);
            return status.getStatus();
        } catch (Exception e) {
            logger.error("Username checking error, reason: " + e.getMessage(), e);
            IamAdminServicesException iamAdminServicesException = new IamAdminServicesException(e.getMessage());
            throw iamAdminServicesException;
        }
    }

    //ToDo: Will only be secure when using SSL between PGA and Airavata
    @Override
    @SecurityCheck
    public boolean registerUser(AuthzToken authzToken, String username, String emailAddress, String firstName, String lastName, String newPassword) throws IamAdminServicesException, AuthorizationException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

        try {
            RegisterUserResponse response = userManagementClient.registerUser(username,
                    firstName, lastName, newPassword, emailAddress, false, custosId);

            return response.getIsRegistered();

        } catch (Exception ex) {
            String msg = "Error while registering user into Custos Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean enableUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            UserRepresentation representation = userManagementClient.enableUser(username, custosId);
            if (representation != null && representation.getUsername() != null && !
                    representation.getUsername().trim().equals("")) {

                UserProfile profile = CustosToAiravataDataModelMapper.transform(representation, gatewayId);
                profile.setAiravataInternalUserId(profile.getUserId()+"@"+gatewayId);
                dbEventPublisherUtils.publish(EntityType.USER_PROFILE, CrudType.CREATE, profile);
                return true;
            }

            return false;

        } catch (Exception e) {
            String msg = "Error while enabling user account, reason: " + e.getMessage();
            logger.error(msg, e);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserEnabled(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {
            OperationStatus status = userManagementClient.isUserEnabled(username, custosId);
            return status.getStatus();
        } catch (Exception ex) {
            String msg = "Error while checking if user account is enabled, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserExist(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

        try {
            FindUsersResponse response = userManagementClient.
                    findUsers(null, username, null, null, null, 0, 1, custosId);
            if (!response.getUsersList().isEmpty()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            String msg = "Error while checking if user account exists, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public UserProfile getUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            UserRepresentation userRepresentation = userManagementClient.getUser(username, custosId);
            return CustosToAiravataDataModelMapper.transform(userRepresentation, gatewayId);

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
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            FindUsersResponse response = userManagementClient.findUsers(search, null,
                    null, null, null, offset, limit, custosId);
            List<UserProfile> userProfiles = new ArrayList<>();
            if (response.getUsersList() != null && !response.getUsersList().isEmpty()) {
                for (UserRepresentation representation : response.getUsersList()) {
                    userProfiles.add(CustosToAiravataDataModelMapper.transform(representation, gatewayId));
                }
            }
            return userProfiles;
        } catch (Exception ex) {
            String msg = "Error while retrieving user profile from IAM backend, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {
            OperationStatus status = userManagementClient.resetUserPassword(username, newPassword, custosId);
            return status.getStatus();

        } catch (Exception ex) {
            String msg = "Error while resetting user password in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            FindUsersResponse response = userManagementClient.findUsers(null, userId,
                    null, null, email, 0, -1, custosId);
            List<UserProfile> userProfiles = new ArrayList<>();
            if (response.getUsersList() != null && !response.getUsersList().isEmpty()) {
                for (UserRepresentation representation : response.getUsersList()) {
                    userProfiles.add(CustosToAiravataDataModelMapper.transform(representation, gatewayId));
                }
            }
            return userProfiles;
        } catch (Exception ex) {
            String msg = "Error while retrieving users from Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {
            userManagementClient.updateUserProfile(userDetails.getUserId(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getEmails().get(0),
                    custosId);
        } catch (Exception ex) {
            String msg = "Error while updating user profile in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {

            OperationStatus status = userManagementClient.deleteUser(username, custosId, authzToken.getAccessToken());
            return status.getStatus();
        } catch (Exception ex) {
            String msg = "Error while deleting user  in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        return false;
    }

    @Override
    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        return false;
    }

    @Override
    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        return null;
    }

//    @Override
//    @SecurityCheck
//    @Deprecated
//    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
//        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
//        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
//        try {
//            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
//            return keycloakclient.addRoleToUser(isRealmAdminCredentials, gatewayId, username, roleName);
//        } catch (TException | ApplicationSettingsException ex) {
//            String msg = "Error while adding role to user, reason: " + ex.getMessage();
//            logger.error(msg, ex);
//            throw new IamAdminServicesException(msg);
//        }
//    }
//
//    @Override
//    @SecurityCheck
//    @Deprecated
//    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
//        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
//        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
//        try {
//            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
//            return keycloakclient.removeRoleFromUser(isRealmAdminCredentials, gatewayId, username, roleName);
//        } catch (TException | ApplicationSettingsException ex) {
//            String msg = "Error while removing role from user, reason: " + ex.getMessage();
//            logger.error(msg, ex);
//            throw new IamAdminServicesException(msg);
//        }
//    }
//
//    @Override
//    @SecurityCheck
//    @Deprecated
//    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
//
//        TenantManagementKeycloakImpl keycloakclient = new TenantManagementKeycloakImpl();
//        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
//        try {
//            PasswordCredential isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
//            return keycloakclient.getUsersWithRole(isRealmAdminCredentials, gatewayId, roleName);
//        } catch (Exception ex) {
//            String msg = "Error while retrieving users with role, reason: " + ex.getMessage();
//            logger.error(msg, ex);
//            throw new IamAdminServicesException(msg);
//        }
//    }

//    private PasswordCredential getSuperAdminPasswordCredential() {
//        PasswordCredential isSuperAdminCredentials = new PasswordCredential();
//        try {
//            isSuperAdminCredentials.setLoginUserName(ServerSettings.getIamServerSuperAdminUsername());
//            isSuperAdminCredentials.setPassword(ServerSettings.getIamServerSuperAdminPassword());
//        } catch (ApplicationSettingsException e) {
//            throw new RuntimeException("Unable to get settings for IAM super admin username/password", e);
//        }
//        return isSuperAdminCredentials;
//    }

//    private PasswordCredential getTenantAdminPasswordCredential(String tenantId) throws TException, ApplicationSettingsException {
//
//        GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(tenantId);
//
//        CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
//        return csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
//    }
//
//    private RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
//        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
//        final String serverHost = ServerSettings.getRegistryServerHost();
//        try {
//            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
//        } catch (RegistryServiceException e) {
//            throw new TException("Unable to create registry client...", e);
//        }
//    }
//
//    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
//        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
//        final String serverHost = ServerSettings.getCredentialStoreServerHost();
//        try {
//            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
//        } catch (CredentialStoreException e) {
//            throw new TException("Unable to create credential store client...", e);
//        }
//    }
}
