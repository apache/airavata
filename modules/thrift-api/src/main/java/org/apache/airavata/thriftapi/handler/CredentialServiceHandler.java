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
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CredentialServiceHandler implements org.apache.airavata.thriftapi.credential.model.CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialServiceHandler.class);

    private final CredentialStoreService credentialStoreService;

    public CredentialServiceHandler(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.credential.model.credential_store_cpiConstants.CS_CPI_VERSION;
    }

    private org.apache.airavata.thriftapi.credential.exception.CredentialStoreException wrapException(
            org.apache.airavata.credential.exception.CredentialStoreException e) {
        org.apache.airavata.thriftapi.credential.exception.CredentialStoreException thriftException =
                new org.apache.airavata.thriftapi.credential.exception.CredentialStoreException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    @Override
    public String addSSHCredential(org.apache.airavata.thriftapi.credential.model.SSHCredential sshCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            // Convert thrift to domain
            SSHCredential domainCredential = convertToDomainSSH(sshCredential);
            return credentialStoreService.addSSHCredential(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addCertificateCredential(
            org.apache.airavata.thriftapi.credential.model.CertificateCredential certificateCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            // Convert thrift to domain
            CertificateCredential domainCredential = convertToDomainCertificate(certificateCredential);
            return credentialStoreService.addCertificateCredential(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addPasswordCredential(
            org.apache.airavata.thriftapi.credential.model.PasswordCredential passwordCredential)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            // Convert thrift to domain
            PasswordCredential domainCredential = convertToDomainPassword(passwordCredential);
            return credentialStoreService.addPasswordCredential(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.SSHCredential getSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            SSHCredential domainCredential = credentialStoreService.getSSHCredential(tokenId, gatewayId);
            return convertToThriftSSH(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.CredentialSummary getCredentialSummary(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            CredentialSummary domainSummary = credentialStoreService.getCredentialSummary(tokenId, gatewayId);
            return convertToThriftSummary(domainSummary);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
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
            SummaryType domainType = SummaryType.valueOf(type.name());
            List<CredentialSummary> domainSummaries =
                    credentialStoreService.getAllCredentialSummaries(domainType, accessibleTokenIds, gatewayId);
            return domainSummaries.stream().map(this::convertToThriftSummary).collect(Collectors.toList());
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.CertificateCredential getCertificateCredential(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            CertificateCredential domainCredential = credentialStoreService.getCertificateCredential(tokenId, gatewayId);
            return convertToThriftCertificate(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public org.apache.airavata.thriftapi.credential.model.PasswordCredential getPasswordCredential(
            String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            PasswordCredential domainCredential = credentialStoreService.getPasswordCredential(tokenId, gatewayId);
            return convertToThriftPassword(domainCredential);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.thriftapi.credential.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    // Helper methods to convert between domain and thrift models
    private SSHCredential convertToDomainSSH(org.apache.airavata.thriftapi.credential.model.SSHCredential thrift) {
        SSHCredential domain = new SSHCredential();
        domain.setTokenId(thrift.getTokenId());
        domain.setPublicKey(thrift.getPublicKey());
        domain.setPrivateKey(thrift.getPrivateKey());
        domain.setPassphrase(thrift.getPassphrase());
        domain.setGatewayId(thrift.getGatewayId());
        domain.setUserName(thrift.getUserName());
        domain.setDescription(thrift.getDescription());
        domain.setPersistedTime(thrift.getPersistedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.credential.model.SSHCredential convertToThriftSSH(SSHCredential domain) {
        org.apache.airavata.thriftapi.credential.model.SSHCredential thrift =
                new org.apache.airavata.thriftapi.credential.model.SSHCredential();
        thrift.setTokenId(domain.getTokenId());
        thrift.setPublicKey(domain.getPublicKey());
        thrift.setPrivateKey(domain.getPrivateKey());
        thrift.setPassphrase(domain.getPassphrase());
        thrift.setGatewayId(domain.getGatewayId());
        thrift.setUserName(domain.getUserName());
        thrift.setDescription(domain.getDescription());
        thrift.setPersistedTime(domain.getPersistedTime());
        return thrift;
    }

    private CertificateCredential convertToDomainCertificate(
            org.apache.airavata.thriftapi.credential.model.CertificateCredential thrift) {
        CertificateCredential domain = new CertificateCredential();
        domain.setTokenId(thrift.getTokenId());
        domain.setCertificate(thrift.getCertificate());
        domain.setGatewayId(thrift.getGatewayId());
        domain.setUserName(thrift.getUserName());
        domain.setDescription(thrift.getDescription());
        domain.setPersistedTime(thrift.getPersistedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.credential.model.CertificateCredential convertToThriftCertificate(
            CertificateCredential domain) {
        org.apache.airavata.thriftapi.credential.model.CertificateCredential thrift =
                new org.apache.airavata.thriftapi.credential.model.CertificateCredential();
        thrift.setTokenId(domain.getTokenId());
        thrift.setCertificate(domain.getCertificate());
        thrift.setGatewayId(domain.getGatewayId());
        thrift.setUserName(domain.getUserName());
        thrift.setDescription(domain.getDescription());
        thrift.setPersistedTime(domain.getPersistedTime());
        return thrift;
    }

    private PasswordCredential convertToDomainPassword(
            org.apache.airavata.thriftapi.credential.model.PasswordCredential thrift) {
        PasswordCredential domain = new PasswordCredential();
        domain.setTokenId(thrift.getTokenId());
        domain.setLoginUserName(thrift.getLoginUserName());
        domain.setPassword(thrift.getPassword());
        domain.setGatewayId(thrift.getGatewayId());
        domain.setUserName(thrift.getUserName());
        domain.setDescription(thrift.getDescription());
        domain.setPersistedTime(thrift.getPersistedTime());
        return domain;
    }

    private org.apache.airavata.thriftapi.credential.model.PasswordCredential convertToThriftPassword(
            PasswordCredential domain) {
        org.apache.airavata.thriftapi.credential.model.PasswordCredential thrift =
                new org.apache.airavata.thriftapi.credential.model.PasswordCredential();
        thrift.setTokenId(domain.getTokenId());
        thrift.setLoginUserName(domain.getLoginUserName());
        thrift.setPassword(domain.getPassword());
        thrift.setGatewayId(domain.getGatewayId());
        thrift.setUserName(domain.getUserName());
        thrift.setDescription(domain.getDescription());
        thrift.setPersistedTime(domain.getPersistedTime());
        return thrift;
    }

    private org.apache.airavata.thriftapi.credential.model.CredentialSummary convertToThriftSummary(
            CredentialSummary domain) {
        org.apache.airavata.thriftapi.credential.model.CredentialSummary thrift =
                new org.apache.airavata.thriftapi.credential.model.CredentialSummary();
        thrift.setTokenId(domain.getTokenId());
        thrift.setGatewayId(domain.getGatewayId());
        thrift.setUserName(domain.getUserName());
        thrift.setDescription(domain.getDescription());
        thrift.setPersistedTime(domain.getPersistedTime());
        if (domain.getType() != null) {
            thrift.setType(org.apache.airavata.thriftapi.credential.model.SummaryType.valueOf(domain.getType().name()));
        }
        return thrift;
    }
}
