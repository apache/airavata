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
package org.apache.airavata.service.profile.iam.admin.services.core.tests;


import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SetupNewGateway {

    private final static Logger logger = LoggerFactory.getLogger(SetupNewGateway.class);

    public static void main(String[] args) {
        findUser();
//        final PasswordCredential tenantAdminCreds = createTenantAdminCreds("tenant", "admin", "admin-password");
//        getUserRoles(tenantAdminCreds, "username");
    }

    public static void setUpGateway(){
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId("maven.test.gateway");
        testGateway.setGatewayName("maven test gateway");
        testGateway.setIdentityServerUserName("mavenTest");
        testGateway.setGatewayAdminFirstName("Maven");
        testGateway.setGatewayAdminLastName("Test");
        testGateway.setGatewayAdminEmail("some.man@gmail.com");
        PasswordCredential superAdminCreds = new PasswordCredential();
        superAdminCreds.setGatewayId(testGateway.getGatewayId());
        superAdminCreds.setDescription("test credentials for IS admin creation");
        superAdminCreds.setLoginUserName("airavataAdmin");
        superAdminCreds.setPassword("Airavata@123");
        superAdminCreds.setPortalUserName("superAdmin");
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
     public static void UserRegistration(){
         UserProfile user = new UserProfile();
         user.setUserId("testuser");
         user.setFirstName("test-firstname");
         user.setLastName("test-lastname");
         List<String> emails = new ArrayList<>();
         emails.add("some.man@outlook.com");
         user.setGatewayId("maven.test.gateway");
         user.setEmails(emails);
        PasswordCredential tenantAdminCreds = new PasswordCredential();
         tenantAdminCreds.setGatewayId(user.getGatewayId());
         tenantAdminCreds.setDescription("test credentials for tenant admin creation");
         tenantAdminCreds.setLoginUserName("mavenTest");
         tenantAdminCreds.setPassword("Test@1234");
         tenantAdminCreds.setPortalUserName("TenantAdmin");

         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
         try {
             // FIXME: get an access token from tenant admin creds
             String accessToken = "";
             client.createUser(accessToken, user.getGatewayId(), user.getUserId(), user.getEmails().get(0), user.getFirstName(), user.getLastName(),"test@123");
             client.enableUserAccount(accessToken, user.getGatewayId(), user.getUserId());
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }

//     public static void resetPassword(){
//         UserProfile user = new UserProfile();
//         user.setUserId("testuser");
//         List<String> emails = new ArrayList<>();
//         emails.add("some.man@outlook.com");
//         user.setGatewayId("maven.test.gateway");
//         user.setEmails(emails);
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

     public static void findUser(){
         UserProfile user = new UserProfile();

         List<String> emails = new ArrayList<>();
         emails.add("some.man@outlook.com");
         user.setGatewayId("maven.test.gateway");
         user.setEmails(emails);
         TenantManagementKeycloakImpl client = new TenantManagementKeycloakImpl();
         try {
             PasswordCredential tenantAdminCreds = new PasswordCredential();
             tenantAdminCreds.setGatewayId(user.getGatewayId());
             tenantAdminCreds.setDescription("test credentials for tenant admin creation");
             tenantAdminCreds.setLoginUserName("mavenTest");
             tenantAdminCreds.setPassword("Test@1234");
             tenantAdminCreds.setPortalUserName("TenantAdmin");
             // FIXME: get an access token from tenant admin creds
             String accessToken = "";
             List<UserProfile> list = client.findUser(accessToken,"maven.test.gateway","some.man@outlook.com",null);
             System.out.println(list.get(0).getUserId());
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }

     public static void getUserRoles(PasswordCredential tenantAdminCreds, String username) {
         TenantManagementKeycloakImpl keycloakClient = new TenantManagementKeycloakImpl();

         try {
             List<String> roleNames = keycloakClient.getUserRoles(tenantAdminCreds, tenantAdminCreds.getGatewayId(), username);
             System.out.println("Roles=" + roleNames);
         } catch (IamAdminServicesException e) {
             e.printStackTrace();
         }
     }

    private static PasswordCredential createTenantAdminCreds(String tenantId, String username, String password) {
        PasswordCredential tenantAdminCreds = new PasswordCredential();
        tenantAdminCreds.setGatewayId(tenantId);
        tenantAdminCreds.setLoginUserName(username);
        tenantAdminCreds.setPassword(password);
        return tenantAdminCreds;
    }
}
