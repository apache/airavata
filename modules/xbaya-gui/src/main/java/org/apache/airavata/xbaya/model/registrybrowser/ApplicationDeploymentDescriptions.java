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

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry2;

public class ApplicationDeploymentDescriptions {
    private AiravataRegistry2 registry;
    private String serviceName;
    
    public ApplicationDeploymentDescriptions(AiravataRegistry2 registry, String serviceName) {
        setRegistry(registry);
        setServiceName(serviceName);
    }
    
    public ApplicationDeploymentDescriptions(AiravataRegistry2 registry) {
        this(registry,null);
    }

    public AiravataRegistry2 getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry2 registry) {
        this.registry = registry;
    }

    public List<ApplicationDeploymentDescriptionWrap> getDescriptions() throws RegistryException {
        List<ApplicationDeploymentDescriptionWrap> list = new ArrayList<ApplicationDeploymentDescriptionWrap>();
        if (getServiceName()==null) {
        	List<ServiceDescription> serviceDescriptors = getRegistry().getServiceDescriptors();
        	for (ServiceDescription serviceDescription : serviceDescriptors) {
        		String serviceName = serviceDescription.getType().getName();
				Map<String,ApplicationDeploymentDescription> deploymentDescriptions = getRegistry().getApplicationDescriptors(serviceName);
				for (String hostName : deploymentDescriptions.keySet()) {
					ApplicationDeploymentDescription descriptionWrap=deploymentDescriptions.get(hostName);
					list.add(new ApplicationDeploymentDescriptionWrap(getRegistry(), descriptionWrap, serviceName,hostName));
				}
			}
			
		}else{
			Map<String,ApplicationDeploymentDescription> deploymentDescriptions = getRegistry().getApplicationDescriptors(serviceName);
			for (String hostName : deploymentDescriptions.keySet()) {
				ApplicationDeploymentDescription descriptionWrap=deploymentDescriptions.get(hostName);
				list.add(new ApplicationDeploymentDescriptionWrap(getRegistry(), descriptionWrap, getServiceName(),hostName));
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
