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
package org.apache.airavata.metascheduler.process.scheduling.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
@Component
public class ProcessSchedulerImpl implements ProcessScheduler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);
    
    @Autowired
    private RegistryService registryService;

    @Override
    public boolean canLaunch(String experimentId) {
        try {
            List<ProcessModel> processModels = registryService.getProcessList(experimentId);

            ExperimentModel experiment = registryService.getExperiment(experimentId);
            boolean allProcessesScheduled = true;

            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();

            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy)
                    Class.forName(selectionPolicyClass).getDeclaredConstructor().newInstance();

            for (ProcessModel processModel : processModels) {
                ProcessStatus processStatus = registryService.getProcessStatus(processModel.getProcessId());

                if (processStatus.getState().equals(ProcessState.CREATED)
                        || processStatus.getState().equals(ProcessState.VALIDATED)) {

                    Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel =
                            policy.selectComputeResource(processModel.getProcessId());

                    if (computationalResourceSchedulingModel.isPresent()) {
                        ComputationalResourceSchedulingModel resourceSchedulingModel =
                                computationalResourceSchedulingModel.get();
                        List<InputDataObjectType> inputDataObjectTypeList = experiment.getExperimentInputs();
                        inputDataObjectTypeList.forEach(obj -> {
                            if (obj.getName().equals("Wall_Time")) {
                                obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                            }
                            if (obj.getName().equals("Parallel_Group_Count")) {
                                obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                            }
                        });

                        experiment.setExperimentInputs(inputDataObjectTypeList);

                        // update experiment model with selected compute resource
                        experiment.setProcesses(new ArrayList<>()); // avoid duplication issues
                        UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();
                        userConfigurationDataModel.setComputationalResourceScheduling(resourceSchedulingModel);
                        experiment.setUserConfigurationData(userConfigurationDataModel);
                        registryService.updateExperiment(experimentId, experiment);

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
                        processModel.setProcessResourceSchedule(resourceSchedulingModel);
                        processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());

                        registryService.updateProcess(processModel, processModel.getProcessId());

                    } else {
                        ProcessStatus newProcessStatus = new ProcessStatus();
                        newProcessStatus.setState(ProcessState.QUEUED);
                        registryService.updateProcessStatus(newProcessStatus, processModel.getProcessId());
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
