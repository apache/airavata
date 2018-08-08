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

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GatewayRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRepositoryTest.class);

    GatewayRepository gatewayRepository;

    public GatewayRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
    }

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    @Test
    public void addGatewayTest() throws RegistryException {
        Gateway actualGateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(actualGateway);
        Assert.assertNotNull(gatewayId);
        assertEquals(1, gatewayRepository.getAllGateways().size());

        Gateway savedGateway = gatewayRepository.get(gatewayId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(savedGateway, actualGateway, "__isset_bitfield", "requestCreationTime"));
    }

    @Test
    public void updateGatewayTest() throws RegistryException {
        Gateway actualGateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(actualGateway);
        Assert.assertNotNull(gatewayId);

        actualGateway.setGatewayAdminFirstName("ABC");
        String testGatewayId = "testGateway";
        actualGateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);

        gatewayRepository.updateGateway(testGatewayId, actualGateway);

        Gateway savedGateway = gatewayRepository.get(gatewayId);
        assertEquals(actualGateway.getGatewayAdminFirstName(), savedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, savedGateway.getGatewayApprovalStatus());
        Assert.assertTrue(EqualsBuilder.reflectionEquals(actualGateway, savedGateway, "__isset_bitfield", "requestCreationTime"));
    }

    @Test
    public void retrieveSingleGatewayTest() throws RegistryException {
        List<Gateway> actualGatewayList = new ArrayList<>();
        List<String> gatewayIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);
            gatewayIdList.add(gatewayId);
            actualGatewayList.add(gateway);
        }

        for (int j = 0 ; j < 5; j++) {
            Gateway retrievedGateway = gatewayRepository.get(gatewayIdList.get(j));
            Gateway expectedGateway = actualGatewayList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(retrievedGateway, expectedGateway,"__isset_bitfield", "requestCreationTime"));
        }
    }

    @Test
    public void removeGatewayTest() throws RegistryException {
        Gateway actualGateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(actualGateway);
        Assert.assertNotNull(gatewayId);

        actualGateway.setGatewayAdminFirstName("ABC");
        String testGatewayId = "testGateway";
        gatewayRepository.updateGateway(testGatewayId, actualGateway);

        gatewayRepository.removeGateway(gatewayId);
        gatewayRepository.getAllGateways().size();
        Assert.assertEquals(0, gatewayRepository.getAllGateways().size());
    }

    @Test
    public void retrieveMultipleGatewayTest() throws RegistryException {
        List<String> gatewayIdList = new ArrayList<>();
        HashMap<String, Gateway> actualGatewayModelMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);
            gatewayIdList.add(gatewayId);
            actualGatewayModelMap.put(gatewayId, gateway);
        }

        for (int j = 0 ; j < 5; j++) {
            Gateway actualGateway = actualGatewayModelMap.get(gatewayIdList.get(j));
            Gateway expectedGateway = gatewayRepository.get(gatewayIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualGateway, expectedGateway,"__isset_bitfield", "requestCreationTime"));
        }
    }

}
