/*
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
 *
 */

package org.apache.airavata.service.profile.iam.admin.services.core.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
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
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TenantManagementKeycloakImpl implements TenantManagementInterface {

    private final static Logger logger = LoggerFactory.getLogger(TenantManagementKeycloakImpl.class);

    private String superAdminRealmId = "master";

    private static Keycloak getClient(String adminUrl, String realm, PasswordCredential AdminPasswordCreds) {

        ResteasyClient resteasyClient = new ResteasyClientBuilder()
                .connectionPoolSize(10)
                .trustStore(loadKeyStore())
                .build();
        return KeycloakBuilder.builder()
                .serverUrl(adminUrl)
                .realm(realm)
                .username(AdminPasswordCreds.getLoginUserName())
                .password(AdminPasswordCreds.getPassword())
                .clientId("admin-cli")
                .resteasyClient(resteasyClient)
                .build();
    }

    private static KeyStore loadKeyStore() {

        FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(ServerSettings.getTrustStorePath());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, ServerSettings.getTrustStorePassword().toCharArray());
            return ks;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trust store KeyStore instance", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
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
    public boolean createTenantAdminAccount(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), this.superAdminRealmId, isSuperAdminPasswordCreds);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(gatewayDetails.getIdentityServerUserName());
            user.setFirstName(gatewayDetails.getGatewayAdminFirstName());
            user.setLastName(gatewayDetails.getGatewayAdminLastName());
            user.setEmail(gatewayDetails.getGatewayAdminEmail());
            user.setEnabled(true);
            List<String> requiredActionList = new ArrayList<>();
            requiredActionList.add("UPDATE_PASSWORD");
            user.setRequiredActions(requiredActionList);
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
                credential.setValue(ServerSettings.getGatewayAdminTempPwd());
                credential.setTemporary(true);
                retrievedUser.resetPassword(credential);
                List<ClientRepresentation> realmClients = client.realm(gatewayDetails.getGatewayId()).clients().findAll();
                String realmManagementClientId=null;
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
            String[] defaultRoles = {"gateway-user"};
            pgaClient.setDefaultRoles(defaultRoles);
            List<String> redirectUris = new ArrayList<>();
            if(gatewayDetails.getGatewayURL()!=null){
                if(gatewayDetails.getGatewayURL().endsWith("/")){
                    redirectUris.add(gatewayDetails.getGatewayURL() + "callback-url");
                } else {
                    redirectUris.add(gatewayDetails.getGatewayURL() + "/callback-url");
                }
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

    @Override
    public boolean createUser(PasswordCredential realmAdminCreds, String tenantId, String username, String emailAddress, String firstName, String lastName, String newPassword) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
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
        return false;
    }

    @Override
    public boolean enableUserAccount(PasswordCredential realmAdminCreds, String tenantId, String username) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            List<UserRepresentation> userResourceList = client.realm(tenantId).users().search(username,0,1);
            UserResource userResource = client.realm(tenantId).users().get(userResourceList.get(0).getId());
            UserRepresentation profile = userResource.toRepresentation();
            profile.setEnabled(true);
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
    public boolean resetUserPassword(PasswordCredential realmAdminCreds, String tenantId, String username, String newPassword) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveUserList = client.realm(tenantId).users().search(username,
                    null,
                    null,
                    null,
                    0, 1);
            if(!retrieveUserList.isEmpty())
            {
                UserResource retrievedUser = client.realm(tenantId).users().get(retrieveUserList.get(0).getId());
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newPassword);
                credential.setTemporary(false);
                retrievedUser.resetPassword(credential);
                // Remove the UPDATE_PASSWORD required action
                UserRepresentation userRepresentation = retrievedUser.toRepresentation();
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
    public List<UserProfile> findUser(PasswordCredential realmAdminCreds, String tenantId, String email, String userName) throws IamAdminServicesException{
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
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
    public void updateUserProfile(PasswordCredential realmAdminCreds, String tenantId, String username, UserProfile userDetails) throws IamAdminServicesException {

        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            List<UserRepresentation> retrieveUserList = client.realm(tenantId).users().search(username,
                    null,
                    null,
                    null,
                    0, 1);
            if(!retrieveUserList.isEmpty())
            {
                UserRepresentation userRepresentation = retrieveUserList.get(0);
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

    @Override
    public List<UserProfile> getUsersWithRole(PasswordCredential realmAdminCreds, String tenantId, String roleName) throws IamAdminServicesException {
        Keycloak client = null;
        try{
            client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, realmAdminCreds);
            // FIXME: this only gets the first 1000 users to search through for the given role
            List<UserRepresentation> allUsers = client.realm(tenantId).users().search(null,
                    null,
                    null,
                    null,
                    0, 1000);

            List<UserProfile> usersWithRole = new ArrayList<>();
            for (UserRepresentation user: allUsers) {
                if (user.getRealmRoles().contains(roleName)) {
                    usersWithRole.add(convertUserRepresentationToUserProfile(user, tenantId));
                }
            }
            return usersWithRole;
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

    private UserProfile convertUserRepresentationToUserProfile(UserRepresentation userRepresentation, String tenantId) {

        UserProfile profile = new UserProfile();
        profile.setAiravataInternalUserId(userRepresentation.getUsername() + "@" + tenantId);
        profile.setGatewayId(tenantId);
        profile.setUserId(userRepresentation.getUsername());
        profile.setFirstName(userRepresentation.getFirstName());
        profile.setLastName(userRepresentation.getLastName());
        profile.setEmails(Arrays.asList(new String[]{userRepresentation.getEmail()}));

        // Just default these. UserProfile isn't a great data model for this data since it isn't actually the Airavata UserProfile
        profile.setLastAccessTime(0);
        profile.setCreationTime(0);
        profile.setValidUntil(0);
        profile.setState(Status.ACTIVE);

        return profile;
    }
}
