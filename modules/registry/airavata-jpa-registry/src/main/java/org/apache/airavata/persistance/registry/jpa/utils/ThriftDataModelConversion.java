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

package org.apache.airavata.persistance.registry.jpa.utils;

import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ThriftDataModelConversion {
    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static Experiment getExperiment(ExperimentResource experimentResource){
        Experiment experiment = new Experiment();
        experiment.setProjectID(experimentResource.getProject().getName());
        experiment.setCreationTime(experimentResource.getCreationTime().getTime());
        experiment.setUserName(experimentResource.getWorker().getUser());
        experiment.setName(experimentResource.getExpName());
        experiment.setDescription(experimentResource.getDescription());
        experiment.setApplicationId(experimentResource.getApplicationId());
        experiment.setApplicationVersion(experimentResource.getApplicationVersion());
        experiment.setWorkflowTemplateId(experimentResource.getWorkflowTemplateId());
        experiment.setWorkflowTemplateVersion(experimentResource.getWorkflowTemplateVersion());
        experiment.setWorkflowExecutionInstanceId(experimentResource.getWorkflowExecutionId());
        List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
        experiment.setExperimentInputs(getExpInputs(experimentInputs));
        List<ExperimentOutputResource> experimentOutputs = experimentResource.getExperimentOutputs();
        experiment.setExperimentOutputs(getExpOutputs(experimentOutputs));
        StatusResource experimentStatus = experimentResource.getExperimentStatus();
        experiment.setExperimentStatus(getExperimentStatus(experimentStatus));
        List<StatusResource> changeList = experimentResource.getWorkflowNodeStatuses();
        experiment.setStateChangeList(getWorkflowNodeStatusList(changeList));
        List<WorkflowNodeDetailResource> workflowNodeDetails = experimentResource.getWorkflowNodeDetails();
        experiment.setWorkflowNodeDetailsList(getWfNodeList(workflowNodeDetails));
        List<ErrorDetailResource> errorDetails = experimentResource.getErrorDetails();
        experiment.setErrors(getErrorDetailList(errorDetails));
        return experiment;
    }

    public static DataObjectType getInputOutput(Object object){
        DataObjectType dataObjectType = new DataObjectType();
        if (object instanceof  ExperimentInputResource){
            ExperimentInputResource expInput = (ExperimentInputResource) object;
            dataObjectType.setKey(expInput.getExperimentKey());
            dataObjectType.setValue(expInput.getValue());
            dataObjectType.setType(expInput.getInputType());
            dataObjectType.setMetaData(expInput.getMetadata());
            return dataObjectType;
        }else if (object instanceof ExperimentOutputResource){
            ExperimentOutputResource expOutput = (ExperimentOutputResource)object;
            dataObjectType.setKey(expOutput.getExperimentKey());
            dataObjectType.setValue(expOutput.getValue());
            dataObjectType.setType(expOutput.getOutputType());
            dataObjectType.setMetaData(expOutput.getMetadata());
            return dataObjectType;
        }else if (object instanceof NodeInputResource){
            NodeInputResource nodeInputResource = (NodeInputResource)object;
            dataObjectType.setKey(nodeInputResource.getInputKey());
            dataObjectType.setValue(nodeInputResource.getValue());
            dataObjectType.setType(nodeInputResource.getInputType());
            dataObjectType.setMetaData(nodeInputResource.getMetadata());
            return dataObjectType;
        }else if (object instanceof NodeOutputResource){
            NodeOutputResource nodeOutputResource = (NodeOutputResource)object;
            dataObjectType.setKey(nodeOutputResource.getOutputKey());
            dataObjectType.setValue(nodeOutputResource.getValue());
            dataObjectType.setType(nodeOutputResource.getOutputType());
            dataObjectType.setMetaData(nodeOutputResource.getMetadata());
            return dataObjectType;
        }else if (object instanceof ApplicationInputResource){
            ApplicationInputResource inputResource = (ApplicationInputResource)object;
            dataObjectType.setKey(inputResource.getInputKey());
            dataObjectType.setValue(inputResource.getValue());
            dataObjectType.setType(inputResource.getInputType());
            dataObjectType.setMetaData(inputResource.getMetadata());
            return dataObjectType;
        }else if (object instanceof ApplicationOutputResource){
            ApplicationOutputResource outputResource = (ApplicationOutputResource)object;
            dataObjectType.setKey(outputResource.getOutputKey());
            dataObjectType.setValue(outputResource.getValue());
            dataObjectType.setType(outputResource.getOutputType());
            dataObjectType.setMetaData(outputResource.getMetadata());
            return dataObjectType;
        }else {
            return null;
        }
    }

    public static List<DataObjectType> getExpInputs (List<ExperimentInputResource> exInputList){
        List<DataObjectType> expInputs = new ArrayList<DataObjectType>();
        for (ExperimentInputResource inputResource : exInputList){
            DataObjectType exInput = getInputOutput(inputResource);
            expInputs.add(exInput);
        }
        return expInputs;
    }

    public static List<DataObjectType> getExpOutputs (List<ExperimentOutputResource> experimentOutputResourceList){
        List<DataObjectType> exOutputs = new ArrayList<DataObjectType>();
        for (ExperimentOutputResource outputResource : experimentOutputResourceList){
            DataObjectType output = getInputOutput(outputResource);
            exOutputs.add(output);
        }
        return exOutputs;
    }

    public static List<DataObjectType> getNodeInputs (List<NodeInputResource> nodeInputResources){
        List<DataObjectType> nodeInputs = new ArrayList<DataObjectType>();
        for (NodeInputResource inputResource : nodeInputResources){
            DataObjectType nodeInput = getInputOutput(inputResource);
            nodeInputs.add(nodeInput);
        }
        return nodeInputs;
    }

    public static List<DataObjectType> getNodeOutputs (List<NodeOutputResource> nodeOutputResourceList){
        List<DataObjectType> nodeOutputs = new ArrayList<DataObjectType>();
        for (NodeOutputResource outputResource : nodeOutputResourceList){
            DataObjectType output = getInputOutput(outputResource);
            nodeOutputs.add(output);
        }
        return nodeOutputs;
    }

    public static List<DataObjectType> getApplicationInputs (List<ApplicationInputResource> applicationInputResources){
        List<DataObjectType> appInputs = new ArrayList<DataObjectType>();
        for (ApplicationInputResource inputResource : applicationInputResources){
            DataObjectType appInput = getInputOutput(inputResource);
            appInputs.add(appInput);
        }
        return appInputs;
    }

    public static List<DataObjectType> getApplicationOutputs (List<ApplicationOutputResource> outputResources){
        List<DataObjectType> appOutputs = new ArrayList<DataObjectType>();
        for (ApplicationOutputResource outputResource : outputResources){
            DataObjectType output = getInputOutput(outputResource);
            appOutputs.add(output);
        }
        return appOutputs;
    }

    public static ExperimentStatus getExperimentStatus(StatusResource status){
        ExperimentStatus experimentStatus = new ExperimentStatus();
        experimentStatus.setExperimentState(ExperimentState.valueOf(status.getState()));
        experimentStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return experimentStatus;
    }

    public static WorkflowNodeStatus getWorkflowNodeStatus (StatusResource status){
        WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
        workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.valueOf(status.getState()));
        workflowNodeStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return workflowNodeStatus;
    }

    public static TaskStatus getTaskStatus (StatusResource status){
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setExecutionState(TaskState.valueOf(status.getState()));
        taskStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return taskStatus;
    }

    public static JobStatus getJobStatus (StatusResource status){
        JobStatus jobStatus = new JobStatus();
        jobStatus.setJobState(JobState.valueOf(status.getState()));
        jobStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return jobStatus;
    }

    public static TransferStatus getTransferStatus (StatusResource status){
        TransferStatus transferStatus = new TransferStatus();
        transferStatus.setTransferState(TransferState.valueOf(status.getState()));
        transferStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return transferStatus;
    }

    public static ApplicationStatus getApplicationStatus (StatusResource status){
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setApplicationState(status.getState());
        applicationStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
        return applicationStatus;
    }

    public static List<WorkflowNodeStatus> getWorkflowNodeStatusList(List<StatusResource> statuses){
        List<WorkflowNodeStatus> wfNodeStatuses = new ArrayList<WorkflowNodeStatus>();
        for (StatusResource statusResource : statuses){
            wfNodeStatuses.add(getWorkflowNodeStatus(statusResource));
        }
        return wfNodeStatuses;
    }

    public static WorkflowNodeDetails getWorkflowNodeDetails(WorkflowNodeDetailResource nodeDetailResource){
        WorkflowNodeDetails wfNode = new WorkflowNodeDetails();
        wfNode.setNodeInstanceId(nodeDetailResource.getNodeInstanceId());
        wfNode.setCreationTime(nodeDetailResource.getCreationTime().getTime());
        wfNode.setNodeName(nodeDetailResource.getNodeName());
        List<NodeInputResource> nodeInputs = nodeDetailResource.getNodeInputs();
        wfNode.setNodeInputs(getNodeInputs(nodeInputs));
        List<NodeOutputResource> nodeOutputs = nodeDetailResource.getNodeOutputs();
        wfNode.setNodeOutputs(getNodeOutputs(nodeOutputs));
        List<TaskDetailResource> taskDetails = nodeDetailResource.getTaskDetails();
        wfNode.setTaskDetailsList(getTaskDetailsList(taskDetails));
        wfNode.setWorkflowNodeStatus(getWorkflowNodeStatus(nodeDetailResource.getWorkflowNodeStatus()));
        List<ErrorDetailResource> errorDetails = nodeDetailResource.getErrorDetails();
        wfNode.setErrors(getErrorDetailList(errorDetails));
        return wfNode;
    }

    public static List<WorkflowNodeDetails> getWfNodeList (List<WorkflowNodeDetailResource> resources){
        List<WorkflowNodeDetails> workflowNodeDetailsList = new ArrayList<WorkflowNodeDetails>();
        for (WorkflowNodeDetailResource resource : resources){
            workflowNodeDetailsList.add(getWorkflowNodeDetails(resource));
        }
        return workflowNodeDetailsList;
    }

    public static TaskDetails getTaskDetail (TaskDetailResource taskDetailResource){
        TaskDetails taskDetails = new TaskDetails();
        taskDetails.setTaskID(taskDetailResource.getTaskId());
        taskDetails.setApplicationId(taskDetailResource.getApplicationId());
        taskDetails.setApplicationVersion(taskDetailResource.getApplicationVersion());
        List<ApplicationInputResource> applicationInputs = taskDetailResource.getApplicationInputs();
        taskDetails.setApplicationInputs(getApplicationInputs(applicationInputs));
        List<ApplicationOutputResource> applicationOutputs = taskDetailResource.getApplicationOutputs();
        taskDetails.setApplicationOutputs(getApplicationOutputs(applicationOutputs));
        ComputationSchedulingResource computationScheduling = taskDetailResource.getComputationScheduling(taskDetailResource.getTaskId());
        taskDetails.setTaskScheduling(getComputationalResourceScheduling(computationScheduling));
        AdvanceInputDataHandlingResource inputDataHandling = taskDetailResource.getInputDataHandling(taskDetailResource.getTaskId());
        taskDetails.setAdvancedInputDataHandling(getAdvanceInputDataHandling(inputDataHandling));
        AdvancedOutputDataHandlingResource outputDataHandling = taskDetailResource.getOutputDataHandling(taskDetailResource.getTaskId());
        taskDetails.setAdvancedOutputDataHandling(getAdvanceOutputDataHandling(outputDataHandling));
        taskDetails.setTaskStatus(getTaskStatus(taskDetailResource.getTaskStatus()));
        List<JobDetailResource> jobDetailList = taskDetailResource.getJobDetailList();
        taskDetails.setJobDetailsList(getJobDetailsList(jobDetailList));
        taskDetails.setErrors(getErrorDetailList(taskDetailResource.getErrorDetailList()));
        taskDetails.setDataTransferDetailsList(getDataTransferlList(taskDetailResource.getDataTransferDetailList()));
        return taskDetails;
    }

    public static List<TaskDetails> getTaskDetailsList (List<TaskDetailResource> resources){
        List<TaskDetails> taskDetailsList = new ArrayList<TaskDetails>();
        for (TaskDetailResource resource : resources){
            taskDetailsList.add(getTaskDetail(resource));
        }
        return taskDetailsList;
    }

    public static List<JobDetails> getJobDetailsList(List<JobDetailResource> jobs){
        List<JobDetails> jobDetailsList = new ArrayList<JobDetails>();
        for (JobDetailResource resource : jobs){
            jobDetailsList.add(getJobDetail(resource));
        }
        return jobDetailsList;
    }


    public static JobDetails getJobDetail(JobDetailResource jobDetailResource){
        JobDetails jobDetails = new JobDetails();
        jobDetails.setJobID(jobDetailResource.getJobId());
        jobDetails.setJobDescription(jobDetailResource.getJobDescription());
        jobDetails.setCreationTime(jobDetailResource.getCreationTime().getTime());
        StatusResource jobStatus = jobDetailResource.getJobStatus();
        jobDetails.setJobStatus(getJobStatus(jobStatus));
        StatusResource applicationStatus = jobDetailResource.getApplicationStatus();
        jobDetails.setApplicationStatus(getApplicationStatus(applicationStatus));
        List<ErrorDetailResource> errorDetails = jobDetailResource.getErrorDetails();
        jobDetails.setErrors(getErrorDetailList(errorDetails));
        jobDetails.setComputeResourceConsumed(jobDetailResource.getComputeResourceConsumed());
        return jobDetails;
    }

    public static ErrorDetails getErrorDetails (ErrorDetailResource resource){
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorID(String.valueOf(resource.getErrorId()));
        errorDetails.setCreationTime(resource.getCreationTime().getTime());
        errorDetails.setActualErrorMessage(resource.getActualErrorMsg());
        errorDetails.setUserFriendlyMessage(resource.getUserFriendlyErrorMsg());
        errorDetails.setErrorCategory(ErrorCategory.valueOf(resource.getErrorCategory()));
        errorDetails.setTransientOrPersistent(resource.isTransientPersistent());
        errorDetails.setCorrectiveAction(CorrectiveAction.valueOf(resource.getCorrectiveAction()));
        errorDetails.setActionableGroup(ActionableGroup.valueOf(resource.getActionableGroup()));
        return errorDetails;

    }

    public static List<ErrorDetails> getErrorDetailList (List<ErrorDetailResource> errorDetailResources){
        List<ErrorDetails> errorDetailsList = new ArrayList<ErrorDetails>();
        for (ErrorDetailResource errorDetailResource : errorDetailResources){
            errorDetailsList.add(getErrorDetails(errorDetailResource));
        }
        return errorDetailsList;
    }

    public static DataTransferDetails getDataTransferDetail (DataTransferDetailResource resource){
        DataTransferDetails details = new DataTransferDetails();
        details.setTransferID(resource.getTransferId());
        details.setCreationTime(resource.getCreationTime().getTime());
        details.setTransferDescription(resource.getTransferDescription());
        details.setTransferStatus(getTransferStatus(resource.getDataTransferStatus()));
        return details;
    }

    public static List<DataTransferDetails> getDataTransferlList (List<DataTransferDetailResource> resources){
        List<DataTransferDetails> transferDetailsList = new ArrayList<DataTransferDetails>();
        for (DataTransferDetailResource resource : resources){
            transferDetailsList.add(getDataTransferDetail(resource));
        }
        return transferDetailsList;
    }


    public static UserConfigurationData getUserConfigData (ConfigDataResource resource){
        UserConfigurationData data = new UserConfigurationData();
        data.setAiravataAutoSchedule(resource.isAiravataAutoSchedule());
        data.setOverrideManualScheduledParams(resource.isOverrideManualParams());
        data.setShareExperimentPublicly(resource.isShareExp());
        ExperimentResource experimentResource = resource.getExperimentResource();
        String expID = experimentResource.getExpID();
        ComputationSchedulingResource computationScheduling = experimentResource.getComputationScheduling(expID);
        AdvanceInputDataHandlingResource inputDataHandling = experimentResource.getInputDataHandling(expID);
        AdvancedOutputDataHandlingResource outputDataHandling = experimentResource.getOutputDataHandling(expID);
        data.setAdvanceInputDataHandling(getAdvanceInputDataHandling(inputDataHandling));
        data.setAdvanceOutputDataHandling(getAdvanceOutputDataHandling(outputDataHandling));
        data.setComputationalResourceScheduling(getComputationalResourceScheduling(computationScheduling));
        data.setQosParams(getQOSParams(experimentResource.getQOSparams(expID)));
        return data;
    }


    public static ComputationalResourceScheduling getComputationalResourceScheduling (ComputationSchedulingResource csr){
        ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
        scheduling.setResourceHostId(csr.getResourceHostId());
        scheduling.setTotalCPUCount(csr.getCpuCount());
        scheduling.setNodeCount(csr.getNodeCount());
        scheduling.setNumberOfThreads(csr.getNumberOfThreads());
        scheduling.setQueueName(csr.getQueueName());
        scheduling.setWallTimeLimit(csr.getWalltimeLimit());
        scheduling.setJobStartTime((int)csr.getJobStartTime().getTime());
        scheduling.setTotalPhysicalMemory(csr.getPhysicalMemory());
        scheduling.setComputationalProjectAccount(csr.getProjectName());
        return scheduling;
    }

    public static AdvancedInputDataHandling getAdvanceInputDataHandling(AdvanceInputDataHandlingResource adhr){
        AdvancedInputDataHandling adih = new AdvancedInputDataHandling();
        adih.setStageInputFilesToWorkingDir(adhr.isStageInputFiles());
        adih.setParentWorkingDirectory(adhr.getWorkingDirParent());
        adih.setUniqueWorkingDirectory(adhr.getWorkingDir());
        adih.setCleanUpWorkingDirAfterJob(adhr.isCleanAfterJob());
        return adih;
    }

    public static AdvancedOutputDataHandling getAdvanceOutputDataHandling(AdvancedOutputDataHandlingResource adodh){
        AdvancedOutputDataHandling outputDataHandling = new AdvancedOutputDataHandling();
        outputDataHandling.setOutputDataDir(adodh.getOutputDataDir());
        outputDataHandling.setDataRegistryURL(adodh.getDataRegUrl());
        outputDataHandling.setPersistOutputData(adodh.isPersistOutputData());
        return outputDataHandling;
    }

    public static QualityOfServiceParams getQOSParams (QosParamResource qos){
        QualityOfServiceParams qosParams = new QualityOfServiceParams();
        qosParams.setStartExecutionAt(qos.getStartExecutionAt());
        qosParams.setExecuteBefore(qos.getExecuteBefore());
        qosParams.setNumberofRetries(qos.getNoOfRetries());
        return qosParams;
    }
}
