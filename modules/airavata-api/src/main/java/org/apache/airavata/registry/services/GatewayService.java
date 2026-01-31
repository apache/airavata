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

import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.mappers.GatewayMapper;
import org.apache.airavata.registry.repositories.GatewayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing Gateway entities.
 *
 * <p>This service uses the unified GatewayRepository which supports queries by both
 * airavataInternalGatewayId (primary key) and gatewayId (human-readable identifier).
 */
@Service
@Transactional
public class GatewayService {
    private final GatewayRepository gatewayRepository;
    private final GatewayMapper gatewayMapper;

    public GatewayService(GatewayRepository gatewayRepository, GatewayMapper gatewayMapper) {
        this.gatewayRepository = gatewayRepository;
        this.gatewayMapper = gatewayMapper;
    }

    /**
     * Check if a gateway exists by its human-readable gateway ID.
     *
     * @param gatewayId the gateway ID
     * @return true if the gateway exists
     */
    public boolean isGatewayExist(String gatewayId) throws RegistryException {
        return gatewayRepository.existsByGatewayId(gatewayId);
    }

    /**
     * Check if a gateway exists by its internal Airavata ID.
     *
     * @param airavataInternalGatewayId the internal gateway ID (UUID)
     * @return true if the gateway exists
     */
    public boolean isGatewayExistByInternalId(String airavataInternalGatewayId) throws RegistryException {
        return gatewayRepository.existsById(airavataInternalGatewayId);
    }

    /**
     * Get a gateway by its human-readable gateway ID.
     *
     * @param gatewayId the gateway ID
     * @return the Gateway model or null if not found
     */
    public Gateway getGateway(String gatewayId) throws RegistryException {
        var entity = gatewayRepository.findByGatewayId(gatewayId).orElse(null);
        if (entity == null) return null;
        return gatewayMapper.toModel(entity);
    }

    /**
     * Get a gateway by its internal Airavata ID (primary key).
     *
     * @param airavataInternalGatewayId the internal gateway ID (UUID)
     * @return the Gateway model or null if not found
     */
    public Gateway getGatewayByInternalId(String airavataInternalGatewayId) throws RegistryException {
        var entity = gatewayRepository.findByAiravataInternalGatewayId(airavataInternalGatewayId).orElse(null);
        if (entity == null) return null;
        return gatewayMapper.toModel(entity);
    }

    /**
     * Get all gateways.
     *
     * @return list of all Gateway models
     */
    public List<Gateway> getAllGateways() throws RegistryException {
        var entities = gatewayRepository.findAllGateways();
        return gatewayMapper.toModelList(entities);
    }

    /**
     * Get gateways by name pattern.
     *
     * @param gatewayName the gateway name pattern (supports LIKE matching)
     * @return list of matching Gateway models
     */
    public List<Gateway> getGatewaysByName(String gatewayName) throws RegistryException {
        var entities = gatewayRepository.findByGatewayName(gatewayName);
        return gatewayMapper.toModelList(entities);
    }

    /**
     * Get gateways by approval status.
     *
     * @param status the approval status
     * @return list of Gateway models with the specified status
     */
    public List<Gateway> getGatewaysByApprovalStatus(GatewayApprovalStatus status) throws RegistryException {
        var entities = gatewayRepository.findByGatewayApprovalStatus(status);
        return gatewayMapper.toModelList(entities);
    }

    /**
     * Get gateways requested by a specific user.
     *
     * @param requesterUsername the username of the requester
     * @return list of Gateway models requested by the user
     */
    public List<Gateway> getGatewaysForUser(String requesterUsername) throws RegistryException {
        var entities = gatewayRepository.findByRequesterUsername(requesterUsername);
        return gatewayMapper.toModelList(entities);
    }

    /**
     * Remove a gateway by its human-readable gateway ID.
     *
     * @param gatewayId the gateway ID
     */
    public void removeGateway(String gatewayId) throws RegistryException {
        var entity = gatewayRepository.findByGatewayId(gatewayId).orElse(null);
        if (entity != null) {
            gatewayRepository.delete(entity);
        }
    }

    /**
     * Remove a gateway by its internal Airavata ID.
     *
     * @param airavataInternalGatewayId the internal gateway ID (UUID)
     * @return true if the gateway was deleted, false if it didn't exist
     */
    public boolean removeGatewayByInternalId(String airavataInternalGatewayId) throws RegistryException {
        if (gatewayRepository.existsById(airavataInternalGatewayId)) {
            gatewayRepository.deleteById(airavataInternalGatewayId);
            return true;
        }
        return false;
    }

    /**
     * Add a new gateway.
     *
     * @param gateway the Gateway model to add
     * @return the airavataInternalGatewayId of the created gateway
     */
    public String addGateway(Gateway gateway) throws RegistryException {
        var entity = gatewayMapper.toEntity(gateway);
        // Ensure a new internal ID is generated if not provided
        if (entity.getAiravataInternalGatewayId() == null || entity.getAiravataInternalGatewayId().isEmpty()) {
            entity.setAiravataInternalGatewayId(UUID.randomUUID().toString());
        }
        var saved = gatewayRepository.save(entity);
        // Return the gatewayId (human-readable) for backward compatibility
        return saved.getGatewayId();
    }

    /**
     * Update an existing gateway by its gateway ID.
     *
     * @param gatewayId the gateway ID
     * @param gateway the updated Gateway model
     */
    public void updateGateway(String gatewayId, Gateway gateway) throws RegistryException {
        var existingEntity = gatewayRepository.findByGatewayId(gatewayId).orElse(null);
        if (existingEntity == null) {
            throw new RegistryException("Gateway not found with ID: " + gatewayId);
        }
        var entity = gatewayMapper.toEntity(gateway);
        // Preserve the internal ID and gateway ID
        entity.setAiravataInternalGatewayId(existingEntity.getAiravataInternalGatewayId());
        entity.setGatewayId(gatewayId);
        gatewayRepository.save(entity);
    }

    /**
     * Update an existing gateway by its internal Airavata ID.
     *
     * @param airavataInternalGatewayId the internal gateway ID (UUID)
     * @param gateway the updated Gateway model
     * @return the updated Gateway model or null if not found
     */
    public Gateway updateGatewayByInternalId(String airavataInternalGatewayId, Gateway gateway)
            throws RegistryException {
        var existingEntity = gatewayRepository.findByAiravataInternalGatewayId(airavataInternalGatewayId).orElse(null);
        if (existingEntity == null) {
            return null;
        }
        var entity = gatewayMapper.toEntity(gateway);
        entity.setAiravataInternalGatewayId(airavataInternalGatewayId);
        var saved = gatewayRepository.save(entity);
        return gatewayMapper.toModel(saved);
    }

    /**
     * Find potential duplicate gateways based on gatewayId, name, or URL.
     *
     * @param statuses list of approval statuses to check
     * @param gatewayId the gateway ID to check
     * @param gatewayName the gateway name to check
     * @param gatewayUrl the gateway URL to check
     * @return the first duplicate gateway found, or null if none
     */
    public Gateway findDuplicateGateway(
            List<GatewayApprovalStatus> statuses, String gatewayId, String gatewayName, String gatewayUrl)
            throws RegistryException {
        var entities = gatewayRepository.findDuplicateGateways(statuses, gatewayId, gatewayName, gatewayUrl);
        if (entities.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entities.get(0));
    }

    /**
     * Save a gateway entity directly (for use when the entity is already mapped).
     *
     * @param entity the GatewayEntity to save
     * @return the saved GatewayEntity
     */
    @Transactional
    public GatewayEntity saveEntity(GatewayEntity entity) {
        return gatewayRepository.save(entity);
    }
}
