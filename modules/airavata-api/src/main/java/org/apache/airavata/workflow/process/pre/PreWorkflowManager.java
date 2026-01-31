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
package org.apache.airavata.workflow.process.pre;

import io.dapr.workflows.client.DaprWorkflowClient;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.airavata.workflow.common.WorkflowManager;
import org.apache.airavata.workflow.process.cancel.ProcessCancelWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MessagingFactory messagingFactory;

    @Autowired(required = false)
    private DaprWorkflowClient workflowClient;

    public PreWorkflowManager(
            AiravataServerProperties properties,
            org.apache.airavata.task.factory.TaskFactory taskFactory,
            org.springframework.context.ApplicationContext applicationContext,
            org.apache.airavata.service.registry.RegistryService registryService,
            MessagingFactory messagingFactory,
            ProcessStatusUpdater statusUpdateHelper) {
        // Default values, will be updated in @PostConstruct
        super("pre-workflow-manager", false, registryService, messagingFactory, statusUpdateHelper);
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
                    "Failed to initialize workflow manager components: {}. "
                            + "Workflow management features will be unavailable.",
                    e.getMessage());
            // Allow server to start even if workflow manager components can't be initialized
        }
    }

    @Override
    protected void doStop() throws Exception {
        // Subscriptions are in-memory; no explicit close needed.
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    public String createAndLaunchPreWorkflow(String processId, boolean forceRun) throws Exception {

        prewfCounter.inc();
        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        ExperimentModel experimentModel;
        try {
            processModel = registryService.getProcess(processId);
            experimentModel = registryService.getExperiment(processModel.getExperimentId());
        } catch (RegistryException e) {
            logger.error(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
            throw new Exception(
                    "Failed to fetch experiment or process from registry associated with process id " + processId, e);
        }

        // Use durable workflow instead of building task chains
        if (workflowClient != null) {
            try {
                // Create ProcessSubmitEvent for the workflow
                org.apache.airavata.common.model.ProcessSubmitEvent event =
                        new org.apache.airavata.common.model.ProcessSubmitEvent(
                                processId,
                                experimentModel.getGatewayId(),
                                experimentModel.getExperimentId(),
                                null); // tokenId can be null for internal workflows

                // Schedule the workflow (returns instance ID)
                logger.info("Scheduling ProcessPreWorkflow for process {}", processId);
                String workflowInstanceId = workflowClient.scheduleNewWorkflow(ProcessPreWorkflow.class, event);

                // Register the workflow instance ID with the process
                registerWorkflowForProcess(
                        processId,
                        workflowInstanceId,
                        org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.TYPE_PRE);

                updateProcessStatus(
                        processId,
                        experimentModel.getExperimentId(),
                        experimentModel.getGatewayId(),
                        org.apache.airavata.common.model.ProcessState.STARTED);
                logger.info(
                        "Successfully scheduled ProcessPreWorkflow {} for process {}", workflowInstanceId, processId);
                return workflowInstanceId;

            } catch (Exception e) {
                logger.error("Failed to schedule ProcessPreWorkflow for process {}", processId, e);
                throw new Exception("Failed to schedule ProcessPreWorkflow for process " + processId, e);
            }
        } else {
            // Fallback: log warning if workflow client is not available
            logger.warn(
                    "Workflow client not available; cannot launch workflow for process {}. "
                            + "Enable airavata.services.controller.enabled=true",
                    processId);
            // Still register workflow name for compatibility
            String workflowName =
                    org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.preWorkflow(processId);
            registerWorkflowForProcess(
                    processId,
                    workflowName,
                    org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.TYPE_PRE);
            return workflowName;
        }
    }

    public String createAndLaunchCancelWorkflow(String processId, String gateway) throws Exception {

        RegistryService registryService = this.registryService;

        ProcessModel processModel;
        try {
            processModel = registryService.getProcess(processId);
        } catch (RegistryException e) {
            logger.error("Failed to fetch process from registry associated with process id " + processId, e);
            throw new Exception("Failed to fetch process from registry associated with process id " + processId, e);
        }

        String experimentId = processModel.getExperimentId();

        // Use durable workflow instead of building task chains
        if (workflowClient != null) {
            try {
                // Create ProcessTerminateEvent for the workflow
                org.apache.airavata.common.model.ProcessTerminateEvent event =
                        new org.apache.airavata.common.model.ProcessTerminateEvent(
                                processId, gateway, experimentId, null); // tokenId can be null for internal workflows

                // Schedule the workflow (returns instance ID)
                logger.info("Scheduling ProcessCancelWorkflow for process {}", processId);
                String workflowInstanceId = workflowClient.scheduleNewWorkflow(ProcessCancelWorkflow.class, event);

                // Register the workflow instance ID with the process
                registerWorkflowForProcess(
                        processId,
                        workflowInstanceId,
                        org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.TYPE_CANCEL);

                logger.info(
                        "Successfully scheduled ProcessCancelWorkflow {} for process {}",
                        workflowInstanceId,
                        processId);
                return workflowInstanceId;

            } catch (Exception e) {
                logger.error("Failed to schedule ProcessCancelWorkflow for process {}", processId, e);
                throw new Exception("Failed to schedule ProcessCancelWorkflow for process " + processId, e);
            }
        } else {
            // Fallback: log warning if workflow client is not available
            logger.warn(
                    "Workflow client not available; cannot launch cancel workflow for process {}. "
                            + "Enable airavata.services.controller.enabled=true",
                    processId);
            // Still register workflow name for compatibility
            String workflow =
                    org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.cancelWorkflow(processId);
            logger.info("Started launching workflow {} to cancel process {}", workflow, processId);
            return workflow;
        }
    }
}
