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
package org.apache.airavata.sharing.services;

import java.util.List;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.apache.airavata.registry.repositories.GatewayRepository;
import org.apache.airavata.sharing.mappers.DomainMapper;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing sharing domains.
 *
 * <p>Domains are now stored as part of GatewayEntity. The domainId corresponds to
 * the gateway's gatewayId. This service provides a domain-centric view of gateway
 * data for sharing registry operations.
 */
@Service
@Transactional
public class DomainService {
    private final GatewayRepository gatewayRepository;
    private final DomainMapper domainMapper;

    public DomainService(GatewayRepository gatewayRepository, DomainMapper domainMapper) {
        this.gatewayRepository = gatewayRepository;
        this.domainMapper = domainMapper;
    }

    /**
     * Gets a domain by its ID (which is the same as the gateway's gatewayId).
     *
     * @param domainId the domain ID (equivalent to gatewayId)
     * @return the domain, or null if not found
     */
    public Domain get(String domainId) throws SharingRegistryException {
        var entity = gatewayRepository.findByGatewayId(domainId).orElse(null);
        if (entity == null) return null;
        return domainMapper.toModel(entity);
    }

    /**
     * Creates a new domain. Since domains are stored in GatewayEntity,
     * this updates the domain-related fields of an existing gateway.
     *
     * @param domain the domain to create
     * @return the created domain
     */
    public Domain create(Domain domain) throws SharingRegistryException {
        return update(domain);
    }

    /**
     * Updates domain fields on an existing gateway.
     *
     * @param domain the domain with updated values
     * @return the updated domain
     */
    public Domain update(Domain domain) throws SharingRegistryException {
        // Find existing gateway by gatewayId (which equals domainId)
        GatewayEntity existingGateway = gatewayRepository.findByGatewayId(domain.getDomainId()).orElse(null);
        if (existingGateway == null) {
            throw new SharingRegistryException("Gateway not found for domainId: " + domain.getDomainId());
        }

        // Update only domain-related fields
        existingGateway.setDomainDescription(domain.getDescription());
        existingGateway.setDomainCreatedTime(domain.getCreatedTime());
        existingGateway.setLastUpdatedTime(domain.getUpdatedTime());
        existingGateway.setInitialUserGroupId(domain.getInitialUserGroupId());

        // If domain name is provided and differs, optionally update gatewayName
        // Note: This may need policy decision - whether domain name can update gateway name
        if (domain.getName() != null && !domain.getName().equals(existingGateway.getGatewayName())) {
            existingGateway.setGatewayName(domain.getName());
        }

        var saved = gatewayRepository.save(existingGateway);
        return domainMapper.toModel(saved);
    }

    /**
     * Deleting a domain is not supported since domains are part of gateways.
     * Gateway deletion should be handled through GatewayService.
     *
     * @param domainId the domain ID
     * @return always throws exception
     * @throws SharingRegistryException always - domain deletion not supported
     */
    public boolean delete(String domainId) throws SharingRegistryException {
        throw new SharingRegistryException(
                "Domain deletion is not supported. Domains are part of gateways. "
                        + "Delete the gateway instead via GatewayService.");
    }

    /**
     * Checks if a domain exists (by checking if the corresponding gateway exists).
     *
     * @param domainId the domain ID (equivalent to gatewayId)
     * @return true if the gateway/domain exists
     */
    public boolean isExists(String domainId) throws SharingRegistryException {
        return gatewayRepository.findByGatewayId(domainId).isPresent();
    }

    /**
     * Gets all domains (as domain views of all gateways).
     *
     * @return list of all domains
     */
    public List<Domain> getAll() throws SharingRegistryException {
        var entities = gatewayRepository.findAll();
        return domainMapper.toModelList(entities);
    }
}
