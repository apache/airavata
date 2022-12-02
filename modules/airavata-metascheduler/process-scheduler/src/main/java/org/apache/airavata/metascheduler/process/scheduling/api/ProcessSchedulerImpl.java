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

import java.util.List;
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
    public boolean canLaunch(String experimentId) {
        final RegistryService.Client registryClient = this.registryClientPool.getResource();
        try {
            List<ProcessModel> processModels = registryClient.getProcessList(experimentId);
            boolean allProcessesScheduled = true;

            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy) Class.forName(selectionPolicyClass).newInstance();

            for(ProcessModel processModel:processModels) {
                ProcessStatus processStatus = registryClient.getProcessStatus(processModel.getProcessId());

                if (processStatus.getState().equals(ProcessState.CREATED) || processStatus.getState().equals(ProcessState.VALIDATED)) {

                    Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel = policy.
                            selectComputeResource(processModel.getProcessId());

                    if (computationalResourceSchedulingModel.isPresent()) {
                        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel.get());
                        registryClient.updateProcess(processModel, processModel.getProcessId());
                    } else {
                        ProcessStatus newProcessStatus = new ProcessStatus();
                        newProcessStatus.setState(ProcessState.QUEUED);
                        registryClient.updateProcessStatus(newProcessStatus,processModel.getProcessId());
                        allProcessesScheduled = false;
                    }
                }
            }
            return allProcessesScheduled;
        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling experiment with Id {}", experimentId, exception);
        } finally {
            this.registryClientPool.returnResource(registryClient);
        }

        return false;
    }

    @Override
    public boolean reschedule(String experimentId) {
        return false;
    }

}
