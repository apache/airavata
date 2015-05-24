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
import org.apache.airavata.registry.cpi.RegistryException;
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

    
    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ErrorDetail errorDetail;
            if (errorId != 0) {
                errorDetail = em.find(ErrorDetail.class, errorId);
                errorDetail.setErrorId(errorId);
            } else {
                errorDetail = new ErrorDetail();
            }
            errorDetail.setErrorId(errorId);
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            errorDetail.setExperiment(experiment);
            errorDetail.setExpId(experimentResource.getExpID());
            if (taskDetailResource != null) {
                TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
                errorDetail.setTask(taskDetail);
                errorDetail.setTaskId(taskDetail.getTaskId());
            }

            if (nodeDetail != null) {
                WorkflowNodeDetail workflowNodeDetail = em.find(WorkflowNodeDetail.class, nodeDetail.getNodeInstanceId());
                errorDetail.setNodeDetails(workflowNodeDetail);
                errorDetail.setNodeId(workflowNodeDetail.getNodeId());
            }
            errorDetail.setCreationTime(creationTime);
            if (actualErrorMsg != null){
                errorDetail.setActualErrorMsg(actualErrorMsg.toCharArray());
            }

            errorDetail.setUserFriendlyErrorMsg(userFriendlyErrorMsg);
            errorDetail.setTransientPersistent(transientPersistent);
            errorDetail.setErrorCategory(errorCategory);
            errorDetail.setCorrectiveAction(correctiveAction);
            errorDetail.setActionableGroup(actionableGroup);
            errorDetail.setJobId(jobId);
            em.persist(errorDetail);
            errorId = errorDetail.getErrorId();
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
