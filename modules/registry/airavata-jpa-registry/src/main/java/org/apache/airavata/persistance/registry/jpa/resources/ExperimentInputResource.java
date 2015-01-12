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
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Input;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Input_PK;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ExperimentInputResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentInputResource.class);

    private ExperimentResource experimentResource;
    private String experimentKey;
    private String value;
    private String metadata;
    private String dataType;
    private String appArgument;
    private boolean standardInput;
    private String userFriendlyDesc;
    private int inputOrder;
    private boolean isRequired;
    private boolean requiredToCMD;
    private boolean dataStaged;

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public String getExperimentKey() {
        return experimentKey;
    }

    public void setExperimentKey(String experimentKey) {
        this.experimentKey = experimentKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Experiment_Input existingInput = em.find(Experiment_Input.class, new Experiment_Input_PK(experimentResource.getExpID(), experimentKey));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Experiment_Input exInput = new Experiment_Input();
            exInput.setEx_key(experimentKey);
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            exInput.setExperiment(experiment);
            exInput.setExperiment_id(experiment.getExpId());
            if (value != null){
                exInput.setValue(value.toCharArray());
            }
            exInput.setDataType(dataType);
            exInput.setMetadata(metadata);
            exInput.setAppArgument(appArgument);
            exInput.setStandardInput(standardInput);
            exInput.setUserFriendlyDesc(userFriendlyDesc);
            exInput.setInputOrder(inputOrder);
            exInput.setRequiredToCMD(requiredToCMD);
            exInput.setRequired(isRequired);
            exInput.setDataStaged(dataStaged);
            if (existingInput != null) {
                existingInput.setEx_key(experimentKey);
                existingInput.setExperiment(experiment);
                existingInput.setExperiment_id(experiment.getExpId());
                if (value != null){
                    existingInput.setValue(value.toCharArray());
                }
                existingInput.setDataType(dataType);
                existingInput.setMetadata(metadata);
                existingInput.setAppArgument(appArgument);
                existingInput.setStandardInput(standardInput);
                existingInput.setUserFriendlyDesc(userFriendlyDesc);
                existingInput.setInputOrder(inputOrder);
                existingInput.setRequiredToCMD(requiredToCMD);
                existingInput.setRequired(isRequired);
                existingInput.setDataStaged(dataStaged);
                exInput = em.merge(existingInput);
            } else {
                em.persist(exInput);
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
