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
package org.apache.airavata.workflow.monitoring.compute;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import java.time.Duration;
import org.apache.airavata.activities.monitoring.compute.ComputeMonitorActivity;
import org.apache.airavata.activities.shared.ScheduledActivityInput;
import org.apache.airavata.orchestrator.ScheduledWorkflowInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for scheduled compute resource monitoring.
 */
public class ComputeMonitorWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ComputeMonitorWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            var input = ctx.getInput(ScheduledWorkflowInput.class);
            logger.info(
                    "ComputeMonitorWorkflow started: interval={}s, jobId={}", input.intervalSeconds(), input.jobId());

            var activityInput = new ScheduledActivityInput(input.jobId(), input.parallelJobs());

            while (true) {
                try {
                    logger.debug("Executing ComputeMonitorActivity for jobId {}", input.jobId());
                    ctx.callActivity(ComputeMonitorActivity.class.getName(), activityInput, Void.class)
                            .await();
                    logger.debug("ComputeMonitorActivity completed for jobId {}", input.jobId());

                    // Wait for configured interval before next execution
                    ctx.createTimer(Duration.ofSeconds(input.intervalSeconds())).await();
                } catch (Exception e) {
                    logger.error("Error in ComputeMonitorWorkflow for jobId {}: {}", input.jobId(), e.getMessage(), e);
                    // Continue execution even on error - wait before retry
                    ctx.createTimer(Duration.ofSeconds(input.intervalSeconds())).await();
                }
            }
        };
    }
}
