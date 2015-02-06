package org.apache.airavata.credential.store.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.cpi.cs_cpi_serviceConstants;
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
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
        return cs_cpi_serviceConstants.CS_CPI_VERSION;
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
            credential.setPassphrase(sshCredential.getPassphrase());
            if (sshCredential.getPrivateKey() != null) {
                credential.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            }
            if (sshCredential.getPublicKey() != null) {
                credential.setPublicKey(sshCredential.getPublicKey().getBytes());
            }
            if (sshCredential.getPublicKey() == null || sshCredential.getPrivateKey() == null) {
                credential = Utility.generateKeyPair(sshCredential.getUsername(), sshCredential.getPassphrase());
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
            credential.setCommunityUser(new CommunityUser(certificateCredential.getCommunityUser().getGatewayNmae(),
                    certificateCredential.getCommunityUser().getUsername(), certificateCredential.getCommunityUser().getUserEmail()));
            String token = TokenGenerator.generateToken(certificateCredential.getCommunityUser().getGatewayNmae(), null);
            credential.setToken(token);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream stream = new ByteArrayInputStream(certificateCredential.getX509Cert().getBytes());
            X509Certificate certificate = (X509Certificate)cf.generateCertificate(stream);
            X509Certificate[] certificates = new X509Certificate[1];
            certificates[0] = certificate;
            credential.setCertificates(certificates);
            certificateCredentialWriter.writeCredentials(credential);
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
    public String addPasswordCredential(PasswordCredential passwordCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
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
                communityUser.setGatewayNmae(credential1.getCommunityUser().getGatewayName());
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
        return null;
    }


}
