/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.gfac.server;

import com.google.common.eventbus.EventBus;
import org.airavata.appcatalog.cpi.AppCatalog;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.InputHandlerWorker;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpi_serviceConstants;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQTaskLaunchConsumer;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskTerminateEvent;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.ExperimentStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;


public class GfacServerHandler implements GfacService.Iface {
    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(GfacServerHandler.class);
    private static RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer;
    private static int requestCount=0;
    private Registry registry;
    private AppCatalog appCatalog;
    private String gatewayName;
    private String airavataUserName;
//    private ZooKeeper zk;
    private CuratorFramework curatorClient;
    private static Integer mutex = -1;
    private static Lock lock;
    private MonitorPublisher publisher;
    private String gfacServer;
    private String gfacExperiments;
    private String airavataServerHostPort;
    private BlockingQueue<TaskSubmitEvent> taskSubmitEvents;

    public GfacServerHandler() throws Exception {
        try {
            // start curator client
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
            curatorClient = CuratorFrameworkFactory.newClient(zkhostPort, retryPolicy);
            curatorClient.start();
            gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            gfacExperiments = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            airavataServerHostPort = ServerSettings.getSetting(Constants.GFAC_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.GFAC_SERVER_PORT);
            storeServerConfig();
            publisher = new MonitorPublisher(new EventBus());
            BetterGfacImpl.setMonitorPublisher(publisher);
            registry = RegistryFactory.getDefaultRegistry();
            appCatalog = AppCatalogFactory.getAppCatalog();
            setGatewayProperties();
            BetterGfacImpl.startDaemonHandlers();

            if (ServerSettings.isGFacPassiveMode()) {
                rabbitMQTaskLaunchConsumer = new RabbitMQTaskLaunchConsumer();
                rabbitMQTaskLaunchConsumer.listen(new TaskLaunchMessageHandler());
            }
            BetterGfacImpl.startStatusUpdators(registry, curatorClient, publisher, rabbitMQTaskLaunchConsumer);
        } catch (Exception e) {
            throw new Exception("Error initialising GFAC", e);
        }
    }

    public static void main(String[] args) {
        RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer = null;
        try {
            rabbitMQTaskLaunchConsumer = new RabbitMQTaskLaunchConsumer();
            rabbitMQTaskLaunchConsumer.listen(new TestHandler());
        } catch (AiravataException e) {
            logger.error(e.getMessage(), e);
        }
    }
    private void storeServerConfig() throws Exception {
        Stat stat = curatorClient.checkExists().forPath(gfacServer);
        if (stat == null) {
            curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(gfacServer, new byte[0]);
        }
        String instanceId = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME);
        String instanceNode = gfacServer + File.separator + instanceId;
        stat = curatorClient.checkExists().forPath(instanceNode);
        if (stat == null) {
            curatorClient.create().withMode(CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(instanceNode, airavataServerHostPort.getBytes());
            curatorClient.getChildren().watched().forPath(instanceNode);
        }
        stat = curatorClient.checkExists().forPath(gfacExperiments);
        if (stat == null) {
            curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(gfacExperiments, airavataServerHostPort.getBytes());
        }
        stat = curatorClient.checkExists().forPath(gfacExperiments + File.separator + instanceId);
        if (stat == null) {
            curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(gfacExperiments + File.separator + instanceId, airavataServerHostPort.getBytes());
        }
    }

    private long ByateArrayToLong(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++)
        {
            value += ((long) data[i] & 0xffL) << (8 * i);
        }
        return value;
    }

    public String getGFACServiceVersion() throws TException {
        return gfac_cpi_serviceConstants.GFAC_CPI_VERSION;
    }

    /**
     * * After creating the experiment Data and Task Data in the orchestrator
     * * Orchestrator has to invoke this operation for each Task per experiment to run
     * * the actual Job related actions.
     * *
     * * @param experimentID
     * * @param taskID
     * * @param gatewayId:
     * *  The GatewayId is inferred from security context and passed onto gfac.
     * * @return sucess/failure
     * *
     * *
     *
     * @param experimentId
     * @param taskId
     * @param gatewayId
     */
    public boolean submitJob(String experimentId, String taskId, String gatewayId, String tokenId) throws TException {
        requestCount++;
        logger.info("-----------------------------------------------------" + requestCount + "-----------------------------------------------------");
        logger.infoId(experimentId, "GFac Received submit job request for the Experiment: {} TaskId: {}", experimentId, taskId);
        GFac gfac = getGfac();
        InputHandlerWorker inputHandlerWorker = new InputHandlerWorker(gfac, experimentId, taskId, gatewayId, tokenId);
//        try {
//            if( gfac.submitJob(experimentId, taskId, gatewayId)){
        logger.debugId(experimentId, "Submitted job to the Gfac Implementation, experiment {}, task {}, gateway " +
                "{}", experimentId, taskId, gatewayId);

        GFacThreadPoolExecutor.getCachedThreadPool().execute(inputHandlerWorker);

        // we immediately return when we have a threadpool
        return true;
    }

    public boolean cancelJob(String experimentId, String taskId, String gatewayId, String tokenId) throws TException {
        logger.infoId(experimentId, "GFac Received cancel job request for Experiment: {} TaskId: {} ", experimentId, taskId);
        GFac gfac = getGfac();
        try {
            if (gfac.cancel(experimentId, taskId, gatewayId, tokenId)) {
                logger.debugId(experimentId, "Successfully cancelled job, experiment {} , task {}", experimentId, taskId);
                return true;
            } else {
                logger.errorId(experimentId, "Job cancellation failed, experiment {} , task {}", experimentId, taskId);
                return false;
            }
        } catch (Exception e) {
            logger.errorId(experimentId, "Error cancelling the experiment {}.", experimentId);
            throw new TException("Error cancelling the experiment : " + e.getMessage(), e);
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getAiravataUserName() {
        return airavataUserName;
    }

    public void setAiravataUserName(String airavataUserName) {
        this.airavataUserName = airavataUserName;
    }

    protected void setGatewayProperties() throws ApplicationSettingsException {
        setAiravataUserName(ServerSettings.getDefaultUser());
        setGatewayName(ServerSettings.getDefaultUserGateway());
    }

    private GFac getGfac() throws TException {
        try {
            return new BetterGfacImpl(registry, appCatalog, curatorClient, publisher);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    private static  class TestHandler implements MessageHandler{
        @Override
        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(ServerSettings.getLaunchQueueName());
            keys.add(ServerSettings.getCancelQueueName());
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, keys);
            props.put(MessagingConstants.RABBIT_QUEUE, ServerSettings.getLaunchQueueName());
            return props;
        }

        @Override
        public void onMessage(MessageContext message) {
            TaskSubmitEvent event = new TaskSubmitEvent();
            TBase messageEvent = message.getEvent();
            byte[] bytes = new byte[0];
            try {
                bytes = ThriftUtils.serializeThriftObject(messageEvent);
                ThriftUtils.createThriftFromBytes(bytes, event);
                System.out.println(event.getExperimentId());
            } catch (TException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private class TaskLaunchMessageHandler implements MessageHandler {
        private String experimentNode;
        private String nodeName;

        public TaskLaunchMessageHandler() {
            experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            nodeName = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME,"gfac-node0");
        }

        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(ServerSettings.getLaunchQueueName());
            keys.add(ServerSettings.getCancelQueueName());
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, keys);
            props.put(MessagingConstants.RABBIT_QUEUE, ServerSettings.getLaunchQueueName());
            return props;
        }

        public void onMessage(MessageContext message) {
            System.out.println(" Message Received with message id '" + message.getMessageId()
                    + "' and with message type '" + message.getType());
            if (message.getType().equals(MessageType.LAUNCHTASK)) {
                try {
                    TaskSubmitEvent event = new TaskSubmitEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    // update experiment status to executing
                    ExperimentStatus status = new ExperimentStatus();
                    status.setExperimentState(ExperimentState.EXECUTING);
                    status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                    registry.update(RegistryModelType.EXPERIMENT_STATUS, status, event.getExperimentId());
                    experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
                    try {
                        GFacUtils.createExperimentEntryForPassive(event.getExperimentId(), event.getTaskId(), curatorClient,
                                experimentNode, nodeName, event.getTokenId(), message.getDeliveryTag());
                        AiravataZKUtils.getExpStatePath(event.getExperimentId());
                        submitJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId(), event.getTokenId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        String experimentPath = experimentNode + File.separator + nodeName + File.separator + event.getExperimentId();
                        logger.info("GFacServerHandler Launchtask - Acknowledging delivery Tag - " + message.getDeliveryTag());
                        rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                        try {
                            if (curatorClient.checkExists().forPath(experimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX) != null) {
                                ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(),
                                        experimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, true);
                            }

                            if (curatorClient.checkExists().forPath(experimentPath) != null) {
                                ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), experimentPath, true);
                            }
                        } catch (Exception e1) {
                            logger.error("Error while deleting experiment node in zookeeper, expId : {}" , experimentPath);
                        }
                    }
                } catch (TException e) {
                    logger.error(e.getMessage(), e); //nobody is listening so nothing to throw
                } catch (RegistryException e) {
                    logger.error("Error while updating experiment status", e);
                }
            } else if (message.getType().equals(MessageType.TERMINATETASK)) {
                boolean cancelSuccess = false;
                TaskTerminateEvent event = new TaskTerminateEvent();
                TBase messageEvent = message.getEvent();
                try {
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    boolean saveDeliveryTagSuccess = GFacUtils.setExperimentCancel(event.getExperimentId(), curatorClient, message.getDeliveryTag());
                    if (saveDeliveryTagSuccess) {
                        cancelSuccess = cancelJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId(), event.getTokenId());
                        System.out.println(" Message Received with message id '" + message.getMessageId()
                                + "' and with message type '" + message.getType());
                    } else {
                        throw new GFacException("Terminate Task fail to save delivery tag : " + String.valueOf(message.getDeliveryTag()) + " \n" +
                                "This happens when another cancel operation is being processed or experiment is in one of final states, complete|failed|cancelled.");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }finally {
                    if (cancelSuccess) {
                        // if cancel success , AiravataExperimentStatusUpdator will send an ack to this message.
                    } else {
                        try {
                            if (GFacUtils.ackCancelRequest(event.getExperimentId(), curatorClient)) {
                                if (!rabbitMQTaskLaunchConsumer.isOpen()) {
                                    rabbitMQTaskLaunchConsumer.reconnect();
                                }
                                logger.info("GFacServerHandler TerminateTask - Acknowledging cancel delivery Tag - " + message.getDeliveryTag());
                                rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                            }
                        } catch (Exception e) {
                            logger.error("Error while ack to cancel request, experimentId: " + event.getExperimentId());
                        }
                    }
                }
            }
        }
    }
}
