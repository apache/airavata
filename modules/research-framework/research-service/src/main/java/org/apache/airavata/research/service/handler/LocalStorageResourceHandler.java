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
package org.apache.airavata.research.service.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.Timestamp;
import org.apache.airavata.research.service.entity.LocalStorageResourceEntity;
import org.apache.airavata.research.service.dto.StorageResourceDTO;
import org.apache.airavata.research.service.repository.LocalStorageResourceRepository;
import org.apache.airavata.research.service.util.DTOConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Local handler for Storage Resource operations using airavata-api entities with local database
 * Alternative to external registry services for development
 */
@Component("localStorageResourceHandler")
public class LocalStorageResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageResourceHandler.class);

    private final LocalStorageResourceRepository storageResourceRepository;
    private final DTOConverter dtoConverter;

    public LocalStorageResourceHandler(LocalStorageResourceRepository storageResourceRepository,
                                      DTOConverter dtoConverter) {
        this.storageResourceRepository = storageResourceRepository;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Get all enabled storage resources
     */
    public List<StorageResourceDTO> getAllStorageResources() {
        LOGGER.info("Getting all local storage resources");
        
        try {
            List<LocalStorageResourceEntity> entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            List<StorageResourceDTO> dtos = new ArrayList<>();
            
            for (LocalStorageResourceEntity entity : entities) {
                StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} local storage resources", dtos.size());
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to get local storage resources", e);
            throw new RuntimeException("Failed to get local storage resources", e);
        }
    }

    /**
     * Search storage resources by keyword
     */
    public List<StorageResourceDTO> searchStorageResources(String keyword) {
        LOGGER.info("Searching local storage resources with keyword: {}", keyword);
        
        try {
            List<LocalStorageResourceEntity> entities;
            
            if (keyword == null || keyword.trim().isEmpty()) {
                entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            } else {
                entities = storageResourceRepository.searchEnabledStorageResources(keyword.trim());
            }
            
            List<StorageResourceDTO> dtos = new ArrayList<>();
            for (LocalStorageResourceEntity entity : entities) {
                StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} storage resources matching keyword '{}'", dtos.size(), keyword);
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to search local storage resources", e);
            throw new RuntimeException("Failed to search local storage resources", e);
        }
    }

    /**
     * Get storage resource by ID
     */
    public StorageResourceDTO getStorageResource(String storageResourceId) {
        LOGGER.info("Getting local storage resource by ID: {}", storageResourceId);
        
        try {
            Optional<LocalStorageResourceEntity> entityOpt = storageResourceRepository.findById(storageResourceId);
            
            if (entityOpt.isEmpty()) {
                LOGGER.warn("Storage resource not found with ID: {}", storageResourceId);
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            LocalStorageResourceEntity entity = entityOpt.get();
            StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
            
            LOGGER.info("Found local storage resource: {}", entity.getHostName());
            return dto;
        } catch (Exception e) {
            LOGGER.error("Failed to get local storage resource by ID: {}", storageResourceId, e);
            throw new RuntimeException("Failed to get local storage resource", e);
        }
    }

    /**
     * Create new storage resource
     */
    public StorageResourceDTO createStorageResource(StorageResourceDTO storageResourceDTO) {
        LOGGER.info("Creating local storage resource: {}", storageResourceDTO.getHostName());
        
        try {
            // Convert DTO to entity using existing DTOConverter
            LocalStorageResourceEntity entity = dtoConverter.storageResourceDTOToEntity(storageResourceDTO);
            
            // Set system fields
            entity.setStorageResourceId(UUID.randomUUID().toString());
            entity.setEnabled(true);
            entity.setCreationTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save to local database
            LocalStorageResourceEntity savedEntity = storageResourceRepository.save(entity);
            
            // Convert back to DTO
            StorageResourceDTO savedDTO = dtoConverter.storageEntityToDTO(savedEntity);
            
            LOGGER.info("Created local storage resource with ID: {}", savedEntity.getStorageResourceId());
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to create local storage resource", e);
            throw new RuntimeException("Failed to create local storage resource", e);
        }
    }

    /**
     * Update existing storage resource
     */
    public StorageResourceDTO updateStorageResource(String storageResourceId, StorageResourceDTO storageResourceDTO) {
        LOGGER.info("Updating local storage resource: {}", storageResourceId);
        
        try {
            Optional<LocalStorageResourceEntity> existingOpt = storageResourceRepository.findById(storageResourceId);
            
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            // Convert DTO to entity
            LocalStorageResourceEntity updatedEntity = dtoConverter.storageResourceDTOToEntity(storageResourceDTO);
            
            // Preserve system fields
            LocalStorageResourceEntity existing = existingOpt.get();
            updatedEntity.setStorageResourceId(storageResourceId);
            updatedEntity.setCreationTime(existing.getCreationTime());
            updatedEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save updated entity
            LocalStorageResourceEntity savedEntity = storageResourceRepository.save(updatedEntity);
            
            // Convert back to DTO
            StorageResourceDTO savedDTO = dtoConverter.storageEntityToDTO(savedEntity);
            
            LOGGER.info("Updated local storage resource: {}", storageResourceId);
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to update local storage resource: {}", storageResourceId, e);
            throw new RuntimeException("Failed to update local storage resource", e);
        }
    }

    /**
     * Delete storage resource
     */
    public void deleteStorageResource(String storageResourceId) {
        LOGGER.info("Deleting local storage resource: {}", storageResourceId);
        
        try {
            if (!storageResourceRepository.existsById(storageResourceId)) {
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            storageResourceRepository.deleteById(storageResourceId);
            LOGGER.info("Deleted local storage resource: {}", storageResourceId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete local storage resource: {}", storageResourceId, e);
            throw new RuntimeException("Failed to delete local storage resource", e);
        }
    }

    /**
     * Get storage resources by storage type
     */
    public List<StorageResourceDTO> getStorageResourcesByType(String storageType) {
        LOGGER.info("Getting local storage resources by type: {}", storageType);
        
        try {
            List<LocalStorageResourceEntity> entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            List<StorageResourceDTO> dtos = new ArrayList<>();
            
            for (LocalStorageResourceEntity entity : entities) {
                StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
                // Filter by storage type from UI fields
                if (storageType == null || storageType.isEmpty() || 
                    (dto.getStorageType() != null && dto.getStorageType().equalsIgnoreCase(storageType))) {
                    dtos.add(dto);
                }
            }
            
            LOGGER.info("Found {} storage resources of type '{}'", dtos.size(), storageType);
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to get storage resources by type", e);
            throw new RuntimeException("Failed to get storage resources by type", e);
        }
    }

    /**
     * Check if storage resource exists
     */
    public boolean existsStorageResource(String storageResourceId) {
        LOGGER.debug("Checking if storage resource exists: {}", storageResourceId);
        
        try {
            boolean exists = storageResourceRepository.existsById(storageResourceId);
            LOGGER.debug("Storage resource {} exists: {}", storageResourceId, exists);
            return exists;
        } catch (Exception e) {
            LOGGER.error("Failed to check storage resource existence: {}", storageResourceId, e);
            return false;
        }
    }
}