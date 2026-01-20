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

import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.registry.entities.expcatalog.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between TaskEntity and TaskModel.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {TaskStatusMapper.class, ErrorModelMapper.class, JobModelMapper.class})
public interface TaskModelMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastUpdateTime",
            expression = "java(entity.getLastUpdateTime() != null ? entity.getLastUpdateTime().getTime() : 0L)")
    @Mapping(target = "subTaskModel", ignore = true) // byte[] to Object conversion handled manually
    @Mapping(target = "taskStatuses", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "taskErrors", defaultExpression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "jobs", defaultExpression = "java(new java.util.ArrayList<>())")
    TaskModel toModel(TaskEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "lastUpdateTime",
            expression =
                    "java(model.getLastUpdateTime() > 0 ? new java.sql.Timestamp(model.getLastUpdateTime()) : null)")
    @Mapping(target = "subTaskModel", ignore = true) // Object to byte[] conversion handled manually
    @Mapping(target = "process", ignore = true) // Set by parent entity
    TaskEntity toEntity(TaskModel model);

    List<TaskModel> toModelList(List<TaskEntity> entities);

    List<TaskEntity> toEntityList(List<TaskModel> models);

    default Timestamp mapLongToTimestamp(Long value) {
        return value != null && value > 0 ? new Timestamp(value) : null;
    }

    default Long mapTimestampToLong(Timestamp value) {
        return value != null ? value.getTime() : null;
    }
}
