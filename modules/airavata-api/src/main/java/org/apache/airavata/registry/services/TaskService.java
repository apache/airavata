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

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.entities.expcatalog.TaskEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.TaskModelMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessRepository;
import org.apache.airavata.registry.repositories.expcatalog.TaskRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProcessRepository processRepository;
    private final JobService jobService;
    private final TaskModelMapper taskModelMapper;

    public TaskService(
            TaskRepository taskRepository,
            ProcessRepository processRepository,
            JobService jobService,
            TaskModelMapper taskModelMapper) {
        this.taskRepository = taskRepository;
        this.processRepository = processRepository;
        this.jobService = jobService;
        this.taskModelMapper = taskModelMapper;
    }

    public void populateParentIds(TaskEntity taskEntity) {
        String taskId = taskEntity.getTaskId();

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating the Primary Key of TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(taskStatusEntity -> {
                taskStatusEntity.setTaskId(taskId);
                taskStatusEntity.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp());
            });
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating the Primary Key of TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(taskErrorEntity -> {
                taskErrorEntity.setTaskId(taskId);
                taskErrorEntity.setCreationTime(AiravataUtils.getUniqueTimestamp());
            });
        }

        if (taskEntity.getJobs() != null) {
            logger.debug("Populating the Job objects' Task ID for the Task");
            java.sql.Timestamp currentTimestamp = AiravataUtils.getUniqueTimestamp();
            taskEntity.getJobs().forEach(jobEntity -> {
                jobEntity.setTaskId(taskId);
                // Ensure CREATION_TIME is set if not already set
                if (jobEntity.getCreationTime() == null) {
                    jobEntity.setCreationTime(currentTimestamp);
                }
                jobService.populateParentIds(jobEntity);
            });
        }
    }

    public String addTask(TaskModel task, String processId) throws RegistryException {
        task.setParentProcessId(processId);
        return saveTaskModelData(task);
    }

    public String updateTask(TaskModel task, String taskId) throws RegistryException {
        return saveTaskModelData(task);
    }

    public TaskModel getTask(String taskId) throws RegistryException {
        TaskEntity entity = taskRepository.findById(taskId).orElse(null);
        if (entity == null) return null;
        TaskModel taskModel = taskModelMapper.toModel(entity);
        // Handle subTaskModel conversion: byte[] to Object
        if (entity.getSubTaskModel() != null) {
            taskModel.setSubTaskModel(entity.getSubTaskModel());
        }
        return taskModel;
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        List<TaskModel> taskModelList;

        if (fieldName.equals(DBConstants.Task.PARENT_PROCESS_ID)) {
            logger.debug("Search criteria is ParentProcessId");
            List<TaskEntity> entities = taskRepository.findByParentProcessId((String) value);
            taskModelList = taskModelMapper.toModelList(entities);
            // Handle subTaskModel conversion for each task
            for (int i = 0; i < taskModelList.size(); i++) {
                if (entities.get(i).getSubTaskModel() != null) {
                    taskModelList.get(i).setSubTaskModel(entities.get(i).getSubTaskModel());
                }
            }
        } else {
            logger.error("Unsupported field name for Task module.");
            throw new IllegalArgumentException("Unsupported field name for Task module.");
        }

        return taskModelList;
    }

    public List<String> getTaskIds(String fieldName, Object value) throws RegistryException {
        List<String> taskIds = new ArrayList<>();
        List<TaskModel> taskModelList = getTaskList(fieldName, value);
        for (TaskModel taskModel : taskModelList) {
            taskIds.add(taskModel.getTaskId());
        }
        return taskIds;
    }

    public boolean isTaskExist(String taskId) throws RegistryException {
        return taskRepository.existsById(taskId);
    }

    public void removeTask(String taskId) throws RegistryException {
        taskRepository.deleteById(taskId);
    }

    public void deleteTasks(String processId) throws RegistryException {
        List<TaskEntity> entities = taskRepository.findByParentProcessId(processId);
        for (TaskEntity entity : entities) {
            taskRepository.deleteById(entity.getTaskId());
        }
    }

    private String saveTaskModelData(TaskModel taskModel) throws RegistryException {
        TaskEntity taskEntity = saveTask(taskModel);
        return taskEntity.getTaskId();
    }

    private TaskEntity saveTask(TaskModel taskModel) throws RegistryException {
        if (taskModel.getTaskId() == null || taskModel.getTaskId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Task's TaskId");
            taskModel.setTaskId(ExpCatalogUtils.getID("TASK"));
        }

        String taskId = taskModel.getTaskId();

        if (taskModel.getTaskStatuses() != null) {
            logger.debug("Populating the status id of TaskStatus objects for the Task");
            taskModel.getTaskStatuses().forEach(taskStatusEntity -> {
                if (taskStatusEntity.getStatusId() == null) {
                    taskStatusEntity.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
                }
            });
        }

        if (!isTaskExist(taskId)) {
            logger.debug("Setting creation time if Task doesn't already exist");
            taskModel.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        } else if (taskModel.getCreationTime() == 0) {
            // If updating and creation time is not set, use current time
            taskModel.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        }

        // Always set last update time
        long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
        if (taskModel.getLastUpdateTime() == 0) {
            taskModel.setLastUpdateTime(currentTime);
        }

        TaskEntity taskEntity = taskModelMapper.toEntity(taskModel);

        // Handle subTaskModel conversion: Object (byte[]) to byte[]
        if (taskModel.getSubTaskModel() != null) {
            if (taskModel.getSubTaskModel() instanceof byte[]) {
                taskEntity.setSubTaskModel((byte[]) taskModel.getSubTaskModel());
            } else {
                logger.warn("SubTaskModel is not a byte array, skipping");
            }
        }

        // Ensure timestamps are set if mapper didn't set them (mapper returns null if model time is 0)
        if (taskEntity.getCreationTime() == null) {
            long creationTime = taskModel.getCreationTime() > 0 ? taskModel.getCreationTime() : currentTime;
            taskEntity.setCreationTime(new java.sql.Timestamp(creationTime));
        }
        if (taskEntity.getLastUpdateTime() == null) {
            long lastUpdateTime = taskModel.getLastUpdateTime() > 0 ? taskModel.getLastUpdateTime() : currentTime;
            taskEntity.setLastUpdateTime(new java.sql.Timestamp(lastUpdateTime));
        }

        // Set process relationship - required for inserts because the @JoinColumn is insertable (default)
        // while the parentProcessId @Column has insertable=false
        if (taskModel.getParentProcessId() != null) {
            ProcessEntity processEntity =
                    processRepository.findById(taskModel.getParentProcessId()).orElse(null);
            if (processEntity != null) {
                taskEntity.setProcess(processEntity);
            } else {
                // If process doesn't exist, create a reference with just the ID
                processEntity = new ProcessEntity();
                processEntity.setProcessId(taskModel.getParentProcessId());
                taskEntity.setProcess(processEntity);
            }
        }

        populateParentIds(taskEntity);

        return taskRepository.save(taskEntity);
    }
}
