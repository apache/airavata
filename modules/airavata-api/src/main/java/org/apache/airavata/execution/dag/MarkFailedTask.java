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
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Terminal DAG task that marks the process and experiment as FAILED.
 *
 * <p>This is the common failure sink in DAG templates. When any task fails
 * and its failure edge points to "fail", this task publishes the terminal
 * FAILED status for both the process and experiment.
 */
@Component("markFailedTask")
public class MarkFailedTask implements DagTask {

    private static final Logger logger = LoggerFactory.getLogger(MarkFailedTask.class);

    private final StatusService statusService;
    private final ExperimentStatusManager experimentStatusManager;

    public MarkFailedTask(StatusService statusService, ExperimentStatusManager experimentStatusManager) {
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
