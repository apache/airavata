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

import java.sql.Timestamp;
import org.apache.airavata.compute.model.QueueStatusEntity;
import org.apache.airavata.iam.model.GatewayEntity;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.status.proto.TaskState;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.orchestration.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExecutionMapper extends CommonMapperConversions {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    // --- Process (MapStruct abstract) ---
    @Mapping(target = "emailAddressesList", ignore = true)
    @Mapping(target = "processStatusesList", ignore = true)
    @Mapping(target = "processErrorsList", ignore = true)
    @Mapping(target = "processInputsList", ignore = true)
    @Mapping(target = "processOutputsList", ignore = true)
    ProcessModel processToModelBase(ProcessEntity entity);

    @Mapping(target = "emailAddresses", expression = "java(listToCsv(model.getEmailAddressesList()))")
    @Mapping(target = "processStatuses", ignore = true)
    @Mapping(target = "processErrors", ignore = true)
    @Mapping(target = "processInputs", ignore = true)
    @Mapping(target = "processOutputs", ignore = true)
    ProcessEntity processToEntityBase(ProcessModel model);

    // --- Task (MapStruct abstract) ---
    @Mapping(target = "taskStatusesList", ignore = true)
    @Mapping(target = "taskErrorsList", ignore = true)
    TaskModel taskToModelBase(TaskEntity entity);

    @Mapping(target = "taskStatuses", ignore = true)
    @Mapping(target = "taskErrors", ignore = true)
    TaskEntity taskToEntityBase(TaskModel model);

    // --- Job (MapStruct abstract) ---
    @Mapping(target = "jobStatusesList", ignore = true)
    JobModel jobToModelBase(JobEntity entity);

    @Mapping(target = "jobStatuses", ignore = true)
    JobEntity jobToEntityBase(JobModel model);

    // --- Process (default, handles child collections) ---
    default ProcessModel processToModel(ProcessEntity entity) {
        if (entity == null) return null;
        ProcessModel.Builder b = processToModelBase(entity).toBuilder();
        if (entity.getProcessStatuses() != null) {
            entity.getProcessStatuses().forEach(s -> b.addProcessStatuses(processStatusToModel(s)));
        }
        if (entity.getProcessErrors() != null) {
            entity.getProcessErrors().forEach(e -> b.addProcessErrors(processErrorToModel(e)));
        }
        if (entity.getProcessInputs() != null) {
            entity.getProcessInputs().forEach(i -> b.addProcessInputs(processInputToModel(i)));
        }
        if (entity.getProcessOutputs() != null) {
            entity.getProcessOutputs().forEach(o -> b.addProcessOutputs(processOutputToModel(o)));
        }
        return b.build();
    }

    default ProcessEntity processToEntity(ProcessModel model) {
        if (model == null) return null;
        ProcessEntity entity = processToEntityBase(model);
        if (!model.getProcessStatusesList().isEmpty()) {
            entity.setProcessStatuses(new java.util.ArrayList<>());
            model.getProcessStatusesList()
                    .forEach(s -> entity.getProcessStatuses().add(processStatusToEntity(s)));
        }
        if (!model.getProcessErrorsList().isEmpty()) {
            entity.setProcessErrors(new java.util.ArrayList<>());
            model.getProcessErrorsList().forEach(e -> entity.getProcessErrors().add(processErrorToEntity(e)));
        }
        if (!model.getProcessInputsList().isEmpty()) {
            entity.setProcessInputs(new java.util.ArrayList<>());
            model.getProcessInputsList().forEach(i -> entity.getProcessInputs().add(processInputToEntity(i)));
        }
        if (!model.getProcessOutputsList().isEmpty()) {
            entity.setProcessOutputs(new java.util.ArrayList<>());
            model.getProcessOutputsList()
                    .forEach(o -> entity.getProcessOutputs().add(processOutputToEntity(o)));
        }
        return entity;
    }

    // --- Task (default, handles child collections) ---
    default TaskModel taskToModel(TaskEntity entity) {
        if (entity == null) return null;
        TaskModel.Builder b = taskToModelBase(entity).toBuilder();
        if (entity.getTaskStatuses() != null) {
            entity.getTaskStatuses().forEach(s -> b.addTaskStatuses(taskStatusToModel(s)));
        }
        if (entity.getTaskErrors() != null) {
            entity.getTaskErrors().forEach(e -> b.addTaskErrors(taskErrorToModel(e)));
        }
        return b.build();
    }

    default TaskEntity taskToEntity(TaskModel model) {
        if (model == null) return null;
        TaskEntity entity = taskToEntityBase(model);
        if (!model.getTaskStatusesList().isEmpty()) {
            entity.setTaskStatuses(new java.util.ArrayList<>());
            model.getTaskStatusesList().forEach(s -> entity.getTaskStatuses().add(taskStatusToEntity(s)));
        }
        if (!model.getTaskErrorsList().isEmpty()) {
            entity.setTaskErrors(new java.util.ArrayList<>());
            model.getTaskErrorsList().forEach(e -> entity.getTaskErrors().add(taskErrorToEntity(e)));
        }
        return entity;
    }

    // --- Job (default, handles child collections) ---
    default JobModel jobToModel(JobEntity entity) {
        if (entity == null) return null;
        JobModel.Builder b = jobToModelBase(entity).toBuilder();
        if (entity.getJobStatuses() != null) {
            entity.getJobStatuses().forEach(s -> b.addJobStatuses(jobStatusToModel(s)));
        }
        return b.build();
    }

    default JobEntity jobToEntity(JobModel model) {
        if (model == null) return null;
        JobEntity entity = jobToEntityBase(model);
        if (!model.getJobStatusesList().isEmpty()) {
            entity.setJobStatuses(new java.util.ArrayList<>());
            model.getJobStatusesList().forEach(s -> entity.getJobStatuses().add(jobStatusToEntity(s)));
        }
        return entity;
    }

    // --- ProcessStatus ---
    default ProcessStatus processStatusToModel(ExecStatusEntity entity) {
        if (entity == null) return null;
        ProcessStatus.Builder b = ProcessStatus.newBuilder();
        if (entity.getStatusId() != null) b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setState(ProcessState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null) b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity processStatusToEntity(ProcessStatus model) {
        if (model == null) return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.getStatusId().isEmpty()) e.setStatusId(model.getStatusId());
        if (model.getState() != ProcessState.PROCESS_STATE_UNKNOWN)
            e.setState(model.getState().name());
        if (model.getTimeOfStateChange() != 0) e.setTimeOfStateChange(new Timestamp(model.getTimeOfStateChange()));
        if (!model.getReason().isEmpty()) e.setReason(model.getReason());
        return e;
    }

    // --- TaskStatus ---
    default TaskStatus taskStatusToModel(ExecStatusEntity entity) {
        if (entity == null) return null;
        TaskStatus.Builder b = TaskStatus.newBuilder();
        if (entity.getStatusId() != null) b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setState(TaskState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null) b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity taskStatusToEntity(TaskStatus model) {
        if (model == null) return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.getStatusId().isEmpty()) e.setStatusId(model.getStatusId());
        if (model.getState() != TaskState.TASK_STATE_UNKNOWN)
            e.setState(model.getState().name());
        if (model.getTimeOfStateChange() != 0) e.setTimeOfStateChange(new Timestamp(model.getTimeOfStateChange()));
        if (!model.getReason().isEmpty()) e.setReason(model.getReason());
        return e;
    }

    // --- JobStatus ---
    default JobStatus jobStatusToModel(ExecStatusEntity entity) {
        if (entity == null) return null;
        JobStatus.Builder b = JobStatus.newBuilder();
        if (entity.getStatusId() != null) b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setJobState(JobState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
                b.setJobState(JobState.JOB_STATE_UNKNOWN);
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null) b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity jobStatusToEntity(JobStatus model) {
        if (model == null) return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.getStatusId().isEmpty()) e.setStatusId(model.getStatusId());
        if (model.getJobState() != JobState.JOB_STATE_UNKNOWN)
            e.setState(model.getJobState().name());
        if (model.getTimeOfStateChange() != 0) e.setTimeOfStateChange(new Timestamp(model.getTimeOfStateChange()));
        if (!model.getReason().isEmpty()) e.setReason(model.getReason());
        return e;
    }

    // --- ProcessError / TaskError ---
    default ErrorModel processErrorToModel(ExecErrorEntity entity) {
        return execErrorToModel(entity);
    }

    default ExecErrorEntity processErrorToEntity(ErrorModel model) {
        return errorModelToExecError(model);
    }

    default ErrorModel taskErrorToModel(ExecErrorEntity entity) {
        return execErrorToModel(entity);
    }

    default ExecErrorEntity taskErrorToEntity(ErrorModel model) {
        return errorModelToExecError(model);
    }

    default ErrorModel execErrorToModel(ExecErrorEntity entity) {
        if (entity == null) return null;
        ErrorModel.Builder b = ErrorModel.newBuilder();
        if (entity.getErrorId() != null) b.setErrorId(entity.getErrorId());
        if (entity.getActualErrorMessage() != null) b.setActualErrorMessage(entity.getActualErrorMessage());
        if (entity.getUserFriendlyMessage() != null) b.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        b.setTransientOrPersistent(entity.isTransientOrPersistent());
        if (entity.getCreationTime() != null)
            b.setCreationTime(entity.getCreationTime().getTime());
        if (entity.getRootCauseErrorIdList() != null
                && !entity.getRootCauseErrorIdList().isEmpty()) {
            for (String id : entity.getRootCauseErrorIdList().split(",")) {
                if (!id.isEmpty()) b.addRootCauseErrorIdList(id.trim());
            }
        }
        return b.build();
    }

    default ExecErrorEntity errorModelToExecError(ErrorModel model) {
        if (model == null) return null;
        ExecErrorEntity e = new ExecErrorEntity();
        if (!model.getErrorId().isEmpty()) e.setErrorId(model.getErrorId());
        if (!model.getActualErrorMessage().isEmpty()) e.setActualErrorMessage(model.getActualErrorMessage());
        if (!model.getUserFriendlyMessage().isEmpty()) e.setUserFriendlyMessage(model.getUserFriendlyMessage());
        e.setTransientOrPersistent(model.getTransientOrPersistent());
        if (model.getCreationTime() != 0) e.setCreationTime(new Timestamp(model.getCreationTime()));
        if (!model.getRootCauseErrorIdListList().isEmpty()) {
            e.setRootCauseErrorIdList(String.join(",", model.getRootCauseErrorIdListList()));
        }
        return e;
    }

    // --- ProcessInput ---
    default InputDataObjectType processInputToModel(ExecIoParamEntity entity) {
        if (entity == null) return null;
        InputDataObjectType.Builder b = InputDataObjectType.newBuilder();
        if (entity.getName() != null) b.setName(entity.getName());
        if (entity.getValue() != null) b.setValue(entity.getValue());
        if (entity.getType() != null) b.setType(entity.getType());
        if (entity.getApplicationArgument() != null) b.setApplicationArgument(entity.getApplicationArgument());
        b.setIsRequired(entity.isIsRequired());
        b.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        b.setStandardInput(entity.isStandardInput());
        if (entity.getUserFriendlyDescription() != null)
            b.setUserFriendlyDescription(entity.getUserFriendlyDescription());
        if (entity.getMetaData() != null) b.setMetaData(entity.getMetaData());
        b.setInputOrder(entity.getInputOrder());
        b.setDataStaged(entity.isDataStaged());
        b.setIsReadOnly(entity.isReadOnly());
        if (entity.getOverrideFilename() != null) b.setOverrideFilename(entity.getOverrideFilename());
        if (entity.getStorageResourceId() != null) b.setStorageResourceId(entity.getStorageResourceId());
        return b.build();
    }

    default ExecIoParamEntity processInputToEntity(InputDataObjectType model) {
        if (model == null) return null;
        ExecIoParamEntity e = new ExecIoParamEntity();
        e.setDirection("INPUT");
        if (!model.getName().isEmpty()) e.setName(model.getName());
        if (!model.getValue().isEmpty()) e.setValue(model.getValue());
        e.setType(model.getType());
        if (!model.getApplicationArgument().isEmpty()) e.setApplicationArgument(model.getApplicationArgument());
        e.setIsRequired(model.getIsRequired());
        e.setRequiredToAddedToCommandLine(model.getRequiredToAddedToCommandLine());
        e.setStandardInput(model.getStandardInput());
        if (!model.getUserFriendlyDescription().isEmpty())
            e.setUserFriendlyDescription(model.getUserFriendlyDescription());
        if (!model.getMetaData().isEmpty()) e.setMetaData(model.getMetaData());
        e.setInputOrder(model.getInputOrder());
        e.setDataStaged(model.getDataStaged());
        e.setReadOnly(model.getIsReadOnly());
        if (!model.getOverrideFilename().isEmpty()) e.setOverrideFilename(model.getOverrideFilename());
        if (!model.getStorageResourceId().isEmpty()) e.setStorageResourceId(model.getStorageResourceId());
        return e;
    }

    // --- ProcessOutput ---
    default OutputDataObjectType processOutputToModel(ExecIoParamEntity entity) {
        if (entity == null) return null;
        OutputDataObjectType.Builder b = OutputDataObjectType.newBuilder();
        if (entity.getName() != null) b.setName(entity.getName());
        if (entity.getValue() != null) b.setValue(entity.getValue());
        if (entity.getType() != null) b.setType(entity.getType());
        if (entity.getApplicationArgument() != null) b.setApplicationArgument(entity.getApplicationArgument());
        b.setIsRequired(entity.isIsRequired());
        b.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        b.setDataMovement(entity.isDataMovement());
        if (entity.getLocation() != null) b.setLocation(entity.getLocation());
        if (entity.getSearchQuery() != null) b.setSearchQuery(entity.getSearchQuery());
        b.setOutputStreaming(entity.isOutputStreaming());
        if (entity.getStorageResourceId() != null) b.setStorageResourceId(entity.getStorageResourceId());
        if (entity.getMetaData() != null) b.setMetaData(entity.getMetaData());
        return b.build();
    }

    default ExecIoParamEntity processOutputToEntity(OutputDataObjectType model) {
        if (model == null) return null;
        ExecIoParamEntity e = new ExecIoParamEntity();
        e.setDirection("OUTPUT");
        if (!model.getName().isEmpty()) e.setName(model.getName());
        if (!model.getValue().isEmpty()) e.setValue(model.getValue());
        e.setType(model.getType());
        if (!model.getApplicationArgument().isEmpty()) e.setApplicationArgument(model.getApplicationArgument());
        e.setIsRequired(model.getIsRequired());
        e.setRequiredToAddedToCommandLine(model.getRequiredToAddedToCommandLine());
        e.setDataMovement(model.getDataMovement());
        if (!model.getLocation().isEmpty()) e.setLocation(model.getLocation());
        if (!model.getSearchQuery().isEmpty()) e.setSearchQuery(model.getSearchQuery());
        e.setOutputStreaming(model.getOutputStreaming());
        if (!model.getStorageResourceId().isEmpty()) e.setStorageResourceId(model.getStorageResourceId());
        if (!model.getMetaData().isEmpty()) e.setMetaData(model.getMetaData());
        return e;
    }

    // --- ProcessWorkflow ---
    ProcessWorkflow processWorkflowToModel(ProcessWorkflowEntity entity);

    ProcessWorkflowEntity processWorkflowToEntity(ProcessWorkflow model);

    // --- QueueStatus ---
    QueueStatusModel queueStatusToModel(QueueStatusEntity entity);

    QueueStatusEntity queueStatusToEntity(QueueStatusModel model);

    // --- Gateway ---
    Gateway gatewayToModel(GatewayEntity entity);

    GatewayEntity gatewayToEntity(Gateway model);

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
