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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.AiravataRegistry;

public class ApplicationDeploymentDescriptions {
    private AiravataRegistry registry;
    private String serviceName;
    
    public ApplicationDeploymentDescriptions(AiravataRegistry registry, String serviceName) {
        setRegistry(registry);
        setServiceName(serviceName);
    }
    
    public ApplicationDeploymentDescriptions(AiravataRegistry registry) {
        this(registry,null);
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }

    public List<ApplicationDeploymentDescriptionWrap> getDescriptions() throws RegistryException {
        List<ApplicationDeploymentDescriptionWrap> list = new ArrayList<ApplicationDeploymentDescriptionWrap>();
        if (getServiceName()==null) {
			Map<ApplicationDeploymentDescription, String> deploymentDescriptions = getRegistry()
					.searchDeploymentDescription();
			for (ApplicationDeploymentDescription descriptionWrap : deploymentDescriptions
					.keySet()) {
				String[] descDetails = deploymentDescriptions.get(
						descriptionWrap).split("\\$");
				list.add(new ApplicationDeploymentDescriptionWrap(
						getRegistry(), descriptionWrap, descDetails[0],
						descDetails[1]));
			}
		}else{
	        Map<HostDescription, List<ApplicationDeploymentDescription>> deploymentDescriptions = getRegistry().searchDeploymentDescription(getServiceName());
	        for (HostDescription descriptionWrap : deploymentDescriptions.keySet()) {
	            list.add(new ApplicationDeploymentDescriptionWrap(getRegistry(), deploymentDescriptions.get(descriptionWrap).get(0), getServiceName(),
	            		descriptionWrap.getType().getHostName()));
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
