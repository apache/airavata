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
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Arrays;

public class KeycloakIdentityServerClient {

    public void setAdminUserName(String adminUserName) {
        adminUserName = adminUserName;
    }

    public void setAdminUserPassword(String adminUserPassword) {
        this.adminUserPassword = adminUserPassword;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    private String adminUrl;
    private String realm;
    private String adminUserName;
    private String adminUserPassword;
    private Keycloak client;

    public KeycloakIdentityServerClient(String adminUrl, String realm, String adminUserName, String adminUserPassword) {
        this.adminUrl = adminUrl;
        this.realm = realm;
        this.adminUserName = adminUserName;
        this.adminUserPassword = adminUserPassword;
        this.client = Keycloak.getInstance(
                this.adminUrl,
                this.realm, // the realm to log in to
                this.adminUserName, this.adminUserPassword,  // the user
                "security-admin-console");
    }

    boolean createUser(){

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("test123");
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCredentials(Arrays.asList(credential));
        this.client.realm(this.realm).users().create(user);
        return true;
    }

    public static void main(String[] args){
        KeycloakIdentityServerClient client = new KeycloakIdentityServerClient("https://iam.scigap.org/auth",
                                                                        "accord.scigap.org",
                                                                        "AccordAdmin",
                                                                        "Accord@123");
        client.createUser();
    }

}
