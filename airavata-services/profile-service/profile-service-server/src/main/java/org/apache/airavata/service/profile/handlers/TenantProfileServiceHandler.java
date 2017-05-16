/*
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
 *
*/
package org.apache.airavata.service.profile.handlers;

import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
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
import org.apache.airavata.service.profile.utils.ProfileServiceUtils;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by goshenoy on 3/6/17.
 */
public class TenantProfileServiceHandler implements TenantProfileService.Iface {

    private final static Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);

    private TenantProfileRepository tenantProfileRepository;

    public TenantProfileServiceHandler() {
        logger.debug("Initializing TenantProfileServiceHandler");
        this.tenantProfileRepository = new TenantProfileRepository(Gateway.class, GatewayEntity.class);
    }

    @Override
    @SecurityCheck
    public String getAPIVersion(AuthzToken authzToken) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
        } catch (Exception ex) {
            logger.error("Error getting API version, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting API version, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            gateway = tenantProfileRepository.create(gateway);
            if (gateway != null) {
                logger.info("Added Airavata Gateway with Id: " + gateway.getGatewayId());
                // replicate tenant at end-places only if status is APPROVED
                if (gateway.getGatewayApprovalStatus().equals(GatewayApprovalStatus.APPROVED)) {
                    logger.info("Gateway with ID: {}, is now APPROVED, replicating to subscribers.", gateway.getGatewayId());
                    ProfileServiceUtils.getDbEventPublisher().publish(
                            ProfileServiceUtils.getDBEventMessageContext(EntityType.TENANT, CrudType.CREATE, gateway),
                            DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                    );
                }
                // return gatewayId
                return gateway.getGatewayId();
            } else {
                throw new Exception("Gateway object is null.");
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
            if (tenantProfileRepository.update(updatedGateway) != null) {
                logger.debug("Updated gateway-profile with ID: " + updatedGateway.getGatewayId());
                // replicate tenant at end-places
                ProfileServiceUtils.getDbEventPublisher().publish(
                        ProfileServiceUtils.getDBEventMessageContext(EntityType.TENANT, CrudType.UPDATE, updatedGateway),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                );
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
    public Gateway getGateway(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            Gateway gateway = tenantProfileRepository.getGateway(gatewayId);
            if (gateway == null) {
                throw new Exception("Could not find Gateway with ID: " + gatewayId);
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
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId) throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            logger.debug("Deleting Airavata gateway-profile with ID: " + gatewayId);
            boolean deleteSuccess = tenantProfileRepository.delete(gatewayId);
            if (deleteSuccess) {
                // delete tenant at end-places
                ProfileServiceUtils.getDbEventPublisher().publish(
                        ProfileServiceUtils.getDBEventMessageContext(EntityType.TENANT, CrudType.DELETE,
                                // pass along gateway datamodel, with correct gatewayId;
                                // approvalstatus is not used for delete, hence set dummy value
                                new Gateway(
                                        gatewayId,
                                        GatewayApprovalStatus.DEACTIVATED
                                )
                        ),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString())
                );
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
            return gateway != null;
        } catch (Exception ex) {
            logger.error("Error checking if gateway-profile exists, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error checking if gateway-profile exists, reason: " + ex.getMessage());
            throw exception;
        }
    }
}
