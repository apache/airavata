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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ProcessEntity;
import org.apache.airavata.orchestration.model.TaskEntity;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskRepository extends AbstractRepository<TaskModel, TaskEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    private final JobRepository jobRepository = new JobRepository();

    public TaskRepository() {
        super(TaskModel.class, TaskEntity.class);
    }

    @Override
    protected TaskModel toModel(TaskEntity entity) {
        return ExecutionMapper.INSTANCE.taskToModel(entity);
    }

    @Override
    protected TaskEntity toEntity(TaskModel model) {
        return ExecutionMapper.INSTANCE.taskToEntity(model);
    }

    protected String saveTaskModelData(TaskModel taskModel) throws RegistryException {
        TaskEntity taskEntity = saveTask(taskModel);
        return taskEntity.getTaskId();
    }

    protected TaskEntity saveTask(TaskModel taskModel) throws RegistryException {
        if (taskModel.getTaskId().isEmpty() || taskModel.getTaskId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Task's TaskId");
            taskModel = taskModel.toBuilder()
                    .setTaskId(ExpCatalogUtils.getID("TASK"))
                    .build();
        }

        String taskId = taskModel.getTaskId();

        if (!taskModel.getTaskStatusesList().isEmpty()) {
            logger.debug("Populating the status id of TaskStatus objects for the Task");
            TaskModel.Builder tmBuilder = taskModel.toBuilder().clearTaskStatuses();
            for (org.apache.airavata.model.status.proto.TaskStatus ts : taskModel.getTaskStatusesList()) {
                if (ts.getStatusId().isEmpty()) {
                    ts = ts.toBuilder()
                            .setStatusId(ExpCatalogUtils.getID("TASK_STATE"))
                            .build();
                }
                tmBuilder.addTaskStatuses(ts);
            }
            taskModel = tmBuilder.build();
        }

        if (!isTaskExist(taskId)) {
            logger.debug("Setting creation time if Task doesn't already exist");
            taskModel = taskModel.toBuilder()
                    .setCreationTime(System.currentTimeMillis())
                    .build();
        }

        taskModel = taskModel.toBuilder()
                .setLastUpdateTime(System.currentTimeMillis())
                .build();
        TaskEntity taskEntity = ExecutionMapper.INSTANCE.taskToEntity(taskModel);

        populateParentIds(taskEntity);

        return execute(entityManager -> {
            // Hibernate 6 requires @ManyToOne references to be set (not just the FK column)
            if (taskEntity.getProcess() == null && taskEntity.getParentProcessId() != null) {
                ProcessEntity processRef =
                        entityManager.getReference(ProcessEntity.class, taskEntity.getParentProcessId());
                taskEntity.setProcess(processRef);
            }
            return entityManager.merge(taskEntity);
        });
    }

    protected void populateParentIds(TaskEntity taskEntity) {

        String taskId = taskEntity.getTaskId();

        if (taskEntity.getTaskStatuses() != null) {
            logger.debug("Populating entityType for TaskStatus objects for the Task");
            taskEntity.getTaskStatuses().forEach(e -> {
                e.setEntityType("TASK");
                if (e.getTimeOfStateChange() == null) {
                    e.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
                }
            });
        }

        if (taskEntity.getTaskErrors() != null) {
            logger.debug("Populating entityType for TaskError objects for the Task");
            taskEntity.getTaskErrors().forEach(e -> {
                e.setEntityType("TASK");
                if (e.getCreationTime() == null) {
                    e.setCreationTime(AiravataUtils.getCurrentTimestamp());
                }
            });
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
        task = task.toBuilder().setParentProcessId(processId).build();
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
            taskModelList =
                    taskRepository.select(QueryConstants.GET_TASK_FOR_PARENT_PROCESS_ID, -1, 0, queryParameters);
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
        return isExists(taskId);
    }

    public void removeTask(String taskId) throws RegistryException {
        delete(taskId);
    }

    public void deleteTasks(String processId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Task.PARENT_PROCESS_ID, processId);
        List<TaskModel> taskModelList =
                taskRepository.select(QueryConstants.GET_TASK_FOR_PARENT_PROCESS_ID, -1, 0, queryParameters);
        for (TaskModel taskModel : taskModelList) {
            delete(taskModel.getTaskId());
        }
    }
}
