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

import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
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

    // --- DataProductModel ---
    DataProductModel dataProductToModel(DataProductEntity entity);

    DataProductEntity dataProductToEntity(DataProductModel model);

    // --- DataReplicaLocationModel ---
    DataReplicaLocationModel dataReplicaToModel(DataReplicaLocationEntity entity);

    DataReplicaLocationEntity dataReplicaToEntity(DataReplicaLocationModel model);

    // --- StoragePreference ---
    StoragePreference storagePrefToModel(StoragePreferenceEntity entity);

    StoragePreferenceEntity storagePrefToEntity(StoragePreference model);

    // --- UserStoragePreference ---
    UserStoragePreference userStoragePrefToModel(UserStoragePreferenceEntity entity);

    UserStoragePreferenceEntity userStoragePrefToEntity(UserStoragePreference model);

    // --- Data Movement types ---

    // LOCALDataMovement
    LOCALDataMovement localDataMovementToModel(LocalDataMovementEntity entity);

    LocalDataMovementEntity localDataMovementToEntity(LOCALDataMovement model);

    // SCPDataMovement
    SCPDataMovement scpDataMovementToModel(ScpDataMovementEntity entity);

    ScpDataMovementEntity scpDataMovementToEntity(SCPDataMovement model);

    // UnicoreDataMovement
    @Mapping(source = "unicoreEndpointUrl", target = "unicoreEndPointUrl")
    UnicoreDataMovement unicoreDataMovementToModel(UnicoreDatamovementEntity entity);

    @Mapping(source = "unicoreEndPointUrl", target = "unicoreEndpointUrl")
    UnicoreDatamovementEntity unicoreDataMovementToEntity(UnicoreDataMovement model);

    // GridFTPDataMovement
    GridFTPDataMovement gridFtpDataMovementToModel(GridftpDataMovementEntity entity);

    GridftpDataMovementEntity gridFtpDataMovementToEntity(GridFTPDataMovement model);
}
