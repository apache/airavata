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

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentOutputEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentOutputPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExperimentOutputRepository extends ExpCatAbstractRepository<OutputDataObjectType, ExperimentOutputEntity, ExperimentOutputPK> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentOutputRepository.class);

    public ExperimentOutputRepository() {
        super(OutputDataObjectType.class, ExperimentOutputEntity.class);
    }

    protected void saveExperimentOutput(List<OutputDataObjectType> experimentOutputs, String experimentId) throws RegistryException {

        for (OutputDataObjectType output : experimentOutputs) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ExperimentOutputEntity experimentOutputEntity = mapper.map(output, ExperimentOutputEntity.class);

            if (experimentOutputEntity.getExperimentId() == null) {
                logger.debug("Setting the ExperimentOutputEntity's ExperimentId");
                experimentOutputEntity.setExperimentId(experimentId);
            }

            execute(entityManager -> entityManager.merge(experimentOutputEntity));
        }

    }

    public String addExperimentOutputs(List<OutputDataObjectType> experimentOutputs, String experimentId) throws RegistryException {
        saveExperimentOutput(experimentOutputs, experimentId);
        return experimentId;
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> updatedExperimentOutputs, String experimentId) throws RegistryException {
        saveExperimentOutput(updatedExperimentOutputs, experimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getExperimentOutputs();
    }

}
