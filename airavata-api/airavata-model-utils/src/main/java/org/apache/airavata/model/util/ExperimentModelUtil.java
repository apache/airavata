/**
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
 */
package org.apache.airavata.model.util;


import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;

import java.util.List;



public class ExperimentModelUtil {

	public static ExperimentModel createSimpleExperiment(String gatewayId,
	                                                     String projectID,
	                                                     String userName,
	                                                     String experimentName,
	                                                     String expDescription,
	                                                     String applicationId,
	                                                     List<InputDataObjectType> experimentInputList) {
		ExperimentModel experiment = new ExperimentModel();
		experiment.setGatewayId(gatewayId);
		experiment.setProjectId(projectID);
		experiment.setExperimentType(ExperimentType.SINGLE_APPLICATION);
		experiment.setUserName(userName);
		experiment.setExperimentName(experimentName);
		experiment.setDescription(expDescription);
		experiment.setExecutionId(applicationId);
		experiment.setExperimentInputs(experimentInputList);
		return experiment;
	}


    public static ComputationalResourceSchedulingModel createComputationResourceScheduling(String resourceHostId,
                                                                                      int cpuCount,
                                                                                      int nodeCount,
                                                                                      int numberOfThreads,
                                                                                      String queueName,
                                                                                      int wallTimeLimit,
                                                                                      int totalPhysicalMemory) {

        ComputationalResourceSchedulingModel cmRS = new ComputationalResourceSchedulingModel();
        cmRS.setResourceHostId(resourceHostId);
        cmRS.setTotalCPUCount(cpuCount);
        cmRS.setNodeCount(nodeCount);
        cmRS.setNumberOfThreads(numberOfThreads);
        cmRS.setQueueName(queueName);
        cmRS.setWallTimeLimit(wallTimeLimit);
        cmRS.setTotalPhysicalMemory(totalPhysicalMemory);
        return cmRS;
    }

    public static ProcessModel cloneProcessFromExperiment (ExperimentModel experiment){
        ProcessModel processModel = new ProcessModel();
        processModel.setCreationTime(experiment.getCreationTime());
        processModel.setExperimentId(experiment.getExperimentId());
        processModel.setApplicationInterfaceId(experiment.getExecutionId());
        processModel.setEnableEmailNotification(experiment.isEnableEmailNotification());
        List<String> emailAddresses = experiment.getEmailAddresses();
        if (emailAddresses != null && !emailAddresses.isEmpty()){
            processModel.setEmailAddresses(emailAddresses);
        }
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
        if (experimentInputs != null){
            processModel.setProcessInputs(experimentInputs);
        }

        List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
        if (experimentOutputs != null){
            processModel.setProcessOutputs(experimentOutputs);
        }

        UserConfigurationDataModel configData = experiment.getUserConfigurationData();
        if (configData != null){
            processModel.setStorageResourceId(configData.getStorageId());
            processModel.setExperimentDataDir(configData.getExperimentDataDir());
            processModel.setGenerateCert(configData.isGenerateCert());
            processModel.setUserDn(configData.getUserDN());
            ComputationalResourceSchedulingModel scheduling = configData.getComputationalResourceScheduling();
            if (scheduling != null){
                processModel.setProcessResourceSchedule(scheduling);
                processModel.setComputeResourceId(scheduling.getResourceHostId());
            }
            processModel.setUseUserCRPref(configData.isUseUserCRPref());
            processModel.setGroupResourceProfileId(configData.getGroupResourceProfileId());
        }
        processModel.setUserName(experiment.getUserName());
        return processModel;
    }
}
