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
package org.apache.airavata.credential.impl.store;

import java.io.Serializable;
import java.util.List;
import org.apache.airavata.credential.CommunityUser;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.impl.certificate.CertificateAuditInfo;
import org.apache.airavata.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.services.CredentialEntityService;
import org.apache.airavata.credential.utils.CredentialReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Credential store API implementation.
 */
@Component
public class CredentialReaderImpl implements CredentialReader, Serializable {

    @Autowired
    private CredentialEntityService credentialEntityService;

    public CredentialReaderImpl() {}

    @Override
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        return credentialEntityService.getCredential(gatewayId, tokenId);
    }

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
    public List<Credential> getAllCredentialsPerUser(String userName) throws CredentialStoreException {
        return null;
    }

    public String getPortalUser(String gatewayName, String tokenId) throws CredentialStoreException {
        Credential credential = credentialEntityService.getCredential(gatewayName, tokenId);
        if (credential == null) {
            return null;
        }
        return credential.getPortalUserName();
    }

    public CertificateAuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException {
        CertificateCredential certificateCredential =
                (CertificateCredential) credentialEntityService.getCredential(gatewayName, tokenId);

        if (certificateCredential == null) {
            return null;
        }

        CertificateAuditInfo certificateAuditInfo = new CertificateAuditInfo();

        CommunityUser retrievedUser = certificateCredential.getCommunityUser();
        certificateAuditInfo.setCommunityUserName(retrievedUser.getUserName());
        certificateAuditInfo.setCredentialLifeTime(certificateCredential.getLifeTime());
        certificateAuditInfo.setCredentialsRequestedTime(certificateCredential.getCertificateRequestedTime());
        certificateAuditInfo.setGatewayName(gatewayName);
        certificateAuditInfo.setNotAfter(certificateCredential.getNotAfter());
        certificateAuditInfo.setNotBefore(certificateCredential.getNotBefore());
        certificateAuditInfo.setPortalUserName(certificateCredential.getPortalUserName());

        return certificateAuditInfo;
    }

    public void updateCommunityUserEmail(String gatewayName, String communityUser, String email)
            throws CredentialStoreException {
        // TODO
    }

    public void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException {
        credentialEntityService.deleteCredential(gatewayName, tokenId);
    }

    @Override
    public String getGatewayID(String tokenId) throws CredentialStoreException {
        return credentialEntityService.getGatewayId(tokenId);
    }
}
