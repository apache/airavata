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
package org.apache.airavata.orchestration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    @Mock
    RegistryHandler registryHandler;

    @Mock
    SharingFacade sharingHandler;

    GatewayService gatewayService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        gatewayService = new GatewayService(registryHandler, sharingHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void addGateway_returnsGatewayId() throws Exception {
        Gateway gateway = Gateway.newBuilder().setGatewayId("gw-1").build();
        gateway = gateway.toBuilder().setGatewayName("Test Gateway").build();

        when(registryHandler.addGateway(gateway)).thenReturn("gw-1");

        // isSharingEnabled() will return false (ServerSettings not configured in test)
        String result = gatewayService.addGateway(ctx, gateway);

        assertEquals("gw-1", result);
        verify(registryHandler).addGateway(gateway);
    }

    @Test
    void getGateway_delegatesToRegistry() throws Exception {
        Gateway gateway = Gateway.newBuilder().setGatewayId("gw-1").build();
        gateway = gateway.toBuilder().setGatewayName("Test Gateway").build();

        when(registryHandler.getGateway("gw-1")).thenReturn(gateway);

        Gateway result = gatewayService.getGateway(ctx, "gw-1");

        assertNotNull(result);
        assertEquals("gw-1", result.getGatewayId());
        verify(registryHandler).getGateway("gw-1");
    }

    @Test
    void getAllGateways_delegatesToRegistry() throws Exception {
        Gateway g1 = Gateway.newBuilder().setGatewayId("gw-1").build();
        Gateway g2 = Gateway.newBuilder().setGatewayId("gw-2").build();

        when(registryHandler.getAllGateways()).thenReturn(List.of(g1, g2));

        List<Gateway> result = gatewayService.getAllGateways(ctx);

        assertEquals(2, result.size());
        verify(registryHandler).getAllGateways();
    }

    @Test
    void updateGateway_delegatesToRegistry() throws Exception {
        Gateway updatedGateway = Gateway.newBuilder().setGatewayId("gw-1").build();

        when(registryHandler.updateGateway("gw-1", updatedGateway)).thenReturn(true);

        boolean result = gatewayService.updateGateway(ctx, "gw-1", updatedGateway);

        assertTrue(result);
        verify(registryHandler).updateGateway("gw-1", updatedGateway);
    }

    @Test
    void deleteGateway_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteGateway("gw-1")).thenReturn(true);

        boolean result = gatewayService.deleteGateway(ctx, "gw-1");

        assertTrue(result);
        verify(registryHandler).deleteGateway("gw-1");
    }

    @Test
    void isGatewayExist_delegatesToRegistry() throws Exception {
        when(registryHandler.isGatewayExist("gw-1")).thenReturn(true);

        boolean result = gatewayService.isGatewayExist(ctx, "gw-1");

        assertTrue(result);
        verify(registryHandler).isGatewayExist("gw-1");
    }

    @Test
    void getAllUsersInGateway_delegatesToRegistry() throws Exception {
        when(registryHandler.getAllUsersInGateway("gw-1")).thenReturn(List.of("user1", "user2"));

        List<String> result = gatewayService.getAllUsersInGateway(ctx, "gw-1");

        assertEquals(2, result.size());
        verify(registryHandler).getAllUsersInGateway("gw-1");
    }

    @Test
    void isUserExists_returnsTrueWhenExists() throws Exception {
        when(registryHandler.isUserExists("gw-1", "alice")).thenReturn(true);

        boolean result = gatewayService.isUserExists(ctx, "gw-1", "alice");

        assertTrue(result);
        verify(registryHandler).isUserExists("gw-1", "alice");
    }

    @Test
    void isUserExists_returnsFalseWhenNotExists() throws Exception {
        when(registryHandler.isUserExists("gw-1", "nobody")).thenReturn(false);

        boolean result = gatewayService.isUserExists(ctx, "gw-1", "nobody");

        assertFalse(result);
    }
}
