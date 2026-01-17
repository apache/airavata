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
package org.apache.airavata.service.security;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.exception.AuthorizationException;
import org.apache.airavata.common.exception.InvalidRequestException;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.application.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Service for authorization and access control operations.
 */
@Service
@ConditionalOnBean(org.apache.airavata.service.SharingRegistryService.class)
public class AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final AiravataServerProperties properties;
    private final SharingRegistryService sharingRegistryService;
    private final ApplicationService applicationService;

    public AuthorizationService(
            AiravataServerProperties properties,
            SharingRegistryService sharingRegistryService,
            @Qualifier("applicationServiceFacade") ApplicationService applicationService) {
        this.properties = properties;
        this.sharingRegistryService = sharingRegistryService;
        this.applicationService = applicationService;
    }

    private boolean userHasAccess(String gatewayId, String userId, String entityId, String permissionTypeId) {
        try {
            return sharingRegistryService.userHasAccess(gatewayId, userId, entityId, permissionTypeId);
        } catch (Exception e) {
            logger.error("Error checking user access: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates that a user has access to an experiment for reading.
     */
    public void validateExperimentReadAccess(
            AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (username.equals(experimentOwner) && gatewayId.equals(experimentGatewayId)) {
            return; // Owner has access
        }

        if (properties.sharing().enabled()) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } else {
            throw new AuthorizationException("User does not have permission to access this resource");
        }
    }

    /**
     * Validates that a user has access to an experiment for writing.
     */
    public void validateExperimentWriteAccess(
            AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (properties.sharing().enabled()
                && (!username.equals(experimentOwner) || !gatewayId.equals(experimentGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }

    /**
     * Validates that a user has access to a project for reading.
     */
    public void validateProjectReadAccess(
            AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (username.equals(projectOwner) && gatewayId.equals(projectGatewayId)) {
            return; // Owner has access
        }

        if (properties.sharing().enabled()) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }

    /**
     * Validates that a user has access to a project for writing.
     */
    public void validateProjectWriteAccess(
            AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId)
            throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);

        if (properties.sharing().enabled()
                && (!username.equals(projectOwner) || !gatewayId.equals(projectGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }

    /**
     * Validates launch experiment access, checking group resource profile and application deployment permissions.
     */
    public void validateLaunchExperimentAccess(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
            throws InvalidRequestException, AuthorizationException, AiravataSystemException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);

        if (experiment.getUserConfigurationData().getGroupResourceProfileId() == null) {
            throw new InvalidRequestException("Experiment doesn't have groupResourceProfileId");
        }

        // Verify user has READ access to groupResourceProfileId
        if (!userHasAccess(
                gatewayId,
                username + "@" + gatewayId,
                experiment.getUserConfigurationData().getGroupResourceProfileId(),
                gatewayId + ":READ")) {
            throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                    + " doesn't have access to group resource profile "
                    + experiment.getUserConfigurationData().getGroupResourceProfileId());
        }

        // Verify user has READ access to Application Deployment
        final String appInterfaceId = experiment.getExecutionId();
        ApplicationInterfaceDescription applicationInterfaceDescription =
                applicationService.getApplicationInterface(appInterfaceId);

        List<String> appModuleIds = applicationInterfaceDescription.getApplicationModules();
        // Assume that there is only one app module for this interface
        var appModuleId = appModuleIds.get(0);
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions =
                applicationService.getApplicationDeployments(appModuleId);

        if (!experiment.getUserConfigurationData().getAiravataAutoSchedule()) {
            final String resourceHostId = experiment
                    .getUserConfigurationData()
                    .getComputationalResourceScheduling()
                    .getResourceHostId();

            Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                    applicationDeploymentDescriptions.stream()
                            .filter(dep -> dep.getComputeHostId().equals(resourceHostId))
                            .findFirst();
            if (applicationDeploymentDescription.isPresent()) {
                final String appDeploymentId =
                        applicationDeploymentDescription.get().getAppDeploymentId();
                if (!userHasAccess(gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                    throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                            + " doesn't have access to app deployment " + appDeploymentId);
                }
            } else {
                throw new InvalidRequestException("Application deployment doesn't exist for application interface "
                        + appInterfaceId + " and host " + resourceHostId + " in gateway " + gatewayId);
            }
        } else if (experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList() != null
                && !experiment
                        .getUserConfigurationData()
                        .getAutoScheduledCompResourceSchedulingList()
                        .isEmpty()) {
            List<ComputationalResourceSchedulingModel> compResourceSchedulingList =
                    experiment.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList();
            for (ComputationalResourceSchedulingModel crScheduling : compResourceSchedulingList) {
                Optional<ApplicationDeploymentDescription> applicationDeploymentDescription =
                        applicationDeploymentDescriptions.stream()
                                .filter(dep -> dep.getComputeHostId().equals(crScheduling.getResourceHostId()))
                                .findFirst();
                if (applicationDeploymentDescription.isPresent()) {
                    final String appDeploymentId =
                            applicationDeploymentDescription.get().getAppDeploymentId();
                    if (!userHasAccess(gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                }
            }
        }
    }
}
