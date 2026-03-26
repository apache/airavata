package org.apache.airavata.service.gateway;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    @Mock RegistryServerHandler registryHandler;
    @Mock SharingRegistryServerHandler sharingHandler;

    GatewayService gatewayService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        gatewayService = new GatewayService(registryHandler, sharingHandler);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void addGateway_returnsGatewayId() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gw-1");
        gateway.setGatewayName("Test Gateway");

        when(registryHandler.addGateway(gateway)).thenReturn("gw-1");

        // isSharingEnabled() will return false (ServerSettings not configured in test)
        String result = gatewayService.addGateway(ctx, gateway);

        assertEquals("gw-1", result);
        verify(registryHandler).addGateway(gateway);
    }

    @Test
    void getGateway_delegatesToRegistry() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gw-1");
        gateway.setGatewayName("Test Gateway");

        when(registryHandler.getGateway("gw-1")).thenReturn(gateway);

        Gateway result = gatewayService.getGateway(ctx, "gw-1");

        assertNotNull(result);
        assertEquals("gw-1", result.getGatewayId());
        verify(registryHandler).getGateway("gw-1");
    }

    @Test
    void getAllGateways_delegatesToRegistry() throws Exception {
        Gateway g1 = new Gateway();
        g1.setGatewayId("gw-1");
        Gateway g2 = new Gateway();
        g2.setGatewayId("gw-2");

        when(registryHandler.getAllGateways()).thenReturn(List.of(g1, g2));

        List<Gateway> result = gatewayService.getAllGateways(ctx);

        assertEquals(2, result.size());
        verify(registryHandler).getAllGateways();
    }

    @Test
    void updateGateway_delegatesToRegistry() throws Exception {
        Gateway updatedGateway = new Gateway();
        updatedGateway.setGatewayId("gw-1");

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
}
