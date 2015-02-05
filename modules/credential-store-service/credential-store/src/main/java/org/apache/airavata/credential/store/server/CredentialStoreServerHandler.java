package org.apache.airavata.credential.store.server;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.cpi.cs_cpi_serviceConstants;
import org.apache.airavata.credential.store.datamodel.CertificateCredential;
import org.apache.airavata.credential.store.datamodel.PasswordCredential;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.db.CredentialsDAO;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class CredentialStoreServerHandler implements CredentialStoreService.Iface {
    protected static Logger log = LoggerFactory.getLogger(CredentialStoreServerHandler.class);
    private DBUtil dbUtil;
    private CredentialsDAO credentialsDAO;

    public CredentialStoreServerHandler() throws ApplicationSettingsException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
        String userName = ServerSettings.getCredentialStoreDBUser();
        String password = ServerSettings.getCredentialStoreDBPassword();
        String driverName = ServerSettings.getCredentialStoreDBDriver();

        log.debug("Starting credential store, connecting to database - " + jdbcUrl + " DB user - " + userName + " driver name - " + driverName);
        dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        this.credentialsDAO = new CredentialsDAO(ApplicationSettings.getCredentialStoreKeyStorePath(),
                ApplicationSettings.getCredentialStoreKeyAlias(), new DefaultKeyStorePasswordCallback());
    }

    @Override
    public String getCSServiceVersion() throws TException {
        return cs_cpi_serviceConstants.CS_CPI_VERSION;
    }

    @Override
    public String addSSHCredential(SSHCredential sshCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        Connection connection = null;
        org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential credential = new org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential();
        credential.setGateway(sshCredential.getGatewayId());
        credential.setPortalUserName(sshCredential.getUsername());
        // only username and gateway id will be sent by client.
        String token = TokenGenerator.generateToken(sshCredential.getGatewayId(), null);
        credential.setToken(token);
        credential.setPassphrase(sshCredential.getPassphrase());
        if (sshCredential.getPrivateKey() != null){
            credential.setPrivateKey(sshCredential.getPrivateKey().getBytes());
        }
        if (sshCredential.getPublicKey() != null){
            credential.setPublicKey(sshCredential.getPublicKey().getBytes());
        }
        try {
            connection = dbUtil.getConnection();
            // First delete existing credentials
            credentialsDAO.deleteCredentials(sshCredential.getGatewayId(), sshCredential.getToken(), connection);
            // Add the new certificate
            credentialsDAO.addCredentials(sshCredential.getGatewayId(), credential, connection);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return token;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Unable to rollback transaction", e1);
                }
            }
            log.error("Unable to retrieve database connection.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Unable to retrieve database connection.");
        } catch (CredentialStoreException e) {
            log.error("Error occurred while saving SSH Credentials.", e);
            throw new org.apache.airavata.credential.store.exception.CredentialStoreException("Error occurred while saving SSH Credentials.");
        } finally {
            DBUtil.cleanup(connection);
        }
    }

    @Override
    public String addCertificateCredential(CertificateCredential certificateCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
    }

    @Override
    public String addPasswordCredential(PasswordCredential passwordCredential) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
    }

    @Override
    public SSHCredential getSSHCredential(String tokenId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
    }

    @Override
    public CertificateCredential getCertificateCredential(String tokenId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
    }

    @Override
    public PasswordCredential getPasswordCredential(String tokenId) throws org.apache.airavata.credential.store.exception.CredentialStoreException, TException {
        return null;
    }


}
