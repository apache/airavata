/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity;
import org.apache.airavata.service.profile.tenant.core.repositories.TenantProfileRepository;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementClient;
import org.apache.custos.resource.secret.service.AddResourceCredentialResponse;
import org.apache.custos.tenant.management.service.CreateTenantResponse;
import org.apache.custos.tenant.management.service.GetTenantResponse;
import org.apache.custos.tenant.manamgement.client.TenantManagementClient;
import org.apache.custos.tenant.profile.service.GetAllTenantsForUserResponse;
import org.apache.custos.tenant.profile.service.Tenant;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goshenoy on 3/6/17.
 */
public class TenantProfileServiceHandler implements TenantProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);

    private TenantProfileRepository tenantProfileRepository;
    private ResourceSecretManagementClient resourceSecretManagementClient;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.TENANT);
    private ThriftClientPool<RegistryService.Client> registryClientPool;

    private TenantManagementClient tenantManagementClient;

    public TenantProfileServiceHandler() throws ApplicationSettingsException, IOException {
        logger.debug("Initializing TenantProfileServiceHandler");
        this.tenantProfileRepository = new TenantProfileRepository(Gateway.class, GatewayEntity.class);
        tenantManagementClient = CustosUtils.getCustosClientProvider().getTenantManagementClient();
        resourceSecretManagementClient = CustosUtils.getCustosClientProvider().getResourceSecretManagementClient();
        registryClientPool = new ThriftClientPool<>(
                tProtocol -> new RegistryService.Client(tProtocol),
                this.<RegistryService.Client>createGenericObjectPoolConfig(),
                ServerSettings.getRegistryServerHost(),
                Integer.parseInt(ServerSettings.getRegistryServerPort()));

    }

    private <T> GenericObjectPoolConfig<T> createGenericObjectPoolConfig() {

        GenericObjectPoolConfig<T> poolConfig = new GenericObjectPoolConfig<T>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);
        return poolConfig;
    }

    @Override
    public String getAPIVersion() throws TException {
        return profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws TenantProfileServiceException, AuthorizationException, TException {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {

            // Assign UUID to gateway
            if (!registryClient.isGatewayExist(gateway.getGatewayId())) {
                // If admin password, copy it in the credential store under the requested gateway's gatewayId

                String adminPassword = getAdminPassword(authzToken, gateway);
                String[] contacts = {gateway.getEmailAddress()};
                String comment = "Airavata gateway internal id " + gateway.getGatewayId();
                CreateTenantResponse response = tenantManagementClient.registerTenant(gateway.getGatewayName(),
                        gateway.getRequesterUsername(),
                        gateway.getGatewayAdminFirstName(),
                        gateway.getGatewayAdminLastName(),
                        gateway.getEmailAddress(),
                        gateway.getIdentityServerUserName(),
                        adminPassword,
                        contacts,
                        (String[]) gateway.getRedirectURLs().toArray(),
                        gateway.getGatewayURL(),
                        gateway.getScope(),
                        gateway.getDomain(),
                        gateway.getGatewayURL(),
                        comment);

                copyAdminPasswordToGateway(authzToken, gateway, response.getClientId());

                if (response != null && response.getClientId() != null && !response.getClientId().trim().equals("")) {
                    logger.info("Added Airavata Gateway with Id: " + gateway.getGatewayId());
                    // replicate tenant at end-places only if status is APPROVED
                    if (gateway.getGatewayApprovalStatus().equals(GatewayApprovalStatus.APPROVED)) {
                        logger.info("Gateway with ID: {}, is now APPROVED, replicating to subscribers.", gateway.getGatewayId());
                        dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.CREATE, gateway);
                    }
                    // return internal id
                    registryClientPool.returnResource(registryClient);
                    String stringToBereturned =
                            gateway.getAiravataInternalGatewayId() + ":" + response.getClientId() + ":" + response.getClientSecret();
                    return stringToBereturned;
                } else {
                    registryClientPool.returnResource(registryClient);
                    throw new Exception("Gateway object is null.");
                }
            } else {
                registryClientPool.returnBrokenResource(registryClient);
                throw new TenantProfileServiceException("An approved Gateway already exists with the same GatewayId, Name or URL");
            }
        } catch (Exception ex) {
            logger.error("Error adding gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error adding gateway-profile, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;

        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, Gateway updatedGateway) throws TenantProfileServiceException, AuthorizationException, TException {
        try {

            // if admin password token changes then copy the admin password and store under this gateway id and then update the admin password token

            GetTenantResponse response = tenantManagementClient.getTenant(updatedGateway.getOauthClientId());

            if (response != null && response.getClientName() != null && !response.getClientName().trim().equals("")) {

                if (updatedGateway.getIdentityServerPasswordToken() != null && !updatedGateway.getIdentityServerPasswordToken().trim().equals("")) {
                    copyAdminPasswordToGateway(authzToken, updatedGateway, updatedGateway.getOauthClientId());
                }

                String[] contacts = {updatedGateway.getEmailAddress()};

                String comment = "Airavata gateway internal id " + updatedGateway.getGatewayId();

                String adminPassword = getAdminPassword(authzToken, updatedGateway);

                tenantManagementClient.updateTenant(updatedGateway.getOauthClientId(),
                        updatedGateway.getGatewayName(),
                        updatedGateway.getRequesterUsername(),
                        updatedGateway.getGatewayAdminFirstName(),
                        updatedGateway.getGatewayAdminLastName(),
                        updatedGateway.getEmailAddress(),
                        updatedGateway.getIdentityServerUserName(),
                        adminPassword,
                        contacts,
                        (String[]) updatedGateway.getRedirectURLs().toArray(),
                        updatedGateway.getGatewayURL(),
                        updatedGateway.getScope(),
                        updatedGateway.getDomain(),
                        updatedGateway.getGatewayURL(),
                        comment);

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
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            Gateway gateway = registryClient.getGateway(airavataInternalGatewayId);
            if (gateway == null) {
                throw new Exception("Could not find Gateway with internal ID: " + airavataInternalGatewayId);
            }
            return gateway;
        } catch (Exception ex) {
            logger.error("Error getting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting gateway-profile, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            logger.debug("Deleting Airavata gateway-profile with ID: " + gatewayId + "Internal ID: " + airavataInternalGatewayId);
            Gateway gateway = registryClient.getGateway(airavataInternalGatewayId);

            tenantManagementClient.deleteTenant(gateway.getOauthClientId());

            // delete tenant at end-places
            dbEventPublisherUtils.publish(EntityType.TENANT, CrudType.DELETE,
                    // pass along gateway datamodel, with correct gatewayId;
                    // approvalstatus is not used for delete, hence set dummy value
                    new Gateway(gatewayId, GatewayApprovalStatus.DEACTIVATED));

            return true;
        } catch (Exception ex) {
            logger.error("Error deleting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error deleting gateway-profile, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws TenantProfileServiceException, AuthorizationException, TException {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            return registryClient.getAllGateways();
        } catch (Exception ex) {
            logger.error("Error getting all gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting all gateway-profiles, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            return registryClient.isGatewayExist(gatewayId);
        } catch (Exception ex) {
            logger.error("Error checking if gateway-profile exists, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error checking if gateway-profile exists, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername) throws TenantProfileServiceException, AuthorizationException, TException {
        RegistryService.Client registryClient = registryClientPool.getResource();
        try {
            GetAllTenantsForUserResponse response = tenantManagementClient.getAllTenants(requesterUsername);

            List<Gateway> gateways = new ArrayList<>();

            for (Tenant t : response.getTenantList()) {
                gateways.add(registryClient.getGateway(t.getClientName()));
            }

            return gateways;
        } catch (Exception ex) {
            logger.error("Error getting user's gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting user's gateway-profiles, reason: " + ex.getMessage());
            registryClientPool.returnBrokenResource(registryClient);
            throw exception;
        }
    }


    // admin passwords are stored in credential store in the super portal gateway and need to be
    // copied to a credential that is stored in the requested/newly created gateway
    private void copyAdminPasswordToGateway(AuthzToken authzToken, Gateway gateway, String createdGatewayCustosId) throws TException, ApplicationSettingsException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            org.apache.custos.resource.secret.service.PasswordCredential passwordCredential =
                    resourceSecretManagementClient.getPasswordCredential(custosId, gateway.getIdentityServerPasswordToken());

            AddResourceCredentialResponse response = resourceSecretManagementClient.addPasswordCredential(createdGatewayCustosId,
                    passwordCredential.getMetadata().getDescription(),
                    gateway.getIdentityServerUserName(),
                    passwordCredential.getPassword());
            gateway.setIdentityServerPasswordToken(response.getToken());
        } catch (Exception ex) {
            logger.error("Unable to save admin password credential, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Unable to save admin password credential, reason: " + ex.getMessage());
            throw exception;
        }
    }

    private String getAdminPassword(AuthzToken authzToken, Gateway gateway) throws TException, ApplicationSettingsException {
        try {
            String custosId = authzToken.getClaimsMap().get(Constants.CUSTOS_ID);

            org.apache.custos.resource.secret.service.PasswordCredential passwordCredential =
                    resourceSecretManagementClient.getPasswordCredential(custosId, gateway.getIdentityServerPasswordToken());

            return passwordCredential.getPassword();
        } catch (Exception ex) {
            logger.error("Unable to fetch admin password credential, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Unable to fetch admin password credential, reason: " + ex.getMessage());
            throw exception;
        }
    }


}
