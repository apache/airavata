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

package org.apache.airavata.credential.store.impl.db;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.CommunityUser;
import org.apache.airavata.credential.store.CredentialStoreException;
import org.apache.airavata.credential.store.CertificateCredential;

import java.io.*;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access class for credential store.
 */
public class CredentialsDAO extends ParentDAO {

    public CredentialsDAO(DBUtil dbUtil) {
        super(dbUtil);
    }

    public void addCredentials(CertificateCredential certificateCredential) throws CredentialStoreException {

        String sql = "insert into credentials values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, certificateCredential.getCommunityUser().getGatewayName());
            preparedStatement.setString(2, certificateCredential.getCommunityUser().getUserName());

            InputStream isCert = new ByteArrayInputStream(
                    convertObjectToByteArray(certificateCredential.getCertificate()));
            preparedStatement.setBinaryStream(3, isCert);

            InputStream isPk = new ByteArrayInputStream(
                    convertObjectToByteArray(certificateCredential.getPrivateKey()));
            preparedStatement.setBinaryStream(4, isPk);

            preparedStatement.setString(5, certificateCredential.getNotBefore());
            preparedStatement.setString(6, certificateCredential.getNotAfter());
            preparedStatement.setLong(7, certificateCredential.getLifeTime());
            preparedStatement.setString(8, certificateCredential.getPortalUserName());
            preparedStatement.setTimestamp(9, new Timestamp(new java.util.Date().getTime()));


            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials. Unsupported encoding.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting community credentials. Error serializing " +
                    "credentials.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }
    }


    public void deleteCredentials(String gatewayName, String communityUserName) throws CredentialStoreException {

        String sql = "delete from credentials where gateway_name=? and community_user_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, communityUserName);

            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error deleting credentials for .");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("community user name - ").append(communityUserName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }
    }

    public void updateCredentials(CertificateCredential certificateCredential) throws CredentialStoreException {

        String sql = "update credentials set credential = ?, private_key = ?, lifetime = ?, " +
                "requesting_portal_user_name = ?, " + "not_before = ?," + "not_after = ?," +
                "requested_time =  ? where gateway_name = ? and community_user_name = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            InputStream isCert = new ByteArrayInputStream(
                    convertObjectToByteArray(certificateCredential.getCertificate()));
            preparedStatement.setBinaryStream(1, isCert);

            InputStream isPk = new ByteArrayInputStream(
                    convertObjectToByteArray(certificateCredential.getPrivateKey()));
            preparedStatement.setBinaryStream(2, isPk);

            preparedStatement.setLong(3, certificateCredential.getLifeTime());
            preparedStatement.setString(4, certificateCredential.getPortalUserName());
            preparedStatement.setString(5, certificateCredential.getNotBefore());
            preparedStatement.setString(6, certificateCredential.getNotAfter());

            preparedStatement.setTimestamp(7, new Timestamp(new java.util.Date().getTime()));
            preparedStatement.setString(8, certificateCredential.getCommunityUser().getGatewayName());
            preparedStatement.setString(9, certificateCredential.getCommunityUser().getUserName());


            preparedStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (UnsupportedEncodingException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials. Invalid encoding for keys.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials. Error serializing objects.");
            stringBuilder.append(" gateway - ").append(certificateCredential.getCommunityUser().getGatewayName());
            stringBuilder.append(" community user name - ").append(certificateCredential.
                    getCommunityUser().getUserName());
            stringBuilder.append(" life time - ").append(certificateCredential.getLifeTime());

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public CertificateCredential getCredential(String gatewayName, String communityUserName)
            throws CredentialStoreException {

        String sql = "select * from credentials where gateway_name=? and community_user_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);
            preparedStatement.setString(2, communityUserName);

            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                CertificateCredential certificateCredential = new CertificateCredential();

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                Blob blobPK = resultSet.getBlob("PRIVATE_KEY");
                byte[] pk = blobPK.getBytes(1, (int) blobPK.length());

                certificateCredential.setCertificate((X509Certificate) convertByteArrayToObject(certificate));
                certificateCredential.setPrivateKey((PrivateKey) convertByteArrayToObject(pk));

                certificateCredential.setLifeTime(resultSet.getLong("LIFETIME"));
                certificateCredential.setCommunityUser(new CommunityUser(gatewayName, communityUserName, null));
                certificateCredential.setPortalUserName(resultSet.getString("REQUESTING_PORTAL_USER_NAME"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("REQUESTED_TIME"));

                return certificateCredential;
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("community user name - ").append(communityUserName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (ClassNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user. Error " +
                    "de-serializing credential objects.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("community user name - ").append(communityUserName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for community user. Error " +
                    "de-serializing credential objects. An IO Error.");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("community user name - ").append(communityUserName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }

        return null;
    }

    public List<CertificateCredential> getCredentials(String gatewayName)
            throws CredentialStoreException {

        List<CertificateCredential> credentialList = new ArrayList<CertificateCredential>();

        String sql = "select * from credentials where gateway_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, gatewayName);

            ResultSet resultSet = preparedStatement.executeQuery();

            CertificateCredential certificateCredential;

            while (resultSet.next()) {
                certificateCredential = new CertificateCredential();

                certificateCredential.setCommunityUser(new CommunityUser(gatewayName,
                        resultSet.getString("COMMUNITY_USER_NAME"), null));

                Blob blobCredentials = resultSet.getBlob("CREDENTIAL");
                byte[] certificate = blobCredentials.getBytes(1, (int) blobCredentials.length());

                Blob blobPK = resultSet.getBlob("PRIVATE_KEY");
                byte[] pk = blobPK.getBytes(1, (int) blobPK.length());

                certificateCredential.setCertificate((X509Certificate) convertByteArrayToObject(certificate));
                certificateCredential.setPrivateKey((PrivateKey) convertByteArrayToObject(pk));

                certificateCredential.setNotBefore(resultSet.getString("NOT_BEFORE"));
                certificateCredential.setNotBefore(resultSet.getString("NOT_AFTER"));
                certificateCredential.setLifeTime(resultSet.getLong("LIFETIME"));
                certificateCredential.setPortalUserName(resultSet.getString("REQUESTING_PORTAL_USER_NAME"));
                certificateCredential.setCertificateRequestedTime(resultSet.getTimestamp("REQUESTED_TIME"));

                credentialList.add(certificateCredential);
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);

            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (ClassNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("Error de-serializing objects.");
            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credential list for ");
            stringBuilder.append("gateway - ").append(gatewayName);
            stringBuilder.append("Error de-serializing objects.");
            log.error(stringBuilder.toString(), e);

            throw new CredentialStoreException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
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
