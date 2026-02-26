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
package org.apache.airavata.iam.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.apache.airavata.iam.model.Domain;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between GatewayEntity and Domain model.
 *
 * <p>This mapper handles the domain view of a GatewayEntity. The Domain model provides
 * a simplified view of the gateway focused on sharing registry operations.
 *
 * <p>Field mappings:
 * <ul>
 *   <li>Domain.domainId -&gt; GatewayEntity.gatewayId</li>
 *   <li>Domain.name -&gt; GatewayEntity.gatewayName</li>
 *   <li>Domain.createdTime -&gt; GatewayEntity.createdAt (Instant to Long)</li>
 *   <li>Domain.updatedTime -&gt; GatewayEntity.updatedAt (Instant to Long)</li>
 *   <li>Domain.initialUserGroupId -&gt; GatewayEntity.initialUserGroupId</li>
 * </ul>
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface DomainMapper {

    @Named("instantToLong")
    default Long instantToLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    @Named("longToInstant")
    default Instant longToInstant(Long millis) {
        return millis == null ? null : Instant.ofEpochMilli(millis);
    }

    @Mapping(target = "domainId", source = "gatewayId")
    @Mapping(target = "name", source = "gatewayName")
    @Mapping(target = "createdTime", source = "createdAt", qualifiedByName = "instantToLong")
    @Mapping(target = "updatedTime", source = "updatedAt", qualifiedByName = "instantToLong")
    Domain toModel(GatewayEntity entity);

    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "gatewayName", source = "name")
    @Mapping(target = "createdAt", source = "createdTime", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedTime", qualifiedByName = "longToInstant")
    @Mapping(target = "domain", ignore = true)
    @Mapping(target = "emailAddress", ignore = true)
    GatewayEntity toEntity(Domain model);

    List<Domain> toModelList(List<GatewayEntity> entities);

    List<GatewayEntity> toEntityList(List<Domain> models);
}
