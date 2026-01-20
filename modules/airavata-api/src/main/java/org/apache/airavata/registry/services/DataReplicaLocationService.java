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
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.mappers.DataReplicaLocationMapper;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.repositories.replicacatalog.DataReplicaLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataReplicaLocationService {
    private final DataReplicaLocationRepository dataReplicaLocationRepository;
    private final DataProductRepository dataProductRepository;
    private final EntityManager entityManager;
    private final DataReplicaLocationMapper dataReplicaLocationMapper;

    public DataReplicaLocationService(
            DataReplicaLocationRepository dataReplicaLocationRepository,
            DataProductRepository dataProductRepository,
            EntityManager entityManager,
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

    @Transactional(readOnly = true)
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        DataReplicaLocationEntity e =
                dataReplicaLocationRepository.findById(replicaId).orElse(null);
        if (e == null) return null;

        // Manual mapping to avoid session issues with PersistentMap
        DataReplicaLocationModel model = new DataReplicaLocationModel();
        model.setReplicaId(e.getReplicaId());
        model.setProductUri(e.getProductUri());
        model.setReplicaName(e.getReplicaName());
        model.setReplicaDescription(e.getReplicaDescription());
        model.setStorageResourceId(e.getStorageResourceId());
        model.setFilePath(e.getFilePath());
        model.setReplicaLocationCategory(e.getReplicaLocationCategory());
        model.setReplicaPersistentType(e.getReplicaPersistentType());
        if (e.getCreationTime() != null) {
            model.setCreationTime(e.getCreationTime().getTime());
        }
        if (e.getLastModifiedTime() != null) {
            model.setLastModifiedTime(e.getLastModifiedTime().getTime());
        }
        if (e.getValidUntilTime() != null) {
            model.setValidUntilTime(e.getValidUntilTime().getTime());
        }

        // Copy metadata to a regular HashMap
        if (e.getReplicaMetadata() != null && !e.getReplicaMetadata().isEmpty()) {
            model.setReplicaMetadata(new HashMap<>(e.getReplicaMetadata()));
        }

        return model;
    }

    @Transactional(readOnly = true)
    public List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException {
        List<DataReplicaLocationEntity> entities = dataReplicaLocationRepository.findByProductUri(productUri);
        List<DataReplicaLocationModel> result = new java.util.ArrayList<>();

        for (DataReplicaLocationEntity e : entities) {
            DataReplicaLocationModel model = new DataReplicaLocationModel();
            model.setReplicaId(e.getReplicaId());
            model.setProductUri(e.getProductUri());
            model.setReplicaName(e.getReplicaName());
            model.setReplicaDescription(e.getReplicaDescription());
            model.setStorageResourceId(e.getStorageResourceId());
            model.setFilePath(e.getFilePath());
            model.setReplicaLocationCategory(e.getReplicaLocationCategory());
            model.setReplicaPersistentType(e.getReplicaPersistentType());
            if (e.getCreationTime() != null) {
                model.setCreationTime(e.getCreationTime().getTime());
            }
            if (e.getLastModifiedTime() != null) {
                model.setLastModifiedTime(e.getLastModifiedTime().getTime());
            }
            if (e.getValidUntilTime() != null) {
                model.setValidUntilTime(e.getValidUntilTime().getTime());
            }

            // Copy metadata to a regular HashMap
            if (e.getReplicaMetadata() != null && !e.getReplicaMetadata().isEmpty()) {
                model.setReplicaMetadata(new HashMap<>(e.getReplicaMetadata()));
            }

            result.add(model);
        }

        return result;
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
