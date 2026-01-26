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
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.InputDataRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentInputService {
    private final InputDataRepository inputDataRepository;
    private final ExperimentRepository experimentRepository;
    private final EntityManager entityManager;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;

    public ExperimentInputService(
            InputDataRepository inputDataRepository,
            ExperimentRepository experimentRepository,
            EntityManager entityManager,
            InputDataObjectTypeMapper inputDataObjectTypeMapper) {
        this.inputDataRepository = inputDataRepository;
        this.experimentRepository = experimentRepository;
        this.entityManager = entityManager;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
    }

    public String addExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        // Load the experiment to ensure it's in the persistence context and visible for foreign key constraints
        var experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));
        // Save and flush the experiment to ensure it's visible in the database for foreign key constraint checks
        // This is necessary when the experiment was created in a different transaction
        experimentEntity = experimentRepository.save(experimentEntity);
        experimentRepository.flush();
        for (var input : inputs) {
            var entity = inputDataObjectTypeMapper.toExperimentInputEntity(input, experimentId);
            inputDataRepository.save(entity);
        }
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        // Ensure experiment exists and is flushed before any deletes
        var experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));
        experimentEntity = experimentRepository.save(experimentEntity);
        experimentRepository.flush();

        // Delete existing inputs
        inputDataRepository.deleteByParentIdAndParentType(experimentId, DataObjectParentType.EXPERIMENT);
        // Flush to ensure deletes are executed
        inputDataRepository.flush();
        // Clear entity manager to remove any managed entities from persistence context
        entityManager.clear();

        // Add new inputs
        for (var input : inputs) {
            var entity = inputDataObjectTypeMapper.toExperimentInputEntity(input, experimentId);
            inputDataRepository.save(entity);
        }
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        List<InputDataEntity> entities = inputDataRepository.findByExperimentId(experimentId);
        return inputDataObjectTypeMapper.toModelList(entities);
    }
}
