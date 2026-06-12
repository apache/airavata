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
import org.apache.airavata.storage.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StorageMapper extends CommonMapperConversions {

    StorageMapper INSTANCE = Mappers.getMapper(StorageMapper.class);

    // --- StorageResourceDescription ---
    default StorageResourceDescription storageResourceToModel(StorageResourceEntity entity) {
        if (entity == null) return null;
        StorageResourceDescription.Builder b = StorageResourceDescription.newBuilder();
        if (entity.getStorageResourceId() != null) b.setStorageResourceId(entity.getStorageResourceId());
        if (entity.getHostName() != null) b.setHostName(entity.getHostName());
        b.setSftpPort(entity.getSftpPort());
        if (entity.getStorageResourceDescription() != null)
            b.setStorageResourceDescription(entity.getStorageResourceDescription());
        b.setEnabled(entity.isEnabled());
        if (entity.getCreationTime() != null)
            b.setCreationTime(entity.getCreationTime().getTime());
        if (entity.getUpdateTime() != null)
            b.setUpdateTime(entity.getUpdateTime().getTime());
        return b.build();
    }

    StorageResourceEntity storageResourceToEntity(StorageResourceDescription model);

    // --- StoragePreference ---
    StoragePreference storagePrefToModel(StoragePreferenceEntity entity);

    StoragePreferenceEntity storagePrefToEntity(StoragePreference model);
}
