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
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessErrorEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessErrorPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ErrorModelMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessErrorRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ProcessErrorService extends BaseErrorService<ProcessErrorEntity, ProcessErrorRepository, ProcessErrorPK> {

    private final EntityManager entityManager;

    public ProcessErrorService(
            ProcessErrorRepository processErrorRepository,
            @Qualifier("expCatalogEntityManager") EntityManager entityManager,
            ErrorModelMapper errorModelMapper) {
        super(processErrorRepository, errorModelMapper);
        this.entityManager = entityManager;
    }

    @Override
    protected BiConsumer<ProcessErrorEntity, String> getParentIdSetter() {
        return ProcessErrorEntity::setProcessId;
    }

    @Override
    protected Function<String, java.util.List<ProcessErrorEntity>> getFindByParentIdFunction() {
        return repository::findByProcessId;
    }

    @Override
    protected Function<ErrorModel, ProcessErrorEntity> getModelToEntityMapper() {
        return errorModelMapper::toEntityFromProcess;
    }

    @Override
    protected Function<ProcessErrorEntity, ErrorModel> getEntityToModelMapper() {
        return errorModelMapper::toModel;
    }

    @Override
    protected Function<ProcessErrorEntity, String> getErrorIdExtractor() {
        return ProcessErrorEntity::getErrorId;
    }

    /**
     * Add a process error.
     *
     * @param error The error model to persist
     * @param processId The ID of the process
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addProcessError(ErrorModel error, String processId) throws RegistryException {
        ProcessErrorEntity entity = errorModelMapper.toEntityFromProcess(error);
        getParentIdSetter().accept(entity, processId);
        // Ensure CREATION_TIME is set if not already set
        if (entity.getCreationTime() == null) {
            entity.setCreationTime(org.apache.airavata.common.utils.AiravataUtils.getCurrentTimestamp());
        }
        // Get a reference to the process entity (proxy, doesn't fetch from DB)
        ProcessEntity processEntity = entityManager.getReference(ProcessEntity.class, processId);
        entity.setProcess(processEntity);
        ProcessErrorEntity saved = repository.save(entity);
        return getErrorIdExtractor().apply(saved);
    }

    /**
     * Update a process error.
     *
     * @param error The error model with updated information
     * @param processId The ID of the process
     * @throws RegistryException if the operation fails
     */
    public void updateProcessError(ErrorModel error, String processId) throws RegistryException {
        updateError(error, processId);
    }

    /**
     * Retrieve all errors for a process.
     *
     * @param processId The ID of the process
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    public java.util.List<ErrorModel> getProcessError(String processId) throws RegistryException {
        return getErrors(processId);
    }
}
