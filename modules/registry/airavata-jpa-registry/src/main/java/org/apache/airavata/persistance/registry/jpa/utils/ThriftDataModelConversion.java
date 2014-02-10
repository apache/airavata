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

import org.apache.airavata.model.experiment.*;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentConfigDataResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentInputResource;
import org.apache.airavata.persistance.registry.jpa.resources.ExperimentMetadataResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThriftDataModelConversion {
    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static BasicMetadata getBasicMetadata (ExperimentMetadataResource exmetadata){
        BasicMetadata bsmd = new BasicMetadata();
        bsmd.setUserName(exmetadata.getExecutionUser());
        bsmd.setShareExperimentPublicly(exmetadata.isShareExp());
        bsmd.setExperimentDescription(exmetadata.getDescription());
        bsmd.setExperimentName(exmetadata.getExperimentName());
        return bsmd;
    }

    public static ConfigurationData getConfigurationData (ExperimentConfigDataResource excd){
        ConfigurationData configData = new ConfigurationData();
        configData.setBasicMetadata(getBasicMetadata(excd.getExMetadata()));
        configData.setApplicationId(excd.getApplicationID());
        configData.setApplicationVersion(excd.getApplicationVersion());
        configData.setWorkflowTemplateId(excd.getWorkflowTemplateId());
        configData.setWorklfowTemplateVersion(excd.getWorkflowTemplateVersion());
        configData.setExperimentInputs(getExperimentInputs(excd.getExMetadata()));
        configData.setAdvanceInputDataHandling(getAdvanceInputDataHandling(excd));
        configData.setAdvanceOutputDataHandling(getAdvanceOutputDataHandling(excd));
        configData.setComputationalResourceScheduling(getComputationalResourceScheduling(excd));
        configData.setQosParams(getQOSParams(excd));
        return configData;
    }

    public static Map<String, String> getExperimentInputs (ExperimentMetadataResource exmdr){
        List<Resource> resources = exmdr.get(ResourceType.EXPERIMENT_INPUT);
        Map<String, String> exInputs = new HashMap<String, String>();
        for (Resource resource : resources){
            ExperimentInputResource exInput = (ExperimentInputResource)resource;
            exInputs.put(exInput.getExperimentKey(), exInput.getValue());
        }
        return exInputs;
    }

    public static ComputationalResourceScheduling getComputationalResourceScheduling (ExperimentConfigDataResource excdr){
        ComputationalResourceScheduling scheduling = new ComputationalResourceScheduling();
        scheduling.setAiravataAutoSchedule(excdr.isAiravataAutoSchedule());
        scheduling.setOverrideManualScheduledParams(excdr.isOverrideManualSchedule());
        scheduling.setResourceHostId(excdr.getResourceHostID());
        scheduling.setTotalCPUCount(excdr.getCpuCount());
        scheduling.setNodeCount(excdr.getNodeCount());
        scheduling.setNumberOfThreads(excdr.getNumberOfThreads());
        scheduling.setQueueName(excdr.getQueueName());
        scheduling.setWallTimeLimit(excdr.getWallTimeLimit());
        scheduling.setJobStartTime((int)excdr.getJobStartTime().getTime());
        scheduling.setTotalPhysicalMemory(excdr.getPhysicalMemory());
        scheduling.setComputationalProjectAccount(excdr.getProjectAccount());
        return scheduling;
    }

    public static AdvancedInputDataHandling getAdvanceInputDataHandling(ExperimentConfigDataResource excd){
        AdvancedInputDataHandling adih = new AdvancedInputDataHandling();
        adih.setStageInputFilesToWorkingDir(excd.isStageInputsToWDir());
        adih.setWorkingDirectoryParent(excd.getWorkingDirParent());
        adih.setUniqueWorkingDirectory(excd.getWorkingDir());
        adih.setCleanUpWorkingDirAfterJob(excd.isCleanAfterJob());
        return adih;
    }

    public static AdvancedOutputDataHandling getAdvanceOutputDataHandling(ExperimentConfigDataResource excd){
        AdvancedOutputDataHandling outputDataHandling = new AdvancedOutputDataHandling();
        outputDataHandling.setOutputdataDir(excd.getOutputDataDir());
        outputDataHandling.setDataRegistryURL(excd.getDataRegURL());
        outputDataHandling.setPersistOutputData(excd.isPersistOutputData());
        return outputDataHandling;
    }

    public static QualityOfServiceParams getQOSParams (ExperimentConfigDataResource excd){
        QualityOfServiceParams qosParams = new QualityOfServiceParams();
        qosParams.setStartExecutionAt(excd.getStartExecutionAt());
        qosParams.setExecuteBefore(excd.getExecuteBefore());
        qosParams.setNumberofRetries(excd.getNumberOfRetries());
        return qosParams;
    }
}
