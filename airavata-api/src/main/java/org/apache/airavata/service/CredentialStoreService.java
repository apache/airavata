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
package org.apache.airavata.service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.store.impl.util.CredentialStoreDBInitConfig;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.apache.airavata.credential.store.util.Utility;
import org.apache.airavata.model.credential.store.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CredentialStoreService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialStoreService.class);

    private DBUtil dbUtil;
    private SSHCredentialWriter sshCredentialWriter;
    private CertificateCredentialWriter certificateCredentialWriter;
    private CredentialReaderImpl credentialReader;

    public CredentialStoreService()
            throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException,
                    InstantiationException {
        String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
        String userName = ServerSettings.getCredentialStoreDBUser();
        String password = ServerSettings.getCredentialStoreDBPassword();
        String driverName = ServerSettings.getCredentialStoreDBDriver();

        logger.debug("Starting credential store, connecting to database - " + jdbcUrl + " DB user - " + userName
                + " driver name - " + driverName);
        DBInitializer.initializeDB(new CredentialStoreDBInitConfig());

        dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        sshCredentialWriter = new SSHCredentialWriter(dbUtil);
        certificateCredentialWriter = new CertificateCredentialWriter(dbUtil);
        credentialReader = new CredentialReaderImpl(dbUtil);
    }

    public String addSSHCredential(SSHCredential sshCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential =
                    new org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential();
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
                credential = Utility.generateKeyPair(credential);
            }
            credential.setCredentialOwnerType(CredentialOwnerType.GATEWAY);
            sshCredentialWriter.writeCredentials(credential);
            return token;
        } catch (Throwable e) {
            logger.error("Error occurred while saving SSH Credentials.", e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            "Error occurred while saving SSH Credentials.. More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public String addCertificateCredential(CertificateCredential certificateCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential credential =
                    new org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential();
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
            certificateCredentialWriter.writeCredentials(credential);
            return token;
        } catch (Throwable e) {
            logger.error("Error occurred while saving Certificate Credentials.", e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            "Error occurred while saving Certificate Credentials.. More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public String addPasswordCredential(PasswordCredential passwordCredential)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            org.apache.airavata.credential.store.credential.impl.password.PasswordCredential credential =
                    new org.apache.airavata.credential.store.credential.impl.password.PasswordCredential();
            credential.setGateway(passwordCredential.getGatewayId());
            credential.setPortalUserName(passwordCredential.getPortalUserName());
            credential.setUserName(passwordCredential.getLoginUserName());
            credential.setPassword(passwordCredential.getPassword());
            credential.setDescription(passwordCredential.getDescription());
            String token = TokenGenerator.generateToken(passwordCredential.getGatewayId(), null);
            credential.setToken(token);
            sshCredentialWriter.writeCredentials(credential);
            return token;
        } catch (Throwable e) {
            logger.error("Error occurred while saving PWD Credentials.", e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            "Error occurred while saving PWD Credentials.. More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public SSHCredential getSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                    && !(credential
                            instanceof
                            org.apache.airavata.credential.store.credential.impl.password.PasswordCredential)) {
                org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential1 =
                        (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
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
        } catch (Throwable e) {
            String msg = "Error occurred while retrieving SSH credential for token - " + tokenId + " and gateway id - "
                    + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(e);
            throw exception;
        }
    }

    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (isSSHCredential(credential)) {
                return convertToCredentialSummary(
                        (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential);
            } else if (isCertificateCredential(credential)) {
                return convertToCredentialSummary(
                        (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential)
                                credential);
            } else if (isPasswordCredential(credential)) {
                return convertToCredentialSummary(
                        (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) credential);
            }
            String msg = "Unrecognized type of credential for token: " + tokenId;
            logger.error(msg, new RuntimeException(msg));
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(new RuntimeException(msg));
            throw exception;
        } catch (org.apache.airavata.credential.store.exception.CredentialStoreException e) {
            throw e;
        } catch (Throwable e) {
            final String msg = "Error occurred while retrieving credential summary for token - " + tokenId
                    + " and gateway id - " + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            msg + ". More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    public List<CredentialSummary> getAllCredentialSummaries(
            SummaryType type, List<String> accessibleTokenIds, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            List<Credential> credentials =
                    credentialReader.getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
            if (type.equals(SummaryType.SSH)) {
                return credentials.stream()
                        .filter(this::isSSHCredential)
                        .map(cred -> (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.CERT)) {
                return credentials.stream()
                        .filter(this::isCertificateCredential)
                        .map(cred ->
                                (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential)
                                        cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.PASSWD)) {
                return credentials.stream()
                        .filter(this::isPasswordCredential)
                        .map(cred ->
                                (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else {
                String msg = "Summary Type " + type + " is not supported.";
                logger.error(msg, new RuntimeException(msg));
                org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                        new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
                exception.initCause(new RuntimeException(msg));
                throw exception;
            }
        } catch (org.apache.airavata.credential.store.exception.CredentialStoreException e) {
            throw e;
        } catch (Throwable e) {
            final String msg = "Error occurred while retrieving " + type + " credential Summary for tokens - "
                    + accessibleTokenIds + " and gateway id - " + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            msg + ". More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    private boolean isSSHCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                && !(cred instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential);
    }

    private boolean isCertificateCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
    }

    private boolean isPasswordCredential(Credential cred) {
        return cred instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential;
    }

    private CredentialSummary convertToCredentialSummary(
            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential cred) {
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
            org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.CERT);
        credentialSummary.setUsername(cred.getPortalUserName());
        // FIXME: need to get gatewayId for CertificateCredentials
        credentialSummary.setGatewayId("");
        // FIXME: get the public key? Or what would be appropriate for a summary of a CertificateCredential?
        //        credentialSummary.setPublicKey(new String(cred.getPublicKey()));
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    private CredentialSummary convertToCredentialSummary(
            org.apache.airavata.credential.store.credential.impl.password.PasswordCredential cred) {
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
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential
                    instanceof org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) {
                org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential credential1 =
                        (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential)
                                credential;
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
                logger.info("Could not find Certificate credentials for token - " + tokenId + " and " + "gateway id - "
                        + gatewayId);
                return null;
            }
        } catch (Throwable e) {
            String msg = "Error occurred while retrieving Certificate credential for token - " + tokenId
                    + " and gateway id - " + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(e);
            throw exception;
        }
    }

    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential
                    instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) {
                org.apache.airavata.credential.store.credential.impl.password.PasswordCredential credential1 =
                        (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) credential;
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
                logger.info("Could not find PWD credentials for token - " + tokenId + " and " + "gateway id - "
                        + gatewayId);
                return null;
            }
        } catch (Throwable e) {
            String msg = "Error occurred while retrieving PWD credential for token - " + tokenId + " and gateway id - "
                    + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(e);
            throw exception;
        }
    }

    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        if (type.equals(SummaryType.SSH)) {
            Map<String, String> sshKeyMap = new HashMap<>();
            List<CredentialSummary> summaryList = new ArrayList<>();
            try {
                List<Credential> allCredentials = credentialReader.getAllCredentialsPerGateway(gatewayId);
                if (allCredentials != null && !allCredentials.isEmpty()) {
                    for (Credential credential : allCredentials) {
                        if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                                && !(credential
                                        instanceof
                                        org.apache.airavata.credential.store.credential.impl.password
                                                .PasswordCredential)
                                && credential.getCredentialOwnerType() == CredentialOwnerType.GATEWAY) {
                            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential =
                                    (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                            CredentialSummary sshCredentialSummary = new CredentialSummary();
                            sshCredentialSummary.setType(SummaryType.SSH);
                            sshCredentialSummary.setToken(sshCredential.getToken());
                            sshCredentialSummary.setUsername(sshCredential.getPortalUserName());
                            sshCredentialSummary.setGatewayId(sshCredential.getGateway());
                            sshCredentialSummary.setDescription(sshCredential.getDescription());
                            sshCredentialSummary.setPublicKey(new String(sshCredential.getPublicKey()));
                            summaryList.add(sshCredentialSummary);
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error("Error occurred while retrieving credential Summary", e);
                org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                        new org.apache.airavata.credential.store.exception.CredentialStoreException(
                                "Error occurred while retrieving credential Summary. More info : " + e.getMessage());
                exception.initCause(e);
                throw exception;
            }
            return summaryList;
        } else {
            logger.info("Summay Type" + type.toString() + " not supported for gateway id - " + gatewayId);
            return null;
        }
    }

    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(
            SummaryType type, String gatewayId, String userId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        if (type.equals(SummaryType.SSH)) {
            Map<String, String> sshKeyMap = new HashMap<>();
            List<CredentialSummary> summaryList = new ArrayList<>();
            try {
                List<Credential> allCredentials = credentialReader.getAllCredentials();
                if (allCredentials != null && !allCredentials.isEmpty()) {
                    for (Credential credential : allCredentials) {
                        if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                                && !(credential
                                        instanceof
                                        org.apache.airavata.credential.store.credential.impl.password
                                                .PasswordCredential)) {
                            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential =
                                    (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                            String portalUserName = sshCredential.getPortalUserName();
                            String gateway = sshCredential.getGateway();
                            if (portalUserName != null && gateway != null) {
                                if (portalUserName.equals(userId)
                                        && gateway.equals(gatewayId)
                                        && sshCredential.getCredentialOwnerType() == CredentialOwnerType.USER) {
                                    org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                                            sshCredentialKey =
                                                    (org.apache.airavata.credential.store.credential.impl.ssh
                                                                    .SSHCredential)
                                                            credential;
                                    CredentialSummary sshCredentialSummary = new CredentialSummary();
                                    sshCredentialSummary.setType(SummaryType.SSH);
                                    sshCredentialSummary.setToken(sshCredentialKey.getToken());
                                    sshCredentialSummary.setUsername(sshCredentialKey.getPortalUserName());
                                    sshCredentialSummary.setGatewayId(sshCredentialKey.getGateway());
                                    sshCredentialSummary.setDescription(sshCredentialKey.getDescription());
                                    sshCredentialSummary.setPublicKey(new String(sshCredentialKey.getPublicKey()));
                                    summaryList.add(sshCredentialSummary);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                logger.error("Error occurred while retrieving credential Summary", e);
                org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                        new org.apache.airavata.credential.store.exception.CredentialStoreException(
                                "Error occurred while retrieving credential Summary. More info : " + e.getMessage());
                exception.initCause(e);
                throw exception;
            }
            return summaryList;
        } else {
            logger.info("Summay Type" + type.toString() + " not supported for user Id - " + userId + " and "
                    + "gateway id - " + gatewayId);
            return null;
        }
    }

    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        Map<String, String> pwdCredMap = new HashMap<>();
        try {
            List<Credential> allCredentials = credentialReader.getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()) {
                for (Credential credential : allCredentials) {
                    if (credential
                            instanceof
                            org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) {
                        org.apache.airavata.credential.store.credential.impl.password.PasswordCredential pwdCredential =
                                (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential)
                                        credential;
                        pwdCredMap.put(
                                pwdCredential.getToken(),
                                pwdCredential.getDescription() == null ? "" : pwdCredential.getDescription());
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Error occurred while retrieving credentials", e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(
                            "Error occurred while retrieving credentials. More info : " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
        return pwdCredMap;
    }

    public boolean deleteSSHCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            credentialReader.removeCredentials(gatewayId, tokenId);
            return true;
        } catch (Throwable e) {
            String msg = "Error occurred while deleting SSH credential for token - " + tokenId + " and gateway id - "
                    + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(e);
            throw exception;
        }
    }

    public boolean deletePWDCredential(String tokenId, String gatewayId)
            throws org.apache.airavata.credential.store.exception.CredentialStoreException {
        try {
            credentialReader.removeCredentials(gatewayId, tokenId);
            return true;
        } catch (Throwable e) {
            String msg = "Error occurred while deleting PWD credential for token - " + tokenId + " and gateway id - "
                    + gatewayId;
            logger.error(msg, e);
            org.apache.airavata.credential.store.exception.CredentialStoreException exception =
                    new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
            exception.initCause(e);
            throw exception;
        }
    }
}
