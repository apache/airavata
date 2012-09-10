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

package org.apache.airavata.client.api;

import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public interface ApplicationManager {
	//Service descriptors

    /**
     * Retrieve registered service description of the given service name 
     * @param serviceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ServiceDescription getServiceDescription(String serviceName) throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered service descriptions
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> getAllServiceDescriptions() throws AiravataAPIInvocationException;

    /**
     * Save service description on registry
     * @param service
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveServiceDescription(ServiceDescription service)throws AiravataAPIInvocationException;

    /**
     * Delete service description from the registry
     * @param serviceName
     * @throws AiravataAPIInvocationException
     */
    public void deleteServiceDescription(String serviceName) throws AiravataAPIInvocationException;

    /**
     * Retrieve a list of registered service descriptions of the given regex service name
     * @param nameRegEx
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> searchServiceDescription(String nameRegEx) throws AiravataAPIInvocationException;

    //Application descriptors

    /**
     * Retrieve registered application description of the given service name & hostName
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public ApplicationDeploymentDescription getDeploymentDescription(String serviceName, String hostName)throws AiravataAPIInvocationException;

    /**
     * Save deployment description on registry for a given service for a host
     * @param serviceId
     * @param hostId
     * @param app
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app)throws AiravataAPIInvocationException;

    /**
     * Retrieve list of registered deployment descriptions of the given regex service name & regex host name
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName)throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered deployment descriptions, The key represents the service name & host name in string array
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<String[], ApplicationDeploymentDescription> getAllDeploymentDescriptions() throws AiravataAPIInvocationException;

    /**
     * Retrieve list of registered deployment descriptions of the given regex service name, regex host name & regex application name 
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName,String applicationName) throws AiravataAPIInvocationException;

    /**
     * Retrieve registered map of deployment descriptions for their host description of the given service name
     * @param serviceName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(String serviceName)throws AiravataAPIInvocationException;

    /**
     * Delete deployment description from the registry which is exposed as the service name in the host name 
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @throws AiravataAPIInvocationException
     */
    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)throws AiravataAPIInvocationException;
    
    //Host descriptors

    /**
     * Retrieve registered host description of the given host name
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public HostDescription getHostDescription(String hostName) throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered host descriptions
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> getAllHostDescriptions() throws AiravataAPIInvocationException;

    /**
     * Save host description on registry
     * @param host
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveHostDescription(HostDescription host)throws AiravataAPIInvocationException;

    /**
     * Retrieve a list of registered hsot descriptions of the given regex host name
     * @param regExName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> searchHostDescription(String regExName) throws AiravataAPIInvocationException;

    /**
     * Delete host description from the registry
     * @param hostId
     * @throws AiravataAPIInvocationException
     */
    public void deleteHostDescription(String hostId) throws AiravataAPIInvocationException;

    /**
     * Map services to possible hosts 
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean deployServiceOnHost(String serviceName, String hostName)throws AiravataAPIInvocationException;

}
