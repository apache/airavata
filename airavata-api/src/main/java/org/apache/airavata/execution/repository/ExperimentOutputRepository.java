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
package org.apache.airavata.execution.repository;

import java.util.List;
import org.apache.airavata.execution.mapper.ExecutionMapper;
import org.apache.airavata.execution.model.ExperimentOutputEntity;
import org.apache.airavata.execution.model.ExperimentOutputPK;
import org.apache.airavata.execution.util.AbstractRepository;
import org.apache.airavata.execution.util.cpi.RegistryException;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentOutputRepository
        extends AbstractRepository<OutputDataObjectType, ExperimentOutputEntity, ExperimentOutputPK> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentOutputRepository.class);

    public ExperimentOutputRepository() {
        super(OutputDataObjectType.class, ExperimentOutputEntity.class);
    }

    @Override
    protected OutputDataObjectType toModel(ExperimentOutputEntity entity) {
        return ExecutionMapper.INSTANCE.experimentOutputToModel(entity);
    }

    @Override
    protected ExperimentOutputEntity toEntity(OutputDataObjectType model) {
        return ExecutionMapper.INSTANCE.experimentOutputToEntity(model);
    }

    protected void saveExperimentOutput(List<OutputDataObjectType> experimentOutputs, String experimentId)
            throws RegistryException {

        for (OutputDataObjectType output : experimentOutputs) {
            ExperimentOutputEntity experimentOutputEntity = ExecutionMapper.INSTANCE.experimentOutputToEntity(output);

            if (experimentOutputEntity.getExperimentId() == null) {
                logger.debug("Setting the ExperimentOutputEntity's ExperimentId");
                experimentOutputEntity.setExperimentId(experimentId);
            }

            execute(entityManager -> entityManager.merge(experimentOutputEntity));
        }
    }

    public String addExperimentOutputs(List<OutputDataObjectType> experimentOutputs, String experimentId)
            throws RegistryException {
        saveExperimentOutput(experimentOutputs, experimentId);
        return experimentId;
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> updatedExperimentOutputs, String experimentId)
            throws RegistryException {
        saveExperimentOutput(updatedExperimentOutputs, experimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getExperimentOutputs();
    }
}
