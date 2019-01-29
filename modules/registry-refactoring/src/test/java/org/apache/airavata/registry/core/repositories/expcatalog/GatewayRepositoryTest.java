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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.List;

public class GatewayRepositoryTest extends TestBase {

    private String testGatewayId = "testGateway";
    GatewayRepository gatewayRepository;

    public GatewayRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
    }

    @Test
    public void gatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        // Verify that default Gateway is already created
        List<Gateway> defaultGatewayList = gatewayRepository.getAllGateways();
        assertEquals(1, defaultGatewayList.size());
        assertEquals(ServerSettings.getDefaultUserGateway(), defaultGatewayList.get(0).getGatewayId());

        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        gateway.setOauthClientId("pga");
        gateway.setOauthClientSecret("9580cafa-7c1e-434f-bfe9-595f63907a43");

        String gatewayId = gatewayRepository.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway.setGatewayAdminFirstName("ABC");
        gatewayRepository.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayRepository.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, gateway.getGatewayApprovalStatus());
        assertEquals(gateway.getOauthClientId(), retrievedGateway.getOauthClientId());
        assertEquals(gateway.getOauthClientSecret(), retrievedGateway.getOauthClientSecret());

        assertEquals("should be 2 gateways (1 default plus 1 just added)", 2, gatewayRepository.getAllGateways().size());

        gatewayRepository.removeGateway(gatewayId);
        assertFalse(gatewayRepository.isGatewayExist(gatewayId));
    }

}
