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

import org.apache.airavata.config.TestBase;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.service.GatewayService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Tests for gateway-groups operations now consolidated on GatewayService.
 *
 * <p>Note: GatewayGroups data is stored in the GATEWAY table, so we need
 * to create a gateway first before setting gateway groups.
 */
@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GatewayGroupsRepositoryTest extends TestBase {

    private static final String GATEWAY_ID = "test-gateway-groups-id";
    private static final String ADMIN_GROUPS_ID = "admin-groups-id";
    private static final String READ_ONLY_ADMINS_GROUP_ID = "read-only-admins-group-id";
    private static final String DEFAULT_GATEWAY_USERS_GROUP_ID = "default-gateway-users-group-id";

    private final GatewayService gatewayService;

    public GatewayGroupsRepositoryTest(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Create gateway first since GatewayGroups is now stored in GATEWAY table
        Gateway gateway = new Gateway();
        gateway.setGatewayId(GATEWAY_ID);
        gateway.setGatewayName("Test Gateway for Groups");
        gatewayService.createGateway(gateway);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up the gateway
        try {
            gatewayService.deleteGateway(GATEWAY_ID);
        } catch (Exception e) {
            // Ignore if already deleted
        }
    }

    @Test
    public void testCreateAndRetrieveGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayService.createGatewayGroups(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayService.getGatewayGroups(GATEWAY_ID);

        Assertions.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assertions.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assertions.assertEquals(DEFAULT_GATEWAY_USERS_GROUP_ID, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assertions.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayService.deleteGatewayGroups(GATEWAY_ID);

        // After delete, the groups should be cleared but gateway still exists
        GatewayGroups clearedGroups = gatewayService.getGatewayGroups(GATEWAY_ID);
        Assertions.assertNotNull(clearedGroups);
        Assertions.assertNull(clearedGroups.getAdminsGroupId());
    }

    @Test
    public void testUpdateGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayService.createGatewayGroups(gatewayGroups);

        final String defaultGatewayUsersGroupId = "some-other-group-id";
        gatewayGroups.setDefaultGatewayUsersGroupId(defaultGatewayUsersGroupId);

        gatewayService.updateGatewayGroups(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayService.getGatewayGroups(GATEWAY_ID);

        Assertions.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assertions.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assertions.assertEquals(defaultGatewayUsersGroupId, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assertions.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayService.deleteGatewayGroups(GATEWAY_ID);
    }
}
