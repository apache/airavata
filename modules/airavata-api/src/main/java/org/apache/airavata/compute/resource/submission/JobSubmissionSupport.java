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
package org.apache.airavata.compute.resource.submission;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.protocol.CommandOutput;
import org.apache.airavata.protocol.JobSubmissionOutput;
import org.apache.airavata.execution.scheduling.ComputeSubmissionTracker;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnParticipant
public class JobSubmissionSupport {

    private static final Logger logger = LoggerFactory.getLogger(JobSubmissionSupport.class);

    private final JobFactory jobFactory;
    private final ServerProperties serverProperties;
    private final StatusService statusService;

    public JobSubmissionSupport(
            JobFactory jobFactory,
            ServerProperties serverProperties,
            StatusService statusService) {
        this.jobFactory = jobFactory;
        this.serverProperties = serverProperties;
        this.statusService = statusService;
    }

    public JobSubmissionOutput submitBatchJob(
            AgentAdapter agentAdapter,
            JobSubmissionData jobSubmissionData,
            String workingDirectory,
            Resource computeResource,
            String processId,
            ComputeSubmissionTracker computeSubmissionTracker,
            String computeResourceId)
            throws Exception {

        var jobManagerConfiguration = jobFactory.getJobManagerConfiguration(computeResource);

        addMonitoringCommands(jobSubmissionData);

        var scriptAsString = jobSubmissionData.loadFromFile(jobManagerConfiguration.getJobDescriptionTemplateName());
        logger.info("Generated job submission script : " + scriptAsString);

        int number = new SecureRandom().nextInt();
        number = (number < 0 ? -number : number);
        var localDir = getLocalDataDir(processId);
        localDir.mkdirs();
        var tempJobFile =
                new File(localDir, "job_" + Integer.toString(number) + jobManagerConfiguration.getScriptExtension());

        Files.writeString(
                tempJobFile.toPath(), scriptAsString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.info("Job submission file for process " + processId + " was created at : "
                + tempJobFile.getAbsolutePath());

        logger.info("Copying file form " + tempJobFile.getAbsolutePath() + " to remote path " + workingDirectory
                + " of compute resource " + computeResourceId);
        agentAdapter.uploadFile(tempJobFile.getAbsolutePath(), workingDirectory);

        var submitCommand = jobManagerConfiguration.getSubmitCommand(workingDirectory, tempJobFile.getPath());

        logger.info("Submit command for process id " + processId + " : " + submitCommand.getRawCommand());
        logger.debug("Working directory for process id " + processId + " : " + workingDirectory);

        var commandOutput = submitCommandWithRecording(submitCommand, agentAdapter, jobSubmissionData, workingDirectory);
        logger.info("Job " + jobSubmissionData.getJobName() + " submitted to compute resource");
        logger.info("Submission stdout: " + commandOutput.getStdOut() + ", stderr: " + commandOutput.getStdError());

        var jsoutput = new JobSubmissionOutput();
        jsoutput.setDescription(scriptAsString);

        jsoutput.setJobId(jobManagerConfiguration.getParser().parseJobSubmission(commandOutput.getStdOut()));
        if (jsoutput.getJobId() == null) {
            if (jobManagerConfiguration.getParser().isJobSubmissionFailed(commandOutput.getStdOut())) {
                jsoutput.setJobSubmissionFailed(true);
                jsoutput.setFailureReason(
                        "stdout : " + commandOutput.getStdOut() + "\n stderr : " + commandOutput.getStdError());
            }
        }
        jsoutput.setExitCode(commandOutput.getExitCode());
        if (jsoutput.getExitCode() != 0) {
            jsoutput.setJobSubmissionFailed(true);
            jsoutput.setFailureReason(
                    "stdout : " + commandOutput.getStdOut() + "\n stderr : " + commandOutput.getStdError());
        }
        jsoutput.setStdOut(commandOutput.getStdOut());
        jsoutput.setStdErr(commandOutput.getStdError());

        if (computeSubmissionTracker != null && jsoutput.getJobId() != null && !jsoutput.isJobSubmissionFailed()) {
            if (computeResourceId != null) {
                computeSubmissionTracker.recordSubmission(computeResourceId);
                logger.debug("Recorded job submission for compute resource: {}", computeResourceId);
            }
        }

        return jsoutput;
    }

    public boolean cancelJob(AgentAdapter agentAdapter, String jobId, Resource computeResource)
            throws Exception {
        var jobManagerConfiguration = jobFactory.getJobManagerConfiguration(computeResource);
        var commandOutput = agentAdapter.executeCommand(
                jobManagerConfiguration.getCancelCommand(jobId).getRawCommand(), null);
        return commandOutput.getExitCode() == 0;
    }

    public StatusModel<JobState> getJobStatus(AgentAdapter agentAdapter, String jobId,
            Resource computeResource) throws Exception {
        var jobManagerConfiguration = jobFactory.getJobManagerConfiguration(computeResource);

        var monitorCommand = jobManagerConfiguration.getMonitorCommand(jobId);
        if (monitorCommand.isEmpty()) {
            logger.info("No monitor command available for job {}; cannot query status", jobId);
            return null;
        }

        var commandOutput = agentAdapter.executeCommand(monitorCommand.get().getRawCommand(), null);

        return jobManagerConfiguration.getParser().parseJobStatus(jobId, commandOutput.getStdOut());
    }

    public String getJobIdByJobName(AgentAdapter agentAdapter, String jobName, String userName,
            Resource computeResource) throws Exception {
        var jobManagerConfiguration = jobFactory.getJobManagerConfiguration(computeResource);

        var jobIdMonitorCommand = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
        if (jobIdMonitorCommand.isEmpty()) {
            logger.info("No job ID monitor command available for job name {}; cannot resolve job ID", jobName);
            return null;
        }
        var commandOutput =
                agentAdapter.executeCommand(jobIdMonitorCommand.get().getRawCommand(), null);
        return jobManagerConfiguration.getParser().parseJobId(jobName, commandOutput.getStdOut());
    }

    public File getLocalDataDir(String processId) {
        String outputPath = serverProperties.localDataLocation();
        outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
        return new File(outputPath + processId);
    }

    /**
     * Create a new {@link JobModel} pre-populated with the standard fields shared across
     * all job submission task implementations.
     */
    public JobModel createJobModel(String processId, String taskId, JobSubmissionData mapData) {
        var jobModel = new JobModel();
        jobModel.setProcessId(processId);
        jobModel.setWorkingDir(mapData.getWorkingDirectory());
        jobModel.setCreatedAt(IdGenerator.getCurrentTimestamp().toInstant());
        jobModel.setJobName(mapData.getJobName());
        return jobModel;
    }

    /**
     * Set the job status on the model and persist + publish it in one call.
     * This eliminates the repeated pattern of creating a StatusModel, wrapping it
     * in a list, setting it on the job model, then calling saveAndPublishJobStatus.
     */
    public void publishJobStatus(JobModel jobModel, JobState state, String reason) throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(state, reason);
        jobModel.setJobStatuses(List.of(jobStatus));
        saveAndPublishJobStatus(jobModel);
    }

    public void saveAndPublishJobStatus(JobModel jobModel) throws Exception {
        try {
            StatusModel<JobState> jobStatus;
            if (jobModel.getJobStatuses() != null && !jobModel.getJobStatuses().isEmpty()) {
                jobStatus = jobModel.getJobStatuses().get(0);
            } else {
                logger.error("Job statuses can not be empty");
                return;
            }

            jobStatus.setTimeOfStateChange(IdGenerator.getCurrentTimestamp().getTime());
            statusService.addJobStatus(jobStatus, jobModel.getJobId());
        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    public void addMonitoringCommands(JobSubmissionData mapData) {
        try {
            String url = serverProperties.services().monitor().compute().jobStatusCallbackUrl();
            if (serverProperties.services().monitor().realtime().enabled() && url != null && !url.isBlank()) {
                String payload = "'{\"jobName\":\"" + mapData.getJobName() + "\",\"status\":\"%s\",\"task\":\""
                        + mapData.getTaskId() + "\"}'";
                if (mapData.getPreJobCommands() == null) {
                    mapData.setPreJobCommands(new ArrayList<>());
                }
                mapData.getPreJobCommands()
                        .add(
                                0,
                                "curl -s -X POST -H \"Content-Type: application/json\" --data "
                                        + String.format(payload, "RUNNING") + " \"" + url
                                        + "\" > /dev/null || true");
                if (mapData.getPostJobCommands() == null) {
                    mapData.setPostJobCommands(new ArrayList<>());
                }
                mapData.getPostJobCommands()
                        .add("curl -s -X POST -H \"Content-Type: application/json\" --data "
                                + String.format(payload, "COMPLETED") + " \"" + url + "\" > /dev/null || true");
            }
        } catch (Exception e) {
            logger.warn("Could not get properties for monitoring commands", e);
        }
    }

    public JobManagerSpec getJobManagerConfiguration(Resource computeResource) throws Exception {
        return jobFactory.getJobManagerConfiguration(computeResource);
    }

    private CommandOutput submitCommandWithRecording(
            RawCommandInfo submitCommand,
            AgentAdapter agentAdapter,
            JobSubmissionData jobSubmissionData,
            String workingDirectory)
            throws AgentException {

        String modifiedCommand = submitCommand.getCommand() + " | tee " + getJobCommandRecordingFile(jobSubmissionData);
        logger.info("Modified the submit command to support recording : " + modifiedCommand);

        CommandOutput commandOutput = agentAdapter.executeCommand(modifiedCommand, workingDirectory);

        if (commandOutput.getStdOut() == null || "".equals(commandOutput.getStdOut())) {
            logger.warn("command submission returned empty response so reading recording file at "
                    + getJobCommandRecordingFile(jobSubmissionData));
            CommandOutput recordingFileReadCommandOutput = agentAdapter.executeCommand(
                    "cat " + getJobCommandRecordingFile(jobSubmissionData), jobSubmissionData.getWorkingDirectory());
            if (recordingFileReadCommandOutput.getStdOut() != null
                    && !"".equals(recordingFileReadCommandOutput.getStdOut())) {
                logger.info("Received non empty output form recording file : "
                        + recordingFileReadCommandOutput.getStdOut());
                return recordingFileReadCommandOutput;
            } else {
                return commandOutput;
            }
        } else {
            return commandOutput;
        }
    }

    private String getJobCommandRecordingFile(JobSubmissionData mapData) {
        return (mapData.getWorkingDirectory().endsWith(File.separator)
                        ? mapData.getWorkingDirectory()
                        : mapData.getWorkingDirectory() + File.separator)
                + mapData.getJobName();
    }
}
