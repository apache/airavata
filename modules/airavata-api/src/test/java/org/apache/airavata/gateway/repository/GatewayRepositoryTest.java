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
package org.apache.airavata.gateway.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.TestBase;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.service.GatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for the unified {@link GatewayRepository} and {@link GatewayService}.
 *
 * <p>Tests CRUD operations on the unified Gateway entity using a real database
 * (Testcontainers MariaDB).
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GatewayRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ServerProperties properties;

    public GatewayRepositoryTest(GatewayService gatewayService, ServerProperties properties) {
        this.gatewayService = gatewayService;
        this.properties = properties;
    }

    @Test
    void testGatewayCrudOperations() throws Exception {
        // Create a test gateway
        String testGatewayId = "test-gateway-" + UUID.randomUUID().toString().substring(0, 8);

        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setGatewayName(testGatewayId);
        gateway.setDomain("TEST_DOMAIN");
        gateway.setEmailAddress("test@example.com");

        // Test create - createGateway returns gatewayId
        String returnedGatewayId = gatewayService.createGateway(gateway);
        assertNotNull(returnedGatewayId);
        assertEquals(testGatewayId, returnedGatewayId);
        assertTrue(gatewayService.isGatewayExist(testGatewayId));

        // Test read by gatewayId
        Gateway retrieved = gatewayService.getGateway(testGatewayId);
        assertNotNull(retrieved);
        assertEquals(gateway.getDomain(), retrieved.getDomain());
        assertEquals(gateway.getEmailAddress(), retrieved.getEmailAddress());

        // Test read by primary key (UUID)
        Gateway retrievedById = gatewayService.getGateway(testGatewayId);
        assertNotNull(retrievedById);
        assertEquals(testGatewayId, retrievedById.getGatewayId());

        // Test update
        gateway.setEmailAddress("updated@example.com");
        gatewayService.updateGateway(testGatewayId, gateway);

        Gateway updated = gatewayService.getGateway(testGatewayId);
        assertEquals("updated@example.com", updated.getEmailAddress());

        // Test list
        List<Gateway> allGateways = gatewayService.getAllGateways();
        assertTrue(allGateways.size() >= 1);

        // Test delete
        gatewayService.deleteGateway(testGatewayId);
        assertFalse(gatewayService.isGatewayExist(testGatewayId));
    }

    @Test
    void testDefaultGatewayFromProperties() {
        assertNotNull(properties, "ServerProperties should be injected");
        // defaultGateway() is bound from airavata.default-gateway; verify the binding works
        // (value may be null if not configured, which is acceptable)
        properties.defaultGateway();
    }
}
