/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity;
import org.apache.airavata.service.profile.tenant.core.repositories.TenantProfileRepository;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Created by goshenoy on 3/6/17.
 */
public class TenantProfileServiceHandler implements TenantProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);

    private TenantProfileRepository tenantProfileRepository;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.TENANT);

    public TenantProfileServiceHandler() {
        logger.debug("Initializing TenantProfileServiceHandler");
        this.tenantProfileRepository = new TenantProfileRepository(Gateway.class, GatewayEntity.class);
    }

    @Override
    public String getAPIVersion() throws TException {
        return profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            // Assign UUID to gateway
            gateway.setAiravataInternalGatewayId(UUID.randomUUID().toString());
            if (!checkDuplicateGateway(gateway)) {
                // If admin password, copy it in the credential store under the requested gateway's gatewayId
                if (gateway.getIdentityServerPasswordToken() != null) {
                    copyAdminPasswordToGateway(authzToken, gateway);
                }
                gateway = tenantProfileRepository.create(gateway);
                if (gateway != null) {
                    logger.info("Added Airavata Gateway with Id: " + gateway.getGatewayId());
                    // replicate tenant at end-places only if status is APPROVED
                    if (gateway.getGatewayApprovalStatus().equals(GatewayApprovalStatus.APPROVED)) {
                        logger.info("Gateway with ID: {}, is now APPROVED, replicating to subscribers.", gateway.getGatewayId());
                        dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.CREATE, gateway);
                    }
                    // return internal id
                    return gateway.getAiravataInternalGatewayId();
                } else {
                    throw new Exception("Gateway object is null.");
                }
            }
            else {
                throw new TenantProfileServiceException("An approved Gateway already exists with the same GatewayId, Name or URL");
            }
        } catch (Exception ex) {
            logger.error("Error adding gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error adding gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, Gateway updatedGateway) throws TenantProfileServiceException, AuthorizationException, TException {
        try {

            // if admin password token changes then copy the admin password and store under this gateway id and then update the admin password token
            Gateway existingGateway = tenantProfileRepository.getGateway(updatedGateway.getAiravataInternalGatewayId());
            if (updatedGateway.getIdentityServerPasswordToken() != null
                    && (existingGateway.getIdentityServerPasswordToken() == null
                        || !existingGateway.getIdentityServerPasswordToken().equals(updatedGateway.getIdentityServerPasswordToken()))) {
                copyAdminPasswordToGateway(authzToken, updatedGateway);
            }

            if (tenantProfileRepository.update(updatedGateway) != null) {
                logger.debug("Updated gateway-profile with ID: " + updatedGateway.getGatewayId());
                // replicate tenant at end-places
                dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.UPDATE, updatedGateway);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            logger.error("Error updating gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error updating gateway-profile, reason: " + ex.getMessage());
            return false;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String airavataInternalGatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            Gateway gateway = tenantProfileRepository.getGateway(airavataInternalGatewayId);
            if (gateway == null) {
                throw new Exception("Could not find Gateway with internal ID: " + airavataInternalGatewayId);
            }
            return gateway;
        } catch (Exception ex) {
            logger.error("Error getting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            logger.debug("Deleting Airavata gateway-profile with ID: " + gatewayId + "Internal ID: " + airavataInternalGatewayId);
            boolean deleteSuccess = tenantProfileRepository.delete(airavataInternalGatewayId);
            if (deleteSuccess) {
                // delete tenant at end-places
                dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.DELETE,
                        // pass along gateway datamodel, with correct gatewayId;
                        // approvalstatus is not used for delete, hence set dummy value
                        new Gateway(gatewayId, GatewayApprovalStatus.DEACTIVATED));
            }
            return deleteSuccess;
        } catch (Exception ex) {
            logger.error("Error deleting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error deleting gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileRepository.getAllGateways();
        } catch (Exception ex) {
            logger.error("Error getting all gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting all gateway-profiles, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            Gateway gateway = tenantProfileRepository.getGateway(gatewayId);
            return (gateway != null);
        } catch (Exception ex) {
            logger.error("Error checking if gateway-profile exists, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error checking if gateway-profile exists, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileRepository.getAllGatewaysForUser(requesterUsername);
        } catch (Exception ex) {
            logger.error("Error getting user's gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting user's gateway-profiles, reason: " + ex.getMessage());
            throw exception;
        }
    }

    private boolean checkDuplicateGateway(Gateway gateway) throws TenantProfileServiceException {
        try {
            Gateway duplicateGateway = tenantProfileRepository.getDuplicateGateway(gateway.getGatewayId(), gateway.getGatewayName(), gateway.getGatewayURL());
            return duplicateGateway != null;
        } catch (Exception ex) {
            logger.error("Error checking if duplicate gateway-profile exists, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error checking if duplicate gateway-profiles exists, reason: " + ex.getMessage());
            throw exception;
        }
    }

    // admin passwords are stored in credential store in the super portal gateway and need to be
    // copied to a credential that is stored in the requested/newly created gateway
    private void copyAdminPasswordToGateway(AuthzToken authzToken, Gateway gateway) throws TException, ApplicationSettingsException {
        CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
        try {
            String requestGatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            PasswordCredential adminPasswordCredential = csClient.getPasswordCredential(gateway.getIdentityServerPasswordToken(), requestGatewayId);
            adminPasswordCredential.setGatewayId(gateway.getGatewayId());
            String newAdminPasswordCredentialToken = csClient.addPasswordCredential(adminPasswordCredential);
            gateway.setIdentityServerPasswordToken(newAdminPasswordCredentialToken);
        } finally {
            if (csClient.getInputProtocol().getTransport().isOpen()) {
                csClient.getInputProtocol().getTransport().close();
            }
            if (csClient.getOutputProtocol().getTransport().isOpen()) {
                csClient.getOutputProtocol().getTransport().close();
            }
        }
    }

    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }
}
