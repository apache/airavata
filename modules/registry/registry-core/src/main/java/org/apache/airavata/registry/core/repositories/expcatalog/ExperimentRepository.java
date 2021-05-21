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
import org.apache.airavata.model.commons.airavata_commonsConstants;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentRepository extends ExpCatAbstractRepository<ExperimentModel, ExperimentEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    public ExperimentRepository() {
        super(ExperimentModel.class, ExperimentEntity.class);
    }

    protected String saveExperimentModelData(ExperimentModel experimentModel) throws RegistryException {
        ExperimentEntity experimentEntity = saveExperiment(experimentModel);
        return experimentEntity.getExperimentId();
    }

    protected ExperimentEntity saveExperiment(ExperimentModel experimentModel) throws RegistryException {
        String experimentId = experimentModel.getExperimentId();

        if (experimentModel.getExperimentStatus() != null) {
            logger.debug("Populating the status id of ExperimentStatus objects for the Experiment");
            experimentModel.getExperimentStatus().forEach(experimentStatusEntity -> {
                if (experimentStatusEntity.getStatusId() == null) {
                    experimentStatusEntity.setStatusId(AiravataUtils.getId("EXPERIMENT_STATE"));
                }
            });
        }

        if (experimentModel.getProcesses() != null) {
            logger.debug("Populating the Process objects' Experiment ID for the Experiment");
            experimentModel.getProcesses().forEach(processModel -> processModel.setExperimentId(experimentId));
        }

        if (!isExperimentExist(experimentId)) {
            logger.debug("Populating creation time if it doesn't already exist");
            experimentModel.setCreationTime(System.currentTimeMillis());
        }


        Mapper mapper = ObjectMapperSingleton.getInstance();
        ExperimentEntity experimentEntity = mapper.map(experimentModel, ExperimentEntity.class);

        if (experimentEntity.getUserConfigurationData() != null) {
            logger.debug("Populating the Primary Key of UserConfigurationData object for the Experiment");
            experimentEntity.getUserConfigurationData().setExperimentId(experimentId);
        }

        if (experimentEntity.getExperimentInputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentInput objects for the Experiment");
            experimentEntity.getExperimentInputs().forEach(experimentInputEntity -> experimentInputEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getExperimentOutputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentOutput objects for the Experiment");
            experimentEntity.getExperimentOutputs().forEach(experimentOutputEntity -> experimentOutputEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getExperimentStatus() != null) {
            logger.debug("Populating the Primary Key of ExperimentStatus objects for the Experiment");
            experimentEntity.getExperimentStatus().forEach(experimentStatusEntity -> experimentStatusEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getErrors() != null) {
            logger.debug("Populating the Primary Key of ExperimentError objects for the Experiment");
            experimentEntity.getErrors().forEach(experimentErrorEntity -> experimentErrorEntity.setExperimentId(experimentId));
        }

        return execute(entityManager -> entityManager.merge(experimentEntity));
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {

        ExperimentStatus experimentStatus = new ExperimentStatus();
        experimentStatus.setState(ExperimentState.CREATED);
        experimentStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        experimentModel.addToExperimentStatus(experimentStatus);
        String expName = experimentModel.getExperimentName();
        // This is to avoid overflow of experiment id size. Total experiment id length is <= 50 + UUID
        experimentModel.setExperimentId(AiravataUtils.getId(expName.substring(0, Math.min(expName.length(), 50))));

        return saveExperimentModelData(experimentModel);
    }

    public void updateExperiment(ExperimentModel updatedExperimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(updatedExperimentModel);
    }

    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        return get(experimentId);
    }

    public String addUserConfigurationData(UserConfigurationDataModel userConfigurationDataModel, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel.setUserConfigurationData(userConfigurationDataModel);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateUserConfigurationData(UserConfigurationDataModel updatedUserConfigurationDataModel, String experimentId) throws RegistryException {
        return addUserConfigurationData(updatedUserConfigurationDataModel, experimentId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getUserConfigurationData();
    }

    public List<ExperimentModel> getExperimentList(String gatewayId, String fieldName, Object value, int limit, int offset,
                                                   Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentModel> experimentModelList;

        if (fieldName.equals(DBConstants.Experiment.USER_NAME)) {
            logger.debug("Search criteria is Username");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.USER_NAME, value);
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_USER, limit, offset, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.PROJECT_ID, value);
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_PROJECT_ID, limit, offset, queryParameters);
        }

        else {
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
