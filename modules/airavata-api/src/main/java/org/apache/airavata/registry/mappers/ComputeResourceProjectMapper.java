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

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.ComputeResourceProject;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourceProjectEntity;
import org.apache.airavata.registry.entities.appcatalog.ProjectQueueAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between ComputeResourceProjectEntity and ComputeResourceProject.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ComputeResourceProjectMapper {

    @Mapping(source = "queueAccess", target = "allowedQueues", qualifiedByName = "queueAccessToAllowedQueues")
    ComputeResourceProject toModel(ComputeResourceProjectEntity entity);

    @Mapping(target = "computeResourceId", ignore = true) // Set by service layer
    @Mapping(target = "computeResource", ignore = true) // Set by JPA
    @Mapping(target = "queueAccess", ignore = true) // Set by service layer after creation
    ComputeResourceProjectEntity toEntity(ComputeResourceProject model);

    List<ComputeResourceProject> toModelList(List<ComputeResourceProjectEntity> entities);

    List<ComputeResourceProjectEntity> toEntityList(List<ComputeResourceProject> models);

    @Named("queueAccessToAllowedQueues")
    default List<String> queueAccessToAllowedQueues(List<ProjectQueueAccessEntity> queueAccess) {
        if (queueAccess == null) {
            return new ArrayList<>();
        }
        List<String> allowedQueues = new ArrayList<>();
        for (ProjectQueueAccessEntity access : queueAccess) {
            if (access.isHasAccess()) {
                allowedQueues.add(access.getQueueName());
            }
        }
        return allowedQueues;
    }
}
