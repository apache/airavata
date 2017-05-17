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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.CredentialWriter;
import org.apache.airavata.credential.store.store.impl.db.CredentialsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes SSH credentials to database.
 */
public class SSHCredentialWriter implements CredentialWriter {

    private CredentialsDAO credentialsDAO;
    private DBUtil dbUtil;
    
    protected static Logger logger = LoggerFactory.getLogger(SSHCredentialWriter.class);

    public SSHCredentialWriter(DBUtil dbUtil) throws ApplicationSettingsException {
        this.dbUtil = dbUtil;
        this.credentialsDAO = new CredentialsDAO(ApplicationSettings.getCredentialStoreKeyStorePath(),
                ApplicationSettings.getCredentialStoreKeyAlias(), new DefaultKeyStorePasswordCallback());

    }

    public void writeCredentials(Credential credential) throws CredentialStoreException {

        SSHCredential sshCredential = (SSHCredential) credential;
        Connection connection = null;

        try {
            connection = dbUtil.getConnection();
            // First delete existing credentials
            credentialsDAO.deleteCredentials(sshCredential.getGateway(), sshCredential.getToken(), connection);
            // Add the new certificate
            credentialsDAO.addCredentials(sshCredential.getGateway(), credential, connection);

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    logger.error("Unable to rollback transaction", e1);
                }
            }
            throw new CredentialStoreException("Unable to retrieve database connection.", e);
        } finally {
            DBUtil.cleanup(connection);
        }

    }

}
