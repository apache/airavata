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
package org.apache.airavata.thriftapi.handler;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.profile.exception.TenantProfileServiceException;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.service.profile.TenantProfileService;
import org.apache.airavata.thriftapi.mapper.AuthzTokenMapper;
import org.apache.airavata.thriftapi.mapper.GatewayMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by goshenoy on 3/6/17.
 */
@Component
public class TenantProfileServiceHandler implements org.apache.airavata.thriftapi.profile.model.TenantProfileService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(TenantProfileServiceHandler.class);

    private final TenantProfileService tenantProfileService;
    private final AuthzTokenMapper authzTokenMapper = AuthzTokenMapper.INSTANCE;
    private final GatewayMapper gatewayMapper = GatewayMapper.INSTANCE;

    public TenantProfileServiceHandler(TenantProfileService tenantProfileService) {
        this.tenantProfileService = tenantProfileService;
        logger.debug("Initializing TenantProfileServiceHandler");
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.profile.model.profile_tenant_cpiConstants.TENANT_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway gateway)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(gateway);
            return tenantProfileService.addGateway(domainAuthzToken, domainGateway);
        } catch (CredentialStoreException e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error adding gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error adding gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway updatedGateway)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(updatedGateway);
            return tenantProfileService.updateGateway(domainAuthzToken, domainGateway);
        } catch (CredentialStoreException e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error updating gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error updating gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.Gateway getGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataInternalGatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Gateway domainGateway =
                    tenantProfileService.getGateway(domainAuthzToken, airavataInternalGatewayId);
            return gatewayMapper.toThrift(domainGateway);
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error getting gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataInternalGatewayId,
            String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return tenantProfileService.deleteGateway(domainAuthzToken, airavataInternalGatewayId, gatewayId);
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error deleting gateway: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Gateway> getAllGateways(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.Gateway> domainGateways =
                    tenantProfileService.getAllGateways(domainAuthzToken);
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error getting all gateways: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return tenantProfileService.isGatewayExist(domainAuthzToken, gatewayId);
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error checking if gateway exists: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Gateway> getAllGatewaysForUser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String requesterUsername)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.Gateway> domainGateways =
                    tenantProfileService.getAllGatewaysForUser(domainAuthzToken, requesterUsername);
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.profile.exception.TenantProfileServiceException e) {
            throw convertToThriftTenantProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException(
                            "Error getting gateways for user: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // Helper methods for exception conversion
    private org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException convertToThriftTenantProfileServiceException(
            org.apache.airavata.profile.exception.TenantProfileServiceException e) {
        org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException thriftException =
                new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.AuthorizationException convertToThriftAuthorizationException(
            org.apache.airavata.common.exception.AuthorizationException e) {
        org.apache.airavata.thriftapi.exception.AuthorizationException thriftException =
                new org.apache.airavata.thriftapi.exception.AuthorizationException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }
}
