package org.apache.airavata;

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

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakIdentityServerClient {

    private Keycloak client;

    public KeycloakIdentityServerClient(String adminUrl, String adminUserName, String adminUserPassword, String trustStorePath, String trustStorePassword) {
        KeyStore trustKeyStore = loadKeyStore(trustStorePath, trustStorePassword);
        this.client = getClient(
                adminUrl,
                "master", // the realm to log in to
                adminUserName, adminUserPassword,  // the user
                "admin-cli", // admin-cli is the client ID used for keycloak admin operations.
                trustKeyStore);
    }
    private Keycloak getClient(String adminUrl, String realm, String adminUserName, String adminUserPassword, String clientId, KeyStore trustKeyStore) {

        ResteasyClient resteasyClient = new ResteasyClientBuilder()
                .connectionPoolSize(10)
                .trustStore(trustKeyStore)
                .build();
        return KeycloakBuilder.builder()
                .serverUrl(adminUrl)
                .realm(realm)
                .username(adminUserName)
                .password(adminUserPassword)
                .clientId(clientId)
                .resteasyClient(resteasyClient)
                .build();
    }

    private KeyStore loadKeyStore(String trustStorePath, String trustStorePassword) {

        FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(trustStorePath);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, trustStorePassword.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trust store KeyStore instance", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close trust store FileInputStream", e);
                }
            }
        }
    }

    void migrateUserStore(List<UserProfileDAO> userProfiles, String targetRealm, String tempPassword, Map<String,String> roleConversionMap){

        Map<String, RoleRepresentation> allRealmRoles = getRealmRoleNameMap(targetRealm);

        for(UserProfileDAO userProfile : userProfiles){
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userProfile.getUserName());
            user.setFirstName(userProfile.getFirstName());
            user.setLastName(userProfile.getLastName());
            user.setEmail(userProfile.getEmail());
            user.setEmailVerified(true);
            user.setEnabled(true);
            List<String> requiredActionList = new ArrayList<>();
            requiredActionList.add("UPDATE_PASSWORD");
            user.setRequiredActions(requiredActionList);
            Response httpResponse = this.client.realm(targetRealm).users().create(user);
            System.out.println(httpResponse.getStatus());
            if(httpResponse.getStatus() == 201){ //HTTP code for record creation: HTTP 201
                List<UserRepresentation> retrieveCreatedUserList = this.client.realm(targetRealm).users().search(user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        0,1);
                UserResource retirievedUser = this.client.realm(targetRealm).users().get(retrieveCreatedUserList.get(0).getId());

                // Add user to realm roles
                List<RoleRepresentation> userRealmRoles = userProfile.getRoles().stream()
                        .filter(r -> roleConversionMap.containsKey(r))
                        // Convert from IS role name to Keycloak role name
                        .map(r -> roleConversionMap.get(r))
                        // Convert from Keycloak role name to RoleRepresentation
                        .map(r -> allRealmRoles.get(r))
                        .collect(Collectors.toList());
                retirievedUser.roles().realmLevel().add(userRealmRoles);

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(tempPassword);
                credential.setTemporary(true);
                retirievedUser.resetPassword(credential);
                System.out.println("User profile for user " + userProfile.getUserName() + " successfully migrated");
            } else {
                String response = httpResponse.readEntity(String.class);
                System.err.println("Failed to add user [" + userProfile.getUserName() + "] to Keycloak");
                System.err.println("Response: " + response);
            }
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    public void setUserPassword(String realmId, String username, String newPassword) {
        List<UserRepresentation> retrieveUserList = client.realm(realmId).users().search(username,
                null,
                null,
                null,
                0, 1);
        if (!retrieveUserList.isEmpty()) {
            UserResource retrievedUser = client.realm(realmId).users().get(retrieveUserList.get(0).getId());
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);
            retrievedUser.resetPassword(credential);
            // Remove the UPDATE_PASSWORD required action
            UserRepresentation userRepresentation = retrievedUser.toRepresentation();
            userRepresentation.getRequiredActions().remove("UPDATE_PASSWORD");
            retrievedUser.update(userRepresentation);
        } else {
            throw new RuntimeException("Requested user not found");
        }
    }

    private Map<String,RoleRepresentation> getRealmRoleNameMap(String targetRealm) {
        return this.client.realm(targetRealm).roles().list()
                .stream()
                .collect(Collectors.toMap(r -> r.getName(), r -> r));
    }

}