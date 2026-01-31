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
package org.apache.airavata.scheduling.policy;

import java.util.ArrayList;
import java.util.Optional;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This class implements selecting one compute resource out of enabled multiple compute resource polices.
 * Used for load testing; for normal use enable multiple compute resources in experiment creation.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(
        prefix = "services.scheduler",
        name = "selection-policy",
        havingValue = "MultipleComputeResourcePolicy")
public class MultipleComputeResourcePolicy extends ComputeResourceSelectionPolicyImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleComputeResourcePolicy.class);
    private final RegistryService registryService;

    public MultipleComputeResourcePolicy(RegistryService registryService) {
        super(registryService);
        this.registryService = registryService;
    }

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        try {

            var processModel = registryService.getProcess(processId);

            var experiment = registryService.getExperiment(processModel.getExperimentId());

            var userConfigurationDataModel = experiment.getUserConfigurationData();

            var resourceSchedulingModels = userConfigurationDataModel.getAutoScheduledCompResourceSchedulingList();

            var retries = new ArrayList<String>();

            while (retries.size() < resourceSchedulingModels.size()) {
                int upperbound = resourceSchedulingModels.size();
                int int_random =
                        java.util.concurrent.ThreadLocalRandom.current().nextInt(upperbound);
                var resourceSchedulingModel = resourceSchedulingModels.get(int_random);
                var key = resourceSchedulingModel.getResourceHostId() + "_" + resourceSchedulingModel.getQueueName();
                if (!retries.contains(key)) {
                    var comResourceDes =
                            registryService.getComputeResource(resourceSchedulingModel.getResourceHostId());
                    var queueStatusModel = registryService.getQueueStatus(
                            comResourceDes.getHostName(), resourceSchedulingModel.getQueueName());
                    if (queueStatusModel.getQueueUp()) {
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
