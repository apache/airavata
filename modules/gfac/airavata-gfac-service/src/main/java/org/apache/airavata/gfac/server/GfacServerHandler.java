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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.InputHandlerWorker;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.cpi.gfac_cpi_serviceConstants;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.thrift.TException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class GfacServerHandler implements GfacService.Iface, Watcher{
    private final static Logger logger = LoggerFactory.getLogger(GfacServerHandler.class);

    private Registry registry;

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

    public GfacServerHandler() {
        // registering with zk
        try {
            String zkhostPort = AiravataZKUtils.getZKhostPort();
            airavataServerHostPort = ServerSettings.getSetting(Constants.GFAC_SERVER_HOST)
                    + ":" + ServerSettings.getSetting(Constants.GFAC_SERVER_PORT);
            try {
                zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
                gfacServer = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NODE,"/gfac-server");
                gfacExperiments = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE,"/gfac-experiments");
                synchronized(mutex){
                    mutex.wait();  // waiting for the syncConnected event
                }
                storeServerConfig();
                logger.info("Finished starting ZK: " + zk);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
        try {
            publisher = new MonitorPublisher(new EventBus());
            BetterGfacImpl.setMonitorPublisher(publisher);
            registry = RegistryFactory.getDefaultRegistry();
            setGatewayProperties();
            BetterGfacImpl.startDaemonHandlers();
            BetterGfacImpl.startStatusUpdators(registry,zk,publisher);
            inHandlerFutures = new ArrayList<Future>();
        }catch (Exception e){
           logger.error("Error initialising GFAC",e);
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
                    e.printStackTrace();
                } catch (ApplicationSettingsException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
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
        logger.info("GFac Recieved the Experiment: " + experimentId + " TaskId: " + taskId);
        GFac gfac = getGfac();
        InputHandlerWorker inputHandlerWorker = new InputHandlerWorker(gfac, experimentId, taskId, gatewayId);
        inHandlerFutures.add(GFacThreadPoolExecutor.getCachedThreadPool().submit(inputHandlerWorker));
        return true;
    }

    public boolean cancelJob(String experimentId, String taskId) throws TException {
        logger.info("GFac Recieved the Experiment: " + experimentId + " TaskId: " + taskId);
        GFac gfac = getGfac();
        try {
            return gfac.cancel(experimentId, taskId, ServerSettings.getSetting(Constants.GATEWAY_NAME));
        } catch (Exception e) {
            throw new TException("Error launching the experiment : " + e.getMessage(), e);
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
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
         setAiravataUserName(ServerSettings.getSetting("system.user"));
         setGatewayName(ServerSettings.getSetting("system.gateway"));
         setRegistryURL(ServerSettings.getSetting("airavata.server.url"));
     }

    private GFac getGfac()throws TException{
        try {
            return new BetterGfacImpl(registry,zk,publisher);
        } catch (Exception e) {
            throw new TException("Error initializing gfac instance",e);
        }
    }

}
