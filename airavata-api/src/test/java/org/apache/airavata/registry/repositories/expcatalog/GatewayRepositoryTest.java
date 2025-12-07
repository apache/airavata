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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.GatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class GatewayRepositoryTest extends TestBase {

    private String testGatewayId = "testGateway";

    @Autowired
    GatewayService gatewayService;

    public GatewayRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void gatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        // Verify that default Gateway is already created
        List<Gateway> defaultGatewayList = gatewayService.getAllGateways();
        assertEquals(1, defaultGatewayList.size());
        assertEquals(
                ServerSettings.getDefaultUserGateway(),
                defaultGatewayList.get(0).getGatewayId());

        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        gateway.setOauthClientId("pga");
        gateway.setOauthClientSecret("9580cafa-7c1e-434f-bfe9-595f63907a43");

        String gatewayId = gatewayService.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway.setGatewayAdminFirstName("ABC");
        gatewayService.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayService.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, gateway.getGatewayApprovalStatus());
        assertEquals(gateway.getOauthClientId(), retrievedGateway.getOauthClientId());
        assertEquals(gateway.getOauthClientSecret(), retrievedGateway.getOauthClientSecret());

        assertEquals(2, gatewayService.getAllGateways().size(), "should be 2 gateways (1 default plus 1 just added)");

        gatewayService.removeGateway(gatewayId);
        assertFalse(gatewayService.isGatewayExist(gatewayId));
    }
}
