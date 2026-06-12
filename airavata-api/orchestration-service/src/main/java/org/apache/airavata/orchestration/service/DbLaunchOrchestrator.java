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
package org.apache.airavata.orchestration.service;

import java.util.List;
import org.apache.airavata.config.ConditionalOnServer;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * DB-transactional launch bridge: synchronously creates the PROCESS and ordered TASK
 * rows for an experiment and advances status, with no RabbitMQ and no Helix.
 *
 * <p>Reuses {@link SimpleOrchestratorImpl#createProcesses} and
 * {@link SimpleOrchestratorImpl#createAndSaveTasks} for row creation, then writes
 * PROCESS_STATE_STARTED per process and EXPERIMENT_STATE_LAUNCHED once, directly via
 * the in-process {@link RegistryHandler}.
 */
@Service
@ConditionalOnServer("orchestrator")
public class DbLaunchOrchestrator implements LaunchOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(DbLaunchOrchestrator.class);

    @Override
    public void launchExperiment(String experimentId, String gatewayId) throws Exception {
        logger.info("DB launch bridge starting for experiment {} in gateway {}", experimentId, gatewayId);

        RegistryHandler registry = SchedulerUtils.getRegistryHandler();
        SimpleOrchestratorImpl orch = new SimpleOrchestratorImpl();

        List<ProcessModel> procs = orch.createProcesses(experimentId, gatewayId);
        for (ProcessModel proc : procs) {
            String taskDag = orch.createAndSaveTasks(gatewayId, proc);
            proc = proc.toBuilder().setTaskDag(taskDag).build();
            registry.updateProcess(proc, proc.getProcessId());

            ProcessStatus processStatus = ProcessStatus.newBuilder()
                    .setState(ProcessState.PROCESS_STATE_STARTED)
                    .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                    .build();
            registry.addProcessStatus(processStatus, proc.getProcessId());
            logger.info("Created process {} with {} tasks for experiment {}", proc.getProcessId(), taskDag, experimentId);
        }

        ExperimentStatus experimentStatus = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_LAUNCHED)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();
        registry.updateExperimentStatus(experimentStatus, experimentId);

        logger.info(
                "DB launch bridge finished for experiment {}: {} process(es) created and LAUNCHED",
                experimentId,
                procs.size());
    }
}
