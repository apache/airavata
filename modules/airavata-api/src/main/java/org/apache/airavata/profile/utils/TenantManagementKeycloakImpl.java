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
package org.apache.airavata.profile.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Status;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.utils.keycloak.KeycloakRestClient;
import org.apache.airavata.profile.utils.keycloak.dto.ClientRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.CredentialRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.RealmRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.RoleRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.RolesRepresentation;
import org.apache.airavata.profile.utils.keycloak.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TenantManagementKeycloakImpl implements TenantManagementInterface {

    private static final Logger logger = LoggerFactory.getLogger(TenantManagementKeycloakImpl.class);

    private final String superAdminRealmId = "master";
    private AiravataServerProperties properties;
    private KeycloakRestClient restClient;

    public TenantManagementKeycloakImpl() {}

    public TenantManagementKeycloakImpl(AiravataServerProperties properties) {
        this.properties = properties;
        initializeRestClient();
    }

    public void setProperties(AiravataServerProperties properties) {
        this.properties = properties;
        initializeRestClient();
    }

    private void initializeRestClient() {
        try {
            String serverUrl = getIamServerUrl();
            this.restClient = new KeycloakRestClient(serverUrl, properties);
        } catch (IamAdminServicesException e) {
            logger.warn("Failed to initialize Keycloak REST client: {}", e.getMessage());
        }
    }

    private String getIamServerUrl() throws IamAdminServicesException {
        if (properties == null
                || properties.security() == null
                || properties.security().iam() == null
                || properties.security().iam().serverUrl() == null) {
            throw new IamAdminServicesException(
                    "IAM server URL is not configured. Check airavata.properties for security.iam.server-url");
        }
        return properties.security().iam().serverUrl();
    }

    private KeycloakRestClient getRestClient() throws IamAdminServicesException {
        if (restClient == null) {
            String serverUrl = getIamServerUrl();
            restClient = new KeycloakRestClient(serverUrl, properties);
        }
        return restClient;
    }

    /**
     * Obtain an admin access token for the specified realm using password credentials.
     * This can be used to authenticate API calls to Keycloak.
     *
     * @param credentials Password credentials containing username and password
     * @param realm The realm to authenticate against
     * @return Access token string
     * @throws IamAdminServicesException if authentication fails
     */
    public String getAdminAccessToken(PasswordCredential credentials, String realm) throws IamAdminServicesException {
        KeycloakRestClient client = getRestClient();
        return client.obtainAdminToken(realm, credentials);
    }

    @Override
    public Gateway addTenant(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            // create realm
            RealmRepresentation newRealmDetails = new RealmRepresentation();
            newRealmDetails.setEnabled(true);
            newRealmDetails.setId(gatewayDetails.getGatewayId());
            newRealmDetails.setDisplayName(gatewayDetails.getGatewayName());
            newRealmDetails.setRealm(gatewayDetails.getGatewayId());
            // Following two settings allow duplicate email addresses
            newRealmDetails.setLoginWithEmailAllowed(false);
            newRealmDetails.setDuplicateEmailsAllowed(true);
            // Default access token lifespan to 30 minutes, SSO session idle to 60 minutes
            newRealmDetails.setAccessTokenLifespan(1800);
            newRealmDetails.setSsoSessionIdleTimeout(3600);
            newRealmDetails.setEditUsernameAllowed(true);
            RealmRepresentation realmWithRoles = TenantManagementKeycloakImpl.createDefaultRoles(newRealmDetails);
            client.createRealm(realmWithRoles);
            return gatewayDetails;
        } catch (Exception ex) {
            logger.error("Error creating Realm in Keycloak Server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error creating Realm in Keycloak Server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    public static RealmRepresentation createDefaultRoles(RealmRepresentation realmDetails) {
        List<RoleRepresentation> defaultRoles = new ArrayList<RoleRepresentation>();
        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName("admin");
        adminRole.setDescription("Admin role for PGA users");
        defaultRoles.add(adminRole);
        RoleRepresentation adminReadOnlyRole = new RoleRepresentation();
        adminReadOnlyRole.setName("admin-read-only");
        adminReadOnlyRole.setDescription("Read only role for PGA Admin users");
        defaultRoles.add(adminReadOnlyRole);
        RoleRepresentation gatewayUserRole = new RoleRepresentation();
        gatewayUserRole.setName("gateway-user");
        gatewayUserRole.setDescription("default role for PGA users");
        defaultRoles.add(gatewayUserRole);
        RoleRepresentation pendingUserRole = new RoleRepresentation();
        pendingUserRole.setName("user-pending");
        pendingUserRole.setDescription("role for newly registered PGA users");
        defaultRoles.add(pendingUserRole);
        RoleRepresentation gatewayProviderRole = new RoleRepresentation();
        gatewayProviderRole.setName("gateway-provider");
        gatewayProviderRole.setDescription("role for gateway providers in the super-admin PGA");
        defaultRoles.add(gatewayProviderRole);
        RolesRepresentation rolesRepresentation = new RolesRepresentation();
        rolesRepresentation.setRealm(defaultRoles);
        realmDetails.setRoles(rolesRepresentation);
        return realmDetails;
    }

    @Override
    public boolean createTenantAdminAccount(
            PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails, String tenantAdminPassword)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(this.superAdminRealmId, isSuperAdminPasswordCreds);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(gatewayDetails.getIdentityServerUserName());
            user.setFirstName(gatewayDetails.getGatewayAdminFirstName());
            user.setLastName(gatewayDetails.getGatewayAdminLastName());
            user.setEmail(gatewayDetails.getGatewayAdminEmail());
            user.setEmailVerified(true);
            user.setEnabled(true);
            ResponseEntity<Void> httpResponse = client.createUser(gatewayDetails.getGatewayId(), user, adminToken);
            logger.info("Tenant Admin account creation exited with code : " + httpResponse.getStatusCode());
            if (httpResponse.getStatusCode() == HttpStatus.CREATED) { // HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = client.searchUsers(
                        gatewayDetails.getGatewayId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0,
                        1,
                        adminToken);
                if (!retrieveCreatedUserList.isEmpty()) {
                    UserRepresentation createdUser = retrieveCreatedUserList.get(0);
                    String userId = createdUser.getId();

                    // Add user to the "admin" role
                    RoleRepresentation adminRole = client.getRole(gatewayDetails.getGatewayId(), "admin", adminToken);
                    if (adminRole != null) {
                        client.addRealmRolesToUser(
                                gatewayDetails.getGatewayId(), userId, Arrays.asList(adminRole), adminToken);
                    }

                    CredentialRepresentation credential = new CredentialRepresentation();
                    credential.setType(CredentialRepresentation.PASSWORD);
                    credential.setValue(tenantAdminPassword);
                    credential.setTemporary(false);
                    client.resetPassword(gatewayDetails.getGatewayId(), userId, credential, adminToken);

                    // Add realm-management client roles
                    List<ClientRepresentation> realmClients =
                            client.findAllClients(gatewayDetails.getGatewayId(), adminToken);
                    String realmManagementClientId = null;
                    for (ClientRepresentation realmClient : realmClients) {
                        if (realmClient.getClientId().equals("realm-management")) {
                            realmManagementClientId = realmClient.getId();
                            break;
                        }
                    }
                    if (realmManagementClientId != null) {
                        List<RoleRepresentation> availableRoles = client.getAvailableClientRoles(
                                gatewayDetails.getGatewayId(), userId, realmManagementClientId, adminToken);
                        List<RoleRepresentation> manageRoles = availableRoles.stream()
                                .filter(r -> r.getName().equals("manage-users")
                                        || r.getName().equals("manage-clients"))
                                .collect(Collectors.toList());
                        if (!manageRoles.isEmpty()) {
                            client.addClientRolesToUser(
                                    gatewayDetails.getGatewayId(),
                                    userId,
                                    realmManagementClientId,
                                    manageRoles,
                                    adminToken);
                        }
                    }
                    return true;
                } else {
                    logger.error("Created user not found after creation");
                    return false;
                }
            } else {
                logger.error("Request for Tenant Admin Account Creation failed with HTTP code : "
                        + httpResponse.getStatusCode());
                return false;
            }
        } catch (Exception ex) {
            logger.error("Error creating Realm Admin Account in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error creating Realm Admin Account in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    public Gateway configureClient(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(this.superAdminRealmId, isSuperAdminPasswordCreds);
            ClientRepresentation pgaClient = new ClientRepresentation();
            pgaClient.setName("pga");
            pgaClient.setClientId("pga");
            pgaClient.setProtocol("openid-connect");
            pgaClient.setStandardFlowEnabled(true);
            pgaClient.setEnabled(true);
            pgaClient.setAuthorizationServicesEnabled(true);
            pgaClient.setDirectAccessGrantsEnabled(true);
            pgaClient.setServiceAccountsEnabled(true);
            pgaClient.setFullScopeAllowed(true);
            pgaClient.setClientAuthenticatorType("client-secret");
            List<String> redirectUris = new ArrayList<>();
            if (gatewayDetails.getGatewayURL() != null) {
                String gatewayURL = gatewayDetails.getGatewayURL();
                // Remove trailing slash from gatewayURL
                if (gatewayURL.endsWith("/")) {
                    gatewayURL = gatewayURL.substring(0, gatewayURL.length() - 1);
                }
                // Add redirect URL after login
                redirectUris.add(gatewayURL + "/callback-url"); // PGA
                redirectUris.add(gatewayURL + "/auth/callback*"); // Django
                // Add redirect URL after logout
                redirectUris.add(gatewayURL);
            } else {
                logger.error("Request for Realm Client Creation failed, callback URL not present");
                IamAdminServicesException ex = new IamAdminServicesException();
                ex.setMessage("Gateway Url field in GatewayProfile cannot be empty, Realm Client creation failed");
                throw ex;
            }
            pgaClient.setRedirectUris(redirectUris);
            pgaClient.setPublicClient(false);
            ResponseEntity<Void> httpResponse =
                    client.createClient(gatewayDetails.getGatewayId(), pgaClient, adminToken);
            logger.info("Tenant Client configuration exited with code : " + httpResponse.getStatusCode());

            if (httpResponse.getStatusCode() == HttpStatus.CREATED) {
                List<ClientRepresentation> clients = client.findClientsByClientId(
                        gatewayDetails.getGatewayId(), pgaClient.getClientId(), adminToken);
                if (!clients.isEmpty()) {
                    String clientUUID = clients.get(0).getId();
                    CredentialRepresentation clientSecret =
                            client.getClientSecret(gatewayDetails.getGatewayId(), clientUUID, adminToken);
                    gatewayDetails.setOauthClientId(pgaClient.getClientId());
                    gatewayDetails.setOauthClientSecret(clientSecret.getValue());
                    
                    // Add the manage-users and manage-clients roles to the service account
                    // Now that we have the client UUID, we can get the service account user
                    UserRepresentation serviceAccountUser =
                            client.getServiceAccountUser(gatewayDetails.getGatewayId(), clientUUID, adminToken);
                    if (serviceAccountUser != null) {
                        String realmManagementClientId =
                                getRealmManagementClientId(client, gatewayDetails.getGatewayId(), adminToken);
                        if (realmManagementClientId != null) {
                            List<RoleRepresentation> availableRoles = client.getAvailableClientRoles(
                                    gatewayDetails.getGatewayId(),
                                    serviceAccountUser.getId(),
                                    realmManagementClientId,
                                    adminToken);
                            List<RoleRepresentation> manageRoles = availableRoles.stream()
                                    .filter(r -> r.getName().equals("manage-users")
                                            || r.getName().equals("manage-clients"))
                                    .collect(Collectors.toList());
                            if (!manageRoles.isEmpty()) {
                                client.addClientRolesToUser(
                                        gatewayDetails.getGatewayId(),
                                        serviceAccountUser.getId(),
                                        realmManagementClientId,
                                        manageRoles,
                                        adminToken);
                            }
                        }
                    }
                    
                    return gatewayDetails;
                } else {
                    logger.error("Created client not found after creation");
                    return null;
                }
            } else {
                logger.error(
                        "Request for Realm Client Creation failed with HTTP code : " + httpResponse.getStatusCode());
                return null;
            }
        } catch (Exception ex) {
            logger.error("Error configuring client in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error configuring client in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    private String getRealmManagementClientId(KeycloakRestClient client, String realmId, String accessToken)
            throws IamAdminServicesException {
        List<ClientRepresentation> realmClients = client.findAllClients(realmId, accessToken);
        for (ClientRepresentation realmClient : realmClients) {
            if (realmClient.getClientId().equals("realm-management")) {
                return realmClient.getId();
            }
        }
        return null;
    }

    @Override
    public boolean isUsernameAvailable(String accessToken, String tenantId, String username)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            return userRepresentation == null;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error checking username availability: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean createUser(
            String accessToken,
            String tenantId,
            String username,
            String emailAddress,
            String firstName,
            String lastName,
            String newPassword)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(emailAddress);
            user.setEnabled(false);
            ResponseEntity<Void> httpResponse = client.createUser(tenantId, user, accessToken);
            if (httpResponse.getStatusCode() == HttpStatus.CREATED) { // HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = client.searchUsers(
                        tenantId,
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0,
                        1,
                        accessToken);
                if (!retrieveCreatedUserList.isEmpty()) {
                    UserRepresentation createdUser = retrieveCreatedUserList.get(0);
                    CredentialRepresentation credential = new CredentialRepresentation();
                    credential.setType(CredentialRepresentation.PASSWORD);
                    credential.setValue(newPassword);
                    credential.setTemporary(false);
                    client.resetPassword(tenantId, createdUser.getId(), credential, accessToken);
                    return true;
                } else {
                    logger.error("Created user not found after creation");
                    return false;
                }
            } else {
                logger.error(
                        "Request for user Account Creation failed with HTTP code : " + httpResponse.getStatusCode());
                return false;
            }
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean enableUserAccount(String accessToken, String tenantId, String username)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                userRepresentation.setEnabled(true);
                // We require that a user verify their email before enabling the account
                userRepresentation.setEmailVerified(true);
                client.updateUser(tenantId, userRepresentation.getId(), userRepresentation, accessToken);
                return true;
            } else {
                logger.error("User not found: " + username);
                return false;
            }
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error enabling user account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserAccountEnabled(String accessToken, String tenantId, String username)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            return userRepresentation != null && userRepresentation.isEnabled();
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error checking if user is enabled: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserExist(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            return userRepresentation != null;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error checking if user exists: " + e.getMessage(), e);
        }
    }

    @Override
    public UserProfile getUser(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            return userRepresentation != null
                    ? convertUserRepresentationToUserProfile(userRepresentation, tenantId)
                    : null;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserProfile> getUsers(String accessToken, String tenantId, int offset, int limit, String search)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            List<UserRepresentation> userRepresentationList =
                    client.searchUsers(tenantId, search, null, null, null, offset, limit, accessToken);
            return userRepresentationList.stream()
                    .map(ur -> convertUserRepresentationToUserProfile(ur, tenantId))
                    .collect(Collectors.toList());
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting users: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean resetUserPassword(String accessToken, String tenantId, String username, String newPassword)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                client.resetPassword(tenantId, userRepresentation.getId(), credential, accessToken);
                // Remove the UPDATE_PASSWORD required action
                userRepresentation = client.getUser(tenantId, userRepresentation.getId(), accessToken);
                if (userRepresentation != null && userRepresentation.getRequiredActions() != null) {
                    userRepresentation.getRequiredActions().remove("UPDATE_PASSWORD");
                    client.updateUser(tenantId, userRepresentation.getId(), userRepresentation, accessToken);
                }
                return true;
            } else {
                logger.error("requested User not found");
                return false;
            }
        } catch (Exception ex) {
            logger.error("Error resetting user password in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error resetting user password in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    public List<UserProfile> findUser(String accessToken, String tenantId, String email, String userName)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            List<UserRepresentation> retrieveUserList =
                    client.searchUsers(tenantId, userName, null, null, email, 0, 1, accessToken);
            if (!retrieveUserList.isEmpty()) {
                List<UserProfile> userList = new ArrayList<>();
                for (UserRepresentation user : retrieveUserList) {
                    UserProfile profile = new UserProfile();
                    profile.setUserId(user.getUsername());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setEmails(Arrays.asList(new String[] {user.getEmail()}));
                    userList.add(profile);
                }
                return userList;
            } else {
                logger.error("requested User not found");
                return null;
            }
        } catch (Exception ex) {
            logger.error("Error finding user in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error finding user in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateUserProfile(String accessToken, String tenantId, String username, UserProfile userDetails)
            throws IamAdminServicesException {

        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                userRepresentation.setFirstName(userDetails.getFirstName());
                userRepresentation.setLastName(userDetails.getLastName());
                if (userDetails.getEmails() != null && !userDetails.getEmails().isEmpty()) {
                    userRepresentation.setEmail(userDetails.getEmails().get(0));
                }
                client.updateUser(tenantId, userRepresentation.getId(), userRepresentation, accessToken);
            } else {
                throw new IamAdminServicesException("User [" + username + "] wasn't found in Keycloak!");
            }
        } catch (Exception ex) {
            logger.error("Error updating user profile in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error updating user profile in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deleteUser(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                client.deleteUser(tenantId, userRepresentation.getId(), accessToken);
                return true;
            } else {
                throw new IamAdminServicesException("User [" + username + "] wasn't found in Keycloak!");
            }
        } catch (Exception ex) {
            logger.error("Error deleting user in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error deleting user in keycloak server, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean addRoleToUser(PasswordCredential realmAdminCreds, String tenantId, String username, String roleName)
            throws IamAdminServicesException {

        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveCreatedUserList =
                    client.searchUsers(tenantId, username, null, null, null, 0, 1, adminToken);
            if (!retrieveCreatedUserList.isEmpty()) {
                UserRepresentation user = retrieveCreatedUserList.get(0);
                // Add user to the role
                RoleRepresentation roleResource = client.getRole(tenantId, roleName, adminToken);
                if (roleResource != null) {
                    client.addRealmRolesToUser(tenantId, user.getId(), Arrays.asList(roleResource), adminToken);
                    return true;
                } else {
                    logger.error("Role not found: " + roleName);
                    return false;
                }
            } else {
                logger.error("User not found: " + username);
                return false;
            }
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error adding role to user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeRoleFromUser(
            PasswordCredential realmAdminCreds, String tenantId, String username, String roleName)
            throws IamAdminServicesException {

        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveCreatedUserList =
                    client.searchUsers(tenantId, username, null, null, null, 0, 1, adminToken);
            if (!retrieveCreatedUserList.isEmpty()) {
                UserRepresentation user = retrieveCreatedUserList.get(0);
                // Remove role from user
                RoleRepresentation roleResource = client.getRole(tenantId, roleName, adminToken);
                if (roleResource != null) {
                    client.removeRealmRolesFromUser(tenantId, user.getId(), Arrays.asList(roleResource), adminToken);
                    return true;
                } else {
                    logger.error("Role not found: " + roleName);
                    return false;
                }
            } else {
                logger.error("User not found: " + username);
                return false;
            }
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error removing role from user: " + e.getMessage(), e);
        }
    }

    // TODO: Remove after migration to group-based auth is complete
    // This method is needed for backward compatibility during migration from role-based to group-based authentication
    @Override
    public List<UserProfile> getUsersWithRole(PasswordCredential realmAdminCreds, String tenantId, String roleName)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(tenantId, realmAdminCreds);
            // FIXME: this only searches through the most recent 100 users for the given role (assuming there are no
            // more than 10,000 users in the gateway)
            int totalUserCount = client.getUserCount(tenantId, adminToken);
            logger.debug("getUsersWithRole: totalUserCount=" + totalUserCount);
            // Load all users in batches
            List<UserRepresentation> allUsers = new ArrayList<>();
            int userBatchSize = 100;
            for (int start = 0; start < totalUserCount; start = start + userBatchSize) {
                logger.debug("getUsersWithRole: fetching " + userBatchSize + " users...");
                allUsers.addAll(client.searchUsers(tenantId, null, null, null, null, start, userBatchSize, adminToken));
            }
            logger.debug("getUsersWithRole: all users count=" + allUsers.size());
            allUsers.sort((a, b) -> {
                Long aTime = a.getCreatedTimestamp() != null ? a.getCreatedTimestamp() : 0L;
                Long bTime = b.getCreatedTimestamp() != null ? b.getCreatedTimestamp() : 0L;
                return Long.compare(bTime, aTime); // Sort descending (most recent first)
            });
            // The 100 most recently created users
            List<UserRepresentation> mostRecentUsers = allUsers.subList(0, Math.min(allUsers.size(), 100));
            logger.debug("getUsersWithRole: most recent users count=" + mostRecentUsers.size());

            List<UserProfile> usersWithRole = new ArrayList<>();
            for (UserRepresentation user : mostRecentUsers) {
                List<RoleRepresentation> roleRepresentations =
                        client.getUserRealmRoles(tenantId, user.getId(), adminToken);
                for (RoleRepresentation roleRepresentation : roleRepresentations) {
                    if (roleRepresentation.getName().equals(roleName)) {
                        usersWithRole.add(convertUserRepresentationToUserProfile(user, tenantId));
                        break;
                    }
                }
            }
            logger.debug("getUsersWithRole: most recent users with role count=" + usersWithRole.size());
            return usersWithRole;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting users with role: " + e.getMessage(), e);
        }
    }

    public List<String> getUserRoles(PasswordCredential realmAdminCreds, String tenantId, String username)
            throws IamAdminServicesException {
        try {
            KeycloakRestClient client = getRestClient();
            String adminToken = client.obtainAdminToken(tenantId, realmAdminCreds);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username, adminToken);
            if (userRepresentation == null) {
                logger.warn("No Keycloak user found for username [" + username + "] in tenant [" + tenantId + "].");
                return null;
            }
            List<RoleRepresentation> roles = client.getUserRealmRoles(tenantId, userRepresentation.getId(), adminToken);
            return roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting user roles: " + e.getMessage(), e);
        }
    }

    private UserProfile convertUserRepresentationToUserProfile(UserRepresentation userRepresentation, String tenantId) {

        UserProfile profile = new UserProfile();
        profile.setAiravataInternalUserId(userRepresentation.getUsername() + "@" + tenantId);
        profile.setGatewayId(tenantId);
        profile.setUserId(userRepresentation.getUsername());
        profile.setFirstName(userRepresentation.getFirstName());
        profile.setLastName(userRepresentation.getLastName());
        profile.setEmails(Arrays.asList(new String[] {userRepresentation.getEmail()}));
        profile.setCreationTime(userRepresentation.getCreatedTimestamp());

        // Just default these. UserProfile isn't a great data model for this data since it isn't actually the Airavata
        // UserProfile
        profile.setLastAccessTime(0);
        profile.setValidUntil(0);
        // Use state field to indicate whether user has been enabled or email verified in Keycloak
        if (userRepresentation.isEnabled()) {
            profile.setState(Status.ACTIVE);
        } else if (userRepresentation.isEmailVerified()) {
            profile.setState(Status.CONFIRMED);
        } else {
            profile.setState(Status.PENDING_CONFIRMATION);
        }

        return profile;
    }

    private static UserRepresentation getUserByUsername(
            KeycloakRestClient client, String tenantId, String username, String accessToken)
            throws IamAdminServicesException {
        // Use exact=true parameter for exact username match
        List<UserRepresentation> userResourceList =
                client.searchUsers(tenantId, username, null, null, null, null, null, true, accessToken);
        // Even with exact=true, filter to ensure exact match (defensive programming)
        for (UserRepresentation userRepresentation : userResourceList) {
            if (userRepresentation.getUsername().equals(username)) {
                return userRepresentation;
            }
        }
        return null;
    }
}
