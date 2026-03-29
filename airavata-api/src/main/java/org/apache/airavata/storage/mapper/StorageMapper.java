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

import java.sql.Timestamp;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.storage.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StorageMapper {

    StorageMapper INSTANCE = Mappers.getMapper(StorageMapper.class);

    // --- StorageResourceDescription ---
    StorageResourceDescription storageResourceToModel(StorageResourceEntity entity);

    StorageResourceEntity storageResourceToEntity(StorageResourceDescription model);

    // --- DataMovementInterface ---
    DataMovementInterface dataMovementToModel(DataMovementInterfaceEntity entity);

    DataMovementInterfaceEntity dataMovementToEntity(DataMovementInterface model);

    // --- StorageInterface (extra type used in StorageResourceRepository) ---
    DataMovementInterface storageInterfaceToModel(StorageInterfaceEntity entity);

    StorageInterfaceEntity storageInterfaceToEntity(DataMovementInterface model);

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

    // --- Custom converters ---

    default Timestamp longToTimestamp(long millis) {
        return millis == 0 ? null : new Timestamp(millis);
    }

    default long timestampToLong(Timestamp ts) {
        return ts == null ? 0 : ts.getTime();
    }
}
