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
package org.apache.airavata.workflow.process.post;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import java.util.List;
import org.apache.airavata.activities.process.post.ArchiveActivity;
import org.apache.airavata.activities.process.post.JobVerificationActivity;
import org.apache.airavata.activities.process.post.OutputDataStagingActivity;
import org.apache.airavata.activities.process.post.ParsingTriggeringActivity;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.activities.shared.CompletingActivity;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Workflow for post-process execution.
 *
 * <p>This workflow orchestrates the post-execution phase of a process:
 * 1. Job Verification
 * 2. Output Data Staging / Archive
 * 3. Completing
 * 4. Parsing Triggering
 *
 * <p>Replaces the Helix-based workflow orchestration in PostWorkflowManager.
 */
public class ProcessPostWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPostWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            ProcessPostWorkflowInput input = ctx.getInput(ProcessPostWorkflowInput.class);
            String processId = input.processId();
            String experimentId = input.experimentId();
            String gatewayId = input.gatewayId();

            logger.info(
                    "Starting ProcessPostWorkflow for process {} of experiment {} in gateway {}",
                    processId,
                    experimentId,
                    gatewayId);

            try {
                // Step 1: Job Verification
                BaseActivityInput jobVerificationInput = new BaseActivityInput(
                        processId, experimentId, gatewayId, null, true, false, false, input.forceRun());

                String jobVerificationResult = ctx.callActivity(
                                JobVerificationActivity.class.getName(), jobVerificationInput, String.class)
                        .await();

                logger.info("Job verification completed for process {}: {}", processId, jobVerificationResult);

                // Step 2: Output Data Staging / Archive
                try {
                    var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
                    var processModel = registryService.getProcess(processId);
                    List<OutputDataObjectType> processOutputs = processModel.getProcessOutputs();

                    if (processOutputs != null && !processOutputs.isEmpty()) {
                        logger.info("Process {} has {} outputs to process", processId, processOutputs.size());

                        // Check if any outputs need data movement (staging)
                        boolean hasDataMovementOutputs =
                                processOutputs.stream().anyMatch(OutputDataObjectType::getDataMovement);

                        if (hasDataMovementOutputs) {
                            // Stage outputs that require data movement
                            BaseActivityInput outputStagingInput = new BaseActivityInput(
                                    processId, experimentId, gatewayId, null, true, false, false, true);

                            String outputStagingResult = ctx.callActivity(
                                            OutputDataStagingActivity.class.getName(), outputStagingInput, String.class)
                                    .await();

                            logger.info(
                                    "Output data staging completed for process {}: {}", processId, outputStagingResult);
                        }

                        // Check if archiving is needed (typically for all outputs or specific ones)
                        // Archive activity handles archiving of outputs
                        BaseActivityInput archiveInput = new BaseActivityInput(
                                processId, experimentId, gatewayId, null, true, false, false, true);

                        String archiveResult = ctx.callActivity(
                                        ArchiveActivity.class.getName(), archiveInput, String.class)
                                .await();

                        logger.info("Archive activity completed for process {}: {}", processId, archiveResult);
                    } else {
                        logger.info("Process {} has no outputs to stage or archive", processId);
                    }
                } catch (RegistryException e) {
                    logger.warn("Failed to load process outputs for process {}: {}", processId, e.getMessage());
                    // Fallback: still try to stage outputs even if we can't check the model
                    BaseActivityInput outputStagingInput =
                            new BaseActivityInput(processId, experimentId, gatewayId, null, true, false, false, true);

                    String outputStagingResult = ctx.callActivity(
                                    OutputDataStagingActivity.class.getName(), outputStagingInput, String.class)
                            .await();

                    logger.info("Output data staging completed for process {}: {}", processId, outputStagingResult);
                } catch (Exception e) {
                    logger.warn("Unexpected error processing outputs for process {}: {}", processId, e.getMessage());
                    // Continue with workflow even if output processing fails
                }

                // Step 3: Completing
                BaseActivityInput completingInput = new BaseActivityInput(
                        processId, experimentId, gatewayId, null, true, false, false, input.forceRun());

                String completingResult = ctx.callActivity(
                                CompletingActivity.class.getName(), completingInput, String.class)
                        .await();

                logger.info("Completing task completed for process {}: {}", processId, completingResult);

                // Step 4: Parsing Triggering
                BaseActivityInput parsingTriggeringInput =
                        new BaseActivityInput(processId, experimentId, gatewayId, null, true, false, false, false);

                String parsingTriggeringResult = ctx.callActivity(
                                ParsingTriggeringActivity.class.getName(), parsingTriggeringInput, String.class)
                        .await();

                logger.info("Parsing triggering completed for process {}: {}", processId, parsingTriggeringResult);

                String workflowId = ctx.getInstanceId();
                logger.info(
                        "ProcessPostWorkflow completed successfully for process {} with workflow ID {}",
                        processId,
                        workflowId);

                ctx.complete(workflowId);

            } catch (Exception e) {
                logger.error("ProcessPostWorkflow failed for process {}: {}", input.processId(), e.getMessage(), e);
                throw new RuntimeException("ProcessPostWorkflow failed for process " + input.processId(), e);
            }
        };
    }
}
