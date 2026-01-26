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

import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.registry.entities.UserEntity;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.apache.airavata.sharing.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between unified UserEntity and sharing User model.
 * This mapper uses the unified UserEntity from registry.entities.
 *
 * <p>Note: The sharing registry uses 'domainId' which maps to 'gatewayId' in the unified entity.
 * The UserEntity now uses OIDC standard claims (sub, givenName, familyName, email, etc.).
 *
 * <p>Field mappings:
 * <ul>
 *   <li>userId (User) -> sub (UserEntity)</li>
 *   <li>domainId (User) -> gatewayId (UserEntity)</li>
 *   <li>userName (User) -> preferredUsername (UserEntity)</li>
 *   <li>firstName (User) -> givenName (UserEntity)</li>
 *   <li>lastName (User) -> familyName (UserEntity)</li>
 *   <li>email (User) -> email (UserEntity)</li>
 *   <li>createdTime (User) -> createdAt (UserEntity)</li>
 *   <li>updatedTime (User) -> updatedAt (UserEntity)</li>
 *   <li>icon (User) -> ignored (UserEntity uses pictureUrl which is a URL string)</li>
 * </ul>
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class, implementationName = "SharingUserMapperImpl")
public interface UserMapper {

    /**
     * Maps unified UserEntity to sharing User model.
     */
    @Mapping(target = "userId", source = "sub")
    @Mapping(target = "domainId", source = "gatewayId")
    @Mapping(target = "userName", source = "preferredUsername")
    @Mapping(target = "firstName", source = "givenName")
    @Mapping(target = "lastName", source = "familyName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "createdTime", source = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(target = "updatedTime", source = "updatedAt")
    @Mapping(target = "icon", ignore = true) // UserEntity uses pictureUrl (String) not icon (byte[])
    User toModel(UserEntity entity);

    /**
     * Maps sharing User model to unified UserEntity.
     * Note: This creates a NEW entity - use updateEntityFromModel for updates.
     */
    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "preferredUsername", source = "userName")
    @Mapping(target = "givenName", source = "firstName")
    @Mapping(target = "familyName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "airavataInternalUserId", ignore = true) // Will be generated from sub@gatewayId
    @Mapping(target = "createdAt", ignore = true) // Set by @PrePersist for new entities
    @Mapping(target = "updatedAt", ignore = true) // Managed by entity lifecycle
    @Mapping(target = "pictureUrl", ignore = true) // User model uses byte[] icon, not URL
    @Mapping(target = "zoneinfo", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    UserEntity toEntity(User model);

    /**
     * Updates an existing UserEntity from a User model.
     * This preserves fields that should not be changed (like createdAt, airavataInternalUserId).
     */
    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "preferredUsername", source = "userName")
    @Mapping(target = "givenName", source = "firstName")
    @Mapping(target = "familyName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "airavataInternalUserId", ignore = true) // Preserve existing
    @Mapping(target = "createdAt", ignore = true) // Preserve existing
    @Mapping(target = "updatedAt", ignore = true) // Managed by entity lifecycle
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "zoneinfo", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    void updateEntityFromModel(User model, @MappingTarget UserEntity entity);

    List<User> toModelList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<User> models);

    /**
     * Converts Timestamp to Long timestamp (milliseconds since epoch).
     */
    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }

    /**
     * Converts Long timestamp to Timestamp.
     */
    @Named("longToTimestamp")
    default Timestamp longToTimestamp(Long millis) {
        return millis != null ? new Timestamp(millis) : null;
    }
}
