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
import org.apache.airavata.research.service.entity.LocalComputeResourceEntity;
import org.apache.airavata.research.service.dto.ComputeResourceDTO;
import org.apache.airavata.research.service.repository.LocalComputeResourceRepository;
import org.apache.airavata.research.service.util.DTOConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Local handler for Compute Resource operations using airavata-api entities with local database
 * Alternative to external registry services for development
 */
@Component("localComputeResourceHandler")
public class LocalComputeResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalComputeResourceHandler.class);

    private final LocalComputeResourceRepository computeResourceRepository;
    private final DTOConverter dtoConverter;

    public LocalComputeResourceHandler(LocalComputeResourceRepository computeResourceRepository,
                                     DTOConverter dtoConverter) {
        this.computeResourceRepository = computeResourceRepository;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Get all enabled compute resources
     */
    public List<ComputeResourceDTO> getAllComputeResources() {
        LOGGER.info("Getting all local compute resources");
        
        try {
            List<LocalComputeResourceEntity> entities = computeResourceRepository.findAllEnabledOrderByCreationTime();
            List<ComputeResourceDTO> dtos = new ArrayList<>();
            
            for (LocalComputeResourceEntity entity : entities) {
                ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} local compute resources", dtos.size());
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to get local compute resources", e);
            throw new RuntimeException("Failed to get local compute resources", e);
        }
    }

    /**
     * Search compute resources by keyword
     */
    public List<ComputeResourceDTO> searchComputeResources(String keyword) {
        LOGGER.info("Searching local compute resources with keyword: {}", keyword);
        
        try {
            List<LocalComputeResourceEntity> entities;
            
            if (keyword == null || keyword.trim().isEmpty()) {
                entities = computeResourceRepository.findAllEnabledOrderByCreationTime();
            } else {
                entities = computeResourceRepository.searchEnabledComputeResources(keyword.trim());
            }
            
            List<ComputeResourceDTO> dtos = new ArrayList<>();
            for (LocalComputeResourceEntity entity : entities) {
                ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} compute resources matching keyword '{}'", dtos.size(), keyword);
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to search local compute resources", e);
            throw new RuntimeException("Failed to search local compute resources", e);
        }
    }

    /**
     * Get compute resource by ID
     */
    public ComputeResourceDTO getComputeResource(String computeResourceId) {
        LOGGER.info("Getting local compute resource by ID: {}", computeResourceId);
        
        try {
            Optional<LocalComputeResourceEntity> entityOpt = computeResourceRepository.findById(computeResourceId);
            
            if (entityOpt.isEmpty()) {
                LOGGER.warn("Compute resource not found with ID: {}", computeResourceId);
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            LocalComputeResourceEntity entity = entityOpt.get();
            ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
            
            LOGGER.info("Found local compute resource: {}", entity.getHostName());
            return dto;
        } catch (Exception e) {
            LOGGER.error("Failed to get local compute resource by ID: {}", computeResourceId, e);
            throw new RuntimeException("Failed to get local compute resource", e);
        }
    }

    /**
     * Create new compute resource
     */
    public ComputeResourceDTO createComputeResource(ComputeResourceDTO computeResourceDTO) {
        LOGGER.info("Creating local compute resource: {}", computeResourceDTO.getHostName());
        
        try {
            // Convert DTO to entity using existing DTOConverter
            LocalComputeResourceEntity entity = dtoConverter.computeResourceDTOToEntity(computeResourceDTO);
            
            // Set system fields
            entity.setComputeResourceId(UUID.randomUUID().toString());
            entity.setEnabled((short) 1);
            entity.setCreationTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save to local database
            LocalComputeResourceEntity savedEntity = computeResourceRepository.save(entity);
            
            // Convert back to DTO
            ComputeResourceDTO savedDTO = dtoConverter.computeEntityToDTO(savedEntity);
            
            LOGGER.info("Created local compute resource with ID: {}", savedEntity.getComputeResourceId());
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to create local compute resource", e);
            throw new RuntimeException("Failed to create local compute resource", e);
        }
    }

    /**
     * Update existing compute resource
     */
    public ComputeResourceDTO updateComputeResource(String computeResourceId, ComputeResourceDTO computeResourceDTO) {
        LOGGER.info("Updating local compute resource: {}", computeResourceId);
        
        try {
            Optional<LocalComputeResourceEntity> existingOpt = computeResourceRepository.findById(computeResourceId);
            
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            // Convert DTO to entity
            LocalComputeResourceEntity updatedEntity = dtoConverter.computeResourceDTOToEntity(computeResourceDTO);
            
            // Preserve system fields
            LocalComputeResourceEntity existing = existingOpt.get();
            updatedEntity.setComputeResourceId(computeResourceId);
            updatedEntity.setCreationTime(existing.getCreationTime());
            updatedEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save updated entity
            LocalComputeResourceEntity savedEntity = computeResourceRepository.save(updatedEntity);
            
            // Convert back to DTO
            ComputeResourceDTO savedDTO = dtoConverter.computeEntityToDTO(savedEntity);
            
            LOGGER.info("Updated local compute resource: {}", computeResourceId);
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to update local compute resource: {}", computeResourceId, e);
            throw new RuntimeException("Failed to update local compute resource", e);
        }
    }

    /**
     * Delete compute resource
     */
    public void deleteComputeResource(String computeResourceId) {
        LOGGER.info("Deleting local compute resource: {}", computeResourceId);
        
        try {
            if (!computeResourceRepository.existsById(computeResourceId)) {
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            computeResourceRepository.deleteById(computeResourceId);
            LOGGER.info("Deleted local compute resource: {}", computeResourceId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete local compute resource: {}", computeResourceId, e);
            throw new RuntimeException("Failed to delete local compute resource", e);
        }
    }

    /**
     * Check if compute resource exists
     */
    public boolean existsComputeResource(String computeResourceId) {
        LOGGER.debug("Checking if compute resource exists: {}", computeResourceId);
        
        try {
            boolean exists = computeResourceRepository.existsById(computeResourceId);
            LOGGER.debug("Compute resource {} exists: {}", computeResourceId, exists);
            return exists;
        } catch (Exception e) {
            LOGGER.error("Failed to check compute resource existence: {}", computeResourceId, e);
            return false;
        }
    }
}