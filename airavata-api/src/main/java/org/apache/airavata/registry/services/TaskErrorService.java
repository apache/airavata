/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.services;

import com.github.dozermapper.core.Mapper;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity;
import org.apache.airavata.registry.entities.expcatalog.TaskErrorPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.TaskErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskErrorService extends BaseErrorService<TaskErrorEntity, TaskErrorRepository, TaskErrorPK> {

    public TaskErrorService(TaskErrorRepository taskErrorRepository, Mapper mapper) {
        super(taskErrorRepository, mapper);
    }

    @Override
    protected BiConsumer<TaskErrorEntity, String> getParentIdSetter() {
        return TaskErrorEntity::setTaskId;
    }

    @Override
    protected Function<String, java.util.List<TaskErrorEntity>> getFindByParentIdFunction() {
        return repository::findByTaskId;
    }

    @Override
    protected Class<TaskErrorEntity> getEntityClass() {
        return TaskErrorEntity.class;
    }

    @Override
    protected Function<TaskErrorEntity, String> getErrorIdExtractor() {
        return TaskErrorEntity::getErrorId;
    }

    /**
     * Add a task error.
     *
     * @param error The error model to persist
     * @param taskId The ID of the task
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addTaskError(ErrorModel error, String taskId) throws RegistryException {
        return addError(error, taskId);
    }

    /**
     * Update a task error.
     *
     * @param error The error model with updated information
     * @param taskId The ID of the task
     * @throws RegistryException if the operation fails
     */
    public void updateTaskError(ErrorModel error, String taskId) throws RegistryException {
        updateError(error, taskId);
    }

    /**
     * Retrieve all errors for a task.
     *
     * @param taskId The ID of the task
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    public java.util.List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        return getErrors(taskId);
    }
}
