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

import java.util.*;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ProcessInputEntity;
import org.apache.airavata.orchestration.model.ProcessInputPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessInputRepository
        extends AbstractRepository<InputDataObjectType, ProcessInputEntity, ProcessInputPK> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInputRepository.class);

    public ProcessInputRepository() {
        super(InputDataObjectType.class, ProcessInputEntity.class);
    }

    @Override
    protected InputDataObjectType toModel(ProcessInputEntity entity) {
        return ExecutionMapper.INSTANCE.processInputToModel(entity);
    }

    @Override
    protected ProcessInputEntity toEntity(InputDataObjectType model) {
        return ExecutionMapper.INSTANCE.processInputToEntity(model);
    }

    protected void saveProcessInput(List<InputDataObjectType> processInputs, String processId)
            throws RegistryException {

        for (InputDataObjectType input : processInputs) {
            ProcessInputEntity processInputEntity = ExecutionMapper.INSTANCE.processInputToEntity(input);

            if (processInputEntity.getProcessId() == null) {
                logger.debug("Setting the ProcessInputEntity's ProcessId");
                processInputEntity.setProcessId(processId);
            }

            execute(entityManager -> entityManager.merge(processInputEntity));
        }
    }

    public String addProcessInputs(List<InputDataObjectType> processInputs, String processId) throws RegistryException {
        saveProcessInput(processInputs, processId);
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> updatedProcessInputs, String processId)
            throws RegistryException {
        saveProcessInput(updatedProcessInputs, processId);
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessInputsList();
    }
}
