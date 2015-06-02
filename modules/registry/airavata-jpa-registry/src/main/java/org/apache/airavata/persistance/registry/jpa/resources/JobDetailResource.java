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

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JobDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(JobDetailResource.class);
    private String jobId;
    private String taskId;
    private String jobDescription;
    private Timestamp creationTime;
    private String computeResourceConsumed;
    private String jobName;
    private String workingDir;
    private StatusResource jobStatus;
    private List<ErrorDetailResource> errors;

    public void setJobStatus(StatusResource jobStatus) {
        this.jobStatus = jobStatus;
    }

    public List<ErrorDetailResource> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetailResource> errors) {
        this.errors = errors;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getComputeResourceConsumed() {
        return computeResourceConsumed;
    }

    public void setComputeResourceConsumed(String computeResourceConsumed) {
        this.computeResourceConsumed = computeResourceConsumed;
    }

    
    public Resource create(ResourceType type) throws RegistryException {
        switch (type){
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setJobId(jobId);
                return statusResource;
            case ERROR_DETAIL:
                ErrorDetailResource errorDetailResource = new ErrorDetailResource();
                errorDetailResource.setJobId(jobId);
                return errorDetailResource;
            default:
                logger.error("Unsupported resource type for job details data resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.JOB_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.JOB.toString());
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(ErrorDetailConstants.JOB_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for job details resource.", new IllegalArgumentException());
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

    
    public Resource get(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.JOB_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.JOB.toString());
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusResource statusResource = (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.JOB_ID, name);
                    q = generator.selectQuery(em);
                    ErrorDetail errorDetail = (ErrorDetail) q.getSingleResult();
                    ErrorDetailResource errorDetailResource = (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                    em.getTransaction().commit();
                    em.close();
                    return errorDetailResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for job details resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for job details resource.");
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
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.JOB_ID, jobId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Status status = (Status) result;
                            StatusResource statusResource =
                                    (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                            resourceList.add(statusResource);
                        }
                    }
                    break;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.JOB_ID, jobId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            ErrorDetail errorDetail = (ErrorDetail) result;
                            ErrorDetailResource errorDetailResource =
                                    (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                            resourceList.add(errorDetailResource);
                        }
                    }
                    break;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for workflow node details resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
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
        return resourceList;
    }

    
    public void save()  throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            JobDetail existingJobDetail = em.find(JobDetail.class, new JobDetails_PK(jobId, taskId));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            JobDetail jobDetail = new JobDetail();
            jobDetail.setJobId(jobId);
            jobDetail.setTaskId(taskId);
            jobDetail.setCreationTime(creationTime);
            jobDetail.setJobName(jobName);
            jobDetail.setWorkingDir(workingDir);
            if (jobDescription != null) {
                jobDetail.setJobDescription(jobDescription.toCharArray());
            }
            jobDetail.setComputeResourceConsumed(computeResourceConsumed);
            if (existingJobDetail != null) {
                existingJobDetail.setJobId(jobId);
                existingJobDetail.setTaskId(taskId);
                existingJobDetail.setCreationTime(creationTime);
                if (jobDescription != null) {
                    existingJobDetail.setJobDescription(jobDescription.toCharArray());
                }
                existingJobDetail.setComputeResourceConsumed(computeResourceConsumed);
                existingJobDetail.setJobName(jobName);
                existingJobDetail.setWorkingDir(workingDir);
                jobDetail = em.merge(existingJobDetail);
            } else {
                em.persist(jobDetail);
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

    public StatusResource getJobStatus() {
        return jobStatus;
    }

    public StatusResource getJobStatus1() throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource jobStatus = (StatusResource) resource;
            if(jobStatus.getStatusType().equals(StatusType.JOB.toString())){
                if (jobStatus.getState() == null || jobStatus.getState().equals("") ){
                    jobStatus.setState("UNKNOWN");
                }
                return jobStatus;
            }
        }
        return null;
    }

    public StatusResource getApplicationStatus() throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource appStatus = (StatusResource) resource;
            if(appStatus.getStatusType().equals(StatusType.APPLICATION.toString())){
                if (appStatus.getState() == null || appStatus.getState().equals("") ){
                    appStatus.setState("UNKNOWN");
                }
                return appStatus;
            }
        }
        return null;
    }

    public List<ErrorDetailResource> getErrorDetails () throws RegistryException{
        List<ErrorDetailResource> errorDetailResources = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.ERROR_DETAIL);
        for(Resource resource : resources){
            errorDetailResources.add((ErrorDetailResource)resource);
        }
        return errorDetailResources;
    }


}
