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
package org.apache.airavata.storage.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.SecurityProtocol;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.storage.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StorageMapper extends CommonMapperConversions {

    StorageMapper INSTANCE = Mappers.getMapper(StorageMapper.class);

    // --- StorageResourceDescription ---
    StorageResourceDescription storageResourceToModel(StorageResourceEntity entity);

    StorageResourceEntity storageResourceToEntity(StorageResourceDescription model);

    // --- DataMovementInterface (used for StorageResourceEntity.dataMovementInterfaces) ---
    @Mapping(source = "resourceId", target = "storageResourceId")
    DataMovementInterface dataMovementInterfaceToModel(DataMovementInterfaceEntity entity);

    @Mapping(source = "storageResourceId", target = "resourceId")
    DataMovementInterfaceEntity dataMovementInterfaceToEntity(DataMovementInterface model);

    // --- StoragePreference ---
    StoragePreference storagePrefToModel(StoragePreferenceEntity entity);

    StoragePreferenceEntity storagePrefToEntity(StoragePreference model);

    // --- Data Movement types (manual default methods — StorageDataMovementEntity) ---

    // LOCALDataMovement
    default LOCALDataMovement localDataMovementToModel(StorageDataMovementEntity entity) {
        if (entity == null) return null;
        return LOCALDataMovement.newBuilder()
                .setDataMovementInterfaceId(entity.getDataMovementId())
                .build();
    }

    default StorageDataMovementEntity localDataMovementToEntity(LOCALDataMovement model) {
        if (model == null) return null;
        StorageDataMovementEntity entity = new StorageDataMovementEntity();
        entity.setDataMovementId(model.getDataMovementInterfaceId());
        entity.setMovementType("LOCAL");
        entity.setConfig(Collections.emptyMap());
        return entity;
    }

    // SCPDataMovement
    default SCPDataMovement scpDataMovementToModel(StorageDataMovementEntity entity) {
        if (entity == null) return null;
        Map<String, Object> cfg = entity.getConfig() != null ? entity.getConfig() : Collections.emptyMap();
        SCPDataMovement.Builder builder =
                SCPDataMovement.newBuilder().setDataMovementInterfaceId(entity.getDataMovementId());
        if (entity.getSecurityProtocol() != null) {
            builder.setSecurityProtocol(entity.getSecurityProtocol());
        }
        if (cfg.get("alternativeSCPHostName") instanceof String v) {
            builder.setAlternativeScpHostName(v);
        }
        if (cfg.get("sshPort") instanceof Number n) {
            builder.setSshPort(n.intValue());
        }
        return builder.build();
    }

    default StorageDataMovementEntity scpDataMovementToEntity(SCPDataMovement model) {
        if (model == null) return null;
        StorageDataMovementEntity entity = new StorageDataMovementEntity();
        entity.setDataMovementId(model.getDataMovementInterfaceId());
        entity.setMovementType("SCP");
        if (model.getSecurityProtocol() != SecurityProtocol.UNRECOGNIZED) {
            entity.setSecurityProtocol(model.getSecurityProtocol());
        }
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("alternativeSCPHostName", model.getAlternativeScpHostName());
        cfg.put("sshPort", model.getSshPort());
        entity.setConfig(cfg);
        return entity;
    }

    // UnicoreDataMovement
    default UnicoreDataMovement unicoreDataMovementToModel(StorageDataMovementEntity entity) {
        if (entity == null) return null;
        Map<String, Object> cfg = entity.getConfig() != null ? entity.getConfig() : Collections.emptyMap();
        UnicoreDataMovement.Builder builder =
                UnicoreDataMovement.newBuilder().setDataMovementInterfaceId(entity.getDataMovementId());
        if (entity.getSecurityProtocol() != null) {
            builder.setSecurityProtocol(entity.getSecurityProtocol());
        }
        if (cfg.get("unicoreEndpointUrl") instanceof String v) {
            builder.setUnicoreEndPointUrl(v);
        }
        return builder.build();
    }

    default StorageDataMovementEntity unicoreDataMovementToEntity(UnicoreDataMovement model) {
        if (model == null) return null;
        StorageDataMovementEntity entity = new StorageDataMovementEntity();
        entity.setDataMovementId(model.getDataMovementInterfaceId());
        entity.setMovementType("UNICORE");
        if (model.getSecurityProtocol() != SecurityProtocol.UNRECOGNIZED) {
            entity.setSecurityProtocol(model.getSecurityProtocol());
        }
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("unicoreEndpointUrl", model.getUnicoreEndPointUrl());
        entity.setConfig(cfg);
        return entity;
    }

    // GridFTPDataMovement
    default GridFTPDataMovement gridFtpDataMovementToModel(StorageDataMovementEntity entity) {
        if (entity == null) return null;
        Map<String, Object> cfg = entity.getConfig() != null ? entity.getConfig() : Collections.emptyMap();
        GridFTPDataMovement.Builder builder =
                GridFTPDataMovement.newBuilder().setDataMovementInterfaceId(entity.getDataMovementId());
        if (entity.getSecurityProtocol() != null) {
            builder.setSecurityProtocol(entity.getSecurityProtocol());
        }
        Object endpoints = cfg.get("gridFtpEndpoints");
        if (endpoints instanceof List<?> list) {
            list.forEach(ep -> {
                if (ep instanceof String s) builder.addGridFtpEndPoints(s);
            });
        }
        return builder.build();
    }

    default StorageDataMovementEntity gridFtpDataMovementToEntity(GridFTPDataMovement model) {
        if (model == null) return null;
        StorageDataMovementEntity entity = new StorageDataMovementEntity();
        entity.setDataMovementId(model.getDataMovementInterfaceId());
        entity.setMovementType("GRIDFTP");
        if (model.getSecurityProtocol() != SecurityProtocol.UNRECOGNIZED) {
            entity.setSecurityProtocol(model.getSecurityProtocol());
        }
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("gridFtpEndpoints", model.getGridFtpEndPointsList());
        entity.setConfig(cfg);
        return entity;
    }
}
