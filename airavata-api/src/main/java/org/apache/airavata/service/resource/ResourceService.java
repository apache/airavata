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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final RegistryServerHandler registryHandler;

    public ResourceService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    // -------------------------------------------------------------------------
    // Compute Resources
    // -------------------------------------------------------------------------

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws ServiceException {
        try {
            return registryHandler.registerComputeResource(computeResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while saving compute resource: " + e.getMessage(), e);
        }
    }

    public ComputeResourceDescription getComputeResource(String computeResourceId) throws ServiceException {
        try {
            return registryHandler.getComputeResource(computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving compute resource: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllComputeResourceNames() throws ServiceException {
        try {
            return registryHandler.getAllComputeResourceNames();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving compute resource names: " + e.getMessage(), e);
        }
    }

    public boolean updateComputeResource(String computeResourceId,
            ComputeResourceDescription computeResourceDescription) throws ServiceException {
        try {
            return registryHandler.updateComputeResource(computeResourceId, computeResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while updating compute resource: " + e.getMessage(), e);
        }
    }

    public boolean deleteComputeResource(String computeResourceId) throws ServiceException {
        try {
            return registryHandler.deleteComputeResource(computeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting compute resource: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Storage Resources
    // -------------------------------------------------------------------------

    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws ServiceException {
        try {
            return registryHandler.registerStorageResource(storageResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while saving storage resource: " + e.getMessage(), e);
        }
    }

    public StorageResourceDescription getStorageResource(String storageResourceId) throws ServiceException {
        try {
            return registryHandler.getStorageResource(storageResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving storage resource: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getAllStorageResourceNames() throws ServiceException {
        try {
            return registryHandler.getAllStorageResourceNames();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving storage resource names: " + e.getMessage(), e);
        }
    }

    public boolean updateStorageResource(String storageResourceId,
            StorageResourceDescription storageResourceDescription) throws ServiceException {
        try {
            return registryHandler.updateStorageResource(storageResourceId, storageResourceDescription);
        } catch (Exception e) {
            throw new ServiceException("Error while updating storage resource: " + e.getMessage(), e);
        }
    }

    public boolean deleteStorageResource(String storageResourceId) throws ServiceException {
        try {
            return registryHandler.deleteStorageResource(storageResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting storage resource: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Job Submission
    // -------------------------------------------------------------------------

    public String addLocalSubmissionDetails(String computeResourceId, int priorityOrder,
            LOCALSubmission localSubmission) throws ServiceException {
        try {
            return registryHandler.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding local job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws ServiceException {
        try {
            return registryHandler.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating local job submission: " + e.getMessage(), e);
        }
    }

    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getLocalJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving local job submission: " + e.getMessage(), e);
        }
    }

    public String addSSHJobSubmissionDetails(String computeResourceId, int priorityOrder,
            SSHJobSubmission sshJobSubmission) throws ServiceException {
        try {
            return registryHandler.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SSH job submission: " + e.getMessage(), e);
        }
    }

    public String addSSHForkJobSubmissionDetails(String computeResourceId, int priorityOrder,
            SSHJobSubmission sshJobSubmission) throws ServiceException {
        try {
            return registryHandler.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SSH fork job submission: " + e.getMessage(), e);
        }
    }

    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getSSHJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving SSH job submission: " + e.getMessage(), e);
        }
    }

    public String addCloudJobSubmissionDetails(String computeResourceId, int priorityOrder,
            CloudJobSubmission cloudJobSubmission) throws ServiceException {
        try {
            return registryHandler.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding cloud job submission: " + e.getMessage(), e);
        }
    }

    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getCloudJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving cloud job submission: " + e.getMessage(), e);
        }
    }

    public String addUNICOREJobSubmissionDetails(String computeResourceId, int priorityOrder,
            UnicoreJobSubmission unicoreJobSubmission) throws ServiceException {
        try {
            return registryHandler.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder,
                    unicoreJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while adding UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws ServiceException {
        try {
            return registryHandler.getUnicoreJobSubmission(jobSubmissionId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws ServiceException {
        try {
            return registryHandler.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating SSH job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId,
            CloudJobSubmission cloudJobSubmission) throws ServiceException {
        try {
            return registryHandler.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, cloudJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating cloud job submission: " + e.getMessage(), e);
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(String jobSubmissionInterfaceId,
            UnicoreJobSubmission unicoreJobSubmission) throws ServiceException {
        try {
            return registryHandler.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
        } catch (Exception e) {
            throw new ServiceException("Error while updating UNICORE job submission: " + e.getMessage(), e);
        }
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws ServiceException {
        try {
            return registryHandler.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting job submission interface: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Data Movement
    // -------------------------------------------------------------------------

    public String addLocalDataMovementDetails(String resourceId, DMType dmType, int priorityOrder,
            LOCALDataMovement localDataMovement) throws ServiceException {
        try {
            return registryHandler.addLocalDataMovementDetails(resourceId, dmType, priorityOrder, localDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding local data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws ServiceException {
        try {
            return registryHandler.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating local data movement: " + e.getMessage(), e);
        }
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getLocalDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving local data movement: " + e.getMessage(), e);
        }
    }

    public String addSCPDataMovementDetails(String resourceId, DMType dmType, int priorityOrder,
            SCPDataMovement scpDataMovement) throws ServiceException {
        try {
            return registryHandler.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding SCP data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws ServiceException {
        try {
            return registryHandler.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating SCP data movement: " + e.getMessage(), e);
        }
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getSCPDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving SCP data movement: " + e.getMessage(), e);
        }
    }

    public String addUnicoreDataMovementDetails(String resourceId, DMType dmType, int priorityOrder,
            UnicoreDataMovement unicoreDataMovement) throws ServiceException {
        try {
            return registryHandler.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder,
                    unicoreDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateUnicoreDataMovementDetails(String dataMovementInterfaceId,
            UnicoreDataMovement unicoreDataMovement) throws ServiceException {
        try {
            return registryHandler.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getUnicoreDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving UNICORE data movement: " + e.getMessage(), e);
        }
    }

    public String addGridFTPDataMovementDetails(String computeResourceId, DMType dmType, int priorityOrder,
            GridFTPDataMovement gridFTPDataMovement) throws ServiceException {
        try {
            return registryHandler.addGridFTPDataMovementDetails(computeResourceId, dmType, priorityOrder,
                    gridFTPDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while adding GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public boolean updateGridFTPDataMovementDetails(String dataMovementInterfaceId,
            GridFTPDataMovement gridFTPDataMovement) throws ServiceException {
        try {
            return registryHandler.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
        } catch (Exception e) {
            throw new ServiceException("Error while updating GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws ServiceException {
        try {
            return registryHandler.getGridFTPDataMovement(dataMovementId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving GridFTP data movement: " + e.getMessage(), e);
        }
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws ServiceException {
        try {
            return registryHandler.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting data movement interface: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Resource Job Managers
    // -------------------------------------------------------------------------

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws ServiceException {
        try {
            return registryHandler.registerResourceJobManager(resourceJobManager);
        } catch (Exception e) {
            throw new ServiceException("Error while adding resource job manager: " + e.getMessage(), e);
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId,
            ResourceJobManager updatedResourceJobManager) throws ServiceException {
        try {
            return registryHandler.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (Exception e) {
            throw new ServiceException("Error while updating resource job manager: " + e.getMessage(), e);
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws ServiceException {
        try {
            return registryHandler.getResourceJobManager(resourceJobManagerId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving resource job manager: " + e.getMessage(), e);
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws ServiceException {
        try {
            return registryHandler.deleteResourceJobManager(resourceJobManagerId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting resource job manager: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Batch Queues
    // -------------------------------------------------------------------------

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws ServiceException {
        try {
            return registryHandler.deleteBatchQueue(computeResourceId, queueName);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting batch queue: " + e.getMessage(), e);
        }
    }
}
