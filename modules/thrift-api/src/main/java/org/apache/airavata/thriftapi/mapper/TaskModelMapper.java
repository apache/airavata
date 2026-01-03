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
package org.apache.airavata.thriftapi.mapper;

import org.apache.airavata.common.model.TaskModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between domain TaskModel and thrift TaskModel.
 */
@Mapper(config = ModelMapper.class)
public interface TaskModelMapper extends ModelMapper {

    TaskModelMapper INSTANCE = Mappers.getMapper(TaskModelMapper.class);

    /**
     * Convert domain model to thrift model.
     */
    @Mapping(target = "subTaskModel", ignore = true)
    org.apache.airavata.thriftapi.model.TaskModel toThrift(TaskModel domain);

    /**
     * Convert thrift model to domain model.
     */
    @Mapping(target = "subTaskModel", ignore = true)
    TaskModel toDomain(org.apache.airavata.thriftapi.model.TaskModel thrift);
}
