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
package org.apache.airavata.iam;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.exception.IamAdminServicesException;
import org.apache.airavata.iam.service.TenantManagementKeycloakImpl;
import org.apache.airavata.model.credential.store.proto.PasswordCredential;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupNewGateway {

    private static final Logger logger = LoggerFactory.getLogger(SetupNewGateway.class);

    public static void main(String[] args) {
        findUser();
        //        final PasswordCredential tenantAdminCreds = createTenantAdminCreds("tenant", "admin",
        // "admin-password");
        //        getUserRoles(tenantAdminCreds, "username");
    }

    public static void setUpGateway() {
        Gateway testGateway =
                Gateway.newBuilder().setGatewayId("maven.test.gateway").build();
        testGateway =
                testGateway.toBuilder().setGatewayName("maven test gateway").build();
        testGateway =
                testGateway.toBuilder().setIdentityServerUserName("mavenTest").build();
        testGateway = testGateway.toBuilder().setGatewayAdminFirstName("Maven").build();
        testGateway = testGateway.toBuilder().setGatewayAdminLastName("Test").build();
        testGateway = testGateway.toBuilder()
                .setGatewayAdminEmail("some.man@gmail.com")
                .build();
        PasswordCredential superAdminCreds = PasswordCredential.newBuilder()
                .setGatewayId(testGateway.getGatewayId())
                .build();
        superAdminCreds = superAdminCreds.toBuilder()
                .setDescription("test credentials for IS admin creation")
                .build();
        superAdminCreds =
                superAdminCreds.toBuilder().setLoginUserName("airavataAdmin").build();
        superAdminCreds =
                superAdminCreds.toBuilder().setPassword("Airavata@123").build();
        superAdminCreds =
                superAdminCreds.toBuilder().setPortalUserName("superAdmin").build();
        TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
        try {
            client.addTenant(superAdminCreds, testGateway);
            if (!client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123")) {
                logger.error("Admin account creation failed !!, please refer error logs for reason");
            }
            Gateway gatewayWithIdAndSecret = client.configureClient(superAdminCreds, testGateway);
            System.out.println(gatewayWithIdAndSecret.getOauthClientId());
            System.out.println(gatewayWithIdAndSecret.getOauthClientSecret());
        } catch (IamAdminServicesException ex) {
            logger.error("Gateway Setup Failed, reason: " + ex.getCause(), ex);
        }
    }

    public static void UserRegistration() {
        UserProfile user = UserProfile.newBuilder().setUserId("testuser").build();
        user = user.toBuilder().setFirstName("test-firstname").build();
        user = user.toBuilder().setLastName("test-lastname").build();
        List<String> emails = new ArrayList<>();
        emails.add("some.man@outlook.com");
        user = user.toBuilder().setGatewayId("maven.test.gateway").build();
        user = user.toBuilder().addAllEmails(emails).build();
        PasswordCredential tenantAdminCreds = PasswordCredential.newBuilder()
                .setGatewayId(user.getGatewayId())
                .build();
        tenantAdminCreds = tenantAdminCreds.toBuilder()
                .setDescription("test credentials for tenant admin creation")
                .build();
        tenantAdminCreds =
                tenantAdminCreds.toBuilder().setLoginUserName("mavenTest").build();
        tenantAdminCreds = tenantAdminCreds.toBuilder().setPassword("Test@1234").build();
        tenantAdminCreds =
                tenantAdminCreds.toBuilder().setPortalUserName("TenantAdmin").build();

        TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
        try {
            // FIXME: get an access token from tenant admin creds
            String accessToken = "";
            client.createUser(
                    accessToken,
                    user.getGatewayId(),
                    user.getUserId(),
                    user.getEmailsList().get(0),
                    user.getFirstName(),
                    user.getLastName(),
                    "test@123");
            client.enableUserAccount(accessToken, user.getGatewayId(), user.getUserId());
        } catch (IamAdminServicesException e) {
            e.printStackTrace();
        }
    }

    //     public static void resetPassword(){
    //         UserProfile user = UserProfile.getDefaultInstance();
    //         user.setUserId("testuser");
    //         List<String> emails = new ArrayList<>();
    //         emails.add("some.man@outlook.com");
    //         user.setGatewayId("maven.test.gateway");
    //         user.addAllEmails(emails);
    //         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
    //         try {
    //             PasswordCredential tenantAdminCreds = new PasswordCredential();
    //             tenantAdminCreds.setGatewayId(user.getGatewayId());
    //             tenantAdminCreds.setDescription("test credentials for tenant admin creation");
    //             tenantAdminCreds.setLoginUserName("mavenTest");
    //             tenantAdminCreds.setPassword("Test@1234");
    //             tenantAdminCreds.setPortalUserName("TenantAdmin");
    //             client.resetUserPassword(tenantAdminCreds,user,"test@123");
    //         } catch (IamAdminServicesException e) {
    //             e.printStackTrace();
    //         }
    //     }

    public static void findUser() {
        UserProfile user = UserProfile.getDefaultInstance();

        List<String> emails = new ArrayList<>();
        emails.add("some.man@outlook.com");
        user = user.toBuilder().setGatewayId("maven.test.gateway").build();
        user = user.toBuilder().addAllEmails(emails).build();
        TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
        try {
            PasswordCredential tenantAdminCreds = PasswordCredential.newBuilder()
                    .setGatewayId(user.getGatewayId())
                    .build();
            tenantAdminCreds = tenantAdminCreds.toBuilder()
                    .setDescription("test credentials for tenant admin creation")
                    .build();
            tenantAdminCreds =
                    tenantAdminCreds.toBuilder().setLoginUserName("mavenTest").build();
            tenantAdminCreds =
                    tenantAdminCreds.toBuilder().setPassword("Test@1234").build();
            tenantAdminCreds = tenantAdminCreds.toBuilder()
                    .setPortalUserName("TenantAdmin")
                    .build();
            // FIXME: get an access token from tenant admin creds
            String accessToken = "";
            List<UserProfile> list = client.findUser(accessToken, "maven.test.gateway", "some.man@outlook.com", null);
            System.out.println(list.get(0).getUserId());
        } catch (IamAdminServicesException e) {
            e.printStackTrace();
        }
    }

    public static void getUserRoles(PasswordCredential tenantAdminCreds, String username) {
        TenantManagementKeycloakImpl keycloakClient = new TenantManagementKeycloakImpl();

        try {
            List<String> roleNames =
                    keycloakClient.getUserRoles(tenantAdminCreds, tenantAdminCreds.getGatewayId(), username);
            System.out.println("Roles=" + roleNames);
        } catch (IamAdminServicesException e) {
            e.printStackTrace();
        }
    }

    private static PasswordCredential createTenantAdminCreds(String tenantId, String username, String password) {
        PasswordCredential tenantAdminCreds =
                PasswordCredential.newBuilder().setGatewayId(tenantId).build();
        tenantAdminCreds =
                tenantAdminCreds.toBuilder().setLoginUserName(username).build();
        tenantAdminCreds = tenantAdminCreds.toBuilder().setPassword(password).build();
        return tenantAdminCreds;
    }
}
