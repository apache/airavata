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

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.ApplicationOutput;
import org.apache.airavata.persistance.registry.jpa.model.ApplicationOutput_PK;
import org.apache.airavata.persistance.registry.jpa.model.TaskDetail;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationOutputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationOutputResource.class);
    private TaskDetailResource taskDetailResource;
    private String outputKey;
    private String dataType;
    private String value;
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

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
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

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
    }

    
    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            ApplicationOutput existingOutput = em.find(ApplicationOutput.class, new ApplicationOutput_PK(outputKey, taskDetailResource.getTaskId()));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationOutput applicationOutput = new ApplicationOutput();
            TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
            applicationOutput.setTask(taskDetail);
            applicationOutput.setTaskId(taskDetail.getTaskId());
            applicationOutput.setOutputKey(outputKey);
            applicationOutput.setDataType(dataType);
            applicationOutput.setRequired(isRequired);
            applicationOutput.setAddedToCmd(requiredToCMD);
            applicationOutput.setDataMovement(dataMovement);
            applicationOutput.setDataNameLocation(dataNameLocation);
            applicationOutput.setSearchQuery(searchQuery);
            applicationOutput.setApplicationArgument(appArgument);
            if (value != null){
                applicationOutput.setValue(value.toCharArray());
            }

            if (existingOutput != null) {
                existingOutput.setTask(taskDetail);
                existingOutput.setTaskId(taskDetail.getTaskId());
                existingOutput.setOutputKey(outputKey);
                existingOutput.setDataType(dataType);
                existingOutput.setRequired(isRequired);
                existingOutput.setAddedToCmd(requiredToCMD);
                existingOutput.setDataMovement(dataMovement);
                existingOutput.setDataNameLocation(dataNameLocation);
                existingOutput.setSearchQuery(searchQuery);
                existingOutput.setApplicationArgument(appArgument);
                if (value != null){
                    existingOutput.setValue(value.toCharArray());
                }
                applicationOutput = em.merge(existingOutput);
            } else {
                em.persist(applicationOutput);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e.getMessage());
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
