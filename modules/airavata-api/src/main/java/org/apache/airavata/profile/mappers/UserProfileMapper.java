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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.registry.entities.UserEntity;
import org.apache.airavata.registry.mappers.EntityMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between unified UserEntity and UserProfile model.
 * This mapper uses the unified UserEntity from registry.entities.
 *
 * <p>The UserEntity now uses OIDC standard claims. Many UserProfile fields are no longer
 * stored in the entity and should be fetched from the identity provider when needed.
 *
 * <p>Field mappings:
 * <ul>
 *   <li>userId (UserProfile) -> sub (UserEntity)</li>
 *   <li>gatewayId (UserProfile) -> gatewayId (UserEntity)</li>
 *   <li>firstName (UserProfile) -> givenName (UserEntity)</li>
 *   <li>lastName (UserProfile) -> familyName (UserEntity)</li>
 *   <li>emails (UserProfile) -> email (UserEntity) - single email extracted</li>
 *   <li>creationTime (UserProfile) -> createdAt (UserEntity)</li>
 *   <li>timeZone (UserProfile) -> zoneinfo (UserEntity)</li>
 * </ul>
 *
 * <p>The following UserProfile fields are NOT stored in UserEntity (fetch from IdP):
 * <ul>
 *   <li>userModelVersion, middleName, namePrefix, nameSuffix, orcidId</li>
 *   <li>phones, country, nationality, homeOrganization, orginationAffiliation</li>
 *   <li>lastAccessTime, validUntil, State, comments, labeledURI, gpgKey</li>
 *   <li>nsfDemographics, customDashboard</li>
 * </ul>
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface UserProfileMapper {

    /**
     * Maps UserEntity to UserProfile model.
     */
    @Mapping(target = "userId", source = "sub")
    @Mapping(target = "firstName", source = "givenName")
    @Mapping(target = "lastName", source = "familyName")
    @Mapping(target = "emails", source = "email", qualifiedByName = "emailToList")
    @Mapping(target = "creationTime", source = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(target = "lastAccessTime", source = "updatedAt", qualifiedByName = "longToLongDefault")
    @Mapping(target = "timeZone", source = "zoneinfo")
    // Fields not in OIDC-based UserEntity - set to defaults or ignore
    @Mapping(target = "userModelVersion", constant = "1.0")
    @Mapping(target = "middleName", ignore = true)
    @Mapping(target = "namePrefix", ignore = true)
    @Mapping(target = "nameSuffix", ignore = true)
    @Mapping(target = "orcidId", ignore = true)
    @Mapping(target = "phones", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "nationality", ignore = true)
    @Mapping(target = "homeOrganization", ignore = true)
    @Mapping(target = "orginationAffiliation", ignore = true)
    @Mapping(target = "validUntil", constant = "-1L")
    @Mapping(target = "state", ignore = true) // Status enum not in entity
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "labeledURI", ignore = true)
    @Mapping(target = "gpgKey", ignore = true)
    @Mapping(target = "nsfDemographics", ignore = true)
    @Mapping(target = "customDashboard", ignore = true)
    UserProfile toModel(UserEntity entity);

    /**
     * Maps UserProfile model to UserEntity.
     */
    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "givenName", source = "firstName")
    @Mapping(target = "familyName", source = "lastName")
    @Mapping(target = "email", source = "emails", qualifiedByName = "listToEmail")
    @Mapping(target = "zoneinfo", source = "timeZone")
    @Mapping(target = "airavataInternalUserId", ignore = true) // Generated from sub@gatewayId
    @Mapping(target = "createdAt", ignore = true) // Set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Managed by entity lifecycle
    @Mapping(target = "preferredUsername", ignore = true) // Not in UserProfile
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    UserEntity toEntity(UserProfile model);

    List<UserProfile> toModelList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<UserProfile> models);

    /**
     * Converts Timestamp to Long (milliseconds since epoch).
     */
    @Named("timestampToLong")
    default long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : 0L;
    }

    /**
     * Converts Long to Long with default of 0.
     */
    @Named("longToLongDefault")
    default long longToLongDefault(Long value) {
        return value != null ? value * 1000 : 0L; // updatedAt is in seconds, convert to millis
    }

    /**
     * Converts single email to list.
     */
    @Named("emailToList")
    default List<String> emailToList(String email) {
        return email != null ? Collections.singletonList(email) : Collections.emptyList();
    }

    /**
     * Extracts first email from list.
     */
    @Named("listToEmail")
    default String listToEmail(List<String> emails) {
        return emails != null && !emails.isEmpty() ? emails.get(0) : null;
    }
}
