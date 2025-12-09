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
import java.util.Map;
import org.apache.airavata.credential.cpi.credential_store_cpiConstants;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.service.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CredentialServiceHandler implements org.apache.airavata.credential.cpi.CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialServiceHandler.class);

    private final CredentialStoreService credentialStoreService;

    public CredentialServiceHandler(CredentialStoreService credentialStoreService) {
        this.credentialStoreService = credentialStoreService;
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return credential_store_cpiConstants.CS_CPI_VERSION;
    }

    private CredentialStoreException wrapException(
            org.apache.airavata.credential.exceptions.CredentialStoreException e) {
        CredentialStoreException thriftException = new CredentialStoreException();
        thriftException.setMessage(e.getMessage());
        return thriftException;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException {
        try {
            return credentialStoreService.addSSHCredential(sshCredential);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        try {
            return credentialStoreService.addCertificateCredential(certificateCredential);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        try {
            return credentialStoreService.addPasswordCredential(passwordCredential);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.getSSHCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        try {
            return credentialStoreService.getCertificateCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.getPasswordCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws CredentialStoreException {
        try {
            return credentialStoreService.getAllCredentialSummaryForGateway(type, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId) throws CredentialStoreException {
        try {
            return credentialStoreService.getAllCredentialSummaryForUserInGateway(type, gatewayId, userId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.getAllPWDCredentialsForGateway(gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
        } catch (org.apache.airavata.credential.exceptions.CredentialStoreException e) {
            throw wrapException(e);
        }
    }
}
