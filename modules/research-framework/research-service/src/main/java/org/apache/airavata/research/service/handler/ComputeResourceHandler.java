/**
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
package org.apache.airavata.research.service.handler;

import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.research.service.config.RegistryServiceConfig;
import org.apache.airavata.research.service.dto.ComputeResourceDTO;
import org.apache.airavata.research.service.util.DTOConverter;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for Compute Resource operations using Airavata Registry Service
 * Integrates with existing airavata-api infrastructure
 */
@Component
public class ComputeResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResourceHandler.class);

    @Autowired
    private RegistryServiceConfig.RegistryServiceProvider registryServiceProvider;

    @Autowired
    private DTOConverter dtoConverter;

    /**
     * Get RegistryService with connection validation
     */
    private RegistryService.Iface getRegistryService() throws RegistryServiceException {
        return registryServiceProvider.getRegistryService();
    }

    /**
     * Check if registry service is available
     */
    private boolean isRegistryServiceAvailable() {
        return registryServiceProvider.isAvailable();
    }

    /**
     * Create a new compute resource using registry service
     */
    public ComputeResourceDTO createComputeResource(ComputeResourceDTO dto) throws RegistryServiceException, TException {
        LOGGER.debug("Creating compute resource: {}", dto.getHostName());
        
        try {
            // Convert DTO to Thrift model
            ComputeResourceDescription thriftModel = dtoConverter.dtoToThrift(dto);
            
            // Generate ID if not provided
            if (thriftModel.getComputeResourceId() == null || thriftModel.getComputeResourceId().isEmpty()) {
                thriftModel.setComputeResourceId(generateComputeResourceId());
            }
            
            // Use existing registry service to save
            String resourceId = getRegistryService().registerComputeResource(thriftModel);
            LOGGER.info("Successfully created compute resource with ID: {}", resourceId);
            
            // Retrieve saved entity with generated/updated fields
            ComputeResourceDescription savedModel = getRegistryService().getComputeResource(resourceId);
            
            // Convert back to DTO for frontend
            return dtoConverter.thriftToDTO(savedModel);
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to create compute resource: {}", e.getMessage(), e);
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error creating compute resource: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error creating compute resource", e);
            throw new RegistryServiceException("Failed to create compute resource: " + e.getMessage());
        }
    }

    /**
     * Get compute resource by ID
     */
    public ComputeResourceDTO getComputeResource(String resourceId) throws RegistryServiceException, TException {
        LOGGER.debug("Retrieving compute resource: {}", resourceId);
        
        try {
            ComputeResourceDescription thriftModel = getRegistryService().getComputeResource(resourceId);
            return dtoConverter.thriftToDTO(thriftModel);
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to get compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error getting compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all compute resources
     */
    public List<ComputeResourceDTO> getAllComputeResources() throws RegistryServiceException, TException {
        LOGGER.debug("Retrieving all compute resources");
        
        try {
            // Get compute resource names (ID -> Name mapping)
            Map<String, String> computeResourceNames = getRegistryService().getAllComputeResourceNames();
            
            // Fetch full details for each compute resource
            return computeResourceNames.keySet().stream()
                .map(resourceId -> {
                    try {
                        return getRegistryService().getComputeResource(resourceId);
                    } catch (RegistryServiceException e) {
                        LOGGER.warn("Failed to get compute resource {}: {}", resourceId, e.getMessage());
                        return null;
                    } catch (TException e) {
                        LOGGER.warn("Thrift error getting compute resource {}: {}", resourceId, e.getMessage());
                        return null;
                    }
                })
                .filter(thriftModel -> thriftModel != null)
                .map(thriftModel -> dtoConverter.thriftToDTO(thriftModel))
                .collect(Collectors.toList());
                
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to get all compute resources: {}", e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error getting all compute resources: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update compute resource
     */
    public ComputeResourceDTO updateComputeResource(String resourceId, ComputeResourceDTO dto) throws RegistryServiceException, TException {
        LOGGER.debug("Updating compute resource: {}", resourceId);
        
        try {
            // Ensure DTO has the correct ID
            dto.setComputeResourceId(resourceId);
            
            // Convert DTO to Thrift model
            ComputeResourceDescription thriftModel = dtoConverter.dtoToThrift(dto);
            
            // Use existing registry service to update
            boolean updated = getRegistryService().updateComputeResource(resourceId, thriftModel);
            
            if (updated) {
                LOGGER.info("Successfully updated compute resource: {}", resourceId);
                
                // Retrieve updated entity
                ComputeResourceDescription updatedModel = getRegistryService().getComputeResource(resourceId);
                return dtoConverter.thriftToDTO(updatedModel);
            } else {
                throw new RegistryServiceException("Failed to update compute resource: " + resourceId);
            }
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to update compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error updating compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete compute resource
     */
    public void deleteComputeResource(String resourceId) throws RegistryServiceException, TException {
        LOGGER.debug("Deleting compute resource: {}", resourceId);
        
        try {
            boolean deleted = getRegistryService().deleteComputeResource(resourceId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted compute resource: {}", resourceId);
            } else {
                throw new RegistryServiceException("Failed to delete compute resource: " + resourceId);
            }
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to delete compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error deleting compute resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Search compute resources by keyword
     * Note: This is a simplified implementation - Airavata registry might have more sophisticated search
     */
    public List<ComputeResourceDTO> searchComputeResources(String keyword) throws RegistryServiceException, TException {
        LOGGER.debug("Searching compute resources with keyword: {}", keyword);
        
        try {
            // Get all compute resources and filter by keyword
            List<ComputeResourceDTO> allResources = getAllComputeResources();
            
            String lowerKeyword = keyword.toLowerCase();
            return allResources.stream()
                .filter(resource -> 
                    (resource.getHostName() != null && resource.getHostName().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getResourceDescription() != null && resource.getResourceDescription().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getComputeType() != null && resource.getComputeType().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getOperatingSystem() != null && resource.getOperatingSystem().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
                
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to search compute resources: {}", e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error searching compute resources: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if compute resource exists
     */
    public boolean existsComputeResource(String resourceId) {
        try {
            ComputeResourceDescription resource = getRegistryService().getComputeResource(resourceId);
            return resource != null;
        } catch (RegistryServiceException e) {
            LOGGER.debug("Compute resource {} does not exist: {}", resourceId, e.getMessage());
            return false;
        } catch (TException e) {
            LOGGER.debug("Thrift error checking compute resource {}: {}", resourceId, e.getMessage());
            return false;
        }
    }

    /**
     * Generate unique compute resource ID
     */
    private String generateComputeResourceId() {
        return "compute_" + UUID.randomUUID().toString().replace("-", "");
    }
}