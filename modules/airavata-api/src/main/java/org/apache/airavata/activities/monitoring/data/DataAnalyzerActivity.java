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
package org.apache.airavata.activities.monitoring.data;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.apache.airavata.activities.shared.ScheduledActivityInput;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity for data analysis and metrics computation.
 */
public class DataAnalyzerActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalyzerActivity.class);

    @Override
    public Void run(WorkflowActivityContext ctx) {
        var input = ctx.getInput(ScheduledActivityInput.class);
        logger.debug("DataAnalyzerActivity for jobId {}", input.jobId());

        var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
        var properties = WorkflowRuntimeHolder.getBean(AiravataServerProperties.class);

        try {
            // TODO: handle multiple gateways
            var gateway = properties.services().parser().enabledGateways();

            var state = JobState.SUBMITTED;
            var jobStatus = new JobStatus();
            jobStatus.setJobState(state);
            var time = properties.services().parser().timeStepSeconds();

            int fiveMinuteCount = registryService.getJobCount(jobStatus, gateway, 5);
            int tenMinuteCount = registryService.getJobCount(jobStatus, gateway, 10);
            int fifteenMinuteCount = registryService.getJobCount(jobStatus, gateway, 15);

            double fiveMinuteAverage = fiveMinuteCount * time / (5 * 60);
            double tenMinuteAverage = tenMinuteCount * time / (10 * 60);
            double fifteenMinuteAverage = fifteenMinuteCount * time / (10 * 60);

            logger.info(
                    "service rate: 5 min avg {} 10 min avg {} 15 min avg {}",
                    fiveMinuteAverage,
                    tenMinuteAverage,
                    fifteenMinuteAverage);

            var timeDistribution = registryService.getAVGTimeDistribution(gateway, 15);

            var msg = new StringBuilder();
            for (var entry : timeDistribution.entrySet()) {
                msg.append(" avg time ").append(entry.getKey()).append("  : ").append(entry.getValue());
            }
            logger.info(msg.toString());

            logger.debug("DataAnalyzerActivity completed for jobId {}", input.jobId());
            return null;
        } catch (Exception ex) {
            logger.error("Error in DataAnalyzerActivity for jobId {}: {}", input.jobId(), ex.getMessage(), ex);
            throw new RuntimeException("DataAnalyzerActivity failed", ex);
        }
    }
}
