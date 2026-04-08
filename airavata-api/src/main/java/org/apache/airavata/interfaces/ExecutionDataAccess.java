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
package org.apache.airavata.interfaces;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;

/**
 * Abstraction over orchestration-service repositories for use by research-service.
 * All methods use proto model types (not JPA entities) so research-service
 * does not need to depend on orchestration-service's concrete repository classes.
 */
public interface ExecutionDataAccess {

    // --- Process ---
    String addProcess(ProcessModel process, String experimentId) throws RegistryException;

    ProcessModel getProcess(String processId) throws RegistryException;

    void updateProcess(ProcessModel process, String processId) throws RegistryException;

    List<ProcessModel> getProcessList(String fieldName, Object value) throws RegistryException;

    List<String> getProcessIds(String fieldName, Object value) throws RegistryException;

    void addProcessResourceSchedule(ComputationalResourceSchedulingModel scheduling, String processId)
            throws RegistryException;

    Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes);

    // --- Process Status ---
    void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException;

    void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException;

    ProcessStatus getProcessStatus(String processId) throws RegistryException;

    List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException;

    List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit) throws RegistryException;

    // --- Process Error ---
    void addProcessError(ErrorModel processError, String processId) throws RegistryException;

    // --- Process Output ---
    void addProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException;

    List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException;

    // --- Process Workflow ---
    void addProcessWorkflow(ProcessWorkflow processWorkflow, String processId) throws RegistryException;

    List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException;

    // --- Task ---
    String addTask(TaskModel task, String processId) throws RegistryException;

    void deleteTasks(String processId) throws RegistryException;

    // --- Task Status ---
    void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryException;

    // --- Task Error ---
    void addTaskError(ErrorModel taskError, String taskId) throws RegistryException;

    // --- Job ---
    void addJob(JobModel job, String processId) throws RegistryException;

    List<JobModel> getJobList(String fieldName, Object value) throws RegistryException;

    void removeJob(JobModel job) throws RegistryException;

    // --- Job Status ---
    void addJobStatus(JobStatus jobStatus, String jobId, String taskId) throws RegistryException;

    List<JobStatus> getDistinctListofJobStatus(String gatewayId, String status, double time);

    // --- UserConfigurationData ---
    UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException;

    void saveUserConfigurationData(UserConfigurationDataModel ucdModel, String experimentId) throws RegistryException;

    // --- Processes for Experiment ---
    List<ProcessModel> getProcessesForExperiment(String experimentId) throws RegistryException;
}
