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
import org.apache.airavata.common.model.ComputeResourceReservation;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ComputeResourceReservationEntity and ComputeResourceReservation.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ComputeResourceReservationMapper {

    @Mapping(
            target = "startTime",
            expression = "java(entity.getStartTime() != null ? entity.getStartTime().getTime() : 0L)")
    @Mapping(target = "endTime", expression = "java(entity.getEndTime() != null ? entity.getEndTime().getTime() : 0L)")
    ComputeResourceReservation toModel(ComputeResourceReservationEntity entity);

    @Mapping(
            target = "startTime",
            expression = "java(model.getStartTime() > 0 ? new java.sql.Timestamp(model.getStartTime()) : null)")
    @Mapping(
            target = "endTime",
            expression = "java(model.getEndTime() > 0 ? new java.sql.Timestamp(model.getEndTime()) : null)")
    ComputeResourceReservationEntity toEntity(ComputeResourceReservation model);

    List<ComputeResourceReservation> toModelList(List<ComputeResourceReservationEntity> entities);

    List<ComputeResourceReservationEntity> toEntityList(List<ComputeResourceReservation> models);
}
