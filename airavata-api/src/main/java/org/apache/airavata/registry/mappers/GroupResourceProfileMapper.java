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
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.registry.entities.appcatalog.GroupResourceProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between GroupResourceProfileEntity and GroupResourceProfile.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {
            GroupComputeResourcePreferenceMapper.class,
            ComputeResourcePolicyMapper.class,
            BatchQueueResourcePolicyMapper.class
        })
public interface GroupResourceProfileMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime() : 0L)")
    @Mapping(
            target = "updatedTime",
            expression = "java(entity.getUpdatedTime() != null ? entity.getUpdatedTime() : 0L)")
    @Mapping(target = "computePreferences", ignore = true) // Handled manually in service layer (polymorphic)
    GroupResourceProfile toModel(GroupResourceProfileEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? java.lang.Long.valueOf(model.getCreationTime()) : null)")
    @Mapping(
            target = "updatedTime",
            expression = "java(model.getUpdatedTime() > 0 ? java.lang.Long.valueOf(model.getUpdatedTime()) : null)")
    @Mapping(target = "computePreferences", ignore = true) // Handled manually in service layer (polymorphic)
    GroupResourceProfileEntity toEntity(GroupResourceProfile model);

    List<GroupResourceProfile> toModelList(List<GroupResourceProfileEntity> entities);

    List<GroupResourceProfileEntity> toEntityList(List<GroupResourceProfile> models);
}
