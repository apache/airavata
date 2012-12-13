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
import java.util.regex.Pattern;

import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.JPAResourceAccessor;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ConfigurationResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentDataRetriever;
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
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.InsufficientDataException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.*;
import org.apache.airavata.registry.api.impl.WorkflowInstanceDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataJPARegistry extends AiravataRegistry2{
    private final static Logger logger = LoggerFactory.getLogger(AiravataJPARegistry.class);
    private JPAResourceAccessor jpa;
    private boolean active=false;
    private static final String DEFAULT_PROJECT_NAME = "default";
    private static final Version API_VERSION=new Version("Airavata Registry API",0,6,null,null,null);
    private URI registryConnectionURI;

    private PasswordCallback callback;

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
    	config.setExpireDate(new Timestamp(expire.getTime()));
    	config.save();
    }

    public void addConfiguration(String key, String value, Date expire) {
    	ConfigurationResource config = ResourceUtils.createConfiguration(key);
    	config.setConfigVal(value);
    	config.setExpireDate(new Timestamp(expire.getTime()));
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
        addConfigurationURL(GFAC_URL, uri);
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
    	removeConfiguration(INTERPRETER_URL, uri.toString());
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
        addApplicationDescriptor(serviceDescription.getType().getName(), hostDescriptor.getType().getHostName(), descriptor);
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws RegistryException {
    	if (serviceName==null || hostName==null){
    		throw new InsufficientDataException("Service name or Host name cannot be null");
    	}
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
    	if (serviceName==null || hostName==null){
    		throw new InsufficientDataException("Service name or Host name cannot be null");
    	}
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
    	if (serviceName==null || hostname==null){
    		throw new InsufficientDataException("Service name or Host name cannot be null");
    	}
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

    private String createProjName(String projectName){
    	return createProjName(getGateway().getGatewayName(),getUser().getUserName(),projectName);
    }

    private String createProjName(String gatewayName, String userName, String projectName){
    	return gatewayName+"\n"+userName+"\n"+projectName;
    }

    private String getProjName(String projectLongName){
    	String[] s = projectLongName.split("\n");
    	return s[s.length-1];
    }

	@Override
	public boolean isWorkspaceProjectExists(String projectName)
			throws RegistryException {
		return isWorkspaceProjectExists(projectName, false);
	}

	@Override
	public boolean isWorkspaceProjectExists(String projectName,
			boolean createIfNotExists) throws RegistryException {
		if (jpa.getWorker().isProjectExists(createProjName(projectName))){
			return true;
		}else if (createIfNotExists){
			addWorkspaceProject(new WorkspaceProject(projectName, this));
			return isWorkspaceProjectExists(projectName);
		}else{
			return false;
		}
	}

    public void addWorkspaceProject(WorkspaceProject project) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (isWorkspaceProjectExists(project.getProjectName())){
        	throw new WorkspaceProjectAlreadyExistsException(createProjName(project.getProjectName()));
        }
		ProjectResource projectResource = worker.createProject(createProjName(project.getProjectName()));
		projectResource.save();
    }

    public void updateWorkspaceProject(WorkspaceProject project) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkspaceProjectExists(project.getProjectName())){
        	throw new WorkspaceProjectDoesNotExistsException(createProjName(project.getProjectName()));
        }
		ProjectResource projectResource = worker.getProject(createProjName(project.getProjectName()));
		projectResource.save();
    }

    public void deleteWorkspaceProject(String projectName) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkspaceProjectExists(projectName)){
        	throw new WorkspaceProjectDoesNotExistsException(createProjName(projectName));
        }
		worker.removeProject(createProjName(projectName));
    }

    public WorkspaceProject getWorkspaceProject(String projectName) throws RegistryException {
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkspaceProjectExists(projectName)){
        	throw new WorkspaceProjectDoesNotExistsException(createProjName(projectName));
        }
		ProjectResource projectResource = worker.getProject(createProjName(projectName));
		return new WorkspaceProject(getProjName(projectResource.getName()), this);
    }

    public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException{
    	WorkerResource worker = jpa.getWorker();
    	List<WorkspaceProject> projects=new ArrayList<WorkspaceProject>();
    	List<ProjectResource> projectResouces = worker.getProjects();
    	for (ProjectResource resource : projectResouces) {
			projects.add(new WorkspaceProject(getProjName(resource.getName()), this));
		}
    	return projects;
    }

    public void addExperiment(String projectName, AiravataExperiment experiment) throws RegistryException {
    	WorkspaceProject workspaceProject = getWorkspaceProject(projectName);
    	ProjectResource project = jpa.getWorker().getProject(createProjName(workspaceProject.getProjectName()));
		String experimentId = experiment.getExperimentId();
		if (isExperimentExists(experimentId)){
        	throw new ExperimentDoesNotExistsException(experimentId);
        }
		ExperimentResource experimentResource = project.createExperiment(experimentId);
		if (experiment.getSubmittedDate()!=null) {
			experimentResource.setSubmittedDate(new Timestamp(experiment.getSubmittedDate().getTime()));
		}
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
		e.setProject(new WorkspaceProject(getProjName(resource.getProject().getName()), this));
		return e;
	}

    public List<AiravataExperiment> getExperiments(String projectName)throws RegistryException {
    	ProjectResource project = jpa.getWorker().getProject(createProjName(projectName));
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
    	publishedWorkflow.setPublishedDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
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
		UserWorkflowResource workflowResource = worker.getWorkflowTemplate(workflowName);
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
    public AiravataUser getAiravataUser() {
        return getUser();
    }

    /**---------------------------------Provenance Registry----------------------------------**/

	@Override
	public boolean isExperimentExists(String experimentId, boolean createIfNotPresent)throws RegistryException {
		if (jpa.getWorker().isExperimentExists(experimentId)){
			return true;
		}else if (createIfNotPresent){
			if (!isWorkspaceProjectExists(DEFAULT_PROJECT_NAME, true)){
				throw new WorkspaceProjectDoesNotExistsException(createProjName(DEFAULT_PROJECT_NAME));
			}
			AiravataExperiment experiment = new AiravataExperiment();
			experiment.setExperimentId(experimentId);
			experiment.setSubmittedDate(Calendar.getInstance().getTime());
			experiment.setGateway(getGateway());
			experiment.setUser(getUser());
			addExperiment(DEFAULT_PROJECT_NAME, experiment);
			return jpa.getWorker().isExperimentExists(experimentId);
		}else{
			return false;
		}
	}

	@Override
	public boolean isExperimentExists(String experimentId)
			throws RegistryException {
		return isExperimentExists(experimentId, false);
	}

	@Override
	public void updateExperimentExecutionUser(String experimentId,
			String user) throws RegistryException {
		if (!isExperimentExists(experimentId, true)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		data.setUserName(user);
		data.save();
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
    public boolean isExperimentNameExist(String experimentName) throws RegistryException {
        return (new ExperimentDataRetriever()).isExperimentNameExist(experimentName);
    }


    @Override
	public String getExperimentName(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
        return (new ExperimentDataRetriever()).getExperimentName(experimentId);
	}


	@Override
	public void updateExperimentName(String experimentId,
			String experimentName) throws RegistryException {
		if (!isExperimentExists(experimentId, true)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		data.setExpName(experimentName);
		data.save();
	}


	@Override
	public String getExperimentMetadata(String experimentId)
			throws RegistryException {
		if (!isExperimentExists(experimentId, true)){
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
	public void updateExperimentMetadata(String experimentId, String metadata)
			throws RegistryException {
		if (!isExperimentExists(experimentId, true)){
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
	}


	@Override
	public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		return wi.getTemplateName();
	}


	@Override
	public void setWorkflowInstanceTemplateName(String workflowInstanceId,
			String templateName) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		wi.setTemplateName(templateName);
		wi.save();
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
	public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) throws RegistryException {
		if (jpa.getWorker().isWorkflowInstancePresent(instanceId)){
			return true;
		}else if (createIfNotPresent){
			//we are using the same id for the experiment id for backward compatibility
			//for up to airavata 0.5
			if (!isExperimentExists(instanceId, true)){
				throw new ExperimentDoesNotExistsException(instanceId);
			}
			addWorkflowInstance(instanceId, instanceId, null);
			return isWorkflowInstanceExists(instanceId);
		}else{
			return false;
		}
	}

	@Override
	public boolean isWorkflowInstanceExists(String instanceId)
			throws RegistryException {
		return isWorkflowInstanceExists(instanceId, false);
	}


	@Override
	public void updateWorkflowInstanceStatus(String instanceId,
			ExecutionStatus status) throws RegistryException {
		if (!isWorkflowInstanceExists(instanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(instanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
		Timestamp currentTime = new Timestamp(Calendar.getInstance().getTime().getTime());
		wi.setStatus(status.toString());
		if (status==ExecutionStatus.STARTED){
			wi.setStartTime(currentTime);
		}
		wi.setLastUpdatedTime(currentTime);
		wi.save();
	}


	@Override
	public void updateWorkflowInstanceStatus(WorkflowInstanceStatus status)
			throws RegistryException {
		if (!isWorkflowInstanceExists(status.getWorkflowInstance().getWorkflowInstanceId(), true)){
			throw new WorkflowInstanceDoesNotExistsException(status.getWorkflowInstance().getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(status.getWorkflowInstance().getWorkflowInstanceId());
		Timestamp currentTime = new Timestamp(status.getStatusUpdateTime().getTime());
        if(status.getExecutionStatus() != null){
            wi.setStatus(status.getExecutionStatus().toString());
        }

		if (status.getExecutionStatus()==ExecutionStatus.STARTED){
			wi.setStartTime(currentTime);
		}
		wi.setLastUpdatedTime(currentTime);
		wi.save();
	}


	@Override
	public WorkflowInstanceStatus getWorkflowInstanceStatus(String instanceId)
			throws RegistryException {
		if (!isWorkflowInstanceExists(instanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(instanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
		return new WorkflowInstanceStatus(new WorkflowInstance(wi.getExperimentID(),wi.getWorkflowInstanceID()),wi.getStatus()==null?null:ExecutionStatus.valueOf(wi.getStatus()),wi.getLastUpdatedTime());
	}


	@Override
	public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data)
			throws RegistryException {
		if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId(),true)){
			throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId(), node.getNodeId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId());
		NodeDataResource nodeData = wi.getNodeData(node.getNodeId());
		nodeData.setInputs(data);
		nodeData.save();
	}


	@Override
	public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) throws RegistryException {
		try {
			if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId(),true)){
				throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId(), node.getNodeId());
			}
			WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId());
			NodeDataResource nodeData = wi.getNodeData(node.getNodeId());
			nodeData.setOutputs(data);
			nodeData.save();
		} catch (RegistryException e) {
			e.printStackTrace();
			throw e;
		}
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
	public void saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
	}

	@Deprecated
	@Override
	public void saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
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
        return (new ExperimentDataRetriever()).getExperiment(experimentId);
	}


	@Override
	public List<String> getExperimentIdByUser(String user)
			throws RegistryException {
        if(user == null){
            user = jpa.getWorker().getUser();
        }
        return (new ExperimentDataRetriever()).getExperimentIdByUser(user);
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user)
			throws RegistryException {
        if(user == null){
            user = jpa.getWorker().getUser();
        }
        return (new ExperimentDataRetriever()).getExperiments(user);
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateWorkflowNodeStatus(
			WorkflowInstanceNodeStatus workflowStatusNode)
			throws RegistryException {
		WorkflowInstance workflowInstance = workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance();
		String nodeId = workflowStatusNode.getWorkflowInstanceNode().getNodeId();
		if (!isWorkflowInstanceNodePresent(workflowInstance.getWorkflowInstanceId(), nodeId, true)){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowInstance.getWorkflowInstanceId(), nodeId);
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstance.getWorkflowInstanceId()).getNodeData(nodeId);
		nodeData.setStatus(workflowStatusNode.getExecutionStatus().toString());
		Timestamp t = new Timestamp(workflowStatusNode.getStatusUpdateTime().getTime());
		if (workflowStatusNode.getExecutionStatus()==ExecutionStatus.STARTED){
			nodeData.setStartTime(t);
		}
		nodeData.setLastUpdateTime(t);
		nodeData.save();
		//Each time node status is updated the the time of update for the workflow status is going to be the same
		WorkflowInstanceStatus currentWorkflowInstanceStatus = getWorkflowInstanceStatus(workflowInstance.getWorkflowInstanceId());
		updateWorkflowInstanceStatus(new WorkflowInstanceStatus(workflowInstance, currentWorkflowInstanceStatus.getExecutionStatus(), t));
	}


	@Override
	public void updateWorkflowNodeStatus(String workflowInstanceId,
			String nodeId, ExecutionStatus status) throws RegistryException {
		updateWorkflowNodeStatus(new WorkflowInstanceNode(new WorkflowInstance(workflowInstanceId, workflowInstanceId), nodeId), status);
	}


	@Override
	public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode,
			ExecutionStatus status) throws RegistryException {
		updateWorkflowNodeStatus(new WorkflowInstanceNodeStatus(workflowNode, status, Calendar.getInstance().getTime()));
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
		return new WorkflowInstanceNodeStatus(new WorkflowInstanceNode(new WorkflowInstance(workflowInstance.getExperimentID(), workflowInstance.getWorkflowInstanceID()), nodeData.getNodeID()), nodeData.getStatus()==null?null:ExecutionStatus.valueOf(nodeData.getStatus()),nodeData.getLastUpdateTime());
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
		if (!isWorkflowInstanceExists(workflowInstance.getWorkflowInstanceId(),true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstance.getWorkflowInstanceId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstance.getWorkflowInstanceId());
		return wi.getStartTime();
	}


	@Override
	public void updateWorkflowNodeGramData(
			WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowNodeGramData.getWorkflowInstanceId(),workflowNodeGramData.getNodeID(), true)){
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
	}


	@Override
	public WorkflowInstanceData getWorkflowInstanceData(
			String workflowInstanceId) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId,true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		try{
            WorkflowDataResource resource = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
            WorkflowInstance workflowInstance = new WorkflowInstance(resource.getExperimentID(), resource.getWorkflowInstanceID());
            workflowInstance.setTemplateName(resource.getTemplateName());
            ExperimentData experimentData = getExperiment(workflowInstanceId);
//            WorkflowInstanceData workflowInstanceData = experimentData.getWorkflowInstance(workflowInstanceId);
            WorkflowInstanceData workflowInstanceData = new WorkflowInstanceDataImpl(null, workflowInstance, new WorkflowInstanceStatus(workflowInstance, resource.getStatus()==null? null:ExecutionStatus.valueOf(resource.getStatus()),resource.getLastUpdatedTime()), null);
            List<NodeDataResource> nodeData = resource.getNodeData();
            for (NodeDataResource nodeDataResource : nodeData) {
                workflowInstanceData.getNodeDataList().add(getWorkflowInstanceNodeData(workflowInstanceId, nodeDataResource.getNodeID()));
            }
            return workflowInstanceData;
        } catch (ExperimentLazyLoadedException e) {
            throw new RegistryException(e);
        }

    }


	@Override
	public WorkflowInstanceNodeData getWorkflowInstanceNodeData(
			String workflowInstanceId, String nodeId) throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowInstanceId, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowInstanceId,nodeId);
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstanceId).getNodeData(nodeId);
		WorkflowInstanceNodeData data = new WorkflowInstanceNodeData(new WorkflowInstanceNode(new WorkflowInstance(nodeData.getWorkflowDataResource().getExperimentID(),nodeData.getWorkflowDataResource().getWorkflowInstanceID()),nodeData.getNodeID()));
		data.setInput(nodeData.getInputs());
		data.setOutput(nodeData.getOutputs());
        data.setType(WorkflowNodeType.getType(nodeData.getNodeType()).getNodeType());
		//TODO setup status
		return data;
	}



	@Override
	public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
			String nodeId) throws RegistryException {
		return isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, false);
	}

	@Override
	public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
			String nodeId, boolean createIfNotPresent) throws RegistryException {
		if (!isWorkflowInstanceExists(workflowInstanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		if (jpa.getWorker().getWorkflowInstance(workflowInstanceId).isNodeExists(nodeId)){
			return true;
		}else if (createIfNotPresent){
			addWorkflowInstanceNode(workflowInstanceId, nodeId);
			return isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
		}else{
			return false;
		}
	}


	@Override
	public void addWorkflowInstance(String experimentId,
			String workflowInstanceId, String templateName) throws RegistryException {
		if (!isExperimentExists(experimentId, true)){
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
	}


	@Override
	public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type)
			throws RegistryException {
		try {
			if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId(), true)){
				throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowInstanceId(),node.getNodeId());
			}
			NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowInstanceId()).getNodeData(node.getNodeId());
			nodeData.setNodeType(type.getNodeType().toString());
			nodeData.save();
		} catch (RegistryException e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Override
	public void addWorkflowInstanceNode(String workflowInstanceId,
			String nodeId) throws RegistryException {
		if (isWorkflowInstanceNodePresent(workflowInstanceId, nodeId)){
			throw new WorkflowInstanceNodeAlreadyExistsException(workflowInstanceId, nodeId);
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstanceId).createNodeData(nodeId);
		nodeData.save();
	}





    @Override
	public ExperimentData getExperimentMetaInformation(String experimentId)
			throws RegistryException {
        if (!isExperimentExists(experimentId)){
            throw new ExperimentDoesNotExistsException(experimentId);
        }
        ExperimentDataRetriever experimentDataRetriever = new ExperimentDataRetriever();
        return experimentDataRetriever.getExperimentMetaInformation(experimentId);
	}


	@Override
	public List<ExperimentData> getAllExperimentMetaInformation(String user)
			throws RegistryException {
        ExperimentDataRetriever experimentDataRetriever = new ExperimentDataRetriever();
        return experimentDataRetriever.getAllExperimentMetaInformation(user);
	}


	@Override
	public List<ExperimentData> searchExperiments(String user, String experimentNameRegex)
			throws RegistryException {
		Pattern pattern = Pattern.compile(experimentNameRegex);
		List<ExperimentData> filteredExperiments=new ArrayList<ExperimentData>();
		List<ExperimentData> allExperimentMetaInformation = getAllExperimentMetaInformation(user);
		for (ExperimentData experimentData : allExperimentMetaInformation) {
			if (experimentData.getExperimentName()!=null && pattern.matcher(experimentData.getExperimentName()).find()){
				filteredExperiments.add(experimentData);
			}
		}
		return filteredExperiments;
	}

	@Override
	public Version getVersion() {
		return API_VERSION;
	}


	@Override
	public void setConnectionURI(URI connectionURI) {
		registryConnectionURI=connectionURI;
	}


	@Override
	public URI getConnectionURI() {
		return registryConnectionURI;
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
