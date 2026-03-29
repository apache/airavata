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
package org.apache.airavata.execution.mapper;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.execution.model.*;
import org.apache.airavata.execution.model.workflow.AiravataWorkflowEntity;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workflow.AiravataWorkflow;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExecutionMapper {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    // --- Experiment ---
    @Mapping(target = "emailAddresses", source = "emailAddresses")
    ExperimentModel experimentToModel(ExperimentEntity entity);

    @Mapping(target = "emailAddresses", source = "emailAddresses")
    ExperimentEntity experimentToEntity(ExperimentModel model);

    // --- ExperimentSummary ---
    ExperimentSummaryModel experimentSummaryToModel(ExperimentSummaryEntity entity);

    ExperimentSummaryEntity experimentSummaryToEntity(ExperimentSummaryModel model);

    // --- Process ---
    @Mapping(target = "emailAddresses", source = "emailAddresses")
    ProcessModel processToModel(ProcessEntity entity);

    @Mapping(target = "emailAddresses", source = "emailAddresses")
    ProcessEntity processToEntity(ProcessModel model);

    // --- Task ---
    TaskModel taskToModel(TaskEntity entity);

    TaskEntity taskToEntity(TaskModel model);

    // --- Job ---
    JobModel jobToModel(JobEntity entity);

    JobEntity jobToEntity(JobModel model);

    // --- ExperimentStatus ---
    ExperimentStatus experimentStatusToModel(ExperimentStatusEntity entity);

    ExperimentStatusEntity experimentStatusToEntity(ExperimentStatus model);

    // --- ProcessStatus ---
    ProcessStatus processStatusToModel(ProcessStatusEntity entity);

    ProcessStatusEntity processStatusToEntity(ProcessStatus model);

    // --- TaskStatus ---
    TaskStatus taskStatusToModel(TaskStatusEntity entity);

    TaskStatusEntity taskStatusToEntity(TaskStatus model);

    // --- JobStatus ---
    JobStatus jobStatusToModel(JobStatusEntity entity);

    JobStatusEntity jobStatusToEntity(JobStatus model);

    // --- ExperimentError ---
    ErrorModel experimentErrorToModel(ExperimentErrorEntity entity);

    ExperimentErrorEntity experimentErrorToEntity(ErrorModel model);

    // --- ProcessError ---
    ErrorModel processErrorToModel(ProcessErrorEntity entity);

    ProcessErrorEntity processErrorToEntity(ErrorModel model);

    // --- TaskError ---
    ErrorModel taskErrorToModel(TaskErrorEntity entity);

    TaskErrorEntity taskErrorToEntity(ErrorModel model);

    // --- ExperimentInput ---
    InputDataObjectType experimentInputToModel(ExperimentInputEntity entity);

    ExperimentInputEntity experimentInputToEntity(InputDataObjectType model);

    // --- ProcessInput ---
    InputDataObjectType processInputToModel(ProcessInputEntity entity);

    ProcessInputEntity processInputToEntity(InputDataObjectType model);

    // --- ExperimentOutput ---
    OutputDataObjectType experimentOutputToModel(ExperimentOutputEntity entity);

    ExperimentOutputEntity experimentOutputToEntity(OutputDataObjectType model);

    // --- ProcessOutput ---
    OutputDataObjectType processOutputToModel(ProcessOutputEntity entity);

    ProcessOutputEntity processOutputToEntity(OutputDataObjectType model);

    // --- ProcessWorkflow ---
    ProcessWorkflow processWorkflowToModel(ProcessWorkflowEntity entity);

    ProcessWorkflowEntity processWorkflowToEntity(ProcessWorkflow model);

    // --- Project ---
    Project projectToModel(ProjectEntity entity);

    ProjectEntity projectToEntity(Project model);

    // --- QueueStatus ---
    QueueStatusModel queueStatusToModel(QueueStatusEntity entity);

    QueueStatusEntity queueStatusToEntity(QueueStatusModel model);

    // --- AiravataWorkflow ---
    AiravataWorkflow workflowToModel(AiravataWorkflowEntity entity);

    AiravataWorkflowEntity workflowToEntity(AiravataWorkflow model);

    // --- Gateway ---
    Gateway gatewayToModel(GatewayEntity entity);

    GatewayEntity gatewayToEntity(Gateway model);

    // --- Notification ---
    Notification notificationToModel(NotificationEntity entity);

    NotificationEntity notificationToEntity(Notification model);

    // --- UserProfile (execution.UserEntity) ---
    UserProfile userToModel(UserEntity entity);

    UserEntity userToEntity(UserProfile model);

    // --- GatewayUsageReportingCommand ---
    GatewayUsageReportingCommand gatewayUsageReportingCommandToModel(GatewayUsageReportingCommandEntity entity);

    GatewayUsageReportingCommandEntity gatewayUsageReportingCommandToEntity(GatewayUsageReportingCommand model);

    // --- Custom converter replacements ---

    /**
     * Custom mapping: UserConfigurationDataEntity -> UserConfigurationDataModel.
     * The entity flattens scheduling fields; the model nests them under computationalResourceScheduling.
     * This replaces the Dozer mapping: {@code <field><a>this</a><b>computationalResourceScheduling</b></field>}
     */
    default UserConfigurationDataModel userConfigDataToModel(UserConfigurationDataEntity entity) {
        if (entity == null) return null;
        UserConfigurationDataModel model = new UserConfigurationDataModel();
        model.setAiravataAutoSchedule(entity.isAiravataAutoSchedule());
        model.setOverrideManualScheduledParams(entity.isOverrideManualScheduledParams());
        model.setShareExperimentPublicly(entity.isShareExperimentPublicly());
        model.setThrottleResources(entity.isThrottleResources());
        model.setUserDN(entity.getUserDN());
        model.setGenerateCert(entity.isGenerateCert());
        model.setExperimentDataDir(entity.getExperimentDataDir());
        model.setGroupResourceProfileId(entity.getGroupResourceProfileId());
        model.setUseUserCRPref(entity.isUseUserCRPref());

        // Map flattened scheduling fields into nested ComputationalResourceSchedulingModel
        ComputationalResourceSchedulingModel scheduling = new ComputationalResourceSchedulingModel();
        scheduling.setResourceHostId(entity.getResourceHostId());
        scheduling.setTotalCPUCount(entity.getTotalCPUCount());
        scheduling.setNodeCount(entity.getNodeCount());
        scheduling.setNumberOfThreads(entity.getNumberOfThreads());
        scheduling.setQueueName(entity.getQueueName());
        scheduling.setWallTimeLimit(entity.getWallTimeLimit());
        scheduling.setTotalPhysicalMemory(entity.getTotalPhysicalMemory());
        scheduling.setStaticWorkingDir(entity.getStaticWorkingDir());
        scheduling.setOverrideLoginUserName(entity.getOverrideLoginUserName());
        scheduling.setOverrideScratchLocation(entity.getOverrideScratchLocation());
        scheduling.setOverrideAllocationProjectNumber(entity.getOverrideAllocationProjectNumber());
        model.setComputationalResourceScheduling(scheduling);

        model.setInputStorageResourceId(entity.getInputStorageResourceId());
        model.setOutputStorageResourceId(entity.getOutputStorageResourceId());

        return model;
    }

    /**
     * Custom mapping: UserConfigurationDataModel -> UserConfigurationDataEntity.
     */
    default UserConfigurationDataEntity userConfigDataToEntity(UserConfigurationDataModel model) {
        if (model == null) return null;
        UserConfigurationDataEntity entity = new UserConfigurationDataEntity();
        entity.setAiravataAutoSchedule(model.isAiravataAutoSchedule());
        entity.setOverrideManualScheduledParams(model.isOverrideManualScheduledParams());
        entity.setShareExperimentPublicly(model.isShareExperimentPublicly());
        entity.setThrottleResources(model.isThrottleResources());
        entity.setUserDN(model.getUserDN());
        entity.setGenerateCert(model.isGenerateCert());
        entity.setExperimentDataDir(model.getExperimentDataDir());
        entity.setGroupResourceProfileId(model.getGroupResourceProfileId());
        entity.setUseUserCRPref(model.isUseUserCRPref());
        entity.setInputStorageResourceId(model.getInputStorageResourceId());
        entity.setOutputStorageResourceId(model.getOutputStorageResourceId());

        // Extract scheduling fields from nested object
        ComputationalResourceSchedulingModel scheduling = model.getComputationalResourceScheduling();
        if (scheduling != null) {
            entity.setResourceHostId(scheduling.getResourceHostId());
            entity.setTotalCPUCount(scheduling.getTotalCPUCount());
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

    /** boolean to int (Thrift boolean fields stored as int in some entities) */
    default int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    /** int to boolean */
    default boolean intToBoolean(int value) {
        return value != 0;
    }

    /** CSV String to List<String> (replaces CsvStringConverter) */
    default List<String> csvToList(String csv) {
        if (csv == null || csv.isEmpty()) {
            return null;
        }
        return Arrays.asList(csv.split(","));
    }

    /** List<String> to CSV String (replaces CsvStringConverter) */
    default String listToCsv(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    /** Long millis to Timestamp (replaces StorageDateConverter) */
    default Timestamp longToTimestamp(long millis) {
        return millis == 0 ? null : new Timestamp(millis);
    }

    /** Timestamp to long millis (replaces StorageDateConverter) */
    default long timestampToLong(Timestamp ts) {
        return ts == null ? 0 : ts.getTime();
    }
}
