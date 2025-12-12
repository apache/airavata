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

import com.github.dozermapper.core.Mapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.entities.expcatalog.ComputationalResourceSchedulingEntity;
import org.apache.airavata.registry.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.utils.DBConstants;
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
    private final Mapper mapper;

    public ExperimentService(ExperimentRepository experimentRepository, ProcessService processService, Mapper mapper) {
        this.experimentRepository = experimentRepository;
        this.processService = processService;
        this.mapper = mapper;
    }

    @Transactional("expCatalogTransactionManager")
    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        ExperimentStatus experimentStatus = new ExperimentStatus();
        experimentStatus.setState(ExperimentState.CREATED);
        experimentStatus.setTimeOfStateChange(
                AiravataUtils.getCurrentTimestamp().getTime());
        experimentModel.addToExperimentStatus(experimentStatus);
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
        return mapper.map(entity, ExperimentModel.class);
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
            experimentModelList = new ArrayList<>();
            entities.forEach(e -> experimentModelList.add(mapper.map(e, ExperimentModel.class)));
        } else if (fieldName.equals(DBConstants.Experiment.PROJECT_ID)) {
            logger.debug("Search criteria is ProjectId");
            List<ExperimentEntity> entities =
                    experimentRepository.findByGatewayIdAndProjectId(gatewayId, (String) value);
            experimentModelList = new ArrayList<>();
            entities.forEach(e -> experimentModelList.add(mapper.map(e, ExperimentModel.class)));
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

        ExperimentEntity existingEntity = experimentRepository.findById(experimentId).orElse(null);
        ExperimentEntity experimentEntity;
        
        if (existingEntity != null) {
            ExperimentEntity newEntity = mapper.map(experimentModel, ExperimentEntity.class);
            mapper.map(experimentModel, existingEntity);
            
            mergeLists(existingEntity.getExperimentStatus(), newEntity.getExperimentStatus(), org.apache.airavata.registry.entities.expcatalog.ExperimentStatusEntity::getStatusId);
            mergeLists(existingEntity.getExperimentInputs(), newEntity.getExperimentInputs(), org.apache.airavata.registry.entities.expcatalog.ExperimentInputEntity::getName);
            mergeLists(existingEntity.getExperimentOutputs(), newEntity.getExperimentOutputs(), org.apache.airavata.registry.entities.expcatalog.ExperimentOutputEntity::getName);
            mergeLists(existingEntity.getErrors(), newEntity.getErrors(), org.apache.airavata.registry.entities.expcatalog.ExperimentErrorEntity::getErrorId);
            mergeLists(existingEntity.getProcesses(), newEntity.getProcesses(), org.apache.airavata.registry.entities.expcatalog.ProcessEntity::getProcessId);
            
            experimentEntity = existingEntity;
        } else {
            experimentEntity = mapper.map(experimentModel, ExperimentEntity.class);
        }

        if (experimentEntity.getUserConfigurationData() != null) {
            logger.debug("Populating the Primary Key of UserConfigurationData object for the Experiment");
            experimentEntity.getUserConfigurationData().setExperimentId(experimentId);
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

    private <T> void mergeLists(List<T> currentList, List<T> newList, java.util.function.Function<T, String> idExtractor) {
        if (currentList == null || newList == null) return;
        
        java.util.Map<String, T> currentMap = currentList.stream()
            .collect(java.util.stream.Collectors.toMap(idExtractor, java.util.function.Function.identity()));
        
        java.util.List<T> result = new ArrayList<>();
        for (T newItem : newList) {
            String id = idExtractor.apply(newItem);
            if (id != null && currentMap.containsKey(id)) {
                T existing = currentMap.get(id);
                mapper.map(newItem, existing);
                result.add(existing);
            } else {
                result.add(newItem);
            }
        }
        
        currentList.clear();
        currentList.addAll(result);
    }
}
