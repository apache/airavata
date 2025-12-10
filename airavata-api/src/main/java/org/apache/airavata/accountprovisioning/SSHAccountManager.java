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
package org.apache.airavata.accountprovisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.springframework.stereotype.Component;

@Component
public class SSHAccountManager {

    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public SSHAccountManager(
            RegistryService registryService,
            CredentialStoreService credentialStoreService) {
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
    }

    /**
     * Check if user has an SSH account on the compute resource.
     * @param gatewayId
     * @param computeResourceId
     * @param userId Airavata user id
     * @return
     * @throws InvalidSetupException
     * @throws InvalidUsernameException
     */
    public boolean doesUserHaveSSHAccount(String gatewayId, String computeResourceId, String userId)
            throws InvalidSetupException, InvalidUsernameException {
        SSHAccountProvisioner sshAccountProvisioner = getSshAccountProvisioner(gatewayId, computeResourceId);

        try {
            return sshAccountProvisioner.hasAccount(userId);
        } catch (InvalidUsernameException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("hasAccount call failed for userId [" + userId + "]: " + e.getMessage(), e);
        }
    }

    private SSHAccountProvisioner getSshAccountProvisioner(String gatewayId, String computeResourceId)
            throws InvalidSetupException {
        // get registry service
        RegistryService registryService = this.registryService;
        // get compute resource preferences for the gateway and hostname
        ComputeResourcePreference computeResourcePreference = null;
        try {
            computeResourcePreference =
                    registryService.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (RegistryServiceException e) {
            throw new RuntimeException(
                    "Failed to get ComputeResourcePreference for [" + gatewayId + "] and [" + computeResourceId + "]: "
                            + e.getMessage(),
                    e);
        }

        // get the account provisioner and config values for the preferences
        if (!computeResourcePreference.isSetSshAccountProvisioner()) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId
                    + "] does not have an SSH Account Provisioner configured for it.");
        }
        return createSshAccountProvisioner(gatewayId, computeResourcePreference);
    }

    public boolean isSSHAccountSetupComplete(
            String gatewayId, String computeResourceId, String userId, SSHCredential sshCredential)
            throws InvalidSetupException, InvalidUsernameException {
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
    public UserComputeResourcePreference setupSSHAccount(
            String gatewayId, String computeResourceId, String userId, SSHCredential sshCredential)
            throws InvalidSetupException, InvalidUsernameException {

        // get compute resource preferences for the gateway and hostname
        RegistryService registryService = this.registryService;
        ComputeResourcePreference computeResourcePreference = null;
        ComputeResourceDescription computeResourceDescription = null;
        SSHJobSubmission sshJobSubmission = null;
        try {
            computeResourcePreference =
                    registryService.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
            computeResourceDescription = registryService.getComputeResource(computeResourceId);
            // Find the SSHJobSubmission
            for (JobSubmissionInterface jobSubmissionInterface :
                    computeResourceDescription.getJobSubmissionInterfaces()) {
                if (jobSubmissionInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
                    sshJobSubmission =
                            registryService.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                    break;
                }
            }
        } catch (RegistryServiceException e) {
            throw new RuntimeException(
                    "Failed to retrieve compute resource information for [" + gatewayId + "] and " + "["
                            + computeResourceId + "]: " + e.getMessage(),
                    e);
        }

        if (sshJobSubmission == null) {
            throw new InvalidSetupException(
                    "Compute resource [" + computeResourceId + "] does not have an SSH Job Submission " + "interface.");
        }

        // get the account provisioner and config values for the preferences
        if (!computeResourcePreference.isSetSshAccountProvisioner()) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId
                    + "] does not have an SSH Account Provisioner " + "configured for it.");
        }

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = createSshAccountProvisioner(gatewayId, computeResourcePreference);
        boolean canCreateAccount =
                SSHAccountProvisionerFactory.canCreateAccount(computeResourcePreference.getSshAccountProvisioner());

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
            throw new InvalidSetupException("User [" + userId + "] doesn't have account and [" + computeResourceId
                    + "] doesn't " + "have a SSH Account Provisioner that supports creating accounts.");
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
            throw new RuntimeException(
                    "Failed to validate SSH public key installation for account for user [" + username + "] on host ["
                            + sshHostname + "]: " + e.getMessage(),
                    e);
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
            throw new RuntimeException(
                    "Failed to create scratch location [" + scratchLocation + "] for user [" + username + "] on host ["
                            + sshHostname + "]: " + e.getMessage(),
                    e);
        }

        UserComputeResourcePreference userComputeResourcePreference = new UserComputeResourcePreference();
        userComputeResourcePreference.setComputeResourceId(computeResourceId);
        userComputeResourcePreference.setLoginUserName(username);
        userComputeResourcePreference.setScratchLocation(scratchLocation);
        userComputeResourcePreference.setValidated(true);
        return userComputeResourcePreference;
    }

    private String getSSHHostname(
            ComputeResourceDescription computeResourceDescription, SSHJobSubmission sshJobSubmission) {
        String alternativeSSHHostName = sshJobSubmission.getAlternativeSSHHostName();
        if (alternativeSSHHostName != null && !"".equals(alternativeSSHHostName.trim())) {
            return alternativeSSHHostName;
        } else {
            return computeResourceDescription.getHostName();
        }
    }

    private SSHAccountProvisioner createSshAccountProvisioner(
            String gatewayId, ComputeResourcePreference computeResourcePreference) throws InvalidSetupException {
        String provisionerName = computeResourcePreference.getSshAccountProvisioner();
        Map<ConfigParam, String> provisionerConfig =
                convertConfigParams(provisionerName, computeResourcePreference.getSshAccountProvisionerConfig());

        Map<ConfigParam, String> resolvedConfig =
                resolveProvisionerConfig(gatewayId, provisionerName, provisionerConfig);

        // instantiate and init the account provisioner
        return SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);
    }

    private Map<ConfigParam, String> resolveProvisionerConfig(
            String gatewayId, String provisionerName, Map<ConfigParam, String> provisionerConfig)
            throws InvalidSetupException {
        CredentialStoreService credentialStoreService = this.credentialStoreService;
        // Resolve any CRED_STORE_PASSWORD_TOKEN config parameters to passwords
        Map<ConfigParam, String> resolvedConfig = new HashMap<>();
        for (Map.Entry<ConfigParam, String> configEntry : provisionerConfig.entrySet()) {
            if (configEntry.getKey().getType() == ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN) {
                try {
                    PasswordCredential password =
                            credentialStoreService.getPasswordCredential(configEntry.getValue(), gatewayId);
                    if (password == null) {
                        throw new InvalidSetupException("Password credential doesn't exist for config param ["
                                + configEntry.getKey().getName() + "] for token [" + configEntry.getValue()
                                + "] for provisioner [" + provisionerName + "].");
                    }
                    resolvedConfig.put(configEntry.getKey(), password.getPassword());
                } catch (CredentialStoreException e) {
                    throw new RuntimeException("Failed to get password needed to configure " + provisionerName, e);
                }
            } else {
                resolvedConfig.put(configEntry.getKey(), configEntry.getValue());
            }
        }
        return resolvedConfig;
    }

    private Map<ConfigParam, String> convertConfigParams(
            String provisionerName, Map<String, String> thriftConfigParams) throws InvalidSetupException {
        List<ConfigParam> configParams =
                SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams(provisionerName);
        Map<String, ConfigParam> configParamMap =
                configParams.stream().collect(Collectors.toMap(ConfigParam::getName, Function.identity()));

        Map<ConfigParam, String> result = thriftConfigParams.entrySet().stream()
                .collect(Collectors.toMap(entry -> configParamMap.get(entry.getKey()), entry -> entry.getValue()));
        for (ConfigParam configParam : configParams) {
            if (!configParam.isOptional() && !result.containsKey(configParam)) {
                throw new InvalidSetupException("Missing required ConfigParam named [" + configParam.getName()
                        + "] for provisioner [" + provisionerName + "].");
            }
        }
        return result;
    }
}
