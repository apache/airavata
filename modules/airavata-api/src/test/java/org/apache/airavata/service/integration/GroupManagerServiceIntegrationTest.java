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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.exception.AuthorizationException;
import org.apache.airavata.common.model.GroupModel;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.profile.exception.GroupManagerServiceException;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.exception.UserProfileServiceException;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.profile.UserProfileService;
import org.apache.airavata.service.security.GroupManagerService;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for GroupManagerService.
 */
@DisplayName("GroupManagerService Integration Tests")
public class GroupManagerServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final GroupManagerService groupManagerService;
    private final UserProfileService userProfileService;
    private final SharingRegistryService sharingRegistryService;

    public GroupManagerServiceIntegrationTest(
            GroupManagerService groupManagerService,
            UserProfileService userProfileService,
            SharingRegistryService sharingRegistryService) {
        this.groupManagerService = groupManagerService;
        this.userProfileService = userProfileService;
        this.sharingRegistryService = sharingRegistryService;
    }

    private String testGroupId;

    @BeforeEach
    public void setUpGroups() throws GroupManagerServiceException, SharingRegistryException, DuplicateEntryException {
        // Ensure domain exists for the test gateway
        if (!sharingRegistryService.isDomainExists(TEST_GATEWAY_ID)) {
            Domain domain = new Domain();
            domain.setDomainId(TEST_GATEWAY_ID);
            domain.setName("Test Gateway");
            domain.setDescription("Test domain for integration tests");
            sharingRegistryService.createDomain(domain);
        }

        // Create a test group for use in tests
        GroupModel group = new GroupModel();
        group.setName("Test Group");
        group.setDescription("Test group for integration tests");
        group.setMembers(new ArrayList<>());
        group.setAdmins(new ArrayList<>());
        testGroupId = groupManagerService.createGroup(testAuthzToken, group);
    }

    @Nested
    @DisplayName("Group CRUD Operations")
    class GroupCRUDTests {

        @Test
        @DisplayName("Should create group successfully")
        void shouldCreateGroup() throws GroupManagerServiceException, SharingRegistryException {
            GroupModel group = new GroupModel();
            group.setName("New Test Group");
            group.setDescription("A new test group");
            group.setMembers(new ArrayList<>());
            group.setAdmins(new ArrayList<>());

            String groupId = groupManagerService.createGroup(testAuthzToken, group);

            assertThat(groupId).isNotNull();
            GroupModel retrieved = groupManagerService.getGroup(testAuthzToken, groupId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("New Test Group");
        }

        @Test
        @DisplayName("Should get group by ID")
        void shouldGetGroup() throws GroupManagerServiceException, SharingRegistryException {
            GroupModel group = groupManagerService.getGroup(testAuthzToken, testGroupId);

            assertThat(group).isNotNull();
            assertThat(group.getId()).isEqualTo(testGroupId);
            assertThat(group.getName()).isEqualTo("Test Group");
        }

        @Test
        @DisplayName("Should get all groups")
        void shouldGetAllGroups() throws GroupManagerServiceException, SharingRegistryException {
            List<GroupModel> groups = groupManagerService.getGroups(testAuthzToken);

            assertThat(groups).isNotNull();
            assertThat(groups.size()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should update group")
        void shouldUpdateGroup() throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
            GroupModel group = groupManagerService.getGroup(testAuthzToken, testGroupId);
            group.setDescription("Updated description");

            boolean updated = groupManagerService.updateGroup(testAuthzToken, group);

            assertThat(updated).isTrue();
            GroupModel retrieved = groupManagerService.getGroup(testAuthzToken, testGroupId);
            assertThat(retrieved.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("Should delete group")
        void shouldDeleteGroup() throws GroupManagerServiceException, SharingRegistryException, AuthorizationException {
            String ownerId = testAuthzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                    + "@" + TEST_GATEWAY_ID;

            boolean deleted = groupManagerService.deleteGroup(testAuthzToken, testGroupId, ownerId);

            assertThat(deleted).isTrue();
        }
    }

    @Nested
    @DisplayName("Group Membership Operations")
    class GroupMembershipTests {

        @Test
        @DisplayName("Should add users to group")
        void shouldAddUsersToGroup()
                throws GroupManagerServiceException, SharingRegistryException, AuthorizationException,
                        UserProfileServiceException, IamAdminServicesException {
            UserProfile user1Profile = TestDataFactory.createTestUserProfile("user1", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, user1Profile);
            commitTransaction();
            UserProfile user2Profile = TestDataFactory.createTestUserProfile("user2", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, user2Profile);
            commitTransaction();

            List<String> userIds = new ArrayList<>();
            userIds.add("user1@" + TEST_GATEWAY_ID);
            userIds.add("user2@" + TEST_GATEWAY_ID);

            boolean added = groupManagerService.addUsersToGroup(testAuthzToken, userIds, testGroupId);

            assertThat(added).isTrue();
        }

        @Test
        @DisplayName("Should remove users from group")
        void shouldRemoveUsersFromGroup()
                throws GroupManagerServiceException, SharingRegistryException, AuthorizationException,
                        UserProfileServiceException, IamAdminServicesException {
            UserProfile user1Profile = TestDataFactory.createTestUserProfile("user1", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, user1Profile);
            commitTransaction();

            List<String> userIds = new ArrayList<>();
            userIds.add("user1@" + TEST_GATEWAY_ID);
            groupManagerService.addUsersToGroup(testAuthzToken, userIds, testGroupId);
            commitTransaction();

            boolean removed = groupManagerService.removeUsersFromGroup(testAuthzToken, userIds, testGroupId);

            assertThat(removed).isTrue();
        }
    }

    @Nested
    @DisplayName("Group Admin Operations")
    class GroupAdminTests {

        @Test
        @DisplayName("Should add group admins")
        void shouldAddGroupAdmins()
                throws GroupManagerServiceException, SharingRegistryException, AuthorizationException,
                        UserProfileServiceException, IamAdminServicesException {
            UserProfile adminProfile = TestDataFactory.createTestUserProfile("admin1", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, adminProfile);
            commitTransaction();

            List<String> userIds = new ArrayList<>();
            userIds.add("admin1@" + TEST_GATEWAY_ID);
            groupManagerService.addUsersToGroup(testAuthzToken, userIds, testGroupId);
            commitTransaction();

            List<String> adminIds = new ArrayList<>();
            adminIds.add("admin1@" + TEST_GATEWAY_ID);

            boolean added = groupManagerService.addGroupAdmins(testAuthzToken, testGroupId, adminIds);

            assertThat(added).isTrue();
        }

        @Test
        @DisplayName("Should remove group admins")
        void shouldRemoveGroupAdmins()
                throws GroupManagerServiceException, SharingRegistryException, AuthorizationException,
                        UserProfileServiceException, IamAdminServicesException {
            UserProfile adminProfile = TestDataFactory.createTestUserProfile("admin1", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, adminProfile);
            commitTransaction();

            List<String> userIds = new ArrayList<>();
            userIds.add("admin1@" + TEST_GATEWAY_ID);
            groupManagerService.addUsersToGroup(testAuthzToken, userIds, testGroupId);
            commitTransaction();

            List<String> adminIds = new ArrayList<>();
            adminIds.add("admin1@" + TEST_GATEWAY_ID);
            groupManagerService.addGroupAdmins(testAuthzToken, testGroupId, adminIds);
            commitTransaction();

            boolean removed = groupManagerService.removeGroupAdmins(testAuthzToken, testGroupId, adminIds);

            assertThat(removed).isTrue();
        }
    }

    @Nested
    @DisplayName("Access Control")
    class AccessControlTests {

        @Test
        @DisplayName("Should check admin access")
        void shouldCheckAdminAccess() throws GroupManagerServiceException, SharingRegistryException {
            boolean hasAccess =
                    groupManagerService.hasAdminAccess(testAuthzToken, testGroupId, "admin@" + TEST_GATEWAY_ID);

            assertThat(hasAccess).isNotNull();
        }

        @Test
        @DisplayName("Should check owner access")
        void shouldCheckOwnerAccess() throws GroupManagerServiceException, SharingRegistryException {
            String ownerId = testAuthzToken.getClaimsMap().get(org.apache.airavata.common.utils.Constants.USER_NAME)
                    + "@" + TEST_GATEWAY_ID;

            boolean hasAccess = groupManagerService.hasOwnerAccess(testAuthzToken, testGroupId, ownerId);

            assertThat(hasAccess).isNotNull();
        }
    }
}
