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

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.AppCatalogUtils;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.storage.mapper.DataMovementMapper;
import org.apache.airavata.storage.mapper.StorageMapper;
import org.apache.airavata.storage.model.DataMovementInterfaceEntity;
import org.apache.airavata.storage.model.DataMovementInterfacePK;
import org.apache.airavata.storage.model.StorageDataMovementEntity;
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
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.localDataMovementToEntity(localDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementId();
    }

    public void updateLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.localDataMovementToEntity(localDataMovement);
        execute(entityManager -> entityManager.merge(entity));
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws AppCatalogException {
        StorageDataMovementEntity entity =
                execute(entityManager -> entityManager.find(StorageDataMovementEntity.class, dataMovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.localDataMovementToModel(entity);
    }

    // --- SCPDataMovement CRUD ---

    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        scpDataMovement = scpDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"))
                .build();
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.scpDataMovementToEntity(scpDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementId();
    }

    public void updateScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.scpDataMovementToEntity(scpDataMovement);
        entity.setUpdateTime(AiravataUtils.getCurrentTimestamp());
        execute(entityManager -> entityManager.merge(entity));
    }

    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws AppCatalogException {
        StorageDataMovementEntity entity =
                execute(entityManager -> entityManager.find(StorageDataMovementEntity.class, dataMovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.scpDataMovementToModel(entity);
    }

    // --- UnicoreDataMovement CRUD ---

    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        unicoreDataMovement = unicoreDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"))
                .build();
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.unicoreDataMovementToEntity(unicoreDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementId();
    }

    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId) throws AppCatalogException {
        StorageDataMovementEntity entity =
                execute(entityManager -> entityManager.find(StorageDataMovementEntity.class, dataMovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.unicoreDataMovementToModel(entity);
    }

    // --- GridFTPDataMovement CRUD ---

    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        gridFTPDataMovement = gridFTPDataMovement.toBuilder()
                .setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"))
                .build();
        StorageDataMovementEntity entity = StorageMapper.INSTANCE.gridFtpDataMovementToEntity(gridFTPDataMovement);
        execute(entityManager -> entityManager.merge(entity));
        return entity.getDataMovementId();
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws AppCatalogException {
        StorageDataMovementEntity entity =
                execute(entityManager -> entityManager.find(StorageDataMovementEntity.class, dataMovementId));
        if (entity == null) return null;
        return StorageMapper.INSTANCE.gridFtpDataMovementToModel(entity);
    }

    // --- Data movement interface removal ---

    public void removeDataMovementInterface(String resourceId, String dataMovementInterfaceId)
            throws AppCatalogException {
        DataMovementInterfacePK pk = new DataMovementInterfacePK();
        pk.setResourceId(resourceId);
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
