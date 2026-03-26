package org.apache.airavata.service.resource;

import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock RegistryServerHandler registryHandler;

    ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(registryHandler);
    }

    // --- Compute Resource ---

    @Test
    void registerComputeResource_returnsId() throws Exception {
        ComputeResourceDescription desc = new ComputeResourceDescription();
        desc.setHostName("cluster.example.com");
        when(registryHandler.registerComputeResource(desc)).thenReturn("cr-001");

        String result = resourceService.registerComputeResource(desc);

        assertEquals("cr-001", result);
        verify(registryHandler).registerComputeResource(desc);
    }

    @Test
    void registerComputeResource_wrapsRegistryException() throws Exception {
        ComputeResourceDescription desc = new ComputeResourceDescription();
        when(registryHandler.registerComputeResource(desc)).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> resourceService.registerComputeResource(desc));
    }

    @Test
    void getComputeResource_returnsDescription() throws Exception {
        ComputeResourceDescription desc = new ComputeResourceDescription();
        desc.setComputeResourceId("cr-001");
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
        StorageResourceDescription desc = new StorageResourceDescription();
        desc.setStorageResourceId("sr-001");
        desc.setHostName("storage.example.com");
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
        SSHJobSubmission submission = new SSHJobSubmission();
        when(registryHandler.addSSHJobSubmissionDetails("cr-001", 1, submission)).thenReturn("js-001");

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
        LOCALSubmission submission = new LOCALSubmission();
        submission.setJobSubmissionInterfaceId("js-local-001");
        when(registryHandler.getLocalJobSubmission("js-local-001")).thenReturn(submission);

        LOCALSubmission result = resourceService.getLocalJobSubmission("js-local-001");

        assertNotNull(result);
        assertEquals("js-local-001", result.getJobSubmissionInterfaceId());
    }

    // --- Data Movement ---

    @Test
    void addSCPDataMovementDetails_returnsId() throws Exception {
        SCPDataMovement movement = new SCPDataMovement();
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
        SCPDataMovement movement = new SCPDataMovement();
        when(registryHandler.addSCPDataMovementDetails(any(), any(), anyInt(), any()))
                .thenThrow(new RuntimeException("registry failure"));

        assertThrows(ServiceException.class,
                () -> resourceService.addSCPDataMovementDetails("cr-001", DMType.COMPUTE_RESOURCE, 0, movement));
    }

    // --- Resource Job Manager ---

    @Test
    void registerResourceJobManager_returnsId() throws Exception {
        ResourceJobManager manager = new ResourceJobManager();
        manager.setResourceJobManagerType(ResourceJobManagerType.SLURM);
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
}
