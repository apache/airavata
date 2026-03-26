package org.apache.airavata.service.resourceprofile;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
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
class GatewayResourceProfileServiceTest {

    @Mock RegistryServerHandler registryHandler;

    GatewayResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new GatewayResourceProfileService(registryHandler);
        ctx = new RequestContext("admin", "testGateway", "token123",
                Map.of("userName", "admin", "gatewayId", "testGateway"));
    }

    @Test
    void registerGatewayResourceProfile_returnsId() throws Exception {
        GatewayResourceProfile profile = new GatewayResourceProfile();
        profile.setGatewayID("testGateway");
        when(registryHandler.registerGatewayResourceProfile(profile)).thenReturn("profile-id");

        String result = service.registerGatewayResourceProfile(ctx, profile);

        assertEquals("profile-id", result);
        verify(registryHandler).registerGatewayResourceProfile(profile);
    }

    @Test
    void getGatewayResourceProfile_delegatesToRegistry() throws Exception {
        GatewayResourceProfile profile = new GatewayResourceProfile();
        profile.setGatewayID("testGateway");
        when(registryHandler.getGatewayResourceProfile("testGateway")).thenReturn(profile);

        GatewayResourceProfile result = service.getGatewayResourceProfile(ctx, "testGateway");

        assertNotNull(result);
        assertEquals("testGateway", result.getGatewayID());
    }

    @Test
    void updateGatewayResourceProfile_delegatesToRegistry() throws Exception {
        GatewayResourceProfile profile = new GatewayResourceProfile();
        when(registryHandler.updateGatewayResourceProfile("testGateway", profile)).thenReturn(true);

        boolean result = service.updateGatewayResourceProfile(ctx, "testGateway", profile);

        assertTrue(result);
    }

    @Test
    void deleteGatewayResourceProfile_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteGatewayResourceProfile("testGateway")).thenReturn(true);

        boolean result = service.deleteGatewayResourceProfile(ctx, "testGateway");

        assertTrue(result);
    }

    @Test
    void addGatewayComputeResourcePreference_delegatesToRegistry() throws Exception {
        ComputeResourcePreference pref = new ComputeResourcePreference();
        when(registryHandler.addGatewayComputeResourcePreference("testGateway", "compute-1", pref)).thenReturn(true);

        boolean result = service.addGatewayComputeResourcePreference(ctx, "testGateway", "compute-1", pref);

        assertTrue(result);
    }

    @Test
    void getAllGatewayComputeResourcePreferences_delegatesToRegistry() throws Exception {
        List<ComputeResourcePreference> prefs = List.of(new ComputeResourcePreference());
        when(registryHandler.getAllGatewayComputeResourcePreferences("testGateway")).thenReturn(prefs);

        List<ComputeResourcePreference> result =
                service.getAllGatewayComputeResourcePreferences(ctx, "testGateway");

        assertEquals(1, result.size());
    }

    @Test
    void getAllGatewayResourceProfiles_delegatesToRegistry() throws Exception {
        List<GatewayResourceProfile> profiles = List.of(new GatewayResourceProfile());
        when(registryHandler.getAllGatewayResourceProfiles()).thenReturn(profiles);

        List<GatewayResourceProfile> result = service.getAllGatewayResourceProfiles(ctx);

        assertEquals(1, result.size());
    }

    @Test
    void addGatewayStoragePreference_delegatesToRegistry() throws Exception {
        StoragePreference pref = new StoragePreference();
        when(registryHandler.addGatewayStoragePreference("testGateway", "storage-1", pref)).thenReturn(true);

        boolean result = service.addGatewayStoragePreference(ctx, "testGateway", "storage-1", pref);

        assertTrue(result);
    }

    @Test
    void registryException_wrappedAsServiceException() throws Exception {
        when(registryHandler.getGatewayResourceProfile("bad-gw"))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> service.getGatewayResourceProfile(ctx, "bad-gw"));
    }
}
