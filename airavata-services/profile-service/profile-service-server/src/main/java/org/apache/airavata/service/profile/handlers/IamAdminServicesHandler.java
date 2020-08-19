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

import org.apache.airavata.common.exception.ApplicationSettingsException;
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
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.custos.iam.service.FindUsersResponse;
import org.apache.custos.iam.service.OperationStatus;
import org.apache.custos.iam.service.RegisterUserResponse;
import org.apache.custos.iam.service.UserRepresentation;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementClient;
import org.apache.custos.resource.secret.service.AddResourceCredentialResponse;
import org.apache.custos.tenant.management.service.CreateTenantResponse;
import org.apache.custos.tenant.manamgement.client.TenantManagementClient;
import org.apache.custos.user.management.client.UserManagementClient;
import org.apache.custos.user.profile.service.GetAllUserProfilesResponse;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IamAdminServicesHandler implements IamAdminServices.Iface {

    private final static Logger logger = LoggerFactory.getLogger(IamAdminServicesHandler.class);
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.IAM_ADMIN);

    private UserManagementClient userManagementClient;
    private TenantManagementClient tenantManagementClient;
    private ResourceSecretManagementClient resourceSecretManagementClient;

    public IamAdminServicesHandler() {
        try {
            userManagementClient = CustosUtils.getCustosClientProvider().getUserManagementClient();
            tenantManagementClient = CustosUtils.getCustosClientProvider().getTenantManagementClient();
            resourceSecretManagementClient = CustosUtils.getCustosClientProvider().getResourceSecretManagementClient();

        } catch (Exception ex) {
            logger.error("Error while initiating Custos User management client ");
        }
    }

    @Override
    public String getAPIVersion() throws TException {
        return iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_VERSION;
    }


    @Override
    @SecurityCheck
    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            String[] contacts = {gateway.getEmailAddress()};
            String comment = "Airavata gateway internal id " + gateway.getGatewayId();

            String domain = gateway.getDomain();
            if (gateway.getDomain() == null || gateway.getDomain().trim().equals("")) {
                String gatewayURL = gateway.getGatewayURL();
                domain = gatewayURL.substring(gatewayURL.lastIndexOf("://"), gatewayURL.lastIndexOf("/"));
            }
            String adminPassword = getAdminPassword(authzToken, gateway);
            CreateTenantResponse response = tenantManagementClient.registerTenant(gateway.getGatewayName(),
                    gateway.getRequesterUsername(),
                    gateway.getGatewayAdminFirstName(),
                    gateway.getGatewayAdminLastName(),
                    gateway.getEmailAddress(),
                    gateway.getIdentityServerUserName(),
                    adminPassword,
                    contacts,
                    gateway.getRedirectURLs().toArray(new String[gateway.getRedirectURLs().size()]),
                    gateway.getGatewayURL(),
                    gateway.getScope(),
                    domain,
                    gateway.getGatewayURL(),
                    comment);

            copyAdminPasswordToGateway(authzToken, gateway, response.getClientId());
            gateway.setOauthClientId(response.getClientId());
            gateway.setOauthClientSecret(response.getClientSecret());

            dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.UPDATE, gateway);

            return gateway;
        } catch (Exception e) {
            logger.error("Gateway creating error, reason: " + e.getMessage(), e);
            IamAdminServicesException iamAdminServicesException = new IamAdminServicesException(e.getMessage());
            throw iamAdminServicesException;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException, AuthorizationException, TException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
            OperationStatus status =
                    userManagementClient.isUsernameAvailable(username, custosId);
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
                profile.setAiravataInternalUserId(profile.getUserId() + "@" + gatewayId);
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
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {
            String[] usernames = {username};
            String[] roles = {roleName};


            OperationStatus status = userManagementClient.addRolesToUsers(roles, usernames, false, custosId, authzToken.getAccessToken());

            return status.getStatus();
        } catch (Exception ex) {
            String msg = "Error while deleting user  in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        try {
            String[] roles = {roleName};
            String[] clientRoles = {};

            OperationStatus status = userManagementClient.deleteUserRoles(clientRoles, roles, username, custosId, authzToken.getAccessToken());

            return status.getStatus();
        } catch (Exception ex) {
            String msg = "Error while deleting user  in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }
    }

    @Override
    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName) throws IamAdminServicesException, AuthorizationException, TException {
        String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {

            GetAllUserProfilesResponse response = userManagementClient.getAllUserProfiles(custosId);

            List<org.apache.custos.user.profile.service.UserProfile> userProfileList = response.getProfilesList();

            List<org.apache.custos.user.profile.service.UserProfile> selectedList = new ArrayList<>();

            List<UserProfile> profiles = new ArrayList<>();

            if (userProfileList != null && !userProfileList.isEmpty()) {

                for (org.apache.custos.user.profile.service.UserProfile profile : userProfileList) {
                    if (profile.getRealmRolesList() != null && !profile.getRealmRolesList().isEmpty()) {
                        for (String realmRole : profile.getRealmRolesList()) {
                            if (realmRole.equals(roleName)) {
                                selectedList.add(profile);
                            }
                        }
                    }

                }

                for (org.apache.custos.user.profile.service.UserProfile profile : selectedList) {
                    profiles.add(CustosToAiravataDataModelMapper.transform(profile, gatewayId));
                }


            }

            return profiles;
        } catch (Exception ex) {
            String msg = "Error while deleting user  in Identity Server, reason: " + ex.getMessage();
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg);
        }


    }

    private String getAdminPassword(AuthzToken authzToken, Gateway gateway) throws TException, ApplicationSettingsException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            org.apache.custos.resource.secret.service.PasswordCredential passwordCredential =
                    resourceSecretManagementClient.getPasswordCredential(custosId, gateway.getIdentityServerPasswordToken());

            return passwordCredential.getPassword();
        } catch (Exception ex) {
            logger.error("Unable to fetch admin password credential, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Unable to fetch admin password credential, reason: " + ex.getMessage());
            throw exception;
        }
    }

    private void copyAdminPasswordToGateway(AuthzToken authzToken, Gateway gateway, String createdGatewayCustosId) throws TException, ApplicationSettingsException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            org.apache.custos.resource.secret.service.PasswordCredential passwordCredential =
                    resourceSecretManagementClient.getPasswordCredential(custosId, gateway.getIdentityServerPasswordToken());

            AddResourceCredentialResponse response = resourceSecretManagementClient.addPasswordCredential(createdGatewayCustosId,
                    passwordCredential.getMetadata().getDescription(),
                    gateway.getIdentityServerUserName(),
                    passwordCredential.getPassword());
            gateway.setIdentityServerPasswordToken(response.getToken());
        } catch (Exception ex) {
            logger.error("Unable to save admin password credential, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Unable to save admin password credential, reason: " + ex.getMessage());
            throw exception;
        }
    }

}
