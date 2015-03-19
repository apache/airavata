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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ProjectResource.class);
    private String name;
    private String id;
    private GatewayResource gateway;
    private WorkerResource worker;
    private String description;
    private Timestamp creationTime;

    /**
     *
     */
    public ProjectResource() {
    }

    /**
     *
     * @param worker gateway worker
     * @param gateway gateway
     * @param projectId project name
     */
    public ProjectResource(WorkerResource worker, GatewayResource gateway, String projectId) {
        this.setWorker(worker);
        this.setGateway(gateway);
        this.id = projectId;
    }

    /**
     *
     * @param type child resource type
     * @return child resource
     */
    public Resource create(ResourceType type) throws RegistryException {
        if (type == ResourceType.EXPERIMENT) {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setGateway(getGateway());
            experimentResource.setExecutionUser(worker.getUser());
            experimentResource.setProject(this);
            return experimentResource;
        } else if (type == ResourceType.PROJECT_USER){
            ProjectUserResource pr = new ProjectUserResource();
            pr.setProjectId(id);
            pr.setUserName(worker.getUser());
            return pr;
        }
        else {
            logger.error("Unsupported resource type for project resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported resource type for project resource.");
        }
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if (type == ResourceType.EXPERIMENT) {
                QueryGenerator generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                Query q = generator.deleteQuery(em);
                q.executeUpdate();
            } else if (type == ResourceType.PROJECT_USER) {
                QueryGenerator generator = new QueryGenerator(PROJECT_USER);
                generator.setParameter(ProjectUserConstants.USERNAME, name);
                generator.setParameter(ProjectUserConstants.PROJECT_ID, this.id);
                Query q = generator.deleteQuery(em);
                q.executeUpdate();
            } else {
                logger.error("Unsupported resource type for project resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for project resource.");
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return child resource
     */
    public Resource get(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            if (type == ResourceType.EXPERIMENT) {
                em = ResourceUtils.getEntityManager();
                em.getTransaction().begin();
                QueryGenerator generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                Query q = generator.selectQuery(em);
                Experiment experiment = (Experiment) q.getSingleResult();
                ExperimentResource experimentResource = (ExperimentResource)
                        Utils.getResource(ResourceType.EXPERIMENT, experiment);
                em.getTransaction().commit();
                em.close();
                return experimentResource;
            } else if (type == ResourceType.PROJECT_USER) {
                em = ResourceUtils.getEntityManager();
                em.getTransaction().begin();
                QueryGenerator generator = new QueryGenerator(PROJECT_USER);
                generator.setParameter(ProjectUserConstants.USERNAME, name);
                generator.setParameter(ProjectUserConstants.PROJECT_ID, this.id);
                Query q = generator.selectQuery(em);
                ProjectUser prUser = (ProjectUser) q.getSingleResult();
                ExperimentResource experimentResource = (ExperimentResource)
                        Utils.getResource(ResourceType.PROJECT_USER, prUser);
                em.getTransaction().commit();
                em.close();
                return experimentResource;
            } else {
                logger.error("Unsupported resource type for project resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for project resource.");
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     *
     * @param type child resource type
     * @return list of child resources
     */
    public List<Resource> get(ResourceType type) throws RegistryException{
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            if (type == ResourceType.EXPERIMENT) {
                em = ResourceUtils.getEntityManager();
                em.getTransaction().begin();
                QueryGenerator generator = new QueryGenerator(EXPERIMENT);
                generator.setParameter(ExperimentConstants.PROJECT_ID, id);
                Query q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Experiment experiment = (Experiment) result;
                        ExperimentResource experimentResource = (ExperimentResource)
                                Utils.getResource(ResourceType.EXPERIMENT, experiment);
                        resourceList.add(experimentResource);
                    }
                }
                em.getTransaction().commit();
                em.close();
            } else if (type == ResourceType.PROJECT_USER) {
                em = ResourceUtils.getEntityManager();
                em.getTransaction().begin();
                QueryGenerator generator = new QueryGenerator(PROJECT_USER);
                generator.setParameter(ProjectUserConstants.PROJECT_ID, id);
                Query q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ProjectUser projectUser = (ProjectUser) result;
                        ProjectUserResource pr = (ProjectUserResource)
                                Utils.getResource(ResourceType.PROJECT_USER, projectUser);
                        resourceList.add(pr);
                    }
                }
                em.getTransaction().commit();
                em.close();
            } else {
                logger.error("Unsupported resource type for project resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for project resource.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    /**
     * save project to the database
     */
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Project existingProject = em.find(Project.class, id);
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Project project = new Project();
            project.setProject_id(id);
            project.setProject_name(name);
            Gateway modelGateway = em.find(Gateway.class, gateway.getGatewayId());
            project.setGateway(modelGateway);
            project.setGateway_id(modelGateway.getGateway_id());
            Users user = em.find(Users.class, worker.getUser());
            project.setUsers(user);
            project.setUser_name(user.getUser_name());
            project.setDescription(description);
            project.setCreationTime(creationTime);

            if (existingProject != null) {
                existingProject.setProject_name(name);
                existingProject.setGateway(modelGateway);
                existingProject.setGateway_id(modelGateway.getGateway_id());
                existingProject.setUsers(user);
                existingProject.setUser_name(user.getUser_name());
                existingProject.setDescription(description);
                existingProject.setCreationTime(creationTime);
                project = em.merge(existingProject);
            } else {
                em.persist(project);
            }

            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return project name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name  project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return gateway worker
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
     * @return gateway resource
     */
    public GatewayResource getGateway() {
		return gateway;
	}

    /**
     *
     * @param gateway gateway resource
     */
    public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    /**
     *
     * @param experimentId experiment ID
     * @return whether the experiment exist
     */
    public boolean isExperimentExists(String experimentId) throws RegistryException{
		return isExists(ResourceType.EXPERIMENT, experimentId);
	}

    /**
     *
     * @param experimentId experiment ID
     * @return  experiment resource
     */
    public ExperimentResource createExperiment(String experimentId) throws RegistryException{
		ExperimentResource experimentResource = (ExperimentResource)create(ResourceType.EXPERIMENT);
		experimentResource.setExpID(experimentId);
		return experimentResource;
	}

    /**
     *
     * @param experimentId experiment ID
     * @return experiment resource
     */
	public ExperimentResource getExperiment(String experimentId) throws RegistryException{
		return (ExperimentResource)get(ResourceType.EXPERIMENT,experimentId);
	}

    /**
     *
     * @return  list of experiments
     */
    public List<ExperimentResource> getExperiments() throws RegistryException{
		List<Resource> list = get(ResourceType.EXPERIMENT);
		List<ExperimentResource> result=new ArrayList<ExperimentResource>();
		for (Resource resource : list) {
			result.add((ExperimentResource) resource);
		}
		return result;
	}

    /**
     *
     * @param experimentId experiment ID
     */
    public void removeExperiment(String experimentId) throws RegistryException{
		remove(ResourceType.EXPERIMENT, experimentId);
	}

    public List<ProjectUserResource> getProjectUserList () throws RegistryException{
        List<Resource> resources = get(ResourceType.PROJECT_USER);
        List<ProjectUserResource> projectUserResources = new ArrayList<ProjectUserResource>();
        if (resources != null && !resources.isEmpty()){
            for (Resource r : resources){
                projectUserResources.add((ProjectUserResource)r);
            }
        }
        return projectUserResources;
    }

}
