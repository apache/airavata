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
package org.apache.airavata.orchestrator.internal.workflow;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.airavata.activities.monitoring.cluster.ClusterStatusMonitorActivity;
import org.apache.airavata.activities.monitoring.compute.ComputeMonitorActivity;
import org.apache.airavata.activities.monitoring.data.DataAnalyzerActivity;
import org.apache.airavata.activities.parsing.DataParsingActivity;
import org.apache.airavata.activities.process.cancel.CancelCompletingActivity;
import org.apache.airavata.activities.process.cancel.RemoteJobCancellationActivity;
import org.apache.airavata.activities.process.cancel.WorkflowCancellationActivity;
import org.apache.airavata.activities.process.post.ArchiveActivity;
import org.apache.airavata.activities.process.post.JobVerificationActivity;
import org.apache.airavata.activities.process.post.OutputDataStagingActivity;
import org.apache.airavata.activities.process.post.ParsingTriggeringActivity;
import org.apache.airavata.activities.process.pre.EnvSetupActivity;
import org.apache.airavata.activities.process.pre.InputDataStagingActivity;
import org.apache.airavata.activities.process.pre.JobSubmissionActivity;
import org.apache.airavata.activities.scheduling.ProcessScannerActivity;
import org.apache.airavata.activities.shared.CompletingActivity;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.workflow.monitoring.cluster.ClusterStatusMonitorWorkflow;
import org.apache.airavata.workflow.monitoring.compute.ComputeMonitorWorkflow;
import org.apache.airavata.workflow.monitoring.data.DataAnalyzerWorkflow;
import org.apache.airavata.workflow.process.cancel.ProcessCancelWorkflow;
import org.apache.airavata.workflow.process.parsing.ParsingWorkflow;
import org.apache.airavata.workflow.process.post.ProcessPostWorkflow;
import org.apache.airavata.workflow.process.pre.ProcessPreWorkflow;
import org.apache.airavata.workflow.scheduling.ProcessScannerWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Configuration for Dapr Workflow Runtime.
 *
 * <p>Requires both conditions:
 * <ul>
 *   <li>{@code airavata.services.controller.enabled=true}</li>
 *   <li>{@code airavata.dapr.enabled=true}</li>
 * </ul>
 */
@Configuration
@Conditional(DaprWorkflowRuntimeConfig.DaprWorkflowCondition.class)
public class DaprWorkflowRuntimeConfig {

    static class DaprWorkflowCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var env = context.getEnvironment();
            return "true".equalsIgnoreCase(env.getProperty("airavata.services.controller.enabled", "false"))
                    && "true".equalsIgnoreCase(env.getProperty("airavata.dapr.enabled", "false"));
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DaprWorkflowRuntimeConfig.class);

    private final ApplicationContext applicationContext;
    private WorkflowRuntime workflowRuntime;

    public DaprWorkflowRuntimeConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void initialize() {
        WorkflowRuntimeHolder.initialize(applicationContext);

        var builder = new WorkflowRuntimeBuilder();

        // Register workflows
        builder.registerWorkflow(ProcessPreWorkflow.class);
        builder.registerWorkflow(ProcessPostWorkflow.class);
        builder.registerWorkflow(ProcessCancelWorkflow.class);
        builder.registerWorkflow(ParsingWorkflow.class);
        builder.registerWorkflow(ProcessScannerWorkflow.class);
        builder.registerWorkflow(DataAnalyzerWorkflow.class);
        builder.registerWorkflow(ComputeMonitorWorkflow.class);
        builder.registerWorkflow(ClusterStatusMonitorWorkflow.class);

        // Register activities
        builder.registerActivity(EnvSetupActivity.class);
        builder.registerActivity(InputDataStagingActivity.class);
        builder.registerActivity(JobSubmissionActivity.class);
        builder.registerActivity(OutputDataStagingActivity.class);
        builder.registerActivity(ArchiveActivity.class);
        builder.registerActivity(JobVerificationActivity.class);
        builder.registerActivity(CompletingActivity.class);
        builder.registerActivity(ParsingTriggeringActivity.class);
        builder.registerActivity(DataParsingActivity.class);
        builder.registerActivity(WorkflowCancellationActivity.class);
        builder.registerActivity(RemoteJobCancellationActivity.class);
        builder.registerActivity(CancelCompletingActivity.class);
        builder.registerActivity(ProcessScannerActivity.class);
        builder.registerActivity(DataAnalyzerActivity.class);
        builder.registerActivity(ComputeMonitorActivity.class);
        builder.registerActivity(ClusterStatusMonitorActivity.class);

        workflowRuntime = builder.build();
        workflowRuntime.start(false);

        logger.info("Dapr Workflow Runtime started");
    }

    @PreDestroy
    public void shutdown() {
        if (workflowRuntime != null) {
            try {
                workflowRuntime.close();
                logger.info("Dapr Workflow Runtime shut down");
            } catch (Exception e) {
                logger.error("Error shutting down Dapr Workflow Runtime", e);
            }
        }
    }

    @Bean
    public WorkflowRuntime workflowRuntime() {
        return workflowRuntime;
    }
}
