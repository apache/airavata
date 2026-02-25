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

import org.apache.airavata.config.ServiceIntegrationTestBase;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.iam.model.Domain;
import org.apache.airavata.iam.model.SharingEntity;
import org.apache.airavata.core.model.EntitySearchField;
import org.apache.airavata.iam.model.EntityType;
import org.apache.airavata.iam.model.GroupCardinality;
import org.apache.airavata.iam.model.GroupType;
import org.apache.airavata.iam.model.PermissionType;
import org.apache.airavata.core.model.SearchCondition;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.model.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestConstructor;

/**
 * Comprehensive integration tests for Sharing Services.
 *
 * <p>Covers additional scenarios:
 * - Domain management
 * - User lifecycle
 * - Group membership and administration
 * - Permission types and access control
 * - Entity creation with parent-child relationships
 * - Entity searching with various criteria
 * - Cascading permissions
 * - Permission revocation
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("Sharing Service Comprehensive Tests")
public class SharingServiceComprehensiveTest extends ServiceIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(SharingServiceComprehensiveTest.class);

    private final SharingService sharingService;
    private final GatewayService gatewayService;

    private String testDomainId;

    public SharingServiceComprehensiveTest(
            SharingService sharingService,
            GatewayService gatewayService) {
        this.sharingService = sharingService;
        this.gatewayService = gatewayService;
    }

    @BeforeEach
    void setupTestDomain() throws Exception {
        testDomainId = "test-domain-" + UUID.randomUUID().toString().substring(0, 8);

        // Create gateway first (domain requires a gateway)
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testDomainId);
        gateway.setGatewayName("Test Gateway " + testDomainId);
        gatewayService.createGateway(gateway);

        // Create domain
        Domain domain = new Domain();
        domain.setDomainId(testDomainId);
        domain.setName("Test Domain");
        domain.setDescription("Test domain for comprehensive sharing tests");
        sharingService.createDomain(domain);

        logger.info("Created test domain: {}", testDomainId);
    }

    @Nested
    @DisplayName("Domain Management Tests")
    class DomainManagementTests {

        @Test
        @DisplayName("Should create and retrieve domain")
        void shouldCreateAndRetrieveDomain() throws Exception {
            // When
            Domain retrieved = sharingService.getDomain(testDomainId);

            // Then
            assertNotNull(retrieved);
            assertEquals(testDomainId, retrieved.getDomainId());
            assertEquals("Test Domain", retrieved.getName());
        }

        @Test
        @DisplayName("Should check domain existence")
        void shouldCheckDomainExistence() throws Exception {
            // Then
            assertTrue(sharingService.isDomainExists(testDomainId));
            assertFalse(sharingService.isDomainExists("non-existent-domain"));
        }

        @Test
        @DisplayName("Should reject duplicate domain creation")
        void shouldRejectDuplicateDomainCreation() throws Exception {
            // When/Then
            Domain duplicate = new Domain();
            duplicate.setDomainId(testDomainId);
            duplicate.setName("Duplicate Domain");

            assertThatThrownBy(() -> sharingService.createDomain(duplicate))
                    .isInstanceOf(DuplicateEntryException.class);
        }

        @Test
        @DisplayName("Should get all domains")
        void shouldGetAllDomains() throws Exception {
            // When
            List<Domain> domains = sharingService.getDomains(0, -1);

            // Then
            assertNotNull(domains);
            assertTrue(domains.stream().anyMatch(d -> testDomainId.equals(d.getDomainId())));
        }
    }

    @Nested
    @DisplayName("User Lifecycle Tests")
    class UserLifecycleTests {

        @Test
        @DisplayName("Should create user with all fields")
        void shouldCreateUserWithAllFields() throws Exception {
            // Given
            User user = createUser("full-user", "John", "Doe", "john.doe@test.com");

            // When
            String userId = sharingService.createUser(user);

            // Then
            assertNotNull(userId);
            assertEquals("full-user", userId);

            User retrieved = sharingService.getUser(testDomainId, "full-user");
            assertNotNull(retrieved);
            assertEquals("full-user", retrieved.getUserName());
            assertEquals("John", retrieved.getFirstName());
            assertEquals("Doe", retrieved.getLastName());
            assertEquals("john.doe@test.com", retrieved.getEmail());
        }

        @Test
        @DisplayName("Should create single-user group automatically for new user")
        void shouldCreateSingleUserGroupForNewUser() throws Exception {
            // Given
            User user = createUser("auto-group-user", "Auto", "Group", "auto@test.com");
            sharingService.createUser(user);

            // When
            UserGroup userGroup = sharingService.getGroup(testDomainId, "auto-group-user");

            // Then
            assertNotNull(userGroup, "Single-user group should be created automatically");
            assertEquals(GroupCardinality.SINGLE_USER, userGroup.getGroupCardinality());
        }

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() throws Exception {
            // Given
            User user = createUser("delete-user", "Delete", "Me", "delete@test.com");
            sharingService.createUser(user);
            assertTrue(sharingService.isUserExists(testDomainId, "delete-user"));

            // When
            boolean deleted = sharingService.deleteUser(testDomainId, "delete-user");

            // Then
            assertTrue(deleted);
            assertFalse(sharingService.isUserExists(testDomainId, "delete-user"));
        }
    }

    @Nested
    @DisplayName("Group Management Tests")
    class GroupManagementTests {

        @Test
        @DisplayName("Should create group with owner")
        void shouldCreateGroupWithOwner() throws Exception {
            // Given
            sharingService.createUser(createUser("group-owner", "Group", "Owner", "owner@test.com"));

            UserGroup group = new UserGroup();
            group.setGroupId("test-group");
            group.setDomainId(testDomainId);
            group.setName("Test Group");
            group.setDescription("A test group");
            group.setOwnerId("group-owner");
            group.setGroupType(GroupType.USER_LEVEL_GROUP);

            // When
            String groupId = sharingService.createGroup(group);

            // Then
            assertNotNull(groupId);
            UserGroup retrieved = sharingService.getGroup(testDomainId, "test-group");
            assertNotNull(retrieved);
            assertEquals("Test Group", retrieved.getName());
            assertEquals("group-owner", retrieved.getOwnerId());
            assertEquals(GroupCardinality.MULTI_USER, retrieved.getGroupCardinality());
        }

        @Test
        @DisplayName("Should add users to group")
        void shouldAddUsersToGroup() throws Exception {
            // Given
            sharingService.createUser(createUser("group-owner-2", "Owner", "Two", "o2@test.com"));
            sharingService.createUser(createUser("member-1", "Member", "One", "m1@test.com"));
            sharingService.createUser(createUser("member-2", "Member", "Two", "m2@test.com"));

            UserGroup group = new UserGroup();
            group.setGroupId("member-test-group");
            group.setDomainId(testDomainId);
            group.setName("Member Test Group");
            group.setOwnerId("group-owner-2");
            group.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group);

            // When - Add users
            boolean added = sharingService.addUsersToGroup(
                    testDomainId, List.of("member-1", "member-2"), "member-test-group");

            // Then
            assertTrue(added, "Adding users to group should succeed");
        }

        @Test
        @DisplayName("Should create group and verify owner has owner access")
        void shouldVerifyOwnerHasOwnerAccess() throws Exception {
            // Given
            sharingService.createUser(createUser("admin-owner", "Admin", "Owner", "admin@test.com"));

            UserGroup group = new UserGroup();
            group.setGroupId("admin-test-group");
            group.setDomainId(testDomainId);
            group.setName("Admin Test Group");
            group.setOwnerId("admin-owner");
            group.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group);

            // Then - Verify owner has owner access
            assertTrue(sharingService.hasOwnerAccess(testDomainId, "admin-test-group", "admin-owner"));
        }

        @Test
        @DisplayName("Should verify group exists after creation")
        void shouldVerifyGroupExistsAfterCreation() throws Exception {
            // Given
            sharingService.createUser(createUser("original-owner", "Original", "Owner", "orig@test.com"));

            UserGroup group = new UserGroup();
            group.setGroupId("ownership-test-group");
            group.setDomainId(testDomainId);
            group.setName("Ownership Test Group");
            group.setOwnerId("original-owner");
            group.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group);

            // Then
            assertTrue(sharingService.isGroupExists(testDomainId, "ownership-test-group"));
            UserGroup retrieved = sharingService.getGroup(testDomainId, "ownership-test-group");
            assertNotNull(retrieved);
            assertEquals("Ownership Test Group", retrieved.getName());
        }

        @Test
        @DisplayName("Should get all member groups for user")
        void shouldGetAllMemberGroupsForUser() throws Exception {
            // Given
            sharingService.createUser(createUser("multi-group-user", "Multi", "Group", "multi@test.com"));
            sharingService.createUser(createUser("group-owner-3", "Owner", "Three", "o3@test.com"));

            UserGroup group1 = new UserGroup();
            group1.setGroupId("multi-group-1");
            group1.setDomainId(testDomainId);
            group1.setName("Multi Group 1");
            group1.setOwnerId("group-owner-3");
            group1.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group1);

            UserGroup group2 = new UserGroup();
            group2.setGroupId("multi-group-2");
            group2.setDomainId(testDomainId);
            group2.setName("Multi Group 2");
            group2.setOwnerId("group-owner-3");
            group2.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group2);

            sharingService.addUsersToGroup(testDomainId, List.of("multi-group-user"), "multi-group-1");
            sharingService.addUsersToGroup(testDomainId, List.of("multi-group-user"), "multi-group-2");

            // When
            List<UserGroup> memberGroups =
                    sharingService.getAllMemberGroupsForUser(testDomainId, "multi-group-user");

            // Then
            assertNotNull(memberGroups);
            assertTrue(memberGroups.size() >= 2);
        }
    }

    @Nested
    @DisplayName("Permission Type Tests")
    class PermissionTypeTests {

        @Test
        @DisplayName("Should create and retrieve permission types")
        void shouldCreateAndRetrievePermissionTypes() throws Exception {
            // Given
            PermissionType readPerm = new PermissionType();
            readPerm.setPermissionTypeId("TEST_READ");
            readPerm.setDomainId(testDomainId);
            readPerm.setName("TEST_READ");
            readPerm.setDescription("Test read permission");

            // When
            sharingService.createPermissionType(readPerm);

            // Then
            assertTrue(sharingService.isPermissionExists(testDomainId, "TEST_READ"));
            PermissionType retrieved = sharingService.getPermissionType(testDomainId, "TEST_READ");
            assertNotNull(retrieved);
            assertEquals("Test read permission", retrieved.getDescription());
        }

        @Test
        @DisplayName("Should update permission type")
        void shouldUpdatePermissionType() throws Exception {
            // Given
            PermissionType perm = new PermissionType();
            perm.setPermissionTypeId("UPDATE_PERM");
            perm.setDomainId(testDomainId);
            perm.setName("UPDATE_PERM");
            perm.setDescription("Original description");
            sharingService.createPermissionType(perm);

            // When
            perm.setDescription("Updated description");
            sharingService.updatePermissionType(perm);

            // Then
            PermissionType retrieved = sharingService.getPermissionType(testDomainId, "UPDATE_PERM");
            assertEquals("Updated description", retrieved.getDescription());
        }

        @Test
        @DisplayName("Should delete permission type")
        void shouldDeletePermissionType() throws Exception {
            // Given
            PermissionType perm = new PermissionType();
            perm.setPermissionTypeId("DELETE_PERM");
            perm.setDomainId(testDomainId);
            perm.setName("DELETE_PERM");
            sharingService.createPermissionType(perm);
            assertTrue(sharingService.isPermissionExists(testDomainId, "DELETE_PERM"));

            // When
            boolean deleted = sharingService.deletePermissionType(testDomainId, "DELETE_PERM");

            // Then
            assertTrue(deleted);
            assertFalse(sharingService.isPermissionExists(testDomainId, "DELETE_PERM"));
        }

        @Test
        @DisplayName("Should list permission types for domain")
        void shouldListPermissionTypesForDomain() throws Exception {
            // Given
            PermissionType perm1 = new PermissionType();
            perm1.setPermissionTypeId("LIST_PERM_1");
            perm1.setDomainId(testDomainId);
            perm1.setName("LIST_PERM_1");
            sharingService.createPermissionType(perm1);

            PermissionType perm2 = new PermissionType();
            perm2.setPermissionTypeId("LIST_PERM_2");
            perm2.setDomainId(testDomainId);
            perm2.setName("LIST_PERM_2");
            sharingService.createPermissionType(perm2);

            // When
            List<PermissionType> permissions = sharingService.getPermissionTypes(testDomainId, 0, -1);

            // Then
            assertNotNull(permissions);
            // Should have at least OWNER (auto-created) + our 2 = 3
            assertTrue(permissions.size() >= 3);
        }
    }

    @Nested
    @DisplayName("Entity Type Tests")
    class EntityTypeTests {

        @Test
        @DisplayName("Should create and manage entity types")
        void shouldCreateAndManageEntityTypes() throws Exception {
            // Given
            EntityType entityType = new EntityType();
            entityType.setEntityTypeId("TEST_PROJECT");
            entityType.setDomainId(testDomainId);
            entityType.setName("TEST_PROJECT");
            entityType.setDescription("Test project entity type");

            // When
            sharingService.createEntityType(entityType);

            // Then
            assertTrue(sharingService.isEntityTypeExists(testDomainId, "TEST_PROJECT"));

            EntityType retrieved = sharingService.getEntityType(testDomainId, "TEST_PROJECT");
            assertNotNull(retrieved);
            assertEquals("Test project entity type", retrieved.getDescription());
        }

        @Test
        @DisplayName("Should list entity types for domain")
        void shouldListEntityTypesForDomain() throws Exception {
            // Given
            EntityType et1 = new EntityType();
            et1.setEntityTypeId("ET_1");
            et1.setDomainId(testDomainId);
            et1.setName("Entity Type 1");
            sharingService.createEntityType(et1);

            EntityType et2 = new EntityType();
            et2.setEntityTypeId("ET_2");
            et2.setDomainId(testDomainId);
            et2.setName("Entity Type 2");
            sharingService.createEntityType(et2);

            // When
            List<EntityType> types = sharingService.getEntityTypes(testDomainId, 0, -1);

            // Then
            assertNotNull(types);
            assertTrue(types.size() >= 2);
        }
    }

    @Nested
    @DisplayName("Entity and Sharing Tests")
    class EntityAndSharingTests {

        @Test
        @DisplayName("Should create entity with owner access")
        void shouldCreateEntityWithOwnerAccess() throws Exception {
            // Given
            sharingService.createUser(createUser("entity-owner", "Entity", "Owner", "eo@test.com"));
            createEntityType("ENTITY_TYPE_1");

            SharingEntity entity = new SharingEntity();
            entity.setEntityId("test-entity-1");
            entity.setDomainId(testDomainId);
            entity.setEntityTypeId("ENTITY_TYPE_1");
            entity.setOwnerId("entity-owner");
            entity.setName("Test Entity 1");
            entity.setDescription("Test entity description");
            entity.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());

            // When
            sharingService.createEntity(entity);

            // Then
            assertTrue(sharingService.isEntityExists(testDomainId, "test-entity-1"));
            SharingEntity retrieved = sharingService.getEntity(testDomainId, "test-entity-1");
            assertEquals("Test Entity 1", retrieved.getName());
            assertEquals("entity-owner", retrieved.getOwnerId());
        }

        @Test
        @DisplayName("Should share entity with users and check access")
        void shouldShareEntityWithUsersAndCheckAccess() throws Exception {
            // Given
            sharingService.createUser(createUser("share-owner", "Share", "Owner", "so@test.com"));
            sharingService.createUser(createUser("share-user", "Share", "User", "su@test.com"));
            createEntityType("SHARE_TYPE");
            createPermissionType("SHARE_READ");

            SharingEntity entity = new SharingEntity();
            entity.setEntityId("share-entity-1");
            entity.setDomainId(testDomainId);
            entity.setEntityTypeId("SHARE_TYPE");
            entity.setOwnerId("share-owner");
            entity.setName("Shared Entity");
            entity.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(entity);

            // Verify initial access
            assertFalse(sharingService.userHasAccess(testDomainId, "share-user", "share-entity-1", "SHARE_READ"));

            // When - Share with user
            boolean shared = sharingService.shareEntityWithUsers(
                    testDomainId, "share-entity-1", List.of("share-user"), "SHARE_READ", false);

            // Then
            assertTrue(shared);
            assertTrue(sharingService.userHasAccess(testDomainId, "share-user", "share-entity-1", "SHARE_READ"));
        }

        @Test
        @DisplayName("Should share entity with groups")
        void shouldShareEntityWithGroups() throws Exception {
            // Given
            sharingService.createUser(createUser("group-share-owner", "GS", "Owner", "gso@test.com"));
            sharingService.createUser(createUser("group-member", "Group", "Member", "gm@test.com"));
            createEntityType("GROUP_SHARE_TYPE");
            createPermissionType("GROUP_READ");

            UserGroup group = new UserGroup();
            group.setGroupId("share-group");
            group.setDomainId(testDomainId);
            group.setName("Share Group");
            group.setOwnerId("group-share-owner");
            group.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(group);
            sharingService.addUsersToGroup(testDomainId, List.of("group-member"), "share-group");

            SharingEntity entity = new SharingEntity();
            entity.setEntityId("group-share-entity");
            entity.setDomainId(testDomainId);
            entity.setEntityTypeId("GROUP_SHARE_TYPE");
            entity.setOwnerId("group-share-owner");
            entity.setName("Group Shared Entity");
            entity.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(entity);

            // When
            boolean shared = sharingService.shareEntityWithGroups(
                    testDomainId, "group-share-entity", List.of("share-group"), "GROUP_READ", false);

            // Then
            assertTrue(shared);
            assertTrue(sharingService.userHasAccess(
                    testDomainId, "group-member", "group-share-entity", "GROUP_READ"));
        }

        @Test
        @DisplayName("Should cascade permissions to child entities")
        void shouldCascadePermissionsToChildEntities() throws Exception {
            // Given
            sharingService.createUser(createUser("cascade-owner", "Cascade", "Owner", "co@test.com"));
            sharingService.createUser(createUser("cascade-user", "Cascade", "User", "cu@test.com"));
            createEntityType("PARENT_TYPE");
            createEntityType("CHILD_TYPE");
            createPermissionType("CASCADE_READ");

            // Create parent entity
            SharingEntity parent = new SharingEntity();
            parent.setEntityId("parent-entity");
            parent.setDomainId(testDomainId);
            parent.setEntityTypeId("PARENT_TYPE");
            parent.setOwnerId("cascade-owner");
            parent.setName("Parent Entity");
            parent.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(parent);

            // Share parent with cascading
            sharingService.shareEntityWithUsers(
                    testDomainId, "parent-entity", List.of("cascade-user"), "CASCADE_READ", true);

            // Create child entity after sharing
            SharingEntity child = new SharingEntity();
            child.setEntityId("child-entity");
            child.setDomainId(testDomainId);
            child.setEntityTypeId("CHILD_TYPE");
            child.setOwnerId("cascade-owner");
            child.setName("Child Entity");
            child.setParentEntityId("parent-entity");
            child.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(child);

            // Then - Child should inherit permissions
            assertTrue(
                    sharingService.userHasAccess(testDomainId, "cascade-user", "child-entity", "CASCADE_READ"));
        }

        @Test
        @DisplayName("Should revoke entity sharing")
        void shouldRevokeEntitySharing() throws Exception {
            // Given
            sharingService.createUser(createUser("revoke-owner", "Revoke", "Owner", "ro@test.com"));
            sharingService.createUser(createUser("revoke-user", "Revoke", "User", "ru@test.com"));
            createEntityType("REVOKE_TYPE");
            createPermissionType("REVOKE_PERM");

            SharingEntity entity = new SharingEntity();
            entity.setEntityId("revoke-entity");
            entity.setDomainId(testDomainId);
            entity.setEntityTypeId("REVOKE_TYPE");
            entity.setOwnerId("revoke-owner");
            entity.setName("Revoke Entity");
            entity.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(entity);

            sharingService.shareEntityWithUsers(
                    testDomainId, "revoke-entity", List.of("revoke-user"), "REVOKE_PERM", false);
            assertTrue(sharingService.userHasAccess(testDomainId, "revoke-user", "revoke-entity", "REVOKE_PERM"));

            // When
            boolean revoked = sharingService.revokeEntitySharingFromUsers(
                    testDomainId, "revoke-entity", List.of("revoke-user"), "REVOKE_PERM");

            // Then
            assertTrue(revoked);
            assertFalse(
                    sharingService.userHasAccess(testDomainId, "revoke-user", "revoke-entity", "REVOKE_PERM"));
        }

        @Test
        @DisplayName("Should get shared count for entity")
        void shouldGetSharedCountForEntity() throws Exception {
            // Given
            sharingService.createUser(createUser("count-owner", "Count", "Owner", "cto@test.com"));
            sharingService.createUser(createUser("count-user-1", "Count", "User1", "ctu1@test.com"));
            sharingService.createUser(createUser("count-user-2", "Count", "User2", "ctu2@test.com"));
            createEntityType("COUNT_TYPE");
            createPermissionType("COUNT_PERM");

            SharingEntity entity = new SharingEntity();
            entity.setEntityId("count-entity");
            entity.setDomainId(testDomainId);
            entity.setEntityTypeId("COUNT_TYPE");
            entity.setOwnerId("count-owner");
            entity.setName("Count Entity");
            entity.setOriginalEntityCreationTime(
                    IdGenerator.getUniqueTimestamp().getTime());
            sharingService.createEntity(entity);

            // Initially
            SharingEntity initial = sharingService.getEntity(testDomainId, "count-entity");
            assertTrue(initial.getSharedCount() == null || initial.getSharedCount() == 0);

            // Share with users
            sharingService.shareEntityWithUsers(
                    testDomainId, "count-entity", List.of("count-user-1", "count-user-2"), "COUNT_PERM", false);

            // Then
            SharingEntity afterShare = sharingService.getEntity(testDomainId, "count-entity");
            assertTrue(afterShare.getSharedCount() >= 2);
        }
    }

    @Nested
    @DisplayName("Entity Search Tests")
    class EntitySearchTests {

        @Test
        @DisplayName("Should search entities by name")
        void shouldSearchEntitiesByName() throws Exception {
            // Given
            sharingService.createUser(createUser("search-owner", "Search", "Owner", "searchowner@test.com"));
            createEntityType("SEARCH_TYPE");
            createPermissionType("SEARCH_PERM");

            SharingEntity entity1 = createEntity("search-entity-1", "SEARCH_TYPE", "search-owner", "Alpha Project");
            SharingEntity entity2 = createEntity("search-entity-2", "SEARCH_TYPE", "search-owner", "Beta Project");
            sharingService.createEntity(entity1);
            sharingService.createEntity(entity2);

            sharingService.shareEntityWithUsers(
                    testDomainId, "search-entity-1", List.of("search-owner"), "SEARCH_PERM", false);

            // When
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria nameCriteria = new SearchCriteria();
            nameCriteria.setSearchField(EntitySearchField.NAME);
            nameCriteria.setSearchCondition(SearchCondition.LIKE);
            nameCriteria.setValue("Alpha");
            filters.add(nameCriteria);

            List<SharingEntity> results = sharingService.searchEntities(testDomainId, "search-owner", filters, 0, -1);

            // Then
            assertNotNull(results);
            assertTrue(results.stream().anyMatch(e -> "Alpha Project".equals(e.getName())));
        }

        @Test
        @DisplayName("Should search entities by full text")
        void shouldSearchEntitiesByFullText() throws Exception {
            // Given
            sharingService.createUser(createUser("fulltext-owner", "FT", "Owner", "fto@test.com"));
            createEntityType("FT_TYPE");
            createPermissionType("FT_PERM");

            SharingEntity entity = createEntity("ft-entity", "FT_TYPE", "fulltext-owner", "Gaussian Experiment");
            entity.setFullText("gaussian chemistry molecular dynamics simulation");
            sharingService.createEntity(entity);

            // When
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria ftCriteria = new SearchCriteria();
            ftCriteria.setSearchField(EntitySearchField.FULL_TEXT);
            ftCriteria.setSearchCondition(SearchCondition.FULL_TEXT);
            ftCriteria.setValue("chemistry");
            filters.add(ftCriteria);

            List<SharingEntity> results = sharingService.searchEntities(testDomainId, "fulltext-owner", filters, 0, -1);

            // Then
            assertNotNull(results);
            assertTrue(results.stream().anyMatch(e -> "ft-entity".equals(e.getEntityId())));
        }

        @Test
        @DisplayName("Should search entities by entity type")
        void shouldSearchEntitiesByEntityType() throws Exception {
            // Given
            sharingService.createUser(createUser("type-search-owner", "TS", "Owner", "tso@test.com"));
            createEntityType("TYPE_A");
            createEntityType("TYPE_B");

            SharingEntity entityA = createEntity("type-a-entity", "TYPE_A", "type-search-owner", "Type A Entity");
            SharingEntity entityB = createEntity("type-b-entity", "TYPE_B", "type-search-owner", "Type B Entity");
            sharingService.createEntity(entityA);
            sharingService.createEntity(entityB);

            // When
            List<SearchCriteria> filters = new ArrayList<>();
            SearchCriteria typeCriteria = new SearchCriteria();
            typeCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            typeCriteria.setSearchCondition(SearchCondition.EQUAL);
            typeCriteria.setValue("TYPE_A");
            filters.add(typeCriteria);

            List<SharingEntity> results =
                    sharingService.searchEntities(testDomainId, "type-search-owner", filters, 0, -1);

            // Then
            assertNotNull(results);
            assertTrue(results.stream().allMatch(e -> "TYPE_A".equals(e.getEntityTypeId())));
        }
    }

    // Helper methods

    private User createUser(String userId, String firstName, String lastName, String email) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userId);
        user.setDomainId(testDomainId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    private void createEntityType(String entityTypeId) throws Exception {
        if (!sharingService.isEntityTypeExists(testDomainId, entityTypeId)) {
            EntityType entityType = new EntityType();
            entityType.setEntityTypeId(entityTypeId);
            entityType.setDomainId(testDomainId);
            entityType.setName(entityTypeId);
            entityType.setDescription("Test entity type " + entityTypeId);
            sharingService.createEntityType(entityType);
        }
    }

    private void createPermissionType(String permissionTypeId) throws Exception {
        if (!sharingService.isPermissionExists(testDomainId, permissionTypeId)) {
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(permissionTypeId);
            permissionType.setDomainId(testDomainId);
            permissionType.setName(permissionTypeId);
            permissionType.setDescription("Test permission " + permissionTypeId);
            sharingService.createPermissionType(permissionType);
        }
    }

    private SharingEntity createEntity(String entityId, String entityTypeId, String ownerId, String name) {
        SharingEntity entity = new SharingEntity();
        entity.setEntityId(entityId);
        entity.setDomainId(testDomainId);
        entity.setEntityTypeId(entityTypeId);
        entity.setOwnerId(ownerId);
        entity.setName(name);
        entity.setDescription("Test entity " + entityId);
        entity.setOriginalEntityCreationTime(IdGenerator.getUniqueTimestamp().getTime());
        return entity;
    }
}
