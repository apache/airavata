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
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatus;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
@Component
public class ProcessSchedulerImpl implements ProcessScheduler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);

    private final RegistryService registryService;
    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContext;

    public ProcessSchedulerImpl(
            RegistryService registryService,
            AiravataServerProperties properties,
            ApplicationContext applicationContext) {
        this.registryService = registryService;
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean canLaunch(String experimentId) {
        try {
            List<ProcessModel> processModels = registryService.getProcessList(experimentId);

            ExperimentModel experiment = registryService.getExperiment(experimentId);
            boolean allProcessesScheduled = true;

            // Get policy bean from Spring context using property-based selection
            String policyClassName = properties.services.scheduler.computeResourceSelectionPolicyClass;
            ComputeResourceSelectionPolicy policy = getPolicyBean(policyClassName);

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

    @Override
    public boolean reschedule(String experimentId) {
        return false;
    }
}
