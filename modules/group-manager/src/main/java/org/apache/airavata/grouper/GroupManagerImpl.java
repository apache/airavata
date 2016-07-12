/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.grouper;

import org.apache.airavata.grouper.group.Group;
import org.apache.airavata.grouper.group.GroupMembership;
import org.apache.airavata.grouper.group.GroupServiceImpl;
import org.apache.airavata.grouper.permission.PermissionAction;
import org.apache.airavata.grouper.permission.PermissionServiceImpl;
import org.apache.airavata.grouper.resource.Resource;
import org.apache.airavata.grouper.resource.ResourceNotFoundException;
import org.apache.airavata.grouper.resource.ResourceServiceImpl;
import org.apache.airavata.grouper.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupManagerImpl implements GroupManagerCPI {
    private final static Logger logger = LoggerFactory.getLogger(GroupManagerImpl.class);

    private ResourceServiceImpl resourceService;
    private PermissionServiceImpl permissionService;
    private GroupServiceImpl groupService;

    public GroupManagerImpl(){
        this.resourceService = new ResourceServiceImpl();
        this.permissionService = new PermissionServiceImpl();
        this.groupService = new GroupServiceImpl();
    }

    @Override
    public void createResource(Resource projectResource) {
        resourceService.createResource(projectResource);
    }

    @Override
    public boolean isResourceRegistered(String resourceId, ResourceType resourceType) {
        try{
            return resourceService.getResource(resourceId, resourceType) != null;
        }catch (ResourceNotFoundException ex){
            return false;
        }
    }

    @Override
    public void grantPermission(String userId, SubjectType subjectType, String resourceId, ResourceType resourceType,
                                PermissionAction permissionAction) {
        permissionService.grantPermission(userId, subjectType, resourceId, resourceType, permissionAction);
    }

    @Override
    public void revokePermission(String userId, SubjectType subjectType, String resourceId, ResourceType resourceType,
                                 PermissionAction action) {
        permissionService.revokePermission(userId, subjectType, resourceId, resourceType, action);
    }

    @Override
    public Set<String> getAllAccessibleUsers(String resourceId, ResourceType resourceType, PermissionAction permissionType) {
        return resourceService.getAllAccessibleUsers(resourceId, resourceType, permissionType);
    }

    @Override
    public List<String> getAccessibleResourcesForUser(String userId, ResourceType resourceType, PermissionAction permissionAction) {
        Set<Resource> allResources = resourceService.getAccessibleResourcesForUser(userId, resourceType, permissionAction, false, 0, -1);
        List<String> ids = new ArrayList<>(allResources.size());
        allResources.stream().forEach(r->ids.add(r.getId()));
        return ids;
    }

    @Override
    public void createGroup(Group group) {
        groupService.createGroup(group);
    }

    @Override
    public void updateGroup(Group group) {
        groupService.updateGroup(group);
    }

    @Override
    public void deleteGroup(String groupId, String userId) {
        groupService.deleteGroup(groupId, userId);
    }

    @Override
    public Group getGroup(String groupId) {
        return groupService.getGroup(groupId);
    }

    @Override
    public List<Group> getAllGroupsUserBelongs(String userId) {
        List<GroupMembership> groupMemberships = groupService.getAllMembershipsForUser(userId);
        List<Group> groupList = new ArrayList<>();
        groupMemberships.stream().forEach(gm->groupList.add(getGroup(gm.getGroupId())));
        return groupList;
    }
}