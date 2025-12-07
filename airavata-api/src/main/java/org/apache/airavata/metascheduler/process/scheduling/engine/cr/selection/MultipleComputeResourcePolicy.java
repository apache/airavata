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
package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.service.RegistryService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements selecting one compute resource out of enabled multiple compute resource polices.
 * //TODO: implemented for load testing, for proper usecases airavata should enable multiple compute resources in Experiment creation
 */
@Component
public class MultipleComputeResourcePolicy extends ComputeResourceSelectionPolicyImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleComputeResourcePolicy.class);
    private static ApplicationContext applicationContext;
    
    @org.springframework.beans.factory.annotation.Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        MultipleComputeResourcePolicy.applicationContext = applicationContext;
    }

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        RegistryService registryService = applicationContext.getBean(RegistryService.class);
        try {

            ProcessModel processModel = registryService.getProcess(processId);

            ExperimentModel experiment = registryService.getExperiment(processModel.getExperimentId());

            UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();

            List<ComputationalResourceSchedulingModel> resourceSchedulingModels =
                    userConfigurationDataModel.getAutoScheduledCompResourceSchedulingList();

            List<String> retries = new ArrayList<>();

            while (retries.size() < resourceSchedulingModels.size()) {
                Random rand = new Random();
                int upperbound = resourceSchedulingModels.size();
                int int_random = rand.nextInt(upperbound);
                ComputationalResourceSchedulingModel resourceSchedulingModel = resourceSchedulingModels.get(int_random);
                String key = resourceSchedulingModel.getResourceHostId() + "_" + resourceSchedulingModel.getQueueName();
                if (!retries.contains(key)) {
                    ComputeResourceDescription comResourceDes =
                            registryService.getComputeResource(resourceSchedulingModel.getResourceHostId());
                    QueueStatusModel queueStatusModel = registryService.getQueueStatus(
                            comResourceDes.getHostName(), resourceSchedulingModel.getQueueName());
                    if (queueStatusModel.isQueueUp()) {
                        return Optional.of(resourceSchedulingModel);
                    } else {
                        retries.add(key);
                    }
                }
            }

        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
        }

        return Optional.empty();
    }
}
