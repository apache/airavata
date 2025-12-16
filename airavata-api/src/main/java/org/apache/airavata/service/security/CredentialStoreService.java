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
package org.apache.airavata.service.security;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CommunityUser;
import org.apache.airavata.credential.model.CredentialSummary;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.model.SummaryType;
import org.apache.airavata.credential.services.CertificateCredentialWriter;
import org.apache.airavata.credential.services.CredentialReaderImpl;
import org.apache.airavata.credential.services.CredentialStoreDBInitConfig;
import org.apache.airavata.credential.services.SSHCredentialWriter;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.apache.airavata.credential.utils.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "services.credentialStoreService.enabled", havingValue = "true", matchIfMissing = true)
public class CredentialStoreService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialStoreService.class);

    private final AiravataServerProperties properties;
    private final SSHCredentialWriter sshCredentialWriter;
    private final CertificateCredentialWriter certificateCredentialWriter;
    private final CredentialReaderImpl credentialReader;
    private final CredentialStoreDBInitConfig dbInitConfig;

    public CredentialStoreService(
            AiravataServerProperties properties,
            SSHCredentialWriter sshCredentialWriter,
            CertificateCredentialWriter certificateCredentialWriter,
            CredentialReaderImpl credentialReader,
            CredentialStoreDBInitConfig dbInitConfig) {
        this.properties = properties;
        this.sshCredentialWriter = sshCredentialWriter;
        this.certificateCredentialWriter = certificateCredentialWriter;
        this.credentialReader = credentialReader;
        this.dbInitConfig = dbInitConfig;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("[BEAN-INIT] CredentialStoreService.init() called");
        var db = properties.database.vault;
        String jdbcUrl = db.url;
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            jdbcUrl = properties.database.registry.url;
        }
        String userName = db.user;
        if (userName == null || userName.isEmpty()) {
            userName = properties.database.registry.user;
        }
        String password = db.password;
        if (password == null || password.isEmpty()) {
            password = properties.database.registry.password;
        }
        String driverName = db.driver;
        if (driverName == null || driverName.isEmpty()) {
            driverName = properties.database.registry.driver;
        }

        logger.debug("Starting credential store, connecting to database - " + jdbcUrl + " DB user - " + userName
                + " driver name - " + driverName);
        DBInitializer.initializeDB(dbInitConfig);
    }

    public String addSSHCredential(SSHCredential sshCredential) throws CredentialStoreException {
        SSHCredential credential = new SSHCredential();
        credential.setGatewayId(sshCredential.getGatewayId());
        credential.setPortalUserName(sshCredential.getUsername());
        // only username and gateway id will be sent by client.
        String token = TokenGenerator.generateToken(sshCredential.getGatewayId(), null);
        credential.setToken(token);
        credential.setPassphrase(String.valueOf(UUID.randomUUID()));
        if (sshCredential.getPrivateKey() != null) {
            credential.setPrivateKey(sshCredential.getPrivateKey());
        }
        if (sshCredential.getDescription() != null) {
            credential.setDescription(sshCredential.getDescription());
        }
        if (sshCredential.getPublicKey() != null) {
            credential.setPublicKey(sshCredential.getPublicKey());
        }
        if (sshCredential.getPublicKey() == null || sshCredential.getPrivateKey() == null) {
            try {
                credential = Utility.generateKeyPair(credential);
            } catch (Exception ex) {
                String message = "Error occurred while generating key pair: " + ex.getMessage();
                logger.error(message, ex);
                throw new CredentialStoreException(message, ex);
            }
        }
        sshCredentialWriter.writeCredentials(credential);
        return token;
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        try {
            CertificateCredential credential = new CertificateCredential();
            credential.setPortalUserName(
                    certificateCredential.getCommunityUser().getUsername());
            credential.setCommunityUser(certificateCredential.getCommunityUser());
            String token = TokenGenerator.generateToken(
                    certificateCredential.getCommunityUser().getGatewayName(), null);
            credential.setToken(token);
            Base64.Decoder decoder = Base64.getMimeDecoder();
            byte[] decoded = decoder.decode(certificateCredential
                    .getX509Cert()
                    .replaceAll("-----BEGIN CERTIFICATE-----", "")
                    .replaceAll("-----END CERTIFICATE-----", ""));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(decoded));
            X509Certificate[] certificates = new X509Certificate[1];
            certificates[0] = certificate;
            credential.setCertificates(certificates);
            try {
                certificateCredentialWriter.writeCredentials(credential);
            } catch (CredentialStoreException e) {
                String message = "Error occurred while saving Certificate Credentials: " + e.getMessage();
                logger.error(message, e);
                throw new CredentialStoreException(message, e);
            }
            return token;
        } catch (CredentialStoreException e) {
            throw e;
        } catch (CertificateException e) {
            String message = "Error occurred while processing certificate: " + e.getMessage();
            logger.error(message, e);
            throw new CredentialStoreException(message, e);
        }
    }

    public String addPasswordCredential(PasswordCredential passwordCredential) throws CredentialStoreException {
        PasswordCredential credential = new PasswordCredential();
        credential.setGatewayId(passwordCredential.getGatewayId());
        credential.setPortalUserName(passwordCredential.getPortalUserName());
        credential.setLoginUserName(passwordCredential.getLoginUserName());
        credential.setPassword(passwordCredential.getPassword());
        credential.setDescription(passwordCredential.getDescription());
        String token = TokenGenerator.generateToken(passwordCredential.getGatewayId(), null);
        credential.setToken(token);
        sshCredentialWriter.writeCredentials(credential);
        return token;
    }

    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        Credential credential = credentialReader.getCredential(gatewayId, tokenId);
        if (credential instanceof SSHCredential c) return c;
        var msg =
                String.format("Credential for token=%s and gateway_id=%s is not an SSH credential", tokenId, gatewayId);
        throw new CredentialStoreException(msg);
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            Credential credential;
            try {
                credential = credentialReader.getCredential(gatewayId, tokenId);
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
                return convertToCredentialSummary(certCred);
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
            credentials = credentialReader.getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
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
                        .map(this::convertToCredentialSummary)
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
        credentialSummary.setUsername(cred.getPortalUserName());
        credentialSummary.setGatewayId(cred.getGatewayId());
        credentialSummary.setPublicKey(new String(cred.getPublicKey()));
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(CertificateCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.CERT);
        credentialSummary.setUsername(cred.getPortalUserName());
        // FIXME: need to get gatewayId for CertificateCredentials
        credentialSummary.setGatewayId("");
        // FIXME: get the public key? Or what would be appropriate for a summary of a
        // CertificateCredential?
        // credentialSummary.setPublicKey(new String(cred.getPublicKey()));
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(PasswordCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.PASSWD);
        credentialSummary.setUsername(cred.getPortalUserName());
        credentialSummary.setGatewayId(cred.getGatewayId());
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        Credential credential = credentialReader.getCredential(gatewayId, tokenId);
        if (credential instanceof CertificateCredential cc) {
            var cred = new CertificateCredential();
            var user = new CommunityUser();
            user.setGatewayName(cc.getCommunityUser().getGatewayName());
            user.setUsername(cc.getCommunityUser().getUsername());
            user.setUserEmail(cc.getCommunityUser().getUserEmail());
            cred.setCommunityUser(user);
            cred.setToken(cc.getToken());
            cred.setLifeTime(cc.getLifeTime());
            cred.setNotAfter(cc.getNotAfter());
            cred.setNotBefore(cc.getNotBefore());
            cred.setPersistedTime(cc.getCertificateRequestedTime().getTime());
            if (cc.getPrivateKey() != null) {
                cred.setPrivateKey(cc.getPrivateKey().toString());
            }
            cred.setX509Cert(cc.getCertificates()[0].toString());
            return cred;
        } else {
            var msg = String.format(
                    "Credential for token=%s and gateway_id=%s is not a CertificateCredential", tokenId, gatewayId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
    }

    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        Credential credential = credentialReader.getCredential(gatewayId, tokenId);
        if (credential instanceof PasswordCredential pc) {
            var cred = new PasswordCredential();
            cred.setGatewayId(pc.getGatewayId());
            cred.setPortalUserName(pc.getPortalUserName());
            cred.setLoginUserName(pc.getLoginUserName());
            cred.setPassword(pc.getPassword());
            cred.setDescription(pc.getDescription());
            cred.setToken(pc.getToken());
            cred.setPersistedTime(pc.getCertificateRequestedTime().getTime());
            return cred;
        } else {
            var msg = String.format(
                    "Credential for token=%s and gateway_id=%s is not a PasswordCredential", tokenId, gatewayId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        credentialReader.removeCredentials(gatewayId, tokenId);
        return true;
    }

    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        credentialReader.removeCredentials(gatewayId, tokenId);
        return true;
    }
}
