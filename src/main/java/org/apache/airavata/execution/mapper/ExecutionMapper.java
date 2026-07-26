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
import org.apache.airavata.common.CommonMapperConversions;
import org.apache.airavata.models.experiment.UserConfigurationDataModel;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;
import org.apache.airavata.models.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.models.commons.ErrorModel;
import org.apache.airavata.models.job.JobModel;
import org.apache.airavata.models.process.ProcessModel;
import org.apache.airavata.models.status.JobState;
import org.apache.airavata.models.status.JobStatus;
import org.apache.airavata.models.status.ProcessState;
import org.apache.airavata.models.status.ProcessStatus;
import org.apache.airavata.models.status.TaskState;
import org.apache.airavata.models.status.TaskStatus;
import org.apache.airavata.models.task.TaskModel;
import org.apache.airavata.execution.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExecutionMapper extends CommonMapperConversions {

    ExecutionMapper INSTANCE = Mappers.getMapper(ExecutionMapper.class);

    // --- Process (MapStruct abstract) ---
    @Mapping(target = "emailAddresses", ignore = true)
    @Mapping(target = "processStatuses", ignore = true)
    @Mapping(target = "processErrors", ignore = true)
    @Mapping(target = "processInputs", ignore = true)
    @Mapping(target = "processOutputs", ignore = true)
    ProcessModel processToModelBase(ProcessEntity entity);

    @Mapping(target = "emailAddresses", expression = "java(listToCsv(model.emailAddresses()))")
    @Mapping(target = "processStatuses", ignore = true)
    @Mapping(target = "processErrors", ignore = true)
    @Mapping(target = "processInputs", ignore = true)
    @Mapping(target = "processOutputs", ignore = true)
    ProcessEntity processToEntityBase(ProcessModel model);

    // --- Task (MapStruct abstract) ---
    @Mapping(target = "taskStatuses", ignore = true)
    @Mapping(target = "taskErrors", ignore = true)
    TaskModel taskToModelBase(TaskEntity entity);

    @Mapping(target = "taskStatuses", ignore = true)
    @Mapping(target = "taskErrors", ignore = true)
    TaskEntity taskToEntityBase(TaskModel model);

    // --- Job (MapStruct abstract) ---
    @Mapping(target = "jobStatuses", ignore = true)
    JobModel jobToModelBase(JobEntity entity);

    @Mapping(target = "jobStatuses", ignore = true)
    JobEntity jobToEntityBase(JobModel model);

    // --- Process (default, handles child collections) ---
    default ProcessModel processToModel(ProcessEntity entity) {
        if (entity == null)
            return null;
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
        if (entity.getEmailAddresses() != null) {
            java.util.List<String> emails = csvToList(entity.getEmailAddresses());
            if (emails != null)
                b.addAllEmailAddresses(emails);
        }
        if (entity.getTasks() != null) {
            entity.getTasks().forEach(t -> b.addTasks(taskToModel(t)));
        }
        return b.build();
    }

    default ProcessEntity processToEntity(ProcessModel model) {
        if (model == null)
            return null;
        ProcessEntity entity = processToEntityBase(model);
        if (!model.processStatuses().isEmpty()) {
            entity.setProcessStatuses(new java.util.ArrayList<>());
            model.processStatuses().forEach(s -> entity.getProcessStatuses().add(processStatusToEntity(s)));
        }
        if (!model.processErrors().isEmpty()) {
            entity.setProcessErrors(new java.util.ArrayList<>());
            model.processErrors().forEach(e -> entity.getProcessErrors().add(processErrorToEntity(e)));
        }
        if (!model.processInputs().isEmpty()) {
            entity.setProcessInputs(new java.util.ArrayList<>());
            model.processInputs().forEach(i -> entity.getProcessInputs().add(processInputToEntity(i)));
        }
        if (!model.processOutputs().isEmpty()) {
            entity.setProcessOutputs(new java.util.ArrayList<>());
            model.processOutputs().forEach(o -> entity.getProcessOutputs().add(processOutputToEntity(o)));
        }
        if (!model.tasks().isEmpty()) {
            entity.setTasks(new java.util.ArrayList<>());
            model.tasks().forEach(t -> entity.getTasks().add(taskToEntity(t)));
        }
        return entity;
    }

    // --- Task (default, handles child collections) ---
    default TaskModel taskToModel(TaskEntity entity) {
        if (entity == null)
            return null;
        TaskModel.Builder b = taskToModelBase(entity).toBuilder();
        if (entity.getTaskStatuses() != null) {
            entity.getTaskStatuses().forEach(s -> b.addTaskStatuses(taskStatusToModel(s)));
        }
        if (entity.getTaskErrors() != null) {
            entity.getTaskErrors().forEach(e -> b.addTaskErrors(taskErrorToModel(e)));
        }
        if (entity.getJobs() != null) {
            entity.getJobs().forEach(j -> b.addJobs(jobToModel(j)));
        }
        return b.build();
    }

    default TaskEntity taskToEntity(TaskModel model) {
        if (model == null)
            return null;
        TaskEntity entity = taskToEntityBase(model);
        if (!model.taskStatuses().isEmpty()) {
            entity.setTaskStatuses(new java.util.ArrayList<>());
            model.taskStatuses().forEach(s -> entity.getTaskStatuses().add(taskStatusToEntity(s)));
        }
        if (!model.taskErrors().isEmpty()) {
            entity.setTaskErrors(new java.util.ArrayList<>());
            model.taskErrors().forEach(e -> entity.getTaskErrors().add(taskErrorToEntity(e)));
        }
        if (!model.jobs().isEmpty()) {
            entity.setJobs(new java.util.ArrayList<>());
            model.jobs().forEach(j -> entity.getJobs().add(jobToEntity(j)));
        }
        return entity;
    }

    // --- Job (default, handles child collections) ---
    default JobModel jobToModel(JobEntity entity) {
        if (entity == null)
            return null;
        JobModel.Builder b = jobToModelBase(entity).toBuilder();
        if (entity.getJobStatuses() != null) {
            entity.getJobStatuses().forEach(s -> b.addJobStatuses(jobStatusToModel(s)));
        }
        return b.build();
    }

    default JobEntity jobToEntity(JobModel model) {
        if (model == null)
            return null;
        JobEntity entity = jobToEntityBase(model);
        if (!model.jobStatuses().isEmpty()) {
            entity.setJobStatuses(new java.util.ArrayList<>());
            model.jobStatuses().forEach(s -> entity.getJobStatuses().add(jobStatusToEntity(s)));
        }
        return entity;
    }

    // --- ProcessStatus ---
    default ProcessStatus processStatusToModel(ExecStatusEntity entity) {
        if (entity == null)
            return null;
        ProcessStatus.Builder b = ProcessStatus.newBuilder();
        if (entity.getStatusId() != null)
            b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setState(ProcessState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null)
            b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity processStatusToEntity(ProcessStatus model) {
        if (model == null)
            return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.statusId().isEmpty())
            e.setStatusId(model.statusId());
        if (model.state() != ProcessState.PROCESS_STATE_UNKNOWN)
            e.setState(model.state().name());
        if (model.timeOfStateChange() != 0)
            e.setTimeOfStateChange(new Timestamp(model.timeOfStateChange()));
        if (!model.reason().isEmpty())
            e.setReason(model.reason());
        return e;
    }

    // --- TaskStatus ---
    default TaskStatus taskStatusToModel(ExecStatusEntity entity) {
        if (entity == null)
            return null;
        TaskStatus.Builder b = TaskStatus.newBuilder();
        if (entity.getStatusId() != null)
            b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setState(TaskState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null)
            b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity taskStatusToEntity(TaskStatus model) {
        if (model == null)
            return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.statusId().isEmpty())
            e.setStatusId(model.statusId());
        if (model.state() != TaskState.TASK_STATE_UNKNOWN)
            e.setState(model.state().name());
        if (model.timeOfStateChange() != 0)
            e.setTimeOfStateChange(new Timestamp(model.timeOfStateChange()));
        if (!model.reason().isEmpty())
            e.setReason(model.reason());
        return e;
    }

    // --- JobStatus ---
    default JobStatus jobStatusToModel(ExecStatusEntity entity) {
        if (entity == null)
            return null;
        JobStatus.Builder b = JobStatus.newBuilder();
        if (entity.getStatusId() != null)
            b.setStatusId(entity.getStatusId());
        if (entity.getState() != null) {
            try {
                b.setJobState(JobState.valueOf(entity.getState()));
            } catch (IllegalArgumentException ignored) {
                b.setJobState(JobState.JOB_STATE_UNKNOWN);
            }
        }
        if (entity.getTimeOfStateChange() != null)
            b.setTimeOfStateChange(entity.getTimeOfStateChange().getTime());
        if (entity.getReason() != null)
            b.setReason(entity.getReason());
        return b.build();
    }

    default ExecStatusEntity jobStatusToEntity(JobStatus model) {
        if (model == null)
            return null;
        ExecStatusEntity e = new ExecStatusEntity();
        if (!model.statusId().isEmpty())
            e.setStatusId(model.statusId());
        if (model.jobState() != JobState.JOB_STATE_UNKNOWN)
            e.setState(model.jobState().name());
        if (model.timeOfStateChange() != 0)
            e.setTimeOfStateChange(new Timestamp(model.timeOfStateChange()));
        if (!model.reason().isEmpty())
            e.setReason(model.reason());
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
        if (entity == null)
            return null;
        ErrorModel.Builder b = ErrorModel.newBuilder();
        if (entity.getErrorId() != null)
            b.setErrorId(entity.getErrorId());
        if (entity.getActualErrorMessage() != null)
            b.setActualErrorMessage(entity.getActualErrorMessage());
        if (entity.getUserFriendlyMessage() != null)
            b.setUserFriendlyMessage(entity.getUserFriendlyMessage());
        b.setTransientOrPersistent(entity.isTransientOrPersistent());
        if (entity.getCreationTime() != null)
            b.setCreationTime(entity.getCreationTime().getTime());
        if (entity.getRootCauseErrorIdList() != null
                && !entity.getRootCauseErrorIdList().isEmpty()) {
            for (String id : entity.getRootCauseErrorIdList().split(",")) {
                if (!id.isEmpty())
                    b.addRootCauseErrorIdList(id.trim());
            }
        }
        return b.build();
    }

    default ExecErrorEntity errorModelToExecError(ErrorModel model) {
        if (model == null)
            return null;
        ExecErrorEntity e = new ExecErrorEntity();
        if (!model.errorId().isEmpty())
            e.setErrorId(model.errorId());
        if (!model.actualErrorMessage().isEmpty())
            e.setActualErrorMessage(model.actualErrorMessage());
        if (!model.userFriendlyMessage().isEmpty())
            e.setUserFriendlyMessage(model.userFriendlyMessage());
        e.setTransientOrPersistent(model.transientOrPersistent());
        if (model.creationTime() != 0)
            e.setCreationTime(new Timestamp(model.creationTime()));
        if (!model.rootCauseErrorIdList().isEmpty()) {
            e.setRootCauseErrorIdList(String.join(",", model.rootCauseErrorIdList()));
        }
        return e;
    }

    // --- ProcessInput ---
    default InputDataObjectType processInputToModel(ExecIoParamEntity entity) {
        if (entity == null)
            return null;
        InputDataObjectType.Builder b = InputDataObjectType.newBuilder();
        if (entity.getName() != null)
            b.setName(entity.getName());
        if (entity.getValue() != null)
            b.setValue(entity.getValue());
        if (entity.getType() != null)
            b.setType(entity.getType());
        if (entity.getApplicationArgument() != null)
            b.setApplicationArgument(entity.getApplicationArgument());
        b.setIsRequired(entity.isIsRequired());
        b.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        b.setStandardInput(entity.isStandardInput());
        if (entity.getUserFriendlyDescription() != null)
            b.setUserFriendlyDescription(entity.getUserFriendlyDescription());
        if (entity.getMetaData() != null)
            b.setMetaData(entity.getMetaData());
        b.setInputOrder(entity.getInputOrder());
        b.setDataStaged(entity.isDataStaged());
        b.setIsReadOnly(entity.isReadOnly());
        if (entity.getOverrideFilename() != null)
            b.setOverrideFilename(entity.getOverrideFilename());
        if (entity.getStorageResourceId() != null)
            b.setStorageResourceId(entity.getStorageResourceId());
        return b.build();
    }

    default ExecIoParamEntity processInputToEntity(InputDataObjectType model) {
        if (model == null)
            return null;
        ExecIoParamEntity e = new ExecIoParamEntity();
        e.setDirection("INPUT");
        if (!model.name().isEmpty())
            e.setName(model.name());
        if (!model.value().isEmpty())
            e.setValue(model.value());
        e.setType(model.type());
        if (!model.applicationArgument().isEmpty())
            e.setApplicationArgument(model.applicationArgument());
        e.setIsRequired(model.isRequired());
        e.setRequiredToAddedToCommandLine(model.requiredToAddedToCommandLine());
        e.setStandardInput(model.standardInput());
        if (!model.userFriendlyDescription().isEmpty())
            e.setUserFriendlyDescription(model.userFriendlyDescription());
        if (!model.metaData().isEmpty())
            e.setMetaData(model.metaData());
        e.setInputOrder(model.inputOrder());
        e.setDataStaged(model.dataStaged());
        e.setReadOnly(model.isReadOnly());
        if (!model.overrideFilename().isEmpty())
            e.setOverrideFilename(model.overrideFilename());
        if (!model.storageResourceId().isEmpty())
            e.setStorageResourceId(model.storageResourceId());
        return e;
    }

    // --- ProcessOutput ---
    default OutputDataObjectType processOutputToModel(ExecIoParamEntity entity) {
        if (entity == null)
            return null;
        OutputDataObjectType.Builder b = OutputDataObjectType.newBuilder();
        if (entity.getName() != null)
            b.setName(entity.getName());
        if (entity.getValue() != null)
            b.setValue(entity.getValue());
        if (entity.getType() != null)
            b.setType(entity.getType());
        if (entity.getApplicationArgument() != null)
            b.setApplicationArgument(entity.getApplicationArgument());
        b.setIsRequired(entity.isIsRequired());
        b.setRequiredToAddedToCommandLine(entity.isRequiredToAddedToCommandLine());
        b.setDataMovement(entity.isDataMovement());
        if (entity.getLocation() != null)
            b.setLocation(entity.getLocation());
        if (entity.getSearchQuery() != null)
            b.setSearchQuery(entity.getSearchQuery());
        b.setOutputStreaming(entity.isOutputStreaming());
        if (entity.getStorageResourceId() != null)
            b.setStorageResourceId(entity.getStorageResourceId());
        if (entity.getMetaData() != null)
            b.setMetaData(entity.getMetaData());
        return b.build();
    }

    default ExecIoParamEntity processOutputToEntity(OutputDataObjectType model) {
        if (model == null)
            return null;
        ExecIoParamEntity e = new ExecIoParamEntity();
        e.setDirection("OUTPUT");
        if (!model.name().isEmpty())
            e.setName(model.name());
        if (!model.value().isEmpty())
            e.setValue(model.value());
        e.setType(model.type());
        if (!model.applicationArgument().isEmpty())
            e.setApplicationArgument(model.applicationArgument());
        e.setIsRequired(model.isRequired());
        e.setRequiredToAddedToCommandLine(model.requiredToAddedToCommandLine());
        e.setDataMovement(model.dataMovement());
        if (!model.location().isEmpty())
            e.setLocation(model.location());
        if (!model.searchQuery().isEmpty())
            e.setSearchQuery(model.searchQuery());
        e.setOutputStreaming(model.outputStreaming());
        if (!model.storageResourceId().isEmpty())
            e.setStorageResourceId(model.storageResourceId());
        if (!model.metaData().isEmpty())
            e.setMetaData(model.metaData());
        return e;
    }

    // --- UserConfigurationData ---

    /**
     * Custom mapping: UserConfigurationDataEntity -> UserConfigurationDataModel.
     * The entity flattens scheduling fields; the model nests them under
     * computationalResourceScheduling.
     */
    default UserConfigurationDataModel userConfigDataToModel(UserConfigurationDataEntity entity) {
        if (entity == null)
            return null;

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
        if (model == null)
            return null;
        UserConfigurationDataEntity entity = new UserConfigurationDataEntity();
        entity.setAiravataAutoSchedule(model.airavataAutoSchedule());
        entity.setOverrideManualScheduledParams(model.overrideManualScheduledParams());
        entity.setShareExperimentPublicly(model.shareExperimentPublicly());
        entity.setThrottleResources(model.throttleResources());
        entity.setExperimentDataDir(model.experimentDataDir());
        entity.setGroupResourceProfileId(model.groupResourceProfileId());
        entity.setUseUserCRPref(model.useUserCrPref());
        entity.setInputStorageResourceId(model.inputStorageResourceId());
        entity.setOutputStorageResourceId(model.outputStorageResourceId());

        if (model.hasComputationalResourceScheduling()) {
            ComputationalResourceSchedulingModel scheduling = model.computationalResourceScheduling();
            entity.setResourceHostId(scheduling.resourceHostId());
            entity.setTotalCPUCount(scheduling.totalCpuCount());
            entity.setNodeCount(scheduling.nodeCount());
            entity.setNumberOfThreads(scheduling.numberOfThreads());
            entity.setQueueName(scheduling.queueName());
            entity.setWallTimeLimit(scheduling.wallTimeLimit());
            entity.setTotalPhysicalMemory(scheduling.totalPhysicalMemory());
            entity.setStaticWorkingDir(scheduling.staticWorkingDir());
            entity.setOverrideLoginUserName(scheduling.overrideLoginUserName());
            entity.setOverrideScratchLocation(scheduling.overrideScratchLocation());
            entity.setOverrideAllocationProjectNumber(scheduling.overrideAllocationProjectNumber());
        }

        return entity;
    }
}
