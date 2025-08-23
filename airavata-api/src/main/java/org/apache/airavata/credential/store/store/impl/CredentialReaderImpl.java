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

import java.io.Serializable;
import java.util.List;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateAuditInfo;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.repository.CommunityUserRepository;
import org.apache.airavata.credential.store.repository.CredentialsRepository;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.db.CommunityUserEntity;
import org.apache.airavata.credential.store.store.impl.db.CredentialsEntity;
import org.apache.airavata.credential.store.utils.CredentialSerializationUtils;

/**
 * Credential store API implementation using JPA repositories.
 */
public class CredentialReaderImpl implements CredentialReader, Serializable {

    private CredentialsRepository credentialsRepository;
    private CommunityUserRepository communityUserRepository;

    public CredentialReaderImpl() throws ApplicationSettingsException {
        this.credentialsRepository = new CredentialsRepository();
        this.communityUserRepository = new CommunityUserRepository();
    }

    @Override
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        try {
            // Get from JPA repository
            CredentialsEntity credentialsEntity = credentialsRepository.get(
                new CredentialsEntity.CredentialsPK(gatewayId, tokenId));
            
            if (credentialsEntity != null) {
                // Deserialize the credential from byte array
                return CredentialSerializationUtils.deserializeCredentialWithDecryption(credentialsEntity.getCredential());
            }
            
            return null;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving credential for gateway: " + gatewayId + 
                ", token: " + tokenId, e);
        }
    }

    public List<Credential> getAllCredentials() throws CredentialStoreException {
        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.getAll();
            List<Credential> credentials = new java.util.ArrayList<>();
            
            for (CredentialsEntity entity : credentialsEntities) {
                Credential credential = CredentialSerializationUtils.deserializeCredentialWithDecryption(entity.getCredential());
                credentials.add(credential);
            }
            
            return credentials;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving all credentials", e);
        }
    }

    @Override
    public List<Credential> getAllCredentialsPerGateway(String gatewayId) throws CredentialStoreException {
        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByGatewayId(gatewayId);
            List<Credential> credentials = new java.util.ArrayList<>();
            
            for (CredentialsEntity entity : credentialsEntities) {
                Credential credential = CredentialSerializationUtils.deserializeCredentialWithDecryption(entity.getCredential());
                credentials.add(credential);
            }
            
            return credentials;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving credentials for gateway: " + gatewayId, e);
        }
    }

    @Override
    public List<Credential> getAllAccessibleCredentialsPerGateway(String gatewayId, List<String> accessibleTokenIds)
            throws CredentialStoreException {
        try {
            List<Credential> credentials = new java.util.ArrayList<>();
            
            for (String tokenId : accessibleTokenIds) {
                CredentialsEntity credentialsEntity = credentialsRepository.get(
                    new CredentialsEntity.CredentialsPK(gatewayId, tokenId));
                
                if (credentialsEntity != null) {
                    Credential credential = CredentialSerializationUtils.deserializeCredentialWithDecryption(credentialsEntity.getCredential());
                    credentials.add(credential);
                }
            }
            
            return credentials;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving accessible credentials for gateway: " + gatewayId, e);
        }
    }

    @Override
    public List<Credential> getAllCredentialsPerUser(String userName) throws CredentialStoreException {
        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByPortalUserId(userName);
            List<Credential> credentials = new java.util.ArrayList<>();
            
            for (CredentialsEntity entity : credentialsEntities) {
                Credential credential = CredentialSerializationUtils.deserializeCredentialWithDecryption(entity.getCredential());
                credentials.add(credential);
            }
            
            return credentials;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving credentials for user: " + userName, e);
        }
    }

    public String getPortalUser(String gatewayName, String tokenId) throws CredentialStoreException {
        try {
            CredentialsEntity credentialsEntity = credentialsRepository.get(
                new CredentialsEntity.CredentialsPK(gatewayName, tokenId));
            
            if (credentialsEntity != null) {
                return credentialsEntity.getPortalUserId();
            }
            
            return null;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving portal user for gateway: " + gatewayName + 
                ", token: " + tokenId, e);
        }
    }

    public CertificateAuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException {
        try {
            CredentialsEntity credentialsEntity = credentialsRepository.get(
                new CredentialsEntity.CredentialsPK(gatewayName, tokenId));
            
            if (credentialsEntity == null) {
                return null;
            }

            CertificateCredential certificateCredential = 
                (CertificateCredential) CredentialSerializationUtils.deserializeCredentialWithDecryption(credentialsEntity.getCredential());

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
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving audit info for gateway: " + gatewayName + 
                ", token: " + tokenId, e);
        }
    }

    public void updateCommunityUserEmail(String gatewayName, String communityUser, String email)
            throws CredentialStoreException {
        try {
            // Find community user entities by gateway and community user name
            List<CommunityUserEntity> communityUserEntities = 
                communityUserRepository.findByGatewayIdAndCommunityUserName(gatewayName, communityUser);
            
            for (CommunityUserEntity entity : communityUserEntities) {
                entity.setCommunityUserEmail(email);
                communityUserRepository.update(entity);
            }
        } catch (Exception e) {
            throw new CredentialStoreException("Error updating community user email for gateway: " + gatewayName + 
                ", user: " + communityUser, e);
        }
    }

    public void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException {
        try {
            credentialsRepository.delete(new CredentialsEntity.CredentialsPK(gatewayName, tokenId));
        } catch (Exception e) {
            throw new CredentialStoreException("Error removing credentials for gateway: " + gatewayName + 
                ", token: " + tokenId, e);
        }
    }

    @Override
    public String getGatewayID(String tokenId) throws CredentialStoreException {
        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByTokenId(tokenId);
            
            if (!credentialsEntities.isEmpty()) {
                return credentialsEntities.get(0).getGatewayId();
            }
            
            return null;
        } catch (Exception e) {
            throw new CredentialStoreException("Error retrieving gateway ID for token: " + tokenId, e);
        }
    }
}
