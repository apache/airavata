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
package org.apache.airavata.workflow.process.cancel;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.apache.airavata.activities.process.cancel.CancelCompletingActivity;
import org.apache.airavata.activities.process.cancel.RemoteJobCancellationActivity;
import org.apache.airavata.activities.process.cancel.WorkflowCancellationActivity;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.common.model.ProcessTerminateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Workflow for process cancellation.
 *
 * <p>This workflow orchestrates the cancellation of a process:
 * 1. Workflow Cancellation (terminate any Dapr workflow instances)
 * 2. Remote Job Cancellation (if SLURM or other schedulers)
 * 3. Cancel Completing
 *
 * <p>Replaces the Helix-based cancellation workflow in PreWorkflowManager.
 */
public class ProcessCancelWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ProcessCancelWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            ProcessTerminateEvent input = ctx.getInput(ProcessTerminateEvent.class);
            String processId = input.getProcessId();
            String gatewayId = input.getGatewayId();
            String experimentId = input.getExperimentId();

            logger.info(
                    "Starting ProcessCancelWorkflow for process {} of experiment {} in gateway {}",
                    processId,
                    experimentId,
                    gatewayId);

            try {
                // Step 1: Cancel any running Dapr workflows for this process
                BaseActivityInput workflowCancellationInput =
                        new BaseActivityInput(processId, experimentId, gatewayId, null, false, false, false, false);

                String workflowCancellationResult = ctx.callActivity(
                                WorkflowCancellationActivity.class.getName(), workflowCancellationInput, String.class)
                        .await();

                logger.info(
                        "Workflow cancellation completed for process {}: {}", processId, workflowCancellationResult);

                // Step 2: Remote Job Cancellation (for SLURM and other schedulers)
                // This should check the compute resource type and only run for appropriate types
                // For now, we'll always attempt it - the activity will handle the logic
                BaseActivityInput remoteJobCancellationInput =
                        new BaseActivityInput(processId, experimentId, gatewayId, null, true, false, false, false);

                String remoteJobCancellationResult = ctx.callActivity(
                                RemoteJobCancellationActivity.class.getName(), remoteJobCancellationInput, String.class)
                        .await();

                logger.info(
                        "Remote job cancellation completed for process {}: {}", processId, remoteJobCancellationResult);

                // Step 3: Cancel Completing
                BaseActivityInput cancelCompletingInput =
                        new BaseActivityInput(processId, experimentId, gatewayId, null, true, false, false, false);

                String cancelCompletingResult = ctx.callActivity(
                                CancelCompletingActivity.class.getName(), cancelCompletingInput, String.class)
                        .await();

                logger.info("Cancel completing completed for process {}: {}", processId, cancelCompletingResult);

                String workflowId = ctx.getInstanceId();
                logger.info(
                        "ProcessCancelWorkflow completed successfully for process {} with workflow ID {}",
                        processId,
                        workflowId);

                ctx.complete(workflowId);

            } catch (Exception e) {
                logger.error(
                        "ProcessCancelWorkflow failed for process {}: {}", input.getProcessId(), e.getMessage(), e);
                throw new RuntimeException("ProcessCancelWorkflow failed for process " + input.getProcessId(), e);
            }
        };
    }
}
