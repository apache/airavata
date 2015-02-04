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

package org.apache.airavata.model.util;


import java.util.Calendar;
import java.util.List;

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.AdvancedInputDataHandling;
import org.apache.airavata.model.workspace.experiment.AdvancedOutputDataHandling;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.QualityOfServiceParams;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.UserConfigurationData;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeStatus;


public class ExperimentModelUtil {
	
	public static WorkflowNodeStatus createWorkflowNodeStatus(WorkflowNodeState state){
		WorkflowNodeStatus status = new WorkflowNodeStatus();
        status.setWorkflowNodeState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        return status;
	}
	
    public static Experiment createSimpleExperiment(String projectID,
                                                    String userName,
                                                    String experimentName,
                                                    String expDescription,
                                                    String applicationId,
                                                    List<InputDataObjectType> experimentInputList) {
        Experiment experiment = new Experiment();
        experiment.setProjectID(projectID);
        experiment.setUserName(userName);
        experiment.setName(experimentName);
        experiment.setDescription(expDescription);
        experiment.setApplicationId(applicationId);
        experiment.setExperimentInputs(experimentInputList);
        return experiment;
    }


    public static ComputationalResourceScheduling createComputationResourceScheduling(String resourceHostId,
                                                                                      int cpuCount,
                                                                                      int nodeCount,
                                                                                      int numberOfThreads,
                                                                                      String queueName,
                                                                                      int wallTimeLimit,
                                                                                      long jobstartTime,
                                                                                      int totalPhysicalMemory,
                                                                                      String projectAccount) {

        ComputationalResourceScheduling cmRS = new ComputationalResourceScheduling();
        cmRS.setResourceHostId(resourceHostId);
        cmRS.setTotalCPUCount(cpuCount);
        cmRS.setNodeCount(nodeCount);
        cmRS.setNumberOfThreads(numberOfThreads);
        cmRS.setQueueName(queueName);
        cmRS.setWallTimeLimit(wallTimeLimit);
        cmRS.setJobStartTime((int) jobstartTime);
        cmRS.setTotalPhysicalMemory(totalPhysicalMemory);
        cmRS.setComputationalProjectAccount(projectAccount);
        return cmRS;
    }

    public static AdvancedInputDataHandling createAdvancedInputHandling(boolean stageInputFilesToWorkingDir,
                                                                        String parentWorkingDir,
                                                                        String uniqueWorkingDir,
                                                                        boolean cleanupAfterJob) {
        AdvancedInputDataHandling inputDataHandling = new AdvancedInputDataHandling();
        inputDataHandling.setStageInputFilesToWorkingDir(stageInputFilesToWorkingDir);
        inputDataHandling.setParentWorkingDirectory(parentWorkingDir);
        inputDataHandling.setUniqueWorkingDirectory(uniqueWorkingDir);
        inputDataHandling.setCleanUpWorkingDirAfterJob(cleanupAfterJob);
        return inputDataHandling;
    }

    public static AdvancedOutputDataHandling createAdvancedOutputDataHandling(String outputDatadir,
                                                                      String dataRegUrl,
                                                                      boolean persistOutput) {
        AdvancedOutputDataHandling outputDataHandling = new AdvancedOutputDataHandling();
        outputDataHandling.setOutputDataDir(outputDatadir);
        outputDataHandling.setDataRegistryURL(dataRegUrl);
        outputDataHandling.setPersistOutputData(persistOutput);
        return outputDataHandling;
    }

    public static QualityOfServiceParams createQOSParams(String startExecutionAt,
                                                         String executeBefore,
                                                         int numberOfRetires) {
        QualityOfServiceParams qosParams = new QualityOfServiceParams();
        qosParams.setStartExecutionAt(startExecutionAt);
        qosParams.setExecuteBefore(executeBefore);
        qosParams.setNumberofRetries(numberOfRetires);
        return qosParams;
    }

    public static TaskDetails cloneTaskFromExperiment (Experiment experiment){
        TaskDetails taskDetails = new TaskDetails();
        taskDetails.setCreationTime(experiment.getCreationTime());
        taskDetails.setApplicationId(experiment.getApplicationId());
        taskDetails.setApplicationVersion(experiment.getApplicationVersion());
        taskDetails.setEnableEmailNotification(experiment.isEnableEmailNotification());
        List<String> emailAddresses = experiment.getEmailAddresses();
        if (emailAddresses != null && !emailAddresses.isEmpty()){
            taskDetails.setEmailAddresses(emailAddresses);
        }
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
        if (experimentInputs != null){
            taskDetails.setApplicationInputs(experimentInputs);
        }

        List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
        if (experimentOutputs != null){
            taskDetails.setApplicationOutputs(experimentOutputs);
        }

        UserConfigurationData configData = experiment.getUserConfigurationData();
        if (configData != null){
            ComputationalResourceScheduling scheduling = configData.getComputationalResourceScheduling();
            if (scheduling != null){
                taskDetails.setTaskScheduling(scheduling);
            }
            AdvancedInputDataHandling advanceInputDataHandling = configData.getAdvanceInputDataHandling();
            if (advanceInputDataHandling != null){
                taskDetails.setAdvancedInputDataHandling(advanceInputDataHandling);
            }
            AdvancedOutputDataHandling outputHandling = configData.getAdvanceOutputDataHandling();
            if (outputHandling != null){
                taskDetails.setAdvancedOutputDataHandling(outputHandling);
            }
        }
        return taskDetails;
    }

    public static TaskDetails cloneTaskFromWorkflowNodeDetails(Experiment experiment, WorkflowNodeDetails nodeDetails){
        TaskDetails taskDetails = new TaskDetails();
        taskDetails.setCreationTime(nodeDetails.getCreationTime());
//        String[] split = ;
        taskDetails.setApplicationId(nodeDetails.getExecutionUnitData());
//        taskDetails.setApplicationVersion(split[1]);
        taskDetails.setEnableEmailNotification(experiment.isEnableEmailNotification());
        List<String> emailAddresses = experiment.getEmailAddresses();
        if (emailAddresses != null && !emailAddresses.isEmpty()){
            taskDetails.setEmailAddresses(emailAddresses);
        }
        List<InputDataObjectType> experimentInputs = nodeDetails.getNodeInputs();
        if (experimentInputs != null){
            taskDetails.setApplicationInputs(experimentInputs);
        }

        List<OutputDataObjectType> experimentOutputs = nodeDetails.getNodeOutputs();
        if (experimentOutputs != null){
            taskDetails.setApplicationOutputs(experimentOutputs);
        }

        UserConfigurationData configData = experiment.getUserConfigurationData();
        if (configData != null){
            ComputationalResourceScheduling scheduling = configData.getComputationalResourceScheduling();
            if (scheduling != null){
                taskDetails.setTaskScheduling(scheduling);
            }
            AdvancedInputDataHandling advanceInputDataHandling = configData.getAdvanceInputDataHandling();
            if (advanceInputDataHandling != null){
                taskDetails.setAdvancedInputDataHandling(advanceInputDataHandling);
            }
            AdvancedOutputDataHandling outputHandling = configData.getAdvanceOutputDataHandling();
            if (outputHandling != null){
                taskDetails.setAdvancedOutputDataHandling(outputHandling);
            }
        }
        return taskDetails;
    }
    public static WorkflowNodeDetails createWorkflowNode (String nodeName,
                                                          List<InputDataObjectType> nodeInputs){
        WorkflowNodeDetails wfnod = new WorkflowNodeDetails();
        wfnod.setNodeName(nodeName);
        wfnod.setNodeInputs(nodeInputs);
        return wfnod;
    }
}
