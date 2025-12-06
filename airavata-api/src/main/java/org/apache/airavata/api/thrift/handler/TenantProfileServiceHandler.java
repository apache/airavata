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
package org.apache.airavata.api.thrift.handler;

import java.util.List;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.profile.tenant.cpi.exception.TenantProfileServiceException;
import org.apache.airavata.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goshenoy on 3/6/17.
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantProfileServiceHandler implements TenantProfileService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);
    
    @Autowired
    private org.apache.airavata.service.TenantProfileService tenantProfileService;

    public TenantProfileServiceHandler() {
        logger.debug("Initializing TenantProfileServiceHandler");
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws TenantProfileServiceException, AuthorizationException {
        try {
            return tenantProfileService.addGateway(authzToken, gateway);
        } catch (CredentialStoreException e) {
            TenantProfileServiceException ex =
                    new TenantProfileServiceException("Error adding gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, Gateway updatedGateway)
            throws TenantProfileServiceException, AuthorizationException {
        try {
            return tenantProfileService.updateGateway(authzToken, updatedGateway);
        } catch (CredentialStoreException e) {
            TenantProfileServiceException ex =
                    new TenantProfileServiceException("Error updating gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String airavataInternalGatewayId)
            throws TenantProfileServiceException, AuthorizationException {
        return tenantProfileService.getGateway(authzToken, airavataInternalGatewayId);
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String airavataInternalGatewayId, String gatewayId)
            throws TenantProfileServiceException, AuthorizationException {
        return tenantProfileService.deleteGateway(authzToken, airavataInternalGatewayId, gatewayId);
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws TenantProfileServiceException, AuthorizationException {
        return tenantProfileService.getAllGateways(authzToken);
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws TenantProfileServiceException, AuthorizationException {
        return tenantProfileService.isGatewayExist(authzToken, gatewayId);
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGatewaysForUser(AuthzToken authzToken, String requesterUsername)
            throws TenantProfileServiceException, AuthorizationException {
        return tenantProfileService.getAllGatewaysForUser(authzToken, requesterUsername);
    }
}
