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
package org.apache.airavata.credential.store.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.KeyStorePasswordCallback;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import org.apache.airavata.credential.store.store.CredentialStoreException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data access class for credential store.
 */
public class CredentialsDAO extends ParentDAO {

    private String keyStorePath = null;
    private String secretKeyAlias = null;
    private KeyStorePasswordCallback keyStorePasswordCallback = null;

    public CredentialsDAO() {
    }

    public CredentialsDAO(String keyStore, String alias, KeyStorePasswordCallback passwordCallback) {
        this.keyStorePath = keyStore;
        this.secretKeyAlias = alias;
        this.keyStorePasswordCallback = passwordCallback;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getSecretKeyAlias() {
        return secretKeyAlias;
    }

    public void setSecretKeyAlias(String secretKeyAlias) {
        this.secretKeyAlias = secretKeyAlias;
    }

    public KeyStorePasswordCallback getKeyStorePasswordCallback() {
        return keyStorePasswordCallback;
    }

    public void setKeyStorePasswordCallback(KeyStorePasswordCallback keyStorePasswordCallback) {
        this.keyStorePasswordCallback = keyStorePasswordCallback;
    }

    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     * "        TOKEN_ID VARCHAR(256) NOT NULL,\n" + // Actual token used to identify the credential
     * "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     * "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n"
     * + ")";
     */

    public void addCredentials(String gatewayId, Credential credential, Connection connection)
            throws CredentialStoreException {

        String sql = "INSERT INTO CREDENTIALS (GATEWAY_ID, TOKEN_ID, CREDENTIAL, PORTAL_USER_ID, TIME_PERSISTED, DESCRIPTION, CREDENTIAL_OWNER_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayId);
            preparedStatement.setString(2, credential.getToken());

            InputStream isCert = new ByteArrayInputStream(convertObjectToByteArray(credential));
            preparedStatement.setBinaryStream(3, isCert);

            preparedStatement.setString(4, credential.getPortalUserName());
            
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            preparedStatement.setTimestamp(5, timestamp);

            preparedStatement.setString(6,credential.getDescription());

            preparedStatement.setString(7, credential.getCredentialOwnerType().toString());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting credentials.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            DBUtil.cleanup(preparedStatement);
        }
    }


    public void deleteCredentials(String gatewayName, String tokenId, Connection connection)
            throws CredentialStoreException {

        String sql = "DELETE FROM CREDENTIALS WHERE GATEWAY_ID=? AND TOKEN_ID=?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, tokenId);

            preparedStatement.executeUpdate();
            connection.commit();
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
     * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     * "        TOKEN_ID VARCHAR(256) NOT NULL,\n" + // Actual token used to identify the credential
     * "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     * "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n"
     * + ")";
     */
    public void updateCredentials(String gatewayId, Credential credential, Connection connection)
            throws CredentialStoreException {

        String sql = "UPDATE CREDENTIALS set CREDENTIAL = ?, PORTAL_USER_ID = ?, TIME_PERSISTED = ?, DESCRIPTION = ?, CREDENTIAL_OWNER_TYPE = ? where GATEWAY_ID = ? and TOKEN_ID = ?";

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            InputStream isCert = new ByteArrayInputStream(convertObjectToByteArray(credential));
            preparedStatement.setBinaryStream(1, isCert);

            preparedStatement.setString(2, credential.getPortalUserName());

            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setString(4, credential.getDescription());
            preparedStatement.setString(5, credential.getCredentialOwnerType().toString());
            preparedStatement.setString(6, gatewayId);
            preparedStatement.setString(7, credential.getToken());


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" gateway - ").append(gatewayId);
            stringBuilder.append(" token id - ").append(credential.getToken());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            DBUtil.cleanup(preparedStatement);
        }

    }

    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     * "        TOKEN_ID VARCHAR(256) NOT NULL,\n" + // Actual token used to identify the credential
     * "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     * "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n"
     * + ")";
     */
    public Credential getCredential(String gatewayName, String tokenId, Connection connection)
            throws CredentialStoreException {

        String sql = "SELECT * FROM CREDENTIALS WHERE GATEWAY_ID=? AND TOKEN_ID=?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, tokenId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // CertificateCredential certificateCredential = new CertificateCredential();

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                Credential certificateCredential = (Credential) convertByteArrayToObject(certificate);

                certificateCredential.setPortalUserName(resultSet.getString("PORTAL_USER_ID"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("TIME_PERSISTED"));
                certificateCredential.setDescription(resultSet.getString("DESCRIPTION"));
                certificateCredential.setCredentialOwnerType(CredentialOwnerType.valueOf(resultSet.getString("CREDENTIAL_OWNER_TYPE")));

                return certificateCredential;
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for user.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("token id - ").append(tokenId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement, resultSet);
        }

        return null;
    }
    /**
     * 
     */
    public String getGatewayID(String tokenId, Connection connection)
            throws CredentialStoreException {

        String sql = "SELECT GATEWAY_ID FROM CREDENTIALS WHERE TOKEN_ID=?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, tokenId);
         
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
            	return resultSet.getString("GATEWAY_ID");
              }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for user.");
            stringBuilder.append("token id - ").append(tokenId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement, resultSet);
        }

        return null;
    }
    /**
     * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
     * "        TOKEN_ID VARCHAR(256) NOT NULL,\n" + // Actual token used to identify the credential
     * "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
     * "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n"
     * + ")";
     */
    public List<Credential> getCredentials(String gatewayName, Connection connection) throws CredentialStoreException {

        return getCredentialsInternal(gatewayName, null, connection);
    }

    public List<Credential> getCredentials(String gatewayId, List<String> accessibleTokenIds, Connection connection) throws CredentialStoreException {

        if (accessibleTokenIds == null || accessibleTokenIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getCredentialsInternal(gatewayId, accessibleTokenIds, connection);
    }

    private List<Credential> getCredentialsInternal(String gatewayId, List<String> accessibleTokenIds, Connection connection) throws CredentialStoreException {
        List<Credential> credentialList = new ArrayList<>();

        String sql = "SELECT * FROM CREDENTIALS WHERE GATEWAY_ID=?";
        if (accessibleTokenIds != null && !accessibleTokenIds.isEmpty()) {
            String tokenIdBindParameters = String.join(", ", accessibleTokenIds.stream().map(tokenId -> "?").collect(Collectors.toList()));
            sql += " AND TOKEN_ID IN (" + tokenIdBindParameters + ")";
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            int parameterIndex = 1;
            preparedStatement.setString(parameterIndex++, gatewayId);
            if (accessibleTokenIds != null) {
                for (String tokenId : accessibleTokenIds) {

                    preparedStatement.setString(parameterIndex++, tokenId);
                }
            }

            resultSet = preparedStatement.executeQuery();

            Credential certificateCredential;

            while (resultSet.next()) {

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                certificateCredential = (Credential) convertByteArrayToObject(certificate);
                certificateCredential.setToken(resultSet.getString("TOKEN_ID"));
                certificateCredential.setPortalUserName(resultSet.getString("PORTAL_USER_ID"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("TIME_PERSISTED"));
                certificateCredential.setDescription(resultSet.getString("DESCRIPTION"));

                credentialList.add(certificateCredential);
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayId);

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement, resultSet);
        }

        return credentialList;
    }

    /**
     * Gets all credentials.
     * @param connection The database connection
     * @return All credentials as a list
     * @throws CredentialStoreException If an error occurred while rerieving credentials.
     */
    public List<Credential> getCredentials(Connection connection) throws CredentialStoreException {

        List<Credential> credentialList = new ArrayList<Credential>();

        String sql = "SELECT * FROM CREDENTIALS";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            resultSet = preparedStatement.executeQuery();

            Credential certificateCredential;

            while (resultSet.next()) {

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                certificateCredential = (Credential) convertByteArrayToObject(certificate);
                certificateCredential.setToken(resultSet.getString("TOKEN_ID"));
                certificateCredential.setPortalUserName(resultSet.getString("PORTAL_USER_ID"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("TIME_PERSISTED"));
                certificateCredential.setDescription(resultSet.getString("DESCRIPTION"));
                certificateCredential.setCredentialOwnerType(CredentialOwnerType.valueOf(resultSet.getString("CREDENTIAL_OWNER_TYPE")));

                credentialList.add(certificateCredential);
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving all credentials");

            log.debug(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            DBUtil.cleanup(preparedStatement, resultSet);
        }

        return credentialList;
    }

    public Object convertByteArrayToObject(byte[] data) throws CredentialStoreException {
        ObjectInputStream objectInputStream = null;
        Object o = null;
        try {
            try {
                //decrypt the data first
                if (encrypt()) {
                    data = SecurityUtil.decrypt(this.keyStorePath, this.secretKeyAlias, this.keyStorePasswordCallback, data);
                }

                objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
                o = objectInputStream.readObject();

            } catch (IOException e) {
                throw new CredentialStoreException("Error de-serializing object.", e);
            } catch (ClassNotFoundException e) {
                throw new CredentialStoreException("Error de-serializing object.", e);
            } catch (GeneralSecurityException e) {
                throw new CredentialStoreException("Error decrypting data.", e);
            }
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the stream", e);
                }
            }
        }
        return o;
    }

    public byte[] convertObjectToByteArray(Serializable o) throws CredentialStoreException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new CredentialStoreException("Error serializing object.", e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing object output stream", e);
                }
            }
        }

        // encrypt the byte array
        if (encrypt()) {
            byte[] array = byteArrayOutputStream.toByteArray();
            try {
                return SecurityUtil.encrypt(this.keyStorePath, this.secretKeyAlias, this.keyStorePasswordCallback, array);
            } catch (GeneralSecurityException e) {
                throw new CredentialStoreException("Error encrypting data", e);
            } catch (IOException e) {
                throw new CredentialStoreException("Error encrypting data. IO exception.", e);
            }
        } else {
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Says whether to encrypt data or not. if alias, keystore is set
     * we treat encryption true.
     * @return true if data should encrypt else false.
     */
    private boolean encrypt() {
        return this.keyStorePath != null;
    }

}
