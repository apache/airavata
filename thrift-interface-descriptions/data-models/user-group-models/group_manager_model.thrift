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

 include "../../airavata-apis/airavata_commons.thrift"

 namespace java org.apache.airavata.model.group
 namespace php Airavata.Model.Group
 namespace cpp apache.airavata.model.group
 namespace py apache.airavata.model.group

const string GROUP_MANAGER_VERSION = "1.0"


enum ResourceType {
    PROJECT,
    EXPERIMENT,
    FILE,
    OTHER
}

enum ResourcePermissionType {
    READ_WRITE
    READ_ONLY,
    WRITE_WRITE_ONLY
}

struct Resource {
    1: required string resourceId = airavata_commons.DEFAULT_ID,
    2: required string resourceName,
    3: required ResourceType resourceType,
    4: required string ownerId,
    5: optional string resourceDescription,
    6: optional i64 createdTime,
    7: optional i64 lastModifiedTime,
    8: optional string parentResourceId,
    9: optional list<Resource> childResources,
    10: optional map<string,string> metadata
}

struct Group{
    1: required string groupId = airavata_commons.DEFAULT_ID,
    2: required string groupName,
    3: optional string description,
    4: optional list<User> directGroupMemberUsers,
    5: optional list<Group> directGroupMemeberGroups,
    6: optional list<User> indirectGroupMemeberUsers,
    7: optional list<Group> indirectGroupMemeberGroups
    8: optional map<string,string> metadata
}

struct User {
    1: required string grouperUserId = airavata_commons.DEFAULT_ID,
    2: required string airavataUserId,
    3: required string userName,
    4: required string gatewayId
    5: optional map<string,string> metadata
}

enum SubjectType {
    USER,
    GROUP
}

enum GroupMembershipType {
    DIRECT,
    INDIRECT
}

struct GroupMembership{
    1: required string parentSubjectId,
    2: required string childSubjectId,
    3: required SubjectType childSubjectType,
    4: required string parentSubjectName,
    5: required string childSubjectName,
    6: required GroupMembershipType groupMembershipType
}