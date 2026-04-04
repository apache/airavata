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

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
public class ProcessSchedulerImpl implements ProcessScheduler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);

    private RegistryHandler registryHandler;

    public ProcessSchedulerImpl() {
        try {
            registryHandler = SchedulerUtils.getRegistryHandler();
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching registry handler", e);
        }
    }

    @Override
    public boolean canLaunch(String experimentId) {
        final RegistryHandler registryClient = this.registryHandler;
        try {
            List<ProcessModel> processModels = registryClient.getProcessList(experimentId);

            ExperimentModel experiment = registryClient.getExperiment(experimentId);
            boolean allProcessesScheduled = true;

            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();

            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                    Class.forName(selectionPolicyClass).newInstance();

            for (ProcessModel processModel : processModels) {
                ProcessStatus processStatus = registryClient.getProcessStatus(processModel.getProcessId());

                if (processStatus.getState().equals(ProcessState.PROCESS_STATE_CREATED)
                        || processStatus.getState().equals(ProcessState.PROCESS_STATE_VALIDATED)) {

                    Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                            policy.selectComputeResource(processModel.getProcessId());

                    if (computationalResourceSchedulingModel.isPresent()) {
                        ComputationalResourceSchedulingModel resourceSchedulingModel =
                                computationalResourceSchedulingModel.get();

                        // Update experiment inputs with scheduling params
                        List<InputDataObjectType> updatedExpInputs = new ArrayList<>();
                        for (InputDataObjectType obj : experiment.getExperimentInputsList()) {
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

                        // update experiment model with selected compute resource
                        UserConfigurationDataModel userConfigurationDataModel =
                                experiment.getUserConfigurationData().toBuilder()
                                        .setComputationalResourceScheduling(resourceSchedulingModel)
                                        .build();
                        experiment = experiment.toBuilder()
                                .clearExperimentInputs()
                                .addAllExperimentInputs(updatedExpInputs)
                                .clearProcesses() // avoid duplication issues
                                .setUserConfigurationData(userConfigurationDataModel)
                                .build();
                        registryClient.updateExperiment(experimentId, experiment);

                        // Update process inputs with scheduling params
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

                        processModel = processModel.toBuilder()
                                .clearProcessInputs()
                                .addAllProcessInputs(updatedProcInputs)
                                .setProcessResourceSchedule(resourceSchedulingModel)
                                .setComputeResourceId(resourceSchedulingModel.getResourceHostId())
                                .build();

                        registryClient.updateProcess(processModel, processModel.getProcessId());

                    } else {
                        ProcessStatus newProcessStatus = ProcessStatus.newBuilder()
                                .setState(ProcessState.PROCESS_STATE_QUEUED)
                                .build();
                        registryClient.updateProcessStatus(newProcessStatus, processModel.getProcessId());
                        allProcessesScheduled = false;
                    }
                }
            }
            return allProcessesScheduled;
        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling experiment with Id {}", experimentId, exception);
        }

        return false;
    }

    @Override
    public boolean reschedule(String experimentId) {
        return false;
    }
}
