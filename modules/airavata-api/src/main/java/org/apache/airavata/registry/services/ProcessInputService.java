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

import java.util.List;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ProcessInputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessInputService {
    private final ProcessInputRepository processInputRepository;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;

    public ProcessInputService(
            ProcessInputRepository processInputRepository, InputDataObjectTypeMapper inputDataObjectTypeMapper) {
        this.processInputRepository = processInputRepository;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
    }

    public String addProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        for (InputDataObjectType input : inputs) {
            ProcessInputEntity entity = inputDataObjectTypeMapper.toEntityFromProcess(input);
            entity.setProcessId(processId);
            // Note: We don't call entity.setProcess() because the @JoinColumn has insertable=false.
            // The processId field is already set and is the only field that gets persisted.
            processInputRepository.save(entity);
        }
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        // Delete existing inputs and add new ones
        List<ProcessInputEntity> existing = processInputRepository.findByProcessId(processId);
        if (!existing.isEmpty()) {
            processInputRepository.deleteAll(existing);
            processInputRepository.flush(); // Ensure deletes are executed before inserting new ones with same IDs
        }
        addProcessInputs(inputs, processId);
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        List<ProcessInputEntity> entities = processInputRepository.findByProcessId(processId);
        return inputDataObjectTypeMapper.toModelListFromProcess(entities);
    }
}
