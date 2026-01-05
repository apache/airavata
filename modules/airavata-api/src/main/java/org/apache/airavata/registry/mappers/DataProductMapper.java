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
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between DataProductEntity and DataProductModel.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {DataReplicaLocationMapper.class})
public interface DataProductMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastModifiedTime",
            expression = "java(entity.getLastModifiedTime() != null ? entity.getLastModifiedTime().getTime() : 0L)")
    DataProductModel toModel(DataProductEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "lastModifiedTime",
            expression =
                    "java(model.getLastModifiedTime() > 0 ? new java.sql.Timestamp(model.getLastModifiedTime()) : null)")
    DataProductEntity toEntity(DataProductModel model);

    List<DataProductModel> toModelList(List<DataProductEntity> entities);

    List<DataProductEntity> toEntityList(List<DataProductModel> models);
}
