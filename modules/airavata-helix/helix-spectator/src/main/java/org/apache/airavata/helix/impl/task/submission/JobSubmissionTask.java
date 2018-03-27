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
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.registry.cpi.*;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.helix.HelixManager;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

public abstract class JobSubmissionTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(JobSubmissionTask.class);

    private CuratorFramework curatorClient = null;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        try {
            this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
            this.curatorClient.start();
        } catch (ApplicationSettingsException e) {
            logger.error("Failed to create curator client ", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    // TODO perform exception handling
    @SuppressWarnings("WeakerAccess")
    protected void createMonitoringNode(String jobId, String jobName) throws Exception {
        logger.info("Creating zookeeper paths for job monitoring for job id : " + jobId + ", process : "
                + getProcessId() + ", gateway : " + getGatewayId());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/lock", new byte[0]);
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/gateway", getGatewayId().getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/process", getProcessId().getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/task", getTaskId().getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/experiment", getExperimentId().getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobId + "/jobName", jobName.getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/monitoring/" + jobName + "/jobId", jobId.getBytes());
        getCuratorClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/registry/" + getProcessId() + "/jobs/" + jobId, new byte[0]);
    }

    @SuppressWarnings("WeakerAccess")
    protected JobSubmissionOutput submitBatchJob(AgentAdaptor agentAdaptor, GroovyMapData groovyMapData, String workingDirectory) throws Exception {
        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                getAppCatalog(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));

        addMonitoringCommands(groovyMapData);

        String scriptAsString = groovyMapData.getAsString(jobManagerConfiguration.getJobDescriptionTemplateName());

        int number = new SecureRandom().nextInt();
        number = (number < 0 ? -number : number);
        File tempJobFile = new File(getLocalDataDir(), "job_" + Integer.toString(number) + jobManagerConfiguration.getScriptExtension());

        FileUtils.writeStringToFile(tempJobFile, scriptAsString);
        logger.info("Job submission file for process " + getProcessId() + " was created at : " + tempJobFile.getAbsolutePath());

        logger.info("Copying file form " + tempJobFile.getAbsolutePath() + " to remote path " + workingDirectory +
                " of compute resource " + getTaskContext().getComputeResourceId());
        agentAdaptor.copyFileTo(tempJobFile.getAbsolutePath(), workingDirectory);
        // TODO transfer file
        RawCommandInfo submitCommand = jobManagerConfiguration.getSubmitCommand(workingDirectory, tempJobFile.getPath());

        logger.debug("Submit command for process id " + getProcessId() + " : " + submitCommand.getRawCommand());
        logger.debug("Working directory for process id " + getProcessId() + " : " + workingDirectory);

        CommandOutput commandOutput = agentAdaptor.executeCommand(submitCommand.getRawCommand(), workingDirectory);

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

    @SuppressWarnings("WeakerAccess")
    public File getLocalDataDir() {
        String outputPath = ServerSettings.getLocalDataLocation();
        outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
        return new File(outputPath + getProcessId());
    }

    @SuppressWarnings("WeakerAccess")
    public JobStatus getJobStatus(AgentAdaptor agentAdaptor, String jobID) throws Exception {
        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                getAppCatalog(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));
        CommandOutput commandOutput = agentAdaptor.executeCommand(jobManagerConfiguration.getMonitorCommand(jobID).getRawCommand(), null);

        return jobManagerConfiguration.getParser().parseJobStatus(jobID, commandOutput.getStdOut());

    }

    @SuppressWarnings("WeakerAccess")
    public String getJobIdByJobName(AgentAdaptor agentAdaptor, String jobName, String userName) throws Exception {
        JobManagerConfiguration jobManagerConfiguration = JobFactory.getJobManagerConfiguration(JobFactory.getResourceJobManager(
                getAppCatalog(), getTaskContext().getJobSubmissionProtocol(), getTaskContext().getPreferredJobSubmissionInterface()));

        RawCommandInfo jobIdMonitorCommand = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
        CommandOutput commandOutput = agentAdaptor.executeCommand(jobIdMonitorCommand.getRawCommand(), null);
        return jobManagerConfiguration.getParser().parseJobId(jobName, commandOutput.getStdOut());
    }

    @SuppressWarnings("WeakerAccess")
    public void saveJobModel(JobModel jobModel) throws RegistryException {
        getExperimentCatalog().add(ExpCatChildDataType.JOB, jobModel, getProcessId());
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

            CompositeIdentifier ids = new CompositeIdentifier(jobModel.getTaskId(), jobModel.getJobId());
            getExperimentCatalog().add(ExpCatChildDataType.JOB_STATUS, jobStatus, ids);
            JobIdentifier identifier = new JobIdentifier(jobModel.getJobId(), jobModel.getTaskId(),
                    getProcessId(), getProcessModel().getExperimentId(), getGatewayId());

            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
            MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
                    (MessageType.JOB.name()), getGatewayId());
            msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            getStatusPublisher().publish(msgCtx);
        } catch (Exception e) {
            throw new Exception("Error persisting job status " + e.getLocalizedMessage(), e);
        }
    }

    private void addMonitoringCommands(GroovyMapData mapData) throws ApplicationSettingsException {
        if (mapData.getPreJobCommands() == null) {
            mapData.setPreJobCommands(new ArrayList<>());
        }

        mapData.getPreJobCommands().add(0, "curl -X POST -H \"Content-Type: application/vnd.kafka.json.v2+json\" " +
                "-H \"Accept: application/vnd.kafka.v2+json\" " +
                "--data '{\"records\":[{\"value\":{\"jobName\":\"" + mapData.getJobName() + "\", \"status\":\"RUNNING\"}}]}' \"" +
                ServerSettings.getSetting("job.status.publish.endpoint") + "\"");

        if (mapData.getPostJobCommands() == null) {
            mapData.setPostJobCommands(new ArrayList<>());
        }

        mapData.getPostJobCommands().add("curl -X POST -H \"Content-Type: application/vnd.kafka.json.v2+json\" " +
                "-H \"Accept: application/vnd.kafka.v2+json\" " +
                "--data '{\"records\":[{\"value\":{\"jobName\":\"" + mapData.getJobName() + "\", \"status\":\"COMPLETED\"}}]}' \"" +
                ServerSettings.getSetting("job.status.publish.endpoint") + "\"");
    }
}
