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
				q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.owner = :usr_name and p.gateway_name =:gate_name");
	            q.setParameter("user_name", getUser());
	            q.setParameter("gate_name", gateway.getGatewayName());
	            for (Object o : q.getResultList()) {
		            User_Workflow userWorkflow = (User_Workflow) o;
		            UserWorkflowResource userWorkflowResource = new UserWorkflowResource(gateway, this, userWorkflow.getTemplate_name());
		            userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
		            userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_updated_date());
		            result.add(userWorkflowResource);
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

}
