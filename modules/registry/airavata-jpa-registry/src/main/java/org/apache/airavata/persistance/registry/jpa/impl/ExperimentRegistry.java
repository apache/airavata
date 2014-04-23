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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.persistance.registry.jpa.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExperimentRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRegistry.class);

    public ExperimentRegistry(GatewayResource gateway, UserResource user) {
        gatewayResource = gateway;
        workerResource = new WorkerResource(user.getUserName(), gatewayResource);
        workerResource.save();
    }

    public String addExperiment(Experiment experiment) throws Exception {
        String experimentID;
        try {
            if (!ResourceUtils.isUserExist(experiment.getUserName())) {
                logger.error("User does not exist in the system..");
                throw new Exception("User does not exist in the system..");
            }
            experimentID = getExperimentID(experiment.getName());
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setExpID(experimentID);
            experimentResource.setExpName(experiment.getName());
            experimentResource.setExecutionUser(experiment.getUserName());
            experimentResource.setGateway(gatewayResource);
            ProjectResource project;
            if (!workerResource.isProjectExists(experiment.getProjectID())) {
                project = workerResource.createProject(experiment.getProjectID());
                project.setGateway(gatewayResource);
                project.save();
                experimentResource.setProject(project);
            } else {
                project = workerResource.getProject(experiment.getProjectID());
                experimentResource.setProject(project);
            }
            experimentResource.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            experimentResource.setDescription(experiment.getDescription());
            experimentResource.setApplicationId(experiment.getApplicationId());
            experimentResource.setApplicationVersion(experiment.getApplicationVersion());
            experimentResource.setWorkflowTemplateId(experiment.getWorkflowTemplateId());
            experimentResource.setWorkflowTemplateVersion(experiment.getWorkflowTemplateVersion());
            experimentResource.setWorkflowExecutionId(experiment.getWorkflowExecutionInstanceId());
            experimentResource.save();
            List<DataObjectType> experimentInputs = experiment.getExperimentInputs();
            if (experimentInputs != null) {
                addExpInputs(experimentInputs, experimentResource);
            }

            UserConfigurationData userConfigurationData = experiment.getUserConfigurationData();
            if (userConfigurationData != null) {
                addUserConfigData(userConfigurationData, experimentID);
            }

            List<DataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()){
                addExpOutputs(experimentOutputs, experimentID);
            }

            ExperimentStatus experimentStatus = experiment.getExperimentStatus();
            if (experimentStatus != null){
                updateExperimentStatus(experimentStatus, experimentID);
            }else {
                experimentStatus = new ExperimentStatus();
                experimentStatus.setExperimentState(ExperimentState.CREATED);
                updateExperimentStatus(experimentStatus, experimentID);
            }

            List<WorkflowNodeDetails> workflowNodeDetailsList = experiment.getWorkflowNodeDetailsList();
            if (workflowNodeDetailsList != null && !workflowNodeDetailsList.isEmpty()){
                for (WorkflowNodeDetails wf : workflowNodeDetailsList){
                    addWorkflowNodeDetails(wf, experimentID);
                }
            }
            List<ErrorDetails> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails errror : errors){
                    addErrorDetails(errror, experimentID);
                }
            }
        } catch (Exception e) {
            logger.error("Error while saving experiment to registry", e.getMessage());
            throw new Exception(e);
        }
        return experimentID;
    }

    public String addUserConfigData(UserConfigurationData configurationData, String experimentID) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(experimentID);
            ConfigDataResource configData = (ConfigDataResource) experiment.create(ResourceType.CONFIG_DATA);
            configData.setExperimentResource(experiment);
            configData.setAiravataAutoSchedule(configurationData.isAiravataAutoSchedule());
            configData.setOverrideManualParams(configurationData.isOverrideManualScheduledParams());
            configData.setShareExp(configurationData.isShareExperimentPublicly());
            configData.save();
            ComputationalResourceScheduling resourceScheduling = configurationData.getComputationalResourceScheduling();
            if (resourceScheduling != null) {
                addComputationScheduling(resourceScheduling, experiment);
            }
            AdvancedInputDataHandling inputDataHandling = configurationData.getAdvanceInputDataHandling();
            if (inputDataHandling != null) {
                addInputDataHandling(inputDataHandling, experiment);
            }

            AdvancedOutputDataHandling outputDataHandling = configurationData.getAdvanceOutputDataHandling();
            if (outputDataHandling != null) {
                addOutputDataHandling(outputDataHandling, experiment);
            }

            QualityOfServiceParams qosParams = configurationData.getQosParams();
            if (qosParams != null) {
                addQosParams(qosParams, experiment);
            }
        } catch (Exception e) {
            logger.error("Unable to save user config data", e.getMessage());
            throw new Exception(e);
        }
        return experimentID;
    }

    public void addQosParams(QualityOfServiceParams qosParams, Resource resource) throws Exception {
        try {
            QosParamResource qosr = new QosParamResource();
            if (resource instanceof ExperimentResource) {
                ExperimentResource experiment = (ExperimentResource) resource;
                qosr.setExperimentResource(experiment);
            }
            if (resource instanceof TaskDetailResource) {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                qosr.setTaskDetailResource(taskDetailResource);
                qosr.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
            qosr.setExecuteBefore(qosParams.getExecuteBefore());
            qosr.setNoOfRetries(qosParams.getNumberofRetries());
            qosr.save();
        } catch (Exception e) {
            logger.error("Unable to save QOS params", e.getMessage());
            throw new Exception(e);
        }

    }

    public void addOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, Resource resource) throws Exception {
        AdvancedOutputDataHandlingResource adodh = new AdvancedOutputDataHandlingResource();
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource experiment = (ExperimentResource) resource;
                adodh.setExperimentResource(experiment);
            }
            if (resource instanceof TaskDetailResource) {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                adodh.setTaskDetailResource(taskDetailResource);
                adodh.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
            adodh.save();
        } catch (Exception e) {
            logger.error("Unable to save output data handling data", e.getMessage());
            throw new Exception(e);
        }

    }

    public void addInputDataHandling(AdvancedInputDataHandling inputDataHandling, Resource resource) throws Exception {
        AdvanceInputDataHandlingResource adidh = new AdvanceInputDataHandlingResource();
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource experiment = (ExperimentResource) resource;
                adidh.setExperimentResource(experiment);
            }
            if (resource instanceof TaskDetailResource) {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                adidh.setTaskDetailResource(taskDetailResource);
                adidh.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
            adidh.save();
        } catch (Exception e) {
            logger.error("Unable to save input data handling data", e.getMessage());
            throw new Exception(e);
        }

    }

    public void addComputationScheduling(ComputationalResourceScheduling resourceScheduling, Resource resource) throws Exception {
        ComputationSchedulingResource cmsr = new ComputationSchedulingResource();
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource experiment = (ExperimentResource) resource;
                cmsr.setExperimentResource(experiment);
            }
            if (resource instanceof TaskDetailResource) {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                cmsr.setTaskDetailResource(taskDetailResource);
                cmsr.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
            cmsr.setNodeCount(resourceScheduling.getNodeCount());
            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
            cmsr.setQueueName(resourceScheduling.getQueueName());
            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
            cmsr.setJobStartTime(AiravataUtils.getTime(resourceScheduling.getJobStartTime()));
            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
            cmsr.setProjectName(resourceScheduling.getComputationalProjectAccount());
            cmsr.save();
        } catch (Exception e) {
            logger.error("Unable to save computational scheduling data", e.getMessage());
            throw new Exception(e);
        }

    }

    public void addExpInputs(List<DataObjectType> exInputs, ExperimentResource experimentResource) throws Exception {
        try {
            for (DataObjectType input : exInputs) {
                ExperimentInputResource resource = (ExperimentInputResource) experimentResource.create(ResourceType.EXPERIMENT_INPUT);
                resource.setExperimentResource(experimentResource);
                resource.setExperimentKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setInputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Unable to save experiment inputs", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateExpInputs(List<DataObjectType> exInputs, ExperimentResource experimentResource) throws Exception {
        try {
            List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
            for (DataObjectType input : exInputs) {
                for (ExperimentInputResource exinput : experimentInputs) {
                    if (exinput.getExperimentKey().equals(input.getKey())) {
                        exinput.setValue(input.getValue());
                        exinput.setInputType(input.getType());
                        exinput.setMetadata(input.getMetaData());
                        exinput.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to update experiment inputs", e.getMessage());
            throw new Exception(e);
        }

    }

    public String addExpOutputs(List<DataObjectType> exOutput, String expId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            for (DataObjectType output : exOutput) {
                ExperimentOutputResource resource = (ExperimentOutputResource) experiment.create(ResourceType.EXPERIMENT_OUTPUT);
                resource.setExperimentResource(experiment);
                resource.setExperimentKey(output.getKey());
                resource.setValue(output.getValue());
                resource.setOutputType(output.getType());
                resource.setMetadata(output.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding experiment outputs...", e.getMessage());
            throw new Exception(e);
        }
        return expId;
    }

    public void updateExpOutputs(List<DataObjectType> exOutput, String expId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            List<ExperimentOutputResource> existingExpOutputs = experiment.getExperimentOutputs();
            for (DataObjectType output : exOutput) {
                for (ExperimentOutputResource resource : existingExpOutputs) {
                    if (resource.getExperimentKey().equals(output.getKey())) {
                        resource.setExperimentResource(experiment);
                        resource.setExperimentKey(output.getKey());
                        resource.setValue(output.getValue());
                        resource.setOutputType(output.getType());
                        resource.setMetadata(output.getMetaData());
                        resource.save();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating experiment outputs", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addNodeOutputs(List<DataObjectType> wfOutputs, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getSecondLevelIdentifier());
            for (DataObjectType output : wfOutputs) {
                NodeOutputResource resource = (NodeOutputResource) workflowNode.create(ResourceType.NODE_OUTPUT);
                resource.setNodeDetailResource(workflowNode);
                resource.setOutputKey(output.getKey());
                resource.setValue(output.getValue());
                resource.setOutputType(output.getType());
                resource.setMetadata(output.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding node outputs...", e.getMessage());
            throw new Exception(e);
        }
        return (String) ids.getSecondLevelIdentifier();
    }

    public void updateNodeOutputs(List<DataObjectType> wfOutputs, String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
            for (DataObjectType output : wfOutputs) {
                for (NodeOutputResource resource : nodeOutputs) {
                    resource.setNodeDetailResource(workflowNode);
                    resource.setOutputKey(output.getKey());
                    resource.setValue(output.getValue());
                    resource.setOutputType(output.getType());
                    resource.setMetadata(output.getMetaData());
                    resource.save();
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating node outputs...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addApplicationOutputs(List<DataObjectType> appOutputs, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getTopLevelIdentifier());
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getSecondLevelIdentifier());
            for (DataObjectType output : appOutputs) {
                ApplicationOutputResource resource = (ApplicationOutputResource) taskDetail.create(ResourceType.APPLICATION_OUTPUT);
                resource.setTaskDetailResource(taskDetail);
                resource.setOutputKey(output.getKey());
                resource.setValue(output.getValue());
                resource.setOutputType(output.getType());
                resource.setMetadata(output.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding application outputs...", e.getMessage());
            throw new Exception(e);
        }
        return (String) ids.getSecondLevelIdentifier();
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String expId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            StatusResource status = experiment.getExperimentStatus();
            if (status == null) {
                status = (StatusResource) experiment.create(ResourceType.STATUS);
            }
            status.setExperimentResource(experiment);
            status.setStatusUpdateTime(AiravataUtils.getTime(experimentStatus.getTimeOfStateChange()));
            status.setState(experimentStatus.getExperimentState().toString());
            status.setStatusType(StatusType.EXPERIMENT.toString());
            status.save();
        } catch (Exception e) {
            logger.error("Error while updating experiment status...", e.getMessage());
            throw new Exception(e);
        }
        return expId;
    }

    public String addWorkflowNodeStatus(WorkflowNodeStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource) experiment.create(ResourceType.STATUS);
            statusResource.setExperimentResource(experiment);
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getWorkflowNodeState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error while adding workflow node status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String updateWorkflowNodeStatus(WorkflowNodeStatus status, String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            StatusResource statusResource = workflowNode.getWorkflowNodeStatus();
            statusResource.setExperimentResource(workflowNode.getExperimentResource());
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getWorkflowNodeState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error whilw updating workflow node status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addTaskStatus(TaskStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getTopLevelIdentifier());
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource) workflowNode.create(ResourceType.STATUS);
            statusResource.setExperimentResource(workflowNode.getExperimentResource());
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.TASK.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getExecutionState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error while adding task status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateTaskStatus(TaskStatus status, String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            StatusResource statusResource;
            if (taskDetail.isTaskStatusExist(taskId)){
                statusResource = workflowNode.geTaskStatus(taskId);
            } else {
                statusResource = (StatusResource)taskDetail.create(ResourceType.STATUS);
            }
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.TASK.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getExecutionState().toString());
            statusResource.save();
        } catch (Exception e) {
            logger.error("Error while updating task status...", e.getMessage());
            throw new Exception(e);
        }
    }

    /**
     * @param status job status
     * @param ids    composite id will contain taskid and jobid
     * @return status id
     */
    public String addJobStatus(JobStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource) jobDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.JOB.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getJobState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error while adding job status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String updateJobStatus(JobStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String)ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String)ids.getSecondLevelIdentifier());
            StatusResource statusResource = jobDetail.getJobStatus();
            workflowNode = taskDetail.getWorkflowNodeDetailResource();
            experiment = workflowNode.getExperimentResource();
            statusResource.setExperimentResource(experiment);
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.JOB.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getJobState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error while updating job status...", e.getMessage());
            throw new Exception(e);
        }
    }

    /**
     * @param status application status
     * @param ids    composite id will contain taskid and jobid
     * @return status id
     */
    public String addApplicationStatus(ApplicationStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource) jobDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.APPLICATION.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getApplicationState());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateApplicationStatus(ApplicationStatus status, String jobId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
            StatusResource statusResource = jobDetail.getApplicationStatus();
            statusResource.setExperimentResource(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(jobDetail.getTaskDetailResource());
            statusResource.setStatusType(StatusType.APPLICATION.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getApplicationState());
            statusResource.save();
        } catch (Exception e) {
            logger.error("Error while updating application status...", e.getMessage());
            throw new Exception(e);
        }
    }


    /**
     * @param status data transfer status
     * @param ids    contains taskId and transfer id
     * @return status id
     */
    public String addTransferStatus(TransferStatus status, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource) dataTransferDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setDataTransferDetail(dataTransferDetail);
            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getTransferState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (Exception e) {
            logger.error("Error while adding transfer status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateTransferStatus(TransferStatus status, String transferId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
            StatusResource statusResource = dataTransferDetail.getDataTransferStatus();
            statusResource.setExperimentResource(dataTransferDetail.getTaskDetailResource().getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(dataTransferDetail.getTaskDetailResource().getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(dataTransferDetail.getTaskDetailResource());
            statusResource.setDataTransferDetail(dataTransferDetail);
            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
            statusResource.setStatusUpdateTime(AiravataUtils.getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getTransferState().toString());
            statusResource.save();
        } catch (Exception e) {
            logger.error("Error while updating transfer status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String expId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            WorkflowNodeDetailResource resource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            resource.setExperimentResource(experiment);
            resource.setNodeName(nodeDetails.getNodeName());
            resource.setCreationTime(AiravataUtils.getTime(nodeDetails.getCreationTime()));
            resource.setNodeInstanceId(getNodeInstanceID(nodeDetails.getNodeName()));
            resource.save();
            String nodeId = resource.getNodeInstanceId();
            List<DataObjectType> nodeInputs = nodeDetails.getNodeInputs();
            if (nodeInputs != null) {
                addWorkflowInputs(nodeDetails.getNodeInputs(), resource);
            }
            List<DataObjectType> nodeOutputs = nodeDetails.getNodeOutputs();
            if (nodeOutputs != null && !nodeOutputs.isEmpty()){
                CompositeIdentifier ids = new CompositeIdentifier(expId, nodeId);
                addNodeOutputs(nodeOutputs, ids);
            }
            WorkflowNodeStatus workflowNodeStatus = nodeDetails.getWorkflowNodeStatus();
            CompositeIdentifier ids = new CompositeIdentifier(expId, nodeId);
            if (workflowNodeStatus != null ){
                if (workflowNodeStatus.getWorkflowNodeState() != null){
                    WorkflowNodeStatus status = getWorkflowNodeStatus(nodeId);
                    if (status != null){
                        updateWorkflowNodeStatus(workflowNodeStatus, nodeId);
                    }else {
                        addWorkflowNodeStatus(workflowNodeStatus,ids);
                    }
                }else {
                    workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.UNKNOWN);
                    addWorkflowNodeStatus(workflowNodeStatus, ids);
                }
            }else {
                WorkflowNodeStatus status = new WorkflowNodeStatus();
                status.setWorkflowNodeState(WorkflowNodeState.UNKNOWN);
                addWorkflowNodeStatus(status, ids);
            }
            List<TaskDetails> taskDetails = nodeDetails.getTaskDetailsList();
            if (taskDetails != null && !taskDetails.isEmpty()){
                for (TaskDetails task : taskDetails){
                    addTaskDetails(task, nodeId);
                }
            }
            List<ErrorDetails> errors = nodeDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors){
                    addErrorDetails(error, nodeId);
                }
            }
            return nodeId;
        } catch (Exception e) {
            logger.error("Error while adding workflow node details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            workflowNode.setNodeName(nodeDetails.getNodeName());
            workflowNode.setCreationTime(AiravataUtils.getTime(nodeDetails.getCreationTime()));
            workflowNode.setNodeInstanceId(nodeId);
            workflowNode.save();
            String expID = workflowNode.getExperimentResource().getExpID();
            List<DataObjectType> nodeInputs = nodeDetails.getNodeInputs();
            if (nodeInputs != null) {
                updateWorkflowInputs(nodeDetails.getNodeInputs(), workflowNode);
            }
            List<DataObjectType> nodeOutputs = nodeDetails.getNodeOutputs();
            if (nodeOutputs != null && !nodeOutputs.isEmpty()){
                updateNodeOutputs(nodeOutputs, nodeId);
            }
            WorkflowNodeStatus workflowNodeStatus = nodeDetails.getWorkflowNodeStatus();
            if (workflowNodeStatus != null){
                if (isWFNodeExist(nodeId)){
                    updateWorkflowNodeStatus(workflowNodeStatus, nodeId);
                }else {
                    CompositeIdentifier ids = new CompositeIdentifier(expID, nodeId);
                    addWorkflowNodeStatus(workflowNodeStatus,ids);
                }
            }
            List<TaskDetails> taskDetails = nodeDetails.getTaskDetailsList();
            if (taskDetails != null && !taskDetails.isEmpty()){
                for (TaskDetails task : taskDetails){
                    String taskID = task.getTaskID();
                    if(isTaskDetailExist(taskID)){
                        updateTaskDetails(task, taskID);
                    }else {
                        addTaskDetails(task, nodeId);
                    }
                }
            }
            List<ErrorDetails> errors = nodeDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors){
                    addErrorDetails(error, nodeId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating workflow node details...", e.getMessage());
            throw new Exception(e);
        }
    }


    public void addWorkflowInputs(List<DataObjectType> wfInputs, WorkflowNodeDetailResource nodeDetailResource) throws Exception {
        try {
            for (DataObjectType input : wfInputs) {
                NodeInputResource resource = (NodeInputResource) nodeDetailResource.create(ResourceType.NODE_INPUT);
                resource.setNodeDetailResource(nodeDetailResource);
                resource.setInputKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setInputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding workflow inputs...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateWorkflowInputs(List<DataObjectType> wfInputs, WorkflowNodeDetailResource nodeDetailResource) throws Exception {
        try {
            List<NodeInputResource> nodeInputs = nodeDetailResource.getNodeInputs();
            for (DataObjectType input : wfInputs) {
                for (NodeInputResource resource : nodeInputs) {
                    resource.setNodeDetailResource(nodeDetailResource);
                    resource.setInputKey(input.getKey());
                    resource.setValue(input.getValue());
                    resource.setInputType(input.getType());
                    resource.setMetadata(input.getMetaData());
                    resource.save();
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating workflow inputs...", e.getMessage());
            throw new Exception(e);
        }

    }

    public String addTaskDetails(TaskDetails taskDetails, String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            taskDetail.setWorkflowNodeDetailResource(workflowNode);
            taskDetail.setTaskId(getTaskID(workflowNode.getNodeName()));
            taskDetail.setApplicationId(taskDetails.getApplicationId());
            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
            taskDetail.setCreationTime(AiravataUtils.getTime(taskDetails.getCreationTime()));
            taskDetail.save();
            List<DataObjectType> applicationInputs = taskDetails.getApplicationInputs();
            if (applicationInputs != null) {
                addAppInputs(applicationInputs, taskDetail);
            }
            List<DataObjectType> applicationOutput = taskDetails.getApplicationOutputs();
            if (applicationOutput != null) {
                addAppOutputs(applicationOutput, taskDetail);
            }
            ComputationalResourceScheduling taskScheduling = taskDetails.getTaskScheduling();
            if (taskScheduling != null) {
                addComputationScheduling(taskScheduling, taskDetail);
            }
            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
            if (inputDataHandling != null) {
                addInputDataHandling(inputDataHandling, taskDetail);
            }
            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
            if (outputDataHandling != null) {
                addOutputDataHandling(outputDataHandling, taskDetail);
            }

            List<JobDetails> jobDetailsList = taskDetails.getJobDetailsList();
            if (jobDetailsList != null && !jobDetailsList.isEmpty()){
                for (JobDetails job : jobDetailsList){
                    CompositeIdentifier ids = new CompositeIdentifier(taskDetail.getTaskId(), job.getJobID());
                    addJobDetails(job,ids);
                }
            }

            List<DataTransferDetails> dataTransferDetailsList = taskDetails.getDataTransferDetailsList();
            if (dataTransferDetailsList != null && !dataTransferDetailsList.isEmpty()){
                for (DataTransferDetails transferDetails : dataTransferDetailsList){
                    addDataTransferDetails(transferDetails, taskDetail.getTaskId());
                }
            }

            List<ErrorDetails> errors = taskDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors){
                    addErrorDetails(error, taskDetail.getTaskId());
                }
            }

            TaskStatus taskStatus = taskDetails.getTaskStatus();
            CompositeIdentifier ids = new CompositeIdentifier(nodeId, taskDetail.getTaskId());
            if (taskStatus != null){
                if (taskStatus.getExecutionState() != null){
                    addTaskStatus(taskStatus, ids);
                }else {
                    taskStatus.setExecutionState(TaskState.UNKNOWN);
                    addTaskStatus(taskStatus, ids);
                }
            }else {
                TaskStatus status = new TaskStatus();
                status.setExecutionState(TaskState.UNKNOWN);
                addTaskStatus(status, ids);
            }
            return taskDetail.getTaskId();
        } catch (Exception e) {
            logger.error("Error while adding task details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String updateTaskDetails(TaskDetails taskDetails, String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
//            taskDetail.setWorkflowNodeDetailResource(workflowNode);
            taskDetail.setApplicationId(taskDetails.getApplicationId());
            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
            taskDetail.setCreationTime(AiravataUtils.getTime(taskDetails.getCreationTime()));
            taskDetail.save();
            List<DataObjectType> applicationInputs = taskDetails.getApplicationInputs();
            if (applicationInputs != null) {
                updateAppInputs(applicationInputs, taskDetail);
            }
            ComputationalResourceScheduling taskScheduling = taskDetails.getTaskScheduling();
            if (taskScheduling != null) {
                updateSchedulingData(taskScheduling, taskDetail);
            }
            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
            if (inputDataHandling != null) {
                updateInputDataHandling(inputDataHandling, taskDetail);
            }
            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
            if (outputDataHandling != null) {
                updateOutputDataHandling(outputDataHandling, taskDetail);
            }
            List<JobDetails> jobDetailsList = taskDetails.getJobDetailsList();
            if (jobDetailsList != null && !jobDetailsList.isEmpty()){
                for (JobDetails job : jobDetailsList){
                    CompositeIdentifier ids = new CompositeIdentifier(taskId, job.getJobID());
                    updateJobDetails(job, ids);
                }
            }

            List<DataTransferDetails> dataTransferDetailsList = taskDetails.getDataTransferDetailsList();
            if (dataTransferDetailsList != null && !dataTransferDetailsList.isEmpty()){
                for (DataTransferDetails transferDetails : dataTransferDetailsList){
                    updateDataTransferDetails(transferDetails, transferDetails.getTransferID());
                }
            }

            List<ErrorDetails> errors = taskDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors){
                    addErrorDetails(error, taskDetail.getTaskId());
                }
            }

            TaskStatus taskStatus = taskDetails.getTaskStatus();
            if (taskStatus != null){
                updateTaskStatus(taskStatus, taskId);
            }
            return taskDetail.getTaskId();
        } catch (Exception e) {
            logger.error("Error while updating task details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void addAppInputs(List<DataObjectType> appInputs, TaskDetailResource taskDetailResource) throws Exception {
        try {
            for (DataObjectType input : appInputs) {
                ApplicationInputResource resource = (ApplicationInputResource) taskDetailResource.create(ResourceType.APPLICATION_INPUT);
                resource.setTaskDetailResource(taskDetailResource);
                resource.setInputKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setInputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding application inputs...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void addAppOutputs(List<DataObjectType> appInputs, TaskDetailResource taskDetailResource) throws Exception {
        try {
            for (DataObjectType input : appInputs) {
                ApplicationOutputResource resource = (ApplicationOutputResource) taskDetailResource.create(ResourceType.APPLICATION_OUTPUT);
                resource.setTaskDetailResource(taskDetailResource);
                resource.setOutputKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setOutputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }
        } catch (Exception e) {
            logger.error("Error while adding application outputs...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateAppOutputs(List<DataObjectType> appOutputs, String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            List<ApplicationOutputResource> outputs = taskDetail.getApplicationOutputs();
            for (DataObjectType output : appOutputs) {
                for (ApplicationOutputResource resource : outputs) {
                    resource.setTaskDetailResource(taskDetail);
                    resource.setOutputKey(output.getKey());
                    resource.setValue(output.getValue());
                    resource.setOutputType(output.getType());
                    resource.setMetadata(output.getMetaData());
                    resource.save();
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating application outputs...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateAppInputs(List<DataObjectType> appInputs, TaskDetailResource taskDetailResource) throws Exception {
        try {
            List<ApplicationInputResource> inputs = taskDetailResource.getApplicationInputs();
            for (DataObjectType input : appInputs) {
                for (ApplicationInputResource resource : inputs) {
                    resource.setTaskDetailResource(taskDetailResource);
                    resource.setInputKey(input.getKey());
                    resource.setValue(input.getValue());
                    resource.setInputType(input.getType());
                    resource.setMetadata(input.getMetaData());
                    resource.save();
                }

            }
        } catch (Exception e) {
            logger.error("Error while updating application inputs...", e.getMessage());
            throw new Exception(e);
        }

    }

    public String addJobDetails(JobDetails jobDetails, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.createJobDetail((String) ids.getSecondLevelIdentifier());
            jobDetail.setTaskDetailResource(taskDetail);
            jobDetail.setJobDescription(jobDetails.getJobDescription());
            jobDetail.setCreationTime(AiravataUtils.getTime(jobDetails.getCreationTime()));
            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
            jobDetail.save();
            JobStatus jobStatus = jobDetails.getJobStatus();
            if (jobStatus != null){
                JobStatus status = getJobStatus(ids);
                if (status != null){
                    updateJobStatus(jobStatus, ids);
                }else {
                    addJobStatus(jobStatus, ids);
                }
            }
            ApplicationStatus applicationStatus = jobDetails.getApplicationStatus();
            if (applicationStatus != null){
                ApplicationStatus appStatus = getApplicationStatus(ids);
                if (appStatus != null){
                    updateApplicationStatus(applicationStatus, (String)ids.getSecondLevelIdentifier());
                }else {
                    addApplicationStatus(applicationStatus, ids);
                }
            }
            List<ErrorDetails> errors = jobDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors ){
                    addErrorDetails(error, ids.getSecondLevelIdentifier());
                }
            }
            return jobDetail.getJobId();
        } catch (Exception e) {
            logger.error("Error while adding job details...", e.getMessage());
            throw new Exception(e);
        }
    }

    // ids - taskId + jobid
    public void updateJobDetails(JobDetails jobDetails, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            String taskId = (String) ids.getTopLevelIdentifier();
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            String jobId = (String) ids.getSecondLevelIdentifier();
            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
            jobDetail.setTaskDetailResource(taskDetail);
            jobDetail.setJobDescription(jobDetails.getJobDescription());
            jobDetail.setCreationTime(AiravataUtils.getTime(jobDetails.getCreationTime()));
            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
            jobDetail.save();
            JobStatus jobStatus = jobDetails.getJobStatus();
            if (jobStatus != null){
                JobStatus status = getJobStatus(ids);
                if (status != null){
                    updateJobStatus(jobStatus, ids);
                }else {
                    addJobStatus(jobStatus, ids);
                }
            }
            ApplicationStatus applicationStatus = jobDetails.getApplicationStatus();
            if (applicationStatus != null){
                ApplicationStatus appStatus = getApplicationStatus(ids);
                if (appStatus != null){
                    updateApplicationStatus(applicationStatus, jobId);
                }else {
                    addApplicationStatus(applicationStatus, ids);
                }
            }
            List<ErrorDetails> errors = jobDetails.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails error : errors ){
                    addErrorDetails(error, jobId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating job details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addDataTransferDetails(DataTransferDetails transferDetails, String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            DataTransferDetailResource resource = (DataTransferDetailResource) taskDetail.create(ResourceType.DATA_TRANSFER_DETAIL);
            resource.setTaskDetailResource(taskDetail);
            resource.setTransferId(getDataTransferID(taskId));
            resource.setTransferDescription(transferDetails.getTransferDescription());
            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
            resource.save();
            String transferId = resource.getTransferId();
            TransferStatus transferStatus = transferDetails.getTransferStatus();
            if (transferStatus != null){
                TransferStatus status = getDataTransferStatus(transferId);
                if (status != null){
                    updateTransferStatus(transferStatus, transferId);
                }else {
                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
                    addTransferStatus(transferStatus, ids);
                }
            }
            return resource.getTransferId();
        } catch (Exception e) {
            logger.error("Error while adding transfer details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String updateDataTransferDetails(DataTransferDetails transferDetails, String transferId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource resource = taskDetail.getDataTransferDetail(transferId);
            resource.setTaskDetailResource(taskDetail);
            resource.setTransferDescription(transferDetails.getTransferDescription());
            resource.setCreationTime(AiravataUtils.getTime(transferDetails.getCreationTime()));
            resource.save();
            String taskId = resource.getTaskDetailResource().getTaskId();
            TransferStatus transferStatus = transferDetails.getTransferStatus();
            if (transferStatus != null){
                TransferStatus status = getDataTransferStatus(transferId);
                if (status != null){
                    updateTransferStatus(transferStatus, transferId);
                }else {
                    CompositeIdentifier ids = new CompositeIdentifier(taskId, transferId);
                    addTransferStatus(transferStatus, ids);
                }
            }
            return resource.getTransferId();
        } catch (Exception e) {
            logger.error("Error while updating transfer details...", e.getMessage());
            throw new Exception(e);
        }
    }

    /**
     * @param scheduling computational resource object
     * @param ids        contains expId and taskId, if it is an experiment, task id can be null
     * @return scheduling id
     */
    public String addComputationalResourceScheduling(ComputationalResourceScheduling scheduling, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource) experiment.create(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING);
            if (ids.getSecondLevelIdentifier() != null) {
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
                schedulingResource.setTaskDetailResource(taskDetail);
            }
            schedulingResource.setExperimentResource(experiment);
            schedulingResource.setResourceHostId(scheduling.getResourceHostId());
            schedulingResource.setCpuCount(scheduling.getTotalCPUCount());
            schedulingResource.setNodeCount(scheduling.getNodeCount());
            schedulingResource.setNumberOfThreads(scheduling.getNumberOfThreads());
            schedulingResource.setQueueName(scheduling.getQueueName());
            schedulingResource.setWalltimeLimit(scheduling.getWallTimeLimit());
            schedulingResource.setJobStartTime(AiravataUtils.getTime(scheduling.getJobStartTime()));
            schedulingResource.setPhysicalMemory(scheduling.getTotalPhysicalMemory());
            schedulingResource.setProjectName(scheduling.getComputationalProjectAccount());
            schedulingResource.save();
            return String.valueOf(schedulingResource.getSchedulingId());
        } catch (Exception e) {
            logger.error("Error while adding scheduling parameters...", e.getMessage());
            throw new Exception(e);
        }
    }

    /**
     * @param dataHandling advanced input data handling object
     * @param ids          contains expId and taskId
     * @return data handling id
     */
    public String addInputDataHandling(AdvancedInputDataHandling dataHandling, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            AdvanceInputDataHandlingResource dataHandlingResource = (AdvanceInputDataHandlingResource) experiment.create(ResourceType.ADVANCE_INPUT_DATA_HANDLING);
            if (ids.getSecondLevelIdentifier() != null) {
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
                dataHandlingResource.setTaskDetailResource(taskDetail);
            }
            dataHandlingResource.setExperimentResource(experiment);
            dataHandlingResource.setWorkingDir(dataHandling.getUniqueWorkingDirectory());
            dataHandlingResource.setWorkingDirParent(dataHandling.getParentWorkingDirectory());
            dataHandlingResource.setStageInputFiles(dataHandling.isStageInputFilesToWorkingDir());
            dataHandlingResource.setCleanAfterJob(dataHandling.isCleanUpWorkingDirAfterJob());
            dataHandlingResource.save();
            return String.valueOf(dataHandlingResource.getDataHandlingId());
        } catch (Exception e) {
            logger.error("Error while adding input data handling...", e.getMessage());
            throw new Exception(e);
        }
    }

    /**
     * @param dataHandling advanced output data handling object
     * @param ids          contains expId and taskId
     * @return data handling id
     */
    public String addOutputDataHandling(AdvancedOutputDataHandling dataHandling, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            AdvancedOutputDataHandlingResource dataHandlingResource = (AdvancedOutputDataHandlingResource) experiment.create(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING);
            if (ids.getSecondLevelIdentifier() != null) {
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
                dataHandlingResource.setTaskDetailResource(taskDetail);
            }
            dataHandlingResource.setExperimentResource(experiment);
            dataHandlingResource.setOutputDataDir(dataHandling.getOutputDataDir());
            dataHandlingResource.setDataRegUrl(dataHandling.getDataRegistryURL());
            dataHandlingResource.setPersistOutputData(dataHandling.isPersistOutputData());
            dataHandlingResource.save();
            return String.valueOf(dataHandlingResource.getOutputDataHandlingId());
        } catch (Exception e) {
            logger.error("Error while adding output data handling...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addQosParams(QualityOfServiceParams qosParams, CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment((String) ids.getTopLevelIdentifier());
            QosParamResource qosParamResource = (QosParamResource) experiment.create(ResourceType.QOS_PARAM);
            if (ids.getSecondLevelIdentifier() != null) {
                WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
                qosParamResource.setTaskDetailResource(taskDetail);
            }
            qosParamResource.setExperimentResource(experiment);
            qosParamResource.setStartExecutionAt(qosParams.getStartExecutionAt());
            qosParamResource.setExecuteBefore(qosParams.getExecuteBefore());
            qosParamResource.setNoOfRetries(qosParams.getNumberofRetries());
            qosParamResource.save();
            return String.valueOf(qosParamResource.getQosId());
        } catch (Exception e) {
            logger.error("Error while adding QOS params...", e.getMessage());
            throw new Exception(e);
        }
    }

    public String addErrorDetails(ErrorDetails error, Object id) throws Exception {
        try {

            ErrorDetailResource errorResource = null;
            ExperimentResource experiment;
            TaskDetailResource taskDetail;
            WorkflowNodeDetailResource workflowNode;
            // figure out the id is an experiment, node task or job
            if (id instanceof String) {
                // FIXME : for .12 we only save task related errors
//                if (isExperimentExist((String) id)) {
//                    experiment = gatewayResource.getExperiment((String) id);
//                    errorResource = (ErrorDetailResource) experiment.create(ResourceType.ERROR_DETAIL);
//                } else if (isWFNodeExist((String) id)) {
//                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
//                    workflowNode = experiment.getWorkflowNode((String) id);
//                    errorResource = (ErrorDetailResource) workflowNode.create(ResourceType.ERROR_DETAIL);
//                    errorResource.setExperimentResource(workflowNode.getExperimentResource());
//                } else
                  if (isTaskDetailExist((String) id)) {
                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    taskDetail = workflowNode.getTaskDetail((String) id);
                    errorResource = (ErrorDetailResource) taskDetail.create(ResourceType.ERROR_DETAIL);
                    errorResource.setTaskDetailResource(taskDetail);
                    errorResource.setNodeDetail(taskDetail.getWorkflowNodeDetailResource());
                    errorResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
                } else {
                    logger.error("The id provided is not an experiment id or a workflow id or a task id..");
                }
            } else if (id instanceof CompositeIdentifier) {
                CompositeIdentifier cid = (CompositeIdentifier) id;
                if (isJobDetailExist(cid)) {
                    experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
                    JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
                    errorResource = (ErrorDetailResource) jobDetail.create(ResourceType.ERROR_DETAIL);
                    errorResource.setTaskDetailResource(taskDetail);
                    errorResource.setNodeDetail(taskDetail.getWorkflowNodeDetailResource());
                    errorResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
                } else {
                    logger.error("The id provided is not a job in the system..");
                }
            } else {
                logger.error("The id provided is not an experiment id or a workflow id or a task id or a composite " +
                        "identifier for job..");
            }
            if (errorResource != null) {
                errorResource.setCreationTime(AiravataUtils.getTime(error.getCreationTime()));
                errorResource.setActualErrorMsg(error.getActualErrorMessage());
                errorResource.setUserFriendlyErrorMsg(error.getUserFriendlyMessage());
                if (error.getErrorCategory() != null){
                    errorResource.setErrorCategory(error.getErrorCategory().toString());
                }
                errorResource.setTransientPersistent(error.isTransientOrPersistent());
                if (error.getCorrectiveAction() != null){
                    errorResource.setCorrectiveAction(error.getCorrectiveAction().toString());
                }else {
                    errorResource.setCorrectiveAction(CorrectiveAction.CONTACT_SUPPORT.toString());
                }
                if (error.getActionableGroup() != null){
                    errorResource.setActionableGroup(error.getActionableGroup().toString());
                }else {
                    errorResource.setActionableGroup(ActionableGroup.GATEWAYS_ADMINS.toString());
                }
                errorResource.save();
                return String.valueOf(errorResource.getErrorId());
            }
        } catch (Exception e) {
            logger.error("Unable to add error details...", e.getMessage());
            throw new Exception(e);
        }
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

    public void updateExperimentField(String expID, String fieldName, Object value) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                experiment.setExpName((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                experiment.setExecutionUser((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                experiment.setDescription((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                experiment.setApplicationId((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
                experiment.setApplicationVersion((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
                experiment.setWorkflowTemplateId((String) value);
                experiment.save();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
                experiment.setWorkflowTemplateVersion((String) value);
                experiment.save();
            } else {
                logger.error("Unsupported field type for Experiment");
            }

        } catch (Exception e) {
            logger.error("Error while updating fields in experiment...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateExpConfigDataField(String expID, String fieldName, Object value) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            ConfigDataResource exConfigData = (ConfigDataResource) experiment.get(ResourceType.CONFIG_DATA, expID);
            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                exConfigData.setAiravataAutoSchedule((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                exConfigData.setOverrideManualParams((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
                exConfigData.setShareExp((Boolean) value);
                exConfigData.save();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
                updateSchedulingData((ComputationalResourceScheduling) value, experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)) {
                updateInputDataHandling((AdvancedInputDataHandling) value, experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)) {
                updateOutputDataHandling((AdvancedOutputDataHandling) value, experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)) {
                updateQosParams((QualityOfServiceParams) value, experiment);
            } else {
                logger.error("Unsupported field type for Experiment config data");
            }

        } catch (Exception e) {
            logger.error("Error while updating fields in experiment config...", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateExperiment(Experiment experiment, String expId) throws Exception {
        try {
            ExperimentResource existingExperiment = gatewayResource.getExperiment(expId);
            existingExperiment.setExpName(experiment.getName());
            existingExperiment.setExecutionUser(experiment.getUserName());
            existingExperiment.setGateway(gatewayResource);
            if (!workerResource.isProjectExists(experiment.getProjectID())) {
                ProjectResource project = workerResource.createProject(experiment.getProjectID());
                existingExperiment.setProject(project);
            }
            existingExperiment.setCreationTime(AiravataUtils.getTime(experiment.getCreationTime()));
            existingExperiment.setDescription(experiment.getDescription());
            existingExperiment.setApplicationId(experiment.getApplicationId());
            existingExperiment.setApplicationVersion(experiment.getApplicationVersion());
            existingExperiment.setWorkflowTemplateId(experiment.getWorkflowTemplateId());
            existingExperiment.setWorkflowTemplateVersion(experiment.getWorkflowTemplateVersion());
            existingExperiment.setWorkflowExecutionId(experiment.getWorkflowExecutionInstanceId());
            existingExperiment.save();
            List<DataObjectType> experimentInputs = experiment.getExperimentInputs();
            if (experimentInputs != null && !experimentInputs.isEmpty()){
                updateExpInputs(experimentInputs, existingExperiment);
            }

            UserConfigurationData userConfigurationData = experiment.getUserConfigurationData();
            if (userConfigurationData != null) {
                updateUserConfigData(userConfigurationData, expId);
            }

            List<DataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()){
                updateExpOutputs(experimentOutputs, expId);
            }
            ExperimentStatus experimentStatus = experiment.getExperimentStatus();
            if (experimentStatus != null){
                updateExperimentStatus(experimentStatus, expId);
            }
            List<WorkflowNodeDetails> workflowNodeDetailsList = experiment.getWorkflowNodeDetailsList();
            if (workflowNodeDetailsList != null && !workflowNodeDetailsList.isEmpty()){
                for (WorkflowNodeDetails wf : workflowNodeDetailsList){
                    updateWorkflowNodeDetails(wf, wf.getNodeInstanceId());
                }
            }
            List<ErrorDetails> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()){
                for (ErrorDetails errror : errors){
                    addErrorDetails(errror, expId);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating experiment...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateUserConfigData(UserConfigurationData configData, String expId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expId);
            ConfigDataResource resource = (ConfigDataResource) experiment.get(ResourceType.CONFIG_DATA, expId);
            resource.setExperimentResource(experiment);
            resource.setAiravataAutoSchedule(configData.isAiravataAutoSchedule());
            resource.setOverrideManualParams(configData.isOverrideManualScheduledParams());
            resource.setShareExp(configData.isShareExperimentPublicly());
            resource.save();
            ComputationalResourceScheduling resourceScheduling = configData.getComputationalResourceScheduling();
            if (resourceScheduling != null) {
                updateSchedulingData(resourceScheduling, experiment);
            }
            AdvancedInputDataHandling inputDataHandling = configData.getAdvanceInputDataHandling();
            if (inputDataHandling != null) {
                updateInputDataHandling(inputDataHandling, experiment);
            }
            AdvancedOutputDataHandling outputDataHandling = configData.getAdvanceOutputDataHandling();
            if (outputDataHandling != null) {
                updateOutputDataHandling(outputDataHandling, experiment);
            }

            QualityOfServiceParams qosParams = configData.getQosParams();
            if (qosParams != null) {
                updateQosParams(qosParams, experiment);
            }
        } catch (Exception e) {
            logger.error("Error while updating user config data...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateQosParams(QualityOfServiceParams qosParams, Resource resource) throws Exception {
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource expResource = (ExperimentResource) resource;
                QosParamResource qosr = expResource.getQOSparams(expResource.getExpID());
                qosr.setExperimentResource(expResource);
                qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
                qosr.setExecuteBefore(qosParams.getExecuteBefore());
                qosr.setNoOfRetries(qosParams.getNumberofRetries());
                qosr.save();
            }
        } catch (Exception e) {
            logger.error("Error while updating QOS data...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, Resource resource) throws Exception {
        AdvancedOutputDataHandlingResource adodh;
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource expResource = (ExperimentResource) resource;
                adodh = expResource.getOutputDataHandling(expResource.getExpID());
                adodh.setExperimentResource(expResource);
            } else {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                adodh = taskDetailResource.getOutputDataHandling(taskDetailResource.getTaskId());
                adodh.setTaskDetailResource(taskDetailResource);
                adodh.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
            adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
            adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
            adodh.save();
        } catch (Exception e) {
            logger.error("Error while updating output data handling...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateInputDataHandling(AdvancedInputDataHandling inputDataHandling, Resource resource) throws Exception {
        AdvanceInputDataHandlingResource adidh;
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource expResource = (ExperimentResource) resource;
                adidh = expResource.getInputDataHandling(expResource.getExpID());
                adidh.setExperimentResource(expResource);
            } else {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                adidh = taskDetailResource.getInputDataHandling(taskDetailResource.getTaskId());
                adidh.setTaskDetailResource(taskDetailResource);
                adidh.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
            adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
            adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
            adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
            adidh.save();
        } catch (Exception e) {
            logger.error("Error while updating input data handling...", e.getMessage());
            throw new Exception(e);
        }

    }

    public void updateSchedulingData(ComputationalResourceScheduling resourceScheduling, Resource resource) throws Exception {
        ComputationSchedulingResource cmsr;
        try {
            if (resource instanceof ExperimentResource) {
                ExperimentResource expResource = (ExperimentResource) resource;
                cmsr = expResource.getComputationScheduling(expResource.getExpID());
                cmsr.setExperimentResource(expResource);
            } else {
                TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
                cmsr = taskDetailResource.getComputationScheduling(taskDetailResource.getTaskId());
                cmsr.setTaskDetailResource(taskDetailResource);
                cmsr.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
            }
            cmsr.setResourceHostId(resourceScheduling.getResourceHostId());
            cmsr.setCpuCount(resourceScheduling.getTotalCPUCount());
            cmsr.setNodeCount(resourceScheduling.getNodeCount());
            cmsr.setNumberOfThreads(resourceScheduling.getNumberOfThreads());
            cmsr.setQueueName(resourceScheduling.getQueueName());
            cmsr.setWalltimeLimit(resourceScheduling.getWallTimeLimit());
            cmsr.setJobStartTime(AiravataUtils.getTime(resourceScheduling.getJobStartTime()));
            cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
            cmsr.setProjectName(resourceScheduling.getComputationalProjectAccount());
            cmsr.save();
        } catch (Exception e) {
            logger.error("Error while updating scheduling data...", e.getMessage());
            throw new Exception(e);
        }

    }

    public List<Experiment> getExperimentList(String fieldName, Object value) throws Exception {
        List<Experiment> experiments = new ArrayList<Experiment>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                WorkerResource resource = (WorkerResource)gatewayResource.create(ResourceType.GATEWAY_WORKER);
                resource.setUser((String)value);
                List<ExperimentResource> resources = resource.getExperiments();
                for (ExperimentResource experimentResource : resources) {
                    Experiment experiment = ThriftDataModelConversion.getExperiment(experimentResource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)) {
                ProjectResource project = workerResource.getProject((String) value);
                List<ExperimentResource> resources = project.getExperiments();
                for (ExperimentResource resource : resources) {
                    Experiment experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                List<ExperimentResource> resources = gatewayResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    Experiment experiment = ThriftDataModelConversion.getExperiment(resource);
                    experiments.add(experiment);
                }
                return experiments;
            } else {
                logger.error("Unsupported field name to retrieve experiment list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment list...", e.getMessage());
            throw new Exception(e);
        }
        return experiments;
    }

    public List<WorkflowNodeDetails> getWFNodeDetails(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.WorkflowNodeConstants.EXPERIMENT_ID)) {
                ExperimentResource experiment = gatewayResource.getExperiment((String) value);
                List<WorkflowNodeDetailResource> workflowNodeDetails = experiment.getWorkflowNodeDetails();
                return ThriftDataModelConversion.getWfNodeList(workflowNodeDetails);
            } else {
                logger.error("Unsupported field name to retrieve workflow detail list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting workfkow details...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<WorkflowNodeStatus> getWFNodeStatusList(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.WorkflowNodeStatusConstants.EXPERIMENT_ID)) {
                ExperimentResource experiment = gatewayResource.getExperiment((String) value);
                List<StatusResource> workflowNodeStatuses = experiment.getWorkflowNodeStatuses();
                return ThriftDataModelConversion.getWorkflowNodeStatusList(workflowNodeStatuses);
            } else {
                logger.error("Unsupported field name to retrieve workflow status list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting workflow status...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<TaskDetails> getTaskDetails(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.TaskDetailConstants.NODE_ID)) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
                List<TaskDetailResource> taskDetails = workflowNode.getTaskDetails();
                return ThriftDataModelConversion.getTaskDetailsList(taskDetails);
            } else {
                logger.error("Unsupported field name to retrieve task detail list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting task details...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<JobDetails> getJobDetails(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.JobDetaisConstants.TASK_ID)) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
                List<JobDetailResource> jobDetailList = taskDetail.getJobDetailList();
                return ThriftDataModelConversion.getJobDetailsList(jobDetailList);
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Error while job details...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<DataTransferDetails> getDataTransferDetails(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.DataTransferDetailConstants.TASK_ID)) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
                List<DataTransferDetailResource> dataTransferDetailList = taskDetail.getDataTransferDetailList();
                return ThriftDataModelConversion.getDataTransferlList(dataTransferDetailList);
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting data transfer details...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<ErrorDetails> getErrorDetails(String fieldName, Object value) throws Exception {
        try {
            if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.EXPERIMENT_ID)) {
                ExperimentResource experiment = gatewayResource.getExperiment((String) value);
                List<ErrorDetailResource> errorDetails = experiment.getErrorDetails();
                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.NODE_ID)) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) value);
                List<ErrorDetailResource> errorDetails = workflowNode.getErrorDetails();
                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.TASK_ID)) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) value);
                List<ErrorDetailResource> errorDetailList = taskDetail.getErrorDetailList();
                return ThriftDataModelConversion.getErrorDetailList(errorDetailList);
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.JOB_ID)) {
                CompositeIdentifier cid = (CompositeIdentifier) value;
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) cid.getTopLevelIdentifier());
                JobDetailResource jobDetail = taskDetail.getJobDetail((String) cid.getSecondLevelIdentifier());
                List<ErrorDetailResource> errorDetails = jobDetail.getErrorDetails();
                return ThriftDataModelConversion.getErrorDetailList(errorDetails);
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Unable to get error details...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public Object getExperiment(String expId, String fieldName) throws Exception {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getExperiment(resource);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                return resource.getExecutionUser();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                return resource.getExpName();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                return resource.getDescription();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                return resource.getApplicationId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)) {
                return resource.getProject().getName();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
                return resource.getApplicationVersion();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
                return resource.getWorkflowTemplateId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
                return resource.getWorkflowTemplateId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS)) {
                return ThriftDataModelConversion.getExpInputs(resource.getExperimentInputs());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS)) {
                return ThriftDataModelConversion.getExpOutputs(resource.getExperimentOutputs());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
                return ThriftDataModelConversion.getExperimentStatus(resource.getExperimentStatus());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_CONFIGURATION_DATA)) {
                return ThriftDataModelConversion.getUserConfigData(resource.getUserConfigData(expId));
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_EXECUTION_ID)) {
                return resource.getWorkflowExecutionId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.STATE_CHANGE_LIST)) {
                return ThriftDataModelConversion.getWorkflowNodeStatusList(resource.getWorkflowNodeStatuses());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)) {
                return ThriftDataModelConversion.getWfNodeList(resource.getWorkflowNodeDetails());
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.ERROR_DETAIL_LIST)) {
                return ThriftDataModelConversion.getErrorDetailList(resource.getErrorDetails());
            } else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment info...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public Object getConfigData(String expId, String fieldName) throws Exception {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            ConfigDataResource userConfigData = resource.getUserConfigData(expId);
            if (fieldName == null) {
                return ThriftDataModelConversion.getUserConfigData(userConfigData);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                return userConfigData.isAiravataAutoSchedule();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                return userConfigData.isOverrideManualParams();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
                return userConfigData.isShareExp();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
                return ThriftDataModelConversion.getComputationalResourceScheduling(resource.getComputationScheduling(expId));
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)) {
                return ThriftDataModelConversion.getAdvanceInputDataHandling(resource.getInputDataHandling(expId));
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)) {
                return ThriftDataModelConversion.getAdvanceOutputDataHandling(resource.getOutputDataHandling(expId));
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)) {
                return ThriftDataModelConversion.getQOSParams(resource.getQOSparams(expId));
            } else {
                logger.error("Unsupported field name for experiment configuration data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting config data..", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public List<DataObjectType> getExperimentOutputs(String expId) throws Exception {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            List<ExperimentOutputResource> experimentOutputs = resource.getExperimentOutputs();
            return ThriftDataModelConversion.getExpOutputs(experimentOutputs);
        } catch (Exception e) {
            logger.error("Error while getting experiment outputs...", e.getMessage());
        }
        return null;
    }

    public ExperimentStatus getExperimentStatus(String expId) throws Exception {
        try {
            ExperimentResource resource = gatewayResource.getExperiment(expId);
            StatusResource experimentStatus = resource.getExperimentStatus();
            return ThriftDataModelConversion.getExperimentStatus(experimentStatus);
        } catch (Exception e) {
            logger.error("Error while getting experiment status...", e.getMessage());
            throw new Exception(e);
        }
    }

    public ComputationalResourceScheduling getComputationalScheduling(DataType type, String id) throws Exception {
        try {
            ComputationSchedulingResource computationScheduling = null;
            switch (type) {
                case EXPERIMENT:
                    ExperimentResource resource = gatewayResource.getExperiment(id);
                    computationScheduling = resource.getComputationScheduling(id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    computationScheduling = taskDetail.getComputationScheduling(id);
                    break;
            }
            if (computationScheduling != null) {
                return ThriftDataModelConversion.getComputationalResourceScheduling(computationScheduling);
            }
        } catch (Exception e) {
            logger.error("Error while getting scheduling data..", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public AdvancedInputDataHandling getInputDataHandling(DataType type, String id) throws Exception {
        try {
            AdvanceInputDataHandlingResource dataHandlingResource = null;
            switch (type) {
                case EXPERIMENT:
                    ExperimentResource resource = gatewayResource.getExperiment(id);
                    dataHandlingResource = resource.getInputDataHandling(id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    dataHandlingResource = taskDetail.getInputDataHandling(id);
                    break;
            }
            if (dataHandlingResource != null) {
                return ThriftDataModelConversion.getAdvanceInputDataHandling(dataHandlingResource);
            }
        } catch (Exception e) {
            logger.error("Error while getting input data handling..", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public AdvancedOutputDataHandling getOutputDataHandling(DataType type, String id) throws Exception {
        try {
            AdvancedOutputDataHandlingResource dataHandlingResource = null;
            switch (type) {
                case EXPERIMENT:
                    ExperimentResource resource = gatewayResource.getExperiment(id);
                    dataHandlingResource = resource.getOutputDataHandling(id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    dataHandlingResource = taskDetail.getOutputDataHandling(id);
                    break;
            }
            if (dataHandlingResource != null) {
                return ThriftDataModelConversion.getAdvanceOutputDataHandling(dataHandlingResource);
            }
        } catch (Exception e) {
            logger.error("Error while getting output data handling...", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public QualityOfServiceParams getQosParams(DataType type, String id) throws Exception {
        try {
            QosParamResource qosParamResource = null;
            switch (type) {
                case EXPERIMENT:
                    ExperimentResource resource = gatewayResource.getExperiment(id);
                    qosParamResource = resource.getQOSparams(id);
                    break;
            }
            if (qosParamResource != null) {
                return ThriftDataModelConversion.getQOSParams(qosParamResource);
            }
        } catch (Exception e) {
            logger.error("Error while getting qos params..", e.getMessage());
            throw new Exception(e);
        }
        return null;
    }

    public WorkflowNodeDetails getWorkflowNodeDetails(String nodeId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
            return ThriftDataModelConversion.getWorkflowNodeDetails(workflowNode);
        } catch (Exception e) {
            logger.error("Error while getting workflow node details...", e.getMessage());
            throw new Exception(e);
        }
    }

    public WorkflowNodeStatus getWorkflowNodeStatus(String nodeId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
            StatusResource workflowNodeStatus = workflowNode.getWorkflowNodeStatus();
            return ThriftDataModelConversion.getWorkflowNodeStatus(workflowNodeStatus);
        } catch (Exception e) {
            logger.error("Error while getting workflow node status..", e.getMessage());
            throw new Exception(e);
        }
    }

    public List<DataObjectType> getNodeOutputs(String nodeId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = resource.getWorkflowNode(nodeId);
            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
            return ThriftDataModelConversion.getNodeOutputs(nodeOutputs);
        } catch (Exception e) {
            logger.error("Error while getting node outputs..", e.getMessage());
            throw new Exception(e);
        }
    }

    public TaskDetails getTaskDetails(String taskId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            return ThriftDataModelConversion.getTaskDetail(taskDetail);
        } catch (Exception e) {
            logger.error("Error while getting task details..", e.getMessage());
            throw new Exception(e);
        }
    }

    public List<DataObjectType> getApplicationOutputs(String taskId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            List<ApplicationOutputResource> applicationOutputs = taskDetail.getApplicationOutputs();
            return ThriftDataModelConversion.getApplicationOutputs(applicationOutputs);
        } catch (Exception e) {
            logger.error("Error while getting application outputs..", e.getMessage());
            throw new Exception(e);
        }
    }

    public TaskStatus getTaskStatus(String taskId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            StatusResource taskStatus = taskDetail.getTaskStatus();
            return ThriftDataModelConversion.getTaskStatus(taskStatus);
        } catch (Exception e) {
            logger.error("Error while getting experiment outputs..", e.getMessage());
            throw new Exception(e);
        }
    }


    // ids contains task id + job id
    public JobDetails getJobDetails(CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            return ThriftDataModelConversion.getJobDetail(jobDetail);
        } catch (Exception e) {
            logger.error("Error while getting job details..", e.getMessage());
            throw new Exception(e);
        }
    }

    // ids contains task id + job id
    public JobStatus getJobStatus(CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource jobStatus = jobDetail.getJobStatus();
            return ThriftDataModelConversion.getJobStatus(jobStatus);
        } catch (Exception e) {
            logger.error("Error while getting job status..", e.getMessage());
            throw new Exception(e);
        }
    }

    public ApplicationStatus getApplicationStatus(CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource applicationStatus = jobDetail.getApplicationStatus();
            return ThriftDataModelConversion.getApplicationStatus(applicationStatus);
        } catch (Exception e) {
            logger.error("Error while getting application status..", e.getMessage());
            throw new Exception(e);
        }
    }

    public DataTransferDetails getDataTransferDetails(String transferId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
            return ThriftDataModelConversion.getDataTransferDetail(dataTransferDetail);
        } catch (Exception e) {
            logger.error("Error while getting data transfer details..", e.getMessage());
            throw new Exception(e);
        }
    }

    public TransferStatus getDataTransferStatus(String transferId) throws Exception {
        try {
            ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
            StatusResource dataTransferStatus = dataTransferDetail.getDataTransferStatus();
            return ThriftDataModelConversion.getTransferStatus(dataTransferStatus);
        } catch (Exception e) {
            logger.error("Error while getting data transfer status..", e.getMessage());
            throw new Exception(e);
        }
    }

    public List<String> getExperimentIDs(String fieldName, Object value) throws Exception {
        List<String> expIDs = new ArrayList<String>();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                if (gatewayResource == null) {
                    logger.error("You should use an existing gateway in order to retrieve experiments..");
                    return null;
                } else {
                    List<ExperimentResource> resources = gatewayResource.getExperiments();
                    for (ExperimentResource resource : resources) {
                        String expID = resource.getExpID();
                        expIDs.add(expID);
                    }
                }
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExpID());
                }
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_NAME)) {
                List<ExperimentResource> resources = workerResource.getExperiments();
                for (ExperimentResource resource : resources) {
                    expIDs.add(resource.getExpID());
                }
            }
        } catch (Exception e) {
            logger.error("Error while retrieving experiment ids..", e.getMessage());
            throw new Exception(e);
        }
        return expIDs;
    }

    public List<String> getWorkflowNodeIds(String fieldName, Object value) throws Exception {
        List<String> wfIds = new ArrayList<String>();
        List<WorkflowNodeDetails> wfNodeDetails = getWFNodeDetails(fieldName, value);
        for (WorkflowNodeDetails wf : wfNodeDetails) {
            wfIds.add(wf.getNodeInstanceId());
        }
        return wfIds;
    }

    public List<String> getTaskDetailIds(String fieldName, Object value) throws Exception {
        List<String> taskDetailIds = new ArrayList<String>();
        List<TaskDetails> taskDetails = getTaskDetails(fieldName, value);
        for (TaskDetails td : taskDetails) {
            taskDetailIds.add(td.getTaskID());
        }
        return taskDetailIds;
    }

    public List<String> getJobDetailIds(String fieldName, Object value) throws Exception {
        List<String> jobIds = new ArrayList<String>();
        List<JobDetails> jobDetails = getJobDetails(fieldName, value);
        for (JobDetails jd : jobDetails) {
            jobIds.add(jd.getJobID());
        }
        return jobIds;
    }

    public List<String> getTransferDetailIds(String fieldName, Object value) throws Exception {
        List<String> transferIds = new ArrayList<String>();
        List<DataTransferDetails> dataTransferDetails = getDataTransferDetails(fieldName, value);
        for (DataTransferDetails dtd : dataTransferDetails) {
            transferIds.add(dtd.getTransferID());
        }
        return transferIds;
    }


    public void removeExperiment(String experimentId) throws Exception {
        try {
            gatewayResource.remove(ResourceType.EXPERIMENT, experimentId);
        } catch (Exception e) {
            logger.error("Error while removing experiment..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeExperimentConfigData(String experimentId) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(experimentId);
            experiment.remove(ResourceType.CONFIG_DATA, experimentId);
        } catch (Exception e) {
            logger.error("Error while removing experiment config..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeWorkflowNode(String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            experiment.remove(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
        } catch (Exception e) {
            logger.error("Error while removing workflow node..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeTaskDetails(String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            nodeDetailResource.remove(ResourceType.TASK_DETAIL, taskId);
        } catch (Exception e) {
            logger.error("Error while removing task details..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeJobDetails(CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetailResource = nodeDetailResource.getTaskDetail((String) ids.getTopLevelIdentifier());
            taskDetailResource.remove(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
        } catch (Exception e) {
            logger.error("Error while removing job details..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeDataTransferDetails(String transferId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) nodeDetailResource.create(ResourceType.TASK_DETAIL);
            taskDetail.remove(ResourceType.DATA_TRANSFER_DETAIL, transferId);
        } catch (Exception e) {
            logger.error("Error while removing transfer details..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeComputationalScheduling(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = gatewayResource.getExperiment(id);
                    experiment.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    taskDetail.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing scheduling data..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeInputDataHandling(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = gatewayResource.getExperiment(id);
                    experiment.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    taskDetail.remove(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing input data handling..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeOutputDataHandling(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = gatewayResource.getExperiment(id);
                    experiment.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
                    break;
                case TASK_DETAIL:
                    ExperimentResource resource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) resource.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    taskDetail.remove(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing output data handling..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void removeQOSParams(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = gatewayResource.getExperiment(id);
                    experiment.remove(ResourceType.QOS_PARAM, id);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing QOS params", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isExperimentExist(String expID) throws Exception {
        try {
            return gatewayResource.isExists(ResourceType.EXPERIMENT, expID);
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isExperimentConfigDataExist(String expID) throws Exception {
        try {
            ExperimentResource experiment = gatewayResource.getExperiment(expID);
            experiment.isExists(ResourceType.CONFIG_DATA, expID);
            return true;
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isWFNodeExist(String nodeId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            return experiment.isExists(ResourceType.WORKFLOW_NODE_DETAIL, nodeId);
        } catch (Exception e) {
            logger.error("Error while retrieving workflow...", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isTaskDetailExist(String taskId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            return wf.isExists(ResourceType.TASK_DETAIL, taskId);
        } catch (Exception e) {
            logger.error("Error while retrieving task.....", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isJobDetailExist(CompositeIdentifier ids) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = wf.getTaskDetail((String) ids.getTopLevelIdentifier());
            return taskDetail.isExists(ResourceType.JOB_DETAIL, (String) ids.getSecondLevelIdentifier());
        } catch (Exception e) {
            logger.error("Error while retrieving job details.....", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isTransferDetailExist(String transferId) throws Exception {
        try {
            ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource) wf.create(ResourceType.TASK_DETAIL);
            return taskDetail.isExists(ResourceType.DATA_TRANSFER_DETAIL, transferId);
        } catch (Exception e) {
            logger.error("Error while retrieving transfer details.....", e.getMessage());
            throw new Exception(e);
        }
    }

    public boolean isComputationalSchedulingExist(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    return experiment.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    return taskDetail.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, id);
                default:
                    logger.error("Unsupported data type...");

            }
        } catch (Exception e) {
            logger.error("Error while retrieving scheduling data.....", e.getMessage());
            throw new Exception(e);
        }
        return false;
    }

    public boolean isInputDataHandlingExist(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    return experiment.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    return taskDetail.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, id);
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving input data handling.....", e.getMessage());
            throw new Exception(e);
        }
        return false;
    }

    public boolean isOutputDataHandlingExist(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    return experiment.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
                case TASK_DETAIL:
                    ExperimentResource exp = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource) exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving output data handling..", e.getMessage());
            throw new Exception(e);
        }
        return false;
    }

    public boolean isQOSParamsExist(DataType dataType, String id) throws Exception {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                    return experiment.isExists(ResourceType.QOS_PARAM, id);
//                case TASK_DETAIL:
//                    ExperimentResource exp = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
//                    WorkflowNodeDetailResource wf = (WorkflowNodeDetailResource)exp.create(ResourceType.WORKFLOW_NODE_DETAIL);
//                    TaskDetailResource taskDetail = wf.getTaskDetail(id);
//                    return taskDetail.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, id);
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving qos params..", e.getMessage());
            throw new Exception(e);
        }
        return false;
    }

    public void updateScheduling(ComputationalResourceScheduling scheduling, String id, String type) throws Exception {
        try {
            if (type.equals(DataType.EXPERIMENT.toString())) {
                ExperimentResource experiment = gatewayResource.getExperiment(id);
                updateSchedulingData(scheduling, experiment);
            } else if (type.equals(DataType.TASK_DETAIL.toString())) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateSchedulingData(scheduling, taskDetail);
            }
        } catch (Exception e) {
            logger.error("Error while updating scheduling..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateInputDataHandling(AdvancedInputDataHandling dataHandling, String id, String type) throws Exception {
        try {
            if (type.equals(DataType.EXPERIMENT.toString())) {
                ExperimentResource experiment = gatewayResource.getExperiment(id);
                updateInputDataHandling(dataHandling, experiment);
            } else if (type.equals(DataType.TASK_DETAIL.toString())) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateInputDataHandling(dataHandling, taskDetail);
            }
        } catch (Exception e) {
            logger.error("Error while updating input data handling..", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateOutputDataHandling(AdvancedOutputDataHandling dataHandling, String id, String type) throws Exception {
        try {
            if (type.equals(DataType.EXPERIMENT.toString())) {
                ExperimentResource experiment = gatewayResource.getExperiment(id);
                updateOutputDataHandling(dataHandling, experiment);
            } else if (type.equals(DataType.TASK_DETAIL.toString())) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateOutputDataHandling(dataHandling, taskDetail);
            }
        } catch (Exception e) {
            logger.error("Error while updating output data handling", e.getMessage());
            throw new Exception(e);
        }
    }

    public void updateQOSParams(QualityOfServiceParams params, String id, String type) throws Exception {
        try {
            if (type.equals(DataType.EXPERIMENT.toString())) {
                ExperimentResource experiment = gatewayResource.getExperiment(id);
                updateQosParams(params, experiment);
            } else if (type.equals(DataType.TASK_DETAIL.toString())) {
                ExperimentResource experiment = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource) experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateQosParams(params, taskDetail);
            }
        } catch (Exception e) {
            logger.error("Error while updating QOS data..", e.getMessage());
            throw new Exception(e);
        }
    }
}
