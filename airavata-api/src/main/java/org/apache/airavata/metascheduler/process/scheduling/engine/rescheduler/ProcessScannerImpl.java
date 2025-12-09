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
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.core.engine.ProcessScanner;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.service.RegistryService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProcessScannerImpl implements ProcessScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScannerImpl.class);
    private static ApplicationContext applicationContext;

    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContextInstance;

    public ProcessScannerImpl(AiravataServerProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContextInstance = applicationContext;
        ProcessScannerImpl.applicationContext = applicationContext;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LOGGER.debug("Executing Process scanner ....... ");
            RegistryService registryService = applicationContextInstance.getBean(RegistryService.class);
            ProcessState state = ProcessState.QUEUED;
            List<ProcessModel> processModelList = registryService.getProcessListInState(state);

            String reSchedulerPolicyClass = properties.services.scheduler.computeResourceReschedulerPolicyClass;
            ReScheduler reScheduler =
                    (ReScheduler) Class.forName(reSchedulerPolicyClass).newInstance();

            for (ProcessModel processModel : processModelList) {
                reScheduler.reschedule(processModel, state);
            }

            ProcessState ReQueuedState = ProcessState.REQUEUED;
            List<ProcessModel> reQueuedProcessModels = registryService.getProcessListInState(ReQueuedState);

            for (ProcessModel processModel : reQueuedProcessModels) {
                reScheduler.reschedule(processModel, ReQueuedState);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
            throw new JobExecutionException(msg, ex);
        }
    }
}
