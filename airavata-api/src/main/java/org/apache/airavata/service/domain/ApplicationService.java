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
package org.apache.airavata.service.domain;

import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.error.AiravataSystemException;

import java.util.List;

/**
 * Service interface for application interface and deployment management operations.
 */
public interface ApplicationService {
    String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface) throws AiravataSystemException;
    
    ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws AiravataSystemException;
    
    List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws AiravataSystemException;
    
    String registerApplicationDeployment(String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws AiravataSystemException;
    
    ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws AiravataSystemException;
    
    List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws AiravataSystemException;
    
    String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws AiravataSystemException;
    
    ApplicationModule getApplicationModule(String appModuleId) throws AiravataSystemException;
    
    List<ApplicationModule> getAllAppModules(String gatewayId) throws AiravataSystemException;
}
