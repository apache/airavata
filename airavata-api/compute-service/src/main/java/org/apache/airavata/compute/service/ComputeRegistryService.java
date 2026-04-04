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

import java.util.Map;
import org.apache.airavata.compute.repository.ComputeResourceRepository;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ComputeRegistryService implements ComputeRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ComputeRegistryService.class);

    private final ComputeResourceRepository computeResourceRepository = new ComputeResourceRepository();

    // =========================================================================
    // ComputeRegistry interface methods
    // =========================================================================

    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception {
        try {
            ComputeResourceDescription computeResource =
                    new ComputeResourceRepository().getComputeResource(computeResourceId);
            logger.debug("Airavata retrieved compute resource with compute resource Id : " + computeResourceId);
            return computeResource;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while retrieving compute resource...", e);
            throw new RegistryException("Error while retrieving compute resource. More info : " + e.getMessage());
        }
    }

    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws Exception {
        try {
            LOCALSubmission localJobSubmission = new ComputeResourceRepository().getLocalJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved local job submission for job submission interface id: " + jobSubmissionId);
            return localJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw new RegistryException(errorMsg + e.getMessage());
        }
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception {
        try {
            SSHJobSubmission sshJobSubmission = new ComputeResourceRepository().getSSHJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved SSH job submission for job submission interface id: " + jobSubmissionId);
            return sshJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw new RegistryException(errorMsg + e.getMessage());
        }
    }

    @Override
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws Exception {
        try {
            UnicoreJobSubmission unicoreJobSubmission =
                    new ComputeResourceRepository().getUNICOREJobSubmission(jobSubmissionId);
            logger.debug(
                    "Airavata retrieved UNICORE job submission for job submission interface id: " + jobSubmissionId);
            return unicoreJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw new RegistryException(errorMsg + e.getMessage());
        }
    }

    @Override
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws Exception {
        try {
            CloudJobSubmission cloudJobSubmission =
                    new ComputeResourceRepository().getCloudJobSubmission(jobSubmissionId);
            logger.debug("Airavata retrieved cloud job submission for job submission interface id: " + jobSubmissionId);
            return cloudJobSubmission;
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            throw new RegistryException(errorMsg + e.getMessage());
        }
    }

    // =========================================================================
    // Additional compute resource methods (not yet on the interface)
    // =========================================================================

    public Map<String, String> getAllComputeResourceNames() throws Exception {
        try {
            return new ComputeResourceRepository().getAllComputeResourceIdList();
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving compute resource. More info : " + e.getMessage());
        }
    }

    public boolean deleteComputeResource(String computeResourceId) throws Exception {
        try {
            new ComputeResourceRepository().removeComputeResource(computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting compute resource. More info : " + e.getMessage());
        }
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription) throws Exception {
        try {
            new ComputeResourceRepository().updateComputeResource(computeResourceId, computeResourceDescription);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updaing compute resource. More info : " + e.getMessage());
        }
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription) throws Exception {
        try {
            return new ComputeResourceRepository().addComputeResource(computeResourceDescription);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while saving compute resource. More info : " + e.getMessage());
        }
    }

    // --- Job submission priority/delete methods ---

    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder) throws Exception {
        return false;
    }

    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap) throws Exception {
        return false;
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws Exception {
        try {
            new ComputeResourceRepository().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting job submission interface. More info : " + e.getMessage());
        }
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws Exception {
        try {
            return new ComputeResourceRepository().getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving resource job manager. More info : " + e.getMessage());
        }
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws Exception {
        try {
            new ComputeResourceRepository().deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting resource job manager. More info : " + e.getMessage());
        }
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws Exception {
        try {
            new ComputeResourceRepository().removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while deleting batch queue. More info : " + e.getMessage());
        }
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws Exception {
        try {
            new ComputeResourceRepository().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while updating resource job manager. More info : " + e.getMessage());
        }
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws Exception {
        try {
            return new ComputeResourceRepository().addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while adding resource job manager. More info : " + e.getMessage());
        }
    }

    // --- Job submission details methods ---

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission) throws Exception {
        try {
            ComputeResourceRepository r = new ComputeResourceRepository();
            return addJobSubmissionInterface(
                    r,
                    computeResourceId,
                    r.addCloudJobSubmission(cloudSubmission),
                    JobSubmissionProtocol.JSP_CLOUD,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission) throws Exception {
        try {
            ComputeResourceRepository r = new ComputeResourceRepository();
            return addJobSubmissionInterface(
                    r,
                    computeResourceId,
                    r.addUNICOREJobSubmission(unicoreJobSubmission),
                    JobSubmissionProtocol.UNICORE,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws Exception {
        try {
            ComputeResourceRepository r = new ComputeResourceRepository();
            return addJobSubmissionInterface(
                    r,
                    computeResourceId,
                    r.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH_FORK,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws Exception {
        try {
            ComputeResourceRepository r = new ComputeResourceRepository();
            return addJobSubmissionInterface(
                    r,
                    computeResourceId,
                    r.addSSHJobSubmission(sshJobSubmission),
                    JobSubmissionProtocol.SSH,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws Exception {
        try {
            computeResourceRepository.updateSSHJobSubmission(sshJobSubmission);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission sshJobSubmission)
            throws Exception {
        try {
            computeResourceRepository.updateCloudJobSubmission(sshJobSubmission);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws Exception {
        throw new RegistryException("updateUnicoreJobSubmissionDetails is not yet implemented");
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws Exception {
        try {
            computeResourceRepository.updateLocalJobSubmission(localSubmission);
            return true;
        } catch (Exception e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws Exception {
        try {
            ComputeResourceRepository r = new ComputeResourceRepository();
            return addJobSubmissionInterface(
                    r,
                    computeResourceId,
                    r.addLocalJobSubmission(localSubmission),
                    JobSubmissionProtocol.LOCAL,
                    priorityOrder);
        } catch (AppCatalogException e) {
            throw new RegistryException(
                    "Error while adding job submission interface to resource compute resource. More info : "
                            + e.getMessage());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String addJobSubmissionInterface(
            ComputeResourceRepository computeResourceRepository,
            String computeResourceId,
            String jobSubmissionInterfaceId,
            JobSubmissionProtocol protocolType,
            int priorityOrder)
            throws AppCatalogException {
        JobSubmissionInterface jsi = JobSubmissionInterface.newBuilder()
                .setJobSubmissionInterfaceId(jobSubmissionInterfaceId)
                .setPriorityOrder(priorityOrder)
                .setJobSubmissionProtocol(protocolType)
                .build();
        return computeResourceRepository.addJobSubmissionProtocol(computeResourceId, jsi);
    }
}
