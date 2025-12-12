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
package org.apache.airavata.service.domain.impl;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.domain.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of ApplicationService.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    
    private final RegistryService registryService;
    
    public ApplicationServiceImpl(RegistryService registryService) {
        this.registryService = registryService;
    }
    
    private AiravataSystemException airavataSystemException(AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(errorType, message, cause);
    }
    
    @Override
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface) throws AiravataSystemException {
        try {
            return registryService.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AiravataSystemException {
        try {
            return registryService.getApplicationInterface(appInterfaceId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application interface: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllApplicationInterfaces(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving all application interfaces: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public String registerApplicationDeployment(String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AiravataSystemException {
        try {
            return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws AiravataSystemException {
        try {
            return registryService.getApplicationDeployment(appDeploymentId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployment: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.getApplicationDeployments(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application deployments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws AiravataSystemException {
        try {
            return registryService.registerApplicationModule(gatewayId, applicationModule);
        } catch (RegistryServiceException e) {
            String msg = "Error while adding application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public ApplicationModule getApplicationModule(String appModuleId) throws AiravataSystemException {
        try {
            return registryService.getApplicationModule(appModuleId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving application module: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
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
