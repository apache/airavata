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
package org.apache.airavata.registry.mappers;

import java.util.List;
import org.apache.airavata.common.model.StoragePreference;
import org.apache.airavata.registry.entities.appcatalog.StoragePreferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between StoragePreferenceEntity and StoragePreference.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface StoragePreferenceMapper {

    StoragePreference toModel(StoragePreferenceEntity entity);

    @Mapping(target = "gatewayProfileResource", ignore = true) // Set by parent entity
    @Mapping(target = "gatewayId", ignore = true) // Set by parent entity
    StoragePreferenceEntity toEntity(StoragePreference model);

    List<StoragePreference> toModelList(List<StoragePreferenceEntity> entities);

    List<StoragePreferenceEntity> toEntityList(List<StoragePreference> models);
}
