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
import org.apache.airavata.common.model.AiravataWorkflow;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between AiravataWorkflowEntity and AiravataWorkflow.
 * Note: Nested lists (applications, handlers, connections, statuses, errors) are handled manually in the service layer.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface AiravataWorkflowMapper {

    @Mapping(
            target = "createdAt",
            expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().getTime() : 0L)")
    @Mapping(
            target = "updatedAt",
            expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L)")
    @Mapping(target = "applications", ignore = true) // Handled manually in service layer
    @Mapping(target = "handlers", ignore = true) // Handled manually in service layer
    @Mapping(target = "connections", ignore = true) // Handled manually in service layer
    @Mapping(target = "statuses", ignore = true) // Handled manually in service layer
    @Mapping(target = "errors", ignore = true) // Handled manually in service layer
    AiravataWorkflow toModel(AiravataWorkflowEntity entity);

    @Mapping(
            target = "createdAt",
            expression = "java(model.getCreatedAt() > 0 ? new java.sql.Timestamp(model.getCreatedAt()) : null)")
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() > 0 ? new java.sql.Timestamp(model.getUpdatedAt()) : null)")
    @Mapping(target = "applications", ignore = true) // Handled manually in service layer
    @Mapping(target = "handlers", ignore = true) // Handled manually in service layer
    @Mapping(target = "connections", ignore = true) // Handled manually in service layer
    @Mapping(target = "statuses", ignore = true) // Handled manually in service layer
    @Mapping(target = "errors", ignore = true) // Handled manually in service layer
    AiravataWorkflowEntity toEntity(AiravataWorkflow model);

    List<AiravataWorkflow> toModelList(List<AiravataWorkflowEntity> entities);

    List<AiravataWorkflowEntity> toEntityList(List<AiravataWorkflow> models);
}
