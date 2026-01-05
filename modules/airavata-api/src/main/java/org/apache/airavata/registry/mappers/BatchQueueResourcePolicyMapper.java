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
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.registry.entities.appcatalog.BatchQueueResourcePolicyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between BatchQueueResourcePolicyEntity and BatchQueueResourcePolicy.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface BatchQueueResourcePolicyMapper {

    @Mapping(
            target = "maxAllowedNodes",
            expression = "java(entity.getMaxAllowedNodes() != null ? entity.getMaxAllowedNodes() : 0)")
    @Mapping(
            target = "maxAllowedCores",
            expression = "java(entity.getMaxAllowedCores() != null ? entity.getMaxAllowedCores() : 0)")
    @Mapping(
            target = "maxAllowedWalltime",
            expression = "java(entity.getMaxAllowedWalltime() != null ? entity.getMaxAllowedWalltime() : 0)")
    BatchQueueResourcePolicy toModel(BatchQueueResourcePolicyEntity entity);

    @Mapping(
            target = "maxAllowedNodes",
            expression =
                    "java(model.getMaxAllowedNodes() != 0 ? java.lang.Integer.valueOf(model.getMaxAllowedNodes()) : null)")
    @Mapping(
            target = "maxAllowedCores",
            expression =
                    "java(model.getMaxAllowedCores() != 0 ? java.lang.Integer.valueOf(model.getMaxAllowedCores()) : null)")
    @Mapping(
            target = "maxAllowedWalltime",
            expression =
                    "java(model.getMaxAllowedWalltime() != 0 ? java.lang.Integer.valueOf(model.getMaxAllowedWalltime()) : null)")
    @Mapping(target = "groupResourceProfile", ignore = true)
    BatchQueueResourcePolicyEntity toEntity(BatchQueueResourcePolicy model);

    List<BatchQueueResourcePolicy> toModelList(List<BatchQueueResourcePolicyEntity> entities);

    List<BatchQueueResourcePolicyEntity> toEntityList(List<BatchQueueResourcePolicy> models);
}
