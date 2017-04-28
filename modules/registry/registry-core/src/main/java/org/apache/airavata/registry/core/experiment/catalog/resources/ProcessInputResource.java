/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.ProcessInput;
import org.apache.airavata.registry.core.experiment.catalog.model.ProcessInputPK;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ProcessInputResource extends AbstractExpCatResource {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInputResource.class);
    private String processId;
    private String inputName;
    private String inputValue;
    private String dataType;
    private String applicationArgument;
    private boolean standardInput = false;
    private String userFriendlyDescription;
    private String metadata;
    private Integer inputOrder;
    private boolean isRequired = false;
    private boolean requiredToAddedToCmd = false;
    private boolean dataStaged = false;
    private String storageResourceId;
    private boolean isReadOnly;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    public boolean getStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public String getUserFriendlyDescription() {
        return userFriendlyDescription;
    }

    public void setUserFriendlyDescription(String userFriendlyDescription) {
        this.userFriendlyDescription = userFriendlyDescription;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(Integer inputOrder) {
        this.inputOrder = inputOrder;
    }

    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean getRequiredToAddedToCmd() {
        return requiredToAddedToCmd;
    }

    public void setRequiredToAddedToCmd(boolean requiredToAddedToCmd) {
        this.requiredToAddedToCmd = requiredToAddedToCmd;
    }

    public boolean getDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for process input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for process input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for process input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for process input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            if(processId == null){
                throw new RegistryException("Does not have the process id");
            }
            em = ExpCatResourceUtils.getEntityManager();
            ProcessInput processInput;
            ProcessInputPK processInputPk = new ProcessInputPK();
            processInputPk.setProcessId(processId);
            processInputPk.setInputName(inputName);
            ProcessInput existingProInput = em.find(ProcessInput.class, processInputPk);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if(existingProInput == null){
                processInput = new ProcessInput();
            }else {
                processInput = existingProInput;
            }
            processInput.setProcessId(processId);
            processInput.setInputName(inputName);
            processInput.setInputValue(inputValue);
            processInput.setDataType(dataType);
            processInput.setApplicationArgument(applicationArgument);
            processInput.setStandardInput(standardInput);
            processInput.setUserFriendlyDescription(userFriendlyDescription);
            processInput.setMetadata(metadata);
            processInput.setInputOrder(inputOrder);
            processInput.setIsRequired(isRequired);
            processInput.setRequiredToAddedToCmd(requiredToAddedToCmd);
            processInput.setDataStaged(dataStaged);
            processInput.setStorageResourceId(storageResourceId);
            processInput.setIsReadOnly(isReadOnly);
            if (existingProInput == null){
                em.persist(processInput);
            }else {
                em.merge(processInput);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
