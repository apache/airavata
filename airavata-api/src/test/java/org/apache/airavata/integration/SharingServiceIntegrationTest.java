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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.util.SharingRegistryDBInitConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test for the sharing registry lifecycle. Extends {@link AbstractIntegrationTest}
 * to use a real MariaDB via Testcontainers.
 *
 * <p>Run with: {@code mvn test -pl airavata-api -Dgroups=integration -DexcludedGroups=""}
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public class SharingServiceIntegrationTest extends AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(SharingServiceIntegrationTest.class);

    private static SharingRegistryServerHandler handler;

    // Domain
    private static String domainId;

    // Users
    private static String userId1, userId2, userId3, userId7;
    private static User user3, user7;

    // Groups
    private static String groupId1, groupId2;

    // Permission types
    private static String permissionTypeId1, permissionTypeId2;

    // Entity types
    private static String entityTypeId1, entityTypeId2, entityTypeId3, entityTypeId4;

    // Entities
    private static String entityId1, entityId2, entityId3, entityId4, entityId5, entityId6;

    @BeforeAll
    static void setUpAll() throws Exception {
        // System properties are already set by SharedMariaDB (via AbstractIntegrationTest)
        SharingRegistryDBInitConfig config = new SharingRegistryDBInitConfig();
        config.setDBInitScriptPrefix("sharing-registry");
        handler = new SharingRegistryServerHandler(config);

        // Create domain
        domainId = "test-domain." + System.currentTimeMillis();
        Domain domain = new Domain();
        domain.setDomainId(domainId);
        domain.setName(domainId);
        domain.setDescription("integration test domain");
        domain.setCreatedTime(System.currentTimeMillis());
        domain.setUpdatedTime(System.currentTimeMillis());
        assertNotNull(handler.createDomain(domain), "domain creation should return id");

        // Create users
        userId1 = createUser("test-user-1");
        userId2 = createUser("test-user-2");
        userId3 = createUser("test-user-3");
        userId7 = createUser("test-user-7");
        user3 = handler.getUser(domainId, userId3);
        user7 = handler.getUser(domainId, userId7);

        // Create groups
        groupId1 = createGroup("test-group-1", userId1);
        groupId2 = createGroup("test-group-2", userId2);

        // Create permission types
        permissionTypeId1 = createPermissionType("READ", "READ description");
        permissionTypeId2 = createPermissionType("WRITE", "WRITE description");

        // Create entity types
        entityTypeId1 = createEntityType("Project", "project type");
        entityTypeId2 = createEntityType("Experiment", "experiment type");
        entityTypeId3 = createEntityType("FileInput", "file input type");
        entityTypeId4 = createEntityType("Application-Deployment", "app deployment type");
    }

    // No @AfterAll teardown needed — SharedMariaDB owns the system properties for the entire test run

    // --- Helpers ---

    private static String createUser(String namePrefix) throws Exception {
        long ts = System.currentTimeMillis();
        String userName = namePrefix + "." + ts;
        String userId = domainId + ":" + userName;
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setDomainId(domainId);
        user.setCreatedTime(ts);
        user.setUpdatedTime(ts);
        assertNotNull(handler.createUser(user), "user creation should return id");
        return userId;
    }

    private static String createGroup(String namePrefix, String ownerId) throws Exception {
        long ts = System.currentTimeMillis();
        String groupName = namePrefix + "." + ts;
        String groupId = domainId + ":" + groupName;
        UserGroup group = new UserGroup();
        group.setGroupId(groupId);
        group.setDomainId(domainId);
        group.setName(groupName);
        group.setDescription("test group");
        group.setOwnerId(ownerId);
        group.setGroupType(GroupType.USER_LEVEL_GROUP);
        group.setGroupCardinality(GroupCardinality.MULTI_USER);
        group.setCreatedTime(ts);
        group.setUpdatedTime(ts);
        assertNotNull(handler.createGroup(group), "group creation should return id");
        return groupId;
    }

    private static String createPermissionType(String name, String description) throws Exception {
        long ts = System.currentTimeMillis();
        PermissionType pt = new PermissionType();
        pt.setPermissionTypeId(domainId + ":" + name);
        pt.setDomainId(domainId);
        pt.setName(name);
        pt.setDescription(description);
        pt.setCreatedTime(ts);
        pt.setUpdatedTime(ts);
        String id = handler.createPermissionType(pt);
        assertNotNull(id, "permission type creation should return id");
        return id;
    }

    private static String createEntityType(String name, String description) throws Exception {
        long ts = System.currentTimeMillis();
        EntityType et = new EntityType();
        et.setEntityTypeId(domainId + ":" + name);
        et.setDomainId(domainId);
        et.setName(name);
        et.setDescription(description);
        et.setCreatedTime(ts);
        et.setUpdatedTime(ts);
        String id = handler.createEntityType(et);
        assertNotNull(id, "entity type creation should return id");
        return id;
    }

    private static String createEntity(
            String entitySuffix,
            String entityTypeId,
            String ownerId,
            String name,
            String description,
            String parentEntityId)
            throws Exception {
        long ts = System.currentTimeMillis();
        Entity entity = new Entity();
        entity.setEntityId(domainId + ":" + entitySuffix);
        entity.setDomainId(domainId);
        entity.setEntityTypeId(entityTypeId);
        entity.setOwnerId(ownerId);
        entity.setName(name);
        entity.setDescription(description);
        entity.setFullText(name + " " + description);
        entity.setCreatedTime(ts);
        entity.setUpdatedTime(ts);
        if (parentEntityId != null) {
            entity.setParentEntityId(parentEntityId);
        }
        String id = handler.createEntity(entity);
        assertNotNull(id, "entity creation should return id");
        return id;
    }

    // --- Tests ---

    @Test
    @Order(1)
    @DisplayName("Domain is listed after creation")
    void domainIsListed() throws Exception {
        assertTrue(handler.getDomains(0, 10).size() > 0, "getDomains should return at least the test domain");
    }

    @Test
    @Order(2)
    @DisplayName("Users are listed in domain")
    void usersAreListed() throws Exception {
        assertTrue(handler.getUsers(domainId, 0, 10).size() > 0, "getUsers should return users in domain");
    }

    @Test
    @Order(3)
    @DisplayName("Group owner has owner access")
    void groupOwnerHasOwnerAccess() throws Exception {
        assertTrue(handler.hasOwnerAccess(domainId, groupId1, userId1), "group creator should have owner access");
    }

    @Test
    @Order(4)
    @DisplayName("Add and remove group admin")
    void addAndRemoveGroupAdmin() throws Exception {
        handler.addUsersToGroup(domainId, Arrays.asList(userId7), groupId1);
        assertTrue(
                handler.addGroupAdmins(domainId, groupId1, Arrays.asList(userId7)),
                "addGroupAdmins should return true");
        assertTrue(
                handler.hasAdminAccess(domainId, groupId1, userId7),
                "userId7 should have admin access after being added");
        assertTrue(
                handler.removeGroupAdmins(domainId, groupId1, Arrays.asList(userId7)),
                "removeGroupAdmins should return true");
        assertFalse(
                handler.hasAdminAccess(domainId, groupId1, userId7),
                "userId7 should not have admin access after removal");
    }

    @Test
    @Order(5)
    @DisplayName("Transfer group ownership")
    void transferGroupOwnership() throws Exception {
        handler.addUsersToGroup(domainId, Arrays.asList(userId2), groupId1);
        assertTrue(
                handler.transferGroupOwnership(domainId, groupId1, userId2), "ownership transfer should return true");
        assertTrue(handler.hasOwnerAccess(domainId, groupId1, userId2), "userId2 should be the new owner");
        // Transfer back
        assertTrue(
                handler.transferGroupOwnership(domainId, groupId1, userId1),
                "ownership transfer back should return true");
        assertFalse(handler.hasOwnerAccess(domainId, groupId1, userId2), "userId2 should no longer be owner");
    }

    @Test
    @Order(6)
    @DisplayName("Group membership: users and child groups are reflected correctly")
    void groupMembership() throws Exception {
        handler.addUsersToGroup(domainId, Arrays.asList(userId1), groupId1);
        handler.addUsersToGroup(domainId, Arrays.asList(userId7), groupId1);
        handler.addUsersToGroup(domainId, Arrays.asList(userId2, userId3), groupId2);
        handler.addChildGroupsToParentGroup(domainId, Arrays.asList(groupId2), groupId1);

        assertEquals(
                1,
                handler.getGroupMembersOfTypeGroup(domainId, groupId1, 0, 10).size(),
                "groupId1 should have one child group (groupId2)");
        assertEquals(
                2,
                handler.getGroupMembersOfTypeUser(domainId, groupId2, 0, 10).size(),
                "groupId2 should have two direct users");
        assertEquals(
                1,
                handler.getAllMemberGroupsForUser(domainId, userId3).size(),
                "userId3 should be a member of groupId2");
    }

    @Test
    @Order(7)
    @DisplayName("Entity sharing via users propagates to children")
    void entitySharingPropagatesViaUsers() throws Exception {
        entityId1 = createEntity("Entity1", entityTypeId1, userId1, "Project name 1", "Project description", null);
        entityId2 =
                createEntity("Entity2", entityTypeId2, userId1, "Experiment name", "Experiment description", entityId1);
        entityId3 =
                createEntity("Entity3", entityTypeId2, userId1, "Experiment name", "Experiment description", entityId1);

        handler.shareEntityWithUsers(domainId, entityId1, Arrays.asList(userId2), permissionTypeId1, true);
        handler.shareEntityWithGroups(domainId, entityId3, Arrays.asList(groupId2), permissionTypeId1, true);

        entityId4 = createEntity("Entity4", entityTypeId3, userId3, "Input name", "Input file description", entityId3);

        assertTrue(
                handler.userHasAccess(domainId, userId3, entityId4, permissionTypeId1),
                "userId3 (group member) should have access to child entity via group share");
        assertTrue(
                handler.userHasAccess(domainId, userId2, entityId4, permissionTypeId1),
                "userId2 should have access to child entity via parent group share");
        assertTrue(
                handler.userHasAccess(domainId, userId1, entityId4, permissionTypeId1),
                "owner userId1 should have access");
        assertFalse(
                handler.userHasAccess(domainId, userId3, entityId1, permissionTypeId1),
                "userId3 should not have access to entityId1 (shared only with userId2)");
    }

    @Test
    @Order(8)
    @DisplayName("Entity sharing with multiple permission types")
    void entitySharingWithMultiplePermissions() throws Exception {
        entityId5 = createEntity(
                "Entity5", entityTypeId4, userId1, "App deployment name", "App deployment description", null);
        handler.shareEntityWithUsers(domainId, entityId5, Arrays.asList(userId2), permissionTypeId1, true);
        handler.shareEntityWithGroups(domainId, entityId5, Arrays.asList(groupId2), permissionTypeId2, true);

        assertTrue(
                handler.userHasAccess(domainId, userId3, entityId5, permissionTypeId2),
                "userId3 (group member) should have WRITE access via group");
        assertTrue(
                handler.userHasAccess(domainId, userId2, entityId5, permissionTypeId1),
                "userId2 should have READ access via direct share");
        assertFalse(
                handler.userHasAccess(domainId, userId3, entityId5, permissionTypeId1),
                "userId3 should not have READ access (only WRITE via group)");
    }

    @Test
    @Order(9)
    @DisplayName("Search entities by name and type")
    void searchEntitiesByNameAndType() throws Exception {
        java.util.ArrayList<SearchCriteria> filters = new java.util.ArrayList<>();
        SearchCriteria nameCriteria = new SearchCriteria();
        nameCriteria.setSearchCondition(SearchCondition.LIKE);
        nameCriteria.setValue("Input");
        nameCriteria.setSearchField(EntitySearchField.NAME);
        filters.add(nameCriteria);

        SearchCriteria typeCriteria = new SearchCriteria();
        typeCriteria.setSearchCondition(SearchCondition.EQUAL);
        typeCriteria.setValue(entityTypeId3);
        typeCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        filters.add(typeCriteria);

        assertTrue(
                handler.searchEntities(domainId, userId1, filters, 0, -1).size() > 0,
                "search should return at least entityId4 (FileInput entity with 'Input' in name)");
    }

    @Test
    @Order(10)
    @DisplayName("Get shared users and groups for entity")
    void getSharedUsersAndGroups() throws Exception {
        assertNotNull(
                handler.getListOfSharedUsers(domainId, entityId1, permissionTypeId1),
                "getListOfSharedUsers should not return null");
        assertNotNull(
                handler.getListOfSharedGroups(domainId, entityId1, permissionTypeId1),
                "getListOfSharedGroups should not return null");
        assertEquals(
                1,
                handler.getListOfSharedUsers(domainId, entityId1, domainId + ":OWNER")
                        .size(),
                "entityId1 should have exactly one owner");
    }

    @Test
    @Order(11)
    @DisplayName("Changing parent entity removes old cascading permissions and applies new ones")
    void changingParentEntityUpdatesPermissions() throws Exception {
        // Setup: entityId2 parent is entityId1; entityId1 is shared with userId2
        assertTrue(handler.userHasAccess(domainId, userId2, entityId1, permissionTypeId1));
        assertTrue(
                handler.userHasAccess(domainId, userId2, entityId2, permissionTypeId1),
                "userId2 should have access to entityId2 via parent entityId1");
        assertFalse(
                handler.userHasAccess(domainId, userId3, entityId2, permissionTypeId1),
                "userId3 should not yet have access to entityId2");

        // Create a second parent entity shared with userId3
        entityId6 = createEntity("Entity6", entityTypeId1, userId1, "Project name 2", "Project description", null);
        handler.shareEntityWithUsers(domainId, entityId6, Arrays.asList(userId3), permissionTypeId1, true);
        assertTrue(handler.userHasAccess(domainId, userId3, entityId6, permissionTypeId1));

        // Directly share entityId2 with userId7 before reparenting
        assertFalse(handler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1));
        handler.shareEntityWithUsers(domainId, entityId2, Arrays.asList(userId7), permissionTypeId1, true);
        assertTrue(handler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1));

        // Reparent entityId2 to entityId6
        Entity entity2 = handler.getEntity(domainId, entityId2);
        entity2.setParentEntityId(entityId6);
        assertTrue(handler.updateEntity(entity2), "updateEntity should return true");

        Entity entity2Updated = handler.getEntity(domainId, entityId2);
        assertEquals(entityId6, entity2Updated.getParentEntityId(), "parent should be updated to entityId6");

        // userId2's access should be removed (was from old parent)
        assertFalse(
                handler.userHasAccess(domainId, userId2, entityId2, permissionTypeId1),
                "userId2 should lose access after parent change");
        // userId3 now has access via new parent
        assertTrue(
                handler.userHasAccess(domainId, userId3, entityId2, permissionTypeId1),
                "userId3 should gain access via new parent entityId6");
        // userId7's direct share is preserved
        assertTrue(
                handler.userHasAccess(domainId, userId7, entityId2, permissionTypeId1),
                "userId7's direct share should be preserved after parent change");
    }

    @Test
    @Order(12)
    @DisplayName("getListOfDirectlySharedUsers returns only direct shares")
    void directlySharedUsersDoNotIncludeCascading() throws Exception {
        assertEquals(
                Arrays.asList(user3),
                handler.getListOfDirectlySharedUsers(domainId, entityId6, permissionTypeId1),
                "entityId6 should be directly shared with user3 only");
        assertEquals(
                Arrays.asList(user7),
                handler.getListOfDirectlySharedUsers(domainId, entityId2, permissionTypeId1),
                "entityId2 should be directly shared with user7 only");

        List<org.apache.airavata.sharing.registry.models.User> entityId2SharedUsers =
                handler.getListOfSharedUsers(domainId, entityId2, permissionTypeId1);
        assertEquals(
                2,
                entityId2SharedUsers.size(),
                "entityId2 should have 2 shared users total (user3 cascaded + user7 direct)");
        assertTrue(
                entityId2SharedUsers.contains(user3) && entityId2SharedUsers.contains(user7),
                "shared users should contain both user3 and user7");

        assertEquals(
                1,
                handler.getListOfDirectlySharedGroups(domainId, entityId3, permissionTypeId1)
                        .size(),
                "entityId3 should have one directly shared group");
        assertEquals(
                groupId2,
                handler.getListOfDirectlySharedGroups(domainId, entityId3, permissionTypeId1)
                        .get(0)
                        .getGroupId(),
                "the directly shared group for entityId3 should be groupId2");
    }

    @Test
    @Order(13)
    @DisplayName("New users are automatically added to initialUserGroupId when configured")
    void newUserAddedToInitialUserGroup() throws Exception {
        String initialUserGroupId = createGroup("initial-user-group", userId1);

        Domain domain = handler.getDomain(domainId);
        domain.setInitialUserGroupId(initialUserGroupId);
        assertTrue(handler.updateDomain(domain), "updateDomain should return true");
        assertEquals(
                initialUserGroupId,
                handler.getDomain(domainId).getInitialUserGroupId(),
                "domain should have initialUserGroupId set");

        String userId8 = createUser("test-user-8");
        List<UserGroup> user8Groups = handler.getAllMemberGroupsForUser(domainId, userId8);
        assertFalse(user8Groups.isEmpty(), "new user should be added to at least one group");
        assertEquals(1, user8Groups.size(), "new user should be in exactly one group");
        assertEquals(initialUserGroupId, user8Groups.get(0).getGroupId(), "new user should be in the initialUserGroup");
    }
}
