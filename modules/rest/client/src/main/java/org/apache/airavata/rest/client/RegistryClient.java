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

package org.apache.airavata.rest.client;

import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.*;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.gateway.*;
import org.apache.airavata.registry.api.exception.worker.*;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.PasswordCallback;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RegistryClient extends AiravataRegistry2 {

    private URI connectionURI;
    private PasswordCallback callback;
    
    private BasicRegistryResourceClient basicRegistryResourceClient;
    private ConfigurationResourceClient configurationResourceClient;
    private DescriptorResourceClient descriptorResourceClient;
    private ExperimentResourceClient experimentResourceClient;
    private ProjectResourceClient projectResourceClient;
    private ProvenanceResourceClient provenanceResourceClient;
    private PublishedWorkflowResourceClient publishedWorkflowResourceClient;
    private UserWorkflowResourceClient userWorkflowResourceClient;

    public RegistryClient(String userName, PasswordCallback callback) {
        basicRegistryResourceClient = new BasicRegistryResourceClient(userName, callback);
        configurationResourceClient = new ConfigurationResourceClient(userName, callback);
        descriptorResourceClient = new DescriptorResourceClient(userName, callback);
        experimentResourceClient = new ExperimentResourceClient(userName, callback);
        projectResourceClient = new ProjectResourceClient(userName, callback);
        provenanceResourceClient = new ProvenanceResourceClient(userName, callback);
        publishedWorkflowResourceClient = new PublishedWorkflowResourceClient(userName, callback);
        userWorkflowResourceClient = new UserWorkflowResourceClient(userName, callback);
    }

    public BasicRegistryResourceClient getBasicRegistryResourceClient() {
        return basicRegistryResourceClient;
    }

    public ConfigurationResourceClient getConfigurationResourceClient() {
        return configurationResourceClient;
    }

    public DescriptorResourceClient getDescriptorResourceClient() {
        return descriptorResourceClient;
    }

    public ExperimentResourceClient getExperimentResourceClient() {
        return experimentResourceClient;
    }

    public ProjectResourceClient getProjectResourceClient() {
        return projectResourceClient;
    }

    public ProvenanceResourceClient getProvenanceResourceClient() {
        return provenanceResourceClient;
    }

    public PublishedWorkflowResourceClient getPublishedWorkflowResourceClient() {
        return publishedWorkflowResourceClient;
    }

    public UserWorkflowResourceClient getUserWorkflowResourceClient() {
        return userWorkflowResourceClient;
    }

    public void setBasicRegistryResourceClient(BasicRegistryResourceClient basicRegistryResourceClient) {
        this.basicRegistryResourceClient = basicRegistryResourceClient;
    }

    public void setConfigurationResourceClient(ConfigurationResourceClient configurationResourceClient) {
        this.configurationResourceClient = configurationResourceClient;
    }

    public void setDescriptorResourceClient(DescriptorResourceClient descriptorResourceClient) {
        this.descriptorResourceClient = descriptorResourceClient;
    }

    public void setExperimentResourceClient(ExperimentResourceClient experimentResourceClient) {
        this.experimentResourceClient = experimentResourceClient;
    }

    public void setProjectResourceClient(ProjectResourceClient projectResourceClient) {
        this.projectResourceClient = projectResourceClient;
    }

    public void setProvenanceResourceClient(ProvenanceResourceClient provenanceResourceClient) {
        this.provenanceResourceClient = provenanceResourceClient;
    }

    public void setPublishedWorkflowResourceClient(PublishedWorkflowResourceClient publishedWorkflowResourceClient) {
        this.publishedWorkflowResourceClient = publishedWorkflowResourceClient;
    }

    public void setUserWorkflowResourceClient(UserWorkflowResourceClient userWorkflowResourceClient) {
        this.userWorkflowResourceClient = userWorkflowResourceClient;
    }

    @Override
    protected void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getConfiguration(String key) {
       return getConfigurationResourceClient().getConfiguration(key);
    }

    @Override
    public List<Object> getConfigurationList(String key) {
        return getConfigurationResourceClient().getConfigurationList(key);
    }

    @Override
    public void setConfiguration(String key, String value, Date expire) {
        getConfigurationResourceClient().setConfiguration(key, value, expire);
    }

    @Override
    public void addConfiguration(String key, String value, Date expire) {
        getConfigurationResourceClient().addConfiguration(key, value, expire);
    }

    @Override
    public void removeAllConfiguration(String key) {
        getConfigurationResourceClient().removeAllConfiguration(key);
    }

    @Override
    public void removeConfiguration(String key, String value) {
        getConfigurationResourceClient().removeConfiguration(key, value);
    }

    @Override
    public List<URI> getGFacURIs() {
        return getConfigurationResourceClient().getGFacURIs();
    }

    @Override
    public List<URI> getWorkflowInterpreterURIs() {
        return getConfigurationResourceClient().getWorkflowInterpreterURIs();
    }

    @Override
    public URI getEventingServiceURI() {
        return getConfigurationResourceClient().getEventingURI();
    }

    @Override
    public URI getMessageBoxURI() {
        return getConfigurationResourceClient().getMsgBoxURI();
    }

    @Override
    public void addGFacURI(URI uri) {
        getConfigurationResourceClient().addGFacURI(uri);
    }

    @Override
    public void addWorkflowInterpreterURI(URI uri) {
        getConfigurationResourceClient().addWFInterpreterURI(uri);
    }

    @Override
    public void setEventingURI(URI uri) {
        getConfigurationResourceClient().setEventingURI(uri);
    }

    @Override
    public void setMessageBoxURI(URI uri) {
        getConfigurationResourceClient().setMessageBoxURI(uri);
    }

    @Override
    public void addGFacURI(URI uri, Date expire) {
        getConfigurationResourceClient().addGFacURIByDate(uri, expire);
    }

    @Override
    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        getConfigurationResourceClient().addWorkflowInterpreterURI(uri, expire);
    }

    @Override
    public void setEventingURI(URI uri, Date expire) {
        getConfigurationResourceClient().setEventingURIByDate(uri, expire);
    }

    @Override
    public void setMessageBoxURI(URI uri, Date expire) {
        getConfigurationResourceClient().setMessageBoxURIByDate(uri, expire);
    }

    @Override
    public void removeGFacURI(URI uri) {
        getConfigurationResourceClient().removeGFacURI(uri);
    }

    @Override
    public void removeAllGFacURI() {
        getConfigurationResourceClient().removeAllGFacURI();
    }

    @Override
    public void removeWorkflowInterpreterURI(URI uri) {
        getConfigurationResourceClient().removeWorkflowInterpreterURI(uri);
    }

    @Override
    public void removeAllWorkflowInterpreterURI() {
        getConfigurationResourceClient().removeAllWorkflowInterpreterURI();
    }

    @Override
    public void unsetEventingURI() {
        getConfigurationResourceClient().unsetEventingURI();
    }

    @Override
    public void unsetMessageBoxURI() {
        getConfigurationResourceClient().unsetMessageBoxURI();
    }

    @Override
    public boolean isHostDescriptorExists(String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isHostDescriptorExists(descriptorName);
    }

    @Override
    public void addHostDescriptor(HostDescription descriptor) throws DescriptorAlreadyExistsException, RegistryException {
        getDescriptorResourceClient().addHostDescriptor(descriptor);
    }

    @Override
    public void updateHostDescriptor(HostDescription descriptor) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().updateHostDescriptor(descriptor);
    }

    @Override
    public HostDescription getHostDescriptor(String hostName) throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getHostDescriptor(hostName);
    }

    @Override
    public void removeHostDescriptor(String hostName) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().removeHostDescriptor(hostName);
    }

    @Override
    public List<HostDescription> getHostDescriptors() throws MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getHostDescriptors();
    }

    @Override
    public ResourceMetadata getHostDescriptorMetadata(String hostName) throws DescriptorDoesNotExistsException, RegistryException {
        return null;
    }

    @Override
    public boolean isServiceDescriptorExists(String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isServiceDescriptorExists(descriptorName);
    }

    @Override
    public void addServiceDescriptor(ServiceDescription descriptor) throws DescriptorAlreadyExistsException, RegistryException {
       getDescriptorResourceClient().addServiceDescriptor(descriptor);
    }

    @Override
    public void updateServiceDescriptor(ServiceDescription descriptor) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().updateServiceDescriptor(descriptor);
    }

    @Override
    public ServiceDescription getServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getServiceDescriptor(serviceName);
    }

    @Override
    public void removeServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().removeServiceDescriptor(serviceName);
    }

    @Override
    public List<ServiceDescription> getServiceDescriptors() throws MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getServiceDescriptors();
    }

    @Override
    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) throws DescriptorDoesNotExistsException, RegistryException {
        return null;
    }

    @Override
    public boolean isApplicationDescriptorExists(String serviceName, String hostName, String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isApplicationDescriptorExists(serviceName, hostName, descriptorName);
    }

    @Override
    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws DescriptorAlreadyExistsException, RegistryException {
        getDescriptorResourceClient().addApplicationDescriptor(serviceDescription, hostDescriptor, descriptor);
    }

    @Override
    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws DescriptorAlreadyExistsException, RegistryException {
        getDescriptorResourceClient().addApplicationDescriptor(serviceName, hostName, descriptor);
    }

    @Override
    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().udpateApplicationDescriptor(serviceDescription, hostDescriptor, descriptor);
    }

    @Override
    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().updateApplicationDescriptor(serviceName, hostName, descriptor);
    }

    @Override
    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName) throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptor(serviceName, hostname, applicationName);
    }

    @Override
    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) throws MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors(serviceName, hostname);
    }

    @Override
    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) throws MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors(serviceName);
    }

    @Override
    public Map<String[], ApplicationDeploymentDescription> getApplicationDescriptors() throws MalformedDescriptorException, RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors();
    }

    @Override
    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws DescriptorDoesNotExistsException, RegistryException {
        getDescriptorResourceClient().removeApplicationDescriptor(serviceName, hostName, applicationName);
    }

    @Override
    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) throws DescriptorDoesNotExistsException, RegistryException {
        return null;
    }

    @Override
    public boolean isWorkspaceProjectExists(String projectName) throws RegistryException {
        return getProjectResourceClient().isWorkspaceProjectExists(projectName);
    }

    @Override
    public boolean isWorkspaceProjectExists(String projectName, boolean createIfNotExists) throws RegistryException {
        return getProjectResourceClient().isWorkspaceProjectExists(projectName, createIfNotExists);
    }

    @Override
    public void addWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectAlreadyExistsException, RegistryException {
        getProjectResourceClient().addWorkspaceProject(project);
    }

    @Override
    public void updateWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectDoesNotExistsException, RegistryException {
        getProjectResourceClient().updateWorkspaceProject(project);
    }

    @Override
    public void deleteWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException {
        getProjectResourceClient().deleteWorkspaceProject(projectName);
    }

    @Override
    public WorkspaceProject getWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException {
        return getProjectResourceClient().getWorkspaceProject(projectName);
    }

    @Override
    public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException {
        return getProjectResourceClient().getWorkspaceProjects();
    }

    @Override
    public void addExperiment(String projectName, AiravataExperiment experiment) throws WorkspaceProjectDoesNotExistsException, ExperimentDoesNotExistsException, RegistryException {
        getExperimentResourceClient().addExperiment(projectName, experiment);
    }

    @Override
    public void removeExperiment(String experimentId) throws ExperimentDoesNotExistsException {
        getExperimentResourceClient().removeExperiment(experimentId);
    }

    @Override
    public List<AiravataExperiment> getExperiments() throws RegistryException {
        return getExperimentResourceClient().getExperiments();
    }

    @Override
    public List<AiravataExperiment> getExperiments(String projectName) throws RegistryException {
        return getExperimentResourceClient().getExperiments(projectName);
    }

    @Override
    public List<AiravataExperiment> getExperiments(Date from, Date to) throws RegistryException {
        return getExperimentResourceClient().getExperiments(from, to);
    }

    @Override
    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) throws RegistryException {
        return getExperimentResourceClient().getExperiments(projectName, from, to);
    }

    @Override
    public boolean isExperimentExists(String experimentId) throws RegistryException {
        return getExperimentResourceClient().isExperimentExists(experimentId);
    }

    @Override
    public boolean isExperimentExists(String experimentId, boolean createIfNotPresent) throws RegistryException {
        return getExperimentResourceClient().isExperimentExists(experimentId, createIfNotPresent);
    }

    @Override
    public void updateExperimentExecutionUser(String experimentId, String user) throws RegistryException {
        getProvenanceResourceClient().updateExperimentExecutionUser(experimentId, user);
    }

    @Override
    public String getExperimentExecutionUser(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentExecutionUser(experimentId);
    }

    @Override
    public boolean isExperimentNameExist(String experimentName) throws RegistryException {
        return getProvenanceResourceClient().isExperimentNameExist(experimentName);
    }

    @Override
    public String getExperimentName(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentName(experimentId);
    }

    @Override
    public void updateExperimentName(String experimentId, String experimentName) throws RegistryException {
        getProvenanceResourceClient().updateExperimentName(experimentId, experimentName);
    }

    @Override
    public String getExperimentMetadata(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentMetadata(experimentId);
    }

    @Override
    public void updateExperimentMetadata(String experimentId, String metadata) throws RegistryException {
        getProvenanceResourceClient().updateExperimentMetadata(experimentId, metadata);
    }

    @Override
    public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionTemplateName(workflowInstanceId);
    }

    @Override
    public void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName) throws RegistryException {
        getProvenanceResourceClient().setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
    }

    @Override
    public List<WorkflowInstance> getExperimentWorkflowInstances(String experimentId) throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWorkflowInstanceExists(String instanceId) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceExists(instanceId);
    }

    @Override
    public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceExists(instanceId, createIfNotPresent);
    }

    @Override
    public void updateWorkflowInstanceStatus(String instanceId, WorkflowInstanceStatus.ExecutionStatus status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowInstanceStatus(instanceId, status);
    }

    @Override
    public void updateWorkflowInstanceStatus(WorkflowInstanceStatus status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowInstanceStatus(status);
    }

    @Override
    public WorkflowInstanceStatus getWorkflowInstanceStatus(String instanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceStatus(instanceId);
    }

    @Override
    public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeInput(node, data);
    }

    @Override
    public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeOutput(node, data);
    }

    @Override
    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx) throws RegistryException {
        return getProvenanceResourceClient().searchWorkflowInstanceNodeInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    }

    @Override
    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx) throws RegistryException {
        return getProvenanceResourceClient().searchWorkflowInstanceNodeOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    }

    @Override
    public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(String workflowInstanceId, String nodeType) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeInput(workflowInstanceId, nodeType);
    }

    @Override
    public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(String workflowInstanceId, String nodeType) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeOutput(workflowInstanceId, nodeType);
    }

    @Override
    public void saveWorkflowExecutionOutput(String experimentId, String outputNodeName, String output) throws RegistryException {
        getProvenanceResourceClient().saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
    }

    @Override
    public void saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException {
        getProvenanceResourceClient().saveWorkflowExecutionOutput(experimentId, data);
    }

    @Override
    public WorkflowIOData getWorkflowExecutionOutput(String experimentId, String outputNodeName) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutput(experimentId, outputNodeName);
    }

    @Override
    public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutput(experimentId);
    }

    @Override
    public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutputNames(exeperimentId);
    }

    @Override
    public ExperimentData getExperiment(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperiment(experimentId);
    }

    @Override
    public ExperimentData getExperimentMetaInformation(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentMetaInformation(experimentId);
    }

    @Override
    public List<ExperimentData> getAllExperimentMetaInformation(String user) throws RegistryException {
        return getProvenanceResourceClient().getAllExperimentMetaInformation(user);
    }

    @Override
    public List<ExperimentData> searchExperiments(String user, String experimentNameRegex) throws RegistryException {
        return getProvenanceResourceClient().searchExperiments(user, experimentNameRegex);
    }

    @Override
    public List<String> getExperimentIdByUser(String user) throws RegistryException {
        return getProvenanceResourceClient().getExperimentIdByUser(user);
    }

    @Override
    public List<ExperimentData> getExperimentByUser(String user) throws RegistryException {
        return getProvenanceResourceClient().getExperimentByUser(user);
    }

    @Override
    public List<ExperimentData> getExperimentByUser(String user, int pageSize, int pageNo) throws RegistryException {
        return getProvenanceResourceClient().getExperimentByUser(user, pageSize, pageNo);
    }

    @Override
    public void updateWorkflowNodeStatus(WorkflowInstanceNodeStatus workflowStatusNode) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowStatusNode);
    }

    @Override
    public void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, WorkflowInstanceStatus.ExecutionStatus status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowInstanceId, nodeId, status);
    }

    @Override
    public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode, WorkflowInstanceStatus.ExecutionStatus status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowNode, status);
    }

    @Override
    public WorkflowInstanceNodeStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowNodeStatus(workflowNode);
    }

    @Override
    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowNodeStartTime(workflowNode);
    }

    @Override
    public Date getWorkflowStartTime(WorkflowInstance workflowInstance) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowStartTime(workflowInstance);
    }

    @Override
    public void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeGramData(workflowNodeGramData);
    }

    @Override
    public WorkflowInstanceData getWorkflowInstanceData(String workflowInstanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceData(workflowInstanceId);
    }

    @Override
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
    }

    @Override
    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, createIfNotPresent);
    }

    @Override
    public WorkflowInstanceNodeData getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeData(workflowInstanceId, nodeId);
    }

    @Override
    public void addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName) throws RegistryException {
        getProvenanceResourceClient().addWorkflowInstance(experimentId, workflowInstanceId, templateName);
    }

    @Override
    public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeType(node, type);
    }

    @Override
    public void addWorkflowInstanceNode(String workflowInstance, String nodeId) throws RegistryException {
        getProvenanceResourceClient().addWorkflowInstanceNode(workflowInstance, nodeId);
    }

    @Override
    public boolean isPublishedWorkflowExists(String workflowName) throws RegistryException {
        return getPublishedWorkflowResourceClient().isPublishedWorkflowExists(workflowName);
    }

    @Override
    public void publishWorkflow(String workflowName, String publishWorkflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegistryException {
        getPublishedWorkflowResourceClient().publishWorkflow(workflowName, publishWorkflowName);
    }

    @Override
    public void publishWorkflow(String workflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegistryException {
        getPublishedWorkflowResourceClient().publishWorkflow(workflowName);
    }

    @Override
    public String getPublishedWorkflowGraphXML(String workflowName) throws PublishedWorkflowDoesNotExistsException, RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowGraphXML(workflowName);
    }

    @Override
    public List<String> getPublishedWorkflowNames() throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowNames();
    }

    @Override
    public Map<String, String> getPublishedWorkflows() throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflows();
    }

    @Override
    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowMetadata(workflowName);
    }

    @Override
    public void removePublishedWorkflow(String workflowName) throws PublishedWorkflowDoesNotExistsException, RegistryException {
        getUserWorkflowResourceClient().removeWorkflow(workflowName);
    }

    @Override
    public boolean isWorkflowExists(String workflowName) throws RegistryException {
        return getUserWorkflowResourceClient().isWorkflowExists(workflowName);
    }

    @Override
    public void addWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowAlreadyExistsException, RegistryException {
        getUserWorkflowResourceClient().addWorkflow(workflowName, workflowGraphXml);
    }

    @Override
    public void updateWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowDoesNotExistsException, RegistryException {
        getUserWorkflowResourceClient().updateWorkflow(workflowName, workflowGraphXml);
    }

    @Override
    public String getWorkflowGraphXML(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException {
        return getUserWorkflowResourceClient().getWorkflowGraphXML(workflowName);
    }

    @Override
    public Map<String, String> getWorkflows() throws RegistryException {
        return getUserWorkflowResourceClient().getWorkflows();
    }

    @Override
    public ResourceMetadata getWorkflowMetadata(String workflowName) throws RegistryException {
        return getUserWorkflowResourceClient().getWorkflowMetadata(workflowName);
    }

    @Override
    public void removeWorkflow(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException {
        getUserWorkflowResourceClient().removeWorkflow(workflowName);
    }

    @Override
    public void setAiravataRegistry(AiravataRegistry2 registry) {
    }

    @Override
    public void setAiravataUser(AiravataUser user) {
        getBasicRegistryResourceClient().setUser(user);
    }

    @Override
    public AiravataUser getAiravataUser() {
        return getBasicRegistryResourceClient().getUser();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Version getVersion() {
        return getBasicRegistryResourceClient().getVersion();
    }
    
	@Override
	public void setConnectionURI(URI connectionURI) {
		this.connectionURI=connectionURI;
	}

	@Override
	public URI getConnectionURI() {
		return connectionURI;
	}

	@Override
	public void setCallback(PasswordCallback callback) {
		this.callback=callback;
	}

	@Override
	public PasswordCallback getCallback() {
		return callback;
	}
}
