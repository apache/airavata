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
package org.apache.airavata.orchestration.util;

import java.util.List;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;

public class ExperimentModelUtil {

    public static ExperimentModel createSimpleExperiment(
            String gatewayId,
            String projectID,
            String userName,
            String experimentName,
            String expDescription,
            String applicationId,
            List<InputDataObjectType> experimentInputList) {
        return ExperimentModel.newBuilder()
                .setGatewayId(gatewayId)
                .setProjectId(projectID)
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .setUserName(userName)
                .setExperimentName(experimentName)
                .setDescription(expDescription)
                .setExecutionId(applicationId)
                .addAllExperimentInputs(experimentInputList)
                .build();
    }

    public static ComputationalResourceSchedulingModel createComputationResourceScheduling(
            String resourceHostId,
            int cpuCount,
            int nodeCount,
            int numberOfThreads,
            String queueName,
            int wallTimeLimit,
            int totalPhysicalMemory) {

        return ComputationalResourceSchedulingModel.newBuilder()
                .setResourceHostId(resourceHostId)
                .setTotalCpuCount(cpuCount)
                .setNodeCount(nodeCount)
                .setNumberOfThreads(numberOfThreads)
                .setQueueName(queueName)
                .setWallTimeLimit(wallTimeLimit)
                .setTotalPhysicalMemory(totalPhysicalMemory)
                .build();
    }

    public static ProcessModel cloneProcessFromExperiment(ExperimentModel experiment) {
        ProcessModel.Builder builder = ProcessModel.newBuilder();
        builder.setCreationTime(experiment.getCreationTime());
        builder.setExperimentId(experiment.getExperimentId());
        builder.setApplicationInterfaceId(experiment.getExecutionId());
        builder.setEnableEmailNotification(experiment.getEnableEmailNotification());
        List<String> emailAddresses = experiment.getEmailAddressesList();
        if (!emailAddresses.isEmpty()) {
            builder.addAllEmailAddresses(emailAddresses);
        }
        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputsList();
        if (!experimentInputs.isEmpty()) {
            builder.addAllProcessInputs(experimentInputs);
        }

        List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputsList();
        if (!experimentOutputs.isEmpty()) {
            builder.addAllProcessOutputs(experimentOutputs);
        }

        UserConfigurationDataModel configData = experiment.getUserConfigurationData();
        if (experiment.hasUserConfigurationData()) {
            builder.setInputStorageResourceId(configData.getInputStorageResourceId());
            builder.setOutputStorageResourceId(configData.getOutputStorageResourceId());
            builder.setExperimentDataDir(configData.getExperimentDataDir());
            builder.setGenerateCert(configData.getGenerateCert());
            builder.setUserDn(configData.getUserDn());
            ComputationalResourceSchedulingModel scheduling = configData.getComputationalResourceScheduling();
            if (configData.hasComputationalResourceScheduling()) {
                builder.setProcessResourceSchedule(scheduling);
                builder.setComputeResourceId(scheduling.getResourceHostId());
            }
            builder.setUseUserCrPref(configData.getUseUserCrPref());
            builder.setGroupResourceProfileId(configData.getGroupResourceProfileId());
        }
        builder.setUserName(experiment.getUserName());
        return builder.build();
    }
}
