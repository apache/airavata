package org.apache.airavata.metascheduler.process.scheduling.api;

import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

import java.util.Optional;

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
public class ProcessSchedulerImpl implements ProcessScheduler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);

    @Override
    public Optional<ProcessModel> schedule(String processId) {
        final RegistryService.Client registryClient = Utils.getRegistryServiceClient();
        try {
            ProcessStatus processStatus = registryClient.getProcessStatus(processId);
            ProcessModel processModel = registryClient.getProcess(processId);
            if (processStatus.equals(ProcessState.CREATED)){
               ExperimentModel experiment = registryClient.getExperiment(processModel.getExperimentId());

               UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();
               ComputationalResourceSchedulingModel computationalResourceSchedulingModel =  userConfigurationDataModel
                       .getComputationalResourceScheduling();






            } else{
                // Just skip the scheduling logic and pass the Process Model
                return Optional.of(processModel);
            }


        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ProcessModel> reschedule(String processId) {
        return Optional.empty();
    }

}
