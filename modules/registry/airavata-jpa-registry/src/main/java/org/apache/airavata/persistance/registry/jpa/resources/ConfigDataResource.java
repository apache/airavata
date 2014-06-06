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
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.ExperimentConfigData;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ConfigDataResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDataResource.class);
    private ExperimentResource experimentResource;
    private boolean airavataAutoSchedule;
    private boolean overrideManualParams;
    private boolean shareExp;

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public boolean isAiravataAutoSchedule() {
        return airavataAutoSchedule;
    }

    public void setAiravataAutoSchedule(boolean airavataAutoSchedule) {
        this.airavataAutoSchedule = airavataAutoSchedule;
    }

    public boolean isOverrideManualParams() {
        return overrideManualParams;
    }

    public void setOverrideManualParams(boolean overrideManualParams) {
        this.overrideManualParams = overrideManualParams;
    }

    public boolean isShareExp() {
        return shareExp;
    }

    public void setShareExp(boolean shareExp) {
        this.shareExp = shareExp;
    }

    
    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            ExperimentConfigData existingConfig = em.find(ExperimentConfigData.class, experimentResource.getExpID());
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ExperimentConfigData configData = new ExperimentConfigData();
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            configData.setExpId(experimentResource.getExpID());
            configData.setExperiment(experiment);
            configData.setAiravataAutoSchedule(airavataAutoSchedule);
            configData.setOverrideManualParams(overrideManualParams);
            configData.setShareExp(shareExp);
            if (existingConfig != null) {
                existingConfig.setExpId(experimentResource.getExpID());
                existingConfig.setExperiment(experiment);
                existingConfig.setAiravataAutoSchedule(airavataAutoSchedule);
                existingConfig.setOverrideManualParams(overrideManualParams);
                existingConfig.setShareExp(shareExp);
                configData = em.merge(existingConfig);
            } else {
                em.persist(configData);
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
}
