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
 * Note: Lists (moduleLoadCmds, preJobCommands, etc.) are handled manually in the service layer.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ApplicationDeploymentMapper {

    @Mapping(target = "moduleLoadCmds", ignore = true) // Handled manually in service layer
    @Mapping(target = "preJobCommands", ignore = true) // Handled manually in service layer
    @Mapping(target = "postJobCommands", ignore = true) // Handled manually in service layer
    @Mapping(target = "libPrependPaths", ignore = true) // Handled manually in service layer
    @Mapping(target = "libAppendPaths", ignore = true) // Handled manually in service layer
    @Mapping(target = "setEnvironment", ignore = true) // Handled manually in service layer
    @Mapping(
            source = "defaultWallTime",
            target = "defaultWalltime") // Field name mismatch: entity uses defaultWallTime, model uses defaultWalltime
    ApplicationDeploymentDescription toModel(ApplicationDeploymentEntity entity);

    @Mapping(target = "moduleLoadCmds", ignore = true) // Handled manually in service layer
    @Mapping(target = "preJobCommands", ignore = true) // Handled manually in service layer
    @Mapping(target = "postJobCommands", ignore = true) // Handled manually in service layer
    @Mapping(target = "libPrependPaths", ignore = true) // Handled manually in service layer
    @Mapping(target = "libAppendPaths", ignore = true) // Handled manually in service layer
    @Mapping(target = "setEnvironment", ignore = true) // Handled manually in service layer
    @Mapping(
            source = "defaultWalltime",
            target = "defaultWallTime") // Field name mismatch: model uses defaultWalltime, entity uses defaultWallTime
    ApplicationDeploymentEntity toEntity(ApplicationDeploymentDescription model);

    List<ApplicationDeploymentDescription> toModelList(List<ApplicationDeploymentEntity> entities);

    List<ApplicationDeploymentEntity> toEntityList(List<ApplicationDeploymentDescription> models);
}
