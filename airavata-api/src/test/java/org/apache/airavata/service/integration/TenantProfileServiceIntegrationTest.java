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

import java.util.List;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.TenantProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for TenantProfileService (Gateway operations).
 */
@DisplayName("TenantProfileService Integration Tests")
public class TenantProfileServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final TenantProfileService tenantProfileService;

    public TenantProfileServiceIntegrationTest(TenantProfileService tenantProfileService) {
        this.tenantProfileService = tenantProfileService;
    }

    @Nested
    @DisplayName("Gateway CRUD Operations")
    class GatewayCRUDTests {

        @Test
        @DisplayName("Should create gateway successfully")
        void shouldCreateGateway() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-create");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);

            // Act
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            // Assert
            assertThat(internalId).isNotNull();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo("test-gateway-create");
        }

        @Test
        @DisplayName("Should update gateway successfully")
        void shouldUpdateGateway() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-update");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            Gateway toUpdate = tenantProfileService.getGateway(testAuthzToken, internalId);
            toUpdate.setGatewayName("Updated Gateway Name");
            boolean updated = tenantProfileService.updateGateway(testAuthzToken, toUpdate);

            // Assert
            assertThat(updated).isTrue();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved.getGatewayName()).isEqualTo("Updated Gateway Name");
        }

        @Test
        @DisplayName("Should get gateway by internal ID")
        void shouldGetGateway() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-get");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo("test-gateway-get");
        }

        @Test
        @DisplayName("Should delete gateway successfully")
        void shouldDeleteGateway() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-delete");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            boolean deleted = tenantProfileService.deleteGateway(testAuthzToken, internalId, gateway.getGatewayId());

            // Assert
            assertThat(deleted).isTrue();
            assertThatThrownBy(() -> tenantProfileService.getGateway(testAuthzToken, internalId))
                    .isInstanceOf(TenantProfileServiceException.class);
        }
    }

    @Nested
    @DisplayName("Gateway Listing")
    class GatewayListingTests {

        @Test
        @DisplayName("Should get all gateways")
        void shouldGetAllGateways() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-list");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            List<Gateway> gateways = tenantProfileService.getAllGateways(testAuthzToken);

            // Assert
            assertThat(gateways).isNotNull();
        }

        @Test
        @DisplayName("Should get all gateways for user")
        void shouldGetAllGatewaysForUser() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-user");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            gateway.setRequesterUsername("test-user");
            tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            List<Gateway> gateways = tenantProfileService.getAllGatewaysForUser(testAuthzToken, "test-user");

            // Assert
            assertThat(gateways).isNotNull();
        }
    }

    @Nested
    @DisplayName("Gateway Duplicate Checks")
    class GatewayDuplicateTests {

        @Test
        @DisplayName("Should check if gateway exists")
        void shouldCheckGatewayExists() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-exists");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act
            boolean exists = tenantProfileService.isGatewayExist(testAuthzToken, gateway.getGatewayId());

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should prevent duplicate gateway creation")
        void shouldPreventDuplicateGateway() throws TenantProfileServiceException, CredentialStoreException {
            // Arrange
            Gateway gateway = TestDataFactory.createTestGateway("test-gateway-duplicate");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            tenantProfileService.addGateway(testAuthzToken, gateway);

            // Act & Assert
            Gateway duplicate = TestDataFactory.createTestGateway("test-gateway-duplicate");
            duplicate.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            assertThatThrownBy(() -> tenantProfileService.addGateway(testAuthzToken, duplicate))
                    .isInstanceOf(TenantProfileServiceException.class);
        }
    }
}
