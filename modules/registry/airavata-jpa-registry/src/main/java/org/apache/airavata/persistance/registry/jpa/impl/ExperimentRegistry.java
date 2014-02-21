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
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
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

    public String addExperiment(Experiment experiment) throws Exception{
        String experimentID = "";
        try {
            if (!ResourceUtils.isUserExist(experiment.getUserName())){
                logger.error("User does not exist in the system..");
                throw new Exception("User does not exist in the system..");
            }
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            userReg = new UserReg();
            WorkerResource worker = userReg.getExistingUser(gateway.getGatewayName(), experiment.getUserName());
            experimentID = getExperimentID(experiment.getName());
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExpID(experimentID);
            experimentResource.setExpName(experiment.getName());
            experimentResource.setWorker(worker);
            experimentResource.setGateway(gateway);
            if (!worker.isProjectExists(experiment.getProjectID())){
                ProjectResource project = worker.createProject(experiment.getProjectID());
                experimentResource.setProject(project);
            }
            experimentResource.setCreationTime(getTime(experiment.getCreationTime()));
            experimentResource.setDescription(experiment.getDescription());
            experimentResource.setApplicationId(experiment.getApplicationId());
            experimentResource.setApplicationVersion(experiment.getApplicationVersion());
            experimentResource.setWorkflowTemplateId(experiment.getWorkflowTemplateId());
            experimentResource.setWorkflowTemplateVersion(experiment.getWorkflowTemplateVersion());
            experimentResource.setWorkflowExecutionId(experiment.getWorkflowExecutionInstanceId());
            experimentResource.save();
            List<DataObjectType> experimentInputs = experiment.getExperimentInputs();
            addExpInputs(experimentInputs, experimentResource);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }catch (Exception e){
            logger.error("Error while saving experiment to registry", e.getMessage());
        }
        return experimentID;
    }

    public void addUserConfigData(UserConfigurationData configurationData, String experimentID) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(experimentID);
            ConfigDataResource configData = (ConfigDataResource)experiment.create(ResourceType.CONFIG_DATA);
            configData.setExperimentResource(experiment);
            configData.setAiravataAutoSchedule(configurationData.isAiravataAutoSchedule());
            configData.setOverrideManualParams(configurationData.isOverrideManualScheduledParams());
            configData.setShareExp(configurationData.isShareExperimentPublicly());

            ComputationalResourceScheduling resourceScheduling = configurationData.getComputationalResourceScheduling();
            if (resourceScheduling != null) {
                ComputationSchedulingResource cmsr = new ComputationSchedulingResource();
                cmsr.setExperimentResource(experiment);
                cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
                cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
                cmsr.setNodeCount(resourceScheduling.getNodeCount());
                cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
                cmsr.setQueueName(resourceScheduling.getQueueName());
                cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
                cmsr.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
                cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
                cmsr.setProjectName(resourceScheduling.getComputationalProjectAccount());
                cmsr.save();
            }
            AdvancedInputDataHandling inputDataHandling = configurationData.getAdvanceInputDataHandling();
            if (inputDataHandling != null) {
                AdvanceInputDataHandlingResource adidh = new AdvanceInputDataHandlingResource();
                adidh.setExperimentResource(experiment);
                adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
                adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
                adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
                adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
                adidh.save();
            }

            AdvancedOutputDataHandling outputDataHandling = configurationData.getAdvanceOutputDataHandling();
            if (outputDataHandling != null) {
                AdvancedOutputDataHandlingResource adodh = new AdvancedOutputDataHandlingResource();
                adodh.setExperimentResource(experiment);
                adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
                adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
                adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
                adodh.save();
            }

            QualityOfServiceParams qosParams = configurationData.getQosParams();
            if (qosParams != null) {
                QosParamResource qosr = new QosParamResource();
                qosr.setExperimentResource(experiment);
                qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
                qosr.setExecuteBefore(qosParams.getExecuteBefore());
                qosr.setNoOfRetries(qosParams.getNumberofRetries());
                qosr.save();
            }
            configData.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }catch (Exception e){
            logger.error("Unable to save user config data", e.getMessage());
        }
    }

    public void addExpInputs (List<DataObjectType> exInputs, ExperimentResource experimentResource ){
        for (DataObjectType input : exInputs){
            ExperimentInputResource resource = (ExperimentInputResource)experimentResource.create(ResourceType.EXPERIMENT_INPUT);
            resource.setExperimentResource(experimentResource);
            resource.setExperimentKey(input.getKey());
            resource.setValue(input.getValue());
            resource.setInputType(input.getType());
            resource.setMetadata(input.getMetaData());
            resource.save();
        }
    }

    public void updateExpInputs (List<DataObjectType> exInputs, ExperimentResource experimentResource ){
        List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
        for (DataObjectType input : exInputs){
            for (ExperimentInputResource exinput : experimentInputs){
                if (exinput.getExperimentKey().equals(input.getKey())){
                    exinput.setValue(input.getValue());
                    exinput.setInputType(input.getType());
                    exinput.setMetadata(input.getMetaData());
                    exinput.save();
                }
            }
        }
    }

    public void addExpOuputs (List<DataObjectType> exOutput, String expId ) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expId);
            for (DataObjectType output : exOutput) {
                ExperimentOutputResource resource = (ExperimentOutputResource) experiment.create(ResourceType.EXPERIMENT_OUTPUT);
                resource.setExperimentResource(experiment);
                resource.setExperimentKey(output.getKey());
                resource.setValue(output.getValue());
                resource.setOutputType(output.getType());
                resource.setMetadata(output.getMetaData());
                resource.save();
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String getExperimentID(String experimentName) {
        return experimentName + "_" + UUID.randomUUID();
    }

    public void updateExperimentField(String expID, String fieldName, Object value) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expID);
            userReg = new UserReg();
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                experiment.setExpName((String)value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource worker = userReg.getExistingUser(gateway.getGatewayName(), (String)value);
                experiment.setWorker(worker);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                experiment.setDescription((String)value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                experiment.setApplicationId((String)value);
                experiment.save();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)){
                experiment.setApplicationVersion((String) value);
                experiment.save();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)){
                experiment.setWorkflowTemplateId((String) value);
                experiment.save();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)){
                experiment.setWorkflowTemplateVersion((String) value);
                experiment.save();
            }else {
                logger.error("Unsupported field type for Experiment");
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateExpConfigDataField(String expID, String fieldName, Object value) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expID);
            ConfigDataResource exConfigData = (ConfigDataResource)experiment.get(ResourceType.CONFIG_DATA, expID);
            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                exConfigData.setAiravataAutoSchedule((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                exConfigData.setOverrideManualParams((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
                exConfigData.setShareExp((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)){
                ComputationalResourceScheduling resourceScheduling = (ComputationalResourceScheduling)value;
                ComputationSchedulingResource resource = experiment.getComputationScheduling(expID);
                resource.setCpuCount(resourceScheduling.getTotalCPUCount());
                resource.setResourceHostId(resourceScheduling.getResourceHostId());
                resource.setNodeCount(resourceScheduling.getNodeCount());
                resource.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
                resource.setQueueName(resourceScheduling.getQueueName());
                resource.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
                resource.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
                resource.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
                resource.setProjectName(resourceScheduling.getComputationalProjectAccount());
                resource.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)){
                AdvancedInputDataHandling adInputHandling = (AdvancedInputDataHandling)value;
                AdvanceInputDataHandlingResource resource = experiment.getInputDataHandling(expID);
                resource.setStageInputFiles(adInputHandling.isStageInputFilesToWorkingDir());
                resource.setWorkingDirParent(adInputHandling.getParentWorkingDirectory());
                resource.setWorkingDir(adInputHandling.getUniqueWorkingDirectory());
                resource.setCleanAfterJob(adInputHandling.isCleanUpWorkingDirAfterJob());
                resource.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)){
                AdvancedOutputDataHandling adOutputHandling = (AdvancedOutputDataHandling)value;
                AdvancedOutputDataHandlingResource resource = experiment.getOutputDataHandling(expID);
                resource.setOutputDataDir(adOutputHandling.getOutputDataDir());
                resource.setDataRegUrl(adOutputHandling.getDataRegistryURL());
                resource.setPersistOutputData(adOutputHandling.isPersistOutputData());
                resource.save();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)){
                QualityOfServiceParams qosParams = (QualityOfServiceParams)value;
                QosParamResource qoSparams = experiment.getQOSparams(expID);
                qoSparams.setStartExecutionAt(qosParams.getStartExecutionAt());
                qoSparams.setExecuteBefore(qosParams.getExecuteBefore());
                qoSparams.setNoOfRetries(qosParams.getNumberofRetries());
                qoSparams.save();
            }else {
                logger.error("Unsupported field type for Experiment config data");
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateExperiment(Experiment experiment, String expId) throws ApplicationSettingsException {
        GatewayResource gateway = gatewayRegistry.getDefaultGateway();
        ExperimentResource existingExperiment = gateway.getExperiment(expId);
        userReg = new UserReg();
        WorkerResource worker = userReg.getExistingUser(gateway.getGatewayName(), experiment.getUserName());
        existingExperiment.setExpName(experiment.getName());
        existingExperiment.setWorker(worker);
        existingExperiment.setGateway(gateway);
        if (!worker.isProjectExists(experiment.getProjectID())){
            ProjectResource project = worker.createProject(experiment.getProjectID());
            existingExperiment.setProject(project);
        }
        existingExperiment.setCreationTime(getTime(experiment.getCreationTime()));
        existingExperiment.setDescription(experiment.getDescription());
        existingExperiment.setApplicationId(experiment.getApplicationId());
        existingExperiment.setApplicationVersion(experiment.getApplicationVersion());
        existingExperiment.setWorkflowTemplateId(experiment.getWorkflowTemplateId());
        existingExperiment.setWorkflowTemplateVersion(experiment.getWorkflowTemplateVersion());
        existingExperiment.setWorkflowExecutionId(experiment.getWorkflowExecutionInstanceId());
        existingExperiment.save();
        List<DataObjectType> experimentInputs = experiment.getExperimentInputs();
        updateExpInputs(experimentInputs, existingExperiment);
    }

    public void updateUserConfigData(UserConfigurationData configData, String expId) throws ApplicationSettingsException {
        GatewayResource gateway = gatewayRegistry.getDefaultGateway();
        ExperimentResource experiment = gateway.getExperiment(expId);
        ConfigDataResource resource = (ConfigDataResource)experiment.get(ResourceType.CONFIG_DATA, expId);
        resource.setExperimentResource(experiment);
        resource.setAiravataAutoSchedule(configData.isAiravataAutoSchedule());
        resource.setOverrideManualParams(configData.isOverrideManualScheduledParams());
        resource.setShareExp(configData.isShareExperimentPublicly());

        ComputationalResourceScheduling resourceScheduling = configData.getComputationalResourceScheduling();
        if (resourceScheduling != null) {
            ComputationSchedulingResource cmsr = experiment.getComputationScheduling(expId);
            cmsr.setExperimentResource(experiment);
            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
            cmsr.setNodeCount(resourceScheduling.getNodeCount());
            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
            cmsr.setQueueName(resourceScheduling.getQueueName());
            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
            cmsr.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
            cmsr.setProjectName(resourceScheduling.getComputationalProjectAccount());
            cmsr.save();
        }
        AdvancedInputDataHandling inputDataHandling = configData.getAdvanceInputDataHandling();
        if (inputDataHandling != null) {
            AdvanceInputDataHandlingResource adidh = experiment.getInputDataHandling(expId);
            adidh.setExperimentResource(experiment);
            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
            adidh.save();
        }

        AdvancedOutputDataHandling outputDataHandling = configData.getAdvanceOutputDataHandling();
        if (outputDataHandling != null) {
            AdvancedOutputDataHandlingResource adodh = experiment.getOutputDataHandling(expId);
            adodh.setExperimentResource(experiment);
            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
            adodh.save();
        }

        QualityOfServiceParams qosParams = configData.getQosParams();
        if (qosParams != null) {
            QosParamResource qosr = experiment.getQOSparams(expId);
            qosr.setExperimentResource(experiment);
            qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
            qosr.setExecuteBefore(qosParams.getExecuteBefore());
            qosr.setNoOfRetries(qosParams.getNumberofRetries());
            qosr.save();
        }
        resource.save();
    }

    public List<Experiment> getExperimentList(String fieldName, Object value){
        List<Experiment> experiments = new ArrayList<Experiment>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)){
                userReg = new UserReg();
                WorkerResource worker = userReg.getExistingUser(ServerSettings.getSystemUserGateway(), (String) value);
                List<ExperimentResource> resources = worker.getExperiments();
                for (ExperimentResource resource : resources){
                    Experiment experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)){
                userReg = new UserReg();
                WorkerResource worker = userReg.getSystemUser();
                ProjectResource project = worker.getProject((String) value);
                List<ExperimentResource> resources = project.getExperiments();
                for (ExperimentResource resource : resources){
                    Experiment experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)){
                gatewayRegistry = new GatewayRegistry();
                GatewayResource existingGateway = gatewayRegistry.getExistingGateway((String) value);
                List<ExperimentResource> resources = existingGateway.getExperiments();
                for (ExperimentResource resource : resources){
                    Experiment experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            }else {
                logger.error("Unsupported field type for Experiment meta data");
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return experiments;
    }

//    public List<UserConfigurationData> getConfigurationDataList (String fieldName, Object value){
//        List<UserConfigurationData> configDataList = new ArrayList<UserConfigurationData>();
//        try {
//            gatewayRegistry = new GatewayRegistry();
//            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
//            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.EXPERIMENT_ID)){
//                ExperimentResource experiment = gateway.getExperiment((String) value);
//                List<Resource> resources = experiment.get(ResourceType.CONFIG_DATA);
//                for (Resource resource : resources){
//                    ExperimentConfigDataResource configDataResource = (ExperimentConfigDataResource)resource;
//                    ConfigurationData conData = ThriftDataModelConversion.getConfigurationData(configDataResource);
//                    configDataList.add(conData);
//                }
//                return configDataList;
//            }else {
//                logger.error("Unsupported field type for Experiment meta data");
//            }
//        } catch (ApplicationSettingsException e) {
//            logger.error("Unable to read airavata-server properties", e.getMessage());
//        }
//        return configDataList;
//    }

    public Object getExperiment(String expId, String fieldName) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource resource = gateway.getExperiment(expId);
            if (fieldName == null){
                return ThriftDataModelConversion.getExperiment(resource);
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)){
                return resource.getWorker().getUser();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)){
                return resource.getExpName();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)){
                return resource.getDescription();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)){
                return resource.getApplicationId();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)){
                return resource.getProject().getName();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)){
                return resource.getApplicationVersion();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)){
                return resource.getWorkflowTemplateId();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)){
                return resource.getWorkflowTemplateId();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS)){
                return ThriftDataModelConversion.getExpInputs(resource.getExperimentInputs());
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS)){
                return ThriftDataModelConversion.getExpOutputs(resource.getExperimentOutputs());
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)){
                return ThriftDataModelConversion.getExperimentStatus(resource.getExperimentStatus());
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_CONFIGURATION_DATA)){
                return ThriftDataModelConversion.getUserConfigData(resource.getUserConfigData(expId));
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_EXECUTION_ID)){
                return resource.getWorkflowExecutionId();
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.STATE_CHANGE_LIST)){
                return ThriftDataModelConversion.getWorkflowNodeStatusList(resource.getWorkflowNodeStatuses());
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)){
                return ThriftDataModelConversion.getWfNodeList(resource.getWorkflowNodeDetails());
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.ERROR_DETAIL_LIST)){
                return ThriftDataModelConversion.getErrorDetailList(resource.getErrorDetails());
            }
            else {
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
            ExperimentResource resource = gateway.getExperiment(expId);
            ConfigDataResource userConfigData = resource.getUserConfigData(expId);
            if (fieldName == null){
                return ThriftDataModelConversion.getUserConfigData(userConfigData);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)){
                return userConfigData.isAiravataAutoSchedule();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)){
                return userConfigData.isOverrideManualParams();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)){
                return userConfigData.isShareExp();
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)){
                return ThriftDataModelConversion.getComputationalResourceScheduling(resource.getComputationScheduling(expId));
           }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)){
                return ThriftDataModelConversion.getAdvanceInputDataHandling(resource.getInputDataHandling(expId));
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)){
                return ThriftDataModelConversion.getAdvanceOutputDataHandling(resource.getOutputDataHandling(expId));
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)){
                return ThriftDataModelConversion.getQOSParams(resource.getQOSparams(expId));
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
        gatewayRegistry = new GatewayRegistry();
        userReg = new UserReg();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                GatewayResource gateway = gatewayRegistry.getExistingGateway((String) value);
                if (gateway == null) {
                    logger.error("You should use an existing gateway in order to retrieve experiments..");
                    return null;
                } else {
                    List<ExperimentResource> resources = gateway.getExperiments();
                    for (ExperimentResource resource : resources) {
                        String expID = resource.getExpID();
                        expIDs.add(expID);
                    }
                }
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource workerResource = userReg.getExistingUser(ServerSettings.getSystemUserGateway(), (String)value);
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExpID());
                }
            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)) {
                WorkerResource workerResource = userReg.getSystemUser();
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExpID());
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
            defaultGateway.remove(ResourceType.EXPERIMENT, experimentId);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public void removeExperimentConfigData(String experimentId) {
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = defaultGateway.getExperiment(experimentId);
            experiment.remove(ResourceType.CONFIG_DATA, experimentId);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public boolean isExperimentExist(String expID) {
        try{
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            defaultGateway.isExists(ResourceType.EXPERIMENT, expID);
            return true;
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
        return false;
    }

    public boolean isExperimentConfigDataExist(String expID) {
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = defaultGateway.getExperiment(expID);
            experiment.isExists(ResourceType.CONFIG_DATA, expID);
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

    public Timestamp getTime(long time) {
        Date date = new Date(time);
        return new Timestamp(date.getTime());
    }
}
