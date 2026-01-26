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
package org.apache.airavata.service.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Status;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.mappers.UserProfileMapper;
import org.apache.airavata.profile.utils.TenantManagementKeycloakImpl;
import org.apache.airavata.registry.repositories.UserRepository;
import org.apache.airavata.profile.utils.keycloak.KeycloakRestClient;
import org.apache.airavata.profile.utils.keycloak.dto.UserRepresentation;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnApiService
@ConditionalOnMissingBean(name = "testIamAdminService")
public class IamAdminService {
    private static final Logger logger = LoggerFactory.getLogger(IamAdminService.class);

    private final AiravataServerProperties properties;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final CredentialStoreService credentialStoreService;
    private final RegistryService registryService;

    private boolean isIamConfigured() {
        return properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().serverUrl() != null
                && !properties.security().iam().serverUrl().isEmpty();
    }

    /**
     * Creates a properly configured TenantManagementKeycloakImpl instance.
     * Injects the properties so the Keycloak client can access the server URL and credentials.
     */
    private TenantManagementKeycloakImpl createKeycloakClient() {
        var client = new TenantManagementKeycloakImpl(properties);
        return client;
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

    public IamAdminService(
            AiravataServerProperties properties,
            UserRepository userRepository,
            UserProfileMapper userProfileMapper,
            CredentialStoreService credentialStoreService,
            RegistryService registryService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userProfileMapper = userProfileMapper;
        this.credentialStoreService = credentialStoreService;
        this.registryService = registryService;
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

            // Load the tenant admin password using the credential token from the gateway request.
            // The identityServerPasswordToken is a reference to a credential in the credential store
            // that was created by the caller (e.g., super-portal) before calling this method.
            // This token is NOT persisted in the Gateway entity - it's only used during setup.
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
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            if (keycloakclient.enableUserAccount(authzToken.getAccessToken(), gatewayId, username)) {
                // Check if user profile exists, if not create it
                var lowerUsername = username.toLowerCase();
                var userProfileExists = userRepository
                        .findByUserIdAndGatewayId(lowerUsername, gatewayId)
                        .isPresent();
                if (!userProfileExists) {
                    // Load basic user profile information from Keycloak and then save in UserRepository
                    var userProfile = keycloakclient.getUser(authzToken.getAccessToken(), gatewayId, username);
                    userProfile.setUserId(lowerUsername);
                    userProfile.setAiravataInternalUserId(lowerUsername + "@" + gatewayId);
                    userProfile.setCreationTime(
                            AiravataUtils.getCurrentTimestamp().getTime());
                    userProfile.setLastAccessTime(
                            AiravataUtils.getCurrentTimestamp().getTime());
                    userProfile.setValidUntil(-1);
                    // Convert to entity and save using repository
                    var entity = userProfileMapper.toEntity(userProfile);
                    userRepository.save(entity);
                }
                return true;
            } else {
                return false;
            }
        } catch (IamAdminServicesException ex) {
            throw ex;
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
        var keycloakclient = createKeycloakClient();
        var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        try {
            return keycloakclient.getUser(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while retrieving user=%s profile from IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public List<UserProfile> getUsers(AuthzToken authzToken, int offset, int limit, String search)
            throws IamAdminServicesException {
        if (!isIamConfigured()) return java.util.Collections.emptyList();
        var keycloakclient = createKeycloakClient();
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
            return keycloakclient.getUsers(authzToken.getAccessToken(), gatewayId, offset, limit, search);
        } catch (IamAdminServicesException ex) {
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
            // IAM not configured - skip update (similar to other methods)
            logger.debug("IAM not configured, skipping user profile update for userId: {}", userDetails.getUserId());
            return;
        }
        try {
            var keycloakclient = createKeycloakClient();
            keycloakclient.setProperties(properties);
            var username = userDetails.getUserId();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            keycloakclient.updateUserProfile(authzToken.getAccessToken(), gatewayId, username, userDetails);
        } catch (IamAdminServicesException ex) {
            String msg = String.format(
                    "Error while updating user=%s profile in IAM backend: %s",
                    userDetails.getUserId(), ex.getMessage());
            logger.error(msg, ex);
            throw new IamAdminServicesException(msg, ex);
        }
    }

    public boolean deleteUser(AuthzToken authzToken, String username) throws IamAdminServicesException {
        try {
            var keycloakclient = createKeycloakClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return keycloakclient.deleteUser(authzToken.getAccessToken(), gatewayId, username);
        } catch (IamAdminServicesException ex) {
            String msg = String.format("Error while deleting user=%s in IAM backend: %s", username, ex.getMessage());
            logger.error(msg, ex);
            throw ex;
        }
    }

    public boolean addRoleToUser(AuthzToken authzToken, String username, String roleName)
            throws IamAdminServicesException, RegistryException, CredentialStoreException {
        try {
            var client = createKeycloakRestClient();
            var gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            var adminToken = client.obtainAdminToken(gatewayId, isRealmAdminCredentials);

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
            var isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            var adminToken = client.obtainAdminToken(gatewayId, isRealmAdminCredentials);

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
            var isRealmAdminCredentials = getTenantAdminPasswordCredential(gatewayId);
            var adminToken = client.obtainAdminToken(gatewayId, isRealmAdminCredentials);

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
        profile.setCreationTime(user.getCreatedTimestamp());
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

    private PasswordCredential getSuperAdminPasswordCredential() throws IamAdminServicesException {
        var isSuperAdminCredentials = new PasswordCredential();

        // Get super admin credentials, with fallback to defaults for testing
        String username = null;
        String password = null;

        if (properties != null
                && properties.security() != null
                && properties.security().iam() != null
                && properties.security().iam().superAdmin() != null) {
            username = properties.security().iam().superAdmin().username();
            password = properties.security().iam().superAdmin().password();
        }

        // Fallback to environment variables or defaults (for testing)
        if (username == null || username.isEmpty()) {
            username = System.getenv("IAM_SUPER_ADMIN_USERNAME");
            if (username == null || username.isEmpty()) {
                username = "default-admin"; // Default from realm-default.json
            }
        }
        if (password == null || password.isEmpty()) {
            password = System.getenv("IAM_SUPER_ADMIN_PASSWORD");
            if (password == null || password.isEmpty()) {
                password = "admin123"; // Default from realm-default.json
            }
        }

        isSuperAdminCredentials.setLoginUserName(username);
        isSuperAdminCredentials.setPassword(password);
        return isSuperAdminCredentials;
    }

    private PasswordCredential getTenantAdminPasswordCredential(String tenantId)
            throws IamAdminServicesException, RegistryException, CredentialStoreException {
        var gwrp = registryService.getGatewayResourceProfile(tenantId);
        return credentialStoreService.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
    }
}
