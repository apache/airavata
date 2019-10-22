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
package org.apache.airavata.sharing.registry;

import org.junit.Assert;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.thrift.TException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharingRegistryServerHandlerTest {
    private final static Logger logger = LoggerFactory.getLogger(SharingRegistryServerHandlerTest.class);

    @Test
    public void test() throws TException, ApplicationSettingsException {
        SharingRegistryDBInitConfig sharingRegistryDBInitConfig = new SharingRegistryDBInitConfig();
        sharingRegistryDBInitConfig.setDBInitScriptPrefix("sharing-registry");
        SharingRegistryServerHandler sharingRegistryServerHandler = new SharingRegistryServerHandler(sharingRegistryDBInitConfig);

        //Creating domain
        Domain domain = new Domain();
        String domainId = "test-domain."+System.currentTimeMillis();
        domain.setDomainId(domainId);
        domain.setName(domainId);
        domain.setDescription("test domain description");
        domain.setCreatedTime(System.currentTimeMillis());
        domain.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createDomain(domain));
        Assert.assertTrue(sharingRegistryServerHandler.getDomains(0, 10).size() > 0);

        //Creating users
        User user1 = new User();
        String userName1 = "test-user-1." + System.currentTimeMillis();
        String userId1 = domainId + ":" + userName1;
        user1.setUserId(userId1);
        user1.setUserName(userName1);
        user1.setDomainId(domainId);
        user1.setCreatedTime(System.currentTimeMillis());
        user1.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createUser(user1));

        User user2 = new User();
        String userName2 = "test-user-2." + System.currentTimeMillis();
        String userId2 = domainId + ":" + userName2;
        user2.setUserId(userId2);
        user2.setUserName(userName2);
        user2.setDomainId(domainId);
        user2.setCreatedTime(System.currentTimeMillis());
        user2.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createUser(user2));

        User user3 = new User();
        String userName3 = "test-user-3." + System.currentTimeMillis();
        String userId3 = domainId + ":" + userName3;
        user3.setUserId(userId3);
        user3.setUserName(userName3);
        user3.setDomainId(domainId);
        user3.setCreatedTime(System.currentTimeMillis());
        user3.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createUser(user3));

        User user7 = new User();
        String userName7 = "test-user-7." + System.currentTimeMillis();
        String userId7 = domainId + ":" + userName7;
        user7.setUserId(userId7);
        user7.setUserName(userName7);
        user7.setDomainId(domainId);
        user7.setCreatedTime(System.currentTimeMillis());
        user7.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createUser(user7));

        Assert.assertTrue(sharingRegistryServerHandler.getUsers(domainId, 0, 10).size() > 0);

        // Creating user groups
        UserGroup userGroup1 = new UserGroup();
        String groupName1 = "test-group-1." + System.currentTimeMillis();
        String groupId1 = domainId + ":" + groupName1;
        userGroup1.setGroupId(groupId1);
        userGroup1.setDomainId(domainId);
        userGroup1.setName(groupName1);
        userGroup1.setDescription("test group description");
        userGroup1.setOwnerId(userId1);
        userGroup1.setGroupType(GroupType.USER_LEVEL_GROUP);
        userGroup1.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup1.setCreatedTime(System.currentTimeMillis());
        userGroup1.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createGroup(userGroup1));
        Assert.assertEquals(1, sharingRegistryServerHandler.getAllMemberGroupsForUser(domainId, userId1).size());

        UserGroup userGroup2 = new UserGroup();
        String groupName2 = "test-group-2." + System.currentTimeMillis();
        String groupId2 = domainId + ":" + groupName2;
        userGroup2.setGroupId(groupId2);
        userGroup2.setDomainId(domainId);
        userGroup2.setName(groupName2);
        userGroup2.setDescription("test group description");
        userGroup2.setOwnerId(userId2);
        userGroup2.setGroupType(GroupType.USER_LEVEL_GROUP);
        userGroup2.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup2.setCreatedTime(System.currentTimeMillis());
        userGroup2.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createGroup(userGroup2));

        sharingRegistryServerHandler.addUsersToGroup(domainId, Arrays.asList(userId1), groupId1);
        sharingRegistryServerHandler.addUsersToGroup(domainId, Arrays.asList(userId7), groupId1);

        sharingRegistryServerHandler.addUsersToGroup(domainId, Arrays.asList(userId2, userId3), groupId2);
        Assert.assertEquals(1, sharingRegistryServerHandler.getAllMemberGroupsForUser(domainId, userId3).size());

        sharingRegistryServerHandler.addChildGroupsToParentGroup(domainId, Arrays.asList(groupId2), groupId1);

        Assert.assertTrue(sharingRegistryServerHandler.getGroupMembersOfTypeGroup(domainId, groupId1, 0, 10).size() == 1);
        Assert.assertTrue(sharingRegistryServerHandler.getGroupMembersOfTypeUser(domainId, groupId2, 0, 10).size() == 2);

        // Group roles tests

        // user has owner access
        Assert.assertTrue(sharingRegistryServerHandler.hasOwnerAccess(domainId, groupId1, userId1));

        // user has admin access
        Assert.assertTrue(sharingRegistryServerHandler.addGroupAdmins(domainId, groupId1, Arrays.asList(userId7)));
        Assert.assertTrue(sharingRegistryServerHandler.hasAdminAccess(domainId, groupId1, userId7));
        Assert.assertTrue(sharingRegistryServerHandler.removeGroupAdmins(domainId, groupId1, Arrays.asList(userId7)));
        Assert.assertFalse(sharingRegistryServerHandler.hasAdminAccess(domainId, groupId1, userId7));

        // transfer group ownership
        sharingRegistryServerHandler.addUsersToGroup(domainId, Arrays.asList(userId2), groupId1);
        Assert.assertTrue(sharingRegistryServerHandler.transferGroupOwnership(domainId, groupId1, userId2));
        Assert.assertTrue(sharingRegistryServerHandler.hasOwnerAccess(domainId, groupId1, userId2));
        Assert.assertTrue(sharingRegistryServerHandler.transferGroupOwnership(domainId, groupId1, userId1));
        Assert.assertFalse(sharingRegistryServerHandler.hasOwnerAccess(domainId, groupId1, userId2));

        //Creating permission types
        PermissionType permissionType1 = new PermissionType();
        String permissionName1 = "READ";
        permissionType1.setPermissionTypeId(domainId+":"+permissionName1);
        permissionType1.setDomainId(domainId);
        permissionType1.setName(permissionName1);
        permissionType1.setDescription("READ description");
        permissionType1.setCreatedTime(System.currentTimeMillis());
        permissionType1.setUpdatedTime(System.currentTimeMillis());
        String permissionTypeId1 = sharingRegistryServerHandler.createPermissionType(permissionType1);
        Assert.assertNotNull(permissionTypeId1);

        PermissionType permissionType2 = new PermissionType();
        String permissionName2 = "WRITE";
        permissionType2.setPermissionTypeId(domainId+":"+permissionName2);
        permissionType2.setDomainId(domainId);
        permissionType2.setName(permissionName2);
        permissionType2.setDescription("WRITE description");
        permissionType2.setCreatedTime(System.currentTimeMillis());
        permissionType2.setUpdatedTime(System.currentTimeMillis());
        String permissionTypeId2 = sharingRegistryServerHandler.createPermissionType(permissionType2);
        Assert.assertNotNull(permissionTypeId2);

        //Creating entity types
        EntityType entityType1 = new EntityType();
        String entityType1Name = "Project";
        entityType1.setEntityTypeId(domainId+":"+entityType1Name);
        entityType1.setDomainId(domainId);
        entityType1.setName(entityType1Name);
        entityType1.setDescription("test entity type");
        entityType1.setCreatedTime(System.currentTimeMillis());
        entityType1.setUpdatedTime(System.currentTimeMillis());
        String entityTypeId1 = sharingRegistryServerHandler.createEntityType(entityType1);
        Assert.assertNotNull(entityTypeId1);

        EntityType entityType2 = new EntityType();
        String entityType2Name = "Experiment";
        entityType2.setEntityTypeId(domainId+":"+entityType2Name);
        entityType2.setDomainId(domainId);
        entityType2.setName(entityType2Name);
        entityType2.setDescription("test entity type");
        entityType2.setCreatedTime(System.currentTimeMillis());
        entityType2.setUpdatedTime(System.currentTimeMillis());
        String entityTypeId2 = sharingRegistryServerHandler.createEntityType(entityType2);
        Assert.assertNotNull(entityTypeId2);

        EntityType entityType3 = new EntityType();
        String entityType3Name = "FileInput";
        entityType3.setEntityTypeId(domainId+":"+entityType3Name);
        entityType3.setDomainId(domainId);
        entityType3.setName(entityType3Name);
        entityType3.setDescription("file input type");
        entityType3.setCreatedTime(System.currentTimeMillis());
        entityType3.setUpdatedTime(System.currentTimeMillis());
        String entityTypeId3 = sharingRegistryServerHandler.createEntityType(entityType3);
        Assert.assertNotNull(entityTypeId3);

        EntityType entityType4 = new EntityType();
        String entityType4Name = "Application-Deployment";
        entityType4.setEntityTypeId(domainId+":"+entityType4Name);
        entityType4.setDomainId(domainId);
        entityType4.setName(entityType4Name);
        entityType4.setDescription("test entity type");
        entityType4.setCreatedTime(System.currentTimeMillis());
        entityType4.setUpdatedTime(System.currentTimeMillis());
        String entityTypeId4 = sharingRegistryServerHandler.createEntityType(entityType4);
        Assert.assertNotNull(entityTypeId4);

        //Creating Entities
        Entity entity1 = new Entity();
        entity1.setEntityId(domainId+":Entity1");
        entity1.setDomainId(domainId);
        entity1.setEntityTypeId(entityTypeId1);
        entity1.setOwnerId(userId1);
        entity1.setName("Project name 1");
        entity1.setDescription("Project description");
        entity1.setFullText("Project name project description");
        entity1.setCreatedTime(System.currentTimeMillis());
        entity1.setUpdatedTime(System.currentTimeMillis());

        String entityId1 = sharingRegistryServerHandler.createEntity(entity1);
        Assert.assertNotNull(entityId1);

        Entity entity2 = new Entity();
        entity2.setEntityId(domainId+":Entity2");
        entity2.setDomainId(domainId);
        entity2.setEntityTypeId(entityTypeId2);
        entity2.setOwnerId(userId1);
        entity2.setName("Experiment name");
        entity2.setDescription("Experiment description");
        entity2.setParentEntityId(entityId1);
        entity2.setFullText("Project name project description");
        entity2.setCreatedTime(System.currentTimeMillis());
        entity2.setUpdatedTime(System.currentTimeMillis());

        String entityId2 = sharingRegistryServerHandler.createEntity(entity2);
        Assert.assertNotNull(entityId2);

        Entity entity3 = new Entity();
        entity3.setEntityId(domainId+":Entity3");
        entity3.setDomainId(domainId);
        entity3.setEntityTypeId(entityTypeId2);
        entity3.setOwnerId(userId1);
        entity3.setName("Experiment name");
        entity3.setDescription("Experiment description");
        entity3.setParentEntityId(entityId1);
        entity3.setFullText("Project name project description");
        entity3.setCreatedTime(System.currentTimeMillis());
        entity3.setUpdatedTime(System.currentTimeMillis());

        String entityId3 = sharingRegistryServerHandler.createEntity(entity3);
        Assert.assertNotNull(entityId3);

        sharingRegistryServerHandler.shareEntityWithUsers(domainId, entityId1, Arrays.asList(userId2), permissionTypeId1, true);
        sharingRegistryServerHandler.shareEntityWithGroups(domainId, entityId3, Arrays.asList(groupId2), permissionTypeId1, true);

        Entity entity4 = new Entity();
        entity4.setEntityId(domainId+":Entity4");
        entity4.setDomainId(domainId);
        entity4.setEntityTypeId(entityTypeId3);
        entity4.setOwnerId(userId3);
        entity4.setName("Input name");
        entity4.setDescription("Input file description");
        entity4.setParentEntityId(entityId3);
        entity4.setFullText("Input File");
        entity4.setCreatedTime(System.currentTimeMillis());
        entity4.setUpdatedTime(System.currentTimeMillis());

        String entityId4 = sharingRegistryServerHandler.createEntity(entity4);
        Assert.assertNotNull(entityId4);

        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId4, permissionTypeId1));
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId2, entityId4, permissionTypeId1));
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId1, entityId4, permissionTypeId1));
        Assert.assertFalse(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId1, permissionTypeId1));

        Entity entity5 = new Entity();
        entity5.setEntityId(domainId+":Entity5");
        entity5.setDomainId(domainId);
        entity5.setEntityTypeId(entityTypeId4);
        entity5.setOwnerId(userId1);
        entity5.setName("App deployment name");
        entity5.setDescription("App deployment description");
        entity5.setFullText("App Deployment name app deployment description");
        entity5.setCreatedTime(System.currentTimeMillis());
        entity5.setUpdatedTime(System.currentTimeMillis());

        String entityId5 = sharingRegistryServerHandler.createEntity(entity5);
        Assert.assertNotNull(entityId5);

        sharingRegistryServerHandler.shareEntityWithUsers(domainId, entityId5, Arrays.asList(userId2), permissionTypeId1, true);
        sharingRegistryServerHandler.shareEntityWithGroups(domainId, entityId5, Arrays.asList(groupId2), permissionTypeId2, true);

        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId5, permissionTypeId2));
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId2, entityId5, permissionTypeId1));
        Assert.assertFalse(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId5, permissionTypeId1));

        ArrayList<SearchCriteria> filters = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.LIKE);
        searchCriteria.setValue("Input");
        searchCriteria.setSearchField(EntitySearchField.NAME);
        filters.add(searchCriteria);

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue(entityTypeId3);
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        filters.add(searchCriteria);

        Assert.assertTrue(sharingRegistryServerHandler.searchEntities(domainId, userId1, filters, 0, -1).size() > 0);

        Assert.assertNotNull(sharingRegistryServerHandler.getListOfSharedUsers(domainId, entityId1, permissionTypeId1));
        Assert.assertNotNull(sharingRegistryServerHandler.getListOfSharedGroups(domainId, entityId1, permissionTypeId1));

        Assert.assertTrue(sharingRegistryServerHandler.getListOfSharedUsers(domainId, entityId1, domainId + ":OWNER").size()==1);

        // test changing parent - old INDIRECT_CASCADING permissions removed, new is added
        // entityId2's parent is entityId1. entityId1 is shared with userId2
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId2, entityId1, permissionTypeId1));
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId2, entityId2, permissionTypeId1));
        Assert.assertFalse(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId2, permissionTypeId1));
        // create a different parent entity
        Entity entity6 = new Entity();
        entity6.setEntityId(domainId+":Entity6");
        entity6.setDomainId(domainId);
        entity6.setEntityTypeId(entityTypeId1);
        entity6.setOwnerId(userId1);
        entity6.setName("Project name 2");
        entity6.setDescription("Project description");
        entity6.setFullText("Project name project description");
        entity6.setCreatedTime(System.currentTimeMillis());
        entity6.setUpdatedTime(System.currentTimeMillis());
        String entityId6 = sharingRegistryServerHandler.createEntity(entity6);
        Assert.assertNotNull(entityId6);

        sharingRegistryServerHandler.shareEntityWithUsers(domainId, entityId6, Arrays.asList(userId3), permissionTypeId1, true);
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId6, permissionTypeId1));
        // Make sure entityId2 isn't shared with userId7 and then share it directly
        Assert.assertFalse(sharingRegistryServerHandler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1));
        sharingRegistryServerHandler.shareEntityWithUsers(domainId, entityId2, Arrays.asList(userId7), permissionTypeId1, true);
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1));
        entity2.setParentEntityId(entityId6);
        logger.debug("Updating entity2");
        Assert.assertTrue(sharingRegistryServerHandler.updateEntity(entity2));
        Entity entity2Updated = sharingRegistryServerHandler.getEntity(domainId, entityId2);
        Assert.assertEquals(entityId6, entity2Updated.getParentEntityId());
        // parent changed so entityId2 should now be shared with entityId6's shared users (userId3)
        Assert.assertFalse(sharingRegistryServerHandler.userHasAccess(domainId, userId2, entityId2, permissionTypeId1));
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId3, entityId2, permissionTypeId1));
        // entityId2 should still be shared with userId7 since that was directly shared
        Assert.assertTrue(sharingRegistryServerHandler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1));

        Assert.assertEquals(Arrays.asList(user3), sharingRegistryServerHandler.getListOfDirectlySharedUsers(domainId, entityId6, permissionTypeId1));
        Assert.assertEquals(Arrays.asList(user7), sharingRegistryServerHandler.getListOfDirectlySharedUsers(domainId, entityId2, permissionTypeId1));
        List<User> entityId2SharedUsers = sharingRegistryServerHandler.getListOfSharedUsers(domainId, entityId2, permissionTypeId1);
        Assert.assertEquals(2, entityId2SharedUsers.size());
        Assert.assertTrue("user3 and user7 in shared users", entityId2SharedUsers.contains(user3) && entityId2SharedUsers.contains(user7));
        Assert.assertEquals(1, sharingRegistryServerHandler.getListOfDirectlySharedGroups(domainId, entityId3, permissionTypeId1).size());
        Assert.assertEquals(groupId2, sharingRegistryServerHandler.getListOfDirectlySharedGroups(domainId, entityId3, permissionTypeId1).get(0).getGroupId());

        // Test that new users are added to initialUserGroupId
        UserGroup initialUserGroup = new UserGroup();
        String initialUserGroupName = "initial user group";
        String initialUserGroupId = domainId + ":" + initialUserGroupName;
        initialUserGroup.setGroupId(initialUserGroupId);
        initialUserGroup.setDomainId(domainId);
        initialUserGroup.setName(initialUserGroupName);
        initialUserGroup.setDescription("initial user group desc");
        initialUserGroup.setOwnerId(userId1);
        initialUserGroup.setGroupType(GroupType.USER_LEVEL_GROUP);
        initialUserGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        initialUserGroup.setCreatedTime(System.currentTimeMillis());
        initialUserGroup.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createGroup(initialUserGroup));

        domain.setInitialUserGroupId(initialUserGroupId);
        Assert.assertTrue(sharingRegistryServerHandler.updateDomain(domain));
        Assert.assertEquals(initialUserGroupId, sharingRegistryServerHandler.getDomain(domain.getDomainId()).getInitialUserGroupId());

        User user8 = new User();
        String userName8 = "test-user-8." + System.currentTimeMillis();
        String userId8 = domainId + ":" + userName8;
        user8.setUserId(userId8);
        user8.setUserName(userName8);
        user8.setDomainId(domainId);
        user8.setCreatedTime(System.currentTimeMillis());
        user8.setUpdatedTime(System.currentTimeMillis());

        Assert.assertNotNull(sharingRegistryServerHandler.createUser(user8));
        List<UserGroup> user8Groups = sharingRegistryServerHandler.getAllMemberGroupsForUser(domain.getDomainId(), userId8);
        Assert.assertFalse(user8Groups.isEmpty());
        Assert.assertEquals(1, user8Groups.size());
        Assert.assertEquals(initialUserGroupId, user8Groups.get(0).getGroupId());
    }
}
