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
import java.util.List;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessInputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessInputRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ProcessInputService {
    private final ProcessInputRepository processInputRepository;
    private final EntityManager entityManager;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;

    public ProcessInputService(
            ProcessInputRepository processInputRepository,
            @Qualifier("expCatalogEntityManager") EntityManager entityManager,
            InputDataObjectTypeMapper inputDataObjectTypeMapper) {
        this.processInputRepository = processInputRepository;
        this.entityManager = entityManager;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
    }

    public String addProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        // Get a reference to the process entity (proxy, doesn't fetch from DB)
        ProcessEntity processEntity = entityManager.getReference(ProcessEntity.class, processId);
        for (InputDataObjectType input : inputs) {
            ProcessInputEntity entity = inputDataObjectTypeMapper.toEntityFromProcess(input);
            entity.setProcessId(processId);
            // Set process relationship to ensure PROCESS_ID is set via @JoinColumn
            entity.setProcess(processEntity);
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
