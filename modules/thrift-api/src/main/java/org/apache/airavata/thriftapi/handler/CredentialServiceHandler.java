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
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.thriftapi.mapper.CertificateCredentialMapper;
import org.apache.airavata.thriftapi.mapper.CredentialSummaryMapper;
import org.apache.airavata.thriftapi.mapper.PasswordCredentialMapper;
import org.apache.airavata.thriftapi.mapper.SSHCredentialMapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CredentialServiceHandler
        implements org.apache.airavata.thriftapi.credential.model.CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialServiceHandler.class);

    private final CredentialStoreService credentialStoreService;
    private final SSHCredentialMapper sshCredentialMapper = SSHCredentialMapper.INSTANCE;
    private final CertificateCredentialMapper certificateCredentialMapper = CertificateCredentialMapper.INSTANCE;
    private final PasswordCredentialMapper passwordCredentialMapper = PasswordCredentialMapper.INSTANCE;
    private final CredentialSummaryMapper credentialSummaryMapper = CredentialSummaryMapper.INSTANCE;

    public CredentialServiceHandler(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.credential.model.credential_store_cpiConstants.CS_CPI_VERSION;
    }

    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;
        if (e instanceof org.apache.airavata.credential.exception.CredentialStoreException) {
            var ex = new org.apache.airavata.thriftapi.credential.exception.CredentialStoreException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.CoreExceptions.AiravataSystemException) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        }
        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        }
        return thriftException;
    }

    @Override
    public String addSSHCredential(org.apache.airavata.thriftapi.credential.model.SSHCredential sshCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = sshCredentialMapper.toDomain(sshCredential);
            return credentialStoreService.addSSHCredential(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addCertificateCredential(
            org.apache.airavata.thriftapi.credential.model.CertificateCredential certificateCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = certificateCredentialMapper.toDomain(certificateCredential);
            return credentialStoreService.addCertificateCredential(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addPasswordCredential(
            org.apache.airavata.thriftapi.credential.model.PasswordCredential passwordCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = passwordCredentialMapper.toDomain(passwordCredential);
            return credentialStoreService.addPasswordCredential(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.SSHCredential getSSHCredential(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = credentialStoreService.getSSHCredential(tokenId, gatewayId);
            return sshCredentialMapper.toThrift(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.CredentialSummary getCredentialSummary(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainSummary = credentialStoreService.getCredentialSummary(tokenId, gatewayId);
            return credentialSummaryMapper.toThrift(domainSummary);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<org.apache.airavata.thriftapi.credential.model.CredentialSummary> getAllCredentialSummaries(
            org.apache.airavata.thriftapi.credential.model.SummaryType type,
            List<String> accessibleTokenIds,
            String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainType = org.apache.airavata.credential.model.SummaryType.valueOf(type.name());
            var domainSummaries =
                    credentialStoreService.getAllCredentialSummaries(domainType, accessibleTokenIds, gatewayId);
            return domainSummaries.stream()
                    .map(credentialSummaryMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.CertificateCredential getCertificateCredential(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = credentialStoreService.getCertificateCredential(tokenId, gatewayId);
            return certificateCredentialMapper.toThrift(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.PasswordCredential getPasswordCredential(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            var domainCredential = credentialStoreService.getPasswordCredential(tokenId, gatewayId);
            return passwordCredentialMapper.toThrift(domainCredential);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
