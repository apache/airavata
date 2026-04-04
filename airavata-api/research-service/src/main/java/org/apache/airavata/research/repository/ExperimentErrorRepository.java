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
package org.apache.airavata.research.repository;

import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.research.mapper.ResearchMapper;
import org.apache.airavata.research.model.ExperimentErrorEntity;
import org.apache.airavata.research.model.ExperimentErrorPK;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExperimentErrorRepository
        extends AbstractRepository<ErrorModel, ExperimentErrorEntity, ExperimentErrorPK> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentErrorRepository.class);

    public ExperimentErrorRepository() {
        super(ErrorModel.class, ExperimentErrorEntity.class);
    }

    @Override
    protected ErrorModel toModel(ExperimentErrorEntity entity) {
        return ResearchMapper.INSTANCE.experimentErrorToModel(entity);
    }

    @Override
    protected ExperimentErrorEntity toEntity(ErrorModel model) {
        return ResearchMapper.INSTANCE.experimentErrorToEntity(model);
    }

    protected String saveExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        ExperimentErrorEntity experimentErrorEntity = ResearchMapper.INSTANCE.experimentErrorToEntity(error);

        if (experimentErrorEntity.getExperimentId() == null) {
            logger.debug("Setting the ExperimentErrorEntity's ExperimentId");
            experimentErrorEntity.setExperimentId(experimentId);
        }

        execute(entityManager -> entityManager.merge(experimentErrorEntity));
        return experimentErrorEntity.getErrorId();
    }

    public String addExperimentError(ErrorModel experimentError, String experimentId) throws RegistryException {

        if (experimentError.getErrorId().isEmpty()) {
            logger.debug("Setting the ExperimentError's ErrorId");
            experimentError = experimentError.toBuilder()
                    .setErrorId(ExpCatalogUtils.getID("ERROR"))
                    .build();
        }

        return saveExperimentError(experimentError, experimentId);
    }

    public String updateExperimentError(ErrorModel updatedExperimentError, String experimentId)
            throws RegistryException {
        return saveExperimentError(updatedExperimentError, experimentId);
    }

    public List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getErrorsList();
    }
}
