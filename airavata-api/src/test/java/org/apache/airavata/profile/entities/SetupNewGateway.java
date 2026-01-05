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
package org.apache.airavata.profile.entities;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.utils.TenantManagementKeycloakImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires external Keycloak infrastructure")
public class SetupNewGateway {

    private static final Logger logger = LoggerFactory.getLogger(SetupNewGateway.class);

    @Test
    public void testFindUser() {
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
            List<UserProfile> list = client.findUser(accessToken, "maven.test.gateway", "some.man@outlook.com", null);
            assertNotNull(list, "User list should not be null");
            if (!list.isEmpty()) {
                logger.info("User ID: {}", list.get(0).getUserId());
            }
        } catch (IamAdminServicesException e) {
            logger.error("Error finding user", e);
            fail("Failed to find user: " + e.getMessage());
        }
    }

    @Test
    @Disabled("Requires Keycloak setup and admin credentials")
    public void testSetUpGateway() {
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
            boolean adminCreated = client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123");
            assertTrue(adminCreated, "Admin account should be created");
            Gateway gatewayWithIdAndSecret = client.configureClient(superAdminCreds, testGateway);
            assertNotNull(gatewayWithIdAndSecret.getOauthClientId(), "OAuth Client ID should be set");
            assertNotNull(gatewayWithIdAndSecret.getOauthClientSecret(), "OAuth Client Secret should be set");
            logger.info("OAuth Client ID: {}", gatewayWithIdAndSecret.getOauthClientId());
            logger.info("OAuth Client Secret: {}", gatewayWithIdAndSecret.getOauthClientSecret());
        } catch (IamAdminServicesException ex) {
            logger.error("Gateway Setup Failed, reason: " + ex.getCause(), ex);
            fail("Gateway setup failed: " + ex.getMessage());
        }
    }

    @Test
    @Disabled("Requires Keycloak setup and admin credentials")
    public void testUserRegistration() {
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
            client.createUser(
                    accessToken,
                    user.getGatewayId(),
                    user.getUserId(),
                    user.getEmails().get(0),
                    user.getFirstName(),
                    user.getLastName(),
                    "test@123");
            client.enableUserAccount(accessToken, user.getGatewayId(), user.getUserId());
        } catch (IamAdminServicesException e) {
            logger.error("User registration failed", e);
            fail("User registration failed: " + e.getMessage());
        }
    }

    @Test
    @Disabled("Requires Keycloak setup and admin credentials")
    public void testGetUserRoles() {
        PasswordCredential tenantAdminCreds = new PasswordCredential();
        tenantAdminCreds.setGatewayId("maven.test.gateway");
        tenantAdminCreds.setLoginUserName("mavenTest");
        tenantAdminCreds.setPassword("Test@1234");
        tenantAdminCreds.setPortalUserName("TenantAdmin");
        
        TenantManagementKeycloakImpl keycloakClient = new TenantManagementKeycloakImpl();

        try {
            List<String> roleNames =
                    keycloakClient.getUserRoles(tenantAdminCreds, tenantAdminCreds.getGatewayId(), "username");
            assertNotNull(roleNames, "Role names should not be null");
            logger.info("Roles: {}", roleNames);
        } catch (IamAdminServicesException e) {
            logger.error("Failed to get user roles", e);
            fail("Failed to get user roles: " + e.getMessage());
        }
    }
}
