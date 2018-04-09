package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.helix.workflow.WorkflowOperator;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessIdentifier;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;
    private CuratorFramework curatorClient = null;
    private WorkflowOperator workflowOperator;
    private ThriftClientPool<RegistryService.Client> registryClientPool;
    private String workflowManagerName;

    public WorkflowManager(String workflowManagerName) {
        this.workflowManagerName = workflowManagerName;
    }

    protected void initComponents() throws Exception {
        initRegistryClientPool();
        initWorkflowOperatorr();
        initStatusPublisher();
        initCuratorClient();
    }

    private void initWorkflowOperatorr() throws Exception {
        workflowOperator = new WorkflowOperator(
                ServerSettings.getSetting("helix.cluster.name"),
                workflowManagerName,
                ServerSettings.getZookeeperConnection());
    }

    private void initStatusPublisher() throws AiravataException {
        this.statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
    }

    private void initCuratorClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private void initRegistryClientPool() throws ApplicationSettingsException {

        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.maxActive = 100;
        poolConfig.minIdle = 5;
        poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        poolConfig.testOnBorrow = true;
        poolConfig.testWhileIdle = true;
        poolConfig.numTestsPerEvictionRun = 10;
        poolConfig.maxWait = 3000;

        this.registryClientPool = new ThriftClientPool<>(
                RegistryService.Client::new, poolConfig, ServerSettings.getRegistryServerHost(),
                Integer.parseInt(ServerSettings.getRegistryServerPort()));
    }

    public Publisher getStatusPublisher() {
        return statusPublisher;
    }

    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public WorkflowOperator getWorkflowOperator() {
        return workflowOperator;
    }

    public ThriftClientPool<RegistryService.Client> getRegistryClientPool() {
        return registryClientPool;
    }

    public void publishProcessStatus(String processId, String experimentId, String gatewayId, ProcessState state)
            throws AiravataException {

        ProcessStatus status = new ProcessStatus();
        status.setState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());

        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        try {
            registryClient.updateProcessStatus(status, processId);
            getRegistryClientPool().returnResource(registryClient);

        } catch (Exception e) {
            logger.error("Failed to update process status " + processId, e);
            getRegistryClientPool().returnBrokenResource(registryClient);
        }

        ProcessIdentifier identifier = new ProcessIdentifier(processId, experimentId, gatewayId);
        ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(status.getState(), identifier);
        MessageContext msgCtx = new MessageContext(processStatusChangeEvent, MessageType.PROCESS,
                AiravataUtils.getId(MessageType.PROCESS.name()), gatewayId);
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
        getStatusPublisher().publish(msgCtx);
    }
}
