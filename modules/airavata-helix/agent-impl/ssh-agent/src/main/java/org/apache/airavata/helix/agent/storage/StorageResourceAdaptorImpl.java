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
 */
package org.apache.airavata.helix.agent.storage;

import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.agent.ssh.SshAdaptorParams;
import org.apache.airavata.helix.agent.ssh.SshAgentAdaptor;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageResourceAdaptorImpl extends SshAgentAdaptor implements StorageResourceAdaptor  {

    private final static Logger logger = LoggerFactory.getLogger(StorageResourceAdaptorImpl.class);

    @Override
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException {

        try {
            logger.info("Initializing Storage Resource Adaptor for storage resource : "+ storageResourceId + ", gateway : " +
                    gatewayId +", user " + loginUser + ", token : " + token);
            StorageResourceDescription storageResource = AgentUtils.getRegistryServiceClient().getStorageResource(storageResourceId);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            SshAdaptorParams adaptorParams = new SshAdaptorParams();
            adaptorParams.setHostName(storageResource.getHostName());
            adaptorParams.setUserName(loginUser);
            adaptorParams.setPassphrase(sshCredential.getPassphrase());
            adaptorParams.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            adaptorParams.setPublicKey(sshCredential.getPublicKey().getBytes());
            adaptorParams.setStrictHostKeyChecking(false);
            init(adaptorParams);

        } catch (Exception e) {
            logger.error("Error while initializing ssh agent for storage resource " + storageResourceId + " to token " + token, e);
            throw new AgentException("Error while initializing ssh agent for storage resource " + storageResourceId + " to token " + token, e);
        }
    }

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        super.uploadFile(localFile, remoteFile);
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        super.downloadFile(remoteFile, localFile);
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        return super.executeCommand(command, workingDirectory);
    }
}
