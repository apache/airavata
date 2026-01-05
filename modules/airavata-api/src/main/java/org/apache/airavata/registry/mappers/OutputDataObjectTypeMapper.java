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
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.entities.appcatalog.ApplicationOutputEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentOutputEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessOutputEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between OutputDataObjectType entities and OutputDataObjectType model.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface OutputDataObjectTypeMapper {

    OutputDataObjectType toModel(ExperimentOutputEntity entity);

    ExperimentOutputEntity toEntity(OutputDataObjectType model);

    OutputDataObjectType toModel(ProcessOutputEntity entity);

    ProcessOutputEntity toEntityFromProcess(OutputDataObjectType model);

    OutputDataObjectType toModel(ApplicationOutputEntity entity);

    ApplicationOutputEntity toEntityFromApplication(OutputDataObjectType model);

    List<OutputDataObjectType> toModelListFromExperiment(List<ExperimentOutputEntity> entities);

    List<ExperimentOutputEntity> toEntityListFromExperiment(List<OutputDataObjectType> models);

    List<OutputDataObjectType> toModelListFromProcess(List<ProcessOutputEntity> entities);

    List<ProcessOutputEntity> toEntityListFromProcess(List<OutputDataObjectType> models);

    List<OutputDataObjectType> toModelListFromApplication(List<ApplicationOutputEntity> entities);

    List<ApplicationOutputEntity> toEntityListFromApplication(List<OutputDataObjectType> models);
}
