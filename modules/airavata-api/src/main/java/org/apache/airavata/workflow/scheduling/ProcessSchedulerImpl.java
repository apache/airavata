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
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.scheduling.policy.ComputeResourceSelectionPolicy;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProcessSchedulerImpl implements ProcessScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);

    private final RegistryService registryService;
    private final ApplicationContext applicationContext;

    public ProcessSchedulerImpl(RegistryService registryService, ApplicationContext applicationContext) {
        this.registryService = registryService;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean canLaunch(String experimentId) {
        try {
            var processModels = registryService.getProcessList(experimentId);
            var experiment = registryService.getExperiment(experimentId);
            boolean allProcessesScheduled = true;
            var policy = getPolicyBean();

            for (var processModel : processModels) {
                var processStatus = registryService.getProcessStatus(processModel.getProcessId());
                if (processStatus.getState().equals(ProcessState.CREATED)
                        || processStatus.getState().equals(ProcessState.VALIDATED)) {

                    var computationalResourceSchedulingModel =
                            policy.selectComputeResource(processModel.getProcessId());

                    if (computationalResourceSchedulingModel.isPresent()) {
                        var resourceSchedulingModel = computationalResourceSchedulingModel.get();
                        var inputDataObjectTypeList = experiment.getExperimentInputs();
                        inputDataObjectTypeList.forEach(obj -> {
                            if (obj.getName().equals("Wall_Time")) {
                                obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                            }
                            if (obj.getName().equals("Parallel_Group_Count")) {
                                obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                            }
                        });
                        experiment.setExperimentInputs(inputDataObjectTypeList);
                        experiment.setProcesses(new ArrayList<>());
                        var userConfigurationDataModel = experiment.getUserConfigurationData();
                        userConfigurationDataModel.setComputationalResourceScheduling(resourceSchedulingModel);
                        experiment.setUserConfigurationData(userConfigurationDataModel);
                        registryService.updateExperiment(experimentId, experiment);

                        var processInputDataObjectTypeList = processModel.getProcessInputs();
                        processInputDataObjectTypeList.forEach(obj -> {
                            if (obj.getName().equals("Wall_Time")) {
                                obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                            }
                            if (obj.getName().equals("Parallel_Group_Count")) {
                                obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                            }
                        });
                        processModel.setProcessInputs(processInputDataObjectTypeList);
                        processModel.setProcessResourceSchedule(resourceSchedulingModel);
                        processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());
                        registryService.updateProcess(processModel, processModel.getProcessId());
                    } else {
                        var newProcessStatus = new ProcessStatus();
                        newProcessStatus.setState(ProcessState.QUEUED);
                        registryService.updateProcessStatus(newProcessStatus, processModel.getProcessId());
                        allProcessesScheduled = false;
                    }
                }
            }
            return allProcessesScheduled;
        } catch (Exception e) {
            LOGGER.error("Exception while scheduling experiment {}", experimentId, e);
        }
        return false;
    }

    @Override
    public boolean reschedule(String experimentId) {
        return false;
    }

    private ComputeResourceSelectionPolicy getPolicyBean() {
        return applicationContext.getBeansOfType(ComputeResourceSelectionPolicy.class).values().stream()
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No ComputeResourceSelectionPolicy bean found. Check services.scheduler.selection-policy property."));
    }
}
