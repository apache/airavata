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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ErrorModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base service class for error management operations.
 * Provides common functionality for adding, updating, and retrieving errors
 * associated with different entity types (Process, Task, Experiment).
 *
 * @param <TEntity> The error entity type (e.g., ProcessErrorEntity, TaskErrorEntity)
 * @param <TRepository> The repository interface extending JpaRepository
 * @param <TPK> The primary key type for the entity
 */
public abstract class BaseErrorService<TEntity, TRepository extends JpaRepository<TEntity, TPK>, TPK> {

    protected final TRepository repository;
    protected final ErrorModelMapper errorModelMapper;

    /**
     * Functional interface to set the parent entity ID on the error entity.
     * For example: entity.setProcessId(parentId)
     */
    protected abstract BiConsumer<TEntity, String> getParentIdSetter();

    /**
     * Functional interface to retrieve errors by parent entity ID.
     * For example: repository.findByProcessId(parentId)
     */
    protected abstract Function<String, List<TEntity>> getFindByParentIdFunction();

    /**
     * Functional interface to map ErrorModel to entity.
     */
    protected abstract Function<ErrorModel, TEntity> getModelToEntityMapper();

    /**
     * Functional interface to map entity to ErrorModel.
     */
    protected abstract Function<TEntity, ErrorModel> getEntityToModelMapper();

    /**
     * Functional interface to extract the error ID from the entity.
     * For example: entity.getErrorId()
     */
    protected abstract Function<TEntity, String> getErrorIdExtractor();

    protected BaseErrorService(TRepository repository, ErrorModelMapper errorModelMapper) {
        this.repository = repository;
        this.errorModelMapper = errorModelMapper;
    }

    /**
     * Add an error associated with a parent entity.
     *
     * @param error The error model to persist
     * @param parentId The ID of the parent entity (processId, taskId, or experimentId)
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    @Transactional
    public String addError(ErrorModel error, String parentId) throws RegistryException {
        TEntity entity = getModelToEntityMapper().apply(error);
        getParentIdSetter().accept(entity, parentId);
        // CREATION_TIME is now set automatically via @PrePersist callback on error entities
        // No reflection needed - JPA handles this
        TEntity saved = repository.save(entity);
        return getErrorIdExtractor().apply(saved);
    }

    /**
     * Update an existing error associated with a parent entity.
     *
     * @param error The error model with updated information
     * @param parentId The ID of the parent entity
     * @throws RegistryException if the operation fails
     */
    @Transactional
    public void updateError(ErrorModel error, String parentId) throws RegistryException {
        TEntity entity = getModelToEntityMapper().apply(error);
        getParentIdSetter().accept(entity, parentId);
        // Ensure CREATION_TIME is set if not already set (for updates, preserve existing or set new)
        if (entity instanceof org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity) {
            org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity taskErrorEntity =
                    (org.apache.airavata.registry.entities.expcatalog.TaskErrorEntity) entity;
            if (taskErrorEntity.getCreationTime() == null) {
                taskErrorEntity.setCreationTime(org.apache.airavata.common.utils.AiravataUtils.getCurrentTimestamp());
            }
        } else if (entity instanceof org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity) {
            org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity experimentErrorEntity =
                    (org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity) entity;
            if (experimentErrorEntity.getCreationTime() == null) {
                experimentErrorEntity.setCreationTime(
                        org.apache.airavata.common.utils.AiravataUtils.getCurrentTimestamp());
            }
        } else if (entity instanceof org.apache.airavata.registry.entities.expcatalog.ProcessErrorEntity) {
            org.apache.airavata.registry.entities.expcatalog.ProcessErrorEntity processErrorEntity =
                    (org.apache.airavata.registry.entities.expcatalog.ProcessErrorEntity) entity;
            if (processErrorEntity.getCreationTime() == null) {
                processErrorEntity.setCreationTime(
                        org.apache.airavata.common.utils.AiravataUtils.getCurrentTimestamp());
            }
        }
        repository.save(entity);
    }

    /**
     * Retrieve all errors associated with a parent entity.
     *
     * @param parentId The ID of the parent entity
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    @Transactional(readOnly = true)
    public List<ErrorModel> getErrors(String parentId) throws RegistryException {
        List<TEntity> entities = getFindByParentIdFunction().apply(parentId);
        return entities.stream().map(getEntityToModelMapper()).collect(Collectors.toList());
    }
}
