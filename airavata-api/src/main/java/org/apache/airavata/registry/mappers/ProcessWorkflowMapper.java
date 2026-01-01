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
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.registry.entities.expcatalog.ProcessWorkflowEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ProcessWorkflowEntity and ProcessWorkflow.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ProcessWorkflowMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    ProcessWorkflow toModel(ProcessWorkflowEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(target = "process", ignore = true) // Set by parent entity
    ProcessWorkflowEntity toEntity(ProcessWorkflow model);

    default Timestamp map(Long value) {
        return value != null ? new Timestamp(value) : null;
    }

    default Long map(Timestamp value) {
        return value != null ? value.getTime() : null;
    }

    List<ProcessWorkflow> toModelList(List<ProcessWorkflowEntity> entities);

    List<ProcessWorkflowEntity> toEntityList(List<ProcessWorkflow> models);
}
