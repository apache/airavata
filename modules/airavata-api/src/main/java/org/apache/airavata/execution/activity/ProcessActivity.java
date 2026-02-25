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
package org.apache.airavata.execution.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.ActivityImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.Serializable;
import java.time.Duration;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.execution.dag.DAGTemplates;
import org.apache.airavata.execution.dag.ProcessDAG;
import org.apache.airavata.execution.dag.ProcessDAGEngine;
import org.apache.airavata.execution.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporal durable workflows for process execution.
 *
 * <p>Contains three workflow entry points for the full process lifecycle:
 * <ul>
 *   <li>{@link PreWf} — Pre-execution: provisioning, input staging, job submission</li>
 *   <li>{@link PostWf} — Post-execution: monitoring, output staging, archival, deprovisioning</li>
 *   <li>{@link CancelWf} — Cancellation: remote job cancellation and cleanup</li>
 * </ul>
 *
 * <p>Each workflow delegates to a single Temporal activity that runs a
 * {@link ProcessDAG} via the {@link ProcessDAGEngine}. The DAG defines the
 * task execution order with success/failure edges; the engine walks the DAG
 * and applies cross-cutting interceptors (status publishing, error recording,
 * metrics, logging).
 */
public class ProcessActivity {

    public static final String TASK_QUEUE = "airavata-workflows";

    // -------------------------------------------------------------------------
    // Workflow contracts (one @WorkflowMethod per interface — Temporal requirement)
    // -------------------------------------------------------------------------

    @WorkflowInterface
    public interface PreWf {
        @WorkflowMethod
        String execute(PreInput input);
    }

    @WorkflowInterface
    public interface PostWf {
        @WorkflowMethod
        String execute(PostInput input);
    }

    @WorkflowInterface
    public interface CancelWf {
        @WorkflowMethod
        String execute(CancelInput input);
    }

    // -------------------------------------------------------------------------
    // Input records
    // -------------------------------------------------------------------------

    public record PreInput(String processId, String experimentId, String gatewayId, String tokenId)
            implements Serializable {}

    public record PostInput(String processId, String experimentId, String gatewayId, boolean forceRun)
            implements Serializable {}

    public record CancelInput(String processId, String experimentId, String gatewayId) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity interface — one activity per DAG phase
    // -------------------------------------------------------------------------

    @ActivityInterface
    public interface Activities {
        @ActivityMethod
        String executePreDag(String processId, String gatewayId);

        @ActivityMethod
        String executePostDag(String processId, String gatewayId);

        @ActivityMethod
        String executeCancelDag(String processId, String gatewayId);
    }

    // -------------------------------------------------------------------------
    // Workflow implementations (deterministic — no DB, no logging, no I/O)
    // -------------------------------------------------------------------------

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PreWfImpl implements PreWf {

        private final Activities activities = Workflow.newActivityStub(
                Activities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(30))
                        .setRetryOptions(
                                RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .build());

        @Override
        public String execute(PreInput input) {
            return activities.executePreDag(input.processId(), input.gatewayId());
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PostWfImpl implements PostWf {

        private final Activities activities = Workflow.newActivityStub(
                Activities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(30))
                        .setRetryOptions(
                                RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .build());

        @Override
        public String execute(PostInput input) {
            return activities.executePostDag(input.processId(), input.gatewayId());
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class CancelWfImpl implements CancelWf {

        private final Activities activities = Workflow.newActivityStub(
                Activities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(10))
                        .setRetryOptions(
                                RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .build());

        @Override
        public String execute(CancelInput input) {
            return activities.executeCancelDag(input.processId(), input.gatewayId());
        }
    }

    // -------------------------------------------------------------------------
    // Activity implementation (Spring-managed, full DI)
    // -------------------------------------------------------------------------

    @ConditionalOnParticipant
    @Component
    @ActivityImpl(taskQueues = TASK_QUEUE)
    public static class ActivitiesImpl implements Activities {

        private static final Logger logger = LoggerFactory.getLogger(ActivitiesImpl.class);

        private final ProcessDAGEngine dagEngine;
        private final ProcessService processService;
        private final ResourceService resourceService;

        public ActivitiesImpl(
                ProcessDAGEngine dagEngine,
                ProcessService processService,
                ResourceService resourceService) {
            this.dagEngine = dagEngine;
            this.processService = processService;
            this.resourceService = resourceService;
        }

        @Override
        public String executePreDag(String processId, String gatewayId) {
            logger.info("Executing pre-DAG for process {}", processId);
            ComputeResourceType type = resolveResourceType(processId);
            ProcessDAG dag = DAGTemplates.preDag(type);
            return dagEngine.execute(dag, processId, gatewayId);
        }

        @Override
        public String executePostDag(String processId, String gatewayId) {
            logger.info("Executing post-DAG for process {}", processId);
            ComputeResourceType type = resolveResourceType(processId);
            ProcessDAG dag = DAGTemplates.postDag(type);
            return dagEngine.execute(dag, processId, gatewayId);
        }

        @Override
        public String executeCancelDag(String processId, String gatewayId) {
            logger.info("Executing cancel-DAG for process {}", processId);
            ComputeResourceType type = resolveResourceType(processId);
            ProcessDAG dag = DAGTemplates.cancelDag(type);
            return dagEngine.execute(dag, processId, gatewayId);
        }

        private ComputeResourceType resolveResourceType(String processId) {
            try {
                var processModel = processService.getProcess(processId);
                Resource resource = resourceService.getResource(processModel.getResourceId());
                if (resource != null && resource.getCapabilities() != null
                        && resource.getCapabilities().getCompute() != null) {
                    return resource.getCapabilities().getCompute().getComputeResourceType();
                }
            } catch (Exception e) {
                logger.warn("Failed to resolve resource type for process {}, defaulting to SLURM", processId, e);
            }
            return ComputeResourceType.SLURM;
        }
    }
}
