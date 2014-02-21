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
import org.apache.airavata.persistance.registry.jpa.model.ErrorDetail;
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.TaskDetail;
import org.apache.airavata.persistance.registry.jpa.model.WorkflowNodeDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class ErrorDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ErrorDetailResource.class);
    private int errorId;
    private ExperimentResource experimentResource;
    private TaskDetailResource taskDetailResource;
    private WorkflowNodeDetailResource nodeDetail;
    private Timestamp creationTime;
    private String actualErrorMsg;
    private String userFriendlyErrorMsg;
    private boolean transientPersistent;
    private String errorCategory;
    private String correctiveAction;
    private String actionableGroup;
    private String jobId;

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getActualErrorMsg() {
        return actualErrorMsg;
    }

    public void setActualErrorMsg(String actualErrorMsg) {
        this.actualErrorMsg = actualErrorMsg;
    }

    public String getUserFriendlyErrorMsg() {
        return userFriendlyErrorMsg;
    }

    public void setUserFriendlyErrorMsg(String userFriendlyErrorMsg) {
        this.userFriendlyErrorMsg = userFriendlyErrorMsg;
    }

    public boolean isTransientPersistent() {
        return transientPersistent;
    }

    public void setTransientPersistent(boolean transientPersistent) {
        this.transientPersistent = transientPersistent;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(String errorCategory) {
        this.errorCategory = errorCategory;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getActionableGroup() {
        return actionableGroup;
    }

    public void setActionableGroup(String actionableGroup) {
        this.actionableGroup = actionableGroup;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
    }

    public WorkflowNodeDetailResource getNodeDetail() {
        return nodeDetail;
    }

    public void setNodeDetail(WorkflowNodeDetailResource nodeDetail) {
        this.nodeDetail = nodeDetail;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        ErrorDetail existingErrorDetail = em.find(ErrorDetail.class, errorId);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorID(errorId);
        Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
        errorDetail.setExperiment(experiment);
        TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
        errorDetail.setTask(taskDetail);
        WorkflowNodeDetail workflowNodeDetail = em.find(WorkflowNodeDetail.class, nodeDetail.getNodeInstanceId());
        errorDetail.setNodeDetails(workflowNodeDetail);
        errorDetail.setCreationTime(creationTime);
        errorDetail.setActualErrorMsg(actualErrorMsg.toCharArray());
        errorDetail.setUserFriendlyErrorMsg(userFriendlyErrorMsg);
        errorDetail.setTransientPersistent(transientPersistent);
        errorDetail.setErrorCategory(errorCategory);
        errorDetail.setCorrectiveAction(correctiveAction);
        errorDetail.setActionableGroup(actionableGroup);
        errorDetail.setJobId(jobId);
        if (existingErrorDetail != null){
            existingErrorDetail.setErrorID(errorId);
            existingErrorDetail.setExperiment(experiment);
            existingErrorDetail.setTask(taskDetail);
            existingErrorDetail.setNodeDetails(workflowNodeDetail);
            existingErrorDetail.setCreationTime(creationTime);
            existingErrorDetail.setActualErrorMsg(actualErrorMsg.toCharArray());
            existingErrorDetail.setUserFriendlyErrorMsg(userFriendlyErrorMsg);
            existingErrorDetail.setTransientPersistent(transientPersistent);
            existingErrorDetail.setErrorCategory(errorCategory);
            existingErrorDetail.setCorrectiveAction(correctiveAction);
            existingErrorDetail.setActionableGroup(actionableGroup);
            existingErrorDetail.setJobId(jobId);
            errorDetail = em.merge(existingErrorDetail);
        }else {
            em.merge(errorDetail);
        }
        em.getTransaction().commit();
        em.close();
    }
}
