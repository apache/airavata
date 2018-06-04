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
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GatewayRepositoryTest {

    private static Initialize initialize;
    private String testGatewayId = "testGateway";
    GatewayRepository gatewayRepository;
    private static final Logger logger = LoggerFactory.getLogger(GatewayRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void GatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);

        String gatewayId = gatewayRepository.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway.setGatewayAdminFirstName("ABC");
        gatewayRepository.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayRepository.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, gateway.getGatewayApprovalStatus());

        assertTrue(gatewayRepository.getAllGateways().size() == 1);

        gatewayRepository.removeGateway(gatewayId);
        assertFalse(gatewayRepository.isGatewayExist(gatewayId));
    }

}
