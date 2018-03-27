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
import org.apache.airavata.helix.impl.task.env.EnvSetupTask;
import org.apache.airavata.helix.impl.task.staging.InputDataStagingTask;
import org.apache.airavata.helix.impl.task.submission.DefaultJobSubmissionTask;
import org.apache.airavata.helix.workflow.WorkflowManager;
import org.apache.airavata.messaging.core.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.ProcessTerminateEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PreWorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);

    private Subscriber subscriber;
    private CuratorFramework curatorClient = null;

    @SuppressWarnings("WeakerAccess")
    public PreWorkflowManager() throws AiravataException {
        init();
    }

    private void init() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(ServerSettings.getRabbitmqProcessExchangeName());
        this.subscriber = MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(), routingKeys, Type.PROCESS_LAUNCH);

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private void registerWorkflow(String processId, String workflowId) throws Exception {
        this.curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                "/registry/" + processId + "/workflows/" + workflowId , new byte[0]);
    }

    private void registerCancelProcess(String processId) throws Exception {
        String path = "/registry/" + processId + "/status";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            this.curatorClient.delete().forPath(path);
        }
        this.curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
                path , "cancel".getBytes());
    }

    private List<String> getWorkflowsOfProcess(String processId) throws Exception {
        String path = "/registry/" + processId + "/workflows";
        if (this.curatorClient.checkExists().forPath(path) != null) {
            return this.curatorClient.getChildren().forPath(path);
        } else {
            return null;
        }
    }

    private String createAndLaunchPreWorkflow(String processId, String gateway) throws Exception {

        ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog(gateway);

        ProcessModel processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
        ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, processModel.getExperimentId());
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

        WorkflowManager workflowManager = new WorkflowManager(
                ServerSettings.getSetting("helix.cluster.name"),
                ServerSettings.getSetting("pre.workflow.manager.name"),
                ServerSettings.getZookeeperConnection());
        String workflowName = workflowManager.launchWorkflow(processId + "-PRE-" + UUID.randomUUID().toString(),
                new ArrayList<>(allTasks), true, false);
        try {
            registerWorkflow(processId, workflowName);
        } catch (Exception e) {
            logger.error("Failed to save workflow " + workflowName + " of process " + processId + " in zookeeper registry. " +
                    "This will affect cancellation tasks", e);
        }
        return workflowName;
    }

    private String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {

        ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog(gateway);
        ProcessModel processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);

        String experimentId = processModel.getExperimentId();

        registerCancelProcess(processId);
        List<String> workflows = getWorkflowsOfProcess(processId);
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

        WorkflowManager workflowManager = new WorkflowManager(
                ServerSettings.getSetting("helix.cluster.name"),
                ServerSettings.getSetting("pre.workflow.manager.name"),
                ServerSettings.getZookeeperConnection());

        String workflow = workflowManager.launchWorkflow(processId + "-CANCEL-" + UUID.randomUUID().toString(), allTasks, true, false);
        logger.info("Started launching workflow " + workflow + " to cancel process " + processId);
        return workflow;
    }

    public static void main(String[] args) throws Exception {
        PreWorkflowManager preWorkflowManager = new PreWorkflowManager();
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
                String gateway = event.getGatewayId();

                logger.info("Received process launch message for process " + processId + " in gateway " + gateway);

                try {
                    logger.info("Launching the pre workflow for process " + processId + " in gateway " + gateway);
                    String workflowName = createAndLaunchPreWorkflow(processId, gateway);
                    logger.info("Completed launching the pre workflow " + workflowName + " for process " + processId + " in gateway " + gateway);
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
