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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.AiravataCommonsConstants;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.repositories.expcatalog.ProcessRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final ProcessRepository processRepository;
    private final ExperimentRepository experimentRepository;
    private final TaskService taskService;
    private final Mapper mapper;

    public ProcessService(ProcessRepository processRepository, ExperimentRepository experimentRepository, TaskService taskService, Mapper mapper) {
        this.processRepository = processRepository;
        this.experimentRepository = experimentRepository;
        this.taskService = taskService;
        this.mapper = mapper;
    }

    public void populateParentIds(ProcessEntity processEntity) {
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
            processEntity
                    .getProcessOutputs()
                    .forEach(processOutputEntity -> processOutputEntity.setProcessId(processId));
        }

        if (processEntity.getProcessStatuses() != null) {
            logger.debug("Populating the Primary Key of ProcessStatus objects for the Process");
            processEntity.getProcessStatuses().forEach(processStatusEntity -> {
                processStatusEntity.setProcessId(processId);
                processStatusEntity.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp());
            });
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating the Primary Key of ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(processErrorEntity -> processErrorEntity.setProcessId(processId));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Primary Key of Task objects for the Process");
            processEntity.getTasks().forEach(taskEntity -> {
                // Set parentProcessId for consistency (even though it's insertable=false)
                taskEntity.setParentProcessId(processId);
                // Set process relationship to ensure PARENT_PROCESS_ID is inserted via @JoinColumn
                taskEntity.setProcess(processEntity);
                taskEntity.setCreationTime(AiravataUtils.getCurrentTimestamp());
                taskService.populateParentIds(taskEntity);
            });
        }
    }

    @Transactional
    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        process.setExperimentId(experimentId);

        ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
        if (process.getProcessStatuses() == null) {
            process.setProcessStatuses(new java.util.ArrayList<>());
        }
        process.getProcessStatuses().add(processStatus);
        String processId = saveProcessModelData(process);
        return processId;
    }

    @Transactional
    public void updateProcess(ProcessModel updatedProcess, String processId) throws RegistryException {
        saveProcessModelData(updatedProcess);
    }

    @Transactional(readOnly = true)
    public ProcessModel getProcess(String processId) throws RegistryException {
        ProcessEntity entity = processRepository.findById(processId).orElse(null);
        if (entity == null) return null;
        
        // Temporarily null emailAddresses to avoid Dozer mapping issues
        String emailAddressesStr = entity.getEmailAddresses();
        entity.setEmailAddresses(null);
        
        ProcessModel model = mapper.map(entity, ProcessModel.class);
        
        // Restore emailAddresses on entity
        entity.setEmailAddresses(emailAddressesStr);
        
        // Manually convert emailAddresses from String (CSV) to List<String>
        if (emailAddressesStr != null && !emailAddressesStr.isEmpty()) {
            model.setEmailAddresses(java.util.Arrays.asList(emailAddressesStr.split(",")));
        } else {
            model.setEmailAddresses(new java.util.ArrayList<>());
        }
        return model;
    }

    @Transactional
    public String addProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        updateProcess(processModel, processId);
        return processId;
    }

    @Transactional
    public String updateProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws RegistryException {
        return addProcessResourceSchedule(computationalResourceSchedulingModel, processId);
    }

    @Transactional(readOnly = true)
    public ComputationalResourceSchedulingModel getProcessResourceSchedule(String processId) throws RegistryException {
        ProcessModel processModel = getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            List<ProcessEntity> entities = processRepository.findByExperimentId((String) value);
            processModelList = new ArrayList<>();
            entities.forEach(e -> {
                // Temporarily null emailAddresses to avoid Dozer mapping issues
                String emailAddressesStr = e.getEmailAddresses();
                e.setEmailAddresses(null);
                ProcessModel model = mapper.map(e, ProcessModel.class);
                // Restore emailAddresses on entity
                e.setEmailAddresses(emailAddressesStr);
                // Manually convert emailAddresses from String (CSV) to List<String>
                if (emailAddressesStr != null && !emailAddressesStr.isEmpty()) {
                    model.setEmailAddresses(java.util.Arrays.asList(emailAddressesStr.split(",")));
                } else {
                    model.setEmailAddresses(new java.util.ArrayList<>());
                }
                processModelList.add(model);
            });
        } else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    @Transactional(readOnly = true)
    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        List<String> processIds = new ArrayList<>();
        List<ProcessModel> processModelList = getProcessList(fieldName, value);
        for (ProcessModel processModel : processModelList) {
            processIds.add(processModel.getProcessId());
        }
        return processIds;
    }

    @Transactional(readOnly = true)
    public boolean isProcessExist(String processId) throws RegistryException {
        return processRepository.existsById(processId);
    }

    @Transactional
    public void removeProcess(String processId) throws RegistryException {
        processRepository.deleteById(processId);
    }

    @Transactional(readOnly = true)
    public List<ProcessModel> getAllProcesses(int offset, int limit) {
        List<ProcessEntity> entities = processRepository.findAll();
        List<ProcessModel> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, ProcessModel.class)));
        return result;
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchTime) {
        // TODO: Migrate native queries to @Query annotations
        Map<String, Double> timeDistributions = new HashMap<>();
        // These native queries need to be migrated to repository methods
        // For now, returning empty map - will be implemented when QueryConstants are migrated
        return timeDistributions;
    }

    private String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        ProcessEntity processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    private ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        if (processModel.getProcessId() == null
                || processModel.getProcessId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
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

        ProcessEntity processEntity = mapper.map(processModel, ProcessEntity.class);
        
        // Manually convert emailAddresses from List<String> to String (CSV) - excluded from Dozer mapping
        java.util.List<String> emailAddressesList = processModel.getEmailAddresses();
        String emailAddressesStr = (emailAddressesList != null && !emailAddressesList.isEmpty()) 
                ? String.join(",", emailAddressesList) : null;
        processEntity.setEmailAddresses(emailAddressesStr);
        
        // Set experiment relationship to ensure EXPERIMENT_ID is set via @JoinColumn
        // (experimentId field is marked as insertable=false, updatable=false)
        if (processModel.getExperimentId() != null) {
            ExperimentEntity experimentEntity = experimentRepository.findById(processModel.getExperimentId()).orElse(null);
            if (experimentEntity != null) {
                processEntity.setExperiment(experimentEntity);
            } else {
                // If experiment doesn't exist, create a reference with just the ID
                experimentEntity = new ExperimentEntity();
                experimentEntity.setExperimentId(processModel.getExperimentId());
                processEntity.setExperiment(experimentEntity);
            }
            // Also set experimentId field directly for consistency
            processEntity.setExperimentId(processModel.getExperimentId());
        }

        populateParentIds(processEntity);

        return processRepository.save(processEntity);
    }
}
