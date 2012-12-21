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
import org.apache.airavata.client.api.DescriptorRecordAlreadyExistsException;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;

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
//			throw new AiravataAPIInvocationException(new Exception("Service Description not found in registry."));
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
        return null;
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
    public void addServiceDescription(ServiceDescription serviceDescription) throws AiravataAPIInvocationException,
            DescriptorRecordAlreadyExistsException {
        try {
            getClient().getRegistryClient().addServiceDescriptor(serviceDescription);
        } catch (DescriptorAlreadyExistsException e) {
            throw new DescriptorRecordAlreadyExistsException("Service descriptor "
                    + serviceDescription.getType().getName()
                    + " already exists.", e);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while trying to add service descriptor"
                    + serviceDescription.getType().getName(),
                    e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to " +
                    "add service descriptor" + serviceDescription.getType().getName(), e);
        }
    }

    @Override
    public void updateServiceDescription(ServiceDescription serviceDescription) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateServiceDescriptor(serviceDescription);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while trying to add service descriptor"
                    + serviceDescription.getType().getName(),
                    e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to " +
                    "add service descriptor" + serviceDescription.getType().getName(), e);
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
    public void addDeploymentDescription(ServiceDescription serviceDescription, HostDescription hostDescription,
                                         ApplicationDeploymentDescription applicationDeploymentDescription)
            throws AiravataAPIInvocationException, DescriptorRecordAlreadyExistsException {

        try {
            getClient().getRegistryClient().addApplicationDescriptor(serviceDescription.getType().getName(),
                    hostDescription.getType().getHostName(), applicationDeploymentDescription);
        } catch (DescriptorAlreadyExistsException e) {
            throw new DescriptorRecordAlreadyExistsException("Application descriptor " +
                    applicationDeploymentDescription.getType().getApplicationName().getStringValue()
                    + " already associated to host " + hostDescription.getType().getHostName()
                    + " and service " + serviceDescription.getType().getName(), e);
        } catch (RegistryException e) {

            throw new AiravataAPIInvocationException("An internal error occurred while trying to add " +
                    "application descriptor " +
                    applicationDeploymentDescription.getType().getApplicationName().getStringValue()
                    + " associated to host " + hostDescription.getType().getHostName()
                    + " and service " + serviceDescription.getType().getName(), e);

        } catch (AiravataConfigurationException e) {

            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to add application descriptor " +
                    applicationDeploymentDescription.getType().getApplicationName().getStringValue()
                    + " associated to host " + hostDescription.getType().getHostName()
                    + " and service " + serviceDescription.getType().getName(), e);
        }

    }

    @Override
    public void updateDeploymentDescription(ServiceDescription serviceDescription, HostDescription hostDescription,
                                            ApplicationDeploymentDescription applicationDeploymentDescription)
            throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateApplicationDescriptor(serviceDescription.getType().getName(),
                    hostDescription.getType().getHostName(), applicationDeploymentDescription);
        } catch (RegistryException e) {

            throw new AiravataAPIInvocationException("An internal error occurred while trying to add " +
                    "application descriptor " +
                    applicationDeploymentDescription.getType().getApplicationName().getStringValue()
                    + " associated to host " + hostDescription.getType().getHostName()
                    + " and service " + serviceDescription.getType().getName(), e);

        } catch (AiravataConfigurationException e) {

            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to add application descriptor " +
                    applicationDeploymentDescription.getType().getApplicationName().getStringValue()
                    + " associated to host " + hostDescription.getType().getHostName()
                    + " and service " + serviceDescription.getType().getName(), e);
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
    public void addHostDescription(HostDescription host) throws AiravataAPIInvocationException,
            DescriptorRecordAlreadyExistsException {

        try {
            getClient().getRegistryClient().addHostDescriptor(host);
        } catch (DescriptorAlreadyExistsException e) {
            throw new DescriptorRecordAlreadyExistsException("Host descriptor " + host.getType().getHostName()
                    + " already exists.", e);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while trying to add host descriptor"
                    + host.getType().getHostName(),
                    e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to " +
                    "add host descriptor" + host.getType().getHostName(), e);
        }

    }

    @Override
    public void updateHostDescription(HostDescription host) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateHostDescriptor(host);
        } catch (RegistryException e) {
            throw new AiravataAPIInvocationException("An internal error occurred while trying to add host descriptor"
                    + host.getType().getHostName(),
                    e);
        } catch (AiravataConfigurationException e) {
            throw new AiravataAPIInvocationException("Error retrieving registry controller. " +
                    "An error occurred while trying to " +
                    "add host descriptor" + host.getType().getHostName(), e);
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

    @Override
    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) throws AiravataAPIInvocationException {
        try{
            Map<String, ApplicationDeploymentDescription> applicationDescriptors = getClient().getRegistryClient().getApplicationDescriptors(serviceName);
            return applicationDescriptors;
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public boolean isHostDescriptorExists(String descriptorName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().isHostDescriptorExists(descriptorName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeHostDescriptor(String hostName) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().removeHostDescriptor(hostName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public boolean isServiceDescriptorExists(String descriptorName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().isServiceDescriptorExists(descriptorName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeServiceDescriptor(String serviceName) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().removeServiceDescriptor(serviceName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().removeApplicationDescriptor(serviceName, hostName, applicationName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void updateHostDescriptor(HostDescription descriptor) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateHostDescriptor(descriptor);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void updateServiceDescriptor(ServiceDescription descriptor) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateServiceDescriptor(descriptor);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws AiravataAPIInvocationException {
        try {
            getClient().getRegistryClient().updateApplicationDescriptor(serviceName, hostName, descriptor);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    @Override
    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName) throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().getApplicationDescriptor(serviceName, hostname, applicationName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
    }

    public AiravataClient getClient() {
		return client;
	}

	public void setClient(AiravataClient client) {
		this.client = client;
	}

	@Override
	public boolean isDeploymentDescriptorExists(String serviceName,
			String hostName, String descriptorName)
			throws AiravataAPIInvocationException {
        try {
            return getClient().getRegistryClient().isApplicationDescriptorExists(serviceName, hostName, descriptorName);
        } catch (Exception e) {
            throw new AiravataAPIInvocationException(e);
        }
	}

}
