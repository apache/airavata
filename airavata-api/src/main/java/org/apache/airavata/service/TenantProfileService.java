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
package org.apache.airavata.service;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.profile.entities.GatewayEntity;
import org.apache.airavata.profile.repositories.TenantProfileRepository;
import org.apache.airavata.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantProfileService {
    private static final Logger logger = LoggerFactory.getLogger(TenantProfileService.class);

    private final TenantProfileRepository tenantProfileRepository;
    private final CredentialStoreService credentialStoreService;
    private final Mapper mapper;
    private final EntityManager entityManager;

    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.TENANT);

    public TenantProfileService(
            TenantProfileRepository tenantProfileRepository,
            CredentialStoreService credentialStoreService,
            Mapper mapper,
            @Qualifier("profileServiceEntityManager") EntityManager entityManager) {
        this.tenantProfileRepository = tenantProfileRepository;
        this.credentialStoreService = credentialStoreService;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

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
                            dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.CREATE, gateway);
                        } catch (AiravataException e) {
                            logger.error("Error publishing gateway creation event", e);
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
            String message = "Error adding gateway-profile: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        } catch (PersistenceException e) {
            String message = "Error adding gateway-profile: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

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
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
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
                    dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.UPDATE, updatedGateway);
                } catch (AiravataException e) {
                    logger.error("Error publishing gateway update event", e);
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
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        } catch (PersistenceException e) {
            String message = "Error updating gateway-profile: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public Gateway getGateway(AuthzToken authzToken, String airavataInternalGatewayId)
            throws TenantProfileServiceException {
        try {
            Gateway gateway;
            try {
                gateway = getGateway(airavataInternalGatewayId);
            } catch (Exception e) {
                String message = "Error getting gateway: " + e.getMessage();
                logger.error(message, e);
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
            }
            if (gateway == null) {
                throw new TenantProfileServiceException(
                        "Could not find Gateway with internal ID: " + airavataInternalGatewayId);
            }
            return gateway;
        } catch (PersistenceException e) {
            String message = "Error getting gateway-profile: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId)
            throws TenantProfileServiceException {
        try {
            logger.debug("Deleting Airavata gateway-profile with ID: " + gatewayId + "Internal ID: "
                    + airavataInternalGatewayId);
            boolean deleteSuccess = deleteGateway(airavataInternalGatewayId);
            if (deleteSuccess) {
                // delete tenant at end-places
                try {
                    dbEventPublisherUtils.publish(
                            EntityType.TENANT,
                            CrudType.DELETE,
                            // pass along gateway datamodel, with correct gatewayId;
                            // approvalstatus is not used for delete, hence set dummy value
                            new Gateway(gatewayId, GatewayApprovalStatus.DEACTIVATED));
                } catch (AiravataException e) {
                    logger.error("Error publishing gateway deletion event", e);
                }
            }
            return deleteSuccess;
        } catch (PersistenceException e) {
            String message = "Error deleting gateway-profile: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<Gateway> getAllGateways(AuthzToken authzToken) throws TenantProfileServiceException {
        try {
            try {
                return getAllGateways();
            } catch (Exception e) {
                String message = "Error getting all gateways: " + e.getMessage();
                logger.error(message, e);
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
            }
        } catch (PersistenceException e) {
            String message = "Error getting all gateway-profiles: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException {
        try {
            Gateway gateway;
            try {
                gateway = getGatewayByGatewayId(gatewayId);
            } catch (Exception e) {
                String message = "Error checking if gateway exists: " + e.getMessage();
                logger.error(message, e);
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
            }
            return (gateway != null);
        } catch (PersistenceException e) {
            String message = "Error checking if gateway-profile exists: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername)
            throws TenantProfileServiceException {
        try {
            try {
                return getAllGatewaysForUser(requesterUsername);
            } catch (Exception e) {
                String message = "Error getting user's gateways: " + e.getMessage();
                logger.error(message, e);
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
            }
        } catch (PersistenceException e) {
            String message = "Error getting user's gateway-profiles: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
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
                TenantProfileServiceException exception = new TenantProfileServiceException();
                exception.setMessage(message);
                exception.initCause(e);
                throw exception;
            }
            return duplicateGateway != null;
        } catch (PersistenceException e) {
            String message = "Error checking if duplicate gateway-profile exists: " + e.getMessage();
            logger.error(message, e);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage(message);
            exception.initCause(e);
            throw exception;
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
                tenantProfileRepository.findByAiravataInternalGatewayId(airavataInternalGatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return mapper.map(entityOpt.get(), Gateway.class);
    }

    private Gateway getGatewayByGatewayId(String gatewayId) throws Exception {
        Optional<GatewayEntity> entityOpt = tenantProfileRepository.findByGatewayId(gatewayId);
        if (entityOpt.isEmpty()) {
            return null;
        }
        return mapper.map(entityOpt.get(), Gateway.class);
    }

    private List<Gateway> getAllGateways() throws Exception {
        List<GatewayEntity> entities = tenantProfileRepository.findAllGateways();
        List<Gateway> result = new ArrayList<>();
        entities.forEach(entity -> result.add(mapper.map(entity, Gateway.class)));
        return result;
    }

    private List<Gateway> getAllGatewaysForUser(String requesterUsername) throws Exception {
        List<GatewayEntity> entities = tenantProfileRepository.findByRequesterUsername(requesterUsername);
        List<Gateway> result = new ArrayList<>();
        entities.forEach(entity -> result.add(mapper.map(entity, Gateway.class)));
        return result;
    }

    private Gateway getDuplicateGateway(String gatewayId, String gatewayName, String gatewayURL) throws Exception {
        List<String> statuses = Arrays.asList(
                GatewayApprovalStatus.APPROVED.name(),
                GatewayApprovalStatus.CREATED.name(),
                GatewayApprovalStatus.DEPLOYED.name());
        List<GatewayEntity> entities =
                tenantProfileRepository.findDuplicateGateways(statuses, gatewayId, gatewayName, gatewayURL);
        if (entities.isEmpty()) {
            return null;
        }
        return mapper.map(entities.get(0), Gateway.class);
    }

    @Transactional
    private Gateway createGateway(Gateway gateway) {
        GatewayEntity entity = mapper.map(gateway, GatewayEntity.class);
        GatewayEntity persistedCopy = entityManager.merge(entity);
        return mapper.map(persistedCopy, Gateway.class);
    }

    @Transactional
    private Gateway updateGateway(Gateway gateway) {
        GatewayEntity entity = mapper.map(gateway, GatewayEntity.class);
        GatewayEntity persistedCopy = entityManager.merge(entity);
        return mapper.map(persistedCopy, Gateway.class);
    }

    @Transactional
    private boolean deleteGateway(String airavataInternalGatewayId) {
        if (tenantProfileRepository.existsById(airavataInternalGatewayId)) {
            tenantProfileRepository.deleteById(airavataInternalGatewayId);
            return true;
        }
        return false;
    }
}
