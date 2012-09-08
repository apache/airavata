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
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

public class ProjectResource extends AbstractResource {

    private String name;
    private int id = -1;
    private GatewayResource gateway;
    private WorkerResource worker;

    public ProjectResource() {
    }

    public ProjectResource(int id) {
        this.id = id;
    }

    public ProjectResource(WorkerResource worker, GatewayResource gateway, int id) {
        this.setWorker(worker);
        this.setGateway(gateway);
        this.id = id;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.EXPERIMENT) {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setGateway(getGateway());
            experimentResource.setProject(this);
            experimentResource.setWorker(getWorker());
            return experimentResource;
        } else {
            return null;
        }
    }

    public void remove(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.EXPERIMENT) {
        	QueryGenerator generator = new QueryGenerator("Experiment");
        	generator.setParameter("project_ID", id);
        	generator.setParameter("user_name", getWorker().getUser());
        	generator.setParameter("experiment_ID", name);
        	Query q = generator.deleteQuery(em);
        	q.executeUpdate();
//            Query q = em.createQuery("Delete p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.experiment_ID = :ex_name");
//            q.setParameter("proj_id", id);
//            q.setParameter("usr_name", getWorker().getUser());
//            q.setParameter("ex_name", name);
//            q.executeUpdate();
        }
        end();
    }

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.EXPERIMENT) {
        	QueryGenerator generator = new QueryGenerator("Experiment");
        	generator.setParameter("project_ID", id);
        	generator.setParameter("user_name", getWorker().getUser());
        	generator.setParameter("experiment_ID", name);
        	Query q = generator.selectQuery(em);
//            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.experiment_ID = :ex_name");
//            q.setParameter("proj_id", id);
//            q.setParameter("usr_name", getWorker().getUser());
//            q.setParameter("ex_name", name);
            Experiment experiment = (Experiment) q.getSingleResult();
            ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
            experimentResource.setProject(this);
            experimentResource.setWorker(getWorker());
            experimentResource.setGateway(getGateway());
            experimentResource.setSubmittedDate(experiment.getSubmitted_date());
            end();
            return experimentResource;
        }
        return null;
    }

    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Project p WHERE p.project_name = :proj_name");
        q.setParameter("proj_name", keys[0]);
        List<?> resultList = q.getResultList();
        if (resultList.size() != 0) {
            for (Object result : resultList) {
                Project project = (Project) result;
                ProjectResource projectResource = new ProjectResource();
//                projectResource.setGateway(gateway);
//                projectResource.setName(name)UserName(workerResource.getUser());
                list.add(projectResource);
            }
        }
        end();
        return list;
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        begin();
        if (type == ResourceType.EXPERIMENT) {
        	QueryGenerator generator = new QueryGenerator("Experiment");
        	generator.setParameter("project_ID", id);
        	Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Experiment experiment = (Experiment) result;
                    ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
                    experimentResource.setProject(this);
                    experimentResource.setGateway(new GatewayResource(experiment.getGateway().getGateway_name()));
                    experimentResource.setWorker(new WorkerResource(experiment.getUser().getUser_name(), experimentResource.getGateway()));
                    experimentResource.setSubmittedDate(experiment.getSubmitted_date());
                    resourceList.add(experimentResource);
                }
            }
        }
        end();
        return resourceList;
    }

    public void save() {
        begin();
        Project project = new Project();
        project.setProject_name(name);
        Gateway gatewayO = new Gateway();
        gatewayO.setGateway_name(gateway.getGatewayName());
        project.setGateway(gatewayO);
        if (id != -1) {
            project.setProject_ID(id);
        }
        em.persist(project);
        end();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

	public WorkerResource getWorker() {
		return worker;
	}

	public void setWorker(WorkerResource worker) {
		this.worker = worker;
	}

	public GatewayResource getGateway() {
		return gateway;
	}

	public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

	public boolean isExperimentExists(String experimentId){
		return isExists(ResourceType.EXPERIMENT, experimentId);
	}
	
	public ExperimentResource createExperiment(String experimentId){
		ExperimentResource experimentResource = (ExperimentResource)create(ResourceType.EXPERIMENT);
		experimentResource.setExpID(experimentId);
		return experimentResource;
	}
	
	public ExperimentResource getExperiment(String experimentId){
		return (ExperimentResource)get(ResourceType.EXPERIMENT,experimentId);
	}
	
	public List<ExperimentResource> getExperiments(){
		List<Resource> list = get(ResourceType.EXPERIMENT);
		List<ExperimentResource> result=new ArrayList<ExperimentResource>();
		for (Resource resource : list) {
			result.add((ExperimentResource) resource);
		}
		return result;
	}
	
	public void removeExperiment(String experimentId){
		remove(ResourceType.EXPERIMENT, experimentId);
	}

}
