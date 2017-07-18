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

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerResource extends AbstractExpCatResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkerResource.class);
    private String user;
    private String gatewayId;

    public WorkerResource() {
    }

    public WorkerResource(String user, String gatewayId) {
        this.user = user;
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * Gateway worker can create child data structures such as projects and user workflows
     *
     * @param type child resource type
     * @return child resource
     */
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        ExperimentCatResource result = null;
        switch (type) {
            case PROJECT:
                org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource projectResource = new org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource();
                projectResource.setWorker(this);
                projectResource.setGatewayId(gatewayId);
                result = projectResource;
                break;
            case EXPERIMENT:
                ExperimentResource experimentResource = new ExperimentResource();
                experimentResource.setUserName(user);
                experimentResource.setGatewayExecutionId(gatewayId);
                result = experimentResource;
                break;
            default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for worker resource.");

        }
        return result;
    }

    /**
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case PROJECT:
                    generator = new QueryGenerator(PROJECT);
                    generator.setParameter(ProjectConstants.PROJECT_ID, name);
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
                    logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     * @param type child resource type
     * @param name child resource name
     * @return child resource
     */
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        ExperimentCatResource result = null;
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case PROJECT:
                    generator = new QueryGenerator(PROJECT);
                    generator.setParameter(ProjectConstants.PROJECT_ID, name);
                    q = generator.selectQuery(em);
                    Project project = (Project) q.getSingleResult();
                    result = Utils.getResource(ResourceType.PROJECT, project);
                    break;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.EXPERIMENT_ID, name);
                    q = generator.selectQuery(em);
                    Experiment experiment = (Experiment) q.getSingleResult();
                    result = Utils.getResource(ResourceType.EXPERIMENT, experiment);
                    break;
                default:
                    logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }

//	public List<GFacJobDataResource> getGFacJobs(String serviceDescriptionId, String hostDescriptionId, String applicationDescriptionId){
//		List<GFacJobDataResource> result = new ArrayList<GFacJobDataResource>();
//        EntityManager em = ResourceUtils.getEntityManager();
//        em.getTransaction().begin();
//        QueryGenerator generator;
//        Query q;
//        generator = new QueryGenerator(GFAC_JOB_DATA);
//        generator.setParameter(GFacJobDataConstants.SERVICE_DESC_ID, serviceDescriptionId);
//        generator.setParameter(GFacJobDataConstants.HOST_DESC_ID, hostDescriptionId);
//        generator.setParameter(GFacJobDataConstants.APP_DESC_ID, applicationDescriptionId);
//        q = generator.selectQuery(em);
//        for (Object o : q.getResultList()) {
//            GFac_Job_Data gFacJobData = (GFac_Job_Data)o;
//            result.add((GFacJobDataResource)Utils.getResource(ResourceType.GFAC_JOB_DATA, gFacJobData));
//        }
//        em.getTransaction().commit();
//        em.close();
//		return result;
//	}
//
//	public List<GFacJobStatusResource> getGFacJobStatuses(String jobId){
//		List<GFacJobStatusResource> resourceList = new ArrayList<GFacJobStatusResource>();
//        EntityManager em = ResourceUtils.getEntityManager();
//        em.getTransaction().begin();
//        QueryGenerator generator;
//        Query q;
//        generator = new QueryGenerator(GFAC_JOB_STATUS);
//        generator.setParameter(GFacJobStatusConstants.LOCAL_JOB_ID, jobId);
//        q = generator.selectQuery(em);
//        for (Object result : q.getResultList()) {
//            GFac_Job_Status gFacJobStatus = (GFac_Job_Status) result;
//            GFacJobStatusResource gFacJobStatusResource =
//                    (GFacJobStatusResource)Utils.getResource(ResourceType.GFAC_JOB_STATUS, gFacJobStatus);
//            resourceList.add(gFacJobStatusResource);
//        }
//        return resourceList;
//	}

    /**
     * Method get all results of the given child resource type
     *
     * @param type child resource type
     * @return list of child resources
     */
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        return get(type, -1, -1, null, null);
    }

    /**
     * Method get all results of the given child resource type with paginaltion and ordering
     *
     * @param type              child resource type
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return list of child resources
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public List<ExperimentCatResource> get(ResourceType type, int limit, int offset, Object orderByIdentifier,
                                           ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentCatResource> result = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case PROJECT:
                    generator = new QueryGenerator(PROJECT);
                    UserPK userPK = new UserPK();
                    userPK.setGatewayId(getGatewayId());
                    userPK.setUserName(user);
                    Users users = em.find(Users.class, userPK);
                    Gateway gatewayModel = em.find(Gateway.class, gatewayId);
                    generator.setParameter("users", users);
                    if (gatewayModel != null) {
                        generator.setParameter("gateway", gatewayModel);
                    }

                    //ordering - only supported only by CREATION_TIME
                    if (orderByIdentifier != null && resultOrderType != null
                            && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
                        q = generator.selectQuery(em, ProjectConstants.CREATION_TIME, resultOrderType);
                    } else {
                        q = generator.selectQuery(em);
                    }

                    //pagination
                    if (limit > 0 && offset >= 0) {
                        q.setFirstResult(offset);
                        q.setMaxResults(limit);
                    }

                    for (Object o : q.getResultList()) {
                        Project project = (Project) o;
                        org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource projectResource = (org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource) Utils.getResource(ResourceType.PROJECT, project);
                        result.add(projectResource);
                    }
                    break;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.USER_NAME, getUser());

                    //ordering - only supported only by CREATION_TIME
                    if (orderByIdentifier != null && resultOrderType != null
                            && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
                        q = generator.selectQuery(em, ExperimentConstants.CREATION_TIME, resultOrderType);
                    } else {
                        q = generator.selectQuery(em);
                    }

                    //pagination
                    if (limit > 0 && offset >= 0) {
                        q.setFirstResult(offset);
                        q.setMaxResults(limit);
                    }
                    for (Object o : q.getResultList()) {
                        Experiment experiment = (Experiment) o;
                        ExperimentResource experimentResource = (ExperimentResource) Utils.getResource(ResourceType.EXPERIMENT, experiment);
                        result.add(experimentResource);
                    }

                    break;
                default:
                    logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }

    /**
     * save gateway worker to database
     */
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            GatewayWorkerPK gatewayWorkerPK = new GatewayWorkerPK();
            gatewayWorkerPK.setGatewayId(gatewayId);
            gatewayWorkerPK.setUserName(user);
            GatewayWorker existingWorker = em.find(GatewayWorker.class, gatewayWorkerPK);
            em.close();

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            GatewayWorker gatewayWorker = new GatewayWorker();
            gatewayWorker.setUserName(user);
            gatewayWorker.setGatewayId(gatewayId);
            if (existingWorker != null) {
                existingWorker.setUserName(user);
                existingWorker.setGatewayId(gatewayId);
                gatewayWorker = em.merge(existingWorker);
            } else {
                em.persist(gatewayWorker);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     * @return user name
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user user name
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param id project id
     * @return whether the project is available under the user
     */
    public boolean isProjectExists(String id) throws RegistryException {
        return isExists(ResourceType.PROJECT, id);
    }

    /**
     * @param projectId project id
     * @return project resource for the user
     */
    public org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource createProject(String projectId) throws RegistryException {
        org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource project = (org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource) create(ResourceType.PROJECT);
        project.setId(projectId);
        return project;
    }

    /**
     * @param id project id
     * @return project resource
     */
    public org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource getProject(String id) throws RegistryException {
        return (org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource) get(ResourceType.PROJECT, id);
    }

    /**
     * @param id project id
     */
    public void removeProject(String id) throws RegistryException {
        remove(ResourceType.PROJECT, id);
    }

    /**
     * Get projects list of user
     *
     * @return list of projects for the user
     */
    public List<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource> getProjects() throws RegistryException {
        return getProjects(-1, -1, null, null);
    }


    /**
     * Get projects list of user with pagination and ordering
     *
     * @return list of projects for the user
     */
    public List<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource> getProjects(int limit, int offset, Object orderByIdentifier,
                                                                                                            ResultOrderType resultOrderType) throws RegistryException {
        List<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource> result = new ArrayList<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource>();
        List<ExperimentCatResource> list = get(ResourceType.PROJECT, limit, offset, orderByIdentifier, resultOrderType);
        for (ExperimentCatResource resource : list) {
            result.add((org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource) resource);
        }
        return result;
    }

    /**
     * @param name experiment name
     * @return whether experiment is already exist for the given user
     */
    public boolean isExperimentExists(String name) throws RegistryException {
        return isExists(ResourceType.EXPERIMENT, name);
    }


    /**
     * @param name experiment name
     * @return experiment resource
     */
    public ExperimentResource getExperiment(String name) throws RegistryException {
        return (ExperimentResource) get(ResourceType.EXPERIMENT, name);
    }
//
//    public GFacJobDataResource getGFacJob(String jobId){
//    	return (GFacJobDataResource)get(ResourceType.GFAC_JOB_DATA,jobId);
//    }

    /**
     * Method to get list of expeirments of user
     *
     * @return list of experiments for the user
     */
    public List<ExperimentResource> getExperiments() throws RegistryException {
        return getExperiments(-1, -1, null, null);
    }

    /**
     * Method to get list of experiments of user with pagination and ordering
     *
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public List<ExperimentResource> getExperiments(int limit, int offset, Object orderByIdentifier,
                                                   ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentResource> result = new ArrayList<ExperimentResource>();
        List<ExperimentCatResource> list = get(ResourceType.EXPERIMENT, limit, offset, orderByIdentifier, resultOrderType);
        for (ExperimentCatResource resource : list) {
            result.add((ExperimentResource) resource);
        }
        return result;
    }

    /**
     * @param experimentId experiment name
     */
    public void removeExperiment(String experimentId) throws RegistryException {
        remove(ResourceType.EXPERIMENT, experimentId);
    }

    /**
     * To search the projects of user with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC. But in the current implementation ordering is only supported based on the project
     * creation time
     *
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public List<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource> searchProjects(
            Map<String, String> filters, List<String> accessibleIds, int limit, int offset, Object orderByIdentifier,
            ResultOrderType resultOrderType) throws RegistryException {

        List<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource> result = new ArrayList<org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource>();
        EntityManager em = null;
        try {
            Map<String, Object> queryParameters = new HashMap<>();
            String query = "SELECT DISTINCT p from Project p WHERE ";

            // FIXME There is a performance bottleneck for using IN clause. Try using temporary tables ?
            if (accessibleIds != null && accessibleIds.size() > 0) {
                query += " p.projectId IN (";
                int accessibleIdIndex = 0;
                for (String id : accessibleIds) {
                    String paramName = "accessibleId" + accessibleIdIndex;
                    query += (":" + paramName + ",");
                    queryParameters.put(paramName, id);
                    accessibleIdIndex++;
                }
                query = query.substring(0, query.length() - 1) + ") AND ";
            }else if(ServerSettings.isEnableSharing() && (accessibleIds==null || accessibleIds.size()==0)){
                return new ArrayList<>();
            }

            if (filters != null && filters.size() != 0) {
                for (String field : filters.keySet()) {
                    String filterVal = filters.get(field);
                    if (field.equals(ProjectConstants.USERNAME)) {
                        query += "p." + field + "= :" + field + " AND ";
                        queryParameters.put(field, filterVal);
                    } else if (field.equals(ProjectConstants.GATEWAY_ID)) {
                        query += "p." + field + "= :" + field + " AND ";
                        queryParameters.put(field, filterVal);
                    } else {
                        if (filterVal.contains("*")) {
                            filterVal = filterVal.replaceAll("\\*", "");
                        }
                        query += "p." + field + " LIKE :" + field + " AND ";
                        queryParameters.put(field, "%" + filterVal + "%");
                    }
                }
            }
            query = query.substring(0, query.length() - 5);

            //ordering
            if (orderByIdentifier != null && resultOrderType != null
                    && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
                String order = (resultOrderType == ResultOrderType.ASC) ? "ASC" : "DESC";
                query += " ORDER BY p." + ProjectConstants.CREATION_TIME + " " + order;
            }

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;

            //pagination
            if (offset >= 0 && limit >= 0) {
                q = em.createQuery(query).setFirstResult(offset).setMaxResults(limit);
            } else {
                q = em.createQuery(query);
            }
            for (String parameterName : queryParameters.keySet()) {
                q.setParameter(parameterName, queryParameters.get(parameterName));
            }


            List resultList = q.getResultList();
            for (Object o : resultList) {
                Project project = (Project) o;
                org.apache.airavata.registry.core.experiment.catalog.resources.ProjectResource projectResource =
                        (ProjectResource) Utils.getResource(ResourceType.PROJECT, project);
                result.add(projectResource);
            }
            em.getTransaction().commit();
            em.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }

    /**
     * To search the experiments of user with the given time period and filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC. But in the current implementation ordering is only supported based on creationTime. Also if
     * time period values i.e fromTime and toTime are null they will be ignored.
     *
     * @param fromTime
     * @param toTime
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public List<ExperimentSummaryResource> searchExperiments(List<String> accessibleIds, Timestamp fromTime, Timestamp toTime, Map<String, String> filters, int limit,
                                                             int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentSummaryResource> result = new ArrayList();
        EntityManager em = null;
        try {
            Map<String, Object> queryParameters = new HashMap<>();
            String query = "SELECT e FROM ExperimentSummary e " +
                    "WHERE ";

            // FIXME There is a performance bottleneck for using IN clause. Try using temporary tables ?
            if (accessibleIds != null && accessibleIds.size() > 0) {
                query += " e.experimentId IN (";
                int accessibleIdIndex = 0;
                for (String id : accessibleIds) {
                    String paramName = "accessibleId" + accessibleIdIndex;
                    query += (":" + paramName + ",");
                    queryParameters.put(paramName, id);
                    accessibleIdIndex++;
                }
                query = query.substring(0, query.length() - 1) + ") AND ";
            }else if(ServerSettings.isEnableSharing() && (accessibleIds==null || accessibleIds.size()==0)){
                return new ArrayList<>();
            }

            if (filters.get(ExperimentStatusConstants.STATE) != null) {
                String experimentState = ExperimentState.valueOf(filters.get(ExperimentStatusConstants.STATE)).toString();
                query += "e.state='" + experimentState + "' AND ";
            }

            if (toTime != null && fromTime != null && toTime.after(fromTime)) {
                query += "e.creationTime > :fromTime AND e.creationTime < :toTime AND ";
                queryParameters.put("fromTime", fromTime);
                queryParameters.put("toTime", toTime);
            }

            filters.remove(ExperimentStatusConstants.STATE);
            if (filters != null && filters.size() != 0) {
                for (String field : filters.keySet()) {
                    String filterVal = filters.get(field);
                    if (field.equals(ExperimentConstants.USER_NAME)) {
                        query += "e." + field + "= :username AND ";
                        queryParameters.put("username", filterVal);
                    } else if (field.equals(ExperimentConstants.GATEWAY_ID)) {
                        query += "e." + field + "= :gateway_id AND ";
                        queryParameters.put("gateway_id", filterVal);
                    } else if (field.equals(ExperimentConstants.PROJECT_ID)) {
                        query += "e." + field + "= :project_id AND ";
                        queryParameters.put("project_id", filterVal);
                    } else {
                        if (filterVal.contains("*")) {
                            filterVal = filterVal.replaceAll("\\*", "");
                        }
                        query += "e." + field + " LIKE :" + field + " AND ";
                        queryParameters.put(field, "%" + filterVal + "%");
                    }
                }
            }
            query = query.substring(0, query.length() - 5);

            //ordering
            if (orderByIdentifier != null && resultOrderType != null
                    && orderByIdentifier.equals(Constants.FieldConstants.ExperimentConstants.CREATION_TIME)) {
                String order = (resultOrderType == ResultOrderType.ASC) ? "ASC" : "DESC";
                query += " ORDER BY e." + ExperimentConstants.CREATION_TIME + " " + order;
            }

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;

            //pagination
            if (offset >= 0 && limit >= 0) {
                q = em.createQuery(query).setFirstResult(offset).setMaxResults(limit);
            } else {
                q = em.createQuery(query);
            }
            for (String parameterName : queryParameters.keySet()) {
                q.setParameter(parameterName, queryParameters.get(parameterName));
            }

            List resultList = q.getResultList();
            for (Object o : resultList) {
                ExperimentSummary experimentSummary = (ExperimentSummary) o;
                ExperimentSummaryResource experimentSummaryResource =
                        (ExperimentSummaryResource) Utils.getResource(ResourceType.EXPERIMENT_SUMMARY,
                                experimentSummary);
                result.add(experimentSummaryResource);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }

    /**
     * Method to get experiment statistics for a gateway
     *
     * @param gatewayId
     * @param fromTime
     * @param toTime
     * @return
     * @throws org.apache.airavata.registry.cpi.RegistryException
     */
    public ExperimentStatisticsResource getExperimentStatistics(String gatewayId, Timestamp fromTime, Timestamp toTime, String userName, String applicationName, String resourceHostName) throws RegistryException {
        ExperimentStatisticsResource experimentStatisticsResource = new ExperimentStatisticsResource();
        List<ExperimentSummaryResource> allExperiments = getExperimentStatisticsForState(null, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        experimentStatisticsResource.setAllExperimentCount(allExperiments.size());
        experimentStatisticsResource.setAllExperiments(allExperiments);

        List<ExperimentSummaryResource> createdExperiments = getExperimentStatisticsForState(ExperimentState.CREATED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        createdExperiments.addAll(getExperimentStatisticsForState(ExperimentState.VALIDATED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName));
        experimentStatisticsResource.setCreatedExperimentCount(createdExperiments.size());
        experimentStatisticsResource.setCreatedExperiments(createdExperiments);

        List<ExperimentSummaryResource> runningExperiments = getExperimentStatisticsForState(ExperimentState.EXECUTING, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.SCHEDULED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName));
        runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.LAUNCHED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName));
        experimentStatisticsResource.setRunningExperimentCount(runningExperiments.size());
        experimentStatisticsResource.setRunningExperiments(runningExperiments);

        List<ExperimentSummaryResource> completedExperiments = getExperimentStatisticsForState(ExperimentState.COMPLETED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        experimentStatisticsResource.setCompletedExperimentCount(completedExperiments.size());
        experimentStatisticsResource.setCompletedExperiments(completedExperiments);

        List<ExperimentSummaryResource> failedExperiments = getExperimentStatisticsForState(ExperimentState.FAILED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        experimentStatisticsResource.setFailedExperimentCount(failedExperiments.size());
        experimentStatisticsResource.setFailedExperiments(failedExperiments);

        List<ExperimentSummaryResource> cancelledExperiments = getExperimentStatisticsForState(ExperimentState.CANCELED, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName);
        cancelledExperiments.addAll(getExperimentStatisticsForState(ExperimentState.CANCELING, gatewayId, fromTime, toTime, userName, applicationName, resourceHostName));
        experimentStatisticsResource.setCancelledExperimentCount(cancelledExperiments.size());
        experimentStatisticsResource.setCancelledExperiments(cancelledExperiments);

        return experimentStatisticsResource;
    }

    private List<ExperimentSummaryResource> getExperimentStatisticsForState(
            ExperimentState expState, String gatewayId, Timestamp fromTime, Timestamp toTime,
            String userName, String applicationName, String resourceHostName) throws RegistryException {
        EntityManager em = null;
        List<ExperimentSummaryResource> result = new ArrayList();
        try {
            Map<String, Object> queryParameters = new HashMap<>();
            String query = "SELECT e FROM ExperimentSummary e " +
                    "WHERE ";
            if (expState != null) {
                query += "e.state='" + expState.toString() + "' AND ";
            }
            query += "e.creationTime > '" + fromTime + "' " + "AND e.creationTime <'" + toTime + "' AND ";
            query += "e." + ExperimentConstants.GATEWAY_ID + "= '" + gatewayId + "' ";
            if (userName != null) {
                query += "AND e.userName LIKE :userName ";
                queryParameters.put("userName", "%" + userName + "%");
            }
            if (applicationName != null) {
                query += "AND e.executionId LIKE :applicationName ";
                queryParameters.put("applicationName", "%" + applicationName + "%");
            }
            if (resourceHostName != null) {
                query += "AND e.resourceHostId LIKE :resourceHostName ";
                queryParameters.put("resourceHostName", "%" + resourceHostName + "%");
            }
            query += "ORDER BY e.creationTime DESC";

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(query);
            for (String parameterName : queryParameters.keySet()) {
                q.setParameter(parameterName, queryParameters.get(parameterName));
            }

            List resultList = q.getResultList();
            for (Object o : resultList) {
                ExperimentSummary experimentSummary = (ExperimentSummary) o;
                ExperimentSummaryResource experimentSummaryResource =
                        (ExperimentSummaryResource) Utils.getResource(ResourceType.EXPERIMENT_SUMMARY,
                                experimentSummary);
                result.add(experimentSummaryResource);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }
}
