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
package org.apache.airavata.iam.keycloak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.model.ClientRepresentation;
import org.apache.airavata.iam.model.CredentialRepresentation;
import org.apache.airavata.iam.model.RealmRepresentation;
import org.apache.airavata.iam.model.RoleRepresentation;
import org.apache.airavata.iam.model.RolesRepresentation;
import org.apache.airavata.iam.model.Status;
import org.apache.airavata.iam.model.UserProfile;
import org.apache.airavata.iam.model.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class KeycloakGatewayManagement implements GatewayManagement {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakGatewayManagement.class);

    private final String superAdminRealmId = "master";
    private ServerProperties properties;
    private KeycloakRestClient restClient;

    public KeycloakGatewayManagement() {}

    public KeycloakGatewayManagement(ServerProperties properties) {
        this.properties = properties;
        initializeRestClient();
    }

    public void setProperties(ServerProperties properties) {
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
        // Try to get from properties first
        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().serverUrl() != null
                && !properties.security().iam().serverUrl().isEmpty()) {
            return properties.security().iam().serverUrl();
        }

        // Fallback to environment variable (useful for testing with dynamic URLs)
        String envUrl = System.getenv("IAM_SERVER_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            logger.info("Using IAM server URL from environment: {}", envUrl);
            return envUrl;
        }

        // Fallback to system property (can be set by tests)
        String sysPropUrl = System.getProperty("airavata.security.iam.server-url");
        if (sysPropUrl != null && !sysPropUrl.isEmpty()) {
            logger.info("Using IAM server URL from system property: {}", sysPropUrl);
            return sysPropUrl;
        }

        throw new IamAdminServicesException(
                "IAM server URL is not configured. Check application.properties for security.iam.server-url, "
                        + "or set IAM_SERVER_URL environment variable");
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
            var client = getRestClient();
            // create realm
            var newRealmDetails = new RealmRepresentation();
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
            var realmWithRoles = KeycloakGatewayManagement.createDefaultRoles(newRealmDetails);
            client.createRealm(realmWithRoles);
            return gatewayDetails;
        } catch (Exception ex) {
            logger.error("Error creating Realm in Keycloak Server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error creating Realm in Keycloak Server, reason: " + ex.getMessage(), ex);
        }
    }

    public static RealmRepresentation createDefaultRoles(RealmRepresentation realmDetails) {
        var defaultRoles = new ArrayList<RoleRepresentation>();
        var adminRole = new RoleRepresentation();
        adminRole.setName("admin");
        adminRole.setDescription("Admin role for PGA users");
        defaultRoles.add(adminRole);
        var adminReadOnlyRole = new RoleRepresentation();
        adminReadOnlyRole.setName("admin-read-only");
        adminReadOnlyRole.setDescription("Read only role for PGA Admin users");
        defaultRoles.add(adminReadOnlyRole);
        var gatewayUserRole = new RoleRepresentation();
        gatewayUserRole.setName("gateway-user");
        gatewayUserRole.setDescription("default role for PGA users");
        defaultRoles.add(gatewayUserRole);
        var pendingUserRole = new RoleRepresentation();
        pendingUserRole.setName("user-pending");
        pendingUserRole.setDescription("role for newly registered PGA users");
        defaultRoles.add(pendingUserRole);
        var gatewayProviderRole = new RoleRepresentation();
        gatewayProviderRole.setName("gateway-provider");
        gatewayProviderRole.setDescription("role for gateway providers in the super-admin PGA");
        defaultRoles.add(gatewayProviderRole);
        var rolesRepresentation = new RolesRepresentation();
        rolesRepresentation.setRealm(defaultRoles);
        realmDetails.setRoles(rolesRepresentation);
        return realmDetails;
    }

    @Override
    public boolean createTenantAdminAccount(
            PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails, String tenantAdminPassword)
            throws IamAdminServicesException {
        try {
            var client = getRestClient();
            var adminToken = client.obtainAdminToken(this.superAdminRealmId, isSuperAdminPasswordCreds);
            var user = new UserRepresentation();
            // Use the gateway email address as the admin username/email.
            // The caller is responsible for supplying a tenantAdminPassword separately.
            String adminUsername = gatewayDetails.getEmailAddress();
            user.setUsername(adminUsername);
            user.setEmail(gatewayDetails.getEmailAddress());
            user.setEmailVerified(true);
            user.setEnabled(true);
            var httpResponse = client.createUser(gatewayDetails.getGatewayId(), user, adminToken);
            logger.info("Tenant Admin account creation exited with code : {}", httpResponse.getStatusCode());
            if (httpResponse.getStatusCode() == HttpStatus.CREATED) { // HTTP code for record creation: HTTP 201
                var retrieveCreatedUserList = client.searchUsers(
                        gatewayDetails.getGatewayId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0,
                        1,
                        adminToken);
                if (!retrieveCreatedUserList.isEmpty()) {
                    var createdUser = retrieveCreatedUserList.get(0);
                    var userId = createdUser.getId();

                    // Add user to the "admin" role
                    var adminRole = client.getRole(gatewayDetails.getGatewayId(), "admin", adminToken);
                    if (adminRole != null) {
                        client.addRealmRolesToUser(
                                gatewayDetails.getGatewayId(), userId, Arrays.asList(adminRole), adminToken);
                    }

                    var credential = new CredentialRepresentation();
                    credential.setType(CredentialRepresentation.PASSWORD);
                    credential.setValue(tenantAdminPassword);
                    credential.setTemporary(false);
                    client.resetPassword(gatewayDetails.getGatewayId(), userId, credential, adminToken);

                    // Add realm-management client roles
                    var realmClients = client.findAllClients(gatewayDetails.getGatewayId(), adminToken);
                    String realmManagementClientId = null;
                    for (ClientRepresentation realmClient : realmClients) {
                        if (realmClient.getClientId().equals("realm-management")) {
                            realmManagementClientId = realmClient.getId();
                            break;
                        }
                    }
                    if (realmManagementClientId != null) {
                        var availableRoles = client.getAvailableClientRoles(
                                gatewayDetails.getGatewayId(), userId, realmManagementClientId, adminToken);
                        var manageRoles = availableRoles.stream()
                                .filter(r -> r.getName().equals("manage-users")
                                        || r.getName().equals("manage-clients"))
                                .toList();
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
                logger.error(
                        "Request for Tenant Admin Account Creation failed with HTTP code : {}",
                        httpResponse.getStatusCode());
                return false;
            }
        } catch (Exception ex) {
            logger.error("Error creating Realm Admin Account in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error creating Realm Admin Account in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Gateway configureClient(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails)
            throws IamAdminServicesException {
        try {
            var client = getRestClient();
            var adminToken = client.obtainAdminToken(this.superAdminRealmId, isSuperAdminPasswordCreds);
            var pgaClient = new ClientRepresentation();
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
            var redirectUris = new ArrayList<String>();
            if (gatewayDetails.getDomain() != null) {
                String gatewayDomain = gatewayDetails.getDomain();
                // Remove trailing slash
                if (gatewayDomain.endsWith("/")) {
                    gatewayDomain = gatewayDomain.substring(0, gatewayDomain.length() - 1);
                }
                redirectUris.add(gatewayDomain + "/callback-url"); // PGA
                redirectUris.add(gatewayDomain + "/auth/callback*"); // Django
                redirectUris.add(gatewayDomain);
            } else {
                logger.error("Request for Realm Client Creation failed, gateway domain not present");
                throw new IamAdminServicesException(
                        "Gateway domain field in GatewayProfile cannot be empty, Realm Client creation failed");
            }
            pgaClient.setRedirectUris(redirectUris);
            pgaClient.setPublicClient(false);
            var httpResponse = client.createClient(gatewayDetails.getGatewayId(), pgaClient, adminToken);
            logger.info("Tenant Client configuration exited with code : {}", httpResponse.getStatusCode());

            if (httpResponse.getStatusCode() == HttpStatus.CREATED) {
                var clients = client.findClientsByClientId(
                        gatewayDetails.getGatewayId(), pgaClient.getClientId(), adminToken);
                if (!clients.isEmpty()) {
                    var clientUUID = clients.get(0).getId();

                    // Add the manage-users and manage-clients roles to the service account
                    var serviceAccountUser =
                            client.getServiceAccountUser(gatewayDetails.getGatewayId(), clientUUID, adminToken);
                    if (serviceAccountUser != null) {
                        var realmManagementClientId =
                                getRealmManagementClientId(client, gatewayDetails.getGatewayId(), adminToken);
                        if (realmManagementClientId != null) {
                            var availableRoles = client.getAvailableClientRoles(
                                    gatewayDetails.getGatewayId(),
                                    serviceAccountUser.getId(),
                                    realmManagementClientId,
                                    adminToken);
                            var manageRoles = availableRoles.stream()
                                    .filter(r -> r.getName().equals("manage-users")
                                            || r.getName().equals("manage-clients"))
                                    .toList();
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
                        "Request for Realm Client Creation failed with HTTP code : {}", httpResponse.getStatusCode());
                return null;
            }
        } catch (Exception ex) {
            logger.error("Error configuring client in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error configuring client in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    private String getRealmManagementClientId(KeycloakRestClient client, String realmId, String accessToken)
            throws IamAdminServicesException {
        var realmClients = client.findAllClients(realmId, accessToken);
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
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
            var client = getRestClient();
            var user = new UserRepresentation();
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(emailAddress);
            user.setEnabled(false);
            var httpResponse = client.createUser(tenantId, user, accessToken);
            if (httpResponse.getStatusCode() == HttpStatus.CREATED) { // HTTP code for record creation: HTTP 201
                var retrieveCreatedUserList = client.searchUsers(
                        tenantId,
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0,
                        1,
                        accessToken);
                if (!retrieveCreatedUserList.isEmpty()) {
                    var createdUser = retrieveCreatedUserList.get(0);
                    var credential = new CredentialRepresentation();
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
                        "Request for user Account Creation failed with HTTP code : {}", httpResponse.getStatusCode());
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                userRepresentation.setEnabled(true);
                // We require that a user verify their email before enabling the account
                userRepresentation.setEmailVerified(true);
                client.updateUser(tenantId, userRepresentation.getId(), userRepresentation, accessToken);
                return true;
            } else {
                logger.error("User not found: {}", username);
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
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
            var client = getRestClient();
            var userRepresentationList =
                    client.searchUsers(tenantId, search, null, null, null, offset, limit, accessToken);
            return userRepresentationList.stream()
                    .map(ur -> convertUserRepresentationToUserProfile(ur, tenantId))
                    .toList();
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
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                var credential = new CredentialRepresentation();
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
            logger.error("Error resetting user password in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error resetting user password in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<UserProfile> findUser(String accessToken, String tenantId, String email, String userName)
            throws IamAdminServicesException {
        try {
            var client = getRestClient();
            var retrieveUserList = client.searchUsers(tenantId, userName, null, null, email, 0, 1, accessToken);
            var userList = new ArrayList<UserProfile>();
            if (!retrieveUserList.isEmpty()) {
                for (var user : retrieveUserList) {
                    var profile = new UserProfile();
                    profile.setUserId(user.getUsername());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setEmails(Arrays.asList(new String[] {user.getEmail()}));
                    userList.add(profile);
                }
            } else {
                logger.debug("No users found matching the search criteria");
            }
            return userList;
        } catch (Exception ex) {
            logger.error("Error finding user in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error finding user in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void updateUserProfile(String accessToken, String tenantId, String username, UserProfile userDetails)
            throws IamAdminServicesException {

        try {
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
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
            logger.error("Error updating user profile in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error updating user profile in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean deleteUser(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        try {
            var client = getRestClient();
            var userRepresentation = getUserByUsername(client, tenantId, username, accessToken);
            if (userRepresentation != null) {
                client.deleteUser(tenantId, userRepresentation.getId(), accessToken);
                return true;
            } else {
                throw new IamAdminServicesException("User [" + username + "] wasn't found in Keycloak!");
            }
        } catch (Exception ex) {
            logger.error("Error deleting user in keycloak server, reason: {}", ex.getMessage(), ex);
            throw new IamAdminServicesException(
                    "Error deleting user in keycloak server, reason: " + ex.getMessage(), ex);
        }
    }

    public List<String> getUserRoles(PasswordCredential realmAdminCreds, String tenantId, String username)
            throws IamAdminServicesException {
        try {
            var client = getRestClient();
            var adminToken = client.obtainAdminToken(tenantId, realmAdminCreds);
            var userRepresentation = getUserByUsername(client, tenantId, username, adminToken);
            if (userRepresentation == null) {
                logger.warn("No Keycloak user found for username [{}] in tenant [{}].", username, tenantId);
                return null;
            }
            var roles = client.getUserRealmRoles(tenantId, userRepresentation.getId(), adminToken);
            return roles.stream().map(RoleRepresentation::getName).toList();
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting user roles: " + e.getMessage(), e);
        }
    }

    private UserProfile convertUserRepresentationToUserProfile(UserRepresentation userRepresentation, String tenantId) {

        var profile = new UserProfile();
        profile.setAiravataInternalUserId(userRepresentation.getUsername() + "@" + tenantId);
        profile.setGatewayId(tenantId);
        profile.setUserId(userRepresentation.getUsername());
        profile.setFirstName(userRepresentation.getFirstName());
        profile.setLastName(userRepresentation.getLastName());
        profile.setEmails(Arrays.asList(new String[] {userRepresentation.getEmail()}));
        profile.setCreatedAt(
                userRepresentation.getCreatedTimestamp() != null
                        ? userRepresentation.getCreatedTimestamp()
                        : System.currentTimeMillis());

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
