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

package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.persistance.registry.jpa.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ExperimentRegistry {
    private GatewayRegistry gatewayRegistry;
    private UserReg userReg;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRegistry.class);

    public String add(BasicMetadata basicMetadata) {
        String experimentID = "";
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            userReg = new UserReg();
            WorkerResource worker = userReg.getSysteUser();
            experimentID = getExperimentID(basicMetadata.getExperimentName());
            ExperimentMetadataResource exBasicData = gateway.createBasicMetada(experimentID);
            exBasicData.setExperimentName(basicMetadata.getExperimentName());
            exBasicData.setDescription(basicMetadata.getExperimentDescription());
            exBasicData.setExecutionUser(basicMetadata.getUserName());
            exBasicData.setSubmittedDate(getCurrentTimestamp());
            exBasicData.setShareExp(basicMetadata.isSetShareExperimentPublicly());
            ProjectResource projectResource = worker.getProject(basicMetadata.getProjectID());
            exBasicData.setProject(projectResource);
            exBasicData.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return experimentID;
    }

    public void add(ConfigurationData configurationData, String experimentID) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, experimentID);
            ExperimentConfigDataResource exConfigData = (ExperimentConfigDataResource) exBasicData.create(ResourceType.EXPERIMENT_CONFIG_DATA);
            BasicMetadata updatedBasicMetadata = configurationData.getBasicMetadata();
            if (updatedBasicMetadata != null) {
                if (updatedBasicMetadata.getExperimentName() != null && !updatedBasicMetadata.getExperimentName().equals("")) {
                    exBasicData.setExperimentName(updatedBasicMetadata.getExperimentName());
                }
                if (updatedBasicMetadata.getExperimentDescription() != null && !updatedBasicMetadata.getExperimentDescription().equals("")) {
                    exBasicData.setDescription(updatedBasicMetadata.getExperimentDescription());
                }
                if (updatedBasicMetadata.getUserName() != null && !updatedBasicMetadata.getUserName().equals("")) {
                    exBasicData.setExecutionUser(updatedBasicMetadata.getUserName());
                }
                exBasicData.setShareExp(updatedBasicMetadata.isSetShareExperimentPublicly());
                exBasicData.save();
            }
            exConfigData.setExMetadata(exBasicData);
            exConfigData.setApplicationID(configurationData.getApplicationId());
            exConfigData.setApplicationVersion(configurationData.getApplicationVersion());
            exConfigData.setWorkflowTemplateId(configurationData.getWorkflowTemplateId());
            exConfigData.setWorkflowTemplateVersion(configurationData.getWorklfowTemplateVersion());

            ComputationalResourceScheduling resourceScheduling = configurationData.getComputationalResourceScheduling();
            if (resourceScheduling != null) {
                exConfigData.setCpuCount(resourceScheduling.getTotalCPUCount());
                exConfigData.setAiravataAutoSchedule(resourceScheduling.isAiravataAutoSchedule());
                exConfigData.setOverrideManualSchedule(resourceScheduling.isOverrideManualScheduledParams());
                exConfigData.setResourceHostID(resourceScheduling.getResourceHostId());
                exConfigData.setNodeCount(resourceScheduling.getNodeCount());
                exConfigData.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
                exConfigData.setQueueName(resourceScheduling.getQueueName());
                exConfigData.setWallTimeLimit(resourceScheduling.getWallTimeLimit());
                exConfigData.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
                exConfigData.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
                exConfigData.setProjectAccount(resourceScheduling.getComputationalProjectAccount());
            }

            AdvancedInputDataHandling inputDataHandling = configurationData.getAdvanceInputDataHandling();
            if (inputDataHandling != null) {
                exConfigData.setStageInputsToWDir(inputDataHandling.isStageInputFilesToWorkingDir());
                exConfigData.setWorkingDirParent(inputDataHandling.getWorkingDirectoryParent());
                exConfigData.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
                exConfigData.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
            }

            AdvancedOutputDataHandling outputDataHandling = configurationData.getAdvanceOutputDataHandling();
            if (outputDataHandling != null) {
                exConfigData.setOutputDataDir(outputDataHandling.getOutputdataDir());
                exConfigData.setDataRegURL(outputDataHandling.getDataRegistryURL());
                exConfigData.setPersistOutputData(outputDataHandling.isPersistOutputData());
            }

            QualityOfServiceParams qosParams = configurationData.getQosParams();
            if (qosParams != null) {
                exConfigData.setStartExecutionAt(qosParams.getStartExecutionAt());
                exConfigData.setExecuteBefore(qosParams.getExecuteBefore());
                exConfigData.setNumberOfRetries(qosParams.getNumberofRetries());
            }

            Map<String, String> experimentInputs = configurationData.getExperimentInputs();
            for (String inputKey : experimentInputs.keySet()) {
                ExperimentInputResource exInputResource = (ExperimentInputResource) exBasicData.create(ResourceType.EXPERIMENT_INPUT);
                String value = experimentInputs.get(inputKey);
                exInputResource.setExperimentKey(inputKey);
                exInputResource.setValue(value);
                exInputResource.setExperimentMetadataResource(exBasicData);
                exInputResource.save();
            }
            exConfigData.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String getExperimentID(String experimentName) {
        return experimentName + "_" + UUID.randomUUID();
    }

    public void update(Object experimentObject, String expId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            if (experimentObject instanceof BasicMetadata) {
                updateBasicData((BasicMetadata) experimentObject, expId);
            } else if (experimentObject instanceof ConfigurationData) {
                updateExpConfigData((ConfigurationData) experimentObject, expId);
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateExpBasicMetadataField(String expID, String fieldName, Object value) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expID);
            if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.EXPERIMENT_NAME)) {
                exBasicData.setExperimentName((String) value);
                exBasicData.save();
            } else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.USER_NAME)) {
                exBasicData.setExecutionUser((String) value);
                exBasicData.save();
            } else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.EXPERIMENT_DESC)) {
                exBasicData.setDescription((String) value);
                exBasicData.save();
            } else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.SHARE_EXP_PUBLIC)) {
                exBasicData.setShareExp((Boolean) value);
                exBasicData.save();
            }else {
                logger.error("Unsupported field type for Experiment basic metadata");
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateExpConfigDataField(String expID, String fieldName, Object value) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expID);
            ExperimentConfigDataResource exConfigData = (ExperimentConfigDataResource)exBasicData.get(ResourceType.EXPERIMENT_CONFIG_DATA, expID);
            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.APPLICATION_ID)) {
                exConfigData.setApplicationID((String) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.APPLICATION_VERSION)) {
                exConfigData.setApplicationVersion((String) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.WORKFLOW_TEMPLATE_ID)) {
                exConfigData.setWorkflowTemplateId((String) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.WORKFLOW_TEMPLATE_VERSION)) {
                exConfigData.setWorkflowTemplateVersion((String) value);
                exConfigData.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)){
                ComputationalResourceScheduling resourceScheduling = (ComputationalResourceScheduling)value;
                exConfigData.setCpuCount(resourceScheduling.getTotalCPUCount());
                exConfigData.setAiravataAutoSchedule(resourceScheduling.isAiravataAutoSchedule());
                exConfigData.setOverrideManualSchedule(resourceScheduling.isOverrideManualScheduledParams());
                exConfigData.setResourceHostID(resourceScheduling.getResourceHostId());
                exConfigData.setNodeCount(resourceScheduling.getNodeCount());
                exConfigData.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
                exConfigData.setQueueName(resourceScheduling.getQueueName());
                exConfigData.setWallTimeLimit(resourceScheduling.getWallTimeLimit());
                exConfigData.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
                exConfigData.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
                exConfigData.setProjectAccount(resourceScheduling.getComputationalProjectAccount());
                exConfigData.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)){
                AdvancedInputDataHandling adInputHandling = (AdvancedInputDataHandling)value;
                exConfigData.setStageInputsToWDir(adInputHandling.isStageInputFilesToWorkingDir());
                exConfigData.setWorkingDirParent(adInputHandling.getWorkingDirectoryParent());
                exConfigData.setWorkingDir(adInputHandling.getUniqueWorkingDirectory());
                exConfigData.setCleanAfterJob(adInputHandling.isCleanUpWorkingDirAfterJob());
                exConfigData.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)){
                AdvancedOutputDataHandling adOutputHandling = (AdvancedOutputDataHandling)value;
                exConfigData.setOutputDataDir(adOutputHandling.getOutputdataDir());
                exConfigData.setDataRegURL(adOutputHandling.getDataRegistryURL());
                exConfigData.setPersistOutputData(adOutputHandling.isPersistOutputData());
                exConfigData.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)){
                QualityOfServiceParams qosParams = (QualityOfServiceParams)value;
                exConfigData.setStartExecutionAt(qosParams.getStartExecutionAt());
                exConfigData.setExecuteBefore(qosParams.getExecuteBefore());
                exConfigData.setNumberOfRetries(qosParams.getNumberofRetries());
                exConfigData.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.EXPERIMENT_INPUTS)){
                if (value instanceof Map){
                    Map<String, String> experimentInputs = (HashMap<String, String>)value;
                    List<Resource> exInputs = exBasicData.get(ResourceType.EXPERIMENT_INPUT);
                    int i = 0;
                    for (String exInputKey : experimentInputs.keySet()){
                        ExperimentInputResource exInput = (ExperimentInputResource)exInputs.get(i);
                        if (exInput.getExperimentKey().equals(exInputKey)){
                            exInput.setValue(experimentInputs.get(exInputKey));
                            exInput.save();
                        }
                        i++;
                    }
                }
            }else {
                logger.error("Unsupported field type for Experiment config data");
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateBasicData(BasicMetadata basicMetadata, String expId) throws ApplicationSettingsException {
        GatewayResource gateway = gatewayRegistry.getDefaultGateway();
        WorkerResource worker = userReg.getSysteUser();
        ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expId);
        exBasicData.setExperimentName(basicMetadata.getExperimentName());
        exBasicData.setDescription(basicMetadata.getExperimentDescription());
        exBasicData.setExecutionUser(basicMetadata.getUserName());
        exBasicData.setSubmittedDate(getCurrentTimestamp());
        exBasicData.setShareExp(basicMetadata.isSetShareExperimentPublicly());
        exBasicData.setProject(worker.getProject(basicMetadata.getProjectID()));
        exBasicData.save();
    }

    public void updateExpConfigData(ConfigurationData configData, String expId) throws ApplicationSettingsException {
        GatewayResource gateway = gatewayRegistry.getDefaultGateway();
        ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expId);
        ExperimentConfigDataResource exConfigResource = (ExperimentConfigDataResource) exBasicData.get(ResourceType.EXPERIMENT_CONFIG_DATA, expId);
        BasicMetadata updatedBasicMetadata = configData.getBasicMetadata();
        if (updatedBasicMetadata != null) {
            if (updatedBasicMetadata.getExperimentName() != null && !updatedBasicMetadata.getExperimentName().equals("")) {
                exBasicData.setExperimentName(updatedBasicMetadata.getExperimentName());
            }
            if (updatedBasicMetadata.getExperimentDescription() != null && !updatedBasicMetadata.getExperimentDescription().equals("")) {
                exBasicData.setDescription(updatedBasicMetadata.getExperimentDescription());
            }
            if (updatedBasicMetadata.getUserName() != null && !updatedBasicMetadata.getUserName().equals("")) {
                exBasicData.setExecutionUser(updatedBasicMetadata.getUserName());
            }
            exBasicData.setShareExp(updatedBasicMetadata.isSetShareExperimentPublicly());
            exBasicData.save();
        }
        exConfigResource.setExMetadata(exBasicData);
        exConfigResource.setApplicationID(configData.getApplicationId());
        exConfigResource.setApplicationVersion(configData.getApplicationVersion());
        exConfigResource.setWorkflowTemplateId(configData.getWorkflowTemplateId());
        exConfigResource.setWorkflowTemplateVersion(configData.getWorklfowTemplateVersion());

        ComputationalResourceScheduling resourceScheduling = configData.getComputationalResourceScheduling();
        if (resourceScheduling != null) {
            exConfigResource.setCpuCount(resourceScheduling.getTotalCPUCount());
            exConfigResource.setAiravataAutoSchedule(resourceScheduling.isAiravataAutoSchedule());
            exConfigResource.setOverrideManualSchedule(resourceScheduling.isOverrideManualScheduledParams());
            exConfigResource.setResourceHostID(resourceScheduling.getResourceHostId());
            exConfigResource.setNodeCount(resourceScheduling.getNodeCount());
            exConfigResource.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
            exConfigResource.setQueueName(resourceScheduling.getQueueName());
            exConfigResource.setWallTimeLimit(resourceScheduling.getWallTimeLimit());
            exConfigResource.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
            exConfigResource.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
            exConfigResource.setProjectAccount(resourceScheduling.getComputationalProjectAccount());
        }

        AdvancedInputDataHandling inputDataHandling = configData.getAdvanceInputDataHandling();
        if (inputDataHandling != null) {
            exConfigResource.setStageInputsToWDir(inputDataHandling.isStageInputFilesToWorkingDir());
            exConfigResource.setWorkingDirParent(inputDataHandling.getWorkingDirectoryParent());
            exConfigResource.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
            exConfigResource.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
        }

        AdvancedOutputDataHandling outputDataHandling = configData.getAdvanceOutputDataHandling();
        if (outputDataHandling != null) {
            exConfigResource.setOutputDataDir(outputDataHandling.getOutputdataDir());
            exConfigResource.setDataRegURL(outputDataHandling.getDataRegistryURL());
            exConfigResource.setPersistOutputData(outputDataHandling.isPersistOutputData());
        }

        QualityOfServiceParams qosParams = configData.getQosParams();
        if (qosParams != null) {
            exConfigResource.setStartExecutionAt(qosParams.getStartExecutionAt());
            exConfigResource.setExecuteBefore(qosParams.getExecuteBefore());
            exConfigResource.setNumberOfRetries(qosParams.getNumberofRetries());
        }

        Map<String, String> experimentInputs = configData.getExperimentInputs();
        for (String inputKey : experimentInputs.keySet()) {
            ExperimentInputResource exInputResource = (ExperimentInputResource) exBasicData.create(ResourceType.EXPERIMENT_INPUT);
            String value = experimentInputs.get(inputKey);
            exInputResource.setExperimentKey(inputKey);
            exInputResource.setValue(value);
            exInputResource.setExperimentMetadataResource(exBasicData);
            exInputResource.save();
        }
        exConfigResource.save();
    }

    public List<BasicMetadata> getExperimentMetaDataList (String fieldName, Object value){
        List<BasicMetadata> metadataList = new ArrayList<BasicMetadata>();
        try {
            if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.USER_NAME)){
                UserReg userRegistry = new UserReg();
                WorkerResource worker = userRegistry.getExistingUser(ServerSettings.getSystemUserGateway(), (String)value);
                List<Resource> resources = worker.get(ResourceType.EXPERIMENT_METADATA);
                for (Resource resource : resources){
                    ExperimentMetadataResource ex =  (ExperimentMetadataResource)resource;
                    BasicMetadata basicMetadata = ThriftDataModelConversion.getBasicMetadata(ex);
                    metadataList.add(basicMetadata);
                }
                return metadataList;
            }else {
                logger.error("Unsupported field type for Experiment meta data");
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return metadataList;
    }

    public List<ConfigurationData> getConfigurationDataList (String fieldName, Object value){
        List<ConfigurationData> configDataList = new ArrayList<ConfigurationData>();
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.EXPERIMENT_ID)){
                ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, value);
                List<Resource> resources = exBasicData.get(ResourceType.EXPERIMENT_CONFIG_DATA);
                for (Resource resource : resources){
                    ExperimentConfigDataResource configDataResource = (ExperimentConfigDataResource)resource;
                    ConfigurationData conData = ThriftDataModelConversion.getConfigurationData(configDataResource);
                    configDataList.add(conData);
                }
                return configDataList;
            }else {
                logger.error("Unsupported field type for Experiment meta data");
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return configDataList;
    }

    public Object getBasicMetaData(String expId, String fieldName) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expId);
            if (fieldName == null){
                return exBasicData;
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.USER_NAME)){
                return exBasicData.getExecutionUser();
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.EXPERIMENT_NAME)){
                return exBasicData.getExperimentName();
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.EXPERIMENT_DESC)){
                return exBasicData.getDescription();
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.SHARE_EXP_PUBLIC)){
                return exBasicData.isShareExp();
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.PROJECT_NAME)){
                return exBasicData.getProject().getName();
            }else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.SUBMITTED_DATE)){
                return exBasicData.getSubmittedDate();
            }else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public Object getConfigData(String expId, String fieldName) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource) gateway.get(ResourceType.EXPERIMENT_METADATA, expId);
            ExperimentConfigDataResource exCongfig = (ExperimentConfigDataResource)exBasicData.get(ResourceType.EXPERIMENT_CONFIG_DATA, expId);
            if (fieldName == null){
                return exCongfig;
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.APPLICATION_ID)){
                return exCongfig.getApplicationID();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.APPLICATION_VERSION)){
                return exCongfig.getApplicationVersion();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.WORKFLOW_TEMPLATE_ID)){
                return exCongfig.getWorkflowTemplateId();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.WORKFLOW_TEMPLATE_VERSION)){
                return exCongfig.getWorkflowTemplateVersion();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.EXPERIMENT_INPUTS)){
                return ThriftDataModelConversion.getExperimentInputs(exBasicData);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)){
                return ThriftDataModelConversion.getComputationalResourceScheduling(exCongfig);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)){
                return ThriftDataModelConversion.getAdvanceInputDataHandling(exCongfig);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)){
                return ThriftDataModelConversion.getAdvanceOutputDataHandling(exCongfig);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)){
                return ThriftDataModelConversion.getQOSParams(exCongfig);
            }else {
                logger.error("Unsupported field name for experiment configuration data..");
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
        return null;
    }

    public List<String> getExperimentIDs (String fieldName, Object value) {
        List<String> expIDs = new ArrayList<String>();
        try {
            if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.GATEWAY)) {
                GatewayResource gateway = gatewayRegistry.getExistingGateway((String) value);
                if (gateway == null) {
                    logger.error("You should use an existing gateway in order to retrieve experiments..");
                    return null;
                } else {
                    List<Resource> resources = gateway.get(ResourceType.EXPERIMENT_METADATA);
                    for (Resource resource : resources) {
                        String expID = ((ExperimentMetadataResource) resource).getExpID();
                        expIDs.add(expID);
                    }
                }
            } else if (fieldName.equals(Constants.FieldConstants.BasicMetadataConstants.USER_NAME)) {
                WorkerResource workerResource = userReg.getExistingUser(ServerSettings.getSystemUserGateway(), (String)value);
                List<Resource> resources = workerResource.get(ResourceType.EXPERIMENT_METADATA);
                for (Resource resource : resources) {
                    String expID = ((ExperimentMetadataResource) resource).getExpID();
                    expIDs.add(expID);
                }
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
        return expIDs;
    }


    public void removeExperiment(String experimentId) {
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            defaultGateway.remove(ResourceType.EXPERIMENT_METADATA, experimentId);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public void removeExperimentConfigData(String experimentId) {
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource)defaultGateway.get(ResourceType.EXPERIMENT_METADATA, experimentId);
            exBasicData.remove(ResourceType.EXPERIMENT_CONFIG_DATA, experimentId);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public boolean isExperimentBasicDataExist(String expID) {
        try{
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            defaultGateway.isExists(ResourceType.EXPERIMENT_METADATA, expID);
            return true;
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
        return false;
    }

    public boolean isExperimentConfigDataExist(String expID) {
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            ExperimentMetadataResource exBasicData = (ExperimentMetadataResource)defaultGateway.get(ResourceType.EXPERIMENT_METADATA, expID);
            exBasicData.isExists(ResourceType.EXPERIMENT_CONFIG_DATA, expID);
            return true;
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
        return false;
    }

    public Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        return new Timestamp(d.getTime());
    }

    public Timestamp getTime(int time) {
        long timeInMileseconds = time * 1000L;
        Date date = new Date(timeInMileseconds);
        return new Timestamp(date.getTime());
    }
}
