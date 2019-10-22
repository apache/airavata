/**
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
 */
package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.entities.GroupMembershipEntity;
import org.apache.airavata.sharing.registry.db.entities.GroupMembershipPK;
import org.apache.airavata.sharing.registry.db.entities.UserEntity;
import org.apache.airavata.sharing.registry.db.entities.UserGroupEntity;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.models.*;

import java.util.*;

public class GroupMembershipRepository extends AbstractRepository<GroupMembership, GroupMembershipEntity, GroupMembershipPK> {

    public GroupMembershipRepository() {
        super(GroupMembership.class, GroupMembershipEntity.class);
    }

    public List<User> getAllChildUsers(String domainId, String groupId) throws SharingRegistryException {
        String queryString = "SELECT DISTINCT U FROM " + UserEntity.class.getSimpleName() + " U, " + GroupMembershipEntity.class.getSimpleName()
                + " GM WHERE GM." + DBConstants.GroupMembershipTable.CHILD_ID + " = U." + DBConstants.UserTable.USER_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID + " = U." + DBConstants.UserTable.DOMAIN_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID + "=:" + DBConstants.GroupMembershipTable.DOMAIN_ID + " AND "+
                "GM." + DBConstants.GroupMembershipTable.PARENT_ID + "=:" + DBConstants.GroupMembershipTable.PARENT_ID + " AND GM." + DBConstants.GroupMembershipTable.CHILD_TYPE
                + "=:" + DBConstants.GroupMembershipTable.CHILD_TYPE;
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.GroupMembershipTable.PARENT_ID, groupId);
        queryParameters.put(DBConstants.GroupMembershipTable.CHILD_TYPE, GroupChildType.USER.toString());
        UserRepository userRepository = new UserRepository();
        List<User> users = userRepository.select(queryString, queryParameters, 0, -1);
        return users;
    }

    public List<UserGroup> getAllChildGroups(String domainId, String groupId) throws SharingRegistryException {
        String queryString = "SELECT DISTINCT G FROM " + UserGroupEntity.class.getSimpleName() + " G, " + GroupMembershipEntity.class.getSimpleName()
                + " GM WHERE GM." + DBConstants.GroupMembershipTable.CHILD_ID + " = G." + DBConstants.UserGroupTable.GROUP_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID + " = G." + DBConstants.UserGroupTable.DOMAIN_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID+"=:"+DBConstants.GroupMembershipTable.DOMAIN_ID + " AND "+
                "GM." + DBConstants.GroupMembershipTable.PARENT_ID+"=:"+DBConstants.GroupMembershipTable.PARENT_ID + " AND GM." + DBConstants.GroupMembershipTable.CHILD_TYPE
                + "=:" + DBConstants.GroupMembershipTable.CHILD_TYPE;
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.GroupMembershipTable.PARENT_ID, groupId);
        queryParameters.put(DBConstants.GroupMembershipTable.CHILD_TYPE, GroupChildType.GROUP.toString());
        UserGroupRepository userGroupRepository = new UserGroupRepository();
        List<UserGroup> groups = userGroupRepository.select(queryString, queryParameters,0, -1);
        return groups;
    }

    public List<UserGroup> getAllMemberGroupsForUser(String domainId, String userId) throws SharingRegistryException {
        String queryString = "SELECT DISTINCT G FROM " + UserGroupEntity.class.getSimpleName() + " G, " + GroupMembershipEntity.class.getSimpleName()
                + " GM WHERE GM." + DBConstants.GroupMembershipTable.PARENT_ID + " = G." + DBConstants.UserGroupTable.GROUP_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID + " = G." + DBConstants.UserGroupTable.DOMAIN_ID + " AND " +
                "GM." + DBConstants.GroupMembershipTable.DOMAIN_ID+"=:"+DBConstants.GroupMembershipTable.DOMAIN_ID + " AND "+
                "GM." + DBConstants.GroupMembershipTable.CHILD_ID+"=:"+DBConstants.GroupMembershipTable.CHILD_ID + " AND GM." + DBConstants.GroupMembershipTable.CHILD_TYPE
                + "=:" + DBConstants.GroupMembershipTable.CHILD_TYPE + " AND " +
                "G." + DBConstants.UserGroupTable.GROUP_CARDINALITY + "=:" + DBConstants.UserGroupTable.GROUP_CARDINALITY;
        Map<String,Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        queryParameters.put(DBConstants.GroupMembershipTable.CHILD_ID, userId);
        queryParameters.put(DBConstants.GroupMembershipTable.CHILD_TYPE, GroupChildType.USER.toString());
        queryParameters.put(DBConstants.UserGroupTable.GROUP_CARDINALITY, GroupCardinality.MULTI_USER.name());
        UserGroupRepository userGroupRepository = new UserGroupRepository();
        List<UserGroup> groups = userGroupRepository.select(queryString, queryParameters, 0, -1);
        return groups;
    }

    public List<GroupMembership> getAllParentMembershipsForChild(String domainId, String childId) throws SharingRegistryException {
        List<GroupMembership> finalParentGroups = new ArrayList<>();
        Map<String, String> filters = new HashMap<>();
        filters.put(DBConstants.GroupMembershipTable.CHILD_ID, childId);
        filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
        LinkedList<GroupMembership> temp = new LinkedList<>();
        select(filters, 0, -1).stream().forEach(m -> temp.addLast(m));
        while (temp.size() > 0){
            GroupMembership gm = temp.pop();
            filters = new HashMap<>();
            filters.put(DBConstants.GroupMembershipTable.CHILD_ID, gm.getParentId());
            filters.put(DBConstants.GroupMembershipTable.DOMAIN_ID, domainId);
            select(filters, 0, -1).stream().forEach(m -> temp.addLast(m));
            finalParentGroups.add(gm);
        }
        return finalParentGroups;
    }
}