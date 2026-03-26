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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.ResourceIdentifier;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.model.TaskState;
import org.apache.airavata.core.telemetry.CounterMetric;
import org.apache.airavata.core.telemetry.GaugeMetric;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.orchestration.LocalStatusEvent;
import org.apache.airavata.execution.state.StateValidators;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.model.ProcessStatusChangedEvent;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Built-in {@link TaskInterceptor} implementations for DAG task execution.
 *
 * <p>Grouped in a single file to reduce file sprawl. Each interceptor is ordered
 * so they execute in a predictable sequence: logging → metrics → status → errors.
 */

/** Sets up and tears down MDC logging context for each task execution. */
@Component
@Order(1)
class LoggingInterceptor implements TaskInterceptor {

    @Override
    public void before(TaskContext context, TaskNode node) {
        MDC.put("experiment", context.getExperimentId());
        MDC.put("process", context.getProcessId());
        MDC.put("gateway", context.getGatewayId());
        MDC.put("task", context.getTaskId());
        MDC.put("dagNode", node.id());
    }

    @Override
    public void afterSuccess(TaskContext context, TaskNode node, DagTaskResult.Success result) {
        MDC.clear();
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        MDC.clear();
    }
}

/** Tracks task execution metrics: active count, completion count, and failure count. */
@Component
@Order(2)
class MetricsInterceptor implements TaskInterceptor {

    private static final GaugeMetric activeTaskGauge = new GaugeMetric("dag_task_active");
    private static final CounterMetric completedCounter = new CounterMetric("dag_task_completed");
    private static final CounterMetric failedCounter = new CounterMetric("dag_task_failed");

    @Override
    public void before(TaskContext context, TaskNode node) {
        activeTaskGauge.inc();
    }

    @Override
    public void afterSuccess(TaskContext context, TaskNode node, DagTaskResult.Success result) {
        activeTaskGauge.dec();
        completedCounter.inc();
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        activeTaskGauge.dec();
        failedCounter.inc();
    }
}

/** Publishes task and process state transitions around each DAG task execution. */
@Component
@Order(3)
class StatusPublishingInterceptor implements TaskInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StatusPublishingInterceptor.class);

    private final StatusService statusService;
    private final ApplicationContext applicationContext;

    StatusPublishingInterceptor(StatusService statusService, ApplicationContext applicationContext) {
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
                    currentState,
                    state,
                    context.getProcessId(),
                    "process")) {
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

/** Records error details to the error registry when a DAG task fails. */
@Component
@Order(4)
class ErrorRecordingInterceptor implements TaskInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorRecordingInterceptor.class);

    private final StatusService errorService;

    ErrorRecordingInterceptor(StatusService errorService) {
        this.errorService = errorService;
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        String errorCode = UUID.randomUUID().toString();
        String errorMessage = "Error Code: " + errorCode + ", Node '" + node.id() + "' failed: " + result.reason();

        ErrorModel errorModel = new ErrorModel();
        errorModel.setUserFriendlyMessage(result.reason());
        errorModel.setCreatedAt(IdGenerator.getCurrentTimestamp().toEpochMilli());

        if (result.cause() != null) {
            StringWriter sw = new StringWriter();
            result.cause().printStackTrace(new PrintWriter(sw));
            errorModel.setActualErrorMessage(sw.toString());
        } else {
            errorModel.setActualErrorMessage(errorMessage);
        }

        saveError(errorModel, "TASK_ERROR", context.getTaskId(), errorService::addTaskError);
        saveError(errorModel, "PROCESS_ERROR", context.getProcessId(), errorService::addProcessError);
    }

    private void saveError(ErrorModel errorModel, String idPrefix, String entityId, ErrorSaver saver) {
        try {
            errorModel.setErrorId(IdGenerator.getId(idPrefix));
            saver.save(errorModel, entityId);
        } catch (Exception e) {
            logger.error("Failed to save {} for {}", idPrefix, entityId, e);
        }
    }

    @FunctionalInterface
    private interface ErrorSaver {
        void save(ErrorModel error, String entityId) throws Exception;
    }
}
