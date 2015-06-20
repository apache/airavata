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

package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.*;
import org.apache.airavata.registry.core.experiment.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExperimentRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRegistry.class);

    public ExperimentRegistry(GatewayResource gateway, UserResource user) throws RegistryException {
        gatewayResource = gateway;
        if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())) {
            workerResource = ExpCatResourceUtils.addGatewayWorker(gateway, user);
        } else {
            workerResource = (WorkerResource) ExpCatResourceUtils.getWorker(gateway.getGatewayId(), user.getUserName());
        }

    }

    public String addExperiment(ExperimentModel experiment, String gatewayId) throws RegistryException {
//        String experimentID;
//        try {
//            if (!ExpCatResourceUtils.isUserExist(experiment.getUserName())) {
//                ExpCatResourceUtils.addUser(experiment.getUserName(), null);
//            }
//
//            experimentID = getExperimentID(experiment.getExperimentName());
//            experiment.setExperimentId(experimentID);
//            ExperimentResource experimentResource = new ExperimentResource();
//            experimentResource.setExpID(experimentID);
//            experimentResource.setExpName(experiment.getExperimentName());
//            experimentResource.setExecutionUser(experiment.getUserName());
//            experimentResource.setGatewayId(gatewayId);
//            experimentResource.setGatewayExecutionId(experiment.getGatewayExecutionId());
//            experimentResource.setEnableEmailNotifications(experiment.isEnableEmailNotification());
//            if (!workerResource.isProjectExists(experiment.getProjectId())) {
//                logger.error("Project does not exist in the system..");
//                throw new Exception("Project does not exist in the system, Please create the project first...");
//            }
//            experimentResource.setProjectId(experiment.getProjectId());
//            experimentResource.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
//            experimentResource.setDescription(experiment.getDescription());
//            experimentResource.setApplicationId(experiment.getExecutionId());
//            experimentResource.save();
//
//            List<String> emailAddresses = experiment.getEmailAddresses();
//            if (emailAddresses != null && !emailAddresses.isEmpty()){
//                for (String email : emailAddresses){
//                    NotificationEmailResource emailResource = new NotificationEmailResource();
//                    emailResource.setExperimentId(experimentID);
//                    emailResource.setEmailAddress(email);
//                    emailResource.save();
//                }
//            }
//
//            List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
//            if (experimentInputs != null) {
//                addExpInputs(experimentInputs, experimentResource);
//            }
//
//            UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
//            if (userConfigurationData != null) {
//                addUserConfigData(userConfigurationData, experimentID);
//            }
//
//            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
//            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
//                //TODO: short change.
////                for (DataObjectType output : experimentOutputs){
////                    output.setValue("");
////                }
//                addExpOutputs(experimentOutputs, experimentID);
//            }
//
////            ExperimentStatus experimentStatus = experiment.getExperimentStatus();
////            if (experimentStatus != null){
////                updateExperimentStatus(experimentStatus, experimentID);
////            }else {
//            ExperimentStatus experimentStatus = new ExperimentStatus();
//            experimentStatus.setState(ExperimentState.CREATED);
//            updateExperimentStatus(experimentStatus, experimentID);
////            }
//
////            List<WorkflowNodeDetails> workflowNodeDetailsList = experiment.getWorkflowNodeDetailsList();
////            if (workflowNodeDetailsList != null && !workflowNodeDetailsList.isEmpty()) {
////                for (WorkflowNodeDetails wf : workflowNodeDetailsList) {
////                    addWorkflowNodeDetails(wf, experimentID);
////                }
////            }
//            List<ErrorModel> errors = experiment.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorModel errror : errors) {
//                    addErrorDetails(errror, experimentID);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while saving experiment to registry", e);
//            throw new RegistryException(e);
//        }
//        return experimentID;
        return null;
    }

    public String addUserConfigData(UserConfigurationDataModel configurationData, String experimentID) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(experimentID);
//            ConfigDataResource configData = (ConfigDataResource) experiment.create(ResourceType.CONFIG_DATA);
//            configData.setExperimentId(experimentID);
//            configData.setAiravataAutoSchedule(configurationData.isAiravataAutoSchedule());
//            configData.setOverrideManualParams(configurationData.isOverrideManualScheduledParams());
//            configData.setShareExp(configurationData.isShareExperimentPublicly());
//            configData.setUserDn(configurationData.getUserDN());
//            configData.setGenerateCert(configurationData.isGenerateCert());
//            configData.save();
//            ComputationalResourceSchedulingModel resourceScheduling = configurationData.getComputationalResourceScheduling();
//            if (resourceScheduling != null) {
//                addComputationScheduling(resourceScheduling, experiment);
//            }
////            AdvancedInputDataHandling inputDataHandling = configurationData.getAdvanceInputDataHandling();
////            if (inputDataHandling != null) {
////                addInputDataHandling(inputDataHandling, experiment);
////            }
////
////            AdvancedOutputDataHandling outputDataHandling = configurationData.getAdvanceOutputDataHandling();
////            if (outputDataHandling != null) {
////                addOutputDataHandling(outputDataHandling, experiment);
////            }
////
////            QualityOfServiceParams qosParams = configurationData.getQosParams();
////            if (qosParams != null) {
////                addQosParams(qosParams, experiment);
////            }
//        } catch (Exception e) {
//            logger.error("Unable to save user config data", e);
//            throw new RegistryException(e);
//        }
//        return experimentID;
        return null;
    }

//    public void addQosParams(QualityOfServiceParams qosParams, ExperimentCatResource resource) throws RegistryException {
//        try {
//            QosParamResource qosr = new QosParamResource();
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource experiment = (ExperimentResource) resource;
//                qosr.setProcessId(experiment.getExpID());
//            }
//            if (resource instanceof TaskDetailResource) {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                qosr.setTaskId(taskDetailResource.getTaskId());
//                String nodeId = taskDetailResource.getNodeId();
//                ExperimentResource experimentResource = new ExperimentResource();
//                WorkflowNodeDetailResource workflowNode = experimentResource.getWorkflowNode(nodeId);
//                qosr.setProcessId(workflowNode.getProcessId());
//            }
//            qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
//            qosr.setExecuteBefore(qosParams.getExecuteBefore());
//            qosr.setNoOfRetries(qosParams.getNumberofRetries());
//            qosr.save();
//        } catch (Exception e) {
//            logger.error("Unable to save QOS params", e);
//            throw new RegistryException(e);
//        }
//
//    }

//    public void addOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvancedOutputDataHandlingResource adodh = new AdvancedOutputDataHandlingResource();
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource experiment = (ExperimentResource) resource;
//                adodh.setProcessId(experiment.getExpID());
//            }
//            if (resource instanceof TaskDetailResource) {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                String nodeId = taskDetailResource.getNodeId();
//                ExperimentResource experimentResource = new ExperimentResource();
//                WorkflowNodeDetailResource workflowNode = experimentResource.getWorkflowNode(nodeId);
//                adodh.setProcessId(workflowNode.getProcessId());
//                adodh.setTaskId(taskDetailResource.getTaskId());
//            }
//            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
//            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
//            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
//            adodh.save();
//        } catch (Exception e) {
//            logger.error("Unable to save output data handling data", e);
//            throw new RegistryException(e);
//        }
//
//    }

//    public void addInputDataHandling(AdvancedInputDataHandling inputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvanceInputDataHandlingResource adidh = new AdvanceInputDataHandlingResource();
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource experiment = (ExperimentResource) resource;
//                adidh.setProcessId(experiment.getExpID());
//            }
//            if (resource instanceof TaskDetailResource) {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                String nodeId = taskDetailResource.getNodeId();
//                ExperimentResource experimentResource = new ExperimentResource();
//                WorkflowNodeDetailResource workflowNode = experimentResource.getWorkflowNode(nodeId);
//                adidh.setProcessId(workflowNode.getProcessId());
//                adidh.setTaskId(taskDetailResource.getTaskId());
//            }
//            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
//            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
//            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
//            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
//            adidh.save();
//        } catch (Exception e) {
//            logger.error("Unable to save input data handling data", e);
//            throw new RegistryException(e);
//        }
//
//    }

    public void addComputationScheduling(ComputationalResourceSchedulingModel resourceScheduling, ExperimentCatResource resource) throws RegistryException {
//        ComputationSchedulingResource cmsr = new ComputationSchedulingResource();
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource experiment = (ExperimentResource) resource;
//                cmsr.setExperimentId(experiment.getExpID());
//            }
//            if (resource instanceof TaskDetailResource) {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                String nodeId = taskDetailResource.getNodeId();
//                ExperimentResource experimentResource = new ExperimentResource();
//                WorkflowNodeDetailResource workflowNode = experimentResource.getWorkflowNode(nodeId);
//                cmsr.setExperimentId(workflowNode.getExperimentId());
//                cmsr.setTaskId(taskDetailResource.getTaskId());
//            }
//            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
//            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
//            cmsr.setNodeCount(resourceScheduling.getNodeCount());
//            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
//            cmsr.setQueueName(resourceScheduling.getQueueName());
//            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
//            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
//            cmsr.setChessisName(resourceScheduling.getChessisNumber());
//            cmsr.save();
//        } catch (Exception e) {
//            logger.error("Unable to save computational scheduling data", e);
//            throw new RegistryException(e);
//        }

    }

    public void addExpInputs(List<InputDataObjectType> exInputs, ExperimentResource experimentResource) throws RegistryException {
//        try {
//            for (InputDataObjectType input : exInputs) {
//                ExperimentInputResource resource = (ExperimentInputResource) experimentResource.create(ResourceType.EXPERIMENT_INPUT);
//                resource.setExperimentId(experimentResource.getExpID());
//                resource.setExperimentKey(input.getName());
//                resource.setValue(input.getValue());
//                if (input.getType() != null) {
//                    resource.setDataType(input.getType().toString());
//                }
//                resource.setMetadata(input.getMetaData());
//                resource.setAppArgument(input.getApplicationArgument());
//                resource.setInputOrder(input.getInputOrder());
//                resource.setRequired(input.isIsRequired());
//                resource.setRequiredToCMD(input.isRequiredToAddedToCommandLine());
//                resource.save();
//            }
//        } catch (Exception e) {
//            logger.error("Unable to save experiment inputs", e);
//            throw new RegistryException(e);
//        }
    }

    public void updateExpInputs(List<InputDataObjectType> exInputs, ExperimentResource experimentResource) throws RegistryException {
//        try {
//            List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
//            for (InputDataObjectType input : exInputs) {
//                for (ExperimentInputResource exinput : experimentInputs) {
//                    if (exinput.getExperimentKey().equals(input.getName())) {
//                        exinput.setValue(input.getValue());
//                        if (input.getType() != null) {
//                            exinput.setDataType(input.getType().toString());
//                        }
//                        exinput.setMetadata(input.getMetaData());
//                        exinput.setAppArgument(input.getApplicationArgument());
//                        exinput.setInputOrder(input.getInputOrder());
//                        exinput.setRequired(input.isIsRequired());
//                        exinput.setRequiredToCMD(input.isRequiredToAddedToCommandLine());
//                        exinput.save();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Unable to update experiment inputs", e);
//            throw new RegistryException(e);
//        }

    }

    public String addExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expId);
//            for (OutputDataObjectType output : exOutput) {
//                ExperimentOutputResource resource = (ExperimentOutputResource) experiment.create(ResourceType.EXPERIMENT_OUTPUT);
//                resource.setExperimentId(expId);
//                resource.setExperimentKey(output.getName());
//                resource.setValue(output.getValue());
//                if (output.getType() != null) {
//                    resource.setDataType(output.getType().toString());
//                }
//                resource.setRequired(output.isIsRequired());
//                resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                resource.setDataMovement(output.isDataMovement());
//                resource.setDataNameLocation(output.getLocation());
//                resource.setAppArgument(output.getApplicationArgument());
//                resource.setSearchQuery(output.getSearchQuery());
////                resource.setMetadata(output.get());
//                resource.save();
//            }
//        } catch (Exception e) {
//            logger.error("Error while adding experiment outputs...", e);
//            throw new RegistryException(e);
//        }
//        return expId;
        return null;
    }

    public void updateExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expId);
//            List<ExperimentOutputResource> existingExpOutputs = experiment.getExperimentOutputs();
//            for (OutputDataObjectType output : exOutput) {
//                for (ExperimentOutputResource resource : existingExpOutputs) {
//                    if (resource.getExperimentKey().equals(output.getName())) {
//                        resource.setExperimentId(expId);
//                        resource.setExperimentKey(output.getName());
//                        resource.setValue(output.getValue());
//                        if (output.getType() != null) {
//                            resource.setDataType(output.getType().toString());
//                        }
//                        resource.setRequired(output.isIsRequired());
//                        resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                        resource.setDataMovement(output.isDataMovement());
//                        resource.setDataNameLocation(output.getLocation());
//                        resource.setAppArgument(output.getApplicationArgument());
//                        resource.setSearchQuery(output.getSearchQuery());
////                        resource.setMetadata(output.getMetaData());
//                        resource.save();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating experiment outputs", e);
//            throw new RegistryException(e);
//        }
    }

    public String addNodeOutputs(List<OutputDataObjectType> wfOutputs, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getSecondLevelIdentifier());
//            for (OutputDataObjectType output : wfOutputs) {
//                NodeOutputResource resource = (NodeOutputResource) workflowNode.create(ResourceType.NODE_OUTPUT);
//                resource.setNodeId(workflowNode.getNodeInstanceId());
//                resource.setOutputKey(output.getName());
//                resource.setValue(output.getValue());
//                if (output.getType() != null) {
//                    resource.setDataType(output.getType().toString());
//                }
//                resource.setRequired(output.isIsRequired());
//                resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                resource.setDataMovement(output.isDataMovement());
//                resource.setDataNameLocation(output.getLocation());
//                resource.setAppArgument(output.getApplicationArgument());
//                resource.setSearchQuery(output.getSearchQuery());
////                resource.setMetadata(output.getMetaData());
//                resource.save();
//            }
//        } catch (Exception e) {
//            logger.error("Error while adding node outputs...", e);
//            throw new RegistryException(e);
//        }
//        return (String) ids.getSecondLevelIdentifier();
        return null;
    }

    public void updateNodeOutputs(List<OutputDataObjectType> wfOutputs, String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
//            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
//            for (OutputDataObjectType output : wfOutputs) {
//                for (NodeOutputResource resource : nodeOutputs) {
//                    resource.setNodeId(workflowNode.getNodeInstanceId());
//                    resource.setOutputKey(output.getName());
//                    resource.setValue(output.getValue());
//                    if (output.getType() != null) {
//                        resource.setDataType(output.getType().toString());
//                    }
//                    resource.setRequired(output.isIsRequired());
//                    resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                    resource.setDataMovement(output.isDataMovement());
//                    resource.setDataNameLocation(output.getLocation());
//                    resource.setAppArgument(output.getApplicationArgument());
//                    resource.setSearchQuery(output.getSearchQuery());
////                    resource.setMetadata(output.getMetaData());
//                    resource.save();
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating node outputs...", e);
//            throw new RegistryException(e);
//        }
    }

    public String addApplicationOutputs(List<OutputDataObjectType> appOutputs, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getTopLevelIdentifier());
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getSecondLevelIdentifier());
//            for (OutputDataObjectType output : appOutputs) {
//                ApplicationOutputResource resource = (ApplicationOutputResource) taskDetail.create(ResourceType.APPLICATION_OUTPUT);
//                resource.setTaskId(taskDetail.getTaskId());
//                resource.setOutputKey(output.getName());
//                resource.setValue(output.getValue());
//                if (output.getType() != null) {
//                    resource.setDataType(output.getType().toString());
//                }
//                resource.setRequired(output.isIsRequired());
//                resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                resource.setDataMovement(output.isDataMovement());
//                resource.setDataNameLocation(output.getLocation());
//                resource.setAppArgument(output.getApplicationArgument());
//                resource.setSearchQuery(output.getSearchQuery());
////                resource.setMetadata(output.getMetaData());
//                resource.save();
//            }
//        } catch (Exception e) {
//            logger.error("Error while adding application outputs...", e);
//            throw new RegistryException(e);
//        }
//        return (String) ids.getSecondLevelIdentifier();
        return null;
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String expId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expId);
//            StatusResource status = experiment.getExperimentStatus();
//            if (status == null) {
//                status = (StatusResource) experiment.create(ResourceType.STATUS);
//            }
//            status.setExperimentId(expId);
//            status.setStatusUpdateTime(AiravataUtils.getTime(experimentStatus.getTimeOfStateChange()));
////            if (status.getState() == null) {
////                status.setState(ExperimentState.UNKNOWN.name());
////            }
//            if (isValidStatusTransition(ExperimentState.valueOf(status.getState()), experimentStatus.getState())) {
//                status.setState(experimentStatus.getState().toString());
//                status.setStatusType(StatusType.EXPERIMENT.toString());
//                status.save();
//                logger.debug(expId, "Updated experiment {} status to {}.", expId, experimentStatus.toString());
//            }
//        } catch (Exception e) {
//            logger.error(expId, "Error while updating experiment status...", e);
//            throw new RegistryException(e);
//        }
//        return expId;
        return null;
    }

//    public String addWorkflowNodeStatus(WorkflowNodeStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = (StatusResource) experiment.create(ResourceType.STATUS);
//            statusResource.setProcessId(experiment.getExpID());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            if (status.getWorkflowNodeState() == null) {
//                statusResource.setState(WorkflowNodeState.UNKNOWN.toString());
//            } else {
//                statusResource.setState(status.getWorkflowNodeState().toString());
//            }
//            statusResource.save();
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error("Error while adding workflow node status...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public String updateWorkflowNodeStatus(WorkflowNodeStatus status, String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
//            StatusResource statusResource = workflowNode.getWorkflowNodeStatus();
//            if (statusResource == null) {
//                statusResource = (StatusResource) workflowNode.create(ResourceType.STATUS);
//            }
//            statusResource.setProcessId(workflowNode.getProcessId());
//            statusResource.setNodeId(nodeId);
//            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            statusResource.setState(status.getWorkflowNodeState().toString());
//            statusResource.save();
//            logger.debugId(nodeId, "Updated workflow node {} status to {}.", nodeId, status.toString());
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.errorId(nodeId, "Error while updating workflow node status to " + status.toString() + "...", e);
//            throw new RegistryException(e);
//        }
//    }

    public String addTaskStatus(TaskStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getTopLevelIdentifier());
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = (StatusResource) workflowNode.create(ResourceType.STATUS);
//            statusResource.setExperimentId(workflowNode.getExperimentId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setStatusType(StatusType.TASK.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            if (status.getState() == null) {
////              statusResource.setState(TaskState.UNKNOWN.toString());
//            } else {
//                statusResource.setState(status.getState().toString());
//            }
//            statusResource.save();
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error("Error while adding task status...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public void updateTaskStatus(TaskStatus status, String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            StatusResource statusResource;
//            if (taskDetail.isTaskStatusExist(taskId)) {
//                workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//                statusResource = workflowNode.getTaskStatus(taskId);
//            } else {
//                statusResource = (StatusResource) taskDetail.create(ResourceType.STATUS);
//            }
//            statusResource.setExperimentId(workflowNode.getExperimentId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskId);
//            statusResource.setStatusType(StatusType.TASK.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            statusResource.setState(status.getState().toString());
//            statusResource.save();
//            logger.info(taskId, "Updated task {} status to {}.", taskId, status.toString());
//        } catch (Exception e) {
//            logger.error(taskId, "Error while updating task status to " + status.toString() + "...", e);
//            throw new RegistryException(e);
//        }
    }

    /**
     * @param status job status
     * @param ids    composite id will contain taskid and jobid
     * @return status id
     */
    public String addJobStatus(JobStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = (StatusResource) jobDetail.create(ResourceType.STATUS);
//            statusResource.setExperimentId(workflowNode.getExperimentId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setStatusType(StatusType.JOB.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            if (status.getJobState() == null) {
//                statusResource.setState(JobState.UNKNOWN.toString());
//            } else {
//                statusResource.setState(status.getJobState().toString());
//            }
//            statusResource.save();
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error("Error while adding job status...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public String updateJobStatus(JobStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = jobDetail.getJobStatus();
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            statusResource.setExperimentId(workflowNode.getExperimentId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setStatusType(StatusType.JOB.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            statusResource.setState(status.getJobState().toString());
//            statusResource.save();
//            logger.info(ids.toString(), "Updated job status to {}", status.toString());
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error(ids.toString(), "Error while updating job status to " + status.toString() + " ...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    /**
//     * @param status application status
//     * @param ids    composite id will contain taskid and jobid
//     * @return status id
//     */
//    public String addApplicationStatus(ApplicationStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = (StatusResource) jobDetail.create(ResourceType.STATUS);
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            statusResource.setProcessId(workflowNode.getProcessId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setStatusType(StatusType.APPLICATION.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            if (status.getApplicationState() == null) {
//                statusResource.setState("UNKNOWN");
//            } else {
//                statusResource.setState(status.getApplicationState());
//            }
//            statusResource.save();
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error("Unable to read airavata-server properties", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateApplicationStatus(ApplicationStatus status, String jobId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
//            StatusResource statusResource = jobDetail.getApplicationStatus();
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            statusResource.setProcessId(workflowNode.getProcessId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setStatusType(StatusType.APPLICATION.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            statusResource.setState(status.getApplicationState());
//            statusResource.save();
//        } catch (Exception e) {
//            logger.error("Error while updating application status...", e);
//            throw new RegistryException(e);
//        }
//    }


//    public String addTransferStatus(TransferStatus status, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource statusResource = (StatusResource) dataTransferDetail.create(ResourceType.STATUS);
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            statusResource.setProcessId(workflowNode.getProcessId());
//            statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            statusResource.setTaskId(taskDetail.getTaskId());
//            statusResource.setTransferId(dataTransferDetail.getTransferId());
//            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            if (status.getTransferState() == null) {
//                statusResource.setState(TransferState.UNKNOWN.toString());
//            } else {
//                statusResource.setState(status.getTransferState().toString());
//            }
//            statusResource.save();
//            return String.valueOf(statusResource.getStatusId());
//        } catch (Exception e) {
//            logger.error("Error while adding transfer status...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateTransferStatus(TransferStatus status, String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
//            StatusResource statusResource = dataTransferDetail.getDataTransferStatus();
//
//            String taskId = dataTransferDetail.getTaskId();
//            taskDetail = workflowNode.getTaskDetail(taskId);
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//            if (workflowNode != null) {
//                statusResource.setProcessId(workflowNode.getProcessId());
//                statusResource.setNodeId(workflowNode.getNodeInstanceId());
//            }
//            statusResource.setTaskId(taskId);
//            statusResource.setTransferId(transferId);
//            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
//            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
//            statusResource.setState(status.getTransferState().toString());
//            statusResource.save();
//        } catch (Exception e) {
//            logger.error("Error while updating transfer status...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public String addWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String expId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expId);
//            WorkflowNodeDetailResource resource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            resource.setProcessId(expId);
//            resource.setNodeName(nodeDetails.getNodeName());
//            resource.setExecutionUnit(nodeDetails.getExecutionUnit().toString());
//            resource.setExecutionUnitData(nodeDetails.getExecutionUnitData());
//            resource.setCreationTime(AiravataUtils.getTime(nodeDetails.getCreationTime()));
//            resource.setNodeInstanceId(getNodeInstanceID(nodeDetails.getNodeName()));
//            resource.save();
//            String nodeId = resource.getNodeInstanceId();
//            List<InputDataObjectType> nodeInputs = nodeDetails.getNodeInputs();
//            if (nodeInputs != null) {
//                addWorkflowInputs(nodeDetails.getNodeInputs(), resource);
//            }
//            List<OutputDataObjectType> nodeOutputs = nodeDetails.getNodeOutputs();
//            if (nodeOutputs != null && !nodeOutputs.isEmpty()) {
//                CompositeIdentifier ids = new CompositeIdentifier(expId, nodeId);
//                addNodeOutputs(nodeOutputs, ids);
//            }
//            WorkflowNodeStatus workflowNodeStatus = nodeDetails.getWorkflowNodeStatus();
//            CompositeIdentifier ids = new CompositeIdentifier(expId, nodeId);
//            if (workflowNodeStatus == null) {
//                workflowNodeStatus = new WorkflowNodeStatus();
//            }
////                if (workflowNodeStatus.getWorkflowNodeState() != null){
////                    WorkflowNodeStatus status = getWorkflowNodeStatus(nodeId);
////                    if (status != null){
////                        updateWorkflowNodeStatus(workflowNodeStatus, nodeId);
////                    }else {
////                        addWorkflowNodeStatus(workflowNodeStatus,ids);
////                    }
////                }else {
////                    workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.UNKNOWN);
////                    addWorkflowNodeStatus(workflowNodeStatus, ids);
////                }
//            workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.UNKNOWN);
//            addWorkflowNodeStatus(workflowNodeStatus, ids);
//            List<TaskDetails> taskDetails = nodeDetails.getTaskDetailsList();
//            if (taskDetails != null && !taskDetails.isEmpty()) {
//                for (TaskDetails task : taskDetails) {
//                    addTaskDetails(task, nodeId);
//                }
//            }
//            List<ErrorDetails> errors = nodeDetails.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorDetails error : errors) {
//                    addErrorDetails(error, nodeId);
//                }
//            }
//            return nodeId;
//        } catch (Exception e) {
//            logger.error("Error while adding workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
//            workflowNode.setNodeName(nodeDetails.getNodeName());
//            workflowNode.setExecutionUnit(nodeDetails.getExecutionUnit().toString());
//            workflowNode.setExecutionUnitData(nodeDetails.getExecutionUnitData());
//            workflowNode.setCreationTime(AiravataUtils.getTime(nodeDetails.getCreationTime()));
//            workflowNode.setNodeInstanceId(nodeId);
//            workflowNode.save();
//            String expID = workflowNode.getProcessId();
//            List<InputDataObjectType> nodeInputs = nodeDetails.getNodeInputs();
//            if (nodeInputs != null) {
//                updateWorkflowInputs(nodeDetails.getNodeInputs(), workflowNode);
//            }
//            List<OutputDataObjectType> nodeOutputs = nodeDetails.getNodeOutputs();
//            if (nodeOutputs != null && !nodeOutputs.isEmpty()) {
//                updateNodeOutputs(nodeOutputs, nodeId);
//            }
//            WorkflowNodeStatus workflowNodeStatus = nodeDetails.getWorkflowNodeStatus();
//            if (workflowNodeStatus != null) {
//                if (isWFNodeExist(nodeId)) {
//                    updateWorkflowNodeStatus(workflowNodeStatus, nodeId);
//                } else {
//                    CompositeIdentifier ids = new CompositeIdentifier(expID, nodeId);
//                    addWorkflowNodeStatus(workflowNodeStatus, ids);
//                }
//            }
//            List<TaskDetails> taskDetails = nodeDetails.getTaskDetailsList();
//            if (taskDetails != null && !taskDetails.isEmpty()) {
//                for (TaskDetails task : taskDetails) {
//                    String taskID = task.getTaskID();
//                    if (isTaskDetailExist(taskID)) {
//                        updateTaskDetails(task, taskID);
//                    } else {
//                        addTaskDetails(task, nodeId);
//                    }
//                }
//            }
//            List<ErrorDetails> errors = nodeDetails.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorDetails error : errors) {
//                    addErrorDetails(error, nodeId);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }


//    public String addTaskDetails(TaskModel taskDetails, String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            taskDetail.setNodeId(nodeId);
//            taskDetail.setTaskId(getTaskID(workflowNode.getNodeName()));
//            taskDetail.setApplicationId(taskDetails.getApplicationId());
//            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
//            taskDetail.setCreationTime(AiravataUtils.getTime(taskDetails.getCreationTime()));
//            taskDetail.setEnableEmailNotifications(taskDetails.isEnableEmailNotification());
//            taskDetail.save();
//
//            List<String> emailAddresses = taskDetails.getEmailAddresses();
//            if (emailAddresses != null && !emailAddresses.isEmpty()){
//                for (String email : emailAddresses){
//                    NotificationEmailResource emailResource = new NotificationEmailResource();
//                    emailResource.setProcessId(workflowNode.getProcessId());
//                    emailResource.setTaskId(taskDetail.getTaskId());
//                    emailResource.setEmailAddress(email);
//                    emailResource.save();
//                }
//            }
//
//            List<InputDataObjectType> applicationInputs = taskDetails.getApplicationInputs();
//            if (applicationInputs != null) {
//                addAppInputs(applicationInputs, taskDetail);
//            }
//            List<OutputDataObjectType> applicationOutput = taskDetails.getApplicationOutputs();
//            if (applicationOutput != null) {
//                addAppOutputs(applicationOutput, taskDetail);
//            }
//            ComputationalResourceSchedulingModel taskScheduling = taskDetails.getTaskScheduling();
//            if (taskScheduling != null) {
//                addComputationScheduling(taskScheduling, taskDetail);
//            }
////            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
////            if (inputDataHandling != null) {
////                addInputDataHandling(inputDataHandling, taskDetail);
////            }
////            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
////            if (outputDataHandling != null) {
////                addOutputDataHandling(outputDataHandling, taskDetail);
////            }
//
//            List<JobModel> jobDetailsList = taskDetails.getJobDetailsList();
//            if (jobDetailsList != null && !jobDetailsList.isEmpty()) {
//                for (JobDetails job : jobDetailsList) {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskDetail.getTaskId(), job.getJobID());
//                    addJobDetails(job, ids);
//                }
//            }
//
//            List<DataTransferDetails> dataTransferDetailsList = taskDetails.getDataTransferDetailsList();
//            if (dataTransferDetailsList != null && !dataTransferDetailsList.isEmpty()) {
//                for (DataTransferDetails transferDetails : dataTransferDetailsList) {
//                    addDataTransferDetails(transferDetails, taskDetail.getTaskId());
//                }
//            }
//
//            List<ErrorDetails> errors = taskDetails.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorDetails error : errors) {
//                    addErrorDetails(error, taskDetail.getTaskId());
//                }
//            }
//
//            TaskStatus taskStatus = taskDetails.getTaskStatus();
//            CompositeIdentifier ids = new CompositeIdentifier(nodeId, taskDetail.getTaskId());
//            if (taskStatus != null) {
//                if (taskStatus.getExecutionState() != null) {
//                    addTaskStatus(taskStatus, ids);
//                } else {
//                    taskStatus.setExecutionState(TaskState.UNKNOWN);
//                    addTaskStatus(taskStatus, ids);
//                }
//            } else {
//                TaskStatus status = new TaskStatus();
//                status.setExecutionState(TaskState.UNKNOWN);
//                addTaskStatus(status, ids);
//            }
//            return taskDetail.getTaskId();
//        } catch (Exception e) {
//            logger.error("Error while adding task details...", e);
//            throw new RegistryException(e);
//        }
//    }

//    public String updateTaskDetails(TaskModel taskDetails, String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
////            taskDetail.setWorkflowNodeDetailResource(workflowNode);
//            taskDetail.setApplicationId(taskDetails.getApplicationId());
//            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
//            taskDetail.setCreationTime(AiravataUtils.getTime(taskDetails.getCreationTime()));
//            taskDetail.setApplicationDeploymentId(taskDetails.getApplicationDeploymentId());
//            taskDetail.setEnableEmailNotifications(taskDetails.isEnableEmailNotification());
//            taskDetail.save();
//
//            workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//
//            List<String> emailAddresses = taskDetails.getEmailAddresses();
//            // remove existing emails
//            taskDetail.remove(ResourceType.NOTIFICATION_EMAIL, taskId);
//            if (emailAddresses != null && !emailAddresses.isEmpty()){
//                for (String email : emailAddresses){
//                    NotificationEmailResource emailResource = new NotificationEmailResource();
//                    emailResource.setProcessId(workflowNode.getProcessId());
//                    emailResource.setTaskId(taskId);
//                    emailResource.setEmailAddress(email);
//                    emailResource.save();
//                }
//            }
//            List<InputDataObjectType> applicationInputs = taskDetails.getApplicationInputs();
//            if (applicationInputs != null) {
//                updateAppInputs(applicationInputs, taskDetail);
//            }
//            ComputationalResourceScheduling taskScheduling = taskDetails.getTaskScheduling();
//            if (taskScheduling != null) {
//                updateSchedulingData(taskScheduling, taskDetail);
//            }
//            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
//            if (inputDataHandling != null) {
//                updateInputDataHandling(inputDataHandling, taskDetail);
//            }
//            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
//            if (outputDataHandling != null) {
//                updateOutputDataHandling(outputDataHandling, taskDetail);
//            }
//            List<JobDetails> jobDetailsList = taskDetails.getJobDetailsList();
//            if (jobDetailsList != null && !jobDetailsList.isEmpty()) {
//                for (JobDetails job : jobDetailsList) {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskId, job.getJobID());
//                    updateJobDetails(job, ids);
//                }
//            }
//
//            List<DataTransferDetails> dataTransferDetailsList = taskDetails.getDataTransferDetailsList();
//            if (dataTransferDetailsList != null && !dataTransferDetailsList.isEmpty()) {
//                for (DataTransferDetails transferDetails : dataTransferDetailsList) {
//                    updateDataTransferDetails(transferDetails, transferDetails.getTransferID());
//                }
//            }
//
//            List<ErrorDetails> errors = taskDetails.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorDetails error : errors) {
//                    addErrorDetails(error, taskDetail.getTaskId());
//                }
//            }
//
//            TaskStatus taskStatus = taskDetails.getTaskStatus();
//            if (taskStatus != null) {
//                updateTaskStatus(taskStatus, taskId);
//            }
//            return taskDetail.getTaskId();
//        } catch (Exception e) {
//            logger.error("Error while updating task details...", e);
//            throw new RegistryException(e);
//        }
//    }

    public void updateAppOutputs(List<OutputDataObjectType> appOutputs, String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            List<ApplicationOutputResource> outputs = taskDetail.getApplicationOutputs();
//            for (OutputDataObjectType output : appOutputs) {
//                for (ApplicationOutputResource resource : outputs) {
//                    resource.setTaskId(taskId);
//                    resource.setOutputKey(output.getName());
//                    resource.setValue(output.getValue());
//                    if (output.getType() != null) {
//                        resource.setDataType(output.getType().toString());
//                    }
//                    resource.setRequired(output.isIsRequired());
//                    resource.setRequiredToCMD(output.isRequiredToAddedToCommandLine());
//                    resource.setDataMovement(output.isDataMovement());
//                    resource.setDataNameLocation(output.getLocation());
//                    resource.setAppArgument(output.getApplicationArgument());
//                    resource.setSearchQuery(output.getSearchQuery());
////                    resource.setMetadata(output.getMetaData());
//                    resource.save();
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating application outputs...", e);
//            throw new RegistryException(e);
//        }
    }

    public String addJobDetails(JobModel jobDetails, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.createJobDetail((String) ids.getSecondLevelIdentifier());
//            jobDetail.setTaskId(taskDetail.getTaskId());
//            jobDetail.setJobDescription(jobDetails.getJobDescription());
//            jobDetail.setCreationTime(AiravataUtils.getTime(jobDetails.getCreationTime()));
//            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
//            jobDetail.setWorkingDir(jobDetails.getWorkingDir());
//            jobDetail.setJobName(jobDetails.getJobName());
//            jobDetail.save();
//            JobStatus jobStatus = jobDetails.getJobStatus();
//            if (jobStatus != null) {
//                JobStatus status = getJobStatus(ids);
//                if (status != null) {
//                    updateJobStatus(jobStatus, ids);
//                } else {
//                    addJobStatus(jobStatus, ids);
//                }
//            }
//            JobStatus applicationStatus = jobDetails.getJobStatus();
//            if (applicationStatus != null) {
//                JobStatus appStatus = getJobStatus(ids);
//                if (appStatus != null) {
//                    updateJobStatus(applicationStatus, ids);
//                } else {
//                    addJobStatus(applicationStatus, ids);
//                }
//            }
////            List<ErrorModel> errors = jobDetails.getErrors();
////            if (errors != null && !errors.isEmpty()) {
////                for (ErrorDetails error : errors) {
////                    addErrorDetails(error, ids.getSecondLevelIdentifier());
////                }
////            }
//            return jobDetail.getJobId();
//        } catch (Exception e) {
//            logger.error("Error while adding job details...", e);
//            throw new RegistryException(e);
//        }
        return  null;
    }

    // ids - taskId + jobid
    public void updateJobDetails(JobModel jobDetails, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            String taskId = (String) ids.getTopLevelIdentifier();
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            String jobId = (String) ids.getSecondLevelIdentifier();
//            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
//            jobDetail.setTaskId(taskDetail.getTaskId());
//            jobDetail.setJobDescription(jobDetails.getJobDescription());
//            jobDetail.setCreationTime(AiravataUtils.getTime(jobDetails.getCreationTime()));
//            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
//            jobDetail.setJobName(jobDetails.getJobName());
//            jobDetail.setWorkingDir(jobDetails.getWorkingDir());
//            jobDetail.save();
//            JobStatus jobStatus = jobDetails.getJobStatus();
//            if (jobStatus != null) {
//                JobStatus status = getJobStatus(ids);
//                if (status != null) {
//                    updateJobStatus(jobStatus, ids);
//                } else {
//                    addJobStatus(jobStatus, ids);
//                }
//            }
//            JobStatus applicationStatus = jobDetails.getJobStatus();
//            if (applicationStatus != null) {
//                JobStatus appStatus = getJobStatus(ids);
//                if (appStatus != null) {
//                    updateJobStatus(applicationStatus, ids);
//                } else {
//                    addJobStatus(applicationStatus, ids);
//                }
//            }
////            List<ErrorDetails> errors = jobDetails.getErrors();
////            if (errors != null && !errors.isEmpty()) {
////                for (ErrorDetails error : errors) {
////                    addErrorDetails(error, jobId);
////                }
////            }
//        } catch (Exception e) {
//            logger.error("Error while updating job details...", e);
//            throw new RegistryException(e);
//        }
    }

//    public String addDataTransferDetails(DataTransferDetails transferDetails, String taskId) throws RegistryException {
//        try {
//            if (transferDetails.getTransferDescription() == null){
//                throw new RegistryException("Data transfer description cannot be empty");
//            }
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            DataTransferDetailResource resource = (DataTransferDetailResource) taskDetail.create(ResourceType.DATA_TRANSFER_DETAIL);
//            resource.setTaskId(taskId);
//            resource.setTransferId(getDataTransferID(taskId));
//
//            resource.setTransferDescription(transferDetails.getTransferDescription());
//            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
//            resource.save();
//            String transferId = resource.getTransferId();
//            TransferStatus transferStatus = transferDetails.getTransferStatus();
//            if (transferStatus != null) {
//                TransferStatus status = getDataTransferStatus(transferId);
//                if (status != null) {
//                    updateTransferStatus(transferStatus, transferId);
//                } else {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
//                    addTransferStatus(transferStatus, ids);
//                }
//            }
//            return resource.getTransferId();
//        } catch (Exception e) {
//            logger.error("Error while adding transfer details...", e);
//            throw new RegistryException(e);
//        }
//    }

//    public String updateDataTransferDetails(DataTransferDetails transferDetails, String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource resource = taskDetail.getDataTransferDetail(transferId);
////            resource.setTaskDetailResource(taskDetail);
//            resource.setTransferDescription(transferDetails.getTransferDescription());
//            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
//            resource.save();
//            String taskId = resource.getTaskId();
//            TransferStatus transferStatus = transferDetails.getTransferStatus();
//            if (transferStatus != null) {
//                TransferStatus status = getDataTransferStatus(transferId);
//                if (status != null) {
//                    updateTransferStatus(transferStatus, transferId);
//                } else {
//                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
//                    addTransferStatus(transferStatus, ids);
//                }
//            }
//            return resource.getTransferId();
//        } catch (Exception e) {
//            logger.error("Error while updating transfer details...", e);
//            throw new RegistryException(e);
//        }
//    }

    /**
     * @param scheduling computational resource object
     * @param ids        contains expId and taskId, if it is an experiment, task id can be null
     * @return scheduling id
     */
    public String addComputationalResourceScheduling(ComputationalResourceSchedulingModel scheduling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource) experiment.create(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                schedulingResource.setTaskId(taskDetail.getTaskId());
//            }
//            schedulingResource.setExperimentId(experiment.getExpID());
//            schedulingResource.setResourceHostId(scheduling.getResourceHostId());
//            schedulingResource.setCpuCount(scheduling.getTotalCPUCount());
//            schedulingResource.setNodeCount(scheduling.getNodeCount());
//            schedulingResource.setNumberOfThreads(scheduling.getNumberOfThreads());
//            schedulingResource.setQueueName(scheduling.getQueueName());
//            schedulingResource.setWalltimeLimit(scheduling.getWallTimeLimit());
//            schedulingResource.setPhysicalMemory(scheduling.getTotalPhysicalMemory());
//            schedulingResource.setChessisName(scheduling.getChessisNumber());
//            schedulingResource.save();
//            return String.valueOf(schedulingResource.getSchedulingId());
//        } catch (Exception e) {
//            logger.error("Error while adding scheduling parameters...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    /**
//     * @param dataHandling advanced input data handling object
//     * @param ids          contains expId and taskId
//     * @return data handling id
//     */
//    public String addInputDataHandling(AdvancedInputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            AdvanceInputDataHandlingResource dataHandlingResource = (AdvanceInputDataHandlingResource) experiment.create(ResourceType.ADVANCE_INPUT_DATA_HANDLING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                dataHandlingResource.setTaskId(taskDetail.getTaskId());
//            }
//            dataHandlingResource.setProcessId(experiment.getExpID());
//            dataHandlingResource.setWorkingDir(dataHandling.getUniqueWorkingDirectory());
//            dataHandlingResource.setWorkingDirParent(dataHandling.getParentWorkingDirectory());
//            dataHandlingResource.setStageInputFiles(dataHandling.isStageInputFilesToWorkingDir());
//            dataHandlingResource.setCleanAfterJob(dataHandling.isCleanUpWorkingDirAfterJob());
//            dataHandlingResource.save();
//            return String.valueOf(dataHandlingResource.getDataHandlingId());
//        } catch (Exception e) {
//            logger.error("Error while adding input data handling...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    /**
//     * @param dataHandling advanced output data handling object
//     * @param ids          contains expId and taskId
//     * @return data handling id
//     */
//    public String addOutputDataHandling(AdvancedOutputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            AdvancedOutputDataHandlingResource dataHandlingResource = (AdvancedOutputDataHandlingResource) experiment.create(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                dataHandlingResource.setTaskId(taskDetail.getTaskId());
//            }
//            dataHandlingResource.setProcessId(experiment.getExpID());
//            dataHandlingResource.setOutputDataDir(dataHandling.getOutputDataDir());
//            dataHandlingResource.setDataRegUrl(dataHandling.getDataRegistryURL());
//            dataHandlingResource.setPersistOutputData(dataHandling.isPersistOutputData());
//            dataHandlingResource.save();
//            return String.valueOf(dataHandlingResource.getOutputDataHandlingId());
//        } catch (Exception e) {
//            logger.error("Error while adding output data handling...", e);
//            throw new RegistryException(e);
//        }
//    }

//    public String addQosParams(QualityOfServiceParams qosParams, CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
//            QosParamResource qosParamResource = (QosParamResource) experiment.create(ResourceType.QOS_PARAM);
//            if (ids.getSecondLevelIdentifier() != null) {
//                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
//                qosParamResource.setTaskId(taskDetail.getTaskId());
//            }
//            qosParamResource.setProcessId(experiment.getExpID());
//            qosParamResource.setStartExecutionAt(qosParams.getStartExecutionAt());
//            qosParamResource.setExecuteBefore(qosParams.getExecuteBefore());
//            qosParamResource.setNoOfRetries(qosParams.getNumberofRetries());
//            qosParamResource.save();
//            return String.valueOf(qosParamResource.getQosId());
//        } catch (Exception e) {
//            logger.error("Error while adding QOS params...", e);
//            throw new RegistryException(e);
//        }
//    }

    public String addErrorDetails(ErrorModel error, Object id) throws RegistryException {
//        try {
//
//            ErrorDetailResource errorResource = null;
//            ExperimentResource experiment;
//            TaskDetailResource taskDetail;
//            WorkflowNodeDetailResource workflowNode;
//            // figure out the id is an experiment, node task or job
//            if (id instanceof String) {
//                // FIXME : for .12 we only save task related errors
////                if (isExperimentExist((String) id)) {
////                    experiment = gatewayResource.getExperiment((String) id);
////                    errorResource = (ErrorDetailResource) experiment.create(ResourceType.ERROR_DETAIL);
////                } else if (isWFNodeExist((String) id)) {
////                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
////                    workflowNode = experiment.getWorkflowNode((String) id);
////                    errorResource = (ErrorDetailResource) workflowNode.create(ResourceType.ERROR_DETAIL);
////                    errorResource.setExperimentResource(workflowNode.getExperimentResource());
////                } else
//                if (isTaskDetailExist((String) id)) {
//                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    taskDetail = workflowNode.getTaskDetail((String) id);
//                    errorResource = (ErrorDetailResource) taskDetail.create(ResourceType.ERROR_DETAIL);
//                    if (error.getErrorId() != null && !error.getErrorId().equals(airavata_commonsConstants.DEFAULT_ID)) {
//                        List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                        if (errorDetailList != null && !errorDetailList.isEmpty()) {
//                            for (ErrorDetailResource errorDetailResource : errorDetailList) {
//                                if (errorDetailResource.getErrorId() == Integer.parseInt(error.getErrorId())) {
//                                    errorResource = errorDetailResource;
//                                }
//                            }
//                        }
//                    }
//                    errorResource.setTaskId(taskDetail.getTaskId());
//                    errorResource.setNodeId(taskDetail.getNodeId());
//                    workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//                    errorResource.setExperimentId(workflowNode.getExperimentId());
//                } else {
////                    logger.error("The id provided is not an experiment id or a workflow id or a task id..");
//                }
//            } else if (id instanceof CompositeIdentifier) {
//                CompositeIdentifier cid = (CompositeIdentifier) id;
//                if (isJobDetailExist(cid)) {
//                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
//                    JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
//                    errorResource = (ErrorDetailResource) jobDetail.create(ResourceType.ERROR_DETAIL);
//                    if (error.getErrorId() != null && !error.getErrorId().equals(airavata_commonsConstants.DEFAULT_ID)) {
//                        List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                        if (errorDetailList != null && !errorDetailList.isEmpty()) {
//                            for (ErrorDetailResource errorDetailResource : errorDetailList) {
//                                if (errorDetailResource.getErrorId() == Integer.parseInt(error.getErrorId())) {
//                                    errorResource = errorDetailResource;
//                                }
//                            }
//                        }
//                    }
//                    errorResource.setTaskId(taskDetail.getTaskId());
//                    errorResource.setNodeId(taskDetail.getNodeId());
//                    workflowNode = experiment.getWorkflowNode(taskDetail.getNodeId());
//                    errorResource.setExperimentId(workflowNode.getExperimentId());
//                } else {
//                    logger.error("The id provided is not a job in the system..");
//                }
//            } else {
////                logger.error("The id provided is not an experiment id or a workflow id or a task id or a composite " +
////                        "identifier for job..");
//            }
//            if (errorResource != null) {
//                errorResource.setCreationTime(AiravataUtils.getTime(error.getCreationTime()));
//                errorResource.setActualErrorMsg(error.getActualErrorMessage());
//                errorResource.setUserFriendlyErrorMsg(error.getUserFriendlyMessage());
//                errorResource.setTransientPersistent(error.isTransientOrPersistent());
//                errorResource.save();
//                return String.valueOf(errorResource.getErrorId());
//            }
//        } catch (Exception e) {
//            logger.error("Unable to add error details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public String getNodeInstanceID(String nodeName) {
        String node = nodeName.replaceAll("\\s", "");
        return node + "_" + UUID.randomUUID();
    }

    public String getExperimentID(String experimentName) {
        String exp = experimentName.replaceAll("\\s", "");
        return exp + "_" + UUID.randomUUID();
    }

    public String getTaskID(String nodeName) {
        String node = nodeName.replaceAll("\\s", "");
        return node + "_" + UUID.randomUUID();
    }

    public String getDataTransferID(String taskId) {
        String task = taskId.replaceAll("\\s", "");
        return task + "_" + UUID.randomUUID();
    }

    public void updateExperimentField(String expID, String fieldName, Object value) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expID);
//            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
//                experiment.setExpName((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
//                experiment.setExecutionUser((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
//                experiment.setDescription((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
//                experiment.setApplicationId((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
//                experiment.setApplicationVersion((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
//                experiment.setWorkflowTemplateId((String) value);
//                experiment.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
//                experiment.setWorkflowTemplateVersion((String) value);
//                experiment.save();
//            } else {
//                logger.error("Unsupported field type for Experiment");
//            }
//
//        } catch (Exception e) {
//            logger.error("Error while updating fields in experiment...", e);
//            throw new RegistryException(e);
//        }
    }

    public void updateUserConfigDataField(String expID, String fieldName, Object value) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expID);
//            ConfigDataResource exConfigData = (ConfigDataResource) experiment.get(ResourceType.CONFIG_DATA, expID);
//            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
//                exConfigData.setAiravataAutoSchedule((Boolean) value);
//                exConfigData.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
//                exConfigData.setOverrideManualParams((Boolean) value);
//                exConfigData.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
//                exConfigData.setShareExp((Boolean) value);
//                exConfigData.save();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
//                updateSchedulingData((ComputationalResourceSchedulingModel) value, experiment);
//            } else {
//                logger.error("Unsupported field type for Experiment config data");
//            }
//
//        } catch (Exception e) {
//            logger.error("Error while updating fields in experiment config...", e);
//            throw new RegistryException(e);
//        }
    }

    public void updateExperiment(ExperimentModel experiment, String expId) throws RegistryException {
//        try {
//            ExperimentResource existingExperiment = gatewayResource.getExperiment(expId);
//            existingExperiment.setExpName(experiment.getExperimentName());
//            existingExperiment.setExecutionUser(experiment.getUserName());
//            existingExperiment.setGatewayId(gatewayResource.getGatewayId());
//            existingExperiment.setGatewayExecutionId(experiment.getGatewayExecutionId());
//            if (!workerResource.isProjectExists(experiment.getProjectId())) {
//                logger.error("Project does not exist in the system..");
//                throw new Exception("Project does not exist in the system, Please create the project first...");
//            }
//            existingExperiment.setProjectId(experiment.getProjectId());
//            existingExperiment.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
//            existingExperiment.setDescription(experiment.getDescription());
//            existingExperiment.setApplicationId(experiment.getExecutionId());
//            existingExperiment.setEnableEmailNotifications(experiment.isEnableEmailNotification());
//            existingExperiment.save();
//
//            List<String> emailAddresses = experiment.getEmailAddresses();
//            // remove existing email addresses
//            existingExperiment.remove(ResourceType.NOTIFICATION_EMAIL, expId);
//            if (emailAddresses != null && !emailAddresses.isEmpty()){
//                for (String email : emailAddresses){
//                    NotificationEmailResource emailResource = new NotificationEmailResource();
//                    emailResource.setExperimentId(expId);
//                    emailResource.setEmailAddress(email);
//                    emailResource.save();
//                }
//            }
//
//            List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
//            if (experimentInputs != null && !experimentInputs.isEmpty()) {
//                updateExpInputs(experimentInputs, existingExperiment);
//            }
//
//            UserConfigurationDataModel userConfigurationData = experiment.getUserConfigurationData();
//            if (userConfigurationData != null) {
//                updateUserConfigData(userConfigurationData, expId);
//            }
//
//            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
//            if (experimentOutputs != null && !experimentOutputs.isEmpty()) {
//                updateExpOutputs(experimentOutputs, expId);
//            }
//            ExperimentStatus experimentStatus = experiment.getExperimentStatus();
//            if (experimentStatus != null) {
//                updateExperimentStatus(experimentStatus, expId);
//            }
////            List<WorkflowNodeDetails> workflowNodeDetailsList = experiment.getWorkflowNodeDetailsList();
////            if (workflowNodeDetailsList != null && !workflowNodeDetailsList.isEmpty()) {
////                for (WorkflowNodeDetails wf : workflowNodeDetailsList) {
////                    updateWorkflowNodeDetails(wf, wf.getNodeInstanceId());
////                }
////            }
//            List<ErrorModel> errors = experiment.getErrors();
//            if (errors != null && !errors.isEmpty()) {
//                for (ErrorModel errror : errors) {
//                    addErrorDetails(errror, expId);
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating experiment...", e);
//            throw new RegistryException(e);
//        }

    }

    public void updateUserConfigData(UserConfigurationDataModel configData, String expId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expId);
//            ConfigDataResource resource = (ConfigDataResource) experiment.get(ResourceType.CONFIG_DATA, expId);
//            resource.setExperimentId(expId);
//            resource.setAiravataAutoSchedule(configData.isAiravataAutoSchedule());
//            resource.setOverrideManualParams(configData.isOverrideManualScheduledParams());
//            resource.setShareExp(configData.isShareExperimentPublicly());
//            resource.setUserDn(configData.getUserDN());
//            resource.setGenerateCert(configData.isGenerateCert());
//            resource.save();
//            ComputationalResourceSchedulingModel resourceScheduling = configData.getComputationalResourceScheduling();
//            if (resourceScheduling != null) {
//                updateSchedulingData(resourceScheduling, experiment);
//            }
////            AdvancedInputDataHandling inputDataHandling = configData.getAdvanceInputDataHandling();
////            if (inputDataHandling != null) {
////                updateInputDataHandling(inputDataHandling, experiment);
////            }
////            AdvancedOutputDataHandling outputDataHandling = configData.getAdvanceOutputDataHandling();
////            if (outputDataHandling != null) {
////                updateOutputDataHandling(outputDataHandling, experiment);
////            }
////
////            QualityOfServiceParams qosParams = configData.getQosParams();
////            if (qosParams != null) {
////                updateQosParams(qosParams, experiment);
////            }
//        } catch (Exception e) {
//            logger.error("Error while updating user config data...", e);
//            throw new RegistryException(e);
//        }

    }

//    public void updateQosParams(QualityOfServiceParams qosParams, ExperimentCatResource resource) throws RegistryException {
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                QosParamResource qosr = expResource.getQOSparams(expResource.getExpID());
//                qosr.setProcessId(expResource.getExpID());
//                qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
//                qosr.setExecuteBefore(qosParams.getExecuteBefore());
//                qosr.setNoOfRetries(qosParams.getNumberofRetries());
//                qosr.save();
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating QOS data...", e);
//            throw new RegistryException(e);
//        }
//
//    }

//    public void updateOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvancedOutputDataHandlingResource adodh;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                adodh = expResource.getOutputDataHandling(expResource.getExpID());
//                adodh.setProcessId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                adodh = taskDetailResource.getOutputDataHandling(taskDetailResource.getTaskId());
//                adodh.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                adodh.setProcessId(nodeDetailResource.getProcessId());
//            }
//            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
//            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
//            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
//            adodh.save();
//        } catch (Exception e) {
//            logger.error("Error while updating output data handling...", e);
//            throw new RegistryException(e);
//        }
//
//    }
//
//    public void updateInputDataHandling(AdvancedInputDataHandling inputDataHandling, ExperimentCatResource resource) throws RegistryException {
//        AdvanceInputDataHandlingResource adidh;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                adidh = expResource.getInputDataHandling(expResource.getExpID());
//                adidh.setProcessId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                adidh = taskDetailResource.getInputDataHandling(taskDetailResource.getTaskId());
//                adidh.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                adidh.setProcessId(nodeDetailResource.getProcessId());
//            }
//            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
//            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
//            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
//            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
//            adidh.save();
//        } catch (Exception e) {
//            logger.error("Error while updating input data handling...", e);
//            throw new RegistryException(e);
//        }
//
//    }

    public void updateSchedulingData(ComputationalResourceSchedulingModel resourceScheduling, ExperimentCatResource resource) throws RegistryException {
//        ComputationSchedulingResource cmsr;
//        try {
//            if (resource instanceof ExperimentResource) {
//                ExperimentResource expResource = (ExperimentResource) resource;
//                cmsr = expResource.getComputationScheduling(expResource.getExpID());
//                cmsr.setExperimentId(expResource.getExpID());
//            } else {
//                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
//                ExperimentResource experimentResource = new ExperimentResource();
//                cmsr = taskDetailResource.getComputationScheduling(taskDetailResource.getTaskId());
//                cmsr.setTaskId(taskDetailResource.getTaskId());
//                WorkflowNodeDetailResource nodeDetailResource = experimentResource.getWorkflowNode(taskDetailResource.getNodeId());
//                cmsr.setExperimentId(nodeDetailResource.getExperimentId());
//            }
//            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
//            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
//            cmsr.setNodeCount(resourceScheduling.getNodeCount());
//            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
//            cmsr.setQueueName(resourceScheduling.getQueueName());
//            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
//            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
//            cmsr.save();
//        } catch (Exception e) {
//            logger.error("Error while updating scheduling data...", e);
//            throw new RegistryException(e);
//        }
    }

    /**
     * Method to get matching experiment list
     * @param fieldName
     * @param value
     * @return
     * @throws RegistryException
     */
    public List<ExperimentModel> getExperimentList(String fieldName, Object value) throws RegistryException {
//        List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
//        try {
//            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
//                WorkerResource resource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
//                resource.setUser((String) value);
//                List<ExperimentResource> resources = resource.getExperiments();
//                for (ExperimentResource experimentResource : resources) {
//                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(experimentResource);
//                    experiments.add(experiment);
//                }
//                return experiments;
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
//                ProjectResource project = workerResource.getProject((String) value);
//                List<ExperimentResource> resources = project.getExperiments();
//                for (ExperimentResource resource : resources) {
//                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
//                    experiments.add(experiment);
//                }
//                return experiments;
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
//                List<ExperimentResource> resources = gatewayResource.getExperiments();
//                for (ExperimentResource resource : resources) {
//                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
//                    experiments.add(experiment);
//                }
//                return experiments;
//            }
////            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)) {
////                if (value instanceof List<?>) {
////                    return getExperimentList(fieldName, ((List<?>) value).get(0));
////                } else if (value instanceof WorkflowNodeDetails) {
////                    WorkflowNodeDetailResource nodeDetailResource = getWorkflowNodeDetailResource(((WorkflowNodeDetails) value).getNodeInstanceId());
////                    if (nodeDetailResource != null) {
////                        String experimentId = nodeDetailResource.getProcessId();
////                        ExperimentResource experiment = gatewayResource.getExperiment(experimentId);
////                        return Arrays.asList(ThriftDataModelConversion
////                                .getExperiment(experiment));
////                    }
////                } else {
////                    logger.error("Unsupported field value to retrieve workflow node detail list...");
////                }
////
////            } else {
////                logger.error("Unsupported field name to retrieve experiment list...");
////            }
//        } catch (Exception e) {
//            logger.error("Error while getting experiment list...", e);
//            throw new RegistryException(e);
//        }
//        return experiments;
        return null;
    }

    /**
     * Method to get matching experiment list with pagination and ordering
     * @param fieldName
     * @param value
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<ExperimentModel> getExperimentList(String fieldName, Object value, int limit, int offset,
                                              Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource resource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
                resource.setUser((String) value);
                List<ExperimentResource> resources = resource.getExperiments(limit, offset,
                        orderByIdentifier, resultOrderType);
                for (ExperimentResource experimentResource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(experimentResource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                ProjectResource project = workerResource.getProject((String) value);
                List<ExperimentResource> resources = project.getExperiments(limit, offset,
                        Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
                for (ExperimentResource resource : resources) {
                    ExperimentModel experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            }
            logger.error("Unsupported field name to retrieve experiment list...");
        } catch (Exception e) {
            logger.error("Error while getting experiment list...", e);
            throw new RegistryException(e);
        }
        return experiments;
    }


    public List<TaskModel> getTaskDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.TaskDetailConstants.NODE_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
//                List<TaskDetailResource> taskDetails = workflowNode.getTaskDetails();
//                return ThriftDataModelConversion.getTaskDetailsList(taskDetails);
//            } else {
//                logger.error("Unsupported field name to retrieve task detail list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting task details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public List<JobModel> getJobDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.JobDetaisConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<JobDetailResource> jobDetailList = taskDetail.getJobDetailList();
//                return ThriftDataModelConversion.getJobDetailsList(jobDetailList);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while job details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }
//
//    public List<DataTransferDetails> getDataTransferDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.DataTransferDetailConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<DataTransferDetailResource> dataTransferDetailList = taskDetail.getDataTransferDetailList();
//                return ThriftDataModelConversion.getDataTransferlList(dataTransferDetailList);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer details...", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }

    public List<ErrorModel> getErrorDetails(String fieldName, Object value) throws RegistryException {
//        try {
//            if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.EXPERIMENT_ID)) {
//                ExperimentResource experiment = gatewayResource.getExperiment((String) value);
//                List<ErrorDetailResource> errorDetails = experiment.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.NODE_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
//                List<ErrorDetailResource> errorDetails = workflowNode.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.TASK_ID)) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
//                List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetailList);
//            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.JOB_ID)) {
//                CompositeIdentifier cid = (CompositeIdentifier) value;
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
//                JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
//                List<ErrorDetailResource> errorDetails = jobDetail.getErrorDetails();
//                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
//            } else {
//                logger.error("Unsupported field name to retrieve job details list...");
//            }
//        } catch (Exception e) {
//            logger.error("Unable to get error details...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public Object getExperiment(String expId, String fieldName) throws RegistryException {
//        try {
//            ExperimentResource resource = gatewayResource.getExperiment(expId);
//            if (fieldName == null) {
//                return ThriftDataModelConversion.getExperiment(resource);
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
//                return resource.getExecutionUser();
//            }else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
//                return resource.getGatewayId();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
//                return resource.getExpName();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
//                return resource.getDescription();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
//                return resource.getApplicationId();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
//                return resource.getProjectId();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
//                return resource.getApplicationVersion();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
//                return resource.getWorkflowTemplateId();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
//                return resource.getWorkflowTemplateId();
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS)) {
//                return ThriftDataModelConversion.getExpInputs(resource.getExperimentInputs());
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS)) {
//                return ThriftDataModelConversion.getExpOutputs(resource.getExperimentOutputs());
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
//                return ThriftDataModelConversion.getExperimentStatus(resource.getExperimentStatus());
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_CONFIGURATION_DATA)) {
//                return ThriftDataModelConversion.getUserConfigData(resource.getUserConfigData(expId));
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_EXECUTION_ID)) {
//                return resource.getWorkflowExecutionId();
//            }
////            else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.STATE_CHANGE_LIST)) {
////                return ThriftDataModelConversion.getWorkflowNodeStatusList(resource.getWorkflowNodeStatuses());
////            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)) {
////                return ThriftDataModelConversion.getWfNodeList(resource.getWorkflowNodeDetails());
////            }
//            else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.ERROR_DETAIL_LIST)) {
//                return ThriftDataModelConversion.getErrorDetailList(resource.getErrorDetails());
//            } else {
//                logger.error("Unsupported field name for experiment basic data..");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting experiment info...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public Object getUserConfigData(String expId, String fieldName) throws RegistryException {
//        try {
//            ExperimentResource resource = gatewayResource.getExperiment(expId);
//            ConfigDataResource userConfigData = resource.getUserConfigData(expId);
//            if (fieldName == null) {
//                return ThriftDataModelConversion.getUserConfigData(userConfigData);
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
//                return userConfigData.isAiravataAutoSchedule();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
//                return userConfigData.isOverrideManualParams();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
//                return userConfigData.isShareExp();
//            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
//                return ThriftDataModelConversion.getComputationalResourceScheduling(resource.getComputationScheduling(expId));
////            }
////            else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)) {
////                return ThriftDataModelConversion.getAdvanceInputDataHandling(resource.getInputDataHandling(expId));
////            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)) {
////                return ThriftDataModelConversion.getAdvanceOutputDataHandling(resource.getOutputDataHandling(expId));
////            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)) {
////                return ThriftDataModelConversion.getQOSParams(resource.getQOSparams(expId));
//            } else {
//                logger.error("Unsupported field name for experiment configuration data..");
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting config data..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String expId) throws RegistryException {
//        try {
//            ExperimentResource resource = gatewayResource.getExperiment(expId);
//            List<ExperimentOutputResource> experimentOutputs = resource.getExperimentOutputs();
//            return ThriftDataModelConversion.getExpOutputs(experimentOutputs);
//        } catch (Exception e) {
//            logger.error("Error while getting experiment outputs...", e);
//        }
        return null;
    }

    public ExperimentStatus getExperimentStatus(String expId) throws RegistryException {
//        try {
//            ExperimentResource resource = gatewayResource.getExperiment(expId);
//            StatusResource experimentStatus = resource.getExperimentStatus();
//            return ThriftDataModelConversion.getExperimentStatus(experimentStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting experiment status...", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public ComputationalResourceSchedulingModel getComputationalScheduling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            ComputationSchedulingResource computationScheduling = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    computationScheduling = resource.getComputationScheduling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    computationScheduling = taskDetail.getComputationScheduling(id);
//                    break;
//            }
//            if (computationScheduling != null) {
//                return ThriftDataModelConversion.getComputationalResourceScheduling(computationScheduling);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting scheduling data..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    public AdvancedInputDataHandling getInputDataHandling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            AdvanceInputDataHandlingResource dataHandlingResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    dataHandlingResource = resource.getInputDataHandling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    dataHandlingResource = taskDetail.getInputDataHandling(id);
//                    break;
//            }
//            if (dataHandlingResource != null) {
//                return ThriftDataModelConversion.getAdvanceInputDataHandling(dataHandlingResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting input data handling..", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }
//
//    public AdvancedOutputDataHandling getOutputDataHandling(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            AdvancedOutputDataHandlingResource dataHandlingResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    dataHandlingResource = resource.getOutputDataHandling(id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    dataHandlingResource = taskDetail.getOutputDataHandling(id);
//                    break;
//            }
//            if (dataHandlingResource != null) {
//                return ThriftDataModelConversion.getAdvanceOutputDataHandling(dataHandlingResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting output data handling...", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }
//
//    public QualityOfServiceParams getQosParams(ExperimentCatalogModelType type, String id) throws RegistryException {
//        try {
//            QosParamResource qosParamResource = null;
//            switch (type) {
//                case EXPERIMENT:
//                    ExperimentResource resource = gatewayResource.getExperiment(id);
//                    qosParamResource = resource.getQOSparams(id);
//                    break;
//            }
//            if (qosParamResource != null) {
//                return ThriftDataModelConversion.getQOSParams(qosParamResource);
//            }
//        } catch (Exception e) {
//            logger.error("Error while getting qos params..", e);
//            throw new RegistryException(e);
//        }
//        return null;
//    }

//    private WorkflowNodeDetailResource getWorkflowNodeDetailResource(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            return resource.getWorkflowNode(nodeId);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public WorkflowNodeDetails getWorkflowNodeDetails(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            return ThriftDataModelConversion.getWorkflowNodeDetails(workflowNode);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node details...", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public WorkflowNodeStatus getWorkflowNodeStatus(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            StatusResource workflowNodeStatus = workflowNode.getWorkflowNodeStatus();
//            return ThriftDataModelConversion.getWorkflowNodeStatus(workflowNodeStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting workflow node status..", e);
//            throw new RegistryException(e);
//        }
//    }

    public List<OutputDataObjectType> getNodeOutputs(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
//            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
//            return ThriftDataModelConversion.getNodeOutputs(nodeOutputs);
//        } catch (Exception e) {
//            logger.error("Error while getting node outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public TaskModel getTaskDetails(String taskId) throws RegistryException {
//        try {
//            TaskDetailResource taskDetail = getTaskDetailResource(taskId);
//            return ThriftDataModelConversion.getTaskModel(taskDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting task details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    private TaskResource getTaskDetailResource(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            return workflowNode.getTaskDetail(taskId);
//        } catch (Exception e) {
//            logger.error("Error while getting task details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public List<OutputDataObjectType> getApplicationOutputs(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            List<ApplicationOutputResource> applicationOutputs = taskDetail.getApplicationOutputs();
//            return ThriftDataModelConversion.getApplicationOutputs(applicationOutputs);
//        } catch (Exception e) {
//            logger.error("Error while getting application outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            StatusResource taskStatus = taskDetail.getTaskStatus();
//            return ThriftDataModelConversion.getTaskStatus(taskStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting experiment outputs..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }


    // ids contains task id + job id
    public JobModel getJobDetails(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            return ThriftDataModelConversion.getJobDetail(jobDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting job details..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

    // ids contains task id + job id
    public JobStatus getJobStatus(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource jobStatus = jobDetail.getJobStatus();
//            return ThriftDataModelConversion.getJobStatus(jobStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting job status..", e);
//            throw new RegistryException(e);
//        }
        return null;
    }

//    public ApplicationStatus getApplicationStatus(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
//            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
//            StatusResource applicationStatus = jobDetail.getApplicationStatus();
//            return ThriftDataModelConversion.getApplicationStatus(applicationStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting application status..", e);
//            throw new RegistryException(e);
//        }
//    }

//    public DataTransferDetails getDataTransferDetails(String transferId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
//            return ThriftDataModelConversion.getDataTransferDetail(dataTransferDetail);
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer details..", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public TransferStatus getDataTransferStatus(String transferId) throws RegistryException {
//        try {
//            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
//            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
//            StatusResource dataTransferStatus = dataTransferDetail.getDataTransferStatus();
//            return ThriftDataModelConversion.getTransferStatus(dataTransferStatus);
//        } catch (Exception e) {
//            logger.error("Error while getting data transfer status..", e);
//            throw new RegistryException(e);
//        }
//    }

    public List<String> getExperimentIDs(String fieldName, Object value) throws RegistryException {
//        List<String> expIDs = new ArrayList<String>();
//        try {
//            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
//                if (gatewayResource == null) {
//                    logger.error("You should use an existing gateway in order to retrieve experiments..");
//                    return null;
//                } else {
//                    List<ExperimentResource> resources = gatewayResource.getExperiments();
//                    for (ExperimentResource resource : resources) {
//                        String expID = resource.getExpID();
//                        expIDs.add(expID);
//                    }
//                }
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
//                List<ExperimentResource> resources = workerResource.getExperiments();
//                for (ExperimentResource resource : resources) {
//                    expIDs.add(resource.getExpID());
//                }
//            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
//                List<ExperimentResource> resources = workerResource.getExperiments();
//                for (ExperimentResource resource : resources) {
//                    expIDs.add(resource.getExpID());
//                }
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving experiment ids..", e);
//            throw new RegistryException(e);
//        }
//        return expIDs;
        return null;
    }

//    public List<String> getWorkflowNodeIds(String fieldName, Object value) throws RegistryException {
//        List<String> wfIds = new ArrayList<String>();
//        List<WorkflowNodeDetails> wfNodeDetails = getWFNodeDetails(fieldName, value);
//        for (WorkflowNodeDetails wf : wfNodeDetails) {
//            wfIds.add(wf.getNodeInstanceId());
//        }
//        return wfIds;
//    }

    public List<String> getTaskDetailIds(String fieldName, Object value) throws RegistryException {
        List<String> taskDetailIds = new ArrayList<String>();
        List<TaskModel> taskDetails = getTaskDetails(fieldName, value);
        for (TaskModel td : taskDetails) {
            taskDetailIds.add(td.getTaskId());
        }
        return taskDetailIds;
    }

    public List<String> getJobDetailIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<String>();
        List<JobModel> jobDetails = getJobDetails(fieldName, value);
        for (JobModel jd : jobDetails) {
            jobIds.add(jd.getJobId());
        }
        return jobIds;
    }
//
//    public List<String> getTransferDetailIds(String fieldName, Object value) throws RegistryException {
//        List<String> transferIds = new ArrayList<String>();
//        List<DataTransferDetails> dataTransferDetails = getDataTransferDetails(fieldName, value);
//        for (DataTransferDetails dtd : dataTransferDetails) {
//            transferIds.add(dtd.getTransferID());
//        }
//        return transferIds;
//    }


    public void removeExperiment(String experimentId) throws RegistryException {
        try {
            gatewayResource.remove(ResourceType.EXPERIMENT, experimentId);
        } catch (Exception e) {
            logger.error("Error while removing experiment..", e);
            throw new RegistryException(e);
        }
    }

    public void removeUserConfigData(String experimentId) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(experimentId);
//            experiment.remove(ResourceType.CONFIG_DATA, experimentId);
//        } catch (Exception e) {
//            logger.error("Error while removing experiment config..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeWorkflowNode(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            experiment.remove(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
//        } catch (Exception e) {
//            logger.error("Error while removing workflow node..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeTaskDetails(String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            nodeDetailResource.remove(ResourceType.TASK_DETAIL, taskId);
//        } catch (Exception e) {
//            logger.error("Error while removing task details..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeJobDetails(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetailResource = nodeDetailResource.getTaskDetail((String) ids.getTopLevelIdentifier());
//            taskDetailResource.remove(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
//        } catch (Exception e) {
//            logger.error("Error while removing job details..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeDataTransferDetails(String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) nodeDetailResource.create(ResourceType.TASK_DETAIL);
//            taskDetail.remove(ResourceType.DATA_TRANSFER_DETAIL, transferId);
//        } catch (Exception e) {
//            logger.error("Error while removing transfer details..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeComputationalScheduling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing scheduling data..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeInputDataHandling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing input data handling..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeOutputDataHandling(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                    break;
//                case TASK_DETAIL:
//                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    taskDetail.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing output data handling..", e);
//            throw new RegistryException(e);
//        }
    }

    public void removeQOSParams(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = gatewayResource.getExperiment(id);
//                    experiment.remove(ResourceType.QOS_PARAM, id);
//                    break;
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while removing QOS params", e);
//            throw new RegistryException(e);
//        }
    }

    public boolean isExperimentExist(String expID) throws RegistryException {
        try {
            return gatewayResource.isExists(ResourceType.EXPERIMENT, expID);
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isExperimentConfigDataExist(String expID) throws RegistryException {
//        try {
//            ExperimentResource experiment = gatewayResource.getExperiment(expID);
//            experiment.isExists(ResourceType.CONFIG_DATA, expID);
//            return true;
//        } catch (Exception e) {
//            logger.error("Error while retrieving experiment...", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isWFNodeExist(String nodeId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            return experiment.isExists(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving workflow...", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isTaskDetailExist(String taskId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            return wf.isExists(ResourceType.TASK_DETAIL, taskId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving task.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isJobDetailExist(CompositeIdentifier ids) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = wf.getTaskDetail((String) ids.getTopLevelIdentifier());
//            return taskDetail.isExists(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
//        } catch (Exception e) {
//            logger.error("Error while retrieving job details.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isTransferDetailExist(String transferId) throws RegistryException {
//        try {
//            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//            TaskDetailResource taskDetail = (TaskDetailResource) wf.create(ResourceType.TASK_DETAIL);
//            return taskDetail.isExists(ResourceType.DATA_TRANSFER_DETAIL, transferId);
//        } catch (Exception e) {
//            logger.error("Error while retrieving transfer details.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isComputationalSchedulingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
//                default:
//                    logger.error("Unsupported data type...");
//
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving scheduling data.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isInputDataHandlingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving input data handling.....", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isOutputDataHandlingExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving output data handling..", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public boolean isQOSParamsExist(ExperimentCatalogModelType dataType, String id) throws RegistryException {
//        try {
//            switch (dataType) {
//                case EXPERIMENT:
//                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    return experiment.isExists(ResourceType.QOS_PARAM, id);
////                case TASK_DETAIL:
////                    ExperimentResource exp = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
////                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource)exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
////                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
////                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
//                default:
//                    logger.error("Unsupported data type...");
//            }
//        } catch (Exception e) {
//            logger.error("Error while retrieving qos params..", e);
//            throw new RegistryException(e);
//        }
        return false;
    }

    public void updateScheduling(ComputationalResourceSchedulingModel scheduling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateSchedulingData(scheduling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateSchedulingData(scheduling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating scheduling..", e);
//            throw new RegistryException(e);
//        }
    }

//    public void updateInputDataHandling(AdvancedInputDataHandling dataHandling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateInputDataHandling(dataHandling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateInputDataHandling(dataHandling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating input data handling..", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateOutputDataHandling(AdvancedOutputDataHandling dataHandling, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateOutputDataHandling(dataHandling, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateOutputDataHandling(dataHandling, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating output data handling", e);
//            throw new RegistryException(e);
//        }
//    }
//
//    public void updateQOSParams(QualityOfServiceParams params, String id, String type) throws RegistryException {
//        try {
//            if (type.equals(ExperimentCatalogModelType.EXPERIMENT.toString())) {
//                ExperimentResource experiment = gatewayResource.getExperiment(id);
//                updateQosParams(params, experiment);
//            } else if (type.equals(ExperimentCatalogModelType.TASK_DETAIL.toString())) {
//                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
//                updateQosParams(params, taskDetail);
//            }
//        } catch (Exception e) {
//            logger.error("Error while updating QOS data..", e);
//            throw new RegistryException(e);
//        }
//    }

    /**
     * To search the experiments of user with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC.
     *
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<ExperimentSummaryModel> searchExperiments(Map<String, String> filters, int limit,
              int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
//        Map<String, String> fil = new HashMap<String, String>();
//        if (filters != null && filters.size() != 0) {
//            List<ExperimentSummaryModel> experimentSummaries = new ArrayList<>();
//            long fromTime = 0;
//            long toTime = 0;
//            try {
//                for (String field : filters.keySet()) {
//                    if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
//                        fil.put(AbstractExpCatResource.ExperimentConstants.EXPERIMENT_NAME, filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
//                        fil.put(AbstractExpCatResource.ExperimentConstants.EXECUTION_USER, filters.get(field));
//                    }else if (field.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
//                        fil.put(AbstractExpCatResource.ExperimentConstants.GATEWAY_ID, filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
//                        fil.put(AbstractExpCatResource.ExperimentConstants.DESCRIPTION, filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
//                        fil.put(AbstractExpCatResource.ExperimentConstants.APPLICATION_ID, filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
//                        fil.put(AbstractExpCatResource.StatusConstants.STATE, filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.FROM_DATE)) {
//                        fromTime = Long.parseLong(filters.get(field));
//                    } else if (field.equals(Constants.FieldConstants.ExperimentConstants.TO_DATE)) {
//                        toTime = Long.parseLong(filters.get(field));
//                    }
//                }
//                List<ExperimentSummaryResource> experimentSummaryResources;
//                if (fromTime != 0 && toTime != 0) {
//                    experimentSummaryResources = workerResource.searchExperiments(new Timestamp(fromTime), new Timestamp(toTime), fil
//                            ,limit , offset, orderByIdentifier, resultOrderType);
//                } else {
//                    experimentSummaryResources = workerResource
//                            .searchExperiments(null, null, fil, limit, offset, orderByIdentifier, resultOrderType);
//                }
//                if (experimentSummaryResources != null && !experimentSummaryResources.isEmpty()) {
//                    for (ExperimentSummaryResource ex : experimentSummaryResources) {
//                        experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
//                    }
//                }
//                return experimentSummaries;
//
//            } catch (Exception e) {
//                logger.error("Error while retrieving experiment summary from registry", e);
//                throw new RegistryException(e);
//            }
//        }
        return null;
    }

    /**
     * Method to get experiment execution statistics for a specific time period
     * @param filters
     * @return
     * @throws RegistryException
     */
    public ExperimentStatistics getExperimentStatistics(Map<String,String> filters) throws RegistryException {
        try {
            ExperimentStatistics experimentStatistics = new ExperimentStatistics();
            ExperimentStatisticsResource experimentStatisticsResource = workerResource.getExperimentStatistics(
                    filters.get(Constants.FieldConstants.ExperimentConstants.GATEWAY),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.FROM_DATE))),
                    new Timestamp(Long.parseLong(filters.get(Constants.FieldConstants.ExperimentConstants.TO_DATE)))
            );

            experimentStatistics.setAllExperimentCount(experimentStatisticsResource.getAllExperimentCount());
            experimentStatistics.setCompletedExperimentCount(experimentStatisticsResource.getCompletedExperimentCount());
            experimentStatistics.setFailedExperimentCount(experimentStatisticsResource.getFailedExperimentCount());
            experimentStatistics.setCancelledExperimentCount(experimentStatisticsResource.getCancelledExperimentCount());

            ArrayList<ExperimentSummaryModel> experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getAllExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setAllExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getCompletedExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setCompletedExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getFailedExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setFailedExperiments(experimentSummaries);

            experimentSummaries = new ArrayList();
            for (ExperimentSummaryResource ex : experimentStatisticsResource.getCancelledExperiments()) {
                experimentSummaries.add(ThriftDataModelConversion.getExperimentSummary(ex));
            }
            experimentStatistics.setCancelledExperiments(experimentSummaries);

            return experimentStatistics;
        } catch (RegistryException e) {
            logger.error("Error while retrieving experiment statistics from registry", e);
            throw new RegistryException(e);
        }
    }

    public boolean isValidStatusTransition(ExperimentState oldState, ExperimentState nextState) {
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
            //case SUSPENDED:  // We don't change state to SUSPEND
//            case UNKNOWN:
//                return true;
            default:
                return false;
        }
    }

    public Object getExperimentInputs(String identifier) {
        return null;
    }

    public Object getExperimentError(String identifier) {
        return null;
    }

    public Object getProcessError(String identifier) {
        return null;
    }

    public Object getProcessStatus(String identifier) {
        return null;
    }

    public Object getProcessInputs(String identifier) {
        return null;
    }

    public Object getProcessOutputs(String identifier) {
        return null;
    }

    public Object getProcessResourceSchedule(String identifier, Object o) {
        return null;
    }

    public Object getProcess(String identifier, Object o) {
        return null;
    }

    public Object getTask(String identifier) {
        return null;
    }

    public Object getTaskError(String identifier) {
        return null;
    }

    public List<ProcessModel> getProcessList(String fieldName, Object value) {
        return null;
    }

    public List<TaskModel> getTaskList(String fieldName, Object value) {
        return null;
    }

    public Object updateExperimentError(ErrorModel newObjectToAdd, String dependentIdentifier) {
        return null;
    }
}
