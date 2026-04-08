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
package org.apache.airavata.orchestration.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessageHandler;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Subscriber;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ResourceType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.messaging.event.proto.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.proto.ProcessTerminateEvent;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.apache.airavata.orchestration.infrastructure.HelixTaskFactory;
import org.apache.airavata.orchestration.infrastructure.TaskFactory;
import org.apache.airavata.orchestration.messaging.ProcessConsumer;
import org.apache.airavata.orchestration.task.CancelCompletingTask;
import org.apache.airavata.orchestration.task.CompletingTask;
import org.apache.airavata.orchestration.task.RemoteJobCancellationTask;
import org.apache.airavata.orchestration.task.WorkflowCancellationTask;
import org.apache.airavata.server.CountMonitor;
import org.apache.airavata.server.IServer;
import org.apache.airavata.task.AbstractTask;
import org.apache.airavata.task.AiravataTask;
import org.apache.airavata.task.OutPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreWorkflowManager implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);
    private static final CountMonitor prewfCounter = new CountMonitor("pre_wf_counter");

    private final WorkflowManager wfManager;
    private Subscriber subscriber;
    private IServer.ServerStatus status = IServer.ServerStatus.STOPPED;

    public PreWorkflowManager() throws ApplicationSettingsException {
        wfManager = new WorkflowManager(
                ServerSettings.getSetting("pre.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("pre.workflow.manager.loadbalance.clusters")));
    }

    @Override
    public void run() {
        status = ServerStatus.STARTED;
        try {
            wfManager.initComponents();
            initLaunchSubscriber();
            // Park thread — messaging callbacks arrive asynchronously
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            logger.error("PreWorkflowManager failed", e);
            status = ServerStatus.FAILED;
        }
    }

    @Override
    public String getName() {
        return "pre_workflow_manager";
    }

    @Override
    public void stop() throws Exception {
        status = ServerStatus.STOPPING;
        status = ServerStatus.STOPPED;
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    private void initLaunchSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessExchangeName());
        this.subscriber = MessagingFactory.getSubscriber(
                new ProcessConsumer(new ProcessLaunchMessageHandler()), routingKeys, Type.PROCESS_LAUNCH);
    }

    private String createAndLaunchPreWorkflow(String processId, boolean forceRun) throws Exception {

        prewfCounter.inc();
        RegistryHandler registryHandler = wfManager.getRegistryHandler();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        HelixTaskFactory taskFactory;
        try {
            processModel = registryHandler.getProcess(processId);
            experimentModel = registryHandler.getExperiment(processModel.getExperimentId());
            ResourceType resourceType = registryHandler
                    .getGroupComputeResourcePreference(
                            processModel.getComputeResourceId(), processModel.getGroupResourceProfileId())
                    .getResourceType();
            taskFactory = TaskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (Exception e) {
            logger.error(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
            throw new Exception(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasksList();

        boolean intermediateTransfer =
                taskList.stream().anyMatch(task -> task.getTaskType() == TaskTypes.OUTPUT_FETCHING);

        if (intermediateTransfer) {
            logger.info("Process {} contains intermediate file transfers", processId);
        }

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream()
                    .filter(taskModel -> taskModel.getTaskId().equals(taskId))
                    .findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;

                if (intermediateTransfer) {
                    if (taskModel.getTaskType() == TaskTypes.OUTPUT_FETCHING) {
                        airavataTask = taskFactory.createOutputDataStagingTask(processId);
                        airavataTask.setForceRunTask(true);
                        airavataTask.setSkipExperimentStatusPublish(true);
                    }

                } else if (taskModel.getTaskType() == TaskTypes.ENV_SETUP) {
                    airavataTask = taskFactory.createEnvSetupTask(processId);
                    airavataTask.setForceRunTask(true);
                } else if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    airavataTask = taskFactory.createJobSubmissionTask(processId);
                    airavataTask.setForceRunTask(forceRun);
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (!jobSubmissionFound) {
                        airavataTask = taskFactory.createInputDataStagingTask(processId);
                        airavataTask.setForceRunTask(true);
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    airavataTask.setRetryCount(taskModel.getMaxRetry());
                    airavataTask.setAutoSchedule(
                            experimentModel.getUserConfigurationData().getAiravataAutoSchedule());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() - 1)
                                .setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

        // For intermediate transfers add a final CompletingTask
        if (intermediateTransfer) {
            CompletingTask completingTask = new CompletingTask();
            completingTask.setGatewayId(experimentModel.getGatewayId());
            completingTask.setExperimentId(experimentModel.getExperimentId());
            completingTask.setProcessId(processModel.getProcessId());
            completingTask.setTaskId("Completing-Task-" + UUID.randomUUID().toString() + "-");
            completingTask.setForceRunTask(forceRun);
            completingTask.setSkipAllStatusPublish(true);
            if (allTasks.size() > 0) {
                allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(completingTask.getTaskId(), completingTask));
            }
            allTasks.add(completingTask);
        }

        String workflowName = wfManager
                .getWorkflowOperator()
                .launchWorkflow(
                        processId + "-PRE-" + UUID.randomUUID().toString(), new ArrayList<>(allTasks), true, false);

        wfManager.registerWorkflowForProcess(processId, workflowName, "PRE");

        return workflowName;
    }

    private String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {

        RegistryHandler registryHandler = wfManager.getRegistryHandler();

        ProcessModel processModel;
        GroupComputeResourcePreference gcrPref;

        try {
            processModel = registryHandler.getProcess(processId);
            gcrPref = registryHandler.getGroupComputeResourcePreference(
                    processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());

        } catch (Exception e) {
            logger.error("Failed to fetch process from registry associated with process id " + processId, e);
            throw new Exception("Failed to fetch process from registry associated with process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();
        final List<AbstractTask> allTasks = new ArrayList<>();

        Optional<List<String>> workflowsOpt = Optional.ofNullable(processModel.getProcessWorkflowsList())
                .map(wfs -> wfs.stream().map(ProcessWorkflow::getWorkflowId).collect(Collectors.toList()));

        if (workflowsOpt.isPresent()) {
            List<String> workflows = workflowsOpt.get();
            if (workflows.size() > 0) {
                for (String wf : workflows) {
                    logger.info("Creating cancellation task for workflow " + wf + " of process " + processId);
                    WorkflowCancellationTask wfct = new WorkflowCancellationTask();
                    wfct.setTaskId(UUID.randomUUID().toString());
                    wfct.setCancellingWorkflowName(wf);

                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(wfct.getTaskId(), wfct));
                    }
                    allTasks.add(wfct);
                }

            } else {
                logger.warn("No workflow registered with process " + processId + " to cancel");
            }
        } else {
            logger.warn("No workflow registered with process " + processId + " to cancel");
        }

        if (gcrPref.getResourceType() == ResourceType.SLURM) {
            logger.info(
                    "Skipping cancel workflow for process {} as it is not a SLURM process, resource type: {}",
                    processId,
                    gcrPref.getResourceType());

            RemoteJobCancellationTask rjct = new RemoteJobCancellationTask();
            rjct.setTaskId(UUID.randomUUID().toString());
            rjct.setExperimentId(experimentId);
            rjct.setProcessId(processId);
            rjct.setGatewayId(gateway);
            rjct.setSkipAllStatusPublish(true);

            if (!allTasks.isEmpty()) {
                allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(rjct.getTaskId(), rjct));
            }
            allTasks.add(rjct);
        }

        CancelCompletingTask cct = new CancelCompletingTask();
        cct.setTaskId(UUID.randomUUID().toString());
        cct.setExperimentId(experimentId);
        cct.setProcessId(processId);
        cct.setGatewayId(gateway);
        cct.setSkipAllStatusPublish(true);

        if (!allTasks.isEmpty()) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(cct.getTaskId(), cct));
        }
        allTasks.add(cct);

        String workflow = wfManager
                .getWorkflowOperator()
                .launchWorkflow(processId + "-CANCEL-" + UUID.randomUUID(), allTasks, true, false);
        logger.info("Started launching workflow {} to cancel process {}", workflow, processId);
        return workflow;
    }

    public static void main(String[] args) throws Exception {

        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.run();
    }

    private class ProcessLaunchMessageHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            logger.info(" Message Received with message id " + messageContext.getMessageId()
                    + " and with message type: " + messageContext.getType());

            if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent event = (ProcessSubmitEvent) messageContext.getEvent();

                String processId = event.getProcessId();
                String experimentId = event.getExperimentId();
                String gateway = event.getGatewayId();

                logger.info("Received process launch message for process " + processId + " of experiment "
                        + experimentId + " in gateway " + gateway);

                try {
                    logger.info("Launching the pre workflow for process " + processId + " of experiment " + experimentId
                            + " in gateway " + gateway);
                    String workflowName = createAndLaunchPreWorkflow(processId, false);
                    logger.info("Completed launching the pre workflow " + workflowName + " for process" + processId
                            + " of experiment " + experimentId + " in gateway " + gateway);

                    // updating the process status
                    ProcessStatus status = ProcessStatus.newBuilder()
                            .setState(ProcessState.PROCESS_STATE_STARTED)
                            .setTimeOfStateChange(Calendar.getInstance().getTimeInMillis())
                            .build();
                    wfManager.publishProcessStatus(
                            processId, experimentId, gateway, ProcessState.PROCESS_STATE_STARTED);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error(
                            "Failed to launch the pre workflow for process " + processId + " in gateway " + gateway, e);
                    // subscriber.sendAck(messageContext.getDeliveryTag());
                }

            } else if (messageContext.getType().equals(MessageType.TERMINATEPROCESS)) {
                ProcessTerminateEvent event = (ProcessTerminateEvent) messageContext.getEvent();

                String processId = event.getProcessId();
                String gateway = event.getGatewayId();

                logger.info("Received process cancel message for process " + processId + " in gateway " + gateway);

                try {
                    logger.info("Launching the process cancel workflow for process " + processId + " in gateway "
                            + gateway);
                    String workflowName = createAndLaunchCancelWorkflow(processId, gateway);
                    logger.info("Completed process cancel workflow " + workflowName + " for process " + processId
                            + " in gateway " + gateway);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error(
                            "Failed to launch process cancel workflow for process " + processId + " in gateway "
                                    + gateway,
                            e);
                    // subscriber.sendAck(messageContext.getDeliveryTag());
                }
            } else {
                logger.warn("Unknown message type");
                subscriber.sendAck(messageContext.getDeliveryTag());
            }
        }
    }
}
