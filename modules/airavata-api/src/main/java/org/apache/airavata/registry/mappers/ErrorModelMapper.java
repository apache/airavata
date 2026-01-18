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
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessErrorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ErrorModel entities and ErrorModel.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ErrorModelMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(entity.getRootCauseErrorIdList() != null && !entity.getRootCauseErrorIdList().isEmpty() ? java.util.Arrays.asList(entity.getRootCauseErrorIdList().split(\",\")) : null)")
    ErrorModel toModel(ExperimentErrorEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(model.getRootCauseErrorIdList() != null ? String.join(\",\", model.getRootCauseErrorIdList()) : null)")
    @Mapping(target = "experiment", ignore = true) // Set by parent entity - immutable in JPA
    ExperimentErrorEntity toEntity(ErrorModel model);

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(entity.getRootCauseErrorIdList() != null && !entity.getRootCauseErrorIdList().isEmpty() ? java.util.Arrays.asList(entity.getRootCauseErrorIdList().split(\",\")) : null)")
    ErrorModel toModel(ProcessErrorEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(model.getRootCauseErrorIdList() != null ? String.join(\",\", model.getRootCauseErrorIdList()) : null)")
    @Mapping(target = "process", ignore = true) // Set by parent entity - immutable in JPA
    ProcessErrorEntity toEntityFromProcess(ErrorModel model);

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(entity.getRootCauseErrorIdList() != null && !entity.getRootCauseErrorIdList().isEmpty() ? java.util.Arrays.asList(entity.getRootCauseErrorIdList().split(\",\")) : null)")
    ErrorModel toModel(org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(model.getRootCauseErrorIdList() != null ? String.join(\",\", model.getRootCauseErrorIdList()) : null)")
    @Mapping(target = "task", ignore = true) // Set by parent entity - immutable in JPA
    org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity toEntityFromTask(ErrorModel model);

    List<ErrorModel> toModelListFromExperiment(List<ExperimentErrorEntity> entities);

    List<ExperimentErrorEntity> toEntityListFromExperiment(List<ErrorModel> models);

    List<ErrorModel> toModelListFromProcess(List<ProcessErrorEntity> entities);

    List<ProcessErrorEntity> toEntityListFromProcess(List<ErrorModel> models);

    List<ErrorModel> toModelListFromTask(
            List<org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity> entities);

    List<org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity> toEntityListFromTask(
            List<ErrorModel> models);
}
