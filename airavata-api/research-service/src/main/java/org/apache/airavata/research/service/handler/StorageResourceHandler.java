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

import org.apache.airavata.registry.core.entities.appcatalog.StorageResourceEntity;
import org.apache.airavata.research.service.dto.StorageResourceDTO;
import org.apache.airavata.research.service.repository.StorageResourceRepository;
import org.apache.airavata.research.service.util.DTOConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for Storage Resource operations using local entities with app_catalog database
 * Direct integration with Airavata app_catalog database
 */
@Component("storageResourceHandler")
public class StorageResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageResourceHandler.class);

    private final StorageResourceRepository storageResourceRepository;
    private final DTOConverter dtoConverter;

    public StorageResourceHandler(StorageResourceRepository storageResourceRepository,
                                 DTOConverter dtoConverter) {
        this.storageResourceRepository = storageResourceRepository;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Get all enabled storage resources
     */
    public List<StorageResourceDTO> getAllStorageResources() {
        LOGGER.info("Getting all storage resources from app_catalog");
        
        try {
            List<StorageResourceEntity> entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            List<StorageResourceDTO> dtos = new ArrayList<>();
            
            for (StorageResourceEntity entity : entities) {
                StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} storage resources from app_catalog", dtos.size());
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to get storage resources from app_catalog", e);
            throw new RuntimeException("Failed to get storage resources", e);
        }
    }

    /**
     * Search storage resources by keyword
     */
    public List<StorageResourceDTO> searchStorageResources(String keyword) {
        LOGGER.info("Searching storage resources in app_catalog with keyword: {}", keyword);
        
        try {
            List<StorageResourceEntity> entities;
            
            if (keyword == null || keyword.trim().isEmpty()) {
                entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            } else {
                entities = storageResourceRepository.findEnabledByHostNameContaining(keyword.trim());
            }
            
            List<StorageResourceDTO> dtos = new ArrayList<>();
            for (StorageResourceEntity entity : entities) {
                StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} storage resources matching keyword '{}'", dtos.size(), keyword);
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to search storage resources in app_catalog", e);
            throw new RuntimeException("Failed to search storage resources", e);
        }
    }


    /**
     * Get storage resource by ID
     */
    public StorageResourceDTO getStorageResource(String storageResourceId) {
        LOGGER.info("Getting storage resource by ID from app_catalog: {}", storageResourceId);
        
        try {
            Optional<StorageResourceEntity> entityOpt = storageResourceRepository.findById(storageResourceId);
            
            if (entityOpt.isEmpty()) {
                LOGGER.warn("Storage resource not found with ID: {}", storageResourceId);
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            StorageResourceEntity entity = entityOpt.get();
            StorageResourceDTO dto = dtoConverter.storageEntityToDTO(entity);
            
            LOGGER.info("Found storage resource: {}", entity.getHostName());
            return dto;
        } catch (Exception e) {
            LOGGER.error("Failed to get storage resource by ID: {}", storageResourceId, e);
            throw new RuntimeException("Failed to get storage resource", e);
        }
    }

    /**
     * Create new storage resource
     */
    public StorageResourceDTO createStorageResource(StorageResourceDTO storageResourceDTO) {
        LOGGER.info("Creating storage resource in app_catalog: {}", storageResourceDTO.getHostName());
        
        try {
            // Convert DTO to entity using existing DTOConverter
            StorageResourceEntity entity = dtoConverter.storageResourceDTOToEntity(storageResourceDTO);
            
            // Set system fields
            entity.setStorageResourceId(UUID.randomUUID().toString());
            entity.setEnabled(true);
            entity.setCreationTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save to app_catalog database
            StorageResourceEntity savedEntity = storageResourceRepository.save(entity);
            
            // Convert back to DTO
            StorageResourceDTO savedDTO = dtoConverter.storageEntityToDTO(savedEntity);
            
            LOGGER.info("Created storage resource in app_catalog with ID: {}", savedEntity.getStorageResourceId());
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to create storage resource in app_catalog", e);
            throw new RuntimeException("Failed to create storage resource", e);
        }
    }

    /**
     * Update existing storage resource
     */
    public StorageResourceDTO updateStorageResource(String storageResourceId, StorageResourceDTO storageResourceDTO) {
        LOGGER.info("Updating storage resource in app_catalog: {}", storageResourceId);
        
        try {
            Optional<StorageResourceEntity> existingOpt = storageResourceRepository.findById(storageResourceId);
            
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            // Convert DTO to entity
            StorageResourceEntity updatedEntity = dtoConverter.storageResourceDTOToEntity(storageResourceDTO);
            
            // Preserve system fields
            StorageResourceEntity existing = existingOpt.get();
            updatedEntity.setStorageResourceId(storageResourceId);
            updatedEntity.setCreationTime(existing.getCreationTime());
            updatedEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save updated entity
            StorageResourceEntity savedEntity = storageResourceRepository.save(updatedEntity);
            
            // Convert back to DTO
            StorageResourceDTO savedDTO = dtoConverter.storageEntityToDTO(savedEntity);
            
            LOGGER.info("Updated storage resource in app_catalog: {}", storageResourceId);
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to update storage resource in app_catalog: {}", storageResourceId, e);
            throw new RuntimeException("Failed to update storage resource", e);
        }
    }

    /**
     * Delete storage resource
     */
    public void deleteStorageResource(String storageResourceId) {
        LOGGER.info("Deleting storage resource from app_catalog: {}", storageResourceId);
        
        try {
            if (!storageResourceRepository.existsById(storageResourceId)) {
                throw new RuntimeException("Storage resource not found with ID: " + storageResourceId);
            }
            
            storageResourceRepository.deleteById(storageResourceId);
            LOGGER.info("Deleted storage resource from app_catalog: {}", storageResourceId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete storage resource from app_catalog: {}", storageResourceId, e);
            throw new RuntimeException("Failed to delete storage resource", e);
        }
    }

    /**
     * Get storage resources by type
     */
    public List<StorageResourceDTO> getStorageResourcesByType(String storageType) {
        LOGGER.info("Getting storage resources by type from app_catalog: {}", storageType);
        
        try {
            List<StorageResourceEntity> entities = storageResourceRepository.findAllEnabledOrderByCreationTime();
            List<StorageResourceDTO> dtos = new ArrayList<>();
            
            for (StorageResourceEntity entity : entities) {
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
            LOGGER.error("Failed to get storage resources by type from app_catalog", e);
            throw new RuntimeException("Failed to get storage resources by type", e);
        }
    }

    /**
     * Check if storage resource exists
     */
    public boolean existsStorageResource(String storageResourceId) {
        LOGGER.debug("Checking if storage resource exists in app_catalog: {}", storageResourceId);
        
        try {
            boolean exists = storageResourceRepository.existsById(storageResourceId);
            LOGGER.debug("Storage resource {} exists: {}", storageResourceId, exists);
            return exists;
        } catch (Exception e) {
            LOGGER.error("Failed to check storage resource existence in app_catalog: {}", storageResourceId, e);
            return false;
        }
    }
}