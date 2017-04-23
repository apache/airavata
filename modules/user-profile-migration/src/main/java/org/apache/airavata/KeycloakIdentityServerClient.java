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

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class KeycloakIdentityServerClient {

    private Keycloak client;

    public KeycloakIdentityServerClient(String adminUrl, String realm, String adminUserName, String adminUserPassword) {
        this.client = Keycloak.getInstance(
                adminUrl,
                realm, // the realm to log in to
                adminUserName, adminUserPassword,  // the user
                "admin-cli"); // admin-cli is the client ID used for keycloak admin operations.
    }

    boolean migrateUserStore(List<UserProfileDAO> userProfiles, String targetRealm, String tempPassword){

        for(UserProfileDAO userProfile : userProfiles){
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userProfile.getUserName());
            user.setFirstName(userProfile.getFirstName());
            user.setLastName(userProfile.getLastName());
            user.setEmail(userProfile.getEmail());
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
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(tempPassword);
                credential.setTemporary(true);
                retirievedUser.resetPassword(credential);
                System.out.println("User profile for user " + userProfile.getUserName() + " successfully migrated");
            }else{ return false; }
        }
        return true;
    }

}