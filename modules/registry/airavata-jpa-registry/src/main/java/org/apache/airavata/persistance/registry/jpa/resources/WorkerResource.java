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

public class WorkerResource extends AbstractResource {
	private String user;
	private GatewayResource gateway;

	public WorkerResource(String user, GatewayResource gateway) {
		this.setUser(user);
		this.gateway=gateway;
	}
	
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
			default:
				break;
		}
		return result;
	}

	public void remove(ResourceType type, Object name) {
		begin();
        Query q;
        QueryGenerator generator;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.USERNAME, getUser());
                generator.setParameter(ProjectConstants.PROJECT_NAME, name);
                generator.setParameter(ProjectConstants.GATEWAY_NAME, gateway.getGatewayName());
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
			default:
				break;
		}
		end();
	}

	public Resource get(ResourceType type, Object name) {
		Resource result = null;
		begin();
        QueryGenerator generator;
        Query q;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.USERNAME, getUser());
                generator.setParameter(ProjectConstants.PROJECT_NAME, name);
                generator.setParameter(ProjectConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.selectQuery(em);
	            Project project = (Project) q.getSingleResult();
	            ProjectResource projectResource = (ProjectResource)Utils.getResource(ResourceType.PROJECT, project);
	            result=projectResource;
				break;
			case USER_WORKFLOW:
                generator = new QueryGenerator(USER_WORKFLOW);
                generator.setParameter(UserWorkflowConstants.OWNER, getUser());
                generator.setParameter(UserWorkflowConstants.TEMPLATE_NAME, name);
                generator.setParameter(UserWorkflowConstants.GATEWAY_NAME, gateway.getGatewayName());
                q = generator.selectQuery(em);
	            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
	            UserWorkflowResource userWorkflowResource = (UserWorkflowResource)Utils.getResource(ResourceType.USER_WORKFLOW, userWorkflow);
	            result=userWorkflowResource;
	            break;
			case EXPERIMENT:
                generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.USERNAME, getUser());
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                q = generator.selectQuery(em);
	            Experiment experiment = (Experiment) q.getSingleResult();
	            ExperimentResource experimentResource = (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
	            result=experimentResource;
				break;
			default:
				break;
		}
		end();
		return result;
	}

	public List<Resource> get(ResourceType type) {
		List<Resource> result = new ArrayList<Resource>();
		begin();
        QueryGenerator generator;
        Query q;
		switch (type) {
			case PROJECT:
                generator = new QueryGenerator(PROJECT);
                generator.setParameter(ProjectConstants.USERNAME, getUser());
                generator.setParameter(ProjectConstants.GATEWAY_NAME, gateway.getGatewayName());
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
	            q.setParameter("usr_name", getUser());
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
				break;
		}
		end();
		return result;
	}

	public void save() {
        begin();
        Gateway_Worker gatewayWorker = new Gateway_Worker();
        Users users = new Users();
        users.setUser_name(user);
        gatewayWorker.setUser(users);
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gateway.getGateway_name());
        gatewayWorker.setGateway(gateway);
        em.persist(gatewayWorker);
        end();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

    public GatewayResource getGateway() {
        return gateway;
    }

    public void setGateway(GatewayResource gateway) {
        this.gateway = gateway;
    }

    public boolean isProjectExists(String name){
		return isExists(ResourceType.PROJECT, name);
	}
	
	public ProjectResource createProject(String name){
		ProjectResource project=(ProjectResource)create(ResourceType.PROJECT);
		project.setName(name);
		return project;
	}
	
	public ProjectResource getProject(String name){
		return (ProjectResource)get(ResourceType.PROJECT, name);
	}
	
	public void removeProject(String name){
		remove(ResourceType.PROJECT, name);
	}
	
	public List<ProjectResource> getProjects(){
		List<ProjectResource> result=new ArrayList<ProjectResource>();
		List<Resource> list = get(ResourceType.PROJECT);
		for (Resource resource : list) {
			result.add((ProjectResource) resource);
		}
		return result;
	}
	
	public boolean isWorkflowTemplateExists(String templateName){
		return isExists(ResourceType.USER_WORKFLOW, templateName);
	}
	
	public UserWorkflowResource createWorkflowTemplate(String templateName){
		UserWorkflowResource workflow=(UserWorkflowResource)create(ResourceType.USER_WORKFLOW);
		workflow.setName(templateName);
		return workflow;
	}
	
	public UserWorkflowResource getWorkflowTemplate(String templateName){
		return (UserWorkflowResource)get(ResourceType.USER_WORKFLOW, templateName);
	}
	
	public void removeWorkflowTemplate(String templateName){
		remove(ResourceType.USER_WORKFLOW, templateName);
	}
	
	public List<UserWorkflowResource> getWorkflowTemplates(){
		List<UserWorkflowResource> result=new ArrayList<UserWorkflowResource>();
		List<Resource> list = get(ResourceType.USER_WORKFLOW);
		for (Resource resource : list) {
			result.add((UserWorkflowResource) resource);
		}
		return result;
	}
	
	public boolean isExperimentExists(String name){
		return isExists(ResourceType.EXPERIMENT, name);
	}
	
	public ExperimentResource getExperiment(String name){
		return (ExperimentResource)get(ResourceType.EXPERIMENT, name);
	}
	
	public List<ExperimentResource> getExperiments(){
		List<ExperimentResource> result=new ArrayList<ExperimentResource>();
		List<Resource> list = get(ResourceType.EXPERIMENT);
		for (Resource resource : list) {
			result.add((ExperimentResource) resource);
		}
		return result;
	}
	
	public void removeExperiment(String experimentId){
		remove(ResourceType.EXPERIMENT, experimentId);
	}
}
