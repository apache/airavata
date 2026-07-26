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
package org.apache.airavata.experiment.repository;

import java.util.List;

import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.models.commons.ErrorModel;
import org.apache.airavata.models.experiment.ExperimentModel;
import org.apache.airavata.experiment.mapper.ExperimentMapper;
import org.apache.airavata.experiment.model.ExperimentErrorEntity;
import org.apache.airavata.experiment.model.ExperimentErrorPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExperimentErrorRepository
        extends AbstractRepository<ErrorModel, ExperimentErrorEntity, ExperimentErrorPK> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentErrorRepository.class);

    private final ExperimentRepository experimentRepository;

    public ExperimentErrorRepository(ExperimentRepository experimentRepository) {
        super(ErrorModel.class, ExperimentErrorEntity.class);
        this.experimentRepository = experimentRepository;
    }

    @Override
    protected ErrorModel toModel(ExperimentErrorEntity entity) {
        return ExperimentMapper.INSTANCE.experimentErrorToModel(entity);
    }

    @Override
    protected ExperimentErrorEntity toEntity(ErrorModel model) {
        return ExperimentMapper.INSTANCE.experimentErrorToEntity(model);
    }

    protected String saveExperimentError(ErrorModel error, String experimentId) throws Exception {
        ExperimentErrorEntity experimentErrorEntity = ExperimentMapper.INSTANCE.experimentErrorToEntity(error);

        if (experimentErrorEntity.getExperimentId() == null) {
            logger.debug("Setting the ExperimentErrorEntity's ExperimentId");
            experimentErrorEntity.setExperimentId(experimentId);
        }

        execute(entityManager -> entityManager.merge(experimentErrorEntity));
        return experimentErrorEntity.getErrorId();
    }

    public String addExperimentError(ErrorModel experimentError, String experimentId) throws Exception {

        if (experimentError.errorId().isEmpty()) {
            logger.debug("Setting the ExperimentError's ErrorId");
            experimentError = experimentError.toBuilder()
                    .setErrorId(AiravataUtils.getId("ERROR"))
                    .build();
        }

        return saveExperimentError(experimentError, experimentId);
    }

    public String updateExperimentError(ErrorModel updatedExperimentError, String experimentId)
            throws Exception {
        return saveExperimentError(updatedExperimentError, experimentId);
    }

    public List<ErrorModel> getExperimentErrors(String experimentId) throws Exception {
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.errors();
    }
}
