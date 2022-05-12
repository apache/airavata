/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.profile.iam.admin.services.core.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.core.interfaces.TenantManagementInterface;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TenantManagementKeycloakImpl implements TenantManagementInterface {

    private final static Logger logger = LoggerFactory.getLogger(TenantManagementKeycloakImpl.class);

    private String superAdminRealmId = "master";

    private static Keycloak getClient(String adminUrl, String realm, PasswordCredential AdminPasswordCreds) {

        ResteasyClient resteasyClient = getResteasyClient();
        return KeycloakBuilder.builder()
                .serverUrl(adminUrl)
                .realm(realm)
                .username(AdminPasswordCreds.getLoginUserName())
                .password(AdminPasswordCreds.getPassword())
                .clientId("admin-cli")
                .resteasyClient(resteasyClient)
                .build();
    }

    private static Keycloak getClient(String adminUrl, String realm, String accessToken) {

        ResteasyClient resteasyClient = getResteasyClient();
        return KeycloakBuilder.builder()
                .serverUrl(adminUrl)
                .realm(realm)
                .authorization(accessToken)
                .resteasyClient(resteasyClient)
                .build();
    }

    private static ResteasyClient getResteasyClient() {

        ResteasyClientBuilder builder = new ResteasyClientBuilder().connectionPoolSize(10);
        try {
            if (ServerSettings.isTrustStorePathDefined()) {
                builder.trustStore(loadKeyStore());
            }
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException("Failed to read application settings", e);
        }
        return builder.build();
    }

    private static KeyStore loadKeyStore() {

        InputStream is = null;
        try {

            String trustStorePath =  ServerSettings.getTrustStorePath();
            File trustStoreFile = new File(trustStorePath);

            if (trustStoreFile.exists()) {
                logger.debug("Loading trust store file from path " + trustStorePath);
                is = new FileInputStream(trustStorePath);
            } else {
                logger.debug("Trying to load trust store file form class path " + trustStorePath);
                is = SecurityUtil.class.getClassLoader().getResourceAsStream(trustStorePath);
                if (is != null) {
                    logger.debug("Trust store file was loaded form class path " + trustStorePath);
                }
            }

            if (is == null) {
                throw new RuntimeException("Could not find a trust store file in path " + trustStorePath);
            }

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, ServerSettings.getTrustStorePassword().toCharArray());
            return ks;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trust store KeyStore instance", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Failed to close trust store FileInputStream", e);
                }
            }
        }
    }

    @Override
    public Gateway addTenant(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException {
        Keycloak client = null;
        try {
            // get client
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), this.superAdminRealmId, isSuperAdminPasswordCreds);
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
            client.realms().create(realmWithRoles);
            return gatewayDetails;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting Iam server Url from property file, reason: " + ex.getMessage());
            throw exception;
        } catch (Exception ex){
            logger.error("Error creating Realm in Keycloak Server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error creating Realm in Keycloak Server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public static RealmRepresentation createDefaultRoles(RealmRepresentation realmDetails){
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
    public boolean createTenantAdminAccount(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails, String tenantAdminPassword) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), this.superAdminRealmId, isSuperAdminPasswordCreds);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(gatewayDetails.getIdentityServerUserName());
            user.setFirstName(gatewayDetails.getGatewayAdminFirstName());
            user.setLastName(gatewayDetails.getGatewayAdminLastName());
            user.setEmail(gatewayDetails.getGatewayAdminEmail());
            user.setEmailVerified(true);
            user.setEnabled(true);
            Response httpResponse = client.realm(gatewayDetails.getGatewayId()).users().create(user);
            logger.info("Tenant Admin account creation exited with code : " + httpResponse.getStatus()+" : " +httpResponse.getStatusInfo());
            if (httpResponse.getStatus() == 201) { //HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = client.realm(gatewayDetails.getGatewayId()).users().search(user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0, 1);
                UserResource retrievedUser = client.realm(gatewayDetails.getGatewayId()).users().get(retrieveCreatedUserList.get(0).getId());

                // Add user to the "admin" role
                RoleResource adminRoleResource = client.realm(gatewayDetails.getGatewayId()).roles().get("admin");
                retrievedUser.roles().realmLevel().add(Arrays.asList(adminRoleResource.toRepresentation()));

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(tenantAdminPassword);
                credential.setTemporary(false);
                retrievedUser.resetPassword(credential);
                List<ClientRepresentation> realmClients = client.realm(gatewayDetails.getGatewayId()).clients().findAll();
                String realmManagementClientId=getRealmManagementClientId(client, gatewayDetails.getGatewayId());
                for(ClientRepresentation realmClient : realmClients){
                    if(realmClient.getClientId().equals("realm-management")){
                        realmManagementClientId = realmClient.getId();
                    }
                }
                retrievedUser.roles().clientLevel(realmManagementClientId).add(retrievedUser.roles().clientLevel(realmManagementClientId).listAvailable());
                return true;
            } else {
                logger.error("Request for Tenant Admin Account Creation failed with HTTP code : " + httpResponse.getStatus());
                logger.error("Reason for Tenant Admin account creation failure : " + httpResponse.getStatusInfo());
                return false;
            }
        }catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        }catch (Exception ex){
            logger.error("Error creating Realm Admin Account in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error creating Realm Admin Account in keycloak server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public Gateway configureClient(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), this.superAdminRealmId, isSuperAdminPasswordCreds);
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
            if(gatewayDetails.getGatewayURL()!=null){
                String gatewayURL = gatewayDetails.getGatewayURL();
                // Remove trailing slash from gatewayURL
                if(gatewayURL.endsWith("/")) {
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
            Response httpResponse = client.realms().realm(gatewayDetails.getGatewayId()).clients().create(pgaClient);
            logger.info("Tenant Client configuration exited with code : " + httpResponse.getStatus()+" : " +httpResponse.getStatusInfo());

            // Add the manage-users and manage-clients roles to the web client
            UserRepresentation serviceAccountUserRepresentation = getUserByUsername(client, gatewayDetails.getGatewayId(), "service-account-" + pgaClient.getClientId());
            UserResource serviceAccountUser = client.realms().realm(gatewayDetails.getGatewayId()).users().get(serviceAccountUserRepresentation.getId());
            String realmManagementClientId = getRealmManagementClientId(client, gatewayDetails.getGatewayId());
            List<RoleRepresentation> manageUsersAndManageClientsRoles = serviceAccountUser.roles().clientLevel(realmManagementClientId).listAvailable()
                    .stream()
                    .filter(r -> r.getName().equals("manage-users") || r.getName().equals("manage-clients"))
                    .collect(Collectors.toList());
            serviceAccountUser.roles().clientLevel(realmManagementClientId).add(manageUsersAndManageClientsRoles);

            if(httpResponse.getStatus() == 201){
                String ClientUUID = client.realms().realm(gatewayDetails.getGatewayId()).clients().findByClientId(pgaClient.getClientId()).get(0).getId();
                CredentialRepresentation clientSecret = client.realms().realm(gatewayDetails.getGatewayId()).clients().get(ClientUUID).getSecret();
                gatewayDetails.setOauthClientId(pgaClient.getClientId());
                gatewayDetails.setOauthClientSecret(clientSecret.getValue());
                return gatewayDetails;
            } else {
                logger.error("Request for Realm Client Creation failed with HTTP code : " + httpResponse.getStatus());
                logger.error("Reason for Realm Client Creation failure : " + httpResponse.getStatusInfo());
                return null;
            }
        }catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private static String getRealmManagementClientId(Keycloak client, String realmId) {
        List<ClientRepresentation> realmClients = client.realm(realmId).clients().findAll();
        String realmManagementClientId=null;
        for(ClientRepresentation realmClient : realmClients){
            if(realmClient.getClientId().equals("realm-management")){
                realmManagementClientId = realmClient.getId();
            }
        }
        return realmManagementClientId;
    }

    @Override
    public boolean isUsernameAvailable(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        Keycloak client = null;
        try {
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            return userRepresentation == null;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean createUser(String accessToken, String tenantId, String username, String emailAddress, String firstName, String lastName, String newPassword) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(emailAddress);
            user.setEnabled(false);
            Response httpResponse = client.realm(tenantId).users().create(user);
            if (httpResponse.getStatus() == 201) { //HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = client.realm(tenantId).users().search(user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0, 1);
                UserResource retrievedUser = client.realm(tenantId).users().get(retrieveCreatedUserList.get(0).getId());
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                retrievedUser.resetPassword(credential);
                return true;
            } else {
                logger.error("Request for user Account Creation failed with HTTP code : " + httpResponse.getStatus());
                logger.error("Reason for user account creation failure : " + httpResponse.getStatusInfo());
                return false;
            }
        }catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean enableUserAccount(String accessToken, String tenantId, String username) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            UserResource userResource = client.realm(tenantId).users().get(userRepresentation.getId());
            UserRepresentation profile = userResource.toRepresentation();
            profile.setEnabled(true);
            // We require that a user verify their email before enabling the account
            profile.setEmailVerified(true);
            userResource.update(profile);
            return true;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean isUserAccountEnabled(String accessToken, String tenantId, String username) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            return userRepresentation != null && userRepresentation.isEnabled();
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean isUserExist(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            return userRepresentation != null;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public UserProfile getUser(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            return userRepresentation != null ? convertUserRepresentationToUserProfile(userRepresentation, tenantId) : null;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public List<UserProfile> getUsers(String accessToken, String tenantId, int offset, int limit, String search)
            throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            List<UserRepresentation> userRepresentationList = client.realm(tenantId).users().search(search, offset, limit);
            return userRepresentationList.stream().map(ur -> convertUserRepresentationToUserProfile(ur, tenantId))
                    .collect(Collectors.toList());
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean resetUserPassword(String accessToken, String tenantId, String username, String newPassword) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            if(userRepresentation != null)
            {
                UserResource retrievedUser = client.realm(tenantId).users().get(userRepresentation.getId());
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                retrievedUser.resetPassword(credential);
                // Remove the UPDATE_PASSWORD required action
                userRepresentation = retrievedUser.toRepresentation();
                userRepresentation.getRequiredActions().remove("UPDATE_PASSWORD");
                retrievedUser.update(userRepresentation);
                return true;
            }else{
                logger.error("requested User not found");
                return false;
            }
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } catch (Exception ex){
            logger.error("Error resetting user password in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error resetting user password in keycloak server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public List<UserProfile> findUser(String accessToken, String tenantId, String email, String userName) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            List<UserRepresentation> retrieveUserList = client.realm(tenantId).users().search(userName,
                    null,
                    null,
                    email,
                    0, 1);
            if(!retrieveUserList.isEmpty())
            {
                List<UserProfile> userList = new ArrayList<>();
                for(UserRepresentation user : retrieveUserList){
                    UserProfile profile = new UserProfile();
                    profile.setUserId(user.getUsername());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setEmails(Arrays.asList(new String[]{user.getEmail()}));
                    userList.add(profile);
                }
                return userList;
            }else{
                logger.error("requested User not found");
                return null;
            }
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } catch (Exception ex){
            logger.error("Error finding user in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error finding user in keycloak server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public void updateUserProfile(String accessToken, String tenantId, String username, UserProfile userDetails) throws IamAdminServicesException {

        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            if(userRepresentation != null)
            {
                userRepresentation.setFirstName(userDetails.getFirstName());
                userRepresentation.setLastName(userDetails.getLastName());
                userRepresentation.setEmail(userDetails.getEmails().get(0));
                UserResource userResource = client.realm(tenantId).users().get(userRepresentation.getId());
                userResource.update(userRepresentation);
            }else{
                throw new IamAdminServicesException("User [" + username + "] wasn't found in Keycloak!");
            }
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } catch (Exception ex){
            logger.error("Error updating user profile in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error updating user profile in keycloak server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean deleteUser(String accessToken, String tenantId, String username) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, accessToken);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            if(userRepresentation != null)
            {
                client.realm(tenantId).users().delete(userRepresentation.getId());
                return true;
            }else{
                throw new IamAdminServicesException("User [" + username + "] wasn't found in Keycloak!");
            }
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } catch (Exception ex){
            logger.error("Error deleting user in keycloak server, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error deleting user in keycloak server, reason: " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean addRoleToUser(PasswordCredential realmAdminCreds, String tenantId, String username, String roleName) throws IamAdminServicesException {

        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveCreatedUserList = client.realm(tenantId).users().search(username,
                    null,
                    null,
                    null,
                    0, 1);
            UserResource retrievedUser = client.realm(tenantId).users().get(retrieveCreatedUserList.get(0).getId());

            // Add user to the role
            RoleResource roleResource = client.realm(tenantId).roles().get(roleName);
            retrievedUser.roles().realmLevel().add(Arrays.asList(roleResource.toRepresentation()));
            return true;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Override
    public boolean removeRoleFromUser(PasswordCredential realmAdminCreds, String tenantId, String username, String roleName) throws IamAdminServicesException {

        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveCreatedUserList = client.realm(tenantId).users().search(username,
                    null,
                    null,
                    null,
                    0, 1);
            UserResource retrievedUser = client.realm(tenantId).users().get(retrieveCreatedUserList.get(0).getId());

            // Remove role from user
            RoleResource roleResource = client.realm(tenantId).roles().get(roleName);
            retrievedUser.roles().realmLevel().remove(Arrays.asList(roleResource.toRepresentation()));
            return true;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    // TODO: this is needed for migrating from roles to group-based auth but after migration we can remove this
    @Override
    public List<UserProfile> getUsersWithRole(PasswordCredential realmAdminCreds, String tenantId, String roleName) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            // FIXME: this only searches through the most recent 100 users for the given role (assuming there are no more than 10,000 users in the gateway)
            int totalUserCount = client.realm(tenantId).users().count();
            logger.debug("getUsersWithRole: totalUserCount=" + totalUserCount);
            // Load all users in batches
            List<UserRepresentation> allUsers = new ArrayList<>();
            int userBatchSize = 100;
            for (int start = 0; start < totalUserCount; start=start+userBatchSize) {

                logger.debug("getUsersWithRole: fetching " + userBatchSize + " users...");
                allUsers.addAll(client.realm(tenantId).users().search(null,
                        null,
                        null,
                        null,
                        start, userBatchSize));
            }
            logger.debug("getUsersWithRole: all users count=" + allUsers.size());
            allUsers.sort((a, b) -> a.getCreatedTimestamp() - b.getCreatedTimestamp() > 0 ? -1 : 1);
            // The 100 most recently created users
            List<UserRepresentation> mostRecentUsers = allUsers.subList(0, Math.min(allUsers.size(), 100));
            logger.debug("getUsersWithRole: most recent users count=" + mostRecentUsers.size());

            List<UserProfile> usersWithRole = new ArrayList<>();
            for (UserRepresentation user: mostRecentUsers) {
                UserResource userResource = client.realm(tenantId).users().get(user.getId());

                List<RoleRepresentation> roleRepresentations = userResource.roles().realmLevel().listAll();
                for (RoleRepresentation roleRepresentation : roleRepresentations){
                    if (roleRepresentation.getName().equals(roleName)) {
                        usersWithRole.add(convertUserRepresentationToUserProfile(user, tenantId));
                        break;
                    }
                }
            }
            logger.debug("getUsersWithRole: most recent users with role count=" + usersWithRole.size());
            return usersWithRole;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                logger.debug("getUsersWithRole: closing client...");
                client.close();
                logger.debug("getUsersWithRole: client closed");
            }
        }
    }

    public List<String> getUserRoles(PasswordCredential realmAdminCreds, String tenantId, String username) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            UserRepresentation userRepresentation = getUserByUsername(client, tenantId, username);
            if (userRepresentation == null) {
                logger.warn("No Keycloak user found for username [" + username + "] in tenant [" + tenantId + "].");
                return null;
            }
            UserResource retrievedUser = client.realm(tenantId).users().get(userRepresentation.getId());
            return retrievedUser.roles().realmLevel().listAll()
                    .stream()
                    .map(roleRepresentation -> roleRepresentation.getName())
                    .collect(Collectors.toList());
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        } finally {
            if (client != null) {
                logger.debug("getUserRoles: closing client...");
                client.close();
                logger.debug("getUserRoles: client closed");
            }
        }
    }

    private UserProfile convertUserRepresentationToUserProfile(UserRepresentation userRepresentation, String tenantId) {

        UserProfile profile = new UserProfile();
        profile.setAiravataInternalUserId(userRepresentation.getUsername() + "@" + tenantId);
        profile.setGatewayId(tenantId);
        profile.setUserId(userRepresentation.getUsername());
        profile.setFirstName(userRepresentation.getFirstName());
        profile.setLastName(userRepresentation.getLastName());
        profile.setEmails(Arrays.asList(new String[]{userRepresentation.getEmail()}));
        profile.setCreationTime(userRepresentation.getCreatedTimestamp());

        // Just default these. UserProfile isn't a great data model for this data since it isn't actually the Airavata UserProfile
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

    private static UserRepresentation getUserByUsername(Keycloak client, String tenantId, String username) {

        // Searching for users by username returns also partial matches, so need to filter down to an exact match if it exists
        List<UserRepresentation> userResourceList = client.realm(tenantId).users().search(
                username, null, null, null, null, null);
        for (UserRepresentation userRepresentation : userResourceList) {
            if (userRepresentation.getUsername().equals(username)) {
                return userRepresentation;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IamAdminServicesException, ApplicationSettingsException {
        TenantManagementKeycloakImpl tenantManagementKeycloak = new TenantManagementKeycloakImpl();
        // If testing with self-signed certificate, load certificate into modules/configuration/server/src/main/resources/client_truststore.jks and uncomment the following
        // ServerSettings.setSetting("trust.store", "./modules/configuration/server/src/main/resources/client_truststore.jks");
        // ServerSettings.setSetting("trust.store.password", "airavata");
        ServerSettings.setSetting("iam.server.url", "");
        String accessToken = "";
        String tenantId = "";
        String username = "";
        boolean isUsernameAvailable = tenantManagementKeycloak.isUsernameAvailable(accessToken, tenantId, username);
        System.out.println("Username " + username + " is " + (isUsernameAvailable ? "": "NOT ") + "available");
    }
}
