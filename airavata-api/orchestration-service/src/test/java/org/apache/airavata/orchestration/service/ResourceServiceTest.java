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
import org.apache.airavata.compute.service.GroupResourceProfileService;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.movement.proto.DMType;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
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
    GroupResourceProfileService groupResourceProfileService;

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

    // --- Job Submission ---

    @Test
    void addSSHJobSubmissionDetails_returnsId() throws Exception {
        SSHJobSubmission submission = SSHJobSubmission.getDefaultInstance();
        when(registryHandler.addSSHJobSubmissionDetails("cr-001", 1, submission))
                .thenReturn("js-001");

        String result = resourceService.addSSHJobSubmissionDetails("cr-001", 1, submission);

        assertEquals("js-001", result);
    }

    @Test
    void deleteJobSubmissionInterface_returnsTrue() throws Exception {
        when(registryHandler.deleteJobSubmissionInterface("cr-001", "js-001")).thenReturn(true);

        boolean result = resourceService.deleteJobSubmissionInterface("cr-001", "js-001");

        assertTrue(result);
        verify(registryHandler).deleteJobSubmissionInterface("cr-001", "js-001");
    }

    @Test
    void getLocalJobSubmission_returnsSubmission() throws Exception {
        LOCALSubmission submission = LOCALSubmission.newBuilder()
                .setJobSubmissionInterfaceId("js-local-001")
                .build();
        when(registryHandler.getLocalJobSubmission("js-local-001")).thenReturn(submission);

        LOCALSubmission result = resourceService.getLocalJobSubmission("js-local-001");

        assertNotNull(result);
        assertEquals("js-local-001", result.getJobSubmissionInterfaceId());
    }

    // --- Data Movement ---

    @Test
    void addSCPDataMovementDetails_returnsId() throws Exception {
        SCPDataMovement movement = SCPDataMovement.getDefaultInstance();
        when(registryHandler.addSCPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement))
                .thenReturn("dm-001");

        String result = resourceService.addSCPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement);

        assertEquals("dm-001", result);
    }

    @Test
    void deleteDataMovementInterface_returnsTrue() throws Exception {
        when(registryHandler.deleteDataMovementInterface("cr-001", "dm-001", DMType.COMPUTE_RESOURCE))
                .thenReturn(true);

        boolean result = resourceService.deleteDataMovementInterface("cr-001", "dm-001", DMType.COMPUTE_RESOURCE);

        assertTrue(result);
    }

    @Test
    void addSCPDataMovementDetails_wrapsException() throws Exception {
        SCPDataMovement movement = SCPDataMovement.getDefaultInstance();
        when(registryHandler.addSCPDataMovementDetails(any(), any(), anyInt(), any()))
                .thenThrow(new RuntimeException("registry failure"));

        assertThrows(
                ServiceException.class,
                () -> resourceService.addSCPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement));
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

    // --- Job Submission (additional) ---

    @Test
    void addLocalSubmissionDetails_returnsId() throws Exception {
        LOCALSubmission submission = LOCALSubmission.getDefaultInstance();
        when(registryHandler.addLocalSubmissionDetails("cr-001", 1, submission)).thenReturn("js-local-001");

        String result = resourceService.addLocalSubmissionDetails("cr-001", 1, submission);

        assertEquals("js-local-001", result);
    }

    @Test
    void addLocalSubmissionDetails_wrapsException() throws Exception {
        LOCALSubmission submission = LOCALSubmission.getDefaultInstance();
        when(registryHandler.addLocalSubmissionDetails("cr-001", 1, submission))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.addLocalSubmissionDetails("cr-001", 1, submission));
    }

    @Test
    void updateLocalSubmissionDetails_returnsTrue() throws Exception {
        LOCALSubmission submission = LOCALSubmission.getDefaultInstance();
        when(registryHandler.updateLocalSubmissionDetails("js-local-001", submission))
                .thenReturn(true);

        boolean result = resourceService.updateLocalSubmissionDetails("js-local-001", submission);

        assertTrue(result);
    }

    @Test
    void addSSHForkJobSubmissionDetails_returnsId() throws Exception {
        SSHJobSubmission submission = SSHJobSubmission.getDefaultInstance();
        when(registryHandler.addSSHForkJobSubmissionDetails("cr-001", 1, submission))
                .thenReturn("js-fork-001");

        String result = resourceService.addSSHForkJobSubmissionDetails("cr-001", 1, submission);

        assertEquals("js-fork-001", result);
    }

    @Test
    void addSSHForkJobSubmissionDetails_wrapsException() throws Exception {
        SSHJobSubmission submission = SSHJobSubmission.getDefaultInstance();
        when(registryHandler.addSSHForkJobSubmissionDetails("cr-001", 1, submission))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(
                ServiceException.class, () -> resourceService.addSSHForkJobSubmissionDetails("cr-001", 1, submission));
    }

    @Test
    void getSSHJobSubmission_returnsSubmission() throws Exception {
        SSHJobSubmission submission = SSHJobSubmission.newBuilder()
                .setJobSubmissionInterfaceId("js-ssh-001")
                .build();
        when(registryHandler.getSSHJobSubmission("js-ssh-001")).thenReturn(submission);

        SSHJobSubmission result = resourceService.getSSHJobSubmission("js-ssh-001");

        assertNotNull(result);
        assertEquals("js-ssh-001", result.getJobSubmissionInterfaceId());
    }

    @Test
    void addCloudJobSubmissionDetails_returnsId() throws Exception {
        CloudJobSubmission submission = CloudJobSubmission.getDefaultInstance();
        when(registryHandler.addCloudJobSubmissionDetails("cr-001", 1, submission))
                .thenReturn("js-cloud-001");

        String result = resourceService.addCloudJobSubmissionDetails("cr-001", 1, submission);

        assertEquals("js-cloud-001", result);
    }

    @Test
    void getCloudJobSubmission_returnsSubmission() throws Exception {
        CloudJobSubmission submission = CloudJobSubmission.newBuilder()
                .setJobSubmissionInterfaceId("js-cloud-001")
                .build();
        when(registryHandler.getCloudJobSubmission("js-cloud-001")).thenReturn(submission);

        CloudJobSubmission result = resourceService.getCloudJobSubmission("js-cloud-001");

        assertNotNull(result);
        assertEquals("js-cloud-001", result.getJobSubmissionInterfaceId());
    }

    @Test
    void addUNICOREJobSubmissionDetails_returnsId() throws Exception {
        UnicoreJobSubmission submission = UnicoreJobSubmission.getDefaultInstance();
        when(registryHandler.addUNICOREJobSubmissionDetails("cr-001", 1, submission))
                .thenReturn("js-unicore-001");

        String result = resourceService.addUNICOREJobSubmissionDetails("cr-001", 1, submission);

        assertEquals("js-unicore-001", result);
    }

    @Test
    void getUnicoreJobSubmission_returnsSubmission() throws Exception {
        UnicoreJobSubmission submission = UnicoreJobSubmission.newBuilder()
                .setJobSubmissionInterfaceId("js-unicore-001")
                .build();
        when(registryHandler.getUnicoreJobSubmission("js-unicore-001")).thenReturn(submission);

        UnicoreJobSubmission result = resourceService.getUnicoreJobSubmission("js-unicore-001");

        assertNotNull(result);
        assertEquals("js-unicore-001", result.getJobSubmissionInterfaceId());
    }

    @Test
    void updateSSHJobSubmissionDetails_returnsTrue() throws Exception {
        SSHJobSubmission submission = SSHJobSubmission.getDefaultInstance();
        when(registryHandler.updateSSHJobSubmissionDetails("js-001", submission))
                .thenReturn(true);

        boolean result = resourceService.updateSSHJobSubmissionDetails("js-001", submission);

        assertTrue(result);
    }

    @Test
    void updateCloudJobSubmissionDetails_returnsTrue() throws Exception {
        CloudJobSubmission submission = CloudJobSubmission.getDefaultInstance();
        when(registryHandler.updateCloudJobSubmissionDetails("js-001", submission))
                .thenReturn(true);

        boolean result = resourceService.updateCloudJobSubmissionDetails("js-001", submission);

        assertTrue(result);
    }

    @Test
    void updateUnicoreJobSubmissionDetails_returnsTrue() throws Exception {
        UnicoreJobSubmission submission = UnicoreJobSubmission.getDefaultInstance();
        when(registryHandler.updateUnicoreJobSubmissionDetails("js-001", submission))
                .thenReturn(true);

        boolean result = resourceService.updateUnicoreJobSubmissionDetails("js-001", submission);

        assertTrue(result);
    }

    @Test
    void deleteJobSubmissionInterface_wrapsException() throws Exception {
        when(registryHandler.deleteJobSubmissionInterface("cr-001", "js-bad")).thenThrow(new RuntimeException("fail"));

        assertThrows(ServiceException.class, () -> resourceService.deleteJobSubmissionInterface("cr-001", "js-bad"));
    }

    // --- Data Movement (additional) ---

    @Test
    void addLocalDataMovementDetails_returnsId() throws Exception {
        LOCALDataMovement movement = LOCALDataMovement.getDefaultInstance();
        when(registryHandler.addLocalDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement))
                .thenReturn("dm-local-001");

        String result = resourceService.addLocalDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement);

        assertEquals("dm-local-001", result);
    }

    @Test
    void updateLocalDataMovementDetails_returnsTrue() throws Exception {
        LOCALDataMovement movement = LOCALDataMovement.getDefaultInstance();
        when(registryHandler.updateLocalDataMovementDetails("dm-001", movement)).thenReturn(true);

        boolean result = resourceService.updateLocalDataMovementDetails("dm-001", movement);

        assertTrue(result);
    }

    @Test
    void getLocalDataMovement_returnsMovement() throws Exception {
        LOCALDataMovement movement = LOCALDataMovement.newBuilder()
                .setDataMovementInterfaceId("dm-local-001")
                .build();
        when(registryHandler.getLocalDataMovement("dm-local-001")).thenReturn(movement);

        LOCALDataMovement result = resourceService.getLocalDataMovement("dm-local-001");

        assertNotNull(result);
        assertEquals("dm-local-001", result.getDataMovementInterfaceId());
    }

    @Test
    void updateSCPDataMovementDetails_returnsTrue() throws Exception {
        SCPDataMovement movement = SCPDataMovement.getDefaultInstance();
        when(registryHandler.updateSCPDataMovementDetails("dm-001", movement)).thenReturn(true);

        boolean result = resourceService.updateSCPDataMovementDetails("dm-001", movement);

        assertTrue(result);
    }

    @Test
    void getSCPDataMovement_returnsMovement() throws Exception {
        SCPDataMovement movement = SCPDataMovement.newBuilder()
                .setDataMovementInterfaceId("dm-scp-001")
                .build();
        when(registryHandler.getSCPDataMovement("dm-scp-001")).thenReturn(movement);

        SCPDataMovement result = resourceService.getSCPDataMovement("dm-scp-001");

        assertNotNull(result);
        assertEquals("dm-scp-001", result.getDataMovementInterfaceId());
    }

    @Test
    void addUnicoreDataMovementDetails_returnsId() throws Exception {
        UnicoreDataMovement movement = UnicoreDataMovement.getDefaultInstance();
        when(registryHandler.addUnicoreDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement))
                .thenReturn("dm-unicore-001");

        String result = resourceService.addUnicoreDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement);

        assertEquals("dm-unicore-001", result);
    }

    @Test
    void updateUnicoreDataMovementDetails_returnsTrue() throws Exception {
        UnicoreDataMovement movement = UnicoreDataMovement.getDefaultInstance();
        when(registryHandler.updateUnicoreDataMovementDetails("dm-001", movement))
                .thenReturn(true);

        boolean result = resourceService.updateUnicoreDataMovementDetails("dm-001", movement);

        assertTrue(result);
    }

    @Test
    void getUnicoreDataMovement_returnsMovement() throws Exception {
        UnicoreDataMovement movement = UnicoreDataMovement.newBuilder()
                .setDataMovementInterfaceId("dm-unicore-001")
                .build();
        when(registryHandler.getUnicoreDataMovement("dm-unicore-001")).thenReturn(movement);

        UnicoreDataMovement result = resourceService.getUnicoreDataMovement("dm-unicore-001");

        assertNotNull(result);
        assertEquals("dm-unicore-001", result.getDataMovementInterfaceId());
    }

    @Test
    void addGridFTPDataMovementDetails_returnsId() throws Exception {
        GridFTPDataMovement movement = GridFTPDataMovement.getDefaultInstance();
        when(registryHandler.addGridFTPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement))
                .thenReturn("dm-gridftp-001");

        String result = resourceService.addGridFTPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement);

        assertEquals("dm-gridftp-001", result);
    }

    @Test
    void updateGridFTPDataMovementDetails_returnsTrue() throws Exception {
        GridFTPDataMovement movement = GridFTPDataMovement.getDefaultInstance();
        when(registryHandler.updateGridFTPDataMovementDetails("dm-001", movement))
                .thenReturn(true);

        boolean result = resourceService.updateGridFTPDataMovementDetails("dm-001", movement);

        assertTrue(result);
    }

    @Test
    void getGridFTPDataMovement_returnsMovement() throws Exception {
        GridFTPDataMovement movement = GridFTPDataMovement.newBuilder()
                .setDataMovementInterfaceId("dm-gridftp-001")
                .build();
        when(registryHandler.getGridFTPDataMovement("dm-gridftp-001")).thenReturn(movement);

        GridFTPDataMovement result = resourceService.getGridFTPDataMovement("dm-gridftp-001");

        assertNotNull(result);
        assertEquals("dm-gridftp-001", result.getDataMovementInterfaceId());
    }

    @Test
    void deleteDataMovementInterface_wrapsException() throws Exception {
        when(registryHandler.deleteDataMovementInterface("cr-001", "dm-bad", DMType.COMPUTE_RESOURCE))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(
                ServiceException.class,
                () -> resourceService.deleteDataMovementInterface("cr-001", "dm-bad", DMType.COMPUTE_RESOURCE));
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
