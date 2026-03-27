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
package org.apache.airavata.compute.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
class GatewayResourceProfileServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    GatewayResourceProfileService service;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        service = new GatewayResourceProfileService(registryHandler);
        ctx = new RequestContext(
                "admin", "testGateway", "token123", Map.of("userName", "admin", "gatewayId", "testGateway"));
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
        when(registryHandler.updateGatewayResourceProfile("testGateway", profile))
                .thenReturn(true);

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
        when(registryHandler.addGatewayComputeResourcePreference("testGateway", "compute-1", pref))
                .thenReturn(true);

        boolean result = service.addGatewayComputeResourcePreference(ctx, "testGateway", "compute-1", pref);

        assertTrue(result);
    }

    @Test
    void getAllGatewayComputeResourcePreferences_delegatesToRegistry() throws Exception {
        List<ComputeResourcePreference> prefs = List.of(new ComputeResourcePreference());
        when(registryHandler.getAllGatewayComputeResourcePreferences("testGateway"))
                .thenReturn(prefs);

        List<ComputeResourcePreference> result = service.getAllGatewayComputeResourcePreferences(ctx, "testGateway");

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
        when(registryHandler.addGatewayStoragePreference("testGateway", "storage-1", pref))
                .thenReturn(true);

        boolean result = service.addGatewayStoragePreference(ctx, "testGateway", "storage-1", pref);

        assertTrue(result);
    }

    @Test
    void registryException_wrappedAsServiceException() throws Exception {
        when(registryHandler.getGatewayResourceProfile("bad-gw")).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> service.getGatewayResourceProfile(ctx, "bad-gw"));
    }
}
