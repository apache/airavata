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

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.ErrorDetail;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class ErrorDetailResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(ErrorDetailResource.class);
    private int errorId;
    private String experimentId;
    private String taskId;
    private String nodeId;
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

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for error details data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ErrorDetail errorDetail;
            if (errorId != 0) {
                errorDetail = em.find(ErrorDetail.class, errorId);
                errorDetail.setErrorID(errorId);
            } else {
                errorDetail = new ErrorDetail();
            }
            errorDetail.setErrorID(errorId);
            errorDetail.setExpId(experimentId);
            errorDetail.setTaskId(taskId);
            errorDetail.setNodeId(nodeId);
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
            errorId = errorDetail.getErrorID();
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
