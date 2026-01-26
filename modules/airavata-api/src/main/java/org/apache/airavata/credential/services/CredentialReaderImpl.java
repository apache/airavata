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
package org.apache.airavata.credential.services;

import java.io.Serializable;
import java.util.List;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateAuditInfo;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CredentialReader;
import org.springframework.stereotype.Component;

/**
 * Credential store API implementation.
 */
@Component
public class CredentialReaderImpl implements CredentialReader, Serializable {
    private static final long serialVersionUID = 1L;

    private final CredentialEntityService credentialEntityService;

    public CredentialReaderImpl(CredentialEntityService credentialEntityService) {
        this.credentialEntityService = credentialEntityService;
    }

    @Override
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        return credentialEntityService.getCredential(gatewayId, tokenId);
    }

    @Override
    public List<Credential> getAllCredentials() throws CredentialStoreException {
        return credentialEntityService.getAllCredentials();
    }

    @Override
    public List<Credential> getAllCredentialsPerGateway(String gatewayId) throws CredentialStoreException {
        return credentialEntityService.getCredentials(gatewayId);
    }

    @Override
    public List<Credential> getAllAccessibleCredentialsPerGateway(String gatewayId, List<String> accessibleTokenIds)
            throws CredentialStoreException {
        return credentialEntityService.getCredentials(gatewayId, accessibleTokenIds);
    }

    @Override
    public List<Credential> getAllCredentialsPerUser(String userId) throws CredentialStoreException {
        // TODO: Implement when needed - would require a new repository method
        return null;
    }

    @Override
    public String getUserId(String gatewayName, String tokenId) throws CredentialStoreException {
        var credential = credentialEntityService.getCredential(gatewayName, tokenId);
        if (credential == null) {
            return null;
        }
        return credential.getUserId();
    }

    @Override
    public CertificateAuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException {
        var certificateCredential = (CertificateCredential) credentialEntityService.getCredential(gatewayName, tokenId);

        if (certificateCredential == null) {
            return null;
        }

        var certificateAuditInfo = new CertificateAuditInfo();
        certificateAuditInfo.setUserId(certificateCredential.getUserId());
        certificateAuditInfo.setCredentialLifeTime(certificateCredential.getLifeTime());
        certificateAuditInfo.setCredentialsRequestedTime(certificateCredential.getPersistedTime());
        certificateAuditInfo.setGatewayName(gatewayName);
        certificateAuditInfo.setNotAfter(certificateCredential.getNotAfter());
        certificateAuditInfo.setNotBefore(certificateCredential.getNotBefore());

        return certificateAuditInfo;
    }

    @Override
    public void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException {
        credentialEntityService.deleteCredential(gatewayName, tokenId);
    }

    @Override
    public String getGatewayID(String tokenId) throws CredentialStoreException {
        return credentialEntityService.getGatewayId(tokenId);
    }
}
