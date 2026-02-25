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
package org.apache.airavata.iam.keycloak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.KeycloakTestConfig;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.iam.exception.IamAdminServicesException;
import org.apache.airavata.iam.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Keycloak gateway and user management.
 * Tests verify KeycloakGatewayManagement against a real Keycloak instance.
 *
 * Uses devcontainer Keycloak on port 18080 if available, otherwise falls back to Testcontainers.
 * Tests will be skipped if neither is available.
 */
@SpringBootTest(classes = KeycloakTestConfig.class)
@ActiveProfiles("test")
@EnabledIf("isKeycloakAvailable")
public class SetupNewGateway {

    private static final Logger logger = LoggerFactory.getLogger(SetupNewGateway.class);

    @Autowired
    private ServerProperties properties;

    private KeycloakGatewayManagement client;
    private String testGatewayId;
    private PasswordCredential superAdminCreds;

    /**
     * Check if Keycloak is available (devcontainer or Docker for Testcontainers).
     */
    static boolean isKeycloakAvailable() {
        return KeycloakTestConfig.isKeycloakAvailable();
    }

    @BeforeEach
    public void setUp() {
        // Use unique gateway ID to avoid conflicts between test runs
        testGatewayId = "test-gateway-" + UUID.randomUUID().toString().substring(0, 8);

        // Initialize client with Testcontainers Keycloak configuration
        client = new KeycloakGatewayManagement(properties);

        // Set up super admin credentials using Testcontainers Keycloak admin
        superAdminCreds = new PasswordCredential();
        superAdminCreds.setGatewayId("master");
        superAdminCreds.setDescription("Keycloak super admin credentials");
        superAdminCreds.setLoginUserName("admin");
        superAdminCreds.setPassword("admin");
        superAdminCreds.setUserId("admin");
    }

    @Test
    public void testSetUpGateway() throws IamAdminServicesException {
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId(testGatewayId);
        testGateway.setGatewayName("Test Gateway " + testGatewayId);
        testGateway.setEmailAddress("admin@" + testGatewayId + ".test");
        testGateway.setDomain("http://localhost:8080");

        // Create the gateway realm in Keycloak
        Gateway createdGateway = client.addTenant(superAdminCreds, testGateway);
        assertNotNull(createdGateway, "Gateway should be created");

        // Create gateway admin account
        boolean adminCreated = client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123");
        assertTrue(adminCreated, "Admin account should be created");

        // Configure OAuth client in Keycloak
        client.configureClient(superAdminCreds, testGateway);

        logger.info("Gateway {} created with OAuth client configured in Keycloak", testGatewayId);
    }

    @Test
    public void testUserRegistration() throws IamAdminServicesException {
        // First set up a gateway for user registration
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId(testGatewayId);
        testGateway.setGatewayName("Test Gateway " + testGatewayId);
        testGateway.setEmailAddress("admin@" + testGatewayId + ".test");
        testGateway.setDomain("http://localhost:8080");

        client.addTenant(superAdminCreds, testGateway);
        client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123");
        client.configureClient(superAdminCreds, testGateway);

        // Create test user
        String userId = "testuser-" + UUID.randomUUID().toString().substring(0, 8);
        String email = userId + "@test.example.com";

        // Get access token from admin (use master realm for super admin auth)
        String accessToken = client.getAdminAccessToken(superAdminCreds, "master");
        assertNotNull(accessToken, "Access token should not be null");

        // Create user
        boolean userCreated =
                client.createUser(accessToken, testGatewayId, userId, email, "Test", "User", "Password@123");
        assertTrue(userCreated, "User should be created");

        // Enable user account
        boolean enabled = client.enableUserAccount(accessToken, testGatewayId, userId);
        assertTrue(enabled, "User account should be enabled");

        // Verify user exists
        boolean userExists = client.isUserExist(accessToken, testGatewayId, userId);
        assertTrue(userExists, "User should exist");

        logger.info("User {} created in gateway {}", userId, testGatewayId);
    }

    @Test
    public void testFindUser() throws IamAdminServicesException {
        // First set up a gateway and user
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId(testGatewayId);
        testGateway.setGatewayName("Test Gateway " + testGatewayId);
        testGateway.setEmailAddress("admin@" + testGatewayId + ".test");
        testGateway.setDomain("http://localhost:8080");

        client.addTenant(superAdminCreds, testGateway);
        client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123");
        client.configureClient(superAdminCreds, testGateway);

        // Get access token from master realm (super admin auth)
        String accessToken = client.getAdminAccessToken(superAdminCreds, "master");

        // Create a user to find
        String userId = "findme-" + UUID.randomUUID().toString().substring(0, 8);
        String email = userId + "@findtest.example.com";

        client.createUser(accessToken, testGatewayId, userId, email, "Find", "Me", "Password@123");
        client.enableUserAccount(accessToken, testGatewayId, userId);

        // Find the user by email
        List<UserProfile> foundUsers = client.findUser(accessToken, testGatewayId, email, null);
        assertNotNull(foundUsers, "User list should not be null");
        assertFalse(foundUsers.isEmpty(), "Should find at least one user");
        assertEquals(userId, foundUsers.get(0).getUserId(), "Found user ID should match");

        logger.info("Found user: {}", foundUsers.get(0).getUserId());
    }

    @Test
    public void testGetUserRoles() throws IamAdminServicesException {
        // Set up gateway with user
        Gateway testGateway = new Gateway();
        testGateway.setGatewayId(testGatewayId);
        testGateway.setGatewayName("Test Gateway " + testGatewayId);
        testGateway.setEmailAddress("admin@" + testGatewayId + ".test");
        testGateway.setDomain("http://localhost:8080");

        client.addTenant(superAdminCreds, testGateway);
        client.createTenantAdminAccount(superAdminCreds, testGateway, "Test@123");
        client.configureClient(superAdminCreds, testGateway);

        // Get access token from master realm (super admin auth)
        String accessToken = client.getAdminAccessToken(superAdminCreds, "master");

        // Create a user
        String userId = "roletest-" + UUID.randomUUID().toString().substring(0, 8);
        String email = userId + "@roletest.example.com";

        client.createUser(accessToken, testGatewayId, userId, email, "Role", "Test", "Password@123");
        client.enableUserAccount(accessToken, testGatewayId, userId);

        // Get user roles - new users typically have default roles
        PasswordCredential gatewayAdminCreds = new PasswordCredential();
        gatewayAdminCreds.setGatewayId(testGatewayId);
        gatewayAdminCreds.setLoginUserName("admin@" + testGatewayId + ".test");
        gatewayAdminCreds.setPassword("Test@123");

        List<String> roles = client.getUserRoles(gatewayAdminCreds, testGatewayId, userId);
        assertNotNull(roles, "Roles list should not be null");

        logger.info("User {} has roles: {}", userId, roles);
    }
}
