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

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("replicaCatalogTransactionManager")
public class DataProductService {
    private final DataProductRepository dataProductRepository;
    private final EntityManager entityManager;
    private final Mapper mapper;

    public DataProductService(
            DataProductRepository dataProductRepository,
            @Qualifier("replicaCatalogEntityManager") EntityManager entityManager,
            Mapper mapper) {
        this.dataProductRepository = dataProductRepository;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
    public DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException {
        DataProductEntity entity = dataProductRepository.findById(productUri).orElse(null);
        if (entity == null) return null;

        // Process replica locations to convert PersistentMap to regular HashMap
        if (entity.getReplicaLocations() != null) {
            for (DataReplicaLocationEntity replicaEntity : entity.getReplicaLocations()) {
                // Force initialization of replicaMetadata by iterating over it
                Map<String, String> metadataCopy = new HashMap<>();
                if (replicaEntity.getReplicaMetadata() != null) {
                    try {
                        // Iterate over the map to force loading of all entries
                        for (Map.Entry<String, String> entry :
                                replicaEntity.getReplicaMetadata().entrySet()) {
                            metadataCopy.put(entry.getKey(), entry.getValue());
                        }
                    } catch (Exception e) {
                        metadataCopy = new HashMap<>();
                    }
                }

                // Detach the replica entity to convert PersistentMap to regular HashMap
                entityManager.detach(replicaEntity);

                // Replace PersistentMap with regular HashMap
                replicaEntity.setReplicaMetadata(new HashMap<>(metadataCopy));
            }
        }

        // Detach the main entity as well
        entityManager.detach(entity);

        return mapper.map(entity, DataProductModel.class);
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
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

        return mapper.map(parentEntity, DataProductModel.class);
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
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
                    return mapper.map(e, DataProductModel.class);
                })
                .collect(Collectors.toList());
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
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
                    return mapper.map(e, DataProductModel.class);
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
        DataProductEntity entity = mapper.map(dataProductModel, DataProductEntity.class);
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
        DataProductEntity entity = mapper.map(dataProductModel, DataProductEntity.class);
        dataProductRepository.save(entity);
        return true;
    }

    public void removeDataProduct(String productUri) throws ReplicaCatalogException {
        dataProductRepository.deleteById(productUri);
    }
}
