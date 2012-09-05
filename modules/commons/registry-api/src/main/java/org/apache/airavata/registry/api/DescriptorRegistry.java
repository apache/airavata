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

import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public interface DescriptorRegistry extends AiravataSubRegistry {
	
	/*Note
	 * Name changes of the descriptors should not be allowed
	 */
	
	//---------Host Descriptor data------------
	public void addHostDescriptor(HostDescription descriptor);
	public void updateHostDescriptor(HostDescription descriptor);
	public HostDescription getHostDescriptor(String hostName);
	public void removeHostDescriptor(String hostName);
	public ResourceMetadata getHostDescriptorMetadata(String hostName);

	//---------Service Descriptor data------------
	public void addServiceDescriptor(ServiceDescription descriptor);
	public void updateServiceDescriptor(ServiceDescription descriptor);
	public ServiceDescription getServiceDescriptor(String serviceName);
	public void removeServiceDescriptor(String serviceName);
	public ResourceMetadata getServiceDescriptorMetadata(String serviceName);
	
	//---------Service Descriptor data------------
	public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor);
	public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor);
	public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor);
	public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor);
	public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname);
	public Map<String,ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName);
	public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName);
	public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName);


}
