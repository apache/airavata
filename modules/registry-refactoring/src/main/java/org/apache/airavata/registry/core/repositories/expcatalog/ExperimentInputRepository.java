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

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentInputEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentInputPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExperimentInputRepository extends ExpCatAbstractRepository<InputDataObjectType, ExperimentInputEntity, ExperimentInputPK> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentInputRepository.class);

    public ExperimentInputRepository() { super(InputDataObjectType.class, ExperimentInputEntity.class); }

    protected void saveExperimentInput(List<InputDataObjectType> experimentInputs, String experimentId) throws RegistryException {

        for (InputDataObjectType input : experimentInputs) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ExperimentInputEntity experimentInputEntity = mapper.map(input, ExperimentInputEntity.class);

            if (experimentInputEntity.getExperimentId() == null) {
                logger.debug("Setting the ExperimentInputEntity's ExperimentId");
                experimentInputEntity.setExperimentId(experimentId);
            }

            execute(entityManager -> entityManager.merge(experimentInputEntity));
        }

    }

    public String addExperimentInputs(List<InputDataObjectType> experimentInputs, String experimentId) throws RegistryException {
        saveExperimentInput(experimentInputs, experimentId);
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> updatedExperimentInputs, String experimentId) throws RegistryException {
        saveExperimentInput(updatedExperimentInputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getExperimentInputs();
    }

}
