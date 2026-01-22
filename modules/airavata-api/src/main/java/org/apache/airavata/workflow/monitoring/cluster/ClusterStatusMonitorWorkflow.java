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
package org.apache.airavata.workflow.monitoring.cluster;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import java.time.Duration;
import org.apache.airavata.activities.monitoring.cluster.ClusterStatusMonitorActivity;
import org.apache.airavata.activities.shared.BaseActivityInput;
import org.apache.airavata.orchestrator.ScheduledWorkflowInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dapr Workflow for scheduled cluster status monitoring.
 *
 * <p>This workflow runs indefinitely, monitoring cluster status at configured intervals.
 * Replaces Quartz-based ClusterStatusMonitorJobScheduler.
 */
public class ClusterStatusMonitorWorkflow implements Workflow {

    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorWorkflow.class);

    @Override
    public WorkflowStub create() {
        return ctx -> {
            ScheduledWorkflowInput input = ctx.getInput(ScheduledWorkflowInput.class);
            logger.info("Starting ClusterStatusMonitorWorkflow with interval {}s", input.intervalSeconds());

            BaseActivityInput activityInput = new BaseActivityInput(
                    null, // processId
                    null, // experimentId
                    null, // gatewayId
                    null, // tokenId
                    false, // skipAllStatusPublish
                    false, // skipProcessStatusPublish
                    false, // skipExperimentStatusPublish
                    false // forceRunTask
                    );

            while (true) {
                try {
                    logger.debug("Executing ClusterStatusMonitorActivity");
                    ctx.callActivity(ClusterStatusMonitorActivity.class.getName(), activityInput, String.class)
                            .await();
                    logger.debug("ClusterStatusMonitorActivity completed");

                    // Wait for configured interval before next execution
                    ctx.createTimer(Duration.ofSeconds(input.intervalSeconds())).await();
                } catch (Exception e) {
                    logger.error("Error in ClusterStatusMonitorWorkflow: {}", e.getMessage(), e);
                    // Continue execution even on error - wait before retry
                    ctx.createTimer(Duration.ofSeconds(input.intervalSeconds())).await();
                }
            }
        };
    }
}
