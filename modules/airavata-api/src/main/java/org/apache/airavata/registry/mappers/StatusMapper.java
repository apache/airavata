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

import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.registry.entities.StatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Unified MapStruct mapper for converting between StatusEntity and various status model classes.
 *
 * <p>This mapper consolidates the functionality of the legacy status mappers:
 * <ul>
 *   <li>{@code ExperimentStatusMapper}</li>
 *   <li>{@code ProcessStatusMapper}</li>
 *   <li>{@code TaskStatusMapper}</li>
 *   <li>{@code JobStatusMapper}</li>
 * </ul>
 *
 * <p>The unified {@link StatusEntity} stores the state as a String, which allows different
 * state enum types to be stored in a single table. This mapper handles the conversion
 * between the String representation and the specific enum types.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface StatusMapper {

    // ========== ExperimentStatus Mappings ==========

    /**
     * Convert StatusEntity to ExperimentStatus.
     */
    @Mapping(
            target = "timeOfStateChange",
            expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    @Mapping(target = "state", expression = "java(mapToExperimentState(entity.getState()))")
    ExperimentStatus toExperimentStatus(StatusEntity entity);

    /**
     * Convert ExperimentStatus to StatusEntity.
     * Note: parentId must be set separately after this mapping.
     * Timestamp is set immediately to ensure correct ordering (not deferred to @PrePersist).
     */
    @Mapping(
            target = "timeOfStateChange",
            expression =
                    "java(model.getTimeOfStateChange() > 0 ? new java.sql.Timestamp(model.getTimeOfStateChange()) : org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp())")
    @Mapping(target = "state", expression = "java(model.getState() != null ? model.getState().name() : null)")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parentType", constant = "EXPERIMENT")
    StatusEntity fromExperimentStatus(ExperimentStatus model);

    /**
     * Convert list of StatusEntity to list of ExperimentStatus.
     */
    List<ExperimentStatus> toExperimentStatusList(List<StatusEntity> entities);

    /**
     * Convert list of ExperimentStatus to list of StatusEntity.
     */
    List<StatusEntity> fromExperimentStatusList(List<ExperimentStatus> models);

    // ========== ProcessStatus Mappings ==========

    /**
     * Convert StatusEntity to ProcessStatus.
     */
    @Mapping(
            target = "timeOfStateChange",
            expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    @Mapping(target = "state", expression = "java(mapToProcessState(entity.getState()))")
    @Mapping(target = "processId", source = "parentId")
    ProcessStatus toProcessStatus(StatusEntity entity);

    /**
     * Convert ProcessStatus to StatusEntity.
     * Note: parentId is set from processId if available.
     * Timestamp is set immediately to ensure correct ordering (not deferred to @PrePersist).
     */
    @Mapping(
            target = "timeOfStateChange",
            expression =
                    "java(model.getTimeOfStateChange() > 0 ? new java.sql.Timestamp(model.getTimeOfStateChange()) : org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp())")
    @Mapping(target = "state", expression = "java(model.getState() != null ? model.getState().name() : null)")
    @Mapping(target = "parentId", source = "processId")
    @Mapping(target = "parentType", constant = "PROCESS")
    StatusEntity fromProcessStatus(ProcessStatus model);

    /**
     * Convert list of StatusEntity to list of ProcessStatus.
     */
    List<ProcessStatus> toProcessStatusList(List<StatusEntity> entities);

    /**
     * Convert list of ProcessStatus to list of StatusEntity.
     */
    List<StatusEntity> fromProcessStatusList(List<ProcessStatus> models);

    // ========== TaskStatus Mappings ==========

    /**
     * Convert StatusEntity to TaskStatus.
     */
    @Mapping(
            target = "timeOfStateChange",
            expression = "java(entity.getTimeOfStateChange() != null ? entity.getTimeOfStateChange().getTime() : 0L)")
    @Mapping(target = "state", expression = "java(mapToTaskState(entity.getState()))")
    TaskStatus toTaskStatus(StatusEntity entity);

    /**
     * Convert TaskStatus to StatusEntity.
     * Note: parentId must be set separately after this mapping.
     * Timestamp is set immediately to ensure correct ordering (not deferred to @PrePersist).
     */
    @Mapping(
            target = "timeOfStateChange",
            expression =
                    "java(model.getTimeOfStateChange() > 0 ? new java.sql.Timestamp(model.getTimeOfStateChange()) : org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp())")
    @Mapping(target = "state", expression = "java(model.getState() != null ? model.getState().name() : null)")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parentType", constant = "TASK")
    StatusEntity fromTaskStatus(TaskStatus model);

    /**
     * Convert list of StatusEntity to list of TaskStatus.
     */
    List<TaskStatus> toTaskStatusList(List<StatusEntity> entities);

    /**
     * Convert list of TaskStatus to list of StatusEntity.
     */
    List<StatusEntity> fromTaskStatusList(List<TaskStatus> models);

    // ========== Helper methods for state conversion ==========

    /**
     * Convert String state to ExperimentState enum.
     */
    default ExperimentState mapToExperimentState(String state) {
        if (state == null) {
            return null;
        }
        try {
            return ExperimentState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Convert String state to ProcessState enum.
     */
    default ProcessState mapToProcessState(String state) {
        if (state == null) {
            return null;
        }
        try {
            return ProcessState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Convert String state to TaskState enum.
     */
    default TaskState mapToTaskState(String state) {
        if (state == null) {
            return null;
        }
        try {
            return TaskState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Convert Timestamp to long.
     */
    default long mapTimestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : 0L;
    }

    /**
     * Convert long to Timestamp.
     */
    default Timestamp mapLongToTimestamp(long time) {
        return time > 0 ? new Timestamp(time) : null;
    }
}
