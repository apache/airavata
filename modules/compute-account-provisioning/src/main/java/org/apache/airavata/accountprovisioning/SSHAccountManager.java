/*
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

package org.apache.airavata.accountprovisioning;

import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

import java.util.Map;

public class SSHAccountManager {

    // TODO: need private key too to verify
    public static void setupSSHAccount(String gatewayId, String hostname, String username, String sshPublicKey) {

        // TODO: finish implementing

        // get compute resource preferences for the gateway and hostname

        // get the account provisioner and config values for the preferences
        String provisionerName = null;
        Map<ConfigParam,String> provisionerConfig = null;

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, provisionerConfig);

        // First check if username has an account
        boolean hasAccount = sshAccountProvisioner.hasAccount(username);

        if (!hasAccount && !sshAccountProvisioner.canCreateAccount()) {
            // TODO: throw an exception
        }

        // TODO: first check if SSH key is already installed, or do we care?

        // Install SSH key

        // Verify can authenticate to host

        // create the scratch location on the host
        String scratchLocation = sshAccountProvisioner.getScratchLocation(username);
    }

    private RegistryService.Client getRegistryServiceClient() throws RegistryServiceException {

        // TODO: finish implementing
        return RegistryServiceClientFactory.createRegistryClient(null, 0);
    }
}
