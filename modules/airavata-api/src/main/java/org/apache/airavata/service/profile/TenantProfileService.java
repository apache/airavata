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
package org.apache.airavata.service.profile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.airavata.common.exception.CoreExceptions.ApplicationSettingsException;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnApiService;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.profile.exception.TenantProfileServiceException;
import org.apache.airavata.registry.entities.GatewayEntity;
import org.apache.airavata.registry.mappers.GatewayMapper;
import org.apache.airavata.registry.repositories.GatewayRepository;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnApiService
public class TenantProfileService {
    private static final Logger logger = LoggerFactory.getLogger(TenantProfileService.class);

    private final GatewayRepository gatewayRepository;
    private final CredentialStoreService credentialStoreService;
    private final GatewayMapper gatewayMapper;
    private final EntityManager entityManager;

    public TenantProfileService(
            GatewayRepository gatewayRepository,
            CredentialStoreService credentialStoreService,
            GatewayMapper gatewayMapper,
            EntityManager entityManager) {
        this.gatewayRepository = gatewayRepository;
        this.credentialStoreService = credentialStoreService;
        this.gatewayMapper = gatewayMapper;
        this.entityManager = entityManager;
    }

    @Transactional
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws TenantProfileServiceException, CredentialStoreException {
        try {
            // Assign UUID to gateway
            gateway.setAiravataInternalGatewayId(UUID.randomUUID().toString());
            if (!checkDuplicateGateway(gateway)) {
                // If admin password, copy it in the credential store under the requested gateway's gatewayId
                if (gateway.getIdentityServerPasswordToken() != null) {
                    copyAdminPasswordToGateway(authzToken, gateway);
                }
                gateway = createGateway(gateway);
                if (gateway != null) {
                    logger.info("Added Airavata Gateway with Id: " + gateway.getGatewayId());
                    // return internal id
                    return gateway.getAiravataInternalGatewayId();
                } else {
                    throw new TenantProfileServiceException("Gateway object is null.");
                }
            } else {
                throw new TenantProfileServiceException(
                        "An approved Gateway already exists with the same GatewayId, Name or URL");
            }
        } catch (CredentialStoreException e) {
            throw e;
        } catch (ApplicationSettingsException e) {
            var message = "Error adding gateway-profile: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        } catch (PersistenceException e) {
            var message = String.format("Error adding gateway-profile: %s", e.getMessage());
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional
    public boolean updateGateway(AuthzToken authzToken, Gateway updatedGateway)
            throws TenantProfileServiceException, CredentialStoreException {
        try {
            // Verify gateway exists
            Gateway existingGateway;
            try {
                existingGateway = getGateway(updatedGateway.getAiravataInternalGatewayId());
            } catch (Exception e) {
                String message = "Error getting gateway: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
            if (existingGateway == null) {
                throw new TenantProfileServiceException(
                        "Gateway not found: " + updatedGateway.getAiravataInternalGatewayId());
            }

            // If a new admin password token is provided in the request, copy the admin password
            // to the target gateway's credential store. The identityServerPasswordToken is a
            // reference to a credential in the requesting gateway's credential store.
            // Note: This token is not persisted in the Gateway entity - it's only used during
            // the setup process to copy credentials.
            if (updatedGateway.getIdentityServerPasswordToken() != null) {
                copyAdminPasswordToGateway(authzToken, updatedGateway);
            }

            if (updateGateway(updatedGateway) != null) {
                logger.debug("Updated gateway-profile with ID: " + updatedGateway.getGatewayId());
                return true;
            } else {
                return false;
            }
        } catch (CredentialStoreException e) {
            throw e;
        } catch (ApplicationSettingsException e) {
            String message = "Error updating gateway-profile: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        } catch (PersistenceException e) {
            String message = "Error updating gateway-profile: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional(readOnly = true)
    public Gateway getGateway(AuthzToken authzToken, String airavataInternalGatewayId)
            throws TenantProfileServiceException {
        try {
            Gateway gateway;
            try {
                gateway = getGateway(airavataInternalGatewayId);
            } catch (Exception e) {
                String message = "Error getting gateway: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
            if (gateway == null) {
                throw new TenantProfileServiceException(
                        "Could not find Gateway with internal ID: " + airavataInternalGatewayId);
            }
            return gateway;
        } catch (PersistenceException e) {
            String message = "Error getting gateway-profile: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional
    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId)
            throws TenantProfileServiceException {
        try {
            logger.debug("Deleting Airavata gateway-profile with ID: " + gatewayId + "Internal ID: "
                    + airavataInternalGatewayId);
            boolean deleteSuccess = deleteGateway(airavataInternalGatewayId);
            return deleteSuccess;
        } catch (PersistenceException e) {
            String message = "Error deleting gateway-profile: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional(readOnly = true)
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws TenantProfileServiceException {
        try {
            try {
                return getAllGateways();
            } catch (Exception e) {
                String message = "Error getting all gateways: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
        } catch (PersistenceException e) {
            String message = "Error getting all gateway-profiles: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException {
        try {
            Gateway gateway;
            try {
                gateway = getGatewayByGatewayId(gatewayId);
            } catch (Exception e) {
                String message = "Error checking if gateway exists: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
            return (gateway != null);
        } catch (PersistenceException e) {
            String message = "Error checking if gateway-profile exists: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    @Transactional(readOnly = true)
    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername)
            throws TenantProfileServiceException {
        try {
            try {
                return getAllGatewaysForUser(requesterUsername);
            } catch (Exception e) {
                String message = "Error getting user's gateways: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
        } catch (PersistenceException e) {
            String message = "Error getting user's gateway-profiles: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    private boolean checkDuplicateGateway(Gateway gateway) throws TenantProfileServiceException {
        try {
            Gateway duplicateGateway;
            try {
                duplicateGateway =
                        getDuplicateGateway(gateway.getGatewayId(), gateway.getGatewayName(), gateway.getGatewayURL());
            } catch (Exception e) {
                String message = "Error checking duplicate gateway: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
            return duplicateGateway != null;
        } catch (PersistenceException e) {
            String message = "Error checking if duplicate gateway-profile exists: " + e.getMessage();
            logger.error(message, e);
            throw new TenantProfileServiceException(message, e);
        }
    }

    // admin passwords are stored in credential store in the super portal gateway and need to be
    // copied to a credential that is stored in the requested/newly created gateway
    private void copyAdminPasswordToGateway(AuthzToken authzToken, Gateway gateway)
            throws ApplicationSettingsException, TenantProfileServiceException, CredentialStoreException {
        String requestGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        PasswordCredential adminPasswordCredential = credentialStoreService.getPasswordCredential(
                gateway.getIdentityServerPasswordToken(), requestGatewayId);
        adminPasswordCredential.setGatewayId(gateway.getGatewayId());
        String newAdminPasswordCredentialToken = credentialStoreService.addPasswordCredential(adminPasswordCredential);
        gateway.setIdentityServerPasswordToken(newAdminPasswordCredentialToken);
    }

    // Helper methods that wrap repository calls and handle entity-to-model mapping
    private Gateway getGateway(String airavataInternalGatewayId) throws Exception {
        Optional<GatewayEntity> entityOpt =
                gatewayRepository.findByAiravataInternalGatewayId(airavataInternalGatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entityOpt.get());
    }

    private Gateway getGatewayByGatewayId(String gatewayId) throws Exception {
        Optional<GatewayEntity> entityOpt = gatewayRepository.findByGatewayId(gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entityOpt.get());
    }

    private List<Gateway> getAllGateways() throws Exception {
        List<GatewayEntity> entities = gatewayRepository.findAllGateways();
        return gatewayMapper.toModelList(entities);
    }

    private List<Gateway> getAllGatewaysForUser(String requesterUsername) throws Exception {
        List<GatewayEntity> entities = gatewayRepository.findByRequesterUsername(requesterUsername);
        return gatewayMapper.toModelList(entities);
    }

    private Gateway getDuplicateGateway(String gatewayId, String gatewayName, String gatewayURL) throws Exception {
        List<GatewayApprovalStatus> statuses = Arrays.asList(
                GatewayApprovalStatus.APPROVED,
                GatewayApprovalStatus.CREATED,
                GatewayApprovalStatus.DEPLOYED);
        List<GatewayEntity> entities =
                gatewayRepository.findDuplicateGateways(statuses, gatewayId, gatewayName, gatewayURL);
        if (entities.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entities.get(0));
    }

    private Gateway createGateway(Gateway gateway) {
        GatewayEntity entity = gatewayMapper.toEntity(gateway);
        GatewayEntity persistedCopy = entityManager.merge(entity);
        entityManager.flush(); // Ensure entity is persisted and visible in same transaction
        return gatewayMapper.toModel(persistedCopy);
    }

    private Gateway updateGateway(Gateway gateway) {
        GatewayEntity entity = gatewayMapper.toEntity(gateway);
        GatewayEntity persistedCopy = entityManager.merge(entity);
        return gatewayMapper.toModel(persistedCopy);
    }

    private boolean deleteGateway(String airavataInternalGatewayId) {
        if (gatewayRepository.existsById(airavataInternalGatewayId)) {
            gatewayRepository.deleteById(airavataInternalGatewayId);
            return true;
        }
        return false;
    }
}
