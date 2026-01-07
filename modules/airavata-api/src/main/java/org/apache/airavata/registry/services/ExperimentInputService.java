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
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentInputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.InputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentInputRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ExperimentInputService {
    private final ExperimentInputRepository experimentInputRepository;
    private final ExperimentRepository experimentRepository;
    private final EntityManager entityManager;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper;

    public ExperimentInputService(
            ExperimentInputRepository experimentInputRepository,
            ExperimentRepository experimentRepository,
            @Qualifier("expCatalogEntityManager") EntityManager entityManager,
            InputDataObjectTypeMapper inputDataObjectTypeMapper) {
        this.experimentInputRepository = experimentInputRepository;
        this.experimentRepository = experimentRepository;
        this.entityManager = entityManager;
        this.inputDataObjectTypeMapper = inputDataObjectTypeMapper;
    }

    public String addExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        // Load the experiment to ensure it's in the persistence context and visible for foreign key constraints
        ExperimentEntity experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));
        // Save and flush the experiment to ensure it's visible in the database for foreign key constraint checks
        // This is necessary when the experiment was created in a different transaction
        experimentEntity = experimentRepository.save(experimentEntity);
        experimentRepository.flush();
        for (InputDataObjectType input : inputs) {
            ExperimentInputEntity entity = inputDataObjectTypeMapper.toEntity(input);
            entity.setExperimentId(experimentId);
            // Set experiment relationship to ensure EXPERIMENT_ID is set via @JoinColumn
            entity.setExperiment(experimentEntity);
            experimentInputRepository.save(entity);
        }
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        // Delete existing inputs and add new ones
        List<ExperimentInputEntity> existing = experimentInputRepository.findByExperimentId(experimentId);
        if (!existing.isEmpty()) {
            experimentInputRepository.deleteAll(existing);
            // Flush deletes to ensure they're executed before inserting new ones with same IDs
            experimentInputRepository.flush();
        }
        // Use addExperimentInputs to add new inputs (reuses the same logic)
        // Note: We don't need to explicitly load the experiment here because addExperimentInputs will handle it
        addExperimentInputs(inputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        List<ExperimentInputEntity> entities = experimentInputRepository.findByExperimentId(experimentId);
        return inputDataObjectTypeMapper.toModelListFromExperiment(entities);
    }
}
