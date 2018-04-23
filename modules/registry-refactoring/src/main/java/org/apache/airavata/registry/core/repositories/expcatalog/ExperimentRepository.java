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
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.registry.core.entities.expcatalog.*;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.*;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

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
        if (experimentModel.getExperimentId() == null || experimentModel.getExperimentId().equals("DO_NOT_SET_AT_CLIENTS")) {
            logger.debug("Setting the Experiment's ExperimentId");
            experimentModel.setExperimentId(ExpCatalogUtils.getID(experimentModel.getExperimentName()));
        }

        String experimentId = experimentModel.getExperimentId();
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
            experimentEntity.getExperimentStatus().forEach(experimentStatusEntity -> { experimentStatusEntity.setExperimentId(experimentId);
                if (experimentStatusEntity.getStatusId() == null) {
                    experimentStatusEntity.setStatusId(ExpCatalogUtils.getID("STATUS"));
                }
            });
        }

        if (experimentEntity.getErrors() != null) {
            logger.debug("Populating the Primary Key of ExperimentError objects for the Experiment");
            experimentEntity.getErrors().forEach(experimentErrorEntity -> experimentErrorEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getProcesses() != null) {
            logger.debug("Populating the Process objects' Experiment ID for the Experiment");
            experimentEntity.getProcesses().forEach(processEntity -> processEntity.setExperimentId(experimentId));
        }

        if (!isExperimentExist(experimentId)) {
            logger.debug("Checking if the Experiment already exists");
            experimentEntity.setCreationTime(new Timestamp((System.currentTimeMillis())));
        }

        return execute(entityManager -> entityManager.merge(experimentEntity));
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        return saveExperimentModelData(experimentModel);
    }

    public void updateExperiment(ExperimentModel updatedExperimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(updatedExperimentModel);
    }

    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        return get(experimentId);
    }

    public String addExperimentInputs(List<InputDataObjectType> experimentInputs, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<InputDataObjectType> inputDataObjectTypeList = experimentModel.getExperimentInputs();

        for (InputDataObjectType input : experimentInputs) {

            if (inputDataObjectTypeList != null && !inputDataObjectTypeList.contains(input)) {
                logger.debug("Adding the ExperimentInput to the list");
                inputDataObjectTypeList.add(input);
                experimentModel.setExperimentInputs(inputDataObjectTypeList);
                updateExperiment(experimentModel, experimentId);
            }

        }

        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> updatedExperimentInputs, String experimentId) throws RegistryException {
        addExperimentInputs(updatedExperimentInputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentInputs();
    }

    public String addExperimentOutputs(List<OutputDataObjectType> experimentOutputs, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<OutputDataObjectType> outputDataObjectTypeList = experimentModel.getExperimentOutputs();

        for (OutputDataObjectType output : experimentOutputs) {

            if (outputDataObjectTypeList != null && !outputDataObjectTypeList.contains(output)) {
                logger.debug("Adding the ExperimentOutput to the list");
                outputDataObjectTypeList.add(output);
                experimentModel.setExperimentOutputs(outputDataObjectTypeList);
                updateExperiment(experimentModel, experimentId);
            }

        }

        return experimentId;
    }

    public void updateExperimentOutputs(List<OutputDataObjectType> updatedExperimentOutputs, String experimentId) throws RegistryException {
        addExperimentOutputs(updatedExperimentOutputs, experimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentOutputs();
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<ExperimentStatus> experimentStatusList = experimentModel.getExperimentStatus();

        if (experimentStatusList == null) {
            logger.debug("Adding the first ExperimentStatus to the list");
            experimentModel.setExperimentStatus(Arrays.asList(experimentStatus));
        }

        else if (!experimentStatusList.contains(experimentStatus)) {
            logger.debug("Adding the ExperimentStatus to the list");
            experimentStatusList.add(experimentStatus);
            experimentModel.setExperimentStatus(experimentStatusList);
        }

        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateExperimentStatus(ExperimentStatus updatedExperimentStatus, String experimentId) throws RegistryException {
        return addExperimentStatus(updatedExperimentStatus, experimentId);
    }

    public List<ExperimentStatus> getExperimentStatus(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getExperimentStatus();
    }

    public String addExperimentError(ErrorModel experimentError, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        List<ErrorModel> errorModelList = experimentModel.getErrors();

        if (errorModelList == null) {
            logger.debug("Adding the first ExperimentError to the list");
            experimentModel.setErrors(Arrays.asList(experimentError));
        }

        else if (!errorModelList.contains(experimentError)) {
            logger.debug("Adding the ExperimentError to the list");
            errorModelList.add(experimentError);
            experimentModel.setErrors(errorModelList);
        }

        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateExperimentError(ErrorModel updatedExperimentError, String experimentId) throws RegistryException {
        return addExperimentError(updatedExperimentError, experimentId);
    }

    public List<ErrorModel> getExperimentErrors(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getErrors();
    }

    /*public void updateExperimentField(String experimentId, String fieldName, Object value) throws RegistryException {

    }*/

    /*public void updateUserConfigurationDataField(String experimentId, String fieldName, Object value) throws RegistryException {

    }*/

    /*public void updateComputeResourceScheduling(ComputationalResourceSchedulingModel value, String experimentId) throws RegistryException {

    }*/

    public List<ExperimentModel> getExperimentList(String fieldName, Object value) throws RegistryException {
        return getExperimentList(fieldName, value, -1, 0, null, null);
    }

    public List<ExperimentModel> getExperimentList(String fieldName, Object value, int limit, int offset,
                                                   Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentModel> experimentModelList;

        if (fieldName.equals(DBConstants.Experiment.USER_NAME)) {
            logger.debug("Search criteria is Username");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.USER_NAME, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_USER, limit, offset, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.PROJECT_ID, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_PROJECT_ID, limit, offset, queryParameters);
        }

        else if (fieldName.equals(DBConstants.Experiment.GATEWAY_ID)) {
            logger.debug("Search criteria is GatewayId");
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, value);
            experimentModelList = select(QueryConstants.GET_EXPERIMENTS_FOR_GATEWAY_ID, limit, offset, queryParameters);
        }

        else {
            logger.error("Unsupported field name for Experiment module.");
            throw new IllegalArgumentException("Unsupported field name for Experiment module.");
        }

        return experimentModelList;
    }

    public List<String> getExperimentIDs(String fieldName, Object value) throws RegistryException {
        List<String> experimentIds = new ArrayList<>();
        List<ExperimentModel> experimentModelList = getExperimentList(fieldName, value);
        for (ExperimentModel experimentModel : experimentModelList) {
            experimentIds.add(experimentModel.getExperimentId());
        }
        return experimentIds;
    }

    public boolean isExperimentExist(String experimentId) throws RegistryException {
        return isExists(experimentId);
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        delete(experimentId);
    }

    public boolean isValidStatusTransition(Object object1, Object object2) {

        if (object1 instanceof ExperimentState && object2 instanceof ExperimentState) {
            ExperimentState oldState = (ExperimentState) object1;
            ExperimentState nextState = (ExperimentState) object2;

            if (nextState == null) {
                return false;
            }

            switch (oldState) {
                case CREATED:
                    return true;
                case VALIDATED:
                    return nextState != ExperimentState.CREATED;
                case SCHEDULED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED;
                case LAUNCHED:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED;
                case EXECUTING:
                    return nextState != ExperimentState.CREATED
                            || nextState != ExperimentState.VALIDATED
                            || nextState != ExperimentState.SCHEDULED
                            || nextState != ExperimentState.LAUNCHED;

                case CANCELING:
                    return nextState == ExperimentState.CANCELING
                            || nextState == ExperimentState.CANCELED
                            || nextState == ExperimentState.COMPLETED
                            || nextState == ExperimentState.FAILED;
                case CANCELED:
                    return nextState == ExperimentState.CANCELED;
                case COMPLETED:
                    return nextState == ExperimentState.COMPLETED;
                case FAILED:
                    return nextState == ExperimentState.FAILED;
                default:
                    return false;
            }

        }

        else if (object1 instanceof ProcessState && object2 instanceof ProcessState) {
            ProcessState oldState = (ProcessState) object1;
            ProcessState nextState = (ProcessState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        }

        else if (object1 instanceof TaskState && object2 instanceof TaskState) {
            TaskState oldState = (TaskState) object1;
            TaskState nextState = (TaskState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                case CREATED:
//                    return true;
//                default:
//                    return false;
//            }
        }

        else if (object1 instanceof JobState && object2 instanceof JobState) {
            JobState oldState = (JobState) object1;
            JobState nextState = (JobState) object2;

            if (nextState == null) {
                return false;
            }

            return true;
//            TODO - need the state machine to complete these data
//            switch (oldState) {
//                default:
//                    return false;
//            }
        }

        return false;
    }

    public String getStatusID(String parentId) {
        String status = parentId.replaceAll("\\s", "");
        return status + "_" + UUID.randomUUID();
    }

    public String getErrorID(String parentId) {
        String error = parentId.replaceAll("\\s", "");
        return error + "_" + UUID.randomUUID();
    }

}