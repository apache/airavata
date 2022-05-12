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
 */
package org.apache.airavata.helix.impl.task.submission;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.JobSubmissionOutput;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.submission.config.GroovyMapData;
import org.apache.airavata.helix.impl.task.submission.config.JobFactory;
import org.apache.airavata.helix.impl.task.submission.config.JobManagerConfiguration;
import org.apache.airavata.helix.impl.task.submission.config.RawCommandInfo;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobStatus;
import org.apache.commons.io.FileUtils;
import org.apache.helix.HelixManager;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

public abstract class JobSubmissionTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(JobSubmissionTask.class);

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
    }


    @SuppressWarnings("WeakerAccess")
    protected JobSubmissionOutput submitBatchJob(AgentAdaptor agentAdaptor, GroovyMapData groovyMapData, String workingDirectory) throws Exception {
        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                getRegistryServiceClient(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));

        addMonitoringCommands(groovyMapData);

        String scriptAsString = groovyMapData.loadFromFile(jobManagerConfiguration.getJobDescriptionTemplateName());
        logger.info("Generated job submission script : " + scriptAsString);

        int number = new SecureRandom().nextInt();
        number = (number < 0 ? -number : number);
        File tempJobFile = new File(getLocalDataDir(), "job_" + Integer.toString(number) + jobManagerConfiguration.getScriptExtension());

        FileUtils.writeStringToFile(tempJobFile, scriptAsString);
        logger.info("Job submission file for process " + getProcessId() + " was created at : " + tempJobFile.getAbsolutePath());

        logger.info("Copying file form " + tempJobFile.getAbsolutePath() + " to remote path " + workingDirectory +
                " of compute resource " + getTaskContext().getComputeResourceId());
        agentAdaptor.uploadFile(tempJobFile.getAbsolutePath(), workingDirectory);

        RawCommandInfo submitCommand = jobManagerConfiguration.getSubmitCommand(workingDirectory, tempJobFile.getPath());

        logger.info("Submit command for process id " + getProcessId() + " : " + submitCommand.getRawCommand());
        logger.debug("Working directory for process id " + getProcessId() + " : " + workingDirectory);

        CommandOutput commandOutput = submitCommandWithRecording(submitCommand, agentAdaptor, groovyMapData, workingDirectory);
        logger.info("Job " + groovyMapData.getJobName() + " submitted to compute resource");
        logger.info("Submission stdout: " + commandOutput.getStdOut() + ", stderr: " + commandOutput.getStdError());

        JobSubmissionOutput jsoutput = new JobSubmissionOutput();
        jsoutput.setDescription(scriptAsString);

        jsoutput.setJobId(jobManagerConfiguration.getParser().parseJobSubmission(commandOutput.getStdOut()));
        if (jsoutput.getJobId() == null) {
            if (jobManagerConfiguration.getParser().isJobSubmissionFailed(commandOutput.getStdOut())) {
                jsoutput.setJobSubmissionFailed(true);
                jsoutput.setFailureReason("stdout : " + commandOutput.getStdOut() +
                        "\n stderr : " + commandOutput.getStdError());
            }
        }
        jsoutput.setExitCode(commandOutput.getExitCode());
        if (jsoutput.getExitCode() != 0) {
            jsoutput.setJobSubmissionFailed(true);
            jsoutput.setFailureReason("stdout : " + commandOutput.getStdOut() +
                    "\n stderr : " + commandOutput.getStdError());
        }
        jsoutput.setStdOut(commandOutput.getStdOut());
        jsoutput.setStdErr(commandOutput.getStdError());
        return jsoutput;
    }

    /**
     * This will write the standard output of the command to a file inside the working directory of the process and
     * if the agent does not receive the output through first invocation, it retries by looking into the output file.
     *
     * @param submitCommand command to submit
     * @param agentAdaptor agent adaptor to communicate with compute resource
     * @param groovyMapData metadata object of the job
     * @param workingDirectory working directory for the process
     * @return {@link CommandOutput} of the submitted command
     * @throws AgentException if agent failed to communicate with the compute host
     */
    private CommandOutput submitCommandWithRecording(RawCommandInfo submitCommand, AgentAdaptor agentAdaptor,
                                                     GroovyMapData groovyMapData, String workingDirectory) throws AgentException {

        String modifiedCommand =  submitCommand.getCommand() + " | tee " + getJobCommandRecordingFile(groovyMapData);
        logger.info("Modified the submit command to support recording : " + modifiedCommand);

        CommandOutput commandOutput = agentAdaptor.executeCommand(modifiedCommand, workingDirectory);

        if (commandOutput.getStdOut() == null || "".equals(commandOutput.getStdOut())) {
            logger.warn("command submission returned empty response so reading recording file at " + getJobCommandRecordingFile(groovyMapData));
            CommandOutput recordingFileReadCommandOutput = agentAdaptor.executeCommand("cat " + getJobCommandRecordingFile(groovyMapData),
                    groovyMapData.getWorkingDirectory());
            if (recordingFileReadCommandOutput.getStdOut() != null && !"".equals(recordingFileReadCommandOutput.getStdOut())) {
                logger.info("Received non empty output form recording file : " + recordingFileReadCommandOutput.getStdOut());
                return recordingFileReadCommandOutput;
            } else {
                return commandOutput;
            }
        } else {
            return commandOutput;
        }
    }

    private String getJobCommandRecordingFile(GroovyMapData mapData) {
        return (mapData.getWorkingDirectory().endsWith(File.separator) ?
                mapData.getWorkingDirectory() : mapData.getWorkingDirectory() + File.separator) +
                mapData.getJobName();
    }

    @SuppressWarnings("WeakerAccess")
    public File getLocalDataDir() {
        String outputPath = ServerSettings.getLocalDataLocation();
        outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
        return new File(outputPath + getProcessId());
    }

    @SuppressWarnings("WeakerAccess")
    public boolean cancelJob(AgentAdaptor agentAdaptor, String jobId) throws Exception {
        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                getRegistryServiceClient(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));
        CommandOutput commandOutput = agentAdaptor.executeCommand(jobManagerConfiguration.getCancelCommand(jobId).getRawCommand(), null);
        return commandOutput.getExitCode() == 0;
    }

    @SuppressWarnings("WeakerAccess")
    public JobStatus getJobStatus(AgentAdaptor agentAdaptor, String jobId) throws Exception {

        ResourceJobManager resourceJobManager = JobFactory.getResourceJobManager(
                getRegistryServiceClient(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface());

        if (resourceJobManager == null) {
            throw new Exception("Resource job manager can not be null for protocol " + getTaskContext().getJobSubmissionProtocol() + " and job id " + jobId);
        }

        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(resourceJobManager);

        CommandOutput commandOutput = agentAdaptor.executeCommand(jobManagerConfiguration.getMonitorCommand(jobId).getRawCommand(), null);

        return jobManagerConfiguration.getParser().parseJobStatus(jobId, commandOutput.getStdOut());

    }

    @SuppressWarnings("WeakerAccess")
    public String getJobIdByJobName(AgentAdaptor agentAdaptor, String jobName, String userName) throws Exception {

        ResourceJobManager resourceJobManager = JobFactory.getResourceJobManager(
                getRegistryServiceClient(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface());

        if (resourceJobManager == null) {
            throw new Exception("Resource job manager can not be null for protocol " + getTaskContext().getJobSubmissionProtocol()
                    + " and job name " + jobName + " and user " + userName);
        }

        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(resourceJobManager);

        RawCommandInfo jobIdMonitorCommand = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
        CommandOutput commandOutput = agentAdaptor.executeCommand(jobIdMonitorCommand.getRawCommand(), null);
        return jobManagerConfiguration.getParser().parseJobId(jobName, commandOutput.getStdOut());
    }

    @SuppressWarnings("WeakerAccess")
    public void saveJobModel(JobModel jobModel) throws TException {
        getRegistryServiceClient().addJob(jobModel, getProcessId());
    }

    @SuppressWarnings("WeakerAccess")
    public void saveAndPublishJobStatus(JobModel jobModel) throws Exception {
        try {
            // first we save job jobModel to the registry for sa and then save the job status.
            JobStatus jobStatus;
            if (jobModel.getJobStatuses() != null && jobModel.getJobStatuses().size() > 0) {
                jobStatus = jobModel.getJobStatuses().get(0);
            } else {
                logger.error("Job statuses can not be empty");
                return;
            }

            List<JobStatus> statuses = new ArrayList<>();
            statuses.add(jobStatus);
            jobModel.setJobStatuses(statuses);

            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0 ) {
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            } else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }

            getRegistryServiceClient().addJobStatus(jobStatus, jobModel.getTaskId(), jobModel.getJobId());
            /*JobIdentifier identifier = new JobIdentifier(jobModel.getJobId(), jobModel.getTaskId(),
                    getProcessId(), getProcessModel().getExperimentId(), getGatewayId());

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
                    (MessageType.JOB.name()), getGatewayId());
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);*/
        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    private void addMonitoringCommands(GroovyMapData mapData) throws ApplicationSettingsException {

        if (Boolean.parseBoolean(ServerSettings.getSetting("enable.realtime.monitor"))) {
            if (mapData.getPreJobCommands() == null) {
                mapData.setPreJobCommands(new ArrayList<>());
            }
            mapData.getPreJobCommands().add(0, "curl -X POST -H \"Content-Type: application/vnd.kafka.json.v2+json\" " +
                    "-H \"Accept: application/vnd.kafka.v2+json\" " +
                    "--data '{\"records\":[{\"value\":{\"jobName\":\"" + mapData.getJobName() + "\", \"status\":\"RUNNING\", \"task\":\"" + mapData.getTaskId() + "\"}}]}' \"" +
                    ServerSettings.getSetting("job.status.publish.endpoint") + "\" > /dev/null || true");


            if (mapData.getPostJobCommands() == null) {
                mapData.setPostJobCommands(new ArrayList<>());
            }
            mapData.getPostJobCommands().add("curl -X POST -H \"Content-Type: application/vnd.kafka.json.v2+json\" " +
                    "-H \"Accept: application/vnd.kafka.v2+json\" " +
                    "--data '{\"records\":[{\"value\":{\"jobName\":\"" + mapData.getJobName() + "\", \"status\":\"COMPLETED\", \"task\":\"" + mapData.getTaskId() + "\"}}]}' \"" +
                    ServerSettings.getSetting("job.status.publish.endpoint") + "\" > /dev/null || true");
        }
    }
}
