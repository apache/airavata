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
import org.apache.airavata.common.model.IOType;
import org.apache.airavata.common.model.ParserOutput;
import org.apache.airavata.registry.entities.appcatalog.ParserOutputEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between ParserOutputEntity and ParserOutput.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ParserOutputMapper {

    @Mapping(source = "type", target = "type", qualifiedByName = "stringToIOType")
    ParserOutput toModel(ParserOutputEntity entity);

    @Mapping(source = "type", target = "type", qualifiedByName = "ioTypeToString")
    @Mapping(target = "parser", ignore = true) // Set by parent entity
    ParserOutputEntity toEntity(ParserOutput model);

    @Named("stringToIOType")
    default IOType stringToIOType(String type) {
        return type != null ? IOType.valueOf(type) : null;
    }

    @Named("ioTypeToString")
    default String ioTypeToString(IOType type) {
        return type != null ? type.name() : null;
    }

    List<ParserOutput> toModelList(List<ParserOutputEntity> entities);

    List<ParserOutputEntity> toEntityList(List<ParserOutput> models);
}
