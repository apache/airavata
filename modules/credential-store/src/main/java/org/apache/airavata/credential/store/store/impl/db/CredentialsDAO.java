package org.apache.airavata.credential.store.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.store.CredentialStoreException;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access class for credential store.
 */
public class CredentialsDAO extends ParentDAO {

    public CredentialsDAO() {
        super();
    }

    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" +
     "(\n" +
     "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     "        TOKEN_ID VARCHAR(256) NOT NULL,\n" +       // Actual token used to identify the credential
     "        CREDENTIAL BLOB NOT NULL,\n" +
     "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
     "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" +
     ")";
     */

    public void addCredentials(String gatewayId, Credential credential,
                               Connection connection) throws CredentialStoreException {

        String sql = "insert into credentials values (?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayId);
            preparedStatement.setString(2, credential.getToken());

            InputStream isCert = new ByteArrayInputStream(
                    convertObjectToByteArray(credential));
            preparedStatement.setBinaryStream(3, isCert);

            preparedStatement.setString(4, credential.getPortalUserName());

            java.util.Date date= new java.util.Date();
            Timestamp timestamp = new Timestamp(date.getTime());

            preparedStatement.setTimestamp(5, timestamp);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials. Unsupported encoding.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials. Error serializing " +
                    "credentials.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" community user name - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            DBUtil.cleanup(preparedStatement);
        }
    }


    public void deleteCredentials(String gatewayName, String tokenId, Connection connection)
            throws CredentialStoreException {

        String sql = "delete from credentials where GATEWAY_ID=? and TOKEN_ID=?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, tokenId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error deleting credentials for .");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("token id - ").append(tokenId);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement);
        }
    }

    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" +
     "(\n" +
     "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     "        TOKEN_ID VARCHAR(256) NOT NULL,\n" +       // Actual token used to identify the credential
     "        CREDENTIAL BLOB NOT NULL,\n" +
     "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
     "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" +
     ")";
     */
    public void updateCredentials(String gatewayId, Credential credential,
                                  Connection connection) throws CredentialStoreException {

        String sql = "update CREDENTIALS set CREDENTIAL = ?, PORTAL_USER_ID = ?, TIME_PERSISTED = ? where GATEWAY_ID = ? and TOKEN_ID = ?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            InputStream isCert = new ByteArrayInputStream(
                    convertObjectToByteArray(credential));
            preparedStatement.setBinaryStream(1, isCert);

            preparedStatement.setString(2, credential.getPortalUserName());

            preparedStatement.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
            preparedStatement.setString(4, gatewayId);
            preparedStatement.setString(5, credential.getToken());


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials. Invalid encoding for keys.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials. Error serializing objects.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            DBUtil.cleanup(preparedStatement);
        }

    }

    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" +
     "(\n" +
     "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     "        TOKEN_ID VARCHAR(256) NOT NULL,\n" +       // Actual token used to identify the credential
     "        CREDENTIAL BLOB NOT NULL,\n" +
     "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
     "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" +
     ")";
     */
    public Credential getCredential(String gatewayName, String tokenId, Connection connection)
            throws CredentialStoreException {

        String sql = "select * from credentials where GATEWAY_ID=? and TOKEN_ID=?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, tokenId);

            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                //CertificateCredential certificateCredential = new CertificateCredential();

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                Credential certificateCredential = (Credential) convertByteArrayToObject(certificate);

                certificateCredential.setPortalUserName(resultSet.getString("PORTAL_USER_ID"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("TIME_PERSISTED"));

                return certificateCredential;
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("token id - ").append(tokenId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (ClassNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user. Error " +
                    "de-serializing credential objects.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("token id - ").append(tokenId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user. Error " +
                    "de-serializing credential objects. An IO Error.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("tokenId - ").append(tokenId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement);
        }

        return null;
    }


    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" +
     "(\n" +
     "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     "        TOKEN_ID VARCHAR(256) NOT NULL,\n" +       // Actual token used to identify the credential
     "        CREDENTIAL BLOB NOT NULL,\n" +
     "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
     "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" +
     ")";
     */
    public List<Credential> getCredentials(String gatewayName, Connection connection)
            throws CredentialStoreException {

        List<Credential> credentialList = new ArrayList<Credential>();

        String sql = "select * from credentials where GATEWAY_ID=?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);

            ResultSet resultSet = preparedStatement.executeQuery();

            Credential certificateCredential;

            while (resultSet.next()) {

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                certificateCredential = (Credential) convertByteArrayToObject(certificate);

                certificateCredential.setPortalUserName(resultSet.getString("PORTAL_USER_ID"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("TIME_PERSISTED"));

                credentialList.add(certificateCredential);
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (ClassNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("Error de-serializing objects.");
            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("Error de-serializing objects.");
            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement);
        }

        return credentialList;
    }

    public static Object convertByteArrayToObject(byte[] data) throws IOException,
            ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = null;
        try {
            o = objectInputStream.readObject();
        } finally {
            objectInputStream.close();
        }
        return o;
    }

    public static byte[] convertObjectToByteArray(Serializable o) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        }

        return byteArrayOutputStream.toByteArray();
    }

}
