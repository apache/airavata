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
package org.apache.airavata.registry.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.CatalogResource;
import org.apache.airavata.common.model.CatalogResource.Tag;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.Privacy;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.ResourceScope;
import org.apache.airavata.registry.entities.catalog.CatalogResourceEntity.ResourceType;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.catalog.CatalogResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Research Catalog Resources.
 * 
 * <h2>Resource Scope Model</h2>
 * <p>Resources support a two-level scope model with inferred delegation:</p>
 * <ul>
 *   <li><b>USER</b>: Resources owned by a specific user (stored in DB with scope=USER, ownerId=userId)</li>
 *   <li><b>GATEWAY</b>: Resources owned at gateway level (stored in DB with scope=GATEWAY, ownerId=null)</li>
 *   <li><b>DELEGATED</b>: Resources accessible via group credentials but not directly owned (inferred at runtime)</li>
 * </ul>
 * 
 * <h3>Scope Inference</h3>
 * <p>The {@link #inferScope(CatalogResourceEntity, String, String, List)} method determines the effective scope:</p>
 * <ul>
 *   <li>If resource.scope=USER and resource.ownerId=userId → returns "USER"</li>
 *   <li>If resource.scope=GATEWAY and resource.gatewayId=gatewayId → returns "GATEWAY"</li>
 *   <li>If resource.groupResourceProfileId is in user's accessible groups AND not directly owned → returns "DELEGATED"</li>
 *   <li>Otherwise → returns the stored scope (USER or GATEWAY)</li>
 * </ul>
 * 
 * <h3>Resource Types</h3>
 * <p>Only two resource types are supported:</p>
 * <ul>
 *   <li><b>DATASET</b>: Data files and datasets</li>
 *   <li><b>REPOSITORY</b>: Code repositories, notebooks, models, or any version-controlled resource</li>
 * </ul>
 */
@Service
@Transactional
public class CatalogResourceService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CatalogResourceService.class);
    
    private final CatalogResourceRepository catalogResourceRepository;

    public CatalogResourceService(CatalogResourceRepository catalogResourceRepository) {
        this.catalogResourceRepository = catalogResourceRepository;
    }

    public List<CatalogResource> getPublicResources(String type, String nameSearch, int pageNumber, int pageSize)
            throws RegistryException {
        try {
            logger.debug("Getting public resources: type={}, nameSearch={}, pageNumber={}, pageSize={}", 
                type, nameSearch, pageNumber, pageSize);
            
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            ResourceType resourceType = null;
            if (type != null && !type.trim().isEmpty()) {
                try {
                    resourceType = ResourceType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid resource type: {}", type);
                    throw new RegistryException("Invalid resource type: " + type + ". Must be DATASET or REPOSITORY.");
                }
            }

            logger.debug("Querying repository with resourceType={}, nameSearch={}", resourceType, nameSearch);
            List<CatalogResourceEntity> entities =
                    catalogResourceRepository.findPublicWithFilters(resourceType, nameSearch, pageable);
            
            logger.debug("Found {} entities, converting to models", entities != null ? entities.size() : 0);
            
            if (entities == null || entities.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            
            List<CatalogResource> resources = entities.stream()
                    .map(this::toModel)
                    .collect(Collectors.toList());
            
            logger.debug("Successfully converted {} resources", resources.size());
            return resources;
        } catch (RegistryException e) {
            logger.error("Registry exception getting public resources: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving public resources: {}", e.getMessage(), e);
            throw new RegistryException("Error retrieving public resources: " + e.getMessage(), e);
        }
    }

    public List<CatalogResource> getAllResources(String type, int pageNumber, int pageSize) throws RegistryException {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        ResourceType resourceType = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                resourceType = ResourceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid resource type: {}", type);
                resourceType = null;
            }
        }

        List<CatalogResourceEntity> entities;
        if (resourceType != null) {
            entities = catalogResourceRepository.findByPrivacyAndType(Privacy.PUBLIC, resourceType, pageable);
        } else {
            entities = catalogResourceRepository.findAll(pageable).getContent();
        }

        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public CatalogResource getResource(String resourceId) throws RegistryException {
        CatalogResourceEntity entity =
                catalogResourceRepository.findById(resourceId).orElse(null);
        if (entity == null) {
            return null;
        }
        return toModel(entity);
    }

    public CatalogResource getPublicResource(String resourceId) throws RegistryException {
        CatalogResourceEntity entity =
                catalogResourceRepository.findById(resourceId).orElse(null);
        if (entity == null || entity.getPrivacy() != Privacy.PUBLIC) {
            return null;
        }
        return toModel(entity);
    }

    public List<CatalogResource> searchResources(String query, String type) throws RegistryException {
        List<CatalogResourceEntity> entities = catalogResourceRepository.searchByName(Privacy.PUBLIC, query);

        if (type != null) {
            try {
                ResourceType resourceType = ResourceType.valueOf(type.toUpperCase());
                entities = entities.stream()
                        .filter(e -> e.getType() == resourceType)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid resource type: {}", type);
                // Return empty list for invalid type
                return java.util.Collections.emptyList();
            }
        }

        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<Tag> getAllPublicTags() throws RegistryException {
        try {
            logger.debug("Getting all public tags");
            List<String> tagNames = catalogResourceRepository.findAllPublicTags();
            logger.debug("Found {} tag names from repository", tagNames != null ? tagNames.size() : 0);
            
            if (tagNames == null || tagNames.isEmpty()) {
                logger.debug("No tags found, returning empty list");
                return java.util.Collections.emptyList();
            }
            
            List<Tag> tags = tagNames.stream()
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .map(name -> {
                        Tag tag = new Tag();
                        tag.setId(name.toLowerCase().replace(" ", "-"));
                        tag.setName(name);
                        return tag;
                    })
                    .collect(Collectors.toList());
            
            logger.debug("Successfully converted {} tags", tags.size());
            return tags;
        } catch (Exception e) {
            logger.error("Error retrieving public tags: {}", e.getMessage(), e);
            // Return empty list instead of throwing exception to prevent page from hanging
            logger.warn("Returning empty tag list due to error");
            return java.util.Collections.emptyList();
        }
    }

    public String createResource(CatalogResource resource) throws RegistryException {
        CatalogResourceEntity entity = toEntity(resource);

        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(UUID.randomUUID().toString());
        }

        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        CatalogResourceEntity saved = catalogResourceRepository.save(entity);
        return saved.getId();
    }

    public void updateResource(String resourceId, CatalogResource resource) throws RegistryException {
        CatalogResourceEntity existing =
                catalogResourceRepository.findById(resourceId).orElse(null);
        if (existing == null) {
            throw new RegistryException("Resource not found: " + resourceId);
        }

        // Update fields
        if (resource.getName() != null) existing.setName(resource.getName());
        if (resource.getDescription() != null) existing.setDescription(resource.getDescription());
        if (resource.getStatus() != null)
            existing.setStatus(CatalogResourceEntity.ResourceStatus.valueOf(resource.getStatus()));
        if (resource.getPrivacy() != null) existing.setPrivacy(Privacy.valueOf(resource.getPrivacy()));
        if (resource.getScope() != null) {
            try {
                existing.setScope(ResourceScope.valueOf(resource.getScope()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid scope: {}", resource.getScope());
            }
        }
        if (resource.getGroupResourceProfileId() != null) existing.setGroupResourceProfileId(resource.getGroupResourceProfileId());
        if (resource.getHeaderImage() != null) existing.setHeaderImage(resource.getHeaderImage());
        if (resource.getAuthors() != null) existing.setAuthors(resource.getAuthors());
        if (resource.getTags() != null) {
            existing.setTags(
                    resource.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }

        // Type-specific fields
        if (resource.getNotebookPath() != null) existing.setNotebookPath(resource.getNotebookPath());
        if (resource.getJupyterServerUrl() != null) existing.setJupyterServerUrl(resource.getJupyterServerUrl());
        if (resource.getDatasetUrl() != null) existing.setDatasetUrl(resource.getDatasetUrl());
        if (resource.getSize() != null) existing.setDatasetSize(resource.getSize());
        if (resource.getFormat() != null) existing.setDatasetFormat(resource.getFormat());
        if (resource.getRepositoryUrl() != null) existing.setRepositoryUrl(resource.getRepositoryUrl());
        if (resource.getBranch() != null) existing.setRepositoryBranch(resource.getBranch());
        if (resource.getCommit() != null) existing.setRepositoryCommit(resource.getCommit());
        if (resource.getModelUrl() != null) existing.setModelUrl(resource.getModelUrl());
        if (resource.getApplicationInterfaceId() != null)
            existing.setApplicationInterfaceId(resource.getApplicationInterfaceId());
        if (resource.getFramework() != null) existing.setModelFramework(resource.getFramework());

        existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        catalogResourceRepository.save(existing);
    }

    public void deleteResource(String resourceId) throws RegistryException {
        catalogResourceRepository.deleteById(resourceId);
    }

    /**
     * Get all accessible resources for a user
     * Includes: user-level, gateway-level, and delegated (group-accessible) resources
     * DELEGATED scope is inferred for resources accessible via groups but not directly owned
     */
    public List<CatalogResource> getAccessibleResources(
            String userId, String gatewayId, List<String> groupIds, String type, String nameSearch,
            int pageNumber, int pageSize) throws RegistryException {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            ResourceType resourceType = null;
            if (type != null && !type.trim().isEmpty()) {
                try {
                    resourceType = ResourceType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid resource type: {}", type);
                    resourceType = null;
                }
            }

            List<CatalogResourceEntity> entities;
            if (nameSearch != null && !nameSearch.trim().isEmpty()) {
                entities = catalogResourceRepository.findAccessibleResourcesWithFilters(
                        userId, gatewayId, groupIds != null ? groupIds : java.util.Collections.emptyList(),
                        resourceType, nameSearch, pageable);
            } else if (resourceType != null) {
                entities = catalogResourceRepository.findAccessibleResourcesByType(
                        userId, gatewayId, groupIds != null ? groupIds : java.util.Collections.emptyList(),
                        resourceType, pageable);
            } else {
                entities = catalogResourceRepository.findAccessibleResources(
                        userId, gatewayId, groupIds != null ? groupIds : java.util.Collections.emptyList(),
                        pageable);
            }

            // Convert to models and infer DELEGATED scope
            return entities.stream()
                    .map(entity -> toModelWithDelegatedScope(entity, userId, gatewayId, groupIds))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving accessible resources: {}", e.getMessage(), e);
            throw new RegistryException("Error retrieving accessible resources: " + e.getMessage(), e);
        }
    }

    private CatalogResource toModel(CatalogResourceEntity entity) {
        return toModelWithDelegatedScope(entity, null, null, null);
    }

    /**
     * Convert entity to model, inferring DELEGATED scope when appropriate
     * DELEGATED is inferred for resources accessible via groups but not directly owned by user or gateway
     */
    private CatalogResource toModelWithDelegatedScope(
            CatalogResourceEntity entity, String userId, String gatewayId, List<String> groupIds) {
        CatalogResource model = new CatalogResource();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        ResourceType type = entity.getType();
        if (type == null) {
            // Default to DATASET if type is null
            type = ResourceType.DATASET;
        }
        model.setType(type.name());
        model.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "NONE");
        model.setPrivacy(entity.getPrivacy() != null ? entity.getPrivacy().name() : "PRIVATE");
        
        // Infer scope: USER, GATEWAY, or DELEGATED
        String inferredScope = inferScope(entity, userId, gatewayId, groupIds);
        model.setScope(inferredScope);
        
        model.setGatewayId(entity.getGatewayId());
        model.setOwnerId(entity.getOwnerId());
        model.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        model.setHeaderImage(entity.getHeaderImage());
        model.setAuthors(entity.getAuthors());

        // Convert tag strings to Tag objects
        if (entity.getTags() != null) {
            model.setTags(entity.getTags().stream()
                    .map(name -> {
                        Tag tag = new Tag();
                        tag.setId(name.toLowerCase().replace(" ", "-"));
                        tag.setName(name);
                        return tag;
                    })
                    .collect(Collectors.toList()));
        }

        model.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0);
        model.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0);

        // Type-specific fields
        model.setNotebookPath(entity.getNotebookPath());
        model.setJupyterServerUrl(entity.getJupyterServerUrl());
        model.setDatasetUrl(entity.getDatasetUrl());
        model.setSize(entity.getDatasetSize());
        model.setFormat(entity.getDatasetFormat());
        model.setRepositoryUrl(entity.getRepositoryUrl());
        model.setBranch(entity.getRepositoryBranch());
        model.setCommit(entity.getRepositoryCommit());
        model.setModelUrl(entity.getModelUrl());
        model.setApplicationInterfaceId(entity.getApplicationInterfaceId());
        model.setFramework(entity.getModelFramework());

        return model;
    }

    /**
     * Infer the effective scope of a resource
     * - USER: if scope is USER and ownerId matches userId (directly owned by user)
     * - GATEWAY: if scope is GATEWAY and gatewayId matches (directly owned by gateway)
     * - DELEGATED: if accessible via group but not directly owned by user or gateway
     */
    private String inferScope(CatalogResourceEntity entity, String userId, String gatewayId, List<String> groupIds) {
        ResourceScope dbScope = entity.getScope() != null ? entity.getScope() : ResourceScope.USER;
        
        // If no context provided, return the DB scope
        if (userId == null && gatewayId == null) {
            return dbScope.name();
        }
        
        boolean isDirectlyOwnedByUser = dbScope == ResourceScope.USER 
                && userId != null 
                && userId.equals(entity.getOwnerId());
        
        boolean isDirectlyOwnedByGateway = dbScope == ResourceScope.GATEWAY 
                && gatewayId != null 
                && gatewayId.equals(entity.getGatewayId());
        
        // If directly owned, return the ownership scope
        if (isDirectlyOwnedByUser) {
            return "USER";
        }
        if (isDirectlyOwnedByGateway) {
            return "GATEWAY";
        }
        
        // Check if accessible via group delegation (and not directly owned)
        boolean isAccessibleViaGroup = groupIds != null 
                && !groupIds.isEmpty() 
                && entity.getGroupResourceProfileId() != null
                && groupIds.contains(entity.getGroupResourceProfileId());
        
        if (isAccessibleViaGroup) {
            return "DELEGATED";
        }
        
        // Default to DB scope if no delegation detected
        return dbScope.name();
    }

    private CatalogResourceEntity toEntity(CatalogResource model) {
        CatalogResourceEntity entity = new CatalogResourceEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        String typeStr = model.getType();
        ResourceType resourceType;
        if (typeStr == null || typeStr.isEmpty()) {
            resourceType = ResourceType.DATASET;
        } else {
            try {
                resourceType = ResourceType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid resource type: {}, defaulting to DATASET", typeStr);
                resourceType = ResourceType.DATASET;
            }
        }
        entity.setType(resourceType);
        entity.setStatus(model.getStatus() != null
                ? CatalogResourceEntity.ResourceStatus.valueOf(model.getStatus())
                : CatalogResourceEntity.ResourceStatus.NONE);
        entity.setPrivacy(model.getPrivacy() != null ? Privacy.valueOf(model.getPrivacy()) : Privacy.PRIVATE);
        // Only USER and GATEWAY are stored in DB, DELEGATED is inferred at runtime
        if (model.getScope() != null && !"DELEGATED".equals(model.getScope())) {
            try {
                ResourceScope scope = ResourceScope.valueOf(model.getScope());
                entity.setScope(scope);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid scope: {}, defaulting to USER. Only USER and GATEWAY are valid.", model.getScope());
                entity.setScope(ResourceScope.USER);
            }
        } else {
            // Default to USER if scope is null or DELEGATED (DELEGATED cannot be stored)
            entity.setScope(ResourceScope.USER);
        }
        entity.setGatewayId(model.getGatewayId());
        entity.setOwnerId(model.getOwnerId());
        entity.setGroupResourceProfileId(model.getGroupResourceProfileId());
        entity.setHeaderImage(model.getHeaderImage());
        entity.setAuthors(model.getAuthors());

        // Convert Tag objects to tag strings
        if (model.getTags() != null) {
            entity.setTags(
                    model.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }

        // Type-specific fields
        entity.setNotebookPath(model.getNotebookPath());
        entity.setJupyterServerUrl(model.getJupyterServerUrl());
        entity.setDatasetUrl(model.getDatasetUrl());
        entity.setDatasetSize(model.getSize());
        entity.setDatasetFormat(model.getFormat());
        entity.setRepositoryUrl(model.getRepositoryUrl());
        entity.setRepositoryBranch(model.getBranch());
        entity.setRepositoryCommit(model.getCommit());
        entity.setModelUrl(model.getModelUrl());
        entity.setApplicationInterfaceId(model.getApplicationInterfaceId());
        entity.setModelFramework(model.getFramework());

        return entity;
    }
}
