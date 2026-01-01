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
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ComputeResourceEntity and ComputeResourceDescription.
 * Note: Nested lists (batchQueues, jobSubmissionInterfaces, dataMovementInterfaces) are handled
 * manually in the service layer due to polymorphic types and complex merging logic.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {BatchQueueMapper.class, ComputeResourceDataMovementInterfaceMapper.class})
public interface ComputeResourceMapper {

    @Mapping(target = "enabled", expression = "java(entity.getEnabled() != 0)")
    @Mapping(target = "batchQueues", ignore = true) // Handled by service layer
    @Mapping(target = "jobSubmissionInterfaces", ignore = true) // Handled by service layer (polymorphic)
    @Mapping(target = "dataMovementInterfaces", ignore = true) // Handled by service layer
    @Mapping(target = "fileSystems", ignore = true) // Handled by service layer
    ComputeResourceDescription toModel(ComputeResourceEntity entity);

    @Mapping(target = "enabled", expression = "java(model.getEnabled() ? (short)1 : (short)0)")
    @Mapping(target = "batchQueues", ignore = true) // Handled by service layer
    @Mapping(target = "jobSubmissionInterfaces", ignore = true) // Handled by service layer (polymorphic)
    @Mapping(target = "dataMovementInterfaces", ignore = true) // Handled by service layer
    ComputeResourceEntity toEntity(ComputeResourceDescription model);

    List<ComputeResourceDescription> toModelList(List<ComputeResourceEntity> entities);

    List<ComputeResourceEntity> toEntityList(List<ComputeResourceDescription> models);
}
