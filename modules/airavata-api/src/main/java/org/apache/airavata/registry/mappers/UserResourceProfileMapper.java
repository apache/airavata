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
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.registry.entities.appcatalog.UserResourceProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserResourceProfileEntity and UserResourceProfile.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {UserComputeResourcePreferenceMapper.class, UserStoragePreferenceMapper.class})
public interface UserResourceProfileMapper {

    @Mapping(target = "userComputeResourcePreferences", ignore = true) // Handled manually in service layer
    @Mapping(target = "userStoragePreferences", ignore = true) // Handled manually in service layer
    @Mapping(
            source = "gatewayId",
            target = "gatewayID") // Field name mismatch: entity uses gatewayId, model uses gatewayID
    UserResourceProfile toModel(UserResourceProfileEntity entity);

    @Mapping(target = "userComputeResourcePreferences", ignore = true) // Handled manually in service layer
    @Mapping(target = "userStoragePreferences", ignore = true) // Handled manually in service layer
    @Mapping(
            source = "gatewayID",
            target = "gatewayId") // Field name mismatch: model uses gatewayID, entity uses gatewayId
    UserResourceProfileEntity toEntity(UserResourceProfile model);

    List<UserResourceProfile> toModelList(List<UserResourceProfileEntity> entities);

    List<UserResourceProfileEntity> toEntityList(List<UserResourceProfile> models);
}
