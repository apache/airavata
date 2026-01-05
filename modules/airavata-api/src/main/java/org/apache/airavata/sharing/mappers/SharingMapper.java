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
package org.apache.airavata.sharing.mappers;

import java.util.List;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.model.Sharing;
import org.apache.airavata.sharing.model.SharingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between SharingEntity and Sharing.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface SharingMapper {

    @Mapping(source = "sharingType", target = "sharingType", qualifiedByName = "mapStringToSharingType")
    Sharing toModel(SharingEntity entity);

    @Mapping(source = "sharingType", target = "sharingType", qualifiedByName = "mapSharingTypeToString")
    SharingEntity toEntity(Sharing model);

    List<Sharing> toModelList(List<SharingEntity> entities);

    List<SharingEntity> toEntityList(List<Sharing> models);

    @Named("mapStringToSharingType")
    default SharingType mapStringToSharingType(String sharingType) {
        return sharingType != null ? SharingType.valueOf(sharingType) : null;
    }

    @Named("mapSharingTypeToString")
    default String mapSharingTypeToString(SharingType sharingType) {
        return sharingType != null ? sharingType.name() : null;
    }
}
