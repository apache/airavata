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
package org.apache.airavata.activities.scheduling;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.apache.airavata.activities.shared.ScheduledActivityInput;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.workflow.scheduling.ReScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity for process scanning and rescheduling.
 */
public class ProcessScannerActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(ProcessScannerActivity.class);

    @Override
    public Void run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(ScheduledActivityInput.class);
        logger.debug("ProcessScannerActivity for jobId {}", input.jobId());

        RegistryService registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);

        try {
            // Get queued processes
            var state = ProcessState.QUEUED;
            var processModelList = registryService.getProcessListInState(state);

            // Get reScheduler bean from Spring context
            var appContext = WorkflowRuntimeHolder.getApplicationContext();
            var reScheduler = appContext.getBeansOfType(ReScheduler.class).values().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No ReScheduler bean found"));

            for (var processModel : processModelList) {
                reScheduler.reschedule(processModel, state);
            }

            // Get requeued processes
            var requeuedState = ProcessState.REQUEUED;
            var requeuedProcessModels = registryService.getProcessListInState(requeuedState);

            for (var processModel : requeuedProcessModels) {
                reScheduler.reschedule(processModel, requeuedState);
            }

            logger.debug("ProcessScannerActivity completed for jobId {}", input.jobId());
            return null;
        } catch (Exception ex) {
            logger.error("Error in ProcessScannerActivity for jobId {}: {}", input.jobId(), ex.getMessage(), ex);
            throw new RuntimeException("ProcessScannerActivity failed", ex);
        }
    }
}
