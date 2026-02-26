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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.dag.TaskInterceptor;
import org.apache.airavata.execution.dag.TaskNode;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Records error details to the error registry when a DAG task fails.
 *
 * <p>Persists error models at the task, process, and experiment levels,
 * mirroring the behavior previously in {@code TaskErrorHandler}.
 */
@Component
@Order(4)
public class ErrorRecordingInterceptor implements TaskInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorRecordingInterceptor.class);

    private final StatusService errorService;

    public ErrorRecordingInterceptor(StatusService errorService) {
        this.errorService = errorService;
    }

    @Override
    public void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {
        String errorCode = UUID.randomUUID().toString();
        String errorMessage = "Error Code: " + errorCode + ", Node '" + node.id() + "' failed: " + result.reason();

        ErrorModel errorModel = new ErrorModel();
        errorModel.setUserFriendlyMessage(result.reason());
        errorModel.setCreationTime(IdGenerator.getCurrentTimestamp().getTime());

        if (result.cause() != null) {
            StringWriter sw = new StringWriter();
            result.cause().printStackTrace(new PrintWriter(sw));
            errorModel.setActualErrorMessage(sw.toString());
        } else {
            errorModel.setActualErrorMessage(errorMessage);
        }

        saveTaskError(errorModel, context);
        saveProcessError(errorModel, context);
    }

    private void saveTaskError(ErrorModel errorModel, TaskContext context) {
        try {
            errorModel.setErrorId(IdGenerator.getId("TASK_ERROR"));
            errorService.addTaskError(errorModel, context.getTaskId());
        } catch (Exception e) {
            logger.error("Failed to save task error for task {}", context.getTaskId(), e);
        }
    }

    private void saveProcessError(ErrorModel errorModel, TaskContext context) {
        try {
            errorModel.setErrorId(IdGenerator.getId("PROCESS_ERROR"));
            errorService.addProcessError(errorModel, context.getProcessId());
        } catch (Exception e) {
            logger.error("Failed to save process error for process {}", context.getProcessId(), e);
        }
    }
}
