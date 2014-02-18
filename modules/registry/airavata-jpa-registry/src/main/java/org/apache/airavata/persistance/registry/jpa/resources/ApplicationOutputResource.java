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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationOutputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationOutputResource.class);

	private String taskId;
    private String outputKey;
    private String outputType;
    private String metadata;
    private String value;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
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

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for application output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        ApplicationOutput existingOutput = em.find(ApplicationOutput.class, new ApplicationOutput_PK(outputKey, taskId));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        ApplicationOutput applicationOutput = new ApplicationOutput();
        TaskDetail taskDetail = em.find(TaskDetail.class, taskId);
        applicationOutput.setTask(taskDetail);
        applicationOutput.setTaskId(taskDetail.getTaskId());
        applicationOutput.setOutputKey(outputKey);
        applicationOutput.setOutputKeyType(outputType);
        applicationOutput.setValue(value);
        applicationOutput.setMetadata(metadata);
        
        if (existingOutput != null){
        	existingOutput.setTask(taskDetail);
        	existingOutput.setTaskId(taskDetail.getTaskId());
        	existingOutput.setOutputKey(outputKey);
        	existingOutput.setOutputKeyType(outputType);
        	existingOutput.setValue(value);
        	existingOutput.setMetadata(metadata);
        	applicationOutput = em.merge(existingOutput);
        }else {
            em.persist(applicationOutput);
        }
        em.getTransaction().commit();
        em.close();


    }
}
