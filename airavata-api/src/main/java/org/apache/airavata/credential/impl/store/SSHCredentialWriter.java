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

import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.services.CredentialEntityService;
import org.apache.airavata.credential.utils.CredentialWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Writes SSH credentials to database.
 */
@Component
public class SSHCredentialWriter implements CredentialWriter {

    protected static Logger logger = LoggerFactory.getLogger(SSHCredentialWriter.class);

    @Autowired
    private CredentialEntityService credentialEntityService;

    public SSHCredentialWriter() {}

    public void writeCredentials(Credential credential) throws CredentialStoreException {
        SSHCredential sshCredential = (SSHCredential) credential;

        // Delete existing credentials and add the new one
        credentialEntityService.deleteCredential(sshCredential.getGateway(), sshCredential.getToken());
        credentialEntityService.saveCredential(sshCredential.getGateway(), credential);
    }
}
