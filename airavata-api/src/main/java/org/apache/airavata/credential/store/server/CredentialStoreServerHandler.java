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
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.model.credential.store.*;
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
    public String getAPIVersion() throws TException {
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
        try {
            return credentialStoreService.addCertificateCredential(certificateCredential);
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving Certificate Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while saving Certificate Credentials.");
        } catch (Throwable e) {
            log.error("Error occurred while saving Certificate Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while saving Certificate Credentials.");
        }
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.addPasswordCredential(passwordCredential);
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving PWD Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while saving PWD Credentials.");
        } catch (Throwable e) {
            log.error("Error occurred while saving PWD Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while saving PWD Credentials.");
        }
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getSSHCredential(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            log.error(
                    "Error occurred while retrieving SSH credentialfor token - " + tokenId + " and gateway id - "
                            + gatewayId,
                    e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving SSH credential for token - " + tokenId + " and gateway id - "
                            + gatewayId);
        }
    }

    @Override
    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getCredentialSummary(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            final String msg = "Error occurred while retrieving credential summary for token - " + tokenId
                    + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
        }
    }

    @Override
    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getAllCredentialSummaries(type, accessibleTokenIds, gatewayId);
        } catch (CredentialStoreException e) {
            final String msg = "Error occurred while retrieving " + type + " credential Summary for tokens - "
                    + accessibleTokenIds + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
        }
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getCertificateCredential(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            log.error(
                    "Error occurred while retrieving Certificate credential for token - " + tokenId
                            + " and gateway id - " + gatewayId,
                    e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving Certificate credential for token - " + tokenId
                            + " and gateway id - " + gatewayId);
        }
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getPasswordCredential(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            log.error(
                    "Error occurred while retrieving PWD credentialfor token - " + tokenId + " and gateway id - "
                            + gatewayId,
                    e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving PWD credential for token - " + tokenId + " and gateway id - "
                            + gatewayId);
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getAllCredentialSummaryForGateway(type, gatewayId);
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credential Summary", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving credential Summary");
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getAllCredentialSummaryForUserInGateway(type, gatewayId, userId);
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credential Summary", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving credential Summary");
        }
    }

    @Override
    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.getAllPWDCredentialsForGateway(gatewayId);
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credentials", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while retrieving credentials");
        }
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deleteSSHCredential(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            log.error(
                    "Error occurred while deleting SSH credential for token - " + tokenId + " and gateway id - "
                            + gatewayId,
                    e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while deleting SSH credential for token - " + tokenId + " and gateway id - "
                            + gatewayId);
        }
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            return credentialStoreService.deletePWDCredential(tokenId, gatewayId);
        } catch (CredentialStoreException e) {
            log.error(
                    "Error occurred while deleting PWD credential for token - " + tokenId + " and gateway id - "
                            + gatewayId,
                    e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(
                    "Error occurred while deleting PWD credential for token - " + tokenId + " and gateway id - "
                            + gatewayId);
        }
    }
}
