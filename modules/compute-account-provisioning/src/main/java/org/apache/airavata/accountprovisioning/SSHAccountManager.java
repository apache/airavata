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
import org.apache.airavata.model.appcatalog.accountprovisioning.SSHAccountProvisionerConfigParam;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SSHAccountManager {

    // TODO: change return type to one that returns some details of the SSH account setup, for example the scratch location
    public static void setupSSHAccount(String gatewayId, String computeResourceId, String username, SSHCredential sshCredential) {

        // get compute resource preferences for the gateway and hostname
        // TODO: close the registry service client transport when done with it
        RegistryService.Client registryServiceClient = getRegistryServiceClient();
        ComputeResourcePreference computeResourcePreference = null;
        ComputeResourceDescription computeResourceDescription = null;
        try {
            computeResourcePreference = registryServiceClient.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
            computeResourceDescription = registryServiceClient.getComputeResource(computeResourceId);
        } catch(TException e) {
            throw new RuntimeException(e);
        }

        // get the account provisioner and config values for the preferences
        if (!computeResourcePreference.isSetSshAccountProvisioner()) {
            // TODO: provide better exception?
            throw new RuntimeException("Compute resource [" + computeResourceId + "] does not have an SSH Account Provisioner configured for it.");
        }
        String provisionerName = computeResourcePreference.getSshAccountProvisioner();
        Map<ConfigParam,String> provisionerConfig = convertConfigParams(provisionerName, computeResourcePreference.getSshAccountProvisionerConfig());

        Map<ConfigParam, String> resolvedConfig = resolveProvisionerConfig(gatewayId, provisionerName, provisionerConfig);

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);

        // First check if username has an account
        boolean hasAccount = sshAccountProvisioner.hasAccount(username);

        if (!hasAccount && !sshAccountProvisioner.canCreateAccount()) {
            // TODO: provide better exception
            throw new RuntimeException("User [" + username + "] doesn't have account and [" + provisionerName + "] doesn't support creating account.");
        }

        // TODO: first check if SSH key is already installed, or do we care?

        // Install SSH key
        sshAccountProvisioner.installSSHKey(username, sshCredential.getPublicKey());

        // Verify can authenticate to host
        boolean validated = SSHUtil.validate(username, computeResourceDescription.getHostName(), 22, sshCredential);
        if (!validated) {
            throw new RuntimeException("Failed to validate installation of key for [" + username
                    + "] on [" + computeResourceDescription.getHostName() + "] using SSH Account Provisioner ["
                    + computeResourcePreference.getSshAccountProvisioner() + "]");
        }

        // create the scratch location on the host
        // TODO: create the scratch location
        String scratchLocation = sshAccountProvisioner.getScratchLocation(username);
    }

    private static Map<ConfigParam, String> resolveProvisionerConfig(String gatewayId, String provisionerName, Map<ConfigParam, String> provisionerConfig) {
        CredentialStoreService.Client credentialStoreServiceClient = null;
        try {
            credentialStoreServiceClient = getCredentialStoreClient();
            // Resolve any CRED_STORE_PASSWORD_TOKEN config parameters to passwords
            Map<ConfigParam, String> resolvedConfig = new HashMap<>();
            for (Map.Entry<ConfigParam, String> configEntry : provisionerConfig.entrySet()) {
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
            return resolvedConfig;
        } finally {
            if (credentialStoreServiceClient != null) {
                if (credentialStoreServiceClient.getInputProtocol().getTransport().isOpen()) {
                    credentialStoreServiceClient.getInputProtocol().getTransport().close();
                }
            }
        }
    }

    private static Map<ConfigParam, String> convertConfigParams(String provisionerName, Map<SSHAccountProvisionerConfigParam, String> thriftConfigParams) {
        List<ConfigParam> configParams = SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams(provisionerName);
        Map<String, ConfigParam> configParamMap = configParams.stream().collect(Collectors.toMap(ConfigParam::getName, Function.identity()));

        return thriftConfigParams.entrySet().stream().collect(Collectors.toMap(entry -> configParamMap.get(entry.getKey().getName()), entry -> entry.getValue()));
    }

    private static RegistryService.Client getRegistryServiceClient() {

        try {
            String registryServerHost = ServerSettings.getRegistryServerHost();
            int registryServerPort = Integer.valueOf(ServerSettings.getRegistryServerPort());
            return RegistryServiceClientFactory.createRegistryClient(registryServerHost, registryServerPort);
        } catch (ApplicationSettingsException|RegistryServiceException e) {
            throw new RuntimeException("Failed to create registry service client", e);
        }
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
