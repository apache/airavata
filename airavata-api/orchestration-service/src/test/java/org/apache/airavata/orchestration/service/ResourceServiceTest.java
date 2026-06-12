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

import java.util.Map;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.GroupResourceProfileProvider;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    @Mock
    GroupResourceProfileProvider groupResourceProfileService;

    ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(registryHandler, groupResourceProfileService);
    }

    // --- Compute Resource ---

    @Test
    void registerComputeResource_returnsId() throws Exception {
        ComputeResourceDescription desc = ComputeResourceDescription.newBuilder()
                .setHostName("cluster.example.com")
                .build();
        when(registryHandler.registerComputeResource(desc)).thenReturn("cr-001");

        String result = resourceService.registerComputeResource(desc);

        assertEquals("cr-001", result);
        verify(registryHandler).registerComputeResource(desc);
    }

    @Test
    void registerComputeResource_wrapsRegistryException() throws Exception {
        ComputeResourceDescription desc = ComputeResourceDescription.getDefaultInstance();
        when(registryHandler.registerComputeResource(desc)).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> resourceService.registerComputeResource(desc));
    }

    @Test
    void getComputeResource_returnsDescription() throws Exception {
        ComputeResourceDescription desc = ComputeResourceDescription.newBuilder()
                .setComputeResourceId("cr-001")
                .build();
        when(registryHandler.getComputeResource("cr-001")).thenReturn(desc);

        ComputeResourceDescription result = resourceService.getComputeResource("cr-001");

        assertNotNull(result);
        assertEquals("cr-001", result.getComputeResourceId());
    }

    @Test
    void deleteComputeResource_returnsTrue() throws Exception {
        when(registryHandler.deleteComputeResource("cr-001")).thenReturn(true);

        boolean result = resourceService.deleteComputeResource("cr-001");

        assertTrue(result);
    }

    // --- Storage Resource ---

    @Test
    void getStorageResource_returnsDescription() throws Exception {
        StorageResourceDescription desc = StorageResourceDescription.newBuilder()
                .setStorageResourceId("sr-001")
                .build();
        desc = desc.toBuilder().setHostName("storage.example.com").build();
        when(registryHandler.getStorageResource("sr-001")).thenReturn(desc);

        StorageResourceDescription result = resourceService.getStorageResource("sr-001");

        assertNotNull(result);
        assertEquals("sr-001", result.getStorageResourceId());
    }

    @Test
    void getAllStorageResourceNames_returnsMap() throws Exception {
        Map<String, String> names = Map.of("sr-001", "storage.example.com");
        when(registryHandler.getAllStorageResourceNames()).thenReturn(names);

        Map<String, String> result = resourceService.getAllStorageResourceNames();

        assertEquals(1, result.size());
        assertEquals("storage.example.com", result.get("sr-001"));
    }

    @Test
    void deleteStorageResource_wrapsException() throws Exception {
        when(registryHandler.deleteStorageResource("sr-bad")).thenThrow(new RuntimeException("not found"));

        assertThrows(ServiceException.class, () -> resourceService.deleteStorageResource("sr-bad"));
    }

    // --- Resource Job Manager ---

    @Test
    void registerResourceJobManager_returnsId() throws Exception {
        ResourceJobManager manager = ResourceJobManager.newBuilder()
                .setResourceJobManagerType(ResourceJobManagerType.SLURM)
                .build();
        when(registryHandler.registerResourceJobManager(manager)).thenReturn("rjm-001");

        String result = resourceService.registerResourceJobManager(manager);

        assertEquals("rjm-001", result);
    }

    @Test
    void deleteResourceJobManager_returnsTrue() throws Exception {
        when(registryHandler.deleteResourceJobManager("rjm-001")).thenReturn(true);

        boolean result = resourceService.deleteResourceJobManager("rjm-001");

        assertTrue(result);
    }

    // --- Batch Queue ---

    @Test
    void deleteBatchQueue_returnsTrue() throws Exception {
        when(registryHandler.deleteBatchQueue("cr-001", "normal")).thenReturn(true);

        boolean result = resourceService.deleteBatchQueue("cr-001", "normal");

        assertTrue(result);
        verify(registryHandler).deleteBatchQueue("cr-001", "normal");
    }

    // --- Compute Resource (additional) ---

    @Test
    void getAllComputeResourceNames_returnsMap() throws Exception {
        Map<String, String> names = Map.of("cr-001", "cluster.example.com");
        when(registryHandler.getAllComputeResourceNames()).thenReturn(names);

        Map<String, String> result = resourceService.getAllComputeResourceNames();

        assertEquals(1, result.size());
        assertEquals("cluster.example.com", result.get("cr-001"));
    }

    @Test
    void getAllComputeResourceNames_wrapsException() throws Exception {
        when(registryHandler.getAllComputeResourceNames()).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> resourceService.getAllComputeResourceNames());
    }

    @Test
    void updateComputeResource_returnsTrue() throws Exception {
        ComputeResourceDescription desc = ComputeResourceDescription.getDefaultInstance();
        when(registryHandler.updateComputeResource("cr-001", desc)).thenReturn(true);

        boolean result = resourceService.updateComputeResource("cr-001", desc);

        assertTrue(result);
    }

    @Test
    void updateComputeResource_wrapsException() throws Exception {
        ComputeResourceDescription desc = ComputeResourceDescription.getDefaultInstance();
        when(registryHandler.updateComputeResource("cr-001", desc)).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.updateComputeResource("cr-001", desc));
    }

    @Test
    void getComputeResource_wrapsException() throws Exception {
        when(registryHandler.getComputeResource("cr-bad")).thenThrow(new RuntimeException("not found"));

        assertThrows(ServiceException.class, () -> resourceService.getComputeResource("cr-bad"));
    }

    @Test
    void deleteComputeResource_wrapsException() throws Exception {
        when(registryHandler.deleteComputeResource("cr-bad")).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.deleteComputeResource("cr-bad"));
    }

    // --- Storage Resource (additional) ---

    @Test
    void registerStorageResource_returnsId() throws Exception {
        StorageResourceDescription desc = StorageResourceDescription.newBuilder()
                .setHostName("storage.example.com")
                .build();
        when(registryHandler.registerStorageResource(desc)).thenReturn("sr-001");

        String result = resourceService.registerStorageResource(desc);

        assertEquals("sr-001", result);
    }

    @Test
    void registerStorageResource_wrapsException() throws Exception {
        StorageResourceDescription desc = StorageResourceDescription.getDefaultInstance();
        when(registryHandler.registerStorageResource(desc)).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.registerStorageResource(desc));
    }

    @Test
    void updateStorageResource_returnsTrue() throws Exception {
        StorageResourceDescription desc = StorageResourceDescription.getDefaultInstance();
        when(registryHandler.updateStorageResource("sr-001", desc)).thenReturn(true);

        boolean result = resourceService.updateStorageResource("sr-001", desc);

        assertTrue(result);
    }

    @Test
    void updateStorageResource_wrapsException() throws Exception {
        StorageResourceDescription desc = StorageResourceDescription.getDefaultInstance();
        when(registryHandler.updateStorageResource("sr-001", desc)).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.updateStorageResource("sr-001", desc));
    }

    @Test
    void deleteStorageResource_returnsTrue() throws Exception {
        when(registryHandler.deleteStorageResource("sr-001")).thenReturn(true);

        boolean result = resourceService.deleteStorageResource("sr-001");

        assertTrue(result);
    }

    // --- Resource Job Manager (additional) ---

    @Test
    void updateResourceJobManager_returnsTrue() throws Exception {
        ResourceJobManager manager = ResourceJobManager.getDefaultInstance();
        when(registryHandler.updateResourceJobManager("rjm-001", manager)).thenReturn(true);

        boolean result = resourceService.updateResourceJobManager("rjm-001", manager);

        assertTrue(result);
    }

    @Test
    void getResourceJobManager_returnsManager() throws Exception {
        ResourceJobManager manager = ResourceJobManager.newBuilder()
                .setResourceJobManagerId("rjm-001")
                .setResourceJobManagerType(ResourceJobManagerType.SLURM)
                .build();
        when(registryHandler.getResourceJobManager("rjm-001")).thenReturn(manager);

        ResourceJobManager result = resourceService.getResourceJobManager("rjm-001");

        assertNotNull(result);
        assertEquals("rjm-001", result.getResourceJobManagerId());
        assertEquals(ResourceJobManagerType.SLURM, result.getResourceJobManagerType());
    }

    @Test
    void registerResourceJobManager_wrapsException() throws Exception {
        ResourceJobManager manager = ResourceJobManager.getDefaultInstance();
        when(registryHandler.registerResourceJobManager(manager)).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.registerResourceJobManager(manager));
    }

    @Test
    void deleteResourceJobManager_wrapsException() throws Exception {
        when(registryHandler.deleteResourceJobManager("rjm-bad")).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.deleteResourceJobManager("rjm-bad"));
    }

    // --- Batch Queue (additional) ---

    @Test
    void deleteBatchQueue_wrapsException() throws Exception {
        when(registryHandler.deleteBatchQueue("cr-001", "bad-queue")).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.deleteBatchQueue("cr-001", "bad-queue"));
    }

    // --- ComputeResourceProvider SPI delegates ---

    @Test
    void getGatewayResourceProfile_returnsProfile() throws Exception {
        GatewayResourceProfile profile =
                GatewayResourceProfile.newBuilder().setGatewayId("gw-001").build();
        when(registryHandler.getGatewayResourceProfile("gw-001")).thenReturn(profile);

        GatewayResourceProfile result = resourceService.getGatewayResourceProfile("gw-001");

        assertNotNull(result);
        assertEquals("gw-001", result.getGatewayId());
    }

    @Test
    void getGatewayResourceProfile_wrapsException() throws Exception {
        when(registryHandler.getGatewayResourceProfile("gw-bad")).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.getGatewayResourceProfile("gw-bad"));
    }

    @Test
    void getGroupComputeResourcePreference_returnsPreference() throws Exception {
        GroupComputeResourcePreference pref = GroupComputeResourcePreference.newBuilder()
                .setComputeResourceId("cr-001")
                .build();
        when(registryHandler.getGroupComputeResourcePreference("cr-001", "grp-001"))
                .thenReturn(pref);

        GroupComputeResourcePreference result = resourceService.getGroupComputeResourcePreference("cr-001", "grp-001");

        assertNotNull(result);
        assertEquals("cr-001", result.getComputeResourceId());
    }

    @Test
    void getGroupComputeResourcePreference_wrapsException() throws Exception {
        when(registryHandler.getGroupComputeResourcePreference("cr-bad", "grp-bad"))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(
                ServiceException.class, () -> resourceService.getGroupComputeResourcePreference("cr-bad", "grp-bad"));
    }
}
