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
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.mappers.DataReplicaLocationMapper;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.repositories.replicacatalog.DataReplicaLocationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("replicaCatalogTransactionManager")
public class DataReplicaLocationService {
    private final DataReplicaLocationRepository dataReplicaLocationRepository;
    private final DataProductRepository dataProductRepository;
    private final EntityManager entityManager;
    private final DataReplicaLocationMapper dataReplicaLocationMapper;

    public DataReplicaLocationService(
            DataReplicaLocationRepository dataReplicaLocationRepository,
            DataProductRepository dataProductRepository,
            @Qualifier("replicaCatalogEntityManager") EntityManager entityManager,
            DataReplicaLocationMapper dataReplicaLocationMapper) {
        this.dataReplicaLocationRepository = dataReplicaLocationRepository;
        this.dataProductRepository = dataProductRepository;
        this.entityManager = entityManager;
        this.dataReplicaLocationMapper = dataReplicaLocationMapper;
    }

    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws ReplicaCatalogException {
        // Generate replicaId if not set
        if (replicaLocationModel.getReplicaId() == null
                || replicaLocationModel.getReplicaId().isEmpty()) {
            String replicaId = org.apache.airavata.common.utils.AiravataUtils.getId(
                    replicaLocationModel.getReplicaName() != null ? replicaLocationModel.getReplicaName() : "replica");
            replicaLocationModel.setReplicaId(replicaId);
        }
        DataReplicaLocationEntity entity = dataReplicaLocationMapper.toEntity(replicaLocationModel);
        // Set the dataProduct relationship if productUri is provided
        if (replicaLocationModel.getProductUri() != null
                && !replicaLocationModel.getProductUri().isEmpty()) {
            DataProductEntity dataProduct = dataProductRepository
                    .findById(replicaLocationModel.getProductUri())
                    .orElseThrow(() -> new ReplicaCatalogException(
                            "DataProduct not found with productUri: " + replicaLocationModel.getProductUri()));
            entity.setDataProduct(dataProduct);
        }
        DataReplicaLocationEntity saved = dataReplicaLocationRepository.save(entity);
        return saved.getReplicaId();
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        DataReplicaLocationEntity entity =
                dataReplicaLocationRepository.findById(replicaId).orElse(null);
        if (entity == null) return null;

        // Force initialization of replicaMetadata by iterating over it
        // This ensures the metadata is fully loaded within the transaction
        Map<String, String> metadataCopy = new HashMap<>();
        if (entity.getReplicaMetadata() != null) {
            try {
                // Iterate over the map to force loading of all entries
                for (Map.Entry<String, String> entry :
                        entity.getReplicaMetadata().entrySet()) {
                    metadataCopy.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                // If metadata access fails, use empty map
                metadataCopy = new HashMap<>();
            }
        }

        // Detach the entity to convert PersistentMap to regular HashMap
        // This prevents Dozer from accessing the lazy collection
        entityManager.detach(entity);

        // Replace PersistentMap with regular HashMap
        entity.setReplicaMetadata(new HashMap<>(metadataCopy));

        // Now perform MapStruct mapping - the metadata is a regular HashMap, not a PersistentMap
        DataReplicaLocationModel model = dataReplicaLocationMapper.toModel(entity);

        // Always set the metadata copy on the model to ensure it's available after transaction ends
        model.setReplicaMetadata(metadataCopy);
        return model;
    }

    @Transactional(value = "replicaCatalogTransactionManager", readOnly = true)
    public List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException {
        List<DataReplicaLocationEntity> entities = dataReplicaLocationRepository.findByProductUri(productUri);
        return entities.stream()
                .map(e -> {
                    // Force initialization of replicaMetadata by iterating over it
                    Map<String, String> metadataCopy = new HashMap<>();
                    if (e.getReplicaMetadata() != null) {
                        try {
                            // Iterate over the map to force loading of all entries
                            for (Map.Entry<String, String> entry :
                                    e.getReplicaMetadata().entrySet()) {
                                metadataCopy.put(entry.getKey(), entry.getValue());
                            }
                        } catch (Exception ex) {
                            metadataCopy = new HashMap<>();
                        }
                    }

                    // Detach the entity to convert PersistentMap to regular HashMap
                    entityManager.detach(e);

                    // Replace PersistentMap with regular HashMap
                    e.setReplicaMetadata(new HashMap<>(metadataCopy));

                    // Now perform MapStruct mapping
                    DataReplicaLocationModel model = dataReplicaLocationMapper.toModel(e);

                    // Always set the metadata copy on the model
                    model.setReplicaMetadata(metadataCopy);
                    return model;
                })
                .collect(Collectors.toList());
    }

    public boolean updateReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws ReplicaCatalogException {
        DataReplicaLocationEntity entity = dataReplicaLocationMapper.toEntity(replicaLocationModel);
        // Set the dataProduct relationship if productUri is provided
        if (replicaLocationModel.getProductUri() != null
                && !replicaLocationModel.getProductUri().isEmpty()) {
            DataProductEntity dataProduct = dataProductRepository
                    .findById(replicaLocationModel.getProductUri())
                    .orElseThrow(() -> new ReplicaCatalogException(
                            "DataProduct not found with productUri: " + replicaLocationModel.getProductUri()));
            entity.setDataProduct(dataProduct);
        }
        dataReplicaLocationRepository.save(entity);
        return true;
    }

    public void removeReplicaLocation(String replicaId) throws ReplicaCatalogException {
        dataReplicaLocationRepository.deleteById(replicaId);
    }
}
