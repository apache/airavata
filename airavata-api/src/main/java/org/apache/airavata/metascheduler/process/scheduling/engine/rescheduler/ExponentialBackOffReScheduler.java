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
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    protected RegistryService.Iface registry = AiravataServiceFactory.getRegistry();

    @Override
    public void reschedule(ProcessModel processModel, ProcessState processState) {
        try {
            int maxReschedulingCount = ServerSettings.getMetaschedulerReschedulingThreshold();
            List<ProcessStatus> processStatusList = processModel.getProcessStatuses();
            ExperimentModel experimentModel = registry.getExperiment(processModel.getExperimentId());
            LOGGER.info("Rescheduling process with Id " + processModel.getProcessId() + " experimentId "
                    + processModel.getExperimentId());
            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                    Class.forName(selectionPolicyClass).newInstance();
            if (processState.equals(ProcessState.QUEUED)) {
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                        policy.selectComputeResource(processModel.getProcessId());

                if (computationalResourceSchedulingModel.isPresent()) {
                    updateResourceSchedulingModel(processModel, experimentModel, registry);
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

                    registry.deleteJobs(processModel.getProcessId());
                    LOGGER.debug("Cleaned up current  job stack for process " + processModel.getProcessId()
                            + " experimentId " + processModel.getExperimentId());
                    ProcessStatus processStatus = registry.getProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();

                    int value = fib(currentCount);

                    long currentTime = System.currentTimeMillis();

                    double scanningInterval = ServerSettings.getMetaschedulerJobScanningInterval();

                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        updateResourceSchedulingModel(processModel, experimentModel, registry);
                        Utils.saveAndPublishProcessStatus(
                                ProcessState.DEQUEUING,
                                processModel.getProcessId(),
                                processModel.getExperimentId(),
                                experimentModel.getGatewayId());
                    }
                }
            }
            return;
        } catch (Exception exception) {
            LOGGER.error("Error occurred while rescheduling process", exception);
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
            ProcessModel processModel, ExperimentModel experimentModel, RegistryService.Iface registry)
            throws TException, ExperimentNotFoundException, ApplicationSettingsException, ClassNotFoundException,
                    IllegalAccessException, InstantiationException, RegistryServiceException {
        String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
        ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                Class.forName(selectionPolicyClass).newInstance();

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
            registry.updateExperiment(processModel.getExperimentId(), experimentModel);

            processModel.setProcessResourceSchedule(resourceSchedulingModel);
            processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());
            registry.updateProcess(processModel, processModel.getProcessId());
        }
    }
}
