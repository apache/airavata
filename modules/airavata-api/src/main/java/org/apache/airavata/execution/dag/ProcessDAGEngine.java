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
package org.apache.airavata.execution.dag;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Walks a {@link ProcessDAG}, executing each {@link DagTask} node in sequence
 * and following success/failure edges based on the task result.
 *
 * <p>Cross-cutting concerns are applied via ordered {@link TaskInterceptor}s
 * that run before and after each task. The engine builds a {@link TaskContext}
 * via the {@link TaskContextFactory} and passes it to every task in the DAG.
 *
 * <p>This engine runs inside a single Temporal activity. The Temporal workflow
 * (PreWf, PostWf, CancelWf) selects the appropriate DAG and invokes this engine
 * as a single activity call.
 */
@Component
@ConditionalOnParticipant
public class ProcessDAGEngine {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDAGEngine.class);

    private final ApplicationContext applicationContext;
    private final TaskContextFactory contextFactory;
    private final List<TaskInterceptor> interceptors;

    public ProcessDAGEngine(
            ApplicationContext applicationContext,
            TaskContextFactory contextFactory,
            List<TaskInterceptor> interceptors) {
        this.applicationContext = applicationContext;
        this.contextFactory = contextFactory;
        this.interceptors = interceptors;
    }

    /**
     * Executes a DAG for the given process.
     *
     * @param dag          the process DAG defining task order and branching
     * @param processId    the process to execute
     * @param gatewayId    the owning gateway
     * @return a summary message describing the outcome
     */
    public String execute(ProcessDAG dag, String processId, String gatewayId) {
        String taskId = UUID.randomUUID().toString();
        TaskContext context = contextFactory.buildContext(processId, gatewayId, taskId);

        String currentNodeId = dag.entryNodeId();
        String lastMessage = null;

        logger.info("Starting DAG execution for process {} at node '{}'", processId, currentNodeId);

        while (currentNodeId != null) {
            TaskNode node = dag.getNode(currentNodeId);
            if (node == null) {
                throw new IllegalStateException("DAG node '" + currentNodeId + "' not found");
            }

            DagTask task = applicationContext.getBean(node.taskBeanName(), DagTask.class);

            logger.info("Executing node '{}' (bean: {}) for process {}", node.id(), node.taskBeanName(), processId);

            for (TaskInterceptor interceptor : interceptors) {
                interceptor.before(context, node);
            }

            DagTaskResult result;
            try {
                result = task.execute(context);
            } catch (Exception e) {
                logger.error("Uncaught exception in node '{}' for process {}", node.id(), processId, e);
                result = new DagTaskResult.Failure("Uncaught exception: " + e.getMessage(), false, e);
            }

            switch (result) {
                case DagTaskResult.Success success -> {
                    logger.info("Node '{}' succeeded: {}", node.id(), success.message());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterSuccess(context, node, success);
                    }
                    context.getDagState().putAll(success.output());
                    lastMessage = success.message();
                    currentNodeId = node.onSuccess();
                }
                case DagTaskResult.Failure failure -> {
                    logger.warn("Node '{}' failed: {} (fatal={})", node.id(), failure.reason(), failure.fatal());
                    for (TaskInterceptor interceptor : interceptors) {
                        interceptor.afterFailure(context, node, failure);
                    }
                    lastMessage = failure.reason();
                    currentNodeId = node.onFailure();
                    if (currentNodeId == null) {
                        throw new RuntimeException("DAG execution failed at node '" + node.id()
                                + "' for process " + processId + ": " + failure.reason(),
                                failure.cause());
                    }
                }
            }
        }

        logger.info("DAG execution completed for process {}", processId);
        return lastMessage != null ? lastMessage : "DAG completed for process " + processId;
    }
}
