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
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.registry.entities.appcatalog.ApplicationInputEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentInputEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessInputEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between InputDataObjectType entities and InputDataObjectType model.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface InputDataObjectTypeMapper {

    InputDataObjectType toModel(ExperimentInputEntity entity);

    ExperimentInputEntity toEntity(InputDataObjectType model);

    InputDataObjectType toModel(ProcessInputEntity entity);

    ProcessInputEntity toEntityFromProcess(InputDataObjectType model);

    InputDataObjectType toModel(ApplicationInputEntity entity);

    ApplicationInputEntity toEntityFromApplication(InputDataObjectType model);

    List<InputDataObjectType> toModelListFromExperiment(List<ExperimentInputEntity> entities);

    List<ExperimentInputEntity> toEntityListFromExperiment(List<InputDataObjectType> models);

    List<InputDataObjectType> toModelListFromProcess(List<ProcessInputEntity> entities);

    List<ProcessInputEntity> toEntityListFromProcess(List<InputDataObjectType> models);

    List<InputDataObjectType> toModelListFromApplication(List<ApplicationInputEntity> entities);

    List<ApplicationInputEntity> toEntityListFromApplication(List<InputDataObjectType> models);
}
