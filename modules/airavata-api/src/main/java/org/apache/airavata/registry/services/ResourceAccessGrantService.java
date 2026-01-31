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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.ResourceAccessGrant;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.registry.entities.ResourceAccessGrantEntity;
import org.apache.airavata.registry.mappers.ResourceAccessGrantMapper;
import org.apache.airavata.registry.repositories.ResourceAccessGrantRepository;
import org.apache.airavata.registry.repositories.ResourceAccessRepository;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for unified resource access grants (credential + compute resource + deployment settings).
 */
@Service
@Transactional
public class ResourceAccessGrantService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceAccessGrantService.class);

    private final ResourceAccessGrantRepository repository;
    private final ResourceAccessGrantMapper mapper;
    private final ResourceAccessRepository resourceAccessRepository;
    private final CredentialStoreService credentialStoreService;

    public ResourceAccessGrantService(
            ResourceAccessGrantRepository repository,
            ResourceAccessGrantMapper mapper,
            ResourceAccessRepository resourceAccessRepository,
            CredentialStoreService credentialStoreService) {
        this.repository = repository;
        this.mapper = mapper;
        this.resourceAccessRepository = resourceAccessRepository;
        this.credentialStoreService = credentialStoreService;
    }

    /**
     * Returns the set of compute resource IDs the credential has access to,
     * merging RESOURCE_ACCESS (COMPUTE) and RESOURCE_ACCESS_GRANT.
     */
    public Set<String> getComputeResourceIdsForCredential(String credentialToken) {
        Set<String> ids = new LinkedHashSet<>();
        resourceAccessRepository.findByCredentialToken(credentialToken).stream()
                .filter(ra -> ra.getResourceType() == PreferenceResourceType.COMPUTE && ra.isEnabled())
                .map(ra -> ra.getResourceId())
                .forEach(ids::add);
        getByCredentialEnabled(credentialToken).stream()
                .map(ResourceAccessGrant::getComputeResourceId)
                .forEach(ids::add);
        return ids;
    }

    public ResourceAccessGrant create(ResourceAccessGrant grant) throws CredentialStoreException {
        if (!credentialStoreService.credentialExists(grant.getCredentialToken(), grant.getGatewayId())) {
            throw new CredentialStoreException(
                    "Credential does not exist: gatewayId=" + grant.getGatewayId()
                            + ", tokenId=" + grant.getCredentialToken());
        }
        ResourceAccessGrantEntity entity = mapper.toEntity(grant);
        ResourceAccessGrantEntity saved = repository.save(entity);
        return mapper.toModel(saved);
    }

    public Optional<ResourceAccessGrant> getById(Long id) {
        return repository.findById(id).map(mapper::toModel);
    }

    public Optional<ResourceAccessGrant> getByGatewayCredentialAndResource(
            String gatewayId, String credentialToken, String computeResourceId) {
        return repository
                .findByGatewayIdAndCredentialTokenAndComputeResourceId(
                        gatewayId, credentialToken, computeResourceId)
                .map(mapper::toModel);
    }

    public List<ResourceAccessGrant> getByGateway(String gatewayId) {
        return mapper.toModelList(repository.findByGatewayId(gatewayId));
    }

    public List<ResourceAccessGrant> getByGatewayEnabled(String gatewayId) {
        return mapper.toModelList(repository.findByGatewayIdAndEnabledTrue(gatewayId));
    }

    public List<ResourceAccessGrant> getByCredential(String credentialToken) {
        return mapper.toModelList(repository.findByCredentialToken(credentialToken));
    }

    public List<ResourceAccessGrant> getByCredentialEnabled(String credentialToken) {
        return mapper.toModelList(repository.findByCredentialTokenAndEnabledTrue(credentialToken));
    }

    public List<ResourceAccessGrant> getByComputeResource(String computeResourceId) {
        return mapper.toModelList(repository.findByComputeResourceId(computeResourceId));
    }

    public List<ResourceAccessGrant> getByComputeResourceEnabled(String computeResourceId) {
        return mapper.toModelList(repository.findByComputeResourceIdAndEnabledTrue(computeResourceId));
    }

    public List<ResourceAccessGrant> getByGatewayAndComputeResource(String gatewayId, String computeResourceId) {
        return mapper.toModelList(repository.findByGatewayIdAndComputeResourceId(gatewayId, computeResourceId));
    }

    public ResourceAccessGrant update(Long id, ResourceAccessGrant grant) throws CredentialStoreException {
        if (!credentialStoreService.credentialExists(grant.getCredentialToken(), grant.getGatewayId())) {
            throw new CredentialStoreException(
                    "Credential does not exist: gatewayId=" + grant.getGatewayId()
                            + ", tokenId=" + grant.getCredentialToken());
        }
        ResourceAccessGrantEntity existing =
                repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Grant not found: " + id));
        ResourceAccessGrantEntity updated = mapper.toEntity(grant);
        updated.setId(existing.getId());
        updated.setCreationTime(existing.getCreationTime());
        ResourceAccessGrantEntity saved = repository.save(updated);
        return mapper.toModel(saved);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
