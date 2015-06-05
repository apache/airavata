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
import org.apache.airavata.registry.core.experiment.catalog.model.QosParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.cpi.RegistryException;

import javax.persistence.EntityManager;
import java.util.List;

public class QosParamResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(QosParamResource.class);
    private int  qosId;
    private String experimentId;
    private String taskId;
    private String startExecutionAt;
    private String executeBefore;
    private int noOfRetries;

    public int getQosId() {
        return qosId;
    }

    public void setQosId(int qosId) {
        this.qosId = qosId;
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

    public String getStartExecutionAt() {
        return startExecutionAt;
    }

    public void setStartExecutionAt(String startExecutionAt) {
        this.startExecutionAt = startExecutionAt;
    }

    public String getExecuteBefore() {
        return executeBefore;
    }

    public void setExecuteBefore(String executeBefore) {
        this.executeBefore = executeBefore;
    }

    public int getNoOfRetries() {
        return noOfRetries;
    }

    public void setNoOfRetries(int noOfRetries) {
        this.noOfRetries = noOfRetries;
    }

    
    public ExperimentCatResource create(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for qos params resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for qos params resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for qos params resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for qos params resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QosParam qosParam = new QosParam();
            qosParam.setTaskId(taskId);
            qosParam.setExpId(experimentId);
            qosParam.setStartExecutionAt(startExecutionAt);
            qosParam.setExecuteBefore(executeBefore);
            qosParam.setNoOfRetries(noOfRetries);
            em.persist(qosParam);
            qosId = qosParam.getQosId();
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
