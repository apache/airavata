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
import org.airavata.appcatalog.cpi.AppCatalogException;
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
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.ExperimentStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.persistance.registry.jpa.model.Status;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;


public class GfacServerHandler implements GfacService.Iface, Watcher {
    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(GfacServerHandler.class);

    private static RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer;

    private static int requestCount=0;

    private Registry registry;
    private AppCatalog appCatalog;

    private String gatewayName;

    private String airavataUserName;

    private ZooKeeper zk;

    private static Integer mutex = -1;

    private static Lock lock;

    private MonitorPublisher publisher;

    private String gfacServer;

    private String gfacExperiments;

    private String airavataServerHostPort;


    private BlockingQueue<TaskSubmitEvent> taskSubmitEvents;

    public GfacServerHandler() throws Exception {
        // registering with zk
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            airavataServerHostPort = ServerSettings.getSetting(Constants.GFAC_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.GFAC_SERVER_PORT);
            zk = new ZooKeeper(zkhostPort, AiravataZKUtils.getZKTimeout(), this);   // no watcher is required, this will only use to store some data
            gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            gfacExperiments = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            logger.info("Waiting for zookeeper to connect to the server");
            synchronized (mutex) {
                mutex.wait(5000);  // waiting for the syncConnected event
            }
            storeServerConfig();
            logger.info("Finished starting ZK: " + zk);
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
            BetterGfacImpl.startStatusUpdators(registry, zk, publisher, rabbitMQTaskLaunchConsumer);
        } catch (ApplicationSettingsException e) {
            logger.error("Error initialising GFAC", e);
            throw new Exception("Error initialising GFAC", e);
        } catch (InterruptedException e) {
            logger.error("Error initialising GFAC", e);
            throw new Exception("Error initialising GFAC", e);
        } catch (AppCatalogException e) {
            logger.error("Error initialising GFAC", e);
            throw new Exception("Error initialising GFAC", e);
        } catch (RegistryException e) {
            logger.error("Error initialising GFAC", e);
            throw new Exception("Error initialising GFAC", e);
        } catch (KeeperException e) {
            logger.error("Error initialising GFAC", e);
            throw new Exception("Error initialising GFAC", e);
        } catch (IOException e) {
            logger.error("Error initialising GFAC", e);
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
    private void storeServerConfig() throws KeeperException, InterruptedException, ApplicationSettingsException {
        Stat zkStat = zk.exists(gfacServer, false);
        if (zkStat == null) {
            zk.create(gfacServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        String instanceId = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME);
        String instanceNode = gfacServer + File.separator + instanceId;
        zkStat = zk.exists(instanceNode, true);
        if (zkStat == null) {
            zk.create(instanceNode,
                    airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
            zk.getChildren(instanceNode, true);
        }
        zkStat = zk.exists(gfacExperiments, false);
        if (zkStat == null) {
            zk.create(gfacExperiments,
                    airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        zkStat = zk.exists(gfacExperiments + File.separator + instanceId, false);
        if (zkStat == null) {
            zk.create(gfacExperiments + File.separator + instanceId,
                    airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        } else {
            logger.error(" Zookeeper is inconsistent state  !!!!!");
        }
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        logger.info(watchedEvent.getPath());
        logger.info(watchedEvent.getType().toString());
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            logger.info(state.name());
            switch (state){
                case SyncConnected:
                    mutex.notify();
                    break;
                case Expired:case Disconnected:
                   logger.info("ZK Connection is "+ state.toString());
                    try {
                        zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(), AiravataZKUtils.getZKTimeout(), this);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    } catch (ApplicationSettingsException e) {
                        logger.error(e.getMessage(), e);
                    }
//                    synchronized (mutex) {
//                        mutex.wait(5000);  // waiting for the syncConnected event
//                    }
            }
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
            return new BetterGfacImpl(registry, appCatalog,null , publisher);

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
                        GFacUtils.createExperimentEntryForPassive(event.getExperimentId(), event.getTaskId(), zk, experimentNode, nodeName, event.getTokenId(), message.getDeliveryTag());
                        AiravataZKUtils.getExpStatePath(event.getExperimentId());
                        submitJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId(), event.getTokenId());
                    } catch (KeeperException e) {
                        logger.error(nodeName + " was interrupted.");
                        rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                        rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                    } catch (ApplicationSettingsException e) {
                        logger.error(e.getMessage(), e);
                        rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                    }
                } catch (TException e) {
                    logger.error(e.getMessage(), e); //nobody is listening so nothing to throw
                } catch (RegistryException e) {
                    logger.error("Error while updating experiment status", e);
                }
            } else if (message.getType().equals(MessageType.TERMINATETASK)) {
                try {
                    TaskTerminateEvent event = new TaskTerminateEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    GFacUtils.setExperimentCancel(event.getExperimentId(), event.getTaskId(), zk);
                    AiravataZKUtils.getExpStatePath(event.getExperimentId());
                    cancelJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId(), event.getTokenId());
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType());
                } catch (TException e) {
                    logger.error(e.getMessage(), e); //nobody is listening so nothing to throw
                    rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                } catch (ApplicationSettingsException e) {
                    logger.error(e.getMessage(), e);
                    rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                } catch (KeeperException e) {
                    logger.error(e.getMessage(), e);
                    rabbitMQTaskLaunchConsumer.sendAck(message.getDeliveryTag());
                }
            }
        }
    }
}
