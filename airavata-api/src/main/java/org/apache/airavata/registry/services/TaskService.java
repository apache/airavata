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
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.entities.expcatalog.TaskEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.TaskRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JobService jobService;

    public void populateParentIds(TaskEntity taskEntity) {
        String taskId = taskEntity.getTaskId();

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating the Primary Key of TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(taskStatusEntity -> {
                taskStatusEntity.setTaskId(taskId);
                taskStatusEntity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
            });
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating the Primary Key of TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(taskErrorEntity -> {
                taskErrorEntity.setTaskId(taskId);
                taskErrorEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
            });
        }

        if (taskEntity.getJobs() != null) {
            logger.debug("Populating the Job objects' Task ID for the Task");
            taskEntity.getJobs().forEach(jobEntity -> {
                jobEntity.setTaskId(taskId);
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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, TaskModel.class);
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        List<TaskModel> taskModelList;

        if (fieldName.equals(DBConstants.Task.PARENT_PROCESS_ID)) {
            logger.debug("Search criteria is ParentProcessId");
            List<TaskEntity> entities = taskRepository.findByParentProcessId((String) value);
            Mapper mapper = ObjectMapperSingleton.getInstance();
            taskModelList = new ArrayList<>();
            entities.forEach(e -> taskModelList.add(mapper.map(e, TaskModel.class)));
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
        if (taskModel.getTaskId() == null || taskModel.getTaskId().equals(airavata_commonsConstants.DEFAULT_ID)) {
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
            taskModel.setCreationTime(System.currentTimeMillis());
        }

        taskModel.setLastUpdateTime(System.currentTimeMillis());

        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskEntity taskEntity = mapper.map(taskModel, TaskEntity.class);

        populateParentIds(taskEntity);

        return taskRepository.save(taskEntity);
    }
}
