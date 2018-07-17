/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentErrorEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentErrorPK;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExperimentErrorRepository extends ExpCatAbstractRepository<ErrorModel, ExperimentErrorEntity, ExperimentErrorPK> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentErrorRepository.class);

    public ExperimentErrorRepository() {
        super(ErrorModel.class, ExperimentErrorEntity.class);
    }

    protected String saveExperimentError(ErrorModel error, String experimentId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ExperimentErrorEntity experimentErrorEntity = mapper.map(error, ExperimentErrorEntity.class);

        if (experimentErrorEntity.getExperimentId() == null) {
            logger.debug("Setting the ExperimentErrorEntity's ExperimentId");
            experimentErrorEntity.setExperimentId(experimentId);
        }

        execute(entityManager -> entityManager.merge(experimentErrorEntity));
        return experimentErrorEntity.getErrorId();
    }

    public String addExperimentError(ErrorModel experimentError, String experimentId) throws RegistryException {

        if (experimentError.getErrorId() == null) {
            logger.debug("Setting the ExperimentError's ErrorId");
            experimentError.setErrorId(ExpCatalogUtils.getID("ERROR"));
        }

        return saveExperimentError(experimentError, experimentId);
    }

    public String updateExperimentError(ErrorModel updatedExperimentError, String experimentId) throws RegistryException {
        return saveExperimentError(updatedExperimentError, experimentId);
    }

    public List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getErrors();
    }

}
