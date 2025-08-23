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
package org.apache.airavata.credential.store.store.impl;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.repository.CredentialsRepository;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.CredentialWriter;
import org.apache.airavata.credential.store.store.impl.db.CredentialsEntity;
import org.apache.airavata.credential.store.utils.CredentialSerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes SSH credentials to database using JPA repositories.
 */
public class SSHCredentialWriter implements CredentialWriter {

    private CredentialsRepository credentialsRepository;
    private static Logger logger = LoggerFactory.getLogger(SSHCredentialWriter.class);

    public SSHCredentialWriter() throws ApplicationSettingsException {
        this.credentialsRepository = new CredentialsRepository();
    }

    public void writeCredentials(Credential credential) throws CredentialStoreException {
        try {
            SSHCredential sshCredential = (SSHCredential) credential;
            
            // First delete existing credentials
            credentialsRepository.delete(new CredentialsEntity.CredentialsPK(sshCredential.getGateway(), sshCredential.getToken()));
            
            // Create new credentials entity
            CredentialsEntity credentialsEntity = new CredentialsEntity();
            credentialsEntity.setGatewayId(sshCredential.getGateway());
            credentialsEntity.setTokenId(sshCredential.getToken());
            credentialsEntity.setPortalUserId(sshCredential.getPortalUserName());
            credentialsEntity.setTimePersisted(new Timestamp(new Date().getTime()));
            credentialsEntity.setDescription(sshCredential.getDescription());
            credentialsEntity.setCredentialOwnerType(CredentialOwnerType.GATEWAY);
            
            // Serialize and encrypt the credential
            byte[] serializedCredential = CredentialSerializationUtils.serializeCredentialWithEncryption(sshCredential);
            credentialsEntity.setCredential(serializedCredential);
            
            // Save the entity
            credentialsRepository.create(credentialsEntity);
            
        } catch (Exception e) {
            logger.error("Error writing SSH credentials", e);
            throw new CredentialStoreException("Error writing SSH credentials", e);
        }
    }
}
