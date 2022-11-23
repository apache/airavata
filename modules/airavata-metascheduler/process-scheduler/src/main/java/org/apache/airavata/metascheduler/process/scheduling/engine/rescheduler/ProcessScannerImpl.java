package org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.metascheduler.core.engine.ProcessScanner;
import org.apache.airavata.metascheduler.core.engine.ReScheduler;
import org.apache.airavata.metascheduler.core.utils.Utils;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessScannerImpl implements ProcessScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScannerImpl.class);

    protected ThriftClientPool<RegistryService.Client> registryClientPool = Utils.getRegistryServiceClientPool();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RegistryService.Client client = null;
        try {
            LOGGER.debug("Executing Process scanner ....... ");
            client = this.registryClientPool.getResource();
            ProcessState state = ProcessState.QUEUED;
            List<ProcessModel> processModelList = client.getProcessStatusList(state);

            String reSchedulerPolicyClass = ServerSettings.getReSchedulerPolicyClass();
            ReScheduler reScheduler = (ReScheduler) Class.forName(reSchedulerPolicyClass).newInstance();

            for (ProcessModel processModel : processModelList) {
                reScheduler.reschedule(processModel);
            }

        } catch (Exception ex) {
            String msg = "Error occurred while executing job" + ex.getMessage();
            LOGGER.error(msg, ex);
        } finally {
            if (client != null) {
                registryClientPool.returnResource(client);
            }
        }


    }
}
