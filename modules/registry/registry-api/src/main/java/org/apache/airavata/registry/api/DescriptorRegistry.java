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

package org.apache.airavata.registry.api;

import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.registry.api.exception.RegException;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;

public interface DescriptorRegistry extends AiravataSubRegistry {
	
	/*Note
	 * Name changes of the descriptors should not be allowed
	 */
	
	//---------Host Descriptor data------------
	public boolean isHostDescriptorExists(String descriptorName)throws RegException;
	public void addHostDescriptor(HostDescription descriptor) throws DescriptorAlreadyExistsException, RegException;
	public void updateHostDescriptor(HostDescription descriptor)throws DescriptorDoesNotExistsException, RegException;
	public HostDescription getHostDescriptor(String hostName)throws DescriptorDoesNotExistsException,MalformedDescriptorException, RegException;
	public void removeHostDescriptor(String hostName)throws DescriptorDoesNotExistsException, RegException;
	public List<HostDescription> getHostDescriptors()throws MalformedDescriptorException, RegException;
	public ResourceMetadata getHostDescriptorMetadata(String hostName)throws DescriptorDoesNotExistsException, RegException;

	//---------Service Descriptor data------------
	public boolean isServiceDescriptorExists(String descriptorName)throws RegException;
	public void addServiceDescriptor(ServiceDescription descriptor)throws DescriptorAlreadyExistsException, RegException;
	public void updateServiceDescriptor(ServiceDescription descriptor)throws DescriptorDoesNotExistsException, RegException;
	public ServiceDescription getServiceDescriptor(String serviceName)throws DescriptorDoesNotExistsException,MalformedDescriptorException, RegException;
	public void removeServiceDescriptor(String serviceName)throws DescriptorDoesNotExistsException, RegException;
	public List<ServiceDescription> getServiceDescriptors()throws MalformedDescriptorException, RegException;
	public ResourceMetadata getServiceDescriptorMetadata(String serviceName)throws DescriptorDoesNotExistsException, RegException;
	
	//---------Application Descriptor data------------
	public boolean isApplicationDescriptorExists(String serviceName, String hostName, String descriptorName)throws RegException;
	public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDescription descriptor)throws DescriptorAlreadyExistsException, RegException;
	public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDescription descriptor)throws DescriptorAlreadyExistsException, RegException;
	public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDescription descriptor)throws DescriptorDoesNotExistsException, RegException;
	public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDescription descriptor)throws DescriptorDoesNotExistsException, RegException;
	public ApplicationDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName)throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegException;
	public ApplicationDescription getApplicationDescriptors(String serviceName, String hostname)throws MalformedDescriptorException, RegException;
	public Map<String,ApplicationDescription> getApplicationDescriptors(String serviceName)throws MalformedDescriptorException, RegException;
	//public Map<String,ApplicationDescription> getApplicationDescriptorsFromHostName(String hostName)throws MalformedDescriptorException, RegistryException;
	public Map<String[],ApplicationDescription> getApplicationDescriptors()throws MalformedDescriptorException, RegException;
	public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName)throws DescriptorDoesNotExistsException, RegException;
	public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName)throws DescriptorDoesNotExistsException, RegException;


}
