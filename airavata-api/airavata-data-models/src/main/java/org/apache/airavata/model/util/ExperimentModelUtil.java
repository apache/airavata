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

import org.apache.airavata.model.experiment.*;

import java.util.Map;


public class ExperimentModelUtil {
    public static BasicMetadata createExperimentBasicMetadata (String experimentName,
                                                               String expDescription,
                                                               String userName,
                                                               String projectID,
                                                               boolean shareExp){
        BasicMetadata basicMetadata = new BasicMetadata();
        basicMetadata.setUserName(userName);
        basicMetadata.setExperimentDescription(expDescription);
        basicMetadata.setExperimentName(experimentName);
        basicMetadata.setProjectID(projectID);
        basicMetadata.setShareExperimentPublicly(shareExp);
        return basicMetadata;
    }

    public static  ConfigurationData createConfigData (String applicationId,
                                                       String applicationVersion,
                                                       String workflowId,
                                                       String workflowVersion,
                                                       BasicMetadata basicMetadata,
                                                       Map<String, String> experimentInputs,
                                                       ComputationalResourceScheduling resourceScheduling,
                                                       AdvancedInputDataHandling inputDataHandling,
                                                       AdvancedOutputDataHandling outputDataHandling,
                                                       QualityOfServiceParams qosParms){
        ConfigurationData configData = new ConfigurationData();
        configData.setApplicationId(applicationId);
        configData.setApplicationVersion(applicationVersion);
        configData.setWorkflowTemplateId(workflowId);
        configData.setWorklfowTemplateVersion(workflowVersion);
        configData.setBasicMetadata(basicMetadata);
        configData.setExperimentInputs(experimentInputs);
        configData.setComputationalResourceScheduling(resourceScheduling);
        configData.setAdvanceInputDataHandling(inputDataHandling);
        configData.setAdvanceOutputDataHandling(outputDataHandling);
        configData.setQosParams(qosParms);
        return configData;
    }

    public static ComputationalResourceScheduling createComputationResourceScheduling (boolean airavataAutoSchedule,
                                                       boolean overrideManualSchedulingParams,
                                                       String resourceHostId,
                                                       int cpuCount,
                                                       int nodeCount,
                                                       int numberOfThreads,
                                                       String queueName,
                                                       int wallTimeLimit,
                                                       long jobstartTime,
                                                       int totalPhysicalMemory,
                                                       String projectAccount){

        ComputationalResourceScheduling cmRS = new ComputationalResourceScheduling();
        cmRS.setAiravataAutoSchedule(airavataAutoSchedule);
        cmRS.setOverrideManualScheduledParams(overrideManualSchedulingParams);
        cmRS.setResourceHostId(resourceHostId);
        cmRS.setTotalCPUCount(cpuCount);
        cmRS.setNodeCount(nodeCount);
        cmRS.setNumberOfThreads(numberOfThreads);
        cmRS.setQueueName(queueName);
        cmRS.setWallTimeLimit(wallTimeLimit);
        cmRS.setJobStartTime((int)jobstartTime);
        cmRS.setTotalPhysicalMemory(totalPhysicalMemory);
        cmRS.setComputationalProjectAccount(projectAccount);
        return cmRS;
    }

    public static AdvancedInputDataHandling createAdvancedInputHandling (boolean stageInputFilesToWorkingDir,
                                                                         String workingDirParent,
                                                                         String uniqueWorkingDir,
                                                                         boolean cleanupAfterJob){
        AdvancedInputDataHandling inputDataHandling = new AdvancedInputDataHandling();
        inputDataHandling.setStageInputFilesToWorkingDir(stageInputFilesToWorkingDir);
        inputDataHandling.setWorkingDirectoryParent(workingDirParent);
        inputDataHandling.setUniqueWorkingDirectory(uniqueWorkingDir);
        inputDataHandling.setCleanUpWorkingDirAfterJob(cleanupAfterJob);
        return inputDataHandling;
    }

    public static AdvancedOutputDataHandling createOutputDataHandling (String outputDatadir,
                                                                       String dataRegUrl,
                                                                       boolean persistOutput){
        AdvancedOutputDataHandling outputDataHandling = new AdvancedOutputDataHandling();
        outputDataHandling.setOutputdataDir(outputDatadir);
        outputDataHandling.setDataRegistryURL(dataRegUrl);
        outputDataHandling.setPersistOutputData(persistOutput);
        return outputDataHandling;
    }

    public static QualityOfServiceParams createQOSParams (String startExecutionAt,
                                                          String executeBefore,
                                                          int numberOfRetires){
        QualityOfServiceParams qosParams = new QualityOfServiceParams();
        qosParams.setStartExecutionAt(startExecutionAt);
        qosParams.setExecuteBefore(executeBefore);
        qosParams.setNumberofRetries(numberOfRetires);
        return qosParams;
    }
}
