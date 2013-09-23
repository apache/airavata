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
package org.apache.airavata.persistance.registry.jpa.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayResource.class);
    private String gatewayName;
    private String owner;

    /**
     *
     * @param gatewayName gateway name
     */
    public GatewayResource(String gatewayName) {
    	setGatewayName(gatewayName);
	}

    /**
     *
     */
    public GatewayResource() {
	}

    /**
     *
     * @return gateway name
     */
    public String getGatewayName() {
        return gatewayName;
    }

    /**
     *
     * @param gatewayName
     */
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    /**
     *
     * @return owner of the gateway
     */
    public String getOwner() {
        return owner;
    }

    /**
     *
     * @param owner owner of the gateway
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gateway is at the root level.  So it can populate his child resources.
     * Project, User, Published Workflows, User workflows, Host descriptors,
     * Service Descriptors, Application descriptors and Experiments are all
     * its children
     * @param type resource type of the children
     * @return specific child resource type
     */
    public Resource create(ResourceType type) {
        switch (type) {
            case PROJECT:
                ProjectResource projectResource = new ProjectResource();
                projectResource.setGateway(this);
                return projectResource;
            case USER:
                UserResource userResource = new UserResource();
                userResource.setGatewayName(this.getGatewayName());
                return userResource;
            case PUBLISHED_WORKFLOW:
                PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource();
                publishWorkflowResource.setGateway(this);
                return publishWorkflowResource;
            case USER_WORKFLOW:
                UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
                userWorkflowResource.setGateway(this);
                return userWorkflowResource;
            case HOST_DESCRIPTOR:
                HostDescriptorResource hostDescriptorResource = new HostDescriptorResource();
                hostDescriptorResource.setGatewayName(gatewayName);
                return hostDescriptorResource;
            case SERVICE_DESCRIPTOR:
                ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource();
                serviceDescriptorResource.setGatewayName(gatewayName);
                return serviceDescriptorResource;
            case APPLICATION_DESCRIPTOR:
                ApplicationDescriptorResource applicationDescriptorResource =
                        new ApplicationDescriptorResource();
                applicationDescriptorResource.setGatewayName(gatewayName);
                return applicationDescriptorResource;
            case EXPERIMENT:
                ExperimentResource experimentResource =new ExperimentResource();
                experimentResource.setGateway(this);
                return experimentResource;
            case GATEWAY_WORKER:
                WorkerResource workerResource = new WorkerResource();
                workerResource.setGateway(this);
                return workerResource;
            default:
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
    }

    /**
     * Child resources can be removed from a gateway
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case USER:
                generator = new QueryGenerator(USERS);
                generator.setParameter(UserConstants.USERNAME, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case PUBLISHED_WORKFLOW:
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, name);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, name);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.SERVICE_DESC_ID, name);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case APPLICATION_DESCRIPTOR:
                generator = new QueryGenerator(APPLICATION_DESCRIPTOR);
                generator.setParameter(ApplicationDescriptorConstants.APPLICATION_DESC_ID, name);
                generator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                break;
        }

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Gateway can get information of his children
     * @param type child resource type
     * @param name child resource name
     * @return specific child resource type
     */
    public Resource get(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case USER:
                generator = new QueryGenerator(GATEWAY_WORKER);
                generator.setParameter(GatewayWorkerConstants.USERNAME, name);
                generator.setParameter(GatewayWorkerConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Gateway_Worker worker = (Gateway_Worker) q.getSingleResult();
                WorkerResource workerResource =
                        (WorkerResource)Utils.getResource(ResourceType.GATEWAY_WORKER, worker);
                em.getTransaction().commit();
                em.close();
                return workerResource;
            case PUBLISHED_WORKFLOW:
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, name);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Published_Workflow ePub_workflow = (Published_Workflow) q.getSingleResult();
                PublishWorkflowResource publishWorkflowResource =
                        (PublishWorkflowResource)Utils.getResource(ResourceType.PUBLISHED_WORKFLOW, ePub_workflow);
                em.getTransaction().commit();
                em.close();
                return publishWorkflowResource;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, name);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Host_Descriptor eHostDesc = (Host_Descriptor) q.getSingleResult();
                HostDescriptorResource hostDescriptorResource =
                        (HostDescriptorResource)Utils.getResource(ResourceType.HOST_DESCRIPTOR, eHostDesc);
                em.getTransaction().commit();
                em.close();
                return hostDescriptorResource;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Experiment experiment = (Experiment)q.getSingleResult();
                ExperimentResource experimentResource =
                        (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
                em.getTransaction().commit();
                em.close();
                return experimentResource;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.SERVICE_DESC_ID, name);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Service_Descriptor eServiceDesc = (Service_Descriptor) q.getSingleResult();
                ServiceDescriptorResource serviceDescriptorResource =
                        (ServiceDescriptorResource)Utils.getResource(ResourceType.SERVICE_DESCRIPTOR, eServiceDesc);
                em.getTransaction().commit();
                em.close();
                return serviceDescriptorResource;
            case APPLICATION_DESCRIPTOR:
                generator = new QueryGenerator(APPLICATION_DESCRIPTOR);
                generator.setParameter(ApplicationDescriptorConstants.APPLICATION_DESC_ID, name);
                generator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Application_Descriptor eAppDesc = (Application_Descriptor) q.getSingleResult();
                ApplicationDescriptorResource applicationDescriptorResource =
                        (ApplicationDescriptorResource)Utils.getResource(ResourceType.APPLICATION_DESCRIPTOR, eAppDesc);
                em.getTransaction().commit();
                em.close();
                return applicationDescriptorResource;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");

        }

    }

    /**
     *
     * @param type child resource type
     * @return list of child resources
     */
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case PROJECT:
                generator = new QueryGenerator(PROJECT);
                Gateway gatewayModel = em.find(Gateway.class, gatewayName);
                generator.setParameter("gateway", gatewayModel);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Project project = (Project) result;
                        ProjectResource projectResource =
                                (ProjectResource)Utils.getResource(ResourceType.PROJECT, project);
                        resourceList.add(projectResource);
                    }
                }
                break;
            case GATEWAY_WORKER:
                generator = new QueryGenerator(GATEWAY_WORKER);
                generator.setParameter(GatewayWorkerConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Gateway_Worker gatewayWorker = (Gateway_Worker) result;
                        WorkerResource workerResource =
                                (WorkerResource)Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
                        resourceList.add(workerResource);
                    }
                }
                break;
            case  PUBLISHED_WORKFLOW :
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Published_Workflow publishedWorkflow = (Published_Workflow) result;
                        PublishWorkflowResource publishWorkflowResource =
                                (PublishWorkflowResource)Utils.getResource(ResourceType.PUBLISHED_WORKFLOW, publishedWorkflow);
                        resourceList.add(publishWorkflowResource);
                    }
                }
                break;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Host_Descriptor hostDescriptor = (Host_Descriptor) result;
                        HostDescriptorResource hostDescriptorResource =
                                (HostDescriptorResource)Utils.getResource(ResourceType.HOST_DESCRIPTOR, hostDescriptor);
                        resourceList.add(hostDescriptorResource);
                    }
                }
                break;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Service_Descriptor serviceDescriptor = (Service_Descriptor) result;
                        ServiceDescriptorResource serviceDescriptorResource =
                                (ServiceDescriptorResource)Utils.getResource(ResourceType.SERVICE_DESCRIPTOR, serviceDescriptor);
                        resourceList.add(serviceDescriptorResource);
                    }
                }
                break;
            case APPLICATION_DESCRIPTOR:
                generator = new QueryGenerator(APPLICATION_DESCRIPTOR);
                generator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                        ApplicationDescriptorResource applicationDescriptorResource =
                                (ApplicationDescriptorResource)Utils.getResource(ResourceType.APPLICATION_DESCRIPTOR, applicationDescriptor);
                        resourceList.add(applicationDescriptorResource);
                    }
                }
                break;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Experiment experiment = (Experiment) result;
                        ExperimentResource experimentResource =
                                (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
                        resourceList.add(experimentResource);
                    }
                }
                break;
            case USER:
		        generator = new QueryGenerator(USERS);
		        q = generator.selectQuery(em);
		        for (Object o : q.getResultList()) {
		        	Users user = (Users) o;
		        	UserResource userResource =
	                        (UserResource)Utils.getResource(ResourceType.USER, user);
		        	resourceList.add(userResource);
		        }
		        break;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    /**
     * save the gateway to the database
     */
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Gateway existingGateway = em.find(Gateway.class, gatewayName);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gatewayName);
        gateway.setOwner(owner);
        if (existingGateway != null) {
            existingGateway.setOwner(owner);
            gateway = em.merge(existingGateway);
        } else {
            em.persist(gateway);
        }
        em.getTransaction().commit();
        em.close();

    }

    /**
     * check whether child resource already exist in the database
     * @param type child resource type
     * @param name name of the child resource
     * @return true or false
     */
    public boolean isExists(ResourceType type, Object name) {
        EntityManager em;
        Query q;
        Number count;
        switch (type){
            case USER:
                em = ResourceUtils.getEntityManager();
                Gateway_Worker existingWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gatewayName, name.toString()));
                em.close();
                return existingWorker!= null;
            case PUBLISHED_WORKFLOW:
                em = ResourceUtils.getEntityManager();
                Published_Workflow existingWf = em.find(Published_Workflow.class, new Published_Workflow_PK(gatewayName, name.toString()));
                em.close();
                boolean a = existingWf != null;
                return existingWf != null;
            case HOST_DESCRIPTOR:
                em = ResourceUtils.getEntityManager();
                Host_Descriptor existingHostDesc = em.find(Host_Descriptor.class, new Host_Descriptor_PK(gatewayName, name.toString()));
                em.close();
                return existingHostDesc != null;
            case SERVICE_DESCRIPTOR:
                em = ResourceUtils.getEntityManager();
                Service_Descriptor existingServiceDesc = em.find(Service_Descriptor.class, new Service_Descriptor_PK(gatewayName, name.toString()));
                em.close();
                return existingServiceDesc != null;
            case APPLICATION_DESCRIPTOR:
                em = ResourceUtils.getEntityManager();
                Application_Descriptor existingAppDesc = em.find(Application_Descriptor.class, new Application_Descriptor_PK(gatewayName, name.toString()));
                em.close();
                return existingAppDesc != null;
            case EXPERIMENT:
                em = ResourceUtils.getEntityManager();
                Experiment existingExp = em.find(Experiment.class, name.toString());
                em.close();
                return existingExp != null;
            default:
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
    }

    /**
     *
     * @param descriptorName host descriptor name
     * @return whether host descriptor already available
     */
    public boolean isHostDescriptorExists(String descriptorName){
    	return isExists(ResourceType.HOST_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @param hostDescriptorName host descriptor name
     * @return HostDescriptorResource
     */
    public HostDescriptorResource createHostDescriptorResource(String hostDescriptorName){
    	HostDescriptorResource hdr = (HostDescriptorResource)create(ResourceType.HOST_DESCRIPTOR);
    	hdr.setHostDescName(hostDescriptorName);
    	return hdr;
    }

    /**
     *
     * @param hostDescriptorName host descriptor name
     * @return HostDescriptorResource
     */
    public HostDescriptorResource getHostDescriptorResource(String hostDescriptorName){
    	return (HostDescriptorResource)get(ResourceType.HOST_DESCRIPTOR,hostDescriptorName);
    }

    /**
     *
     * @param descriptorName host descriptor name
     */
    public void removeHostDescriptor(String descriptorName){
    	remove(ResourceType.HOST_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @return list of host descriptors available for the gateway
     */
    public List<HostDescriptorResource> getHostDescriptorResources(){
    	List<HostDescriptorResource> results=new ArrayList<HostDescriptorResource>();
    	List<Resource> list = get(ResourceType.HOST_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((HostDescriptorResource) resource);
		}
    	return results;
    }

    /**
     *
     * @param descriptorName service descriptor name
     * @return whether service descriptor already available
     */
    public boolean isServiceDescriptorExists(String descriptorName){
    	return isExists(ResourceType.SERVICE_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @param descriptorName  service descriptor name
     * @return  ServiceDescriptorResource
     */
    public ServiceDescriptorResource createServiceDescriptorResource(String descriptorName){
    	ServiceDescriptorResource hdr = (ServiceDescriptorResource)create(ResourceType.SERVICE_DESCRIPTOR);
    	hdr.setServiceDescName(descriptorName);
    	return hdr;
    }

    /**
     *
     * @param descriptorName   service descriptor name
     * @return ServiceDescriptorResource
     */
    public ServiceDescriptorResource getServiceDescriptorResource(String descriptorName){
    	return (ServiceDescriptorResource)get(ResourceType.SERVICE_DESCRIPTOR,descriptorName);
    }

    /**
     *
     * @param descriptorName Service descriptor name
     */
    public void removeServiceDescriptor(String descriptorName){
    	remove(ResourceType.SERVICE_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @return list of service descriptors for the gateway
     */
    public List<ServiceDescriptorResource> getServiceDescriptorResources(){
    	List<ServiceDescriptorResource> results=new ArrayList<ServiceDescriptorResource>();
    	List<Resource> list = get(ResourceType.SERVICE_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((ServiceDescriptorResource) resource);
		}
    	return results;
    }

    /**
     *
     * @param descriptorName application descriptor name
     * @return  whether application descriptor already available
     */
    public boolean isApplicationDescriptorExists(String descriptorName){
    	return isExists(ResourceType.APPLICATION_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @param descriptorName  application descriptor name
     * @return ApplicationDescriptorResource
     */
    public ApplicationDescriptorResource createApplicationDescriptorResource(String descriptorName){
    	ApplicationDescriptorResource hdr = (ApplicationDescriptorResource)create(ResourceType.APPLICATION_DESCRIPTOR);
    	hdr.setName(descriptorName);
    	return hdr;
    }

    /**
     *
     * @param descriptorName application descriptor name
     * @return ApplicationDescriptorResource
     */
    public ApplicationDescriptorResource getApplicationDescriptorResource(String descriptorName){
    	return (ApplicationDescriptorResource)get(ResourceType.APPLICATION_DESCRIPTOR,descriptorName);
    }

    /**
     *
     * @param descriptorName  application descriptor name
     */
    public void removeApplicationDescriptor(String descriptorName){
    	remove(ResourceType.APPLICATION_DESCRIPTOR, descriptorName);
    }

    /**
     *
     * @return list of application descriptors for the gateway
     */
    public List<ApplicationDescriptorResource> getApplicationDescriptorResources(){
    	List<ApplicationDescriptorResource> results=new ArrayList<ApplicationDescriptorResource>();
    	List<Resource> list = get(ResourceType.APPLICATION_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((ApplicationDescriptorResource) resource);
		}
    	return results;
    }

    /**
     *
     * @param serviceName service descriptor name
     * @param hostName host descriptor name
     * @return  list of application descriptors for the gateway
     */
    public List<ApplicationDescriptorResource> getApplicationDescriptorResources(String serviceName,String hostName){
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        String qString = "SELECT p FROM Application_Descriptor p WHERE " +
                "p.gateway_name =:gate_name";
        if (hostName!=null){
        	qString+=" and p.host_descriptor_ID =:host_name";
        }
        if (serviceName!=null){
        	qString+=" and p.service_descriptor_ID =:service_name";
        }
		Query q = em.createQuery(qString);
        q.setParameter("gate_name", gatewayName);
        if (serviceName!=null){
        	q.setParameter("service_name", serviceName);
        }
        if (hostName!=null){
        	q.setParameter("host_name",hostName);
        }
        List<?> results = q.getResultList();
    	List<ApplicationDescriptorResource> resourceList = new ArrayList<ApplicationDescriptorResource>();
        if (results.size() != 0) {
            for (Object result : results) {
                Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                ApplicationDescriptorResource applicationDescriptorResource =
                        new ApplicationDescriptorResource(
                                applicationDescriptor.getApplication_descriptor_ID(),
                                applicationDescriptor.getGateway().getGateway_name());
                applicationDescriptorResource.setContent(new String(applicationDescriptor.getApplication_descriptor_xml()));
                applicationDescriptorResource.setUpdatedUser(applicationDescriptor.getUser().getUser_name());
                applicationDescriptorResource.setHostDescName(applicationDescriptor.getHost_descriptor_ID());
                applicationDescriptorResource.setServiceDescName(applicationDescriptor.getService_descriptor_ID());
                resourceList.add(applicationDescriptorResource);
            }
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    /**
     *
     * @param workflowTemplateName published workflow template name
     * @return boolean - whether workflow with the same name exists
     */
    public boolean isPublishedWorkflowExists(String workflowTemplateName){
    	return isExists(ResourceType.PUBLISHED_WORKFLOW, workflowTemplateName);
    }

    /**
     *
     * @param workflowTemplateName published workflow template name
     * @return publish workflow resource
     */
    public PublishWorkflowResource createPublishedWorkflow(String workflowTemplateName){
    	PublishWorkflowResource publishedWorkflowResource =
                (PublishWorkflowResource)create(ResourceType.PUBLISHED_WORKFLOW);
    	publishedWorkflowResource.setName(workflowTemplateName);
    	publishedWorkflowResource.setPath("/");
    	publishedWorkflowResource.setVersion("1.0");
    	return publishedWorkflowResource;
    }

    /**
     *
     * @param workflowTemplateName published workflow template name
     * @return publish workflow resource
     */
    public PublishWorkflowResource getPublishedWorkflow(String workflowTemplateName){
    	return (PublishWorkflowResource)get(ResourceType.PUBLISHED_WORKFLOW,workflowTemplateName);
    }

    /**
     *
     * @return list of publish workflows for the gateway
     */
    public List<PublishWorkflowResource> getPublishedWorkflows(){
    	List<PublishWorkflowResource> result=new ArrayList<PublishWorkflowResource>();
    	List<Resource> list = get(ResourceType.PUBLISHED_WORKFLOW);
    	for (Resource resource : list) {
			result.add((PublishWorkflowResource) resource);
		}
    	return result;
    }

    /**
     *
     * @param workflowTemplateName published workflow template name
     */
    public void removePublishedWorkflow(String workflowTemplateName){
    	remove(ResourceType.PUBLISHED_WORKFLOW, workflowTemplateName);
    }
}

