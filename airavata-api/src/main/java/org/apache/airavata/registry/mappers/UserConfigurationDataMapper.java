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
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.registry.entities.expcatalog.UserConfigurationDataEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between UserConfigurationDataEntity and UserConfigurationDataModel.
 * Note: computationalResourceScheduling is mapped from entity fields using @AfterMapping.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {ComputationalResourceSchedulingMapper.class})
public interface UserConfigurationDataMapper {

    @Mapping(target = "computationalResourceScheduling", ignore = true) // Set in @AfterMapping
    UserConfigurationDataModel toModel(UserConfigurationDataEntity entity);

    @Mapping(target = "resourceHostId", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "totalCPUCount", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "nodeCount", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "numberOfThreads", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "queueName", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "wallTimeLimit", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "totalPhysicalMemory", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "staticWorkingDir", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "overrideLoginUserName", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "overrideScratchLocation", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "overrideAllocationProjectNumber", ignore = true) // Set from computationalResourceScheduling
    @Mapping(target = "experiment", ignore = true) // Set by parent entity
    @Mapping(target = "autoScheduledCompResourceSchedulingList", ignore = true) // Handled separately
    UserConfigurationDataEntity toEntity(UserConfigurationDataModel model);

    @AfterMapping
    default void setComputationalResourceScheduling(
            @MappingTarget UserConfigurationDataModel model, UserConfigurationDataEntity entity) {
        // Map entity fields to computationalResourceScheduling nested object
        ComputationalResourceSchedulingModel crs = new ComputationalResourceSchedulingModel();
        crs.setResourceHostId(entity.getResourceHostId());
        crs.setTotalCPUCount(entity.getTotalCPUCount());
        crs.setNodeCount(entity.getNodeCount());
        crs.setNumberOfThreads(entity.getNumberOfThreads());
        crs.setQueueName(entity.getQueueName());
        crs.setWallTimeLimit(entity.getWallTimeLimit());
        crs.setTotalPhysicalMemory(entity.getTotalPhysicalMemory());
        crs.setStaticWorkingDir(entity.getStaticWorkingDir());
        crs.setOverrideLoginUserName(entity.getOverrideLoginUserName());
        crs.setOverrideScratchLocation(entity.getOverrideScratchLocation());
        crs.setOverrideAllocationProjectNumber(entity.getOverrideAllocationProjectNumber());
        model.setComputationalResourceScheduling(crs);
    }

    @AfterMapping
    default void setEntityFieldsFromComputationalResourceScheduling(
            @MappingTarget UserConfigurationDataEntity entity, UserConfigurationDataModel model) {
        // Map computationalResourceScheduling nested object to entity fields
        if (model.getComputationalResourceScheduling() != null) {
            ComputationalResourceSchedulingModel crs = model.getComputationalResourceScheduling();
            entity.setResourceHostId(crs.getResourceHostId());
            entity.setTotalCPUCount(crs.getTotalCPUCount());
            entity.setNodeCount(crs.getNodeCount());
            entity.setNumberOfThreads(crs.getNumberOfThreads());
            entity.setQueueName(crs.getQueueName());
            entity.setWallTimeLimit(crs.getWallTimeLimit());
            entity.setTotalPhysicalMemory(crs.getTotalPhysicalMemory());
            entity.setStaticWorkingDir(crs.getStaticWorkingDir());
            entity.setOverrideLoginUserName(crs.getOverrideLoginUserName());
            entity.setOverrideScratchLocation(crs.getOverrideScratchLocation());
            entity.setOverrideAllocationProjectNumber(crs.getOverrideAllocationProjectNumber());
        }
    }

    List<UserConfigurationDataModel> toModelList(List<UserConfigurationDataEntity> entities);

    List<UserConfigurationDataEntity> toEntityList(List<UserConfigurationDataModel> models);
}
