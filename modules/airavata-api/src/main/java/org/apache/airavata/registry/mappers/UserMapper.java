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
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.registry.entities.expcatalog.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserEntity (registry) and UserProfile.
 * Note: This is different from profile UserProfileMapper which maps UserProfileEntity.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class, implementationName = "RegistryUserMapperImpl")
public interface UserMapper {

    UserProfile toModel(UserEntity entity);

    @Mapping(target = "gateway", ignore = true) // Set by service layer
    @Mapping(target = "password", ignore = true) // Not mapped for security
    UserEntity toEntity(UserProfile model);

    List<UserProfile> toModelList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<UserProfile> models);
}
