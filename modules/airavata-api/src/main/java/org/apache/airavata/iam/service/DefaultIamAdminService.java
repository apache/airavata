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
package org.apache.airavata.iam.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.iam.model.Status;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.iam.mapper.UserProfileMapper;
import org.apache.airavata.iam.repository.UserRepository;
import org.apache.airavata.iam.keycloak.KeycloakGatewayManagement;
import org.apache.airavata.iam.keycloak.KeycloakRestClient;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.UserRepresentation;
import org.apache.airavata.iam.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnMissingBean(name = "testIamAdminService")
public class DefaultIamAdminService implements IamAdminService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultIamAdminService.class);

    private final ServerProperties properties;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final CredentialStoreService credentialStoreService;
    private final KeycloakAdminTokenResolver adminTokenResolver;

    public DefaultIamAdminService(
            ServerProperties properties,
            UserRepository userRepository,
            UserProfileMapper userProfileMapper,
            CredentialStoreService credentialStoreService,
            KeycloakAdminTokenResolver adminTokenResolver) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userProfileMapper = userProfileMapper;
        this.credentialStoreService = credentialStoreService;
        this.adminTokenResolver = adminTokenResolver;
    }

    private boolean isIamConfigured() {
        return properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().serverUrl() != null
                && !properties.security().iam().serverUrl().isEmpty();
    }

    /**
     * Creates a properly configured KeycloakGatewayManagement instance.
     * Injects the properties so the Keycloak client can access the server URL and credentials.
     */
    private KeycloakGatewayManagement createKeycloakClient() {
        return new KeycloakGatewayManagement(properties);
    }

    /**
     * Creates a KeycloakRestClient instance.
     */
    private KeycloakRestClient createKeycloakRestClient() throws IamAdminServicesException {
        if (!isIamConfigured()) {
            throw new IamAdminServicesException("IAM is not configured");
        }
        String serverUrl = properties.security().iam().serverUrl();
        return new KeycloakRestClient(serverUrl, properties);
    }

    public Gateway setUpGateway(AuthzToken authzToken, Gateway gateway)
            throws IamAdminServicesException, CredentialStoreException {
        var keycloakclient = createKeycloakClient();
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

            // Gateway setup no longer uses identityServerPasswordToken (dropped in V2 schema).
            // The tenant admin account is created without a pre-stored credential token.
            // Callers must perform any post-setup credential provisioning separately.
            if (!keycloakclient.createTenantAdminAccount(
                    isSuperAdminCredentials, gateway, "")) {
                logger.error("Admin account creation failed !!, please refer error logs for reason");
            }
            var gatewayWithIdAndSecret = keycloakclient.configureClient(isSuperAdminCredentials, gateway);
            return gatewayWithIdAndSecret;
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Gateway Setup Failed: %s", ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean isUsernameAvailable(AuthzToken authzToken, String username) throws IamAdminServicesException {
        try {
            var keycloakClient = createKeycloakClient();
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
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.createUser(
                    authzToken.getAccessToken(), gatewayId, username, emailAddress, firstName, lastName, newPassword))
                return true;
            else return false;
        } catch (IamAdminServicesException ex) {
            String msg =
                    String.format("Error while registering user=%s into IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    @Transactional
    public boolean enableUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, Boolean.TRUE, adminToken);
            if (users != null && !users.isEmpty()) {
                var userRepresentation = users.get(0);
                userRepresentation.setEnabled(true);
                userRepresentation.setEmailVerified(true);
                client.updateUser(gatewayId, userRepresentation.getId(), userRepresentation, adminToken);

                // Check if user profile exists, if not create it
                var lowerUsername = username.toLowerCase();
                var userProfileExists = userRepository
                        .findByUserIdAndGatewayId(lowerUsername, gatewayId)
                        .isPresent();
                if (!userProfileExists) {
                    // Load basic user profile information from Keycloak and then save in UserRepository
                    var userProfile = convertUserRepresentationToUserProfile(userRepresentation, gatewayId);
                    userProfile.setUserId(lowerUsername);
                    userProfile.setAiravataInternalUserId(lowerUsername + "@" + gatewayId);
                    userProfile.setCreationTime(
                            IdGenerator.getCurrentTimestamp().getTime());
                    userProfile.setLastAccessTime(
                            IdGenerator.getCurrentTimestamp().getTime());
                    userProfile.setValidUntil(-1);
                    // Convert to entity and save using repository
                    var entity = userProfileMapper.toEntity(userProfile);
                    userRepository.save(entity);
                }
                return true;
            } else {
                logger.error("User not found: {}", username);
                return false;
            }
        } catch (IamAdminServicesException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IamAdminServicesException("Error enabling user: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public boolean disableUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, Boolean.TRUE, adminToken);
            if (users != null && !users.isEmpty()) {
                var userRepresentation = users.get(0);
                userRepresentation.setEnabled(false);
                client.updateUser(gatewayId, userRepresentation.getId(), userRepresentation, adminToken);
                return true;
            } else {
                logger.error("User not found: {}", username);
                return false;
            }
        } catch (IamAdminServicesException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IamAdminServicesException("Error disabling user: " + ex.getMessage(), ex);
        }
    }

    public boolean isUserEnabled(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.isUserAccountEnabled(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while checking if user=%s is enabled in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean isUserExist(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.isUserExist(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while checking if user=%s exists in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public UserProfile getUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) {
            UserProfile up = new UserProfile();
            up.setUserId(username);
            up.setGatewayId(authzToken.getClaimsMap().get(Constants.GATEWAY_ID));
            return up;
        }
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminToken(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, Boolean.TRUE, adminToken);
            if (users != null && !users.isEmpty()) {
                return convertUserRepresentationToUserProfile(users.get(0), gatewayId);
            }
            return null;
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while retrieving user=%s profile from IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        } catch (Exception ex) {
            String msg = String.format(
                    "Error while retrieving user=%s profile from IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException {
        if (!isIamConfigured()) return java.util.Collections.emptyList();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        // Check if gatewayId is null or empty - this indicates IAM isn't properly configured for this gateway
        if (gatewayId == null || gatewayId.isEmpty()) {
            String msg = "Gateway ID (realm) is not configured in the authorization token. "
                    + "The Users API requires a gateway to be set up with IAM. "
                    + "Please ensure the gateway is properly configured with IAM settings.";
            logger.warn(msg);
            throw new IamAdminServicesException(msg);
        }

        try {
            var client = createKeycloakRestClient();
            var adminToken = adminTokenResolver.resolveAdminToken(gatewayId, client);

            var userRepresentationList =
                    client.searchUsers(gatewayId, search, null, null, null, offset, limit, adminToken);

            return userRepresentationList.stream()
                    .map(ur -> convertUserRepresentationToUserProfile(ur, gatewayId))
                    .collect(Collectors.toList());
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while retrieving users from IAM backend: %s", ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        } catch (Exception ex) {
            String msg = String.format("Error while retrieving users from IAM backend: %s", ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean resetUserPassword(AuthzToken authzToken, String username, String newPassword)
            throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.resetUserPassword(authzToken.getAccessToken(), gatewayId, username, newPassword))
                return true;
            else return false;
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while resetting user=%s password in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public List<UserProfile> findUsers(AuthzToken authzToken, String email, String userId)
            throws IamAdminServicesException {
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.findUser(authzToken.getAccessToken(), gatewayId, email, userId);
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while finding users in IAM backend: %s", ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public void updateUserProfile(AuthzToken authzToken, UserProfile userDetails) throws IamAdminServicesException {
        if (!isIamConfigured()) {
            logger.debug("IAM not configured, skipping user profile update for userId: {}", userDetails.getUserId());
            return;
        }
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var username = userDetails.getUserId();
            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, Boolean.TRUE, adminToken);
            if (users != null && !users.isEmpty()) {
                var userRepresentation = users.get(0);
                if (userDetails.getFirstName() != null) {
                    userRepresentation.setFirstName(userDetails.getFirstName());
                }
                if (userDetails.getLastName() != null) {
                    userRepresentation.setLastName(userDetails.getLastName());
                }
                if (userDetails.getEmails() != null && !userDetails.getEmails().isEmpty()) {
                    userRepresentation.setEmail(userDetails.getEmails().get(0));
                }
                client.updateUser(gatewayId, userRepresentation.getId(), userRepresentation, adminToken);
            } else {
                throw new IamAdminServicesException("User not found: " + username);
            }
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while updating user=%s profile in IAM backend: %s",
                    userDetails.getUserId(), ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        } catch (Exception ex) {
            String msg = String.format(
                    "Error while updating user=%s profile in IAM backend: %s",
                    userDetails.getUserId(), ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        if (!isIamConfigured()) return true;
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, Boolean.TRUE, adminToken);
            if (users != null && !users.isEmpty()) {
                var userRepresentation = users.get(0);
                client.deleteUser(gatewayId, userRepresentation.getId(), adminToken);
                return true;
            } else {
                logger.error("User not found: {}", username);
                return false;
            }
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while deleting user=%s in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw ex;
        } catch (Exception ex) {
            String msg = String.format("Error while deleting user=%s in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException {
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, adminToken);
            if (users.isEmpty()) {
                logger.error("User not found: " + username);
                return false;
            }

            var user = users.get(0);
            var role = client.getRole(gatewayId, roleName, adminToken);
            if (role == null) {
                logger.error("Role not found: " + roleName);
                return false;
            }

            client.addRealmRolesToUser(gatewayId, user.getId(), Arrays.asList(role), adminToken);
            return true;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error adding role to user: " + e.getMessage(), e);
        }
    }

    public boolean removeRoleFromUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException {
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var users = client.searchUsers(gatewayId, username, null, null, null, 0, 1, adminToken);
            if (users.isEmpty()) {
                logger.error("User not found: " + username);
                return false;
            }

            var user = users.get(0);
            var role = client.getRole(gatewayId, roleName, adminToken);
            if (role == null) {
                logger.error("Role not found: " + roleName);
                return false;
            }

            client.removeRealmRolesFromUser(gatewayId, user.getId(), Arrays.asList(role), adminToken);
            return true;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error removing role from user: " + e.getMessage(), e);
        }
    }

    public List<UserProfile> getUsersWithRole(AuthzToken authzToken, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException {
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var adminToken = adminTokenResolver.resolveAdminTokenCompact(gatewayId, client);

            var totalUserCount = client.getUserCount(gatewayId, adminToken);
            var allUsers = new ArrayList<UserRepresentation>();
            var userBatchSize = 100;
            for (int start = 0; start < totalUserCount; start += userBatchSize) {
                allUsers.addAll(
                        client.searchUsers(gatewayId, null, null, null, null, start, userBatchSize, adminToken));
            }

            allUsers.sort((a, b) -> {
                Long aTime = a.getCreatedTimestamp() != null ? a.getCreatedTimestamp() : 0L;
                Long bTime = b.getCreatedTimestamp() != null ? b.getCreatedTimestamp() : 0L;
                return Long.compare(bTime, aTime);
            });

            var mostRecentUsers = allUsers.subList(0, Math.min(allUsers.size(), 100));
            var usersWithRole = new ArrayList<UserProfile>();

            for (var user : mostRecentUsers) {
                var roles = client.getUserRealmRoles(gatewayId, user.getId(), adminToken);
                for (var role : roles) {
                    if (role.getName().equals(roleName)) {
                        usersWithRole.add(convertUserRepresentationToUserProfile(user, gatewayId));
                        break;
                    }
                }
            }

            return usersWithRole;
        } catch (IamAdminServicesException e) {
            throw e;
        } catch (Exception e) {
            throw new IamAdminServicesException("Error getting users with role: " + e.getMessage(), e);
        }
    }

    private UserProfile convertUserRepresentationToUserProfile(UserRepresentation user, String tenantId) {
        var profile = new UserProfile();
        profile.setAiravataInternalUserId(user.getUsername() + "@" + tenantId);
        profile.setGatewayId(tenantId);
        profile.setUserId(user.getUsername());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmails(Arrays.asList(new String[] {user.getEmail()}));
        profile.setCreationTime(user.getCreatedTimestamp() != null ? user.getCreatedTimestamp() : 0L);
        profile.setLastAccessTime(0);
        profile.setValidUntil(0);
        if (user.isEnabled()) {
            profile.setState(Status.ACTIVE);
        } else if (user.isEmailVerified()) {
            profile.setState(Status.CONFIRMED);
        } else {
            profile.setState(Status.PENDING_CONFIRMATION);
        }
        return profile;
    }

    /**
     * Returns the super-admin password credential used for gateway setup operations.
     * Kept here (not delegated to the resolver) so that {@link #setUpGateway} can call it
     * directly without going through the token-acquisition chain.
     */
    private PasswordCredential getSuperAdminPasswordCredential() throws IamAdminServicesException {
        var isSuperAdminCredentials = new PasswordCredential();

        String username = null;
        String password = null;

        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().superAdmin() != null) {
            username = properties.security().iam().superAdmin().username();
            password = properties.security().iam().superAdmin().password();
        }

        if (username == null || username.isEmpty()) {
            username = System.getenv("IAM_SUPER_ADMIN_USERNAME");
            if (username == null || username.isEmpty()) {
                username = "admin";
            }
        }
        if (password == null || password.isEmpty()) {
            password = System.getenv("IAM_SUPER_ADMIN_PASSWORD");
            if (password == null || password.isEmpty()) {
                password = "admin";
            }
        }

        isSuperAdminCredentials.setLoginUserName(username);
        isSuperAdminCredentials.setPassword(password);
        return isSuperAdminCredentials;
    }
}
