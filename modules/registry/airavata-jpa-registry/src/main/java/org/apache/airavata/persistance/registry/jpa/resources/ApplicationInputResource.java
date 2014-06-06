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
import org.apache.airavata.persistance.registry.jpa.model.ApplicationInput;
import org.apache.airavata.persistance.registry.jpa.model.ApplicationInput_PK;
import org.apache.airavata.persistance.registry.jpa.model.TaskDetail;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationInputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationInputResource.class);
    private TaskDetailResource taskDetailResource;
    private String inputKey;
    private String inputType;
    private String metadata;
    private String value;

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
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
        logger.error("Unsupported resource type for application input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for application input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for application input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for application input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            ApplicationInput existingInput = em.find(ApplicationInput.class, new ApplicationInput_PK(inputKey, taskDetailResource.getTaskId()));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            ApplicationInput applicationInput = new ApplicationInput();
            TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
            applicationInput.setTask(taskDetail);
            applicationInput.setTaskId(taskDetail.getTaskId());
            applicationInput.setInputKey(inputKey);
            applicationInput.setInputKeyType(inputType);
            applicationInput.setValue(value);
            applicationInput.setMetadata(metadata);

            if (existingInput != null) {
                existingInput.setTask(taskDetail);
                existingInput.setTaskId(taskDetail.getTaskId());
                existingInput.setInputKey(inputKey);
                existingInput.setInputKeyType(inputType);
                existingInput.setValue(value);
                existingInput.setMetadata(metadata);
                applicationInput = em.merge(existingInput);
            } else {
                em.persist(applicationInput);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
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
