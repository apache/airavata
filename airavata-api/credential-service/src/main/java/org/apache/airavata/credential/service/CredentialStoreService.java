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
package org.apache.airavata.credential.service;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.airavata.credential.model.CredentialEntity;
import org.apache.airavata.credential.model.CredentialPK;
import org.apache.airavata.credential.repository.CredentialRepository;
import org.apache.airavata.credential.repository.CredentialStoreException;
import org.apache.airavata.credential.util.CredentialEncryptionUtil;
import org.apache.airavata.credential.util.TokenGenerator;
import org.apache.airavata.credential.util.Utility;
import org.apache.airavata.interfaces.CommunityUserProvider;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.model.credential.store.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialStoreService implements CredentialProvider {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreService.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CommunityUserProvider communityUserProvider;

    @Autowired
    private CredentialEncryptionUtil encryptionUtil;

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException {
        try {
            String token = TokenGenerator.generateToken(sshCredential.getGatewayId(), null);
            SSHCredential.Builder builder =
                    sshCredential.toBuilder().setToken(token).setPassphrase(String.valueOf(UUID.randomUUID()));

            SSHCredential credential = builder.build();
            if (sshCredential.getPublicKey().isEmpty()
                    || sshCredential.getPrivateKey().isEmpty()) {
                credential = Utility.generateKeyPair(credential);
            }
            StoredCredential stored =
                    StoredCredential.newBuilder().setSshCredential(credential).build();
            saveCredential(credential.getGatewayId(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving SSH Credentials.", e);
            throw new CredentialStoreException("Error occurred while saving SSH Credentials.", e);
        }
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        try {
            String token = TokenGenerator.generateToken(
                    certificateCredential.getCommunityUser().getGatewayName(), null);
            CertificateCredential credential =
                    certificateCredential.toBuilder().setToken(token).build();
            StoredCredential stored = StoredCredential.newBuilder()
                    .setCertificateCredential(credential)
                    .build();

            // Save community user
            CommunityUser communityUser = credential.getCommunityUser();
            saveCommunityUser(communityUser, token);

            // Save credential
            saveCredential(communityUser.getGatewayName(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving Certificate Credentials.", e);
            throw new CredentialStoreException("Error occurred while saving Certificate Credentials.", e);
        }
    }

    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        try {
            String token = TokenGenerator.generateToken(passwordCredential.getGatewayId(), null);
            PasswordCredential credential =
                    passwordCredential.toBuilder().setToken(token).build();
            StoredCredential stored = StoredCredential.newBuilder()
                    .setPasswordCredential(credential)
                    .build();
            saveCredential(credential.getGatewayId(), stored);
            return token;
        } catch (Exception e) {
            log.error("Error occurred while saving PWD Credentials.", e);
            throw new CredentialStoreException("Error occurred while saving PWD Credentials.", e);
        }
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return getTypedCredential(
                tokenId,
                gatewayId,
                "SSH",
                StoredCredential.CredentialCase.SSH_CREDENTIAL,
                StoredCredential::getSshCredential);
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            StoredCredential stored = getCredential(gatewayId, tokenId);
            if (stored == null) {
                throw new CredentialStoreException("No credential found for token: " + tokenId);
            }
            return convertToCredentialSummary(stored);
        } catch (Exception e) {
            final String msg = "Error occurred while retrieving credential summary for token - " + tokenId
                    + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new CredentialStoreException(msg);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException {
        try {
            List<StoredCredential> credentials = getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
            StoredCredential.CredentialCase targetCase = summaryTypeToCredentialCase(type);
            return credentials.stream()
                    .filter(c -> c.getCredentialCase() == targetCase)
                    .map(this::convertToCredentialSummary)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            final String msg = "Error occurred while retrieving " + type + " credential Summary for tokens - "
                    + accessibleTokenIds + " and gateway id - " + gatewayId;
            log.error(msg, e);
            throw new CredentialStoreException(msg);
        }
    }

    private StoredCredential.CredentialCase summaryTypeToCredentialCase(SummaryType type) {
        switch (type) {
            case SSH:
                return StoredCredential.CredentialCase.SSH_CREDENTIAL;
            case PASSWD:
                return StoredCredential.CredentialCase.PASSWORD_CREDENTIAL;
            case CERT:
                return StoredCredential.CredentialCase.CERTIFICATE_CREDENTIAL;
            default:
                throw new RuntimeException("Summary Type " + type + " is not supported.");
        }
    }

    private CredentialSummary convertToCredentialSummary(StoredCredential stored) {
        switch (stored.getCredentialCase()) {
            case SSH_CREDENTIAL: {
                var cred = stored.getSshCredential();
                CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                        .setType(SummaryType.SSH)
                        .setUsername(cred.getUsername())
                        .setGatewayId(cred.getGatewayId())
                        .setPublicKey(cred.getPublicKey())
                        .setToken(cred.getToken())
                        .setPersistedTime(cred.getPersistedTime());
                if (!cred.getDescription().isEmpty()) {
                    builder.setDescription(cred.getDescription());
                }
                return builder.build();
            }
            case PASSWORD_CREDENTIAL: {
                var cred = stored.getPasswordCredential();
                CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                        .setType(SummaryType.PASSWD)
                        .setUsername(cred.getPortalUserName())
                        .setGatewayId(cred.getGatewayId())
                        .setToken(cred.getToken())
                        .setPersistedTime(cred.getPersistedTime());
                if (!cred.getDescription().isEmpty()) {
                    builder.setDescription(cred.getDescription());
                }
                return builder.build();
            }
            case CERTIFICATE_CREDENTIAL: {
                var cred = stored.getCertificateCredential();
                CredentialSummary.Builder builder = CredentialSummary.newBuilder()
                        .setType(SummaryType.CERT)
                        .setUsername(cred.getCommunityUser().getUsername())
                        // FIXME: need to get gatewayId for CertificateCredentials
                        .setGatewayId("")
                        .setToken(cred.getToken())
                        .setPersistedTime(cred.getPersistedTime());
                return builder.build();
            }
            default:
                throw new RuntimeException("Unrecognized credential type: " + stored.getCredentialCase());
        }
    }

    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        return getTypedCredential(
                tokenId,
                gatewayId,
                "Certificate",
                StoredCredential.CredentialCase.CERTIFICATE_CREDENTIAL,
                StoredCredential::getCertificateCredential);
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return getTypedCredential(
                tokenId,
                gatewayId,
                "PWD",
                StoredCredential.CredentialCase.PASSWORD_CREDENTIAL,
                StoredCredential::getPasswordCredential);
    }

    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws CredentialStoreException {
        if (type.equals(SummaryType.SSH)) {
            return collectSshSummaries(gatewayId, ssh -> true);
        } else {
            log.info("Summary type {} not supported for gateway id - {}", type, gatewayId);
            return Collections.emptyList();
        }
    }

    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId) throws CredentialStoreException {
        if (type.equals(SummaryType.SSH)) {
            return collectSshSummaries(gatewayId, ssh -> userId.equals(ssh.getUsername()));
        } else {
            log.info("Summary type {} not supported for user id - {} and gateway id - {}", type, userId, gatewayId);
            return Collections.emptyList();
        }
    }

    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId) throws CredentialStoreException {
        Map<String, String> pwdCredMap = new HashMap<>();
        try {
            List<StoredCredential> allCredentials = getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()) {
                for (StoredCredential stored : allCredentials) {
                    if (stored.getCredentialCase() == StoredCredential.CredentialCase.PASSWORD_CREDENTIAL) {
                        var pwdCred = stored.getPasswordCredential();
                        pwdCredMap.put(
                                pwdCred.getToken(), pwdCred.getDescription().isEmpty() ? "" : pwdCred.getDescription());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving credentials", e);
            throw new CredentialStoreException("Error occurred while retrieving credentials");
        }
        return pwdCredMap;
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return deleteCredential(tokenId, gatewayId, "SSH");
    }

    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        return deleteCredential(tokenId, gatewayId, "PWD");
    }

    // --- Internal data access methods using Spring Data repos ---

    private void saveCredential(String gatewayId, StoredCredential stored) throws CredentialStoreException {
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

    private void saveCommunityUser(CommunityUser communityUser, String token) {
        communityUserProvider.saveCommunityUser(
                communityUser.getGatewayName(), token, communityUser.getUsername(), communityUser.getUserEmail());
    }

    private StoredCredential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        return credentialRepository
                .findById(new CredentialPK(gatewayId, tokenId))
                .map(this::toStoredCredential)
                .orElse(null);
    }

    private <T> T getTypedCredential(
            String tokenId,
            String gatewayId,
            String label,
            StoredCredential.CredentialCase expectedCase,
            Function<StoredCredential, T> accessor)
            throws CredentialStoreException {
        try {
            StoredCredential stored = getCredential(gatewayId, tokenId);
            if (stored != null && stored.getCredentialCase() == expectedCase) {
                return accessor.apply(stored);
            } else {
                log.info("Could not find {} credentials for token - {} and gateway id - {}", label, tokenId, gatewayId);
                return null;
            }
        } catch (Exception e) {
            log.error(
                    "Error occurred while retrieving {} credential for token - {} and gateway id - {}",
                    label,
                    tokenId,
                    gatewayId,
                    e);
            throw new CredentialStoreException("Error occurred while retrieving " + label + " credential for token - "
                    + tokenId + " and gateway id - " + gatewayId);
        }
    }

    private List<CredentialSummary> collectSshSummaries(String gatewayId, Predicate<SSHCredential> filter)
            throws CredentialStoreException {
        List<CredentialSummary> summaryList = new ArrayList<>();
        try {
            List<StoredCredential> allCredentials = getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()) {
                for (StoredCredential stored : allCredentials) {
                    if (stored.getCredentialCase() == StoredCredential.CredentialCase.SSH_CREDENTIAL
                            && filter.test(stored.getSshCredential())) {
                        summaryList.add(convertToCredentialSummary(stored));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving credential Summary", e);
            throw new CredentialStoreException("Error occurred while retrieving credential Summary");
        }
        return summaryList;
    }

    private boolean deleteCredential(String tokenId, String gatewayId, String label) throws CredentialStoreException {
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
            throw new CredentialStoreException("Error occurred while deleting " + label + " credential for token - "
                    + tokenId + " and gateway id - " + gatewayId);
        }
    }

    private List<StoredCredential> getAllCredentialsPerGateway(String gatewayId) throws CredentialStoreException {
        return credentialRepository.findByGatewayId(gatewayId).stream()
                .map(this::toStoredCredential)
                .collect(Collectors.toList());
    }

    private List<StoredCredential> getAllAccessibleCredentialsPerGateway(
            String gatewayId, List<String> accessibleTokenIds) throws CredentialStoreException {
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
