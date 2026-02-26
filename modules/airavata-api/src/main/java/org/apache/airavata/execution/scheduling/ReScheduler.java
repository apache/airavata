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

import java.util.List;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.apache.airavata.status.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(
        prefix = "airavata.services.scheduler",
        name = "rescheduler-policy",
        havingValue = "exponential-backoff")
public class ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReScheduler.class);

    private final ServerProperties properties;
    private final ProcessService processService;
    private final ExperimentService experimentService;
    private final StatusService statusService;
    private final JobService jobService;

    public ReScheduler(
            ServerProperties properties,
            ProcessService processService,
            ExperimentService experimentService,
            StatusService statusService,
            JobService jobService) {
        this.properties = properties;
        this.processService = processService;
        this.experimentService = experimentService;
        this.statusService = statusService;
        this.jobService = jobService;
    }

    public void reschedule(ProcessModel processModel, ProcessState processState) {
        try {
            int maxReschedulingCount = properties.services().scheduler().maximumReschedulerThreshold();
            List<StatusModel<ProcessState>> processStatusList = processModel.getProcessStatuses();
            ExperimentModel experimentModel = experimentService.getExperiment(processModel.getExperimentId());
            LOGGER.info(
                    "Rescheduling process {} experimentId {}",
                    processModel.getProcessId(),
                    processModel.getExperimentId());
            if (processState.equals(ProcessState.QUEUED)) {
                statusService.addProcessStatus(StatusModel.of(ProcessState.DEQUEUING), processModel.getProcessId());
            } else if (processState.equals(ProcessState.REQUEUED)) {
                int currentCount = getRequeuedCount(processStatusList);
                if (currentCount >= maxReschedulingCount) {
                    statusService.addProcessStatus(StatusModel.of(ProcessState.FAILED), processModel.getProcessId());
                } else {
                    jobService.deleteJobsByProcessId(processModel.getProcessId());
                    LOGGER.debug(
                            "Cleaned up job stack for process {} experimentId {}",
                            processModel.getProcessId(),
                            processModel.getExperimentId());
                    StatusModel<ProcessState> processStatus =
                            statusService.getLatestProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();
                    int value = fib(currentCount);
                    long currentTime = IdGenerator.getUniqueTimestamp().getTime();
                    double scanningInterval = properties.services().scheduler().jobScanningInterval();
                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        statusService.addProcessStatus(
                                StatusModel.of(ProcessState.DEQUEUING), processModel.getProcessId());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error rescheduling process {}", processModel.getProcessId(), e);
        }
    }

    private int getRequeuedCount(List<StatusModel<ProcessState>> processStatusList) {
        return (int) processStatusList.stream()
                .filter(x -> ProcessState.REQUEUED.equals(x.getState()))
                .count();
    }

    private int fib(int n) {
        if (n <= 1) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            int tmp = a + b;
            a = b;
            b = tmp;
        }
        return b;
    }
}
