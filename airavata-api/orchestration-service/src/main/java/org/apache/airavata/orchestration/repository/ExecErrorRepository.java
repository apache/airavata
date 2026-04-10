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

import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ExecErrorEntity;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecErrorRepository extends AbstractRepository<ExecErrorEntity, ExecErrorEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ExecErrorRepository.class);

    public ExecErrorRepository() {
        super(ExecErrorEntity.class, ExecErrorEntity.class);
    }

    @Override
    protected ExecErrorEntity toModel(ExecErrorEntity entity) {
        return entity;
    }

    @Override
    protected ExecErrorEntity toEntity(ExecErrorEntity model) {
        return model;
    }

    // --- Process Error methods ---

    public String addProcessError(ErrorModel processError, String processId) throws RegistryException {
        if (processError.getErrorId().isEmpty()) {
            processError = processError.toBuilder()
                    .setErrorId(ExpCatalogUtils.getID("ERROR"))
                    .build();
        }
        return saveProcessError(processError, processId);
    }

    public String updateProcessError(ErrorModel processError, String processId) throws RegistryException {
        return saveProcessError(processError, processId);
    }

    protected String saveProcessError(ErrorModel error, String processId) throws RegistryException {
        ExecErrorEntity entity = ExecutionMapper.INSTANCE.processErrorToEntity(error);
        entity.setEntityType("PROCESS");
        entity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        org.apache.airavata.orchestration.model.ProcessEntity processEntity =
                execute(em -> em.find(org.apache.airavata.orchestration.model.ProcessEntity.class, processId));
        if (processEntity != null) {
            if (processEntity.getProcessErrors() == null) {
                processEntity.setProcessErrors(new java.util.ArrayList<>());
            }
            boolean found = false;
            if (entity.getErrorId() != null) {
                for (ExecErrorEntity ee : processEntity.getProcessErrors()) {
                    if (entity.getErrorId().equals(ee.getErrorId())) {
                        ee.setActualErrorMessage(entity.getActualErrorMessage());
                        ee.setUserFriendlyMessage(entity.getUserFriendlyMessage());
                        ee.setTransientOrPersistent(entity.isTransientOrPersistent());
                        ee.setRootCauseErrorIdList(entity.getRootCauseErrorIdList());
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                processEntity.getProcessErrors().add(entity);
            }
            execute(em -> em.merge(processEntity));
        }
        return entity.getErrorId();
    }

    public List<ErrorModel> getProcessError(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.getProcess(processId).getProcessErrorsList();
    }

    // --- Task Error methods ---

    public String addTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        if (taskError.getErrorId().isEmpty()) {
            taskError = taskError.toBuilder()
                    .setErrorId(ExpCatalogUtils.getID("ERROR"))
                    .build();
        }
        return saveTaskError(taskError, taskId);
    }

    public String updateTaskError(ErrorModel taskError, String taskId) throws RegistryException {
        return saveTaskError(taskError, taskId);
    }

    protected String saveTaskError(ErrorModel error, String taskId) throws RegistryException {
        ExecErrorEntity entity = ExecutionMapper.INSTANCE.taskErrorToEntity(error);
        entity.setEntityType("TASK");
        entity.setCreationTime(AiravataUtils.getCurrentTimestamp());
        org.apache.airavata.orchestration.model.TaskEntity taskEntity =
                execute(em -> em.find(org.apache.airavata.orchestration.model.TaskEntity.class, taskId));
        if (taskEntity != null) {
            if (taskEntity.getTaskErrors() == null) {
                taskEntity.setTaskErrors(new java.util.ArrayList<>());
            }
            boolean found = false;
            if (entity.getErrorId() != null) {
                for (ExecErrorEntity ee : taskEntity.getTaskErrors()) {
                    if (entity.getErrorId().equals(ee.getErrorId())) {
                        ee.setActualErrorMessage(entity.getActualErrorMessage());
                        ee.setUserFriendlyMessage(entity.getUserFriendlyMessage());
                        ee.setTransientOrPersistent(entity.isTransientOrPersistent());
                        ee.setRootCauseErrorIdList(entity.getRootCauseErrorIdList());
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                taskEntity.getTaskErrors().add(entity);
            }
            execute(em -> em.merge(taskEntity));
        }
        return entity.getErrorId();
    }

    public List<ErrorModel> getTaskError(String taskId) throws RegistryException {
        TaskRepository taskRepository = new TaskRepository();
        return taskRepository.getTask(taskId).getTaskErrorsList();
    }
}
