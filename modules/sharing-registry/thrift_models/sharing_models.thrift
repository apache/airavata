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

 namespace java org.apache.airavata.sharing.registry.models

const string DO_NOT_SET_AT_CLIENTS_ID = "DO_NOT_SET_AT_CLIENTS_ID"

struct Domain {
    1: optional string domainId = DO_NOT_SET_AT_CLIENTS_ID,
    2: optional string name,
    3: optional string description,
    4: optional i64 createdTime,
    5: optional i64 updatedTime
}

 struct User {
     1: optional string userId = DO_NOT_SET_AT_CLIENTS_ID,
     2: optional string domainId,
     3: optional string userName,
     4: optional i64 createdTime,
     5: optional i64 updatedTime
 }

enum GroupType {
    SINGLE_USER,
    MULTI_USER
}

 struct UserGroup {
     1: optional string groupId = DO_NOT_SET_AT_CLIENTS_ID,
     2: optional string domainId,
     3: optional string name,
     6: optional string description,
     7: optional string ownerId,
     8: optional i64 createdTime,
     9: optional i64 updatedTime,
     10: optional GroupType groupType
 }

 enum GroupChildType {
    USER,
    GROUP
 }

 struct GroupMembership {
    1: optional string parentId,
    2: optional string childId,
    3: optional GroupChildType childType
    4: optional i64 createdTime,
    5: optional i64 updatedTime
 }



struct EntityType {
    1: optional string entityTypeId = DO_NOT_SET_AT_CLIENTS_ID,
    2: optional string domainId,
    3: optional string name,
    4: optional string description,
    5: optional i64 createdTime,
    6: optional i64 updatedTime
}

enum EntitySearchField {
    NAME,
    DESCRIPTION,
    FULL_TEXT,
    PRRENT_ENTITY_ID,
    CREATED_TIME,
    UPDATED_TIME
}

enum SearchCondition {
    EQUAL,
    LIKE,
    GTE,
    LTE
}

struct SearchCriteria {
    1: optional EntitySearchField searchField,
    2: optional string value,
    3: optional SearchCondition searchCondition
}

struct Entity {
    1: optional string entityId = DO_NOT_SET_AT_CLIENTS_ID,
    2: optional string domainId,
    3: optional string entityTypeId,
    4: optional string ownerId,
    5: optional string parentEntityId,
    6: optional string name,
    7: optional string description,
    8: optional map<string,string> metadata,
    9: optional string fullText,
    10: optional i64 createdTime,
    11: optional i64 updatedTime
}

struct PermissionType {
    1: optional string permissionTypeId = DO_NOT_SET_AT_CLIENTS_ID,
    2: optional string domainId,
    3: optional string name,
    4: optional string description,
    5: optional i64 createdTime,
    6: optional i64 updatedTime
}

enum SharingType {
    DIRECT_NON_CASCADING,
    DIRECT_CASCADING,
    INDIRECT_CASCADING
 }

struct Sharing {
    1: optional string permissionTypeId,
    2: optional string entityId,
    3: optional string groupId,
    4: optional SharingType sharingType,
    5: optional string inheritedParentId,
    6: optional i64 createdTime,
    7: optional i64 updatedTime
}

exception SharingRegistryException {
  1: required string message
}
