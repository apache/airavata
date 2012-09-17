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
package org.apache.airavata.persistance.registry.jpa.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.JPAResourceAccessor;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ConfigurationResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.PublishWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.ServiceDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.provenance.model.Experiment_Data;
import org.apache.airavata.provenance.model.Node_Data;
import org.apache.airavata.provenance.model.Workflow_Data;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowRunTimeData;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataJPARegistry extends AiravataRegistry2{
    private final static Logger logger = LoggerFactory.getLogger(AiravataJPARegistry.class);
    private JPAResourceAccessor jpa;
    private boolean active=false;
    
    @Override
    protected void initialize() {
    	jpa = new JPAResourceAccessor(this);
    	//TODO check if the db connections are proper & accessible & the relevant db/tables are 
    	//present
    	active=true;
    }
    

	@Override
	public boolean isActive() {
		return active;
	}

    /**---------------------------------Configuration Registry----------------------------------**/
    
    public Object getConfiguration(String key) {
		ConfigurationResource configuration = ResourceUtils.getConfiguration(key);
		return configuration==null? null: configuration.getConfigVal();
    }
    // Not sure about this.. need some description
    public List<Object> getConfigurationList(String key) {
    	List<Object> values = new ArrayList<Object>();
    	List<ConfigurationResource> configurations = ResourceUtils.getConfigurations(key);
        for (ConfigurationResource configurationResource : configurations) {
			values.add(configurationResource.getConfigVal());
		}
		return values;
    }

    public void setConfiguration(String key, String value, Date expire) {
    	ConfigurationResource config;
		if (ResourceUtils.isConfigurationExist(key)) {
			config = ResourceUtils.getConfiguration(key);
		}else{
			config = ResourceUtils.createConfiguration(key);
		}
    	config.setConfigVal(value);
    	config.setExpireDate(new java.sql.Date(expire.getTime()));
    	config.save();
    }

    public void addConfiguration(String key, String value, Date expire) {
    	ConfigurationResource config = ResourceUtils.createConfiguration(key);
    	config.setConfigVal(value);
    	config.setExpireDate(new java.sql.Date(expire.getTime()));
    	config.save();
    }

    public void removeAllConfiguration(String key) {
    	ResourceUtils.removeConfiguration(key);
    }

    public void removeConfiguration(String key, String value) {
    	ResourceUtils.removeConfiguration(key, value);
    }
    
    private static final String GFAC_URL="gfac.url";
    private static final String INTERPRETER_URL="interpreter.url";
    private static final String MESSAGE_BOX_URL="messagebox.url";
    private static final String EVENTING_URL="eventing.url";
    
    public List<URI> getGFacURIs() {
    	return retrieveURIsFromConfiguration(GFAC_URL);
    }

	private List<URI> retrieveURIsFromConfiguration(String urlType) {
		List<URI> urls=new ArrayList<URI>();
    	List<Object> configurationList = getConfigurationList(urlType);
    	for (Object o : configurationList) {
			try {
				urls.add(new URI(o.toString()));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
        return urls;
	}

    public List<URI> getWorkflowInterpreterURIs() {
    	return retrieveURIsFromConfiguration(INTERPRETER_URL);
    }

    public URI getEventingServiceURI() {
    	List<URI> eventingURLs = retrieveURIsFromConfiguration(EVENTING_URL);
		return eventingURLs.size()==0? null: eventingURLs.get(0);
    }

    public URI getMessageBoxURI() {
    	List<URI> messageboxURLs = retrieveURIsFromConfiguration(MESSAGE_BOX_URL);
		return messageboxURLs.size()==0? null: messageboxURLs.get(0);
    }

    public void addGFacURI(URI uri) {
        addConfigurationURL(GFAC_URL,uri);
    }

	private void addConfigurationURL(String urlType,URI uri) {
		Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, AiravataRegistry2.SERVICE_TTL);
		Date expire = instance.getTime();
		addConfigurationURL(urlType, uri, expire);
	}

	private void addConfigurationURL(String urlType, URI uri, Date expire) {
		addConfiguration(urlType, uri.toString(), expire);
	}

    public void addWorkflowInterpreterURI(URI uri) {
        addConfigurationURL(INTERPRETER_URL,uri);
    }

    public void setEventingURI(URI uri) {
    	addConfigurationURL(EVENTING_URL,uri);
    }

    public void setMessageBoxURI(URI uri) {
    	addConfigurationURL(MESSAGE_BOX_URL,uri);
    }

    public void addGFacURI(URI uri, Date expire) {
    	addConfigurationURL(GFAC_URL, uri, expire);
    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {
    	addConfigurationURL(INTERPRETER_URL, uri, expire);
    }

    public void setEventingURI(URI uri, Date expire) {
    	addConfigurationURL(EVENTING_URL, uri, expire);
    }

    public void setMessageBoxURI(URI uri, Date expire) {
    	addConfigurationURL(MESSAGE_BOX_URL, uri, expire);
    }

    public void removeGFacURI(URI uri) {
        removeConfiguration(GFAC_URL, uri.toString());
    }

    public void removeWorkflowInterpreterURI(URI uri) {
    	removeConfiguration(INTERPRETER_URL,uri.toString());
    }

    public void removeAllGFacURI() {
        removeAllConfiguration(GFAC_URL);
    }

    public void removeAllWorkflowInterpreterURI() {
    	removeAllConfiguration(INTERPRETER_URL);
    }

    public void unsetEventingURI() {
        removeAllConfiguration(EVENTING_URL);
    }

    public void unsetMessageBoxURI() {
    	removeAllConfiguration(MESSAGE_BOX_URL);
    }

    /**---------------------------------Descriptor Registry----------------------------------**/
    
    public boolean isHostDescriptorExists(String descriptorName)throws RegistryException{
    	return jpa.getGateway().isHostDescriptorExists(descriptorName);
    }
    public void addHostDescriptor(HostDescription descriptor) throws RegistryException {
        GatewayResource gateway = jpa.getGateway();
        WorkerResource workerResource = jpa.getWorker();
        String hostName = descriptor.getType().getHostName();
		if (isHostDescriptorExists(hostName)){
        	throw new DescriptorAlreadyExistsException(hostName);
        }
        HostDescriptorResource hostDescriptorResource = gateway.createHostDescriptorResource(hostName);
        hostDescriptorResource.setUserName(workerResource.getUser());
        hostDescriptorResource.setContent(descriptor.toXML());
        hostDescriptorResource.save();
    }

    public void updateHostDescriptor(HostDescription descriptor) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
        String hostName = descriptor.getType().getHostName();
		if (!isHostDescriptorExists(hostName)){
        	throw new DescriptorDoesNotExistsException(hostName);
        }
        HostDescriptorResource hostDescriptorResource = gateway.getHostDescriptorResource(hostName);
        hostDescriptorResource.setContent(descriptor.toXML());
        hostDescriptorResource.save();
    }

    public HostDescription getHostDescriptor(String hostName) throws RegistryException {
        GatewayResource gateway = jpa.getGateway();
		if (!isHostDescriptorExists(hostName)){
        	return null;
        }
        HostDescriptorResource hostDescriptorResource = gateway.getHostDescriptorResource(hostName);
        return createHostDescriptor(hostDescriptorResource);
    }

	private HostDescription createHostDescriptor(
			HostDescriptorResource hostDescriptorResource)
			throws MalformedDescriptorException {
		try {
            return HostDescription.fromXML(hostDescriptorResource.getContent());
        } catch (XmlException e) {
            throw new MalformedDescriptorException(hostDescriptorResource.getHostDescName(),e);
        }
	}

    public void removeHostDescriptor(String hostName) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
		if (!isHostDescriptorExists(hostName)){
        	throw new DescriptorDoesNotExistsException(hostName);
        }
		gateway.removeHostDescriptor(hostName);
    }


	@Override
	public List<HostDescription> getHostDescriptors()
			throws MalformedDescriptorException, RegistryException {
		GatewayResource gateway = jpa.getGateway();
		List<HostDescription> list=new ArrayList<HostDescription>();
		List<HostDescriptorResource> hostDescriptorResources = gateway.getHostDescriptorResources();
		for (HostDescriptorResource resource : hostDescriptorResources) {
			list.add(createHostDescriptor(resource));
		}
		return list;
	}

    public ResourceMetadata getHostDescriptorMetadata(String hostName) throws RegistryException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    public boolean isServiceDescriptorExists(String descriptorName)throws RegistryException{
    	return jpa.getGateway().isServiceDescriptorExists(descriptorName);
    }
    
    public void addServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
        WorkerResource workerResource = jpa.getWorker();
        String serviceName = descriptor.getType().getName();
		if (isServiceDescriptorExists(serviceName)){
        	throw new DescriptorAlreadyExistsException(serviceName);
        }
        ServiceDescriptorResource serviceDescriptorResource = gateway.createServiceDescriptorResource(serviceName);
        serviceDescriptorResource.setUserName(workerResource.getUser());
        serviceDescriptorResource.setContent(descriptor.toXML());
        serviceDescriptorResource.save();
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
        String serviceName = descriptor.getType().getName();
		if (!isServiceDescriptorExists(serviceName)){
        	throw new DescriptorDoesNotExistsException(serviceName);
        }
        ServiceDescriptorResource serviceDescriptorResource = gateway.getServiceDescriptorResource(serviceName);
        serviceDescriptorResource.setContent(descriptor.toXML());
        serviceDescriptorResource.save();
    }

    public ServiceDescription getServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException, MalformedDescriptorException {
    	GatewayResource gateway = jpa.getGateway();
		if (!gateway.isServiceDescriptorExists(serviceName)){
        	return null;
        }
        ServiceDescriptorResource serviceDescriptorResource = gateway.getServiceDescriptorResource(serviceName);
        return createServiceDescriptor(serviceDescriptorResource);
    }

	private ServiceDescription createServiceDescriptor(
			ServiceDescriptorResource serviceDescriptorResource)
			throws MalformedDescriptorException {
		try {
            return ServiceDescription.fromXML(serviceDescriptorResource.getContent());
        } catch (XmlException e) {
            throw new MalformedDescriptorException(serviceDescriptorResource.getServiceDescName(),e);
        }
	}

    public void removeServiceDescriptor(String serviceName) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
		if (!isServiceDescriptorExists(serviceName)){
        	throw new DescriptorDoesNotExistsException(serviceName);
        }
		gateway.removeServiceDescriptor(serviceName);
    }
    
    @Override
	public List<ServiceDescription> getServiceDescriptors()
			throws MalformedDescriptorException, RegistryException {
		GatewayResource gateway = jpa.getGateway();
		List<ServiceDescription> list=new ArrayList<ServiceDescription>();
		List<ServiceDescriptorResource> serviceDescriptorResources = gateway.getServiceDescriptorResources();
		for (ServiceDescriptorResource resource : serviceDescriptorResources) {
			list.add(createServiceDescriptor(resource));
		}
		return list;
	}
    
    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) throws UnimplementedRegistryOperationException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    private String createAppName(String serviceName, String hostName, String applicationName){
    	return serviceName+"/"+hostName+"/"+applicationName;
    }
    
    public boolean isApplicationDescriptorExists(String serviceName, String hostName, String descriptorName)throws RegistryException{
 		return jpa.getGateway().isApplicationDescriptorExists(createAppName(serviceName, hostName, descriptorName));
    }
    
    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws RegistryException {
        addApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
        WorkerResource workerResource = jpa.getWorker();
        String applicationName = descriptor.getType().getApplicationName().getStringValue();
        applicationName = createAppName(serviceName, hostName, applicationName);
		if (isApplicationDescriptorExists(serviceName,hostName,descriptor.getType().getApplicationName().getStringValue())){
        	throw new DescriptorAlreadyExistsException(applicationName);
        }
        ApplicationDescriptorResource applicationDescriptorResource = gateway.createApplicationDescriptorResource(applicationName);
        applicationDescriptorResource.setUpdatedUser(workerResource.getUser());
        applicationDescriptorResource.setServiceDescName(serviceName);
        applicationDescriptorResource.setHostDescName(hostName);
        applicationDescriptorResource.setContent(descriptor.toXML());
        applicationDescriptorResource.save();
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws RegistryException {
    	updateApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
    	String applicationName = descriptor.getType().getApplicationName().getStringValue();
        applicationName = createAppName(serviceName, hostName, applicationName);
		if (!isApplicationDescriptorExists(serviceName,hostName,descriptor.getType().getApplicationName().getStringValue())){
        	throw new DescriptorDoesNotExistsException(applicationName);
        }
        ApplicationDescriptorResource serviceDescriptorResource = gateway.getApplicationDescriptorResource(applicationName);
        serviceDescriptorResource.setContent(descriptor.toXML());
        serviceDescriptorResource.save();
    }
    private ApplicationDeploymentDescription createApplicationDescriptor(
			ApplicationDescriptorResource applicationDescriptorResource)
			throws MalformedDescriptorException {
		try {
            return ApplicationDeploymentDescription.fromXML(applicationDescriptorResource.getContent());
        } catch (XmlException e) {
            throw new MalformedDescriptorException(applicationDescriptorResource.getName(),e);
        }
	}
    
    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName)throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException{
    	GatewayResource gateway = jpa.getGateway();
		if (!isApplicationDescriptorExists(serviceName,hostname,applicationName)){
        	throw new DescriptorDoesNotExistsException(createAppName(serviceName, hostname, applicationName));
        }
        return createApplicationDescriptor(gateway.getApplicationDescriptorResource(createAppName(serviceName, hostname, applicationName)));
    }
    
    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) throws MalformedDescriptorException {
    	GatewayResource gateway = jpa.getGateway();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources(serviceName, hostname);
		if (applicationDescriptorResources.size()>0){
			return createApplicationDescriptor(applicationDescriptorResources.get(0));
		}
		return null;
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) throws MalformedDescriptorException {
    	GatewayResource gateway = jpa.getGateway();
		Map<String, ApplicationDeploymentDescription> map=new HashMap<String,ApplicationDeploymentDescription>();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources(serviceName, null);
		for (ApplicationDescriptorResource resource : applicationDescriptorResources) {
			map.put(resource.getHostDescName(),createApplicationDescriptor(resource));
		}
		return map;
    }
    
    public Map<String[],ApplicationDeploymentDescription> getApplicationDescriptors()throws MalformedDescriptorException, RegistryException{
    	GatewayResource gateway = jpa.getGateway();
		Map<String[], ApplicationDeploymentDescription> map=new HashMap<String[],ApplicationDeploymentDescription>();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources();
		for (ApplicationDescriptorResource resource : applicationDescriptorResources) {
			map.put(new String[]{resource.getServiceDescName(),resource.getHostDescName()},createApplicationDescriptor(resource));
		}
		return map;
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
    	String appName = createAppName(serviceName, hostName, applicationName);
    	if (!isApplicationDescriptorExists(serviceName,hostName,applicationName)){
			throw new DescriptorDoesNotExistsException(appName);
		}
        gateway.removeApplicationDescriptor(appName);
    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) throws UnimplementedRegistryOperationException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    /**---------------------------------Project Registry----------------------------------**/

    public void addWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectAlreadyExistsException {
    	WorkerResource worker = jpa.getWorker();
		if (worker.isProjectExists(project.getProjectName())){
        	throw new WorkspaceProjectAlreadyExistsException(project.getProjectName());
        }
		ProjectResource projectResource = worker.createProject(project.getProjectName());
		projectResource.save();
    }

    public void updateWorkspaceProject(WorkspaceProject project) throws WorkspaceProjectDoesNotExistsException {
    	WorkerResource worker = jpa.getWorker();
		if (!worker.isProjectExists(project.getProjectName())){
        	throw new WorkspaceProjectDoesNotExistsException(project.getProjectName());
        }
		ProjectResource projectResource = worker.getProject(project.getProjectName());
		projectResource.save();
    }

    public void deleteWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException {
    	WorkerResource worker = jpa.getWorker();
		if (!worker.isProjectExists(projectName)){
        	throw new WorkspaceProjectDoesNotExistsException(projectName);
        }
		worker.removeProject(projectName);
    }

    public WorkspaceProject getWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException {
    	WorkerResource worker = jpa.getWorker();
		if (!worker.isProjectExists(projectName)){
        	throw new WorkspaceProjectDoesNotExistsException(projectName);
        }
		ProjectResource projectResource = worker.getProject(projectName);
		return new WorkspaceProject(projectResource.getName(), this);
    }
    
    public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException{
    	WorkerResource worker = jpa.getWorker();
    	List<WorkspaceProject> projects=new ArrayList<WorkspaceProject>();
    	List<ProjectResource> projectResouces = worker.getProjects();
    	for (ProjectResource resource : projectResouces) {
			projects.add(new WorkspaceProject(resource.getName(), this));
		}
    	return projects;
    }

    public void addExperiment(String projectName, AiravataExperiment experiment) throws WorkspaceProjectDoesNotExistsException, ExperimentDoesNotExistsException {
    	WorkspaceProject workspaceProject = getWorkspaceProject(projectName);
    	ProjectResource project = jpa.getWorker().getProject(workspaceProject.getProjectName());
		String experimentId = experiment.getExperimentId();
		if (project.isExperimentExists(experimentId)){
        	throw new ExperimentDoesNotExistsException(experimentId);
        }
		ExperimentResource experimentResource = project.createExperiment(experimentId);
		experimentResource.setSubmittedDate(new java.sql.Date(experiment.getSubmittedDate().getTime()));
		experimentResource.save();
    }

    public void removeExperiment(String experimentId) throws ExperimentDoesNotExistsException {
    	WorkerResource worker = jpa.getWorker();
    	if (!worker.isExperimentExists(experimentId)){
        	throw new ExperimentDoesNotExistsException(experimentId);
    	}
    	worker.removeExperiment(experimentId);
    }

    public List<AiravataExperiment> getExperiments() throws RegistryException{
    	WorkerResource worker = jpa.getWorker();
    	List<AiravataExperiment> result=new ArrayList<AiravataExperiment>();
    	List<ExperimentResource> experiments = worker.getExperiments();
    	for (ExperimentResource resource : experiments) {
			AiravataExperiment e = createAiravataExperimentObj(resource);
			result.add(e);
		}
        return result;
    }

	private AiravataExperiment createAiravataExperimentObj(
			ExperimentResource resource) {
		AiravataExperiment e = new AiravataExperiment();
		e.setExperimentId(resource.getExpID());
		e.setUser(new AiravataUser(resource.getWorker().getUser()));
		e.setSubmittedDate(new Date(resource.getSubmittedDate().getTime()));
		e.setGateway(new Gateway(resource.getGateway().getGatewayName()));
		e.setProject(new WorkspaceProject(resource.getProject().getName(), this));
		return e;
	}

    public List<AiravataExperiment> getExperiments(String projectName)throws RegistryException {
    	ProjectResource project = jpa.getWorker().getProject(projectName);
    	List<ExperimentResource> experiments = project.getExperiments();
    	List<AiravataExperiment> result=new ArrayList<AiravataExperiment>();
    	for (ExperimentResource resource : experiments) {
			AiravataExperiment e = createAiravataExperimentObj(resource);
			result.add(e);
		}
        return result;
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to)throws RegistryException {
    	List<AiravataExperiment> experiments = getExperiments();
        List<AiravataExperiment> newExperiments = new ArrayList<AiravataExperiment>();
        for(AiravataExperiment exp:experiments){
            Date submittedDate = exp.getSubmittedDate();
            if(submittedDate.after(from) && submittedDate.before(to)) {
                newExperiments.add(exp);
            }
        }
        return newExperiments;
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to)throws RegistryException {
        List<AiravataExperiment> experiments = getExperiments(projectName);
        List<AiravataExperiment> newExperiments = new ArrayList<AiravataExperiment>();
        for (AiravataExperiment exp : experiments) {
            Date submittedDate = exp.getSubmittedDate();
            if (submittedDate.after(from) && submittedDate.before(to)) {
                newExperiments.add(exp);
            }
        }
        return newExperiments;
    }

    /**---------------------------------Published Workflow Registry----------------------------------**/

	@Override
	public boolean isPublishedWorkflowExists(String workflowName)
			throws RegistryException {
		return jpa.getGateway().isPublishedWorkflowExists(workflowName);
	}
    
    public void publishWorkflow(String workflowName, String publishWorkflowName) throws RegistryException {
    	GatewayResource gateway = jpa.getGateway();
    	String workflowGraphXML = getWorkflowGraphXML(workflowName);
    	if (gateway.isPublishedWorkflowExists(publishWorkflowName)){
    		throw new PublishedWorkflowAlreadyExistsException(publishWorkflowName);
    	}
    	PublishWorkflowResource publishedWorkflow = gateway.createPublishedWorkflow(publishWorkflowName);
    	publishedWorkflow.setCreatedUser(getUser().getUserName());
    	publishedWorkflow.setContent(workflowGraphXML);
    	publishedWorkflow.setPublishedDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
    	publishedWorkflow.save();
    }

    public void publishWorkflow(String workflowName) throws RegistryException {
    	publishWorkflow(workflowName, workflowName);
    }

    public String getPublishedWorkflowGraphXML(String workflowName) throws RegistryException {
        GatewayResource gateway = jpa.getGateway();
        if (!isPublishedWorkflowExists(workflowName)){
        	throw new PublishedWorkflowDoesNotExistsException(workflowName);
        }
        return gateway.getPublishedWorkflow(workflowName).getContent();
    }
    
	public List<String> getPublishedWorkflowNames() throws RegistryException{
		GatewayResource gateway = jpa.getGateway();
		List<String> result=new ArrayList<String>();
    	List<PublishWorkflowResource> publishedWorkflows = gateway.getPublishedWorkflows();
    	for (PublishWorkflowResource resource : publishedWorkflows) {
			result.add(resource.getName());
		}
    	return result;
	}

    public Map<String,String> getPublishedWorkflows() throws RegistryException{
    	GatewayResource gateway = jpa.getGateway();
    	Map<String,String> result=new HashMap<String, String>();
    	List<PublishWorkflowResource> publishedWorkflows = gateway.getPublishedWorkflows();
    	for (PublishWorkflowResource resource : publishedWorkflows) {
			result.put(resource.getName(), resource.getContent());
		}
    	return result;
    }

    public void removePublishedWorkflow(String workflowName) throws RegistryException {
        GatewayResource gateway = jpa.getGateway();
        if (!isPublishedWorkflowExists(workflowName)){
        	throw new PublishedWorkflowDoesNotExistsException(workflowName);
        }
        gateway.removePublishedWorkflow(workflowName);
    }
    
    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) throws RegistryException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    /**---------------------------------User Workflow Registry----------------------------------**/

	@Override
	public boolean isWorkflowExists(String workflowName)
			throws RegistryException {
		return jpa.getWorker().isWorkflowTemplateExists(workflowName);
	}
	
    public void addWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (isWorkflowExists(workflowName)){
        	throw new UserWorkflowAlreadyExistsException(workflowName);
        }
		UserWorkflowResource workflowResource = worker.createWorkflowTemplate(workflowName);
		workflowResource.setContent(workflowGraphXml);
		workflowResource.save();
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkflowExists(workflowName)){
        	throw new UserWorkflowDoesNotExistsException(workflowName);
        }
		UserWorkflowResource workflowResource = worker.createWorkflowTemplate(workflowName);
		workflowResource.setContent(workflowGraphXml);
		workflowResource.save();
    }

    public String getWorkflowGraphXML(String workflowName) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkflowExists(workflowName)){
        	throw new UserWorkflowDoesNotExistsException(workflowName);
        }
		return worker.getWorkflowTemplate(workflowName).getContent();
    }
    
	@Override
	public Map<String, String> getWorkflows() throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
    	Map<String, String> workflows=new HashMap<String, String>();
    	List<UserWorkflowResource> workflowTemplates = worker.getWorkflowTemplates();
    	for (UserWorkflowResource resource : workflowTemplates) {
    		workflows.put(resource.getName(), resource.getContent());
		}
    	return workflows;
	}

    public void removeWorkflow(String workflowName) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkflowExists(workflowName)){
        	throw new UserWorkflowDoesNotExistsException(workflowName);
        }
		worker.removeWorkflowTemplate(workflowName);
    }
    
    public ResourceMetadata getWorkflowMetadata(String workflowName) throws UnimplementedRegistryOperationException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }
    public void setAiravataRegistry(AiravataRegistry2 registry) {
        //redundant
    }

    public void setAiravataUser(AiravataUser user) {
        setUser(user);
    }

    
    @Override
	public WorkflowExecution getWorkflowExecution(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String arg0,
			int arg1, int arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getWorkflowExecutionIdByUser(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionMetadata(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionName(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String arg0)
			throws RegistryException {
		return null;
	}

	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String instanceID, String nodeID)
			throws RegistryException {
        EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_InstanceID = :workflow_InstanceID AND p.node_id = :node_id");
        q.setParameter("workflow_InstanceID", instanceID);
        q.setParameter("node_id", nodeID);
        Node_Data singleResult = (Node_Data) q.getSingleResult();
        WorkflowServiceIOData workflowIOData = new WorkflowServiceIOData(singleResult.getOutputs(),instanceID,instanceID,null,nodeID,null);
        return workflowIOData;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceStatus getWorkflowExecutionStatus(String instanceID)
			throws RegistryException {
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Workflow_Data p WHERE p.workflow_InstanceID = :workflow_InstanceID");
        q.setParameter("workflow_InstanceID", instanceID);
        Workflow_Data singleResult = (Workflow_Data) q.getSingleResult();
        WorkflowInstanceStatus workflowInstanceStatus = new
                WorkflowInstanceStatus(new WorkflowInstance(singleResult.getExperiment_Data().getExperiment_ID(),singleResult.getTemplate_name())
                ,ExecutionStatus.valueOf(singleResult.getStatus()),new Date(singleResult.getLast_update_time().getTime()));
        return workflowInstanceStatus;
	}

	@Override
	public String getWorkflowExecutionUser(String arg0)
			throws RegistryException {

		return null;
	}

	@Override
	public boolean saveWorkflowData(WorkflowRunTimeData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		Query q = em.createQuery("SELECT p FROM Experiment_Data p WHERE p.experiment_ID = :exp_ID");
		q.setParameter("exp_ID", arg0.getExperimentID());
		Experiment_Data eData = (Experiment_Data) q.getSingleResult();
		
		Workflow_Data wData = new Workflow_Data();
		wData.setExperiment_Data(eData);
		wData.setExperiment_Data(eData);
		wData.setTemplate_name(arg0.getTemplateID());
		wData.setWorkflow_instanceID(arg0.getWorkflowInstanceID());
		wData.setStatus(arg0.getWorkflowStatus().toString());
		wData.setStart_time(arg0.getStartTime());
		
		em.persist(wData);
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionMetadata(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionName(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Experiment_Data expData = new Experiment_Data();
		expData.setExperiment_ID(arg0);
		expData.setName(arg1);
		
		em.persist(expData);
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String arg0, WorkflowIOData arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String arg0, String arg1,
			String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0.getWorkflowInstanceId());
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		Node_Data nData = new Node_Data();
		nData.setWorkflow_Data(wData);
		nData.setNode_id(arg0.getNodeId());
		nData.setInputs(arg0.getValue());
		nData.setNode_type((arg0.getNodeType().getNodeType().toString()));
		nData.setStatus(arg0.getNodeStatus().getExecutionStatus().toString());
		
		em.persist(nData);
	
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub

        EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0.getWorkflowInstanceId());
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();

		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg0.getNodeId());
		Node_Data nData = (Node_Data) q.getSingleResult();
		nData.setOutputs(arg0.getValue());

		em.getTransaction().commit();
		em.close();

		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String arg0, ExecutionStatus arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();

		wData.setStatus(arg1.toString());
		em.persist(wData);
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionUser(String arg0, String arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowLastUpdateTime(String arg0, Timestamp arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeGramData(WorkflowNodeGramData arg0)
			throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg0.getNodeID());
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowNodeGramLocalJobID(String arg0, String arg1,
			String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeLastUpdateTime(String arg0, String arg1,
			Timestamp arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeStatus(String arg0, String arg1,
			ExecutionStatus arg2) throws RegistryException {
		// TODO Auto-generated method stub
		
		EntityManager em = ResourceUtils.getEntityManager();
		em.getTransaction().begin();
		
		Query q = em.createQuery("SELECT w FROM Workflow_Data w WHERE w.workflow_instanceID = :workflow_ID");
		q.setParameter("workflow_ID", arg0);
		Workflow_Data wData = (Workflow_Data) q.getSingleResult();
		
		q = em.createQuery("SELECT p FROM Node_Data p WHERE p.workflow_Data = :workflow_data AND p.node_id = :node_ID");
		q.setParameter("workflow_data", wData);
		q.setParameter("node_ID", arg1);
		Node_Data nData = (Node_Data) q.getSingleResult();
		nData.setStatus(arg2.toString());
		
		em.getTransaction().commit();
		em.close();
		
		return true;
	}

	@Override
	public boolean saveWorkflowStatus(String arg0, WorkflowInstanceStatus arg1)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(
			String arg0, String arg1, String arg2) throws RegistryException {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(
			String arg0, String arg1, String arg2) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionTemplateName(String experimentId,
			String workflowInstanceId) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorkflowExecutionTemplateName(String experimentId,
			String workflowInstanceId) throws RegistryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			WorkflowInstanceStatus status) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

}
