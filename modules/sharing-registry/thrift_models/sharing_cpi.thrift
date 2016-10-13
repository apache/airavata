/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

namespace java org.apache.airavata.sharing.registry.service.cpi

include "./sharing_models.thrift"

service SharingRegistryService {

    /**
     * Domain Operations
    **/
    string createDomain(1: required sharing_models.Domain domain) throws (1: sharing_models.SharingRegistryException gre)
    bool updateDomain(1: required sharing_models.Domain domain) throws (1: sharing_models.SharingRegistryException gre)
    bool deleteDomain(1: required string domainId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.Domain getDomain(1: required string domainId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.Domain> getDomains(1: required i32 offset, 2: required i32 limit) throws (1: sharing_models.SharingRegistryException gre);

    /**
     * User Operations
    **/
    string createUser(1: required sharing_models.User user) throws (1: sharing_models.SharingRegistryException gre)
    bool updatedUser(1: required sharing_models.User user) throws (1: sharing_models.SharingRegistryException gre)
    bool deleteUser(1: required string userId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.User getUser(1: required string userId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.User> getUsers(1: required string domain, 2: required i32 offset, 3: required i32 limit) throws (1: sharing_models.SharingRegistryException gre);

    /**
     * Group Operations
    **/
    string createGroup(1: required sharing_models.UserGroup group) throws (1: sharing_models.SharingRegistryException gre)
    bool updateGroup(1: required sharing_models.UserGroup group) throws (1: sharing_models.SharingRegistryException gre)
    bool deleteGroup(1: required string groupId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.UserGroup getGroup(1: required string groupId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.UserGroup> getGroups(1: required string domain, 2: required i32 offset, 3: required i32 limit)

    bool addUsersToGroup(1: required list<string> userIds, 2: required string groupId) throws (1: sharing_models.SharingRegistryException gre);
    bool removeUsersFromGroup(1: required list<string> userIds, 2: required string groupId) throws (1: sharing_models.SharingRegistryException gre);
    map<string, sharing_models.GroupChildType> getGroupMembers(1: required string groupId, 2: required i32 offset, 3: required i32 limit) throws (1: sharing_models.SharingRegistryException gre);
    bool addChildGroupToParentGroup(1: required string childId, 2: required string groupId) throws (1: sharing_models.SharingRegistryException gre);
    bool removeChildGroupFromParentGroup(1: required string childId, 2: required string groupId) throws (1: sharing_models.SharingRegistryException gre);

    /**
     * EntityType Operations
    **/
    string createEntityType(1: required sharing_models.EntityType entityType) throws (1: sharing_models.SharingRegistryException gre)
    bool updateEntityType(1: required sharing_models.EntityType entityType) throws (1: sharing_models.SharingRegistryException gre)
    bool deleteEntityType(1: required string entityTypeId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.EntityType getEntityType(1: required string entityTypeId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.EntityType> getEntityTypes(1: required string domain, 2: required i32 offset, 3: required i32 limit) throws (1: sharing_models.SharingRegistryException gre);

    /**
     * Entity Operations
    **/
    string createEntity(1: required sharing_models.Entity entity) throws (1: sharing_models.SharingRegistryException gre)
    bool updateEntity(1: required sharing_models.Entity entity) throws (1: sharing_models.SharingRegistryException gre)
    bool deleteEntity(1: required string entityId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.Entity getEntity(1: required string entityId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.Entity> searchEntities(1: required string userId, 2: required string entityTypeId, 3: required list<sharing_models.SearchCriteria> filters, 4: required i32 offset, 5: required i32 limit) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.User> getListOfSharedUsers(1: required string entityId, 2: required string permissionTypeId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.UserGroup> getListOfSharedGroups(1: required string entityId, 2: required string permissionTypeId) throws (1: sharing_models.SharingRegistryException gre)

    /**
     * Permission Operations
    **/
    string createPermissionType(1: required sharing_models.PermissionType permissionType) throws (1: sharing_models.SharingRegistryException gre)
    bool updatePermissionType(1: required sharing_models.PermissionType permissionType) throws (1: sharing_models.SharingRegistryException gre)
    bool deletePermissionType(1: required string entityTypeId) throws (1: sharing_models.SharingRegistryException gre)
    sharing_models.PermissionType getPermissionType(1: required string permissionTypeId) throws (1: sharing_models.SharingRegistryException gre)
    list<sharing_models.PermissionType> getPermissionTypes(1: required string domain, 2: required i32 offset, 3: required i32 limit) throws (1: sharing_models.SharingRegistryException gre)

    /**
     * Sharing Entity with Users and Groups
    **/
    bool shareEntityWithUsers(1: required string entityId, 2: required list<string> userList, 3: required string perssionTypeId, 4: required bool cascadePermission) throws (1: sharing_models.SharingRegistryException gre)
    bool revokeEntitySharingFromUsers(1: required string entityId, 2: required list<string> userList, 3: required string perssionTypeId ) throws (1: sharing_models.SharingRegistryException gre)
    bool shareEntityWithGroups(1: required string entityId, 2: required list<string> groupList, 3: required string perssionTypeId, 4: required bool cascadePermission) throws (1: sharing_models.SharingRegistryException gre)
    bool revokeEntitySharingFromGroups(1: required string entityId, 2: required list<string> groupList, 3: required string perssionTypeId) throws (1: sharing_models.SharingRegistryException gre)
    bool userHasAccess(1: required string domainId, 2: required string userId, 3: required string entityId, 4: required string permissionTypeId) throws (1: sharing_models.SharingRegistryException gre)
}