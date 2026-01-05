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
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.registry.entities.appcatalog.StorageResourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between StorageResourceEntity and StorageResourceDescription.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {DataMovementInterfaceMapper.class})
public interface StorageResourceMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "updateTime",
            expression = "java(entity.getUpdateTime() != null ? entity.getUpdateTime().getTime() : 0L)")
    StorageResourceDescription toModel(StorageResourceEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "updateTime",
            expression = "java(model.getUpdateTime() > 0 ? new java.sql.Timestamp(model.getUpdateTime()) : null)")
    StorageResourceEntity toEntity(StorageResourceDescription model);

    List<StorageResourceDescription> toModelList(List<StorageResourceEntity> entities);

    List<StorageResourceEntity> toEntityList(List<StorageResourceDescription> models);
}
