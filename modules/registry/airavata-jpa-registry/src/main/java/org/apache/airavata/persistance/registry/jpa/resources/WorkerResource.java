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

import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

public class WorkerResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkerResource.class);
    private String user;
	private GatewayResource gateway;

    /**
     *
     */
    public WorkerResource() {
    }

    /**
     *
     * @param user username
     * @param gateway  gatewayResource
     */
    public WorkerResource(String user, GatewayResource gateway) {
		this.setUser(user);
		this.gateway=gateway;
	}

    /**
     * Gateway worker can create child data structures such as projects and user workflows
     * @param type child resource type
     * @return  child resource
     */
	public Resource create(ResourceType type) throws RegistryException{
		Resource result = null;
		switch (type) {
			case PROJECT:
				ProjectResource projectResource = new ProjectResource();
				projectResource.setWorker(this);
				projectResource.setGateway(gateway);
				result=projectResource;
				break;
            case EXPERIMENT:
                ExperimentResource experimentResource = new ExperimentResource();
                experimentResource.setExecutionUser(user);
                experimentResource.setGateway(gateway);
                result = experimentResource;
                break;
			default:
                logger.error("Unsupported resource type for worker resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for worker resource.");

		}
		return result;
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
	public Resource get(ResourceType type, Object name) throws RegistryException{
        Resource result = null;
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
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
                if (em.getTransaction().isActive()){
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
    public List<Resource> get(ResourceType type) throws RegistryException{
        return get(type, -1, -1, null, null);
    }

    /**
     * Method get all results of the given child resource type with paginaltion and ordering
     *
     * @param type child resource type
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return list of child resources
     * @throws RegistryException
     */
    public List<Resource> get(ResourceType type, int limit, int offset, Object orderByIdentifier,
                              ResultOrderType resultOrderType) throws RegistryException{
        List<Resource> result = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case PROJECT:
                    generator = new QueryGenerator(PROJECT);
                    Users users = em.find(Users.class, getUser());
                    Gateway gatewayModel = em.find(Gateway.class, gateway.getGatewayId());
                    generator.setParameter("users", users);
                    generator.setParameter("gateway", gatewayModel);

                    //ordering - only supported only by CREATION_TIME
                    if(orderByIdentifier != null && resultOrderType != null
                            && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
                        q = generator.selectQuery(em, ProjectConstants.CREATION_TIME, resultOrderType);
                    }else{
                        q = generator.selectQuery(em);
                    }

                    //pagination
                    if(limit>0 && offset>=0){
                        q.setFirstResult(offset);
                        q.setMaxResults(limit);
                    }

                    for (Object o : q.getResultList()) {
                        Project project = (Project) o;
                        ProjectResource projectResource = (ProjectResource) Utils.getResource(ResourceType.PROJECT, project);
                        result.add(projectResource);
                    }
                    break;
                case EXPERIMENT:
                    generator = new QueryGenerator(EXPERIMENT);
                    generator.setParameter(ExperimentConstants.EXECUTION_USER, getUser());

                    //ordering - only supported only by CREATION_TIME
                    if(orderByIdentifier != null && resultOrderType != null
                            && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)) {
                        q = generator.selectQuery(em, ExperimentConstants.CREATION_TIME, resultOrderType);
                    }else{
                        q = generator.selectQuery(em);
                    }

                    //pagination
                    if(limit>0 && offset>=0){
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
                if (em.getTransaction().isActive()){
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
	public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Gateway_Worker existingWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gateway.getGatewayId(), user));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Gateway_Worker gatewayWorker = new Gateway_Worker();
            Users existingUser = em.find(Users.class, this.user);
            gatewayWorker.setUser(existingUser);
            gatewayWorker.setUser_name(existingUser.getUser_name());
            Gateway gatewaymodel = em.find(Gateway.class, gateway.getGatewayId());
            gatewayWorker.setGateway(gatewaymodel);
            gatewayWorker.setGateway_id(gatewaymodel.getGateway_id());
            if (existingWorker != null) {
                existingWorker.setUser_name(existingUser.getUser_name());
                existingWorker.setUser(existingUser);
                existingWorker.setGateway(gatewaymodel);
                existingWorker.setGateway_id(gatewaymodel.getGateway_id());
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
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     *
     * @return user name
     */
	public String getUser() {
		return user;
	}

    /**
     *
     * @param user user name
     */
    public void setUser(String user) {
		this.user = user;
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
     * @param gateway  gateway resource
     */
    public void setGateway(GatewayResource gateway) {
        this.gateway = gateway;
    }

    /**
     *
     * @param id  project id
     * @return whether the project is available under the user
     */
    public boolean isProjectExists(String id) throws RegistryException{
		return isExists(ResourceType.PROJECT, id);
	}

    /**
     *
     * @param projectId project id
     * @return project resource for the user
     */
	public ProjectResource createProject(String projectId) throws RegistryException{
		ProjectResource project=(ProjectResource)create(ResourceType.PROJECT);
        project.setId(projectId);
		return project;
	}

    public String getProjectID(String projectName) {
        String pro = projectName.replaceAll("\\s", "");
        return pro + "_" + UUID.randomUUID();
    }

    /**
     *
     * @param id project id
     * @return project resource
     */
	public ProjectResource getProject(String id) throws RegistryException{
		return (ProjectResource)get(ResourceType.PROJECT, id);
	}

    /**
     *
     * @param id project id
     */
	public void removeProject(String id) throws RegistryException{
		remove(ResourceType.PROJECT, id);
	}

    /**
     * Get projects list of user
     * @return  list of projects for the user
     */
    public List<ProjectResource> getProjects() throws RegistryException{
		return getProjects(-1, -1, null, null);
	}


    /**
     * Get projects list of user with pagination and ordering
     *
     * @return  list of projects for the user
     */
    public List<ProjectResource> getProjects(int limit, int offset, Object orderByIdentifier,
                                             ResultOrderType resultOrderType) throws RegistryException{
        List<ProjectResource> result=new ArrayList<ProjectResource>();
        List<Resource> list = get(ResourceType.PROJECT, limit, offset, orderByIdentifier, resultOrderType);
        for (Resource resource : list) {
            result.add((ProjectResource) resource);
        }
        return result;
    }

    /**
     *
     * @param name experiment name
     * @return whether experiment is already exist for the given user
     */
	public boolean isExperimentExists(String name) throws RegistryException{
		return isExists(ResourceType.EXPERIMENT, name);
	}
	

    /**
     *
     * @param name experiment name
     * @return experiment resource
     */
    public ExperimentResource getExperiment(String name) throws RegistryException{
		return (ExperimentResource)get(ResourceType.EXPERIMENT, name);
	}
//
//    public GFacJobDataResource getGFacJob(String jobId){
//    	return (GFacJobDataResource)get(ResourceType.GFAC_JOB_DATA,jobId);
//    }

    /**
     * Method to get list of expeirments of user
     * @return list of experiments for the user
     */
	public List<ExperimentResource> getExperiments() throws RegistryException{
		return getExperiments(-1, -1, null, null);
	}

    /**
     * Method to get list of experiments of user with pagination and ordering
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<ExperimentResource> getExperiments(int limit, int offset, Object orderByIdentifier,
                                                   ResultOrderType resultOrderType) throws RegistryException{
        List<ExperimentResource> result=new ArrayList<ExperimentResource>();
        List<Resource> list = get(ResourceType.EXPERIMENT, limit, offset, orderByIdentifier, resultOrderType);
        for (Resource resource : list) {
            result.add((ExperimentResource) resource);
        }
        return result;
    }

    /**
     *
     * @param experimentId  experiment name
     */
	public void removeExperiment(String experimentId) throws RegistryException{
		remove(ResourceType.EXPERIMENT, experimentId);
	}

    /**
     * To search projects of user with the given filter criteria. All the matching results will be sent.
     * Results are not ordered in any order
     * @param filters
     * @return
     * @throws RegistryException
     */
    public List<ProjectResource> searchProjects (Map<String, String> filters) throws RegistryException{
        return searchProjects(filters, -1, -1, null, null);
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
     * @throws RegistryException
     */
    public List<ProjectResource> searchProjects(Map<String, String> filters, int limit,
             int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ProjectResource> result = new ArrayList<ProjectResource>();
        EntityManager em = null;
        try {
            String query = "SELECT p from Project p WHERE ";
            if (filters != null && filters.size() != 0) {
                for (String field : filters.keySet()) {
                    String filterVal = filters.get(field);
                    if (field.equals(ProjectConstants.USERNAME)) {
                        query += "p." + field + "= '" + filterVal + "' AND ";
                    }else if (field.equals(ProjectConstants.GATEWAY_ID)) {
                        query += "p." + field + "= '" + filterVal + "' AND ";
                    }else {
                        if (filterVal.contains("*")){
                            filterVal = filterVal.replaceAll("\\*", "");
                        }
                        query += "p." + field + " LIKE '%" + filterVal + "%' AND ";
                    }
                }
            }
            query = query.substring(0, query.length() - 5);

            //ordering
            if( orderByIdentifier != null && resultOrderType != null
                    && orderByIdentifier.equals(Constants.FieldConstants.ProjectConstants.CREATION_TIME)){
                String order = (resultOrderType == ResultOrderType.ASC) ? "ASC" : "DESC";
                query += " ORDER BY p." + ProjectConstants.CREATION_TIME + " " + order;
            }

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;

            //pagination
            if(offset>=0 && limit >=0){
                q = em.createQuery(query).setFirstResult(offset).setMaxResults(limit);
            }else{
                q = em.createQuery(query);
            }

            List resultList = q.getResultList();
            for (Object o : resultList) {
                Project project = (Project) o;
                ProjectResource projectResource =
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
                if (em.getTransaction().isActive()){
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
     * @throws RegistryException
     */
    public List<ExperimentSummaryResource> searchExperiments(Timestamp fromTime, Timestamp toTime, Map<String, String> filters, int limit,
                                                             int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentSummaryResource> result = new ArrayList();
        EntityManager em = null;
        try {
            String query = "SELECT e, s FROM Experiment e " +
                    ",Status s WHERE e.expId=s.expId AND " +
                    "s.statusType='" + StatusType.EXPERIMENT + "' AND ";
            if (filters.get(StatusConstants.STATE) != null) {
                String experimentState = ExperimentState.valueOf(filters.get(StatusConstants.STATE)).toString();
                query += "s.state='" + experimentState + "' AND ";
            }

            if (toTime != null && fromTime != null && toTime.after(fromTime)) {
                query += "e.creationTime > '" + fromTime + "' " + "AND e.creationTime <'" + toTime + "' AND ";
            }

            filters.remove(StatusConstants.STATE);
            if (filters != null && filters.size() != 0) {
                for (String field : filters.keySet()) {
                    String filterVal = filters.get(field);
                    if (field.equals(ExperimentConstants.EXECUTION_USER)) {
                        query += "e." + field + "= '" + filterVal + "' AND ";
                    } else if (field.equals(ExperimentConstants.GATEWAY_ID)) {
                        query += "e." + field + "= '" + filterVal + "' AND ";
                    } else if (field.equals(ExperimentConstants.PROJECT_ID)) {
                        query += "e." + field + "= '" + filterVal + "' AND ";
                    } else {
                        if (filterVal.contains("*")) {
                            filterVal = filterVal.replaceAll("\\*", "");
                        }
                        query += "e." + field + " LIKE '%" + filterVal + "%' AND ";
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

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;

            //pagination
            if (offset >= 0 && limit >= 0) {
                q = em.createQuery(query).setFirstResult(offset).setMaxResults(limit);
            } else {
                q = em.createQuery(query);
            }
            OpenJPAQuery kq = OpenJPAPersistence.cast(q);
            JDBCFetchPlan fetch = (JDBCFetchPlan) kq.getFetchPlan();
            fetch.setEagerFetchMode(FetchMode.JOIN);

            List resultList = q.getResultList();
            for (Object o : resultList) {
                Experiment experiment = (Experiment) ((Object[]) o)[0];
                Status experimentStatus = (Status) ((Object[]) o)[1];
                experiment.setStatuses(Arrays.asList(experimentStatus));
                ExperimentSummaryResource experimentSummaryResource =
                        (ExperimentSummaryResource) Utils.getResource(ResourceType.EXPERIMENT_SUMMARY, experiment);
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
     * @throws RegistryException
     */
    public ExperimentStatisticsResource getExperimentStatistics(String gatewayId, Timestamp fromTime, Timestamp toTime) throws RegistryException {
        ExperimentStatisticsResource experimentStatisticsResource = new ExperimentStatisticsResource();
        List<ExperimentSummaryResource> allExperiments = getExperimentStatisticsForState(null, gatewayId, fromTime, toTime);
        experimentStatisticsResource.setAllExperimentCount(allExperiments.size());
        experimentStatisticsResource.setAllExperiments(allExperiments);

        List<ExperimentSummaryResource> createdExperiments = getExperimentStatisticsForState(ExperimentState.CREATED, gatewayId, fromTime, toTime);
        createdExperiments.addAll(getExperimentStatisticsForState(ExperimentState.VALIDATED, gatewayId, fromTime, toTime));
        experimentStatisticsResource.setCreatedExperimentCount(createdExperiments.size());
        experimentStatisticsResource.setCreatedExperiments(createdExperiments);

        List<ExperimentSummaryResource> runningExperiments = getExperimentStatisticsForState(ExperimentState.EXECUTING, gatewayId, fromTime, toTime);
        runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.SCHEDULED, gatewayId, fromTime, toTime));
        runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.LAUNCHED, gatewayId, fromTime, toTime));
        experimentStatisticsResource.setRunningExperimentCount(runningExperiments.size());
        experimentStatisticsResource.setRunningExperiments(runningExperiments);

        List<ExperimentSummaryResource> completedExperiments = getExperimentStatisticsForState(ExperimentState.COMPLETED, gatewayId, fromTime, toTime);
        experimentStatisticsResource.setCompletedExperimentCount(completedExperiments.size());
        experimentStatisticsResource.setCompletedExperiments(completedExperiments);

        List<ExperimentSummaryResource> failedExperiments = getExperimentStatisticsForState(ExperimentState.FAILED, gatewayId, fromTime, toTime);
        experimentStatisticsResource.setFailedExperimentCount(failedExperiments.size());
        experimentStatisticsResource.setFailedExperiments(failedExperiments);

        List<ExperimentSummaryResource> cancelledExperiments = getExperimentStatisticsForState(ExperimentState.CANCELED, gatewayId, fromTime, toTime);
        experimentStatisticsResource.setCancelledExperimentCount(cancelledExperiments.size());
        experimentStatisticsResource.setCancelledExperiments(cancelledExperiments);

        return experimentStatisticsResource;
    }

    private List<ExperimentSummaryResource> getExperimentStatisticsForState(
            ExperimentState expState, String gatewayId, Timestamp fromTime, Timestamp toTime) throws RegistryException {
        EntityManager em = null;
        List<ExperimentSummaryResource> result = new ArrayList();
        try {
            String query = "SELECT e, s FROM Experiment e " +
                    ",Status s WHERE e.expId=s.expId AND " +
                    "s.statusType='" + StatusType.EXPERIMENT + "' AND ";
            if (expState != null) {
                query += "s.state='" + expState.toString() + "' AND ";
            }
            query += "e.creationTime > '" + fromTime + "' " + "AND e.creationTime <'" + toTime + "' AND ";
            query += "e." + ExperimentConstants.GATEWAY_ID + "= '" + gatewayId + "'";

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(query);
            OpenJPAQuery kq = OpenJPAPersistence.cast(q);
            JDBCFetchPlan fetch = (JDBCFetchPlan) kq.getFetchPlan();
            fetch.setEagerFetchMode(FetchMode.JOIN);

            List resultList = q.getResultList();
            for (Object o : resultList) {
                Experiment experiment = (Experiment) ((Object[]) o)[0];
                Status experimentStatus = (Status) ((Object[]) o)[1];
                experiment.setStatuses(Arrays.asList(experimentStatus));
                ExperimentSummaryResource experimentSummaryResource =
                        (ExperimentSummaryResource) Utils.getResource(ResourceType.EXPERIMENT_SUMMARY, experiment);
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
