/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.credential.store.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.cpi.credential_store_cpiConstants;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.store.impl.util.CredentialStoreDBInitConfig;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.apache.airavata.credential.store.util.Utility;
import org.apache.airavata.model.credential.store.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import sun.security.provider.X509Factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CredentialStoreServerHandler implements CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreServerHandler.class);
    private DBUtil dbUtil;
    private SSHCredentialWriter sshCredentialWriter;
    private CertificateCredentialWriter certificateCredentialWriter;
    private CredentialReaderImpl credentialReader;

    public CredentialStoreServerHandler() throws ApplicationSettingsException, IllegalAccessException,
            ClassNotFoundException, InstantiationException, SQLException, IOException {
        String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
        String userName = ServerSettings.getCredentialStoreDBUser();
        String password = ServerSettings.getCredentialStoreDBPassword();
        String driverName = ServerSettings.getCredentialStoreDBDriver();

        log.debug("Starting credential store, connecting to database - " + jdbcUrl + " DB user - " + userName + " driver name - " + driverName);
        DBInitializer.initializeDB(new CredentialStoreDBInitConfig());

        dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        sshCredentialWriter = new SSHCredentialWriter(dbUtil);
        certificateCredentialWriter = new CertificateCredentialWriter(dbUtil);
        credentialReader = new CredentialReaderImpl(dbUtil);
    }

    @Override
    public String getAPIVersion() throws TException {
        return credential_store_cpiConstants.CS_CPI_VERSION;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential = new org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential();
            credential.setGateway(sshCredential.getGatewayId());
            credential.setPortalUserName(sshCredential.getUsername());
            // only username and gateway id will be sent by client.
            String token = TokenGenerator.generateToken(sshCredential.getGatewayId(), null);
            credential.setToken(token);
            credential.setPassphrase(String.valueOf(UUID.randomUUID()));
            if (sshCredential.getPrivateKey() != null) {
                credential.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            }
            if(sshCredential.getDescription() != null){
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
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving SSH Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while saving SSH Credentials.");
        } catch (Exception e) {
            log.error("Error occurred while generating key pair.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while generating key pair..");
        }
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential credential = new org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential();
            credential.setPortalUserName(certificateCredential.getCommunityUser().getUsername());
            credential.setCommunityUser(new CommunityUser(certificateCredential.getCommunityUser().getGatewayName(),
                    certificateCredential.getCommunityUser().getUsername(), certificateCredential.getCommunityUser().getUserEmail()));
            String token = TokenGenerator.generateToken(certificateCredential.getCommunityUser().getGatewayName(), null);
            credential.setToken(token);
            Base64 encoder = new Base64(64);
            byte [] decoded = encoder.decode(certificateCredential.getX509Cert().replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(decoded));
            X509Certificate[] certificates = new X509Certificate[1];
            certificates[0] = certificate;
            credential.setCertificates(certificates);
            certificateCredentialWriter.writeCredentials(credential);
            return token;
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving Certificate Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while saving Certificate Credentials.");
        } catch (Exception e) {
            log.error("Error occurred while converting to X509 certificate.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while converting to X509 certificate..");
        }
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            org.apache.airavata.credential.store.credential.impl.password.PasswordCredential credential = new org.apache.airavata.credential.store.credential.impl.password.PasswordCredential();
            credential.setGateway(passwordCredential.getGatewayId());
            credential.setPortalUserName(passwordCredential.getPortalUserName());
            credential.setUserName(passwordCredential.getLoginUserName());
            credential.setPassword(passwordCredential.getPassword());
            credential.setDescription(passwordCredential.getDescription());
            String token = TokenGenerator.generateToken(passwordCredential.getGatewayId(), null);
            credential.setToken(token);
            sshCredentialWriter.writeCredentials(credential);
            return token;
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving PWD Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while saving PWD Credentials.");
        } catch (Exception e) {
            log.error("Error occurred while registering PWD Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while registering PWD Credentials..");
        }
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                    && !(credential instanceof org.apache.airavata.credential.store.credential.impl.password
                            .PasswordCredential)) {
                org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential1 = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                SSHCredential sshCredential = new SSHCredential();
                sshCredential.setUsername(credential1.getPortalUserName());
                sshCredential.setGatewayId(credential1.getGateway());
                sshCredential.setPublicKey(new String(credential1.getPublicKey()));
                sshCredential.setPrivateKey(new String(credential1.getPrivateKey()));
                sshCredential.setPassphrase(credential1.getPassphrase());
                sshCredential.setToken(credential1.getToken());
                sshCredential.setPersistedTime(credential1.getCertificateRequestedTime().getTime());
                sshCredential.setDescription(credential1.getDescription());
                return sshCredential;
            } else {
                log.info("Could not find SSH credentials for token - " + tokenId + " and "
                        + "gateway id - " + gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving SSH credentialfor token - " +  tokenId + " and gateway id - " + gatewayId, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving SSH credential for token - " +  tokenId + " and gateway id - " + gatewayId);
        }
    }

    @Override
    public CredentialSummary getCredentialSummary(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (isSSHCredential(credential)) {
                return convertToCredentialSummary((org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential);
            } else if (isCertificateCredential(credential)) {
                return convertToCredentialSummary((org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) credential);
            } else if (isPasswordCredential(credential)) {
                return convertToCredentialSummary((org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) credential);
            }
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Unrecognized type of credential for token: " + tokenId);
        } catch (CredentialStoreException e) {
            final String msg = "Error occurred while retrieving credential summary for token - " + tokenId + " and gateway id - " + gatewayId;
            log.error(msg , e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
        }
    }

    @Override
    public List<CredentialSummary> getAllCredentialSummaries(SummaryType type, List<String> accessibleTokenIds, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            List<Credential> credentials = credentialReader.getAllAccessibleCredentialsPerGateway(gatewayId, accessibleTokenIds);
            if (type.equals(SummaryType.SSH)) {
                return credentials.stream()
                        .filter(this::isSSHCredential)
                        .map(cred -> (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.CERT)) {
                return credentials.stream()
                        .filter(this::isCertificateCredential)
                        .map(cred -> (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else if (type.equals(SummaryType.PASSWD)) {
                return credentials.stream()
                        .filter(this::isPasswordCredential)
                        .map(cred -> (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) cred)
                        .map(cred -> convertToCredentialSummary(cred))
                        .collect(Collectors.toList());
            } else {
                throw new RuntimeException("Summary Type " + type + " is not supported.");
            }
        } catch (CredentialStoreException e) {
            final String msg = "Error occurred while retrieving " + type + " credential Summary for tokens - " + accessibleTokenIds + " and gateway id - " + gatewayId;
            log.error(msg , e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException(msg);
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

    private CredentialSummary convertToCredentialSummary(org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential cred) {
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

    private CredentialSummary convertToCredentialSummary(org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential cred) {
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

    private CredentialSummary convertToCredentialSummary(org.apache.airavata.credential.store.credential.impl.password.PasswordCredential cred) {
        CredentialSummary credentialSummary = new CredentialSummary();
        credentialSummary.setType(SummaryType.PASSWD);
        credentialSummary.setUsername(cred.getPortalUserName());
        credentialSummary.setGatewayId(cred.getGateway());
        credentialSummary.setToken(cred.getToken());
        credentialSummary.setPersistedTime(cred.getCertificateRequestedTime().getTime());
        credentialSummary.setDescription(cred.getDescription());
        return credentialSummary;
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) {
                org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential credential1 = (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) credential;
                CertificateCredential certificateCredential = new CertificateCredential();
                org.apache.airavata.model.credential.store.CommunityUser communityUser = new org.apache.airavata.model.credential.store.CommunityUser();
                communityUser.setGatewayName(credential1.getCommunityUser().getGatewayName());
                communityUser.setUsername(credential1.getCommunityUser().getUserName());
                communityUser.setUserEmail(credential1.getCommunityUser().getUserEmail());
                certificateCredential.setCommunityUser(communityUser);
                certificateCredential.setToken(credential1.getToken());
                certificateCredential.setLifeTime(credential1.getLifeTime());
                certificateCredential.setNotAfter(credential1.getNotAfter());
                certificateCredential.setNotBefore(credential1.getNotBefore());
                certificateCredential.setPersistedTime(credential1.getCertificateRequestedTime().getTime());
                if (credential1.getPrivateKey() != null){
                    certificateCredential.setPrivateKey(credential1.getPrivateKey().toString());
                }
                certificateCredential.setX509Cert(credential1.getCertificates()[0].toString());
                return certificateCredential;
            } else {
                log.info("Could not find Certificate credentials for token - " + tokenId + " and "
                        + "gateway id - " + gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving Certificate credential for token - " +  tokenId + " and gateway id - " + gatewayId, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving Certificate credential for token - " +  tokenId + " and gateway id - " + gatewayId);
        }
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) {
                org.apache.airavata.credential.store.credential.impl.password.PasswordCredential credential1 = (org.apache.airavata.credential
                        .store.credential.impl.password.PasswordCredential) credential;
                PasswordCredential pwdCredential = new PasswordCredential();
                pwdCredential.setGatewayId(credential1.getGateway());
                pwdCredential.setPortalUserName(credential1.getPortalUserName());
                pwdCredential.setLoginUserName(credential1.getUserName());
                pwdCredential.setPassword(credential1.getPassword());
                pwdCredential.setDescription(credential1.getDescription());
                pwdCredential.setToken(credential1.getToken());
                pwdCredential.setPersistedTime(credential1.getCertificateRequestedTime().getTime());
                return pwdCredential;
            } else {
                log.info("Could not find PWD credentials for token - " + tokenId + " and "
                        + "gateway id - " + gatewayId);
                return null;
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving PWD credentialfor token - " +  tokenId + " and gateway id - " + gatewayId, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving PWD credential for token - " +  tokenId + " and gateway id - " + gatewayId);
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForGateway(SummaryType type, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        if(type.equals(SummaryType.SSH)){
            Map<String, String> sshKeyMap = new HashMap<>();
            List<CredentialSummary> summaryList = new ArrayList<>();
            try {
                List<Credential> allCredentials = credentialReader.getAllCredentialsPerGateway(gatewayId);
                if (allCredentials != null && !allCredentials.isEmpty()){
                    for (Credential credential : allCredentials) {
                        if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                                && !(credential instanceof org.apache.airavata.credential.store.credential.impl
                                .password.PasswordCredential)
                                && credential.getCredentialOwnerType() == CredentialOwnerType.GATEWAY) {
                            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
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
            } catch (CredentialStoreException e) {
                log.error("Error occurred while retrieving credential Summary", e);
                throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving credential Summary");
            }
            return summaryList;
        }else{
            log.info("Summay Type"+ type.toString() + " not supported for gateway id - " + gatewayId);
            return null;
        }
    }

    @Override
    @Deprecated
    public List<CredentialSummary> getAllCredentialSummaryForUserInGateway(SummaryType type, String gatewayId, String userId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        if(type.equals(SummaryType.SSH)){
            Map<String, String> sshKeyMap = new HashMap<>();
            List<CredentialSummary> summaryList = new ArrayList<>();
            try {
                List<Credential> allCredentials = credentialReader.getAllCredentials();
                if (allCredentials != null && !allCredentials.isEmpty()){
                    for (Credential credential : allCredentials) {
                        if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential
                                && !(credential instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential)) {
                            org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                            String portalUserName = sshCredential.getPortalUserName();
                            String gateway = sshCredential.getGateway();
                            if (portalUserName != null && gateway != null){
                                if (portalUserName.equals(userId) && gateway.equals(gatewayId) && sshCredential.getCredentialOwnerType() == CredentialOwnerType.USER) {
                                    org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredentialKey = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
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
            } catch (CredentialStoreException e) {
                log.error("Error occurred while retrieving credential Summary", e);
                throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving credential Summary");
            }
            return summaryList;
        }else{
            log.info("Summay Type"+ type.toString() + " not supported for user Id - " + userId + " and "
                    + "gateway id - " + gatewayId);
            return null;
        }
    }


    @Override
    @Deprecated
    public Map<String, String> getAllPWDCredentialsForGateway(String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        Map<String, String> pwdCredMap = new HashMap<>();
        try {
            List<Credential> allCredentials = credentialReader.getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()){
                for (Credential credential : allCredentials) {
                    if (credential instanceof org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) {
                        org.apache.airavata.credential.store.credential.impl.password.PasswordCredential pwdCredential = (org.apache.airavata.credential.store.credential.impl.password.PasswordCredential) credential;
                        pwdCredMap.put(pwdCredential.getToken(),pwdCredential.getDescription() == null ? "" : pwdCredential.getDescription());
                    }
                }
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credentials", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving credentials");
        }
        return pwdCredMap;
    }

    @Override
    public boolean deleteSSHCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            credentialReader.removeCredentials(gatewayId, tokenId);
            return true;
        } catch (CredentialStoreException e) {
            log.error("Error occurred while deleting SSH credential for token - " +  tokenId + " and gateway id - " + gatewayId, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while deleting SSH credential for token - " +  tokenId + " and gateway id - " + gatewayId);
        }
    }

    @Override
    public boolean deletePWDCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            credentialReader.removeCredentials(gatewayId, tokenId);
            return true;
        } catch (CredentialStoreException e) {
            log.error("Error occurred while deleting PWD credential for token - " +  tokenId + " and gateway id - " + gatewayId, e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while deleting PWD credential for token - " +  tokenId + " and gateway id - " + gatewayId);
        }
    }


}
