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
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ExperimentOutputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.OutputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentOutputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ExperimentOutputService {
    private final ExperimentOutputRepository experimentOutputRepository;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper;

    public ExperimentOutputService(
            ExperimentOutputRepository experimentOutputRepository,
            OutputDataObjectTypeMapper outputDataObjectTypeMapper) {
        this.experimentOutputRepository = experimentOutputRepository;
        this.outputDataObjectTypeMapper = outputDataObjectTypeMapper;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        List<ExperimentOutputEntity> entities = experimentOutputRepository.findByExperimentId(experimentId);
        return outputDataObjectTypeMapper.toModelListFromExperiment(entities);
    }

    public void addExperimentOutputs(List<OutputDataObjectType> outputs, String experimentId) throws RegistryException {
        for (OutputDataObjectType output : outputs) {
            ExperimentOutputEntity entity = outputDataObjectTypeMapper.toEntity(output);
            entity.setExperimentId(experimentId);
            experimentOutputRepository.save(entity);
        }
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> outputs, String experimentId)
            throws RegistryException {
        // Delete existing outputs and add new ones
        List<ExperimentOutputEntity> existing = experimentOutputRepository.findByExperimentId(experimentId);
        experimentOutputRepository.deleteAll(existing);
        addExperimentOutputs(outputs, experimentId);
    }
}
