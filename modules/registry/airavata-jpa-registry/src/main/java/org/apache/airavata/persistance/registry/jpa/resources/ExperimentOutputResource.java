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
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Output;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Output_PK;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ExperimentOutputResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentOutputResource.class);

    private ExperimentResource experimentResource;
    private String experimentKey;
    private String value;
    private String dataType;
    private boolean isRequired;
    private boolean dataMovement;
    private String dataNameLocation;
    private boolean requiredToCMD;
    private String searchQuery;
    private String appArgument;

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }


    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
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

    public Resource create(ResourceType type)  throws RegistryException {
        logger.error("Unsupported resource type for experiment output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name)  throws RegistryException{
        logger.error("Unsupported resource type for experiment output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name)  throws RegistryException{
        logger.error("Unsupported resource type for experiment output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Experiment_Output existingOutput = em.find(Experiment_Output.class, new Experiment_Output_PK(experimentResource.getExpID(), experimentKey));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Experiment_Output exOutput = new Experiment_Output();
            exOutput.setEx_key(experimentKey);
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            exOutput.setExperiment(experiment);
            exOutput.setExperiment_id(experiment.getExpId());
            if (value != null){
                exOutput.setValue(value.toCharArray());
            }
            exOutput.setDataType(dataType);
            exOutput.setRequired(isRequired);
            exOutput.setRequiredToCMD(requiredToCMD);
            exOutput.setDataMovement(dataMovement);
            exOutput.setDataNameLocation(dataNameLocation);
            exOutput.setApplicationArgument(appArgument);
            exOutput.setSearchQuery(searchQuery);

            if (existingOutput != null) {
                existingOutput.setEx_key(experimentKey);
                existingOutput.setExperiment(experiment);
                if (value != null){
                    existingOutput.setValue(value.toCharArray());
                }
                existingOutput.setExperiment_id(experiment.getExpId());
                existingOutput.setDataType(dataType);
                existingOutput.setRequired(isRequired);
                existingOutput.setRequiredToCMD(requiredToCMD);
                existingOutput.setDataMovement(dataMovement);
                existingOutput.setDataNameLocation(dataNameLocation);
                existingOutput.setApplicationArgument(appArgument);
                existingOutput.setSearchQuery(searchQuery);
                exOutput = em.merge(existingOutput);
            } else {
                em.persist(exOutput);
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
