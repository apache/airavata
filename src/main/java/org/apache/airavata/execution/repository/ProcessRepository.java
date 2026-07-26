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
package org.apache.airavata.execution.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.models.experiment.UserConfigurationDataModel;
import org.apache.airavata.models.process.ProcessModel;
import org.apache.airavata.models.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.models.status.ProcessState;
import org.apache.airavata.models.status.ProcessStatus;
import org.apache.airavata.execution.mapper.ExecutionMapper;
import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.compute.model.ComputationalResourceSchedulingEntity;
import org.apache.airavata.compute.model.ProcessResourceScheduleEntity;
import org.apache.airavata.execution.model.ProcessEntity;
import org.apache.airavata.execution.model.UserConfigurationDataEntity;
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

    protected String saveProcessModelData(ProcessModel processModel) throws Exception {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    protected ProcessEntity saveProcess(ProcessModel processModel) throws Exception {
        if (processModel.processId().isEmpty() || processModel.processId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Process's ProcessId");
            processModel = processModel.toBuilder()
                    .setProcessId(AiravataUtils.getId("PROCESS"))
                    .build();
        }

        String processId = processModel.processId();

        if (!processModel.processStatuses().isEmpty()) {
            logger.debug("Populating the status id of ProcessStatus objects for the Process");
            ProcessModel.Builder pmBuilder = processModel.toBuilder().clearProcessStatuses();
            for (ProcessStatus ps : processModel.processStatuses()) {
                if (ps.statusId().isEmpty()) {
                    ps = ps.toBuilder()
                            .setStatusId(AiravataUtils.getId("PROCESS_STATE"))
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
            // PROCESS_RESOURCE_SCHEDULE is a shared-PK one-to-one whose PROCESS_ID FKs to
            // PROCESS.
            // Merging it in the same pass can insert the child before the parent PROCESS
            // row exists
            // (FK violation), since the owning join column is insertable=false. Persist the
            // process
            // and its one-to-many children first, then the schedule once the PROCESS row
            // exists.
            ProcessResourceScheduleEntity schedule = processEntity.getProcessResourceSchedule();
            processEntity.setProcessResourceSchedule(null);
            ProcessEntity saved = entityManager.merge(processEntity);
            if (schedule != null) {
                schedule.setProcessId(saved.getProcessId());
                saved.setProcessResourceSchedule(entityManager.merge(schedule));
            }
            return saved;
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

    public String addProcess(ProcessModel process, String experimentId) throws Exception {
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

    public void updateProcess(ProcessModel updatedProcess, String processId) throws Exception {
        saveProcessModelData(updatedProcess);
    }

    public ProcessModel getProcess(String processId) throws Exception {
        return get(processId);
    }

    public String addProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws Exception {
        ProcessModel processModel = getProcess(processId);
        processModel = processModel.toBuilder()
                .setProcessResourceSchedule(computationalResourceSchedulingModel)
                .build();
        updateProcess(processModel, processId);
        return processId;
    }

    public String updateProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws Exception {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws Exception {
        ProcessModel processModel = getProcess(processId);
        return processModel.processResourceSchedule();
    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) throws Exception {
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Process.EXPERIMENT_ID, value);
            processModelList = select(QueryConstants.GET_PROCESS_FOR_EXPERIMENT_ID, -1, 0,
                    queryParameters);
        } else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    public List<String> getProcessIds(String fieldName, Object value) throws Exception {
        List<String> processIds = new ArrayList<>();
        List<ProcessModel> processModelList = getProcessList(fieldName, value);
        for (ProcessModel processModel : processModelList) {
            processIds.add(processModel.processId());
        }
        return processIds;
    }

    public boolean isProcessExist(String processId) throws Exception {
        return isExists(processId);
    }

    public void removeProcess(String processId) throws Exception {
        delete(processId);
    }

    public List<ProcessModel> getAllProcesses(int offset, int limit) {
        return select(QueryConstants.GET_ALL_PROCESSES, limit, offset, new HashMap<>());
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchTime) {
        Map<String, Double> timeDistributions = new HashMap<>();
        List<Object> orchTimeList = selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_UPTO_METASCHEDULER_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        List<Object> queueingTimeList = selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_QUEUED_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        List<Object> submissionTimeList = selectWithNativeQuery(
                QueryConstants.FIND_AVG_TIME_SUBMISSION_NATIVE_QUERY, gatewayId, String.valueOf(searchTime));
        if (orchTimeList.size() > 0 && orchTimeList.get(0) != null) {
            timeDistributions.put(DBConstants.MetaData.ORCH_TIME, ((BigDecimal) orchTimeList.get(0)).doubleValue());
        }
        if (queueingTimeList.size() > 0 && queueingTimeList.get(0) != null) {
            timeDistributions.put(
                    DBConstants.MetaData.QUEUED_TIME, ((BigDecimal) queueingTimeList.get(0)).doubleValue());
        }
        if (submissionTimeList.size() > 0 && submissionTimeList.get(0) != null) {
            timeDistributions.put(
                    DBConstants.MetaData.SUBMISSION, ((BigDecimal) submissionTimeList.get(0)).doubleValue());
        }
        return timeDistributions;
    }

    public void saveUserConfigurationData(UserConfigurationDataModel ucdModel, String experimentId)
            throws Exception {
        UserConfigurationDataEntity ucdEntity = ExecutionMapper.INSTANCE.userConfigDataToEntity(ucdModel);
        ucdEntity.setExperimentId(experimentId);
        if (ucdEntity.getAutoScheduledCompResourceSchedulingList() != null) {
            logger.debug(
                    "Populating the Primary Key of UserConfigurationData.ComputationalResourceSchedulingEntities for Experiment");
            for (ComputationalResourceSchedulingEntity entity : ucdEntity
                    .getAutoScheduledCompResourceSchedulingList()) {
                entity.setExperimentId(experimentId);
            }
        }
        execute(entityManager -> entityManager.merge(ucdEntity));
    }

    // --- Processes for Experiment ---

    public List<ProcessModel> getProcessesForExperiment(String experimentId) throws Exception {
        return execute(entityManager -> {
            List<ProcessEntity> processEntities = entityManager
                    .createQuery("SELECT p FROM ProcessEntity p WHERE p.experimentId = :expId", ProcessEntity.class)
                    .setParameter("expId", experimentId)
                    .getResultList();
            List<ProcessModel> result = new java.util.ArrayList<>();
            for (ProcessEntity pe : processEntities) {
                result.add(ExecutionMapper.INSTANCE.processToModel(pe));
            }
            return result;
        });
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws Exception {
        return execute(entityManager -> {
            UserConfigurationDataEntity ucdEntity = entityManager.find(UserConfigurationDataEntity.class, experimentId);
            if (ucdEntity != null) {
                return ExecutionMapper.INSTANCE.userConfigDataToModel(ucdEntity);
            }
            return null;
        });
    }
}
