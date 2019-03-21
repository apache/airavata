/**
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
package org.apache.airavata.accountprovisioning;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SSHAccountManager {

    private final static Logger logger = LoggerFactory.getLogger(SSHAccountManager.class);

    /**
     * Check if user has an SSH account on the compute resource.
     * @param gatewayId
     * @param computeResourceId
     * @param userId Airavata user id
     * @return
     * @throws InvalidSetupException
     * @throws InvalidUsernameException
     */
    public static boolean doesUserHaveSSHAccount(String gatewayId, String computeResourceId, String userId) throws InvalidSetupException, InvalidUsernameException {
        SSHAccountProvisioner sshAccountProvisioner = getSshAccountProvisioner(gatewayId, computeResourceId);


        try {
            return sshAccountProvisioner.hasAccount(userId);
        } catch (InvalidUsernameException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("hasAccount call failed for userId [" + userId + "]: " + e.getMessage(), e);
        }
    }

    private static SSHAccountProvisioner getSshAccountProvisioner(String gatewayId, String computeResourceId) throws InvalidSetupException {
        // get compute resource preferences for the gateway and hostname
        RegistryService.Client registryServiceClient = getRegistryServiceClient();
        ComputeResourcePreference computeResourcePreference = null;
        try {
            computeResourcePreference = registryServiceClient.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch(TException e) {
            throw new RuntimeException("Failed to get ComputeResourcePreference for [" + gatewayId + "] and [" + computeResourceId + "]: " + e.getMessage(), e);
        } finally {
            if (registryServiceClient.getInputProtocol().getTransport().isOpen()) {
                registryServiceClient.getInputProtocol().getTransport().close();
            }
            if (registryServiceClient.getOutputProtocol().getTransport().isOpen()) {
                registryServiceClient.getOutputProtocol().getTransport().close();
            }
        }

        // get the account provisioner and config values for the preferences
        if (!computeResourcePreference.isSetSshAccountProvisioner()) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId + "] does not have an SSH Account Provisioner configured for it.");
        }
        return createSshAccountProvisioner(gatewayId, computeResourcePreference);
    }

    public static boolean isSSHAccountSetupComplete(String gatewayId, String computeResourceId, String userId, SSHCredential sshCredential) throws InvalidSetupException, InvalidUsernameException {
        SSHAccountProvisioner sshAccountProvisioner = getSshAccountProvisioner(gatewayId, computeResourceId);
        return sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshCredential.getPublicKey());
    }

    /**
     * Add SSH key to compute resource on behalf of user.
     * @param gatewayId
     * @param computeResourceId
     * @param userId Airavata user id
     * @param sshCredential
     * @return a populated but not persisted UserComputeResourcePreference instance
     * @throws InvalidSetupException
     * @throws InvalidUsernameException
     */
    public static UserComputeResourcePreference setupSSHAccount(String gatewayId, String computeResourceId, String userId, SSHCredential sshCredential) throws InvalidSetupException, InvalidUsernameException {

        // get compute resource preferences for the gateway and hostname
        RegistryService.Client registryServiceClient = getRegistryServiceClient();
        ComputeResourcePreference computeResourcePreference = null;
        ComputeResourceDescription computeResourceDescription = null;
        SSHJobSubmission sshJobSubmission = null;
        try {
            computeResourcePreference = registryServiceClient.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
            computeResourceDescription = registryServiceClient.getComputeResource(computeResourceId);
            // Find the SSHJobSubmission
            for (JobSubmissionInterface jobSubmissionInterface : computeResourceDescription.getJobSubmissionInterfaces()) {
                if (jobSubmissionInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
                    sshJobSubmission = registryServiceClient.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                    break;
                }
            }
        } catch(TException e) {
            throw new RuntimeException("Failed to retrieve compute resource information for [" + gatewayId + "] and " +
                    "[" + computeResourceId + "]: " + e.getMessage(), e);
        } finally {
            if (registryServiceClient.getInputProtocol().getTransport().isOpen()) {
                registryServiceClient.getInputProtocol().getTransport().close();
            }
            if (registryServiceClient.getOutputProtocol().getTransport().isOpen()) {
                registryServiceClient.getOutputProtocol().getTransport().close();
            }
        }

        if (sshJobSubmission == null) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId + "] does not have an SSH Job Submission " +
                    "interface.");
        }

        // get the account provisioner and config values for the preferences
        if (!computeResourcePreference.isSetSshAccountProvisioner()) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId + "] does not have an SSH Account Provisioner " +
                    "configured for it.");
        }

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = createSshAccountProvisioner(gatewayId, computeResourcePreference);
        boolean canCreateAccount = SSHAccountProvisionerFactory.canCreateAccount(computeResourcePreference.getSshAccountProvisioner());

        // First check if userId has an account
        boolean hasAccount = false;
        try {
            hasAccount = sshAccountProvisioner.hasAccount(userId);
        } catch (InvalidUsernameException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("hasAccount call failed for userId [" + userId + "]: " + e.getMessage(), e);
        }

        if (!hasAccount && !canCreateAccount) {
            throw new InvalidSetupException("User [" + userId + "] doesn't have account and [" + computeResourceId + "] doesn't " +
                    "have a SSH Account Provisioner that supports creating accounts.");
        }
        // TODO: create account for user if user doesn't have account

        String username = null;
        // Install SSH key
        try {
            username = sshAccountProvisioner.installSSHKey(userId, sshCredential.getPublicKey());
        } catch (InvalidUsernameException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("installSSHKey call failed for userId [" + userId + "]: " + e.getMessage(), e);
        }

        // Verify can authenticate to host
        String sshHostname = getSSHHostname(computeResourceDescription, sshJobSubmission);
        int sshPort = sshJobSubmission.getSshPort();
        boolean validated = false;
        try {
            validated = SSHUtil.validate(sshHostname, sshPort, username, sshCredential);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate SSH public key installation for account for user [" +
                    username + "] on host [" + sshHostname + "]: " + e.getMessage(), e);
        }
        if (!validated) {
            throw new RuntimeException("Failed to validate installation of key for [" + username
                    + "] on [" + computeResourceDescription.getHostName() + "] using SSH Account Provisioner ["
                    + computeResourcePreference.getSshAccountProvisioner() + "]");
        }

        // create the scratch location on the host
        String scratchLocation = sshAccountProvisioner.getScratchLocation(userId);
        try {
            SSHUtil.execute(sshHostname, sshPort, username, sshCredential, "mkdir -p " + scratchLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scratch location [" + scratchLocation + "] for user [" +
                    username + "] on host [" + sshHostname + "]: " + e.getMessage(), e);
        }

        UserComputeResourcePreference userComputeResourcePreference = new UserComputeResourcePreference();
        userComputeResourcePreference.setComputeResourceId(computeResourceId);
        userComputeResourcePreference.setLoginUserName(username);
        userComputeResourcePreference.setScratchLocation(scratchLocation);
        userComputeResourcePreference.setValidated(true);
        return userComputeResourcePreference;
    }

    private static String getSSHHostname(ComputeResourceDescription computeResourceDescription, SSHJobSubmission sshJobSubmission) {
        String alternativeSSHHostName = sshJobSubmission.getAlternativeSSHHostName();
        if (alternativeSSHHostName != null && !"".equals(alternativeSSHHostName.trim())) {
            return alternativeSSHHostName;
        } else {
            return computeResourceDescription.getHostName();
        }
    }

    private static SSHAccountProvisioner createSshAccountProvisioner(String gatewayId, ComputeResourcePreference computeResourcePreference) throws InvalidSetupException {
        String provisionerName = computeResourcePreference.getSshAccountProvisioner();
        Map<ConfigParam,String> provisionerConfig = convertConfigParams(provisionerName, computeResourcePreference.getSshAccountProvisionerConfig());

        Map<ConfigParam, String> resolvedConfig = resolveProvisionerConfig(gatewayId, provisionerName, provisionerConfig);

        // instantiate and init the account provisioner
        return SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);
    }

    private static Map<ConfigParam, String> resolveProvisionerConfig(String gatewayId, String provisionerName, Map<ConfigParam, String> provisionerConfig) throws InvalidSetupException {
        CredentialStoreService.Client credentialStoreServiceClient = null;
        try {
            credentialStoreServiceClient = getCredentialStoreClient();
            // Resolve any CRED_STORE_PASSWORD_TOKEN config parameters to passwords
            Map<ConfigParam, String> resolvedConfig = new HashMap<>();
            for (Map.Entry<ConfigParam, String> configEntry : provisionerConfig.entrySet()) {
                if (configEntry.getKey().getType() == ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN) {
                    try {
                        PasswordCredential password = credentialStoreServiceClient.getPasswordCredential(configEntry.getValue(), gatewayId);
                        if (password == null) {
                            throw new InvalidSetupException("Password credential doesn't exist for config param ["
                                    + configEntry.getKey().getName() + "] for token [" + configEntry.getValue() + "] for provisioner [" + provisionerName + "].");
                        }
                        resolvedConfig.put(configEntry.getKey(), password.getPassword());
                    } catch (TException e) {
                        throw new RuntimeException("Failed to get password needed to configure " + provisionerName, e);
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
                if (credentialStoreServiceClient.getOutputProtocol().getTransport().isOpen()) {
                    credentialStoreServiceClient.getOutputProtocol().getTransport().close();
                }
            }
        }
    }

    private static Map<ConfigParam, String> convertConfigParams(String provisionerName, Map<String, String> thriftConfigParams) throws InvalidSetupException {
        List<ConfigParam> configParams = SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams(provisionerName);
        Map<String, ConfigParam> configParamMap = configParams.stream().collect(Collectors.toMap(ConfigParam::getName, Function.identity()));

        Map<ConfigParam, String> result = thriftConfigParams.entrySet().stream().collect(Collectors.toMap(entry -> configParamMap.get(entry.getKey()), entry -> entry.getValue()));
        for (ConfigParam configParam : configParams) {
            if (!configParam.isOptional() && !result.containsKey(configParam)) {
                throw new InvalidSetupException("Missing required ConfigParam named [" + configParam.getName() + "] for provisioner [" + provisionerName + "].");
            }
        }
        return result;
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
            return CredentialStoreClientFactory.createAiravataCSClient(credServerHost, credServerPort);
        } catch (CredentialStoreException | ApplicationSettingsException e) {
            throw new RuntimeException("Failed to create credential store service client", e);
        }
    }
}
