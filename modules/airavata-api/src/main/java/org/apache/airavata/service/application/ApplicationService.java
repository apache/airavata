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
package org.apache.airavata.service.application;

import java.util.List;
import org.apache.airavata.common.exception.AiravataErrorType;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * Service for application interface and deployment management operations.
 */
@Service("applicationServiceFacade")
@ConditionalOnBean(org.apache.airavata.service.registry.RegistryService.class)
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final RegistryService registryService;

    public ApplicationService(RegistryService registryService) {
        this.registryService = registryService;
    }

    private AiravataSystemException airavataSystemException(
            AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(
                errorType, message, cause);
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws AiravataSystemException {
        try {
            return registryService.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws AiravataSystemException {
        try {
            return registryService.getApplicationInterface(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws AiravataSystemException {
        try {
            return registryService.getAllApplicationInterfaces(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all application interfaces: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AiravataSystemException {
        try {
            return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws AiravataSystemException {
        try {
            return registryService.getApplicationDeployment(appDeploymentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws AiravataSystemException {
        try {
            return registryService.getApplicationDeployments(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws AiravataSystemException {
        try {
            return registryService.registerApplicationModule(gatewayId, applicationModule);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public ApplicationModule getApplicationModule(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.getApplicationModule(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllAppModules(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all application modules: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
