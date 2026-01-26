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
import org.apache.airavata.registry.entities.GatewayEntity;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.apache.airavata.sharing.model.Domain;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between GatewayEntity and Domain model.
 *
 * <p>This mapper handles the domain view of a GatewayEntity. The Domain model provides
 * a simplified view of the gateway focused on sharing registry operations.
 *
 * <p>Field mappings:
 * <ul>
 *   <li>Domain.domainId -> GatewayEntity.gatewayId</li>
 *   <li>Domain.name -> GatewayEntity.gatewayName</li>
 *   <li>Domain.description -> GatewayEntity.domainDescription</li>
 *   <li>Domain.createdTime -> GatewayEntity.domainCreatedTime</li>
 *   <li>Domain.updatedTime -> GatewayEntity.lastUpdatedTime</li>
 *   <li>Domain.initialUserGroupId -> GatewayEntity.initialUserGroupId</li>
 * </ul>
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface DomainMapper {

    @Mapping(target = "domainId", source = "gatewayId")
    @Mapping(target = "name", source = "gatewayName")
    @Mapping(target = "description", source = "domainDescription")
    @Mapping(target = "createdTime", source = "domainCreatedTime")
    @Mapping(target = "updatedTime", source = "lastUpdatedTime")
    Domain toModel(GatewayEntity entity);

    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "gatewayName", source = "name")
    @Mapping(target = "domainDescription", source = "description")
    @Mapping(target = "domainCreatedTime", source = "createdTime")
    @Mapping(target = "lastUpdatedTime", source = "updatedTime")
    @Mapping(target = "airavataInternalGatewayId", ignore = true)
    @Mapping(target = "domain", ignore = true)
    @Mapping(target = "emailAddress", ignore = true)
    @Mapping(target = "gatewayApprovalStatus", ignore = true)
    @Mapping(target = "gatewayAcronym", ignore = true)
    @Mapping(target = "gatewayUrl", ignore = true)
    @Mapping(target = "gatewayPublicAbstract", ignore = true)
    @Mapping(target = "reviewProposalDescription", ignore = true)
    @Mapping(target = "gatewayAdminFirstName", ignore = true)
    @Mapping(target = "gatewayAdminLastName", ignore = true)
    @Mapping(target = "gatewayAdminEmail", ignore = true)
    @Mapping(target = "declinedReason", ignore = true)
    @Mapping(target = "requestCreationTime", ignore = true)
    @Mapping(target = "requesterUsername", ignore = true)
    GatewayEntity toEntity(Domain model);

    List<Domain> toModelList(List<GatewayEntity> entities);

    List<GatewayEntity> toEntityList(List<Domain> models);
}
