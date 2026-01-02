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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.exception.ExperimentNotFoundException;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(
        name = "scheduler.reschedulerPolicy",
        havingValue = "ExponentialBackOffReScheduler",
        matchIfMissing = true)
public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    private final AiravataServerProperties properties;
    private final RegistryService registryService;
    private final ApplicationContext applicationContext;

    public ExponentialBackOffReScheduler(
            AiravataServerProperties properties,
            RegistryService registryService,
            ApplicationContext applicationContext) {
        this.properties = properties;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void reschedule(ProcessModel processModel, ProcessState processState) {
        try {
            int maxReschedulingCount = properties.services.scheduler.maximumReschedulerThreshold;
            List<ProcessStatus> processStatusList = processModel.getProcessStatuses();
            ExperimentModel experimentModel = registryService.getExperiment(processModel.getExperimentId());
            LOGGER.info("Rescheduling process with Id " + processModel.getProcessId() + " experimentId "
                    + processModel.getExperimentId());
            // Get policy bean from Spring context using property-based selection
            String policyClassName = properties.services.scheduler.computeResourceSelectionPolicyClass;
            ComputeResourceSelectionPolicy policy = getPolicyBean(policyClassName);
            if (processState.equals(ProcessState.QUEUED)) {
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                        policy.selectComputeResource(processModel.getProcessId());

                if (computationalResourceSchedulingModel.isPresent()) {
                    updateResourceSchedulingModel(processModel, experimentModel, registryService);
                    Utils.updateProcessStatusAndPublishStatus(
                            ProcessState.DEQUEUING,
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                }
            } else if (processState.equals(ProcessState.REQUEUED)) {
                int currentCount = getRequeuedCount(processStatusList);
                if (currentCount >= maxReschedulingCount) {
                    Utils.updateProcessStatusAndPublishStatus(
                            ProcessState.FAILED,
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                } else {

                    registryService.deleteJobs(processModel.getProcessId());
                    LOGGER.debug("Cleaned up current  job stack for process " + processModel.getProcessId()
                            + " experimentId " + processModel.getExperimentId());
                    ProcessStatus processStatus = registryService.getProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();

                    int value = fib(currentCount);

                    long currentTime = System.currentTimeMillis();

                    double scanningInterval = properties.services.scheduler.jobScanningInterval;

                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        updateResourceSchedulingModel(processModel, experimentModel, registryService);
                        Utils.saveAndPublishProcessStatus(
                                ProcessState.DEQUEUING,
                                processModel.getProcessId(),
                                processModel.getExperimentId(),
                                experimentModel.getGatewayId());
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Error rescheduling process " + processModel.getProcessId(), exception);
        }
    }

    private int getRequeuedCount(List<ProcessStatus> processStatusList) {
        return (int) processStatusList.stream()
                .filter(x -> {
                    if (x.getState().equals(ProcessState.REQUEUED)) {
                        return true;
                    }
                    return false;
                })
                .count();
    }

    private int fib(int n) {
        if (n <= 1) return n;
        return fib(n - 1) + fib(n - 2);
    }

    private void updateResourceSchedulingModel(
            ProcessModel processModel, ExperimentModel experimentModel, RegistryService registryService)
            throws ExperimentNotFoundException, ApplicationSettingsException, RegistryServiceException {
        // Get policy bean from Spring context - all policies are Spring beans now
        ComputeResourceSelectionPolicy policy =
                applicationContext.getBeansOfType(ComputeResourceSelectionPolicy.class).values().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "No ComputeResourceSelectionPolicy bean found in Spring context"));

        Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                policy.selectComputeResource(processModel.getProcessId());

        if (computationalResourceSchedulingModel.isPresent()) {
            ComputationalResourceSchedulingModel resourceSchedulingModel = computationalResourceSchedulingModel.get();
            List<InputDataObjectType> inputDataObjectTypeList = experimentModel.getExperimentInputs();
            inputDataObjectTypeList.forEach(obj -> {
                if (obj.getName().equals("Wall_Time")) {
                    obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                }
                if (obj.getName().equals("Parallel_Group_Count")) {
                    obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                }
            });

            List<InputDataObjectType> processInputDataObjectTypeList = processModel.getProcessInputs();
            processInputDataObjectTypeList.forEach(obj -> {
                if (obj.getName().equals("Wall_Time")) {
                    obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                }
                if (obj.getName().equals("Parallel_Group_Count")) {
                    obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                }
            });

            processModel.setProcessInputs(processInputDataObjectTypeList);
            experimentModel.setExperimentInputs(inputDataObjectTypeList);

            // update experiment model with selected compute resource
            experimentModel.setProcesses(new ArrayList<>()); // avoid duplication issues
            UserConfigurationDataModel userConfigurationDataModel = experimentModel.getUserConfigurationData();
            userConfigurationDataModel.setComputationalResourceScheduling(resourceSchedulingModel);
            experimentModel.setUserConfigurationData(userConfigurationDataModel);
            registryService.updateExperiment(processModel.getExperimentId(), experimentModel);

            processModel.setProcessResourceSchedule(resourceSchedulingModel);
            processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());
            registryService.updateProcess(processModel, processModel.getProcessId());
        }
    }

    /**
     * Get policy bean by bean name from Spring context.
     * Uses property-based selection for deterministic bean resolution.
     * Derives bean name from class name (simple class name with first letter lowercase).
     */
    private ComputeResourceSelectionPolicy getPolicyBean(String policyClassName) {
        try {
            // Extract simple class name from full class name
            String simpleClassName = policyClassName.substring(policyClassName.lastIndexOf('.') + 1);
            // Spring default bean name is simple class name with first letter lowercase
            String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);

            return applicationContext.getBean(beanName, ComputeResourceSelectionPolicy.class);
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
            // Extract bean name for error message
            String simpleClassName = policyClassName.substring(policyClassName.lastIndexOf('.') + 1);
            String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            LOGGER.error(
                    "Policy bean not found in Spring context: {} (derived from class name: {})",
                    beanName,
                    policyClassName,
                    e);
            throw new IllegalStateException(
                    "Policy bean not found: " + beanName + " (from class: " + policyClassName + ")", e);
        } catch (Exception e) {
            LOGGER.error("Failed to get policy bean for class: {}", policyClassName, e);
            throw new IllegalStateException("Failed to get policy bean for: " + policyClassName, e);
        }
    }
}
