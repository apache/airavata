/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.impl.store;

import org.apache.airavata.credential.CommunityUser;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.services.CommunityUserEntityService;
import org.apache.airavata.credential.services.CredentialEntityService;
import org.apache.airavata.credential.utils.CredentialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Writes certificate credentials to database.
 */
@Component
public class CertificateCredentialWriter implements CredentialWriter {

    protected static Logger log = LoggerFactory.getLogger(CertificateCredentialWriter.class);

    @Autowired
    private CredentialEntityService credentialEntityService;

    @Autowired
    private CommunityUserEntityService communityUserEntityService;

    public CertificateCredentialWriter() {}

    public void writeCredentials(Credential credential) throws CredentialStoreException {
        CertificateCredential certificateCredential = (CertificateCredential) credential;

        // Write community user
        writeCommunityUser(certificateCredential.getCommunityUser(), credential.getToken());

        // Delete existing credentials and add the new certificate
        credentialEntityService.deleteCredential(
                certificateCredential.getCommunityUser().getGatewayName(), certificateCredential.getToken());
        credentialEntityService.saveCredential(
                certificateCredential.getCommunityUser().getGatewayName(), credential);
    }

    public void writeCommunityUser(CommunityUser communityUser, String token) throws CredentialStoreException {
        // Delete existing community user and persist new one
        communityUserEntityService.deleteCommunityUserByToken(communityUser, token);
        communityUserEntityService.saveCommunityUser(communityUser, token);
    }
}
