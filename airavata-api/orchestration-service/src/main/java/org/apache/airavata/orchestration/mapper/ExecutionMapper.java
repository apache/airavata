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
package org.apache.airavata.orchestration.mapper;

import org.apache.airavata.iam.model.GatewayEntity;
import org.apache.airavata.iam.model.GatewayUsageReportingCommandEntity;
import org.apache.airavata.iam.model.QueueStatusEntity;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.workflow.proto.AiravataWorkflow;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.apache.airavata.orchestration.model.*;
import org.apache.airavata.orchestration.workflow.AiravataWorkflowEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExecutionMapper extends CommonMapperConversions {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    // --- Process ---
    @Mapping(target = "emailAddressesList", ignore = true)
    ProcessModel processToModel(ProcessEntity entity);

    @Mapping(target = "emailAddresses", expression = "java(listToCsv(model.getEmailAddressesList()))")
    ProcessEntity processToEntity(ProcessModel model);

    // --- Task ---
    TaskModel taskToModel(TaskEntity entity);

    TaskEntity taskToEntity(TaskModel model);

    // --- Job ---
    JobModel jobToModel(JobEntity entity);

    JobEntity jobToEntity(JobModel model);

    // --- ProcessStatus ---
    ProcessStatus processStatusToModel(ProcessStatusEntity entity);

    ProcessStatusEntity processStatusToEntity(ProcessStatus model);

    // --- TaskStatus ---
    TaskStatus taskStatusToModel(TaskStatusEntity entity);

    TaskStatusEntity taskStatusToEntity(TaskStatus model);

    // --- JobStatus ---
    JobStatus jobStatusToModel(JobStatusEntity entity);

    JobStatusEntity jobStatusToEntity(JobStatus model);

    // --- ProcessError ---
    ErrorModel processErrorToModel(ProcessErrorEntity entity);

    ProcessErrorEntity processErrorToEntity(ErrorModel model);

    // --- TaskError ---
    ErrorModel taskErrorToModel(TaskErrorEntity entity);

    TaskErrorEntity taskErrorToEntity(ErrorModel model);

    // --- ProcessInput ---
    InputDataObjectType processInputToModel(ProcessInputEntity entity);

    ProcessInputEntity processInputToEntity(InputDataObjectType model);

    // --- ProcessOutput ---
    OutputDataObjectType processOutputToModel(ProcessOutputEntity entity);

    ProcessOutputEntity processOutputToEntity(OutputDataObjectType model);

    // --- ProcessWorkflow ---
    ProcessWorkflow processWorkflowToModel(ProcessWorkflowEntity entity);

    ProcessWorkflowEntity processWorkflowToEntity(ProcessWorkflow model);

    // --- QueueStatus ---
    QueueStatusModel queueStatusToModel(QueueStatusEntity entity);

    QueueStatusEntity queueStatusToEntity(QueueStatusModel model);

    // --- Gateway ---
    Gateway gatewayToModel(GatewayEntity entity);

    GatewayEntity gatewayToEntity(Gateway model);

    // --- GatewayUsageReportingCommand ---
    GatewayUsageReportingCommand gatewayUsageReportingCommandToModel(GatewayUsageReportingCommandEntity entity);

    GatewayUsageReportingCommandEntity gatewayUsageReportingCommandToEntity(GatewayUsageReportingCommand model);

    // --- AiravataWorkflow ---
    AiravataWorkflow workflowToModel(AiravataWorkflowEntity entity);

    AiravataWorkflowEntity workflowToEntity(AiravataWorkflow model);

    // --- UserConfigurationData ---

    /**
     * Custom mapping: UserConfigurationDataEntity -> UserConfigurationDataModel.
     * The entity flattens scheduling fields; the model nests them under computationalResourceScheduling.
     */
    default UserConfigurationDataModel userConfigDataToModel(UserConfigurationDataEntity entity) {
        if (entity == null) return null;

        ComputationalResourceSchedulingModel scheduling = ComputationalResourceSchedulingModel.newBuilder()
                .setResourceHostId(entity.getResourceHostId())
                .setTotalCpuCount(entity.getTotalCPUCount())
                .setNodeCount(entity.getNodeCount())
                .setNumberOfThreads(entity.getNumberOfThreads())
                .setQueueName(entity.getQueueName())
                .setWallTimeLimit(entity.getWallTimeLimit())
                .setTotalPhysicalMemory(entity.getTotalPhysicalMemory())
                .setStaticWorkingDir(entity.getStaticWorkingDir())
                .setOverrideLoginUserName(entity.getOverrideLoginUserName())
                .setOverrideScratchLocation(entity.getOverrideScratchLocation())
                .setOverrideAllocationProjectNumber(entity.getOverrideAllocationProjectNumber())
                .build();

        return UserConfigurationDataModel.newBuilder()
                .setAiravataAutoSchedule(entity.isAiravataAutoSchedule())
                .setOverrideManualScheduledParams(entity.isOverrideManualScheduledParams())
                .setShareExperimentPublicly(entity.isShareExperimentPublicly())
                .setThrottleResources(entity.isThrottleResources())
                .setUserDn(entity.getUserDN())
                .setGenerateCert(entity.isGenerateCert())
                .setExperimentDataDir(entity.getExperimentDataDir())
                .setGroupResourceProfileId(entity.getGroupResourceProfileId())
                .setUseUserCrPref(entity.isUseUserCRPref())
                .setComputationalResourceScheduling(scheduling)
                .setInputStorageResourceId(entity.getInputStorageResourceId())
                .setOutputStorageResourceId(entity.getOutputStorageResourceId())
                .build();
    }

    /**
     * Custom mapping: UserConfigurationDataModel -> UserConfigurationDataEntity.
     */
    default UserConfigurationDataEntity userConfigDataToEntity(UserConfigurationDataModel model) {
        if (model == null) return null;
        UserConfigurationDataEntity entity = new UserConfigurationDataEntity();
        entity.setAiravataAutoSchedule(model.getAiravataAutoSchedule());
        entity.setOverrideManualScheduledParams(model.getOverrideManualScheduledParams());
        entity.setShareExperimentPublicly(model.getShareExperimentPublicly());
        entity.setThrottleResources(model.getThrottleResources());
        entity.setUserDN(model.getUserDn());
        entity.setGenerateCert(model.getGenerateCert());
        entity.setExperimentDataDir(model.getExperimentDataDir());
        entity.setGroupResourceProfileId(model.getGroupResourceProfileId());
        entity.setUseUserCRPref(model.getUseUserCrPref());
        entity.setInputStorageResourceId(model.getInputStorageResourceId());
        entity.setOutputStorageResourceId(model.getOutputStorageResourceId());

        if (model.hasComputationalResourceScheduling()) {
            ComputationalResourceSchedulingModel scheduling = model.getComputationalResourceScheduling();
            entity.setResourceHostId(scheduling.getResourceHostId());
            entity.setTotalCPUCount(scheduling.getTotalCpuCount());
            entity.setNodeCount(scheduling.getNodeCount());
            entity.setNumberOfThreads(scheduling.getNumberOfThreads());
            entity.setQueueName(scheduling.getQueueName());
            entity.setWallTimeLimit(scheduling.getWallTimeLimit());
            entity.setTotalPhysicalMemory(scheduling.getTotalPhysicalMemory());
            entity.setStaticWorkingDir(scheduling.getStaticWorkingDir());
            entity.setOverrideLoginUserName(scheduling.getOverrideLoginUserName());
            entity.setOverrideScratchLocation(scheduling.getOverrideScratchLocation());
            entity.setOverrideAllocationProjectNumber(scheduling.getOverrideAllocationProjectNumber());
        }

        return entity;
    }
}
