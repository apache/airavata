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
package org.apache.airavata.orchestration.repository;

import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.TaskStatusEntity;
import org.apache.airavata.orchestration.model.TaskStatusPK;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusRepository extends AbstractRepository<TaskStatus, TaskStatusEntity, TaskStatusPK> {
    private static final Logger logger = LoggerFactory.getLogger(TaskStatusRepository.class);

    public TaskStatusRepository() {
        super(TaskStatus.class, TaskStatusEntity.class);
    }

    @Override
    protected TaskStatus toModel(TaskStatusEntity entity) {
        return ExecutionMapper.INSTANCE.taskStatusToModel(entity);
    }

    @Override
    protected TaskStatusEntity toEntity(TaskStatus model) {
        return ExecutionMapper.INSTANCE.taskStatusToEntity(model);
    }

    protected String saveTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        TaskStatusEntity taskStatusEntity = ExecutionMapper.INSTANCE.taskStatusToEntity(taskStatus);

        if (taskStatusEntity.getTaskId() == null) {
            logger.debug("Setting the TaskStatusEntity's TaskId");
            taskStatusEntity.setTaskId(taskId);
        }

        execute(entityManager -> entityManager.merge(taskStatusEntity));
        return taskStatusEntity.getStatusId();
    }

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {

        if (taskStatus.getStatusId().isEmpty()) {
            logger.debug("Setting the TaskStatus's StatusId");
            taskStatus = taskStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("TASK_STATE"))
                    .build();
        }

        return saveTaskStatus(taskStatus, taskId);
    }

    public String updateTaskStatus(TaskStatus updatedTaskStatus, String taskId) throws RegistryException {
        return saveTaskStatus(updatedTaskStatus, taskId);
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        TaskModel taskModel = taskRepository.getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatusesList();

        if (taskStatusList.size() == 0) {
            logger.debug("TaskStatus list is empty");
            return null;
        } else {
            TaskStatus latestTaskStatus = taskStatusList.get(0);

            for (int i = 1; i < taskStatusList.size(); i++) {
                Timestamp timeOfStateChange =
                        new Timestamp(taskStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange.after(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                        || (timeOfStateChange.equals(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                                && taskStatusList.get(i).getState() == TaskState.TASK_STATE_COMPLETED)
                        || (timeOfStateChange.equals(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                                && taskStatusList.get(i).getState() == TaskState.TASK_STATE_FAILED)
                        || (timeOfStateChange.equals(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                                && taskStatusList.get(i).getState() == TaskState.TASK_STATE_CANCELED)) {
                    latestTaskStatus = taskStatusList.get(i);
                }
            }

            return latestTaskStatus;
        }
    }
}
