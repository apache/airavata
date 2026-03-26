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
package org.apache.airavata.execution.process;

import java.util.List;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.util.IdGenerator;
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

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessService.class);

    private final ProcessRepository processRepository;
    private final ProcessMapper mapper;

    public DefaultProcessService(ProcessRepository processRepository, ProcessMapper mapper) {
        this.processRepository = processRepository;
        this.mapper = mapper;
    }

    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryException {
        return registryOp("add process for experiment " + experimentId, () -> {
            processModel.setProcessId(IdGenerator.ensureId(processModel.getProcessId()));
            processModel.setExperimentId(experimentId);
            processRepository.save(mapper.toEntity(processModel));
            logger.debug("Saved process {} for experiment {}", processModel.getProcessId(), experimentId);
            return processModel.getProcessId();
        });
    }

    @Transactional(readOnly = true)
    public ProcessModel getProcess(String processId) throws RegistryException {
        return registryOp(
                "get process " + processId,
                () -> processRepository.findById(processId).map(mapper::toModel).orElse(null));
    }

    public void updateProcess(ProcessModel processModel, String processId) throws RegistryException {
        registryOp("update process " + processId, () -> {
            processModel.setProcessId(processId);
            processRepository.save(mapper.toEntity(processModel));
            logger.debug("Updated process {}", processId);
            return null;
        });
    }

    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryException {
        return registryOp(
                "list processes for experiment " + experimentId,
                () -> mapper.toModelList(processRepository.findByExperimentId(experimentId)));
    }

    @Transactional(readOnly = true)
    public List<String> getProcessIds(String experimentId) throws RegistryException {
        return registryOp(
                "list process IDs for experiment " + experimentId,
                () -> processRepository.findByExperimentId(experimentId).stream()
                        .map(ProcessEntity::getProcessId)
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<ProcessModel> getProcessListInState(ProcessState state) throws RegistryException {
        return registryOp(
                "find processes in state " + state,
                () -> mapper.toModelList(processRepository.findByLatestState(state.name())));
    }

    public void removeProcess(String processId) throws RegistryException {
        registryOp("remove process " + processId, () -> {
            processRepository.deleteById(processId);
            logger.debug("Deleted process {}", processId);
            return null;
        });
    }

    private <T> T registryOp(String description, java.util.concurrent.Callable<T> operation) throws RegistryException {
        try {
            return operation.call();
        } catch (Exception e) {
            throw new RegistryException("Failed to " + description, e);
        }
    }
}
