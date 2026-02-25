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
package org.apache.airavata.execution.scheduling;

import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.execution.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spring-managed scheduled tasks replacing DaprScheduledWorkflowManager.
 *
 * <p>Uses Spring {@code @Scheduled} instead of perpetual Dapr workflows.
 * Each method runs at a fixed interval configured via application properties.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = "airavata.services.controller.enabled", havingValue = "true")
public class ScheduledTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskManager.class);

    private final ServerProperties properties;
    private final ProcessService processService;

    @Autowired(required = false)
    private ReScheduler reScheduler;

    public ScheduledTaskManager(ServerProperties properties, ProcessService processService) {
        this.properties = properties;
        this.processService = processService;
    }

    /**
     * Scans for QUEUED and REQUEUED processes and reschedules them.
     */
    @Scheduled(fixedDelayString = "${airavata.services.scheduler.job-scanning-interval:1800000}")
    public void scanProcesses() {
        if (!properties.services().scheduler().rescheduler().enabled()) {
            return;
        }
        if (reScheduler == null) {
            logger.warn("ReScheduler not available; skipping process scan");
            return;
        }

        try {
            var queuedProcesses = processService.getProcessListInState(ProcessState.QUEUED);
            for (var process : queuedProcesses) {
                reScheduler.reschedule(process, ProcessState.QUEUED);
            }

            var requeuedProcesses = processService.getProcessListInState(ProcessState.REQUEUED);
            for (var process : requeuedProcesses) {
                reScheduler.reschedule(process, ProcessState.REQUEUED);
            }

            logger.debug(
                    "Process scan completed: {} queued, {} requeued", queuedProcesses.size(), requeuedProcesses.size());
        } catch (Exception e) {
            logger.error("Error during process scan", e);
        }
    }
}
