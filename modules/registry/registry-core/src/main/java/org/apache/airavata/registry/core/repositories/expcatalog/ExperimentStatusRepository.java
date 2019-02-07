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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentStatusEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentStatusPK;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

public class ExperimentStatusRepository extends ExpCatAbstractRepository<ExperimentStatus, ExperimentStatusEntity, ExperimentStatusPK> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentStatusRepository.class);

    public ExperimentStatusRepository() { super(ExperimentStatus.class, ExperimentStatusEntity.class); }

    protected String saveExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {

        if (experimentStatus.getStatusId() == null) {

            ExperimentStatus currentExperimentStatus = getExperimentStatus(experimentId);
            if (currentExperimentStatus == null || currentExperimentStatus.getState() != experimentStatus.getState()) {
                experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
            } else {
                // Update the existing current status if experimentStatus has no status id and the same state
                experimentStatus.setStatusId(currentExperimentStatus.getStatusId());
            }
        }

        Mapper mapper = ObjectMapperSingleton.getInstance();
        ExperimentStatusEntity experimentStatusEntity = mapper.map(experimentStatus, ExperimentStatusEntity.class);

        if (experimentStatusEntity.getExperimentId() == null) {
            logger.debug("Setting the ExperimentStatusEntity's ExperimentId");
            experimentStatusEntity.setExperimentId(experimentId);
        }

        execute(entityManager -> entityManager.merge(experimentStatusEntity));
        return experimentStatusEntity.getStatusId();
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {

        if (experimentStatus.getStatusId() == null) {
            logger.debug("Setting the ExperimentStatus's StatusId");
            experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
        }
        experimentStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());

        return saveExperimentStatus(experimentStatus, experimentId);
    }

    public String updateExperimentStatus(ExperimentStatus updatedExperimentStatus, String experimentId) throws RegistryException {
        return saveExperimentStatus(updatedExperimentStatus, experimentId);
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
        ExperimentRepository experimentRepository = new ExperimentRepository();
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        List<ExperimentStatus> experimentStatusList = experimentModel.getExperimentStatus();

        if(experimentStatusList.size() == 0) {
            logger.debug("ExperimentStatus list is empty");
            return null;
        }

        else {
            ExperimentStatus latestExperimentStatus = experimentStatusList.get(0);

            for (int i = 1; i < experimentStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(experimentStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange != null) {

                    if (timeOfStateChange.after(new Timestamp(latestExperimentStatus.getTimeOfStateChange()))
                            || (timeOfStateChange.equals(latestExperimentStatus.getTimeOfStateChange()) && experimentStatusList.get(i).getState().equals(ExperimentState.COMPLETED.toString()))
                            || (timeOfStateChange.equals(latestExperimentStatus.getTimeOfStateChange()) && experimentStatusList.get(i).getState().equals(ExperimentState.FAILED.toString()))
                            || (timeOfStateChange.equals(latestExperimentStatus.getTimeOfStateChange()) && experimentStatusList.get(i).getState().equals(ExperimentState.CANCELED.toString()))) {
                        latestExperimentStatus = experimentStatusList.get(i);
                    }

                }

            }

            return latestExperimentStatus;
        }
    }

}
