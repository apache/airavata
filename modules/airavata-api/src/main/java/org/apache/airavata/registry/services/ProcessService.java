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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.airavata.registry.entities.expcatalog.ProcessWorkflowEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ProcessMapper;
import org.apache.airavata.registry.mappers.ProcessStatusMapper;
import org.apache.airavata.registry.mappers.ProcessWorkflowMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.repositories.expcatalog.ProcessRepository;
import org.apache.airavata.registry.repositories.expcatalog.ProcessStatusRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final ProcessRepository processRepository;
    private final ExperimentRepository experimentRepository;
    private final TaskService taskService;
    private final ProcessMapper processMapper;
    private final ProcessWorkflowMapper processWorkflowMapper;
    private final EntityManager entityManager;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessStatusMapper processStatusMapper;

    public ProcessService(
            ProcessRepository processRepository,
            ExperimentRepository experimentRepository,
            TaskService taskService,
            ProcessMapper processMapper,
            ProcessWorkflowMapper processWorkflowMapper,
            EntityManager entityManager,
            ProcessStatusRepository processStatusRepository,
            ProcessStatusMapper processStatusMapper) {
        this.processRepository = processRepository;
        this.experimentRepository = experimentRepository;
        this.taskService = taskService;
        this.processMapper = processMapper;
        this.processWorkflowMapper = processWorkflowMapper;
        this.entityManager = entityManager;
        this.processStatusRepository = processStatusRepository;
        this.processStatusMapper = processStatusMapper;
    }

    public void populateParentIds(ProcessEntity processEntity) {
        var processId = processEntity.getProcessId();
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
                processStatusEntity.setTimeOfStateChange(AiravataUtils.getUniqueTimestamp());
            });
        }

        if (processEntity.getProcessErrors() != null) {
            logger.debug("Populating the Primary Key of ProcessError objects for the Process");
            processEntity.getProcessErrors().forEach(processErrorEntity -> processErrorEntity.setProcessId(processId));
        }

        if (processEntity.getTasks() != null) {
            logger.debug("Populating the Primary Key of Task objects for the Process");
            var currentTimestamp = AiravataUtils.getCurrentTimestamp();
            processEntity.getTasks().forEach(taskEntity -> {
                // Set parentProcessId for consistency
                taskEntity.setParentProcessId(processId);
                // Set process relationship - required for inserts because the @JoinColumn is insertable (default)
                // while the parentProcessId @Column has insertable=false
                taskEntity.setProcess(processEntity);
                // Ensure required timestamps are set
                if (taskEntity.getCreationTime() == null) {
                    taskEntity.setCreationTime(currentTimestamp);
                }
                if (taskEntity.getLastUpdateTime() == null) {
                    taskEntity.setLastUpdateTime(currentTimestamp);
                }
                taskService.populateParentIds(taskEntity);
            });
        }
    }

    @Transactional
    public String addProcess(ProcessModel process, String experimentId) throws RegistryException {
        process.setExperimentId(experimentId);

        var processStatus = new ProcessStatus(ProcessState.CREATED);
        if (process.getProcessStatuses() == null) {
            process.setProcessStatuses(new ArrayList<>());
        }
        process.getProcessStatuses().add(processStatus);
        var processId = saveProcessModelData(process);
        return processId;
    }

    @Transactional
    public void updateProcess(ProcessModel updatedProcess, String processId) throws RegistryException {
        saveProcessModelData(updatedProcess);
    }

    @Transactional(readOnly = true)
    public ProcessModel getProcess(String processId) throws RegistryException {
        var entity = processRepository.findById(processId).orElse(null);
        if (entity == null) return null;
        // Force initialization of tasks collection to ensure all tasks are loaded
        if (entity.getTasks() != null) {
            entity.getTasks().size(); // Force initialization
        }
        var model = processMapper.toModel(entity);

        // Always load processStatuses from repository to ensure they're loaded
        // The entity's processStatuses collection might be lazy-loaded and appear non-empty
        // but may not contain actual data when accessed outside of transaction
        // Use ascending order so statuses are in chronological order (oldest first)
        try {
            var loadedStatuses = processStatusRepository.findByProcessIdOrderByTimeOfStateChangeAsc(processId);
            if (loadedStatuses != null && !loadedStatuses.isEmpty()) {
                // Convert to model list and set on the model
                var statusModels = new ArrayList<ProcessStatus>();
                for (var statusEntity : loadedStatuses) {
                    var statusModel = processStatusMapper.toModel(statusEntity);
                    statusModels.add(statusModel);
                }
                model.setProcessStatuses(statusModels);
            } else {
                model.setProcessStatuses(new ArrayList<>());
            }
        } catch (Exception e) {
            logger.debug("Could not load processStatuses for process {}: {}", processId, e.getMessage());
            model.setProcessStatuses(new ArrayList<>());
        }

        // Manually map processWorkflows after mapping to avoid LazyInitializationException
        // Access the collection to force initialization within the transaction
        try {
            Collection<ProcessWorkflowEntity> workflows = entity.getProcessWorkflows();
            if (workflows != null) {
                // Force initialization by accessing size
                int size = workflows.size();
                if (size > 0) {
                    model.setProcessWorkflows(processWorkflowMapper.toModelList(new ArrayList<>(workflows)));
                } else {
                    model.setProcessWorkflows(new ArrayList<>());
                }
            } else {
                model.setProcessWorkflows(new ArrayList<>());
            }
        } catch (LazyInitializationException e) {
            // If lazy initialization fails, just set empty list
            logger.debug("Could not initialize processWorkflows for process {}: {}", processId, e.getMessage());
            model.setProcessWorkflows(new ArrayList<>());
        }
        // Manually convert emailAddresses from String (CSV) to List<String>
        var emailAddressesStr = entity.getEmailAddresses();
        if (emailAddressesStr != null && !emailAddressesStr.isEmpty()) {
            model.setEmailAddresses(Arrays.asList(emailAddressesStr.split(",")));
        } else {
            model.setEmailAddresses(new ArrayList<>());
        }
        return model;
    }

    @Transactional
    public String addProcessResourceSchedule(
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel, String processId)
            throws RegistryException {
        var processModel = getProcess(processId);
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
        var processModel = getProcess(processId);
        return processModel.getProcessResourceSchedule();
    }

    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        List<ProcessModel> processModelList;

        if (fieldName.equals(DBConstants.Process.EXPERIMENT_ID)) {
            logger.debug("Search criteria is ExperimentId");
            var entities = processRepository.findByExperimentId((String) value);
            processModelList = processMapper.toModelList(entities);
            // Manually map processWorkflows after mapping to avoid LazyInitializationException
            for (int i = 0; i < processModelList.size(); i++) {
                var entity = entities.get(i);
                var model = processModelList.get(i);
                try {
                    Collection<ProcessWorkflowEntity> workflows = entity.getProcessWorkflows();
                    if (workflows != null) {
                        int size = workflows.size(); // Force initialization
                        if (size > 0) {
                            model.setProcessWorkflows(processWorkflowMapper.toModelList(new ArrayList<>(workflows)));
                        } else {
                            model.setProcessWorkflows(new ArrayList<>());
                        }
                    } else {
                        model.setProcessWorkflows(new ArrayList<>());
                    }
                } catch (LazyInitializationException e) {
                    logger.debug(
                            "Could not initialize processWorkflows for process {}: {}",
                            entity.getProcessId(),
                            e.getMessage());
                    model.setProcessWorkflows(new ArrayList<>());
                }
            }
            // Manually convert emailAddresses from String (CSV) to List<String> for each model
            for (int i = 0; i < processModelList.size(); i++) {
                var entity = entities.get(i);
                var model = processModelList.get(i);
                var emailAddressesStr = entity.getEmailAddresses();
                if (emailAddressesStr != null && !emailAddressesStr.isEmpty()) {
                    model.setEmailAddresses(Arrays.asList(emailAddressesStr.split(",")));
                } else {
                    model.setEmailAddresses(new ArrayList<>());
                }
            }
        } else {
            logger.error("Unsupported field name for Process module.");
            throw new IllegalArgumentException("Unsupported field name for Process module.");
        }

        return processModelList;
    }

    @Transactional(readOnly = true)
    public List<String> getProcessIds(String fieldName, Object value) throws RegistryException {
        var processIds = new ArrayList<String>();
        var processModelList = getProcessList(fieldName, value);
        for (var processModel : processModelList) {
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
        var entities = processRepository.findAll();
        var models = processMapper.toModelList(entities);
        // Manually map processWorkflows after mapping to avoid LazyInitializationException
        for (int i = 0; i < models.size(); i++) {
            var entity = entities.get(i);
            var model = models.get(i);
            try {
                Collection<ProcessWorkflowEntity> workflows = entity.getProcessWorkflows();
                if (workflows != null) {
                    int size = workflows.size(); // Force initialization
                    if (size > 0) {
                        model.setProcessWorkflows(processWorkflowMapper.toModelList(new ArrayList<>(workflows)));
                    } else {
                        model.setProcessWorkflows(new ArrayList<>());
                    }
                } else {
                    model.setProcessWorkflows(new ArrayList<>());
                }
            } catch (LazyInitializationException e) {
                logger.debug(
                        "Could not initialize processWorkflows for process {}: {}",
                        entity.getProcessId(),
                        e.getMessage());
                model.setProcessWorkflows(new ArrayList<>());
            }
        }
        return models;
    }

    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchTime) {
        // TODO: Migrate native queries to @Query annotations
        var timeDistributions = new HashMap<String, Double>();
        // These native queries need to be migrated to repository methods
        // For now, returning empty map - will be implemented when QueryConstants are migrated
        return timeDistributions;
    }

    private String saveProcessModelData(ProcessModel processModel) throws RegistryException {
        var processEntity = saveProcess(processModel);
        return processEntity.getProcessId();
    }

    private ProcessEntity saveProcess(ProcessModel processModel) throws RegistryException {
        if (processModel.getProcessId() == null
                || processModel.getProcessId().equals(AiravataCommonsConstants.DEFAULT_ID)) {
            logger.debug("Setting the Process's ProcessId");
            processModel.setProcessId(ExpCatalogUtils.getID("PROCESS"));
        }

        var processId = processModel.getProcessId();

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
            processModel.setCreationTime(AiravataUtils.getUniqueTimestamp().getTime());
        }
        processModel.setLastUpdateTime(AiravataUtils.getUniqueTimestamp().getTime());

        var processEntity = processMapper.toEntity(processModel);

        // Manually convert emailAddresses from List<String> to String (CSV) - excluded from mapping
        var emailAddressesList = processModel.getEmailAddresses();
        var emailAddressesStr = (emailAddressesList != null && !emailAddressesList.isEmpty())
                ? String.join(",", emailAddressesList)
                : null;
        processEntity.setEmailAddresses(emailAddressesStr);

        // Set experiment relationship - required for inserts because the @JoinColumn is insertable (default)
        // while the experimentId @Column has insertable=false
        if (processModel.getExperimentId() == null) {
            throw new RegistryException("Process must have an experimentId set");
        }
        // Try to find the experiment - first check persistence context, then database
        // This handles cases where the experiment is in the same transaction but not yet flushed
        var experimentEntity = entityManager.find(ExperimentEntity.class, processModel.getExperimentId());
        if (experimentEntity == null) {
            // Not in persistence context - flush to ensure any unflushed experiments are persisted
            entityManager.flush();
            // Query the database after flush
            experimentEntity = experimentRepository
                    .findById(processModel.getExperimentId())
                    .orElse(null);
            if (experimentEntity == null) {
                // Still not found - use getReferenceById to create a proxy
                // This will work if the experiment exists but wasn't found due to transaction isolation
                // If the experiment truly doesn't exist, the foreign key constraint will fail
                // which provides a clear error message
                try {
                    experimentEntity = experimentRepository.getReferenceById(processModel.getExperimentId());
                } catch (EntityNotFoundException e) {
                    throw new RegistryException(
                            "Experiment with id " + processModel.getExperimentId()
                                    + " does not exist. Ensure the experiment is created and saved before creating a process.",
                            e);
                }
            }
        }
        processEntity.setExperiment(experimentEntity);

        populateParentIds(processEntity);

        return processRepository.save(processEntity);
    }
}
