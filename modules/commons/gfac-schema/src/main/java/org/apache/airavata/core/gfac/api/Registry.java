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

package org.apache.airavata.core.gfac.api;

import java.util.List;

import org.apache.airavata.core.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.ServiceDescription;

public interface Registry {
	/**
	 * Find locations where the service is deployed
	 * 
	 * @param serviceName
	 * @return List of HostDescription having the service
	 */
	public List<HostDescription> getServiceLocation(String serviceName);	
	
	/**
	 * Retrieve a service description i.e. name, description, parameters 
	 * 
	 * @param serviceName
	 * @return a service description or null if a description is not found
	 */
	public ServiceDescription getServiceDescription(String serviceName);
	
	/**
	 * Retrieve a deployment description for a service from the specific host
	 * 
	 * @param serviceName
	 * @param hostName
	 * @return a deployment description or null if a description is not found
	 */
	public ApplicationDeploymentDescription getDeploymentDescription(String serviceName, String hostName);
	
	/**
	 * Retrieve a host description
	 * 
	 * @param name
	 * @return a host description or null if a description is not found
	 */
	public HostDescription getHostDescription(String name);
	
	/**
	 * Save a host description with the specific name.
	 * 
	 * @param name
	 * @param host
	 * @return identifier
	 */
	public String saveHostDescription(String name, HostDescription host);
	
	/**
	 * Save a service description with the specific name.
	 * 
	 * @param service
	 * @param host
	 * @return identifier
	 */
	public String saveServiceDescription(String name, ServiceDescription service);
	
	/**
	 * Save a deployment description according to the service and host
	 * 
	 * @param service
	 * @param host
	 * @param app
	 * @return identifier
	 */
	public String saveDeploymentDescription(String service, String host, ApplicationDeploymentDescription app);
	
	/**
	 * Deploy a service on a host
	 * 
	 * @param serviceName
	 * @param hostName
	 * @return true if service can be deploy on the hsot, otherwise false
	 */
	public boolean deployServiceOnHost(String serviceName, String hostName);
		
	/**
	 * Search host description with name
	 * 
	 * @param name
	 * @return the host descriptions with matched name
	 */
	public List<HostDescription> searchHostDescription(String name);
	
	/**
	 * Search service description with name
	 * 
	 * @param name
	 * @return the service descriptions with matched name
	 */
	public List<ServiceDescription> searchServiceDescription(String name);
	
	/**
	 * Search deployment description from a service and/or a hostname
	 *
	 * @param serviceName 
	 * @param hostName
	 * @return the deployment descriptions with matched names
	 */
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName);	
}
