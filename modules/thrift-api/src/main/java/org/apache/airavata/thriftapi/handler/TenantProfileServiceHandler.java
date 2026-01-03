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
public class TenantProfileServiceHandler
        implements org.apache.airavata.thriftapi.profile.model.TenantProfileService.Iface {

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

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;

        if (e instanceof org.apache.airavata.profile.exception.TenantProfileServiceException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.credential.exception.CredentialStoreException) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException();
            ex.setMessage("Credential Store Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AuthorizationException) {
            var ex = new org.apache.airavata.thriftapi.exception.AuthorizationException();
            ex.setMessage(e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }

        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException();
            ex.setMessage("Internal Error: " + e.getMessage());
            ex.initCause(e);
            thriftException = ex;
        }
        return thriftException;
    }

    @Override
    @SecurityCheck
    public String addGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway gateway)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateway = gatewayMapper.toDomain(gateway);
            return tenantProfileService.addGateway(domainAuthzToken, domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateway = gatewayMapper.toDomain(updatedGateway);
            return tenantProfileService.updateGateway(domainAuthzToken, domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.Gateway getGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataInternalGatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateway = tenantProfileService.getGateway(domainAuthzToken, airavataInternalGatewayId);
            return gatewayMapper.toThrift(domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return tenantProfileService.deleteGateway(domainAuthzToken, airavataInternalGatewayId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Gateway> getAllGateways(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateways = tenantProfileService.getAllGateways(domainAuthzToken);
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return tenantProfileService.isGatewayExist(domainAuthzToken, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Gateway> getAllGatewaysForUser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String requesterUsername)
            throws org.apache.airavata.thriftapi.profile.exception.TenantProfileServiceException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGateways = tenantProfileService.getAllGatewaysForUser(domainAuthzToken, requesterUsername);
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
