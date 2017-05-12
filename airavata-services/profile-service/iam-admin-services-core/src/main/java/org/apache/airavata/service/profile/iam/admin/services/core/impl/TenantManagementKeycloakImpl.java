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
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.core.interfaces.TenantManagementInterface;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TenantManagementKeycloakImpl implements TenantManagementInterface {

    private final static Logger logger = LoggerFactory.getLogger(TenantManagementKeycloakImpl.class);

    private static Keycloak getClient(String adminUrl, String realm, PasswordCredential AdminPasswordCreds) {

        return Keycloak.getInstance(
                adminUrl,
                realm, // the realm to log in to
                AdminPasswordCreds.getLoginUserName(), AdminPasswordCreds.getPassword(),  // the user
                "admin-cli"); // admin-cli is the client ID used for keycloak admin operations.
    }

    private static Keycloak getClient(String adminUrl, String realm, String authToken) {

        return Keycloak.getInstance(
                adminUrl,
                realm, // the realm to log in to
                "admin-cli",
                authToken // the realm admin's auth token
            );
    }

    @Override
    public Gateway addTenant(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException {
        try {
            // get client
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), "master", isSuperAdminPasswordCreds);
            // create realm
            RealmRepresentation newRealmDetails = new RealmRepresentation();
            newRealmDetails.setEnabled(true);
            newRealmDetails.setId(gatewayDetails.getGatewayId());
            newRealmDetails.setDisplayName(gatewayDetails.getGatewayName());
            newRealmDetails.setRealm(gatewayDetails.getGatewayId());
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
        RolesRepresentation rolesRepresentation = new RolesRepresentation();
        rolesRepresentation.setRealm(defaultRoles);
        realmDetails.setRoles(rolesRepresentation);
        return realmDetails;
    }

    @Override
    public boolean createTenantAdminAccount(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), "master", isSuperAdminPasswordCreds);
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
        }
    }

    @Override
    public Gateway configureClient(PasswordCredential isSuperAdminPasswordCreds, Gateway gatewayDetails) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), "master", isSuperAdminPasswordCreds);
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
                ex.setMessage("Gateway Url field in GatewayProfile cannot be empty, Relam Client creation failed");
                throw ex;
            }
            redirectUris.add("http://accord.scigap.org/callback-url");
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
        }
    }

    @Override
    public boolean createUser(PasswordCredential realmAdminCreds, UserProfile userProfile, String newPassword) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), userProfile.getGatewayId(), realmAdminCreds);
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userProfile.getUserId());
            user.setFirstName(userProfile.getFirstName());
            user.setLastName(userProfile.getLastName());
            // Always takes the first value
            List<String> emails = userProfile.getEmails();
            user.setEmail(emails.get(0));
            user.setEnabled(false);
            Response httpResponse = client.realm(userProfile.getGatewayId()).users().create(user);
            if (httpResponse.getStatus() == 201) { //HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = client.realm(userProfile.getGatewayId()).users().search(user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0, 1);
                UserResource retrievedUser = client.realm(userProfile.getGatewayId()).users().get(retrieveCreatedUserList.get(0).getId());
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
        }
        return false;
    }

    @Override
    public boolean enableUserAccount(PasswordCredential realmAdminAccount, UserProfile userDetails) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), userDetails.getGatewayId(), realmAdminAccount);
            List<String> emails = userDetails.getEmails();
            List<UserRepresentation> userResourceList = client.realm(userDetails.getGatewayId()).users().search(userDetails.getUserId(),0,1);
            UserResource userResource = client.realm(userDetails.getGatewayId()).users().get(userResourceList.get(0).getId());
            UserRepresentation profile = userResource.toRepresentation();
            profile.setEnabled(true);
            userResource.update(profile);
            return true;
        } catch (ApplicationSettingsException ex) {
            logger.error("Error getting values from property file, reason: " + ex.getMessage(), ex);
            IamAdminServicesException exception = new IamAdminServicesException();
            exception.setMessage("Error getting values from property file, reason " + ex.getMessage());
            throw exception;
        }
    }

    public boolean resetUserPassword(String authToken, String tenantId, String username, String newPassword) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), tenantId, authToken);
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
        }
    }

    public List<UserProfile> findUser(PasswordCredential realmAdminCreds, String gatewayID, String email, String userName) throws IamAdminServicesException{
        try{
            Keycloak client = TenantManagementKeycloakImpl.getClient(ServerSettings.getIamServerUrl(), gatewayID, realmAdminCreds);
            List<UserRepresentation> retrieveUserList = client.realm(gatewayID).users().search(userName,
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
        }
    }
}
