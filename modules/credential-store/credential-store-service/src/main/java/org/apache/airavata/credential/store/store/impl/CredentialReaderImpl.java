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
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateAuditInfo;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.db.CredentialsDAO;
import org.apache.airavata.credential.store.store.CredentialStoreException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Credential store API implementation.
 */
public class CredentialReaderImpl implements CredentialReader, Serializable {

    private CredentialsDAO credentialsDAO;

    private DBUtil dbUtil;

    public CredentialReaderImpl(DBUtil dbUtil) throws ApplicationSettingsException {

        this.credentialsDAO = new CredentialsDAO(ApplicationSettings.getCredentialStoreKeyStorePath(),
                ApplicationSettings.getCredentialStoreKeyAlias(), new DefaultKeyStorePasswordCallback());

        this.dbUtil = dbUtil;
    }

    private Connection getConnection() throws CredentialStoreException {
        try {
            return this.dbUtil.getConnection();
        } catch (SQLException e) {
            throw new CredentialStoreException("Unable to retrieve database connection.", e);
        }
    }

    @Override
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {

        Connection connection = getConnection();

        try {
            return this.credentialsDAO.getCredential(gatewayId, tokenId, connection);
        } finally {
            DBUtil.cleanup(connection);
        }
    }

    public List<Credential> getAllCredentials() throws CredentialStoreException {

        Connection connection = getConnection();

        try {
            return this.credentialsDAO.getCredentials(connection);
        } finally {
            DBUtil.cleanup(connection);
        }

    }

    @Override
    public List<Credential> getAllCredentialsPerGateway(String gatewayId) throws CredentialStoreException {
        Connection connection = getConnection();

        try {
            return this.credentialsDAO.getCredentials(gatewayId, connection);
        } finally {
            DBUtil.cleanup(connection);
        }
    }

    @Override
    public List<Credential> getAllAccessibleCredentialsPerGateway(String gatewayId, List<String> accessibleTokenIds) throws CredentialStoreException {
        Connection connection = getConnection();

        try {
            return this.credentialsDAO.getCredentials(gatewayId, accessibleTokenIds, connection);
        } finally {
            DBUtil.cleanup(connection);
        }
    }

    @Override
    public List<Credential> getAllCredentialsPerUser(String userName) throws CredentialStoreException {
        return null;
    }

    public String getPortalUser(String gatewayName, String tokenId) throws CredentialStoreException {

        Connection connection = getConnection();

        Credential credential;

        try {
            credential = this.credentialsDAO.getCredential(gatewayName, tokenId, connection);

        } finally {
            DBUtil.cleanup(connection);
        }

        return credential.getPortalUserName();
    }

    public CertificateAuditInfo getAuditInfo(String gatewayName, String tokenId) throws CredentialStoreException {

        Connection connection = getConnection();

        CertificateAuditInfo certificateAuditInfo;

        try {

            CertificateCredential certificateCredential = (CertificateCredential) this.credentialsDAO.getCredential(
                    gatewayName, tokenId, connection);

            certificateAuditInfo = new CertificateAuditInfo();

            CommunityUser retrievedUser = certificateCredential.getCommunityUser();
            certificateAuditInfo.setCommunityUserName(retrievedUser.getUserName());
            certificateAuditInfo.setCredentialLifeTime(certificateCredential.getLifeTime());
            certificateAuditInfo.setCredentialsRequestedTime(certificateCredential.getCertificateRequestedTime());
            certificateAuditInfo.setGatewayName(gatewayName);
            certificateAuditInfo.setNotAfter(certificateCredential.getNotAfter());
            certificateAuditInfo.setNotBefore(certificateCredential.getNotBefore());
            certificateAuditInfo.setPortalUserName(certificateCredential.getPortalUserName());

        } finally {
            DBUtil.cleanup(connection);
        }

        return certificateAuditInfo;
    }

    public void updateCommunityUserEmail(String gatewayName, String communityUser, String email)
            throws CredentialStoreException {
        // TODO
    }

    public void removeCredentials(String gatewayName, String tokenId) throws CredentialStoreException {

        Connection connection = getConnection();

        try {
            credentialsDAO.deleteCredentials(gatewayName, tokenId, connection);
        } finally {
            DBUtil.cleanup(connection);
        }

    }

	@Override
	public String getGatewayID(String tokenId) throws CredentialStoreException {
		 Connection connection = getConnection();
	        try {
	            return this.credentialsDAO.getGatewayID(tokenId, connection);
	        } finally {
	            DBUtil.cleanup(connection);
	        }
	}

}
