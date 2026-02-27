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

import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.orchestration.ExperimentStatusManager;
import org.apache.airavata.research.application.model.ApplicationOutput;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple decision and terminal tasks used in DAG templates.
 *
 * <p>Each task is a lightweight {@link DagTask} bean registered by name. Grouping them in one file
 * reduces file sprawl for what are essentially single-method implementations.
 */

/** Succeeds if the process has any declared outputs, fails otherwise. */
@Component("checkOutputsTask")
class CheckOutputsTask implements DagTask {

    @Override
    public DagTaskResult execute(TaskContext context) {
        var outputs = context.getProcessOutputs();
        if (outputs != null && !outputs.isEmpty()) {
            return new DagTaskResult.Success("Process has " + outputs.size() + " outputs");
        }
        return new DagTaskResult.Failure("No outputs defined");
    }
}

/** Succeeds if any process output has data movement enabled, fails otherwise. */
@Component("checkDataMovementTask")
class CheckDataMovementTask implements DagTask {

    @Override
    public DagTaskResult execute(TaskContext context) {
        var outputs = context.getProcessOutputs();
        if (outputs == null || outputs.isEmpty()) {
            return new DagTaskResult.Failure("No outputs defined");
        }
        boolean hasDataMovement = outputs.stream().anyMatch(ApplicationOutput::getDataMovement);
        if (hasDataMovement) {
            return new DagTaskResult.Success("Data movement outputs found");
        }
        return new DagTaskResult.Failure("No data movement outputs");
    }
}

/** Terminal task that marks the process and experiment as FAILED. */
@Component("markFailedTask")
class MarkFailedTask implements DagTask {

    private static final Logger logger = LoggerFactory.getLogger(MarkFailedTask.class);

    private final StatusService statusService;
    private final ExperimentStatusManager experimentStatusManager;

    MarkFailedTask(StatusService statusService, ExperimentStatusManager experimentStatusManager) {
        this.statusService = statusService;
        this.experimentStatusManager = experimentStatusManager;
    }

    @Override
    public DagTaskResult execute(TaskContext context) {
        String processId = context.getProcessId();
        String experimentId = context.getExperimentId();

        logger.info("Marking process {} and experiment {} as FAILED", processId, experimentId);

        try {
            StatusModel<ProcessState> processStatus = StatusModel.of(ProcessState.FAILED, "DAG execution failed");
            statusService.addProcessStatus(processStatus, processId);

            StatusModel<ExperimentState> experimentStatus =
                    StatusModel.of(ExperimentState.FAILED, "Process execution failed");
            experimentStatusManager.updateExperimentStatus(experimentId, experimentStatus, context.getGatewayId());
        } catch (Exception e) {
            logger.error("Failed to update status for process {} / experiment {}", processId, experimentId, e);
        }

        return new DagTaskResult.Success("Marked as FAILED");
    }
}
