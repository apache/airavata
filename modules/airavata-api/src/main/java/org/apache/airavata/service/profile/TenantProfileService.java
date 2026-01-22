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
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.model.CrudType;
import org.apache.airavata.common.model.EntityType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.orchestrator.internal.messaging.Dispatcher;
import org.apache.airavata.profile.entities.ProfileGatewayEntity;
import org.apache.airavata.profile.exception.TenantProfileServiceException;
import org.apache.airavata.profile.mappers.GatewayMapper;
import org.apache.airavata.profile.repositories.TenantProfileRepository;
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

    private final TenantProfileRepository tenantProfileRepository;
    private final CredentialStoreService credentialStoreService;
    private final GatewayMapper gatewayMapper;
    private final EntityManager entityManager;

    private final Dispatcher dbEventDispatcher;

    public TenantProfileService(
            TenantProfileRepository tenantProfileRepository,
            CredentialStoreService credentialStoreService,
            GatewayMapper gatewayMapper,
            EntityManager entityManager,
            Dispatcher dbEventDispatcher) {
        this.tenantProfileRepository = tenantProfileRepository;
        this.credentialStoreService = credentialStoreService;
        this.gatewayMapper = gatewayMapper;
        this.entityManager = entityManager;
        this.dbEventDispatcher = dbEventDispatcher;
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
                    // replicate tenant at end-places only if status is APPROVED
                    if (gateway.getGatewayApprovalStatus().equals(GatewayApprovalStatus.APPROVED)) {
                        logger.info(
                                "Gateway with ID: {}, is now APPROVED, replicating to subscribers.",
                                gateway.getGatewayId());
                        try {
                            dbEventDispatcher.dispatch(EntityType.TENANT, CrudType.CREATE, gateway);
                        } catch (AiravataException e) {
                            logger.error("Error dispatching gateway creation event", e);
                        }
                    }
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

            // if admin password token changes then copy the admin password and store under this gateway id and then
            // update the admin password token
            Gateway existingGateway;
            try {
                existingGateway = getGateway(updatedGateway.getAiravataInternalGatewayId());
            } catch (Exception e) {
                String message = "Error getting gateway: " + e.getMessage();
                logger.error(message, e);
                throw new TenantProfileServiceException(message, e);
            }
            if (updatedGateway.getIdentityServerPasswordToken() != null
                    && (existingGateway.getIdentityServerPasswordToken() == null
                            || !existingGateway
                                    .getIdentityServerPasswordToken()
                                    .equals(updatedGateway.getIdentityServerPasswordToken()))) {
                copyAdminPasswordToGateway(authzToken, updatedGateway);
            }

            if (updateGateway(updatedGateway) != null) {
                logger.debug("Updated gateway-profile with ID: " + updatedGateway.getGatewayId());
                // replicate tenant at end-places
                try {
                    dbEventDispatcher.dispatch(EntityType.TENANT, CrudType.UPDATE, updatedGateway);
                } catch (AiravataException e) {
                    logger.error("Error dispatching gateway update event", e);
                }
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
            if (deleteSuccess) {
                // delete tenant at end-places
                try {
                    Gateway gateway = new Gateway();
                    gateway.setGatewayId(gatewayId);
                    gateway.setGatewayApprovalStatus(GatewayApprovalStatus.DEACTIVATED);
                    dbEventDispatcher.dispatch(
                            EntityType.TENANT,
                            CrudType.DELETE,
                            // pass along gateway datamodel, with correct gatewayId;
                            // approvalstatus is not used for delete, hence set dummy value
                            gateway);
                } catch (AiravataException e) {
                    logger.error("Error dispatching gateway deletion event", e);
                }
            }
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
        Optional<ProfileGatewayEntity> entityOpt =
                tenantProfileRepository.findByAiravataInternalGatewayId(airavataInternalGatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entityOpt.get());
    }

    private Gateway getGatewayByGatewayId(String gatewayId) throws Exception {
        Optional<ProfileGatewayEntity> entityOpt = tenantProfileRepository.findByGatewayId(gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entityOpt.get());
    }

    private List<Gateway> getAllGateways() throws Exception {
        List<ProfileGatewayEntity> entities = tenantProfileRepository.findAllGateways();
        return gatewayMapper.toModelList(entities);
    }

    private List<Gateway> getAllGatewaysForUser(String requesterUsername) throws Exception {
        List<ProfileGatewayEntity> entities = tenantProfileRepository.findByRequesterUsername(requesterUsername);
        return gatewayMapper.toModelList(entities);
    }

    private Gateway getDuplicateGateway(String gatewayId, String gatewayName, String gatewayURL) throws Exception {
        List<String> statuses = Arrays.asList(
                GatewayApprovalStatus.APPROVED.name(),
                GatewayApprovalStatus.CREATED.name(),
                GatewayApprovalStatus.DEPLOYED.name());
        List<ProfileGatewayEntity> entities =
                tenantProfileRepository.findDuplicateGateways(statuses, gatewayId, gatewayName, gatewayURL);
        if (entities.isEmpty()) {
            return null;
        }
        return gatewayMapper.toModel(entities.get(0));
    }

    private Gateway createGateway(Gateway gateway) {
        ProfileGatewayEntity entity = gatewayMapper.toEntity(gateway);
        ProfileGatewayEntity persistedCopy = entityManager.merge(entity);
        entityManager.flush(); // Ensure entity is persisted and visible in same transaction
        return gatewayMapper.toModel(persistedCopy);
    }

    private Gateway updateGateway(Gateway gateway) {
        ProfileGatewayEntity entity = gatewayMapper.toEntity(gateway);
        ProfileGatewayEntity persistedCopy = entityManager.merge(entity);
        return gatewayMapper.toModel(persistedCopy);
    }

    private boolean deleteGateway(String airavataInternalGatewayId) {
        if (tenantProfileRepository.existsById(airavataInternalGatewayId)) {
            tenantProfileRepository.deleteById(airavataInternalGatewayId);
            return true;
        }
        return false;
    }
}
