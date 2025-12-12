/**
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
package org.apache.airavata.service.security.impl;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.AuthorizationException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.domain.ApplicationService;
import org.apache.airavata.service.security.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of AuthorizationService.
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);
    
    private final AiravataServerProperties properties;
    private final SharingRegistryService sharingRegistryService;
    private final ApplicationService applicationService;
    
    public AuthorizationServiceImpl(
            AiravataServerProperties properties,
            SharingRegistryService sharingRegistryService,
            ApplicationService applicationService) {
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
    
    @Override
    public void validateExperimentReadAccess(AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId) throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        
        if (username.equals(experimentOwner) && gatewayId.equals(experimentGatewayId)) {
            return; // Owner has access
        }
        
        if (properties.services.sharing.enabled) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        } else {
            throw new AuthorizationException("User does not have permission to access this resource");
        }
    }
    
    @Override
    public void validateExperimentWriteAccess(AuthzToken authzToken, String experimentId, String experimentOwner, String experimentGatewayId) throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        
        if (properties.services.sharing.enabled
                && (!username.equals(experimentOwner) || !gatewayId.equals(experimentGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, experimentId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }
    
    @Override
    public void validateProjectReadAccess(AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId) throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        
        if (username.equals(projectOwner) && gatewayId.equals(projectGatewayId)) {
            return; // Owner has access
        }
        
        if (properties.services.sharing.enabled) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":READ")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }
    
    @Override
    public void validateProjectWriteAccess(AuthzToken authzToken, String projectId, String projectOwner, String projectGatewayId) throws AuthorizationException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        String gatewayId = authzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        
        if (properties.services.sharing.enabled
                && (!username.equals(projectOwner) || !gatewayId.equals(projectGatewayId))) {
            String userId = username + "@" + gatewayId;
            if (!userHasAccess(gatewayId, userId, projectId, gatewayId + ":WRITE")) {
                throw new AuthorizationException("User does not have permission to access this resource");
            }
        }
    }
    
    @Override
    public void validateLaunchExperimentAccess(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) throws InvalidRequestException, AuthorizationException, AiravataSystemException {
        String username = authzToken.getClaimsMap().get(Constants.USER_NAME);
        
        // For backwards compatibility, if there is no groupResourceProfileId, look up
        // one that is shared with the user
        if (!experiment.getUserConfigurationData().isSetGroupResourceProfileId()) {
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
        ApplicationInterfaceDescription applicationInterfaceDescription = applicationService.getApplicationInterface(appInterfaceId);
        
        List<String> appModuleIds = applicationInterfaceDescription.getApplicationModules();
        // Assume that there is only one app module for this interface
        var appModuleId = appModuleIds.get(0);
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions =
                applicationService.getApplicationDeployments(appModuleId);
        
        if (!experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
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
                if (!userHasAccess(
                        gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
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
                    if (!userHasAccess(
                            gatewayId, username + "@" + gatewayId, appDeploymentId, gatewayId + ":READ")) {
                        throw new AuthorizationException("User " + username + " in gateway " + gatewayId
                                + " doesn't have access to app deployment " + appDeploymentId);
                    }
                }
            }
        }
    }
}
