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
package org.apache.airavata.registry.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.airavata.registry.entities.ResourceAccessGrantEntity;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for {@link ResourceAccessGrantRepository}.
 * Ensures a CREDENTIALS row exists for the (gatewayId, credentialToken) used by RESOURCE_ACCESS_GRANT FK (via TestBase.ensureCredentialExists).
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ResourceAccessGrantRepositoryTest extends TestBase {

    private final ResourceAccessGrantRepository repository;

    public ResourceAccessGrantRepositoryTest(ResourceAccessGrantRepository repository) {
        this.repository = repository;
    }

    @Test
    void saveAndFindByGatewayId() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessGrantEntity entity = new ResourceAccessGrantEntity();
        entity.setGatewayId(gatewayId);
        entity.setCredentialToken(credentialToken);
        entity.setComputeResourceId(computeResourceId);
        entity.setEnabled(true);

        ResourceAccessGrantEntity saved = repository.save(entity);
        assertNotNull(saved.getId());
        flushAndClear();

        List<ResourceAccessGrantEntity> byGateway = repository.findByGatewayId(gatewayId);
        assertFalse(byGateway.isEmpty());
        assertEquals(1, byGateway.size());
        assertEquals(gatewayId, byGateway.get(0).getGatewayId());
        assertEquals(credentialToken, byGateway.get(0).getCredentialToken());
        assertEquals(computeResourceId, byGateway.get(0).getComputeResourceId());
    }

    @Test
    void findByCredentialToken() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessGrantEntity entity = new ResourceAccessGrantEntity();
        entity.setGatewayId(gatewayId);
        entity.setCredentialToken(credentialToken);
        entity.setComputeResourceId(computeResourceId);
        entity.setEnabled(true);
        repository.save(entity);
        flushAndClear();

        List<ResourceAccessGrantEntity> byCred = repository.findByCredentialToken(credentialToken);
        assertFalse(byCred.isEmpty());
        assertEquals(credentialToken, byCred.get(0).getCredentialToken());
    }

    @Test
    void findByComputeResourceId() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessGrantEntity entity = new ResourceAccessGrantEntity();
        entity.setGatewayId(gatewayId);
        entity.setCredentialToken(credentialToken);
        entity.setComputeResourceId(computeResourceId);
        entity.setEnabled(true);
        repository.save(entity);
        flushAndClear();

        List<ResourceAccessGrantEntity> byResource = repository.findByComputeResourceId(computeResourceId);
        assertFalse(byResource.isEmpty());
        assertEquals(computeResourceId, byResource.get(0).getComputeResourceId());
    }

    @Test
    void findByGatewayIdAndCredentialTokenAndComputeResourceId() {
        String gatewayId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);
        String credentialToken = "token-" + UUID.randomUUID().toString().substring(0, 8);
        String computeResourceId = "compute-" + UUID.randomUUID().toString().substring(0, 8);
        ensureCredentialExists(gatewayId, credentialToken);

        ResourceAccessGrantEntity entity = new ResourceAccessGrantEntity();
        entity.setGatewayId(gatewayId);
        entity.setCredentialToken(credentialToken);
        entity.setComputeResourceId(computeResourceId);
        entity.setEnabled(true);
        repository.save(entity);
        flushAndClear();

        Optional<ResourceAccessGrantEntity> found = repository.findByGatewayIdAndCredentialTokenAndComputeResourceId(
                gatewayId, credentialToken, computeResourceId);
        assertTrue(found.isPresent());
        assertEquals(gatewayId, found.get().getGatewayId());
        assertEquals(credentialToken, found.get().getCredentialToken());
        assertEquals(computeResourceId, found.get().getComputeResourceId());
    }
}
