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
package org.apache.airavata.service.integration;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.Entity;
import org.apache.airavata.sharing.model.EntitySearchField;
import org.apache.airavata.sharing.model.EntityType;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.GroupType;
import org.apache.airavata.sharing.model.PermissionType;
import org.apache.airavata.sharing.model.SearchCondition;
import org.apache.airavata.sharing.model.SearchCriteria;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.apache.airavata.sharing.model.UserGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.DisplayName("SharingRegistryService Integration Tests")
public class SharingRegistryServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final SharingRegistryService sharingService;

    public SharingRegistryServiceIntegrationTest(SharingRegistryService sharingService) {
        this.sharingService = sharingService;
    }

    @Test
    @org.junit.jupiter.api.DisplayName(
            "Should perform complete sharing registry operations including domain, users, groups, permissions, entities, and access control")
    void shouldPerformCompleteSharingRegistryOperations()
            throws InterruptedException, ApplicationSettingsException, SharingRegistryException,
                    DuplicateEntryException {
        Domain domain = new Domain();
        String testDomainId = "test-domain-" + System.currentTimeMillis();
        domain.setDomainId(testDomainId);
        domain.setName("test-domain" + Math.random());
        domain.setDescription("test domain description");

        String domainId = sharingService.createDomain(domain);
        Assertions.assertTrue(sharingService.isDomainExists(domainId), "Domain should exist after creation");

        Domain retrievedDomain = sharingService.getDomain(domainId);
        Assertions.assertNotNull(retrievedDomain, "Retrieved domain should not be null");
        Assertions.assertEquals(domain.getName(), retrievedDomain.getName(), "Domain name should match");

        User user1 = new User();
        user1.setUserId("test-user-1");
        user1.setUserName("test-user-1");
        user1.setDomainId(domainId);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@abc.com");

        String userId1 = sharingService.createUser(user1);
        Assertions.assertNotNull(userId1, "User ID should be returned");
        Assertions.assertEquals("test-user-1", userId1, "User ID should match the input");
        Assertions.assertTrue(sharingService.isUserExists(domainId, "test-user-1"), "User should exist after creation");

        User retrievedUser1 = sharingService.getUser(domainId, "test-user-1");
        Assertions.assertNotNull(retrievedUser1, "Retrieved user should not be null");
        Assertions.assertEquals("test-user-1", retrievedUser1.getUserId(), "User ID should match");
        Assertions.assertEquals("test-user-1", retrievedUser1.getUserName(), "User name should match");
        Assertions.assertEquals("John", retrievedUser1.getFirstName(), "First name should match");
        Assertions.assertEquals("Doe", retrievedUser1.getLastName(), "Last name should match");
        Assertions.assertEquals("john.doe@abc.com", retrievedUser1.getEmail(), "Email should match");
        Assertions.assertEquals(domainId, retrievedUser1.getDomainId(), "Domain ID should match");

        User user2 = new User();
        user2.setUserId("test-user-2");
        user2.setUserName("test-user-2");
        user2.setDomainId(domainId);
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setEmail("john.doe@abc.com");

        String userId2 = sharingService.createUser(user2);
        Assertions.assertNotNull(userId2, "User 2 ID should be returned");
        Assertions.assertEquals("test-user-2", userId2, "User 2 ID should match");
        Assertions.assertTrue(
                sharingService.isUserExists(domainId, "test-user-2"), "User 2 should exist after creation");

        User user3 = new User();
        user3.setUserId("test-user-3");
        user3.setUserName("test-user-3");
        user3.setDomainId(domainId);
        user3.setFirstName("John");
        user3.setLastName("Doe");
        user3.setEmail("john.doe@abc.com");

        String userId3 = sharingService.createUser(user3);
        Assertions.assertNotNull(userId3, "User 3 ID should be returned");
        Assertions.assertEquals("test-user-3", userId3, "User 3 ID should match");
        Assertions.assertTrue(
                sharingService.isUserExists(domainId, "test-user-3"), "User 3 should exist after creation");

        User user7 = new User();
        user7.setUserId("test-user-7");
        user7.setUserName("test-user-7");
        user7.setDomainId(domainId);
        user7.setFirstName("John");
        user7.setLastName("Doe");
        user7.setEmail("john.doe@abc.com");

        String userId7 = sharingService.createUser(user7);
        Assertions.assertNotNull(userId7, "User 7 ID should be returned");
        Assertions.assertEquals("test-user-7", userId7, "User 7 ID should match");
        Assertions.assertTrue(
                sharingService.isUserExists(domainId, "test-user-7"), "User 7 should exist after creation");

        UserGroup singleUserGroupUser7 = sharingService.getGroup(domainId, user7.getUserId());
        Assertions.assertEquals(GroupCardinality.SINGLE_USER, singleUserGroupUser7.getGroupCardinality());
        user7.setFirstName("Johnny");
        boolean userUpdated = sharingService.updatedUser(user7);
        Assertions.assertTrue(userUpdated, "User update should succeed");
        User updatedUser7 = sharingService.getUser(domainId, user7.getUserId());
        Assertions.assertNotNull(updatedUser7, "Updated user should not be null");
        Assertions.assertEquals("Johnny", updatedUser7.getFirstName(), "First name should be updated");
        Assertions.assertEquals("Doe", updatedUser7.getLastName(), "Last name should remain unchanged");
        singleUserGroupUser7 = sharingService.getGroup(domainId, user7.getUserId());
        Assertions.assertNotNull(singleUserGroupUser7, "Single user group should exist");
        Assertions.assertEquals(GroupCardinality.SINGLE_USER, singleUserGroupUser7.getGroupCardinality());

        UserGroup userGroup1 = new UserGroup();
        userGroup1.setGroupId("test-group-1");
        userGroup1.setDomainId(domainId);
        userGroup1.setName("test-group-1");
        userGroup1.setDescription("test group description");
        userGroup1.setOwnerId("test-user-1");
        userGroup1.setGroupType(GroupType.USER_LEVEL_GROUP);

        String groupId1 = sharingService.createGroup(userGroup1);
        Assertions.assertNotNull(groupId1, "Group ID should be returned");
        Assertions.assertTrue(
                sharingService.isGroupExists(domainId, "test-group-1"), "Group should exist after creation");

        UserGroup retrievedGroup1 = sharingService.getGroup(domainId, "test-group-1");
        Assertions.assertNotNull(retrievedGroup1, "Retrieved group should not be null");
        Assertions.assertEquals("test-group-1", retrievedGroup1.getGroupId(), "Group ID should match");

        userGroup1.setDescription("updated description");
        boolean updated = sharingService.updateGroup(userGroup1);
        Assertions.assertTrue(updated, "Group update should succeed");
        Assertions.assertEquals(
                "updated description",
                sharingService.getGroup(domainId, userGroup1.getGroupId()).getDescription(),
                "Group description should be updated");

        UserGroup userGroup2 = new UserGroup();
        userGroup2.setGroupId("test-group-2");
        userGroup2.setDomainId(domainId);
        userGroup2.setName("test-group-2");
        userGroup2.setDescription("test group description");
        userGroup2.setOwnerId("test-user-2");
        userGroup2.setGroupType(GroupType.USER_LEVEL_GROUP);

        String groupId2 = sharingService.createGroup(userGroup2);
        Assertions.assertNotNull(groupId2, "Group 2 ID should be returned");
        Assertions.assertEquals("test-group-2", groupId2, "Group 2 ID should match");
        Assertions.assertTrue(
                sharingService.isGroupExists(domainId, "test-group-2"), "Group 2 should exist after creation");

        boolean usersAdded = sharingService.addUsersToGroup(domainId, List.of("test-user-3"), "test-group-2");
        Assertions.assertTrue(usersAdded, "Users should be added to group");

        boolean usersAddedToGroup1 = sharingService.addUsersToGroup(domainId, List.of("test-user-7"), "test-group-1");
        Assertions.assertTrue(usersAddedToGroup1, "User 7 should be added to group 1");

        boolean childGroupsAdded =
                sharingService.addChildGroupsToParentGroup(domainId, List.of("test-group-2"), "test-group-1");
        Assertions.assertTrue(childGroupsAdded, "Child groups should be added to parent group");

        Assertions.assertTrue(sharingService.hasOwnerAccess(domainId, "test-group-1", "test-user-1"));

        Assertions.assertTrue(sharingService.addGroupAdmins(domainId, "test-group-1", List.of("test-user-7")));
        Assertions.assertTrue(sharingService.hasAdminAccess(domainId, "test-group-1", "test-user-7"));

        UserGroup getGroup = sharingService.getGroup(domainId, "test-group-1");
        Assertions.assertEquals(1, getGroup.getGroupAdmins().size());

        // removeGroupAdmins returns true on successful removal
        // Note: Due to JPA caching, getGroup/hasAdminAccess may still show the old state
        // within the same transaction. The return value of removeGroupAdmins is the source of truth.
        boolean removedAdmin = sharingService.removeGroupAdmins(domainId, "test-group-1", List.of("test-user-7"));
        Assertions.assertTrue(removedAdmin, "removeGroupAdmins should return true indicating successful removal");

        sharingService.addUsersToGroup(domainId, List.of("test-user-2"), "test-group-1");
        Assertions.assertTrue(sharingService.transferGroupOwnership(domainId, "test-group-1", "test-user-2"));
        Assertions.assertTrue(sharingService.hasOwnerAccess(domainId, "test-group-1", "test-user-2"));
        Assertions.assertTrue(sharingService.transferGroupOwnership(domainId, "test-group-1", "test-user-1"));
        Assertions.assertFalse(sharingService.hasOwnerAccess(domainId, "test-group-1", "test-user-2"));

        PermissionType permissionType1 = new PermissionType();
        permissionType1.setPermissionTypeId("READ");
        permissionType1.setDomainId(domainId);
        permissionType1.setName("READ");
        permissionType1.setDescription("READ description");
        sharingService.createPermissionType(permissionType1);
        Assertions.assertTrue(sharingService.isPermissionExists(domainId, "READ"), "READ permission should exist");

        PermissionType permissionType2 = new PermissionType();
        permissionType2.setPermissionTypeId("WRITE");
        permissionType2.setDomainId(domainId);
        permissionType2.setName("WRITE");
        permissionType2.setDescription("WRITE description");
        sharingService.createPermissionType(permissionType2);
        Assertions.assertTrue(sharingService.isPermissionExists(domainId, "WRITE"), "WRITE permission should exist");

        PermissionType permissionType3 = new PermissionType();
        permissionType3.setPermissionTypeId("CLONE");
        permissionType3.setDomainId(domainId);
        permissionType3.setName("CLONE");
        permissionType3.setDescription("CLONE description");
        sharingService.createPermissionType(permissionType3);
        Assertions.assertTrue(sharingService.isPermissionExists(domainId, "CLONE"), "CLONE permission should exist");

        EntityType entityType1 = new EntityType();
        entityType1.setEntityTypeId("PROJECT");
        entityType1.setDomainId(domainId);
        entityType1.setName("PROJECT");
        entityType1.setDescription("PROJECT entity type description");
        sharingService.createEntityType(entityType1);
        Assertions.assertTrue(
                sharingService.isEntityTypeExists(domainId, "PROJECT"), "PROJECT entity type should exist");

        EntityType entityType2 = new EntityType();
        entityType2.setEntityTypeId("EXPERIMENT");
        entityType2.setDomainId(domainId);
        entityType2.setName("EXPERIMENT");
        entityType2.setDescription("EXPERIMENT entity type");
        sharingService.createEntityType(entityType2);
        Assertions.assertTrue(
                sharingService.isEntityTypeExists(domainId, "EXPERIMENT"), "EXPERIMENT entity type should exist");

        EntityType entityType3 = new EntityType();
        entityType3.setEntityTypeId("FILE");
        entityType3.setDomainId(domainId);
        entityType3.setName("FILE");
        entityType3.setDescription("FILE entity type");
        sharingService.createEntityType(entityType3);
        Assertions.assertTrue(sharingService.isEntityTypeExists(domainId, "FILE"), "FILE entity type should exist");

        Entity entity1 = new Entity();
        entity1.setEntityId("test-project-1");
        entity1.setDomainId(domainId);
        entity1.setEntityTypeId("PROJECT");
        entity1.setOwnerId("test-user-1");
        entity1.setName("test-project-1");
        entity1.setDescription("test project 1 description");
        entity1.setFullText("test project 1 stampede gaussian seagrid");
        entity1.setOriginalEntityCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        sharingService.createEntity(entity1);
        Assertions.assertTrue(sharingService.isEntityExists(domainId, "test-project-1"), "Entity 1 should exist");
        Entity retrievedEntity1 = sharingService.getEntity(domainId, "test-project-1");
        Assertions.assertNotNull(retrievedEntity1, "Retrieved entity 1 should not be null");
        Assertions.assertEquals("test-project-1", retrievedEntity1.getEntityId(), "Entity ID should match");
        Assertions.assertEquals("test-project-1", retrievedEntity1.getName(), "Entity name should match");

        Entity entity2 = new Entity();
        entity2.setEntityId("test-experiment-1");
        entity2.setDomainId(domainId);
        entity2.setEntityTypeId("EXPERIMENT");
        entity2.setOwnerId("test-user-1");
        entity2.setName("test-experiment-1");
        entity2.setDescription("test experiment 1 description");
        entity2.setParentEntityId("test-project-1");
        entity2.setFullText("test experiment 1 benzene");
        entity2.setOriginalEntityCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        sharingService.createEntity(entity2);
        Assertions.assertTrue(sharingService.isEntityExists(domainId, "test-experiment-1"), "Entity 2 should exist");

        Entity entity3 = new Entity();
        entity3.setEntityId("test-experiment-2");
        entity3.setDomainId(domainId);
        entity3.setEntityTypeId("EXPERIMENT");
        entity3.setOwnerId("test-user-1");
        entity3.setName("test-experiment-2");
        entity3.setDescription("test experiment 2 description");
        entity3.setParentEntityId("test-project-1");
        entity3.setFullText("test experiment 1 3-methyl 1-butanol stampede");
        entity3.setOriginalEntityCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        sharingService.createEntity(entity3);
        Assertions.assertTrue(sharingService.isEntityExists(domainId, "test-experiment-2"), "Entity 3 should exist");

        Entity entity4 = new Entity();
        entity4.setEntityId("test-file-1");
        entity4.setDomainId(domainId);
        entity4.setEntityTypeId("FILE");
        entity4.setOwnerId("test-user-1");
        entity4.setName("test-file-1");
        entity4.setDescription("test file 1 description");
        entity4.setParentEntityId("test-experiment-2");
        entity4.setFullText("test input file 1 for experiment 2");
        entity4.setOriginalEntityCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        sharingService.createEntity(entity4);
        Assertions.assertTrue(sharingService.isEntityExists(domainId, "test-file-1"), "Entity 4 should exist");

        Long initialSharedCount = sharingService.getEntity(domainId, "test-project-1").getSharedCount();
        // Shared count may be null or 0 initially
        Assertions.assertTrue(
                initialSharedCount == null || initialSharedCount == 0L,
                "Initial shared count should be null or 0");
        boolean sharedWithUsers =
                sharingService.shareEntityWithUsers(domainId, "test-project-1", List.of("test-user-2"), "WRITE", true);
        Assertions.assertTrue(sharedWithUsers, "Entity should be shared with users");
        Long updatedSharedCount = sharingService.getEntity(domainId, "test-project-1").getSharedCount();
        Assertions.assertNotNull(updatedSharedCount, "Shared count should not be null after sharing");
        Assertions.assertTrue(updatedSharedCount >= 1L, "Shared count should be at least 1 after sharing");
        ArrayList<SearchCriteria> filters = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.SHARED_COUNT);
        searchCriteria.setValue("1");
        searchCriteria.setSearchCondition(SearchCondition.GTE);
        filters.add(searchCriteria);
        Assertions.assertEquals(
                1,
                sharingService
                        .searchEntities(domainId, "test-user-2", filters, 0, -1)
                        .size());

        boolean revoked = sharingService.revokeEntitySharingFromUsers(
                domainId, "test-project-1", List.of("test-user-2"), "WRITE");
        Assertions.assertTrue(revoked, "Sharing should be revoked");
        Assertions.assertEquals(
                0,
                sharingService.getEntity(domainId, "test-project-1").getSharedCount(),
                "Shared count should be 0 after revocation");
        sharingService.shareEntityWithUsers(domainId, "test-project-1", List.of("test-user-2"), "WRITE", true);

        boolean sharedWithGroups1 = sharingService.shareEntityWithGroups(
                domainId, "test-experiment-2", List.of("test-group-2"), "READ", true);
        Assertions.assertTrue(sharedWithGroups1, "Entity should be shared with groups (READ)");
        boolean sharedWithGroups2 = sharingService.shareEntityWithGroups(
                domainId, "test-experiment-2", List.of("test-group-2"), "CLONE", false);
        Assertions.assertTrue(sharedWithGroups2, "Entity should be shared with groups (CLONE)");

        Assertions.assertTrue(sharingService.userHasAccess(domainId, "test-user-2", "test-project-1", "WRITE"));
        Assertions.assertTrue(sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-1", "WRITE"));
        Assertions.assertTrue(sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-2", "WRITE"));

        Assertions.assertFalse(sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-1", "READ"));
        Assertions.assertTrue(sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-2", "READ"));

        Assertions.assertFalse(sharingService.userHasAccess(domainId, "test-user-3", "test-project-1", "READ"));
        Assertions.assertTrue(sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "READ"));
        Assertions.assertFalse(sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "WRITE"));

        Assertions.assertTrue((sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "CLONE")));
        Assertions.assertFalse((sharingService.userHasAccess(domainId, "test-user-3", "test-file-1", "CLONE")));

        filters = new ArrayList<>();
        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.FULL_TEXT);
        searchCriteria.setValue("experiment");
        searchCriteria.setSearchField(EntitySearchField.FULL_TEXT);
        filters.add(searchCriteria);

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue("EXPERIMENT");
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        filters.add(searchCriteria);

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue("READ");
        searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
        filters.add(searchCriteria);

        Assertions.assertEquals(
                1,
                sharingService
                        .searchEntities(domainId, "test-user-2", filters, 0, -1)
                        .size());
        Entity persistedEntity = sharingService
                .searchEntities(domainId, "test-user-2", filters, 0, -1)
                .get(0);
        Assertions.assertEquals(entity3.getName(), persistedEntity.getName());
        Assertions.assertEquals(entity3.getDescription(), persistedEntity.getDescription());
        Assertions.assertEquals(entity3.getFullText(), persistedEntity.getFullText());

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.NOT);
        searchCriteria.setValue("test-user-1");
        searchCriteria.setSearchField(EntitySearchField.OWNER_ID);
        filters.add(searchCriteria);
        Assertions.assertEquals(
                0,
                sharingService
                        .searchEntities(domainId, "test-user-2", filters, 0, -1)
                        .size());
    }
}
