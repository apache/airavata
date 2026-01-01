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
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.registry.entities.expcatalog.ProcessStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ProcessStatusEntity and ProcessStatus.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ProcessStatusMapper {

    @Mapping(
            target = "timeOfStateChange",
            expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    @Mapping(target = "processId", ignore = true) // Set by parent entity
    ProcessStatus toModel(ProcessStatusEntity entity);

    @Mapping(
            target = "timeOfStateChange",
            expression =
                    "java(model.getTimeOfStateChange() > 0 ? new java.sql.Timestamp(model.getTimeOfStateChange()) : null)")
    @Mapping(target = "process", ignore = true) // Set by parent entity
    ProcessStatusEntity toEntity(ProcessStatus model);

    List<ProcessStatus> toModelList(List<ProcessStatusEntity> entities);

    List<ProcessStatusEntity> toEntityList(List<ProcessStatus> models);
}
