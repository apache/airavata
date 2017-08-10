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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;

public class SSHAccountManager {

    // TODO: need private key too to verify
    public static void setupSSHAccount(String gatewayId, String hostname, String username, String sshPublicKey) {

        // TODO: finish implementing

        // get compute resource preferences for the gateway and hostname

        // get the account provisioner and config values for the preferences
        String provisionerName = null;
        Map<ConfigParam,String> provisionerConfig = null;

        CredentialStoreService.Client credentialStoreServiceClient = getCredentialStoreClient();
        // Resolve any CRED_STORE_PASSWORD_TOKEN config parameters to passwords
        Map<ConfigParam,String> resolvedConfig = new HashMap<>();
        for (Map.Entry<ConfigParam,String> configEntry : provisionerConfig.entrySet() ) {
            if (configEntry.getKey().getType() == ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN) {
                try {
                    PasswordCredential password = credentialStoreServiceClient.getPasswordCredential(configEntry.getValue(), gatewayId);
                    resolvedConfig.put(configEntry.getKey(), password.getPassword());
                } catch (TException e) {
                    throw new RuntimeException("Failed to get password needed to configure " + provisionerName);
                }
            } else {
                resolvedConfig.put(configEntry.getKey(), configEntry.getValue());
            }
        }

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);

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

    private static RegistryService.Client getRegistryServiceClient() throws RegistryServiceException {

        // TODO: finish implementing
        return RegistryServiceClientFactory.createRegistryClient(null, 0);
    }

    private static CredentialStoreService.Client getCredentialStoreClient() {

        try {
            String credServerHost = ServerSettings.getCredentialStoreServerHost();
            int credServerPort = Integer.valueOf(ServerSettings.getCredentialStoreServerPort());
            return CredentialStoreClientFactory.createAiravataCSClient(null, 0);
        } catch (CredentialStoreException | ApplicationSettingsException e) {
            throw new RuntimeException("Failed to create credential store service client", e);
        }
    }
}
