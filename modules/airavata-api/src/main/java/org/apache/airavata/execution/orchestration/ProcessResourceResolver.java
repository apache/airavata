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
package org.apache.airavata.execution.orchestration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.application.model.ApplicationDeploymentDescription;
import org.apache.airavata.compute.resource.adapter.ComputeResourceAdapter;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.compute.resource.model.SecurityProtocol;
import org.apache.airavata.credential.service.CredentialEntityService;
import org.apache.airavata.core.exception.CoreExceptions.AiravataException;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.storage.resource.model.DataMovementProtocol;
import org.apache.airavata.research.experiment.model.UserConfigurationDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Resolves credentials, application deployments, and compute resource
 * properties for process execution.
 *
 * <p>Encapsulates all resource-lookup concerns: credential-token selection from group resource
 * profiles, application deployment selection for a given compute resource,
 * and compute resource properties such as job submission protocol, data movement protocol,
 * security protocol, login user, scratch location, and resource type.
 */
@Service
public class ProcessResourceResolver {

    private static final Logger logger = LoggerFactory.getLogger(ProcessResourceResolver.class);

    private final ApplicationAdapter applicationAdapter;
    private final ResourceProfileAdapter resourceProfileAdapter;
    private final ComputeResourceAdapter computeResourceAdapter;
    private final CredentialEntityService credentialEntityService;

    public ProcessResourceResolver(
            ApplicationAdapter applicationAdapter,
            ResourceProfileAdapter resourceProfileAdapter,
            ComputeResourceAdapter computeResourceAdapter,
            CredentialEntityService credentialEntityService) {
        this.applicationAdapter = applicationAdapter;
        this.resourceProfileAdapter = resourceProfileAdapter;
        this.computeResourceAdapter = computeResourceAdapter;
        this.credentialEntityService = credentialEntityService;
    }

    // -------------------------------------------------------------------------
    // Credential token resolution
    // -------------------------------------------------------------------------

    /**
     * Resolves the credential store token for the given experiment by consulting the group resource
     * profile and the compute-resource-specific preference.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Resource-specific credential token from the matching compute resource binding.
     *   <li>Default credential token from the gateway-level binding.
     * </ol>
     *
     * @throws OrchestratorException if no group resource profile is configured or no token is found
     */
    public String getCredentialToken(ExperimentModel experiment, UserConfigurationDataModel userConfigurationData)
            throws OrchestratorException, RegistryException {
        String token = null;
        final String groupResourceProfileId = userConfigurationData.getGroupResourceProfileId();
        if (groupResourceProfileId == null) {
            throw new OrchestratorException(
                    "Experiment not configured with a Group Resource Profile: " + experiment.getExperimentId());
        }

        if (userConfigurationData.getComputationalResourceScheduling() != null
                && userConfigurationData.getComputationalResourceScheduling().getResourceHostId() != null) {
            ResourceBindingEntity binding = resourceProfileAdapter.getBinding(
                    userConfigurationData.getComputationalResourceScheduling().getResourceHostId(),
                    groupResourceProfileId);

            if (binding != null && binding.getCredentialId() != null) {
                token = binding.getCredentialId();
            }
        }
        if (token == null || token.isEmpty()) {
            token = resourceProfileAdapter.getGatewayDefaultCredentialToken(groupResourceProfileId);
        }
        if (token == null || token.isEmpty()) {
            throw new OrchestratorException(
                    "You have not configured credential store token at group resource profile or compute resource preference."
                            + " Please provide the correct token at group resource profile or compute resource preference.");
        }
        return token;
    }

    // -------------------------------------------------------------------------
    // Application deployment resolution
    // -------------------------------------------------------------------------

    /**
     * Finds the best-matching {@link ApplicationDeploymentDescription} for the process and
     * application, delegating to {@link #getAppDeploymentForModule}.
     *
     * <p>The {@code applicationId} is treated as an application interface ID; the deployment entity
     * has an FK to the APPLICATION table which stores the interface.
     */
    public ApplicationDeploymentDescription getAppDeployment(ProcessModel processModel, String applicationId)
            throws OrchestratorException, RegistryException {
        return getAppDeploymentForModule(processModel, applicationId);
    }

    /**
     * Selects the deployment for {@code selectedModuleId} on the compute resource associated with
     * the process (picks the first matching host).
     */
    public ApplicationDeploymentDescription getAppDeploymentForModule(
            ProcessModel processModel, String selectedModuleId) throws OrchestratorException, RegistryException {

        List<ApplicationDeploymentDescription> applicationDeployements =
                applicationAdapter.getApplicationDeployments(selectedModuleId);
        Map<String, ApplicationDeploymentDescription> deploymentMap = new HashMap<>();

        for (ApplicationDeploymentDescription deploymentDescription : applicationDeployements) {
            if (processModel.getComputeResourceId().equals(deploymentDescription.getComputeResourceId())) {
                deploymentMap.put(deploymentDescription.getComputeResourceId(), deploymentDescription);
            }
        }
        var hostIds = new ArrayList<>(deploymentMap.keySet());
        String selectedResourceId = hostIds.isEmpty() ? null : hostIds.get(0);
        return deploymentMap.get(selectedResourceId);
    }

    // -------------------------------------------------------------------------
    // Compute resource resolution (merged from ComputeResourceResolver)
    // -------------------------------------------------------------------------

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol(ProcessModel model, String gatewayId)
            throws OrchestratorException {
        try {
            var resource = computeResourceAdapter.getResource(model.getComputeResourceId());
            if (resource == null || resource.getCapabilities() == null
                    || resource.getCapabilities().getCompute() == null) {
                throw new OrchestratorException(
                        "Compute resource should have compute capabilities defined...");
            }
            return computeResourceAdapter.mapJobSubmissionProtocol(
                    resource.getCapabilities().getCompute().getProtocol());
        } catch (OrchestratorException e) {
            throw e;
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app registry", e);
        }
    }

    public String getApplicationInterfaceName(ProcessModel model) throws RegistryException, OrchestratorException {
        var appInterface = applicationAdapter.getApplicationInterface(model.getApplicationInterfaceId());
        return appInterface.getApplicationName();
    }

    public DataMovementProtocol getPreferredDataMovementProtocol(ProcessModel model, String gatewayId)
            throws OrchestratorException {
        try {
            var resource = computeResourceAdapter.getResource(model.getComputeResourceId());
            if (resource == null || resource.getCapabilities() == null
                    || resource.getCapabilities().getStorage() == null) {
                throw new OrchestratorException(
                        "Compute resource should have storage capabilities defined...");
            }
            return computeResourceAdapter.mapDataMovementProtocol(
                    resource.getCapabilities().getStorage().getProtocol());
        } catch (OrchestratorException e) {
            throw e;
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app registry", e);
        }
    }

    public String getLoginUserName(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryException {
        ResourceBindingEntity binding = resourceProfileAdapter.getBinding(
                processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());
        String bindingLogin = binding != null ? binding.getLoginUsername() : null;
        var processResourceSchedule = processModel.getProcessResourceSchedule();
        String overrideLoginUserName = scheduleString(processResourceSchedule, "overrideLoginUserName");
        if (processModel.getUseUserCRPref()) {
            ResourceBindingEntity userBinding = resourceProfileAdapter.getUserBinding(
                    processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
            String userLogin = userBinding != null ? userBinding.getLoginUsername() : null;
            if (isValid(userLogin)) {
                return userLogin;
            } else if (isValid(overrideLoginUserName)) {
                logger.warn("User binding doesn't have valid user login name, using computer "
                        + "resource scheduling login name " + overrideLoginUserName);
                return overrideLoginUserName;
            } else if (isValid(bindingLogin)) {
                logger.warn("Either user binding or resource scheduling "
                        + "doesn't have valid user login name, using group binding login name "
                        + bindingLogin);
                return bindingLogin;
            } else {
                throw new AiravataException("Login name is not found");
            }
        } else {
            if (isValid(overrideLoginUserName)) {
                return overrideLoginUserName;
            } else if (isValid(bindingLogin)) {
                logger.warn("Process compute resource scheduling doesn't have valid user login name, "
                        + "using binding login name " + bindingLogin);
                return bindingLogin;
            } else {
                throw new AiravataException("Login name is not found");
            }
        }
    }

    public String getScratchLocation(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryException {
        ResourceBindingEntity binding = resourceProfileAdapter.getBinding(
                processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());
        String scratchLocation = binding != null
                ? ResourceProfileAdapter.getMetadataString(binding.getMetadata(), "scratchLocation")
                : null;
        var processResourceSchedule = processModel.getProcessResourceSchedule();
        String overrideScratchLocation = scheduleString(processResourceSchedule, "overrideScratchLocation");

        if (processModel.getUseUserCRPref()) {
            ResourceBindingEntity userBinding = resourceProfileAdapter.getUserBinding(
                    processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
            String userScratch = userBinding != null
                    ? ResourceProfileAdapter.getMetadataString(userBinding.getMetadata(), "scratchLocation")
                    : null;
            if (isValid(userScratch)) {
                return userScratch;
            } else if (isValid(overrideScratchLocation)) {
                logger.warn("User binding doesn't have valid scratch location, using computer "
                        + "resource scheduling scratch location " + overrideScratchLocation);
                return overrideScratchLocation;
            } else if (isValid(scratchLocation)) {
                logger.warn("Either user binding or resource scheduling doesn't have "
                        + "valid scratch location, using group binding scratch location "
                        + scratchLocation);
                return scratchLocation;
            } else {
                throw new AiravataException("Scratch location is not found");
            }
        } else {
            if (isValid(overrideScratchLocation)) {
                return overrideScratchLocation;
            } else if (isValid(scratchLocation)) {
                logger.warn("Process compute resource scheduling doesn't have valid scratch location, "
                        + "using binding scratch location " + scratchLocation);
                return scratchLocation;
            } else {
                throw new AiravataException("Scratch location is not found");
            }
        }
    }

    public int getDataMovementPort(ProcessModel processModel, String gatewayId) {
        var resource = computeResourceAdapter.getResource(processModel.getComputeResourceId());
        return resource != null ? resource.getPort() : 0;
    }

    public SecurityProtocol getSecurityProtocol(ProcessModel processModel, String gatewayId) {
        return SecurityProtocol.SSH_KEYS;
    }

    public ComputeResourceType getComputeResourceType(ProcessModel model) {
        var resource = computeResourceAdapter.getResource(model.getComputeResourceId());
        if (resource != null && resource.getCapabilities() != null
                && resource.getCapabilities().getCompute() != null) {
            return resource.getCapabilities().getCompute().getComputeResourceType();
        }
        return ComputeResourceType.PLAIN;
    }

    public CredentialEntityService getCredentialEntityService() {
        return credentialEntityService;
    }

    private static String scheduleString(java.util.Map<String, Object> schedule, String key) {
        if (schedule == null) return null;
        Object val = schedule.get(key);
        return val != null ? val.toString() : null;
    }

    private boolean isValid(String str) {
        return (str != null && !str.trim().isEmpty());
    }
}
