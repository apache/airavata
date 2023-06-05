package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.QueueStatusModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This class implements selecting one compute resource out of enabled multiple compute resource polices.
 * //TODO: implemented for load testing, for proper usecases airavata should enable multiple compute resources in Experiment creation
 */
public class MultipleComputeResourcePolicy extends ComputeResourceSelectionPolicyImpl  {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleComputeResourcePolicy.class);

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        RegistryService.Client registryClient = super.registryClientPool.getResource();
        try {

                ProcessModel processModel = registryClient.getProcess(processId);


                ExperimentModel experiment = registryClient.getExperiment(processModel.getExperimentId());


                UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();


                List<ComputationalResourceSchedulingModel> resourceSchedulingModels =
                        userConfigurationDataModel.getAutoScheduledCompResourceSchedulingList();

                List<String> retries = new ArrayList<>();

                while (retries.size()<resourceSchedulingModels.size()) {
                    Random rand = new Random();
                    int upperbound = resourceSchedulingModels.size();
                    int int_random = rand.nextInt(upperbound);
                    ComputationalResourceSchedulingModel resourceSchedulingModel = resourceSchedulingModels.get(int_random);
                    String key = resourceSchedulingModel.getResourceHostId()+"_"+resourceSchedulingModel.getQueueName();
                    if(!retries.contains(key)){
                        ComputeResourceDescription comResourceDes = registryClient.getComputeResource(resourceSchedulingModel.getResourceHostId());
                        QueueStatusModel queueStatusModel = registryClient.getQueueStatus(comResourceDes.getHostName(),
                                resourceSchedulingModel.getQueueName());
                        if (queueStatusModel.isQueueUp()) {
                            return Optional.of(resourceSchedulingModel);
                        }else{
                            retries.add(key);
                        }
                    }
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
