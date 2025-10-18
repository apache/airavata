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
package org.apache.airavata.credential.store.store.impl;

import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.repository.CommunityUserRepository;
import org.apache.airavata.credential.store.repository.CredentialsRepository;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.CredentialWriter;
import org.apache.airavata.credential.store.store.impl.db.CommunityUserEntity;
import org.apache.airavata.credential.store.store.impl.db.CredentialsEntity;
import org.apache.airavata.credential.store.utils.CredentialSerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes certificate credentials to database.
 */
public class CertificateCredentialWriter implements CredentialWriter {

    private final CredentialsRepository credentialsRepository;
    private final CommunityUserRepository communityUserRepository;

    protected static Logger log = LoggerFactory.getLogger(CertificateCredentialWriter.class);

    public CertificateCredentialWriter() {
        this.credentialsRepository = new CredentialsRepository();
        this.communityUserRepository = new CommunityUserRepository();
    }

    public void writeCredentials(Credential credential) throws CredentialStoreException {

        CertificateCredential certificateCredential = (CertificateCredential) credential;

        try {
            // Write community user
            writeCommunityUser(certificateCredential.getCommunityUser(), credential.getToken());

            // First delete existing credentials
            credentialsRepository.delete(new CredentialsEntity.CredentialsPK(
                    certificateCredential.getCommunityUser().getGatewayName(), certificateCredential.getToken()));

            // Create new credentials entity
            CredentialsEntity credentialsEntity = new CredentialsEntity();
            credentialsEntity.setGatewayId(
                    certificateCredential.getCommunityUser().getGatewayName());
            credentialsEntity.setTokenId(certificateCredential.getToken());
            credentialsEntity.setPortalUserId(certificateCredential.getPortalUserName());
            credentialsEntity.setCredentialOwnerType(certificateCredential.getCredentialOwnerType());

            // Serialize and encrypt the credential
            byte[] serializedCredential =
                    CredentialSerializationUtils.serializeCredentialWithEncryption(certificateCredential);
            credentialsEntity.setCredential(serializedCredential);

            // Save the entity
            credentialsRepository.create(credentialsEntity);

        } catch (Exception e) {
            throw new CredentialStoreException("Error writing certificate credentials", e);
        }
    }

    public void writeCommunityUser(CommunityUser communityUser, String token) throws CredentialStoreException {

        // First delete existing community user
        communityUserRepository.deleteByTokenId(token);

        // Create new community user entity
        CommunityUserEntity communityUserEntity = new CommunityUserEntity();
        communityUserEntity.setGatewayId(communityUser.getGatewayName());
        communityUserEntity.setCommunityUserName(communityUser.getUserName());
        communityUserEntity.setCommunityUserEmail(communityUser.getUserEmail());
        communityUserEntity.setTokenId(token);

        // Persist new community user
        communityUserRepository.create(communityUserEntity);
    }
}
