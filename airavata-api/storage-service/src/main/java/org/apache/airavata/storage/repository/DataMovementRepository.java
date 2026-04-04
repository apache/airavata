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

import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.AppCatalogUtils;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.storage.mapper.DataMovementMapper;
import org.apache.airavata.storage.mapper.StorageMapper;
import org.apache.airavata.storage.model.*;
import org.apache.airavata.util.AiravataUtils;
import org.springframework.stereotype.Component;

@Component
public class DataMovementRepository
        extends AbstractRepository<DataMovementInterface, DataMovementInterfaceEntity, DataMovementInterfacePK> {

    public DataMovementRepository() {
        super(DataMovementInterface.class, DataMovementInterfaceEntity.class);
    }

    @Override
    protected DataMovementInterface toModel(DataMovementInterfaceEntity entity) {
        return DataMovementMapper.INSTANCE.toModel(entity);
    }

    @Override
    protected DataMovementInterfaceEntity toEntity(DataMovementInterface model) {
        return DataMovementMapper.INSTANCE.toEntity(model);
    }

    public String addDataMovementProtocol(String resourceId, DataMovementInterface dataMovementInterface) {
        DataMovementInterfaceEntity dataMovementInterfaceEntity =
                DataMovementMapper.INSTANCE.toEntity(dataMovementInterface);
        dataMovementInterfaceEntity.setResourceId(resourceId);
        execute(entityManager -> entityManager.merge(dataMovementInterfaceEntity));
        return dataMovementInterfaceEntity.getDataMovementInterfaceId();
    }

    // --- LOCALDataMovement CRUD ---

    public String addLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        localDataMovement = localDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"))
                .build();
        LocalDataMovementEntity entity = StorageMapper.INSTANCE.localDataMovementToEntity(localDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementInterfaceId();
    }

    public void updateLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        LocalDataMovementEntity entity = StorageMapper.INSTANCE.localDataMovementToEntity(localDataMovement);
        execute(entityManager -> entityManager.merge(entity));
    }

    public LOCALDataMovement getLocalDataMovement(String datamovementId) throws AppCatalogException {
        LocalDataMovementEntity entity =
                execute(entityManager -> entityManager.find(LocalDataMovementEntity.class, datamovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.localDataMovementToModel(entity);
    }

    // --- SCPDataMovement CRUD ---

    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        scpDataMovement = scpDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"))
                .build();
        ScpDataMovementEntity entity = StorageMapper.INSTANCE.scpDataMovementToEntity(scpDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementInterfaceId();
    }

    public void updateScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        ScpDataMovementEntity entity = StorageMapper.INSTANCE.scpDataMovementToEntity(scpDataMovement);
        entity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        execute(entityManager -> entityManager.merge(entity));
    }

    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        ScpDataMovementEntity entity =
                execute(entityManager -> entityManager.find(ScpDataMovementEntity.class, dataMoveId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.scpDataMovementToModel(entity);
    }

    // --- UnicoreDataMovement CRUD ---

    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        unicoreDataMovement = unicoreDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"))
                .build();
        UnicoreDatamovementEntity entity = StorageMapper.INSTANCE.unicoreDataMovementToEntity(unicoreDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementInterfaceId();
    }

    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId) throws AppCatalogException {
        UnicoreDatamovementEntity entity =
                execute(entityManager -> entityManager.find(UnicoreDatamovementEntity.class, dataMovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.unicoreDataMovementToModel(entity);
    }

    // --- GridFTPDataMovement CRUD ---

    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        gridFTPDataMovement = gridFTPDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"))
                .build();
        GridftpDataMovementEntity entity = StorageMapper.INSTANCE.gridFtpDataMovementToEntity(gridFTPDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFtpEndPointsList();
        if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
            for (String endpoint : gridFTPEndPoint) {
                GridftpEndpointEntity endpointEntity = new GridftpEndpointEntity();
                endpointEntity.setGridftpDataMovement(entity);
                endpointEntity.setDataMovementInterfaceId(gridFTPDataMovement.getDataMovementInterfaceId());
                endpointEntity.setEndpoint(endpoint);
                execute(entityManager -> entityManager.merge(endpointEntity));
            }
        }
        return entity.getDataMovementInterfaceId();
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        GridftpDataMovementEntity entity =
                execute(entityManager -> entityManager.find(GridftpDataMovementEntity.class, dataMoveId));
        if (entity == null) return null;

        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.DataMovement.GRID_FTP_DATA_MOVEMENT_ID, entity.getDataMovementInterfaceId());
        List resultSet = execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.FIND_ALL_GRID_FTP_ENDPOINTS_BY_DATA_MOVEMENT);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<GridftpEndpointEntity> endpointEntities = resultSet;
        List<String> endpoints = endpointEntities.stream()
                .map(GridftpEndpointEntity::getEndpoint)
                .collect(Collectors.toList());
        GridFTPDataMovement dataMovement = StorageMapper.INSTANCE.gridFtpDataMovementToModel(entity);
        return dataMovement.toBuilder().addAllGridFtpEndPoints(endpoints).build();
    }

    // --- Data movement interface removal ---

    public void removeDataMovementInterface(String computeResourceId, String dataMovementInterfaceId)
            throws AppCatalogException {
        DataMovementInterfacePK pk = new DataMovementInterfacePK();
        pk.setResourceId(computeResourceId);
        pk.setDataMovementInterfaceId(dataMovementInterfaceId);
        execute(entityManager -> {
            DataMovementInterfaceEntity entity = entityManager.find(DataMovementInterfaceEntity.class, pk);
            if (entity != null) {
                entityManager.remove(entity);
            }
            return null;
        });
    }
}
