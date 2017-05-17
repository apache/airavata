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
package org.apache.airavata.credential.store.store.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.impl.db.CommunityUserDAO;
import org.apache.airavata.credential.store.store.impl.db.CredentialsDAO;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.CredentialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Writes certificate credentials to database.
 */
public class CertificateCredentialWriter implements CredentialWriter {

    private CredentialsDAO credentialsDAO;
    private CommunityUserDAO communityUserDAO;

    protected static Logger log = LoggerFactory.getLogger(CertificateCredentialWriter.class);

    private DBUtil dbUtil;

    public CertificateCredentialWriter(DBUtil dbUtil) throws ApplicationSettingsException {

        this.dbUtil = dbUtil;

        this.credentialsDAO = new CredentialsDAO(ApplicationSettings.getCredentialStoreKeyStorePath(),
                ApplicationSettings.getCredentialStoreKeyAlias(), new DefaultKeyStorePasswordCallback());

        communityUserDAO = new CommunityUserDAO();
    }

    public void writeCredentials(Credential credential) throws CredentialStoreException {

        CertificateCredential certificateCredential = (CertificateCredential) credential;

        Connection connection = null;

        try {

            connection = dbUtil.getConnection();
            // Write community user
            writeCommunityUser(certificateCredential.getCommunityUser(), credential.getToken(), connection);
            // First delete existing credentials
            credentialsDAO.deleteCredentials(certificateCredential.getCommunityUser().getGatewayName(),
                    certificateCredential.getToken(), connection);
            // Add the new certificate
            credentialsDAO.addCredentials(certificateCredential.getCommunityUser().getGatewayName(), credential,
                    connection);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Unable to rollback transaction", e1);
                }
            }
            throw new CredentialStoreException("Unable to retrieve database connection.", e);
        } finally {
            DBUtil.cleanup(connection);
        }

    }

    public void writeCommunityUser(CommunityUser communityUser, String token, Connection connection)
            throws CredentialStoreException {

        // First delete existing community user
        communityUserDAO.deleteCommunityUserByToken(communityUser, token, connection);

        // Persist new community user
        communityUserDAO.addCommunityUser(communityUser, token, connection);

    }

    /*
     * TODO Remove later - If we dont need to expose this in the interface public void writeCommunityUser(CommunityUser
     * communityUser, String token) throws CredentialStoreException {
     * 
     * Connection connection = null; try { connection = dbUtil.getConnection(); writeCommunityUser(communityUser, token,
     * connection);
     * 
     * } catch (SQLException e) { throw new CredentialStoreException("Unable to retrieve database connection.", e); }
     * finally { DBUtil.cleanup(connection); } }
     */
}
