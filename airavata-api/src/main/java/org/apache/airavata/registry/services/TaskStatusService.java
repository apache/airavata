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
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.entities.expcatalog.TaskStatusEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.TaskStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private Mapper mapper;

    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId() == null) {
            taskStatus.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
        }
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        TaskStatusEntity entity = mapper.map(taskStatus, TaskStatusEntity.class);
        entity.setTaskId(taskId);
        taskStatusRepository.save(entity);
    }

    public void updateTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException {
        if (taskStatus.getStatusId() == null) {
            taskStatus.setStatusId(ExpCatalogUtils.getID("TASK_STATE"));
        }
        if (taskStatus.getTimeOfStateChange() == 0) {
            taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        }
        TaskStatusEntity entity = mapper.map(taskStatus, TaskStatusEntity.class);
        entity.setTaskId(taskId);
        taskStatusRepository.save(entity);
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        List<TaskStatusEntity> entities = taskStatusRepository.findByTaskIdOrderByTimeOfStateChangeDesc(taskId);
        if (entities.isEmpty()) return null;
        return mapper.map(entities.get(0), TaskStatus.class);
    }
}
