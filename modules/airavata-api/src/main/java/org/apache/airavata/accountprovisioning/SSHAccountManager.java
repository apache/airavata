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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.UserComputeResourcePreference;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.springframework.stereotype.Component;

@Component
public class SSHAccountManager {

    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public SSHAccountManager(RegistryService registryService, CredentialStoreService credentialStoreService) {
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
        var sshAccountProvisioner = getSshAccountProvisioner(gatewayId, computeResourceId);

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
        var registryService = this.registryService;
        // get compute resource preferences for the gateway and hostname
        ComputeResourcePreference computeResourcePreference = null;
        try {
            computeResourcePreference =
                    registryService.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            throw new RuntimeException(
                    "Failed to get ComputeResourcePreference for [" + gatewayId + "] and [" + computeResourceId + "]: "
                            + e.getMessage(),
                    e);
        }

        // get the account provisioner and config values for the preferences
        if (computeResourcePreference.getSshAccountProvisioner() == null) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId
                    + "] does not have an SSH Account Provisioner configured for it.");
        }
        return createSshAccountProvisioner(gatewayId, computeResourcePreference);
    }

    public boolean isSSHAccountSetupComplete(
            String gatewayId, String computeResourceId, String userId, SSHCredential sshCredential)
            throws InvalidSetupException, InvalidUsernameException {
        var sshAccountProvisioner = getSshAccountProvisioner(gatewayId, computeResourceId);
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
        var registryService = this.registryService;
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
        } catch (RegistryException e) {
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
        if (computeResourcePreference.getSshAccountProvisioner() == null) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId
                    + "] does not have an SSH Account Provisioner " + "configured for it.");
        }

        // instantiate and init the account provisioner
        var sshAccountProvisioner = createSshAccountProvisioner(gatewayId, computeResourcePreference);
        var canCreateAccount =
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
        var sshHostname = getSSHHostname(computeResourceDescription, sshJobSubmission);
        var sshPort = sshJobSubmission.getSshPort();
        var validated = false;
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
        var scratchLocation = sshAccountProvisioner.getScratchLocation(userId);
        try {
            SSHUtil.execute(sshHostname, sshPort, username, sshCredential, "mkdir -p " + scratchLocation);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create scratch location [" + scratchLocation + "] for user [" + username + "] on host ["
                            + sshHostname + "]: " + e.getMessage(),
                    e);
        }

        var userComputeResourcePreference = new UserComputeResourcePreference();
        userComputeResourcePreference.setComputeResourceId(computeResourceId);
        userComputeResourcePreference.setLoginUserName(username);
        userComputeResourcePreference.setScratchLocation(scratchLocation);
        userComputeResourcePreference.setValidated(true);
        return userComputeResourcePreference;
    }

    private String getSSHHostname(
            ComputeResourceDescription computeResourceDescription, SSHJobSubmission sshJobSubmission) {
        var alternativeSSHHostName = sshJobSubmission.getAlternativeSSHHostName();
        if (alternativeSSHHostName != null && !"".equals(alternativeSSHHostName.trim())) {
            return alternativeSSHHostName;
        } else {
            return computeResourceDescription.getHostName();
        }
    }

    private SSHAccountProvisioner createSshAccountProvisioner(
            String gatewayId, ComputeResourcePreference computeResourcePreference) throws InvalidSetupException {
        var provisionerName = computeResourcePreference.getSshAccountProvisioner();
        var provisionerConfig =
                convertConfigParams(provisionerName, computeResourcePreference.getSshAccountProvisionerConfig());

        var resolvedConfig = resolveProvisionerConfig(gatewayId, provisionerName, provisionerConfig);

        // instantiate and init the account provisioner
        return SSHAccountProvisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);
    }

    private Map<ConfigParam, String> resolveProvisionerConfig(
            String gatewayId, String provisionerName, Map<ConfigParam, String> provisionerConfig)
            throws InvalidSetupException {
        var credentialStoreService = this.credentialStoreService;
        // Resolve any CRED_STORE_PASSWORD_TOKEN config parameters to passwords
        var resolvedConfig = new HashMap<ConfigParam, String>();
        for (Map.Entry<ConfigParam, String> configEntry : provisionerConfig.entrySet()) {
            if (configEntry.getKey().getType() == ConfigParam.ConfigParamType.CRED_STORE_PASSWORD_TOKEN) {
                try {
                    var password = credentialStoreService.getPasswordCredential(configEntry.getValue(), gatewayId);
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

    private Map<ConfigParam, String> convertConfigParams(String provisionerName, Map<String, String> thriftConfigParams)
            throws InvalidSetupException {
        var configParams = SSHAccountProvisionerFactory.getSSHAccountProvisionerConfigParams(provisionerName);
        var configParamMap = configParams.stream().collect(Collectors.toMap(ConfigParam::getName, Function.identity()));

        var result = thriftConfigParams.entrySet().stream()
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
