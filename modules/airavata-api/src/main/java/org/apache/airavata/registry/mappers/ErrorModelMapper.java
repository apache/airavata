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
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.registry.entities.ErrorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ErrorEntity and ErrorModel.
 *
 * <p>This mapper uses the unified {@link ErrorEntity} which consolidates error records
 * from experiments, processes, tasks, workflows, applications, and handlers.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ErrorModelMapper {

    // ========== Unified ErrorEntity Mappings ==========

    /**
     * Convert unified ErrorEntity to ErrorModel.
     */
    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(entity.getRootCauseErrorIdList() != null && !entity.getRootCauseErrorIdList().isEmpty() ? java.util.Arrays.asList(entity.getRootCauseErrorIdList().split(\",\")) : java.util.Collections.emptyList())")
    ErrorModel toModel(ErrorEntity entity);

    /**
     * Convert ErrorModel to unified ErrorEntity.
     * Note: parentId and parentType must be set separately after this mapping.
     */
    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "rootCauseErrorIdList",
            expression =
                    "java(model.getRootCauseErrorIdList() != null && !model.getRootCauseErrorIdList().isEmpty() ? String.join(\",\", model.getRootCauseErrorIdList()) : null)")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "parentType", ignore = true)
    ErrorEntity toEntity(ErrorModel model);

    /**
     * Convert list of unified ErrorEntity to list of ErrorModel.
     */
    List<ErrorModel> toModelList(List<ErrorEntity> entities);

    /**
     * Convert list of ErrorModel to list of unified ErrorEntity.
     */
    List<ErrorEntity> toEntityList(List<ErrorModel> models);
}
