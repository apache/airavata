/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GatewayResource extends AbstractExpCatResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayResource.class);

    private String gatewayId;
    private String gatewayName;
    private String domain;
    private String emailAddress;
    private String gatewayApprovalStatus;
    private String gatewayAcronym;
    private String gatewayUrl;
    private String gatewayPublicAbstract;
    private String reviewProposalDescription;
    private String gatewayAdminFirstName;
    private String getGatewayAdminLastName;
    private String gatewayAdminEmail;
    private String identityServerUserName;
    private String identityServerPasswordToken;
    private String declinedReason;
    private String oauthClientId;
    private String oauthClientSecret;
    private Timestamp requestCreationTime;
    private String requesterUsername;

    public String getGatewayAdminFirstName() {
        return gatewayAdminFirstName;
    }

    public void setGatewayAdminFirstName(String gatewayAdminFirstName) {
        this.gatewayAdminFirstName = gatewayAdminFirstName;
    }

    public String getGetGatewayAdminLastName() {
        return getGatewayAdminLastName;
    }

    public void setGetGatewayAdminLastName(String getGatewayAdminLastName) {
        this.getGatewayAdminLastName = getGatewayAdminLastName;
    }

    public String getGatewayAdminEmail() {
        return gatewayAdminEmail;
    }

    public void setGatewayAdminEmail(String gatewayAdminEmail) {
        this.gatewayAdminEmail = gatewayAdminEmail;
    }

    public String getIdentityServerUserName() {
        return identityServerUserName;
    }

    public void setIdentityServerUserName(String identityServerUserName) {
        this.identityServerUserName = identityServerUserName;
    }

    public String getIdentityServerPasswordToken() {
        return identityServerPasswordToken;
    }

    public void setIdentityServerPasswordToken(String identityServerPasswordToken) {
        this.identityServerPasswordToken = identityServerPasswordToken;
    }

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

    public String getGatewayApprovalStatus() {
        return gatewayApprovalStatus;
    }

    public void setGatewayApprovalStatus(String gatewayApprovalStatus) {
        this.gatewayApprovalStatus = gatewayApprovalStatus;
    }

    public String getGatewayAcronym() {
        return gatewayAcronym;
    }

    public void setGatewayAcronym(String gatewayAcronym) {
        this.gatewayAcronym = gatewayAcronym;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getGatewayPublicAbstract() {
        return gatewayPublicAbstract;
    }

    public void setGatewayPublicAbstract(String gatewayPublicAbstract) {
        this.gatewayPublicAbstract = gatewayPublicAbstract;
    }

    public String getReviewProposalDescription() {
        return reviewProposalDescription;
    }

    public void setReviewProposalDescription(String reviewProposalDescription) {
        this.reviewProposalDescription = reviewProposalDescription;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public void setOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
    }

    public Timestamp getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(Timestamp requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
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
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        switch (type) {
            case PROJECT:
                ProjectResource projectResource = new ProjectResource();
                projectResource.setGatewayId(gatewayId);
                return projectResource;
            case EXPERIMENT:
                ExperimentResource experimentResource =new ExperimentResource();
                experimentResource.setGatewayExecutionId(gatewayId);
                return experimentResource;
            case GATEWAY_WORKER:
                WorkerResource workerResource = new WorkerResource();
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
                    GatewayWorker worker = (GatewayWorker) q.getSingleResult();
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
                            GatewayWorker gatewayWorker = (GatewayWorker) result;
                            WorkerResource workerResource =
                                    (WorkerResource) Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
                            resourceList.add(workerResource);
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
            gateway.setGatewayName(gatewayName);
            gateway.setGatewayId(gatewayId);
            gateway.setGatewayApprovalStatus(gatewayApprovalStatus);
            gateway.setDomain(domain);
            gateway.setEmailAddress(emailAddress);
            gateway.setGatewayAcronym(gatewayAcronym);
            gateway.setGatewayUrl(gatewayUrl);
            gateway.setGatewayPublicAbstract(gatewayPublicAbstract);
            gateway.setReviewProposalDescription(reviewProposalDescription);
            gateway.setGatewayAdminFirstName(gatewayAdminFirstName);
            gateway.setGetGatewayAdminLastName(getGatewayAdminLastName);
            gateway.setGatewayAdminEmail(gatewayAdminEmail);
            gateway.setIdentityServerUserName(identityServerUserName);
            gateway.setIdentityServerPasswordToken(identityServerPasswordToken);
            gateway.setDeclinedReason(declinedReason);
            gateway.setOauthClientId(oauthClientId);
            gateway.setGetOauthClientSecret(oauthClientSecret);
            gateway.setRequestCreationTime(requestCreationTime);
            gateway.setRequesterUsername(requesterUsername);
            if (existingGateway != null) {
                existingGateway.setDomain(domain);
                existingGateway.setGatewayApprovalStatus(gatewayApprovalStatus);
                existingGateway.setGatewayName(gatewayName);
                gateway.setGatewayApprovalStatus(gatewayApprovalStatus);
                existingGateway.setEmailAddress(emailAddress);
                existingGateway.setGatewayAcronym(gatewayAcronym);
                existingGateway.setGatewayUrl(gatewayUrl);
                existingGateway.setGatewayPublicAbstract(gatewayPublicAbstract);
                existingGateway.setReviewProposalDescription(reviewProposalDescription);
                existingGateway.setGatewayAdminFirstName(gatewayAdminFirstName);
                existingGateway.setGetGatewayAdminLastName(getGatewayAdminLastName);
                existingGateway.setGatewayAdminEmail(gatewayAdminEmail);
                existingGateway.setIdentityServerUserName(identityServerUserName);
                existingGateway.setIdentityServerPasswordToken(identityServerPasswordToken);
                existingGateway.setDeclinedReason(declinedReason);
                existingGateway.setOauthClientId(oauthClientId);
                existingGateway.setGetOauthClientSecret(oauthClientSecret);
                existingGateway.setRequestCreationTime(requestCreationTime);
                existingGateway.setRequesterUsername(requesterUsername);
                em.merge(existingGateway);
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
                    GatewayWorkerPK gatewayWorkerPK = new GatewayWorkerPK();
                    gatewayWorkerPK.setGatewayId(gatewayId);
                    gatewayWorkerPK.setUserName(name.toString());
                    GatewayWorker existingWorker = em.find(GatewayWorker.class, gatewayWorkerPK);
                    em.close();
                    return existingWorker != null;
                case USER:
                    em = ExpCatResourceUtils.getEntityManager();
                    UserPK userPK = new UserPK();
                    userPK.setGatewayId(getGatewayId());
                    userPK.setUserName(name.toString());
                    Users existingUser = em.find(Users.class, userPK);
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

    public ExperimentResource getExperiment (String expId) throws RegistryException{
        return (ExperimentResource)get(ResourceType.EXPERIMENT, expId);
    }

    public List<ExperimentResource> getExperiments () throws RegistryException{
        List<ExperimentResource> experiments = new ArrayList<ExperimentResource>();
        List<ExperimentCatResource> resources = get(ResourceType.EXPERIMENT);
        for (ExperimentCatResource resource : resources){
            experiments.add((ExperimentResource)resource);
        }
        return experiments;
    }
}

