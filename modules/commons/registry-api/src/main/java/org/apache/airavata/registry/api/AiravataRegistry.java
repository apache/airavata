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

import org.apache.airavata.common.registry.api.Registry;
import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;

public interface AiravataRegistry extends Registry{
    /**
     * Find locations where the service is deployed
     * 
     * @param serviceId
     * @return List of HostDescription having the service
     */
//    public List<HostDescription> getServiceLocation(String serviceId);

    /**
     * Retrieve a service description i.e. name, description, parameters
     * 
     * @param serviceId
     * @return a service description or null if a description is not found
     * @throws ServiceDescriptionRetrieveException
     */
    public ServiceDescription getServiceDescription(String serviceId) throws RegistryException;

    /**
     * Retrieve a deployment description for a service from the specific host
     * 
     * @param serviceId
     * @param hostId
     * @return a deployment description or null if a description is not found
     * @throws DeploymentDescriptionRetrieveException
     */
    public ApplicationDeploymentDescription getDeploymentDescription(String serviceId, String hostId)
            throws RegistryException;

    /**
     * Retrieve a host description
     * 
     * @param hostId
     * @return a host description or null if a description is not found
     */
    public HostDescription getHostDescription(String hostId) throws RegistryException;

    /**
     * Save a host description with the specific name.
     * 
     * @param name
     * @param host
     * @return identifier
     */
    public String saveHostDescription(HostDescription host)throws RegistryException;

    /**
     * Save a service description with the specific name.
     * 
     * @param service
     * @param host
     * @return identifier
     */
    public String saveServiceDescription(ServiceDescription service)throws RegistryException;

    /**
     * Save a deployment description according to the service and host
     * 
     * @param serviceId
     * @param hostId
     * @param app
     * @return identifier
     */
    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app)throws RegistryException;

    /**
     * Deploy a service on a host
     * 
     * @param serviceName
     * @param hostName
     * @return true if service can be deploy on the host, otherwise false
     */
    public boolean deployServiceOnHost(String serviceName, String hostName)throws RegistryException;

    /**
     * Search host description with name
     * 
     * @param name
     * @return the host descriptions with matched name
     * @throws HostDescriptionRetrieveException
     * @throws PathNotFoundException
     */
    public List<HostDescription> searchHostDescription(String name) throws RegistryException;

    /**
     * Search service description with name
     * 
     * @param nameRegEx
     * @return the service descriptions with matched name
     * @throws PathNotFoundException
     * @throws ServiceDescriptionRetrieveException
     */
    public List<ServiceDescription> searchServiceDescription(String nameRegEx) throws RegistryException;

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
            throws RegistryException;

    /**
     * Search deployment description from a service name
     * 
     * @param serviceName
     * @return the deployment descriptions with matched names
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(String serviceName)
            throws RegistryException;
    
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
            String applicationName) throws RegistryException;

    /**
     * Return all deployment descriptions
     * 
     * @return all deployment descriptions on a map with their values containing the service & host as a string
     *         separated by "$"
     * @throws PathNotFoundException
     * @throws DeploymentDescriptionRetrieveException
     */
    public Map<ApplicationDeploymentDescription, String> searchDeploymentDescription() throws RegistryException;

    /**
     * This method can be used to save the gfacURL in to registry
     * 
     * @param gfacURL
     * @return
     */
    public boolean saveGFacDescriptor(String gfacURL)throws RegistryException;

    /**
     * This method can be used to unset the gfacURL from repository resource
     * 
     * @param gfacURL
     * @return
     * @throws RegistryException 
     */
    public boolean deleteGFacDescriptor(String gfacURL) throws RegistryException;

    public List<URI> getInterpreterServiceURLList() throws RegistryException;
    
    public boolean saveInterpreterServiceURL(URI gfacURL)throws RegistryException;

    public boolean deleteInterpreterServiceURL(URI gfacURL) throws RegistryException;
    
    public List<URI> getMessageBoxServiceURLList() throws RegistryException;
    
    public boolean saveMessageBoxServiceURL(URI gfacURL)throws RegistryException;

    public boolean deleteMessageBoxServiceURL(URI gfacURL) throws RegistryException;
    
    public List<URI> getEventingServiceURLList() throws RegistryException;
    
    public boolean saveEventingServiceURL(URI gfacURL)throws RegistryException;

    public boolean deleteEventingServiceURL(URI gfacURL) throws RegistryException;
    
    public List<String> getGFacDescriptorList() throws RegistryException;

    public boolean saveWorkflow(QName ResourceID, String workflowName, String resourceDesc, String workflowAsaString,
            String owner, boolean isMakePublic) throws RegistryException;

    public Map<QName, Node> getWorkflows(String userName) throws RegistryException;

    public Node getWorkflow(QName templateID, String userName) throws RegistryException;

    public boolean deleteWorkflow(QName resourceID, String userName) throws RegistryException;

    public void deleteServiceDescription(String serviceId) throws RegistryException;

    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)
            throws RegistryException;

    public void deleteHostDescription(String hostId) throws RegistryException;

    public boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData workflowInputData) throws RegistryException;

    public boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData workflowOutputData)throws RegistryException;
    
    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;
    
    public String getWorkflowExecutionTemplateName(String experimentId) throws RegistryException;

    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException;
    
    public boolean saveWorkflowExecutionName(String experimentId,String workflowIntanceName)throws RegistryException;
    
    public boolean saveWorkflowExecutionStatus(String experimentId,WorkflowInstanceStatus status)throws RegistryException;
    
    public boolean saveWorkflowExecutionStatus(String experimentId,ExecutionStatus status)throws RegistryException;

    public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)throws RegistryException;

    public boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException;
    
    public boolean saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException;

    public WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException;
    
    public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException;

    public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException;

    public boolean saveWorkflowExecutionUser(String experimentId, String user) throws RegistryException;
    
    public String getWorkflowExecutionUser(String experimentId) throws RegistryException;
    
    public String getWorkflowExecutionName(String experimentId) throws RegistryException;
    
    public WorkflowExecution getWorkflowExecution(String experimentId) throws RegistryException;
    
    public List<String> getWorkflowExecutionIdByUser(String user) throws RegistryException;

    public List<WorkflowExecution> getWorkflowExecutionByUser(String user) throws RegistryException;
    
    public List<WorkflowExecution> getWorkflowExecutionByUser(String user, int pageSize, int pageNo) throws RegistryException;
    
    public String getWorkflowExecutionMetadata(String experimentId) throws RegistryException;
    
    public boolean saveWorkflowExecutionMetadata(String experimentId, String metadata) throws RegistryException;
}
