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
package org.apache.airavata.profile.mappers;

import java.util.List;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.profile.entities.UserProfileEntity;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserProfileEntity and UserProfile model.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface UserProfileMapper {

    @Mapping(
            target = "state",
            expression =
                    "java(entity.getState() != null ? org.apache.airavata.common.model.Status.valueOf(entity.getState()) : null)")
    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastAccessTime",
            expression = "java(entity.getLastAccessTime() != null ? entity.getLastAccessTime().getTime() : 0L)")
    @Mapping(
            target = "validUntil",
            expression = "java(entity.getValidUntil() != null ? entity.getValidUntil().getTime() : -1L)")
    @Mapping(target = "customDashboard", source = "customizedDashboardEntity")
    @Mapping(target = "nsfDemographics", ignore = true)
    UserProfile toModel(UserProfileEntity entity);

    @Mapping(target = "state", expression = "java(model.getState() != null ? model.getState().name() : null)")
    @Mapping(
            target = "validUntil",
            expression = "java(model.getValidUntil() > 0 ? new java.util.Date(model.getValidUntil()) : null)")
    @Mapping(target = "customizedDashboardEntity", source = "customDashboard")
    UserProfileEntity toEntity(UserProfile model);

    List<UserProfile> toModelList(List<UserProfileEntity> entities);

    List<UserProfileEntity> toEntityList(List<UserProfile> models);
}
