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

import org.apache.airavata.common.model.DBEventPublisherContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between domain DBEventPublisherContext and thrift DBEventPublisherContext.
 */
@Mapper(config = ModelMapper.class)
public interface DBEventPublisherContextMapper extends ModelMapper {

    DBEventPublisherContextMapper INSTANCE = Mappers.getMapper(DBEventPublisherContextMapper.class);

    /**
     * Convert domain model to thrift model.
     */
    @Mapping(target = "entityDataModel", ignore = true)
    @Mapping(target = "crudType", ignore = true)
    org.apache.airavata.thriftapi.model.DBEventPublisherContext toThrift(DBEventPublisherContext domain);

    /**
     * Convert thrift model to domain model.
     */
    @Mapping(target = "entityDataModel", ignore = true)
    @Mapping(target = "crudType", ignore = true)
    DBEventPublisherContext toDomain(org.apache.airavata.thriftapi.model.DBEventPublisherContext thrift);
}
