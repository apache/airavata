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

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.NewCookie;

import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.common.utils.AiravataJobState.State;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.ExecutionErrors.Source;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.registry.api.workflow.ApplicationJobExecutionError;
import org.apache.airavata.registry.api.workflow.ApplicationJobStatusData;
import org.apache.airavata.registry.api.workflow.ExecutionError;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.ExperimentExecutionError;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.NodeExecutionError;
import org.apache.airavata.registry.api.workflow.NodeExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionError;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.apache.airavata.rest.utils.CookieManager;

public class RegistryClient extends AiravataRegistry2 {

    private PasswordCallback callback;
    private URI connectionURI;
    
    private BasicRegistryResourceClient basicRegistryResourceClient;
    private ConfigurationResourceClient configurationResourceClient;
    private DescriptorResourceClient descriptorResourceClient;
    private ExperimentResourceClient experimentResourceClient;
    private ProjectResourceClient projectResourceClient;
    private ProvenanceResourceClient provenanceResourceClient;
    private PublishedWorkflowResourceClient publishedWorkflowResourceClient;
    private UserWorkflowResourceClient userWorkflowResourceClient;
    private CredentialStoreResourceClient credentialStoreResourceClient;
//    private CookieManager cookieManager = new CookieManager();


    public RegistryClient() {
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
    
    public CredentialStoreResourceClient getCredentialStoreResourceClient() {
        return credentialStoreResourceClient;
    }



    @Override
    protected void initialize() {
        CookieManager.setCookie(new NewCookie("", ""));
        String userName = getUser().getUserName();
        callback = getCallback();
        String serviceURI = getConnectionURI().toString();
        basicRegistryResourceClient =
                new BasicRegistryResourceClient(
                        userName, getGateway().getGatewayName(), serviceURI, callback, CookieManager.getCookie());
        configurationResourceClient =
                new ConfigurationResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI,  callback, CookieManager.getCookie());
        descriptorResourceClient =
                new DescriptorResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI, callback, CookieManager.getCookie());
        experimentResourceClient =
                new ExperimentResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI, callback, CookieManager.getCookie());
        projectResourceClient =
                new ProjectResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI, callback, CookieManager.getCookie());
        provenanceResourceClient =
                new ProvenanceResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI, callback, CookieManager.getCookie());
        publishedWorkflowResourceClient =
                new PublishedWorkflowResourceClient(
                        userName, getGateway().getGatewayName(),serviceURI, callback, CookieManager.getCookie());
        userWorkflowResourceClient =
                new UserWorkflowResourceClient(
                        userName,getGateway().getGatewayName(),serviceURI,callback, CookieManager.getCookie());
        credentialStoreResourceClient =
                new CredentialStoreResourceClient(
                        userName,getGateway().getGatewayName(),serviceURI,callback, CookieManager.getCookie());
    }

    public Object getConfiguration(String key) {
       return getConfigurationResourceClient().getConfiguration(key);
    }

    public List<Object> getConfigurationList(String key) {
        return getConfigurationResourceClient().getConfigurationList(key);
    }

    public void setConfiguration(String key, String value, Date expire) {
        getConfigurationResourceClient().setConfiguration(key, value, expire);
    }

    public void addConfiguration(String key, String value, Date expire) {
        getConfigurationResourceClient().addConfiguration(key, value, expire);
    }

    public void removeAllConfiguration(String key) {
        getConfigurationResourceClient().removeAllConfiguration(key);
    }

    public void removeConfiguration(String key, String value) {
        getConfigurationResourceClient().removeConfiguration(key, value);
    }

    public List<URI> getGFacURIs() {
        return getConfigurationResourceClient().getGFacURIs();
    }

    public List<URI> getWorkflowInterpreterURIs() {
        return getConfigurationResourceClient().getWorkflowInterpreterURIs();
    }


    public URI getEventingServiceURI() {
        return getConfigurationResourceClient().getEventingURI();
    }


    public URI getMessageBoxURI() {
        return getConfigurationResourceClient().getMsgBoxURI();
    }


    public void addGFacURI(URI uri) {
        getConfigurationResourceClient().addGFacURI(uri);
    }


    public void addWorkflowInterpreterURI(URI uri) {
        getConfigurationResourceClient().addWFInterpreterURI(uri);
    }


    public void setEventingURI(URI uri) {
        getConfigurationResourceClient().setEventingURI(uri);
    }


    public void setMessageBoxURI(URI uri) {
        getConfigurationResourceClient().setMessageBoxURI(uri);
    }


    public void addGFacURI(URI uri, Date expire) {
        getConfigurationResourceClient().addGFacURIByDate(uri, expire);
    }


    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        getConfigurationResourceClient().addWorkflowInterpreterURI(uri, expire);
    }


    public void setEventingURI(URI uri, Date expire) {
        getConfigurationResourceClient().setEventingURIByDate(uri, expire);
    }


    public void setMessageBoxURI(URI uri, Date expire) {
        getConfigurationResourceClient().setMessageBoxURIByDate(uri, expire);
    }


    public void removeGFacURI(URI uri) {
        getConfigurationResourceClient().removeGFacURI(uri);
    }


    public void removeAllGFacURI() {
        getConfigurationResourceClient().removeAllGFacURI();
    }


    public void removeWorkflowInterpreterURI(URI uri) {
        getConfigurationResourceClient().removeWorkflowInterpreterURI(uri);
    }


    public void removeAllWorkflowInterpreterURI() {
        getConfigurationResourceClient().removeAllWorkflowInterpreterURI();
    }


    public void unsetEventingURI() {
        getConfigurationResourceClient().unsetEventingURI();
    }


    public void unsetMessageBoxURI() {
        getConfigurationResourceClient().unsetMessageBoxURI();
    }


    public boolean isHostDescriptorExists(String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isHostDescriptorExists(descriptorName);
    }


    public void addHostDescriptor(HostDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().addHostDescriptor(descriptor);
    }


    public void updateHostDescriptor(HostDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().updateHostDescriptor(descriptor);
    }


    public HostDescription getHostDescriptor(String hostName) throws RegistryException {
        return getDescriptorResourceClient().getHostDescriptor(hostName);
    }


    public void removeHostDescriptor(String hostName) throws RegistryException {
        getDescriptorResourceClient().removeHostDescriptor(hostName);
    }


    public List<HostDescription> getHostDescriptors() throws RegistryException {
        return getDescriptorResourceClient().getHostDescriptors();
    }


    public ResourceMetadata getHostDescriptorMetadata(String hostName) throws RegistryException {
        return null;
    }


    public boolean isServiceDescriptorExists(String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isServiceDescriptorExists(descriptorName);
    }


    public void addServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
       getDescriptorResourceClient().addServiceDescriptor(descriptor);
    }


    public void updateServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().updateServiceDescriptor(descriptor);
    }


    public ServiceDescription getServiceDescriptor(String serviceName) throws RegistryException {
        return getDescriptorResourceClient().getServiceDescriptor(serviceName);
    }


    public void removeServiceDescriptor(String serviceName) throws RegistryException {
        getDescriptorResourceClient().removeServiceDescriptor(serviceName);
    }


    public List<ServiceDescription> getServiceDescriptors() throws RegistryException {
        return getDescriptorResourceClient().getServiceDescriptors();
    }


    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) throws RegistryException {
        return null;
    }


    public boolean isApplicationDescriptorExists(String serviceName,
                                                 String hostName,
                                                 String descriptorName) throws RegistryException {
        return getDescriptorResourceClient().isApplicationDescriptorExists(serviceName, hostName, descriptorName);
    }


    public void addApplicationDescriptor(ServiceDescription serviceDescription,
                                         HostDescription hostDescriptor,
                                         ApplicationDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().addApplicationDescriptor(serviceDescription, hostDescriptor, descriptor);
    }


    public void addApplicationDescriptor(String serviceName,
                                         String hostName,
                                         ApplicationDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().addApplicationDescriptor(serviceName, hostName, descriptor);
    }


    public void udpateApplicationDescriptor(ServiceDescription serviceDescription,
                                            HostDescription hostDescriptor,
                                            ApplicationDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().udpateApplicationDescriptor(serviceDescription, hostDescriptor, descriptor);
    }


    public void updateApplicationDescriptor(String serviceName,
                                            String hostName,
                                            ApplicationDescription descriptor) throws RegistryException {
        getDescriptorResourceClient().updateApplicationDescriptor(serviceName, hostName, descriptor);
    }


    public ApplicationDescription getApplicationDescriptor(String serviceName,
                                                                     String hostname,
                                                                     String applicationName) throws RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptor(serviceName, hostname, applicationName);
    }


    public ApplicationDescription getApplicationDescriptors(String serviceName,
                                                                      String hostname) throws RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors(serviceName, hostname);
    }


    public Map<String, ApplicationDescription> getApplicationDescriptors(String serviceName) throws RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors(serviceName);
    }


    public Map<String[], ApplicationDescription> getApplicationDescriptors() throws RegistryException {
        return getDescriptorResourceClient().getApplicationDescriptors();
    }


    public void removeApplicationDescriptor(String serviceName,
                                            String hostName,
                                            String applicationName) throws RegistryException {
        getDescriptorResourceClient().removeApplicationDescriptor(serviceName, hostName, applicationName);
    }


    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName,
                                                             String hostName,
                                                             String applicationName) throws RegistryException {
        return null;
    }


    public boolean isWorkspaceProjectExists(String projectName) throws RegistryException {
        return getProjectResourceClient().isWorkspaceProjectExists(projectName);
    }


    public boolean isWorkspaceProjectExists(String projectName,
                                            boolean createIfNotExists) throws RegistryException {
        return getProjectResourceClient().isWorkspaceProjectExists(projectName, createIfNotExists);
    }


    public void addWorkspaceProject(WorkspaceProject project) throws RegistryException {
        getProjectResourceClient().addWorkspaceProject(project);
    }


    public void updateWorkspaceProject(WorkspaceProject project) throws RegistryException {
        getProjectResourceClient().updateWorkspaceProject(project);
    }


    public void deleteWorkspaceProject(String projectName) throws RegistryException {
        getProjectResourceClient().deleteWorkspaceProject(projectName);
    }


    public WorkspaceProject getWorkspaceProject(String projectName) throws RegistryException {
        return getProjectResourceClient().getWorkspaceProject(projectName);
    }


    public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException {
        return getProjectResourceClient().getWorkspaceProjects();
    }


    public void addExperiment(String projectName,
                              AiravataExperiment experiment) throws RegistryException {
        getExperimentResourceClient().addExperiment(projectName, experiment);
    }


    public void removeExperiment(String experimentId) throws ExperimentDoesNotExistsException {
        getExperimentResourceClient().removeExperiment(experimentId);
    }


    public List<AiravataExperiment> getExperiments() throws RegistryException {
        return getExperimentResourceClient().getExperiments();
    }


    public List<AiravataExperiment> getExperiments(String projectName) throws RegistryException {
        return getExperimentResourceClient().getExperiments(projectName);
    }


    public List<AiravataExperiment> getExperiments(Date from, Date to) throws RegistryException {
        return getExperimentResourceClient().getExperiments(from, to);
    }


    public List<AiravataExperiment> getExperiments(String projectName,
                                                   Date from,
                                                   Date to) throws RegistryException {
        return getExperimentResourceClient().getExperiments(projectName, from, to);
    }


    public boolean isExperimentExists(String experimentId) throws RegistryException {
        return getExperimentResourceClient().isExperimentExists(experimentId);
    }


    public boolean isExperimentExists(String experimentId,
                                      boolean createIfNotPresent) throws RegistryException {
        return getExperimentResourceClient().isExperimentExists(experimentId, createIfNotPresent);
    }


    public void updateExperimentExecutionUser(String experimentId,
                                              String user) throws RegistryException {
        getProvenanceResourceClient().updateExperimentExecutionUser(experimentId, user);
    }


    public String getExperimentExecutionUser(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentExecutionUser(experimentId);
    }


    public boolean isExperimentNameExist(String experimentName) throws RegistryException {
        return getProvenanceResourceClient().isExperimentNameExist(experimentName);
    }


    public String getExperimentName(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentName(experimentId);
    }


    public void updateExperimentName(String experimentId,
                                     String experimentName) throws RegistryException {
        getProvenanceResourceClient().updateExperimentName(experimentId, experimentName);
    }


    public String getExperimentMetadata(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentMetadata(experimentId);
    }


    public void updateExperimentMetadata(String experimentId,
                                         String metadata) throws RegistryException {
        getProvenanceResourceClient().updateExperimentMetadata(experimentId, metadata);
    }


    public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionTemplateName(workflowInstanceId);
    }


    public void setWorkflowInstanceTemplateName(String workflowInstanceId,
                                                String templateName) throws RegistryException {
        getProvenanceResourceClient().setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
    }


    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentWorkflowInstances(experimentId);
    }


    public boolean isWorkflowInstanceExists(String instanceId) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceExists(instanceId);
    }


    public boolean isWorkflowInstanceExists(String instanceId,
                                            boolean createIfNotPresent) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceExists(instanceId, createIfNotPresent);
    }


    public void updateWorkflowInstanceStatus(String instanceId,
                                             WorkflowExecutionStatus.State status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowInstanceStatus(instanceId, status);
    }


    public void updateWorkflowInstanceStatus(WorkflowExecutionStatus status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowInstanceStatus(status);
    }


    public WorkflowExecutionStatus getWorkflowInstanceStatus(String instanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceStatus(instanceId);
    }


    public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeInput(node, data);
    }


    public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeOutput(node, data);
    }


    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(String experimentIdRegEx,
                                                                    String workflowNameRegEx,
                                                                    String nodeNameRegEx) throws RegistryException {
        return getProvenanceResourceClient().searchWorkflowInstanceNodeInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    }


    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(String experimentIdRegEx,
                                                                     String workflowNameRegEx,
                                                                     String nodeNameRegEx) throws RegistryException {
        return getProvenanceResourceClient().searchWorkflowInstanceNodeOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    }


    public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(String workflowInstanceId,
                                                                 String nodeType) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeInput(workflowInstanceId, nodeType);
    }


    public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(String workflowInstanceId,
                                                                  String nodeType) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeOutput(workflowInstanceId, nodeType);
    }


    public void saveWorkflowExecutionOutput(String experimentId, String outputNodeName,
                                            String output) throws RegistryException {
        getProvenanceResourceClient().saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
    }


    public void saveWorkflowExecutionOutput(String experimentId,
                                            WorkflowIOData data) throws RegistryException {
        getProvenanceResourceClient().saveWorkflowExecutionOutput(experimentId, data);
    }


    public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
                                                     String outputNodeName) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutput(experimentId, outputNodeName);
    }


    public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutput(experimentId);
    }


    public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowExecutionOutputNames(exeperimentId);
    }


    public ExperimentData getExperiment(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperiment(experimentId);
    }


    public ExperimentData getExperimentMetaInformation(String experimentId) throws RegistryException {
        return getProvenanceResourceClient().getExperimentMetaInformation(experimentId);
    }


    public List<ExperimentData> getAllExperimentMetaInformation(String user) throws RegistryException {
        return getProvenanceResourceClient().getAllExperimentMetaInformation(user);
    }


    public List<ExperimentData> searchExperiments(String user,
                                                  String experimentNameRegex) throws RegistryException {
        return getProvenanceResourceClient().searchExperiments(user, experimentNameRegex);
    }


    public List<String> getExperimentIdByUser(String user) throws RegistryException {
        return getProvenanceResourceClient().getExperimentIdByUser(user);
    }


    public List<ExperimentData> getExperimentByUser(String user) throws RegistryException {
        return getProvenanceResourceClient().getExperimentByUser(user);
    }
    
    public List<ExperimentData> getExperiments(HashMap<String,String> params) throws RegistryException {
        return null;
    }


    public List<ExperimentData> getExperimentByUser(String user, int pageSize, int pageNo) throws RegistryException {
        return getProvenanceResourceClient().getExperimentByUser(user, pageSize, pageNo);
    }


    public void updateWorkflowNodeStatus(NodeExecutionStatus workflowStatusNode) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowStatusNode);
    }


    public void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId,
                                         WorkflowExecutionStatus.State status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowInstanceId, nodeId, status);
    }


    public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode,
                                         WorkflowExecutionStatus.State status) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeStatus(workflowNode, status);
    }


    public NodeExecutionStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowNodeStatus(workflowNode);
    }


    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowNodeStartTime(workflowNode);
    }


    public Date getWorkflowStartTime(WorkflowExecution workflowInstance) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowStartTime(workflowInstance);
    }


    public void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeGramData(workflowNodeGramData);
    }


    public WorkflowExecutionData getWorkflowInstanceData(String workflowInstanceId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceData(workflowInstanceId);
    }


    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
                                                 String nodeId) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
    }


    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId,
                                                 boolean createIfNotPresent) throws RegistryException {
        return getProvenanceResourceClient().isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, createIfNotPresent);
    }


    public NodeExecutionData getWorkflowInstanceNodeData(String workflowInstanceId,
                                                                String nodeId) throws RegistryException {
        return getProvenanceResourceClient().getWorkflowInstanceNodeData(workflowInstanceId, nodeId);
    }


    public void addWorkflowInstance(String experimentId, String workflowInstanceId,
                                    String templateName) throws RegistryException {
        getProvenanceResourceClient().addWorkflowInstance(experimentId, workflowInstanceId, templateName);
    }


    public void updateWorkflowNodeType(WorkflowInstanceNode node,
                                       WorkflowNodeType type) throws RegistryException {
        getProvenanceResourceClient().updateWorkflowNodeType(node, type);
    }


    public void addWorkflowInstanceNode(String workflowInstance,
                                        String nodeId) throws RegistryException {
        getProvenanceResourceClient().addWorkflowInstanceNode(workflowInstance, nodeId);
    }


    public boolean isPublishedWorkflowExists(String workflowName) throws RegistryException {
        return getPublishedWorkflowResourceClient().isPublishedWorkflowExists(workflowName);
    }


    public void publishWorkflow(String workflowName,
                                String publishWorkflowName) throws RegistryException {
        getPublishedWorkflowResourceClient().publishWorkflow(workflowName, publishWorkflowName);
    }


    public void publishWorkflow(String workflowName) throws RegistryException {
        getPublishedWorkflowResourceClient().publishWorkflow(workflowName);
    }


    public String getPublishedWorkflowGraphXML(String workflowName) throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowGraphXML(workflowName);
    }


    public List<String> getPublishedWorkflowNames() throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowNames();
    }


    public Map<String, String> getPublishedWorkflows() throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflows();
    }


    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) throws RegistryException {
        return getPublishedWorkflowResourceClient().getPublishedWorkflowMetadata(workflowName);
    }


    public void removePublishedWorkflow(String workflowName) throws RegistryException {
        getUserWorkflowResourceClient().removeWorkflow(workflowName);
    }


    public boolean isWorkflowExists(String workflowName) throws RegistryException {
        return getUserWorkflowResourceClient().isWorkflowExists(workflowName);
    }


    public void addWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
        getUserWorkflowResourceClient().addWorkflow(workflowName, workflowGraphXml);
    }


    public void updateWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
        getUserWorkflowResourceClient().updateWorkflow(workflowName, workflowGraphXml);
    }


    public String getWorkflowGraphXML(String workflowName) throws RegistryException {
        return getUserWorkflowResourceClient().getWorkflowGraphXML(workflowName);
    }


    public Map<String, String> getWorkflows() throws RegistryException {
        return getUserWorkflowResourceClient().getWorkflows();
    }


    public ResourceMetadata getWorkflowMetadata(String workflowName) throws RegistryException {
        return getUserWorkflowResourceClient().getWorkflowMetadata(workflowName);
    }


    public void removeWorkflow(String workflowName) throws RegistryException {
        getUserWorkflowResourceClient().removeWorkflow(workflowName);
    }


    public void setAiravataRegistry(AiravataRegistry2 registry) {
    }


    public void setAiravataUser(AiravataUser user) {
        getBasicRegistryResourceClient().setUser(user);
    }


    public AiravataUser getAiravataUser() {
        return getBasicRegistryResourceClient().getUser();
    }


    public boolean isActive() {
        return false;
    }


    public Version getVersion() {
        return getBasicRegistryResourceClient().getVersion();
    }

    public void setConnectionURI(URI connectionURI) {
        this.connectionURI = connectionURI;
    }

    public URI getConnectionURI() {
		return connectionURI;
	}


	public void setCallback(PasswordCallback callback) {
		this.callback=callback;
	}


	public PasswordCallback getCallback() {
		return callback;
	}

	@Override
	public List<ExperimentExecutionError> getExperimentExecutionErrors(
			String experimentId) throws RegistryException {
		return provenanceResourceClient.getExperimentExecutionErrors(experimentId);
	}

	@Override
	public List<WorkflowExecutionError> getWorkflowExecutionErrors(
			String experimentId, String workflowInstanceId)
			throws RegistryException {
		return provenanceResourceClient.getWorkflowExecutionErrors(experimentId, workflowInstanceId);
	}

	@Override
	public List<NodeExecutionError> getNodeExecutionErrors(String experimentId,
			String workflowInstanceId, String nodeId) throws RegistryException {
		return getProvenanceResourceClient().getNodeExecutionErrors(experimentId, workflowInstanceId, nodeId);
	}

	@Override
	public List<ApplicationJobExecutionError> getApplicationJobErrors(String experimentId,
			String workflowInstanceId, String nodeId, String gfacJobId)
			throws RegistryException {
		return getProvenanceResourceClient().getGFacJobErrors(experimentId, workflowInstanceId, nodeId, gfacJobId);
	}

	@Override
	public List<ApplicationJobExecutionError> getApplicationJobErrors(String gfacJobId)
			throws RegistryException {
		return getProvenanceResourceClient().getGFacJobErrors(gfacJobId);
	}

	@Override
	public List<ExecutionError> getExecutionErrors(String experimentId,
			String workflowInstanceId, String nodeId, String gfacJobId,
			Source... filterBy) throws RegistryException {
		return getProvenanceResourceClient().getExecutionErrors(experimentId, workflowInstanceId, nodeId, gfacJobId, filterBy);
	}

	@Override
	public int addExperimentError(ExperimentExecutionError error)
			throws RegistryException {
		return getProvenanceResourceClient().addExperimentError(error);
	}

	@Override
	public int addWorkflowExecutionError(WorkflowExecutionError error)
			throws RegistryException {
		return getProvenanceResourceClient().addWorkflowExecutionError(error);
	}

	@Override
	public int addNodeExecutionError(NodeExecutionError error)
			throws RegistryException {
		return getProvenanceResourceClient().addNodeExecutionError(error);
	}

	@Override
	public int addApplicationJobExecutionError(ApplicationJobExecutionError error)
			throws RegistryException {
		return getProvenanceResourceClient().addGFacJobExecutionError(error);
	}

	@Override
	public void addApplicationJob(ApplicationJob job) throws RegistryException {
		getProvenanceResourceClient().addApplicationJob(job);
		
	}

	@Override
	public void updateApplicationJob(ApplicationJob job) throws RegistryException {
        getProvenanceResourceClient().updateApplicationJob(job);
		
	}

	@Override
	public void updateApplicationJobStatus(String gfacJobId, ApplicationJobStatus status, Date statusUpdateTime)
			throws RegistryException {
        getProvenanceResourceClient().updateApplicationJobStatus(gfacJobId, status, statusUpdateTime);
		
	}

	@Override
	public void updateApplicationJobData(String gfacJobId, String jobdata)
			throws RegistryException {
        getProvenanceResourceClient().updateApplicationJobData(gfacJobId, jobdata);
		
	}

	@Override
	public void updateApplicationJobSubmittedTime(String gfacJobId, Date submitted)
			throws RegistryException {
        getProvenanceResourceClient().updateApplicationJobSubmittedTime(gfacJobId, submitted);
		
	}

	@Override
	public void updateApplicationJobStatusUpdateTime(String gfacJobId, Date completed)
			throws RegistryException {
        getProvenanceResourceClient().updateApplicationJobCompletedTime(gfacJobId, completed);
		
	}

	@Override
	public void updateApplicationJobMetadata(String gfacJobId, String metadata)
			throws RegistryException {
        getProvenanceResourceClient().updateApplicationJobMetadata(gfacJobId, metadata);
		
	}

	@Override
	public ApplicationJob getApplicationJob(String gfacJobId) throws RegistryException {
		return getProvenanceResourceClient().getApplicationJob(gfacJobId);
	}

	@Override
	public List<ApplicationJob> getApplicationJobsForDescriptors(String serviceDescriptionId,
			String hostDescriptionId, String applicationDescriptionId)
			throws RegistryException {
		return getProvenanceResourceClient().getApplicationJobsForDescriptors(serviceDescriptionId, hostDescriptionId, applicationDescriptionId);
	}

	@Override
	public List<ApplicationJob> getApplicationJobs(String experimentId,
			String workflowExecutionId, String nodeId) throws RegistryException {
		return getProvenanceResourceClient().getApplicationJobs(experimentId, workflowExecutionId, nodeId);
	}

	@Override
	public boolean isApplicationJobExists(String gfacJobId) throws RegistryException {
		return getProvenanceResourceClient().isApplicationJobExists(gfacJobId);
	}

	@Override
	public List<ApplicationJobStatusData> getApplicationJobStatusHistory(
			String jobId) throws RegistryException {
		return getProvenanceResourceClient().getApplicationJobStatusHistory(jobId);
	}

	@Override
	public List<AiravataUser> getUsers() throws RegistryException {
        throw new UnimplementedRegistryOperationException();
	}

	@Override
	public boolean isCredentialExist(String gatewayId, String tokenId)
			throws RegistryException {
		return getCredentialStoreResourceClient().isCredentialExist(gatewayId,tokenId);
	}

	@Override
	public String getCredentialPublicKey(String gatewayId, String tokenId)
			throws RegistryException {
		return getCredentialStoreResourceClient().getCredentialPublicKey(gatewayId, tokenId);
	}

	@Override
	public String createCredential(String gatewayId, String tokenId)
			throws RegistryException {
		return getCredentialStoreResourceClient().createCredential(gatewayId, tokenId);
	}
	
	@Override
	public String createCredential(String gatewayId, String tokenId, String username)
			throws RegistryException {
		return getCredentialStoreResourceClient().createCredential(gatewayId, tokenId, username);
	}


    //todo implement these methods properly


    public boolean addGFACNode(String uri, int nodeID) throws RegistryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Integer> getGFACNodeList() throws RegistryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public boolean changeStatus(String experimentID, State state, String gfacEPR)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AiravataJobState getState(String experimentID)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllJobsWithState(AiravataJobState state)
			throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllAcceptedJobs() throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<String> getAllHangedJobs() throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHangedJobCount() throws RegistryException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean resetHangedJob(String experimentID) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean changeStatus(String experimentID, State state)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

    
}
