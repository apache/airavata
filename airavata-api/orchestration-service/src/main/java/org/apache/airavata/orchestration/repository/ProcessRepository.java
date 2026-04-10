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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ProcessEntity;
import org.apache.airavata.util.AiravataUtils;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessRepository extends AbstractRepository<ProcessModel, ProcessEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessRepository.class);

    private final TaskRepository taskRepository = new TaskRepository();

    public ProcessRepository() {
        super(ProcessModel.class, ProcessEntity.class);
    }

    @Override
    protected ProcessModel toModel(ProcessEntity entity) {
        return ExecutionMapper.INSTANCE.processToModel(entity);
    }

    @Override
    protected ProcessEntity toEntity(ProcessModel model) {
        return ExecutionMapper.INSTANCE.processToEntity(model);
    }

    protected String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    protected ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        if (processModel.getProcessId().isEmpty() || processModel.getProcessId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Process's ProcessId");
            processModel = processModel.toBuilder()
                    .setProcessId(ExpCatalogUtils.getID("PROCESS"))
                    .build();
        }

        String processId = processModel.getProcessId();

        if (!processModel.getProcessStatusesList().isEmpty()) {
            logger.debug("Populating the status id of ProcessStatus objects for the Process");
            ProcessModel.Builder pmBuilder = processModel.toBuilder().clearProcessStatuses();
            for (ProcessStatus ps : processModel.getProcessStatusesList()) {
                if (ps.getStatusId().isEmpty()) {
                    ps = ps.toBuilder()
                            .setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"))
                            .build();
                }
                pmBuilder.addProcessStatuses(ps);
            }
            processModel = pmBuilder.build();
        }

        if (!isProcessExist(processId)) {
            logger.debug("Setting creation time if process doesn't already exist");
            processModel = processModel.toBuilder()
                    .setCreationTime(System.currentTimeMillis())
                    .build();
        }
        processModel = processModel.toBuilder()
                .setLastUpdateTime(System.currentTimeMillis())
                .build();
        ProcessEntity processEntity = ExecutionMapper.INSTANCE.processToEntity(processModel);

        populateParentIds(processEntity);

        return execute(entityManager -> {
            // Set process back-reference on child tasks
            if (processEntity.getTasks() != null) {
                processEntity.getTasks().forEach(taskEntity -> {
                    if (taskEntity.getProcess() == null) {
                        taskEntity.setProcess(processEntity);
                    }
                });
            }
            return entityManager.merge(processEntity);
        });
    }

    protected void populateParentIds(ProcessEntity processEntity) {
        String processId = processEntity.getProcessId();
        if (processEntity.getProcessResourceSchedule() != null) {
            logger.debug("Populating the Primary Key of ProcessResourceSchedule objects for the Process");
            processEntity.getProcessResourceSchedule().setProcessId(processId);
        }

        if (processEntity.getProcessInputs() != null) {
            logger.debug("Populating direction for ProcessInput objects for the Process");
            processEntity.getProcessInputs().forEach(e -> e.setDirection("INPUT"));
        }

        if (processEntity.getProcessOutputs() != null) {
            logger.debug("Populating direction for ProcessOutput objects for the Process");
            processEntity.getProcessOutputs().forEach(e -> e.setDirection("OUTPUT"));
        }

        if (processEntity.getProcessStatuses() != null) {
            logger.debug("Populating entityType for ProcessStatus objects for the Process");
            processEntity.getProcessStatuses().forEach(e -> {
                e.setEntityType("PROCESS");
                if (e.getTimeOfStateChange() == null) {
                    e.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
                }
            });
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating entityType for ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(e -> e.setEntityType("PROCESS"));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Primary Key of Task objects for the Process");
            processEntity.getTasks().forEach(taskEntity -> {
                taskEntity.setParentProcessId(processId);
                taskEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
                taskRepository.populateParentIds(taskEntity);
            });
        }
    }

    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        ProcessStatus processStatus = ProcessStatus.newBuilder()
                .setState(ProcessState.PROCESS_STATE_CREATED)
                .build();
        process = process.toBuilder()
                .setExperimentId(experimentId)
                .addProcessStatuses(processStatus)
                .build();
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

    public String addProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel = processModel.toBuilder()
                .setProcessResourceSchedule(computationalResourceSchedulingModel)
                .build();
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws RegistryException {
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
            processModelList =
                    processRepository.select(QueryConstants.GET_PROCESS_FOR_EXPERIMENT_ID, -1, 0, queryParameters);
        } else {
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

    public List<ProcessModel> getAllProcesses(int offset, int limit) {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.select(QueryConstants.GET_ALL_PROCESSES, limit, offset, new HashMap<>());
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchTime) {
        ProcessRepository processRepository = new ProcessRepository();
        Map<String, Double> timeDistributions = new HashMap<>();
        List<Object> orchTimeList = processRepository.selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_UPTO_METASCHEDULER_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        List<Object> queueingTimeList = processRepository.selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_QUEUED_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        List<Object> helixTimeList = processRepository.selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_HELIX_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        if (orchTimeList.size() > 0 && orchTimeList.get(0) != null) {
            timeDistributions.put(DBConstants.MetaData.ORCH_TIME, ((BigDecimal) orchTimeList.get(0)).doubleValue());
        }
        if (queueingTimeList.size() > 0 && queueingTimeList.get(0) != null) {
            timeDistributions.put(
                    DBConstants.MetaData.QUEUED_TIME, ((BigDecimal) queueingTimeList.get(0)).doubleValue());
        }
        if (helixTimeList.size() > 0 && helixTimeList.get(0) != null) {
            timeDistributions.put(DBConstants.MetaData.HELIX, ((BigDecimal) helixTimeList.get(0)).doubleValue());
        }
        return timeDistributions;
    }
}
