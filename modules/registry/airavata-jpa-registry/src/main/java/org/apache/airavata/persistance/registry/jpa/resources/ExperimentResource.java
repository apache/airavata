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

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Users;

public class ExperimentResource extends AbstractResource {
    private WorkerResource worker;
    private String expID;
    private Date submittedDate;
    private GatewayResource gateway;
    private ProjectResource project;

    public ExperimentResource() {
    }

    public ExperimentResource(String expID) {
        this.setExpID(expID);
    }

    public int getProjectID() {
        return project.getId();
    }

    public String getExpID() {
        return expID;
    }

    public Date getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(Date submittedDate) {
        this.submittedDate = submittedDate;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * key should be the experiment ID
     * @param keys
     * @return
     */
    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.experiment_ID = :exp_ID");
        q.setParameter("exp_ID", keys[0]);
        Experiment experiment = (Experiment)q.getSingleResult();
        ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
        experimentResource.setGateway(getGateway());
        experimentResource.setWorker(getWorker());
        ProjectResource projectResource = new ProjectResource(experiment.getProject().getProject_ID());
        projectResource.setGateway(getGateway());
        projectResource.setWorker(getWorker());
        projectResource.setName(experiment.getProject().getProject_name());
        experimentResource.setProject(projectResource);
        experimentResource.setSubmittedDate(experiment.getSubmitted_date());
        end();
        list.add(experimentResource);
        return list;

    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Experiment experiment = new Experiment();
        experiment.setExperiment_ID(getExpID());
        Project project = new Project();
        project.setProject_ID(this.project.getId());
        experiment.setProject(project);
        Users user = new Users();
        user.setUser_name(getWorker().getUser());
        experiment.setUser(user);
        Gateway gateway = new Gateway();
        gateway.setGateway_name(getGateway().getGatewayName());
		experiment.setGateway(gateway);
        experiment.setSubmitted_date(submittedDate);
        em.persist(experiment);
        end();


    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

	public void setExpID(String expID) {
		this.expID = expID;
	}

	public GatewayResource getGateway() {
		return gateway;
	}

	public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

	public WorkerResource getWorker() {
		return worker;
	}

	public void setWorker(WorkerResource worker) {
		this.worker = worker;
	}

	public ProjectResource getProject() {
		return project;
	}

	public void setProject(ProjectResource project) {
		this.project = project;
	}
}
