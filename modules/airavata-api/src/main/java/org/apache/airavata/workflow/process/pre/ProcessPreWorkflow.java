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
package org.apache.airavata.workflow.process.pre;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.apache.airavata.activities.process.pre.EnvSetupActivity;
import org.apache.airavata.activities.process.pre.InputDataStagingActivity;
import org.apache.airavata.activities.process.pre.JobSubmissionActivity;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.activities.shared.CompletingActivity;
import org.apache.airavata.common.model.ProcessSubmitEvent;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Workflow for pre-process execution.
 *
 * <p>This workflow orchestrates the pre-execution phase of a process:
 * 1. Environment Setup
 * 2. Input Data Staging
 * 3. Job Submission
 * 4. Optional Completing (for intermediate transfers)
 *
 * <p>Replaces the Helix-based workflow orchestration in PreWorkflowManager.
 */
public class ProcessPreWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPreWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            ProcessSubmitEvent input = ctx.getInput(ProcessSubmitEvent.class);
            String processId = input.getProcessId();
            String experimentId = input.getExperimentId();
            String gatewayId = input.getGatewayId();

            logger.info(
                    "Starting ProcessPreWorkflow for process {} of experiment {} in gateway {}",
                    processId,
                    experimentId,
                    gatewayId);

            try {
                // Step 1: Environment Setup
                BaseActivityInput envSetupInput = new BaseActivityInput(
                        processId, experimentId, gatewayId, input.getTokenId(), false, false, false, false);

                String envSetupResult = ctx.callActivity(EnvSetupActivity.class.getName(), envSetupInput, String.class)
                        .await();

                logger.info("Environment setup completed for process {}: {}", processId, envSetupResult);

                // Step 2: Input Data Staging
                BaseActivityInput inputStagingInput = new BaseActivityInput(
                        processId, experimentId, gatewayId, input.getTokenId(), false, false, false, false);

                String inputStagingResult = ctx.callActivity(
                                InputDataStagingActivity.class.getName(), inputStagingInput, String.class)
                        .await();

                logger.info("Input data staging completed for process {}: {}", processId, inputStagingResult);

                // Step 3: Job Submission
                BaseActivityInput jobSubmissionInput = new BaseActivityInput(
                        processId, experimentId, gatewayId, input.getTokenId(), false, false, false, false);

                String jobSubmissionResult = ctx.callActivity(
                                JobSubmissionActivity.class.getName(), jobSubmissionInput, String.class)
                        .await();

                logger.info("Job submission completed for process {}: {}", processId, jobSubmissionResult);

                // Step 4: Optional Completing (for intermediate transfers)
                boolean hasIntermediateTransfer = false;
                try {
                    var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
                    var processModel = registryService.getProcess(processId);
                    hasIntermediateTransfer = processModel.getTasks().stream()
                            .anyMatch(task -> task.getTaskType() == TaskTypes.OUTPUT_FETCHING);
                } catch (Exception e) {
                    logger.warn("Failed to check for intermediate transfers for process {}", processId);
                }

                if (hasIntermediateTransfer) {
                    BaseActivityInput completingInput = new BaseActivityInput(
                            processId, experimentId, gatewayId, input.getTokenId(), true, false, false, false);

                    String completingResult = ctx.callActivity(
                                    CompletingActivity.class.getName(), completingInput, String.class)
                            .await();

                    logger.info("Completing task completed for process {}: {}", processId, completingResult);
                }

                String workflowId = ctx.getInstanceId();
                logger.info(
                        "ProcessPreWorkflow completed successfully for process {} with workflow ID {}",
                        processId,
                        workflowId);

                ctx.complete(workflowId);

            } catch (Exception e) {
                logger.error("ProcessPreWorkflow failed for process {}: {}", input.getProcessId(), e.getMessage(), e);
                throw new RuntimeException("ProcessPreWorkflow failed for process " + input.getProcessId(), e);
            }
        };
    }
}
