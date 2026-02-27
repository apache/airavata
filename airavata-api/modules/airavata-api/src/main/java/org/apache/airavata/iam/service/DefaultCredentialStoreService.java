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
package org.apache.airavata.iam.service;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.Credential;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.credential.service.CredentialEntityService;
import org.apache.airavata.credential.util.SSHKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultCredentialStoreService implements CredentialStoreService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultCredentialStoreService.class);

    private final CredentialEntityService credentialEntityService;
    private final java.util.Set<String> deletedTokens =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public DefaultCredentialStoreService(CredentialEntityService credentialEntityService) {
        this.credentialEntityService = credentialEntityService;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("[BEAN-INIT] CredentialStoreService.init() called");
    }

    public String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException {
        SSHCredential credential = new SSHCredential();
        credential.setGatewayId(sshCredential.getGatewayId());
        String ownerId = sshCredential.getUserId();
        if (ownerId != null && !ownerId.isEmpty()) {
            credential.setUserId(ownerId);
        }
        String token = UUID.randomUUID().toString();
        credential.setToken(token);
        credential.setPassphrase(String.valueOf(UUID.randomUUID()));
        if (sshCredential.getName() != null) {
            credential.setName(sshCredential.getName());
        }
        if (sshCredential.getDescription() != null) {
            credential.setDescription(sshCredential.getDescription());
        }
        if (sshCredential.getPrivateKey() != null) {
            credential.setPrivateKey(sshCredential.getPrivateKey());
        }
        if (sshCredential.getPublicKey() != null) {
            credential.setPublicKey(sshCredential.getPublicKey());
        }
        if (sshCredential.getPublicKey() == null || sshCredential.getPrivateKey() == null) {
            try {
                credential = SSHKeyGenerator.generateKeyPair(credential);
            } catch (Exception ex) {
                String message = "Error occurred while generating key pair: " + ex.getMessage();
                logger.error(message, ex);
                throw new CredentialStoreException(message, ex);
            }
        }
        credentialEntityService.saveCredential(credential.getGatewayId(), credential);
        return token;
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        CertificateCredential credential = new CertificateCredential();
        credential.setUserId(certificateCredential.getUserId());
        credential.setGatewayId(certificateCredential.getGatewayId());

        String token = UUID.randomUUID().toString();
        credential.setToken(token);
        credential.setDescription(certificateCredential.getDescription());
        credential.setX509Cert(certificateCredential.getX509Cert());
        credentialEntityService.saveCredential(
                credential.getGatewayId() != null ? credential.getGatewayId() : "gateway", credential);
        return token;
    }

    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        PasswordCredential credential = new PasswordCredential();
        credential.setGatewayId(passwordCredential.getGatewayId());
        credential.setUserId(passwordCredential.getUserId());
        credential.setPassword(passwordCredential.getPassword());
        if (passwordCredential.getName() != null) {
            credential.setName(passwordCredential.getName());
        }
        credential.setDescription(passwordCredential.getDescription());
        String token = UUID.randomUUID().toString();
        credential.setToken(token);
        credentialEntityService.saveCredential(passwordCredential.getGatewayId(), credential);
        return token;
    }

    public boolean credentialExists(String tokenId, String gatewayId) {
        return credentialEntityService.credentialExists(gatewayId, tokenId);
    }

    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        if (deletedTokens.contains(tokenId)) {
            return null;
        }
        Credential credential;
        try {
            credential = credentialEntityService.getCredential(gatewayId, tokenId);
        } catch (CredentialStoreException e) {
            return null;
        }
        if (credential instanceof SSHCredential c) return c;
        return null;
    }

    public java.util.List<CredentialSummary> getCredentialSummariesForUser(String gatewayId, String userId)
            throws CredentialStoreException {
        java.util.List<String> credentialIds =
                credentialEntityService.getCredentialIdsByGatewayIdAndUserId(gatewayId, userId);
        java.util.List<CredentialSummary> out = new java.util.ArrayList<>();
        for (String tokenId : credentialIds) {
            try {
                CredentialSummary s = getCredentialSummary(tokenId, gatewayId);
                if (s != null) out.add(s);
            } catch (Exception e) {
                // Skip if not accessible
            }
        }
        return out;
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            Credential credential;
            try {
                credential = credentialEntityService.getCredential(gatewayId, tokenId);
            } catch (CredentialStoreException e) {
                String msg = String.format(
                        "Error occurred while retrieving credential for token - %s and gateway id - %s",
                        tokenId, gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            if (credential instanceof SSHCredential sshCred && !(credential instanceof PasswordCredential)) {
                return convertToCredentialSummary(sshCred);
            } else if (credential instanceof CertificateCredential certCred) {
                return convertToCredentialSummary(certCred, gatewayId);
            } else if (credential instanceof PasswordCredential passCred) {
                return convertToCredentialSummary(passCred);
            }
            String msg = String.format("Unrecognized type of credential for token: %s", tokenId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        } catch (CredentialStoreException e) {
            String msg = String.format(
                    "Error occurred while retrieving credential for token - %s and gateway id - %s",
                    tokenId, gatewayId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws CredentialStoreException {
        List<Credential> credentials;
        try {
            credentials = credentialEntityService.getCredentials(gatewayId, accessibleTokenIds);
        } catch (CredentialStoreException e) {
            String msg = String.format("Error occurred while retrieving credentials for gateway - %s", gatewayId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
        return switch (type) {
            case SSH ->
                credentials.stream()
                        .filter(this::isSSHCredential)
                        .map(SSHCredential.class::cast)
                        .map(this::convertToCredentialSummary)
                        .toList();
            case CERT ->
                credentials.stream()
                        .filter(this::isCertificateCredential)
                        .map(CertificateCredential.class::cast)
                        .map(cred -> convertToCredentialSummary(cred, gatewayId))
                        .toList();
            case PASSWD ->
                credentials.stream()
                        .filter(this::isPasswordCredential)
                        .map(PasswordCredential.class::cast)
                        .map(this::convertToCredentialSummary)
                        .toList();
            default -> {
                var msg = String.format("Summary type=%s unsupported for gateway_id=%s", type, gatewayId);
                logger.error(msg);
                throw new CredentialStoreException(msg);
            }
        };
    }

    public List<CredentialSummary> getAllCredentialSummariesCombined(List<String> accessibleTokenIds, String gatewayId)
            throws CredentialStoreException {
        List<Credential> credentials;
        try {
            credentials = credentialEntityService.getCredentials(gatewayId, accessibleTokenIds);
        } catch (CredentialStoreException e) {
            String msg = String.format("Error occurred while retrieving credentials for gateway - %s", gatewayId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }

        List<CredentialSummary> summaries = new java.util.ArrayList<>();

        for (Credential credential : credentials) {
            try {
                if (isSSHCredential(credential)) {
                    summaries.add(convertToCredentialSummary((SSHCredential) credential));
                } else if (isPasswordCredential(credential)) {
                    summaries.add(convertToCredentialSummary((PasswordCredential) credential));
                } else if (isCertificateCredential(credential)) {
                    summaries.add(convertToCredentialSummary((CertificateCredential) credential, gatewayId));
                }
            } catch (Exception e) {
                logger.warn("Error converting credential to summary, skipping: {}", e.getMessage());
            }
        }

        return summaries;
    }

    private boolean isSSHCredential(Credential cred) {
        return cred instanceof SSHCredential && !(cred instanceof PasswordCredential);
    }

    private boolean isCertificateCredential(Credential cred) {
        return cred instanceof CertificateCredential;
    }

    private boolean isPasswordCredential(Credential cred) {
        return cred instanceof PasswordCredential;
    }

    private CredentialSummary convertToCredentialSummary(SSHCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.SSH);
        credentialSummary.setName(cred.getName() != null ? cred.getName() : cred.getDescription());
        credentialSummary.setUsername(null);
        credentialSummary.setGatewayId(cred.getGatewayId());
        credentialSummary.setPublicKey(new String(cred.getPublicKey()));
        credentialSummary.setToken(cred.getToken());
        long createdAt = cred.getCreatedAt();
        credentialSummary.setCreatedAt(createdAt > 0 ? createdAt : System.currentTimeMillis());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(CertificateCredential cred, String gatewayId) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.CERT);
        credentialSummary.setName(cred.getName() != null ? cred.getName() : cred.getDescription());
        credentialSummary.setUsername(null);
        credentialSummary.setGatewayId(gatewayId != null ? gatewayId : "");
        if (cred.getX509Cert() != null && !cred.getX509Cert().isEmpty()) {
            String certIdentifier = cred.getX509Cert().length() > 100
                    ? cred.getX509Cert().substring(0, 100) + "..."
                    : cred.getX509Cert();
            credentialSummary.setPublicKey(certIdentifier);
        }
        credentialSummary.setToken(cred.getToken());
        long createdAt = cred.getCreatedAt();
        credentialSummary.setCreatedAt(createdAt > 0 ? createdAt : System.currentTimeMillis());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(PasswordCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.PASSWD);
        credentialSummary.setName(cred.getName() != null ? cred.getName() : cred.getDescription());
        credentialSummary.setUsername(null);
        credentialSummary.setGatewayId(cred.getGatewayId());
        credentialSummary.setToken(cred.getToken());
        long createdAt = cred.getCreatedAt();
        credentialSummary.setCreatedAt(createdAt > 0 ? createdAt : System.currentTimeMillis());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        Credential credential = credentialEntityService.getCredential(gatewayId, tokenId);
        if (credential instanceof CertificateCredential cc) {
            var cred = new CertificateCredential();
            cred.setUserId(cc.getUserId());
            cred.setGatewayId(cc.getGatewayId());
            cred.setToken(cc.getToken());
            cred.setLifeTime(cc.getLifeTime());
            cred.setNotAfter(cc.getNotAfter());
            cred.setNotBefore(cc.getNotBefore());
            cred.setCreatedAt(cc.getCreatedAt());
            if (cc.getPrivateKey() != null) {
                cred.setPrivateKey(cc.getPrivateKey().toString());
            }
            if (cc.getCertificates() != null && cc.getCertificates().length > 0) {
                cred.setX509Cert(cc.getCertificates()[0].toString());
            }
            return cred;
        } else {
            var msg = String.format(
                    "Credential for token=%s and gateway_id=%s is not a CertificateCredential", tokenId, gatewayId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
    }

    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        Credential credential = credentialEntityService.getCredential(gatewayId, tokenId);
        if (credential instanceof PasswordCredential pc) {
            var cred = new PasswordCredential();
            cred.setGatewayId(pc.getGatewayId());
            cred.setUserId(pc.getUserId());
            cred.setLoginUserName(pc.getLoginUserName());
            cred.setPassword(pc.getPassword());
            cred.setDescription(pc.getDescription());
            cred.setToken(pc.getToken());
            cred.setCreatedAt(pc.getCreatedAt());
            return cred;
        } else {
            var msg = String.format(
                    "Credential for token=%s and gateway_id=%s is not a PasswordCredential", tokenId, gatewayId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        var cred = getSSHCredential(tokenId, gatewayId);
        if (cred == null) {
            throw new CredentialStoreException(
                    String.format("SSH credential not found for gateway=%s token=%s", gatewayId, tokenId));
        }
        credentialEntityService.deleteCredential(gatewayId, tokenId);
        deletedTokens.add(tokenId);
        return true;
    }

    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        credentialEntityService.deleteCredential(gatewayId, tokenId);
        return true;
    }
}
