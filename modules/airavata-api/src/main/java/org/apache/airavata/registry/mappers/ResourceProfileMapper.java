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
import java.util.stream.Collectors;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.common.model.ProfileOwnerType;
import org.apache.airavata.common.model.UserResourceProfile;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between ResourceProfileEntity and the legacy
 * GatewayResourceProfile/UserResourceProfile model objects.
 *
 * <p>This mapper provides backward compatibility by supporting conversion to/from
 * both the gateway and user profile model types.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ResourceProfileMapper {

    // --- Gateway Profile Mappings ---

    /**
     * Convert a ResourceProfileEntity to GatewayResourceProfile.
     * Only valid for entities with profileType = GATEWAY.
     */
    @Mapping(source = "gatewayId", target = "gatewayID")
    @Mapping(target = "computeResourcePreferences", ignore = true)
    @Mapping(target = "storagePreferences", ignore = true)
    GatewayResourceProfile toGatewayResourceProfile(ResourceProfileEntity entity);

    /**
     * Convert a GatewayResourceProfile to ResourceProfileEntity.
     */
    @Named("gatewayToEntity")
    default ResourceProfileEntity gatewayToEntity(GatewayResourceProfile model) {
        if (model == null) {
            return null;
        }
        ResourceProfileEntity entity = ResourceProfileEntity.forGateway(model.getGatewayID());
        entity.setCredentialStoreToken(model.getCredentialStoreToken());
        entity.setIdentityServerPwdCredToken(model.getIdentityServerPwdCredToken());
        entity.setIdentityServerTenant(model.getIdentityServerTenant());
        return entity;
    }

    /**
     * Convert a list of ResourceProfileEntity to GatewayResourceProfile list.
     * Filters to only include GATEWAY type entities.
     */
    default List<GatewayResourceProfile> toGatewayResourceProfileList(List<ResourceProfileEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .filter(e -> e.getProfileType() == ProfileOwnerType.GATEWAY)
                .map(this::toGatewayResourceProfile)
                .collect(Collectors.toList());
    }

    // --- User Profile Mappings ---

    /**
     * Convert a ResourceProfileEntity to UserResourceProfile.
     * Only valid for entities with profileType = USER.
     */
    default UserResourceProfile toUserResourceProfile(ResourceProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getProfileType() != ProfileOwnerType.USER) {
            throw new IllegalArgumentException("Cannot convert non-USER profile to UserResourceProfile");
        }
        UserResourceProfile profile = new UserResourceProfile();
        profile.setUserId(entity.getUserId());
        profile.setGatewayID(entity.getGatewayId());
        profile.setCredentialStoreToken(entity.getCredentialStoreToken());
        profile.setIdentityServerPwdCredToken(entity.getIdentityServerPwdCredToken());
        profile.setIdentityServerTenant(entity.getIdentityServerTenant());
        return profile;
    }

    /**
     * Convert a UserResourceProfile to ResourceProfileEntity.
     */
    @Named("userToEntity")
    default ResourceProfileEntity userToEntity(UserResourceProfile model) {
        if (model == null) {
            return null;
        }
        ResourceProfileEntity entity = ResourceProfileEntity.forUser(model.getUserId(), model.getGatewayID());
        entity.setCredentialStoreToken(model.getCredentialStoreToken());
        entity.setIdentityServerPwdCredToken(model.getIdentityServerPwdCredToken());
        entity.setIdentityServerTenant(model.getIdentityServerTenant());
        return entity;
    }

    /**
     * Convert a list of ResourceProfileEntity to UserResourceProfile list.
     * Filters to only include USER type entities.
     */
    default List<UserResourceProfile> toUserResourceProfileList(List<ResourceProfileEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .filter(e -> e.getProfileType() == ProfileOwnerType.USER)
                .map(this::toUserResourceProfile)
                .collect(Collectors.toList());
    }
}
