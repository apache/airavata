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

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
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

import java.sql.Timestamp;
import java.util.*;

public class TaskRepository extends ExpCatAbstractRepository<TaskModel, TaskEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(TaskRepository.class);

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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        TaskEntity taskEntity = mapper.map(taskModel, TaskEntity.class);

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
            taskEntity.getJobs().forEach(jobEntity -> jobEntity.setTaskId(taskId));
        }

        if (!isTaskExist(taskId)) {
            logger.debug("Checking if the Task already exists");
            taskEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        taskEntity.setLastUpdateTime(new Timestamp((System.currentTimeMillis())));
        return execute(entityManager -> entityManager.merge(taskEntity));
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

    public String addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();

        if (taskStatusList.size() == 0 || !taskStatusList.contains(taskStatus)) {

            if (taskStatus.getStatusId() == null) {
                logger.debug("Set TaskStatus's StatusId");
                taskStatus.setStatusId(ExpCatalogUtils.getID("STATUS"));
            }

            logger.debug("Adding the TaskStatus to the list");
            taskStatusList.add(taskStatus);
        }

        taskModel.setTaskStatuses(taskStatusList);
        updateTask(taskModel, taskId);
        return taskStatus.getStatusId();
    }

    public String updateTaskStatus(TaskStatus updatedTaskStatus, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();

        for (TaskStatus retrievedTaskStatus : taskStatusList) {

            if (retrievedTaskStatus.getStatusId().equals(updatedTaskStatus.getStatusId())) {
                logger.debug("Updating the TaskStatus");
                taskStatusList.remove(retrievedTaskStatus);
                taskStatusList.add(updatedTaskStatus);
            }

        }

        taskModel.setTaskStatuses(taskStatusList);
        updateTask(taskModel, taskId);
        return updatedTaskStatus.getStatusId();
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<TaskStatus> taskStatusList = taskModel.getTaskStatuses();

        if(taskStatusList.size() == 0) {
            logger.debug("TaskStatus list is empty");
            return null;
        }

        else {
            TaskStatus latestTaskStatus = taskStatusList.get(0);

            for(int i = 1; i < taskStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(taskStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange.after(new Timestamp(latestTaskStatus.getTimeOfStateChange()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.COMPLETED.toString()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.FAILED.toString()))
                        || (timeOfStateChange.equals(latestTaskStatus.getTimeOfStateChange()) && taskStatusList.get(i).getState().equals(TaskState.CANCELED.toString()))) {
                    latestTaskStatus = taskStatusList.get(i);
                }

            }

            return latestTaskStatus;
        }
    }

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        List<ErrorModel> errorModelList = taskModel.getTaskErrors();

        if (errorModelList == null) {
            logger.debug("Adding the first TaskError to the list");
            taskModel.setTaskErrors(Arrays.asList(taskError));
        }

        else if (!errorModelList.contains(taskError)) {
            logger.debug("Adding the TaskError to the list");
            errorModelList.add(taskError);
            taskModel.setTaskErrors(errorModelList);
        }

        updateTask(taskModel, taskId);
        return taskId;
    }

    public String updateTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        return addTaskError(taskError, taskId);
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskModel taskModel = getTask(taskId);
        return taskModel.getTaskErrors();
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
