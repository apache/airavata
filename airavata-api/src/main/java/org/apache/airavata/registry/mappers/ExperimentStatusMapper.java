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
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.registry.entities.expcatalog.ExperimentStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ExperimentStatusEntity and ExperimentStatus.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ExperimentStatusMapper {

    @Mapping(
            target = "timeOfStateChange",
            expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    ExperimentStatus toModel(ExperimentStatusEntity entity);

    @Mapping(
            target = "timeOfStateChange",
            expression =
                    "java(model.getTimeOfStateChange() > 0 ? new java.sql.Timestamp(model.getTimeOfStateChange()) : null)")
    @Mapping(target = "experiment", ignore = true) // Set by parent entity
    ExperimentStatusEntity toEntity(ExperimentStatus model);

    List<ExperimentStatus> toModelList(List<ExperimentStatusEntity> entities);

    List<ExperimentStatusEntity> toEntityList(List<ExperimentStatus> models);
}
