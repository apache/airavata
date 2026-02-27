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
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.dag.DAGTemplates;
import org.apache.airavata.execution.dag.DagTask;
import org.apache.airavata.execution.dag.ProcessDAG;
import org.apache.airavata.execution.dag.RetryTier;
import org.apache.airavata.execution.dag.TaskContext;
import org.apache.airavata.execution.dag.TaskContextFactory;
import org.apache.airavata.execution.dag.TaskInterceptor;
import org.apache.airavata.execution.dag.TaskNode;
import org.apache.airavata.execution.orchestration.ProcessResourceResolver;
import org.apache.airavata.execution.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Temporal durable workflows for process execution.
 *
 * <p>Each workflow walks a {@link ProcessDAG} deterministically, calling
 * {@link Activities#executeDagNode} for each node as a separate activity
 * with tier-specific retry options. The DAG defines task order and
 * success/failure branching; Temporal handles retries and durability.
 */
public class ProcessActivity {

    public static final String TASK_QUEUE = "airavata-workflows";

    // -------------------------------------------------------------------------
    // Workflow contracts
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

    public record PreInput(String processId, String experimentId, String gatewayId) implements Serializable {}

    public record PostInput(String processId, String experimentId, String gatewayId, boolean forceRun)
            implements Serializable {}

    public record CancelInput(String processId, String experimentId, String gatewayId) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity return type
    // -------------------------------------------------------------------------

    public record NodeResult(String message, Map<String, String> output) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity interface — one method per node, not per DAG
    // -------------------------------------------------------------------------

    @ActivityInterface
    public interface Activities {
        @ActivityMethod
        ComputeResourceType resolveResourceType(String processId);

        @ActivityMethod
        NodeResult executeDagNode(
                String processId,
                String gatewayId,
                String nodeId,
                String taskBeanName,
                Map<String, String> dagState,
                Map<String, String> nodeMetadata);
    }

    // -------------------------------------------------------------------------
    // DAG walking helper (deterministic — safe for workflow code)
    // -------------------------------------------------------------------------

    private static String walkDag(
            ProcessDAG dag, String processId, String gatewayId, Map<RetryTier, Activities> tierStubs) {
        String currentNodeId = dag.entryNodeId();
        Map<String, String> dagState = new HashMap<>();
        String lastMessage = null;

        while (currentNodeId != null) {
            TaskNode node = dag.getNode(currentNodeId);
            if (node == null) {
                throw ApplicationFailure.newFailure("DAG node '" + currentNodeId + "' not found", "DAG_ERROR");
            }

            RetryTier tier = RetryTier.valueOf(node.metadata().getOrDefault("retryTier", "INFRASTRUCTURE"));
            Activities activities = tierStubs.get(tier);

            try {
                NodeResult result = activities.executeDagNode(
                        processId, gatewayId, node.id(), node.taskBeanName(), dagState, node.metadata());
                dagState.putAll(result.output());
                lastMessage = result.message();
                currentNodeId = node.onSuccess();
            } catch (ActivityFailure e) {
                lastMessage = "Node '" + node.id() + "' failed after retries";
                currentNodeId = node.onFailure();
                if (currentNodeId == null) {
                    throw e;
                }
            }
        }

        return lastMessage != null ? lastMessage : "DAG completed for process " + processId;
    }

    private static Map<RetryTier, Activities> buildTierStubs() {
        Map<RetryTier, Activities> stubs = new HashMap<>();
        for (RetryTier tier : RetryTier.values()) {
            stubs.put(tier, Workflow.newActivityStub(Activities.class, tier.activityOptions()));
        }
        return stubs;
    }

    private static Activities buildSetupStub() {
        return Workflow.newActivityStub(
                Activities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(30))
                        .setRetryOptions(
                                RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .build());
    }

    // -------------------------------------------------------------------------
    // Workflow implementations
    // -------------------------------------------------------------------------

    /**
     * Shared logic: resolve resource type → select DAG template → walk DAG.
     * All three workflow phases (pre, post, cancel) use this pattern.
     */
    private static String resolveAndWalk(
            Activities setup,
            Map<RetryTier, Activities> tierStubs,
            String processId,
            String gatewayId,
            java.util.function.Function<ComputeResourceType, ProcessDAG> dagSelector) {
        ComputeResourceType type = setup.resolveResourceType(processId);
        ProcessDAG dag = dagSelector.apply(type);
        return walkDag(dag, processId, gatewayId, tierStubs);
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PreWfImpl implements PreWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(PreInput input) {
            return resolveAndWalk(setup, tierStubs, input.processId(), input.gatewayId(), DAGTemplates::preDag);
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class PostWfImpl implements PostWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(PostInput input) {
            return resolveAndWalk(setup, tierStubs, input.processId(), input.gatewayId(), DAGTemplates::postDag);
        }
    }

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class CancelWfImpl implements CancelWf {
        private final Activities setup = buildSetupStub();
        private final Map<RetryTier, Activities> tierStubs = buildTierStubs();

        @Override
        public String execute(CancelInput input) {
            return resolveAndWalk(setup, tierStubs, input.processId(), input.gatewayId(), DAGTemplates::cancelDag);
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

        private final ApplicationContext applicationContext;
        private final TaskContextFactory contextFactory;
        private final List<TaskInterceptor> interceptors;
        private final ProcessService processService;
        private final ProcessResourceResolver resourceResolver;

        public ActivitiesImpl(
                ApplicationContext applicationContext,
                TaskContextFactory contextFactory,
                List<TaskInterceptor> interceptors,
                ProcessService processService,
                ProcessResourceResolver resourceResolver) {
            this.applicationContext = applicationContext;
            this.contextFactory = contextFactory;
            this.interceptors = interceptors;
            this.processService = processService;
            this.resourceResolver = resourceResolver;
        }

        @Override
        public ComputeResourceType resolveResourceType(String processId) {
            try {
                var processModel = processService.getProcess(processId);
                return resourceResolver.getComputeResourceType(processModel);
            } catch (Exception e) {
                logger.warn("Failed to resolve resource type for process {}, defaulting to SLURM", processId, e);
            }
            return ComputeResourceType.SLURM;
        }

        @Override
        public NodeResult executeDagNode(
                String processId,
                String gatewayId,
                String nodeId,
                String taskBeanName,
                Map<String, String> dagState,
                Map<String, String> nodeMetadata) {
            String taskId = UUID.randomUUID().toString();
            TaskContext context = contextFactory.buildContext(processId, gatewayId, taskId);
            context.getDagState().putAll(dagState);

            TaskNode node = new TaskNode(nodeId, taskBeanName, null, null, nodeMetadata);
            DagTask task = applicationContext.getBean(taskBeanName, DagTask.class);

            logger.info("Executing node '{}' (bean: {}) for process {}", nodeId, taskBeanName, processId);

            for (TaskInterceptor interceptor : interceptors) {
                interceptor.before(context, node);
            }

            DagTaskResult result;
            try {
                result = task.execute(context);
            } catch (Exception e) {
                logger.error("Uncaught exception in node '{}' for process {}", nodeId, processId, e);
                result = new DagTaskResult.Failure("Uncaught exception: " + e.getMessage(), false, e);
            }

            return switch (result) {
                case DagTaskResult.Success success -> {
                    logger.info("Node '{}' succeeded: {}", nodeId, success.message());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterSuccess(context, node, success);
                    }
                    yield new NodeResult(success.message(), success.output());
                }
                case DagTaskResult.Failure failure -> {
                    logger.warn("Node '{}' failed: {} (fatal={})", nodeId, failure.reason(), failure.fatal());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterFailure(context, node, failure);
                    }
                    if (failure.fatal()) {
                        throw ApplicationFailure.newNonRetryableFailure(failure.reason(), "FATAL_TASK_FAILURE");
                    }
                    throw ApplicationFailure.newFailure(failure.reason(), "TASK_FAILURE");
                }
            };
        }
    }
}
