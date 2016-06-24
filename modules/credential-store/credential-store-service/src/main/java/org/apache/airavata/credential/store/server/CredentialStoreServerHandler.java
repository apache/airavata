/*
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
 *
 */
package org.apache.airavata.credential.store.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.cpi.credential_store_cpiConstants;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.datamodel.CertificateCredential;
import org.apache.airavata.credential.store.datamodel.PasswordCredential;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.CertificateCredentialWriter;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.apache.airavata.credential.store.util.Utility;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.provider.X509Factory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CredentialStoreServerHandler implements CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreServerHandler.class);
    private DBUtil dbUtil;
    private SSHCredentialWriter sshCredentialWriter;
    private CertificateCredentialWriter certificateCredentialWriter;
    private CredentialReaderImpl credentialReader;

    public CredentialStoreServerHandler() throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
        String userName = ServerSettings.getCredentialStoreDBUser();
        String password = ServerSettings.getCredentialStoreDBPassword();
        String driverName = ServerSettings.getCredentialStoreDBDriver();

        log.debug("Starting credential store, connecting to database - " + jdbcUrl + " DB user - " + userName + " driver name - " + driverName);
        dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        sshCredentialWriter = new SSHCredentialWriter(dbUtil);
        certificateCredentialWriter = new CertificateCredentialWriter(dbUtil);
        credentialReader = new CredentialReaderImpl(dbUtil);
    }

    @Override
    public String getCSServiceVersion() throws TException {
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
            if (sshCredential.getPublicKey() != null) {
                credential.setPublicKey(sshCredential.getPublicKey().getBytes());
            }
            if (sshCredential.getPublicKey() == null || sshCredential.getPrivateKey() == null) {
                credential = Utility.generateKeyPair(credential);
            }
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
            byte [] decoded = encoder.decode(certificateCredential.getX509Cert().replaceAll(X509Factory.BEGIN_CERT, "").replaceAll(X509Factory.END_CERT, ""));
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
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) {
                org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential1 = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                SSHCredential sshCredential = new SSHCredential();
                sshCredential.setUsername(credential1.getPortalUserName());
                sshCredential.setGatewayId(credential1.getGateway());
                sshCredential.setPublicKey(new String(credential1.getPublicKey()));
                sshCredential.setPrivateKey(new String(credential1.getPrivateKey()));
                sshCredential.setPassphrase(credential1.getPassphrase());
                sshCredential.setToken(credential1.getToken());
                sshCredential.setPersistedTime(credential1.getCertificateRequestedTime().getTime());
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
    public CertificateCredential getCertificateCredential(String tokenId, String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        try {
            Credential credential = credentialReader.getCredential(gatewayId, tokenId);
            if (credential instanceof org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) {
                org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential credential1 = (org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential) credential;
                CertificateCredential certificateCredential = new CertificateCredential();
                org.apache.airavata.credential.store.datamodel.CommunityUser communityUser = new org.apache.airavata.credential.store.datamodel.CommunityUser();
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
    public Map<String, String> getAllSSHKeysForUser(String username) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        Map<String, String> sshKeyMap = new HashMap<>();
        try {
            List<Credential> allCredentials = credentialReader.getAllCredentials();
            if (allCredentials != null && !allCredentials.isEmpty()){
                for (Credential credential : allCredentials) {
                    if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) {
                        org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                        String portalUserName = sshCredential.getPortalUserName();
                        if (portalUserName != null){
                            if (portalUserName.equals(username)) {
                                byte[] publicKey = sshCredential.getPublicKey();
                                if (publicKey != null) {
                                    sshKeyMap.put(sshCredential.getToken(), new String(publicKey));
                                }
                            }
                        }
                    }
                }
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credentials", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving credentials");
        }
        return sshKeyMap;
    }

    @Override
    public Map<String, String> getAllSSHKeysForGateway(String gatewayId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        Map<String, String> sshKeyMap = new HashMap<>();
        try {
            List<Credential> allCredentials = credentialReader.getAllCredentialsPerGateway(gatewayId);
            if (allCredentials != null && !allCredentials.isEmpty()){
                for (Credential credential : allCredentials) {
                    if (credential instanceof org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) {
                        org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential sshCredential = (org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential) credential;
                        byte[] publicKey = sshCredential.getPublicKey();
                        if (publicKey != null) {
                            sshKeyMap.put(sshCredential.getToken(), new String(publicKey));
                        }
                    }
                }
            }
        } catch (CredentialStoreException e) {
            log.error("Error occurred while retrieving credentials", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while retrieving credentials");
        }
        return sshKeyMap;

    }

    @Override
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
