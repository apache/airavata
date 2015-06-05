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
import org.apache.airavata.registry.core.experiment.catalog.model.ExperimentConfigData;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ConfigDataResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDataResource.class);
    private String experimentId;
    private boolean airavataAutoSchedule;
    private boolean overrideManualParams;
    private boolean shareExp;
    private String userDn;
    private boolean generateCert;
    private ComputationSchedulingResource computationSchedulingResource;
    private AdvanceInputDataHandlingResource advanceInputDataHandlingResource;
    private AdvancedOutputDataHandlingResource advancedOutputDataHandlingResource;
    private QosParamResource qosParamResource;

    public ComputationSchedulingResource getComputationSchedulingResource() {
        return computationSchedulingResource;
    }

    public void setComputationSchedulingResource(ComputationSchedulingResource computationSchedulingResource) {
        this.computationSchedulingResource = computationSchedulingResource;
    }

    public AdvanceInputDataHandlingResource getAdvanceInputDataHandlingResource() {
        return advanceInputDataHandlingResource;
    }

    public void setAdvanceInputDataHandlingResource(AdvanceInputDataHandlingResource advanceInputDataHandlingResource) {
        this.advanceInputDataHandlingResource = advanceInputDataHandlingResource;
    }

    public AdvancedOutputDataHandlingResource getAdvancedOutputDataHandlingResource() {
        return advancedOutputDataHandlingResource;
    }

    public void setAdvancedOutputDataHandlingResource(AdvancedOutputDataHandlingResource advancedOutputDataHandlingResource) {
        this.advancedOutputDataHandlingResource = advancedOutputDataHandlingResource;
    }

    public QosParamResource getQosParamResource() {
        return qosParamResource;
    }

    public void setQosParamResource(QosParamResource qosParamResource) {
        this.qosParamResource = qosParamResource;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public boolean isGenerateCert() {
        return generateCert;
    }

    public void setGenerateCert(boolean generateCert) {
        this.generateCert = generateCert;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
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

    
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for config data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            ExperimentConfigData existingConfig = em.find(ExperimentConfigData.class, experimentId);
            em.close();

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ExperimentConfigData configData = new ExperimentConfigData();
            configData.setExpId(experimentId);
            configData.setAiravataAutoSchedule(airavataAutoSchedule);
            configData.setOverrideManualParams(overrideManualParams);
            configData.setShareExp(shareExp);
            configData.setUserDn(userDn);
            configData.setGenerateCert(generateCert);
            if (existingConfig != null) {
                existingConfig.setExpId(experimentId);
                existingConfig.setAiravataAutoSchedule(airavataAutoSchedule);
                existingConfig.setOverrideManualParams(overrideManualParams);
                existingConfig.setShareExp(shareExp);
                existingConfig.setUserDn(userDn);
                existingConfig.setGenerateCert(generateCert);
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
