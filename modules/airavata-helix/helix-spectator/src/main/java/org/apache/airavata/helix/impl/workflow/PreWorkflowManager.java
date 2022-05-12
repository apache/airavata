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
package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask;
import org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask;
import org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.helix.impl.task.env.EnvSetupTask;
import org.apache.airavata.helix.impl.task.staging.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PreWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);
    private final static CountMonitor prewfCounter = new CountMonitor("pre_wf_counter");

    private Subscriber subscriber;

    public PreWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("pre.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("pre.workflow.manager.loadbalance.clusters")));
    }

    public void startServer() throws Exception {
        super.initComponents();
        initLaunchSubscriber();
    }

    public void stopServer() {

    }

    private void initLaunchSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessExchangeName());
        this.subscriber = MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(), routingKeys, Type.PROCESS_LAUNCH);
    }

    private String createAndLaunchPreWorkflow(String processId, boolean forceRun) throws Exception {

        prewfCounter.inc();
        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = registryClient.getProcess(processId);
            experimentModel = registryClient.getExperiment(processModel.getExperimentId());
            getRegistryClientPool().returnResource(registryClient);

        } catch (Exception e) {
            logger.error("Failed to fetch experiment or process from registry associated with process id " + processId, e);
            getRegistryClientPool().returnBrokenResource(registryClient);
            throw new Exception("Failed to fetch experiment or process from registry associated with process id " + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasks();

        boolean intermediateTransfer = taskList.stream()
                .anyMatch(task -> task.getTaskType() == TaskTypes.OUTPUT_FETCHING);

        if (intermediateTransfer) {
            logger.info("Process {} contains intermediate file transfers", processId);
        }

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream().filter(taskModel -> taskModel.getTaskId().equals(taskId)).findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;

                if (intermediateTransfer) {
                    if (taskModel.getTaskType() == TaskTypes.OUTPUT_FETCHING) {
                        airavataTask = new OutputDataStagingTask();
                        airavataTask.setForceRunTask(true);
                        airavataTask.setSkipExperimentStatusPublish(true);
                    }

                } else if (taskModel.getTaskType() == TaskTypes.ENV_SETUP) {
                    airavataTask = new EnvSetupTask();
                    airavataTask.setForceRunTask(true);
                } else if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    airavataTask = new DefaultJobSubmissionTask();
                    airavataTask.setForceRunTask(forceRun);
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (!jobSubmissionFound) {
                        airavataTask = new InputDataStagingTask();
                        airavataTask.setForceRunTask(true);
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    airavataTask.setRetryCount(taskModel.getMaxRetry());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() -1).setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
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

        String workflowName = getWorkflowOperator().launchWorkflow(processId + "-PRE-" + UUID.randomUUID().toString(),
                new ArrayList<>(allTasks), true, false);

        registerWorkflowForProcess(processId, workflowName, "PRE");

        return workflowName;
    }

    private String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {


        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        ProcessModel processModel;
        try {
            processModel = registryClient.getProcess(processId);
            getRegistryClientPool().returnResource(registryClient);

        } catch (Exception e) {
            logger.error("Failed to fetch process from registry associated with process id " + processId, e);
            getRegistryClientPool().returnBrokenResource(registryClient);
            throw new Exception("Failed to fetch process from registry associated with process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();
        final List<AbstractTask> allTasks = new ArrayList<>();

        Optional<List<String>> workflowsOpt = Optional.ofNullable(processModel.getProcessWorkflows()).map(wfs -> wfs.stream().map(ProcessWorkflow::getWorkflowId).collect(Collectors.toList()));

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

        RemoteJobCancellationTask rjct = new RemoteJobCancellationTask();
        rjct.setTaskId(UUID.randomUUID().toString());
        rjct.setExperimentId(experimentId);
        rjct.setProcessId(processId);
        rjct.setGatewayId(gateway);
        rjct.setSkipAllStatusPublish(true);

        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() -1).setNextTask(new OutPort(rjct.getTaskId(), rjct));
        }
        allTasks.add(rjct);

        CancelCompletingTask cct = new CancelCompletingTask();
        cct.setTaskId(UUID.randomUUID().toString());
        cct.setExperimentId(experimentId);
        cct.setProcessId(processId);
        cct.setGatewayId(gateway);
        cct.setSkipAllStatusPublish(true);

        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() -1).setNextTask(new OutPort(cct.getTaskId(), cct));
        }
        allTasks.add(cct);

        String workflow = getWorkflowOperator().launchWorkflow(processId + "-CANCEL-" + UUID.randomUUID().toString(), allTasks, true, false);
        logger.info("Started launching workflow " + workflow + " to cancel process " + processId);
        return workflow;
    }

    public static void main(String[] args) throws Exception {

        if (ServerSettings.getBooleanSetting("pre.workflow.manager.monitoring.enabled")) {
            MonitoringServer monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("pre.workflow.manager.monitoring.host"),
                    ServerSettings.getIntSetting("pre.workflow.manager.monitoring.port"));
            monitoringServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.startServer();
    }

    private class ProcessLaunchMessageHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            logger.info(" Message Received with message id " + messageContext.getMessageId() + " and with message type: " + messageContext.getType());

            if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent event = new ProcessSubmitEvent();
                TBase messageEvent = messageContext.getEvent();

                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                } catch (TException e) {
                    logger.error("Failed to fetch process submit event", e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }

                String processId = event.getProcessId();
                String experimentId = event.getExperimentId();
                String gateway = event.getGatewayId();

                logger.info("Received process launch message for process " + processId + " of experiment " + experimentId + " in gateway " + gateway);

                try {
                    logger.info("Launching the pre workflow for process " + processId + " of experiment " + experimentId + " in gateway " + gateway);
                    String workflowName = createAndLaunchPreWorkflow(processId, false);
                    logger.info("Completed launching the pre workflow " + workflowName + " for process" + processId + " of experiment " + experimentId + " in gateway " + gateway);

                    // updating the process status
                    ProcessStatus status = new ProcessStatus();
                    status.setState(ProcessState.STARTED);
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    publishProcessStatus(processId, experimentId, gateway, ProcessState.STARTED);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error("Failed to launch the pre workflow for process " + processId + " in gateway " + gateway, e);
                    //subscriber.sendAck(messageContext.getDeliveryTag());
                }

            } else if (messageContext.getType().equals(MessageType.TERMINATEPROCESS)) {
                ProcessTerminateEvent event = new ProcessTerminateEvent();
                TBase messageEvent = messageContext.getEvent();

                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                } catch (TException e) {
                    logger.error("Failed to fetch process cancellation event", e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }

                String processId = event.getProcessId();
                String gateway = event.getGatewayId();

                logger.info("Received process cancel message for process " + processId + " in gateway " + gateway);

                try {
                    logger.info("Launching the process cancel workflow for process " + processId + " in gateway " + gateway);
                    String workflowName = createAndLaunchCancelWorkflow(processId, gateway);
                    logger.info("Completed process cancel workflow " + workflowName + " for process " + processId + " in gateway " + gateway);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error("Failed to launch process cancel workflow for process " + processId + " in gateway " + gateway, e);
                    //subscriber.sendAck(messageContext.getDeliveryTag());
                }
            } else {
                logger.warn("Unknown message type");
                subscriber.sendAck(messageContext.getDeliveryTag());
            }
        }
    }
}
