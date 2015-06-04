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
package org.apache.airavata.registry.core.experiment.catalog.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayExperimentCatResource extends AbstractExperimentCatResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayExperimentCatResource.class);

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
    public GatewayExperimentCatResource(String gatewayId) {
    	setGatewayId(gatewayId);
	}

    /**
     *
     */
    public GatewayExperimentCatResource() {
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
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        switch (type) {
            case PROJECT:
                ProjectExperimentCatResource projectResource = new ProjectExperimentCatResource();
                projectResource.setGatewayId(gatewayId);
                return projectResource;
            case EXPERIMENT:
                ExperimentExperimentCatResource experimentResource =new ExperimentExperimentCatResource();
                experimentResource.setGatewayId(gatewayId);
                return experimentResource;
            case GATEWAY_WORKER:
                WorkerExperimentCatResource workerResource = new WorkerExperimentCatResource();
                workerResource.setGatewayId(gatewayId);
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
            em = ExpCatResourceUtils.getEntityManager();
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
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
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
                    WorkerExperimentCatResource workerResource =
                            (WorkerExperimentCatResource) Utils.getResource(ResourceType.GATEWAY_WORKER, worker);
                    em.getTransaction().commit();
                    em.close();
                    return workerResource;
                case USER:
                    generator = new QueryGenerator(USERS);
                    generator.setParameter(UserConstants.USERNAME, name);
                    q = generator.selectQuery(em);
                    Users user = (Users) q.getSingleResult();
                    UserExperimentCatResource userResource =
                            (UserExperimentCatResource) Utils.getResource(ResourceType.USER, user);
                    em.getTransaction().commit();
                    em.close();
                    return userResource;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Experiment experiment = (Experiment) q.getSingleResult();
                    ExperimentExperimentCatResource experimentResource =
                            (ExperimentExperimentCatResource) Utils.getResource(ResourceType.EXPERIMENT, experiment);
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
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
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
                            ProjectExperimentCatResource projectResource =
                                    (ProjectExperimentCatResource) Utils.getResource(ResourceType.PROJECT, project);
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
                            WorkerExperimentCatResource workerResource =
                                    (WorkerExperimentCatResource) Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
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
                            ExperimentExperimentCatResource experimentResource =
                                    (ExperimentExperimentCatResource) Utils.getResource(ResourceType.EXPERIMENT, experiment);
                            resourceList.add(experimentResource);
                        }
                    }
                    break;
                case USER:
                    generator = new QueryGenerator(USERS);
                    q = generator.selectQuery(em);
                    for (Object o : q.getResultList()) {
                        Users user = (Users) o;
                        UserExperimentCatResource userResource =
                                (UserExperimentCatResource) Utils.getResource(ResourceType.USER, user);
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
            em = ExpCatResourceUtils.getEntityManager();
            Gateway existingGateway = em.find(Gateway.class, gatewayId);
            em.close();

            em = ExpCatResourceUtils.getEntityManager();
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
                    em = ExpCatResourceUtils.getEntityManager();
                    Gateway_Worker existingWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gatewayId, name.toString()));
                    em.close();
                    return existingWorker != null;
                case USER:
                    em = ExpCatResourceUtils.getEntityManager();
                    Users existingUser = em.find(Users.class, name);
                    em.close();
                    return existingUser != null;
                case EXPERIMENT:
                    em = ExpCatResourceUtils.getEntityManager();
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

    public ExperimentExperimentCatResource createExperiment (String experimentID) throws RegistryException{
        ExperimentExperimentCatResource metadataResource = (ExperimentExperimentCatResource)create(ResourceType.EXPERIMENT);
        metadataResource.setExpID(experimentID);
        return metadataResource;
    }

    public ExperimentExperimentCatResource getExperiment (String expId) throws RegistryException{
        return (ExperimentExperimentCatResource)get(ResourceType.EXPERIMENT, expId);
    }

    public List<ExperimentExperimentCatResource> getExperiments () throws RegistryException{
        List<ExperimentExperimentCatResource> experiments = new ArrayList<ExperimentExperimentCatResource>();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT);
        for (ExperimentCatResource resource : resources){
            experiments.add((ExperimentExperimentCatResource)resource);
        }
        return experiments;
    }
}

