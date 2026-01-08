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
package org.apache.airavata.profile.mappers;

import java.util.List;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.profile.entities.ProfileGatewayEntity;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between profile GatewayEntity and Gateway model.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class, implementationName = "ProfileGatewayMapperImpl")
public interface GatewayMapper {

    @Mapping(target = "gatewayURL", source = "gatewayUrl")
    @Mapping(
            target = "gatewayApprovalStatus",
            expression =
                    "java(entity.getGatewayApprovalStatus() != null ? org.apache.airavata.common.model.GatewayApprovalStatus.valueOf(entity.getGatewayApprovalStatus()) : null)")
    Gateway toModel(ProfileGatewayEntity entity);

    @Mapping(target = "gatewayUrl", source = "gatewayURL")
    @Mapping(
            target = "gatewayApprovalStatus",
            expression =
                    "java(model.getGatewayApprovalStatus() != null ? model.getGatewayApprovalStatus().name() : null)")
    ProfileGatewayEntity toEntity(Gateway model);

    List<Gateway> toModelList(List<ProfileGatewayEntity> entities);

    List<ProfileGatewayEntity> toEntityList(List<Gateway> models);
}
