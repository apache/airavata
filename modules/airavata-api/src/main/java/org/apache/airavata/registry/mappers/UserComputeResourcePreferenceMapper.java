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
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.registry.entities.appcatalog.UserComputeResourcePreferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between UserComputeResourcePreferenceEntity and UserComputeResourcePreference.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface UserComputeResourcePreferenceMapper {

    @Mapping(
            target = "reservationStartTime",
            expression =
                    "java(entity.getReservationStartTime() != null ? entity.getReservationStartTime().getTime() : 0L)")
    @Mapping(
            target = "reservationEndTime",
            expression = "java(entity.getReservationEndTime() != null ? entity.getReservationEndTime().getTime() : 0L)")
    @Mapping(target = "sshAccountProvisioner", ignore = true) // Not stored in entity
    UserComputeResourcePreference toModel(UserComputeResourcePreferenceEntity entity);

    @Mapping(
            target = "reservationStartTime",
            expression =
                    "java(model.getReservationStartTime() > 0 ? new java.sql.Timestamp(model.getReservationStartTime()) : null)")
    @Mapping(
            target = "reservationEndTime",
            expression =
                    "java(model.getReservationEndTime() > 0 ? new java.sql.Timestamp(model.getReservationEndTime()) : null)")
    @Mapping(target = "userResourceProfile", ignore = true) // Set by parent entity
    @Mapping(target = "userId", ignore = true) // Set by parent entity
    @Mapping(target = "gatewayId", ignore = true) // Set by parent entity
    UserComputeResourcePreferenceEntity toEntity(UserComputeResourcePreference model);

    List<UserComputeResourcePreference> toModelList(List<UserComputeResourcePreferenceEntity> entities);

    List<UserComputeResourcePreferenceEntity> toEntityList(List<UserComputeResourcePreference> models);
}
