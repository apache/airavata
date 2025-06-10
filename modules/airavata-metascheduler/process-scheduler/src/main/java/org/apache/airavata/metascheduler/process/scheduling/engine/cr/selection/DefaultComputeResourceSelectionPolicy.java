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

import java.util.Optional;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements selecting compute resource defined in USER_CONFIGURATION_DATA and assumes only one
 * compute resource is selected for experiment.
 * This checks whether defined CR is live
 */
public class DefaultComputeResourceSelectionPolicy extends ComputeResourceSelectionPolicyImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComputeResourceSelectionPolicy.class);

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        RegistryService.Client registryClient = this.registryClientPool.getResource();
        try {
            ProcessModel processModel = registryClient.getProcess(processId);

            ExperimentModel experiment = registryClient.getExperiment(processModel.getExperimentId());

            UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();

            // Assume scheduling data is populated in USER_CONFIGURATION_DATA_MODEL
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                    userConfigurationDataModel.getComputationalResourceScheduling();

            String computeResourceId = computationalResourceSchedulingModel.getResourceHostId();

            ComputeResourceDescription comResourceDes = registryClient.getComputeResource(computeResourceId);

            GroupComputeResourcePreference computeResourcePreference =
                    getGroupComputeResourcePreference(computeResourceId, processModel.getGroupResourceProfileId());

            String hostName = comResourceDes.getHostName();
            String queueName = computationalResourceSchedulingModel.getQueueName();

            QueueStatusModel queueStatusModel = registryClient.getQueueStatus(hostName, queueName);
            if (queueStatusModel.isQueueUp()) {
                return Optional.of(computationalResourceSchedulingModel);
            }
        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
            this.registryClientPool.returnBrokenResource(registryClient);
            registryClient = null;
        } finally {
            if (registryClient != null) {
                this.registryClientPool.returnResource(registryClient);
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        DefaultComputeResourceSelectionPolicy defaultComputeResourceSelectionPolicy =
                new DefaultComputeResourceSelectionPolicy();
        defaultComputeResourceSelectionPolicy.selectComputeResource("PROCESS_5dd4f56b-f0fd-41d0-9437-693ad25f4a1d");
    }
}
