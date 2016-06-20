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

include "./group_manager_model.thrift"

namespace java org.apache.airavata.group.cpi

service GroupManagerService {

    /**
     * Resources related API methods
    **/
    string registerResource(group_manager_model.Resource  resource);

    bool updateResource(group_manager_model.Resource  resource);

    bool deleteResource(string resourceId);

    group_manager_model.Resource getResource(string resourceId);


    /**
     * Users related API methods
    **/
    bool registerUser(group_manager_model.User user);

    bool updateUser(group_manager_model.User user);

    bool deleteUser(string grouperUserId);

    group_manager_model.User getUser(string grouperUserId);

    bool assignUserToGroup(string grouperUserId, string groupId);

    bool removeUserFromGroup(string grouperUserId, string groupId);

    list<group_manager_model.GroupMembership> getAllGroupMembershipsForUser(string grouperUserId);


    /**
     * Groups related API methods
    **/
    string registerGroup(group_manager_model.Group group);

    bool updateGroup(group_manager_model.Group group);

    bool deleteGroup(string groupId);

    group_manager_model.Group getGroup(string groupdId);

    bool addGroupToGroup(string parentGroupId, string childGroupId);

    bool removeGroupFromGroup(string parentGroupId, string childGroupId);

    list<group_manager_model.GroupMembership> getAllMembersForTheGroup(string groupId);


    /**
     * Grant Revoke Permissions API methods
    **/
    bool grantPermissions(string subjectId, group_manager_model.SubjectType subjectType, string resourceId,
        list<group_manager_model.ResourcePermissionType> grantPermissions, bool recursively)

    bool revokePermissions(string subjectId, group_manager_model.SubjectType subjectType, string resourceId,
            list<group_manager_model.ResourcePermissionType> revokePermissiones,bool recursively)

    bool revokeAllPermissions(string subjectId, group_manager_model.SubjectType subjectType, string resourceId, bool recursively)


    /**
     * Browse and Search Related API method
    **/
    list<group_manager_model.Resource> getAccessibleResourcesForUser(string grouperUserId, group_manager_model.ResourceType resourceType,
            list<group_manager_model.ResourcePermissionType> permissions, i32 offset, i32 limit)

    list<group_manager_model.Resource> searcgAccessibleResourcesForUser(string grouperUserId, group_manager_model.ResourceType resourceType,
                list<group_manager_model.ResourcePermissionType> permissions, map<string,string> filters, i32 offset, i32 limit)
}