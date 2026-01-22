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
package org.apache.airavata.workflow.scheduling;

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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.scheduling.policy.ComputeResourceSelectionPolicy;
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
        prefix = "services.scheduler",
        name = "rescheduler-policy",
        havingValue = "ExponentialBackOffReScheduler")
public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    private final AiravataServerProperties properties;
    private final RegistryService registryService;
    private final ApplicationContext applicationContext;
    private final ProcessStatusUpdater statusHelper;

    public ExponentialBackOffReScheduler(
            AiravataServerProperties properties,
            RegistryService registryService,
            ApplicationContext applicationContext,
            ProcessStatusUpdater statusHelper) {
        this.properties = properties;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
        this.statusHelper = statusHelper;
    }

    @Override
    public void reschedule(ProcessModel processModel, ProcessState processState) {
        try {
            int maxReschedulingCount = properties.services().scheduler().maximumReschedulerThreshold();
            List<ProcessStatus> processStatusList = processModel.getProcessStatuses();
            ExperimentModel experimentModel = registryService.getExperiment(processModel.getExperimentId());
            LOGGER.info(
                    "Rescheduling process {} experimentId {}",
                    processModel.getProcessId(),
                    processModel.getExperimentId());
            ComputeResourceSelectionPolicy policy = getPolicyBean();
            if (processState.equals(ProcessState.QUEUED)) {
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                        policy.selectComputeResource(processModel.getProcessId());
                if (computationalResourceSchedulingModel.isPresent()) {
                    updateResourceSchedulingModel(processModel, experimentModel, registryService);
                    statusHelper.updateProcessStatus(
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId(),
                            ProcessState.DEQUEUING);
                }
            } else if (processState.equals(ProcessState.REQUEUED)) {
                int currentCount = getRequeuedCount(processStatusList);
                if (currentCount >= maxReschedulingCount) {
                    statusHelper.updateProcessStatus(
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId(),
                            ProcessState.FAILED);
                } else {
                    registryService.deleteJobs(processModel.getProcessId());
                    LOGGER.debug(
                            "Cleaned up job stack for process {} experimentId {}",
                            processModel.getProcessId(),
                            processModel.getExperimentId());
                    ProcessStatus processStatus = registryService.getProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();
                    int value = fib(currentCount);
                    long currentTime = AiravataUtils.getUniqueTimestamp().getTime();
                    double scanningInterval = properties.services().scheduler().jobScanningInterval();
                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        updateResourceSchedulingModel(processModel, experimentModel, registryService);
                        statusHelper.addProcessStatus(
                                processModel.getProcessId(),
                                processModel.getExperimentId(),
                                experimentModel.getGatewayId(),
                                ProcessState.DEQUEUING);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error rescheduling process {}", processModel.getProcessId(), e);
        }
    }

    private int getRequeuedCount(List<ProcessStatus> processStatusList) {
        return (int) processStatusList.stream()
                .filter(x -> ProcessState.REQUEUED.equals(x.getState()))
                .count();
    }

    private int fib(int n) {
        if (n <= 1) return n;
        return fib(n - 1) + fib(n - 2);
    }

    private void updateResourceSchedulingModel(
            ProcessModel processModel, ExperimentModel experimentModel, RegistryService registryService)
            throws ExperimentNotFoundException, ApplicationSettingsException, RegistryException {
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
                if ("Wall_Time".equals(obj.getName()))
                    obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                if ("Parallel_Group_Count".equals(obj.getName()))
                    obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
            });
            List<InputDataObjectType> processInputDataObjectTypeList = processModel.getProcessInputs();
            processInputDataObjectTypeList.forEach(obj -> {
                if ("Wall_Time".equals(obj.getName()))
                    obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                if ("Parallel_Group_Count".equals(obj.getName()))
                    obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
            });
            processModel.setProcessInputs(processInputDataObjectTypeList);
            experimentModel.setExperimentInputs(inputDataObjectTypeList);
            experimentModel.setProcesses(new ArrayList<>());
            UserConfigurationDataModel userConfigurationDataModel = experimentModel.getUserConfigurationData();
            userConfigurationDataModel.setComputationalResourceScheduling(resourceSchedulingModel);
            experimentModel.setUserConfigurationData(userConfigurationDataModel);
            registryService.updateExperiment(processModel.getExperimentId(), experimentModel);
            processModel.setProcessResourceSchedule(resourceSchedulingModel);
            processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());
            registryService.updateProcess(processModel, processModel.getProcessId());
        }
    }

    private ComputeResourceSelectionPolicy getPolicyBean() {
        return applicationContext.getBeansOfType(ComputeResourceSelectionPolicy.class).values().stream()
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No ComputeResourceSelectionPolicy bean found. Check services.scheduler.selection-policy property."));
    }
}
