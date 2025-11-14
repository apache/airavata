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
package org.apache.airavata.service.profile.handlers;

import java.util.List;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goshenoy on 3/6/17.
 */
public class TenantProfileServiceHandler implements TenantProfileService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);
    private org.apache.airavata.service.TenantProfileService tenantProfileService;

    public TenantProfileServiceHandler() {
        logger.debug("Initializing TenantProfileServiceHandler");
        tenantProfileService = new org.apache.airavata.service.TenantProfileService();
    }

    @Override
    public String getAPIVersion() throws TException {
        return profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.addGateway(authzToken, gateway);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error adding gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error adding gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, Gateway updatedGateway)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.updateGateway(authzToken, updatedGateway);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error updating gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error updating gateway-profile, reason: " + ex.getMessage());
            return false;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String airavataInternalGatewayId)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.getGateway(authzToken, airavataInternalGatewayId);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error getting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.deleteGateway(authzToken, airavataInternalGatewayId, gatewayId);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error deleting gateway-profile, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error deleting gateway-profile, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.getAllGateways(authzToken);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error getting all gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting all gateway-profiles, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.isGatewayExist(authzToken, gatewayId);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error checking if gateway-profile exists, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error checking if gateway-profile exists, reason: " + ex.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername)
            throws TenantProfileServiceException, AuthorizationException, TException {
        try {
            return tenantProfileService.getAllGatewaysForUser(authzToken, requesterUsername);
        } catch (TenantProfileServiceException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error getting user's gateway-profiles, reason: " + ex.getMessage(), ex);
            TenantProfileServiceException exception = new TenantProfileServiceException();
            exception.setMessage("Error getting user's gateway-profiles, reason: " + ex.getMessage());
            throw exception;
        }
    }
}
