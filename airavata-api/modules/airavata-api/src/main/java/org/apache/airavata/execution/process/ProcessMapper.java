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
package org.apache.airavata.execution.process;

import java.util.ArrayList;
import org.apache.airavata.core.mapper.EntityMapper;
import org.springframework.stereotype.Component;

/**
 * Hand-written Spring component mapper for {@link ProcessEntity} and {@link ProcessModel}.
 *
 * <p>MapStruct is not used here because the mapping has behaviour that cannot be expressed with
 * simple {@code @Mapping} annotations:
 * <ul>
 *   <li>{@code toModel}: the transient list fields ({@code processStatuses}, {@code processErrors},
 *       {@code tasks}, {@code jobs}) are initialised to empty {@link ArrayList} instances so that
 *       callers can safely append to them without null-checking.</li>
 *   <li>{@code toEntity}: the transient list fields are intentionally not copied to the entity
 *       because they are populated at runtime from separate status/event tables and must never be
 *       persisted via the process entity itself.</li>
 * </ul>
 */
@Component
public class ProcessMapper implements EntityMapper<ProcessEntity, ProcessModel> {

    /**
     * Converts a {@link ProcessEntity} to a {@link ProcessModel}.
     *
     * <p>Transient list fields are initialised to mutable empty lists so that callers and workflow
     * activities can populate them after retrieval without null-checking.
     *
     * @param entity the JPA entity read from the database
     * @return a fully populated domain model with empty transient lists
     */
    @Override
    public ProcessModel toModel(ProcessEntity entity) {
        var model = new ProcessModel();
        model.setProcessId(entity.getProcessId());
        model.setExperimentId(entity.getExperimentId());
        model.setApplicationId(entity.getApplicationId());
        model.setResourceId(entity.getResourceId());
        model.setBindingId(entity.getBindingId());
        model.setResourceSchedule(entity.getResourceSchedule());
        model.setProviderContext(entity.getProviderContext());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        // Initialise transient lists so callers never encounter null collections.
        model.setProcessStatuses(new ArrayList<>());
        model.setProcessErrors(new ArrayList<>());
        model.setJobs(new ArrayList<>());
        return model;
    }

    /**
     * Converts a {@link ProcessModel} to a {@link ProcessEntity}.
     *
     * <p>The transient list fields ({@code processStatuses}, {@code processErrors}, {@code tasks},
     * {@code jobs}) are intentionally omitted because they are runtime-only views populated from
     * separate tables and must not be written back through the process entity.
     *
     * @param model the domain model
     * @return a JPA entity containing only the persistable scalar fields
     */
    @Override
    public ProcessEntity toEntity(ProcessModel model) {
        var entity = new ProcessEntity();
        entity.setProcessId(model.getProcessId());
        entity.setExperimentId(model.getExperimentId());
        entity.setApplicationId(model.getApplicationId());
        entity.setResourceId(model.getResourceId());
        entity.setBindingId(model.getBindingId());
        entity.setResourceSchedule(model.getResourceSchedule());
        entity.setProviderContext(model.getProviderContext());
        // Transient list fields (processStatuses, processErrors, tasks, jobs) are
        // deliberately not mapped — they are never persisted via this entity.
        return entity;
    }
}
