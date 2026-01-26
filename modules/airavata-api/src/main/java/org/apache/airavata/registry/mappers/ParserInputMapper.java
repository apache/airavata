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
import org.apache.airavata.common.model.IODirection;
import org.apache.airavata.common.model.IOType;
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.registry.entities.appcatalog.ParserIOEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between ParserIOEntity and ParserInput.
 * ParserInput is stored as ParserIOEntity with direction=INPUT.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ParserInputMapper {

    /**
     * Maps ParserIOEntity to ParserInput model.
     */
    @Mapping(target = "requiredInput", source = "required")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToIOType")
    ParserInput toModel(ParserIOEntity entity);

    /**
     * Maps ParserInput model to ParserIOEntity.
     */
    @Mapping(target = "required", source = "requiredInput")
    @Mapping(target = "direction", constant = "INPUT")
    @Mapping(target = "type", source = "type", qualifiedByName = "ioTypeToString")
    @Mapping(target = "parser", ignore = true)
    ParserIOEntity toEntity(ParserInput model);

    List<ParserInput> toModelList(List<ParserIOEntity> entities);

    List<ParserIOEntity> toEntityList(List<ParserInput> models);

    /**
     * Converts String to IOType enum.
     */
    @Named("stringToIOType")
    default IOType stringToIOType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        try {
            return IOType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converts IOType enum to String.
     */
    @Named("ioTypeToString")
    default String ioTypeToString(IOType type) {
        return type != null ? type.name() : null;
    }

    /**
     * Provides the INPUT direction constant.
     */
    default IODirection inputDirection() {
        return IODirection.INPUT;
    }
}
