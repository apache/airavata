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
     *
     * @param serviceId
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ServiceDescription getServiceDescription(String serviceId) throws AiravataAPIInvocationException;

    /**
     *
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> getAllServiceDescriptions() throws AiravataAPIInvocationException;

    /**
     *
     * @param service
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveServiceDescription(ServiceDescription service)throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceId
     * @throws AiravataAPIInvocationException
     */
    public void deleteServiceDescription(String serviceId) throws AiravataAPIInvocationException;

    /**
     *
     * @param nameRegEx
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> searchServiceDescription(String nameRegEx) throws AiravataAPIInvocationException;

    //Application descriptors

    /**
     *
     * @param serviceId
     * @param hostId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public ApplicationDeploymentDescription getDeploymentDescription(String serviceId, String hostId)throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceId
     * @param hostId
     * @param app
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app)throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName)throws AiravataAPIInvocationException;

    /**
     *
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<ApplicationDeploymentDescription, String> getAllDeploymentDescriptions() throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName,String applicationName) throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(String serviceName)throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @throws AiravataAPIInvocationException
     */
    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)throws AiravataAPIInvocationException;
    
    //Host descriptors

    /**
     *
     * @param hostId
     * @return
     * @throws AiravataAPIInvocationException
     */
    public HostDescription getHostDescription(String hostId) throws AiravataAPIInvocationException;

    /**
     *
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> getAllHostDescriptions() throws AiravataAPIInvocationException;

    /**
     *
     * @param host
     * @return
     * @throws AiravataAPIInvocationException
     */
    public String saveHostDescription(HostDescription host)throws AiravataAPIInvocationException;

    /**
     *
     * @param regExName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> searchHostDescription(String regExName) throws AiravataAPIInvocationException;

    /**
     *
     * @param hostId
     * @throws AiravataAPIInvocationException
     */
    public void deleteHostDescription(String hostId) throws AiravataAPIInvocationException;

    /**
     *
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean deployServiceOnHost(String serviceName, String hostName)throws AiravataAPIInvocationException;

}
