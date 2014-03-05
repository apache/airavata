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
    private TaskDetailResource taskDetailResource;
    private String jobDescription;
    private Timestamp creationTime;
    private String computeResourceConsumed;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
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

    @Override
    public Resource create(ResourceType type) {
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

    @Override
    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.JOB_ID, name);
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
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.JOB_ID, name);
                q = generator.selectQuery(em);
                Status status = (Status)q.getSingleResult();
                StatusResource statusResource = (StatusResource)Utils.getResource(ResourceType.STATUS, status);
                em.getTransaction().commit();
                em.close();
                return statusResource;
            case ERROR_DETAIL:
                generator = new QueryGenerator(ERROR_DETAIL);
                generator.setParameter(ErrorDetailConstants.JOB_ID, name);
                q = generator.selectQuery(em);
                ErrorDetail errorDetail = (ErrorDetail)q.getSingleResult();
                ErrorDetailResource errorDetailResource = (ErrorDetailResource)Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                em.getTransaction().commit();
                em.close();
                return errorDetailResource;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for job details resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for job details resource.");
        }
    }

    @Override
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.JOB_ID, jobId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Status status = (Status) result;
                        StatusResource statusResource =
                                (StatusResource)Utils.getResource(ResourceType.STATUS, status);
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
                                (ErrorDetailResource)Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
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
        return resourceList;
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        JobDetail existingJobDetail = em.find(JobDetail.class, new JobDetails_PK(jobId, taskDetailResource.getTaskId()));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        JobDetail jobDetail = new JobDetail();
        TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
        jobDetail.setJobId(jobId);
        jobDetail.setTask(taskDetail);
        jobDetail.setTaskId(taskDetailResource.getTaskId());
        jobDetail.setCreationTime(creationTime);
        if (jobDescription != null){
            jobDetail.setJobDescription(jobDescription.toCharArray());
        }
        jobDetail.setComputeResourceConsumed(computeResourceConsumed);
        if (existingJobDetail != null){
            existingJobDetail.setJobId(jobId);
            existingJobDetail.setTask(taskDetail);
            existingJobDetail.setTaskId(taskDetailResource.getTaskId());
            existingJobDetail.setCreationTime(creationTime);
            if (jobDescription != null){
                existingJobDetail.setJobDescription(jobDescription.toCharArray());
            }
            existingJobDetail.setComputeResourceConsumed(computeResourceConsumed);
            jobDetail = em.merge(existingJobDetail);
        }else {
            em.merge(jobDetail);
        }
        em.getTransaction().commit();
        em.close();
    }

    public StatusResource getJobStatus(){
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource jobStatus = (StatusResource) resource;
            if(jobStatus.getStatusType().equals(StatusType.JOB.toString())){
                return jobStatus;
            }
        }
        return null;
    }

    public StatusResource getApplicationStatus(){
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource appStatus = (StatusResource) resource;
            if(appStatus.getStatusType().equals(StatusType.APPLICATION.toString())){
                return appStatus;
            }
        }
        return null;
    }

    public List<ErrorDetailResource> getErrorDetails (){
        List<ErrorDetailResource> errorDetailResources = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.ERROR_DETAIL);
        for(Resource resource : resources){
            errorDetailResources.add((ErrorDetailResource)resource);
        }
        return errorDetailResources;
    }


}
