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
package org.apache.airavata.execution.dag.interceptor;

import org.apache.airavata.core.model.ResourceIdentifier;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.dag.TaskInterceptor;
import org.apache.airavata.execution.dag.TaskNode;
import org.apache.airavata.execution.event.LocalStatusEvent;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.TaskState;
import org.apache.airavata.execution.state.StateValidators;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Publishes task and process state transitions around each DAG task execution.
 *
 * <p>Before execution: publishes {@code TaskState.EXECUTING}.
 * After success: publishes {@code TaskState.COMPLETED} and optionally a
 * {@link ProcessState} read from the node's {@code "processState"} metadata key.
 * After failure: publishes {@code TaskState.FAILED} and {@code ProcessState.FAILED}.
 */
@Component
@Order(3)
public class StatusPublishingInterceptor implements TaskInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StatusPublishingInterceptor.class);

    private final StatusService statusService;
    private final ApplicationContext applicationContext;

    public StatusPublishingInterceptor(StatusService statusService, ApplicationContext applicationContext) {
        this.statusService = statusService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void before(TaskContext context, TaskNode node) {
        publishTaskState(context, TaskState.EXECUTING);

        String processStateStr = node.metadata().get("processState");
        if (processStateStr != null) {
            try {
                ProcessState state = ProcessState.valueOf(processStateStr);
                publishProcessState(context, state);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid processState '{}' in node '{}' metadata", processStateStr, node.id());
            }
        }
    }

    @Override
    public void afterSuccess(TaskContext context, TaskNode node, DagTaskResult.Success result) {
        publishTaskState(context, TaskState.COMPLETED);
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        publishTaskState(context, TaskState.FAILED);
    }

    private void publishTaskState(TaskContext context, TaskState state) {
        try {
            TaskState currentState = context.getTaskState();
            if (!StateValidators.StateTransitionService.validateAndLog(
                    StateValidators.TaskStateValidator.INSTANCE,
                    currentState, state, context.getTaskId(), "task")) {
                return;
            }

            StatusModel<TaskState> taskStatus = StatusModel.of(state);
            statusService.addTaskStatus(taskStatus, context.getTaskId());
        } catch (Exception e) {
            logger.error("Failed to publish task status {} for task {}", state, context.getTaskId(), e);
        }
    }

    private void publishProcessState(TaskContext context, ProcessState state) {
        try {
            StatusModel<ProcessState> currentStatus = statusService.getLatestProcessStatus(context.getProcessId());
            ProcessState currentState = currentStatus != null ? currentStatus.getState() : null;

            if (!StateValidators.StateTransitionService.validateAndLog(
                    StateValidators.ProcessStateValidator.INSTANCE,
                    currentState, state, context.getProcessId(), "process")) {
                return;
            }

            StatusModel<ProcessState> processStatus = StatusModel.of(state);
            statusService.addProcessStatus(processStatus, context.getProcessId());

            var identifier = ResourceIdentifier.forProcess(
                    context.getProcessId(), context.getExperimentId(), context.getGatewayId());
            var event = new ProcessStatusChangedEvent(state, identifier);
            applicationContext.publishEvent(new LocalStatusEvent<>(this, event, context.getGatewayId()));
        } catch (Exception e) {
            logger.error("Failed to publish process status {} for process {}", state, context.getProcessId(), e);
        }
    }
}
