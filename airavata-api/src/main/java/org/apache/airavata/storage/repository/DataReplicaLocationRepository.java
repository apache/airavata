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
package org.apache.airavata.storage.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.execution.util.AbstractRepository;
import org.apache.airavata.execution.util.cpi.DataReplicaLocationInterface;
import org.apache.airavata.execution.util.cpi.ReplicaCatalogException;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.storage.mapper.StorageMapper;
import org.apache.airavata.storage.model.DataProductEntity;
import org.apache.airavata.storage.model.DataReplicaLocationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReplicaLocationRepository
        extends AbstractRepository<DataReplicaLocationModel, DataReplicaLocationEntity, String>
        implements DataReplicaLocationInterface {
    private static final Logger logger = LoggerFactory.getLogger(DataReplicaLocationRepository.class);

    public DataReplicaLocationRepository() {
        super(DataReplicaLocationModel.class, DataReplicaLocationEntity.class);
    }

    @Override
    protected DataReplicaLocationModel toModel(DataReplicaLocationEntity entity) {
        return StorageMapper.INSTANCE.dataReplicaToModel(entity);
    }

    @Override
    protected DataReplicaLocationEntity toEntity(DataReplicaLocationModel model) {
        return StorageMapper.INSTANCE.dataReplicaToEntity(model);
    }

    @Override
    protected void initializeEntity(DataReplicaLocationEntity entity) {
        // Replace Hibernate PersistentMap with plain HashMap to prevent
        // LazyInitializationException when Dozer accesses map entries
        if (entity.getReplicaMetadata() != null) {
            entity.setReplicaMetadata(new java.util.HashMap<>(entity.getReplicaMetadata()));
        }
    }

    private String saveDataReplicaLocationModelData(DataReplicaLocationModel dataReplicaLocationModel)
            throws ReplicaCatalogException {
        DataReplicaLocationEntity dataReplicaLocationEntity = saveDataReplicaLocation(dataReplicaLocationModel);
        return dataReplicaLocationEntity.getReplicaId();
    }

    private DataReplicaLocationEntity saveDataReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel)
            throws ReplicaCatalogException {

        if (dataReplicaLocationModel.getReplicaId() == null) {
            logger.debug("Setting the Replica ID for the new Data Replica Location");
            dataReplicaLocationModel.setReplicaId(UUID.randomUUID().toString());
        }

        String replicaId = dataReplicaLocationModel.getReplicaId();
        dataReplicaLocationModel.setReplicaId(replicaId);
        DataReplicaLocationEntity dataReplicaLocationEntity =
                StorageMapper.INSTANCE.dataReplicaToEntity(dataReplicaLocationModel);

        if (!isExists(replicaId)) {
            logger.debug("Checking if the Data Replica Location already exists");
            dataReplicaLocationEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        dataReplicaLocationEntity.setLastModifiedTime(new Timestamp(System.currentTimeMillis()));

        return execute(entityManager -> {
            // Hibernate 6 requires @ManyToOne references to be set (not just the FK column)
            if (dataReplicaLocationEntity.getDataProduct() == null
                    && dataReplicaLocationEntity.getProductUri() != null) {
                DataProductEntity dataProductRef =
                        entityManager.getReference(DataProductEntity.class, dataReplicaLocationEntity.getProductUri());
                dataReplicaLocationEntity.setDataProduct(dataProductRef);
            }
            return entityManager.merge(dataReplicaLocationEntity);
        });
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel)
            throws ReplicaCatalogException {
        return saveDataReplicaLocationModelData(dataReplicaLocationModel);
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel)
            throws ReplicaCatalogException {
        return (saveDataReplicaLocationModelData(dataReplicaLocationModel) != null);
    }

    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        return get(replicaId);
    }

    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException {
        DataProductRepository dataProductRepository = new DataProductRepository();
        DataProductModel dataProductModel = dataProductRepository.getDataProduct(productUri);
        List<DataReplicaLocationModel> dataReplicaLocationModelList = dataProductModel.getReplicaLocations();
        return dataReplicaLocationModelList;
    }

    @Override
    public boolean removeReplicaLocation(String replicaId) throws ReplicaCatalogException {
        return delete(replicaId);
    }
}
