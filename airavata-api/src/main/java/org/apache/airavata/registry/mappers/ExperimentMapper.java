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
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ExperimentEntity and ExperimentModel.
 * Note: emailAddresses is excluded per dozer_mapping.xml configuration.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {
            UserConfigurationDataMapper.class,
            InputDataObjectTypeMapper.class,
            OutputDataObjectTypeMapper.class,
            ExperimentStatusMapper.class,
            ErrorModelMapper.class,
            ProcessMapper.class
        })
public interface ExperimentMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(target = "emailAddresses", ignore = true) // Excluded per dozer_mapping.xml
    @Mapping(target = "workflow", ignore = true) // Not mapped from entity
    @Mapping(target = "cleanUpStrategy", ignore = true) // Not mapped from entity
    ExperimentModel toModel(ExperimentEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(target = "emailAddresses", ignore = true) // Excluded per dozer_mapping.xml
    ExperimentEntity toEntity(ExperimentModel model);

    List<ExperimentModel> toModelList(List<ExperimentEntity> entities);

    List<ExperimentEntity> toEntityList(List<ExperimentModel> models);
}
