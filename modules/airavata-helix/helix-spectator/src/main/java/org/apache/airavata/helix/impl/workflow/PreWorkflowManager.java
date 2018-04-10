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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask;
import org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask;
import org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask;
import org.apache.airavata.helix.impl.task.env.EnvSetupTask;
import org.apache.airavata.helix.impl.task.staging.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PreWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);

    private Subscriber subscriber;

    public PreWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("pre.workflow.manager.name"));
    }

    private void initAllComponents() throws Exception {
        super.initComponents();
        initLaunchSubscriber();
    }

    private void initLaunchSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessExchangeName());
        this.subscriber = MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(), routingKeys, Type.PROCESS_LAUNCH);
    }

    private String createAndLaunchPreWorkflow(String processId) throws Exception {

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

        String[] taskIds = taskDag.split(",");
        final List<AiravataTask> allTasks = new ArrayList<>();

        boolean jobSubmissionFound = false;

        for (String taskId : taskIds) {
            Optional<TaskModel> model = taskList.stream().filter(taskModel -> taskModel.getTaskId().equals(taskId)).findFirst();

            if (model.isPresent()) {
                TaskModel taskModel = model.get();
                AiravataTask airavataTask = null;
                if (taskModel.getTaskType() == TaskTypes.ENV_SETUP) {
                    airavataTask = new EnvSetupTask();
                } else if (taskModel.getTaskType() == TaskTypes.JOB_SUBMISSION) {
                    airavataTask = new DefaultJobSubmissionTask();
                    airavataTask.setRetryCount(1);
                    jobSubmissionFound = true;
                } else if (taskModel.getTaskType() == TaskTypes.DATA_STAGING) {
                    if (!jobSubmissionFound) {
                        airavataTask = new InputDataStagingTask();
                    }
                }

                if (airavataTask != null) {
                    airavataTask.setGatewayId(experimentModel.getGatewayId());
                    airavataTask.setExperimentId(experimentModel.getExperimentId());
                    airavataTask.setProcessId(processModel.getProcessId());
                    airavataTask.setTaskId(taskModel.getTaskId());
                    if (allTasks.size() > 0) {
                        allTasks.get(allTasks.size() -1).setNextTask(new OutPort(airavataTask.getTaskId(), airavataTask));
                    }
                    allTasks.add(airavataTask);
                }
            }
        }

        String workflowName = getWorkflowOperator().launchWorkflow(processId + "-PRE-" + UUID.randomUUID().toString(),
                new ArrayList<>(allTasks), true, false);
        try {
            MonitoringUtil.registerWorkflow(getCuratorClient(), processId, workflowName);
        } catch (Exception e) {
            logger.error("Failed to save workflow " + workflowName + " of process " + processId + " in zookeeper registry. " +
                    "This will affect cancellation tasks", e);
        }
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

        MonitoringUtil.registerCancelProcess(getCuratorClient(), processId);
        List<String> workflows = MonitoringUtil.getWorkflowsOfProcess(getCuratorClient(), processId);
        final List<AbstractTask> allTasks = new ArrayList<>();
        if (workflows != null && workflows.size() > 0) {
            for (String wf : workflows) {
                logger.info("Creating cancellation task for workflow " + wf + " of process " + processId);
                WorkflowCancellationTask wfct = new WorkflowCancellationTask();
                wfct.setTaskId(UUID.randomUUID().toString());
                wfct.setCancellingWorkflowName(wf);

                if (allTasks.size() > 0) {
                    allTasks.get(allTasks.size() -1).setNextTask(new OutPort(wfct.getTaskId(), wfct));
                }
                allTasks.add(wfct);
            }

        } else {
            logger.info("No workflow registered with process " + processId + " to cancel");
            return null;
        }

        RemoteJobCancellationTask rjct = new RemoteJobCancellationTask();
        rjct.setTaskId(UUID.randomUUID().toString());
        rjct.setExperimentId(experimentId);
        rjct.setProcessId(processId);
        rjct.setGatewayId(gateway);
        rjct.setSkipTaskStatusPublish(true);

        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() -1).setNextTask(new OutPort(rjct.getTaskId(), rjct));
        }
        allTasks.add(rjct);

        CancelCompletingTask cct = new CancelCompletingTask();
        cct.setTaskId(UUID.randomUUID().toString());
        cct.setExperimentId(experimentId);
        cct.setProcessId(processId);
        cct.setGatewayId(gateway);
        cct.setSkipTaskStatusPublish(true);

        if (allTasks.size() > 0) {
            allTasks.get(allTasks.size() -1).setNextTask(new OutPort(cct.getTaskId(), cct));
        }
        allTasks.add(cct);

        String workflow = getWorkflowOperator().launchWorkflow(processId + "-CANCEL-" + UUID.randomUUID().toString(), allTasks, true, false);
        logger.info("Started launching workflow " + workflow + " to cancel process " + processId);
        return workflow;
    }

    public static void main(String[] args) throws Exception {
        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.initAllComponents();
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
                    String workflowName = createAndLaunchPreWorkflow(processId);
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
