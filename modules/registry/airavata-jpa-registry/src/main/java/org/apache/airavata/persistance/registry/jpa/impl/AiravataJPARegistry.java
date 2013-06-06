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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.JPAResourceAccessor;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ConfigurationResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExecutionErrorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentDataRetriever;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentMetadataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.GFacJobDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
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
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataSubRegistry;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.ConfigurationRegistry;
import org.apache.airavata.registry.api.DescriptorRegistry;
import org.apache.airavata.registry.api.ExecutionErrors;
import org.apache.airavata.registry.api.ExecutionErrors.Source;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.ProjectsRegistry;
import org.apache.airavata.registry.api.ProvenanceRegistry;
import org.apache.airavata.registry.api.PublishedWorkflowRegistry;
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.UserWorkflowRegistry;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.RegistryAPIVersionIncompatibleException;
import org.apache.airavata.registry.api.exception.RegistryAccessorInstantiateException;
import org.apache.airavata.registry.api.exception.RegistryAccessorNotFoundException;
import org.apache.airavata.registry.api.exception.RegistryAccessorUndefinedException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.UnimplementedRegistryOperationException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.InsufficientDataException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.exception.worker.ApplicationJobAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.ApplicationJobDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.InvalidApplicationJobIDException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceNodeAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkflowInstanceNodeDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.util.RegistryConstants;
import org.apache.airavata.registry.api.workflow.ExecutionError;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.ExperimentExecutionError;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;
import org.apache.airavata.registry.api.workflow.ApplicationJobExecutionError;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.NodeExecutionError;
import org.apache.airavata.registry.api.workflow.NodeExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionError;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus.State;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataJPARegistry extends AiravataRegistry2{
    private final static Logger logger = LoggerFactory.getLogger(AiravataJPARegistry.class);
    private JPAResourceAccessor jpa;
    private boolean active=false;
    private static final String DEFAULT_PROJECT_NAME = "default";
    private static final Version API_VERSION=new Version("Airavata Registry API",0,8,null,null,null);
    private URI registryConnectionURI;
    private ConfigurationRegistry configurationRegistry;
    private DescriptorRegistry descriptorRegistry;
    private ProjectsRegistry projectsRegistry;
    private ProvenanceRegistry provenanceRegistry;
    private UserWorkflowRegistry userWorkflowRegistry;
    private PublishedWorkflowRegistry publishedWorkflowRegistry;
    private static Map<String, String[]> compatibleVersionMap;



    private PasswordCallback callback;

    @Override
    protected void initialize() throws RegistryException {
    	jpa = new JPAResourceAccessor(this);
    	//TODO check if the db connections are proper & accessible & the relevant db/tables are
    	//present
    	active=true;

        initializeCustomRegistries();
        String apiVersion = getVersion().toString();
        String registryVersion = getConfiguration("registry.version").toString();
        if (!apiVersion.equals(registryVersion)){
           throw new RegistryAPIVersionIncompatibleException("Incompatible registry versions. Please check whether you updated the API and Registry " +
                   "versions.");
        }
        String[] list = compatibleVersionMap.get(apiVersion);
        if (list == null){
            throw new RegistryAPIVersionIncompatibleException("Incompatible registry versions. Please check whether you updated the API and Registry " +
                    "versions.");
        }
        if (!Arrays.asList(list).contains(registryVersion)){
            throw new RegistryAPIVersionIncompatibleException("Incompatible registry versions. Please check whether you updated the API and Registry " +
                    "versions.");
        }
    }

    static {
        compatibleVersionMap = new HashMap<String, String[]>();
        compatibleVersionMap.put("0.6", new String[]{"0.6"});
        compatibleVersionMap.put("0.7", new String[]{"0.6", "0.7"});
        compatibleVersionMap.put("0.8", new String[]{"0.8"});
    }

    /**
     * Initialize the custom registries defined in the registry settings
     * @throws RegistryException
     */
	private void initializeCustomRegistries() throws RegistryException {
		// retrieving user defined registry classes from registry settings
        try {
            configurationRegistry = (ConfigurationRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.CONFIGURATION_REGISTRY_ACCESSOR_CLASS);
            descriptorRegistry = (DescriptorRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.DESCRIPTOR_REGISTRY_ACCESSOR_CLASS);
            projectsRegistry = (ProjectsRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.PROJECT_REGISTRY_ACCESSOR_CLASS);
            provenanceRegistry = (ProvenanceRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.PROVENANCE_REGISTRY_ACCESSOR_CLASS);
            userWorkflowRegistry = (UserWorkflowRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.USER_WF_REGISTRY_ACCESSOR_CLASS);
            publishedWorkflowRegistry = (PublishedWorkflowRegistry)getClassInstance(ConfigurationRegistry.class,RegistryConstants.PUBLISHED_WF_REGISTRY_ACCESSOR_CLASS);
        } catch (AiravataConfigurationException e) {
            throw new RegistryException("An error occured when attempting to determine any custom implementations of the registries!!!", e);
        }
	}

    private <T extends AiravataSubRegistry> Object getClassInstance(Class<T> c, String registryAccessorKey) throws AiravataConfigurationException{
		try {
			T registryClass = c.cast(AiravataRegistryFactory.getRegistryClass(registryAccessorKey));
			registryClass.setAiravataRegistry(this);
			return registryClass;
		} catch (ClassCastException e){
			logger.error("The class defined for accessor type "+registryAccessorKey+" MUST be an extention of the interface "+c.getName(),e);
		} catch (RegistryAccessorNotFoundException e) {
			logger.error("Error in loading class for registry accessor "+registryAccessorKey,e);
		} catch (RegistryAccessorUndefinedException e) {
			// happens when user has not defined an accessor for the registry accessor key
			// thus ignore error
		} catch (RegistryAccessorInstantiateException e) {
			logger.error("Error in instantiating instance from class for registry accessor "+registryAccessorKey,e);
		}
		return null;
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
        if (configurationRegistry != null){
            return configurationRegistry.getConfigurationList(key);
        } else {
            List<Object> values = new ArrayList<Object>();
            List<ConfigurationResource> configurations = ResourceUtils.getConfigurations(key);
            for (ConfigurationResource configurationResource : configurations) {
                values.add(configurationResource.getConfigVal());
            }
            return values;
        }

    }

    public void setConfiguration(String key, String value, Date expire) {
        if (configurationRegistry != null){
            configurationRegistry.setConfiguration(key, value, expire);
        }else {
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
    }

    public void addConfiguration(String key, String value, Date expire) {
        if (configurationRegistry != null){
            configurationRegistry.addConfiguration(key, value, expire);
        } else {
            ConfigurationResource config = ResourceUtils.createConfiguration(key);
            config.setConfigVal(value);
            config.setExpireDate(new Timestamp(expire.getTime()));
            config.save();
        }


    }

    public void removeAllConfiguration(String key) {
        if (configurationRegistry  != null){
            configurationRegistry.removeAllConfiguration(key);
        } else {
            ResourceUtils.removeConfiguration(key);
        }

    }

    public void removeConfiguration(String key, String value) {
        if (configurationRegistry != null){
            configurationRegistry.removeConfiguration(key, value);
        } else {
            ResourceUtils.removeConfiguration(key, value);
        }
    }

    private static final String GFAC_URL="gfac.url";
    private static final String INTERPRETER_URL="interpreter.url";
    private static final String MESSAGE_BOX_URL="messagebox.url";
    private static final String EVENTING_URL="eventing.url";

    public List<URI> getGFacURIs() {
        if (configurationRegistry != null) {
            return configurationRegistry.getGFacURIs();
        } else {
            return retrieveURIsFromConfiguration(GFAC_URL);
        }
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
        if (configurationRegistry != null) {
            return configurationRegistry.getWorkflowInterpreterURIs();
        }  else {
            return retrieveURIsFromConfiguration(INTERPRETER_URL);
        }
    }

    public URI getEventingServiceURI() {
        if (configurationRegistry != null) {
           return configurationRegistry.getEventingServiceURI();
        }else {
            List<URI> eventingURLs = retrieveURIsFromConfiguration(EVENTING_URL);
            return eventingURLs.size()==0? null: eventingURLs.get(0);
        }
    }

    public URI getMessageBoxURI() {
        if (configurationRegistry != null) {
            return configurationRegistry.getMessageBoxURI();
        }
    	List<URI> messageboxURLs = retrieveURIsFromConfiguration(MESSAGE_BOX_URL);
		return messageboxURLs.size()==0? null: messageboxURLs.get(0);
    }

    public void addGFacURI(URI uri) {
        if (configurationRegistry != null) {
            addGFacURI(uri);
        } else {
            addConfigurationURL(GFAC_URL, uri);
        }
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
        if (configurationRegistry != null) {
            configurationRegistry.addWorkflowInterpreterURI(uri);
        }else {
            addConfigurationURL(INTERPRETER_URL,uri);
        }
    }

    public void setEventingURI(URI uri) {
        if (configurationRegistry != null) {
            configurationRegistry.setEventingURI(uri);
        } else {
            addConfigurationURL(EVENTING_URL,uri);
        }
    }

    public void setMessageBoxURI(URI uri) {
        if (configurationRegistry != null) {
            configurationRegistry.setMessageBoxURI(uri);
        } else {
            addConfigurationURL(MESSAGE_BOX_URL,uri);
        }
    }

    public void addGFacURI(URI uri, Date expire) {
        if (configurationRegistry != null) {
            configurationRegistry.addGFacURI(uri, expire);
        } else {
            addConfigurationURL(GFAC_URL, uri, expire);
        }
    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        if (configurationRegistry != null) {
            configurationRegistry.addWorkflowInterpreterURI(uri, expire);
        } else {
            addConfigurationURL(INTERPRETER_URL, uri, expire);
        }
    }

    public void setEventingURI(URI uri, Date expire) {
        if (configurationRegistry != null) {
            configurationRegistry.setEventingURI(uri, expire);
        } else {
            addConfigurationURL(EVENTING_URL, uri, expire);
        }
    }

    public void setMessageBoxURI(URI uri, Date expire) {
        if (configurationRegistry != null) {
            configurationRegistry.setMessageBoxURI(uri, expire);
        } else {
    	    addConfigurationURL(MESSAGE_BOX_URL, uri, expire);
        }
    }

    public void removeGFacURI(URI uri) {
        if (configurationRegistry != null) {
            configurationRegistry.removeGFacURI(uri);
        } else {
            removeConfiguration(GFAC_URL, uri.toString());
        }
    }

    public void removeWorkflowInterpreterURI(URI uri) {
        if (configurationRegistry != null) {
            configurationRegistry.removeWorkflowInterpreterURI(uri);
        } else {
            removeConfiguration(INTERPRETER_URL, uri.toString());
        }
    }

    public void removeAllGFacURI() {
        if (configurationRegistry != null) {
            configurationRegistry.removeAllGFacURI();
        } else {
            removeAllConfiguration(GFAC_URL);
        }
    }

    public void removeAllWorkflowInterpreterURI() {
        if (configurationRegistry != null) {
            configurationRegistry.removeAllWorkflowInterpreterURI();
        } else {
            removeAllConfiguration(INTERPRETER_URL);
        }
    }

    public void unsetEventingURI() {
        if (configurationRegistry != null) {
            configurationRegistry.unsetEventingURI();
        } else {
            removeAllConfiguration(EVENTING_URL);
        }

    }

    public void unsetMessageBoxURI() {
        if (configurationRegistry != null) {
            configurationRegistry.unsetMessageBoxURI();
        } else {
            removeAllConfiguration(MESSAGE_BOX_URL);
        }

    }

    /**---------------------------------Descriptor Registry----------------------------------**/

    public boolean isHostDescriptorExists(String descriptorName)throws RegistryException{
        if (descriptorRegistry != null){
            return descriptorRegistry.isHostDescriptorExists(descriptorName);
        }
    	return jpa.getGateway().isHostDescriptorExists(descriptorName);
    }
    public void addHostDescriptor(HostDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null){
            descriptorRegistry.addHostDescriptor(descriptor);
        } else {
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
    }

    public void updateHostDescriptor(HostDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null){
            descriptorRegistry.updateHostDescriptor(descriptor);
        } else {
            GatewayResource gateway = jpa.getGateway();
            String hostName = descriptor.getType().getHostName();
            if (!isHostDescriptorExists(hostName)){
                throw new DescriptorDoesNotExistsException(hostName);
            }
            HostDescriptorResource hostDescriptorResource = gateway.getHostDescriptorResource(hostName);
            hostDescriptorResource.setContent(descriptor.toXML());
            hostDescriptorResource.save();
        }
    }

    public HostDescription getHostDescriptor(String hostName) throws RegistryException {
        if (descriptorRegistry != null){
            return descriptorRegistry.getHostDescriptor(hostName);
        } else {
            GatewayResource gateway = jpa.getGateway();
            if (!isHostDescriptorExists(hostName)){
                return null;
            }
            HostDescriptorResource hostDescriptorResource = gateway.getHostDescriptorResource(hostName);
            return createHostDescriptor(hostDescriptorResource);
        }
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
        if (descriptorRegistry != null){
            descriptorRegistry.removeHostDescriptor(hostName);
        } else {
            GatewayResource gateway = jpa.getGateway();
            if (!isHostDescriptorExists(hostName)){
                throw new DescriptorDoesNotExistsException(hostName);
            }
            gateway.removeHostDescriptor(hostName);
            try {
                //we need to delete the application descriptors bound to this host
				Map<String, ApplicationDescription> applicationDescriptors = getApplicationDescriptorsFromHostName(hostName);
				for (String serviceName : applicationDescriptors.keySet()) {
					removeApplicationDescriptor(serviceName, hostName, applicationDescriptors.get(serviceName).getType().getApplicationName().getStringValue());
				}
			} catch (Exception e) {
				logger.error("Error while removing application descriptors bound to host "+hostName, e);
			}
        }
    }

	@Override
	public List<HostDescription> getHostDescriptors()
			throws MalformedDescriptorException, RegistryException {
        if (descriptorRegistry != null){
            return descriptorRegistry.getHostDescriptors();
        }
		GatewayResource gateway = jpa.getGateway();
		List<HostDescription> list=new ArrayList<HostDescription>();
		List<HostDescriptorResource> hostDescriptorResources = gateway.getHostDescriptorResources();
		for (HostDescriptorResource resource : hostDescriptorResources) {
			list.add(createHostDescriptor(resource));
		}
		return list;
	}

    public ResourceMetadata getHostDescriptorMetadata(String hostName) throws RegistryException {
    	if (descriptorRegistry != null) {
            return descriptorRegistry.getHostDescriptorMetadata(hostName);
        }
    	//TODO
        throw new UnimplementedRegistryOperationException();
    }

    public boolean isServiceDescriptorExists(String descriptorName)throws RegistryException{
        if (descriptorRegistry != null) {
            return descriptorRegistry.isServiceDescriptorExists(descriptorName);
        }
    	return jpa.getGateway().isServiceDescriptorExists(descriptorName);
    }

    public void addServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null) {
            descriptorRegistry.addServiceDescriptor(descriptor);
        }else {
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
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null) {
            descriptorRegistry.updateServiceDescriptor(descriptor);
        }else {
            GatewayResource gateway = jpa.getGateway();
            String serviceName = descriptor.getType().getName();
            if (!isServiceDescriptorExists(serviceName)){
                throw new DescriptorDoesNotExistsException(serviceName);
            }
            ServiceDescriptorResource serviceDescriptorResource = gateway.getServiceDescriptorResource(serviceName);
            serviceDescriptorResource.setContent(descriptor.toXML());
            serviceDescriptorResource.save();
        }
    }

    public ServiceDescription getServiceDescriptor(String serviceName) throws RegistryException, MalformedDescriptorException {
        if (descriptorRegistry != null) {
            return descriptorRegistry.getServiceDescriptor(serviceName);
        }else {
            GatewayResource gateway = jpa.getGateway();
            if (!gateway.isServiceDescriptorExists(serviceName)){
                return null;
            }
            ServiceDescriptorResource serviceDescriptorResource = gateway.getServiceDescriptorResource(serviceName);
            return createServiceDescriptor(serviceDescriptorResource);
        }
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
        if (descriptorRegistry != null) {
            descriptorRegistry.removeServiceDescriptor(serviceName);
        }else {
            GatewayResource gateway = jpa.getGateway();
            if (!isServiceDescriptorExists(serviceName)){
                throw new DescriptorDoesNotExistsException(serviceName);
            }
            gateway.removeServiceDescriptor(serviceName);
            try {
				//we need to delete the application descriptors bound to this service
				Map<String, ApplicationDescription> applicationDescriptors = getApplicationDescriptors(serviceName);
				for (String hostName : applicationDescriptors.keySet()) {
					removeApplicationDescriptor(serviceName, hostName, applicationDescriptors.get(hostName).getType().getApplicationName().getStringValue());
				}
			} catch (Exception e) {
				logger.error("Error while removing application descriptors bound to service "+serviceName, e);
			}
        }
    }

    @Override
	public List<ServiceDescription> getServiceDescriptors()
			throws MalformedDescriptorException, RegistryException {
        if (descriptorRegistry != null) {
            return descriptorRegistry.getServiceDescriptors();
        }else {
            GatewayResource gateway = jpa.getGateway();
            List<ServiceDescription> list=new ArrayList<ServiceDescription>();
            List<ServiceDescriptorResource> serviceDescriptorResources = gateway.getServiceDescriptorResources();
            for (ServiceDescriptorResource resource : serviceDescriptorResources) {
                list.add(createServiceDescriptor(resource));
            }
            return list;
        }
	}

    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) throws RegistryException {
        if (descriptorRegistry != null) {
            return descriptorRegistry.getServiceDescriptorMetadata(serviceName);
        }else {
            //TODO
            throw new UnimplementedRegistryOperationException();
        }
    }

    private String createAppName(String serviceName, String hostName, String applicationName){
    	return serviceName+"/"+hostName+"/"+applicationName;
    }

    public boolean isApplicationDescriptorExists(String serviceName,
                                                 String hostName,
                                                 String descriptorName)throws RegistryException{
        if (descriptorRegistry != null) {
            return descriptorRegistry.isApplicationDescriptorExists(serviceName, hostName, descriptorName);
        }else {
            return jpa.getGateway().isApplicationDescriptorExists(createAppName(serviceName, hostName, descriptorName));
        }
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription,
                                         HostDescription hostDescriptor,
                                         ApplicationDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null) {
            descriptorRegistry.addApplicationDescriptor(serviceDescription, hostDescriptor, descriptor);
        }else {
            addApplicationDescriptor(serviceDescription.getType().getName(), hostDescriptor.getType().getHostName(), descriptor);
        }
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null){
            descriptorRegistry.addApplicationDescriptor(serviceName, hostName, descriptor);
        }  else {
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
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription,
                                            HostDescription hostDescriptor,
                                            ApplicationDescription descriptor) throws RegistryException {
        if (descriptorRegistry != null){
            descriptorRegistry.udpateApplicationDescriptor(serviceDescription,hostDescriptor,descriptor);
        } else {
            updateApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
        }
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDescription descriptor) throws RegistryException {
    	if (descriptorRegistry != null){
            descriptorRegistry.updateApplicationDescriptor(serviceName, hostName, descriptor);
        } else {
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
    }
    private ApplicationDescription createApplicationDescriptor(
			ApplicationDescriptorResource applicationDescriptorResource)
			throws MalformedDescriptorException {
		try {
            return ApplicationDescription.fromXML(applicationDescriptorResource.getContent());
        } catch (XmlException e) {
            throw new MalformedDescriptorException(applicationDescriptorResource.getName(),e);
        }
	}

    public ApplicationDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName)throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException{
        if (descriptorRegistry != null){
            return descriptorRegistry.getApplicationDescriptor(serviceName, hostname, applicationName);
        }
        if (serviceName==null || hostname==null){
    		throw new InsufficientDataException("Service name or Host name cannot be null");
    	}
    	GatewayResource gateway = jpa.getGateway();
		if (!isApplicationDescriptorExists(serviceName,hostname,applicationName)){
        	throw new DescriptorDoesNotExistsException(createAppName(serviceName, hostname, applicationName));
        }
        return createApplicationDescriptor(gateway.getApplicationDescriptorResource(createAppName(serviceName, hostname, applicationName)));
    }

    public ApplicationDescription getApplicationDescriptors(String serviceName, String hostname) throws RegistryException {
        if (descriptorRegistry != null){
            return descriptorRegistry.getApplicationDescriptors(serviceName, hostname);
        }
        GatewayResource gateway = jpa.getGateway();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources(serviceName, hostname);
		if (applicationDescriptorResources.size()>0){
			return createApplicationDescriptor(applicationDescriptorResources.get(0));
		}
		return null;
    }

    public Map<String, ApplicationDescription> getApplicationDescriptors(String serviceName) throws RegistryException {
        if (descriptorRegistry != null){
            return descriptorRegistry.getApplicationDescriptors(serviceName);
        }
        GatewayResource gateway = jpa.getGateway();
		Map<String, ApplicationDescription> map=new HashMap<String,ApplicationDescription>();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources(serviceName, null);
		for (ApplicationDescriptorResource resource : applicationDescriptorResources) {
			map.put(resource.getHostDescName(),createApplicationDescriptor(resource));
		}
		return map;
    }

    private Map<String,ApplicationDescription> getApplicationDescriptorsFromHostName(String hostName)throws RegistryException {
        GatewayResource gateway = jpa.getGateway();
		Map<String, ApplicationDescription> map=new HashMap<String,ApplicationDescription>();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources(null, hostName);
		for (ApplicationDescriptorResource resource : applicationDescriptorResources) {
			map.put(resource.getServiceDescName(),createApplicationDescriptor(resource));
		}
		return map;
    }
    
    public Map<String[],ApplicationDescription> getApplicationDescriptors()throws MalformedDescriptorException, RegistryException{
        if (descriptorRegistry != null){
            return descriptorRegistry.getApplicationDescriptors();
        }
        GatewayResource gateway = jpa.getGateway();
		Map<String[], ApplicationDescription> map=new HashMap<String[],ApplicationDescription>();
		List<ApplicationDescriptorResource> applicationDescriptorResources = gateway.getApplicationDescriptorResources();
		for (ApplicationDescriptorResource resource : applicationDescriptorResources) {
			map.put(new String[]{resource.getServiceDescName(),resource.getHostDescName()},createApplicationDescriptor(resource));
		}
		return map;
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws RegistryException {
        if (descriptorRegistry != null){
             descriptorRegistry.removeApplicationDescriptor(serviceName, hostName, applicationName);
        } else {
            GatewayResource gateway = jpa.getGateway();
            String appName = createAppName(serviceName, hostName, applicationName);
            if (!isApplicationDescriptorExists(serviceName,hostName,applicationName)){
                throw new DescriptorDoesNotExistsException(appName);
            }
            gateway.removeApplicationDescriptor(appName);
        }

    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) throws RegistryException {
    	if (descriptorRegistry != null) {
            return descriptorRegistry.getApplicationDescriptorMetadata(serviceName, hostName, applicationName);
        }
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
        if (projectsRegistry != null){
            return projectsRegistry.isWorkspaceProjectExists(projectName);
        }
		return isWorkspaceProjectExists(projectName, false);
	}

	@Override
	public boolean isWorkspaceProjectExists(String projectName,
			boolean createIfNotExists) throws RegistryException {
        if (projectsRegistry != null){
            return projectsRegistry.isWorkspaceProjectExists(projectName, createIfNotExists);
        }
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
        if (projectsRegistry != null){
            projectsRegistry.addWorkspaceProject(project);
        } else {
            WorkerResource worker = jpa.getWorker();
            if (isWorkspaceProjectExists(project.getProjectName())){
                throw new WorkspaceProjectAlreadyExistsException(createProjName(project.getProjectName()));
            }
            ProjectResource projectResource = worker.createProject(createProjName(project.getProjectName()));
            projectResource.save();
        }
    }

    public void updateWorkspaceProject(WorkspaceProject project) throws RegistryException {
        if (projectsRegistry != null){
            projectsRegistry.updateWorkspaceProject(project);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (!isWorkspaceProjectExists(project.getProjectName())){
                throw new WorkspaceProjectDoesNotExistsException(createProjName(project.getProjectName()));
            }
            ProjectResource projectResource = worker.getProject(createProjName(project.getProjectName()));
            projectResource.save();
        }
    }

    public void deleteWorkspaceProject(String projectName) throws RegistryException {
        if (projectsRegistry != null){
            projectsRegistry.deleteWorkspaceProject(projectName);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (!isWorkspaceProjectExists(projectName)){
                throw new WorkspaceProjectDoesNotExistsException(createProjName(projectName));
            }
            worker.removeProject(createProjName(projectName));
        }
    }

    public WorkspaceProject getWorkspaceProject(String projectName) throws RegistryException {
        if (projectsRegistry != null){
            return projectsRegistry.getWorkspaceProject(projectName);
        }
    	WorkerResource worker = jpa.getWorker();
		if (!isWorkspaceProjectExists(projectName)){
        	throw new WorkspaceProjectDoesNotExistsException(createProjName(projectName));
        }
		ProjectResource projectResource = worker.getProject(createProjName(projectName));
		return new WorkspaceProject(getProjName(projectResource.getName()), this);
    }

    public List<WorkspaceProject> getWorkspaceProjects() throws RegistryException{
        if (projectsRegistry != null){
            return projectsRegistry.getWorkspaceProjects();
        }
    	WorkerResource worker = jpa.getWorker();
    	List<WorkspaceProject> projects=new ArrayList<WorkspaceProject>();
    	List<ProjectResource> projectResouces = worker.getProjects();
    	for (ProjectResource resource : projectResouces) {
			projects.add(new WorkspaceProject(getProjName(resource.getName()), this));
		}
    	return projects;
    }

    public void addExperiment(String projectName, AiravataExperiment experiment) throws RegistryException {
    	if (projectsRegistry != null){
            projectsRegistry.addExperiment(projectName, experiment);
        }else {
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
    }

    public void removeExperiment(String experimentId) throws ExperimentDoesNotExistsException {
        if (projectsRegistry != null){
            projectsRegistry.removeExperiment(experimentId);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (!worker.isExperimentExists(experimentId)){
                throw new ExperimentDoesNotExistsException(experimentId);
            }
            worker.removeExperiment(experimentId);
        }
    }

    public List<AiravataExperiment> getExperiments() throws RegistryException{
        if (projectsRegistry != null){
            return projectsRegistry.getExperiments();
        }
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
        if (projectsRegistry != null){
            return projectsRegistry.getExperiments(projectName);
        }
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
        if (projectsRegistry != null){
            return projectsRegistry.getExperiments(from, to);
        }
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
        if (projectsRegistry != null){
            return projectsRegistry.getExperiments(projectName, from, to);
        }
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
        if (publishedWorkflowRegistry != null){
            return publishedWorkflowRegistry.isPublishedWorkflowExists(workflowName);
        }
		return jpa.getGateway().isPublishedWorkflowExists(workflowName);
	}

    public void publishWorkflow(String workflowName, String publishWorkflowName) throws RegistryException {
        if (publishedWorkflowRegistry != null){
            publishedWorkflowRegistry.publishWorkflow(workflowName, publishWorkflowName);
        } else {
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
    }

    public void publishWorkflow(String workflowName) throws RegistryException {
        if (publishedWorkflowRegistry != null){
            publishedWorkflowRegistry.publishWorkflow(workflowName);
        } else {
            publishWorkflow(workflowName, workflowName);
        }
    }

    public String getPublishedWorkflowGraphXML(String workflowName) throws RegistryException {
        if (publishedWorkflowRegistry != null){
            return publishedWorkflowRegistry.getPublishedWorkflowGraphXML(workflowName);
        }
        GatewayResource gateway = jpa.getGateway();
        if (!isPublishedWorkflowExists(workflowName)){
        	throw new PublishedWorkflowDoesNotExistsException(workflowName);
        }
        return gateway.getPublishedWorkflow(workflowName).getContent();
    }

	public List<String> getPublishedWorkflowNames() throws RegistryException{
        if (publishedWorkflowRegistry != null){
            return publishedWorkflowRegistry.getPublishedWorkflowNames();
        }
		GatewayResource gateway = jpa.getGateway();
		List<String> result=new ArrayList<String>();
    	List<PublishWorkflowResource> publishedWorkflows = gateway.getPublishedWorkflows();
    	for (PublishWorkflowResource resource : publishedWorkflows) {
			result.add(resource.getName());
		}
    	return result;
	}

    public Map<String,String> getPublishedWorkflows() throws RegistryException{
        if (publishedWorkflowRegistry != null){
            return publishedWorkflowRegistry.getPublishedWorkflows();
        }
    	GatewayResource gateway = jpa.getGateway();
    	Map<String,String> result=new HashMap<String, String>();
    	List<PublishWorkflowResource> publishedWorkflows = gateway.getPublishedWorkflows();
    	for (PublishWorkflowResource resource : publishedWorkflows) {
			result.put(resource.getName(), resource.getContent());
		}
    	return result;
    }

    public void removePublishedWorkflow(String workflowName) throws RegistryException {
        if (publishedWorkflowRegistry != null){
            publishedWorkflowRegistry.removePublishedWorkflow(workflowName);
        } else {
            GatewayResource gateway = jpa.getGateway();
            if (!isPublishedWorkflowExists(workflowName)){
                throw new PublishedWorkflowDoesNotExistsException(workflowName);
            }
            gateway.removePublishedWorkflow(workflowName);
        }
    }

    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) throws RegistryException {
        if (publishedWorkflowRegistry != null){
            return publishedWorkflowRegistry.getPublishedWorkflowMetadata(workflowName);
        }
        //TODO
        throw new UnimplementedRegistryOperationException();
    }

    /**---------------------------------User Workflow Registry----------------------------------**/

	@Override
	public boolean isWorkflowExists(String workflowName)
			throws RegistryException {
        if (userWorkflowRegistry != null){
           return userWorkflowRegistry.isWorkflowExists(workflowName);
        }
		return jpa.getWorker().isWorkflowTemplateExists(workflowName);
	}

    public void addWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
        if (userWorkflowRegistry != null){
            userWorkflowRegistry.addWorkflow(workflowName, workflowGraphXml);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (isWorkflowExists(workflowName)){
                throw new UserWorkflowAlreadyExistsException(workflowName);
            }
            UserWorkflowResource workflowResource = worker.createWorkflowTemplate(workflowName);
            workflowResource.setContent(workflowGraphXml);
            workflowResource.save();
        }
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) throws RegistryException {
        if (userWorkflowRegistry != null){
            userWorkflowRegistry.updateWorkflow(workflowName, workflowGraphXml);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (!isWorkflowExists(workflowName)){
                throw new UserWorkflowDoesNotExistsException(workflowName);
            }
            UserWorkflowResource workflowResource = worker.getWorkflowTemplate(workflowName);
            workflowResource.setContent(workflowGraphXml);
            workflowResource.save();
        }
    }

    public String getWorkflowGraphXML(String workflowName) throws RegistryException {
        if (userWorkflowRegistry != null){
            return userWorkflowRegistry.getWorkflowGraphXML(workflowName);
        }
        WorkerResource worker = jpa.getWorker();
		if (!isWorkflowExists(workflowName)){
        	throw new UserWorkflowDoesNotExistsException(workflowName);
        }
		return worker.getWorkflowTemplate(workflowName).getContent();
    }

	@Override
	public Map<String, String> getWorkflows() throws RegistryException {
        if (userWorkflowRegistry != null){
            return userWorkflowRegistry.getWorkflows();
        }
        WorkerResource worker = jpa.getWorker();
    	Map<String, String> workflows=new HashMap<String, String>();
    	List<UserWorkflowResource> workflowTemplates = worker.getWorkflowTemplates();
    	for (UserWorkflowResource resource : workflowTemplates) {
    		workflows.put(resource.getName(), resource.getContent());
		}
    	return workflows;
	}

    public void removeWorkflow(String workflowName) throws RegistryException {
        if (userWorkflowRegistry != null){
            userWorkflowRegistry.removeWorkflow(workflowName);
        }else {
            WorkerResource worker = jpa.getWorker();
            if (!isWorkflowExists(workflowName)){
                throw new UserWorkflowDoesNotExistsException(workflowName);
            }
            worker.removeWorkflowTemplate(workflowName);
        }
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) throws RegistryException {
        if (userWorkflowRegistry != null){
            return userWorkflowRegistry.getWorkflowMetadata(workflowName);
        }
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
		if (provenanceRegistry != null){
             return provenanceRegistry.isExperimentExists(experimentId, createIfNotPresent);
        }
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
        if (provenanceRegistry != null){
            return provenanceRegistry.isExperimentExists(experimentId);
        }
		return isExperimentExists(experimentId, false);
	}

	@Override
	public void updateExperimentExecutionUser(String experimentId,
			String user) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateExperimentExecutionUser(experimentId, user);
        }else {
            if (!isExperimentExists(experimentId, true)){
                throw new ExperimentDoesNotExistsException(experimentId);
            }
            ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
            ExperimentDataResource data = experiment.getData();
            data.setUserName(user);
            data.save();
        }
	}


	@Override
	public String getExperimentExecutionUser(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentExecutionUser(experimentId);
        }
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		return experiment.getData().getUserName();
	}

    @Override
    public boolean isExperimentNameExist(String experimentName) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.isExperimentNameExist(experimentName);
        }
        return (new ExperimentDataRetriever()).isExperimentNameExist(experimentName);
    }


    @Override
	public String getExperimentName(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentName(experimentId);
        }
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
        return (new ExperimentDataRetriever()).getExperimentName(experimentId);
	}


	@Override
	public void updateExperimentName(String experimentId,
			String experimentName) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateExperimentName(experimentId, experimentName);
        }else {
            if (!isExperimentExists(experimentId, true)){
                throw new ExperimentDoesNotExistsException(experimentId);
            }
            ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
            ExperimentDataResource data = experiment.getData();
            data.setExpName(experimentName);
            data.save();
        }
	}


	@Override
	public String getExperimentMetadata(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentMetadata(experimentId);
        }
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
        if (provenanceRegistry != null){
            provenanceRegistry.updateExperimentMetadata(experimentId, metadata);
        }else {
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
	}


	@Override
	public String getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowExecutionTemplateName(workflowInstanceId);
        }
		if (!isWorkflowInstanceExists(workflowInstanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
		return wi.getTemplateName();
	}


	@Override
	public void setWorkflowInstanceTemplateName(String workflowInstanceId,
			String templateName) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
        }else {
            if (!isWorkflowInstanceExists(workflowInstanceId, true)){
                throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
            }
            WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
            wi.setTemplateName(templateName);
            wi.save();
        }
	}


	@Override
	public List<WorkflowExecution> getExperimentWorkflowInstances(
			String experimentId) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentWorkflowInstances(experimentId);
        }
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExperimentResource experiment = jpa.getWorker().getExperiment(experimentId);
		ExperimentDataResource data = experiment.getData();
		List<WorkflowExecution> result=new ArrayList<WorkflowExecution>();
		List<WorkflowDataResource> workflowInstances = data.getWorkflowInstances();
		for (WorkflowDataResource resource : workflowInstances) {
			WorkflowExecution workflowInstance = new WorkflowExecution(resource.getExperimentID(), resource.getWorkflowInstanceID());
			workflowInstance.setTemplateName(resource.getTemplateName());
			result.add(workflowInstance);
		}
		return result;
	}


	@Override
	public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.isWorkflowInstanceExists(instanceId, createIfNotPresent);
        }
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
        if (provenanceRegistry != null){
            return provenanceRegistry.isWorkflowInstanceExists(instanceId);
        }
        return isWorkflowInstanceExists(instanceId, false);
	}


	@Override
	public void updateWorkflowInstanceStatus(String instanceId,
			State status) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowInstanceStatus(instanceId, status);
        }else {
            if (!isWorkflowInstanceExists(instanceId, true)){
                throw new WorkflowInstanceDoesNotExistsException(instanceId);
            }
            WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
            Timestamp currentTime = new Timestamp(Calendar.getInstance().getTime().getTime());
            wi.setStatus(status.toString());
            if (status==State.STARTED){
                wi.setStartTime(currentTime);
            }
            wi.setLastUpdatedTime(currentTime);
            wi.save();
        }
	}


	@Override
	public void updateWorkflowInstanceStatus(WorkflowExecutionStatus status)
			throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowInstanceStatus(status);
        }else {
            if (!isWorkflowInstanceExists(status.getWorkflowInstance().getWorkflowExecutionId(), true)){
                throw new WorkflowInstanceDoesNotExistsException(status.getWorkflowInstance().getWorkflowExecutionId());
            }
            WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(status.getWorkflowInstance().getWorkflowExecutionId());
            Timestamp currentTime = new Timestamp(status.getStatusUpdateTime().getTime());
            if(status.getExecutionStatus() != null){
                wi.setStatus(status.getExecutionStatus().toString());
            }

            if (status.getExecutionStatus()==State.STARTED){
                wi.setStartTime(currentTime);
            }
            wi.setLastUpdatedTime(currentTime);
            wi.save();
        }
	}


	@Override
	public WorkflowExecutionStatus getWorkflowInstanceStatus(String instanceId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowInstanceStatus(instanceId);
        }
        if (!isWorkflowInstanceExists(instanceId, true)){
			throw new WorkflowInstanceDoesNotExistsException(instanceId);
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(instanceId);
		return new WorkflowExecutionStatus(new WorkflowExecution(wi.getExperimentID(),wi.getWorkflowInstanceID()),wi.getStatus()==null?null:State.valueOf(wi.getStatus()),wi.getLastUpdatedTime());
	}


	@Override
	public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data)
			throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeInput(node, data);
        }else {
            if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowExecutionId(),node.getNodeId(),true)){
                throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowExecutionId(), node.getNodeId());
            }
            WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowExecutionId());
            NodeDataResource nodeData = wi.getNodeData(node.getNodeId());
            nodeData.setInputs(data);
            nodeData.save();
        }
	}


	@Override
	public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeOutput(node, data);
        }else {
            try {
                if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowExecutionId(),node.getNodeId(),true)){
                    throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowExecutionId(), node.getNodeId());
                }
                WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowExecutionId());
                NodeDataResource nodeData = wi.getNodeData(node.getNodeId());
                nodeData.setOutputs(data);
                nodeData.save();
            } catch (RegistryException e) {
                e.printStackTrace();
                throw e;
            }
        }
	}


	@Override
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.searchWorkflowInstanceNodeInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
        }
        return null;
	}


	@Override
	public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.searchWorkflowInstanceNodeOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
        }
        return null;
	}


	@Override
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(
			String workflowInstanceId, String nodeType)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowInstanceNodeInput(workflowInstanceId, nodeType);
        }
        return null;
	}


	@Override
	public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(
			String workflowInstanceId, String nodeType)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowInstanceNodeOutput(workflowInstanceId, nodeType);
        }
        return null;
	}


	@Deprecated
	@Override
	public void saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
        }
	}

	@Deprecated
	@Override
	public void saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.saveWorkflowExecutionOutput(experimentId, data);
        }
	}

	@Deprecated
	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
			String outputNodeName) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowExecutionOutput(experimentId, outputNodeName);
        }
		// TODO Auto-generated method stub
		return null;
	}


	@Deprecated
	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowExecutionOutput(experimentId);
        }
		// TODO Auto-generated method stub
		return null;
	}


	@Deprecated
	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowExecutionOutputNames(exeperimentId);
        }
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ExperimentData getExperiment(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperiment(experimentId);
        }
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
        return (new ExperimentDataRetriever()).getExperiment(experimentId);
	}


	@Override
	public List<String> getExperimentIdByUser(String user)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentIdByUser(user);
        }
        if(user == null){
            user = jpa.getWorker().getUser();
        }
        return (new ExperimentDataRetriever()).getExperimentIdByUser(user);
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentByUser(user);
        }
        if(user == null){
            user = jpa.getWorker().getUser();
        }
        return (new ExperimentDataRetriever()).getExperiments(user);
	}


	@Override
	public List<ExperimentData> getExperimentByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentByUser(user, pageSize, pageNo);
        }
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateWorkflowNodeStatus(NodeExecutionStatus workflowStatusNode) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeStatus(workflowStatusNode);
        }else {
            WorkflowExecution workflowInstance = workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance();
            String nodeId = workflowStatusNode.getWorkflowInstanceNode().getNodeId();
            if (!isWorkflowInstanceNodePresent(workflowInstance.getWorkflowExecutionId(), nodeId, true)){
                throw new WorkflowInstanceNodeDoesNotExistsException(workflowInstance.getWorkflowExecutionId(), nodeId);
            }
            NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstance.getWorkflowExecutionId()).getNodeData(nodeId);
            nodeData.setStatus(workflowStatusNode.getExecutionStatus().toString());
            Timestamp t = new Timestamp(workflowStatusNode.getStatusUpdateTime().getTime());
            if (workflowStatusNode.getExecutionStatus()==State.STARTED){
                nodeData.setStartTime(t);
            }
            nodeData.setLastUpdateTime(t);
            nodeData.save();
            //Each time node status is updated the the time of update for the workflow status is going to be the same
            WorkflowExecutionStatus currentWorkflowInstanceStatus = getWorkflowInstanceStatus(workflowInstance.getWorkflowExecutionId());
            updateWorkflowInstanceStatus(new WorkflowExecutionStatus(workflowInstance, currentWorkflowInstanceStatus.getExecutionStatus(), t));
        }
	}


	@Override
	public void updateWorkflowNodeStatus(String workflowInstanceId,
			String nodeId, State status) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeStatus(workflowInstanceId, nodeId, status);
        }else {
            updateWorkflowNodeStatus(new WorkflowInstanceNode(new WorkflowExecution(workflowInstanceId, workflowInstanceId), nodeId), status);
        }

	}


	@Override
	public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode,
			State status) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeStatus(workflowNode, status);
        }else {
            updateWorkflowNodeStatus(new NodeExecutionStatus(workflowNode, status, Calendar.getInstance().getTime()));
        }
	}


	@Override
	public NodeExecutionStatus getWorkflowNodeStatus(
			WorkflowInstanceNode workflowNode) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowNodeStatus(workflowNode);
        }
        String id = workflowNode.getWorkflowInstance().getWorkflowExecutionId();
		String nodeId = workflowNode.getNodeId();
		if (!isWorkflowInstanceNodePresent(id, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(id, nodeId);
		}
		WorkflowDataResource workflowInstance = jpa.getWorker().getWorkflowInstance(id);
		NodeDataResource nodeData = workflowInstance.getNodeData(nodeId);
		return new NodeExecutionStatus(new WorkflowInstanceNode(new WorkflowExecution(workflowInstance.getExperimentID(), workflowInstance.getWorkflowInstanceID()), nodeData.getNodeID()), nodeData.getStatus()==null?null:State.valueOf(nodeData.getStatus()),nodeData.getLastUpdateTime());
	}


	@Override
	public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowNodeStartTime(workflowNode);
        }
        String id = workflowNode.getWorkflowInstance().getWorkflowExecutionId();
		String nodeId = workflowNode.getNodeId();
		if (!isWorkflowInstanceNodePresent(id, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(id, nodeId);
		}
		WorkflowDataResource workflowInstance = jpa.getWorker().getWorkflowInstance(id);
		NodeDataResource nodeData = workflowInstance.getNodeData(nodeId);
		return nodeData.getStartTime();
	}


	@Override
	public Date getWorkflowStartTime(WorkflowExecution workflowInstance)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowStartTime(workflowInstance);
        }
        if (!isWorkflowInstanceExists(workflowInstance.getWorkflowExecutionId(),true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstance.getWorkflowExecutionId());
		}
		WorkflowDataResource wi = jpa.getWorker().getWorkflowInstance(workflowInstance.getWorkflowExecutionId());
		return wi.getStartTime();
	}


	@Override
	public void updateWorkflowNodeGramData(
			WorkflowNodeGramData workflowNodeGramData) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeGramData(workflowNodeGramData);
        }else {
        	ApplicationJob job = new ApplicationJob();
        	job.setJobId(workflowNodeGramData.getGramJobID());
        	job.setHostDescriptionId(workflowNodeGramData.getInvokedHost());
        	job.setExperimentId(workflowNodeGramData.getWorkflowInstanceId());
        	job.setWorkflowExecutionId(workflowNodeGramData.getWorkflowInstanceId());
        	job.setNodeId(workflowNodeGramData.getNodeID());
        	job.setJobData(workflowNodeGramData.getRsl());
        	if (isApplicationJobExists(job.getJobId())){
        		updateApplicationJob(job);
        	}else{
        		addApplicationJob(job);
        	}
        }
	}


	@Override
	public WorkflowExecutionData getWorkflowInstanceData(
			String workflowInstanceId) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowInstanceData(workflowInstanceId);
        }
        if (!isWorkflowInstanceExists(workflowInstanceId,true)){
			throw new WorkflowInstanceDoesNotExistsException(workflowInstanceId);
		}
		try{
            WorkflowDataResource resource = jpa.getWorker().getWorkflowInstance(workflowInstanceId);
            WorkflowExecution workflowInstance = new WorkflowExecution(resource.getExperimentID(), resource.getWorkflowInstanceID());
            workflowInstance.setTemplateName(resource.getTemplateName());
            WorkflowExecutionData workflowInstanceData = new WorkflowExecutionDataImpl(null, workflowInstance, new WorkflowExecutionStatus(workflowInstance, resource.getStatus()==null? null:State.valueOf(resource.getStatus()),resource.getLastUpdatedTime()), null);
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
	public NodeExecutionData getWorkflowInstanceNodeData(
			String workflowInstanceId, String nodeId) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowInstanceNodeData(workflowInstanceId, nodeId);
        }
        if (!isWorkflowInstanceNodePresent(workflowInstanceId, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowInstanceId,nodeId);
		}
		NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstanceId).getNodeData(nodeId);
		NodeExecutionData data = new NodeExecutionData(new WorkflowInstanceNode(new WorkflowExecution(nodeData.getWorkflowDataResource().getExperimentID(),nodeData.getWorkflowDataResource().getWorkflowInstanceID()),nodeData.getNodeID()));
		data.setInput(nodeData.getInputs());
		data.setOutput(nodeData.getOutputs());
        data.setType(WorkflowNodeType.getType(nodeData.getNodeType()).getNodeType());
		//TODO setup status
		return data;
	}



	@Override
	public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
			String nodeId) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
        }
        return isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, false);
	}

	@Override
	public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
			String nodeId, boolean createIfNotPresent) throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, createIfNotPresent);
        }
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
        if (provenanceRegistry != null){
            provenanceRegistry.addWorkflowInstance(experimentId,workflowInstanceId, templateName);
        }else {
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
	}


	@Override
	public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type)
			throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.updateWorkflowNodeType(node, type);
        }else {
            try {
                if (!isWorkflowInstanceNodePresent(node.getWorkflowInstance().getWorkflowExecutionId(),node.getNodeId(), true)){
                    throw new WorkflowInstanceNodeDoesNotExistsException(node.getWorkflowInstance().getWorkflowExecutionId(),node.getNodeId());
                }
                NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(node.getWorkflowInstance().getWorkflowExecutionId()).getNodeData(node.getNodeId());
                nodeData.setNodeType(type.getNodeType().toString());
                nodeData.save();
            } catch (RegistryException e) {
                e.printStackTrace();
                throw e;
            }
        }
	}


	@Override
	public void addWorkflowInstanceNode(String workflowInstanceId,
			String nodeId) throws RegistryException {
        if (provenanceRegistry != null){
            provenanceRegistry.addWorkflowInstanceNode(workflowInstanceId, nodeId);
        }else {
            if (isWorkflowInstanceNodePresent(workflowInstanceId, nodeId)){
                throw new WorkflowInstanceNodeAlreadyExistsException(workflowInstanceId, nodeId);
            }
            NodeDataResource nodeData = jpa.getWorker().getWorkflowInstance(workflowInstanceId).createNodeData(nodeId);
            nodeData.save();
        }

	}

    @Override
	public ExperimentData getExperimentMetaInformation(String experimentId)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentMetaInformation(experimentId);
        }
        if (!isExperimentExists(experimentId)){
            throw new ExperimentDoesNotExistsException(experimentId);
        }
        ExperimentDataRetriever experimentDataRetriever = new ExperimentDataRetriever();
        return experimentDataRetriever.getExperimentMetaInformation(experimentId);
	}


	@Override
	public List<ExperimentData> getAllExperimentMetaInformation(String user)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.getAllExperimentMetaInformation(user);
        }
        ExperimentDataRetriever experimentDataRetriever = new ExperimentDataRetriever();
        return experimentDataRetriever.getAllExperimentMetaInformation(user);
	}


	@Override
	public List<ExperimentData> searchExperiments(String user, String experimentNameRegex)
			throws RegistryException {
        if (provenanceRegistry != null){
            return provenanceRegistry.searchExperiments(user, experimentNameRegex);
        }
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

	@Override
	public List<ExperimentExecutionError> getExperimentExecutionErrors(
			String experimentId) throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getExperimentExecutionErrors(experimentId);
        }
		List<ExperimentExecutionError> result=new ArrayList<ExperimentExecutionError>();
		List<ExecutionErrorResource> executionErrors = jpa.getWorker().getExperiment(experimentId).getData().getExecutionErrors(Source.EXPERIMENT.toString(), experimentId, null, null, null);
		for (ExecutionErrorResource errorResource : executionErrors) {
			ExperimentExecutionError error = new ExperimentExecutionError();
			setupValues(errorResource, error);
			error.setExperimentId(errorResource.getExperimentDataResource().getExperimentID());
			result.add(error);
		}
		return result;
	}

	@Override
	public List<WorkflowExecutionError> getWorkflowExecutionErrors(
			String experimentId, String workflowInstanceId)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getWorkflowExecutionErrors(experimentId, workflowInstanceId);
        }
		List<WorkflowExecutionError> result=new ArrayList<WorkflowExecutionError>();
		List<ExecutionErrorResource> executionErrors = jpa.getWorker().getExperiment(experimentId).getData().getExecutionErrors(Source.WORKFLOW.toString(), experimentId, workflowInstanceId, null, null);
		for (ExecutionErrorResource errorResource : executionErrors) {
			WorkflowExecutionError error = new WorkflowExecutionError();
			setupValues(errorResource, error);
			error.setExperimentId(errorResource.getExperimentDataResource().getExperimentID());
			error.setWorkflowInstanceId(errorResource.getWorkflowDataResource().getWorkflowInstanceID());
			result.add(error);
		}
		return result;
	}

	@Override
	public List<NodeExecutionError> getNodeExecutionErrors(String experimentId,
			String workflowInstanceId, String nodeId) throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getNodeExecutionErrors(experimentId, workflowInstanceId, nodeId);
        }
		List<NodeExecutionError> result=new ArrayList<NodeExecutionError>();
		List<ExecutionErrorResource> executionErrors = jpa.getWorker().getExperiment(experimentId).getData().getExecutionErrors(Source.NODE.toString(), experimentId, workflowInstanceId, nodeId, null);
		for (ExecutionErrorResource errorResource : executionErrors) {
			NodeExecutionError error = new NodeExecutionError();
			setupValues(errorResource, error);
			error.setExperimentId(errorResource.getExperimentDataResource().getExperimentID());
			error.setNodeId(errorResource.getNodeID());
			error.setWorkflowInstanceId(errorResource.getWorkflowDataResource().getWorkflowInstanceID());
			result.add(error);
		}
		return result;
	}

	@Override
	public List<ApplicationJobExecutionError> getApplicationJobErrors(String experimentId,
			String workflowInstanceId, String nodeId, String gfacJobId)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getApplicationJobErrors(experimentId, workflowInstanceId, nodeId, gfacJobId);
        }
		List<ApplicationJobExecutionError> result=new ArrayList<ApplicationJobExecutionError>();
		List<ExecutionErrorResource> executionErrors = jpa.getWorker().getExperiment(experimentId).getData().getExecutionErrors(Source.APPLICATION.toString(), experimentId, workflowInstanceId, nodeId, gfacJobId);
		for (ExecutionErrorResource errorResource : executionErrors) {
			ApplicationJobExecutionError error = new ApplicationJobExecutionError();
			setupValues(errorResource, error);
			error.setExperimentId(errorResource.getExperimentDataResource().getExperimentID());
			error.setJobId(errorResource.getGfacJobID());
			error.setNodeId(errorResource.getNodeID());
			error.setWorkflowInstanceId(errorResource.getWorkflowDataResource().getWorkflowInstanceID());
			result.add(error);
		}
		return result;
	}

	private void setupValues(ExecutionErrorResource source,
			ExecutionError destination) {
		destination.setActionTaken(source.getActionTaken());
		destination.setErrorCode(source.getErrorCode());
		destination.setErrorDescription(source.getErrorDes());
		destination.setErrorLocation(source.getErrorLocation());
		destination.setErrorMessage(source.getErrorMsg());
		destination.setErrorReported(source.getErrorReporter());
		destination.setErrorTime(source.getErrorTime());
		destination.setSource(Source.valueOf(source.getSourceType()));
		destination.setErrorReference(source.getErrorReference());
	}

	@Override
	public List<ApplicationJobExecutionError> getApplicationJobErrors(String gfacJobId)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getApplicationJobErrors(gfacJobId);
        }
		return getApplicationJobErrors(null, null, null, gfacJobId);
	}

	@Override
	public List<ExecutionError> getExecutionErrors(String experimentId,
			String workflowInstanceId, String nodeId, String gfacJobId,
			Source... filterBy) throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.getExecutionErrors(experimentId, workflowInstanceId, nodeId, gfacJobId, filterBy);
        }
		List<ExecutionError> errors = new ArrayList<ExecutionError>();
		for (Source sourceType : filterBy) {
			if (sourceType==Source.ALL){
				errors.addAll(getExperimentExecutionErrors(experimentId));
				errors.addAll(getWorkflowExecutionErrors(experimentId, workflowInstanceId));
				errors.addAll(getNodeExecutionErrors(experimentId, workflowInstanceId, nodeId));
				errors.addAll(getApplicationJobErrors(experimentId, workflowInstanceId, nodeId, gfacJobId));
				break;
			} else if (sourceType==Source.EXPERIMENT){
				errors.addAll(getExperimentExecutionErrors(experimentId));
			} else if (sourceType==Source.WORKFLOW){
				errors.addAll(getWorkflowExecutionErrors(experimentId, workflowInstanceId));
			} else if (sourceType==Source.NODE){
				errors.addAll(getNodeExecutionErrors(experimentId, workflowInstanceId, nodeId));
			} else if (sourceType==Source.APPLICATION){
				errors.addAll(getApplicationJobErrors(experimentId, workflowInstanceId, nodeId, gfacJobId));
			}
		}
		return errors;
	}
	
//	@Override
//	public List<ExecutionError> getAllExperimentErrors(String experimentId,
//			Source... filterBy) throws RegistryException {
//		return getExecutionErrors(experimentId, null, null, null, filterBy);
//	}
//	@Override
//	public List<ExecutionError> getAllWorkflowErrors(String experimentId,
//			String workflowInstanceId, Source... filterBy)
//			throws RegistryException {
//		return getExecutionErrors(experimentId, workflowInstanceId, null, null, filterBy);
//	}
//	@Override
//	public List<ExecutionError> getAllNodeErrors(String experimentId,
//			String workflowInstanceId, String nodeId, Source... filterBy)
//			throws RegistryException {
//		return getExecutionErrors(experimentId, workflowInstanceId, nodeId, null, filterBy);
//	}

	@Override
	public int addExperimentError(ExperimentExecutionError error)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.addExperimentError(error);
        }
		ExecutionErrorResource executionError = createNewExecutionErrorResource(error.getExperimentId(),error,ExecutionErrors.Source.EXPERIMENT);
		executionError.save();
		return executionError.getErrorID();
	}

	private ExecutionErrorResource createNewExecutionErrorResource(
			String experimentId, ExecutionError errorSource, ExecutionErrors.Source type) throws RegistryException {
		if (!isExperimentExists(experimentId)){
			throw new ExperimentDoesNotExistsException(experimentId);
		}
		ExecutionErrorResource executionError = jpa.getWorker().getExperiment(experimentId).getData().createExecutionError();
		setupValues(errorSource, executionError);
		executionError.setSourceType(type.toString());
		return executionError;
	}

	private void setupValues(ExecutionError source,
			ExecutionErrorResource destination) {
		destination.setErrorCode(source.getErrorCode());
		destination.setErrorDes(source.getErrorDescription());
		destination.setErrorLocation(source.getErrorLocation());
		destination.setErrorMsg(source.getErrorMessage());
		destination.setErrorReference(source.getErrorReference());
		destination.setErrorReporter(source.getErrorReported());
		destination.setErrorTime(new Timestamp(source.getErrorTime().getTime()));
		destination.setActionTaken(source.getActionTaken());
	}

	@Override
	public int addWorkflowExecutionError(WorkflowExecutionError error)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.addWorkflowExecutionError(error);
        }
		ExecutionErrorResource executionError = createNewExecutionErrorResource(error.getExperimentId(),error,ExecutionErrors.Source.WORKFLOW);
		executionError.setWorkflowDataResource(jpa.getWorker().getExperiment(error.getExperimentId()).getData().getWorkflowInstance(error.getWorkflowInstanceId()));
		executionError.save();
		return executionError.getErrorID();
	}

	@Override
	public int addNodeExecutionError(NodeExecutionError error)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.addNodeExecutionError(error);
        }
		ExecutionErrorResource executionError = createNewExecutionErrorResource(error.getExperimentId(),error, Source.NODE);
		executionError.setWorkflowDataResource(jpa.getWorker().getExperiment(error.getExperimentId()).getData().getWorkflowInstance(error.getWorkflowInstanceId()));
		executionError.setNodeID(error.getNodeId());
		executionError.save();
		return executionError.getErrorID();
	}

	@Override
	public int addApplicationJobExecutionError(ApplicationJobExecutionError error)
			throws RegistryException {
		if (provenanceRegistry != null){
            return provenanceRegistry.addApplicationJobExecutionError(error);
        }
		ExecutionErrorResource executionError = createNewExecutionErrorResource(error.getExperimentId(),error, Source.APPLICATION);
		executionError.setWorkflowDataResource(jpa.getWorker().getExperiment(error.getExperimentId()).getData().getWorkflowInstance(error.getWorkflowInstanceId()));
		executionError.setNodeID(error.getNodeId());
		executionError.setGfacJobID(error.getJobId());
		executionError.save();
		return executionError.getErrorID();
	}

	@Override
	public void addApplicationJob(ApplicationJob job) throws RegistryException {
		if (provenanceRegistry != null){
            provenanceRegistry.addApplicationJob(job);
        }
		if (job.getJobId()==null || job.getJobId().equals("")){
			throw new InvalidApplicationJobIDException();
		}
		if (isApplicationJobExists(job.getJobId())){
			throw new ApplicationJobAlreadyExistsException(job.getJobId());
		}
		if (!isWorkflowInstanceNodePresent(job.getWorkflowExecutionId(), job.getNodeId())){
			throw new WorkflowInstanceNodeDoesNotExistsException(job.getWorkflowExecutionId(), job.getNodeId());
		}
		ExperimentDataResource expData = jpa.getWorker().getExperiment(job.getExperimentId()).getData();
		GFacJobDataResource gfacJob = expData.createGFacJob(job.getJobId());
		gfacJob.setExperimentDataResource(expData);
		gfacJob.setWorkflowDataResource(expData.getWorkflowInstance(job.getWorkflowExecutionId()));
		gfacJob.setNodeID(job.getNodeId());
		setupValues(job, gfacJob);
		gfacJob.save();
	}

	private void setupValues(ApplicationJob job, GFacJobDataResource gfacJob) {
		gfacJob.setApplicationDescID(job.getApplicationDescriptionId());
		gfacJob.setStatusUpdateTime(new Timestamp(job.getStatusUpdateTime().getTime()));
		gfacJob.setHostDescID(job.getHostDescriptionId());
		gfacJob.setJobData(job.getJobData());
		gfacJob.setMetadata(job.getMetadata());
		gfacJob.setServiceDescID(job.getServiceDescriptionId());
		gfacJob.setStatus(job.getJobStatus().toString());
		gfacJob.setSubmittedTime(new Timestamp(job.getSubmittedTime().getTime()));
	}

	@Override
	public void updateApplicationJob(ApplicationJob job) throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(job.getJobId());
		setupValues(job, gFacJob);
		gFacJob.save();
	}

	private GFacJobDataResource validateAndGetGFacJob(String jobId)
			throws InvalidApplicationJobIDException, RegistryException,
			ApplicationJobDoesNotExistsException {
		if (jobId==null || jobId.equals("")){
			throw new InvalidApplicationJobIDException();
		}
		if (!isApplicationJobExists(jobId)){
			throw new ApplicationJobDoesNotExistsException(jobId);
		}
		GFacJobDataResource gFacJob = jpa.getWorker().getGFacJob(jobId);
		return gFacJob;
	}

	@Override
	public void updateApplicationJobStatus(String gfacJobId, ApplicationJobStatus status, Date statusUpdateTime)
			throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(gfacJobId);
		gFacJob.setStatus(status.toString());
		gFacJob.setStatusUpdateTime(new Timestamp(statusUpdateTime.getTime()));
		gFacJob.save();
	}

	@Override
	public void updateApplicationJobData(String gfacJobId, String jobdata)
			throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(gfacJobId);
		gFacJob.setJobData(jobdata);
		gFacJob.save();
	}

	@Override
	public void updateApplicationJobSubmittedTime(String gfacJobId, Date submitted)
			throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(gfacJobId);
		gFacJob.setSubmittedTime(new Timestamp(submitted.getTime()));
		gFacJob.save();
	}

	@Override
	public void updateApplicationJobStatusUpdateTime(String gfacJobId, Date completed)
			throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(gfacJobId);
		gFacJob.setStatusUpdateTime(new Timestamp(completed.getTime()));
		gFacJob.save();
	}

	@Override
	public void updateApplicationJobMetadata(String gfacJobId, String metadata)
			throws RegistryException {
		GFacJobDataResource gFacJob = validateAndGetGFacJob(gfacJobId);
		gFacJob.setMetadata(metadata);
		gFacJob.save();
	}

	@Override
	public ApplicationJob getApplicationJob(String gfacJobId) throws RegistryException {
		GFacJobDataResource gfacJob = validateAndGetGFacJob(gfacJobId);
		ApplicationJob job = new ApplicationJob();
		setupValues(gfacJob, job);
		return job;
	}

	private void setupValues(GFacJobDataResource gfacJob, ApplicationJob job) {
		job.setApplicationDescriptionId(gfacJob.getApplicationDescID());
		job.setStatusUpdateTime(gfacJob.getStatusUpdateTime());
		job.setExperimentId(gfacJob.getExperimentDataResource().getExperimentID());
		job.setHostDescriptionId(gfacJob.getHostDescID());
		job.setJobData(gfacJob.getJobData());
		job.setJobId(gfacJob.getLocalJobID());
		job.setJobStatus(ApplicationJobStatus.valueOf(gfacJob.getStatus()));
		job.setMetadata(gfacJob.getMetadata());
		job.setNodeId(gfacJob.getNodeID());
		job.setServiceDescriptionId(gfacJob.getServiceDescID());
		job.setSubmittedTime(gfacJob.getSubmittedTime());
		job.setWorkflowExecutionId(gfacJob.getWorkflowDataResource().getWorkflowInstanceID());
	}

	@Override
	public List<ApplicationJob> getApplicationJobsForDescriptors(String serviceDescriptionId,
			String hostDescriptionId, String applicationDescriptionId)
			throws RegistryException {
		List<ApplicationJob> jobs=new ArrayList<ApplicationJob>();
		List<GFacJobDataResource> gFacJobs = jpa.getWorker().getGFacJobs(serviceDescriptionId,hostDescriptionId,applicationDescriptionId);
		for (GFacJobDataResource resource : gFacJobs) {
			ApplicationJob job = new ApplicationJob();
			setupValues(resource, job);
			jobs.add(job);
		}
		return jobs;
	}

	@Override
	public List<ApplicationJob> getApplicationJobs(String experimentId,
			String workflowExecutionId, String nodeId) throws RegistryException {
		if (!isWorkflowInstanceNodePresent(workflowExecutionId, nodeId)){
			throw new WorkflowInstanceNodeDoesNotExistsException(workflowExecutionId, nodeId);
		}
		List<ApplicationJob> jobs=new ArrayList<ApplicationJob>();
		List<Resource> gFacJobs;
		if (workflowExecutionId==null){
			gFacJobs = jpa.getWorker().getExperiment(experimentId).getData().getGFacJobs();
		}else if (nodeId==null){
			gFacJobs = jpa.getWorker().getExperiment(experimentId).getData().getWorkflowInstance(workflowExecutionId).getGFacJobs();
		}else{
			gFacJobs = jpa.getWorker().getExperiment(experimentId).getData().getWorkflowInstance(workflowExecutionId).getNodeData(nodeId).getGFacJobs();
		}
		for (Resource resource : gFacJobs) {
			ApplicationJob job = new ApplicationJob();
			setupValues((GFacJobDataResource)resource, job);
			jobs.add(job);
		}
		return jobs;
	}

	@Override
	public boolean isApplicationJobExists(String gfacJobId) throws RegistryException {
		return jpa.getWorker().isGFacJobExists(gfacJobId);
	}

}
