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
}
