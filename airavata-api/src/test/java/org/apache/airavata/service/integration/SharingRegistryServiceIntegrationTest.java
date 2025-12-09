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
import org.apache.airavata.sharing.models.Domain;
import org.apache.airavata.sharing.models.DuplicateEntryException;
import org.apache.airavata.sharing.models.Entity;
import org.apache.airavata.sharing.models.EntityType;
import org.apache.airavata.sharing.models.PermissionType;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.User;
import org.apache.airavata.sharing.models.UserGroup;
import org.apache.airavata.service.SharingRegistryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for SharingRegistryService.
 */
@DisplayName("SharingRegistryService Integration Tests")
public class SharingRegistryServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private SharingRegistryService sharingRegistryService;

    @Nested
    @DisplayName("Domain Operations")
    class DomainOperationsTests {

        @Test
        @DisplayName("Should create domain")
        void shouldCreateDomain() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            Domain domain = new Domain();
            domain.setDomainId(TEST_GATEWAY_ID);
            domain.setName("Test Domain");
            domain.setDescription("Test domain for integration tests");

            // Act
            String domainId = sharingRegistryService.createDomain(domain);

            // Assert
            assertThat(domainId).isEqualTo(TEST_GATEWAY_ID);
        }

        @Test
        @DisplayName("Should get domain")
        void shouldGetDomain() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            Domain domain = new Domain();
            domain.setDomainId(TEST_GATEWAY_ID);
            domain.setName("Test Domain");
            try {
                sharingRegistryService.createDomain(domain);
            } catch (DuplicateEntryException e) {
                // Domain may already exist, that's okay
            }

            // Act
            Domain retrieved = sharingRegistryService.getDomain(TEST_GATEWAY_ID);

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getDomainId()).isEqualTo(TEST_GATEWAY_ID);
        }
    }

    @Nested
    @DisplayName("Entity Operations")
    class EntityOperationsTests {

        @Test
        @DisplayName("Should create entity")
        void shouldCreateEntity() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            Entity entity = new Entity();
            entity.setEntityId("test-entity-1");
            entity.setDomainId(TEST_GATEWAY_ID);
            entity.setEntityTypeId("EXPERIMENT");
            entity.setOwnerId("test-user@" + TEST_GATEWAY_ID);
            entity.setName("Test Entity");

            // Act
            String entityId = sharingRegistryService.createEntity(entity);

            // Assert
            assertThat(entityId).isNotNull();
        }

        @Test
        @DisplayName("Should get entity")
        void shouldGetEntity() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            Entity entity = new Entity();
            entity.setEntityId("test-entity-2");
            entity.setDomainId(TEST_GATEWAY_ID);
            entity.setEntityTypeId("EXPERIMENT");
            entity.setOwnerId("test-user@" + TEST_GATEWAY_ID);
            entity.setName("Test Entity 2");
            try {
                sharingRegistryService.createEntity(entity);
            } catch (DuplicateEntryException e) {
                // Entity may already exist, that's okay
            }

            // Act
            Entity retrieved = sharingRegistryService.getEntity(TEST_GATEWAY_ID, "test-entity-2");

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getEntityId()).isEqualTo("test-entity-2");
        }
    }

    @Nested
    @DisplayName("User and Group Operations")
    class UserAndGroupOperationsTests {

        @Test
        @DisplayName("Should create user")
        void shouldCreateUser() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            User user = new User();
            user.setUserId("test-sharing-user@" + TEST_GATEWAY_ID);
            user.setDomainId(TEST_GATEWAY_ID);
            user.setUserName("test-sharing-user");

            // Act
            String userId = sharingRegistryService.createUser(user);

            // Assert
            assertThat(userId).isNotNull();
        }

        @Test
        @DisplayName("Should create group")
        void shouldCreateGroup() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            UserGroup group = new UserGroup();
            group.setGroupId("test-group-1");
            group.setDomainId(TEST_GATEWAY_ID);
            group.setName("Test Group");
            group.setOwnerId("test-user@" + TEST_GATEWAY_ID);

            // Act
            String groupId = sharingRegistryService.createGroup(group);

            // Assert
            assertThat(groupId).isNotNull();
        }
    }

    @Nested
    @DisplayName("Permission Operations")
    class PermissionOperationsTests {

        @Test
        @DisplayName("Should create permission type")
        void shouldCreatePermissionType() throws SharingRegistryException, DuplicateEntryException {
            // Arrange
            PermissionType permissionType = new PermissionType();
            permissionType.setPermissionTypeId(TEST_GATEWAY_ID + ":READ");
            permissionType.setDomainId(TEST_GATEWAY_ID);
            permissionType.setName("READ");
            permissionType.setDescription("Read permission");

            // Act
            String permissionId = sharingRegistryService.createPermissionType(permissionType);

            // Assert
            assertThat(permissionId).isNotNull();
        }
    }
}

