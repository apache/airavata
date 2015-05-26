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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.cpi.RegistryException;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class StatusResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(StatusResource.class);
    private int statusId = 0;
    private ExperimentResource experimentResource;
    private WorkflowNodeDetailResource workflowNodeDetail;
    private DataTransferDetailResource dataTransferDetail;
    private TaskDetailResource taskDetailResource;
    private String jobId;
    private String state;
    private Timestamp statusUpdateTime;
    private String statusType;

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public WorkflowNodeDetailResource getWorkflowNodeDetail() {
        return workflowNodeDetail;
    }

    public void setWorkflowNodeDetail(WorkflowNodeDetailResource workflowNodeDetail) {
        this.workflowNodeDetail = workflowNodeDetail;
    }

    public DataTransferDetailResource getDataTransferDetail() {
        return dataTransferDetail;
    }

    public void setDataTransferDetail(DataTransferDetailResource dataTransferDetail) {
        this.dataTransferDetail = dataTransferDetail;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    
    public Resource create(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for status resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for status resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for status resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for status resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Status status;
            if (statusId != 0) {
                status = em.find(Status.class, statusId);
                status.setStatusId(statusId);
            } else {
                status = new Status();
            }
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            if (taskDetailResource != null) {
                TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
                status.setTask(taskDetail);
                status.setTaskId(taskDetailResource.getTaskId());
            }
            if (workflowNodeDetail != null) {
                WorkflowNodeDetail nodeDetail = em.find(WorkflowNodeDetail.class, workflowNodeDetail.getNodeInstanceId());
                status.setNode(nodeDetail);
                status.setNodeId(workflowNodeDetail.getNodeInstanceId());
            }
            if (dataTransferDetail != null) {
                DataTransferDetail transferDetail = em.find(DataTransferDetail.class, dataTransferDetail.getTransferId());
                status.setTransferDetail(transferDetail);
                status.setTransferId(dataTransferDetail.getTransferId());
            }
            status.setExperiment(experiment);
            status.setJobId(jobId);
            status.setExpId(experimentResource.getExpID());
            status.setState(state);
            status.setStatusUpdateTime(statusUpdateTime);
            status.setStatusType(statusType);
            em.persist(status);
            statusId = status.getStatusId();
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
}
