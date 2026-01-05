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
import org.apache.airavata.common.model.SetEnvPaths;
import org.apache.airavata.registry.entities.appcatalog.AppEnvironmentEntity;
import org.apache.airavata.registry.entities.appcatalog.LibraryApendPathEntity;
import org.apache.airavata.registry.entities.appcatalog.LibraryPrependPathEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between SetEnvPaths and environment path entities.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface SetEnvPathsMapper {

    SetEnvPaths toModel(AppEnvironmentEntity entity);

    @Mapping(target = "applicationDeployment", ignore = true) // Set by parent entity
    @Mapping(target = "deploymentId", ignore = true) // Set by parent entity
    AppEnvironmentEntity toEntity(SetEnvPaths model);

    @Mapping(target = "envPathOrder", constant = "0") // LibraryPrependPathEntity doesn't have envPathOrder
    SetEnvPaths toModelFromPrepend(LibraryPrependPathEntity entity);

    @Mapping(target = "applicationDeployment", ignore = true) // Set by parent entity
    @Mapping(target = "deploymentId", ignore = true) // Set by parent entity
    LibraryPrependPathEntity toEntityToPrepend(SetEnvPaths model);

    @Mapping(target = "envPathOrder", constant = "0") // LibraryApendPathEntity doesn't have envPathOrder
    SetEnvPaths toModelFromAppend(LibraryApendPathEntity entity);

    @Mapping(target = "applicationDeployment", ignore = true) // Set by parent entity
    @Mapping(target = "deploymentId", ignore = true) // Set by parent entity
    LibraryApendPathEntity toEntityToAppend(SetEnvPaths model);

    List<SetEnvPaths> toModelListFromEnvironment(List<AppEnvironmentEntity> entities);

    List<AppEnvironmentEntity> toEntityListToEnvironment(List<SetEnvPaths> models);

    List<SetEnvPaths> toModelListFromPrepend(List<LibraryPrependPathEntity> entities);

    List<LibraryPrependPathEntity> toEntityListToPrepend(List<SetEnvPaths> models);

    List<SetEnvPaths> toModelListFromAppend(List<LibraryApendPathEntity> entities);

    List<LibraryApendPathEntity> toEntityListToAppend(List<SetEnvPaths> models);
}
