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

import com.github.dozermapper.core.Mapper;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentErrorPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentErrorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentErrorService extends BaseErrorService<ExperimentErrorEntity, ExperimentErrorRepository, ExperimentErrorPK> {

    public ExperimentErrorService(ExperimentErrorRepository experimentErrorRepository, Mapper mapper) {
        super(experimentErrorRepository, mapper);
    }

    @Override
    protected BiConsumer<ExperimentErrorEntity, String> getParentIdSetter() {
        return ExperimentErrorEntity::setExperimentId;
    }

    @Override
    protected Function<String, java.util.List<ExperimentErrorEntity>> getFindByParentIdFunction() {
        return repository::findByExperimentId;
    }

    @Override
    protected Class<ExperimentErrorEntity> getEntityClass() {
        return ExperimentErrorEntity.class;
    }

    @Override
    protected Function<ExperimentErrorEntity, String> getErrorIdExtractor() {
        return ExperimentErrorEntity::getErrorId;
    }

    /**
     * Add an experiment error.
     *
     * @param error The error model to persist
     * @param experimentId The ID of the experiment
     * @return The ID of the saved error entity
     * @throws RegistryException if the operation fails
     */
    public String addExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        return addError(error, experimentId);
    }

    /**
     * Update an experiment error.
     *
     * @param error The error model with updated information
     * @param experimentId The ID of the experiment
     * @throws RegistryException if the operation fails
     */
    public void updateExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        updateError(error, experimentId);
    }

    /**
     * Retrieve all errors for an experiment.
     *
     * @param experimentId The ID of the experiment
     * @return List of error models
     * @throws RegistryException if the operation fails
     */
    public java.util.List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        return getErrors(experimentId);
    }
}
