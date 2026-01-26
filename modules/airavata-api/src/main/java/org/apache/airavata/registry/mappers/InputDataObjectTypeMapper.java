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
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between InputDataEntity and InputDataObjectType model.
 *
 * <p>This mapper uses the unified InputDataEntity which stores inputs for all parent types
 * (experiments, processes, applications, and handlers) in a single table.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface InputDataObjectTypeMapper {

    /**
     * Convert unified InputDataEntity to InputDataObjectType model.
     */
    @Mapping(target = "isRequired", source = "required")
    @Mapping(target = "isReadOnly", source = "readOnly")
    InputDataObjectType toModel(InputDataEntity entity);

    /**
     * Convert InputDataObjectType model to unified InputDataEntity.
     * Note: parentId and parentType must be set after mapping.
     */
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parentType", ignore = true)
    @Mapping(target = "required", source = "isRequired")
    @Mapping(target = "readOnly", source = "isReadOnly")
    InputDataEntity toEntity(InputDataObjectType model);

    /**
     * Convert list of unified InputDataEntity to list of InputDataObjectType models.
     */
    List<InputDataObjectType> toModelList(List<InputDataEntity> entities);

    /**
     * Convert list of InputDataObjectType models to list of unified InputDataEntity.
     * Note: parentId and parentType must be set on each entity after mapping.
     */
    List<InputDataEntity> toEntityList(List<InputDataObjectType> models);

    /**
     * Create an InputDataEntity for an experiment input.
     *
     * @param model the input data object type
     * @param experimentId the experiment ID
     * @return the input data entity with parent type set to EXPERIMENT
     */
    default InputDataEntity toExperimentInputEntity(InputDataObjectType model, String experimentId) {
        InputDataEntity entity = toEntity(model);
        entity.setParentId(experimentId);
        entity.setParentType(DataObjectParentType.EXPERIMENT);
        return entity;
    }

    /**
     * Create an InputDataEntity for a process input.
     *
     * @param model the input data object type
     * @param processId the process ID
     * @return the input data entity with parent type set to PROCESS
     */
    default InputDataEntity toProcessInputEntity(InputDataObjectType model, String processId) {
        InputDataEntity entity = toEntity(model);
        entity.setParentId(processId);
        entity.setParentType(DataObjectParentType.PROCESS);
        return entity;
    }

    /**
     * Create an InputDataEntity for an application input.
     *
     * @param model the input data object type
     * @param applicationId the application interface ID
     * @return the input data entity with parent type set to APPLICATION
     */
    default InputDataEntity toApplicationInputEntity(InputDataObjectType model, String applicationId) {
        InputDataEntity entity = toEntity(model);
        entity.setParentId(applicationId);
        entity.setParentType(DataObjectParentType.APPLICATION);
        return entity;
    }

    /**
     * Create an InputDataEntity for a handler input.
     *
     * @param model the input data object type
     * @param handlerId the handler ID
     * @return the input data entity with parent type set to HANDLER
     */
    default InputDataEntity toHandlerInputEntity(InputDataObjectType model, String handlerId) {
        InputDataEntity entity = toEntity(model);
        entity.setParentId(handlerId);
        entity.setParentType(DataObjectParentType.HANDLER);
        return entity;
    }

    /**
     * Create InputDataEntity list for experiment inputs.
     *
     * @param models the list of input data object types
     * @param experimentId the experiment ID
     * @return list of input data entities with parent type set to EXPERIMENT
     */
    default List<InputDataEntity> toExperimentInputEntities(List<InputDataObjectType> models, String experimentId) {
        return models.stream()
                .map(model -> toExperimentInputEntity(model, experimentId))
                .toList();
    }

    /**
     * Create InputDataEntity list for process inputs.
     *
     * @param models the list of input data object types
     * @param processId the process ID
     * @return list of input data entities with parent type set to PROCESS
     */
    default List<InputDataEntity> toProcessInputEntities(List<InputDataObjectType> models, String processId) {
        return models.stream()
                .map(model -> toProcessInputEntity(model, processId))
                .toList();
    }

    /**
     * Create InputDataEntity list for application inputs.
     *
     * @param models the list of input data object types
     * @param applicationId the application interface ID
     * @return list of input data entities with parent type set to APPLICATION
     */
    default List<InputDataEntity> toApplicationInputEntities(List<InputDataObjectType> models, String applicationId) {
        return models.stream()
                .map(model -> toApplicationInputEntity(model, applicationId))
                .toList();
    }

    /**
     * Create InputDataEntity list for handler inputs.
     *
     * @param models the list of input data object types
     * @param handlerId the handler ID
     * @return list of input data entities with parent type set to HANDLER
     */
    default List<InputDataEntity> toHandlerInputEntities(List<InputDataObjectType> models, String handlerId) {
        return models.stream()
                .map(model -> toHandlerInputEntity(model, handlerId))
                .toList();
    }
}
