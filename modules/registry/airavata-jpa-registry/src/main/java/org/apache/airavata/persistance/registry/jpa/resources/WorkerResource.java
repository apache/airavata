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
import org.apache.airavata.persistance.registry.jpa.resources.AbstractResource.GFacJobStatusConstants;
import org.apache.airavata.persistance.registry.jpa.resources.AbstractResource.WorkflowDataConstants;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkerResource.class);
    private String user;
	private GatewayResource gateway;

    /**
     *
     */
    public WorkerResource() {
    }

    /**
     *
     * @param user username
     * @param gateway  gatewayResource
     */
    public WorkerResource(String user, GatewayResource gateway) {
		this.setUser(user);
		this.gateway=gateway;
	}

    /**
     * Gateway worker can create child data structures such as projects and user workflows
     * @param type child resource type
     * @return  child resource
     */
	public Resource create(ResourceType type) {
		Resource result = null;
		switch (type) {
			case PROJECT:
				ProjectResource projectResource = new ProjectResource();
				projectResource.setWorker(this);
				projectResource.setGateway(gateway);
				result=projectResource;
				break;
			case USER_WORKFLOW:
				UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
				userWorkflowResource.setWorker(this);
				userWorkflowResource.setGateway(gateway);
				result=userWorkflowResource;
                break;
            case EXPERIMENT:
                ExperimentResource experimentResource = new ExperimentResource();
                experimentResource.setWorker(this);
                experimentResource.setGateway(gateway);
                result=experimentResource;
                break;
			default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for worker resource.");

		}
		return result;
	}

    /**
     *
     * @param type child resource type
     * @param name child resource name
     */
	public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.PROJECT_NAME, name);
                q = generator.deleteQuery(em);
	            q.executeUpdate();
				break;
			case USER_WORKFLOW:
                generator = new QueryGenerator(USER_WORKFLOW);
                generator.setParameter(UserWorkflowConstants.OWNER, getUser());
                generator.setParameter(UserWorkflowConstants.TEMPLATE_NAME, name);
                generator.setParameter(UserWorkflowConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.deleteQuery(em);
	            q.executeUpdate();
	            break;
			case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.USERNAME, getUser());
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                q = generator.deleteQuery(em);
	            q.executeUpdate();
	            break;
            case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
                generator.setParameter(WorkflowDataConstants.WORKFLOW_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
			default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                break;
		}
        em.getTransaction().commit();
        em.close();
	}

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return child resource
     */
	public Resource get(ResourceType type, Object name) {
		Resource result = null;
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.PROJECT_NAME, name);
                q = generator.selectQuery(em);
	            Project project = (Project) q.getSingleResult();
                result= Utils.getResource(ResourceType.PROJECT, project);
				break;
			case USER_WORKFLOW:
                generator = new QueryGenerator(USER_WORKFLOW);
                generator.setParameter(UserWorkflowConstants.OWNER, getUser());
                generator.setParameter(UserWorkflowConstants.TEMPLATE_NAME, name);
                generator.setParameter(UserWorkflowConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.selectQuery(em);
	            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
                result= Utils.getResource(ResourceType.USER_WORKFLOW, userWorkflow);
	            break;
			case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.USERNAME, getUser());
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
	            Experiment experiment = (Experiment) q.getSingleResult();
                result= Utils.getResource(ResourceType.EXPERIMENT, experiment);
				break;
			case WORKFLOW_DATA:
                generator = new QueryGenerator(WORKFLOW_DATA);
                generator.setParameter(WorkflowDataConstants.WORKFLOW_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                Workflow_Data eworkflowData = (Workflow_Data)q.getSingleResult();
                WorkflowDataResource workflowDataResource = (WorkflowDataResource)Utils.getResource(ResourceType.WORKFLOW_DATA, eworkflowData);
                result= workflowDataResource;
                break;
            case GFAC_JOB_DATA:
                generator = new QueryGenerator(GFAC_JOB_DATA);
                generator.setParameter(GFacJobDataConstants.LOCAL_JOB_ID, name);
                q = generator.selectQuery(em);
                GFac_Job_Data gFacJobData = (GFac_Job_Data)q.getSingleResult();
                GFacJobDataResource gFacJobDataResource = (GFacJobDataResource)Utils.getResource(ResourceType.GFAC_JOB_DATA, gFacJobData);
                result= gFacJobDataResource;
                break;
			default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                break;
		}
        em.getTransaction().commit();
        em.close();
		return result;
	}
	
	public List<GFacJobDataResource> getGFacJobs(String serviceDescriptionId, String hostDescriptionId, String applicationDescriptionId){
		List<GFacJobDataResource> result = new ArrayList<GFacJobDataResource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        generator = new QueryGenerator(GFAC_JOB_DATA);
        generator.setParameter(GFacJobDataConstants.SERVICE_DESC_ID, serviceDescriptionId);
        generator.setParameter(GFacJobDataConstants.HOST_DESC_ID, hostDescriptionId);
        generator.setParameter(GFacJobDataConstants.APP_DESC_ID, applicationDescriptionId);
        q = generator.selectQuery(em);
        for (Object o : q.getResultList()) {
            GFac_Job_Data gFacJobData = (GFac_Job_Data)o;
            result.add((GFacJobDataResource)Utils.getResource(ResourceType.GFAC_JOB_DATA, gFacJobData));
        }
        em.getTransaction().commit();
        em.close();
		return result;
	}
	
	public List<GFacJobStatusResource> getGFacJobStatuses(String jobId){
		List<GFacJobStatusResource> resourceList = new ArrayList<GFacJobStatusResource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        generator = new QueryGenerator(GFAC_JOB_STATUS);
        generator.setParameter(GFacJobStatusConstants.LOCAL_JOB_ID, jobId);
        q = generator.selectQuery(em);
        for (Object result : q.getResultList()) {
            GFac_Job_Status gFacJobStatus = (GFac_Job_Status) result;
            GFacJobStatusResource gFacJobStatusResource =
                    (GFacJobStatusResource)Utils.getResource(ResourceType.GFAC_JOB_STATUS, gFacJobStatus);
            resourceList.add(gFacJobStatusResource);
        }
        return resourceList;
	}

    /**
     *
     * @param type child resource type
     * @return list of child resources
     */
    public List<Resource> get(ResourceType type) {
		List<Resource> result = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                Users users = em.find(Users.class, getUser());
                Gateway gatewayModel = em.find(Gateway.class, gateway.getGatewayName());
                generator.setParameter("users", users);
                generator.setParameter("gateway", gatewayModel);
//                generator.setParameter(ProjectConstants.USERNAME, getUser());
//                generator.setParameter(ProjectConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.selectQuery(em);
	            for (Object o : q.getResultList()) {
	            	Project project = (Project) o;
		            ProjectResource projectResource = (ProjectResource)Utils.getResource(ResourceType.PROJECT, project);
		            result.add(projectResource);
				}
				break;
			case USER_WORKFLOW:
                generator = new QueryGenerator(USER_WORKFLOW);
                generator.setParameter(UserWorkflowConstants.OWNER, getUser());
                q = generator.selectQuery(em);
//	            q.setParameter("usr_name", getUser());
	            for (Object o : q.getResultList()) {
		            User_Workflow userWorkflow = (User_Workflow) o;
		            UserWorkflowResource userWorkflowResource = (UserWorkflowResource)Utils.getResource(ResourceType.USER_WORKFLOW, userWorkflow);
		            result.add(userWorkflowResource);
	            }
	            break;
			case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.USERNAME, getUser());
                generator.setParameter(ExperimentConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.selectQuery(em);
	            for (Object o : q.getResultList()) {
	            	Experiment experiment = (Experiment) o;
	            	ExperimentResource experimentResource = (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
		            result.add(experimentResource);
	            }
	            break;
			default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                break;
		}
        em.getTransaction().commit();
        em.close();
		return result;
	}

    /**
     * save gateway worker to database
     */
	public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Gateway_Worker existingWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gateway.getGatewayName(), user));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Gateway_Worker gatewayWorker = new Gateway_Worker();
        Users users = new Users();
        users.setUser_name(user);
        gatewayWorker.setUser(users);
        Gateway gatewaymodel = new Gateway();
        gatewaymodel.setGateway_name(gateway.getGatewayName());
        gatewaymodel.setOwner(gateway.getOwner());
        gatewayWorker.setGateway(gatewaymodel);
        if(existingWorker != null){
            gatewayWorker = em.merge(existingWorker);
        }else {
            em.merge(gatewayWorker);
        }
        em.getTransaction().commit();
        em.close();
	}

    /**
     *
     * @return user name
     */
	public String getUser() {
		return user;
	}

    /**
     *
     * @param user user name
     */
    public void setUser(String user) {
		this.user = user;
	}

    /**
     *
     * @return gateway resource
     */
    public GatewayResource getGateway() {
        return gateway;
    }

    /**
     *
     * @param gateway  gateway resource
     */
    public void setGateway(GatewayResource gateway) {
        this.gateway = gateway;
    }

    /**
     *
     * @param name  project name
     * @return whether the project is available under the user
     */
    public boolean isProjectExists(String name){
		return isExists(ResourceType.PROJECT, name);
	}

    /**
     *
     * @param name project name
     * @return project resource for the user
     */
	public ProjectResource createProject(String name){
		ProjectResource project=(ProjectResource)create(ResourceType.PROJECT);
		project.setName(name);
		return project;
	}

    /**
     *
     * @param name project name
     * @return project resource
     */
	public ProjectResource getProject(String name){
		return (ProjectResource)get(ResourceType.PROJECT, name);
	}

    /**
     *
     * @param name project name
     */
	public void removeProject(String name){
		remove(ResourceType.PROJECT, name);
	}

    /**
     *
     * @return  list of projects for the user
     */
    public List<ProjectResource> getProjects(){
		List<ProjectResource> result=new ArrayList<ProjectResource>();
		List<Resource> list = get(ResourceType.PROJECT);
		for (Resource resource : list) {
			result.add((ProjectResource) resource);
		}
		return result;
	}

    /**
     *
     * @param templateName user workflow template
     * @return whether the workflow is already exists under the given user
     */
	public boolean isWorkflowTemplateExists(String templateName){
		return isExists(ResourceType.USER_WORKFLOW, templateName);
	}

    /**
     *
     * @param templateName user workflow template
     * @return user workflow resource
     */
	public UserWorkflowResource createWorkflowTemplate(String templateName){
		UserWorkflowResource workflow=(UserWorkflowResource)create(ResourceType.USER_WORKFLOW);
		workflow.setName(templateName);
		return workflow;
	}

    /**
     *
     * @param templateName user workflow template
     * @return user workflow resource
     */
	public UserWorkflowResource getWorkflowTemplate(String templateName){
		return (UserWorkflowResource)get(ResourceType.USER_WORKFLOW, templateName);
	}

    /**
     *
     * @param templateName user workflow template
     */
    public void removeWorkflowTemplate(String templateName){
		remove(ResourceType.USER_WORKFLOW, templateName);
	}

    /**
     *
     * @return list of user workflows for the given user
     */
    public List<UserWorkflowResource> getWorkflowTemplates(){
		List<UserWorkflowResource> result=new ArrayList<UserWorkflowResource>();
		List<Resource> list = get(ResourceType.USER_WORKFLOW);
		for (Resource resource : list) {
			result.add((UserWorkflowResource) resource);
		}
		return result;
	}

    /**
     *
     * @param name experiment name
     * @return whether experiment is already exist for the given user
     */
	public boolean isExperimentExists(String name){
		return isExists(ResourceType.EXPERIMENT, name);
	}
	
	/**
	 * Returns of the gfac job record is present for the job id
	 * @param jobId
	 * @return
	 */
	public boolean isGFacJobExists(String jobId){
		return isExists(ResourceType.GFAC_JOB_DATA, jobId);
	}

    /**
     *
     * @param name experiment name
     * @return experiment resource
     */
    public ExperimentResource getExperiment(String name){
		return (ExperimentResource)get(ResourceType.EXPERIMENT, name);
	}
    
    public GFacJobDataResource getGFacJob(String jobId){
    	return (GFacJobDataResource)get(ResourceType.GFAC_JOB_DATA,jobId);
    }

    /**
     *
     * @return list of experiments for the user
     */
	public List<ExperimentResource> getExperiments(){
		List<ExperimentResource> result=new ArrayList<ExperimentResource>();
		List<Resource> list = get(ResourceType.EXPERIMENT);
		for (Resource resource : list) {
			result.add((ExperimentResource) resource);
		}
		return result;
	}

    /**
     *
     * @param experimentId  experiment name
     */
	public void removeExperiment(String experimentId){
		remove(ResourceType.EXPERIMENT, experimentId);
	}
	
    public boolean isWorkflowInstancePresent(String workflowInstanceId){
		return isExists(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
    
    public WorkflowDataResource getWorkflowInstance(String workflowInstanceId){
    	return (WorkflowDataResource)get(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
    
    public void removeWorkflowInstance(String workflowInstanceId){
    	remove(ResourceType.WORKFLOW_DATA, workflowInstanceId);
    }
}
