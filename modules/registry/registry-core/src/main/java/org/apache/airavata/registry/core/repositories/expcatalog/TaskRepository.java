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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.entities.expcatalog.TaskEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository extends ExpCatAbstractRepository<TaskModel, TaskEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    private final JobRepository jobRepository = new JobRepository();

    public TaskRepository() { super(TaskModel.class, TaskEntity.class); }

    protected String saveTaskModelData(TaskModel taskModel) throws RegistryException {
        TaskEntity taskEntity = saveTask(taskModel);
        return taskEntity.getTaskId();
    }

    protected TaskEntity saveTask(TaskModel taskModel) throws RegistryException {
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

        return execute(entityManager -> entityManager.merge(taskEntity));
    }

    protected void populateParentIds(TaskEntity taskEntity) {

        String taskId = taskEntity.getTaskId();

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating the Primary Key of TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(taskStatusEntity -> taskStatusEntity.setTaskId(taskId));
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating the Primary Key of TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(taskErrorEntity -> taskErrorEntity.setTaskId(taskId));
        }

        if (taskEntity.getJobs() != null) {
            logger.debug("Populating the Job objects' Task ID for the Task");
            taskEntity.getJobs().forEach(jobEntity -> {
                jobEntity.setTaskId(taskId);
                jobRepository.populateParentIds(jobEntity);
            });
        }
    }

    public String addTask(TaskModel task, String processId) throws RegistryException {
        task.setParentProcessId(processId);
        String taskId = saveTaskModelData(task);
        return taskId;
    }

    public String updateTask(TaskModel task, String taskId) throws RegistryException {
        return saveTaskModelData(task);
    }

    public TaskModel getTask(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        return taskRepository.get(taskId);
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        List<TaskModel> taskModelList;

        if (fieldName.equals(DBConstants.Task.PARENT_PROCESS_ID)) {
            logger.debug("Search criteria is ParentProcessId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Task.PARENT_PROCESS_ID, value);
            taskModelList = taskRepository.select(QueryConstants.GET_TASK_FOR_PARENT_PROCESS_ID, -1, 0, queryParameters);
        }

        else {
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
        return isExists(taskId);
    }

    public void removeTask(String taskId) throws RegistryException {
        delete(taskId);
    }

}
