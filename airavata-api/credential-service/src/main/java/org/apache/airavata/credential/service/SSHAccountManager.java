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
package org.apache.airavata.credential.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.airavata.credential.util.SSHUtil;
import org.apache.airavata.interfaces.ConfigParam;
import org.apache.airavata.interfaces.InvalidSetupException;
import org.apache.airavata.interfaces.InvalidUsernameException;
import org.apache.airavata.interfaces.RegistryProvider;
import org.apache.airavata.interfaces.SSHAccountProvisioner;
import org.apache.airavata.interfaces.SSHAccountProvisionerFactory;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.credential.store.proto.PasswordCredential;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SSHAccountManager {

    private static final Logger logger = LoggerFactory.getLogger(SSHAccountManager.class);

    private final RegistryProvider registryProvider;
    private final SSHAccountProvisionerFactory provisionerFactory;
    private final CredentialStoreService credentialStoreService;

    public SSHAccountManager(
            RegistryProvider registryProvider,
            SSHAccountProvisionerFactory provisionerFactory,
            CredentialStoreService credentialStoreService) {
        this.registryProvider = registryProvider;
        this.provisionerFactory = provisionerFactory;
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
        ComputeResourcePreference computeResourcePreference = null;
        try {
            computeResourcePreference =
                    registryProvider.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get ComputeResourcePreference for [" + gatewayId + "] and [" + computeResourceId + "]: "
                            + e.getMessage(),
                    e);
        }

        // get the account provisioner and config values for the preferences
        if (computeResourcePreference.getSshAccountProvisioner().isEmpty()) {
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

        ComputeResourcePreference computeResourcePreference = null;
        ComputeResourceDescription computeResourceDescription = null;
        SSHJobSubmission sshJobSubmission = null;
        try {
            computeResourcePreference =
                    registryProvider.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
            computeResourceDescription = registryProvider.getComputeResource(computeResourceId);
            // Find the SSHJobSubmission
            for (JobSubmissionInterface jobSubmissionInterface :
                    computeResourceDescription.getJobSubmissionInterfacesList()) {
                if (jobSubmissionInterface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
                    sshJobSubmission =
                            registryProvider.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                    break;
                }
            }
        } catch (Exception e) {
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
        if (computeResourcePreference.getSshAccountProvisioner().isEmpty()) {
            throw new InvalidSetupException("Compute resource [" + computeResourceId
                    + "] does not have an SSH Account Provisioner " + "configured for it.");
        }

        // instantiate and init the account provisioner
        SSHAccountProvisioner sshAccountProvisioner = createSshAccountProvisioner(gatewayId, computeResourcePreference);
        boolean canCreateAccount =
                provisionerFactory.canCreateAccount(computeResourcePreference.getSshAccountProvisioner());

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

        UserComputeResourcePreference userComputeResourcePreference = UserComputeResourcePreference.newBuilder()
                .setComputeResourceId(computeResourceId)
                .setLoginUserName(username)
                .setScratchLocation(scratchLocation)
                .setValidated(true)
                .build();
        return userComputeResourcePreference;
    }

    private static String getSSHHostname(
            ComputeResourceDescription computeResourceDescription, SSHJobSubmission sshJobSubmission) {
        String alternativeSSHHostName = sshJobSubmission.getAlternativeSshHostName();
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
                convertConfigParams(provisionerName, computeResourcePreference.getSshAccountProvisionerConfigMap());

        Map<ConfigParam, String> resolvedConfig =
                resolveProvisionerConfig(gatewayId, provisionerName, provisionerConfig);

        // instantiate and init the account provisioner
        return provisionerFactory.createSSHAccountProvisioner(provisionerName, resolvedConfig);
    }

    private Map<ConfigParam, String> resolveProvisionerConfig(
            String gatewayId, String provisionerName, Map<ConfigParam, String> provisionerConfig)
            throws InvalidSetupException {
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
                } catch (Exception e) {
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
        List<ConfigParam> configParams = provisionerFactory.getSSHAccountProvisionerConfigParams(provisionerName);
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
