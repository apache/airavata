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
package org.apache.airavata.execution.repository;

import java.util.List;
import org.apache.airavata.execution.mapper.ExecutionMapper;
import org.apache.airavata.execution.model.TaskErrorEntity;
import org.apache.airavata.execution.model.TaskErrorPK;
import org.apache.airavata.execution.util.AbstractRepository;
import org.apache.airavata.execution.util.ExpCatalogUtils;
import org.apache.airavata.execution.util.cpi.RegistryException;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.task.TaskModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskErrorRepository extends AbstractRepository<ErrorModel, TaskErrorEntity, TaskErrorPK> {
    private static final Logger logger = LoggerFactory.getLogger(TaskErrorRepository.class);

    public TaskErrorRepository() {
        super(ErrorModel.class, TaskErrorEntity.class);
    }

    @Override
    protected ErrorModel toModel(TaskErrorEntity entity) {
        return ExecutionMapper.INSTANCE.taskErrorToModel(entity);
    }

    @Override
    protected TaskErrorEntity toEntity(ErrorModel model) {
        return ExecutionMapper.INSTANCE.taskErrorToEntity(model);
    }

    protected String saveTaskError(ErrorModel error, String taskId) throws RegistryException {
        TaskErrorEntity taskErrorEntity = ExecutionMapper.INSTANCE.taskErrorToEntity(error);

        if (taskErrorEntity.getTaskId() == null) {
            logger.debug("Setting the TaskErrorEntity's TaskId");
            taskErrorEntity.setTaskId(taskId);
        }

        execute(entityManager -> entityManager.merge(taskErrorEntity));
        return taskErrorEntity.getErrorId();
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {

        if (taskError.getErrorId() == null) {
            logger.debug("Setting the TaskError's ErrorId");
            taskError.setErrorId(ExpCatalogUtils.getID("ERROR"));
        }

        return saveTaskError(taskError, taskId);
    }

    public String updateTaskError(ErrorModel updatedTaskError, String taskId) throws RegistryException {
        return saveTaskError(updatedTaskError, taskId);
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        return taskModel.getTaskErrors();
    }
}
