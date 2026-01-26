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
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between JobEntity and JobModel.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class)
public interface JobModelMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(target = "jobStatuses", source = "jobStatuses", qualifiedByName = "statusEntityListToJobStatusList")
    JobModel toModel(JobEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(target = "task", ignore = true) // Set by parent entity
    @Mapping(target = "jobStatuses", ignore = true) // Statuses managed separately
    JobEntity toEntity(JobModel model);

    List<JobModel> toModelList(List<JobEntity> entities);

    List<JobEntity> toEntityList(List<JobModel> models);

    @Named("statusEntityToJobStatus")
    @Mapping(target = "jobState", expression = "java(entity.getState() != null ? org.apache.airavata.common.model.JobState.valueOf(entity.getState()) : null)")
    @Mapping(target = "timeOfStateChange", expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    JobStatus statusEntityToJobStatus(StatusEntity entity);

    @Named("statusEntityListToJobStatusList")
    default List<JobStatus> statusEntityListToJobStatusList(List<StatusEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::statusEntityToJobStatus).toList();
    }
}
