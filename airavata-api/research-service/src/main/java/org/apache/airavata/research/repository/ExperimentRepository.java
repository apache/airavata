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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.ExecutionDataAccess;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.interfaces.ResultOrderType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.research.mapper.ResearchMapper;
import org.apache.airavata.research.model.ExperimentEntity;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExperimentRepository extends AbstractRepository<ExperimentModel, ExperimentEntity, String> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    private ExecutionDataAccess executionDataAccess;

    public ExperimentRepository() {
        super(ExperimentModel.class, ExperimentEntity.class);
    }

    public void setExecutionDataAccess(ExecutionDataAccess executionDataAccess) {
        this.executionDataAccess = executionDataAccess;
    }

    @Override
    protected ExperimentModel toModel(ExperimentEntity entity) {
        return ResearchMapper.INSTANCE.experimentToModel(entity);
    }

    @Override
    protected ExperimentEntity toEntity(ExperimentModel model) {
        return ResearchMapper.INSTANCE.experimentToEntity(model);
    }

    protected String saveExperimentModelData(ExperimentModel experimentModel) throws RegistryException {
        ExperimentEntity experimentEntity = saveExperiment(experimentModel);
        return experimentEntity.getExperimentId();
    }

    protected ExperimentEntity saveExperiment(ExperimentModel experimentModel) throws RegistryException {
        String experimentId = experimentModel.getExperimentId();

        if (!experimentModel.getExperimentStatusList().isEmpty()) {
            logger.debug("Populating the status id of ExperimentStatus objects for the Experiment");
            ExperimentModel.Builder expBuilder = experimentModel.toBuilder().clearExperimentStatus();
            for (ExperimentStatus es : experimentModel.getExperimentStatusList()) {
                if (es.getStatusId().isEmpty()) {
                    es = es.toBuilder()
                            .setStatusId(AiravataUtils.getId("EXPERIMENT_STATE"))
                            .build();
                }
                expBuilder.addExperimentStatus(es);
            }
            experimentModel = expBuilder.build();
        }

        if (!experimentModel.getProcessesList().isEmpty()) {
            logger.debug("Populating the Process objects' Experiment ID for the Experiment");
            ExperimentModel.Builder expBuilder2 = experimentModel.toBuilder().clearProcesses();
            for (ProcessModel pm : experimentModel.getProcessesList()) {
                expBuilder2.addProcesses(
                        pm.toBuilder().setExperimentId(experimentId).build());
            }
            experimentModel = expBuilder2.build();
        }

        if (!isExperimentExist(experimentId)) {
            logger.debug("Populating creation time if it doesn't already exist");
            experimentModel = experimentModel.toBuilder()
                    .setCreationTime(System.currentTimeMillis())
                    .build();
        } else {
            // creation_time is immutable after create. This update path persists the full model, and
            // callers may resend it with creation_time cleared — a clone, for instance, re-sends the
            // experiment via update_experiment with an unset creation_time. longToTimestamp maps 0 to a
            // NULL column that reads back as epoch 1970 ("56 years ago"). Carry the stored value forward.
            long existingCreationTime = getExperiment(experimentId).getCreationTime();
            if (existingCreationTime > 0) {
                experimentModel = experimentModel.toBuilder()
                        .setCreationTime(existingCreationTime)
                        .build();
            }
        }
        ExperimentEntity experimentEntity = ResearchMapper.INSTANCE.experimentToEntity(experimentModel);

        if (experimentEntity.getExperimentInputs() != null) {
            logger.debug("Populating the entity ID of ExperimentInput params for the Experiment");
            experimentEntity.getExperimentInputs().forEach(param -> {
                param.setDirection("INPUT");
                param.setEntityId(experimentId);
            });
        }

        if (experimentEntity.getExperimentOutputs() != null) {
            logger.debug("Populating the entity ID of ExperimentOutput params for the Experiment");
            experimentEntity.getExperimentOutputs().forEach(param -> {
                param.setDirection("OUTPUT");
                param.setEntityId(experimentId);
            });
        }

        if (experimentEntity.getExperimentStatus() != null) {
            logger.debug("Populating the Primary Key of ExperimentStatus objects for the Experiment");
            experimentEntity
                    .getExperimentStatus()
                    .forEach(experimentStatusEntity -> experimentStatusEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getErrors() != null) {
            logger.debug("Populating the Primary Key of ExperimentError objects for the Experiment");
            experimentEntity
                    .getErrors()
                    .forEach(experimentErrorEntity -> experimentErrorEntity.setExperimentId(experimentId));
        }

        // Persist the experiment row FIRST so the EXPERIMENT_ID exists before any child row
        // (cascaded inputs/outputs/status/errors, and the user_configuration_data below) references it.
        ExperimentEntity savedEntity = execute(entityManager -> entityManager.merge(experimentEntity));

        // UserConfigurationData and Processes are managed by their own entities (in orchestration-service).
        // Persist UserConfigurationData via ExecutionDataAccess AFTER the experiment row exists, since
        // user_configuration_data.EXPERIMENT_ID is a FK to experiment.EXPERIMENT_ID.
        if (experimentModel.hasUserConfigurationData()) {
            executionDataAccess.saveUserConfigurationData(experimentModel.getUserConfigurationData(), experimentId);
        }
        return savedEntity;
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {

        ExperimentStatus experimentStatus = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_CREATED)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();
        // experiment_id is a short, human-readable, URL-safe code (e.g. EXP-7QK2F9MX). The experiment
        // name is kept separately in experiment_name. Regenerate on the rare collision since it's the PK.
        String experimentId;
        do {
            experimentId = AiravataUtils.getReadableId("EXP");
        } while (isExperimentExist(experimentId));
        experimentModel = experimentModel.toBuilder()
                .addExperimentStatus(experimentStatus)
                .setExperimentId(experimentId)
                .build();

        return saveExperimentModelData(experimentModel);
    }

    public void updateExperiment(ExperimentModel updatedExperimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(updatedExperimentModel);
    }

    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        ExperimentModel baseModel = get(experimentId);
        ExperimentModel.Builder builder = baseModel.toBuilder();

        // Load UserConfigurationData and Processes via ExecutionDataAccess (avoids cross-module entity imports).
        // executionDataAccess is wired only on the Spring-managed instance; ad-hoc `new ExperimentRepository()`
        // callers (e.g. ExperimentStatusRepository, which only needs the status already present on the base model)
        // get a best-effort load without it rather than an NPE.
        if (executionDataAccess != null) {
            UserConfigurationDataModel ucdModel = executionDataAccess.getUserConfigurationData(experimentId);
            if (ucdModel != null) {
                builder.setUserConfigurationData(ucdModel);
            }
            List<ProcessModel> processes = executionDataAccess.getProcessesForExperiment(experimentId);
            for (ProcessModel pm : processes) {
                builder.addProcesses(pm);
            }
        }

        return builder.build();
    }

    public String addUserConfigurationData(UserConfigurationDataModel userConfigurationDataModel, String experimentId)
            throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel = experimentModel.toBuilder()
                .setUserConfigurationData(userConfigurationDataModel)
                .build();
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateUserConfigurationData(
            UserConfigurationDataModel updatedUserConfigurationDataModel, String experimentId)
            throws RegistryException {
        return addUserConfigurationData(updatedUserConfigurationDataModel, experimentId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getUserConfigurationData();
    }

    public List<ExperimentModel> getExperimentList(
            String gatewayId,
            String fieldName,
            Object value,
            int limit,
            int offset,
            Object orderByIdentifier,
            ResultOrderType resultOrderType)
            throws RegistryException {
        List<ExperimentModel> experimentModelList;

        if (fieldName.equals(DBConstants.Experiment.USER_NAME)) {
            logger.debug("Search criteria is Username");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.USER_NAME, value);
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_USER, limit, offset, queryParameters);
        } else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.PROJECT_ID, value);
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_PROJECT_ID, limit, offset, queryParameters);
        } else {
            logger.error("Unsupported field name for Experiment module.");
            throw new IllegalArgumentException("Unsupported field name for Experiment module.");
        }

        return experimentModelList;
    }

    public boolean isExperimentExist(String experimentId) throws RegistryException {
        return isExists(experimentId);
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        delete(experimentId);
    }
}
