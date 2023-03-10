package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ComputeResourceSelectionPolicy;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.error.ExperimentNotFoundException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ExponentialBackOffReScheduler implements ReScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExponentialBackOffReScheduler.class);

    protected ThriftClientPool<RegistryService.Client> registryClientPool = Utils.getRegistryServiceClientPool();

    @Override
    public void reschedule(ProcessModel processModel, ProcessState processState) {
        RegistryService.Client client = null;
        try {
            client = this.registryClientPool.getResource();
            int maxReschedulingCount = ServerSettings.getMetaschedulerReschedulingThreshold();
            List<ProcessStatus> processStatusList = processModel.getProcessStatuses();
            ExperimentModel experimentModel = client.getExperiment(processModel.getExperimentId());
            LOGGER.debug("Rescheduling process with Id " + processModel.getProcessId() + " experimentId " +
                    processModel.getExperimentId());
            String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
            ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy) Class.forName(selectionPolicyClass)
                    .newInstance();
            if (processState.equals(ProcessState.QUEUED)) {
                Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel = policy.
                        selectComputeResource(processModel.getProcessId());

                if (computationalResourceSchedulingModel.isPresent()) {
                    updateResourceSchedulingModel(processModel,experimentModel,client);
                    Utils.updateProcessStatusAndPublishStatus(ProcessState.DEQUEUING, processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                }
            } else if (processState.equals(ProcessState.REQUEUED)) {
                int currentCount = getRequeuedCount(processStatusList);
                if (currentCount >= maxReschedulingCount) {
                    Utils.updateProcessStatusAndPublishStatus(ProcessState.FAILED, processModel.getProcessId(),
                            processModel.getExperimentId(),
                            experimentModel.getGatewayId());
                } else {

                    client.deleteJobs(processModel.getProcessId());
                    LOGGER.debug("Cleaned up current  job stack for process " + processModel.getProcessId() +
                            " experimentId " + processModel.getExperimentId());
                    ProcessStatus processStatus = client.getProcessStatus(processModel.getProcessId());
                    long pastValue = processStatus.getTimeOfStateChange();

                    int value = fib(currentCount);

                    long currentTime = System.currentTimeMillis();

                    double scanningInterval = ServerSettings.getMetaschedulerScanningInterval();

                    if (currentTime >= (pastValue + value * scanningInterval * 1000)) {
                        updateResourceSchedulingModel(processModel,experimentModel,client);
                        Utils.saveAndPublishProcessStatus(ProcessState.DEQUEUING, processModel.getProcessId(),
                                processModel.getExperimentId(),
                                experimentModel.getGatewayId());
                    }
                }
            }
            return;
        } catch (Exception exception) {
            if (client != null) {
                registryClientPool.returnBrokenResource(client);
                client = null;
            }
        } finally {
            if (client != null) {
                registryClientPool.returnResource(client);
            }
        }
    }


    private int getRequeuedCount(List<ProcessStatus> processStatusList) {
        return (int) processStatusList.stream().filter(x -> {
            if (x.getState().equals(ProcessState.REQUEUED)) {
                return true;
            }
            return false;
        }).count();
    }

    private int fib(int n) {
        if (n <= 1)
            return n;
        return fib(n - 1) + fib(n - 2);
    }


    private void updateResourceSchedulingModel(ProcessModel processModel, ExperimentModel experimentModel,
                                               RegistryService.Client registryClient) throws
            TException, ExperimentNotFoundException,ApplicationSettingsException, ClassNotFoundException, IllegalAccessException, InstantiationException, RegistryServiceException {
        String selectionPolicyClass = ServerSettings.getComputeResourceSelectionPolicyClass();
        ComputeResourceSelectionPolicy policy = (ComputeResourceSelectionPolicy) Class.forName(selectionPolicyClass)
                .newInstance();

        Optional<ComputationalResourceSchedulingModel> computationalResourceSchedulingModel = policy.
                selectComputeResource(processModel.getProcessId());

        if (computationalResourceSchedulingModel.isPresent()) {
            ComputationalResourceSchedulingModel resourceSchedulingModel = computationalResourceSchedulingModel.get();
            List<InputDataObjectType> inputDataObjectTypeList = experimentModel.getExperimentInputs();
            inputDataObjectTypeList.forEach(obj -> {
                if (obj.getName().equals("Wall_Time")) {
                    obj.setValue("-walltime=" + resourceSchedulingModel.getWallTimeLimit());
                }
                if (obj.getName().equals("Parallel_Group_Count")) {
                    obj.setValue("-mgroupcount=" + resourceSchedulingModel.getMGroupCount());
                }
            });

            List<InputDataObjectType> processInputDataObjectTypeList =  processModel.getProcessInputs();
            processInputDataObjectTypeList.forEach(obj->{
                if (obj.getName().equals("Wall_Time")){
                    obj.setValue("-walltime="+resourceSchedulingModel.getWallTimeLimit());
                }
                if (obj.getName().equals("Parallel_Group_Count")){
                    obj.setValue("-mgroupcount="+resourceSchedulingModel.getMGroupCount());
                }
            });

            processModel.setProcessInputs(processInputDataObjectTypeList);

            experimentModel.setExperimentInputs(inputDataObjectTypeList);
            processModel.setProcessResourceSchedule(resourceSchedulingModel);
            processModel.setComputeResourceId(resourceSchedulingModel.getResourceHostId());

            registryClient.updateProcess(processModel, processModel.getProcessId());
            registryClient.updateExperiment(processModel.getExperimentId(), experimentModel);
        }
    }
}
