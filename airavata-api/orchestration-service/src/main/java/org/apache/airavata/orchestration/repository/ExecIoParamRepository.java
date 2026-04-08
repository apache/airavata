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
import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ExecIoParamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExecIoParamRepository extends AbstractRepository<ExecIoParamEntity, ExecIoParamEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ExecIoParamRepository.class);

    public ExecIoParamRepository() {
        super(ExecIoParamEntity.class, ExecIoParamEntity.class);
    }

    @Override
    protected ExecIoParamEntity toModel(ExecIoParamEntity entity) {
        return entity;
    }

    @Override
    protected ExecIoParamEntity toEntity(ExecIoParamEntity model) {
        return model;
    }

    // --- Process Input methods ---

    public String addProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        saveProcessInputs(inputs, processId);
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        saveProcessInputs(inputs, processId);
    }

    protected void saveProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        org.apache.airavata.orchestration.model.ProcessEntity processEntity =
                execute(em -> em.find(org.apache.airavata.orchestration.model.ProcessEntity.class, processId));
        if (processEntity == null) return;
        if (processEntity.getProcessInputs() == null) {
            processEntity.setProcessInputs(new ArrayList<>());
        }
        for (InputDataObjectType input : inputs) {
            ExecIoParamEntity entity = ExecutionMapper.INSTANCE.processInputToEntity(input);
            entity.setDirection("INPUT");
            boolean found = false;
            if (entity.getName() != null) {
                for (ExecIoParamEntity existing : processEntity.getProcessInputs()) {
                    if (entity.getName().equals(existing.getName())) {
                        existing.setValue(entity.getValue());
                        existing.setType(entity.getType());
                        existing.setApplicationArgument(entity.getApplicationArgument());
                        existing.setStandardInput(entity.isStandardInput());
                        existing.setUserFriendlyDescription(entity.getUserFriendlyDescription());
                        existing.setMetaData(entity.getMetaData());
                        existing.setInputOrder(entity.getInputOrder());
                        existing.setIsRequired(entity.isIsRequired());
                        existing.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
                        existing.setDataStaged(entity.isDataStaged());
                        existing.setStorageResourceId(entity.getStorageResourceId());
                        existing.setReadOnly(entity.isReadOnly());
                        existing.setOverrideFilename(entity.getOverrideFilename());
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                processEntity.getProcessInputs().add(entity);
            }
        }
        execute(em -> em.merge(processEntity));
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.getProcess(processId).getProcessInputsList();
    }

    // --- Process Output methods ---

    public String addProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        saveProcessOutputs(outputs, processId);
        return processId;
    }

    public void updateProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        saveProcessOutputs(outputs, processId);
    }

    protected void saveProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        org.apache.airavata.orchestration.model.ProcessEntity processEntity =
                execute(em -> em.find(org.apache.airavata.orchestration.model.ProcessEntity.class, processId));
        if (processEntity == null) return;
        if (processEntity.getProcessOutputs() == null) {
            processEntity.setProcessOutputs(new ArrayList<>());
        }
        for (OutputDataObjectType output : outputs) {
            ExecIoParamEntity entity = ExecutionMapper.INSTANCE.processOutputToEntity(output);
            entity.setDirection("OUTPUT");
            boolean found = false;
            if (entity.getName() != null) {
                for (ExecIoParamEntity existing : processEntity.getProcessOutputs()) {
                    if (entity.getName().equals(existing.getName())) {
                        existing.setValue(entity.getValue());
                        existing.setType(entity.getType());
                        existing.setApplicationArgument(entity.getApplicationArgument());
                        existing.setIsRequired(entity.isIsRequired());
                        existing.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
                        existing.setDataMovement(entity.isDataMovement());
                        existing.setLocation(entity.getLocation());
                        existing.setSearchQuery(entity.getSearchQuery());
                        existing.setOutputStreaming(entity.isOutputStreaming());
                        existing.setStorageResourceId(entity.getStorageResourceId());
                        existing.setMetaData(entity.getMetaData());
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                processEntity.getProcessOutputs().add(entity);
            }
        }
        execute(em -> em.merge(processEntity));
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        return processRepository.getProcess(processId).getProcessOutputsList();
    }
}
