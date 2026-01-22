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

import java.util.Optional;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This class implements selecting compute resource defined in USER_CONFIGURATION_DATA and assumes only one
 * compute resource is selected for experiment.
 * This checks whether defined CR is live
 */
@Component
@Profile("!test")
@ConditionalOnProperty(
        prefix = "services.scheduler",
        name = "selection-policy",
        havingValue = "DefaultComputeResourceSelectionPolicy")
public class DefaultComputeResourceSelectionPolicy extends ComputeResourceSelectionPolicyImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComputeResourceSelectionPolicy.class);
    private final RegistryService registryService;

    public DefaultComputeResourceSelectionPolicy(RegistryService registryService) {
        super(registryService);
        this.registryService = registryService;
    }

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        try {
            var processModel = registryService.getProcess(processId);

            var experiment = registryService.getExperiment(processModel.getExperimentId());

            var userConfigurationDataModel = experiment.getUserConfigurationData();

            // Assume scheduling data is populated in USER_CONFIGURATION_DATA_MODEL
            var computationalResourceSchedulingModel = userConfigurationDataModel.getComputationalResourceScheduling();

            var computeResourceId = computationalResourceSchedulingModel.getResourceHostId();

            var comResourceDes = registryService.getComputeResource(computeResourceId);

            var hostName = comResourceDes.getHostName();
            var queueName = computationalResourceSchedulingModel.getQueueName();

            var queueStatusModel = registryService.getQueueStatus(hostName, queueName);
            if (queueStatusModel.getQueueUp()) {
                return Optional.of(computationalResourceSchedulingModel);
            }
        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
        }
        return Optional.empty();
    }
}
