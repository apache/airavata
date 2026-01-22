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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.airavata.common.model.Status;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.exception.UserProfileServiceException;
import org.apache.airavata.service.profile.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName(
        "UserProfileService Integration Tests - User profile CRUD operations, existence checks, and data validation")
public class UserProfileServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final UserProfileService userProfileService;

    public UserProfileServiceIntegrationTest(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Nested
    @DisplayName("User Profile CRUD Operations")
    class UserProfileCRUDTests {

        @Test
        @DisplayName("Should add user profile with all required fields and persist successfully")
        void shouldAddUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-add-user", TEST_GATEWAY_ID);

            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);
            assertThat(userId).isNotNull().isEqualTo("test-add-user");
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserId()).isEqualTo(userId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
            assertThat(retrieved.getAiravataInternalUserId()).isEqualTo(userId + "@" + TEST_GATEWAY_ID);
            assertThat(retrieved.getFirstName()).isEqualTo("Test");
            assertThat(retrieved.getLastName()).isEqualTo("User");
            assertThat(retrieved.getEmails()).isNotEmpty();
            assertThat(retrieved.getState()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should update user profile first and last name and persist changes successfully")
        void shouldUpdateUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-update-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            UserProfile toUpdate = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            toUpdate.setFirstName("Updated");
            toUpdate.setLastName("Name");
            boolean updated = userProfileService.updateUserProfile(testAuthzToken, toUpdate);
            assertThat(updated).isTrue();
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved.getFirstName()).isEqualTo("Updated");
            assertThat(retrieved.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("Should get user profile by ID and gateway with all fields including creation time")
        void shouldGetUserProfileById() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-get-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserId()).isEqualTo(userId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
            assertThat(retrieved.getAiravataInternalUserId()).isEqualTo(userId + "@" + TEST_GATEWAY_ID);
            assertThat(retrieved.getFirstName()).isEqualTo("Test");
            assertThat(retrieved.getLastName()).isEqualTo("User");
            assertThat(retrieved.getEmails()).isNotEmpty();
            assertThat(retrieved.getState()).isEqualTo(Status.ACTIVE);
            assertThat(retrieved.getCreationTime()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should throw exception when getting non-existent user profile")
        void shouldThrowExceptionForNonExistentUser() {
            assertThatThrownBy(() ->
                            userProfileService.getUserProfileById(testAuthzToken, "non-existent-user", TEST_GATEWAY_ID))
                    .isInstanceOf(UserProfileServiceException.class);
        }

        @Test
        @DisplayName("Should delete user profile and verify it no longer exists")
        void shouldDeleteUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-delete-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            assertThat(userProfileService.doesUserExist(testAuthzToken, userId, TEST_GATEWAY_ID))
                    .isTrue();

            boolean deleted = userProfileService.deleteUserProfile(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(deleted).isTrue();
            assertThat(userProfileService.doesUserExist(testAuthzToken, userId, TEST_GATEWAY_ID))
                    .isFalse();
            assertThatThrownBy(() -> userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID))
                    .isInstanceOf(UserProfileServiceException.class);
        }
    }

    @Nested
    @DisplayName("User Existence Checks")
    class UserExistenceTests {

        @Test
        @DisplayName("Should return true when user exists and verify user can be retrieved")
        void shouldReturnTrueWhenUserExists() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-exists-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            boolean exists = userProfileService.doesUserExist(testAuthzToken, userId, TEST_GATEWAY_ID);

            assertThat(exists).isTrue();
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should return false when user does not exist")
        void shouldReturnFalseWhenUserDoesNotExist() throws UserProfileServiceException {
            boolean exists = userProfileService.doesUserExist(testAuthzToken, "non-existent-user", TEST_GATEWAY_ID);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("User Profile Listing")
    class UserProfileListingTests {

        @Test
        @DisplayName("Should get all user profiles in gateway with pagination and verify created users are included")
        void shouldGetAllUserProfilesInGateway() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile user1 = TestDataFactory.createTestUserProfile("test-list-user1", TEST_GATEWAY_ID);
            UserProfile user2 = TestDataFactory.createTestUserProfile("test-list-user2", TEST_GATEWAY_ID);
            String userId1 = userProfileService.addUserProfile(testAuthzToken, user1);
            String userId2 = userProfileService.addUserProfile(testAuthzToken, user2);

            var users = userProfileService.getAllUserProfilesInGateway(testAuthzToken, TEST_GATEWAY_ID, 0, 10);

            assertThat(users).isNotNull().isNotEmpty();
            assertThat(users.size()).isGreaterThanOrEqualTo(2);
            assertThat(users.stream().anyMatch(u -> userId1.equals(u.getUserId())))
                    .isTrue();
            assertThat(users.stream().anyMatch(u -> userId2.equals(u.getUserId())))
                    .isTrue();
            assertThat(users.stream().allMatch(u -> TEST_GATEWAY_ID.equals(u.getGatewayId())))
                    .isTrue();
        }

        @Test
        @DisplayName("Should respect pagination limits and offset for retrieving user profiles")
        void shouldRespectPaginationLimits() throws UserProfileServiceException, IamAdminServicesException {
            for (int i = 0; i < 5; i++) {
                UserProfile user = TestDataFactory.createTestUserProfile("test-pag-user" + i, TEST_GATEWAY_ID);
                userProfileService.addUserProfile(testAuthzToken, user);
            }

            var users = userProfileService.getAllUserProfilesInGateway(testAuthzToken, TEST_GATEWAY_ID, 0, 2);

            assertThat(users).isNotNull();
            assertThat(users.size()).isLessThanOrEqualTo(2);
            var nextPage = userProfileService.getAllUserProfilesInGateway(testAuthzToken, TEST_GATEWAY_ID, 2, 2);
            assertThat(nextPage).isNotNull();
            assertThat(nextPage.size()).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("User Profile Data Validation")
    class UserProfileValidationTests {

        @Test
        @DisplayName("Should preserve user profile state when creating and retrieving")
        void shouldPreserveUserProfileState() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-state-user", TEST_GATEWAY_ID);
            userProfile.setState(Status.ACTIVE);

            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getState()).isEqualTo(Status.ACTIVE);
            assertThat(retrieved.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should preserve multiple email addresses when creating and retrieving user profile")
        void shouldPreserveEmailAddresses() throws UserProfileServiceException, IamAdminServicesException {
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-email-user", TEST_GATEWAY_ID);
            if (userProfile.getEmails() == null) {
                userProfile.setEmails(new java.util.ArrayList<>());
            }
            userProfile.getEmails().add("email1@example.com");
            userProfile.getEmails().add("email2@example.com");

            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getEmails()).isNotNull().isNotEmpty();
            assertThat(retrieved.getEmails().size()).isGreaterThanOrEqualTo(2);
            assertThat(retrieved.getEmails()).contains("email1@example.com", "email2@example.com");
        }
    }
}
