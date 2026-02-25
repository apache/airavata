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
package org.apache.airavata.execution.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.util.DBConstants;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.entity.ProcessEntity;
import org.apache.airavata.execution.mapper.ProcessMapper;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.execution.repository.ProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for CRUD operations on process records.
 *
 * <p>A process represents a single execution attempt of an experiment on a compute resource.
 * Each experiment may have one or more processes corresponding to distinct job submission cycles.
 */
@Service
@Transactional
public class DefaultProcessService implements ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final ProcessRepository processRepository;
    private final ProcessMapper mapper;

    public DefaultProcessService(ProcessRepository processRepository, ProcessMapper mapper) {
        this.processRepository = processRepository;
        this.mapper = mapper;
    }

    /**
     * Persist a new process record and return its generated ID.
     *
     * @param processModel the process to save
     * @param experimentId the parent experiment identifier
     * @return the new process ID
     * @throws RegistryException on persistence failure
     */
    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        try {
            processModel.setProcessId(IdGenerator.ensureId(processModel.getProcessId()));
            processModel.setExperimentId(experimentId);
            ProcessEntity entity = mapper.toEntity(processModel);
            processRepository.save(entity);
            logger.debug("Saved process {} for experiment {}", processModel.getProcessId(), experimentId);
            return processModel.getProcessId();
        } catch (Exception e) {
            throw new RegistryException("Failed to add process for experiment " + experimentId, e);
        }
    }

    /**
     * Load a process by its ID.
     *
     * @param processId the process identifier
     * @return the process model, or null if not found
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public ProcessModel getProcess(String processId) throws RegistryException {
        try {
            Optional<ProcessEntity> entity = processRepository.findById(processId);
            return entity.map(mapper::toModel).orElse(null);
        } catch (Exception e) {
            throw new RegistryException("Failed to get process " + processId, e);
        }
    }

    /**
     * Update an existing process record.
     *
     * @param processModel the updated process data
     * @param processId    the process identifier
     * @throws RegistryException on persistence failure
     */
    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        try {
            processModel.setProcessId(processId);
            ProcessEntity entity = mapper.toEntity(processModel);
            processRepository.save(entity);
            logger.debug("Updated process {}", processId);
        } catch (Exception e) {
            throw new RegistryException("Failed to update process " + processId, e);
        }
    }

    /**
     * Return all processes matching a field/value query.
     *
     * <p>Currently supports {@code DBConstants.Process.EXPERIMENT_ID} as the field name.
     *
     * @param fieldName the field to filter by
     * @param value     the expected value
     * @return list of matching process models
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException {
        try {
            if (value == null) {
                return new ArrayList<>();
            }
            if (DBConstants.Process.EXPERIMENT_ID.equalsIgnoreCase(fieldName) || "experimentId".equals(fieldName)) {
                List<ProcessEntity> entities = processRepository.findByExperimentId(value.toString());
                return mapper.toModelList(entities);
            }
            logger.warn("Unsupported process query field: {}", fieldName);
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RegistryException("Failed to list processes by " + fieldName + "=" + value, e);
        }
    }

    /**
     * Convenience overload: return all processes for a given experiment.
     *
     * @param experimentId the parent experiment identifier
     * @return list of process models
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        try {
            List<ProcessEntity> entities = processRepository.findByExperimentId(experimentId);
            return mapper.toModelList(entities);
        } catch (Exception e) {
            throw new RegistryException("Failed to list processes for experiment " + experimentId, e);
        }
    }

    /**
     * Return just the process IDs for a given experiment.
     *
     * @param experimentId the parent experiment identifier
     * @return list of process ID strings
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public List<String> getProcessIds(String experimentId) throws RegistryException {
        try {
            List<ProcessEntity> entities = processRepository.findByExperimentId(experimentId);
            return entities.stream().map(ProcessEntity::getProcessId).toList();
        } catch (Exception e) {
            throw new RegistryException("Failed to list process IDs for experiment " + experimentId, e);
        }
    }

    /**
     * Return all processes whose latest status matches the given state.
     *
     * @param state the process state to filter by
     * @return list of matching process models
     * @throws RegistryException on retrieval failure
     */
    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessListInState(ProcessState state) throws RegistryException {
        try {
            List<ProcessEntity> entities = processRepository.findByLatestState(state.name());
            return mapper.toModelList(entities);
        } catch (Exception e) {
            throw new RegistryException("Failed to find processes in state " + state, e);
        }
    }

    /**
     * Remove a process record by its ID.
     *
     * @param processId the process identifier
     * @throws RegistryException on deletion failure
     */
    public void removeProcess(String processId) throws RegistryException {
        try {
            processRepository.deleteById(processId);
            logger.debug("Deleted process {}", processId);
        } catch (Exception e) {
            throw new RegistryException("Failed to remove process " + processId, e);
        }
    }
}
