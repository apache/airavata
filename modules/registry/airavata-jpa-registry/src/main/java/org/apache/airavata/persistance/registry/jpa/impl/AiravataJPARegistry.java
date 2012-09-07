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
import org.apache.airavata.persistance.registry.jpa.model.Configuration;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
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
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key");
        q.setParameter("config_key", key);
        return q.getSingleResult();
    }
    // Not sure about this.. need some description
    public List<Object> getConfigurationList(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setConfiguration(String key, String value, Date expire) {
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Configuration configuraton = new Configuration();
        configuraton.setConfig_key(key);
        configuraton.setConfig_val(value);
        configuraton.setExpire_date((java.sql.Date) expire);
        em.persist(configuraton);
        em.getTransaction().commit();
		em.close();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addConfiguration(String key, String value, Date expire) {
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Configuration configuraton = new Configuration();
        configuraton.setConfig_key(key);
        configuraton.setConfig_val(value);
        configuraton.setExpire_date((java.sql.Date) expire);
        em.persist(configuraton);
        em.getTransaction().commit();
		em.close();
    }

    public void removeAllConfiguration(String key) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key");
        q.setParameter("config_key", key);
        List<Configuration> resultList = q.getResultList();
        for (Configuration config : resultList) {
            em.remove(config);
        }
        em.getTransaction();
        em.close();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeConfiguration(String key, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key AND p.config_value = :config_value");
        q.setParameter("config_key", key);
        q.setParameter("config_value", value);
        Configuration config = (Configuration)q.getSingleResult();
        em.remove(config);
        em.getTransaction();
        em.close();
    }

    public List<URI> getGFacURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<URI> getWorkflowInterpreterURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getEventingServiceURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getMessageBoxURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetEventingURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetMessageBoxURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    // DescriptorRegistry Implementation
    public void addHostDescriptor(HostDescription descriptor) {
        //todo how to fill other data
        GatewayResource gatewayResource = jpa.getGateway();
        HostDescriptorResource resource = (HostDescriptorResource)gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
    }

    public void updateHostDescriptor(HostDescription descriptor) {
        addHostDescriptor(descriptor);
    }

    public HostDescription getHostDescriptor(String hostName) {
        GatewayResource gatewayResource = jpa.getGateway();
        Resource resource = gatewayResource.get(ResourceType.HOST_DESCRIPTOR, hostName);
        try {
            return HostDescription.fromXML(((HostDescriptorResource)resource).getContent());
        } catch (XmlException e) {
            logger.error("Error parsing Host Descriptor");
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeHostDescriptor(String hostName) {
       GatewayResource gatewayResource = jpa.getGateway();
       gatewayResource.remove(ResourceType.HOST_DESCRIPTOR, hostName);
    }

    public ResourceMetadata getHostDescriptorMetadata(String hostName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addServiceDescriptor(ServiceDescription descriptor) {
         //todo how to fill other data
        GatewayResource gatewayResource = jpa.getGateway();
        ServiceDescriptorResource resource = (ServiceDescriptorResource)gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) {
        addServiceDescriptor(descriptor);
    }

    public ServiceDescription getServiceDescriptor(String serviceName) {
        GatewayResource gatewayResource = jpa.getGateway();
        Resource resource = gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR, serviceName);
        try {
            return ServiceDescription.fromXML(((ServiceDescriptorResource) resource).getContent());
        } catch (XmlException e) {
            logger.error("Error parsing Host Descriptor");
        }
        return null;
    }

    public void removeServiceDescriptor(String serviceName) {
       GatewayResource gatewayResource = jpa.getGateway();
       gatewayResource.remove(ResourceType.SERVICE_DESCRIPTOR, serviceName);
    }

    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        GatewayResource gatewayResource = jpa.getGateway();
        ApplicationDescriptorResource resource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        resource.setHostDescName(hostName);
        resource.setServiceDescName(serviceName);
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceDescription,hostDescriptor,descriptor);
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceName,hostName,descriptor);
    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) {
        //todo finish implementation
        GatewayResource gatewayResource = jpa.getGateway();
        ApplicationDescriptorResource resource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        resource.setHostDescName(hostname);
        resource.setServiceDescName(serviceName);
//        resource.get()
//        gatewayResource.
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) {
        GatewayResource gatewayResource = jpa.getGateway();
        ServiceDescriptorResource resource = (ServiceDescriptorResource)gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR,serviceName);
        resource.setServiceDescName(serviceName);
        List<Resource> resources = resource.get(ResourceType.APPLICATION_DESCRIPTOR);
        HashMap<String, ApplicationDeploymentDescription> stringApplicationDescriptorResourceHashMap =
                new HashMap<String, ApplicationDeploymentDescription>();
        for(Resource applicationDescriptorResource:resources){
            try {
                stringApplicationDescriptorResourceHashMap.put(resource.getServiceDescName(),
                        ApplicationDeploymentDescription.fromXML(((ApplicationDescriptorResource) applicationDescriptorResource).getContent()));
            } catch (XmlException e) {
                logger.error("Error parsing Application Descriptor");
            }
        }
        return stringApplicationDescriptorResourceHashMap;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) {

    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



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
