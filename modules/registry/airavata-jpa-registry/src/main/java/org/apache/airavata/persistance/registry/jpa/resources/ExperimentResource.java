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
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

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
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        QueryGenerator queryGenerator = new QueryGenerator(EXPERIMENT);
        queryGenerator.setParameter(ExperimentConstants.EXPERIMENT_ID, keys[0]);
        Query q = queryGenerator.selectQuery(em);
        Experiment experiment = (Experiment)q.getSingleResult();
        ExperimentResource experimentResource = (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
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
        Project project = new Project();
        project.setProject_ID(this.project.getId());
        Users user = new Users();
        user.setUser_name(getWorker().getUser());
        Gateway gateway = new Gateway();
        gateway.setGateway_name(getGateway().getGatewayName());

        experiment.setProject(project);
        experiment.setExperiment_ID(getExpID());
        experiment.setUser(user);
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
