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
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.core.engine.ProcessScanner;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.service.registry.RegistryService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProcessScannerImpl implements ProcessScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScannerImpl.class);

    private final AiravataServerProperties properties;
    private final RegistryService registryService;
    private final ApplicationContext applicationContext;

    public ProcessScannerImpl(
            AiravataServerProperties properties,
            RegistryService registryService,
            ApplicationContext applicationContext) {
        this.properties = properties;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LOGGER.debug("Executing Process scanner ....... ");
            ProcessState state = ProcessState.QUEUED;
            List<ProcessModel> processModelList = registryService.getProcessListInState(state);

            // Get reScheduler bean from Spring context using property-based selection
            String reSchedulerClassName = properties.services.scheduler.computeResourceReschedulerPolicyClass;
            ReScheduler reScheduler = getReSchedulerBean(reSchedulerClassName);

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

    /**
     * Get ReScheduler bean by bean name from Spring context.
     * Uses property-based selection for deterministic bean resolution.
     * Derives bean name from class name (simple class name with first letter lowercase).
     */
    private ReScheduler getReSchedulerBean(String reSchedulerClassName) {
        try {
            // Extract simple class name from full class name
            String simpleClassName = reSchedulerClassName.substring(reSchedulerClassName.lastIndexOf('.') + 1);
            // Spring default bean name is simple class name with first letter lowercase
            String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);

            return applicationContext.getBean(beanName, ReScheduler.class);
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            // Extract bean name for error message
            String simpleClassName = reSchedulerClassName.substring(reSchedulerClassName.lastIndexOf('.') + 1);
            String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            LOGGER.error(
                    "ReScheduler bean not found in Spring context: {} (derived from class name: {})",
                    beanName,
                    reSchedulerClassName,
                    e);
            throw new IllegalStateException(
                    "ReScheduler bean not found: " + beanName + " (from class: " + reSchedulerClassName + ")", e);
        } catch (Exception e) {
            LOGGER.error("Failed to get ReScheduler bean for class: {}", reSchedulerClassName, e);
            throw new IllegalStateException("Failed to get ReScheduler bean for: " + reSchedulerClassName, e);
        }
    }
}
