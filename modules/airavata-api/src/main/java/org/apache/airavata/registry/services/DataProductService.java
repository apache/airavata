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

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.ReplicaCatalogException;
import org.apache.airavata.registry.mappers.DataProductMapper;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataProductService {
    private final DataProductRepository dataProductRepository;
    private final EntityManager entityManager;
    private final DataProductMapper dataProductMapper;

    public DataProductService(
            DataProductRepository dataProductRepository,
            EntityManager entityManager,
            DataProductMapper dataProductMapper) {
        this.dataProductRepository = dataProductRepository;
        this.entityManager = entityManager;
        this.dataProductMapper = dataProductMapper;
    }

    @Transactional(readOnly = true)
    public DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException {
        var entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null) return null;

        // Manual mapping to avoid session issues
        var model = new DataProductModel();
        model.setProductUri(entity.getProductUri());
        model.setGatewayId(entity.getGatewayId());
        model.setOwnerName(entity.getOwnerName());
        model.setProductName(entity.getProductName());
        model.setProductDescription(entity.getProductDescription());
        model.setProductSize(entity.getProductSize());
        model.setDataProductType(entity.getDataProductType());
        model.setParentProductUri(entity.getParentProductUri());
        if (entity.getCreationTime() != null) {
            model.setCreationTime(entity.getCreationTime().getTime());
        }
        if (entity.getLastModifiedTime() != null) {
            model.setLastModifiedTime(entity.getLastModifiedTime().getTime());
        }
        if (entity.getUpdatedAt() != null) {
            model.setUpdatedAt(entity.getUpdatedAt().getTime());
        }

        model.setPrimaryStorageResourceId(entity.getPrimaryStorageResourceId());
        model.setPrimaryFilePath(entity.getPrimaryFilePath());
        model.setOwnerId(entity.getOwnerId());
        model.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        model.setHeaderImage(entity.getHeaderImage());
        model.setFormat(entity.getFormat());
        if (entity.getStatus() != null) {
            model.setStatus(entity.getStatus().name());
        }
        if (entity.getPrivacy() != null) {
            model.setPrivacy(entity.getPrivacy().name());
        }
        if (entity.getResourceScope() != null) {
            model.setScope(entity.getResourceScope().name());
        }
        if (entity.getAuthors() != null && !entity.getAuthors().isEmpty()) {
            model.setAuthors(new ArrayList<>(entity.getAuthors()));
        }
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            var tagList = entity.getTags().stream()
                    .map(s -> {
                        var t = new DataProductModel.Tag();
                        t.setId(s);
                        t.setName(s);
                        return t;
                    })
                    .collect(Collectors.toList());
            model.setTags(tagList);
        }

        // Copy metadata to regular HashMap
        if (entity.getProductMetadata() != null && !entity.getProductMetadata().isEmpty()) {
            model.setProductMetadata(new HashMap<>(entity.getProductMetadata()));
        }

        // Map replica locations
        if (entity.getReplicaLocations() != null
                && !entity.getReplicaLocations().isEmpty()) {
            var replicaModels = new ArrayList<DataReplicaLocationModel>();
            for (var re : entity.getReplicaLocations()) {
                var rm = new DataReplicaLocationModel();
                rm.setReplicaId(re.getReplicaId());
                rm.setProductUri(productUri);
                rm.setReplicaName(re.getReplicaName());
                rm.setReplicaDescription(re.getReplicaDescription());
                rm.setStorageResourceId(re.getStorageResourceId());
                rm.setFilePath(re.getFilePath());
                rm.setReplicaLocationCategory(re.getReplicaLocationCategory());
                rm.setReplicaPersistentType(re.getReplicaPersistentType());
                if (re.getCreationTime() != null) {
                    rm.setCreationTime(re.getCreationTime().getTime());
                }
                if (re.getLastModifiedTime() != null) {
                    rm.setLastModifiedTime(re.getLastModifiedTime().getTime());
                }
                if (re.getValidUntilTime() != null) {
                    rm.setValidUntilTime(re.getValidUntilTime().getTime());
                }
                if (re.getReplicaMetadata() != null && !re.getReplicaMetadata().isEmpty()) {
                    rm.setReplicaMetadata(new HashMap<>(re.getReplicaMetadata()));
                }
                replicaModels.add(rm);
            }
            model.setReplicaLocations(replicaModels);
        }

        return model;
    }

    @Transactional(readOnly = true)
    public DataProductModel getParentDataProduct(String productUri) throws ReplicaCatalogException {
        var entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null || entity.getParentProductUri() == null) return null;
        var parentEntity =
                dataProductRepository.findById(entity.getParentProductUri()).orElse(null);
        if (parentEntity == null) return null;

        // Process replica locations if any
        if (parentEntity.getReplicaLocations() != null) {
            for (var replicaEntity : parentEntity.getReplicaLocations()) {
                var metadataCopy = new HashMap<String, String>();
                if (replicaEntity.getReplicaMetadata() != null) {
                    try {
                        for (var entry : replicaEntity.getReplicaMetadata().entrySet()) {
                            metadataCopy.put(entry.getKey(), entry.getValue());
                        }
                    } catch (Exception e) {
                        metadataCopy = new HashMap<>();
                    }
                }
                entityManager.detach(replicaEntity);
                replicaEntity.setReplicaMetadata(new HashMap<>(metadataCopy));
            }
        }
        entityManager.detach(parentEntity);

        return dataProductMapper.toModel(parentEntity);
    }

    @Transactional(readOnly = true)
    public List<DataProductModel> getChildDataProducts(String productUri) throws ReplicaCatalogException {
        var entities = dataProductRepository.findByParentProductUri(productUri);
        return entities.stream()
                .map(e -> {
                    // Process replica locations if any
                    if (e.getReplicaLocations() != null) {
                        for (var replicaEntity : e.getReplicaLocations()) {
                            var metadataCopy = new HashMap<String, String>();
                            if (replicaEntity.getReplicaMetadata() != null) {
                                try {
                                    for (var entry :
                                            replicaEntity.getReplicaMetadata().entrySet()) {
                                        metadataCopy.put(entry.getKey(), entry.getValue());
                                    }
                                } catch (Exception ex) {
                                    metadataCopy = new HashMap<>();
                                }
                            }
                            entityManager.detach(replicaEntity);
                            replicaEntity.setReplicaMetadata(new HashMap<>(metadataCopy));
                        }
                    }
                    entityManager.detach(e);
                    return dataProductMapper.toModel(e);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws ReplicaCatalogException {
        var searchPattern = "%" + productName + "%";
        var entities =
                dataProductRepository.findByGatewayIdAndOwnerNameAndProductNameLike(gatewayId, userId, searchPattern);
        return entities.stream()
                .map(e -> {
                    // Process replica locations if any
                    if (e.getReplicaLocations() != null) {
                        for (var replicaEntity : e.getReplicaLocations()) {
                            var metadataCopy = new HashMap<String, String>();
                            if (replicaEntity.getReplicaMetadata() != null) {
                                try {
                                    for (var entry :
                                            replicaEntity.getReplicaMetadata().entrySet()) {
                                        metadataCopy.put(entry.getKey(), entry.getValue());
                                    }
                                } catch (Exception ex) {
                                    metadataCopy = new HashMap<>();
                                }
                            }
                            entityManager.detach(replicaEntity);
                            replicaEntity.setReplicaMetadata(new HashMap<>(metadataCopy));
                        }
                    }
                    entityManager.detach(e);
                    return dataProductMapper.toModel(e);
                })
                .collect(Collectors.toList());
    }

    public String registerDataProduct(DataProductModel dataProductModel) throws ReplicaCatalogException {
        // Generate productUri if not set
        if (dataProductModel.getProductUri() == null
                || dataProductModel.getProductUri().isEmpty()) {
            var productUri = AiravataUtils.getId(
                    dataProductModel.getProductName() != null ? dataProductModel.getProductName() : "dataProduct");
            dataProductModel.setProductUri(productUri);
        }
        var now = System.currentTimeMillis();
        if (dataProductModel.getUpdatedAt() == 0) {
            dataProductModel.setUpdatedAt(now);
        }
        // Generate replicaIds for replicaLocations if not set
        if (dataProductModel.getReplicaLocations() != null) {
            for (var replica : dataProductModel.getReplicaLocations()) {
                if (replica.getReplicaId() == null || replica.getReplicaId().isEmpty()) {
                    var replicaId = AiravataUtils.getId(
                            replica.getReplicaName() != null ? replica.getReplicaName() : "replica");
                    replica.setReplicaId(replicaId);
                }
                // Set productUri if not set
                if (replica.getProductUri() == null || replica.getProductUri().isEmpty()) {
                    replica.setProductUri(dataProductModel.getProductUri());
                }
            }
        }
        var entity = dataProductMapper.toEntity(dataProductModel);
        // Ensure productUri is set on entity
        entity.setProductUri(dataProductModel.getProductUri());
        // Ensure dataProduct relationship is set on replica locations
        if (entity.getReplicaLocations() != null) {
            for (var replicaEntity : entity.getReplicaLocations()) {
                if (replicaEntity.getProductUri() == null
                        || replicaEntity.getProductUri().isEmpty()) {
                    replicaEntity.setProductUri(dataProductModel.getProductUri());
                }
                // Set the dataProduct relationship (productUri is insertable=false, updatable=false)
                replicaEntity.setDataProduct(entity);
            }
        }
        var saved = dataProductRepository.save(entity);
        return saved.getProductUri();
    }

    public boolean isDataProductExists(String productUri) throws ReplicaCatalogException {
        return dataProductRepository.existsById(productUri);
    }

    public boolean updateDataProduct(DataProductModel dataProductModel) throws ReplicaCatalogException {
        // Fetch existing entity first to properly merge changes
        var existing =
                dataProductRepository.findById(dataProductModel.getProductUri()).orElse(null);
        if (existing == null) {
            return false;
        }

        // Update fields from the model
        if (dataProductModel.getProductName() != null) {
            existing.setProductName(dataProductModel.getProductName());
        }
        if (dataProductModel.getProductDescription() != null) {
            existing.setProductDescription(dataProductModel.getProductDescription());
        }
        if (dataProductModel.getOwnerName() != null) {
            existing.setOwnerName(dataProductModel.getOwnerName());
        }
        if (dataProductModel.getParentProductUri() != null) {
            existing.setParentProductUri(dataProductModel.getParentProductUri());
        }
        if (dataProductModel.getProductSize() > 0) {
            existing.setProductSize(dataProductModel.getProductSize());
        }
        if (dataProductModel.getDataProductType() != null) {
            existing.setDataProductType(dataProductModel.getDataProductType());
        }
        if (dataProductModel.getProductMetadata() != null) {
            existing.setProductMetadata(dataProductModel.getProductMetadata());
        }
        if (dataProductModel.getLastModifiedTime() > 0) {
            existing.setLastModifiedTime(new Timestamp(dataProductModel.getLastModifiedTime()));
        }
        if (dataProductModel.getUpdatedAt() > 0) {
            existing.setUpdatedAt(new Timestamp(dataProductModel.getUpdatedAt()));
        }

        if (dataProductModel.getPrimaryStorageResourceId() != null) {
            existing.setPrimaryStorageResourceId(dataProductModel.getPrimaryStorageResourceId());
        }
        if (dataProductModel.getPrimaryFilePath() != null) {
            existing.setPrimaryFilePath(dataProductModel.getPrimaryFilePath());
        }
        if (dataProductModel.getStatus() != null) {
            try {
                existing.setStatus(DataProductEntity.ResourceStatus.valueOf(dataProductModel.getStatus()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dataProductModel.getPrivacy() != null) {
            try {
                existing.setPrivacy(DataProductEntity.Privacy.valueOf(dataProductModel.getPrivacy()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dataProductModel.getScope() != null) {
            try {
                existing.setResourceScope(DataProductEntity.ResourceScope.valueOf(dataProductModel.getScope()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dataProductModel.getOwnerId() != null) {
            existing.setOwnerId(dataProductModel.getOwnerId());
        }
        if (dataProductModel.getGroupResourceProfileId() != null) {
            existing.setGroupResourceProfileId(dataProductModel.getGroupResourceProfileId());
        }
        if (dataProductModel.getHeaderImage() != null) {
            existing.setHeaderImage(dataProductModel.getHeaderImage());
        }
        if (dataProductModel.getFormat() != null) {
            existing.setFormat(dataProductModel.getFormat());
        }
        if (dataProductModel.getAuthors() != null) {
            existing.setAuthors(new ArrayList<>(dataProductModel.getAuthors()));
        }
        if (dataProductModel.getTags() != null) {
            var tagStrings = dataProductModel.getTags().stream()
                    .map(t -> t.getId() != null ? t.getId() : t.getName())
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.toList());
            existing.setTags(tagStrings);
        }

        // Handle replica locations update - merge existing with new
        if (dataProductModel.getReplicaLocations() != null
                && !dataProductModel.getReplicaLocations().isEmpty()) {
            // Clear existing and add updated ones
            if (existing.getReplicaLocations() != null) {
                existing.getReplicaLocations().clear();
            } else {
                existing.setReplicaLocations(new ArrayList<>());
            }
            for (var replicaModel : dataProductModel.getReplicaLocations()) {
                var replicaEntity = new DataReplicaLocationEntity();
                replicaEntity.setReplicaId(replicaModel.getReplicaId());
                replicaEntity.setProductUri(dataProductModel.getProductUri());
                replicaEntity.setReplicaName(replicaModel.getReplicaName());
                replicaEntity.setReplicaDescription(replicaModel.getReplicaDescription());
                replicaEntity.setFilePath(replicaModel.getFilePath());
                replicaEntity.setStorageResourceId(replicaModel.getStorageResourceId());
                replicaEntity.setReplicaLocationCategory(replicaModel.getReplicaLocationCategory());
                replicaEntity.setReplicaPersistentType(replicaModel.getReplicaPersistentType());
                if (replicaModel.getCreationTime() > 0) {
                    replicaEntity.setCreationTime(new Timestamp(replicaModel.getCreationTime()));
                }
                if (replicaModel.getLastModifiedTime() > 0) {
                    replicaEntity.setLastModifiedTime(new Timestamp(replicaModel.getLastModifiedTime()));
                }
                if (replicaModel.getValidUntilTime() > 0) {
                    replicaEntity.setValidUntilTime(new Timestamp(replicaModel.getValidUntilTime()));
                }
                replicaEntity.setReplicaMetadata(replicaModel.getReplicaMetadata());
                replicaEntity.setDataProduct(existing);
                existing.getReplicaLocations().add(replicaEntity);
            }
        }

        dataProductRepository.save(existing);
        entityManager.flush(); // Ensure changes are persisted
        return true;
    }

    public void removeDataProduct(String productUri) throws ReplicaCatalogException {
        dataProductRepository.deleteById(productUri);
    }

    /**
     * Get public data products with optional name search.
     */
    @Transactional(readOnly = true)
    public List<DataProductModel> getPublicDataProducts(String nameSearch, int pageNumber, int pageSize)
            throws ReplicaCatalogException {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var entities = dataProductRepository.findPublicWithFilters(
                DataProductEntity.Privacy.PUBLIC.name(), nameSearch, pageable);
        return entities.stream().map(this::entityToModelDetached).collect(Collectors.toList());
    }

    /**
     * Get data products accessible to the user (owned, gateway, or via groups).
     * Scope (USER/GATEWAY/DELEGATED) is inferred and set on each returned model.
     */
    @Transactional(readOnly = true)
    public List<DataProductModel> getAccessibleDataProducts(
            String userId, String gatewayId, List<String> groupIds, String nameSearch, int pageNumber, int pageSize)
            throws ReplicaCatalogException {
        var safeGroupIds = groupIds != null ? groupIds : new ArrayList<String>();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        var entities = dataProductRepository.findAccessibleResourcesWithFilters(
                userId, gatewayId, safeGroupIds,
                DataProductEntity.ResourceScope.USER.name(),
                DataProductEntity.ResourceScope.GATEWAY.name(),
                nameSearch,
                pageable);
        return entities.stream()
                .map(e -> {
                    var model = entityToModelDetached(e);
                    model.setScope(inferScope(e, userId, gatewayId, groupIds));
                    return model;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct tag values from public data products.
     */
    @Transactional(readOnly = true)
    public List<String> getPublicTags() throws ReplicaCatalogException {
        return dataProductRepository.findAllPublicTags();
    }

    /**
     * Infer effective scope (USER, GATEWAY, or DELEGATED) for a data product entity.
     */
    public String inferScope(DataProductEntity entity, String userId, String gatewayId, List<String> userGroupIds) {
        if (entity.getResourceScope() == DataProductEntity.ResourceScope.USER && userId != null
                && userId.equals(entity.getOwnerId())) {
            return "USER";
        }
        if (entity.getResourceScope() == DataProductEntity.ResourceScope.GATEWAY && gatewayId != null
                && gatewayId.equals(entity.getGatewayId())) {
            return "GATEWAY";
        }
        if (entity.getGroupResourceProfileId() != null && userGroupIds != null
                && userGroupIds.contains(entity.getGroupResourceProfileId())) {
            return "DELEGATED";
        }
        return entity.getResourceScope() != null ? entity.getResourceScope().name() : "USER";
    }

    private DataProductModel entityToModelDetached(DataProductEntity e) {
        if (e.getReplicaLocations() != null) {
            for (var replicaEntity : e.getReplicaLocations()) {
                var metadataCopy = new HashMap<String, String>();
                if (replicaEntity.getReplicaMetadata() != null) {
                    metadataCopy.putAll(replicaEntity.getReplicaMetadata());
                }
                entityManager.detach(replicaEntity);
                replicaEntity.setReplicaMetadata(metadataCopy);
            }
        }
        entityManager.detach(e);
        return dataProductMapper.toModel(e);
    }
}
