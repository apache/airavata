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
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.registry.entities.expcatalog.QueueStatusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between QueueStatusEntity and QueueStatusModel.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface QueueStatusMapper {

    @Mapping(target = "time", expression = "java(entity.getTime() != null ? entity.getTime().longValue() : 0L)")
    @Mapping(target = "runningJobs", expression = "java(entity.isRunningJobs() ? 1 : 0)")
    QueueStatusModel toModel(QueueStatusEntity entity);

    @Mapping(
            target = "time",
            expression = "java(model.getTime() > 0 ? java.math.BigInteger.valueOf(model.getTime()) : null)")
    @Mapping(target = "runningJobs", expression = "java(model.getRunningJobs() > 0)")
    QueueStatusEntity toEntity(QueueStatusModel model);

    List<QueueStatusModel> toModelList(List<QueueStatusEntity> entities);

    List<QueueStatusEntity> toEntityList(List<QueueStatusModel> models);
}
