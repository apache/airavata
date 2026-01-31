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
package org.apache.airavata.registry.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.ResourceAccessGrant;
import org.apache.airavata.registry.entities.ResourceAccessEntity;
import org.apache.airavata.registry.entities.ResourceAccessGrantEntity;
import org.apache.airavata.registry.repositories.ResourceAccessGrantRepository;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Integration tests for ResourceAccessGrantService, including getComputeResourceIdsForCredential merge.
 */
@Import(ResourceAccessGrantServiceTest.CredentialStoreServiceTestConfig.class)
public class ResourceAccessGrantServiceTest extends TestBase {

    @TestConfiguration
    static class CredentialStoreServiceTestConfig {
        @Bean
        CredentialStoreService credentialStoreService() {
            CredentialStoreService service = mock(CredentialStoreService.class);
            when(service.credentialExists(anyString(), anyString())).thenReturn(true);
            return service;
        }
    }

    @Autowired
    private ResourceAccessGrantService resourceAccessGrantService;

    @Autowired
    private ResourceAccessRepository resourceAccessRepository;

    @Autowired
    private ResourceAccessGrantRepository resourceAccessGrantRepository;

    @Test
    void getComputeResourceIdsForCredential_mergesResourceAccessAndGrants() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeFromRa = "compute-ra-" + UUID.randomUUID().toString().substring(0, 8);
        String computeFromRag = "compute-rag-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessEntity ra = new ResourceAccessEntity();
        ra.setResourceType(PreferenceResourceType.COMPUTE);
        ra.setResourceId(computeFromRa);
        ra.setOwnerId(gatewayId);
        ra.setOwnerType(PreferenceLevel.GATEWAY);
        ra.setGatewayId(gatewayId);
        ra.setCredentialToken(credentialToken);
        ra.setEnabled(true);
        resourceAccessRepository.save(ra);

        ResourceAccessGrantEntity ragEntity = new ResourceAccessGrantEntity();
        ragEntity.setGatewayId(gatewayId);
        ragEntity.setCredentialToken(credentialToken);
        ragEntity.setComputeResourceId(computeFromRag);
        ragEntity.setEnabled(true);
        resourceAccessGrantRepository.save(ragEntity);

        flushAndClear();

        Set<String> ids = resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken);
        assertTrue(ids.contains(computeFromRa), "Should include resource from RESOURCE_ACCESS");
        assertTrue(ids.contains(computeFromRag), "Should include resource from RESOURCE_ACCESS_GRANT");
        assertEquals(2, ids.size());
    }

    @Test
    void getComputeResourceIdsForCredential_excludesDisabled() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessEntity ra = new ResourceAccessEntity();
        ra.setResourceType(PreferenceResourceType.COMPUTE);
        ra.setResourceId(computeId);
        ra.setOwnerId(gatewayId);
        ra.setOwnerType(PreferenceLevel.GATEWAY);
        ra.setGatewayId(gatewayId);
        ra.setCredentialToken(credentialToken);
        ra.setEnabled(false);
        resourceAccessRepository.save(ra);

        flushAndClear();

        Set<String> ids = resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken);
        assertTrue(ids.isEmpty(), "Disabled RESOURCE_ACCESS should not be included");
    }

    @Test
    void createAndGetByCredential() throws CredentialStoreException {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessGrant grant = new ResourceAccessGrant();
        grant.setGatewayId(gatewayId);
        grant.setCredentialToken(credentialToken);
        grant.setComputeResourceId(computeResourceId);
        grant.setEnabled(true);

        ResourceAccessGrant created = resourceAccessGrantService.create(grant);
        assertTrue(created.getId() != null);
        flushAndClear();

        var list = resourceAccessGrantService.getByCredential(credentialToken);
        assertEquals(1, list.size());
        assertEquals(computeResourceId, list.get(0).getComputeResourceId());
    }

    /**
     * Integration test for dual-write: when a COMPUTE ResourceAccess is created (e.g. via ResourceAccessController),
     * the controller also creates a ResourceAccessGrant. This test simulates that flow and verifies merged read
     * returns the compute resource id.
     */
    @Test
    void dualWriteSync_afterCreateComputeAccess_grantExistsAndMergedReadReturnsIt() throws CredentialStoreException {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessEntity ra = new ResourceAccessEntity();
        ra.setResourceType(PreferenceResourceType.COMPUTE);
        ra.setResourceId(computeResourceId);
        ra.setOwnerId(gatewayId);
        ra.setOwnerType(PreferenceLevel.GATEWAY);
        ra.setGatewayId(gatewayId);
        ra.setCredentialToken(credentialToken);
        ra.setEnabled(true);
        resourceAccessRepository.save(ra);

        ResourceAccessGrant grant = new ResourceAccessGrant();
        grant.setGatewayId(gatewayId);
        grant.setCredentialToken(credentialToken);
        grant.setComputeResourceId(computeResourceId);
        grant.setEnabled(true);
        resourceAccessGrantService.create(grant);

        flushAndClear();

        assertTrue(
                resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken).contains(computeResourceId),
                "Merged read should return compute resource after dual-write");
        assertEquals(
                1,
                resourceAccessGrantRepository.findByCredentialToken(credentialToken).size(),
                "ResourceAccessGrant should exist after sync");
    }

    /**
     * Integration test for sync-on-delete: when a COMPUTE ResourceAccess is deleted, the controller also deletes
     * the corresponding ResourceAccessGrant. This test simulates that flow and verifies merged read no longer
     * returns the resource.
     */
    @Test
    void dualWriteSync_afterDeleteComputeAccess_grantRemovedAndMergedReadExcludes() throws CredentialStoreException {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessEntity ra = new ResourceAccessEntity();
        ra.setResourceType(PreferenceResourceType.COMPUTE);
        ra.setResourceId(computeResourceId);
        ra.setOwnerId(gatewayId);
        ra.setOwnerType(PreferenceLevel.GATEWAY);
        ra.setGatewayId(gatewayId);
        ra.setCredentialToken(credentialToken);
        ra.setEnabled(true);
        ResourceAccessEntity savedRa = resourceAccessRepository.save(ra);

        ResourceAccessGrant grant = new ResourceAccessGrant();
        grant.setGatewayId(gatewayId);
        grant.setCredentialToken(credentialToken);
        grant.setComputeResourceId(computeResourceId);
        grant.setEnabled(true);
        ResourceAccessGrant createdRag = resourceAccessGrantService.create(grant);

        flushAndClear();

        assertTrue(
                resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken).contains(computeResourceId),
                "Merged read should include resource before delete");

        resourceAccessGrantService.getByGatewayCredentialAndResource(gatewayId, credentialToken, computeResourceId)
                .ifPresent(rag -> resourceAccessGrantService.delete(rag.getId()));
        resourceAccessRepository.deleteById(savedRa.getId());

        flushAndClear();

        Set<String> ids = resourceAccessGrantService.getComputeResourceIdsForCredential(credentialToken);
        assertTrue(ids.isEmpty(), "Merged read should not return resource after sync-on-delete");
        assertTrue(createdRag.getId() != null);
    }
}
