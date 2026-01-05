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
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.entities.expcatalog.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ProjectEntity and Project.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ProjectMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(target = "sharedUsers", ignore = true) // Handled separately
    @Mapping(target = "sharedGroups", ignore = true) // Handled separately
    Project toModel(ProjectEntity entity);

    @Mapping(
            target = "creationTime",
            expression =
                    "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : new java.sql.Timestamp(System.currentTimeMillis()))")
    ProjectEntity toEntity(Project model);

    List<Project> toModelList(List<ProjectEntity> entities);

    List<ProjectEntity> toEntityList(List<Project> models);
}
