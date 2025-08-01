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

import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.research.service.config.RegistryServiceConfig;
import org.apache.airavata.research.service.dto.StorageResourceDTO;
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
 * Handler for Storage Resource operations using Airavata Registry Service
 * Integrates with existing airavata-api infrastructure
 */
@Component
public class StorageResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageResourceHandler.class);

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
     * Create a new storage resource using registry service
     */
    public StorageResourceDTO createStorageResource(StorageResourceDTO dto) throws RegistryServiceException, TException {
        LOGGER.debug("Creating storage resource: {}", dto.getHostName());
        
        try {
            // Convert DTO to Thrift model
            StorageResourceDescription thriftModel = dtoConverter.dtoToThrift(dto);
            
            // Generate ID if not provided
            if (thriftModel.getStorageResourceId() == null || thriftModel.getStorageResourceId().isEmpty()) {
                thriftModel.setStorageResourceId(generateStorageResourceId());
            }
            
            // Set timestamps
            long currentTime = System.currentTimeMillis();
            thriftModel.setCreationTime(currentTime);
            thriftModel.setUpdateTime(currentTime);
            
            // Use existing registry service to save
            String resourceId = getRegistryService().registerStorageResource(thriftModel);
            LOGGER.info("Successfully created storage resource with ID: {}", resourceId);
            
            // Retrieve saved entity with generated/updated fields
            StorageResourceDescription savedModel = getRegistryService().getStorageResource(resourceId);
            
            // Convert back to DTO for frontend
            return dtoConverter.thriftToDTO(savedModel);
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to create storage resource: {}", e.getMessage(), e);
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error creating storage resource: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error creating storage resource", e);
            throw new RegistryServiceException("Failed to create storage resource: " + e.getMessage());
        }
    }

    /**
     * Get storage resource by ID
     */
    public StorageResourceDTO getStorageResource(String resourceId) throws RegistryServiceException, TException {
        LOGGER.debug("Retrieving storage resource: {}", resourceId);
        
        try {
            StorageResourceDescription thriftModel = getRegistryService().getStorageResource(resourceId);
            return dtoConverter.thriftToDTO(thriftModel);
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to get storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error getting storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all storage resources
     */
    public List<StorageResourceDTO> getAllStorageResources() throws RegistryServiceException, TException {
        LOGGER.debug("Retrieving all storage resources");
        
        try {
            // Get storage resource names (ID -> Name mapping)
            Map<String, String> storageResourceNames = getRegistryService().getAllStorageResourceNames();
            
            // Fetch full details for each storage resource
            return storageResourceNames.keySet().stream()
                .map(resourceId -> {
                    try {
                        return getRegistryService().getStorageResource(resourceId);
                    } catch (RegistryServiceException e) {
                        LOGGER.warn("Failed to get storage resource {}: {}", resourceId, e.getMessage());
                        return null;
                    } catch (TException e) {
                        LOGGER.warn("Thrift error getting storage resource {}: {}", resourceId, e.getMessage());
                        return null;
                    }
                })
                .filter(thriftModel -> thriftModel != null)
                .map(thriftModel -> dtoConverter.thriftToDTO(thriftModel))
                .collect(Collectors.toList());
                
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to get all storage resources: {}", e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error getting all storage resources: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update storage resource
     */
    public StorageResourceDTO updateStorageResource(String resourceId, StorageResourceDTO dto) throws RegistryServiceException, TException {
        LOGGER.debug("Updating storage resource: {}", resourceId);
        
        try {
            // Ensure DTO has the correct ID
            dto.setStorageResourceId(resourceId);
            
            // Convert DTO to Thrift model
            StorageResourceDescription thriftModel = dtoConverter.dtoToThrift(dto);
            
            // Set update timestamp
            thriftModel.setUpdateTime(System.currentTimeMillis());
            
            // Use existing registry service to update
            boolean updated = getRegistryService().updateStorageResource(resourceId, thriftModel);
            
            if (updated) {
                LOGGER.info("Successfully updated storage resource: {}", resourceId);
                
                // Retrieve updated entity
                StorageResourceDescription updatedModel = getRegistryService().getStorageResource(resourceId);
                return dtoConverter.thriftToDTO(updatedModel);
            } else {
                throw new RegistryServiceException("Failed to update storage resource: " + resourceId);
            }
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to update storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error updating storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete storage resource
     */
    public void deleteStorageResource(String resourceId) throws RegistryServiceException, TException {
        LOGGER.debug("Deleting storage resource: {}", resourceId);
        
        try {
            boolean deleted = getRegistryService().deleteStorageResource(resourceId);
            
            if (deleted) {
                LOGGER.info("Successfully deleted storage resource: {}", resourceId);
            } else {
                throw new RegistryServiceException("Failed to delete storage resource: " + resourceId);
            }
            
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to delete storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error deleting storage resource {}: {}", resourceId, e.getMessage());
            throw e;
        }
    }

    /**
     * Search storage resources by keyword
     * Note: This is a simplified implementation - Airavata registry might have more sophisticated search
     */
    public List<StorageResourceDTO> searchStorageResources(String keyword) throws RegistryServiceException, TException {
        LOGGER.debug("Searching storage resources with keyword: {}", keyword);
        
        try {
            // Get all storage resources and filter by keyword
            List<StorageResourceDTO> allResources = getAllStorageResources();
            
            String lowerKeyword = keyword.toLowerCase();
            return allResources.stream()
                .filter(resource -> 
                    (resource.getHostName() != null && resource.getHostName().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getStorageResourceDescription() != null && resource.getStorageResourceDescription().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getStorageType() != null && resource.getStorageType().toLowerCase().contains(lowerKeyword)) ||
                    (resource.getAccessProtocol() != null && resource.getAccessProtocol().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
                
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to search storage resources: {}", e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error searching storage resources: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Filter storage resources by type
     */
    public List<StorageResourceDTO> getStorageResourcesByType(String storageType) throws RegistryServiceException, TException {
        LOGGER.debug("Filtering storage resources by type: {}", storageType);
        
        try {
            List<StorageResourceDTO> allResources = getAllStorageResources();
            
            return allResources.stream()
                .filter(resource -> resource.getStorageType() != null && 
                                  resource.getStorageType().equalsIgnoreCase(storageType))
                .collect(Collectors.toList());
                
        } catch (RegistryServiceException e) {
            LOGGER.error("Failed to filter storage resources by type: {}", e.getMessage());
            throw e;
        } catch (TException e) {
            LOGGER.error("Thrift error filtering storage resources by type: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if storage resource exists
     */
    public boolean existsStorageResource(String resourceId) {
        try {
            StorageResourceDescription resource = getRegistryService().getStorageResource(resourceId);
            return resource != null;
        } catch (RegistryServiceException e) {
            LOGGER.debug("Storage resource {} does not exist: {}", resourceId, e.getMessage());
            return false;
        } catch (TException e) {
            LOGGER.debug("Thrift error checking storage resource {}: {}", resourceId, e.getMessage());
            return false;
        }
    }

    /**
     * Generate unique storage resource ID
     */
    private String generateStorageResourceId() {
        return "storage_" + UUID.randomUUID().toString().replace("-", "");
    }
}