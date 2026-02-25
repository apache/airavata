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
import java.util.List;
import org.apache.airavata.iam.entity.UserEntity;
import org.apache.airavata.iam.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Maps UserEntity to sharing User model.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfiguration.class,
        implementationName = "SharingUserMapperImpl")
public interface SharingUserMapper {

    @Mapping(target = "userId", source = "sub")
    @Mapping(target = "domainId", source = "gatewayId")
    @Mapping(target = "createdTime", source = "createdAt", qualifiedByName = "timestampToLong")
    @Mapping(target = "userName", expression = "java(entity.getSub() != null ? entity.getSub() : entity.getUserId())")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "icon", ignore = true)
    User toModel(UserEntity entity);

    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    @Mapping(target = "personalGroupId", ignore = true)
    UserEntity toEntity(User model);

    @Mapping(target = "sub", source = "userId")
    @Mapping(target = "gatewayId", source = "domainId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    @Mapping(target = "personalGroupId", ignore = true)
    void updateEntityFromModel(User model, @MappingTarget UserEntity entity);

    List<User> toModelList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<User> models);

    @Named("timestampToLong")
    default Long timestampToLong(Timestamp timestamp) {
        return timestamp != null ? timestamp.getTime() : null;
    }
}
