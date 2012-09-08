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

import javax.persistence.EntityManagerFactory;

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
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserWorkflowResource;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.descriptor.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.descriptor.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.descriptor.MalformedDescriptorException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
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
    private static final String PERSISTENCE_UNIT_NAME = "airavata_registry";
	private EntityManagerFactory factory;

    @Override
    protected void initialize() {
    	jpa = new JPAResourceAccessor(this);
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
    private static final String INTERPRETER_URL="gfac.url";
    private static final String MESSAGE_BOX_URL="gfac.url";
    private static final String EVENTING_URL="gfac.url";
    
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
    
    public void addHostDescriptor(HostDescription descriptor) throws DescriptorAlreadyExistsException {
        GatewayResource gateway = jpa.getGateway();
        String hostName = descriptor.getType().getHostName();
		if (gateway.isHostDescriptorExists(hostName)){
        	throw new DescriptorAlreadyExistsException(hostName);
        }
        HostDescriptorResource hostDescriptorResource = gateway.createHostDescriptorResource(hostName);
        hostDescriptorResource.setContent(descriptor.toXML());
        hostDescriptorResource.save();
    }

    public void updateHostDescriptor(HostDescription descriptor) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
        String hostName = descriptor.getType().getHostName();
		if (!gateway.isHostDescriptorExists(hostName)){
        	throw new DescriptorDoesNotExistsException(hostName);
        }
        HostDescriptorResource hostDescriptorResource = gateway.getHostDescriptorResource(hostName);
        hostDescriptorResource.setContent(descriptor.toXML());
        hostDescriptorResource.save();
    }

    public HostDescription getHostDescriptor(String hostName) throws DescriptorDoesNotExistsException, MalformedDescriptorException {
        GatewayResource gateway = jpa.getGateway();
		if (!gateway.isHostDescriptorExists(hostName)){
        	throw new DescriptorDoesNotExistsException(hostName);
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

    public void removeHostDescriptor(String hostName) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
		if (!gateway.isHostDescriptorExists(hostName)){
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

    public void addServiceDescriptor(ServiceDescription descriptor) throws DescriptorAlreadyExistsException {
    	GatewayResource gateway = jpa.getGateway();
        String serviceName = descriptor.getType().getName();
		if (gateway.isServiceDescriptorExists(serviceName)){
        	throw new DescriptorAlreadyExistsException(serviceName);
        }
        ServiceDescriptorResource serviceDescriptorResource = gateway.createServiceDescriptorResource(serviceName);
        serviceDescriptorResource.setContent(descriptor.toXML());
        serviceDescriptorResource.save();
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
        String serviceName = descriptor.getType().getName();
		if (!gateway.isServiceDescriptorExists(serviceName)){
        	throw new DescriptorDoesNotExistsException(serviceName);
        }
        ServiceDescriptorResource serviceDescriptorResource = gateway.getServiceDescriptorResource(serviceName);
        serviceDescriptorResource.setContent(descriptor.toXML());
        serviceDescriptorResource.save();
    }

    public ServiceDescription getServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException, MalformedDescriptorException {
    	GatewayResource gateway = jpa.getGateway();
		if (!gateway.isHostDescriptorExists(serviceName)){
        	throw new DescriptorDoesNotExistsException(serviceName);
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

    public void removeServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
		if (!gateway.isServiceDescriptorExists(serviceName)){
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
    
    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws DescriptorAlreadyExistsException {
        addApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws DescriptorAlreadyExistsException {
    	GatewayResource gateway = jpa.getGateway();
        String applicationName = descriptor.getType().getApplicationName().getStringValue();
        applicationName = createAppName(serviceName, hostName, applicationName);
		if (gateway.isApplicationDescriptorExists(applicationName)){
        	throw new DescriptorAlreadyExistsException(applicationName);
        }
        ApplicationDescriptorResource applicationDescriptorResource = gateway.createApplicationDescriptorResource(applicationName);
        applicationDescriptorResource.setServiceDescName(serviceName);
        applicationDescriptorResource.setHostDescName(hostName);
        applicationDescriptorResource.setContent(descriptor.toXML());
        applicationDescriptorResource.save();
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) throws DescriptorDoesNotExistsException {
    	updateApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
    	String applicationName = descriptor.getType().getApplicationName().getStringValue();
        applicationName = createAppName(serviceName, hostName, applicationName);
		if (!gateway.isApplicationDescriptorExists(applicationName)){
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

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws DescriptorDoesNotExistsException {
    	GatewayResource gateway = jpa.getGateway();
    	String appName = createAppName(serviceName, hostName, applicationName);
		if (!gateway.isApplicationDescriptorExists(appName)) {
			throw new DescriptorDoesNotExistsException(appName);
		}
		gateway.removeApplicationDescriptor(appName);
    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) throws UnimplementedRegistryOperationException {
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    /**---------------------------------Project Registry----------------------------------**/

    public void addWorkspaceProject(WorkspaceProject project) {
        GatewayResource gatewayResource = jpa.getGateway();
        ProjectResource resource = (ProjectResource)gatewayResource.create(ResourceType.PROJECT);
        resource.setName(project.getProjectName());
        //todo fix the IDs to Names
//        resource.setUserID(getUser().getUserName());
        resource.save();
    }

    public void updateWorkspaceProject(WorkspaceProject project) {
        addWorkspaceProject(project);
    }

    public void deleteWorkspaceProject(String projectName) {
        GatewayResource gatewayResource = jpa.getGateway();
        gatewayResource.remove(ResourceType.PROJECT,projectName);
    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        GatewayResource gatewayResource = jpa.getGateway();
        ProjectResource resource = (ProjectResource)gatewayResource.get(ResourceType.PROJECT, projectName);
        WorkspaceProject workspaceProject = new WorkspaceProject(projectName, this);
        return workspaceProject;
    }

    public void createExperiment(String projectName, AiravataExperiment experiment) {
        GatewayResource gatewayResource = jpa.getGateway();
        ExperimentResource resource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);
        resource.setExpID(experiment.getExperimentId());
        resource.setSubmittedDate(new java.sql.Date(experiment.getSubmittedDate().getTime()));
        resource.save();
    }

    public void removeExperiment(String experimentId) {
        GatewayResource gatewayResource = jpa.getGateway();
        gatewayResource.remove(ResourceType.EXPERIMENT, experimentId);
    }

    public List<AiravataExperiment> getExperiments() {
        UserResource userResource = new UserResource();
        userResource.setUserName(getUser().getUserName());
        List<Resource> resources = userResource.get(ResourceType.EXPERIMENT);
        List<AiravataExperiment> result = new ArrayList<AiravataExperiment>();
        for(Resource resource:resources) {
            AiravataExperiment airavataExperiment = new AiravataExperiment();
            airavataExperiment.setExperimentId(((ExperimentResource) resource).getExpID());
            airavataExperiment.setUser(getUser());
            airavataExperiment.setSubmittedDate(new java.sql.Date(((ExperimentResource) resource).getSubmittedDate().getTime()));
            result.add(airavataExperiment);
        }
        return result;
    }

    public List<AiravataExperiment> getExperiments(String projectName) {
        ProjectResource projectResource = new ProjectResource();
        projectResource.setName(projectName);
        List<Resource> resources = projectResource.get(ResourceType.EXPERIMENT);
        List<AiravataExperiment> result = new ArrayList<AiravataExperiment>();
        for(Resource resource:resources) {
            AiravataExperiment airavataExperiment = new AiravataExperiment();
            airavataExperiment.setExperimentId(((ExperimentResource) resource).getExpID());
            airavataExperiment.setUser(getUser());
            airavataExperiment.setSubmittedDate(new java.sql.Date(((ExperimentResource) resource).getSubmittedDate().getTime()));
            result.add(airavataExperiment);
        }
        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to) {
        List<AiravataExperiment> experiments = getExperiments();
        List<AiravataExperiment> newExperiments = new ArrayList<AiravataExperiment>();
        for(AiravataExperiment exp:experiments){
            Date submittedDate = exp.getSubmittedDate();
            if(submittedDate.after(from) && submittedDate.before(to)) {
                newExperiments.add(exp);
            }
        }
        return newExperiments;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) {
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

    public void publishWorkflow(String workflowName, String publishWorkflowName) {
        UserResource userResource = new UserResource();
        userResource.setUserName(getUser().getUserName());
        UserWorkflowResource resource = (UserWorkflowResource)userResource.get(ResourceType.USER_WORKFLOW, workflowName);
        GatewayResource gatewayResource = jpa.getGateway();
        PublishWorkflowResource resource1 = (PublishWorkflowResource)gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        resource1.setContent(resource.getContent());
        resource1.setPublishedDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
        resource1.setName(publishWorkflowName);
        //todo have to set version
    }

    public void publishWorkflow(String workflowName) {
        UserResource userResource = new UserResource();
        userResource.setUserName(getUser().getUserName());
        UserWorkflowResource resource = (UserWorkflowResource)userResource.get(ResourceType.USER_WORKFLOW, workflowName);
        GatewayResource gatewayResource = jpa.getGateway();
        PublishWorkflowResource resource1 = (PublishWorkflowResource)gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        resource1.setContent(resource.getContent());
        resource1.setPublishedDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
        //todo is this right ????
        resource1.setName(workflowName);
    }

    public String getPublishedWorkflowGraphXML(String workflowName) {
        GatewayResource gatewayResource = jpa.getGateway();
        PublishWorkflowResource resource1 = (PublishWorkflowResource) gatewayResource.get(ResourceType.PUBLISHED_WORKFLOW, workflowName);
        return resource1.getContent();
    }

    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removePublishedWorkflow(String workflowName) {
        GatewayResource gatewayResource = jpa.getGateway();
        gatewayResource.remove(ResourceType.PUBLISHED_WORKFLOW, workflowName);
    }

    public void addWorkflow(String workflowName, String workflowGraphXml) {
        ProjectResource projectResource = new ProjectResource();
        UserWorkflowResource resource = (UserWorkflowResource)projectResource.create(ResourceType.USER_WORKFLOW);
        resource.setName(workflowName);
        resource.setContent(workflowGraphXml);
        resource.setLastUpdateDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
        resource.save();
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) {
         addWorkflow(workflowName,workflowGraphXml);
    }

    public String getWorkflowGraphXML(String workflowName) {
        GatewayResource gatewayResource = jpa.getGateway();
        UserResource resource = (UserResource)gatewayResource.get(ResourceType.USER_WORKFLOW, getUser().getUserName());
        UserWorkflowResource resource1 = (UserWorkflowResource) resource.get(ResourceType.USER_WORKFLOW, workflowName);
        return resource1.getContent();
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflow(String workflowName) {
        GatewayResource gatewayResource = jpa.getGateway();
        UserResource resource = (UserResource)gatewayResource.get(ResourceType.USER_WORKFLOW, getUser().getUserName());
        resource.remove(ResourceType.USER_WORKFLOW, workflowName);
    }

    public void setAiravataRegistry(AiravataRegistry2 registry) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAiravataUser(AiravataUser user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public boolean saveWorkflowExecutionUser(String experimentId, String user)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getWorkflowExecutionUser(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionName(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveWorkflowExecutionName(String experimentId,
			String workflowIntanceName) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			ExecutionStatus status) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkflowExecutionMetadata(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveWorkflowExecutionMetadata(String experimentId,
			String metadata) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionServiceInput(
			WorkflowServiceIOData workflowInputData) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionServiceOutput(
			WorkflowServiceIOData workflowOutputData) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
			String outputNodeName) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowExecution getWorkflowExecution(String experimentId)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getWorkflowExecutionIdByUser(String user)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveWorkflowData(WorkflowRunTimeData runTimeData)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowStatus(String workflowInstanceID,
			WorkflowInstanceStatus workflowStatus) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowLastUpdateTime(String workflowInstanceID,
			Timestamp lastUpdateTime) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeStatus(String workflowInstanceID,
			String workflowNodeID, ExecutionStatus status)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeLastUpdateTime(String workflowInstanceID,
			String workflowNodeID, Timestamp lastUpdateTime)
			throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeGramData(
			WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveWorkflowNodeGramLocalJobID(String workflowInstanceID,
			String workflowNodeID, String localJobID) throws RegistryException {
		// TODO Auto-generated method stub
		return false;
	}

}
