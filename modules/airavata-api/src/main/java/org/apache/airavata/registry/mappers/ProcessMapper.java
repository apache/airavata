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
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ProcessEntity and ProcessModel.
 * Note: emailAddresses is excluded as it's handled separately.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {
            ProcessStatusMapper.class,
            InputDataObjectTypeMapper.class,
            OutputDataObjectTypeMapper.class,
            ComputationalResourceSchedulingMapper.class,
            TaskModelMapper.class,
            ErrorModelMapper.class,
            ProcessWorkflowMapper.class
        })
public interface ProcessMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastUpdateTime",
            expression = "java(entity.getLastUpdateTime() != null ? entity.getLastUpdateTime().getTime() : 0L)")
    @Mapping(target = "emailAddresses", ignore = true) // Handled separately
    @Mapping(target = "processWorkflows", ignore = true) // Handled separately to avoid LazyInitializationException
    ProcessModel toModel(ProcessEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "lastUpdateTime",
            expression =
                    "java(model.getLastUpdateTime() > 0 ? new java.sql.Timestamp(model.getLastUpdateTime()) : null)")
    @Mapping(target = "emailAddresses", ignore = true) // Handled separately
    @Mapping(target = "experiment", ignore = true) // Set by service layer
    ProcessEntity toEntity(ProcessModel model);

    List<ProcessModel> toModelList(List<ProcessEntity> entities);

    List<ProcessEntity> toEntityList(List<ProcessModel> models);
}
