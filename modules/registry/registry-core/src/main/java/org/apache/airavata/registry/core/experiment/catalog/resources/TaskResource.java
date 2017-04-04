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

import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Job;
import org.apache.airavata.registry.core.experiment.catalog.model.Task;
import org.apache.airavata.registry.core.experiment.catalog.model.TaskError;
import org.apache.airavata.registry.core.experiment.catalog.model.TaskStatus;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaskResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(TaskResource.class);
    private String taskId;
    private String taskType;
    private String parentProcessId;
    private Timestamp creationTime;
    private Timestamp lastUpdateTime;
    private String taskDetail;
    private byte[] subTaskModel;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getParentProcessId() {
        return parentProcessId;
    }

    public void setParentProcessId(String parentProcessId) {
        this.parentProcessId = parentProcessId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
    }

    public byte[] getSubTaskModel() {
        return subTaskModel;
    }

    public void setSubTaskModel(byte[] subTaskModel) {
        this.subTaskModel = subTaskModel;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        switch (type){
            case TASK_STATUS:
                TaskStatusResource taskStatusResource = new TaskStatusResource();
                taskStatusResource.setTaskId(taskId);
                return taskStatusResource;
            case TASK_ERROR:
                TaskErrorResource taskErrorResource = new TaskErrorResource();
                taskErrorResource.setTaskId(taskId);
                return taskErrorResource;
            case JOB:
                JobResource jobResource = new JobResource();
                jobResource.setTaskId(taskId);
                return jobResource;
            default:
                logger.error("Unsupported resource type for task data resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case TASK_STATUS:
                    generator = new QueryGenerator(TASK_STATUS);
                    generator.setParameter(TaskStatusConstants.STATUS_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case TASK_ERROR:
                    generator = new QueryGenerator(TASK_ERROR);
                    generator.setParameter(TaskErrorConstants.ERROR_ID, name);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                case JOB:
                    generator = new QueryGenerator(JOB);
                    generator.setParameter(JobConstants.JOB_ID, name);
                    generator.setParameter(JobConstants.TASK_ID, taskId);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for job details resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
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

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case TASK_STATUS:
                    generator = new QueryGenerator(TASK_STATUS);
                    generator.setParameter(TaskStatusConstants.STATUS_ID, name);
                    q = generator.selectQuery(em);
                    TaskStatus status = (TaskStatus) q.getSingleResult();
                    TaskStatusResource statusResource = (TaskStatusResource) Utils.getResource(ResourceType.TASK_STATUS, status);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return statusResource;
                case TASK_ERROR:
                    generator = new QueryGenerator(TASK_ERROR);
                    generator.setParameter(TaskErrorConstants.ERROR_ID, name);
                    q = generator.selectQuery(em);
                    TaskError error = (TaskError) q.getSingleResult();
                    TaskErrorResource errorResource = (TaskErrorResource) Utils.getResource(
                            ResourceType.TASK_ERROR, error
                    );
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return errorResource;
                case JOB:
                    generator = new QueryGenerator(JOB);
                    generator.setParameter(JobConstants.JOB_ID, name);
                    generator.setParameter(JobConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    Job job = (Job) q.getSingleResult();
                    JobResource jobResource = (JobResource) Utils.getResource(ResourceType.JOB, job);
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    return jobResource;
                default:
                    em.getTransaction().commit();
                    if (em.isOpen()) {
                        if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
                        em.close();
                    }
                    logger.error("Unsupported resource type for Task resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for Task resource.");
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
                case TASK_ERROR:
                    generator = new QueryGenerator(TASK_ERROR);
                    generator.setParameter(TaskErrorConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            TaskError taskError = (TaskError) result;
                            TaskErrorResource taskErrorResource =
                                    (TaskErrorResource) Utils.getResource(ResourceType.TASK_ERROR, taskError);
                            resourceList.add(taskErrorResource);
                        }
                    }
                    break;
                case TASK_STATUS:
                    generator = new QueryGenerator(TASK_STATUS);
                    generator.setParameter(TaskStatusConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            TaskStatus taskStatus = (TaskStatus) result;
                            TaskStatusResource taskStatusResource =
                                    (TaskStatusResource) Utils.getResource(ResourceType.TASK_STATUS, taskStatus);
                            resourceList.add(taskStatusResource);
                        }
                    }
                    break;
                case JOB:
                    generator = new QueryGenerator(JOB);
                    generator.setParameter(JobConstants.TASK_ID, taskId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Job job = (Job) result;
                            JobResource jobResource =
                                    (JobResource) Utils.getResource(ResourceType.JOB, job);
                            resourceList.add(jobResource);
                        }
                    }
                    break;
                default:
                    logger.error("Unsupported resource type for task resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
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

    
    public void save()  throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            Task task;
            Task existingTask = em.find(Task.class, taskId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if(existingTask == null){
                task = new Task();
            }else {
                task = existingTask;
            }
            task.setTaskId(taskId);
            task.setTaskType(taskType);
            task.setParentProcessId(parentProcessId);
            task.setCreationTime(creationTime);
            task.setLastUpdateTime(lastUpdateTime);
            task.setTaskDetail(taskDetail);
            task.setSetSubTaskModel(subTaskModel);
            if (existingTask == null){
                em.persist(task);
            }else {
                em.merge(task);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
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

    public List<TaskStatusResource> getTaskStatuses() throws RegistryException{
        List<TaskStatusResource> taskStatusResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.TASK_STATUS);
        for (ExperimentCatResource resource : resources) {
            TaskStatusResource statusResource = (TaskStatusResource) resource;
            taskStatusResources.add(statusResource);
        }
        return taskStatusResources;
    }

    public TaskStatusResource getTaskStatus() throws RegistryException{
        List<TaskStatusResource> taskStatusResources = getTaskStatuses();
        if(taskStatusResources.size() == 0){
            return null;
        }else{
            TaskStatusResource max = taskStatusResources.get(0);
            for(int i=1; i<taskStatusResources.size();i++) {
                if (taskStatusResources.get(i).getTimeOfStateChange().after(max.getTimeOfStateChange())
                  || (taskStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && taskStatusResources.get(i).getState().equals(TaskState.COMPLETED.toString()))
                        || (taskStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && taskStatusResources.get(i).getState().equals(TaskState.FAILED.toString()))
                        || (taskStatusResources.get(i).getTimeOfStateChange().equals(max.getTimeOfStateChange()) && taskStatusResources.get(i).getState().equals(TaskState.CANCELED.toString()))) {
                    max = taskStatusResources.get(i);
                }
            }
            return max;
        }
    }

    public List<TaskErrorResource> getTaskErrors() throws RegistryException{
        List<TaskErrorResource> taskErrorResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.TASK_ERROR);
        for (ExperimentCatResource resource : resources) {
            TaskErrorResource errorResource = (TaskErrorResource) resource;
            taskErrorResources.add(errorResource);
        }
        return taskErrorResources;
    }

    public TaskErrorResource getTaskError() throws RegistryException{
        List<TaskErrorResource> taskErrorResources = getTaskErrors();
        if(taskErrorResources.size() == 0){
            return null;
        }else{
            TaskErrorResource max = taskErrorResources.get(0);
            for(int i=1; i<taskErrorResources.size();i++){
                if(taskErrorResources.get(i).getCreationTime().after(max.getCreationTime())){
                    max = taskErrorResources.get(i);
                }
            }
            return max;
        }
    }

    public JobResource getJob(String jobId) throws RegistryException {
        return (JobResource) get(ResourceType.JOB, jobId);
    }

    public List<JobResource> getJobList() throws RegistryException {
        List<JobResource> jobResources = new ArrayList();
        List<ExperimentCatResource> resources = get(ResourceType.JOB);
        for (ExperimentCatResource resource : resources) {
            JobResource jobResource = (JobResource) resource;
            jobResources.add(jobResource);
        }
        return jobResources;
    }
}