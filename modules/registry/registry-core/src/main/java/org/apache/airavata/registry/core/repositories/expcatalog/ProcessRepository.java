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
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessEntity;
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

public class ProcessRepository extends ExpCatAbstractRepository<ProcessModel, ProcessEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessRepository.class);

    private final TaskRepository taskRepository = new TaskRepository();

    public ProcessRepository() { super(ProcessModel.class, ProcessEntity.class); }

    protected String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    protected ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        if (processModel.getProcessId() == null || processModel.getProcessId().equals(airavata_commonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Process's ProcessId");
            processModel.setProcessId(ExpCatalogUtils.getID("PROCESS"));
        }

        String processId = processModel.getProcessId();

        if (processModel.getProcessStatuses() != null) {
            logger.debug("Populating the status id of ProcessStatus objects for the Process");
            processModel.getProcessStatuses().forEach(processStatusEntity -> {
                if (processStatusEntity.getStatusId() == null) {
                    processStatusEntity.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
                }
            });
        }

        if (!isProcessExist(processId)) {
            logger.debug("Setting creation time if process doesn't already exist");
            processModel.setCreationTime(System.currentTimeMillis());
        }
        processModel.setLastUpdateTime(System.currentTimeMillis());

        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProcessEntity processEntity = mapper.map(processModel, ProcessEntity.class);

        populateParentIds(processEntity);

        return execute(entityManager -> entityManager.merge(processEntity));
    }

    protected void populateParentIds(ProcessEntity processEntity) {
        String processId = processEntity.getProcessId();
        if (processEntity.getProcessResourceSchedule() != null) {
            logger.debug("Populating the Primary Key of ProcessResourceSchedule objects for the Process");
            processEntity.getProcessResourceSchedule().setProcessId(processId);
        }

        if (processEntity.getProcessInputs() != null) {
            logger.debug("Populating the Primary Key of ProcessInput objects for the Process");
            processEntity.getProcessInputs().forEach(processInputEntity -> processInputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessOutputs() != null) {
            logger.debug("Populating the Primary Key of ProcessOutput objects for the Process");
            processEntity.getProcessOutputs().forEach(processOutputEntity -> processOutputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessStatuses() != null) {
            logger.debug("Populating the Primary Key of ProcessStatus objects for the Process");
            processEntity.getProcessStatuses().forEach(processStatusEntity -> processStatusEntity.setProcessId(processId));
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating the Primary Key of ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(processErrorEntity -> processErrorEntity.setProcessId(processId));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Primary Key of Task objects for the Process");
            processEntity.getTasks().forEach(taskEntity -> {
                taskEntity.setParentProcessId(processId);
                taskRepository.populateParentIds(taskEntity);
            });
        }
    }

    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        process.setExperimentId(experimentId);

        ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
        process.addToProcessStatuses(processStatus);
        String processId = saveProcessModelData(process);
        return processId;
    }

    public void updateProcess(ProcessModel updatedProcess, String processId) throws RegistryException {
        saveProcessModelData(updatedProcess);
    }

    public ProcessModel getProcess(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.get(processId);
    }

    public String addProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId) throws RegistryException {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Process.EXPERIMENT_ID, value);
            processModelList = processRepository.select(QueryConstants.GET_PROCESS_FOR_EXPERIMENT_ID, -1, 0, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        List<String> processIds = new ArrayList<>();
        List<ProcessModel> processModelList = getProcessList(fieldName, value);
        for (ProcessModel processModel : processModelList) {
            processIds.add(processModel.getProcessId());
        }
        return processIds;
    }

    public boolean isProcessExist(String processId) throws RegistryException {
        return isExists(processId);
    }

    public void removeProcess(String processId) throws RegistryException {
        delete(processId);
    }

}
