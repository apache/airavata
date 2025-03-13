package org.apache.airavata.file.server.service;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.impl.task.staging.OutputDataStagingTask;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ProcessDataManager extends OutputDataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(ProcessDataManager.class);

    private String processId;
    private AdaptorSupport adaptorSupport;

    private ProcessModel process;
    ExperimentModel experiment;

    public ProcessDataManager(ThriftClientPool<RegistryService.Client> registryClientPool,
                              String processId, AdaptorSupport adaptorSupport) throws Exception {

        this.adaptorSupport = adaptorSupport;
        RegistryService.Client regClient = registryClientPool.getResource();
        try {
            process = regClient.getProcess(processId);
            experiment = regClient.getExperiment(process.getExperimentId());

            setTaskId(UUID.randomUUID().toString());
            setProcessId(processId);
            setExperimentId(process.getExperimentId());
            setGatewayId(experiment.getGatewayId());
            loadContext();

            registryClientPool.returnResource(regClient);
        } catch (Exception e) {
            logger.error("Failed to initialize the output data mover for process {}", processId, e);
            registryClientPool.returnBrokenResource(regClient);
            throw e;
        }
        this.processId = processId;
    }

    public AgentAdaptor getAgentAdaptor() throws TaskOnFailException {
        return getComputeResourceAdaptor(adaptorSupport);
    }

    public String getBaseDir() throws Exception {
        return getTaskContext().getWorkingDir();
    }
}
