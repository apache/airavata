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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.mappers.DataProductMapper;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
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
        DataProductEntity entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null) return null;

        // Manual mapping to avoid session issues
        DataProductModel model = new DataProductModel();
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

        // Copy metadata to regular HashMap
        if (entity.getProductMetadata() != null && !entity.getProductMetadata().isEmpty()) {
            model.setProductMetadata(new HashMap<>(entity.getProductMetadata()));
        }

        // Map replica locations
        if (entity.getReplicaLocations() != null
                && !entity.getReplicaLocations().isEmpty()) {
            List<org.apache.airavata.common.model.DataReplicaLocationModel> replicaModels = new java.util.ArrayList<>();
            for (DataReplicaLocationEntity re : entity.getReplicaLocations()) {
                org.apache.airavata.common.model.DataReplicaLocationModel rm =
                        new org.apache.airavata.common.model.DataReplicaLocationModel();
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
        DataProductEntity entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null || entity.getParentProductUri() == null) return null;
        DataProductEntity parentEntity =
                dataProductRepository.findById(entity.getParentProductUri()).orElse(null);
        if (parentEntity == null) return null;

        // Process replica locations if any
        if (parentEntity.getReplicaLocations() != null) {
            for (DataReplicaLocationEntity replicaEntity : parentEntity.getReplicaLocations()) {
                Map<String, String> metadataCopy = new HashMap<>();
                if (replicaEntity.getReplicaMetadata() != null) {
                    try {
                        for (Map.Entry<String, String> entry :
                                replicaEntity.getReplicaMetadata().entrySet()) {
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
        List<DataProductEntity> entities = dataProductRepository.findByParentProductUri(productUri);
        return entities.stream()
                .map(e -> {
                    // Process replica locations if any
                    if (e.getReplicaLocations() != null) {
                        for (DataReplicaLocationEntity replicaEntity : e.getReplicaLocations()) {
                            Map<String, String> metadataCopy = new HashMap<>();
                            if (replicaEntity.getReplicaMetadata() != null) {
                                try {
                                    for (Map.Entry<String, String> entry :
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
        String searchPattern = "%" + productName + "%";
        List<DataProductEntity> entities =
                dataProductRepository.findByGatewayIdAndOwnerNameAndProductNameLike(gatewayId, userId, searchPattern);
        return entities.stream()
                .map(e -> {
                    // Process replica locations if any
                    if (e.getReplicaLocations() != null) {
                        for (DataReplicaLocationEntity replicaEntity : e.getReplicaLocations()) {
                            Map<String, String> metadataCopy = new HashMap<>();
                            if (replicaEntity.getReplicaMetadata() != null) {
                                try {
                                    for (Map.Entry<String, String> entry :
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
            String productUri = org.apache.airavata.common.utils.AiravataUtils.getId(
                    dataProductModel.getProductName() != null ? dataProductModel.getProductName() : "dataProduct");
            dataProductModel.setProductUri(productUri);
        }
        // Generate replicaIds for replicaLocations if not set
        if (dataProductModel.getReplicaLocations() != null) {
            for (org.apache.airavata.common.model.DataReplicaLocationModel replica :
                    dataProductModel.getReplicaLocations()) {
                if (replica.getReplicaId() == null || replica.getReplicaId().isEmpty()) {
                    String replicaId = org.apache.airavata.common.utils.AiravataUtils.getId(
                            replica.getReplicaName() != null ? replica.getReplicaName() : "replica");
                    replica.setReplicaId(replicaId);
                }
                // Set productUri if not set
                if (replica.getProductUri() == null || replica.getProductUri().isEmpty()) {
                    replica.setProductUri(dataProductModel.getProductUri());
                }
            }
        }
        DataProductEntity entity = dataProductMapper.toEntity(dataProductModel);
        // Ensure productUri is set on entity
        entity.setProductUri(dataProductModel.getProductUri());
        // Ensure dataProduct relationship is set on replica locations
        if (entity.getReplicaLocations() != null) {
            for (org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity replicaEntity :
                    entity.getReplicaLocations()) {
                if (replicaEntity.getProductUri() == null
                        || replicaEntity.getProductUri().isEmpty()) {
                    replicaEntity.setProductUri(dataProductModel.getProductUri());
                }
                // Set the dataProduct relationship (productUri is insertable=false, updatable=false)
                replicaEntity.setDataProduct(entity);
            }
        }
        DataProductEntity saved = dataProductRepository.save(entity);
        return saved.getProductUri();
    }

    public boolean isDataProductExists(String productUri) throws ReplicaCatalogException {
        return dataProductRepository.existsById(productUri);
    }

    public boolean updateDataProduct(DataProductModel dataProductModel) throws ReplicaCatalogException {
        // Fetch existing entity first to properly merge changes
        DataProductEntity existing =
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
            existing.setLastModifiedTime(new java.sql.Timestamp(dataProductModel.getLastModifiedTime()));
        }

        // Handle replica locations update - merge existing with new
        if (dataProductModel.getReplicaLocations() != null
                && !dataProductModel.getReplicaLocations().isEmpty()) {
            // Clear existing and add updated ones
            if (existing.getReplicaLocations() != null) {
                existing.getReplicaLocations().clear();
            } else {
                existing.setReplicaLocations(new java.util.ArrayList<>());
            }
            for (org.apache.airavata.common.model.DataReplicaLocationModel replicaModel :
                    dataProductModel.getReplicaLocations()) {
                org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity replicaEntity =
                        new org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity();
                replicaEntity.setReplicaId(replicaModel.getReplicaId());
                replicaEntity.setProductUri(dataProductModel.getProductUri());
                replicaEntity.setReplicaName(replicaModel.getReplicaName());
                replicaEntity.setReplicaDescription(replicaModel.getReplicaDescription());
                replicaEntity.setFilePath(replicaModel.getFilePath());
                replicaEntity.setStorageResourceId(replicaModel.getStorageResourceId());
                replicaEntity.setReplicaLocationCategory(replicaModel.getReplicaLocationCategory());
                replicaEntity.setReplicaPersistentType(replicaModel.getReplicaPersistentType());
                if (replicaModel.getCreationTime() > 0) {
                    replicaEntity.setCreationTime(new java.sql.Timestamp(replicaModel.getCreationTime()));
                }
                if (replicaModel.getLastModifiedTime() > 0) {
                    replicaEntity.setLastModifiedTime(new java.sql.Timestamp(replicaModel.getLastModifiedTime()));
                }
                if (replicaModel.getValidUntilTime() > 0) {
                    replicaEntity.setValidUntilTime(new java.sql.Timestamp(replicaModel.getValidUntilTime()));
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
}
