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
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between the unified GatewayEntity and Gateway model.
 *
 * <p>This mapper handles the unified GatewayEntity which combines fields from both
 * the former ProfileGatewayEntity and expcatalog GatewayEntity.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface GatewayMapper {

    /**
     * Maps GatewayEntity to Gateway model.
     *
     * <p>Note: The following fields exist in the Gateway model but NOT in GatewayEntity
     * (credentials are managed by Keycloak, not stored in the database):
     * <ul>
     *   <li>identityServerUserName - tenant admin username (use gatewayAdminEmail instead)</li>
     *   <li>identityServerPasswordToken - credential store token (stored in GatewayResourceProfile)</li>
     *   <li>oauthClientId - OAuth client ID (retrieved from Keycloak)</li>
     *   <li>oauthClientSecret - OAuth client secret (retrieved from Keycloak)</li>
     * </ul>
     */
    @Mapping(target = "gatewayURL", source = "gatewayUrl")
    @Mapping(
            target = "requestCreationTime",
            expression =
                    "java(entity.getRequestCreationTime() != null ? entity.getRequestCreationTime().getTime() : 0L)")
    @Mapping(target = "identityServerUserName", ignore = true)
    @Mapping(target = "identityServerPasswordToken", ignore = true)
    @Mapping(target = "oauthClientId", ignore = true)
    @Mapping(target = "oauthClientSecret", ignore = true)
    Gateway toModel(GatewayEntity entity);

    /**
     * Maps Gateway model to GatewayEntity.
     *
     * <p>Note: The following fields from the Gateway model are ignored during mapping
     * because they are not stored in the database (credentials are managed by Keycloak):
     * <ul>
     *   <li>identityServerUserName - tenant admin username</li>
     *   <li>identityServerPasswordToken - credential store token</li>
     *   <li>oauthClientId - OAuth client ID</li>
     *   <li>oauthClientSecret - OAuth client secret</li>
     * </ul>
     */
    @Mapping(target = "gatewayUrl", source = "gatewayURL")
    @Mapping(
            target = "requestCreationTime",
            expression =
                    "java(model.getRequestCreationTime() > 0 ? new java.sql.Timestamp(model.getRequestCreationTime()) : null)")
    GatewayEntity toEntity(Gateway model);

    List<Gateway> toModelList(List<GatewayEntity> entities);

    List<GatewayEntity> toEntityList(List<Gateway> models);
}
