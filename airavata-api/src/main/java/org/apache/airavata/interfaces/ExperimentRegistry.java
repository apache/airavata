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
import org.apache.airavata.model.experiment.proto.*;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.status.proto.TaskStatus;
import org.apache.airavata.model.task.proto.TaskModel;

/**
 * Registry operations for experiment lifecycle, processes, jobs, tasks, and errors.
 */
public interface ExperimentRegistry {

    // --- Experiment operations ---
    ExperimentModel getExperiment(String airavataExperimentId) throws Exception;

    ExperimentStatus getExperimentStatus(String airavataExperimentId) throws Exception;

    void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws Exception;

    void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws Exception;

    void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id) throws Exception;

    String createExperiment(String gatewayId, ExperimentModel experiment) throws Exception;

    boolean deleteExperiment(String experimentId) throws Exception;

    ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws Exception;

    List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws Exception;

    List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset) throws Exception;

    List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws Exception;

    List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws Exception;

    ExperimentStatistics getExperimentStatistics(
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> accessibleExpIds,
            int limit,
            int offset)
            throws Exception;

    List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception;

    void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws Exception;

    void updateResourceScheduleing(String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws Exception;

    UserConfigurationDataModel getUserConfigurationData(String experimentId) throws Exception;

    // --- Process operations ---
    String addProcess(ProcessModel processModel, String experimentId) throws Exception;

    ProcessModel getProcess(String processId) throws Exception;

    List<ProcessModel> getProcessList(String experimentId) throws Exception;

    List<ProcessModel> getProcessListInState(ProcessState processState) throws Exception;

    List<String> getProcessIds(String experimentId) throws Exception;

    ProcessStatus getProcessStatus(String processId) throws Exception;

    List<ProcessStatus> getProcessStatusList(String processId) throws Exception;

    void updateProcess(ProcessModel processModel, String processId) throws Exception;

    void addProcessStatus(ProcessStatus processStatus, String processId) throws Exception;

    void updateProcessStatus(ProcessStatus processStatus, String processId) throws Exception;

    void addProcessWorkflow(ProcessWorkflow processWorkflow) throws Exception;

    List<OutputDataObjectType> getProcessOutputs(String processId) throws Exception;

    List<ProcessWorkflow> getProcessWorkflows(String processId) throws Exception;

    // --- Task operations ---
    String addTask(TaskModel taskModel, String processId) throws Exception;

    void addTaskStatus(TaskStatus taskStatus, String taskId) throws Exception;

    void deleteTasks(String processId) throws Exception;

    // --- Job operations ---
    void addJob(JobModel jobModel, String processId) throws Exception;

    List<JobModel> getJobs(String queryType, String id) throws Exception;

    void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws Exception;

    void deleteJobs(String processId) throws Exception;

    int getJobCount(JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes) throws Exception;

    boolean isJobExist(String queryType, String id) throws Exception;

    JobModel getJob(String queryType, String id) throws Exception;

    Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws Exception;

    List<JobModel> getJobDetails(String airavataExperimentId) throws Exception;

    // --- Error operations ---
    void addErrors(String errorType, ErrorModel errorModel, String id) throws Exception;

    // --- Stats operations ---
    Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes) throws Exception;
}
