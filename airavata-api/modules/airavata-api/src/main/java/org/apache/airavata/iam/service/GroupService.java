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
package org.apache.airavata.iam.service;

import java.util.List;
import org.apache.airavata.iam.exception.AuthExceptions.AuthorizationException;
import org.apache.airavata.iam.exception.GroupManagerServiceException;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.model.UserGroup;

public interface GroupService {

    String createGroup(AuthzToken authzToken, UserGroup group)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean updateGroup(AuthzToken authzToken, UserGroup group)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean deleteGroup(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    UserGroup getGroup(AuthzToken authzToken, String groupId)
            throws GroupManagerServiceException, SharingRegistryException;

    List<UserGroup> getGroups(AuthzToken authzToken) throws GroupManagerServiceException, SharingRegistryException;

    List<UserGroup> getAllGroupsUserBelongs(AuthzToken authzToken, String userName)
            throws GroupManagerServiceException, SharingRegistryException;

    boolean addUsersToGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean removeUsersFromGroup(AuthzToken authzToken, List<String> userIds, String groupId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean transferGroupOwnership(AuthzToken authzToken, String groupId, String newOwnerId)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean addGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean removeGroupAdmins(AuthzToken authzToken, String groupId, List<String> adminIds)
            throws GroupManagerServiceException, SharingRegistryException, AuthorizationException;

    boolean hasAdminAccess(AuthzToken authzToken, String groupId, String adminId)
            throws GroupManagerServiceException, SharingRegistryException;

    boolean hasOwnerAccess(AuthzToken authzToken, String groupId, String ownerId)
            throws GroupManagerServiceException, SharingRegistryException;
}
