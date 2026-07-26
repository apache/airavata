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
package org.apache.airavata.security.service;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.airavata.security.CredentialEncryptionUtil;
import org.apache.airavata.security.SecurityUtil;
import org.apache.airavata.security.model.CredentialEntity;
import org.apache.airavata.security.model.CredentialPK;
import org.apache.airavata.security.repository.CredentialRepository;
import org.apache.airavata.common.TokenGenerator;
import org.apache.airavata.iam.service.CommunityUserService;
import org.apache.airavata.models.credential.store.CertificateCredential;
import org.apache.airavata.models.credential.store.CommunityUser;
import org.apache.airavata.models.credential.store.CredentialSummary;
import org.apache.airavata.models.credential.store.PasswordCredential;
import org.apache.airavata.models.credential.store.SSHCredential;
import org.apache.airavata.models.credential.store.StoredCredential;
import org.apache.airavata.models.credential.store.SummaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialStoreService {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreService.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CommunityUserService communityUserService;

    @Autowired
    private CredentialEncryptionUtil encryptionUtil;

    public String addSSHCredential(SSHCredential sshCredential) throws Exception {
        try {
            String token = TokenGenerator.generateToken(sshCredential.gatewayId(), null);
            SSHCredential.Builder builder = sshCredential.toBuilder().setToken(token)
                    .setPassphrase(String.valueOf(UUID.randomUUID()));

            SSHCredential credential = builder.build();
            if (sshCredential.publicKey().isEmpty()
                    || sshCredential.privateKey().isEmpty()) {
                credential = SecurityUtil.generateKeyPair(credential);
            }
            StoredCredential stored = new StoredCredential.Ssh(credential);
            saveCredential(credential.gatewayId(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving SSH Credentials.", e);
            throw new Exception("Error occurred while saving SSH Credentials.", e);
        }
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws Exception {
        try {
            String token = TokenGenerator.generateToken(
                    certificateCredential.communityUser().gatewayName(), null);
            CertificateCredential credential = certificateCredential.toBuilder().setToken(token).build();
            StoredCredential stored = new StoredCredential.Certificate(credential);

            // Save community user
            CommunityUser communityUser = credential.communityUser();
            saveCommunityUserToken(communityUser, token);

            // Save credential
            saveCredential(communityUser.gatewayName(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving Certificate Credentials.", e);
            throw new Exception("Error occurred while saving Certificate Credentials.", e);
        }
    }

    public String addPasswordCredential(PasswordCredential passwordCredential) throws Exception {
        try {
            String token = TokenGenerator.generateToken(passwordCredential.gatewayId(), null);
            PasswordCredential credential = passwordCredential.toBuilder().setToken(token).build();
            StoredCredential stored = new StoredCredential.Password(credential);
            saveCredential(credential.gatewayId(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving PWD Credentials.", e);
            throw new Exception("Error occurred while saving PWD Credentials.", e);
        }
    }

    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws Exception {
        try {
            StoredCredential stored = getCredential(gatewayId, tokenId);
            if (stored instanceof StoredCredential.Ssh ssh) {
                return ssh.sshCredential();
            }
            log.info("Could not find SSH credentials for token - {} and gateway id - {}", tokenId, gatewayId);
            return null;
        } catch (Exception e) {
            log.error(
                    "Error occurred while retrieving SSH credential for token - {} and gateway id - {}",
                    tokenId,
                    gatewayId,
                    e);
            throw new Exception("Error occurred while retrieving SSH credential for token - "
                    + tokenId + " and gateway id - " + gatewayId);
        }
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws Exception {
        try {
            StoredCredential stored = getCredential(gatewayId, tokenId);
            if (stored == null) {
                throw new Exception("No credential found for token: " + tokenId);
            }
            return convertToCredentialSummary(stored);
        } catch (Exception e) {
            final String msg = "Error occurred while retrieving credential summary for token - " + tokenId
                    + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws Exception {
        try {
            List<StoredCredential> credentials = getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
            return credentials.stream()
                    .filter(c -> matchesSummaryType(c, type))
                    .map(this::convertToCredentialSummary)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            final String msg = "Error occurred while retrieving " + type + " credential Summary for tokens - "
                    + accessibleTokenIds + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new Exception(msg);
        }
    }

    private boolean matchesSummaryType(StoredCredential stored, SummaryType type) {
        return switch (type) {
            case SSH -> stored instanceof StoredCredential.Ssh;
            case PASSWD -> stored instanceof StoredCredential.Password;
            case CERT -> stored instanceof StoredCredential.Certificate;
            default -> throw new RuntimeException("Summary Type " + type + " is not supported.");
        };
    }

    private CredentialSummary convertToCredentialSummary(StoredCredential stored) {
        if (stored instanceof StoredCredential.Ssh ssh) {
            var cred = ssh.sshCredential();
            CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                    .setType(SummaryType.SSH)
                    .setUsername(cred.username())
                    .setGatewayId(cred.gatewayId())
                    .setPublicKey(cred.publicKey())
                    .setToken(cred.token())
                    .setPersistedTime(cred.persistedTime());
            if (!cred.description().isEmpty()) {
                builder.setDescription(cred.description());
            }
            return builder.build();
        } else if (stored instanceof StoredCredential.Password password) {
            var cred = password.passwordCredential();
            CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                    .setType(SummaryType.PASSWD)
                    .setUsername(cred.portalUserName())
                    .setGatewayId(cred.gatewayId())
                    .setToken(cred.token())
                    .setPersistedTime(cred.persistedTime());
            if (!cred.description().isEmpty()) {
                builder.setDescription(cred.description());
            }
            return builder.build();
        } else if (stored instanceof StoredCredential.Certificate certificate) {
            var cred = certificate.certificateCredential();
            CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                    .setType(SummaryType.CERT)
                    .setUsername(cred.communityUser().username())
                    // FIXME: need to get gatewayId for CertificateCredentials
                    .setGatewayId("")
                    .setToken(cred.token())
                    .setPersistedTime(cred.persistedTime());
            return builder.build();
        }
        throw new RuntimeException("Unrecognized credential type: " + stored);
    }

    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws Exception {
        try {
            StoredCredential stored = getCredential(gatewayId, tokenId);
            if (stored instanceof StoredCredential.Certificate certificate) {
                return certificate.certificateCredential();
            }
            log.info("Could not find Certificate credentials for token - {} and gateway id - {}", tokenId, gatewayId);
            return null;
        } catch (Exception e) {
            log.error(
                    "Error occurred while retrieving Certificate credential for token - {} and gateway id - {}",
                    tokenId,
                    gatewayId,
                    e);
            throw new Exception("Error occurred while retrieving Certificate credential for token - "
                    + tokenId + " and gateway id - " + gatewayId);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws Exception {
        if (type.equals(SummaryType.SSH)) {
            return collectSshSummaries(gatewayId, ssh -> true);
        } else {
            log.info("Summary type {} not supported for gateway id - {}", type, gatewayId);
            return Collections.emptyList();
        }
    }

    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId) throws Exception {
        if (type.equals(SummaryType.SSH)) {
            return collectSshSummaries(gatewayId, ssh -> userId.equals(ssh.username()));
        } else {
            log.info("Summary type {} not supported for user id - {} and gateway id - {}", type, userId, gatewayId);
            return Collections.emptyList();
        }
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws Exception {
        return deleteCredential(tokenId, gatewayId, "SSH");
    }
    // --- Internal data access methods using Spring Data repos ---

    private void saveCredential(String gatewayId, StoredCredential stored) throws Exception {
        String token = CredentialEncryptionUtil.getToken(stored);
        byte[] data = encryptionUtil.convertCredentialToByteArray(stored);

        CredentialEntity entity = new CredentialEntity();
        entity.setGatewayId(gatewayId);
        entity.setTokenId(token);
        entity.setCredential(data);
        entity.setPortalUserId(CredentialEncryptionUtil.getPortalUserName(stored));
        entity.setTimePersisted(new Timestamp(System.currentTimeMillis()));
        entity.setDescription(CredentialEncryptionUtil.getDescription(stored));

        credentialRepository.save(entity);
    }

    private void saveCommunityUserToken(CommunityUser communityUser, String token) {
        communityUserService.saveCommunityUser(
                communityUser.gatewayName(), token, communityUser.username(), communityUser.userEmail());
    }

    private StoredCredential getCredential(String gatewayId, String tokenId) throws Exception {
        return credentialRepository
                .findById(new CredentialPK(gatewayId, tokenId))
                .map(this::toStoredCredential)
                .orElse(null);
    }

    private List<CredentialSummary> collectSshSummaries(String gatewayId, Predicate<SSHCredential> filter)
            throws Exception {
        List<CredentialSummary> summaryList = new ArrayList<>();
        try {
            List<StoredCredential> allCredentials = getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()) {
                for (StoredCredential stored : allCredentials) {
                    if (stored instanceof StoredCredential.Ssh ssh && filter.test(ssh.sshCredential())) {
                        summaryList.add(convertToCredentialSummary(stored));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving credential Summary", e);
            throw new Exception("Error occurred while retrieving credential Summary");
        }
        return summaryList;
    }

    private boolean deleteCredential(String tokenId, String gatewayId, String label) throws Exception {
        try {
            credentialRepository.deleteById(new CredentialPK(gatewayId, tokenId));
            return true;
        } catch (Exception e) {
            log.error(
                    "Error occurred while deleting {} credential for token - {} and gateway id - {}",
                    label,
                    tokenId,
                    gatewayId,
                    e);
            throw new Exception("Error occurred while deleting " + label + " credential for token - "
                    + tokenId + " and gateway id - " + gatewayId);
        }
    }

    private List<StoredCredential> getAllCredentialsPerGateway(String gatewayId) throws Exception {
        return credentialRepository.findByGatewayId(gatewayId).stream()
                .map(this::toStoredCredential)
                .collect(Collectors.toList());
    }

    private List<StoredCredential> getAllAccessibleCredentialsPerGateway(
            String gatewayId, List<String> accessibleTokenIds) throws Exception {
        if (accessibleTokenIds == null || accessibleTokenIds.isEmpty()) {
            return Collections.emptyList();
        }
        return credentialRepository.findByGatewayIdAndTokenIdIn(gatewayId, accessibleTokenIds).stream()
                .map(this::toStoredCredential)
                .collect(Collectors.toList());
    }

    private StoredCredential toStoredCredential(CredentialEntity entity) {
        try {
            StoredCredential stored = encryptionUtil.convertByteArrayToCredential(entity.getCredential());
            long persistedTime = entity.getTimePersisted() != null
                    ? entity.getTimePersisted().getTime()
                    : 0;
            return CredentialEncryptionUtil.overlayDbFields(
                    stored, entity.getPortalUserId(), persistedTime, entity.getDescription(), entity.getTokenId());
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing credential for token " + entity.getTokenId(), e);
        }
    }
}
