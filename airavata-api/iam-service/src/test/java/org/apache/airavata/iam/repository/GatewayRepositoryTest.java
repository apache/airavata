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
package org.apache.airavata.iam.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayApprovalStatus;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;

public class GatewayRepositoryTest extends TestBase {

    private String testGatewayId = "testGateway";
    GatewayRepository gatewayRepository;

    public GatewayRepositoryTest() {
        super();
        gatewayRepository = new GatewayRepository();
    }

    @Test
    public void gatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        // After truncation, no gateways should exist
        List<Gateway> initialGatewayList = gatewayRepository.getAllGateways();
        assertEquals(0, initialGatewayList.size());

        Gateway gateway = Gateway.newBuilder().setGatewayId(testGatewayId).build();
        gateway = gateway.toBuilder().setDomain("SEAGRID").build();
        gateway = gateway.toBuilder().setEmailAddress("abc@d.com").build();
        gateway = gateway.toBuilder()
                .setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED)
                .build();
        gateway = gateway.toBuilder().setOauthClientId("pga").build();
        gateway = gateway.toBuilder()
                .setOauthClientSecret("9580cafa-7c1e-434f-bfe9-595f63907a43")
                .build();

        String gatewayId = gatewayRepository.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway = gateway.toBuilder().setGatewayAdminFirstName("ABC").build();
        gatewayRepository.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayRepository.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, gateway.getGatewayApprovalStatus());
        assertEquals(gateway.getOauthClientId(), retrievedGateway.getOauthClientId());
        assertEquals(gateway.getOauthClientSecret(), retrievedGateway.getOauthClientSecret());

        assertEquals(1, gatewayRepository.getAllGateways().size(), "should be 1 gateway (the one just added)");

        gatewayRepository.removeGateway(gatewayId);
        assertFalse(gatewayRepository.isGatewayExist(gatewayId));
    }
}
