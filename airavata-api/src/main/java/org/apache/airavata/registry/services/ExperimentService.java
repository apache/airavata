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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ComputationalResourceSchedulingEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentInputEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentOutputEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentStatusEntity;
import org.apache.airavata.registry.entities.expcatalog.ProcessEntity;
import org.apache.airavata.registry.entities.expcatalog.UserConfigurationDataEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ExperimentMapper;
import org.apache.airavata.registry.model.ResultOrderType;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.utils.DBConstants;
import org.apache.airavata.registry.utils.EntityMergeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ExperimentService {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    private final ExperimentRepository experimentRepository;
    private final ProcessService processService;
    private final ExperimentMapper experimentMapper;

    public ExperimentService(
            ExperimentRepository experimentRepository,
            ProcessService processService,
            ExperimentMapper experimentMapper) {
        this.experimentRepository = experimentRepository;
        this.processService = processService;
        this.experimentMapper = experimentMapper;
    }

    @Transactional("expCatalogTransactionManager")
    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        ExperimentStatus experimentStatus = new ExperimentStatus();
        experimentStatus.setState(ExperimentState.CREATED);
        experimentStatus.setTimeOfStateChange(
                AiravataUtils.getCurrentTimestamp().getTime());
        if (experimentModel.getExperimentStatus() == null) {
            experimentModel.setExperimentStatus(new java.util.ArrayList<>());
        }
        experimentModel.getExperimentStatus().add(experimentStatus);
        String expName = experimentModel.getExperimentName();
        // This is to avoid overflow of experiment id size. Total experiment id length is <= 50 + UUID
        experimentModel.setExperimentId(AiravataUtils.getId(expName.substring(0, Math.min(expName.length(), 50))));

        return saveExperimentModelData(experimentModel);
    }

    @Transactional("expCatalogTransactionManager")
    public void updateExperiment(ExperimentModel updatedExperimentModel, String experimentId) throws RegistryException {
        saveExperimentModelData(updatedExperimentModel);
    }

    @Transactional(readOnly = true)
    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        ExperimentEntity entity = experimentRepository.findById(experimentId).orElse(null);
        if (entity == null) return null;

        ExperimentModel model = experimentMapper.toModel(entity);

        // Manually convert emailAddresses from String (CSV) to List<String>
        String emailAddressesStr = entity.getEmailAddresses();
        if (emailAddressesStr != null && !emailAddressesStr.isEmpty()) {
            model.setEmailAddresses(java.util.Arrays.asList(emailAddressesStr.split(",")));
        } else {
            model.setEmailAddresses(new java.util.ArrayList<>());
        }
        // Initialize empty lists if null to prevent NullPointerException
        if (model.getExperimentInputs() == null) {
            model.setExperimentInputs(new java.util.ArrayList<>());
        }
        if (model.getExperimentOutputs() == null) {
            model.setExperimentOutputs(new java.util.ArrayList<>());
        }
        if (model.getErrors() == null) {
            model.setErrors(new java.util.ArrayList<>());
        }
        if (model.getProcesses() == null) {
            model.setProcesses(new java.util.ArrayList<>());
        }
        // Manually map computationalResourceScheduling from UserConfigurationDataEntity fields
        if (model.getUserConfigurationData() != null && entity.getUserConfigurationData() != null) {
            UserConfigurationDataEntity ucdEntity = entity.getUserConfigurationData();
            // Always create ComputationalResourceSchedulingModel from entity fields
            // (entity stores scheduling fields directly, model has them in a nested object)
            ComputationalResourceSchedulingModel crsModel = new ComputationalResourceSchedulingModel();
            crsModel.setResourceHostId(ucdEntity.getResourceHostId());
            crsModel.setTotalCPUCount(ucdEntity.getTotalCPUCount());
            crsModel.setNodeCount(ucdEntity.getNodeCount());
            crsModel.setNumberOfThreads(ucdEntity.getNumberOfThreads());
            crsModel.setQueueName(ucdEntity.getQueueName());
            crsModel.setWallTimeLimit(ucdEntity.getWallTimeLimit());
            crsModel.setTotalPhysicalMemory(ucdEntity.getTotalPhysicalMemory());
            crsModel.setStaticWorkingDir(ucdEntity.getStaticWorkingDir());
            crsModel.setOverrideLoginUserName(ucdEntity.getOverrideLoginUserName());
            crsModel.setOverrideScratchLocation(ucdEntity.getOverrideScratchLocation());
            crsModel.setOverrideAllocationProjectNumber(ucdEntity.getOverrideAllocationProjectNumber());
            model.getUserConfigurationData().setComputationalResourceScheduling(crsModel);
        }
        return model;
    }

    @Transactional("expCatalogTransactionManager")
    public String addUserConfigurationData(UserConfigurationDataModel userConfigurationDataModel, String experimentId)
            throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        experimentModel.setUserConfigurationData(userConfigurationDataModel);
        updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    @Transactional("expCatalogTransactionManager")
    public String updateUserConfigurationData(
            UserConfigurationDataModel updatedUserConfigurationDataModel, String experimentId)
            throws RegistryException {
        return addUserConfigurationData(updatedUserConfigurationDataModel, experimentId);
    }

    @Transactional(value = "expCatalogTransactionManager", readOnly = true)
    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = getExperiment(experimentId);
        return experimentModel.getUserConfigurationData();
    }

    @Transactional(value = "expCatalogTransactionManager", readOnly = true)
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
            List<ExperimentEntity> entities =
                    experimentRepository.findByGatewayIdAndUserName(gatewayId, (String) value);
            experimentModelList = experimentMapper.toModelList(entities);
        } else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            List<ExperimentEntity> entities =
                    experimentRepository.findByGatewayIdAndProjectId(gatewayId, (String) value);
            experimentModelList = experimentMapper.toModelList(entities);
        } else {
            logger.error("Unsupported field name for Experiment module.");
            throw new IllegalArgumentException("Unsupported field name for Experiment module.");
        }

        return experimentModelList;
    }

    @Transactional(value = "expCatalogTransactionManager", readOnly = true)
    public boolean isExperimentExist(String experimentId) throws RegistryException {
        return experimentRepository.existsById(experimentId);
    }

    @Transactional("expCatalogTransactionManager")
    public void removeExperiment(String experimentId) throws RegistryException {
        experimentRepository.deleteById(experimentId);
    }

    private String saveExperimentModelData(ExperimentModel experimentModel) throws RegistryException {
        ExperimentEntity experimentEntity = saveExperiment(experimentModel);
        return experimentEntity.getExperimentId();
    }

    private ExperimentEntity saveExperiment(ExperimentModel experimentModel) throws RegistryException {
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

        ExperimentEntity existingEntity =
                experimentRepository.findById(experimentId).orElse(null);
        ExperimentEntity experimentEntity;

        if (existingEntity != null) {
            // Map model to new entity to get the desired state
            ExperimentEntity newEntity = experimentMapper.toEntity(experimentModel);
            // Copy simple fields to existing entity manually (MapStruct doesn't support updating existing entities)
            existingEntity.setProjectId(experimentModel.getProjectId());
            existingEntity.setGatewayId(experimentModel.getGatewayId());
            existingEntity.setExperimentType(experimentModel.getExperimentType());
            existingEntity.setUserName(experimentModel.getUserName());
            existingEntity.setExperimentName(experimentModel.getExperimentName());
            existingEntity.setCreationTime(
                    experimentModel.getCreationTime() > 0
                            ? new java.sql.Timestamp(experimentModel.getCreationTime())
                            : null);
            existingEntity.setDescription(experimentModel.getDescription());
            existingEntity.setExecutionId(experimentModel.getExecutionId());
            existingEntity.setGatewayExecutionId(experimentModel.getGatewayExecutionId());
            existingEntity.setGatewayInstanceId(experimentModel.getGatewayInstanceId());
            existingEntity.setEnableEmailNotification(experimentModel.getEnableEmailNotification());

            // Manually set emailAddresses on both entities (excluded from mapping)
            java.util.List<String> emailAddressesList = experimentModel.getEmailAddresses();
            String emailAddressesStr = (emailAddressesList != null && !emailAddressesList.isEmpty())
                    ? String.join(",", emailAddressesList)
                    : null;
            newEntity.setEmailAddresses(emailAddressesStr);
            existingEntity.setEmailAddresses(emailAddressesStr);

            // Properly merge lists using EntityMergeHelper (handles duplicates gracefully)
            EntityMergeHelper.mergeLists(
                    existingEntity.getExperimentStatus(),
                    newEntity.getExperimentStatus(),
                    ExperimentStatusEntity::getStatusId);
            EntityMergeHelper.mergeLists(
                    existingEntity.getExperimentInputs(),
                    newEntity.getExperimentInputs(),
                    ExperimentInputEntity::getName);
            EntityMergeHelper.mergeLists(
                    existingEntity.getExperimentOutputs(),
                    newEntity.getExperimentOutputs(),
                    ExperimentOutputEntity::getName);
            EntityMergeHelper.mergeLists(
                    existingEntity.getErrors(), newEntity.getErrors(), ExperimentErrorEntity::getErrorId);
            EntityMergeHelper.mergeLists(
                    existingEntity.getProcesses(), newEntity.getProcesses(), ProcessEntity::getProcessId);

            // Merge UserConfigurationData
            if (newEntity.getUserConfigurationData() != null) {
                if (existingEntity.getUserConfigurationData() == null) {
                    // Create new UserConfigurationData if it doesn't exist
                    existingEntity.setUserConfigurationData(newEntity.getUserConfigurationData());
                } else {
                    // Update existing UserConfigurationData
                    UserConfigurationDataEntity existingUcd = existingEntity.getUserConfigurationData();
                    UserConfigurationDataEntity newUcd = newEntity.getUserConfigurationData();
                    // Copy all fields from new to existing
                    existingUcd.setAiravataAutoSchedule(newUcd.isAiravataAutoSchedule());
                    existingUcd.setOverrideManualScheduledParams(newUcd.isOverrideManualScheduledParams());
                    existingUcd.setShareExperimentPublicly(newUcd.isShareExperimentPublicly());
                    existingUcd.setThrottleResources(newUcd.isThrottleResources());
                    existingUcd.setUserDN(newUcd.getUserDN());
                    existingUcd.setGenerateCert(newUcd.isGenerateCert());
                    existingUcd.setInputStorageResourceId(newUcd.getInputStorageResourceId());
                    existingUcd.setOutputStorageResourceId(newUcd.getOutputStorageResourceId());
                    existingUcd.setExperimentDataDir(newUcd.getExperimentDataDir());
                    existingUcd.setGroupResourceProfileId(newUcd.getGroupResourceProfileId());
                    existingUcd.setUseUserCRPref(newUcd.isUseUserCRPref());
                    // Copy scheduling fields (these are set from computationalResourceScheduling below)
                    existingUcd.setResourceHostId(newUcd.getResourceHostId());
                    existingUcd.setTotalCPUCount(newUcd.getTotalCPUCount());
                    existingUcd.setNodeCount(newUcd.getNodeCount());
                    existingUcd.setNumberOfThreads(newUcd.getNumberOfThreads());
                    existingUcd.setQueueName(newUcd.getQueueName());
                    existingUcd.setWallTimeLimit(newUcd.getWallTimeLimit());
                    existingUcd.setTotalPhysicalMemory(newUcd.getTotalPhysicalMemory());
                    existingUcd.setStaticWorkingDir(newUcd.getStaticWorkingDir());
                    existingUcd.setOverrideLoginUserName(newUcd.getOverrideLoginUserName());
                    existingUcd.setOverrideScratchLocation(newUcd.getOverrideScratchLocation());
                    existingUcd.setOverrideAllocationProjectNumber(newUcd.getOverrideAllocationProjectNumber());
                }
            } else if (experimentModel.getUserConfigurationData() == null
                    && existingEntity.getUserConfigurationData() != null) {
                // If model has no UserConfigurationData but entity does, remove it
                existingEntity.setUserConfigurationData(null);
            }

            experimentEntity = existingEntity;
        } else {
            experimentEntity = experimentMapper.toEntity(experimentModel);
            // Manually convert emailAddresses from List<String> to String (CSV) - excluded from mapping
            java.util.List<String> emailAddressesList = experimentModel.getEmailAddresses();
            String emailAddressesStr = (emailAddressesList != null && !emailAddressesList.isEmpty())
                    ? String.join(",", emailAddressesList)
                    : null;
            experimentEntity.setEmailAddresses(emailAddressesStr);
        }

        if (experimentEntity.getUserConfigurationData() != null) {
            logger.debug("Populating the Primary Key of UserConfigurationData object for the Experiment");
            UserConfigurationDataEntity ucdEntity = experimentEntity.getUserConfigurationData();
            ucdEntity.setExperimentId(experimentId);
            // Copy fields from ComputationalResourceSchedulingModel to entity fields if present
            if (experimentModel.getUserConfigurationData() != null
                    && experimentModel.getUserConfigurationData().getComputationalResourceScheduling() != null) {
                ComputationalResourceSchedulingModel crsModel =
                        experimentModel.getUserConfigurationData().getComputationalResourceScheduling();
                ucdEntity.setResourceHostId(crsModel.getResourceHostId());
                ucdEntity.setTotalCPUCount(crsModel.getTotalCPUCount());
                ucdEntity.setNodeCount(crsModel.getNodeCount());
                ucdEntity.setNumberOfThreads(crsModel.getNumberOfThreads());
                ucdEntity.setQueueName(crsModel.getQueueName());
                ucdEntity.setWallTimeLimit(crsModel.getWallTimeLimit());
                ucdEntity.setTotalPhysicalMemory(crsModel.getTotalPhysicalMemory());
                ucdEntity.setStaticWorkingDir(crsModel.getStaticWorkingDir());
                ucdEntity.setOverrideLoginUserName(crsModel.getOverrideLoginUserName());
                ucdEntity.setOverrideScratchLocation(crsModel.getOverrideScratchLocation());
                ucdEntity.setOverrideAllocationProjectNumber(crsModel.getOverrideAllocationProjectNumber());
            }
        }

        if (experimentEntity.getUserConfigurationData() != null
                && experimentEntity.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList() != null) {
            logger.debug(
                    "Populating the Primary Key of UserConfigurationData.ComputationalResourceSchedulingEntities object for the Experiment");
            for (ComputationalResourceSchedulingEntity entity :
                    experimentEntity.getUserConfigurationData().getAutoScheduledCompResourceSchedulingList()) {
                entity.setExperimentId(experimentId);
            }
        }

        if (experimentEntity.getExperimentInputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentInput objects for the Experiment");
            experimentEntity
                    .getExperimentInputs()
                    .forEach(experimentInputEntity -> experimentInputEntity.setExperimentId(experimentId));
        }

        if (experimentEntity.getExperimentOutputs() != null) {
            logger.debug("Populating the Primary Key of ExperimentOutput objects for the Experiment");
            experimentEntity
                    .getExperimentOutputs()
                    .forEach(experimentOutputEntity -> experimentOutputEntity.setExperimentId(experimentId));
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

        if (experimentEntity.getProcesses() != null) {
            logger.debug("Populating the Process objects for the Experiment");
            experimentEntity.getProcesses().forEach(processEntity -> {
                processEntity.setExperimentId(experimentId);
                processService.populateParentIds(processEntity);
            });
        }

        return experimentRepository.save(experimentEntity);
    }
}
