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
package org.apache.airavata.orchestration.task;

import java.util.List;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessScannerImpl implements ProcessScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScannerImpl.class);

    protected static RegistryHandler registryHandler = SchedulerUtils.getRegistryHandler();

    @Override
    public void run() {
        try {
            LOGGER.debug("Executing Process scanner ....... ");

            ProcessState state = ProcessState.PROCESS_STATE_QUEUED;
            List<ProcessModel> processModelList = registryHandler.getProcessListInState(state);

            String reSchedulerPolicyClass = ServerSettings.getReSchedulerPolicyClass();
            ReScheduler reScheduler =
                    (ReScheduler) Class.forName(reSchedulerPolicyClass).newInstance();

            for (ProcessModel processModel : processModelList) {
                reScheduler.reschedule(processModel, state);
            }

            ProcessState ReQueuedState = ProcessState.PROCESS_STATE_REQUEUED;
            List<ProcessModel> reQueuedProcessModels = registryHandler.getProcessListInState(ReQueuedState);

            for (ProcessModel processModel : reQueuedProcessModels) {
                reScheduler.reschedule(processModel, ReQueuedState);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        }
    }
}
