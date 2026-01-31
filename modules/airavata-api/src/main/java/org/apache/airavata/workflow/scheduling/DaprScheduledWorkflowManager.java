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
package org.apache.airavata.workflow.scheduling;

import io.dapr.workflows.client.DaprWorkflowClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.ScheduledWorkflowInput;
import org.apache.airavata.workflow.monitoring.cluster.ClusterStatusMonitorWorkflow;
import org.apache.airavata.workflow.monitoring.compute.ComputeMonitorWorkflow;
import org.apache.airavata.workflow.monitoring.data.DataAnalyzerWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

/**
 * Manager for Dapr scheduled workflows.
 *
 * <p>Starts and manages scheduled workflows that replace Quartz-based schedulers.
 * Each scheduled task runs as a perpetual Dapr workflow with timer-based intervals.
 *
 * <p>Requires both conditions to be met:
 * <ul>
 *   <li>{@code airavata.services.controller.enabled=true} - Controller service must be enabled</li>
 *   <li>{@code airavata.dapr.enabled=true} - Dapr must be enabled (sidecar available)</li>
 * </ul>
 */
@Component
@Conditional(DaprScheduledWorkflowManager.DaprScheduledWorkflowCondition.class)
public class DaprScheduledWorkflowManager {

    /**
     * Condition that requires both controller service and Dapr to be enabled.
     */
    static class DaprScheduledWorkflowCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var env = context.getEnvironment();
            boolean controllerEnabled =
                    "true".equalsIgnoreCase(env.getProperty("airavata.services.controller.enabled", "false"));
            boolean daprEnabled = "true".equalsIgnoreCase(env.getProperty("airavata.dapr.enabled", "false"));
            return controllerEnabled && daprEnabled;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DaprScheduledWorkflowManager.class);

    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;
    private final List<String> workflowInstanceIds = new ArrayList<>();

    public DaprScheduledWorkflowManager(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @PostConstruct
    public void startScheduledWorkflows() {
        try {
            DaprWorkflowClient workflowClient = applicationContext.getBean(DaprWorkflowClient.class);
            if (workflowClient == null) {
                logger.warn("DaprWorkflowClient not available, skipping scheduled workflow startup");
                return;
            }

            // Start ProcessScanner workflows
            if (properties.services().scheduler().rescheduler().enabled()) {
                startProcessScannerWorkflows(workflowClient);
            }

            // Start DataAnalyzer workflows
            if (properties.services().parser().enabled()) {
                startDataAnalyzerWorkflows(workflowClient);
            }

            // Start ComputeMonitor workflows
            if (properties.services().monitor().compute().enabled()) {
                startComputeMonitorWorkflows(workflowClient);
            }

            // Start ClusterStatusMonitor workflow
            if (properties.services().monitor().compute().enabled()) {
                startClusterStatusMonitorWorkflow(workflowClient);
            }

            logger.info("Started {} scheduled workflows", workflowInstanceIds.size());
        } catch (Exception e) {
            logger.error("Failed to start scheduled workflows", e);
        }
    }

    @PreDestroy
    public void stopScheduledWorkflows() {
        try {
            DaprWorkflowClient workflowClient = applicationContext.getBean(DaprWorkflowClient.class);
            if (workflowClient == null) {
                return;
            }

            for (String instanceId : workflowInstanceIds) {
                try {
                    workflowClient.terminateWorkflow(instanceId, "Application shutdown");
                    logger.debug("Terminated workflow instance: {}", instanceId);
                } catch (Exception e) {
                    logger.warn("Failed to terminate workflow instance {}: {}", instanceId, e.getMessage());
                }
            }
            workflowInstanceIds.clear();
            logger.info("Stopped all scheduled workflows");
        } catch (Exception e) {
            logger.error("Error stopping scheduled workflows", e);
        }
    }

    private void startProcessScannerWorkflows(DaprWorkflowClient workflowClient) {
        final int parallelJobs = properties.services().scheduler().clusterScanningParallelJobs();
        final double scanningInterval = properties.services().scheduler().jobScanningInterval();

        for (int i = 0; i < parallelJobs; i++) {
            ScheduledWorkflowInput input = new ScheduledWorkflowInput((int) scanningInterval, i, parallelJobs);

            try {
                String instanceId = workflowClient.scheduleNewWorkflow(ProcessScannerWorkflow.class, input);
                workflowInstanceIds.add(instanceId);
                logger.info(
                        "Started ProcessScannerWorkflow instance {} with interval {}s, jobId {}",
                        instanceId,
                        (int) scanningInterval,
                        i);
            } catch (Exception e) {
                logger.error("Failed to start ProcessScannerWorkflow for jobId {}: {}", i, e.getMessage(), e);
            }
        }
    }

    private void startDataAnalyzerWorkflows(DaprWorkflowClient workflowClient) {
        final int parallelJobs = properties.services().parser().scanningParallelJobs();
        final double scanningInterval = properties.services().parser().scanningInterval();

        for (int i = 0; i < parallelJobs; i++) {
            ScheduledWorkflowInput input = new ScheduledWorkflowInput((int) scanningInterval, i, parallelJobs);

            try {
                String instanceId = workflowClient.scheduleNewWorkflow(DataAnalyzerWorkflow.class, input);
                workflowInstanceIds.add(instanceId);
                logger.info(
                        "Started DataAnalyzerWorkflow instance {} with interval {}s, jobId {}",
                        instanceId,
                        (int) scanningInterval,
                        i);
            } catch (Exception e) {
                logger.error("Failed to start DataAnalyzerWorkflow for jobId {}: {}", i, e.getMessage(), e);
            }
        }
    }

    private void startComputeMonitorWorkflows(DaprWorkflowClient workflowClient) {
        // Note: These properties are not in AiravataServerProperties yet, using defaults
        // Add to AiravataServerProperties if needed
        final int parallelJobs = 1; // default
        final double scanningInterval = 1800; // default in seconds (30 minutes)

        for (int i = 0; i < parallelJobs; i++) {
            ScheduledWorkflowInput input = new ScheduledWorkflowInput((int) scanningInterval, i, parallelJobs);

            try {
                String instanceId = workflowClient.scheduleNewWorkflow(ComputeMonitorWorkflow.class, input);
                workflowInstanceIds.add(instanceId);
                logger.info(
                        "Started ComputeMonitorWorkflow instance {} with interval {}s, jobId {}",
                        instanceId,
                        (int) scanningInterval,
                        i);
            } catch (Exception e) {
                logger.error("Failed to start ComputeMonitorWorkflow for jobId {}: {}", i, e.getMessage(), e);
            }
        }
    }

    private void startClusterStatusMonitorWorkflow(DaprWorkflowClient workflowClient) {
        final int repeatTime = properties.services().monitor().compute().clusterCheckRepeatTime();
        ScheduledWorkflowInput input = new ScheduledWorkflowInput(repeatTime, 0, 1);

        try {
            String instanceId = workflowClient.scheduleNewWorkflow(ClusterStatusMonitorWorkflow.class, input);
            workflowInstanceIds.add(instanceId);
            logger.info("Started ClusterStatusMonitorWorkflow instance {} with interval {}s", instanceId, repeatTime);
        } catch (Exception e) {
            logger.error("Failed to start ClusterStatusMonitorWorkflow: {}", e.getMessage(), e);
        }
    }
}
