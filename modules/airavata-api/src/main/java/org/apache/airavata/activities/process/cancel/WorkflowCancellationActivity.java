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
package org.apache.airavata.activities.process.cancel;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import io.dapr.workflows.client.DaprWorkflowClient;
import java.util.Optional;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowCancellationActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowCancellationActivity.class);

    @Override
    public String run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(BaseActivityInput.class);
        logger.info("WorkflowCancellationActivity for process {}", input.processId());

        try {
            var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
            var workflowClient = WorkflowRuntimeHolder.getBean(DaprWorkflowClient.class);

            // Get all workflows registered for this process
            var processModel = registryService.getProcess(input.processId());
            var workflowsOpt = Optional.ofNullable(processModel.getProcessWorkflows());

            if (workflowsOpt.isPresent()) {
                var workflows = workflowsOpt.get();
                logger.info("Found {} workflows to cancel for process {}", workflows.size(), input.processId());

                for (var workflow : workflows) {
                    var workflowId = workflow.getWorkflowId();
                    logger.info("Terminating Dapr workflow {} for process {}", workflowId, input.processId());

                    try {
                        // Use Dapr Workflow client to terminate the workflow
                        // The workflow ID is the Dapr workflow instance ID
                        workflowClient.terminateWorkflow(workflowId, null);
                        logger.info(
                                "Successfully terminated workflow {} for process {}", workflowId, input.processId());
                    } catch (Exception e) {
                        logger.warn(
                                "Failed to terminate workflow {} for process {}: {}",
                                workflowId,
                                input.processId(),
                                e.getMessage());
                        // Continue with other workflows even if one fails
                    }
                }
            } else {
                logger.warn("No workflows found for process {}", input.processId());
            }

            return "Workflow cancellation completed for process " + input.processId();

        } catch (RegistryException e) {
            logger.error("Failed to get process workflows for process {}", input.processId(), e);
            throw new RuntimeException("WorkflowCancellationActivity failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("WorkflowCancellationActivity failed for process {}", input.processId(), e);
            throw new RuntimeException("WorkflowCancellationActivity failed: " + e.getMessage(), e);
        }
    }
}
