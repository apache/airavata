package org.apache.airavata.metascheduler.process.scheduling.api;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.api.ProcessScheduler;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * This class provides implementation of the ProcessSchedule Interface
 */
public class ProcessSchedulerImpl implements ProcessScheduler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProcessSchedulerImpl.class);

    private ThriftClientPool<RegistryService.Client> registryClientPool;

    public ProcessSchedulerImpl() {
        try {
            registryClientPool = Utils.getRegistryServiceClientPool();
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching registry client pool", e);
        }
    }


    @Override
    public Optional<ProcessModel> schedule(String processId) {
        final RegistryService.Client registryClient = this.registryClientPool.getResource();
        try {
            ProcessStatus processStatus = registryClient.getProcessStatus(processId);
            ProcessModel processModel = registryClient.getProcess(processId);
            if (processStatus.equals(ProcessState.CREATED)) {

                String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();

                ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy) Class.forName(selectionPolicyClass).newInstance();
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel = policy.selectComputeResource(processId);

                if (computationalResourceSchedulingModel.isPresent()) {
                    processModel.setProcessResourceSchedule(computationalResourceSchedulingModel.get());
                }
            }
            return Optional.of(processModel);


        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
        } finally {
            this.registryClientPool.returnResource(registryClient);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ProcessModel> reschedule(String processId) {
        return Optional.empty();
    }

}
