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
package org.apache.airavata.research.experiment.mapper;

import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.apache.airavata.research.experiment.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link ProjectEntity} and {@link Project}.
 *
 * <p>Field names differ between the model and the entity:
 * {@code Project.userName} maps to {@code ProjectEntity.ownerId}, and
 * {@code Project.projectName} maps to {@code ProjectEntity.name}.
 * The {@code experiments} lazy collection, {@code repositoryArtifact},
 * {@code datasetArtifacts}, and {@code state} are excluded from the
 * {@code toEntity} direction to prevent unintended Hibernate proxy
 * initialisation and to avoid overwriting managed state.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface ProjectMapper extends EntityMapper<ProjectEntity, Project> {

    @Override
    @Mapping(target = "ownerId", source = "userName")
    @Mapping(target = "name", source = "projectName")
    @Mapping(target = "experiments", ignore = true)
    @Mapping(target = "repositoryArtifact", ignore = true)
    @Mapping(target = "datasetArtifacts", ignore = true)
    @Mapping(target = "state", ignore = true)
    ProjectEntity toEntity(Project model);

    @Override
    @Mapping(target = "userName", source = "ownerId")
    @Mapping(target = "projectName", source = "name")
    Project toModel(ProjectEntity entity);
}
