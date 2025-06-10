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
package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import java.util.List;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ProcessScanner;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.registry.api.RegistryService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessScannerImpl implements ProcessScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScannerImpl.class);

    protected static ThriftClientPool<RegistryService.Client> registryClientPool = Utils.getRegistryServiceClientPool();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RegistryService.Client client = null;
        try {
            LOGGER.debug("Executing Process scanner ....... ");
            client = this.registryClientPool.getResource();
            ProcessState state = ProcessState.QUEUED;
            List<ProcessModel> processModelList = client.getProcessListInState(state);

            String reSchedulerPolicyClass = ServerSettings.getReSchedulerPolicyClass();
            ReScheduler reScheduler =
                    (ReScheduler) Class.forName(reSchedulerPolicyClass).newInstance();

            for (ProcessModel processModel : processModelList) {
                reScheduler.reschedule(processModel, state);
            }

            ProcessState ReQueuedState = ProcessState.REQUEUED;
            List<ProcessModel> reQueuedProcessModels = client.getProcessListInState(ReQueuedState);

            for (ProcessModel processModel : reQueuedProcessModels) {
                reScheduler.reschedule(processModel, ReQueuedState);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
            if (client != null) {
                this.registryClientPool.returnBrokenResource(client);
            }
            client = null;
        } finally {
            if (client != null) {
                this.registryClientPool.returnResource(client);
            }
        }
    }
}
