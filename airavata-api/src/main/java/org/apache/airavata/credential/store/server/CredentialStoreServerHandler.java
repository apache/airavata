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
package org.apache.airavata.credential.store.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.cpi.credential_store_cpiConstants;
import org.apache.airavata.model.credential.store.*;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import sun.security.provider.X509Factory;

public class CredentialStoreServerHandler
        implements org.apache.airavata.credential.store.cpi.CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreServerHandler.class);
    private org.apache.airavata.service.CredentialStoreService credentialStoreService;

    public CredentialStoreServerHandler()
            throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException,
                    SQLException, IOException {
        credentialStoreService = new org.apache.airavata.service.CredentialStoreService();
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException {
        return credential_store_cpiConstants.CS_CPI_VERSION;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.addSSHCredential(sshCredential);
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.addCertificateCredential(certificateCredential);
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.addPasswordCredential(passwordCredential);
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getSSHCredential(tokenId, gatewayId);
    }

    @Override
    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
    }

    @Override
    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getCertificateCredential(tokenId, gatewayId);
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getPasswordCredential(tokenId, gatewayId);
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getAllCredentialSummaryForGateway(type, gatewayId);
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getAllCredentialSummaryForUserInGateway(type, gatewayId, userId);
    }

    @Override
    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.getAllPWDCredentialsForGateway(gatewayId);
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
    }
}
