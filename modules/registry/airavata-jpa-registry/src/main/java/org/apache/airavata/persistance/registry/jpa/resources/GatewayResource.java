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
import org.apache.airavata.persistance.registry.mongo.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayResource.class);

    private String gatewayId;
    private String gatewayName;
    private String domain;
    private String emailAddress;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     *
     * @param gatewayId gateway name
     */
    public GatewayResource(String gatewayId) {
    	setGatewayId(gatewayId);
	}

    /**
     *
     */
    public GatewayResource() {
	}

    /**
     *
     * @return gateway name
     */
    public String getGatewayName() {
        return gatewayName;
    }

    /**
     *
     * @param gatewayName
     */
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    /**
     *
     * @return domain of the gateway
     */
    public String getDomain() {
        return domain;
    }

    /**
     *
     * @param domain domain of the gateway
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * Gateway is at the root level.  So it can populate his child resources.
     * Project, User, Published Workflows, User workflows, Host descriptors,
     * Service Descriptors, Application descriptors and Experiments are all
     * its children
     * @param type resource type of the children
     * @return specific child resource type
     */
    public Resource create(ResourceType type) throws RegistryException {
        switch (type) {
            case PROJECT:
                ProjectResource projectResource = new ProjectResource();
                projectResource.setGateway(this);
                return projectResource;
            case EXPERIMENT:
                ExperimentResource experimentResource =new ExperimentResource();
                experimentResource.setGateway(this);
                return experimentResource;
            case GATEWAY_WORKER:
                WorkerResource workerResource = new WorkerResource();
                workerResource.setGateway(this);
                return workerResource;
            default:
                logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
        }
    }

    /**
     * Child resources can be removed from a gateway
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case USER:
                    generator = new QueryGenerator(USERS);
                    generator.setParameter(UserConstants.USERNAME, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                    break;
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
     * Gateway can get information of his children
     * @param type child resource type
     * @param name child resource name
     * @return specific child resource type
     */
    public Resource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case GATEWAY_WORKER:
                    generator = new QueryGenerator(GATEWAY_WORKER);
                    generator.setParameter(GatewayWorkerConstants.USERNAME, name);
                    generator.setParameter(GatewayWorkerConstants.GATEWAY_ID, gatewayId);
                    q = generator.selectQuery(em);
                    Gateway_Worker worker = (Gateway_Worker) q.getSingleResult();
                    WorkerResource workerResource =
                            (WorkerResource) Utils.getResource(ResourceType.GATEWAY_WORKER, worker);
                    em.getTransaction().commit();
                    em.close();
                    return workerResource;
                case USER:
                    generator = new QueryGenerator(USERS);
                    generator.setParameter(UserConstants.USERNAME, name);
                    q = generator.selectQuery(em);
                    Users user = (Users) q.getSingleResult();
                    UserResource userResource =
                            (UserResource) Utils.getResource(ResourceType.USER, user);
                    em.getTransaction().commit();
                    em.close();
                    return userResource;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Experiment experiment = (Experiment) q.getSingleResult();
                    ExperimentResource experimentResource =
                            (ExperimentResource) Utils.getResource(ResourceType.EXPERIMENT, experiment);
                    em.getTransaction().commit();
                    em.close();
                    return experimentResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
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
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case PROJECT:
                    generator = new QueryGenerator(PROJECT);
                    Gateway gatewayModel = em.find(Gateway.class, gatewayId);
                    generator.setParameter("gateway", gatewayModel);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Project project = (Project) result;
                            ProjectResource projectResource =
                                    (ProjectResource) Utils.getResource(ResourceType.PROJECT, project);
                            resourceList.add(projectResource);
                        }
                    }
                    break;
                case GATEWAY_WORKER:
                    generator = new QueryGenerator(GATEWAY_WORKER);
                    generator.setParameter(GatewayWorkerConstants.GATEWAY_ID, gatewayId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Gateway_Worker gatewayWorker = (Gateway_Worker) result;
                            WorkerResource workerResource =
                                    (WorkerResource) Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
                            resourceList.add(workerResource);
                        }
                    }
                    break;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.GATEWAY_ID, gatewayId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Experiment experiment = (Experiment) result;
                            ExperimentResource experimentResource =
                                    (ExperimentResource) Utils.getResource(ResourceType.EXPERIMENT, experiment);
                            resourceList.add(experimentResource);
                        }
                    }
                    break;
                case USER:
                    generator = new QueryGenerator(USERS);
                    q = generator.selectQuery(em);
                    for (Object o : q.getResultList()) {
                        Users user = (Users) o;
                        UserResource userResource =
                                (UserResource) Utils.getResource(ResourceType.USER, user);
                        resourceList.add(userResource);
                    }
                    break;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
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
        return resourceList;
    }

    /**
     * save the gateway to the database
     */
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Gateway existingGateway = em.find(Gateway.class, gatewayId);
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Gateway gateway = new Gateway();
            gateway.setGateway_name(gatewayName);
            gateway.setGateway_id(gatewayId);
            gateway.setDomain(domain);
            gateway.setEmailAddress(emailAddress);
            if (existingGateway != null) {
                existingGateway.setDomain(domain);
                existingGateway.setGateway_name(gatewayName);
                existingGateway.setEmailAddress(emailAddress);
                gateway = em.merge(existingGateway);
            } else {
                em.persist(gateway);
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
     * check whether child resource already exist in the database
     * @param type child resource type
     * @param name name of the child resource
     * @return true or false
     */
    public boolean isExists(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            switch (type) {
                case GATEWAY_WORKER:
                    em = ResourceUtils.getEntityManager();
                    Gateway_Worker existingWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gatewayId, name.toString()));
                    em.close();
                    return existingWorker != null;
                case USER:
                    em = ResourceUtils.getEntityManager();
                    Users existingUser = em.find(Users.class, name);
                    em.close();
                    return existingUser != null;
                case EXPERIMENT:
                    em = ResourceUtils.getEntityManager();
                    Experiment existingExp = em.find(Experiment.class, name.toString());
                    em.close();
                    return existingExp != null;
                default:
                    logger.error("Unsupported resource type for gateway resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for gateway resource.");
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
    }

    public ExperimentResource createExperiment (String experimentID) throws RegistryException{
        ExperimentResource metadataResource = (ExperimentResource)create(ResourceType.EXPERIMENT);
        metadataResource.setExpID(experimentID);
        return metadataResource;
    }

    public ExperimentResource getExperiment (String expId) throws RegistryException{
        return (ExperimentResource)get(ResourceType.EXPERIMENT, expId);
    }

    public List<ExperimentResource> getExperiments () throws RegistryException{
        List<ExperimentResource> experiments = new ArrayList<ExperimentResource>();
        List<Resource> resources = get(ResourceType.EXPERIMENT);
        for (Resource resource : resources){
            experiments.add((ExperimentResource)resource);
        }
        return experiments;
    }
}

