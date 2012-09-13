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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
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

    /**
     *
     */
    public ExperimentResource() {
    }

    /**
     *
     * @return  experiment ID
     */
    public String getExpID() {
        return expID;
    }

    /**
     *
     * @return submitted date
     */
    public Date getSubmittedDate() {
        return submittedDate;
    }

    /**
     *
     * @param submittedDate  submitted date
     */
    public void setSubmittedDate(Date submittedDate) {
        this.submittedDate = submittedDate;
    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * key should be the experiment ID
     * @param keys experiment ID
     * @return ExperimentResource
     */
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator queryGenerator = new QueryGenerator(EXPERIMENT);
        queryGenerator.setParameter(ExperimentConstants.EXPERIMENT_ID, keys[0]);
        Query q = queryGenerator.selectQuery(em);
        Experiment experiment = (Experiment)q.getSingleResult();
        ExperimentResource experimentResource =
                (ExperimentResource)Utils.getResource(ResourceType.EXPERIMENT, experiment);
        em.getTransaction().commit();
        em.close();
        list.add(experimentResource);
        return list;

    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @return UnsupportedOperationException
     */
    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     * save experiment
     */
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Experiment existingExp = em.find(Experiment.class, expID);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
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
        if(existingExp != null){
            existingExp.setGateway(gateway);
            existingExp.setProject(project);
            existingExp.setUser(user);
            existingExp.setSubmitted_date(submittedDate);
            experiment = em.merge(existingExp);
        } else{
           em.merge(experiment);
        }

        em.getTransaction().commit();
        em.close();
    }

    /**
     * Since experiments are at the leaf level, this method is not
     * valid for an experiment
     * @param type  child resource types
     * @param name name of the child resource
     * @return UnsupportedOperationException
     */
    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param expID experiment ID
     */
    public void setExpID(String expID) {
		this.expID = expID;
	}

    /**
     *
     * @return gatewayResource
     */
    public GatewayResource getGateway() {
		return gateway;
	}

    /**
     *
     * @param gateway gateway
     */
    public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

    /**
     *
     * @return worker for the gateway
     */
    public WorkerResource getWorker() {
		return worker;
	}

    /**
     *
     * @param worker gateway worker
     */
    public void setWorker(WorkerResource worker) {
		this.worker = worker;
	}

    /**
     *
     * @return project
     */
    public ProjectResource getProject() {
		return project;
	}

    /**
     *
     * @param project  project
     */
    public void setProject(ProjectResource project) {
		this.project = project;
	}
}
