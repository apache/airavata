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

import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ThriftDataModelConversion {
    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static Project getProject (ProjectResource pr) throws RegistryException {
        if (pr != null) {
            Project project = new Project();
            project.setProjectID(pr.getId());
            project.setName(pr.getName());
            if (pr.getCreationTime()!=null) {
				project.setCreationTime(pr.getCreationTime().getTime());
			}
			project.setDescription(pr.getDescription());
            project.setOwner(pr.getWorker().getUser());
            List<ProjectUserResource> projectUserList = pr.getProjectUserList();
            List<String> sharedUsers = new ArrayList<String>();
            if (projectUserList != null && !projectUserList.isEmpty()){
                for (ProjectUserResource resource : projectUserList){
                    sharedUsers.add(resource.getUserName());
                }
            }
            project.setSharedUsers(sharedUsers);
            return project;
        }
        return null;
    }

    public static Gateway getGateway (GatewayResource resource){
        Gateway gateway = new Gateway();
        gateway.setGatewayId(resource.getGatewayId());
        gateway.setGatewayName(resource.getGatewayName());
        gateway.setDomain(resource.getDomain());
        gateway.setEmailAddress(resource.getEmailAddress());
        return gateway;
    }

    public static List<Gateway> getAllGateways (List<Resource> gatewayList){
        List<Gateway> gateways = new ArrayList<Gateway>();
        for (Resource resource : gatewayList){
            gateways.add(getGateway((GatewayResource)resource));
        }
        return gateways;
    }


    public static Experiment getExperiment(ExperimentResource experimentResource) throws RegistryException {
        if (experimentResource != null){
            Experiment experiment = new Experiment();
            if (experimentResource.getProject()!= null){
                experiment.setProjectID(experimentResource.getProject().getId());
            }
            experiment.setExperimentID(experimentResource.getExpID());
            experiment.setCreationTime(experimentResource.getCreationTime().getTime());
            experiment.setUserName(experimentResource.getExecutionUser());
            experiment.setName(experimentResource.getExpName());
            experiment.setDescription(experimentResource.getDescription());
            experiment.setApplicationId(experimentResource.getApplicationId());
            experiment.setApplicationVersion(experimentResource.getApplicationVersion());
            experiment.setWorkflowTemplateId(experimentResource.getWorkflowTemplateId());
            experiment.setEnableEmailNotification(experimentResource.isEnableEmailNotifications());
            experiment.setGatewayExecutionId(experimentResource.getGatewayExecutionId());
            if (experiment.isEnableEmailNotification()){
                List<NotificationEmailResource> notificationEmails = experimentResource.getNotificationEmails();
                experiment.setEmailAddresses(getEmailAddresses(notificationEmails));
            }
            experiment.setWorkflowTemplateVersion(experimentResource.getWorkflowTemplateVersion());
            experiment.setWorkflowExecutionInstanceId(experimentResource.getWorkflowExecutionId());
            List<ExperimentInputResource> experimentInputs = experimentResource.getExperimentInputs();
            experiment.setExperimentInputs(getExpInputs(experimentInputs));
            List<ExperimentOutputResource> experimentOutputs = experimentResource.getExperimentOutputs();
            experiment.setExperimentOutputs(getExpOutputs(experimentOutputs));
            StatusResource experimentStatus = experimentResource.getExperimentStatus();
            if (experimentStatus != null){
                experiment.setExperimentStatus(getExperimentStatus(experimentStatus));
            }
            List<StatusResource> changeList = experimentResource.getWorkflowNodeStatuses();
            if (changeList != null && !changeList.isEmpty()){
                experiment.setStateChangeList(getWorkflowNodeStatusList(changeList));
            }

            List<WorkflowNodeDetailResource> workflowNodeDetails = experimentResource.getWorkflowNodeDetails();
            if (workflowNodeDetails != null && !workflowNodeDetails.isEmpty()){
                experiment.setWorkflowNodeDetailsList(getWfNodeList(workflowNodeDetails));
            }
            List<ErrorDetailResource> errorDetails = experimentResource.getErrorDetails();
            if (errorDetails!= null && !errorDetails.isEmpty()){
                experiment.setErrors(getErrorDetailList(errorDetails));
            }
            String expID = experimentResource.getExpID();
            if (experimentResource.isExists(ResourceType.CONFIG_DATA, expID)){
                ConfigDataResource userConfigData = experimentResource.getUserConfigData(expID);
                experiment.setUserConfigurationData(getUserConfigData(userConfigData));
            }
            return experiment;
        }
        return null;
    }

    public static ExperimentSummary getExperimentSummary(ExperimentSummaryResource experimentSummaryResource) throws RegistryException {
        if (experimentSummaryResource != null){
            ExperimentSummary experimentSummary = new ExperimentSummary();
            experimentSummary.setProjectID(experimentSummaryResource.getProjectID());
            experimentSummary.setExperimentID(experimentSummaryResource.getExpID());
            experimentSummary.setCreationTime(experimentSummaryResource.getCreationTime().getTime());
            experimentSummary.setUserName(experimentSummaryResource.getExecutionUser());
            experimentSummary.setName(experimentSummaryResource.getExpName());
            experimentSummary.setDescription(experimentSummaryResource.getDescription());
            experimentSummary.setApplicationId(experimentSummaryResource.getApplicationId());
            StatusResource experimentStatus = experimentSummaryResource.getStatus();
            if (experimentStatus != null){
                experimentSummary.setExperimentStatus(getExperimentStatus(experimentStatus));
            }
            return experimentSummary;
        }
        return null;
    }

    public static InputDataObjectType getInput(Object object){
        if (object != null){
            InputDataObjectType dataObjectType = new InputDataObjectType();
            if (object instanceof  ExperimentInputResource){
                ExperimentInputResource expInput = (ExperimentInputResource) object;
                dataObjectType.setName(expInput.getExperimentKey());
                dataObjectType.setValue(expInput.getValue());
                if (expInput.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(expInput.getDataType()));
                }
                dataObjectType.setMetaData(expInput.getMetadata());
                dataObjectType.setApplicationArgument(expInput.getAppArgument());
                dataObjectType.setStandardInput(expInput.isStandardInput());
                dataObjectType.setUserFriendlyDescription(expInput.getUserFriendlyDesc());
                dataObjectType.setInputOrder(expInput.getInputOrder());
                dataObjectType.setIsRequired(expInput.getRequired());
                dataObjectType.setRequiredToAddedToCommandLine(expInput.getRequiredToCMD());
                dataObjectType.setDataStaged(expInput.isDataStaged());
                return dataObjectType;
            }else if (object instanceof NodeInputResource){
                NodeInputResource nodeInputResource = (NodeInputResource)object;
                dataObjectType.setName(nodeInputResource.getInputKey());
                dataObjectType.setValue(nodeInputResource.getValue());
                if (nodeInputResource.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(nodeInputResource.getDataType()));
                }
                dataObjectType.setMetaData(nodeInputResource.getMetadata());
                dataObjectType.setApplicationArgument(nodeInputResource.getAppArgument());
                dataObjectType.setStandardInput(nodeInputResource.isStandardInput());
                dataObjectType.setUserFriendlyDescription(nodeInputResource.getUserFriendlyDesc());
                dataObjectType.setInputOrder(nodeInputResource.getInputOrder());
                dataObjectType.setIsRequired(nodeInputResource.getRequired());
                dataObjectType.setRequiredToAddedToCommandLine(nodeInputResource.getRequiredToCMD());
                dataObjectType.setDataStaged(nodeInputResource.isDataStaged());
                return dataObjectType;
            }else if (object instanceof ApplicationInputResource){
                ApplicationInputResource inputResource = (ApplicationInputResource)object;
                dataObjectType.setName(inputResource.getInputKey());
                dataObjectType.setValue(inputResource.getValue());
                if (inputResource.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(inputResource.getDataType()));
                }
                dataObjectType.setMetaData(inputResource.getMetadata());
                dataObjectType.setApplicationArgument(inputResource.getAppArgument());
                dataObjectType.setStandardInput(inputResource.isStandardInput());
                dataObjectType.setUserFriendlyDescription(inputResource.getUserFriendlyDesc());
                dataObjectType.setInputOrder(inputResource.getInputOrder());
                dataObjectType.setIsRequired(inputResource.isRequired());
                dataObjectType.setRequiredToAddedToCommandLine(inputResource.isRequiredToCMD());
                dataObjectType.setDataStaged(inputResource.isDataStaged());
                return dataObjectType;
            }else {
                return null;
            }
        }
        return null;
    }

    public static OutputDataObjectType getOutput(Object object){
        if (object != null){
            OutputDataObjectType dataObjectType = new OutputDataObjectType();
            if (object instanceof ExperimentOutputResource){
                ExperimentOutputResource expOutput = (ExperimentOutputResource)object;
                dataObjectType.setName(expOutput.getExperimentKey());
                dataObjectType.setValue(expOutput.getValue());
                if (expOutput.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(expOutput.getDataType()));
                }
                dataObjectType.setIsRequired(expOutput.getRequired());
                dataObjectType.setRequiredToAddedToCommandLine(expOutput.getRequiredToCMD());
                dataObjectType.setDataMovement(expOutput.isDataMovement());
                dataObjectType.setLocation(expOutput.getDataNameLocation());
                dataObjectType.setSearchQuery(expOutput.getSearchQuery());
                dataObjectType.setApplicationArgument(expOutput.getAppArgument());
                return dataObjectType;
            }else if (object instanceof NodeOutputResource){
                NodeOutputResource nodeOutputResource = (NodeOutputResource)object;
                dataObjectType.setName(nodeOutputResource.getOutputKey());
                dataObjectType.setValue(nodeOutputResource.getValue());
                if (nodeOutputResource.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(nodeOutputResource.getDataType()));
                }
                dataObjectType.setIsRequired(nodeOutputResource.getRequired());
                dataObjectType.setRequiredToAddedToCommandLine(nodeOutputResource.getRequiredToCMD());
                dataObjectType.setDataMovement(nodeOutputResource.isDataMovement());
                dataObjectType.setLocation(nodeOutputResource.getDataNameLocation());
                dataObjectType.setSearchQuery(nodeOutputResource.getSearchQuery());
                dataObjectType.setApplicationArgument(nodeOutputResource.getAppArgument());
                return dataObjectType;
            }else if (object instanceof ApplicationOutputResource){
                ApplicationOutputResource outputResource = (ApplicationOutputResource)object;
                dataObjectType.setName(outputResource.getOutputKey());
                dataObjectType.setValue(outputResource.getValue());
                dataObjectType.setIsRequired(outputResource.isRequired());
                dataObjectType.setRequiredToAddedToCommandLine(outputResource.isRequiredToCMD());
                if (outputResource.getDataType() != null){
                    dataObjectType.setType(DataType.valueOf(outputResource.getDataType()));
                }
                dataObjectType.setDataMovement(outputResource.isDataMovement());
                dataObjectType.setLocation(outputResource.getDataNameLocation());
                dataObjectType.setSearchQuery(outputResource.getSearchQuery());
                dataObjectType.setApplicationArgument(outputResource.getAppArgument());
                return dataObjectType;
            }else {
                return null;
            }
        }
        return null;
    }

    public static List<String> getEmailAddresses (List<NotificationEmailResource> resourceList){
        List<String> emailAddresses = new ArrayList<String>();
        if (resourceList != null && !resourceList.isEmpty()){
            for (NotificationEmailResource emailResource : resourceList){
                emailAddresses.add(emailResource.getEmailAddress());
            }
        }
        return emailAddresses;
    }

    public static List<InputDataObjectType> getExpInputs (List<ExperimentInputResource> exInputList){
        List<InputDataObjectType> expInputs = new ArrayList<InputDataObjectType>();
        if (exInputList != null && !exInputList.isEmpty()){
            for (ExperimentInputResource inputResource : exInputList){
                InputDataObjectType exInput = getInput(inputResource);
                expInputs.add(exInput);
            }
        }
        return expInputs;
    }

    public static List<OutputDataObjectType> getExpOutputs (List<ExperimentOutputResource> experimentOutputResourceList){
        List<OutputDataObjectType> exOutputs = new ArrayList<OutputDataObjectType>();
        if (experimentOutputResourceList != null && !experimentOutputResourceList.isEmpty()){
            for (ExperimentOutputResource outputResource : experimentOutputResourceList){
                OutputDataObjectType output = getOutput(outputResource);
                exOutputs.add(output);
            }
        }
        return exOutputs;
    }

    public static List<InputDataObjectType> getNodeInputs (List<NodeInputResource> nodeInputResources){
        List<InputDataObjectType> nodeInputs = new ArrayList<InputDataObjectType>();
        if (nodeInputResources != null && !nodeInputResources.isEmpty()){
            for (NodeInputResource inputResource : nodeInputResources){
                InputDataObjectType nodeInput = getInput(inputResource);
                nodeInputs.add(nodeInput);
            }
        }
        return nodeInputs;
    }

    public static List<OutputDataObjectType> getNodeOutputs (List<NodeOutputResource> nodeOutputResourceList){
        List<OutputDataObjectType> nodeOutputs = new ArrayList<OutputDataObjectType>();
        if (nodeOutputResourceList != null && !nodeOutputResourceList.isEmpty()){
            for (NodeOutputResource outputResource : nodeOutputResourceList){
                OutputDataObjectType output = getOutput(outputResource);
                nodeOutputs.add(output);
            }
        }
        return nodeOutputs;
    }

    public static List<InputDataObjectType> getApplicationInputs (List<ApplicationInputResource> applicationInputResources){
        List<InputDataObjectType> appInputs = new ArrayList<InputDataObjectType>();
        if (applicationInputResources != null && !applicationInputResources.isEmpty()){
            for (ApplicationInputResource inputResource : applicationInputResources){
                InputDataObjectType appInput = getInput(inputResource);
                appInputs.add(appInput);
            }
        }
        return appInputs;
    }

    public static List<OutputDataObjectType> getApplicationOutputs (List<ApplicationOutputResource> outputResources){
        List<OutputDataObjectType> appOutputs = new ArrayList<OutputDataObjectType>();
        if (outputResources != null && !outputResources.isEmpty()){
            for (ApplicationOutputResource outputResource : outputResources){
                OutputDataObjectType output = getOutput(outputResource);
                appOutputs.add(output);
            }
        }
        return appOutputs;
    }

    public static ExperimentStatus getExperimentStatus(StatusResource status){
        if (status != null){
            ExperimentStatus experimentStatus = new ExperimentStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            experimentStatus.setExperimentState(ExperimentState.valueOf(status.getState()));
            experimentStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return experimentStatus;
        }
        return null;
    }

    public static WorkflowNodeStatus getWorkflowNodeStatus (StatusResource status){
        if (status != null){
            WorkflowNodeStatus workflowNodeStatus = new WorkflowNodeStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            workflowNodeStatus.setWorkflowNodeState(WorkflowNodeState.valueOf(status.getState()));
            workflowNodeStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return workflowNodeStatus;
        }
        return null;
    }

    public static TaskStatus getTaskStatus (StatusResource status){
        if (status != null){
            TaskStatus taskStatus = new TaskStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            taskStatus.setExecutionState(TaskState.valueOf(status.getState()));
            taskStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return taskStatus;
        }
        return null;
    }

    public static JobStatus getJobStatus (StatusResource status){
        if (status != null){
            JobStatus jobStatus = new JobStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            jobStatus.setJobState(JobState.valueOf(status.getState()));
            jobStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return jobStatus;
        }
        return null;
    }

    public static TransferStatus getTransferStatus (StatusResource status){
        if (status != null){
            TransferStatus transferStatus = new TransferStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            transferStatus.setTransferState(TransferState.valueOf(status.getState()));
            transferStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return transferStatus;
        }
        return null;
    }

    public static ApplicationStatus getApplicationStatus (StatusResource status){
        if (status != null){
            ApplicationStatus applicationStatus = new ApplicationStatus();
            if (status.getState() == null || status.getState().equals("")){
                status.setState("UNKNOWN");
            }
            applicationStatus.setApplicationState(status.getState());
            applicationStatus.setTimeOfStateChange(status.getStatusUpdateTime().getTime());
            return applicationStatus;
        }
        return null;
    }

    public static List<WorkflowNodeStatus> getWorkflowNodeStatusList(List<StatusResource> statuses){
        List<WorkflowNodeStatus> wfNodeStatuses = new ArrayList<WorkflowNodeStatus>();
        if (statuses != null && !statuses.isEmpty()){
            for (StatusResource statusResource : statuses){
                wfNodeStatuses.add(getWorkflowNodeStatus(statusResource));
            }
        }
        return wfNodeStatuses;
    }

    public static WorkflowNodeDetails getWorkflowNodeDetails(WorkflowNodeDetailResource nodeDetailResource) throws RegistryException {
        if (nodeDetailResource != null){
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
            wfNode.setExecutionUnit(ExecutionUnit.valueOf(nodeDetailResource.getExecutionUnit()));
            wfNode.setExecutionUnitData(nodeDetailResource.getExecutionUnitData());
            return wfNode;
        }
        return null;
    }

    public static List<WorkflowNodeDetails> getWfNodeList (List<WorkflowNodeDetailResource> resources) throws RegistryException {
        List<WorkflowNodeDetails> workflowNodeDetailsList = new ArrayList<WorkflowNodeDetails>();
        if (resources != null && !resources.isEmpty()){
            for (WorkflowNodeDetailResource resource : resources){
                workflowNodeDetailsList.add(getWorkflowNodeDetails(resource));
            }
        }
        return workflowNodeDetailsList;
    }

    public static TaskDetails getTaskDetail (TaskDetailResource taskDetailResource) throws RegistryException {
        if (taskDetailResource != null){
            TaskDetails taskDetails = new TaskDetails();
            String taskId = taskDetailResource.getTaskId();
            taskDetails.setTaskID(taskId);
            taskDetails.setApplicationId(taskDetailResource.getApplicationId());
            taskDetails.setApplicationVersion(taskDetailResource.getApplicationVersion());
            List<ApplicationInputResource> applicationInputs = taskDetailResource.getApplicationInputs();
            taskDetails.setApplicationInputs(getApplicationInputs(applicationInputs));
            List<ApplicationOutputResource> applicationOutputs = taskDetailResource.getApplicationOutputs();
            taskDetails.setApplicationOutputs(getApplicationOutputs(applicationOutputs));
            taskDetails.setEnableEmailNotification(taskDetailResource.isEnableEmailNotifications());
            if (taskDetails.isEnableEmailNotification()){
                List<NotificationEmailResource> notificationEmails = taskDetailResource.getNotificationEmails();
                taskDetails.setEmailAddresses(getEmailAddresses(notificationEmails));
            }
            taskDetails.setApplicationDeploymentId(taskDetailResource.getApplicationDeploymentId());
            if (taskDetailResource.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, taskId)){
                ComputationSchedulingResource computationScheduling = taskDetailResource.getComputationScheduling(taskId);
                taskDetails.setTaskScheduling(getComputationalResourceScheduling(computationScheduling));
            }

            if (taskDetailResource.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, taskId)){
                AdvanceInputDataHandlingResource inputDataHandling = taskDetailResource.getInputDataHandling(taskId);
                taskDetails.setAdvancedInputDataHandling(getAdvanceInputDataHandling(inputDataHandling));
            }

            if (taskDetailResource.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, taskId)){
                AdvancedOutputDataHandlingResource outputDataHandling = taskDetailResource.getOutputDataHandling(taskId);
                taskDetails.setAdvancedOutputDataHandling(getAdvanceOutputDataHandling(outputDataHandling));
            }

            taskDetails.setTaskStatus(getTaskStatus(taskDetailResource.getTaskStatus()));
            List<JobDetailResource> jobDetailList = taskDetailResource.getJobDetailList();
            taskDetails.setJobDetailsList(getJobDetailsList(jobDetailList));
            taskDetails.setErrors(getErrorDetailList(taskDetailResource.getErrorDetailList()));
            taskDetails.setDataTransferDetailsList(getDataTransferlList(taskDetailResource.getDataTransferDetailList()));
            return taskDetails;
        }
        return null;
    }

    public static List<TaskDetails> getTaskDetailsList (List<TaskDetailResource> resources) throws RegistryException {
        List<TaskDetails> taskDetailsList = new ArrayList<TaskDetails>();
        if (resources != null && !resources.isEmpty()){
            for (TaskDetailResource resource : resources){
                taskDetailsList.add(getTaskDetail(resource));
            }
        }
        return taskDetailsList;
    }

    public static List<JobDetails> getJobDetailsList(List<JobDetailResource> jobs) throws RegistryException {
        List<JobDetails> jobDetailsList = new ArrayList<JobDetails>();
        if (jobs != null && !jobs.isEmpty()){
            for (JobDetailResource resource : jobs){
                jobDetailsList.add(getJobDetail(resource));
            }
        }
        return jobDetailsList;
    }


    public static JobDetails getJobDetail(JobDetailResource jobDetailResource) throws RegistryException {
        if (jobDetailResource != null){
            JobDetails jobDetails = new JobDetails();
            jobDetails.setJobID(jobDetailResource.getJobId());
            jobDetails.setJobDescription(jobDetailResource.getJobDescription());
            jobDetails.setCreationTime(jobDetailResource.getCreationTime().getTime());
            StatusResource jobStatus = jobDetailResource.getJobStatus();
            jobDetails.setJobStatus(getJobStatus(jobStatus));
            jobDetails.setJobName(jobDetailResource.getJobName());
            jobDetails.setWorkingDir(jobDetailResource.getWorkingDir());
            StatusResource applicationStatus = jobDetailResource.getApplicationStatus();
            jobDetails.setApplicationStatus(getApplicationStatus(applicationStatus));
            List<ErrorDetailResource> errorDetails = jobDetailResource.getErrorDetails();
            jobDetails.setErrors(getErrorDetailList(errorDetails));
            jobDetails.setComputeResourceConsumed(jobDetailResource.getComputeResourceConsumed());
            return jobDetails;
        }
        return null;
    }

    public static ErrorDetails getErrorDetails (ErrorDetailResource resource){
        if (resource != null){
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
        return null;
    }

    public static List<ErrorDetails> getErrorDetailList (List<ErrorDetailResource> errorDetailResources){
        List<ErrorDetails> errorDetailsList = new ArrayList<ErrorDetails>();
        if (errorDetailResources != null && !errorDetailResources.isEmpty()){
            for (ErrorDetailResource errorDetailResource : errorDetailResources){
                errorDetailsList.add(getErrorDetails(errorDetailResource));
            }
        }
        return errorDetailsList;
    }

    public static DataTransferDetails getDataTransferDetail (DataTransferDetailResource resource) throws RegistryException {
        if (resource != null){
            DataTransferDetails details = new DataTransferDetails();
            details.setTransferID(resource.getTransferId());
            details.setCreationTime(resource.getCreationTime().getTime());
            details.setTransferDescription(resource.getTransferDescription());
            details.setTransferStatus(getTransferStatus(resource.getDataTransferStatus()));
            return details;
        }
        return null;
    }

    public static List<DataTransferDetails> getDataTransferlList (List<DataTransferDetailResource> resources) throws RegistryException {
        List<DataTransferDetails> transferDetailsList = new ArrayList<DataTransferDetails>();
        if (resources != null && !resources.isEmpty()){
            for (DataTransferDetailResource resource : resources){
                transferDetailsList.add(getDataTransferDetail(resource));
            }
        }
        return transferDetailsList;
    }


    public static UserConfigurationData getUserConfigData (ConfigDataResource resource) throws RegistryException {
        if (resource != null){
            UserConfigurationData data = new UserConfigurationData();
            data.setAiravataAutoSchedule(resource.isAiravataAutoSchedule());
            data.setOverrideManualScheduledParams(resource.isOverrideManualParams());
            data.setShareExperimentPublicly(resource.isShareExp());
            data.setUserDN(resource.getUserDn());
            data.setGenerateCert(resource.isGenerateCert());
            ExperimentResource experimentResource = resource.getExperimentResource();
            String expID = experimentResource.getExpID();
            if (experimentResource.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, expID)){
                ComputationSchedulingResource computationScheduling = experimentResource.getComputationScheduling(expID);
                data.setComputationalResourceScheduling(getComputationalResourceScheduling(computationScheduling));
            }

            if (experimentResource.isExists(ResourceType.ADVANCE_INPUT_DATA_HANDLING, expID)){
                AdvanceInputDataHandlingResource inputDataHandling = experimentResource.getInputDataHandling(expID);
                data.setAdvanceInputDataHandling(getAdvanceInputDataHandling(inputDataHandling));
            }

            if (experimentResource.isExists(ResourceType.ADVANCE_OUTPUT_DATA_HANDLING, expID)){
                AdvancedOutputDataHandlingResource outputDataHandling = experimentResource.getOutputDataHandling(expID);
                data.setAdvanceOutputDataHandling(getAdvanceOutputDataHandling(outputDataHandling));
            }

            if (experimentResource.isExists(ResourceType.QOS_PARAM, expID)){
                QosParamResource qoSparams = experimentResource.getQOSparams(expID);
                data.setQosParams(getQOSParams(qoSparams));
            }
            return data;
        }
        return null;
    }


    public static ComputationalResourceScheduling getComputationalResourceScheduling (ComputationSchedulingResource csr){
        if (csr != null){
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
            scheduling.setChassisName(csr.getChessisName());
            return scheduling;
        }
        return null;
    }

    public static AdvancedInputDataHandling getAdvanceInputDataHandling(AdvanceInputDataHandlingResource adhr){
        if (adhr != null){
            AdvancedInputDataHandling adih = new AdvancedInputDataHandling();
            adih.setStageInputFilesToWorkingDir(adhr.isStageInputFiles());
            adih.setParentWorkingDirectory(adhr.getWorkingDirParent());
            adih.setUniqueWorkingDirectory(adhr.getWorkingDir());
            adih.setCleanUpWorkingDirAfterJob(adhr.isCleanAfterJob());
            return adih;
        }
        return null;
    }

    public static AdvancedOutputDataHandling getAdvanceOutputDataHandling(AdvancedOutputDataHandlingResource adodh){
        if (adodh != null){
            AdvancedOutputDataHandling outputDataHandling = new AdvancedOutputDataHandling();
            outputDataHandling.setOutputDataDir(adodh.getOutputDataDir());
            outputDataHandling.setDataRegistryURL(adodh.getDataRegUrl());
            outputDataHandling.setPersistOutputData(adodh.isPersistOutputData());
            return outputDataHandling;
        }
        return null;
    }

    public static QualityOfServiceParams getQOSParams (QosParamResource qos){
        if (qos != null){
            QualityOfServiceParams qosParams = new QualityOfServiceParams();
            qosParams.setStartExecutionAt(qos.getStartExecutionAt());
            qosParams.setExecuteBefore(qos.getExecuteBefore());
            qosParams.setNumberofRetries(qos.getNoOfRetries());
            return qosParams;
        }
        return null;
    }



}
