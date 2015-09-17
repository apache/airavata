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
import org.apache.airavata.registry.core.experiment.catalog.model.UserConfigurationData;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class UserConfigurationDataResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(UserConfigurationDataResource.class);
    private String experimentId;
    private boolean airavataAutoSchedule;
    private boolean overrideManualScheduledParams;
    private boolean shareExperimentPublically;
    private boolean throttleResources;
    private String userDn;
    private boolean generateCert;
    private String resourceHostId;
    private Integer totalCpuCount;
    private Integer nodeCount;
    private Integer numberOfThreads;
    private String queueName;
    private Integer wallTimeLimit;
    private Integer totalPhysicalMemory;

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public Integer getTotalCpuCount() {
        return totalCpuCount;
    }

    public void setTotalCpuCount(Integer totalCpuCount) {
        this.totalCpuCount = totalCpuCount;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Integer getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Integer getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(Integer wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public Integer getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(Integer totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public boolean getAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean getOverrideManualScheduledParams() {
        return overrideManualScheduledParams;
    }

    public void setOverrideManualScheduledParams(boolean overrideManualScheduledParams) {
        this.overrideManualScheduledParams = overrideManualScheduledParams;
    }

    public boolean getShareExperimentPublically() {
        return shareExperimentPublically;
    }

    public void setShareExperimentPublically(boolean shareExperimentPublically) {
        this.shareExperimentPublically = shareExperimentPublically;
    }

    public boolean getThrottleResources() {
        return throttleResources;
    }

    public void setThrottleResources(boolean throttleResources) {
        this.throttleResources = throttleResources;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean getGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for process resource scheduling data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for process resource scheduling data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for process resource scheduling data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for process resource scheduling data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();

            UserConfigurationData userConfigurationData;
            if(experimentId == null){
                throw new RegistryException("Does not have the experiment id");
            }
            userConfigurationData = em.find(UserConfigurationData.class, experimentId);
            if(userConfigurationData == null){
                userConfigurationData = new UserConfigurationData();
            }
            userConfigurationData.setExperimentId(experimentId);
            userConfigurationData.setAiravataAutoSchedule(airavataAutoSchedule);
            userConfigurationData.setOverrideManualScheduledParams(overrideManualScheduledParams);
            userConfigurationData.setShareExperimentPublically(shareExperimentPublically);
            userConfigurationData.setThrottleResources(throttleResources);
            userConfigurationData.setUserDn(userDn);
            userConfigurationData.setGenerateCert(generateCert);
            userConfigurationData.setResourceHostId(resourceHostId);
            userConfigurationData.setTotalCpuCount(totalCpuCount);
            userConfigurationData.setNodeCount(nodeCount);
            userConfigurationData.setNumberOfThreads(numberOfThreads);
            userConfigurationData.setQueueName(queueName);
            userConfigurationData.setWallTimeLimit(wallTimeLimit);
            userConfigurationData.setTotalPhysicalMemory(totalPhysicalMemory);
            em.getTransaction().begin();
            em.persist(userConfigurationData);
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
