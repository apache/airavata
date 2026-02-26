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
package org.apache.airavata.protocol.ssh;

import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.protocol.ResourceLookup;
import org.apache.airavata.protocol.StorageResourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"!test", "orchestrator-integration"})
public class SSHJStorageAdapter extends SSHJAgentAdapter implements StorageResourceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SSHJStorageAdapter.class);

    public SSHJStorageAdapter(ResourceLookup resourceLookup, CredentialStoreService credentialService)
            throws AgentException {
        super(resourceLookup, credentialService);
        // Services are inherited from SSHJAgentAdapter or will be injected via Spring
    }

    @Override
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException {
        try {
            logger.info("Initializing Storage Resource SCP Adapter for storage resource : " + storageResourceId
                    + ", gateway : " + gatewayId + ", user " + loginUser + ", token : " + token);

            var resource = resourceLookup.getResource(storageResourceId);
            if (resource == null) {
                throw new AgentException("No resource found for id " + storageResourceId);
            }

            logger.info("Fetching credentials for cred store token " + token);

            var sshCredential = credentialService.getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }

            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            var selectedHostName = resource.getHostName();
            int selectedPort = resource.getPort() == 0 ? 22 : resource.getPort();

            createPoolingSSHJClient(
                    loginUser,
                    selectedHostName,
                    selectedPort,
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase());

        } catch (Exception e) {
            logger.error(
                    "Error while initializing ssh agent for storage resource " + storageResourceId + " to token "
                            + token,
                    e);
            throw new AgentException(
                    "Error while initializing ssh agent for storage resource " + storageResourceId + " to token "
                            + token,
                    e);
        }
    }
}
