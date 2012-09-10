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

import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

public class GatewayResource extends AbstractResource {
    private String gatewayName;
    private String owner;

    public GatewayResource(String gatewayName) {
    	setGatewayName(gatewayName);
	}
    
    public GatewayResource() {
	}
    
    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

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
                ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
                applicationDescriptorResource.setGatewayName(gatewayName);
                return applicationDescriptorResource;
            case EXPERIMENT:
                ExperimentResource experimentResource =new ExperimentResource();
                experimentResource.setGateway(this);
                return experimentResource;
            default:
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
    }

    public void remove(ResourceType type, Object name) {
        begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case USER:
                generator = new QueryGenerator(USERS);
                generator.setParameter(UserConstants.USERNAME, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                end();
                break;
            case PUBLISHED_WORKFLOW:
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, name);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                end();
                break;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, name);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                end();
                break;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.SERVICE_DESC_ID, name);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                end();
                break;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                end();
                break;
            default:
                break;

        }
    }

    public Resource get(ResourceType type, Object name) {
        begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case USER:
                generator = new QueryGenerator(GATEWAY_WORKER);
                generator.setParameter(GatewayWorkerConstants.USERNAME, name);
                generator.setParameter(GatewayWorkerConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Users eUser = (Users) q.getSingleResult();
                WorkerResource workerResource = (WorkerResource)Utils.getResource(ResourceType.GATEWAY_WORKER, eUser);
                end();
                return workerResource;
            case PUBLISHED_WORKFLOW:
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, name);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Published_Workflow ePub_workflow = (Published_Workflow) q.getSingleResult();
                PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)Utils.getResource(ResourceType.PUBLISHED_WORKFLOW, ePub_workflow);
                end();
                return publishWorkflowResource;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, name);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Host_Descriptor eHostDesc = (Host_Descriptor) q.getSingleResult();
                HostDescriptorResource hostDescriptorResource = (HostDescriptorResource)Utils.getResource(ResourceType.HOST_DESCRIPTOR, eHostDesc);
                end();
                return hostDescriptorResource;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Experiment experiment = (Experiment)q.getSingleResult();
                ExperimentResource experimentResource = (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
                return experimentResource;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.SERVICE_DESC_ID, name);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                Service_Descriptor eServiceDesc = (Service_Descriptor) q.getSingleResult();
                ServiceDescriptorResource serviceDescriptorResource = (ServiceDescriptorResource)Utils.getResource(ResourceType.SERVICE_DESCRIPTOR, eServiceDesc);
                end();
                return serviceDescriptorResource;
            default:
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");

        }


    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.GATEWAY_NAME, gatewayName);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Project project = (Project) result;
                        ProjectResource projectResource = (ProjectResource)Utils.getResource(ResourceType.PROJECT, project);
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
                        WorkerResource workerResource = (WorkerResource)Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
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
                        PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)Utils.getResource(ResourceType.PUBLISHED_WORKFLOW, publishedWorkflow);
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
                        HostDescriptorResource hostDescriptorResource = (HostDescriptorResource)Utils.getResource(ResourceType.HOST_DESCRIPTOR, hostDescriptor);
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
                        ServiceDescriptorResource serviceDescriptorResource = (ServiceDescriptorResource)Utils.getResource(ResourceType.SERVICE_DESCRIPTOR, serviceDescriptor);
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
                        ApplicationDescriptorResource applicationDescriptorResource = (ApplicationDescriptorResource)Utils.getResource(ResourceType.APPLICATION_DESCRIPTOR, applicationDescriptor);
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
                        ExperimentResource experimentResource = (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
                        resourceList.add(experimentResource);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
        end();
        return resourceList;
    }

    public void save() {
        begin();
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gatewayName);
        em.persist(gateway);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case USER:
                generator = new QueryGenerator(GATEWAY_WORKER);
                generator.setParameter(GatewayWorkerConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(GatewayWorkerConstants.USERNAME, name);
                q = generator.selectQuery(em);
                Gateway_Worker gatewayWorker = (Gateway_Worker) q.getSingleResult();
                end();
                return gatewayWorker != null;
            case PUBLISHED_WORKFLOW:
                generator = new QueryGenerator(PUBLISHED_WORKFLOW);
                generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, name);
                q = generator.selectQuery(em);
                Published_Workflow publishedWrkflow = (Published_Workflow) q.getSingleResult();
                end();
                return publishedWrkflow != null;
            case HOST_DESCRIPTOR:
                generator = new QueryGenerator(HOST_DESCRIPTOR);
                generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, name);
                q = generator.selectQuery(em);
                Host_Descriptor hostDescriptor = (Host_Descriptor) q.getSingleResult();
                end();
                return hostDescriptor != null;
            case SERVICE_DESCRIPTOR:
                generator = new QueryGenerator(SERVICE_DESCRIPTOR);
                generator.setParameter(ServiceDescriptorConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(ServiceDescriptorConstants.SERVICE_DESC_ID, name);
                q = generator.selectQuery(em);
                Service_Descriptor serviceDescriptor = (Service_Descriptor) q.getSingleResult();
                end();
                return serviceDescriptor != null;
            case APPLICATION_DESCRIPTOR:
                generator = new QueryGenerator(APPLICATION_DESCRIPTOR);
                generator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(ApplicationDescriptorConstants.APPLICATION_DESC_ID, name);
                q = generator.selectQuery(em);
                Application_Descriptor applicationDescriptor = (Application_Descriptor) q.getSingleResult();
                end();
                return applicationDescriptor != null;
            case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gatewayName);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
                Experiment experiment = (Experiment) q.getSingleResult();
                end();
                return experiment != null;
            default:
                end();
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
    }

    public boolean isHostDescriptorExists(String descriptorName){
    	return isExists(ResourceType.HOST_DESCRIPTOR, descriptorName);
    }
    
    public HostDescriptorResource createHostDescriptorResource(String hostDescriptorName){
    	HostDescriptorResource hdr = (HostDescriptorResource)create(ResourceType.HOST_DESCRIPTOR);
    	hdr.setHostDescName(hostDescriptorName);
    	return hdr;
    }
    
    public HostDescriptorResource getHostDescriptorResource(String hostDescriptorName){
    	return (HostDescriptorResource)get(ResourceType.HOST_DESCRIPTOR,hostDescriptorName);
    }
    
    public void removeHostDescriptor(String descriptorName){
    	remove(ResourceType.HOST_DESCRIPTOR, descriptorName);
    }
    
    public List<HostDescriptorResource> getHostDescriptorResources(){
    	List<HostDescriptorResource> results=new ArrayList<HostDescriptorResource>();
    	List<Resource> list = get(ResourceType.HOST_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((HostDescriptorResource) resource);
		}
    	return results;
    }
    
    public boolean isServiceDescriptorExists(String descriptorName){
    	return isExists(ResourceType.SERVICE_DESCRIPTOR, descriptorName);
    }
    
    public ServiceDescriptorResource createServiceDescriptorResource(String descriptorName){
    	ServiceDescriptorResource hdr = (ServiceDescriptorResource)create(ResourceType.SERVICE_DESCRIPTOR);
    	hdr.setServiceDescName(descriptorName);
    	return hdr;
    }
    
    public ServiceDescriptorResource getServiceDescriptorResource(String descriptorName){
    	return (ServiceDescriptorResource)get(ResourceType.SERVICE_DESCRIPTOR,descriptorName);
    }
    
    public void removeServiceDescriptor(String descriptorName){
    	remove(ResourceType.SERVICE_DESCRIPTOR, descriptorName);
    }

    public List<ServiceDescriptorResource> getServiceDescriptorResources(){
    	List<ServiceDescriptorResource> results=new ArrayList<ServiceDescriptorResource>();
    	List<Resource> list = get(ResourceType.SERVICE_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((ServiceDescriptorResource) resource);
		}
    	return results;
    }
    
    public boolean isApplicationDescriptorExists(String descriptorName){
    	return isExists(ResourceType.APPLICATION_DESCRIPTOR, descriptorName);
    }
    
    public ApplicationDescriptorResource createApplicationDescriptorResource(String descriptorName){
    	ApplicationDescriptorResource hdr = (ApplicationDescriptorResource)create(ResourceType.APPLICATION_DESCRIPTOR);
    	hdr.setName(descriptorName);
    	return hdr;
    }
    
    public ApplicationDescriptorResource getApplicationDescriptorResource(String descriptorName){
    	return (ApplicationDescriptorResource)get(ResourceType.APPLICATION_DESCRIPTOR,descriptorName);
    }
    
    public void removeApplicationDescriptor(String descriptorName){
    	remove(ResourceType.APPLICATION_DESCRIPTOR, descriptorName);
    }

    public List<ApplicationDescriptorResource> getApplicationDescriptorResources(){
    	List<ApplicationDescriptorResource> results=new ArrayList<ApplicationDescriptorResource>();
    	List<Resource> list = get(ResourceType.APPLICATION_DESCRIPTOR);
    	for (Resource resource : list) {
    		results.add((ApplicationDescriptorResource) resource);
		}
    	return results;
    }
    
    public List<ApplicationDescriptorResource> getApplicationDescriptorResources(String serviceName,String hostName){
        begin();
        String qString = "SELECT p FROM Application_Descriptor p WHERE p.gateway_name =:gate_name and p.service_descriptor_ID =:service_name";
        if (hostName!=null){
        	qString+=" and p.host_descriptor_ID =:host_name";
        }
		Query q = em.createQuery(qString);
        q.setParameter("gate_name", gatewayName);
        q.setParameter("service_name", serviceName);
        if (hostName!=null){
        	q.setParameter("host_name",hostName);
        }
        List<?> results = q.getResultList();
    	List<ApplicationDescriptorResource> resourceList = new ArrayList<ApplicationDescriptorResource>();
        if (results.size() != 0) {
            for (Object result : results) {
                Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID(),applicationDescriptor.getGateway().getGateway_name(),
                        applicationDescriptor.getHost_descriptor_ID(), applicationDescriptor.getService_descriptor_ID());
                applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
                applicationDescriptorResource.setUpdatedUser(applicationDescriptor.getUser().getUser_name());
                resourceList.add(applicationDescriptorResource);
            }
        }
        end();
        return resourceList;
    }
    
    public boolean isPublishedWorkflowExists(String workflowTemplateName){
    	return isExists(ResourceType.PUBLISHED_WORKFLOW, workflowTemplateName);
    }
    
    public PublishWorkflowResource createPublishedWorkflow(String workflowTemplateName){
    	PublishWorkflowResource publishedWorkflowResource = (PublishWorkflowResource)create(ResourceType.PUBLISHED_WORKFLOW);
    	publishedWorkflowResource.setName(workflowTemplateName);
    	publishedWorkflowResource.setPath("/");
    	publishedWorkflowResource.setVersion("1.0");
    	return publishedWorkflowResource;
    }
    
    public PublishWorkflowResource getPublishedWorkflow(String workflowTemplateName){
    	return (PublishWorkflowResource)get(ResourceType.PUBLISHED_WORKFLOW,workflowTemplateName);
    }
    
    public List<PublishWorkflowResource> getPublishedWorkflows(){
    	List<PublishWorkflowResource> result=new ArrayList<PublishWorkflowResource>();
    	List<Resource> list = get(ResourceType.PUBLISHED_WORKFLOW);
    	for (Resource resource : list) {
			result.add((PublishWorkflowResource) resource);
		}
    	return result;
    }
    
    public void removePublishedWorkflow(String workflowTemplateName){
    	remove(ResourceType.PUBLISHED_WORKFLOW, workflowTemplateName);
    }
}

