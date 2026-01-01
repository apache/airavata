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
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaLocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between DataReplicaLocationEntity and DataReplicaLocationModel.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface DataReplicaLocationMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastModifiedTime",
            expression = "java(entity.getLastModifiedTime() != null ? entity.getLastModifiedTime().getTime() : 0L)")
    @Mapping(
            target = "validUntilTime",
            expression = "java(entity.getValidUntilTime() != null ? entity.getValidUntilTime().getTime() : 0L)")
    @Mapping(target = "productUri", ignore = true) // Set by parent entity
    DataReplicaLocationModel toModel(DataReplicaLocationEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "lastModifiedTime",
            expression =
                    "java(model.getLastModifiedTime() > 0 ? new java.sql.Timestamp(model.getLastModifiedTime()) : null)")
    @Mapping(
            target = "validUntilTime",
            expression =
                    "java(model.getValidUntilTime() > 0 ? new java.sql.Timestamp(model.getValidUntilTime()) : null)")
    @Mapping(target = "productUri", ignore = true) // Set by parent entity
    @Mapping(target = "dataProduct", ignore = true) // Set by parent entity
    DataReplicaLocationEntity toEntity(DataReplicaLocationModel model);

    List<DataReplicaLocationModel> toModelList(List<DataReplicaLocationEntity> entities);

    List<DataReplicaLocationEntity> toEntityList(List<DataReplicaLocationModel> models);
}
