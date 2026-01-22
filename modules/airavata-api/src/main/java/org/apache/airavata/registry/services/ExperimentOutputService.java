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
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ExperimentOutputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.OutputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentOutputRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentOutputService {
    private final ExperimentOutputRepository experimentOutputRepository;
    private final ExperimentRepository experimentRepository;
    private final EntityManager entityManager;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper;

    public ExperimentOutputService(
            ExperimentOutputRepository experimentOutputRepository,
            ExperimentRepository experimentRepository,
            EntityManager entityManager,
            OutputDataObjectTypeMapper outputDataObjectTypeMapper) {
        this.experimentOutputRepository = experimentOutputRepository;
        this.experimentRepository = experimentRepository;
        this.entityManager = entityManager;
        this.outputDataObjectTypeMapper = outputDataObjectTypeMapper;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        List<ExperimentOutputEntity> entities = experimentOutputRepository.findByExperimentId(experimentId);
        return outputDataObjectTypeMapper.toModelListFromExperiment(entities);
    }

    public void addExperimentOutputs(List<OutputDataObjectType> outputs, String experimentId) throws RegistryException {
        // Ensure experiment exists and is flushed to database for foreign key constraints
        // We need to actually load and flush it, not just get a reference
        var experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));
        // Save and flush the experiment to ensure it's visible in the database for foreign key constraint checks
        experimentEntity = experimentRepository.save(experimentEntity);
        experimentRepository.flush();
        for (var output : outputs) {
            var entity = outputDataObjectTypeMapper.toEntity(output);
            entity.setExperimentId(experimentId);
            // Note: We don't call entity.setExperiment() because the @JoinColumn has insertable=false.
            // The experimentId field is already set and is the only field that gets persisted.
            experimentOutputRepository.save(entity);
        }
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> outputs, String experimentId)
            throws RegistryException {
        // Ensure experiment exists and is flushed before any deletes
        var experimentEntity = experimentRepository
                .findById(experimentId)
                .orElseThrow(() -> new RegistryException("Experiment with ID " + experimentId + " does not exist"));
        experimentEntity = experimentRepository.save(experimentEntity);
        experimentRepository.flush();

        // Delete existing outputs using native query to bypass persistence context issues
        experimentOutputRepository.deleteByExperimentId(experimentId);
        // Flush to ensure deletes are executed
        experimentOutputRepository.flush();
        // Clear entity manager to remove any managed entities from persistence context
        entityManager.clear();

        // Add new outputs
        for (var output : outputs) {
            var entity = outputDataObjectTypeMapper.toEntity(output);
            entity.setExperimentId(experimentId);
            // Note: We don't call entity.setExperiment() because the @JoinColumn has insertable=false.
            // The experimentId field is already set and is the only field that gets persisted.
            experimentOutputRepository.save(entity);
        }
    }
}
