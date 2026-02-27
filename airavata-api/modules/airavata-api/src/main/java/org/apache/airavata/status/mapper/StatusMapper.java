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
package org.apache.airavata.status.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.util.EnumUtil;
import org.apache.airavata.status.entity.EventEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between EventEntity and StatusModel.
 *
 * <p>Uses generics to handle all state enum types (ExperimentState, ProcessState, TaskState, JobState)
 * through a single set of methods, eliminating the copy-paste duplication of the legacy per-domain mappers.
 */
@Component
public class StatusMapper {

    /**
     * Convert an EventEntity to a StatusModel with the given state enum type.
     */
    public <S extends Enum<S>> StatusModel<S> toStatus(EventEntity entity, Class<S> stateClass) {
        StatusModel<S> status = new StatusModel<>();
        status.setStatusId(entity.getEventId());
        status.setState(mapToState(entity.getState(), stateClass));
        status.setReason(entity.getReason());
        status.setTimeOfStateChange(
                entity.getEventTime() != null ? entity.getEventTime().toEpochMilli() : 0L);
        return status;
    }

    /**
     * Convert a list of EventEntities to StatusModels with the given state enum type.
     */
    public <S extends Enum<S>> List<StatusModel<S>> toStatusList(List<EventEntity> entities, Class<S> stateClass) {
        return entities.stream().map(e -> toStatus(e, stateClass)).toList();
    }

    /**
     * Convert a String state name to the corresponding enum constant, or null if invalid.
     */
    public <S extends Enum<S>> S mapToState(String state, Class<S> enumClass) {
        return EnumUtil.safeValueOf(enumClass, state);
    }

    public long mapInstantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
    }

    public Instant mapLongToInstant(long time) {
        return time > 0 ? Instant.ofEpochMilli(time) : null;
    }
}
