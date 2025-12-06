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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.cpi.credential_store_cpiConstants;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.ServiceFactory;
import org.apache.airavata.service.ServiceFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialServiceHandler implements org.apache.airavata.credential.cpi.CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialServiceHandler.class);
    private CredentialStoreService credentialStoreService;

    public CredentialServiceHandler()
            throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException,
                    ServiceFactoryException {
        credentialStoreService = ServiceFactory.getInstance().getCredentialStoreService();
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return credential_store_cpiConstants.CS_CPI_VERSION;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException {
        return credentialStoreService.addSSHCredential(sshCredential);
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        return credentialStoreService.addCertificateCredential(certificateCredential);
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        return credentialStoreService.addPasswordCredential(passwordCredential);
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getSSHCredential(tokenId, gatewayId);
    }

    @Override
    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
    }

    @Override
    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        return credentialStoreService.getCertificateCredential(tokenId, gatewayId);
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getPasswordCredential(tokenId, gatewayId);
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws CredentialStoreException {
        return credentialStoreService.getAllCredentialSummaryForGateway(type, gatewayId);
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId) throws CredentialStoreException {
        return credentialStoreService.getAllCredentialSummaryForUserInGateway(type, gatewayId, userId);
    }

    @Override
    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId) throws CredentialStoreException {
        return credentialStoreService.getAllPWDCredentialsForGateway(gatewayId);
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
    }
}
