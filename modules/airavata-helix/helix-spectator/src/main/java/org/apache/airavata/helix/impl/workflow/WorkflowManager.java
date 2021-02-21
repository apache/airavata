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
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.manager.zk.ZNRecordSerializer;
import org.apache.helix.manager.zk.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

    private Publisher statusPublisher;
    private CuratorFramework curatorClient = null;
    private List<WorkflowOperator> workflowOperators = new ArrayList<>();
    private ThriftClientPool<RegistryService.Client> registryClientPool;
    private String workflowManagerName;
    private ZKHelixAdmin zkHelixAdmin;
    private boolean loadBalanceClusters;

    private int currentOperator = 0;

    public WorkflowManager(String workflowManagerName, boolean loadBalanceClusters) {
        this.workflowManagerName = workflowManagerName;
        this.loadBalanceClusters = loadBalanceClusters;
    }

    protected void initComponents() throws Exception {
        initRegistryClientPool();
        initHelixAdmin();
        initWorkflowOperators();
        initStatusPublisher();
        initCuratorClient();

    }

    private void initWorkflowOperators() throws Exception {

        if (!loadBalanceClusters) {
            logger.info("Using default cluster " + ServerSettings.getSetting("helix.cluster.name") + " to submit workflows");
            workflowOperators.add(new WorkflowOperator(
                    ServerSettings.getSetting("helix.cluster.name"),
                    workflowManagerName,
                    ServerSettings.getZookeeperConnection()));
        } else {
            logger.info("Load balancing workflows among existing clusters");
            List<String> clusters = zkHelixAdmin.getClusters();
            logger.info("Total available clusters " + clusters.size());
            for (String cluster : clusters) {
                workflowOperators.add(new WorkflowOperator(
                        cluster,
                        workflowManagerName,
                        ServerSettings.getZookeeperConnection()));
            }
        }
    }

    private void initStatusPublisher() throws AiravataException {
        this.statusPublisher = MessagingFactory.getPublisher(Type.STATUS);
    }

    private void initCuratorClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorClient = CuratorFrameworkFactory.newClient(ServerSettings.getZookeeperConnection(), retryPolicy);
        this.curatorClient.start();
    }

    private void initHelixAdmin() throws ApplicationSettingsException {
        ZkClient zkClient = new ZkClient(ServerSettings.getZookeeperConnection(), ZkClient.DEFAULT_SESSION_TIMEOUT,
                ZkClient.DEFAULT_CONNECTION_TIMEOUT, new ZNRecordSerializer());
        zkHelixAdmin = new ZKHelixAdmin(zkClient);
    }
    private void initRegistryClientPool() throws ApplicationSettingsException {

        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(100);
        poolConfig.setMinIdle(5);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);

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
        currentOperator++;
        if (workflowOperators.size() <= currentOperator) {
            currentOperator = 0;
        }
        return workflowOperators.get(currentOperator);
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

    public String normalizeTaskId(String taskId) {
        return taskId.replace(":", "-").replace(",", "-");
    }

    protected void registerWorkflowForProcess(String processId, String workflowName, String workflowType) {
        RegistryService.Client registryClient = getRegistryClientPool().getResource();
        try {
            ProcessWorkflow processWorkflow = new ProcessWorkflow();
            processWorkflow.setProcessId(processId);
            processWorkflow.setWorkflowId(workflowName);
            processWorkflow.setType(workflowType);
            processWorkflow.setCreationTime(System.currentTimeMillis());
            registryClient.addProcessWorkflow(processWorkflow);
            getRegistryClientPool().returnResource(registryClient);

        } catch (Exception e) {
            logger.error("Failed to save workflow " + workflowName + " of process " + processId + ". This will affect cancellation tasks", e);
            getRegistryClientPool().returnBrokenResource(registryClient);
        }
    }
}
