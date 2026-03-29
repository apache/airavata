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
package org.apache.airavata.sharing.mapper;

import org.apache.airavata.sharing.model.*;
import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntityType;
import org.apache.airavata.sharing.registry.models.GroupAdmin;
import org.apache.airavata.sharing.registry.models.GroupMembership;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.models.Sharing;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SharingMapper {

    SharingMapper INSTANCE = Mappers.getMapper(SharingMapper.class);

    // --- Domain ---
    Domain domainToModel(DomainEntity entity);

    DomainEntity domainToEntity(Domain model);

    // --- User ---
    User userToModel(UserEntity entity);

    UserEntity userToEntity(User model);

    // --- UserGroup ---
    UserGroup userGroupToModel(UserGroupEntity entity);

    UserGroupEntity userGroupToEntity(UserGroup model);

    // --- GroupAdmin ---
    GroupAdmin groupAdminToModel(GroupAdminEntity entity);

    GroupAdminEntity groupAdminToEntity(GroupAdmin model);

    // --- GroupMembership ---
    GroupMembership groupMembershipToModel(GroupMembershipEntity entity);

    GroupMembershipEntity groupMembershipToEntity(GroupMembership model);

    // --- EntityType ---
    EntityType entityTypeToModel(EntityTypeEntity entity);

    EntityTypeEntity entityTypeToEntity(EntityType model);

    // --- Entity ---
    Entity entityToModel(EntityEntity entity);

    EntityEntity entityToEntity(Entity model);

    // --- PermissionType ---
    PermissionType permissionTypeToModel(PermissionTypeEntity entity);

    PermissionTypeEntity permissionTypeToEntity(PermissionType model);

    // --- Sharing ---
    Sharing sharingToModel(SharingEntity entity);

    SharingEntity sharingToEntity(Sharing model);
}
