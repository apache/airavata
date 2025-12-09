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

import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.profile.user.cpi.exception.UserProfileServiceException;
import org.apache.airavata.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for UserProfileService.
 */
@DisplayName("UserProfileService Integration Tests")
public class UserProfileServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final UserProfileService userProfileService;

    public UserProfileServiceIntegrationTest(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Nested
    @DisplayName("User Profile CRUD Operations")
    class UserProfileCRUDTests {

        @Test
        @DisplayName("Should add user profile successfully")
        void shouldAddUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-add-user", TEST_GATEWAY_ID);

            // Act
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            // Assert
            assertThat(userId).isNotNull().isEqualTo("test-add-user");
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserId()).isEqualTo(userId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
        }

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-update-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            // Act
            UserProfile toUpdate = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            toUpdate.setFirstName("Updated");
            toUpdate.setLastName("Name");
            boolean updated = userProfileService.updateUserProfile(testAuthzToken, toUpdate);

            // Assert
            assertThat(updated).isTrue();
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);
            assertThat(retrieved.getFirstName()).isEqualTo("Updated");
            assertThat(retrieved.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("Should get user profile by ID and gateway")
        void shouldGetUserProfileById() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-get-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            // Act
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getUserId()).isEqualTo(userId);
            assertThat(retrieved.getGatewayId()).isEqualTo(TEST_GATEWAY_ID);
            assertThat(retrieved.getAiravataInternalUserId()).isEqualTo(userId + "@" + TEST_GATEWAY_ID);
        }

        @Test
        @DisplayName("Should throw exception when getting non-existent user profile")
        void shouldThrowExceptionForNonExistentUser() {
            // Act & Assert
            assertThatThrownBy(() ->
                            userProfileService.getUserProfileById(testAuthzToken, "non-existent-user", TEST_GATEWAY_ID))
                    .isInstanceOf(UserProfileServiceException.class);
        }

        @Test
        @DisplayName("Should delete user profile successfully")
        void shouldDeleteUserProfile() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-delete-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            // Act
            boolean deleted = userProfileService.deleteUserProfile(testAuthzToken, userId, TEST_GATEWAY_ID);

            // Assert
            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID))
                    .isInstanceOf(UserProfileServiceException.class);
        }
    }

    @Nested
    @DisplayName("User Existence Checks")
    class UserExistenceTests {

        @Test
        @DisplayName("Should return true when user exists")
        void shouldReturnTrueWhenUserExists() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-exists-user", TEST_GATEWAY_ID);
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);

            // Act
            boolean exists = userProfileService.doesUserExist(testAuthzToken, userId, TEST_GATEWAY_ID);

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when user does not exist")
        void shouldReturnFalseWhenUserDoesNotExist() throws UserProfileServiceException {
            // Act
            boolean exists = userProfileService.doesUserExist(testAuthzToken, "non-existent-user", TEST_GATEWAY_ID);

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("User Profile Listing")
    class UserProfileListingTests {

        @Test
        @DisplayName("Should get all user profiles in gateway with pagination")
        void shouldGetAllUserProfilesInGateway() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile user1 = TestDataFactory.createTestUserProfile("test-list-user1", TEST_GATEWAY_ID);
            UserProfile user2 = TestDataFactory.createTestUserProfile("test-list-user2", TEST_GATEWAY_ID);
            userProfileService.addUserProfile(testAuthzToken, user1);
            userProfileService.addUserProfile(testAuthzToken, user2);

            // Act
            var users = userProfileService.getAllUserProfilesInGateway(testAuthzToken, TEST_GATEWAY_ID, 0, 10);

            // Assert
            assertThat(users).isNotNull();
            assertThat(users.size()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should respect pagination limits")
        void shouldRespectPaginationLimits() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            for (int i = 0; i < 5; i++) {
                UserProfile user = TestDataFactory.createTestUserProfile("test-pag-user" + i, TEST_GATEWAY_ID);
                userProfileService.addUserProfile(testAuthzToken, user);
            }

            // Act
            var users = userProfileService.getAllUserProfilesInGateway(testAuthzToken, TEST_GATEWAY_ID, 0, 2);

            // Assert
            assertThat(users).isNotNull();
            assertThat(users.size()).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("User Profile Data Validation")
    class UserProfileValidationTests {

        @Test
        @DisplayName("Should preserve user profile state")
        void shouldPreserveUserProfileState() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-state-user", TEST_GATEWAY_ID);
            userProfile.setState(Status.ACTIVE);

            // Act
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);

            // Assert
            assertThat(retrieved.getState()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Should preserve email addresses")
        void shouldPreserveEmailAddresses() throws UserProfileServiceException, IamAdminServicesException {
            // Arrange
            UserProfile userProfile = TestDataFactory.createTestUserProfile("test-email-user", TEST_GATEWAY_ID);
            userProfile.addToEmails("email1@example.com");
            userProfile.addToEmails("email2@example.com");

            // Act
            String userId = userProfileService.addUserProfile(testAuthzToken, userProfile);
            UserProfile retrieved = userProfileService.getUserProfileById(testAuthzToken, userId, TEST_GATEWAY_ID);

            // Assert
            assertThat(retrieved.getEmails()).isNotNull();
            assertThat(retrieved.getEmailsSize()).isGreaterThanOrEqualTo(1);
        }
    }
}
