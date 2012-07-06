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

import java.util.List;
import java.util.Map;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public class ApplicationManagerImpl implements ApplicationManager {
	private AiravataClient client;
	
	public ApplicationManagerImpl(AiravataClient client) {
		setClient(client);
	}
	
	@Override
	public ServiceDescription getServiceDescription(String serviceId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getServiceDescription(serviceId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ServiceDescription> getAllServiceDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchServiceDescription(".*");
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveServiceDescription(ServiceDescription service)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().saveServiceDescription(service);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void deleteServiceDescription(String serviceId)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().deleteServiceDescription(serviceId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	@Override
	public List<ServiceDescription> searchServiceDescription(String nameRegEx)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchServiceDescription(nameRegEx);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public ApplicationDeploymentDescription getDeploymentDescription(
			String serviceId, String hostId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getDeploymentDescription(serviceId, hostId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveDeploymentDescription(String serviceId, String hostId,
			ApplicationDeploymentDescription app)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().saveDeploymentDescription(serviceId, hostId, app);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchDeploymentDescription(serviceName, hostName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Map<ApplicationDeploymentDescription, String> getAllDeploymentDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchDeploymentDescription();
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName, String applicationName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchDeploymentDescription(serviceName, hostName, applicationName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(
			String serviceName) throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchDeploymentDescription(serviceName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void deleteDeploymentDescription(String serviceName,
			String hostName, String applicationName)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().deleteDeploymentDescription(serviceName, hostName, applicationName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public HostDescription getHostDescription(String hostId)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().getHostDescription(hostId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<HostDescription> getAllHostDescriptions()
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchHostDescription(".*");
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String saveHostDescription(HostDescription host)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().saveHostDescription(host);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public List<HostDescription> searchHostDescription(String regExName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().searchHostDescription(regExName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public void deleteHostDescription(String hostId)
			throws AiravataAPIInvocationException {
		try {
			getClient().getRegistry().deleteHostDescription(hostId);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public boolean deployServiceOnHost(String serviceName, String hostName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().getRegistry().deployServiceOnHost(serviceName, hostName);
		} catch (RegistryException e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

}
