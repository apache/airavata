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
package org.apache.airavata.compute.resource.mapper;

import org.apache.airavata.compute.resource.entity.ResourceEntity;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.core.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link ResourceEntity} and {@link Resource}.
 *
 * <p>All scalar fields map 1:1. The {@code gateway} lazy association on the entity side
 * is excluded from both directions to prevent unintended Hibernate proxy initialisation.
 * MapStruct handles the {@code Integer} (entity) to {@code int} (model) port widening
 * automatically using the {@code NullValuePropertyMappingStrategy.IGNORE} policy defined
 * in {@link EntityMapperConfiguration}.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface ResourceMapper extends EntityMapper<ResourceEntity, Resource> {

    @Override
    @Mapping(target = "gateway", ignore = true)
    ResourceEntity toEntity(Resource model);
}
