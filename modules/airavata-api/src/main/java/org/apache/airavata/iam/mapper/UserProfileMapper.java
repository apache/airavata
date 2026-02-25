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

import org.apache.airavata.config.EntityMapperConfiguration;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.apache.airavata.iam.entity.UserEntity;
import org.apache.airavata.iam.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Maps minimal UserEntity to UserProfile. Profile fields (firstName, lastName, email, etc.)
 * are NOT in the entity; they must be enriched from IAM by the service layer.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface UserProfileMapper {

    @Mapping(target = "userId", source = "sub")
    @Mapping(target = "airavataInternalUserId", source = "userId")
    @Mapping(target = "gatewayId", source = "gatewayId")
    @Mapping(target = "creationTime", source = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "emails", ignore = true)
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "lastAccessTime", ignore = true)
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
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "labeledURI", ignore = true)
    @Mapping(target = "gpgKey", ignore = true)
    UserProfile toModel(UserEntity entity);

    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "gatewayId", source = "gatewayId")
    @Mapping(target = "personalGroupId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    UserEntity toEntity(UserProfile model);

    List<UserProfile> toModelList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<UserProfile> models);

    @Named("timestampToLong")
    default long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : 0L;
    }

    default List<String> emailToList(String email) {
        return email != null ? Collections.singletonList(email) : Collections.emptyList();
    }

    default String listToEmail(List<String> emails) {
        return emails != null && !emails.isEmpty() ? emails.get(0) : null;
    }
}
