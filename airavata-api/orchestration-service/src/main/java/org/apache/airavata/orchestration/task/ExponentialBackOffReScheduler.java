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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    protected RegistryHandler registryHandler = SchedulerUtils.getRegistryHandler();

    @Override
    public void reschedule(ProcessModel processModel, ProcessState processState) {

        try {

            int maxReschedulingCount = ServerSettings.getMetaschedulerReschedulingThreshold();
            List<ProcessStatus> processStatusList = processModel.getProcessStatusesList();
            ExperimentModel experimentModel = registryHandler.getExperiment(processModel.getExperimentId());
            LOGGER.info("Rescheduling process with Id " + processModel.getProcessId() + " experimentId "
                    + processModel.getExperimentId());
            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                    Class.forName(selectionPolicyClass).newInstance();
            if (processState.equals(ProcessState.PROCESS_STATE_QUEUED)) {
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                        policy.selectComputeResource(processModel.getProcessId());

                if (computationalResourceSchedulingModel.isPresent()) {
                    updateResourceSchedulingModel(processModel, experimentModel, registryHandler);
                    SchedulerUtils.updateProcessStatusAndPublishStatus(
                            ProcessState.PROCESS_STATE_DEQUEUING,
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                }
            } else if (processState.equals(ProcessState.PROCESS_STATE_REQUEUED)) {
                int currentCount = getRequeuedCount(processStatusList);
                if (currentCount >= maxReschedulingCount) {
                    SchedulerUtils.updateProcessStatusAndPublishStatus(
                            ProcessState.PROCESS_STATE_FAILED,
                            processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                } else {

                    registryHandler.deleteJobs(processModel.getProcessId());
                    LOGGER.debug("Cleaned up current  job stack for process " + processModel.getProcessId()
                            + " experimentId " + processModel.getExperimentId());
                    ProcessStatus processStatus = registryHandler.getProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();

                    int value = fib(currentCount);

                    long currentTime = System.currentTimeMillis();

                    double scanningInterval = ServerSettings.getMetaschedulerJobScanningInterval();

                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        updateResourceSchedulingModel(processModel, experimentModel, registryHandler);
                        SchedulerUtils.saveAndPublishProcessStatus(
                                ProcessState.PROCESS_STATE_DEQUEUING,
                                processModel.getProcessId(),
                                processModel.getExperimentId(),
                                experimentModel.getGatewayId());
                    }
                }
            }
            return;
        } catch (Exception exception) {
            LOGGER.error("Error rescheduling process " + processModel.getProcessId(), exception);
        }
    }

    private int getRequeuedCount(List<ProcessStatus> processStatusList) {
        return (int) processStatusList.stream()
                .filter(x -> {
                    if (x.getState().equals(ProcessState.PROCESS_STATE_REQUEUED)) {
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
            ProcessModel processModel, ExperimentModel experimentModel, RegistryHandler registryClient)
            throws Exception {
        String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
        ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                Class.forName(selectionPolicyClass).newInstance();

        Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                policy.selectComputeResource(processModel.getProcessId());

        if (computationalResourceSchedulingModel.isPresent()) {
            ComputationalResourceSchedulingModel resourceSchedulingModel = computationalResourceSchedulingModel.get();

            List<InputDataObjectType> updatedExpInputs = new ArrayList<>();
            for (InputDataObjectType obj : experimentModel.getExperimentInputsList()) {
                if (obj.getName().equals("Wall_Time")) {
                    obj = obj.toBuilder()
                            .setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit())
                            .build();
                } else if (obj.getName().equals("Parallel_Group_Count")) {
                    obj = obj.toBuilder()
                            .setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount())
                            .build();
                }
                updatedExpInputs.add(obj);
            }

            List<InputDataObjectType> updatedProcInputs = new ArrayList<>();
            for (InputDataObjectType obj : processModel.getProcessInputsList()) {
                if (obj.getName().equals("Wall_Time")) {
                    obj = obj.toBuilder()
                            .setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit())
                            .build();
                } else if (obj.getName().equals("Parallel_Group_Count")) {
                    obj = obj.toBuilder()
                            .setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount())
                            .build();
                }
                updatedProcInputs.add(obj);
            }

            // update experiment model with selected compute resource
            UserConfigurationDataModel userConfigurationDataModel =
                    experimentModel.getUserConfigurationData().toBuilder()
                            .setComputationalResourceScheduling(resourceSchedulingModel)
                            .build();
            experimentModel = experimentModel.toBuilder()
                    .clearExperimentInputs()
                    .addAllExperimentInputs(updatedExpInputs)
                    .clearProcesses() // avoid duplication issues
                    .setUserConfigurationData(userConfigurationDataModel)
                    .build();
            registryClient.updateExperiment(processModel.getExperimentId(), experimentModel);

            processModel = processModel.toBuilder()
                    .clearProcessInputs()
                    .addAllProcessInputs(updatedProcInputs)
                    .setProcessResourceSchedule(resourceSchedulingModel)
                    .setComputeResourceId(resourceSchedulingModel.getResourceHostId())
                    .build();
            registryClient.updateProcess(processModel, processModel.getProcessId());
        }
    }
}
