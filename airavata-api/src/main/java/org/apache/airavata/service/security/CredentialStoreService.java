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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.CommunityUser;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.CredentialOwnerType;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.impl.store.CertificateCredentialWriter;
import org.apache.airavata.credential.impl.store.CredentialReaderImpl;
import org.apache.airavata.credential.impl.store.SSHCredentialWriter;
import org.apache.airavata.credential.utils.CredentialStoreDBInitConfig;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.apache.airavata.credential.utils.Utility;
import org.apache.airavata.model.credential.store.CertificateCredential;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "services.credentialStoreService.enabled",
        havingValue = "true",
        matchIfMissing = true)
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
        try {
            org.apache.airavata.credential.impl.ssh.SSHCredential credential =
                    new org.apache.airavata.credential.impl.ssh.SSHCredential();
            credential.setGateway(sshCredential.getGatewayId());
            credential.setPortalUserName(sshCredential.getUsername());
            // only username and gateway id will be sent by client.
            String token = TokenGenerator.generateToken(sshCredential.getGatewayId(), null);
            credential.setToken(token);
            credential.setPassphrase(String.valueOf(UUID.randomUUID()));
            if (sshCredential.getPrivateKey() != null) {
                credential.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            }
            if (sshCredential.getDescription() != null) {
                credential.setDescription(sshCredential.getDescription());
            }
            if (sshCredential.getPublicKey() != null) {
                credential.setPublicKey(sshCredential.getPublicKey().getBytes());
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
            credential.setCredentialOwnerType(CredentialOwnerType.GATEWAY);
            try {
                sshCredentialWriter.writeCredentials(credential);
            } catch (CredentialStoreException e) {
                String message = "Error occurred while saving SSH Credentials: " + e.getMessage();
                logger.error(message, e);
                throw new CredentialStoreException(message, e);
            }
            return token;
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws CredentialStoreException {
        try {
            org.apache.airavata.credential.impl.certificate.CertificateCredential credential =
                    new org.apache.airavata.credential.impl.certificate.CertificateCredential();
            credential.setPortalUserName(
                    certificateCredential.getCommunityUser().getUsername());
            credential.setCommunityUser(new CommunityUser(
                    certificateCredential.getCommunityUser().getGatewayName(),
                    certificateCredential.getCommunityUser().getUsername(),
                    certificateCredential.getCommunityUser().getUserEmail()));
            String token = TokenGenerator.generateToken(
                    certificateCredential.getCommunityUser().getGatewayName(), null);
            credential.setToken(token);
            Base64 encoder = new Base64(64);
            byte[] decoded = encoder.decode(certificateCredential
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
        try {
            org.apache.airavata.credential.impl.password.PasswordCredential credential =
                    new org.apache.airavata.credential.impl.password.PasswordCredential();
            credential.setGateway(passwordCredential.getGatewayId());
            credential.setPortalUserName(passwordCredential.getPortalUserName());
            credential.setUserName(passwordCredential.getLoginUserName());
            credential.setPassword(passwordCredential.getPassword());
            credential.setDescription(passwordCredential.getDescription());
            String token = TokenGenerator.generateToken(passwordCredential.getGatewayId(), null);
            credential.setToken(token);
            try {
                sshCredentialWriter.writeCredentials(credential);
            } catch (CredentialStoreException e) {
                String message = "Error occurred while saving PWD Credentials: " + e.getMessage();
                logger.error(message, e);
                throw new CredentialStoreException(message, e);
            }
            return token;
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            Credential credential;
            try {
                credential = credentialReader.getCredential(gatewayId, tokenId);
            } catch (CredentialStoreException e) {
                String msg = String.format(
                        "Error occurred while retrieving SSH credential for token - %s and gateway id - %s",
                        tokenId, gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            if (credential instanceof org.apache.airavata.credential.impl.ssh.SSHCredential
                    && !(credential instanceof org.apache.airavata.credential.impl.password.PasswordCredential)) {
                org.apache.airavata.credential.impl.ssh.SSHCredential credential1 =
                        (org.apache.airavata.credential.impl.ssh.SSHCredential) credential;
                SSHCredential sshCredential = new SSHCredential();
                sshCredential.setUsername(credential1.getPortalUserName());
                sshCredential.setGatewayId(credential1.getGateway());
                sshCredential.setPublicKey(new String(credential1.getPublicKey()));
                sshCredential.setPrivateKey(new String(credential1.getPrivateKey()));
                sshCredential.setPassphrase(credential1.getPassphrase());
                sshCredential.setToken(credential1.getToken());
                sshCredential.setPersistedTime(
                        credential1.getCertificateRequestedTime().getTime());
                sshCredential.setDescription(credential1.getDescription());
                return sshCredential;
            } else {
                logger.info("Could not find SSH credentials for token - " + tokenId + " and " + "gateway id - "
                        + gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            throw e;
        }
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
            if (isSSHCredential(credential)) {
                return convertToCredentialSummary((org.apache.airavata.credential.impl.ssh.SSHCredential) credential);
            } else if (isCertificateCredential(credential)) {
                return convertToCredentialSummary(
                        (org.apache.airavata.credential.impl.certificate.CertificateCredential) credential);
            } else if (isPasswordCredential(credential)) {
                return convertToCredentialSummary(
                        (org.apache.airavata.credential.impl.password.PasswordCredential) credential);
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
        try {
            List<Credential> credentials;
            try {
                credentials = credentialReader.getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
            } catch (CredentialStoreException e) {
                String msg = String.format("Error occurred while retrieving credentials for gateway - %s", gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            if (type.equals(SummaryType.SSH)) {
                return credentials.stream()
                        .filter(this::isSSHCredential)
                        .map(cred -> (org.apache.airavata.credential.impl.ssh.SSHCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.CERT)) {
                return credentials.stream()
                        .filter(this::isCertificateCredential)
                        .map(cred -> (org.apache.airavata.credential.impl.certificate.CertificateCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.PASSWD)) {
                return credentials.stream()
                        .filter(this::isPasswordCredential)
                        .map(cred -> (org.apache.airavata.credential.impl.password.PasswordCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else {
                String msg = "Summary Type " + type + " is not supported.";
                logger.error(msg);
                CredentialStoreException exception = new CredentialStoreException(msg);
                throw exception;
            }
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    private boolean isSSHCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.impl.ssh.SSHCredential
                && !(cred instanceof org.apache.airavata.credential.impl.password.PasswordCredential);
    }

    private boolean isCertificateCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.impl.certificate.CertificateCredential;
    }

    private boolean isPasswordCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.impl.password.PasswordCredential;
    }

    private CredentialSummary convertToCredentialSummary(org.apache.airavata.credential.impl.ssh.SSHCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.SSH);
        credentialSummary.setUsername(cred.getPortalUserName());
        credentialSummary.setGatewayId(cred.getGateway());
        credentialSummary.setPublicKey(new String(cred.getPublicKey()));
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(
            org.apache.airavata.credential.impl.certificate.CertificateCredential cred) {
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

    private CredentialSummary convertToCredentialSummary(
            org.apache.airavata.credential.impl.password.PasswordCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.PASSWD);
        credentialSummary.setUsername(cred.getPortalUserName());
        credentialSummary.setGatewayId(cred.getGateway());
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId)
            throws CredentialStoreException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.impl.certificate.CertificateCredential) {
                org.apache.airavata.credential.impl.certificate.CertificateCredential credential1 =
                        (org.apache.airavata.credential.impl.certificate.CertificateCredential) credential;
                CertificateCredential certificateCredential = new CertificateCredential();
                org.apache.airavata.model.credential.store.CommunityUser communityUser =
                        new org.apache.airavata.model.credential.store.CommunityUser();
                communityUser.setGatewayName(credential1.getCommunityUser().getGatewayName());
                communityUser.setUsername(credential1.getCommunityUser().getUserName());
                communityUser.setUserEmail(credential1.getCommunityUser().getUserEmail());
                certificateCredential.setCommunityUser(communityUser);
                certificateCredential.setToken(credential1.getToken());
                certificateCredential.setLifeTime(credential1.getLifeTime());
                certificateCredential.setNotAfter(credential1.getNotAfter());
                certificateCredential.setNotBefore(credential1.getNotBefore());
                certificateCredential.setPersistedTime(
                        credential1.getCertificateRequestedTime().getTime());
                if (credential1.getPrivateKey() != null) {
                    certificateCredential.setPrivateKey(
                            credential1.getPrivateKey().toString());
                }
                certificateCredential.setX509Cert(credential1.getCertificates()[0].toString());
                return certificateCredential;
            } else {
                logger.info("certificateCredential not found for token=%s and gateway_id=%s", tokenId, gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            Credential credential;
            try {
                credential = credentialReader.getCredential(gatewayId, tokenId);
            } catch (CredentialStoreException e) {
                String msg = String.format(
                        "Error occurred while retrieving PWD credential for token %s and gateway_id=%s",
                        tokenId, gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            if (credential instanceof org.apache.airavata.credential.impl.password.PasswordCredential) {
                org.apache.airavata.credential.impl.password.PasswordCredential credential1 =
                        (org.apache.airavata.credential.impl.password.PasswordCredential) credential;
                PasswordCredential pwdCredential = new PasswordCredential();
                pwdCredential.setGatewayId(credential1.getGateway());
                pwdCredential.setPortalUserName(credential1.getPortalUserName());
                pwdCredential.setLoginUserName(credential1.getUserName());
                pwdCredential.setPassword(credential1.getPassword());
                pwdCredential.setDescription(credential1.getDescription());
                pwdCredential.setToken(credential1.getToken());
                pwdCredential.setPersistedTime(
                        credential1.getCertificateRequestedTime().getTime());
                return pwdCredential;
            } else {
                logger.info("Could not find PWD credentials for token %s and gateway_id=%s", tokenId, gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            try {
                credentialReader.removeCredentials(gatewayId, tokenId);
            } catch (CredentialStoreException e) {
                String msg = String.format(
                        "Error deleting SSH credential for token=%s and gateway_id=%s", tokenId, gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            return true;
        } catch (CredentialStoreException e) {
            throw e;
        }
    }

    public boolean deletePWDCredential(String tokenId, String gatewayId) throws CredentialStoreException {
        try {
            try {
                credentialReader.removeCredentials(gatewayId, tokenId);
            } catch (CredentialStoreException e) {
                String msg = String.format(
                        "Error deleting PWD credential for token=%s and gateway_id=%s", tokenId, gatewayId);
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
            return true;
        } catch (CredentialStoreException e) {
            throw e;
        }
    }
}
