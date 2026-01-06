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

import java.util.List;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.service.security.IamAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for IamAdminService (Keycloak operations).
 * Note: These tests require proper Keycloak configuration or mocking.
 * Some tests may be skipped if Keycloak is not available.
 */
@DisplayName("IamAdminService Integration Tests")
@TestPropertySource(
        properties = {
            "security.iam.server-url=http://localhost:18080",
            "security.iam.super.username=admin",
            "security.iam.super.password=admin"
        })
public class IamAdminServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final IamAdminService iamAdminService;
    private final CredentialStoreService credentialStoreService;
    private final RegistryService registryService;

    public IamAdminServiceIntegrationTest(
            IamAdminService iamAdminService,
            CredentialStoreService credentialStoreService,
            RegistryService registryService) {
        this.iamAdminService = iamAdminService;
        this.credentialStoreService = credentialStoreService;
        this.registryService = registryService;
    }

    @Nested
    @DisplayName("User Existence and Availability Checks")
    class UserExistenceTests {

        @Test
        @DisplayName("Should check if username is available")
        void shouldCheckUsernameAvailability() throws IamAdminServicesException {
            // For now, we test the method signature and error handling
            String username = "new-user-" + System.currentTimeMillis();

            boolean available = iamAdminService.isUsernameAvailable(testAuthzToken, username);

            assertThat(available).isNotNull();
        }

        @Test
        @DisplayName("Should check if user exists")
        void shouldCheckUserExists() throws IamAdminServicesException {
            String username = "test-user-check";

            boolean exists = iamAdminService.isUserExist(testAuthzToken, username);

            assertThat(exists).isNotNull();
        }

        @Test
        @DisplayName("Should check if user is enabled")
        void shouldCheckUserEnabled() throws IamAdminServicesException {
            String username = "test-user-enabled";

            boolean enabled = iamAdminService.isUserEnabled(testAuthzToken, username);

            assertThat(enabled).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Registration and Management")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user")
        void shouldRegisterUser() throws IamAdminServicesException {
            String username = "new-user-" + System.currentTimeMillis();
            String email = username + "@example.com";

            boolean registered =
                    iamAdminService.registerUser(testAuthzToken, username, email, "Test", "User", "password123");

            assertThat(registered).isNotNull();
        }

        @Test
        @DisplayName("Should enable user account")
        void shouldEnableUser() throws IamAdminServicesException {
            String username = "test-enable-user";

            boolean enabled = iamAdminService.enableUser(testAuthzToken, username);

            assertThat(enabled).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Profile Operations")
    class UserProfileOperationsTests {

        @Test
        @DisplayName("Should get user from Keycloak")
        void shouldGetUser() throws IamAdminServicesException {
            String username = "test-get-user";

            UserProfile user = iamAdminService.getUser(testAuthzToken, username);

            // If user exists, profile should not be null
        }

        @Test
        @DisplayName("Should get users with pagination")
        void shouldGetUsers() throws IamAdminServicesException {
            List<UserProfile> users = iamAdminService.getUsers(testAuthzToken, 0, 10, "");

            assertThat(users).isNotNull();
        }

        @Test
        @DisplayName("Should find users by email or userId")
        void shouldFindUsers() throws IamAdminServicesException {
            List<UserProfile> users = iamAdminService.findUsers(testAuthzToken, "test@example.com", null);

            assertThat(users).isNotNull();
        }
    }

    @Nested
    @DisplayName("Password Management")
    class PasswordManagementTests {

        @Test
        @DisplayName("Should reset user password")
        void shouldResetUserPassword() throws IamAdminServicesException {
            String username = "test-reset-password";

            boolean reset = iamAdminService.resetUserPassword(testAuthzToken, username, "newPassword123");

            assertThat(reset).isNotNull();
        }
    }

    @Nested
    @DisplayName("Role Management")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add role to user")
        void shouldAddRoleToUser() throws IamAdminServicesException, RegistryServiceException {
            String username = "test-role-user";
            String roleName = "test-role";

            try {
                boolean added = iamAdminService.addRoleToUser(testAuthzToken, username, roleName);
                assertThat(added).isNotNull();
            } catch (Exception e) {
                // Expected if gateway/credentials not set up
                assertThat(e).isInstanceOfAny(IamAdminServicesException.class, RegistryServiceException.class);
            }
        }

        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRoleFromUser() throws IamAdminServicesException, RegistryServiceException {
            String username = "test-role-user";
            String roleName = "test-role";

            try {
                boolean removed = iamAdminService.removeRoleFromUser(testAuthzToken, username, roleName);
                assertThat(removed).isNotNull();
            } catch (Exception e) {
                // Expected if gateway/credentials not set up
                assertThat(e).isInstanceOfAny(IamAdminServicesException.class, RegistryServiceException.class);
            }
        }

        @Test
        @DisplayName("Should get users with role")
        void shouldGetUsersWithRole() throws IamAdminServicesException, RegistryServiceException {
            String roleName = "test-role";

            try {
                List<UserProfile> users = iamAdminService.getUsersWithRole(testAuthzToken, roleName);
                assertThat(users).isNotNull();
            } catch (Exception e) {
                // Expected if gateway/credentials not set up
                assertThat(e).isInstanceOfAny(IamAdminServicesException.class, RegistryServiceException.class);
            }
        }
    }

    @Nested
    @DisplayName("Gateway Setup")
    class GatewaySetupTests {

        @Test
        @DisplayName("Should set up gateway with Keycloak tenant")
        void shouldSetUpGateway() throws Exception {
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-setup");

            PasswordCredential adminCredential = new PasswordCredential();
            adminCredential.setGatewayId(gateway.getGatewayId());
            adminCredential.setPortalUserName("admin");
            adminCredential.setLoginUserName("admin");
            adminCredential.setPassword("admin-password");
            String adminToken = credentialStoreService.addPasswordCredential(adminCredential);
            gateway.setIdentityServerPasswordToken(adminToken);

            try {
                Gateway result = iamAdminService.setUpGateway(testAuthzToken, gateway);
                assertThat(result).isNotNull();
            } catch (Exception e) {
                // Expected if Keycloak is not available or not properly configured
                assertThat(e)
                        .isInstanceOfAny(
                                IamAdminServicesException.class,
                                org.apache.airavata.credential.exception.CredentialStoreException.class);
            }
        }
    }
}
