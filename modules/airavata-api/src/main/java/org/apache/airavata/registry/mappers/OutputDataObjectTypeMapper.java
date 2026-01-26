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
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.entities.OutputDataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between OutputDataEntity and OutputDataObjectType model.
 *
 * <p>This mapper uses the unified OutputDataEntity which stores outputs for all parent types
 * (experiments, processes, applications, and handlers) in a single table.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface OutputDataObjectTypeMapper {

    /**
     * Convert unified OutputDataEntity to OutputDataObjectType model.
     */
    @Mapping(target = "isRequired", source = "required")
    OutputDataObjectType toModel(OutputDataEntity entity);

    /**
     * Convert OutputDataObjectType model to unified OutputDataEntity.
     * Note: parentId and parentType must be set after mapping.
     */
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parentType", ignore = true)
    @Mapping(target = "required", source = "isRequired")
    OutputDataEntity toEntity(OutputDataObjectType model);

    /**
     * Convert list of unified OutputDataEntity to list of OutputDataObjectType models.
     */
    List<OutputDataObjectType> toModelList(List<OutputDataEntity> entities);

    /**
     * Convert list of OutputDataObjectType models to list of unified OutputDataEntity.
     * Note: parentId and parentType must be set on each entity after mapping.
     */
    List<OutputDataEntity> toEntityList(List<OutputDataObjectType> models);

    /**
     * Create an OutputDataEntity for an experiment output.
     *
     * @param model the output data object type
     * @param experimentId the experiment ID
     * @return the output data entity with parent type set to EXPERIMENT
     */
    default OutputDataEntity toExperimentOutputEntity(OutputDataObjectType model, String experimentId) {
        OutputDataEntity entity = toEntity(model);
        entity.setParentId(experimentId);
        entity.setParentType(DataObjectParentType.EXPERIMENT);
        return entity;
    }

    /**
     * Create an OutputDataEntity for a process output.
     *
     * @param model the output data object type
     * @param processId the process ID
     * @return the output data entity with parent type set to PROCESS
     */
    default OutputDataEntity toProcessOutputEntity(OutputDataObjectType model, String processId) {
        OutputDataEntity entity = toEntity(model);
        entity.setParentId(processId);
        entity.setParentType(DataObjectParentType.PROCESS);
        return entity;
    }

    /**
     * Create an OutputDataEntity for an application output.
     *
     * @param model the output data object type
     * @param applicationId the application interface ID
     * @return the output data entity with parent type set to APPLICATION
     */
    default OutputDataEntity toApplicationOutputEntity(OutputDataObjectType model, String applicationId) {
        OutputDataEntity entity = toEntity(model);
        entity.setParentId(applicationId);
        entity.setParentType(DataObjectParentType.APPLICATION);
        return entity;
    }

    /**
     * Create an OutputDataEntity for a handler output.
     *
     * @param model the output data object type
     * @param handlerId the handler ID
     * @return the output data entity with parent type set to HANDLER
     */
    default OutputDataEntity toHandlerOutputEntity(OutputDataObjectType model, String handlerId) {
        OutputDataEntity entity = toEntity(model);
        entity.setParentId(handlerId);
        entity.setParentType(DataObjectParentType.HANDLER);
        return entity;
    }

    /**
     * Create OutputDataEntity list for experiment outputs.
     *
     * @param models the list of output data object types
     * @param experimentId the experiment ID
     * @return list of output data entities with parent type set to EXPERIMENT
     */
    default List<OutputDataEntity> toExperimentOutputEntities(List<OutputDataObjectType> models, String experimentId) {
        return models.stream()
                .map(model -> toExperimentOutputEntity(model, experimentId))
                .toList();
    }

    /**
     * Create OutputDataEntity list for process outputs.
     *
     * @param models the list of output data object types
     * @param processId the process ID
     * @return list of output data entities with parent type set to PROCESS
     */
    default List<OutputDataEntity> toProcessOutputEntities(List<OutputDataObjectType> models, String processId) {
        return models.stream()
                .map(model -> toProcessOutputEntity(model, processId))
                .toList();
    }

    /**
     * Create OutputDataEntity list for application outputs.
     *
     * @param models the list of output data object types
     * @param applicationId the application interface ID
     * @return list of output data entities with parent type set to APPLICATION
     */
    default List<OutputDataEntity> toApplicationOutputEntities(
            List<OutputDataObjectType> models, String applicationId) {
        return models.stream()
                .map(model -> toApplicationOutputEntity(model, applicationId))
                .toList();
    }

    /**
     * Create OutputDataEntity list for handler outputs.
     *
     * @param models the list of output data object types
     * @param handlerId the handler ID
     * @return list of output data entities with parent type set to HANDLER
     */
    default List<OutputDataEntity> toHandlerOutputEntities(List<OutputDataObjectType> models, String handlerId) {
        return models.stream()
                .map(model -> toHandlerOutputEntity(model, handlerId))
                .toList();
    }
}
