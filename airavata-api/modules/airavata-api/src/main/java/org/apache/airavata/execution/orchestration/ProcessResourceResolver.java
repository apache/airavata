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
import java.util.function.Function;
import org.apache.airavata.compute.resource.adapter.ComputeResourceAdapter;
import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.compute.resource.model.ResourceCapabilities;
import org.apache.airavata.core.exception.CoreExceptions.AiravataException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.execution.dag.ScheduleHelper;
import org.apache.airavata.execution.process.ProcessModel;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.application.model.ApplicationDeploymentDescription;
import org.apache.airavata.storage.resource.model.DataMovementProtocol;
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

    public ProcessResourceResolver(
            ApplicationAdapter applicationAdapter,
            ResourceProfileAdapter resourceProfileAdapter,
            ComputeResourceAdapter computeResourceAdapter) {
        this.applicationAdapter = applicationAdapter;
        this.resourceProfileAdapter = resourceProfileAdapter;
        this.computeResourceAdapter = computeResourceAdapter;
    }

    // -------------------------------------------------------------------------
    // Application deployment resolution
    // -------------------------------------------------------------------------

    /**
     * Selects the deployment for the given application on the compute resource associated with
     * the process (picks the first matching host).
     */
    public ApplicationDeploymentDescription getAppDeployment(ProcessModel processModel, String selectedModuleId)
            throws OrchestratorException, RegistryException {

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
        var capabilities = getResourceCapabilities(model);
        if (capabilities.getCompute() == null) {
            throw new OrchestratorException("Compute resource should have compute capabilities defined...");
        }
        return computeResourceAdapter.mapJobSubmissionProtocol(
                capabilities.getCompute().getProtocol());
    }

    public DataMovementProtocol getPreferredDataMovementProtocol(ProcessModel model, String gatewayId)
            throws OrchestratorException {
        var capabilities = getResourceCapabilities(model);
        if (capabilities.getStorage() == null) {
            throw new OrchestratorException("Compute resource should have storage capabilities defined...");
        }
        return computeResourceAdapter.mapDataMovementProtocol(
                capabilities.getStorage().getProtocol());
    }

    public String getLoginUserName(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryException {
        return resolveBindingProperty(
                processModel,
                gatewayId,
                ResourceBindingEntity::getLoginUsername,
                "overrideLoginUserName",
                "Login name");
    }

    public String getScratchLocation(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryException {
        return resolveBindingProperty(
                processModel,
                gatewayId,
                b -> ResourceProfileAdapter.getMetadataString(b.getMetadata(), "scratchLocation"),
                "overrideScratchLocation",
                "Scratch location");
    }

    /**
     * Generic resolution cascade for binding-derived properties.
     *
     * <p>Resolution order when {@code useUserCRPref} is true:
     * user binding → schedule override → group binding → error.
     * Otherwise: schedule override → group binding → error.
     */
    private String resolveBindingProperty(
            ProcessModel processModel,
            String gatewayId,
            Function<ResourceBindingEntity, String> extractor,
            String scheduleKey,
            String propertyName)
            throws AiravataException, RegistryException {

        ResourceBindingEntity binding = resourceProfileAdapter.getBinding(
                processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());
        String bindingValue = binding != null ? extractor.apply(binding) : null;
        String overrideValue = scheduleString(processModel.getResourceSchedule(), scheduleKey);

        if (processModel.getUseUserCRPref()) {
            ResourceBindingEntity userBinding = resourceProfileAdapter.getUserBinding(
                    processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
            String userValue = userBinding != null ? extractor.apply(userBinding) : null;
            if (isValid(userValue)) {
                return userValue;
            } else if (isValid(overrideValue)) {
                logger.warn("{}: user binding empty, falling back to schedule override", propertyName);
                return overrideValue;
            } else if (isValid(bindingValue)) {
                logger.warn("{}: user binding and schedule empty, falling back to group binding", propertyName);
                return bindingValue;
            }
        } else {
            if (isValid(overrideValue)) {
                return overrideValue;
            } else if (isValid(bindingValue)) {
                logger.warn("{}: schedule override empty, falling back to group binding", propertyName);
                return bindingValue;
            }
        }
        throw new AiravataException(propertyName + " is not found");
    }

    public ComputeResourceType getComputeResourceType(ProcessModel model) {
        var resource = computeResourceAdapter.getResource(model.getComputeResourceId());
        if (resource != null
                && resource.getCapabilities() != null
                && resource.getCapabilities().getCompute() != null) {
            return resource.getCapabilities().getCompute().getComputeResourceType();
        }
        return ComputeResourceType.PLAIN;
    }

    private ResourceCapabilities getResourceCapabilities(ProcessModel model) throws OrchestratorException {
        try {
            var resource = computeResourceAdapter.getResource(model.getComputeResourceId());
            if (resource == null || resource.getCapabilities() == null) {
                throw new OrchestratorException(
                        "Compute resource capabilities not defined for " + model.getComputeResourceId());
            }
            return resource.getCapabilities();
        } catch (OrchestratorException e) {
            throw e;
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from application service", e);
        }
    }

    private static String scheduleString(Map<String, Object> schedule, String key) {
        return ScheduleHelper.getString(schedule, key);
    }

    private static boolean isValid(String str) {
        return ScheduleHelper.isValid(str);
    }
}
