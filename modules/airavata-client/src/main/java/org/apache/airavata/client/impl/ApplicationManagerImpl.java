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

package org.apache.airavata.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;

public class ApplicationManagerImpl implements ApplicationManager {
	private AiravataClient client;
	
	public ApplicationManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	@Override
	public ServiceDescription getServiceDescription(String serviceId)
			throws AiravataAPIInvocationException {
		try {
			ServiceDescription desc = getClient().getRegistryClient().getServiceDescriptor(serviceId);
			if(desc!=null){
	        	return desc;
	        }
			throw new AiravataAPIInvocationException(new Exception("Service Description not found in registry."));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ServiceDescription> getAllServiceDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getServiceDescriptors();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveServiceDescription(ServiceDescription service)
			throws AiravataAPIInvocationException {
		try {
			if (getClient().getRegistryClient().isServiceDescriptorExists(service.getType().getName())) {
				getClient().getRegistryClient().updateServiceDescriptor(service);
			}else{
				getClient().getRegistryClient().addServiceDescriptor(service);
			}
			return service.getType().getName();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void deleteServiceDescription(String serviceId)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().removeServiceDescriptor(serviceId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	@Override
	public List<ServiceDescription> searchServiceDescription(String nameRegEx)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new UnimplementedRegistryOperationException());
	}

	@Override
	public ApplicationDeploymentDescription getDeploymentDescription(
			String serviceId, String hostId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getApplicationDescriptors(serviceId, hostId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveDeploymentDescription(String serviceId, String hostId,
			ApplicationDeploymentDescription app)
			throws AiravataAPIInvocationException {
		try {
			if (getClient().getRegistryClient().isApplicationDescriptorExists(serviceId, hostId, app.getType().getApplicationName().getStringValue())) {
				getClient().getRegistryClient().updateApplicationDescriptor(serviceId, hostId, app);
			}else{
				getClient().getRegistryClient().addApplicationDescriptor(serviceId, hostId, app);
			}
			return app.getType().getApplicationName().getStringValue();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new UnimplementedRegistryOperationException());
	}

	@Override
	public Map<String[], ApplicationDeploymentDescription> getAllDeploymentDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getApplicationDescriptors();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName, String applicationName)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new UnimplementedRegistryOperationException());
	}

	@Override
	public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(
			String serviceName) throws AiravataAPIInvocationException {
		try {
			Map<HostDescription, List<ApplicationDeploymentDescription>> map=new HashMap<HostDescription, List<ApplicationDeploymentDescription>>();
			Map<String, ApplicationDeploymentDescription> applicationDescriptors = getClient().getRegistryClient().getApplicationDescriptors(serviceName);
			for (String hostName : applicationDescriptors.keySet()) {
				ArrayList<ApplicationDeploymentDescription> list = new ArrayList<ApplicationDeploymentDescription>();
				list.add(applicationDescriptors.get(hostName));
				map.put(getClient().getRegistryClient().getHostDescriptor(hostName),list);
			}
			return map;
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void deleteDeploymentDescription(String serviceName,
			String hostName, String applicationName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().removeApplicationDescriptor(serviceName, hostName, applicationName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public HostDescription getHostDescription(String hostId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getHostDescriptor(hostId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<HostDescription> getAllHostDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistryClient().getHostDescriptors();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveHostDescription(HostDescription host)
			throws AiravataAPIInvocationException {
		try {
			if (getClient().getRegistryClient().isHostDescriptorExists(host.getType().getHostName())) {
				getClient().getRegistryClient().updateHostDescriptor(host);
			}else{
				getClient().getRegistryClient().addHostDescriptor(host);
			}
			return host.getType().getHostName();
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<HostDescription> searchHostDescription(String regExName)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new UnimplementedRegistryOperationException());
	}

	@Override
	public void deleteHostDescription(String hostId)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistryClient().removeHostDescriptor(hostId);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean deployServiceOnHost(String serviceName, String hostName)
			throws AiravataAPIInvocationException {
		throw new AiravataAPIInvocationException(new UnimplementedRegistryOperationException());
	}

	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

}
