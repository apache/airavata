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
import org.apache.airavata.common.model.CommandObject;
import org.apache.airavata.registry.entities.appcatalog.ApplicationDeploymentCommandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between CommandObject and ApplicationDeploymentCommandEntity.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface CommandObjectMapper {

    @Mapping(target = "command", source = "command")
    @Mapping(target = "commandOrder", source = "commandOrder")
    CommandObject toModel(ApplicationDeploymentCommandEntity entity);

    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "commandType", ignore = true) // Set by caller based on context
    @Mapping(target = "applicationDeployment", ignore = true)
    ApplicationDeploymentCommandEntity toEntity(CommandObject model);

    List<CommandObject> toModelList(List<ApplicationDeploymentCommandEntity> entities);

    List<ApplicationDeploymentCommandEntity> toEntityList(List<CommandObject> models);
}
