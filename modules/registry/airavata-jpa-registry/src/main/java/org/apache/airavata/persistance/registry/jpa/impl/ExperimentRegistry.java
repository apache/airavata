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
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.airavata.registry.cpi.utils.StatusType;
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
            if (experimentInputs != null){
                addExpInputs(experimentInputs, experimentResource);
            }

        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }catch (Exception e){
            logger.error("Error while saving experiment to registry", e.getMessage());
        }
        return experimentID;
    }

    public String addUserConfigData(UserConfigurationData configurationData, String experimentID) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(experimentID);
            ConfigDataResource configData = (ConfigDataResource)experiment.create(ResourceType.CONFIG_DATA);
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
                addOutputDataHandling(outputDataHandling,experiment);
            }

            QualityOfServiceParams qosParams = configurationData.getQosParams();
            if (qosParams != null) {
                addQosParams(qosParams,experiment);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }catch (Exception e){
            logger.error("Unable to save user config data", e.getMessage());
        }
        return experimentID;
    }

    public void addQosParams(QualityOfServiceParams qosParams, Resource resource) {
        QosParamResource qosr = new QosParamResource();
        if (resource instanceof  ExperimentResource){
            ExperimentResource experiment = (ExperimentResource)resource;
            qosr.setExperimentResource(experiment);
        }
        if (resource instanceof TaskDetailResource){
            TaskDetailResource taskDetailResource = (TaskDetailResource)resource;
            qosr.setTaskDetailResource(taskDetailResource);
        }
        qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
        qosr.setExecuteBefore(qosParams.getExecuteBefore());
        qosr.setNoOfRetries(qosParams.getNumberofRetries());
        qosr.save();
    }

    public void addOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, Resource resource) {
        AdvancedOutputDataHandlingResource adodh = new AdvancedOutputDataHandlingResource();
        if (resource instanceof  ExperimentResource){
            ExperimentResource experiment = (ExperimentResource)resource;
            adodh.setExperimentResource(experiment);
        }
        if (resource instanceof TaskDetailResource){
            TaskDetailResource taskDetailResource = (TaskDetailResource)resource;
            adodh.setTaskDetailResource(taskDetailResource);
        }
        adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
        adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
        adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
        adodh.save();
    }

    public void addInputDataHandling(AdvancedInputDataHandling inputDataHandling, Resource resource) {
        AdvanceInputDataHandlingResource adidh = new AdvanceInputDataHandlingResource();
        if (resource instanceof  ExperimentResource){
            ExperimentResource experiment = (ExperimentResource)resource;
            adidh.setExperimentResource(experiment);
        }
        if (resource instanceof TaskDetailResource){
            TaskDetailResource taskDetailResource = (TaskDetailResource)resource;
            adidh.setTaskDetailResource(taskDetailResource);
        }
        adidh.setWorkingDir(inputDataHandling.getUniqueWorkingDirectory());
        adidh.setWorkingDirParent(inputDataHandling.getParentWorkingDirectory());
        adidh.setStageInputFiles(inputDataHandling.isSetStageInputFilesToWorkingDir());
        adidh.setCleanAfterJob(inputDataHandling.isCleanUpWorkingDirAfterJob());
        adidh.save();
    }

    public void addComputationScheduling(ComputationalResourceScheduling resourceScheduling, Resource resource) {
        ComputationSchedulingResource cmsr = new ComputationSchedulingResource();
        if (resource instanceof  ExperimentResource){
            ExperimentResource experiment = (ExperimentResource)resource;
            cmsr.setExperimentResource(experiment);
        }
        if (resource instanceof TaskDetailResource){
            TaskDetailResource taskDetailResource = (TaskDetailResource)resource;
            cmsr.setTaskDetailResource(taskDetailResource);
        }
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

    public String addExpOutputs(List<DataObjectType> exOutput, String expId) {
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
        return expId;
    }

    public void updateExpOutputs(List<DataObjectType> exOutput, String expId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expId);
            List<ExperimentOutputResource> existingExpOutputs = experiment.getExperimentOutputs();
            for (DataObjectType output : exOutput) {
                for (ExperimentOutputResource resource : existingExpOutputs){
                    if (resource.getExperimentKey().equals(output.getKey())){
                        resource.setExperimentResource(experiment);
                        resource.setExperimentKey(output.getKey());
                        resource.setValue(output.getValue());
                        resource.setOutputType(output.getType());
                        resource.setMetadata(output.getMetaData());
                        resource.save();
                    }
                }
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String addNodeOutputs (List<DataObjectType> wfOutputs, CompositeIdentifier ids ) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String)ids.getTopLevelIdentifier());
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
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return (String)ids.getSecondLevelIdentifier();
    }

    public void updateNodeOutputs (List<DataObjectType> wfOutputs, String nodeId ) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            List<NodeOutputResource> nodeOutputs = workflowNode.getNodeOutputs();
            for (DataObjectType output : wfOutputs) {
                for (NodeOutputResource resource : nodeOutputs){
                    resource.setNodeDetailResource(workflowNode);
                    resource.setOutputKey(output.getKey());
                    resource.setValue(output.getValue());
                    resource.setOutputType(output.getType());
                    resource.setMetadata(output.getMetaData());
                    resource.save();
                }
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String addApplicationOutputs (List<DataObjectType> appOutputs, CompositeIdentifier ids ) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
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
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return (String)ids.getSecondLevelIdentifier();
    }

    public String updateExperimentStatus (ExperimentStatus experimentStatus, String expId){
        try{
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expId);
            StatusResource status = experiment.getExperimentStatus();
            if (status == null){
                status = (StatusResource)experiment.create(ResourceType.STATUS);
            }
            status.setExperimentResource(experiment);
            status.setStatusUpdateTime(getTime(experimentStatus.getTimeOfStateChange()));
            status.setState(experimentStatus.getExperimentState().toString());
            status.setStatusType(StatusType.EXPERIMENT.toString());
            status.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return expId;
    }

    public String addWorkflowNodeStatus(WorkflowNodeStatus status, CompositeIdentifier ids) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String)ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource)experiment.create(ResourceType.STATUS);
            statusResource.setExperimentResource(experiment);
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getWorkflowNodeState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String updateWorkflowNodeStatus(WorkflowNodeStatus status, String nodeId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            StatusResource statusResource = workflowNode.getWorkflowNodeStatus();
            statusResource.setExperimentResource(workflowNode.getExperimentResource());
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setStatusType(StatusType.WORKFLOW_NODE.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getWorkflowNodeState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String addTaskStatus(TaskStatus status, CompositeIdentifier ids) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode((String) ids.getTopLevelIdentifier());
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource)workflowNode.create(ResourceType.STATUS);
            statusResource.setExperimentResource(workflowNode.getExperimentResource());
            statusResource.setWorkflowNodeDetail(workflowNode);
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.TASK.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getExecutionState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void updateTaskStatus(TaskStatus status, String taskId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            StatusResource statusResource = workflowNode.geTaskStatus(taskId);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.TASK.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getExecutionState().toString());
            statusResource.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    /**
     *
     * @param status job status
     * @param ids composite id will contain taskid and jobid
     * @return status id
     */
    public String addJobStatus(JobStatus status, CompositeIdentifier ids) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource)jobDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.JOB.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getJobState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String updateJobStatus(JobStatus status, String jobId) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
            StatusResource statusResource = jobDetail.getJobStatus();
            statusResource.setExperimentResource(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(jobDetail.getTaskDetailResource());
            statusResource.setStatusType(StatusType.JOB.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getJobState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    /**
     * @param status application status
     * @param ids composite id will contain taskid and jobid
     * @return status id
     */
    public String addApplicationStatus(ApplicationStatus status, CompositeIdentifier ids) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.getJobDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource)jobDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setStatusType(StatusType.APPLICATION.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getApplicationState());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void updateApplicationStatus(ApplicationStatus status, String jobId) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
            StatusResource statusResource = jobDetail.getApplicationStatus();
            statusResource.setExperimentResource(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(jobDetail.getTaskDetailResource().getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(jobDetail.getTaskDetailResource());
            statusResource.setStatusType(StatusType.APPLICATION.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getApplicationState());
            statusResource.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }


    /**
     *
     * @param status data transfer status
     * @param ids contains taskId and transfer id
     * @return status id
     */
    public String addTransferStatus(TransferStatus status, CompositeIdentifier ids) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail((String) ids.getSecondLevelIdentifier());
            StatusResource statusResource = (StatusResource)dataTransferDetail.create(ResourceType.STATUS);
            statusResource.setExperimentResource(taskDetail.getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(taskDetail.getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(taskDetail);
            statusResource.setDataTransferDetail(dataTransferDetail);
            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getTransferState().toString());
            statusResource.save();
            return String.valueOf(statusResource.getStatusId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void updateTransferStatus(TransferStatus status, String transferId) {
        try {
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource dataTransferDetail = taskDetail.getDataTransferDetail(transferId);
            StatusResource statusResource = dataTransferDetail.getDataTransferStatus();
            statusResource.setExperimentResource(dataTransferDetail.getTaskDetailResource().getWorkflowNodeDetailResource().getExperimentResource());
            statusResource.setWorkflowNodeDetail(dataTransferDetail.getTaskDetailResource().getWorkflowNodeDetailResource());
            statusResource.setTaskDetailResource(dataTransferDetail.getTaskDetailResource());
            statusResource.setDataTransferDetail(dataTransferDetail);
            statusResource.setStatusType(StatusType.DATA_TRANSFER.toString());
            statusResource.setStatusUpdateTime(getTime(status.getTimeOfStateChange()));
            statusResource.setState(status.getTransferState().toString());
            statusResource.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String addWorkflowNodeDetails (WorkflowNodeDetails nodeDetails, String expId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment(expId);
            WorkflowNodeDetailResource resource = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            resource.setExperimentResource(experiment);
            resource.setNodeName(nodeDetails.getNodeName());
            resource.setCreationTime(getTime(nodeDetails.getCreationTime()));
            resource.setNodeInstanceId(getNodeInstanceID(nodeDetails.getNodeName()));
            resource.save();
            List<DataObjectType> nodeInputs = nodeDetails.getNodeInputs();
            if (nodeInputs != null){
                addWorkflowInputs (nodeDetails.getNodeInputs(), resource);
            }
            return resource.getNodeInstanceId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void updateWorkflowNodeDetails (WorkflowNodeDetails nodeDetails, String nodeId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            workflowNode.setExperimentResource(experiment);
            workflowNode.setNodeName(nodeDetails.getNodeName());
            workflowNode.setCreationTime(getTime(nodeDetails.getCreationTime()));
            workflowNode.setNodeInstanceId(getNodeInstanceID(nodeDetails.getNodeName()));
            workflowNode.save();
            List<DataObjectType> nodeInputs = nodeDetails.getNodeInputs();
            if (nodeInputs != null){
                updateWorkflowInputs(nodeDetails.getNodeInputs(), workflowNode);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }


    public void addWorkflowInputs (List<DataObjectType> wfInputs, WorkflowNodeDetailResource nodeDetailResource ){
        for (DataObjectType input : wfInputs){
            NodeInputResource resource = (NodeInputResource)nodeDetailResource.create(ResourceType.NODE_INPUT);
            resource.setNodeDetailResource(nodeDetailResource);
            resource.setInputKey(input.getKey());
            resource.setValue(input.getValue());
            resource.setInputType(input.getType());
            resource.setMetadata(input.getMetaData());
            resource.save();
        }
    }

    public void updateWorkflowInputs (List<DataObjectType> wfInputs, WorkflowNodeDetailResource nodeDetailResource ){
        List<NodeInputResource> nodeInputs = nodeDetailResource.getNodeInputs();
        for (DataObjectType input : wfInputs){
            for (NodeInputResource resource : nodeInputs){
                resource.setNodeDetailResource(nodeDetailResource);
                resource.setInputKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setInputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }
        }
    }

    public String addTaskDetails (TaskDetails taskDetails, String nodeId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = experiment.getWorkflowNode(nodeId);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            taskDetail.setWorkflowNodeDetailResource(workflowNode);
            taskDetail.setTaskId(getTaskID(workflowNode.getNodeName()));
            taskDetail.setApplicationId(taskDetails.getApplicationId());
            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
            taskDetail.setCreationTime(getTime(taskDetails.getCreationTime()));
            taskDetail.save();
            List<DataObjectType> applicationInputs = taskDetails.getApplicationInputs();
            if (applicationInputs != null){
                addAppInputs(applicationInputs, taskDetail);
            }
            ComputationalResourceScheduling taskScheduling = taskDetails.getTaskScheduling();
            if (taskScheduling != null){
                addComputationScheduling(taskScheduling, taskDetail);
            }
            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
            if (inputDataHandling != null){
                addInputDataHandling(inputDataHandling, taskDetail);
            }
            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
            if (outputDataHandling != null){
                addOutputDataHandling(outputDataHandling, taskDetail);
            }
            return taskDetail.getTaskId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String updateTaskDetails (TaskDetails taskDetails, String taskId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            taskDetail.setWorkflowNodeDetailResource(workflowNode);
            taskDetail.setTaskId(getTaskID(workflowNode.getNodeName()));
            taskDetail.setApplicationId(taskDetails.getApplicationId());
            taskDetail.setApplicationVersion(taskDetails.getApplicationVersion());
            taskDetail.setCreationTime(getTime(taskDetails.getCreationTime()));
            taskDetail.save();
            List<DataObjectType> applicationInputs = taskDetails.getApplicationInputs();
            if (applicationInputs != null){
                updateAppInputs(applicationInputs, taskDetail);
            }
            ComputationalResourceScheduling taskScheduling = taskDetails.getTaskScheduling();
            if (taskScheduling != null){
                updateSchedulingData(taskScheduling, taskDetail);
            }
            AdvancedInputDataHandling inputDataHandling = taskDetails.getAdvancedInputDataHandling();
            if (inputDataHandling != null){
                updateInputDataHandling(inputDataHandling, taskDetail);
            }
            AdvancedOutputDataHandling outputDataHandling = taskDetails.getAdvancedOutputDataHandling();
            if (outputDataHandling != null){
                updateOutputDataHandling(outputDataHandling, taskDetail);
            }
            return taskDetail.getTaskId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void addAppInputs (List<DataObjectType> appInputs, TaskDetailResource taskDetailResource ){
        for (DataObjectType input :  appInputs){
            ApplicationInputResource resource = (ApplicationInputResource)taskDetailResource.create(ResourceType.APPLICATION_INPUT);
            resource.setTaskDetailResource(taskDetailResource);
            resource.setInputKey(input.getKey());
            resource.setValue(input.getValue());
            resource.setInputType(input.getType());
            resource.setMetadata(input.getMetaData());
            resource.save();
        }
    }

    public void updateAppOutputs (List<DataObjectType> appOutputs, String taskId ) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource) gateway.create(ResourceType.EXPERIMENT);
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
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public void updateAppInputs (List<DataObjectType> appInputs, TaskDetailResource taskDetailResource ){
        List<ApplicationInputResource> inputs = taskDetailResource.getApplicationInputs();
        for (DataObjectType input :  appInputs){
            for (ApplicationInputResource resource : inputs){
                resource.setTaskDetailResource(taskDetailResource);
                resource.setInputKey(input.getKey());
                resource.setValue(input.getValue());
                resource.setInputType(input.getType());
                resource.setMetadata(input.getMetaData());
                resource.save();
            }

        }
    }

    public String addJobDetails (JobDetails jobDetails, CompositeIdentifier ids) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail((String) ids.getTopLevelIdentifier());
            JobDetailResource jobDetail = taskDetail.createJobDetail((String) ids.getSecondLevelIdentifier());
            jobDetail.setTaskDetailResource(taskDetail);
            jobDetail.setJobDescription(jobDetails.getJobDescription());
            jobDetail.setCreationTime(getTime(jobDetails.getCreationTime()));
            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
            jobDetail.save();
            return jobDetail.getJobId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public void updateJobDetails (JobDetails jobDetails, String jobId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            JobDetailResource jobDetail = taskDetail.getJobDetail(jobId);
            jobDetail.setTaskDetailResource(jobDetail.getTaskDetailResource());
            jobDetail.setJobDescription(jobDetails.getJobDescription());
            jobDetail.setCreationTime(getTime(jobDetails.getCreationTime()));
            jobDetail.setComputeResourceConsumed(jobDetails.getComputeResourceConsumed());
            jobDetail.save();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
    }

    public String addDataTransferDetails (DataTransferDetails transferDetails, String taskId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = workflowNode.getTaskDetail(taskId);
            DataTransferDetailResource resource = (DataTransferDetailResource)taskDetail.create(ResourceType.DATA_TRANSFER_DETAIL);
            resource.setTaskDetailResource(taskDetail);
            resource.setTransferId(getDataTransferID(taskId));
            resource.setTransferDescription(transferDetails.getTransferDescription());
            resource.setCreationTime(getTime(transferDetails.getCreationTime()));
            resource.save();
            return resource.getTransferId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String updateDataTransferDetails (DataTransferDetails transferDetails, String transferId) {
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = (ExperimentResource)gateway.create(ResourceType.EXPERIMENT);
            WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = (TaskDetailResource)workflowNode.create(ResourceType.TASK_DETAIL);
            DataTransferDetailResource resource = taskDetail.getDataTransferDetail(transferId);
            resource.setTaskDetailResource(taskDetail);
            resource.setTransferDescription(transferDetails.getTransferDescription());
            resource.setCreationTime(getTime(transferDetails.getCreationTime()));
            resource.save();
            return resource.getTransferId();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param scheduling computational resource object
     * @param ids contains expId and taskId
     * @return scheduling id
     */
    public String addComputationalResourceScheduling (ComputationalResourceScheduling scheduling, CompositeIdentifier ids){
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
            ComputationSchedulingResource schedulingResource = (ComputationSchedulingResource)experiment.create(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING);
            schedulingResource.setExperimentResource(experiment);
            schedulingResource.setTaskDetailResource(taskDetail);
            schedulingResource.setResourceHostId(scheduling.getResourceHostId());
            schedulingResource.setCpuCount(scheduling.getTotalCPUCount());
            schedulingResource.setNodeCount(scheduling.getNodeCount());
            schedulingResource.setNumberOfThreads(scheduling.getNumberOfThreads());
            schedulingResource.setQueueName(scheduling.getQueueName());
            schedulingResource.setWalltimeLimit(scheduling.getWallTimeLimit());
            schedulingResource.setJobStartTime(getTime(scheduling.getJobStartTime()));
            schedulingResource.setPhysicalMemory(scheduling.getTotalPhysicalMemory());
            schedulingResource.setProjectName(scheduling.getComputationalProjectAccount());
            schedulingResource.save();
            return String.valueOf(schedulingResource.getSchedulingId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param dataHandling advanced input data handling object
     * @param ids contains expId and taskId
     * @return data handling id
     */
    public String addInputDataHandling (AdvancedInputDataHandling dataHandling, CompositeIdentifier ids){
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
            AdvanceInputDataHandlingResource dataHandlingResource = (AdvanceInputDataHandlingResource)experiment.create(ResourceType.ADVANCE_INPUT_DATA_HANDLING);
            dataHandlingResource.setExperimentResource(experiment);
            dataHandlingResource.setTaskDetailResource(taskDetail);
            dataHandlingResource.setWorkingDir(dataHandling.getUniqueWorkingDirectory());
            dataHandlingResource.setWorkingDirParent(dataHandling.getParentWorkingDirectory());
            dataHandlingResource.setStageInputFiles(dataHandling.isStageInputFilesToWorkingDir());
            dataHandlingResource.setCleanAfterJob(dataHandling.isCleanUpWorkingDirAfterJob());
            dataHandlingResource.save();
            return String.valueOf(dataHandlingResource.getDataHandlingId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param dataHandling advanced output data handling object
     * @param ids contains expId and taskId
     * @return data handling id
     */
    public String addOutputDataHandling (AdvancedOutputDataHandling dataHandling, CompositeIdentifier ids){
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
            AdvancedOutputDataHandlingResource dataHandlingResource = (AdvancedOutputDataHandlingResource)experiment.create(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING);
            dataHandlingResource.setExperimentResource(experiment);
            dataHandlingResource.setTaskDetailResource(taskDetail);
            dataHandlingResource.setOutputDataDir(dataHandling.getOutputDataDir());
            dataHandlingResource.setDataRegUrl(dataHandling.getDataRegistryURL());
            dataHandlingResource.setPersistOutputData(dataHandling.isPersistOutputData());
            dataHandlingResource.save();
            return String.valueOf(dataHandlingResource.getOutputDataHandlingId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String addQosParams (QualityOfServiceParams qosParams, CompositeIdentifier ids){
        try {
            gatewayRegistry = new GatewayRegistry();
            GatewayResource gateway = gatewayRegistry.getDefaultGateway();
            ExperimentResource experiment = gateway.getExperiment((String) ids.getTopLevelIdentifier());
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
            TaskDetailResource taskDetail = nodeDetailResource.getTaskDetail((String) ids.getSecondLevelIdentifier());
            QosParamResource qosParamResource = (QosParamResource)experiment.create(ResourceType.QOS_PARAM);
            qosParamResource.setExperimentResource(experiment);
            qosParamResource.setTaskDetailResource(taskDetail);
            qosParamResource.setStartExecutionAt(qosParams.getStartExecutionAt());
            qosParamResource.setExecuteBefore(qosParams.getExecuteBefore());
            qosParamResource.setNoOfRetries(qosParams.getNumberofRetries());
            qosParamResource.save();
            return String.valueOf(qosParamResource.getQosId());
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties", e.getMessage());
        }
        return null;
    }

    public String getNodeInstanceID(String nodeName) {
        return nodeName + "_" + UUID.randomUUID();
    }

    public String getExperimentID(String experimentName) {
        return experimentName + "_" + UUID.randomUUID();
    }

    public String getTaskID(String nodeName) {
        return nodeName + "_" + UUID.randomUUID();
    }

    public String getDataTransferID (String taskId){
        return taskId + "_" + UUID.randomUUID();
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
                updateSchedulingData((ComputationalResourceScheduling) value, experiment);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)){
                updateInputDataHandling((AdvancedInputDataHandling) value, experiment);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)){
                updateOutputDataHandling((AdvancedOutputDataHandling) value, experiment);
            }else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)){
                updateQosParams((QualityOfServiceParams) value, experiment);
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
        resource.save();
    }

    public void updateQosParams(QualityOfServiceParams qosParams, Resource resource) {
        if (resource instanceof  ExperimentResource){
            ExperimentResource expResource = (ExperimentResource) resource;
            QosParamResource qosr = expResource.getQOSparams(expResource.getExpID());
            qosr.setExperimentResource(expResource);
            qosr.setStartExecutionAt(qosParams.getStartExecutionAt());
            qosr.setExecuteBefore(qosParams.getExecuteBefore());
            qosr.setNoOfRetries(qosParams.getNumberofRetries());
            qosr.save();
        }
    }

    public void updateOutputDataHandling(AdvancedOutputDataHandling outputDataHandling, Resource resource) {
        AdvancedOutputDataHandlingResource adodh;
        if (resource instanceof ExperimentResource){
            ExperimentResource expResource = (ExperimentResource) resource;
            adodh = expResource.getOutputDataHandling(expResource.getExpID());
            adodh.setExperimentResource(expResource);
        }else {
            TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
            adodh = taskDetailResource.getOutputDataHandling(taskDetailResource.getTaskId());
            adodh.setTaskDetailResource(taskDetailResource);
            adodh.setExperimentResource(taskDetailResource.getWorkflowNodeDetailResource().getExperimentResource());
        }
        adodh.setOutputDataDir(outputDataHandling.getOutputDataDir());
        adodh.setDataRegUrl(outputDataHandling.getDataRegistryURL());
        adodh.setPersistOutputData(outputDataHandling.isPersistOutputData());
        adodh.save();
    }

    public void updateInputDataHandling(AdvancedInputDataHandling inputDataHandling, Resource resource) {

        AdvanceInputDataHandlingResource adidh;
        if (resource instanceof  ExperimentResource){
            ExperimentResource expResource = (ExperimentResource) resource;
            adidh = expResource.getInputDataHandling(expResource.getExpID());
            adidh.setExperimentResource(expResource);
        }else {
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
    }

    public void updateSchedulingData(ComputationalResourceScheduling resourceScheduling, Resource resource) {
        ComputationSchedulingResource cmsr;
        if (resource instanceof ExperimentResource){
            ExperimentResource expResource = (ExperimentResource) resource;
            cmsr = expResource.getComputationScheduling(expResource.getExpID());
            cmsr.setExperimentResource(expResource);
        }else {
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
        cmsr.setJobStartTime(getTime(resourceScheduling.getJobStartTime()));
        cmsr.setPhysicalMemory(resourceScheduling.getTotalPhysicalMemory());
        cmsr.setProjectName(resourceScheduling.getComputationalProjectAccount());
        cmsr.save();
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

    public void updateScheduling(ComputationalResourceScheduling scheduling, String id, String type){
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            if (type.equals(DataType.EXPERIMENT.toString())){
                ExperimentResource experiment = defaultGateway.getExperiment(id);
                updateSchedulingData(scheduling, experiment);
            }else if (type.equals(DataType.TASK_DETAIL.toString())){
                ExperimentResource experiment = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateSchedulingData(scheduling, taskDetail);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public void updateInputDataHandling(AdvancedInputDataHandling dataHandling, String id, String type){
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            if (type.equals(DataType.EXPERIMENT.toString())){
                ExperimentResource experiment = defaultGateway.getExperiment(id);
                updateInputDataHandling(dataHandling, experiment);
            }else if (type.equals(DataType.TASK_DETAIL.toString())){
                ExperimentResource experiment = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateInputDataHandling(dataHandling, taskDetail);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public void updateOutputDataHandling(AdvancedOutputDataHandling dataHandling, String id, String type){
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            if (type.equals(DataType.EXPERIMENT.toString())){
                ExperimentResource experiment = defaultGateway.getExperiment(id);
                updateOutputDataHandling(dataHandling, experiment);
            }else if (type.equals(DataType.TASK_DETAIL.toString())){
                ExperimentResource experiment = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateOutputDataHandling(dataHandling, taskDetail);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
    }

    public void updateQOSParams(QualityOfServiceParams params, String id, String type){
        try {
            GatewayResource defaultGateway = gatewayRegistry.getDefaultGateway();
            if (type.equals(DataType.EXPERIMENT.toString())){
                ExperimentResource experiment = defaultGateway.getExperiment(id);
                updateQosParams(params, experiment);
            }else if (type.equals(DataType.TASK_DETAIL.toString())){
                ExperimentResource experiment = (ExperimentResource)defaultGateway.create(ResourceType.EXPERIMENT);
                WorkflowNodeDetailResource workflowNode = (WorkflowNodeDetailResource)experiment.create(ResourceType.WORKFLOW_NODE_DETAIL);
                TaskDetailResource taskDetail = workflowNode.getTaskDetail(id);
                updateQosParams(params, taskDetail);
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server properties..", e.getMessage());
        }
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
