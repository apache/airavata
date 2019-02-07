/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.replicacatalog;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.core.entities.replicacatalog.DataReplicaLocationEntity;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.DataReplicaLocationInterface;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class DataReplicaLocationRepository extends RepCatAbstractRepository<DataReplicaLocationModel, DataReplicaLocationEntity, String> implements DataReplicaLocationInterface {
    private final static Logger logger = LoggerFactory.getLogger(DataReplicaLocationRepository.class);

    public DataReplicaLocationRepository() { super(DataReplicaLocationModel.class, DataReplicaLocationEntity.class); }

    private String saveDataReplicaLocationModelData(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        DataReplicaLocationEntity dataReplicaLocationEntity = saveDataReplicaLocation(dataReplicaLocationModel);
        return dataReplicaLocationEntity.getReplicaId();
    }

    private DataReplicaLocationEntity saveDataReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {

        if (dataReplicaLocationModel.getReplicaId() == null) {
            logger.debug("Setting the Replica ID for the new Data Replica Location");
            dataReplicaLocationModel.setReplicaId(UUID.randomUUID().toString());
        }

        String replicaId = dataReplicaLocationModel.getReplicaId();
        dataReplicaLocationModel.setReplicaId(replicaId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        DataReplicaLocationEntity dataReplicaLocationEntity = mapper.map(dataReplicaLocationModel, DataReplicaLocationEntity.class);

        if (!isExists(replicaId)) {
            logger.debug("Checking if the Data Replica Location already exists");
            dataReplicaLocationEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        dataReplicaLocationEntity.setLastModifiedTime(new Timestamp(System.currentTimeMillis()));

        return execute(entityManager -> entityManager.merge(dataReplicaLocationEntity));
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        return saveDataReplicaLocationModelData(dataReplicaLocationModel);
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
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
