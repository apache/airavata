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

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.xml.namespace.QName;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;

public interface Registry {
    /**
     * Find locations where the service is deployed
     * 
     * @param serviceId
     * @return List of HostDescription having the service
     */
    public List<HostDescription> getServiceLocation(String serviceId);

    /**
     * Retrieve a service description i.e. name, description, parameters
     * 
     * @param serviceId
     * @return a service description or null if a description is not found
     * @throws PathNotFoundException
     * @throws ServiceDescriptionRetrieveException
     */
    public ServiceDescription getServiceDescription(String serviceId) throws ServiceDescriptionRetrieveException,
            PathNotFoundException;

    /**
     * Retrieve a deployment description for a service from the specific host
     * 
     * @param serviceId
     * @param hostId
     * @return a deployment description or null if a description is not found
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public ApplicationDeploymentDescription getDeploymentDescription(String serviceId, String hostId)
            throws DeploymentDescriptionRetrieveException, PathNotFoundException;

    /**
     * Retrieve a host description
     * 
     * @param hostId
     * @return a host description or null if a description is not found
     */
    public HostDescription getHostDescription(String hostId) throws HostDescriptionRetrieveException,
            PathNotFoundException;

    /**
     * Save a host description with the specific name.
     * 
     * @param name
     * @param host
     * @return identifier
     */
    public String saveHostDescription(HostDescription host);

    /**
     * Save a service description with the specific name.
     * 
     * @param service
     * @param host
     * @return identifier
     */
    public String saveServiceDescription(ServiceDescription service);

    /**
     * Save a deployment description according to the service and host
     * 
     * @param serviceId
     * @param hostId
     * @param app
     * @return identifier
     */
    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app);

    /**
     * Deploy a service on a host
     * 
     * @param serviceName
     * @param hostName
     * @return true if service can be deploy on the host, otherwise false
     */
    public boolean deployServiceOnHost(String serviceName, String hostName);

    /**
     * Search host description with name
     * 
     * @param name
     * @return the host descriptions with matched name
     * @throws HostDescriptionRetrieveException
     * @throws PathNotFoundException
     */
    public List<HostDescription> searchHostDescription(String name) throws HostDescriptionRetrieveException,
            PathNotFoundException;

    /**
     * Search service description with name
     * 
     * @param name
     * @return the service descriptions with matched name
     * @throws PathNotFoundException
     * @throws ServiceDescriptionRetrieveException
     */
    public List<ServiceDescription> searchServiceDescription(String name) throws ServiceDescriptionRetrieveException,
            PathNotFoundException;

    /**
     * Search deployment description from a service and/or a hostname
     * 
     * @param serviceName
     * @param hostName
     * @return the deployment descriptions with matched names
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName)
            throws DeploymentDescriptionRetrieveException, PathNotFoundException;

    /**
     * Search deployment description from a service and/or a hostname
     * 
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @return the deployment descriptions with matched service name, host name & reg-ex supported application name
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName,
            String applicationName) throws PathNotFoundException, DeploymentDescriptionRetrieveException;

    /**
     * Return all deployment descriptions
     * 
     * @return all deployment descriptions on a map with their values containing the service & host as a string
     *         separated by "$"
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public Map<ApplicationDeploymentDescription, String> searchDeploymentDescription() throws PathNotFoundException,
            DeploymentDescriptionRetrieveException;

    /**
     * This method can be used to save the gfacURL in to registry
     * 
     * @param gfacURL
     * @return
     */
    public boolean saveGFacDescriptor(String gfacURL);

    /**
     * This method can be used to unset the gfacURL from repository resource
     * 
     * @param gfacURL
     * @return
     */
    public boolean deleteGFacDescriptor(String gfacURL);

    public List<String> getGFacDescriptorList();

    public boolean saveWorkflow(QName ResourceID, String workflowName, String resourceDesc, String workflowAsaString,
            String owner, boolean isMakePublic);

    public Map<QName, Node> getWorkflows(String userName);

    public Node getWorkflow(QName templateID, String userName);

    public boolean deleteWorkflow(QName resourceID, String userName);

    public void deleteServiceDescription(String serviceId) throws ServiceDescriptionRetrieveException,
            PathNotFoundException;

    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)
            throws PathNotFoundException, DeploymentDescriptionRetrieveException;

    public void deleteHostDescription(String hostId) throws HostDescriptionRetrieveException, PathNotFoundException;

    /**
     * Returns the default username that is specified to connect to the repository
     * 
     * @return
     */
    public String getUsername();

    /**
     * Repository uri
     * 
     * @return
     */
    public URI getRepositoryURI();

    /**
     * Returns the repository name
     * 
     * @return
     */
    public String getName();

    public boolean saveWorkflowData(String data,String experimentId,String nodeId);
}
