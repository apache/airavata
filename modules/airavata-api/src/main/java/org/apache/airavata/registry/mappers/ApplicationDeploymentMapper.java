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
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ApplicationDeploymentEntity and ApplicationDeploymentDescription.
 * Note: Command lists and library paths are handled manually in the service layer using unified entities.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ApplicationDeploymentMapper {

    // Model properties - handled manually in service layer
    @Mapping(target = "moduleLoadCmds", ignore = true)
    @Mapping(target = "preJobCommands", ignore = true)
    @Mapping(target = "postJobCommands", ignore = true)
    @Mapping(target = "libPrependPaths", ignore = true)
    @Mapping(target = "libAppendPaths", ignore = true)
    @Mapping(target = "setEnvironment", ignore = true)
    @Mapping(source = "defaultWallTime", target = "defaultWalltime")
    ApplicationDeploymentDescription toModel(ApplicationDeploymentEntity entity);

    // Entity properties - handled manually in service layer using unified entities
    @Mapping(target = "commands", ignore = true)
    @Mapping(target = "libraryPaths", ignore = true)
    @Mapping(target = "setEnvironment", ignore = true)
    @Mapping(source = "defaultWalltime", target = "defaultWallTime")
    ApplicationDeploymentEntity toEntity(ApplicationDeploymentDescription model);

    List<ApplicationDeploymentDescription> toModelList(List<ApplicationDeploymentEntity> entities);

    List<ApplicationDeploymentEntity> toEntityList(List<ApplicationDeploymentDescription> models);
}
