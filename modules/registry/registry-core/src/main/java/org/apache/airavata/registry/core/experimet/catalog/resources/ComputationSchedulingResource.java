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

package org.apache.airavata.experiment.catalog.resources;

import org.apache.airavata.experiment.catalog.Resource;
import org.apache.airavata.experiment.catalog.ResourceType;
import org.apache.airavata.experiment.catalog.ResourceUtils;
import org.apache.airavata.experiment.catalog.model.Computational_Resource_Scheduling;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class ComputationSchedulingResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ComputationSchedulingResource.class);
    private int schedulingId = 0;
    private String experimentId;
    private String taskId;
    private String resourceHostId;
    private int cpuCount;
    private int nodeCount;
    private int numberOfThreads;
    private String queueName;
    private int walltimeLimit;
    private Timestamp jobStartTime;
    private int physicalMemory;
    private String projectName;
    private String chessisName;

    public String getChessisName() {
        return chessisName;
    }

    public void setChessisName(String chessisName) {
        this.chessisName = chessisName;
    }

    public int getSchedulingId() {
        return schedulingId;
    }

    public void setSchedulingId(int schedulingId) {
        this.schedulingId = schedulingId;
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

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getWalltimeLimit() {
        return walltimeLimit;
    }

    public void setWalltimeLimit(int walltimeLimit) {
        this.walltimeLimit = walltimeLimit;
    }

    public Timestamp getJobStartTime() {
        return jobStartTime;
    }

    public void setJobStartTime(Timestamp jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    
    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for computational scheduling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for computational scheduling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for computational scheduling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for computational scheduling resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Computational_Resource_Scheduling scheduling;
            if (schedulingId != 0) {
                scheduling = em.find(Computational_Resource_Scheduling.class, schedulingId);
                scheduling.setSchedulingId(schedulingId);
            } else {
                scheduling = new Computational_Resource_Scheduling();
            }
            scheduling.setExpId(experimentId);
            scheduling.setTaskId(taskId);
            scheduling.setResourceHostId(resourceHostId);
            scheduling.setCpuCount(cpuCount);
            scheduling.setNodeCount(nodeCount);
            scheduling.setNumberOfThreads(numberOfThreads);
            scheduling.setQueueName(queueName);
            scheduling.setWallTimeLimit(walltimeLimit);
            scheduling.setJobStartTime(jobStartTime);
            scheduling.setTotalPhysicalmemory(physicalMemory);
            scheduling.setProjectName(projectName);
            scheduling.setChessisName(chessisName);
            em.persist(scheduling);
            schedulingId = scheduling.getSchedulingId();
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
