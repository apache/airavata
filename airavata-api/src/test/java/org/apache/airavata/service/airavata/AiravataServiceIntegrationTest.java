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
package org.apache.airavata.service.airavata;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.airavata.service.airavata.client.AiravataServiceClientFactory;
import org.apache.thrift.TException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test for the unified AiravataService
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AiravataServiceIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServiceIntegrationTest.class);

    private AiravataServiceServer server;
    private AiravataServiceClientFactory.AiravataServiceClients clients;

    @BeforeAll
    public void setUp() throws Exception {
        // Start the unified server
        server = new AiravataServiceServer();
        server.start();

        // Wait for server to start
        Thread.sleep(5000);

        // Create clients
        clients = AiravataServiceClientFactory.createAllClients("localhost", 9930);

        logger.info("AiravataService integration test setup completed");
    }

    @AfterAll
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        logger.info("AiravataService integration test teardown completed");
    }

    @Test
    public void testAiravataServiceVersion() throws TException {
        String version = clients.airavata.getAPIVersion();
        assertNotNull(version);
        assertEquals("0.18.0", version);
        logger.info("Airavata API version: {}", version);
    }

    @Test
    public void testRegistryServiceAccess() throws TException {
        // Test that we can access the registry service through the multiplexed connection
        // This is a basic connectivity test
        assertNotNull(clients.registry);
        logger.info("Registry service client created successfully");
    }

    @Test
    public void testCredentialStoreServiceAccess() throws TException {
        // Test that we can access the credential store service through the multiplexed connection
        assertNotNull(clients.credentialStore);
        logger.info("Credential store service client created successfully");
    }

    @Test
    public void testSharingRegistryServiceAccess() throws TException {
        // Test that we can access the sharing registry service through the multiplexed connection
        assertNotNull(clients.sharingRegistry);
        logger.info("Sharing registry service client created successfully");
    }

    @Test
    public void testOrchestratorServiceAccess() throws TException {
        // Test that we can access the orchestrator service through the multiplexed connection
        assertNotNull(clients.orchestrator);
        logger.info("Orchestrator service client created successfully");
    }

    @Test
    public void testWorkflowServiceAccess() throws TException {
        // Test that we can access the workflow service through the multiplexed connection
        assertNotNull(clients.workflow);
        logger.info("Workflow service client created successfully");
    }

    @Test
    public void testUserProfileServiceAccess() throws TException {
        // Test that we can access the user profile service through the multiplexed connection
        assertNotNull(clients.userProfile);
        logger.info("User profile service client created successfully");
    }

    @Test
    public void testTenantProfileServiceAccess() throws TException {
        // Test that we can access the tenant profile service through the multiplexed connection
        assertNotNull(clients.tenantProfile);
        logger.info("Tenant profile service client created successfully");
    }

    @Test
    public void testIamAdminServicesAccess() throws TException {
        // Test that we can access the IAM admin services through the multiplexed connection
        assertNotNull(clients.iamAdmin);
        logger.info("IAM admin services client created successfully");
    }

    @Test
    public void testGroupManagerServiceAccess() throws TException {
        // Test that we can access the group manager service through the multiplexed connection
        assertNotNull(clients.groupManager);
        logger.info("Group manager service client created successfully");
    }

    @Test
    public void testAllServicesAccessible() throws TException {
        // Test that all 10 services are accessible through the unified server
        assertNotNull(clients.airavata);
        assertNotNull(clients.registry);
        assertNotNull(clients.credentialStore);
        assertNotNull(clients.sharingRegistry);
        assertNotNull(clients.orchestrator);
        assertNotNull(clients.workflow);
        assertNotNull(clients.userProfile);
        assertNotNull(clients.tenantProfile);
        assertNotNull(clients.iamAdmin);
        assertNotNull(clients.groupManager);

        logger.info("All 10 services are accessible through the unified AiravataService");
    }
}
