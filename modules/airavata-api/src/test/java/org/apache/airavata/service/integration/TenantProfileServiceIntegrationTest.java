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
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.profile.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.TenantProfileService;
import org.apache.airavata.common.utils.AiravataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TenantProfileService Integration Tests - Gateway CRUD operations, listing, and duplicate checks")
public class TenantProfileServiceIntegrationTest extends ServiceIntegrationTestBase {

    private final TenantProfileService tenantProfileService;

    public TenantProfileServiceIntegrationTest(TenantProfileService tenantProfileService) {
        this.tenantProfileService = tenantProfileService;
    }

    @Nested
    @DisplayName("Gateway CRUD Operations")
    class GatewayCRUDTests {

        @Test
        @DisplayName(
                "Should create gateway with unique ID, name, URL, and approval status, then retrieve it successfully")
        void shouldCreateGateway() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-create-" + AiravataUtils.getUniqueTimestamp().getTime();
            String gatewayName = "Test Gateway " + uniqueId;
            String gatewayURL = "https://test-gateway-" + uniqueId + ".example.com";
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName(gatewayName);
            gateway.setGatewayURL(gatewayURL);
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);

            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);
            assertThat(internalId).isNotNull().isNotEmpty();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo(uniqueId);
            assertThat(retrieved.getGatewayName()).isEqualTo(gatewayName);
            assertThat(retrieved.getGatewayURL()).isEqualTo(gatewayURL);
            assertThat(retrieved.getGatewayApprovalStatus()).isEqualTo(GatewayApprovalStatus.CREATED);
        }

        @Test
        @DisplayName("Should update gateway name and persist changes while keeping ID unchanged")
        void shouldUpdateGateway() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-update-" + AiravataUtils.getUniqueTimestamp().getTime();
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName("Test Gateway " + uniqueId);
            gateway.setGatewayURL("https://test-gateway-" + uniqueId + ".example.com");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            Gateway toUpdate = tenantProfileService.getGateway(testAuthzToken, internalId);
            String updatedName = "Updated Gateway Name";
            toUpdate.setGatewayName(updatedName);
            boolean updated = tenantProfileService.updateGateway(testAuthzToken, toUpdate);

            assertThat(updated).isTrue();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayName()).isEqualTo(updatedName);
            assertThat(retrieved.getGatewayId()).isEqualTo(uniqueId);
        }

        @Test
        @DisplayName("Should get gateway by internal ID with all fields including name, URL, and approval status")
        void shouldGetGateway() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-get-" + AiravataUtils.getUniqueTimestamp().getTime();
            String gatewayName = "Test Gateway " + uniqueId;
            String gatewayURL = "https://test-gateway-" + uniqueId + ".example.com";
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName(gatewayName);
            gateway.setGatewayURL(gatewayURL);
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo(uniqueId);
            assertThat(retrieved.getGatewayName()).isEqualTo(gatewayName);
            assertThat(retrieved.getGatewayURL()).isEqualTo(gatewayURL);
            assertThat(retrieved.getGatewayApprovalStatus()).isEqualTo(GatewayApprovalStatus.CREATED);
        }

        @Test
        @DisplayName("Should delete gateway and verify it no longer exists")
        void shouldDeleteGateway() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-delete-" + AiravataUtils.getUniqueTimestamp().getTime();
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName("Test Gateway " + uniqueId);
            gateway.setGatewayURL("https://test-gateway-" + uniqueId + ".example.com");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            assertThat(tenantProfileService.isGatewayExist(testAuthzToken, gateway.getGatewayId()))
                    .isTrue();

            boolean deleted = tenantProfileService.deleteGateway(testAuthzToken, internalId, gateway.getGatewayId());
            assertThat(deleted).isTrue();
            assertThat(tenantProfileService.isGatewayExist(testAuthzToken, gateway.getGatewayId()))
                    .isFalse();
            assertThatThrownBy(() -> tenantProfileService.getGateway(testAuthzToken, internalId))
                    .isInstanceOf(TenantProfileServiceException.class);
        }
    }

    @Nested
    @DisplayName("Gateway Listing")
    class GatewayListingTests {

        @Test
        @DisplayName("Should get all gateways and verify created gateway is in the list")
        void shouldGetAllGateways() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-list-" + AiravataUtils.getUniqueTimestamp().getTime();
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName("Test Gateway " + uniqueId);
            gateway.setGatewayURL("https://test-gateway-" + uniqueId + ".example.com");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            List<Gateway> gateways = tenantProfileService.getAllGateways(testAuthzToken);

            assertThat(gateways).isNotNull().isNotEmpty();
            assertThat(gateways.stream().anyMatch(g -> uniqueId.equals(g.getGatewayId())))
                    .isTrue();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo(uniqueId);
        }

        @Test
        @DisplayName("Should get all gateways for user and verify all belong to specified user")
        void shouldGetAllGatewaysForUser() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-user-" + AiravataUtils.getUniqueTimestamp().getTime();
            String requesterUsername = "test-user";
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName("Test Gateway " + uniqueId);
            gateway.setGatewayURL("https://test-gateway-" + uniqueId + ".example.com");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            gateway.setRequesterUsername(requesterUsername);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            List<Gateway> gateways = tenantProfileService.getAllGatewaysForUser(testAuthzToken, requesterUsername);

            assertThat(gateways).isNotNull().isNotEmpty();
            assertThat(gateways.stream().anyMatch(g -> uniqueId.equals(g.getGatewayId())))
                    .isTrue();
            assertThat(gateways.stream().allMatch(g -> requesterUsername.equals(g.getRequesterUsername())))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Gateway Duplicate Checks")
    class GatewayDuplicateTests {

        @Test
        @DisplayName("Should check if gateway exists and verify it can be retrieved")
        void shouldCheckGatewayExists() throws TenantProfileServiceException, CredentialStoreException {
            String uniqueId = "test-gateway-exists-" + AiravataUtils.getUniqueTimestamp().getTime();
            Gateway gateway = TestDataFactory.createTestGateway(uniqueId);
            gateway.setGatewayName("Test Gateway " + uniqueId);
            gateway.setGatewayURL("https://test-gateway-" + uniqueId + ".example.com");
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.CREATED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            boolean exists = tenantProfileService.isGatewayExist(testAuthzToken, gateway.getGatewayId());

            assertThat(exists).isTrue();
            Gateway retrieved = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getGatewayId()).isEqualTo(uniqueId);
        }

        @Test
        @DisplayName("Should prevent duplicate gateway creation and keep original gateway unchanged")
        void shouldPreventDuplicateGateway() throws TenantProfileServiceException, CredentialStoreException {
            String duplicateId = "test-gateway-duplicate-" + AiravataUtils.getUniqueTimestamp().getTime();
            Gateway gateway = TestDataFactory.createTestGateway(duplicateId);
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            String internalId = tenantProfileService.addGateway(testAuthzToken, gateway);

            assertThat(tenantProfileService.isGatewayExist(testAuthzToken, duplicateId))
                    .isTrue();

            Gateway duplicate = TestDataFactory.createTestGateway(duplicateId);
            duplicate.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            assertThatThrownBy(() -> tenantProfileService.addGateway(testAuthzToken, duplicate))
                    .isInstanceOf(TenantProfileServiceException.class);
            Gateway original = tenantProfileService.getGateway(testAuthzToken, internalId);
            assertThat(original).isNotNull();
            assertThat(original.getGatewayId()).isEqualTo(duplicateId);
        }
    }
}
