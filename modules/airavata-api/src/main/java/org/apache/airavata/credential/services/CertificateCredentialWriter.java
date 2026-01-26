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
package org.apache.airavata.credential.services;

import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CredentialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Writes certificate credentials to database.
 * User info is stored via the userId field in the credential.
 */
@Component
public class CertificateCredentialWriter implements CredentialWriter {

    protected static Logger log = LoggerFactory.getLogger(CertificateCredentialWriter.class);

    private final CredentialEntityService credentialEntityService;

    public CertificateCredentialWriter(CredentialEntityService credentialEntityService) {
        this.credentialEntityService = credentialEntityService;
    }

    public void writeCredentials(Credential credential) throws CredentialStoreException {
        var certificateCredential = (CertificateCredential) credential;

        // Delete existing credentials and add the new certificate
        credentialEntityService.deleteCredential(certificateCredential.getGatewayId(), certificateCredential.getToken());

        // Save credential - userId is already set on the credential object
        credentialEntityService.saveCredential(certificateCredential.getGatewayId(), credential);
    }
}
