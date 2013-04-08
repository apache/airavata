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

package org.apache.airavata.credential.store.impl;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.*;
import org.apache.airavata.credential.store.impl.db.CommunityUserDAO;
import org.apache.airavata.credential.store.impl.db.CredentialsDAO;

/**
 * Writes certificate credentials to database.
 */
public class CertificateCredentialWriter implements CredentialWriter {

    private CredentialsDAO credentialsDAO;
    private CommunityUserDAO communityUserDAO;

    public CertificateCredentialWriter(DBUtil dbUtil) {
        credentialsDAO = new CredentialsDAO(dbUtil);
        communityUserDAO = new CommunityUserDAO(dbUtil);
    }

    @Override
    public void writeCredentials(Credential credential) throws CredentialStoreException {

        CertificateCredential certificateCredential = (CertificateCredential)credential;

        // Write community user
        writeCommunityUser(certificateCredential.getCommunityUser());

        // First delete existing credentials
        credentialsDAO.deleteCredentials(certificateCredential.getCommunityUser().getGatewayName(),
                certificateCredential.getCommunityUser().getUserName());

        // Add the new certificate
        CertificateCredential certificateCredentials = (CertificateCredential)credential;
        credentialsDAO.addCredentials(certificateCredentials);
    }

    @Override
    public void writeCommunityUser(CommunityUser communityUser) throws CredentialStoreException {

        // First delete existing community user
        communityUserDAO.deleteCommunityUser(communityUser);

        // Persist new community user
        communityUserDAO.addCommunityUser(communityUser);

    }
}
