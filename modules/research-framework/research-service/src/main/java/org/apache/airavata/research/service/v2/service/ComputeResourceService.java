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
package org.apache.airavata.research.service.v2.service;

import java.util.Optional;
import org.apache.airavata.research.service.v2.entity.ComputeResource;
import org.apache.airavata.research.service.v2.repository.ComputeResourceRepository;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ComputeResourceService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResourceService.class);
    
    private final ComputeResourceRepository computeResourceRepository;
    
    public ComputeResourceService(ComputeResourceRepository computeResourceRepository) {
        this.computeResourceRepository = computeResourceRepository;
    }
    
    /**
     * Get paginated compute resources with lazy collections properly initialized
     */
    @Transactional(readOnly = true)
    public Page<ComputeResource> getComputeResources(PrivacyEnum privacy, StateEnum state, Pageable pageable) {
        LOGGER.debug("Fetching compute resources - privacy: {}, state: {}", privacy, state);
        
        Page<ComputeResource> resources = computeResourceRepository.findByPrivacyAndState(privacy, state, pageable);
        
        // Initialize lazy collections within transaction context
        resources.getContent().forEach(this::initializeLazyCollections);
        
        return resources;
    }
    
    /**
     * Search compute resources with lazy collections properly initialized
     */
    @Transactional(readOnly = true)
    public Page<ComputeResource> searchComputeResources(String nameSearch, PrivacyEnum privacy, StateEnum state, Pageable pageable) {
        LOGGER.debug("Searching compute resources - search: {}, privacy: {}, state: {}", nameSearch, privacy, state);
        
        Page<ComputeResource> resources = computeResourceRepository.findByNameSearchAndPrivacyAndState(nameSearch, privacy, state, pageable);
        
        // Initialize lazy collections within transaction context
        resources.getContent().forEach(this::initializeLazyCollections);
        
        return resources;
    }
    
    /**
     * Get compute resource by ID with lazy collections properly initialized
     */
    @Transactional(readOnly = true)
    public Optional<ComputeResource> getComputeResourceById(String id) {
        LOGGER.debug("Fetching compute resource by ID: {}", id);
        
        Optional<ComputeResource> resource = computeResourceRepository.findById(id);
        
        // Initialize lazy collections if resource exists
        resource.ifPresent(this::initializeLazyCollections);
        
        return resource;
    }
    
    /**
     * Create new compute resource
     */
    public ComputeResource createComputeResource(ComputeResource computeResource) {
        LOGGER.debug("Creating compute resource: {}", computeResource.getName());
        
        // Set any business logic defaults here if needed
        ComputeResource savedResource = computeResourceRepository.save(computeResource);
        
        // Initialize collections for return
        initializeLazyCollections(savedResource);
        
        return savedResource;
    }
    
    /**
     * Update existing compute resource
     */
    public Optional<ComputeResource> updateComputeResource(String id, ComputeResource updatedResource) {
        LOGGER.debug("Updating compute resource ID: {}", id);
        
        Optional<ComputeResource> existingResource = computeResourceRepository.findById(id);
        
        if (existingResource.isPresent()) {
            ComputeResource resource = existingResource.get();
            
            // Update fields
            resource.setName(updatedResource.getName());
            resource.setDescription(updatedResource.getDescription());
            resource.setHostname(updatedResource.getHostname());
            resource.setComputeType(updatedResource.getComputeType());
            resource.setCpuCores(updatedResource.getCpuCores());
            resource.setMemoryGB(updatedResource.getMemoryGB());
            resource.setOperatingSystem(updatedResource.getOperatingSystem());
            resource.setQueueSystem(updatedResource.getQueueSystem());
            resource.setResourceManager(updatedResource.getResourceManager());
            resource.setAdditionalInfo(updatedResource.getAdditionalInfo());
            
            // Update new fields
            resource.setHostAliases(updatedResource.getHostAliases());
            resource.setIpAddresses(updatedResource.getIpAddresses());
            resource.setSshUsername(updatedResource.getSshUsername());
            resource.setSshPort(updatedResource.getSshPort());
            resource.setAuthenticationMethod(updatedResource.getAuthenticationMethod());
            resource.setSshKey(updatedResource.getSshKey());
            resource.setWorkingDirectory(updatedResource.getWorkingDirectory());
            resource.setSchedulerType(updatedResource.getSchedulerType());
            resource.setDataMovementProtocol(updatedResource.getDataMovementProtocol());
            resource.setQueues(updatedResource.getQueues());
            
            ComputeResource savedResource = computeResourceRepository.save(resource);
            
            // Initialize collections for return
            initializeLazyCollections(savedResource);
            
            return Optional.of(savedResource);
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete compute resource
     */
    public boolean deleteComputeResource(String id) {
        LOGGER.debug("Deleting compute resource ID: {}", id);
        
        if (computeResourceRepository.existsById(id)) {
            computeResourceRepository.deleteById(id);
            return true;
        }
        
        return false;
    }
    
    /**
     * Initialize lazy collections within transaction context to avoid LazyInitializationException
     */
    private void initializeLazyCollections(ComputeResource resource) {
        try {
            // Force initialization of lazy collections
            if (resource.getHostAliases() != null) {
                resource.getHostAliases().size(); // Trigger lazy loading
            }
            if (resource.getIpAddresses() != null) {
                resource.getIpAddresses().size(); // Trigger lazy loading
            }
            if (resource.getQueues() != null) {
                resource.getQueues().size(); // Trigger lazy loading
                // Also initialize queue details if needed
                resource.getQueues().forEach(queue -> {
                    // Access queue properties to ensure they're loaded
                    queue.getQueueName();
                });
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize lazy collections for compute resource {}: {}", resource.getId(), e.getMessage());
        }
    }
}