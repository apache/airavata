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
package org.apache.airavata.helix.impl.workflow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.airavata.api.thrift.util.ThriftUtils;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.HelixTaskFactory;
import org.apache.airavata.helix.impl.task.cancel.CancelCompletingTask;
import org.apache.airavata.helix.impl.task.cancel.RemoteJobCancellationTask;
import org.apache.airavata.helix.impl.task.cancel.WorkflowCancellationTask;
import org.apache.airavata.helix.impl.task.completing.CompletingTask;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ResourceType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.ProcessTerminateEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.monitor.platform.CountMonitor;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("messagingFactory")
public class PreWorkflowManager extends WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);
    private static final CountMonitor prewfCounter = new CountMonitor("pre_wf_counter");

    private final AiravataServerProperties properties;
    private final org.apache.airavata.helix.impl.task.TaskFactory taskFactory;
    private final org.springframework.context.ApplicationContext applicationContext;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final org.apache.airavata.service.profile.UserProfileService userProfileService;
    private final org.apache.airavata.service.security.CredentialStoreService credentialStoreService;
    private Subscriber subscriber;

    public PreWorkflowManager(
            AiravataServerProperties properties,
            org.apache.airavata.helix.impl.task.TaskFactory taskFactory,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            org.apache.airavata.service.profile.UserProfileService userProfileService,
            org.apache.airavata.service.security.CredentialStoreService credentialStoreService) {
        // Default values, will be updated in @PostConstruct
        super("pre-workflow-manager", false, registryService, properties);
        this.properties = properties;
        this.taskFactory = taskFactory;
        this.applicationContext = applicationContext;
        this.registryService = registryService;
        this.userProfileService = userProfileService;
        this.credentialStoreService = credentialStoreService;
    }

    @jakarta.annotation.PostConstruct
    public void initWorkflowManager() {
        if (properties != null) {
            String managerName = properties.services.prewm.name;
            boolean loadBalance = properties.services.prewm.loadBalanceClusters;
            this.workflowManagerName = managerName;
            this.loadBalanceClusters = loadBalance;
        }
    }

    public void startServer() throws Exception {
        super.initComponents();
        initLaunchSubscriber();
    }

    /**
     * Standardized start method for Spring Boot integration.
     * Non-blocking: initializes components and returns immediately.
     */
    public void start() throws Exception {
        startServer();
    }

    public void stopServer() {}

    private void initLaunchSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(properties.rabbitmq.processExchangeName);
        this.subscriber =
                MessagingFactory.getSubscriber(new ProcessLaunchMessageHandler(), routingKeys, Type.PROCESS_LAUNCH);
    }

    private String createAndLaunchPreWorkflow(String processId, boolean forceRun) throws Exception {

        prewfCounter.inc();
        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        ExperimentModel experimentModel;
        HelixTaskFactory taskFactory;
        try {
            processModel = registryService.getProcess(processId);
            experimentModel = registryService.getExperiment(processModel.getExperimentId());
            ResourceType resourceType = registryService
                    .getGroupComputeResourcePreference(
                            processModel.getComputeResourceId(), processModel.getGroupResourceProfileId())
                    .getResourceType();
            taskFactory = this.taskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (RegistryServiceException e) {
            logger.error(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
            throw new Exception(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
        }

        String taskDag = processModel.getTaskDag();
        List<TaskModel> taskList = processModel.getTasks();

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
                            experimentModel.getUserConfigurationData().isAiravataAutoSchedule());
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
            CompletingTask completingTask =
                    new CompletingTask(applicationContext, registryService, userProfileService, credentialStoreService);
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

        String workflowName = getWorkflowOperator()
                .launchWorkflow(
                        processId + "-PRE-" + UUID.randomUUID().toString(), new ArrayList<>(allTasks), true, false);

        registerWorkflowForProcess(processId, workflowName, "PRE");

        return workflowName;
    }

    private String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {

        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        GroupComputeResourcePreference gcrPref;

        try {
            processModel = registryService.getProcess(processId);
            gcrPref = registryService.getGroupComputeResourcePreference(
                    processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());

        } catch (RegistryServiceException e) {
            logger.error("Failed to fetch process from registry associated with process id " + processId, e);
            throw new Exception("Failed to fetch process from registry associated with process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();
        final List<AbstractTask> allTasks = new ArrayList<>();

        Optional<List<String>> workflowsOpt = Optional.ofNullable(processModel.getProcessWorkflows())
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

            RemoteJobCancellationTask rjct = new RemoteJobCancellationTask(
                    applicationContext, registryService, userProfileService, credentialStoreService);
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

        CancelCompletingTask cct = new CancelCompletingTask(
                applicationContext, registryService, userProfileService, credentialStoreService);
        cct.setTaskId(UUID.randomUUID().toString());
        cct.setExperimentId(experimentId);
        cct.setProcessId(processId);
        cct.setGatewayId(gateway);
        cct.setSkipAllStatusPublish(true);

        if (!allTasks.isEmpty()) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(cct.getTaskId(), cct));
        }
        allTasks.add(cct);

        String workflow =
                getWorkflowOperator().launchWorkflow(processId + "-CANCEL-" + UUID.randomUUID(), allTasks, true, false);
        logger.info("Started launching workflow {} to cancel process {}", workflow, processId);
        return workflow;
    }

    public static void main(String[] args) throws Exception {
        // Note: PreWorkflowManager is a Spring component and requires dependencies.
        // This main method should be run within a Spring application context.
        // For standalone execution, use Spring Boot application or provide dependencies manually.
        throw new UnsupportedOperationException("PreWorkflowManager must be used within a Spring application context");
    }

    private class ProcessLaunchMessageHandler implements MessageHandler {

        @Override
        public void onMessage(MessageContext messageContext) {
            logger.info(" Message Received with message id " + messageContext.getMessageId()
                    + " and with message type: " + messageContext.getType());

            if (messageContext.getType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent event = new ProcessSubmitEvent();
                var messageEvent = messageContext.getEvent();

                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                } catch (Exception e) {
                    logger.error("Failed to fetch process submit event", e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }

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
                    ProcessStatus status = new ProcessStatus();
                    status.setState(ProcessState.STARTED);
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    publishProcessStatus(processId, experimentId, gateway, ProcessState.STARTED);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                } catch (Exception e) {
                    logger.error(
                            "Failed to launch the pre workflow for process " + processId + " in gateway " + gateway, e);
                    // subscriber.sendAck(messageContext.getDeliveryTag());
                }

            } else if (messageContext.getType().equals(MessageType.TERMINATEPROCESS)) {
                ProcessTerminateEvent event = new ProcessTerminateEvent();
                var messageEvent = messageContext.getEvent();

                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                } catch (Exception e) {
                    logger.error("Failed to fetch process cancellation event", e);
                    subscriber.sendAck(messageContext.getDeliveryTag());
                }

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
