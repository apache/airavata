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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.*;
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
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class GfacServerHandler implements GfacService.Iface, Watcher{
    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(GfacServerHandler.class);

    private Registry registry;
    private AppCatalog appCatalog;

    private String registryURL;

    private String gatewayName;

    private String airavataUserName;

    private ZooKeeper zk;

    private boolean connected = false;

    private static Integer mutex = -1;

    private MonitorPublisher publisher;

    private String gfacServer;

    private String gfacExperiments;

    private String airavataServerHostPort;

    private List<Future> inHandlerFutures;

    private String nodeName = null;

    private CuratorFramework curatorFramework = null;

    private BlockingQueue<TaskSubmitEvent> taskSubmitEvents;

    private BlockingQueue<TaskTerminateEvent> taskTerminateEvents;

    private CuratorClient curatorClient;
    public GfacServerHandler() throws Exception{
        // registering with zk
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            airavataServerHostPort = ServerSettings.getSetting(Constants.GFAC_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.GFAC_SERVER_PORT);
            zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
            gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE, "/gfac-server");
            gfacExperiments = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            nodeName = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME);
            synchronized (mutex) {
                mutex.wait();  // waiting for the syncConnected event
            }
            storeServerConfig();
            logger.info("Finished starting ZK: " + zk);
            publisher = new MonitorPublisher(new EventBus());
            BetterGfacImpl.setMonitorPublisher(publisher);
            registry = RegistryFactory.getDefaultRegistry();
            appCatalog = AppCatalogFactory.getAppCatalog();
            setGatewayProperties();
            BetterGfacImpl.startDaemonHandlers();
            BetterGfacImpl.startStatusUpdators(registry, zk, publisher);
            inHandlerFutures = new ArrayList<Future>();

            if (ServerSettings.isGFacPassiveMode()) {
                taskSubmitEvents = new LinkedBlockingDeque<TaskSubmitEvent>();
                taskTerminateEvents = new LinkedBlockingDeque<TaskTerminateEvent>();
                curatorFramework = CuratorFrameworkFactory.newClient(AiravataZKUtils.getZKhostPort(), new ExponentialBackoffRetry(1000, 3));
                curatorClient = new CuratorClient(curatorFramework, nodeName);

                curatorFramework.start();
                curatorClient.start();
            }


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

    private void storeServerConfig() throws KeeperException, InterruptedException, ApplicationSettingsException {
        Stat zkStat = zk.exists(gfacServer, false);
        if (zkStat == null) {
            zk.create(gfacServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        String instanceId = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME);
        String instantNode = gfacServer + File.separator + instanceId;
        zkStat = zk.exists(instantNode, true);
        if (zkStat == null) {
            zk.create(instantNode,
                    airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
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
        }else{
            logger.error(" Zookeeper is inconsistent state  !!!!!");
        }
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex) {
            Event.KeeperState state = watchedEvent.getState();
            logger.info(state.name());
            if (state == Event.KeeperState.SyncConnected) {
                mutex.notify();
                connected = true;
            } else if(state == Event.KeeperState.Expired ||
                    state == Event.KeeperState.Disconnected){
                try {
                    mutex = -1;
                    zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(), 6000, this);
                    synchronized (mutex) {
                        mutex.wait();  // waiting for the syncConnected event
                    }
                    storeServerConfig();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } catch (ApplicationSettingsException e) {
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } catch (KeeperException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
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
    public boolean submitJob(String experimentId, String taskId, String gatewayId) throws TException {
        logger.infoId(experimentId, "GFac Received submit jog request for the Experiment: {} TaskId: {}", experimentId, taskId);
        GFac gfac = getGfac();
        InputHandlerWorker inputHandlerWorker = new InputHandlerWorker(gfac, experimentId, taskId, gatewayId);
//        try {
//            if( gfac.submitJob(experimentId, taskId, gatewayId)){
        logger.debugId(experimentId, "Submitted jog to the Gfac Implementation, experiment {}, task {}, gateway " +
                "{}", experimentId, taskId, gatewayId);
        inHandlerFutures.add(GFacThreadPoolExecutor.getFixedThreadPool().submit(inputHandlerWorker));
        // we immediately return when we have a threadpool
        return true;
    }

    public boolean cancelJob(String experimentId, String taskId) throws TException {
        logger.infoId(experimentId, "GFac Received cancel job request for Experiment: {} TaskId: {} ", experimentId, taskId);
        GFac gfac = getGfac();
        try {
            if (gfac.cancel(experimentId, taskId, ServerSettings.getDefaultUserGateway())) {
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

    private GFac getGfac()throws TException{
        try {
            return new BetterGfacImpl(registry, appCatalog, zk,publisher);
        } catch (Exception e) {
            throw new TException("Error initializing gfac instance",e);
        }
    }

    private class TaskLaunchMessageHandler implements MessageHandler {
        public static final String LAUNCH_TASK = "launch.task";
        public static final String TERMINATE_TASK = "teminate.task";
        public TaskLaunchMessageHandler(){

        }

        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            ArrayList<String> keys = new ArrayList<String>();
            keys.add(LAUNCH_TASK);
            keys.add(TERMINATE_TASK);
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, keys);
            return props;
        }

        public void onMessage(MessageContext message) {
            if (message.getType().equals(MessageType.LAUNCHTASK)) {
                try {
                    TaskSubmitEvent event = new TaskSubmitEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    taskSubmitEvents.add(event);


                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType());
                } catch (TException e) {
                    logger.error(e.getMessage(), e); //nobody is listening so nothing to throw
                }
            } else if (message.getType().equals(MessageType.TERMINATETASK)) {
                try {
                    TaskTerminateEvent event = new TaskTerminateEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    cancelJob(event.getExperimentId(), event.getTaskId());
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType());
                } catch (TException e) {
                    logger.error(e.getMessage(), e); //nobody is listening so nothing to throw
                }
            }
        }
    }

    public class CuratorClient extends LeaderSelectorListenerAdapter implements Closeable {
        private final String name;
        private final LeaderSelector leaderSelector;
        private final AtomicInteger leaderCount = new AtomicInteger();
        private final String path;
        private String experimentNode;

        public CuratorClient(CuratorFramework client, String name) {
            this.name = name;
            // create a leader selector using the given path for management
            // all participants in a given leader selection must use the same path
            // ExampleClient here is also a LeaderSelectorListener but this isn't required
            experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            path = experimentNode + File.separator + "leader";
            leaderSelector = new LeaderSelector(client, path, this);
            // for most cases you will want your instance to requeue when it relinquishes leadership
            leaderSelector.autoRequeue();
        }

        public void start() throws IOException {
            // the selection for this instance doesn't start until the leader selector is started
            // leader selection is done in the background so this call to leaderSelector.start() returns immediately
            leaderSelector.start();
        }

        @Override
        public void close() throws IOException {
            leaderSelector.close();
        }

        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
            // we are now the leader. This method should not return until we want to relinquish leadership
            final int waitSeconds = (int) (5 * Math.random()) + 1;

            logger.info(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
            logger.info(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
            RabbitMQTaskLaunchConsumer rabbitMQTaskLaunchConsumer = new RabbitMQTaskLaunchConsumer();
            String listenId = rabbitMQTaskLaunchConsumer.listen(new TaskLaunchMessageHandler());

            TaskSubmitEvent event = taskSubmitEvents.take();
            try {
                GFacUtils.createExperimentEntryForRPC(event.getExperimentId(),event.getTaskId(),client.getZookeeperClient().getZooKeeper(),experimentNode,name,event.getTokenId());
                submitJob(event.getExperimentId(), event.getTaskId(), event.getGatewayId());
                Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
            } catch (InterruptedException e) {
                logger.error(name + " was interrupted.");
                Thread.currentThread().interrupt();
            } finally {
                Thread.sleep(5);
                logger.info(name + " relinquishing leadership.: "+ new Date().toString());
                rabbitMQTaskLaunchConsumer.stopListen(listenId);
            }
        }
    }

}
