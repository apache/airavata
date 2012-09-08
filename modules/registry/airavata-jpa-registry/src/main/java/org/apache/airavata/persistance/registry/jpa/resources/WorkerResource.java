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
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;

public class WorkerResource extends AbstractResource {
	private String user;
	private GatewayResource gateway;

	public WorkerResource(String user, GatewayResource gateway) {
		this.setUser(user);
		this.gateway=gateway;
	}
	
	@Override
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

	@Override
	public void remove(ResourceType type, Object name) {
		begin();
		switch (type) {
			case PROJECT:
				Query q = em.createQuery("Delete p FROM Project p WHERE p.user_name = :owner and p.project_name = :prj_name and p.gateway_name =: gate_name");
	            q.setParameter("owner", getUser());
	            q.setParameter("prj_name", name);
	            q.setParameter("gate_name", gateway.getGatewayName());
	            q.executeUpdate();
				break;
			case USER_WORKFLOW:
				q = em.createQuery("Delete p FROM User_Workflow p WHERE p.owner = :owner and p.template_name = :usrwf_name and p.gateway_name =: gate_name");
	            q.setParameter("owner", getUser());
	            q.setParameter("usrwf_name", name);
	            q.setParameter("gate_name", gateway.getGatewayName());
	            q.executeUpdate();
	            break;
			case EXPERIMENT:
	            q = em.createQuery("Delete p FROM Experiment p WHERE p.user_name = :usr_name and p.experiment_ID = :ex_name");
	            q.setParameter("usr_name", getUser());
	            q.setParameter("ex_name", name);
	            q.executeUpdate();
	            break;
			default:
				break;
		}
		end();
	}

	@Override
	public void removeMe(Object[] keys) {

	}

	@Override
	public Resource get(ResourceType type, Object name) {
		Resource result = null;
		begin();
		switch (type) {
			case PROJECT:
				Query q = em.createQuery("SELECT p FROM Project p WHERE p.user_name = :owner and p.project_name = :prj_name and p.gateway_name =: gate_name");
	            q.setParameter("owner", getUser());
	            q.setParameter("prj_name", name);
	            q.setParameter("gate_name", gateway.getGatewayName());
	            Project project = (Project) q.getSingleResult();
	            ProjectResource projectResource = new ProjectResource(this, gateway, project.getProject_ID());
	            projectResource.setName(project.getProject_name());
	            result=projectResource;
				break;
			case USER_WORKFLOW:
				q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.owner = :usr_name and p.user_workflow_name = :usrwf_name and p.gateway_name =:gate_name");
	            q.setParameter("user_name", getUser());
	            q.setParameter("usrwf_name", name);
	            q.setParameter("gate_name", gateway.getGatewayName());
	            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
	            UserWorkflowResource userWorkflowResource = new UserWorkflowResource(gateway, this, userWorkflow.getTemplate_name());
	            userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
	            userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_updated_date());
	            result=userWorkflowResource;
	            break;
			case EXPERIMENT:
				q = em.createQuery("SELECT p FROM Experiment p WHERE p.gateway_name = :gateway_name and p.user_name = :usr_name and p.experiment_ID = :ex_name");
	            q.setParameter("usr_name", getUser());
	            q.setParameter("ex_name", name);
	            q.setParameter("gateway_name", gateway.getGatewayName());
	            Experiment experiment = (Experiment) q.getSingleResult();
	            ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
	            ProjectResource projectResource1 = new ProjectResource(experiment.getProject().getProject_ID());
                projectResource1.setGateway(gateway);
                projectResource1.setWorker(this);
                projectResource1.setName(experiment.getProject().getProject_name());
            	experimentResource.setProject(projectResource1);
	            experimentResource.setWorker(this);
	            experimentResource.setGateway(gateway);
	            experimentResource.setSubmittedDate(experiment.getSubmitted_date());
	            result=experimentResource;
				break;
			default:
				break;
		}
		end();
		return result;
	}

	@Override
	public List<Resource> getMe(Object[] keys) {
		return null;
	}

	@Override
	public List<Resource> get(ResourceType type) {
		List<Resource> result = new ArrayList<Resource>();
		begin();
		switch (type) {
			case PROJECT:
				Query q = em.createQuery("SELECT p FROM Project p WHERE p.user_name = :owner and p.gateway_name =: gate_name");
	            q.setParameter("owner", getUser());
	            q.setParameter("gate_name", gateway.getGatewayName());
	            for (Object o : q.getResultList()) {
	            	Project project = (Project) o;
		            ProjectResource projectResource = new ProjectResource(this, gateway, project.getProject_ID());
		            projectResource.setName(project.getProject_name());
		            result.add(projectResource);
				}
				break;
			case USER_WORKFLOW:
				q = em.createQuery("SELECT p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.experiment_ID = :ex_name");
	            q.setParameter("usr_name", getUser());
	            for (Object o : q.getResultList()) {
		            User_Workflow userWorkflow = (User_Workflow) o;
		            UserWorkflowResource userWorkflowResource = new UserWorkflowResource(gateway, this, userWorkflow.getTemplate_name());
		            userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
		            userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_updated_date());
		            result.add(userWorkflowResource);
	            }
	            break;
			case EXPERIMENT:
				q = em.createQuery("SELECT p FROM Experiment p WHERE p.gateway_name = :gateway_name and p.user_name = :usr_name and p.experiment_ID = :ex_name");
	            q.setParameter("usr_name", getUser());
	            q.setParameter("gateway_name", gateway.getGatewayName());
	            for (Object o : q.getResultList()) {
	            	Experiment experiment = (Experiment) o;
	            	ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
	            	experimentResource.setGateway(gateway);
	            	experimentResource.setWorker(this);
	            	ProjectResource projectResource = new ProjectResource(experiment.getProject().getProject_ID());
	                projectResource.setGateway(gateway);
	                projectResource.setWorker(this);
	                projectResource.setName(experiment.getProject().getProject_name());
	            	experimentResource.setProject(projectResource);
	            	experimentResource.setSubmittedDate(experiment.getSubmitted_date());
		            result.add(experimentResource);
	            }
	            break;
			default:
				break;
		}
		end();
		return result;
	}

	@Override
	public void save() {
		//nothing to do... worker resource is just a concept
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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
