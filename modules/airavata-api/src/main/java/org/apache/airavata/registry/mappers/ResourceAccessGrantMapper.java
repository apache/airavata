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
import org.apache.airavata.common.model.ResourceAccessGrant;
import org.apache.airavata.registry.entities.ResourceAccessGrantEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for ResourceAccessGrantEntity and ResourceAccessGrant.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ResourceAccessGrantMapper {

    ResourceAccessGrant toModel(ResourceAccessGrantEntity entity);

    @org.mapstruct.Mapping(target = "creationTime", ignore = true)
    @org.mapstruct.Mapping(target = "updateTime", ignore = true)
    ResourceAccessGrantEntity toEntity(ResourceAccessGrant model);

    List<ResourceAccessGrant> toModelList(List<ResourceAccessGrantEntity> entities);

    List<ResourceAccessGrantEntity> toEntityList(List<ResourceAccessGrant> models);
}
