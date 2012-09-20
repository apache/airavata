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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.JPAResourceAccessor;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ConfigurationResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentMetadataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.GramDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.NodeDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.PublishWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.ServiceDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkflowDataResource;
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
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceNodeDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;
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

    /**---------------------------------Provenance Registry----------------------------------**/

	@Override
	public boolean isExperimentExists(String experimentId)
			throws RegistryException {
		return jpa.getWorker().isExperimentExists(experimentId);
	}


	@Override
	public boolean updateExperimentExecutionUser(String experimentId,
			String user) throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		data.setUserName(user);
		data.save();
		return true;
	}


	@Override
	public String getExperimentExecutionUser(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		return experiment.getData().getUserName();
	}


	@Override
	public String getExperimentName(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		return experiment.getData().getExpName();
	}


	@Override
	public boolean updateExperimentName(String experimentId,
			String experimentName) throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		data.setExpName(experimentName);
		data.save();
		return false;
	}


	@Override
	public String getExperimentMetadata(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		if (data.isExperimentMetadataPresent()){
			return data.getExperimentMetadata().getMetadata();
		}
		return null;
	}


	@Override
	public boolean updateExperimentMetadata(String experimentId, String metadata)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		ExperimentMetadataResource experimentMetadata;
		if (data.isExperimentMetadataPresent()){
			experimentMetadata = data.getExperimentMetadata();
			experimentMetadata.setMetadata(metadata);
		}else{
			experimentMetadata = data.createExperimentMetadata();
			experimentMetadata.setMetadata(metadata);
		}
		experimentMetadata.save();
		return true;
	}


	@Override
	public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		return wi.getTemplateName();
	}


	@Override
	public void setWorkflowInstanceTemplateName(String workflowInstanceId,
			String templateName) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		wi.setTemplateName(templateName);
	}


	@Override
	public List<WorkflowInstance> getExperimentWorkflowInstances(
			String experimentId) throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		List<WorkflowInstance> result=new ArrayList<WorkflowInstance>();
		List<WorkflowDataResource> workflowInstances = data.getWorkflowInstances();
		for (WorkflowDataResource resource : workflowInstances) {
			WorkflowInstance workflowInstance = new WorkflowInstance(resource.getExperimentID(), resource.getWorkflowInstanceID());
			workflowInstance.setTemplateName(resource.getTemplateName());
			result.add(workflowInstance);
		}
		return result;
	}


	@Override
	public boolean isWorkflowInstanceExists(String instanceId)
			throws RegistryException {
		return jpa.getWorker().isWorkflowInstancePresent(instanceId);
	}


	@Override
	public boolean updateWorkflowInstanceStatus(String instanceId,
			ExecutionStatus status) throws RegistryException {
		if (!isWorkflowInstanceExists(instanceId)){
			throw new WorkflowInstanceDoesNotExistsException(instanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
		Timestamp currentTime = new Timestamp(Calendar.getInstance().getTime().getTime());
		wi.setStatus(status.toString());
		if (status==ExecutionStatus.STARTED){
			wi.setStartTime(currentTime);
		}
		wi.setLastUpdatedTime(currentTime);
		return true;
	}


	@Override
	public boolean updateWorkflowInstanceStatus(WorkflowInstanceStatus status)
			throws RegistryException {
		if (!isWorkflowInstanceExists(status.getWorkflowInstance().getWorkflowInstanceId())){
			throw new WorkflowInstanceDoesNotExistsException(status.getWorkflowInstance().getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(status.getWorkflowInstance().getWorkflowInstanceId());
		Timestamp currentTime = new Timestamp(status.getStatusUpdateTime().getTime());
		wi.setStatus(status.getExecutionStatus().toString());
		if (status.getExecutionStatus()==ExecutionStatus.STARTED){
			wi.setStartTime(currentTime);
		}
		wi.setLastUpdatedTime(currentTime);
		return true;
	}


	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(String instanceId)
			throws RegistryException {
		if (!isWorkflowInstanceExists(instanceId)){
			throw new WorkflowInstanceDoesNotExistsException(instanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
		return new WorkflowInstanceStatus(new WorkflowInstance(wi.getExperimentID(),wi.getWorkflowInstanceID()),ExecutionStatus.valueOf(wi.getStatus()),wi.getLastUpdatedTime());
	}


	@Override
	public boolean updateWorkflowNodeInput(WorkflowInstanceNode node, String data)
			throws RegistryException {
		if (!isWorkflowInstanceExists(node.getWorkflowInstance().getWorkflowInstanceId())){
			throw new WorkflowInstanceDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId());
		NodeDataResource nodeData;
		if (wi.isNodeExists(node.getNodeId())){
			nodeData = wi.getNodeData(node.getNodeId());
		}else{
			nodeData = wi.createNodeData(node.getNodeId());
		}
		nodeData.setInputs(data);
		nodeData.save();
		return true;
	}


	@Override
	public boolean updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) throws RegistryException {
		if (!isWorkflowInstanceExists(node.getWorkflowInstance().getWorkflowInstanceId())){
			throw new WorkflowInstanceDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId());
		NodeDataResource nodeData;
		if (wi.isNodeExists(node.getNodeId())){
			nodeData = wi.getNodeData(node.getNodeId());
		}else{
			nodeData = wi.createNodeData(node.getNodeId());
		}
		nodeData.setOutputs(data);
		nodeData.save();
		return true;
	}


	@Override
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		return null;
	}


	@Override
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		return null;
	}


	@Override
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(
			String workflowInstanceId, String nodeType)
			throws RegistryException {
		return null;
	}


	@Override
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(
			String workflowInstanceId, String nodeType)
			throws RegistryException {
		return null;
	}


	@Deprecated
	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
		return false;
	}

	@Deprecated
	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}


	@Deprecated
	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
			String outputNodeName) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Deprecated
	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Deprecated
	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ExperimentData getExperiment(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		ExperimentData e = new ExperimentDataImpl();
		e.setExperimentId(experiment.getExpID());
		e.setExperimentName(data.getExpName());
		e.setUser(data.getUserName());
		e.setMetadata(getExperimentMetadata(experimentId));
		e.setTopic(experiment.getExpID());
		List<WorkflowInstance> experimentWorkflowInstances = getExperimentWorkflowInstances(experimentId);
		for (WorkflowInstance workflowInstance : experimentWorkflowInstances) {
			e.getWorkflowInstanceData().add(getWorkflowInstanceData(workflowInstance.getWorkflowInstanceId()));
		}
		return e;
	}


	@Override
	public List<String> getExperimentIdByUser(String user)
			throws RegistryException {
		List<String> result=new ArrayList<String>();
		List<ExperimentResource> experiments = jpa.getWorker().getExperiments();
		for (ExperimentResource resource : experiments) {
			if (resource.getData().getUserName().equals(user)){
				result.add(resource.getExpID());
			}
		}
		return result;
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user)
			throws RegistryException {
		List<String> experimentIdByUser = getExperimentIdByUser(user);
		List<ExperimentData> result=new ArrayList<ExperimentData>();
		for (String id : experimentIdByUser) {
			result.add(getExperiment(id));
		}
		return result;
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean updateWorkflowNodeStatus(
			WorkflowInstanceNodeStatus workflowStatusNode)
			throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance().getWorkflowInstanceId(), workflowStatusNode.getWorkflowInstanceNode().getNodeId())){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance().getWorkflowInstanceId(), workflowStatusNode.getWorkflowInstanceNode().getNodeId());
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance().getWorkflowInstanceId()).getNodeData(workflowStatusNode.getWorkflowInstanceNode().getNodeId());
		nodeData.setStatus(workflowStatusNode.getExecutionStatus().toString());
		if (workflowStatusNode.getExecutionStatus()==ExecutionStatus.STARTED){
			nodeData.setStartTime(new Timestamp(workflowStatusNode.getStatusUpdateTime().getTime()));
		}
		nodeData.setLastUpdateTime(new Timestamp(workflowStatusNode.getStatusUpdateTime().getTime()));
		nodeData.save();
		return true;
	}


	@Override
	public boolean updateWorkflowNodeStatus(String workflowInstanceId,
			String nodeId, ExecutionStatus status) throws RegistryException {
		return updateWorkflowNodeStatus(new WorkflowInstanceNode(new WorkflowInstance(workflowInstanceId, workflowInstanceId), nodeId), status);
	}


	@Override
	public boolean updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode,
			ExecutionStatus status) throws RegistryException {
		return updateWorkflowNodeStatus(new WorkflowInstanceNodeStatus(workflowNode, status, Calendar.getInstance().getTime()));
	}


	@Override
	public WorkflowInstanceNodeStatus getWorkflowNodeStatus(
			WorkflowInstanceNode workflowNode) throws RegistryException {
		String id = workflowNode.getWorkflowInstance().getWorkflowInstanceId();
		String nodeId = workflowNode.getNodeId();
		if (!isWorkflowInstanceNodePresent(id, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(id, nodeId);
		}
		WorkflowDataResource workflowInstance = jpa.getWorker().getWorkflowInstance(id);
		NodeDataResource nodeData = workflowInstance.getNodeData(nodeId);
		return new WorkflowInstanceNodeStatus(new WorkflowInstanceNode(new WorkflowInstance(workflowInstance.getExperimentID(), workflowInstance.getWorkflowInstanceID()), nodeData.getNodeID()), ExecutionStatus.valueOf(nodeData.getStatus()),nodeData.getLastUpdateTime());
	}


	@Override
	public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode)
			throws RegistryException {
		String id = workflowNode.getWorkflowInstance().getWorkflowInstanceId();
		String nodeId = workflowNode.getNodeId();
		if (!isWorkflowInstanceNodePresent(id, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(id, nodeId);
		}
		WorkflowDataResource workflowInstance = jpa.getWorker().getWorkflowInstance(id);
		NodeDataResource nodeData = workflowInstance.getNodeData(nodeId);
		return nodeData.getStartTime();
	}


	@Override
	public Date getWorkflowStartTime(WorkflowInstance workflowInstance)
			throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstance.getWorkflowInstanceId())){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstance.getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstance.getWorkflowInstanceId());
		return wi.getStartTime();
	}


	@Override
	public boolean updateWorkflowNodeGramData(
			WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowNodeGramData.getWorkflowInstanceId(),workflowNodeGramData.getNodeID())){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowNodeGramData.getWorkflowInstanceId(),workflowNodeGramData.getNodeID());
		}
		WorkflowDataResource workflowInstance = jpa.getWorker().getWorkflowInstance(workflowNodeGramData.getWorkflowInstanceId());
		GramDataResource gramData;
		if (workflowInstance.isGramDataExists(workflowNodeGramData.getNodeID())){
			gramData = workflowInstance.getGramData(workflowNodeGramData.getNodeID());
		}else{
			gramData = workflowInstance.createGramData(workflowNodeGramData.getNodeID());
		}
		gramData.setInvokedHost(workflowNodeGramData.getInvokedHost());
		gramData.setLocalJobID(workflowNodeGramData.getGramJobID());
		gramData.setRsl(workflowNodeGramData.getRsl());
		gramData.save();
		return true;
	}


	@Override
	public WorkflowInstanceData getWorkflowInstanceData(
			String workflowInstanceId) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource resource = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		WorkflowInstance workflowInstance = new WorkflowInstance(resource.getExperimentID(), resource.getWorkflowInstanceID());
		WorkflowInstanceData workflowInstanceData = new WorkflowInstanceData(null, workflowInstance, new WorkflowInstanceStatus(workflowInstance, ExecutionStatus.valueOf(resource.getStatus()),resource.getLastUpdatedTime()), null);
		List<NodeDataResource> nodeData = resource.getNodeData();
		for (NodeDataResource nodeDataResource : nodeData) {
			workflowInstanceData.getNodeDataList().add(getWorkflowInstanceNodeData(workflowInstanceId, nodeDataResource.getNodeID()));
		}
		return workflowInstanceData;
	}


	@Override
	public WorkflowInstanceNodeData getWorkflowInstanceNodeData(
			String workflowInstanceId, String nodeId) throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowInstanceId,nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowInstanceId,nodeId);
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstanceId).getNodeData(nodeId);
		WorkflowInstanceNodeData data = new WorkflowInstanceNodeData(new WorkflowInstanceNode(new WorkflowInstance(nodeData.getWorkflowDataResource().getExperimentID(),nodeData.getWorkflowDataResource().getWorkflowInstanceID()),nodeData.getNodeID()));
		data.setInput(nodeData.getInputs());
		data.setOutput(nodeData.getOutputs());
		//TODO setup status
		return data;
	}


	@Override
	public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
			String nodeId) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		return jpa.getWorker().getWorkflowInstance(workflowInstanceId).isNodeExists(nodeId);

	}


	@Override
	public boolean addWorkflowInstance(String experimentId,
			String workflowInstanceId, String templateName) throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		if (isWorkflowInstanceExists(workflowInstanceId)){
			throw new WorkflowInstanceAlreadyExistsException(workflowInstanceId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		WorkflowDataResource workflowInstanceResource = data.createWorkflowInstanceResource(workflowInstanceId);
		workflowInstanceResource.setTemplateName(templateName);
		workflowInstanceResource.save();
		return true;
	}


	@Override
	public boolean updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type)
			throws RegistryException {
		if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId())){
			throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId());
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId()).getNodeData(node.getNodeId());
		nodeData.setNodeType(type.getNodeType().toString());
		nodeData.save();
		return false;
	}

}
