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
package org.apache.airavata.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ComputeResourceType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessWorkflow;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.dapr.messaging.DaprMessagingFactory;
import org.apache.airavata.dapr.messaging.Subscriber;
import org.apache.airavata.dapr.messaging.Type;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.task.OutPort;
import org.apache.airavata.task.base.AbstractTask;
import org.apache.airavata.task.base.AiravataTask;
import org.apache.airavata.task.cancel.CancelCompletingTask;
import org.apache.airavata.task.cancel.RemoteJobCancellationTask;
import org.apache.airavata.task.cancel.WorkflowCancellationTask;
import org.apache.airavata.task.completing.CompletingTask;
import org.apache.airavata.task.factory.DaprTaskFactory;
import org.apache.airavata.telemetry.CounterMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "airavata.services.prewm", name = "enabled", havingValue = "true")
public class PreWorkflowManager extends WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(PreWorkflowManager.class);
    private static final CounterMetric prewfCounter = new CounterMetric("pre_wf_counter");

    private final AiravataServerProperties properties;
    private final org.apache.airavata.task.factory.TaskFactory taskFactory;
    private final org.springframework.context.ApplicationContext applicationContext;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final DaprMessagingFactory messagingFactory;
    private Subscriber subscriber;

    public PreWorkflowManager(
            AiravataServerProperties properties,
            org.apache.airavata.task.factory.TaskFactory taskFactory,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            DaprMessagingFactory messagingFactory) {
        // Default values, will be updated in @PostConstruct
        super("pre-workflow-manager", false, registryService, messagingFactory);
        this.properties = properties;
        this.taskFactory = taskFactory;
        this.applicationContext = applicationContext;
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
    }

    @jakarta.annotation.PostConstruct
    public void initWorkflowManager() {
        if (properties != null) {
            this.workflowManagerName = this.getClass().getSimpleName();
            this.loadBalanceClusters = properties.services().prewm().loadBalanceClusters();
        }
    }

    @Override
    public String getServerName() {
        return "Pre Workflow Manager";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 20; // Start after participant
    }

    @Override
    protected void doStart() throws Exception {
        try {
            super.initComponents();
        } catch (Exception e) {
            logger.warn(
                    "Failed to initialize workflow manager components (Zookeeper may not be available): {}. "
                            + "Workflow management features will be unavailable until Zookeeper is accessible.",
                    e.getMessage());
            // Allow server to start even if workflow manager components can't be initialized
        }
        initLaunchSubscriber();
    }

    @Override
    protected void doStop() throws Exception {
        // Dapr subscriptions are in-memory; no explicit close needed.
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && subscriber != null;
    }

    private void initLaunchSubscriber() throws AiravataException {
        List<String> routingKeys = new ArrayList<>();
        routingKeys.add(org.apache.airavata.dapr.messaging.DaprTopics.PROCESS);
        // Create handler first (subscriber will be set after creation)
        ProcessLaunchMessageHandler handler = new ProcessLaunchMessageHandler(this, null);
        // Create subscriber with handler
        this.subscriber = messagingFactory.getSubscriber(handler, routingKeys, Type.PROCESS_LAUNCH);
        // Set subscriber reference in handler for acks
        handler.setSubscriber(this.subscriber);
    }

    String createAndLaunchPreWorkflow(String processId, boolean forceRun) throws Exception {

        prewfCounter.inc();
        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        ExperimentModel experimentModel;
        DaprTaskFactory taskFactory;
        try {
            processModel = registryService.getProcess(processId);
            experimentModel = registryService.getExperiment(processModel.getExperimentId());
            ComputeResourceType resourceType = registryService
                    .getGroupComputeResourcePreference(
                            processModel.getComputeResourceId(), processModel.getGroupResourceProfileId())
                    .getResourceType();
            taskFactory = this.taskFactory.getFactory(resourceType);
            logger.info("Initialized task factory for resource type {} for process {}", resourceType, processId);

        } catch (RegistryException e) {
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
            CompletingTask completingTask = applicationContext.getBean(CompletingTask.class);
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

        // TODO: Replace with Dapr Workflow - ProcessPreWorkflow
        // String workflowName = daprWorkflowClient.scheduleNewWorkflow(ProcessPreWorkflow.class, event);
        // For now, generate a workflow name for registration using standardized naming
        String workflowName = org.apache.airavata.dapr.workflow.WorkflowNaming.preWorkflow(processId);
        registerWorkflowForProcess(processId, workflowName, org.apache.airavata.dapr.workflow.WorkflowNaming.TYPE_PRE);

        return workflowName;
    }

    String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {

        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        GroupComputeResourcePreference gcrPref;

        try {
            processModel = registryService.getProcess(processId);
            gcrPref = registryService.getGroupComputeResourcePreference(
                    processModel.getComputeResourceId(), processModel.getGroupResourceProfileId());

        } catch (RegistryException e) {
            logger.error("Failed to fetch process from registry associated with process id " + processId, e);
            throw new Exception("Failed to fetch process from registry associated with process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();
        final List<AbstractTask> allTasks = new ArrayList<>();

        Optional<List<String>> workflowsOpt = Optional.ofNullable(processModel.getProcessWorkflows())
                .map(wfs -> wfs.stream().map(ProcessWorkflow::getWorkflowId).toList());

        if (workflowsOpt.isPresent()) {
            List<String> workflows = workflowsOpt.get();
            if (workflows.size() > 0) {
                for (String wf : workflows) {
                    logger.info("Creating cancellation task for workflow " + wf + " of process " + processId);
                    WorkflowCancellationTask wfct = applicationContext.getBean(WorkflowCancellationTask.class);
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

        if (gcrPref.getResourceType() == ComputeResourceType.SLURM) {
            logger.info(
                    "Skipping cancel workflow for process {} as it is not a SLURM process, resource type: {}",
                    processId,
                    gcrPref.getResourceType());

            RemoteJobCancellationTask rjct = applicationContext.getBean(RemoteJobCancellationTask.class);
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

        CancelCompletingTask cct = applicationContext.getBean(CancelCompletingTask.class);
        cct.setTaskId(UUID.randomUUID().toString());
        cct.setExperimentId(experimentId);
        cct.setProcessId(processId);
        cct.setGatewayId(gateway);
        cct.setSkipAllStatusPublish(true);

        if (!allTasks.isEmpty()) {
            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(cct.getTaskId(), cct));
        }
        allTasks.add(cct);

        // TODO: Replace with Dapr Workflow - ProcessCancelWorkflow
        // String workflow = daprWorkflowClient.scheduleNewWorkflow(ProcessCancelWorkflow.class, event);
        // For now, generate a workflow name using standardized naming
        String workflow = org.apache.airavata.dapr.workflow.WorkflowNaming.cancelWorkflow(processId);
        logger.info("Started launching workflow {} to cancel process {}", workflow, processId);
        return workflow;
    }
}
