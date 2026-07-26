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

import java.sql.Timestamp;
import java.util.List;

import org.apache.airavata.common.AiravataUtils;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.models.experiment.ExperimentModel;
import org.apache.airavata.models.status.ExperimentState;
import org.apache.airavata.models.status.ExperimentStatus;
import org.apache.airavata.experiment.mapper.ExperimentMapper;
import org.apache.airavata.experiment.model.ExperimentStatusEntity;
import org.apache.airavata.experiment.model.ExperimentStatusPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExperimentStatusRepository
        extends AbstractRepository<ExperimentStatus, ExperimentStatusEntity, ExperimentStatusPK> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusRepository.class);

    private final ExperimentRepository experimentRepository;

    public ExperimentStatusRepository(ExperimentRepository experimentRepository) {
        super(ExperimentStatus.class, ExperimentStatusEntity.class);
        this.experimentRepository = experimentRepository;
    }

    @Override
    protected ExperimentStatus toModel(ExperimentStatusEntity entity) {
        return ExperimentMapper.INSTANCE.experimentStatusToModel(entity);
    }

    @Override
    protected ExperimentStatusEntity toEntity(ExperimentStatus model) {
        return ExperimentMapper.INSTANCE.experimentStatusToEntity(model);
    }

    protected String saveExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws Exception {

        if (experimentStatus.statusId().isEmpty()) {

            ExperimentStatus currentExperimentStatus = getExperimentStatus(experimentId);
            if (currentExperimentStatus == null || currentExperimentStatus.state() != experimentStatus.state()) {
                experimentStatus = experimentStatus.toBuilder()
                        .setStatusId(AiravataUtils.getId("EXPERIMENT_STATE"))
                        .build();
            } else {
                // Update the existing current status if experimentStatus has no status id and
                // the same state
                experimentStatus = experimentStatus.toBuilder()
                        .setStatusId(currentExperimentStatus.statusId())
                        .build();
            }
        }
        ExperimentStatusEntity experimentStatusEntity = ExperimentMapper.INSTANCE
                .experimentStatusToEntity(experimentStatus);

        if (experimentStatusEntity.getExperimentId() == null) {
            logger.debug("Setting the ExperimentStatusEntity's ExperimentId");
            experimentStatusEntity.setExperimentId(experimentId);
        }

        execute(entityManager -> entityManager.merge(experimentStatusEntity));
        return experimentStatusEntity.getStatusId();
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws Exception {

        if (experimentStatus.statusId().isEmpty()) {
            logger.debug("Setting the ExperimentStatus's StatusId");
            experimentStatus = experimentStatus.toBuilder()
                    .setStatusId(AiravataUtils.getId("EXPERIMENT_STATE"))
                    .build();
        }
        experimentStatus = experimentStatus.toBuilder()
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();

        return saveExperimentStatus(experimentStatus, experimentId);
    }

    public String updateExperimentStatus(ExperimentStatus updatedExperimentStatus, String experimentId)
            throws Exception {
        return saveExperimentStatus(updatedExperimentStatus, experimentId);
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws Exception {
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        List<ExperimentStatus> experimentStatusList = experimentModel.experimentStatus();

        if (experimentStatusList.size() == 0) {
            logger.debug("ExperimentStatus list is empty");
            return null;
        } else {
            ExperimentStatus latestExperimentStatus = experimentStatusList.get(0);

            for (int i = 1; i < experimentStatusList.size(); i++) {
                Timestamp timeOfStateChange = new Timestamp(experimentStatusList.get(i).timeOfStateChange());

                if (timeOfStateChange != null) {

                    if (timeOfStateChange.after(new Timestamp(latestExperimentStatus.timeOfStateChange()))
                            || (timeOfStateChange.equals(latestExperimentStatus.timeOfStateChange())
                                    && experimentStatusList.get(i).state()
                                            == ExperimentState.EXPERIMENT_STATE_COMPLETED)
                            || (timeOfStateChange.equals(latestExperimentStatus.timeOfStateChange())
                                    && experimentStatusList.get(i).state()
                                            == ExperimentState.EXPERIMENT_STATE_FAILED)
                            || (timeOfStateChange.equals(latestExperimentStatus.timeOfStateChange())
                                    && experimentStatusList.get(i).state()
                                            == ExperimentState.EXPERIMENT_STATE_CANCELED)) {
                        latestExperimentStatus = experimentStatusList.get(i);
                    }
                }
            }

            return latestExperimentStatus;
        }
    }
}
