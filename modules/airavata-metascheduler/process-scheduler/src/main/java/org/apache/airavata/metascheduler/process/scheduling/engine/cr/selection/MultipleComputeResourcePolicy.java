package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.QueueStatusModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

import java.util.List;
import java.util.Optional;

/**
 * This class implements selecting one compute resource out of enabled multiple compute resource polices.
 * //TODO: implemented for load testing, for proper usecases airavata should enable multiple compute resources in Experiment creation
 */
public class MultipleComputeResourcePolicy extends DefaultComputeResourceSelectionPolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleComputeResourcePolicy.class);

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        RegistryService.Client registryClient = super.registryClientPool.getResource();
        try {

            Optional<ComputationalResourceSchedulingModel> optionalComputationalResourceSchedulingModel =
                    super.selectComputeResource(processId);

            if (optionalComputationalResourceSchedulingModel.isPresent()) {
                return optionalComputationalResourceSchedulingModel;
            } else {

            }
            ProcessModel processModel = registryClient.getProcess(processId);

            ExperimentModel experiment = registryClient.getExperiment(processModel.getExperimentId());


            UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();

            // Assume scheduling data is populated in USER_CONFIGURATION_DATA_MODEL
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel = userConfigurationDataModel
                    .getComputationalResourceScheduling();

            int crPoolFraction = ServerSettings.getMetaschedulerMultipleCREnablingFactor();

            List<ComputeResourcePolicy> policyList = registryClient.
                    getGroupComputeResourcePolicyList(processModel.getGroupResourceProfileId());

            int count = 0;
            int maxCount = (int) (policyList.size() * crPoolFraction);

            while (count < maxCount) {
                ComputeResourcePolicy resourcePolicy = policyList.get(count);
                List<String> queues = resourcePolicy.getAllowedBatchQueues();

                String computeResourceId = resourcePolicy.getComputeResourceId();
                ComputeResourceDescription comResourceDes = registryClient.getComputeResource(computeResourceId);

                if (!queues.isEmpty()) {
                    QueueStatusModel queueStatusModel = registryClient.getQueueStatus(comResourceDes.getHostName(), queues.get(0));
                    if (queueStatusModel.isQueueUp()) {
                        return Optional.of(computationalResourceSchedulingModel);
                    }
                }
                count++;
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
}
