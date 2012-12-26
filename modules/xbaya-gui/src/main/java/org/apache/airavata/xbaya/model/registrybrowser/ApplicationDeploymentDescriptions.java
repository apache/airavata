/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.xbaya.model.registrybrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class ApplicationDeploymentDescriptions {
//    private AiravataRegistry2 registry;
    private AiravataAPI airavataAPI;
    private String serviceName;
    
//    public ApplicationDeploymentDescriptions(AiravataRegistry2 registry, String serviceName) {
//        setRegistry(registry);
//        setServiceName(serviceName);
//    }


    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public ApplicationDeploymentDescriptions(AiravataAPI airavataAPI, String serviceName) {
        setAiravataAPI(airavataAPI);
        setServiceName(serviceName);
    }
    
//    public ApplicationDeploymentDescriptions(AiravataRegistry2 registry) {
//        this(registry,null);
//    }

    public ApplicationDeploymentDescriptions(AiravataAPI airavataAPI) {
        this(airavataAPI,null);
    }

//    public AiravataRegistry2 getRegistry() {
//        return registry;
//    }
//
//    public void setRegistry(AiravataRegistry2 registry) {
//        this.registry = registry;
//    }

    public List<ApplicationDeploymentDescriptionWrap> getDescriptions() throws RegistryException, AiravataAPIInvocationException {
        List<ApplicationDeploymentDescriptionWrap> list = new ArrayList<ApplicationDeploymentDescriptionWrap>();
        if (getServiceName()==null) {
            ApplicationManager applicationManager = getAiravataAPI().getApplicationManager();
            List<ServiceDescription> serviceDescriptors = applicationManager.getAllServiceDescriptions();
        	for (ServiceDescription serviceDescription : serviceDescriptors) {
        		String serviceName = serviceDescription.getType().getName();
				Map<String,ApplicationDescription> deploymentDescriptions = applicationManager.getApplicationDescriptors(serviceName);
				for (String hostName : deploymentDescriptions.keySet()) {
					ApplicationDescription descriptionWrap=deploymentDescriptions.get(hostName);
					list.add(new ApplicationDeploymentDescriptionWrap(getAiravataAPI(), descriptionWrap, serviceName,hostName));
				}
			}
			
		}else{
			Map<String,ApplicationDescription> deploymentDescriptions = getAiravataAPI().getApplicationManager().getApplicationDescriptors(serviceName);
			for (String hostName : deploymentDescriptions.keySet()) {
				ApplicationDescription descriptionWrap=deploymentDescriptions.get(hostName);
				list.add(new ApplicationDeploymentDescriptionWrap(getAiravataAPI(), descriptionWrap, getServiceName(),hostName));
			}
		}
		return list;
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
