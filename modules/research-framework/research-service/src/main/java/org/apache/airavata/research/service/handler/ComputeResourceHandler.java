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

import org.apache.airavata.research.service.entity.ComputeResourceEntity;
import org.apache.airavata.research.service.dto.ComputeResourceDTO;
import org.apache.airavata.research.service.repository.ComputeResourceRepository;
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
 * Handler for Compute Resource operations using local entities with app_catalog database
 * Direct integration with Airavata app_catalog database
 */
@Component("computeResourceHandler")
public class ComputeResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResourceHandler.class);

    private final ComputeResourceRepository computeResourceRepository;
    private final DTOConverter dtoConverter;

    public ComputeResourceHandler(ComputeResourceRepository computeResourceRepository,
                                 DTOConverter dtoConverter) {
        this.computeResourceRepository = computeResourceRepository;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Get all enabled compute resources
     */
    public List<ComputeResourceDTO> getAllComputeResources() {
        LOGGER.info("Getting all compute resources from app_catalog");
        
        try {
            List<ComputeResourceEntity> entities = computeResourceRepository.findAllEnabledOrderByCreationTime();
            List<ComputeResourceDTO> dtos = new ArrayList<>();
            
            for (ComputeResourceEntity entity : entities) {
                ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} compute resources from app_catalog", dtos.size());
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to get compute resources from app_catalog", e);
            throw new RuntimeException("Failed to get compute resources", e);
        }
    }

    /**
     * Search compute resources by hostname
     */
    public List<ComputeResourceDTO> searchComputeResources(String keyword) {
        LOGGER.info("Searching compute resources in app_catalog with keyword: {}", keyword);
        
        try {
            List<ComputeResourceEntity> entities;
            
            if (keyword == null || keyword.trim().isEmpty()) {
                entities = computeResourceRepository.findAllEnabledOrderByCreationTime();
            } else {
                entities = computeResourceRepository.findEnabledByHostNameContaining(keyword.trim());
            }
            
            List<ComputeResourceDTO> dtos = new ArrayList<>();
            for (ComputeResourceEntity entity : entities) {
                ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
                dtos.add(dto);
            }
            
            LOGGER.info("Found {} compute resources matching keyword '{}'", dtos.size(), keyword);
            return dtos;
        } catch (Exception e) {
            LOGGER.error("Failed to search compute resources in app_catalog", e);
            throw new RuntimeException("Failed to search compute resources", e);
        }
    }

    /**
     * Get compute resource by ID
     */
    public ComputeResourceDTO getComputeResource(String computeResourceId) {
        LOGGER.info("Getting compute resource by ID from app_catalog: {}", computeResourceId);
        
        try {
            Optional<ComputeResourceEntity> entityOpt = computeResourceRepository.findById(computeResourceId);
            
            if (entityOpt.isEmpty()) {
                LOGGER.warn("Compute resource not found with ID: {}", computeResourceId);
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            ComputeResourceEntity entity = entityOpt.get();
            ComputeResourceDTO dto = dtoConverter.computeEntityToDTO(entity);
            
            LOGGER.info("Found compute resource: {}", entity.getHostName());
            return dto;
        } catch (Exception e) {
            LOGGER.error("Failed to get compute resource by ID: {}", computeResourceId, e);
            throw new RuntimeException("Failed to get compute resource", e);
        }
    }

    /**
     * Create new compute resource
     */
    public ComputeResourceDTO createComputeResource(ComputeResourceDTO computeResourceDTO) {
        LOGGER.info("Creating compute resource in app_catalog: {}", computeResourceDTO.getHostName());
        
        try {
            // Convert DTO to entity using existing DTOConverter
            ComputeResourceEntity entity = dtoConverter.computeResourceDTOToEntity(computeResourceDTO);
            
            // Set system fields
            entity.setResourceId(UUID.randomUUID().toString());
            entity.setEnabled((short) 1);
            entity.setCreationTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save to app_catalog database
            ComputeResourceEntity savedEntity = computeResourceRepository.save(entity);
            
            // Convert back to DTO
            ComputeResourceDTO savedDTO = dtoConverter.computeEntityToDTO(savedEntity);
            
            LOGGER.info("Created compute resource in app_catalog with ID: {}", savedEntity.getResourceId());
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to create compute resource in app_catalog", e);
            throw new RuntimeException("Failed to create compute resource", e);
        }
    }

    /**
     * Update existing compute resource
     */
    public ComputeResourceDTO updateComputeResource(String computeResourceId, ComputeResourceDTO computeResourceDTO) {
        LOGGER.info("Updating compute resource in app_catalog: {}", computeResourceId);
        
        try {
            Optional<ComputeResourceEntity> existingOpt = computeResourceRepository.findById(computeResourceId);
            
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            // Convert DTO to entity
            ComputeResourceEntity updatedEntity = dtoConverter.computeResourceDTOToEntity(computeResourceDTO);
            
            // Preserve system fields
            ComputeResourceEntity existing = existingOpt.get();
            updatedEntity.setResourceId(computeResourceId);
            updatedEntity.setCreationTime(existing.getCreationTime());
            updatedEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            
            // Save updated entity
            ComputeResourceEntity savedEntity = computeResourceRepository.save(updatedEntity);
            
            // Convert back to DTO
            ComputeResourceDTO savedDTO = dtoConverter.computeEntityToDTO(savedEntity);
            
            LOGGER.info("Updated compute resource in app_catalog: {}", computeResourceId);
            return savedDTO;
        } catch (Exception e) {
            LOGGER.error("Failed to update compute resource in app_catalog: {}", computeResourceId, e);
            throw new RuntimeException("Failed to update compute resource", e);
        }
    }

    /**
     * Delete compute resource
     */
    public void deleteComputeResource(String computeResourceId) {
        LOGGER.info("Deleting compute resource from app_catalog: {}", computeResourceId);
        
        try {
            if (!computeResourceRepository.existsById(computeResourceId)) {
                throw new RuntimeException("Compute resource not found with ID: " + computeResourceId);
            }
            
            computeResourceRepository.deleteById(computeResourceId);
            LOGGER.info("Deleted compute resource from app_catalog: {}", computeResourceId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete compute resource from app_catalog: {}", computeResourceId, e);
            throw new RuntimeException("Failed to delete compute resource", e);
        }
    }

    /**
     * Check if compute resource exists
     */
    public boolean existsComputeResource(String computeResourceId) {
        LOGGER.debug("Checking if compute resource exists in app_catalog: {}", computeResourceId);
        
        try {
            boolean exists = computeResourceRepository.existsById(computeResourceId);
            LOGGER.debug("Compute resource {} exists: {}", computeResourceId, exists);
            return exists;
        } catch (Exception e) {
            LOGGER.error("Failed to check compute resource existence in app_catalog: {}", computeResourceId, e);
            return false;
        }
    }
}